package com.steel.product.trading.service;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.steel.product.trading.entity.BrandEntity;
import com.steel.product.trading.entity.CategoryEntity;
import com.steel.product.trading.entity.ItemgradeEntity;
import com.steel.product.trading.entity.ManufacturerEntity;
import com.steel.product.trading.entity.MaterialMasterEntity;
import com.steel.product.trading.entity.SubCategoryEntity;
import com.steel.product.trading.request.BrandRequest;
import com.steel.product.trading.request.CategoryRequest;
import com.steel.product.trading.request.DeleteRequest;
import com.steel.product.trading.request.ItemgradeRequest;
import com.steel.product.trading.request.ManufacturerRequest;
import com.steel.product.trading.request.MaterialMasterRequest;
import com.steel.product.trading.request.SearchRequest;
import com.steel.product.trading.request.SubCategoryRequest;

import java.util.List;

public interface MaterialMasterService {

	ResponseEntity<Object> save(MaterialMasterRequest materialRequest, MultipartFile itemImage,
			MultipartFile crossSectionalImage);

	Page<MaterialMasterEntity> getMaterialList(SearchRequest searchListPageRequest);

	MaterialMasterEntity findByItemId(Integer itemId);

	ResponseEntity<Object> categorySave(CategoryRequest categoryRequest);

	CategoryEntity findByCategoryId(Integer id);

	Page<CategoryEntity> getCategoryList(SearchRequest searchListPageRequest);

	List<SubCategoryEntity> getSubCategoriesByCategoryIds(List<Integer> categoryIds);

	ResponseEntity<Object> materialDelete(DeleteRequest deleteRequest);

	ResponseEntity<Object> categoryDelete(DeleteRequest deleteRequest);

	ResponseEntity<Object> subcategorySave(SubCategoryRequest categoryRequest);

	Page<Object[]> getSubCategoryList(SearchRequest searchListPageRequest);

	SubCategoryEntity findBySubCategoryId(Integer id);

	ResponseEntity<Object> subcategoryDelete(DeleteRequest deleteRequest);

	ResponseEntity<Object> manufacturerSave(ManufacturerRequest categoryRequest);

	Page<ManufacturerEntity> getManufacturerList(SearchRequest searchPageRequest);

	ManufacturerEntity findByManufacturerId(Integer id);

	ResponseEntity<Object> manufacturerDelete(DeleteRequest deleteRequest);

	ResponseEntity<Object> brandSave(BrandRequest brandRequest);

	BrandEntity findByBrandId(Integer id);

	Page<BrandEntity> getBrandList(SearchRequest searchPageRequest);

	ResponseEntity<Object> brandDelete(DeleteRequest deleteRequest);

	ResponseEntity<Object> itemgradeSave(ItemgradeRequest itemgradeRequest);

	Page<ItemgradeEntity> getItemgradeList(SearchRequest searchPageRequest);

	ItemgradeEntity findByItemgradeId(Integer id);

	ResponseEntity<Object> itemgradeDelete(DeleteRequest deleteRequest);

}
