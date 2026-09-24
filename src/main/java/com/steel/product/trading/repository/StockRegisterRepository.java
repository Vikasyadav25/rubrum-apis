package com.steel.product.trading.repository;

import com.steel.product.trading.dto.StockRegisterResponse;
import com.steel.product.trading.dto.StockRegisterResponse.Row;
import com.steel.product.trading.request.OpeningStockRequest;
import com.steel.product.trading.request.StockCorrectionRequest;
import java.util.*;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class StockRegisterRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public StockRegisterRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Inward net_weight and rate are KG and INR/KG, independently of the unit-volume field.
    private static final String RECEIPTS =
        "SELECT CONCAT('INW-', i.itemchild_id) id, i.item_id, m.item_name name, m.item_code code, " +
        "COALESCE(h.vendor_batch_no, '') heat, m.manufacturer_name manufacturer, c.category_name category, " +
        "h.location_id, l.location_name location, h.created_on entry_date, h.vehicle_no vehicle, " +
        "COALESCE(i.net_weight, 0) / 1000.0 incoming, COALESCE(i.actual_noof_pieces, 0) pieces, " +
        "COALESCE(i.rate, 0) * 1000 rate " +
        "FROM trading_inward_items i JOIN trading_inward h ON h.inward_id = i.inwardid " +
        "LEFT JOIN trading_material_master m ON m.item_id = i.item_id " +
        "LEFT JOIN trading_category_master c ON c.category_id = m.category_id " +
        "LEFT JOIN trading_location_master l ON l.location_id = h.location_id " +
        "WHERE COALESCE(i.is_deleted, 0) = 0 AND COALESCE(h.is_deleted, 0) = 0 " +
        "AND UPPER(h.status) = 'COMPLETED' " +
        "UNION ALL SELECT CONCAT('OPEN-', o.stock_id), NULL, o.material_name, o.material_code, " +
        "o.heat_no, o.manufacturer, o.category, o.location_id, l.location_name, o.created_on, '', " +
        "o.weight, o.pieces, o.rate FROM trading_opening_stock o " +
        "LEFT JOIN trading_location_master l ON l.location_id = o.location_id";

    // Dispatch has no receipt/heat foreign key. Do not invent a lot deduction or a zero outflow.
    private static final String REGISTER = "SELECT r.*, " +
        "r.incoming + COALESCE(a.added, 0) total_incoming, COALESCE(a.deducted, 0) outgoing, " +
        "r.incoming + COALESCE(a.added, 0) - COALESCE(a.deducted, 0) balance, CASE WHEN EXISTS (" +
        "SELECT 1 FROM trading_eqp_items e JOIN trading_eqp q ON q.enquiry_id = e.enquiryid " +
        "JOIN trading_delivery_chalan d ON d.enquiryid = q.enquiry_id " +
        "LEFT JOIN trading_material_master dm ON dm.item_id = e.item_id " +
        "WHERE COALESCE(e.is_deleted, 0) = 0 AND COALESCE(q.is_deleted, 0) = 0 " +
        "AND COALESCE(d.is_deleted, 0) = 0 AND UPPER(e.status) = 'DC' " +
        "AND (e.item_id = r.item_id OR (r.item_id IS NULL AND dm.item_code = r.code)) " +
        "AND (COALESCE(d.dispatch_from_id, e.location_id) IS NULL OR " +
        "COALESCE(d.dispatch_from_id, e.location_id) = r.location_id)" +
        ") THEN 1 ELSE 0 END allocation_pending FROM (" + RECEIPTS + ") r " +
        "LEFT JOIN (SELECT stock_id, SUM(add_kg) / 1000.0 added, SUM(deduct_kg) / 1000.0 deducted " +
        "FROM trading_stock_correction GROUP BY stock_id) a ON a.stock_id = r.id";

    private static final RowMapper<Row> MAPPER = (rs, index) -> {
        Row row = new Row();
        row.setId(rs.getString("id"));
        row.setItemId((Integer) rs.getObject("item_id"));
        row.setName(value(rs.getString("name")));
        row.setCode(value(rs.getString("code")));
        row.setHeat(value(rs.getString("heat")));
        row.setManufacturer(value(rs.getString("manufacturer")));
        row.setCategory(value(rs.getString("category")));
        row.setLocationId((Integer) rs.getObject("location_id"));
        row.setLocation(value(rs.getString("location")));
        java.sql.Date date = rs.getDate("entry_date");
        row.setDate(date == null ? "" : date.toLocalDate().toString());
        row.setVehicle(value(rs.getString("vehicle")));
        row.setIncoming(rs.getBigDecimal("total_incoming"));
        row.setPieces(rs.getInt("pieces"));
        row.setRate(rs.getBigDecimal("rate"));
        row.setAllocationPending(rs.getBoolean("allocation_pending"));
        if (!row.isAllocationPending()) {
            row.setOutgoing(rs.getBigDecimal("outgoing"));
            row.setWeight(rs.getBigDecimal("balance"));
        }
        return row;
    };

    private static String value(String value) { return value == null ? "" : value; }

    public StockRegisterResponse list(String search, Integer locationId, int page, int size, String sort) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String where = " WHERE 1 = 1";
        if (locationId != null) {
            where += " AND s.location_id = :locationId";
            params.addValue("locationId", locationId);
        }
        if (!search.isEmpty()) {
            where += " AND LOWER(CONCAT(COALESCE(name,''), ' ', COALESCE(code,''), ' ', heat, ' ', " +
                "COALESCE(manufacturer,''), ' ', COALESCE(category,''), ' ', COALESCE(location,''), ' ', " +
                "COALESCE(vehicle,''))) LIKE :search ESCAPE '!'";
            params.addValue("search", "%" + search.toLowerCase(Locale.ROOT)
                .replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%");
        }
        String from = " FROM (" + REGISTER + ") s";
        StockRegisterResponse response = new StockRegisterResponse();
        long count = jdbc.queryForObject("SELECT COUNT(*)" + from + where, params, Long.class);
        response.setTotalItems(count);
        response.setTotalPages((int) Math.ceil((double) count / size));
        int currentPage = Math.min(page, Math.max(1, response.getTotalPages()));
        response.setCurrentPage(currentPage);
        String order = sort.isEmpty() ? "entry_date DESC, id DESC" :
            "allocation_pending ASC, balance " + (sort.equals("asc") ? "ASC" : "DESC") + ", id DESC";
        params.addValue("limit", size).addValue("offset", (currentPage - 1) * size);
        response.setContent(jdbc.query("SELECT *" + from + where + " ORDER BY " + order +
            " LIMIT :limit OFFSET :offset", params, MAPPER));
        response.setSummary(jdbc.queryForObject("SELECT COUNT(*) total_items, " +
            "COALESCE(SUM(CASE WHEN allocation_pending = 0 AND balance > 0 AND balance < 5 THEN 1 ELSE 0 END), 0) low_stock, " +
            "COALESCE(SUM(CASE WHEN allocation_pending = 0 AND balance = 0 THEN 1 ELSE 0 END), 0) out_of_stock, " +
            "COALESCE(SUM(allocation_pending), 0) pending, COALESCE(SUM(balance * rate), 0) valuation" + from,
            new MapSqlParameterSource(), (rs, index) -> {
                StockRegisterResponse.Summary summary = new StockRegisterResponse.Summary();
                summary.setTotalItems(rs.getLong("total_items"));
                summary.setLowStock(rs.getLong("low_stock"));
                summary.setOutOfStock(rs.getLong("out_of_stock"));
                summary.setAllocationPending(rs.getLong("pending"));
                summary.setValuation(summary.getAllocationPending() > 0 ? null : rs.getBigDecimal("valuation"));
                return summary;
            }));
        return response;
    }

    public List<Map<String, Object>> locations() {
        return jdbc.query("SELECT location_id, location_name FROM trading_location_master " +
            "WHERE COALESCE(is_deleted, 0) = 0 ORDER BY location_name, location_id", (rs, i) -> {
                Map<String, Object> location = new LinkedHashMap<>();
                location.put("id", rs.getInt("location_id"));
                location.put("name", rs.getString("location_name"));
                return location;
            });
    }

    public boolean locationExists(Integer id) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM trading_location_master " +
            "WHERE location_id = :id AND COALESCE(is_deleted, 0) = 0",
            new MapSqlParameterSource("id", id), Integer.class) > 0;
    }

    public String saveOpening(OpeningStockRequest request) {
        MapSqlParameterSource p = new MapSqlParameterSource()
            .addValue("requestId", request.getRequestId()).addValue("name", request.getName())
            .addValue("code", request.getCode()).addValue("heat", request.getHeat())
            .addValue("manufacturer", request.getManufacturer()).addValue("category", request.getCategory())
            .addValue("locationId", request.getLocationId()).addValue("weight", request.getWeight())
            .addValue("pieces", request.getPieces()).addValue("rate", request.getRate());
        // Retrying the same request after a network timeout must not double the inventory.
        jdbc.update("INSERT INTO trading_opening_stock (request_id, material_name, material_code, heat_no, " +
            "manufacturer, category, location_id, weight, pieces, rate) VALUES (:requestId, :name, :code, " +
            ":heat, :manufacturer, :category, :locationId, :weight, :pieces, :rate) " +
            "ON DUPLICATE KEY UPDATE request_id = request_id", p);
        Boolean same = jdbc.queryForObject("SELECT * FROM trading_opening_stock WHERE request_id = :requestId", p,
            (rs, index) -> request.getName().equals(rs.getString("material_name")) &&
                request.getCode().equals(rs.getString("material_code")) && request.getHeat().equals(rs.getString("heat_no")) &&
                request.getManufacturer().equals(rs.getString("manufacturer")) && request.getCategory().equals(rs.getString("category")) &&
                request.getLocationId() == rs.getInt("location_id") && request.getPieces() == rs.getInt("pieces") &&
                request.getWeight().compareTo(rs.getBigDecimal("weight")) == 0 && request.getRate().compareTo(rs.getBigDecimal("rate")) == 0);
        if (!Boolean.TRUE.equals(same)) {
            throw new IllegalArgumentException("This request was already saved with different values. Refresh the register before adding another entry.");
        }
        return jdbc.queryForObject("SELECT CONCAT('OPEN-', stock_id) FROM trading_opening_stock " +
            "WHERE request_id = :requestId", p, String.class);
    }

    public boolean correctionExists(StockCorrectionRequest request) {
        List<Boolean> matches = jdbc.query("SELECT * FROM trading_stock_correction WHERE request_id = :id",
            new MapSqlParameterSource("id", request.getRequestId()), (rs, index) ->
                request.getStockId().equals(rs.getString("stock_id")) && request.getDate().equals(rs.getDate("correction_date").toLocalDate()) &&
                request.getAuditor().equals(rs.getString("auditor")) && request.getConsignment().equals(rs.getString("consignment")) &&
                request.getRemarks().equals(rs.getString("remarks")) && request.getBefore().compareTo(rs.getBigDecimal("before_weight")) == 0 &&
                request.getVerified().compareTo(rs.getBigDecimal("verified_kg")) == 0 && request.getAdd().compareTo(rs.getBigDecimal("add_kg")) == 0 &&
                request.getDeduct().compareTo(rs.getBigDecimal("deduct_kg")) == 0);
        if (!matches.isEmpty() && !matches.get(0)) {
            throw new IllegalArgumentException("This adjustment request was already saved with different values. Refresh the register.");
        }
        return !matches.isEmpty();
    }

    public Row lockStock(String stockId) {
        boolean opening = stockId.startsWith("OPEN-");
        String id = stockId.substring(stockId.indexOf('-') + 1);
        // Only validated prefixes reach here; table and column names never come from request text.
        String table = opening ? "trading_opening_stock" : "trading_inward_items";
        String column = opening ? "stock_id" : "itemchild_id";
        jdbc.queryForList("SELECT " + column + " FROM " + table + " WHERE " + column + " = :id FOR UPDATE",
            new MapSqlParameterSource("id", Long.parseLong(id)));
        return findStock(stockId);
    }

    public Row findStock(String stockId) {
        List<Row> rows = jdbc.query("SELECT * FROM (" + REGISTER + ") s WHERE id = :id",
            new MapSqlParameterSource("id", stockId), MAPPER);
        if (rows.isEmpty()) throw new IllegalArgumentException("Stock entry is unavailable.");
        return rows.get(0);
    }

    public void saveCorrection(StockCorrectionRequest request) {
        jdbc.update("INSERT INTO trading_stock_correction (request_id, stock_id, correction_date, auditor, " +
            "consignment, before_weight, verified_kg, add_kg, deduct_kg, remarks) VALUES " +
            "(:requestId, :stockId, :date, :auditor, :consignment, :before, :verified, :add, :deduct, :remarks)",
            new MapSqlParameterSource().addValue("requestId", request.getRequestId())
                .addValue("stockId", request.getStockId()).addValue("date", java.sql.Date.valueOf(request.getDate()))
                .addValue("auditor", request.getAuditor()).addValue("consignment", request.getConsignment())
                .addValue("before", request.getBefore()).addValue("verified", request.getVerified())
                .addValue("add", request.getAdd()).addValue("deduct", request.getDeduct())
                .addValue("remarks", request.getRemarks()));
    }
}
