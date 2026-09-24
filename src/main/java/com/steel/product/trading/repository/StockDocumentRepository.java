package com.steel.product.trading.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.steel.product.trading.dto.StockDocument;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class StockDocumentRepository {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    public StockDocumentRepository(JdbcTemplate jdbc, ObjectMapper mapper) { this.jdbc = jdbc; this.mapper = mapper; }

    private RowMapper<StockDocument> rowMapper() {
        return (rs, i) -> {
            StockDocument d = new StockDocument();
            d.setKind(rs.getString("kind"));
            d.setId(("AUDIT".equals(d.getKind()) ? "AUD-" : "ADJ-") + rs.getLong("document_id"));
            d.setVersion(rs.getInt("version_no")); d.setDate(rs.getDate("document_date").toLocalDate());
            d.setLocationId(rs.getInt("location_id")); d.setLocation(rs.getString("location_name"));
            d.setAuditor(rs.getString("auditor")); d.setConsignment(rs.getString("consignment"));
            try { d.setLines(mapper.readValue(rs.getString("lines_json"), new TypeReference<List<StockDocument.Line>>() {})); }
            catch (Exception e) { throw new IllegalStateException("Unable to read stock document", e); }
            return d;
        };
    }

    private String filters(String kind, String search, Integer locationId, List<Object> args) {
        String where = " WHERE kind = ?";
        args.add(kind);
        if (locationId != null) {
            where += " AND location_id = ?";
            args.add(locationId);
        }
        if (!search.isEmpty()) {
            where += " AND LOWER(CONCAT(CASE WHEN kind = 'AUDIT' THEN 'AUD-' ELSE 'ADJ-' END, " +
                "document_id, ' ', auditor, ' ', location_name, ' ', consignment, ' ', lines_json)) LIKE ? ESCAPE '!'";
            args.add("%" + search.toLowerCase(Locale.ROOT).replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%");
        }
        return where;
    }

    public List<StockDocument> list(String kind, String search, Integer locationId, int page, int size, String sort) {
        List<Object> args = new ArrayList<>();
        String where = filters(kind, search, locationId, args);
        String order = sort.isEmpty() ? "document_id DESC" :
            "document_date " + ("asc".equals(sort) ? "ASC" : "DESC") + ", document_id DESC";
        args.add(size);
        args.add((long) (page - 1) * size);
        return jdbc.query("SELECT * FROM trading_stock_document" + where + " ORDER BY " + order + " LIMIT ? OFFSET ?",
            rowMapper(), args.toArray());
    }
    public long count(String kind, String search, Integer locationId) {
        List<Object> args = new ArrayList<>();
        String where = filters(kind, search, locationId, args);
        return jdbc.queryForObject("SELECT COUNT(*) FROM trading_stock_document" + where, Long.class, args.toArray());
    }
    public StockDocument get(long id, boolean lock) {
        List<StockDocument> rows = jdbc.query("SELECT * FROM trading_stock_document WHERE document_id = ?" +
            (lock ? " FOR UPDATE" : ""), rowMapper(), id);
        if (rows.isEmpty()) throw new IllegalArgumentException("Stock document not found.");
        return rows.get(0);
    }
    public Long beginRequest(String requestId, String hash) {
        jdbc.update("INSERT INTO trading_stock_document_request (request_id, payload_hash) VALUES (?, ?) " +
            "ON DUPLICATE KEY UPDATE request_id = request_id", requestId, hash);
        return jdbc.queryForObject("SELECT * FROM trading_stock_document_request WHERE request_id = ?", (rs, i) -> {
            if (!hash.equals(rs.getString("payload_hash"))) throw new IllegalArgumentException("Request already used with different values. Reload the record.");
            Number id = (Number) rs.getObject("document_id");
            return id == null ? null : id.longValue();
        }, requestId);
    }
    public void finishRequest(String requestId, long id) {
        jdbc.update("UPDATE trading_stock_document_request SET document_id = ? WHERE request_id = ?", id, requestId);
    }
    public long save(StockDocument d, Long id) {
        final String json;
        try { json = mapper.writeValueAsString(d.getLines()); }
        catch (Exception e) { throw new IllegalStateException("Unable to serialize stock document", e); }
        if (id != null) {
            jdbc.update("UPDATE trading_stock_document SET version_no = ?, document_date = ?, auditor = ?, consignment = ?, lines_json = ? WHERE document_id = ?",
                d.getVersion(), java.sql.Date.valueOf(d.getDate()), d.getAuditor(), d.getConsignment(), json, id);
            return id;
        }
        GeneratedKeyHolder key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            java.sql.PreparedStatement statement = connection.prepareStatement("INSERT INTO trading_stock_document " +
                "(kind, version_no, document_date, location_id, location_name, auditor, consignment, lines_json) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                new String[] { "document_id" });
            statement.setString(1, d.getKind()); statement.setInt(2, d.getVersion());
            statement.setDate(3, java.sql.Date.valueOf(d.getDate())); statement.setInt(4, d.getLocationId());
            statement.setString(5, d.getLocation()); statement.setString(6, d.getAuditor());
            statement.setString(7, d.getConsignment()); statement.setString(8, json); return statement;
        }, key);
        return key.getKey().longValue();
    }
    public void linkEffect(long documentId, String requestId) {
        jdbc.update("INSERT INTO trading_stock_document_effect (request_id, document_id) VALUES (?, ?)", requestId, documentId);
    }
    public List<Map<String, Object>> legacyCorrections() {
        return jdbc.queryForList("SELECT c.* FROM trading_stock_correction c WHERE NOT EXISTS " +
            "(SELECT 1 FROM trading_stock_document_effect e WHERE e.request_id = c.request_id) ORDER BY correction_id FOR UPDATE");
    }
}
