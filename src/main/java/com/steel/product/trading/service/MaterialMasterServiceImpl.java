package com.steel.product.trading.service;

import com.steel.product.application.service.AWSS3Service;
import com.steel.product.trading.entity.BrandEntity;
import com.steel.product.trading.entity.CategoryEntity;
import com.steel.product.trading.entity.ItemgradeEntity;
import com.steel.product.trading.entity.ManufacturerEntity;
import com.steel.product.trading.entity.MaterialMasterEntity;
import com.steel.product.trading.entity.SubCategoryEntity;
import com.steel.product.trading.repository.BrandRepository;
import com.steel.product.trading.repository.CategoryRepository;
import com.steel.product.trading.repository.ItemgradeRepository;
import com.steel.product.trading.repository.ManufacturerRepository;
import com.steel.product.trading.repository.MaterialMasterRepository;
import com.steel.product.trading.repository.SubCategoryRepository;
import com.steel.product.trading.request.BrandRequest;
import com.steel.product.trading.request.CategoryRequest;
import com.steel.product.trading.request.DeleteRequest;
import com.steel.product.trading.request.ItemgradeRequest;
import com.steel.product.trading.request.ManufacturerRequest;
import com.steel.product.trading.request.MaterialMasterRequest;
import com.steel.product.trading.request.SearchRequest;
import com.steel.product.trading.request.SubCategoryRequest;

import lombok.extern.log4j.Log4j2;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Log4j2
public class MaterialMasterServiceImpl implements MaterialMasterService {

	@Autowired
	SubCategoryRepository subCategoryRepository;

	@Autowired
	CategoryRepository categoryRepository;
	
	@Autowired
	ItemgradeRepository itemgradeRepository;
	
	@Autowired
	ManufacturerRepository manufacturerRepository;
	
	@Autowired
	BrandRepository brandRepository;
	
	@Autowired
	MaterialMasterRepository materialMasterRepository;

	@Autowired
	AWSS3Service awsS3Service;

	@Value("${templateFilesPath}")
	private String templateFilesPath;
	
	// Material Master Master APIs

	@Override
	public ResponseEntity<Object> save(MaterialMasterRequest materialMasterRequest, MultipartFile itemImage,
			MultipartFile crossSectionalImage) {
		log.info("In save page ");

		ResponseEntity<Object> response = null;
		HttpHeaders header = new HttpHeaders();
		header.set("Content-Type", "application/json");
		String message="Materal details saved successfully..! ";
		try {
			MaterialMasterEntity materialMasterEntity = new MaterialMasterEntity();
			BeanUtils.copyProperties(materialMasterRequest, materialMasterEntity);

			if(materialMasterRequest.getCategoryId()!=null && materialMasterRequest.getCategoryId() >0 ) {
				Optional<CategoryEntity> category = categoryRepository.findByCategoryIdAndIsDeleted(
						materialMasterRequest.getCategoryId(), false);
				if (!category.isPresent()) {
					return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Please select a valid category\"}", header, HttpStatus.BAD_REQUEST);
				}
				materialMasterEntity.setCategoryEntity(category.get());
			}
			if(materialMasterRequest.getSubCategoryId()!=null && materialMasterRequest.getSubCategoryId() >0 ) {
				Optional<SubCategoryEntity> subcategory = subCategoryRepository.findBySubcategoryIdAndIsDeleted(
						materialMasterRequest.getSubCategoryId(), false);
				if (!subcategory.isPresent()
						|| materialMasterRequest.getCategoryId() == null
						|| !materialMasterRequest.getCategoryId().equals(subcategory.get().getCategoryId())) {
					return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Please select a valid subcategory for the selected category\"}", header, HttpStatus.BAD_REQUEST);
				}
				materialMasterEntity.setSubCategoryEntity(subcategory.get());
			}
			if(materialMasterEntity.getItemId()!=null && materialMasterEntity.getItemId()>0) {
				MaterialMasterEntity oldEntity = null;

				Optional<MaterialMasterEntity> kk = materialMasterRepository.findById(materialMasterEntity.getItemId());
				if (kk.isPresent()) {
					oldEntity = kk.get();
				}
				if(oldEntity!=null && oldEntity.getItemId()>0) {
					
					List<MaterialMasterEntity> testItemName = materialMasterRepository.findByItemName(materialMasterEntity.getItemName(), oldEntity.getItemId());
					if(testItemName!=null && testItemName.size()>0) {
						return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Entered Item Name already used\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
					}
					List<MaterialMasterEntity> testItemCode = materialMasterRepository.findByItemCode(materialMasterEntity.getItemCode(), oldEntity.getItemId());
					if(testItemCode!=null && testItemCode.size()>0) {
						return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Entered Item Code already used\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
					}
					materialMasterEntity.setUpdatedBy( materialMasterRequest.getUserId());
					materialMasterEntity.setUpdatedOn(new Date());
					materialMasterEntity.setCreatedBy(oldEntity.getCreatedBy());
					materialMasterEntity.setCreatedOn(oldEntity.getCreatedOn());
					if (!Boolean.TRUE.equals(materialMasterRequest.getRemoveItemImage())) {
						materialMasterEntity.setItemImage(oldEntity.getItemImage());
					}
					if (!Boolean.TRUE.equals(materialMasterRequest.getRemoveCrossSectionalImage())) {
						materialMasterEntity.setCrossSectionalImage(oldEntity.getCrossSectionalImage());
					}
					materialMasterEntity.setIsDeleted(false);
					message="Materal details updated successfully..! ";
				} else {
					return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Please enter valid data\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				List<MaterialMasterEntity> testItemName = materialMasterRepository.findByItemName(materialMasterEntity.getItemName());
				if(testItemName!=null && testItemName.size()>0) {
					return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Entered Item Name already used\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
				}
				List<MaterialMasterEntity> testItemCode = materialMasterRepository.findByItemCode(materialMasterEntity.getItemCode());
				if(testItemCode!=null && testItemCode.size()>0) {
					return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Entered Item Code already used\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
				}
				materialMasterEntity.setCreatedBy(materialMasterRequest.getUserId());
				materialMasterEntity.setCreatedOn(new Date());
				materialMasterEntity.setIsDeleted(false);
			}

			if (itemImage != null) {
				String fileUrl = awsS3Service.persistTradingFiles(templateFilesPath, materialMasterRequest.getItemCode(), itemImage);
				materialMasterEntity.setItemImage(fileUrl);
			}
			if (crossSectionalImage != null) {
				String fileUrl = awsS3Service.persistTradingFiles(templateFilesPath, materialMasterRequest.getItemCode(), crossSectionalImage);
				materialMasterEntity.setCrossSectionalImage(fileUrl);
			}
			materialMasterRepository.save(materialMasterEntity);
			response = new ResponseEntity<>("{\"status\": \"success\", \"message\": \""+message+" \"}",	new HttpHeaders(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			log.info("error is ==" + e.getMessage());
			response = new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Error Occurred\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	@Override
	public Page<MaterialMasterEntity> getMaterialList(SearchRequest searchListPageRequest) {
		log.info("In getMaterialList page ");
		Pageable pageable = PageRequest.of((searchListPageRequest.getPageNo() - 1), searchListPageRequest.getPageSize(), Sort.by("itemId").descending());

		if (searchListPageRequest.getSearchText() != null && searchListPageRequest.getSearchText().length() > 0) {
			Page<MaterialMasterEntity> pageResult = materialMasterRepository.findAllWithSearchText(searchListPageRequest.getSearchText(), pageable);
			pageResult.forEach(this::populateResponseFields);
			return pageResult;
		} else {
			Page<MaterialMasterEntity> pageResult = materialMasterRepository.findAll(pageable);
			pageResult.forEach(this::populateResponseFields);
			return pageResult;
		}
	}

	@Override
	public MaterialMasterEntity findByItemId(Integer itemId) {
		Optional<MaterialMasterEntity> kk = materialMasterRepository.findByItemIdAndIsDeleted(itemId, false);
		MaterialMasterEntity materialMasterEntity = null;
		if (kk.isPresent()) {
			materialMasterEntity = kk.get();
			populateResponseFields(materialMasterEntity);
		}
		return materialMasterEntity;
	}

	private void populateResponseFields(MaterialMasterEntity materialMasterEntity) {
		if (materialMasterEntity.getCreatedOn() != null) {
			materialMasterEntity.setMaterialCreationDate(
					new SimpleDateFormat("dd-MMM-yyyy").format(materialMasterEntity.getCreatedOn()));
		}
		if (materialMasterEntity.getItemImage() != null && !materialMasterEntity.getItemImage().trim().isEmpty()) {
			materialMasterEntity.setItemImagePresignedURL(
					awsS3Service.generatePresignedUrlForTrading(materialMasterEntity.getItemImage()));
		}
		if (materialMasterEntity.getCrossSectionalImage() != null
				&& !materialMasterEntity.getCrossSectionalImage().trim().isEmpty()) {
			materialMasterEntity.setCrossSectionalImagePresignedURL(
					awsS3Service.generatePresignedUrlForTrading(materialMasterEntity.getCrossSectionalImage()));
		}
	}

	@Override
	public ResponseEntity<Object> materialDelete(DeleteRequest deleteRequest) {
		log.info("In materialDelete page ");
		ResponseEntity<Object> response = null;
		HttpHeaders header = new HttpHeaders();
		header.set("Content-Type", "application/json");
		
		try {
			materialMasterRepository.deleteData(deleteRequest.getIds(), deleteRequest.getUserId());
			response = new ResponseEntity<>("{\"status\": \"success\", \"message\": \"Selected material has been deleted successfully..! \"}", new HttpHeaders(), HttpStatus.OK);
		} catch (Exception e) {
			response = new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Error Occurred\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}
	
	// Category Master APIs
	
	@Override
	public ResponseEntity<Object> categorySave(CategoryRequest categoryRequest) {
		log.info("In categorySave page ");
		ResponseEntity<Object> response = null;
		HttpHeaders header = new HttpHeaders();
		header.set("Content-Type", "application/json");
		String message="Category details saved successfully..! ";
		try {
			CategoryEntity categoryEntity = new CategoryEntity();
			BeanUtils.copyProperties(categoryRequest, categoryEntity);
			categoryEntity.setIsDeleted(false);
			if (categoryEntity.getCategoryId() != null && categoryEntity.getCategoryId() > 0) {
				CategoryEntity oldEntity = null;

				Optional<CategoryEntity> kk = categoryRepository.findById(categoryEntity.getCategoryId());
				if (kk.isPresent()) {
					oldEntity = kk.get();
				}
				if(oldEntity!=null && oldEntity.getCategoryId()>0) {
					
					List<CategoryEntity> testItemName = categoryRepository.findByCategoryName(categoryEntity.getCategoryName(), oldEntity.getCategoryId());
					if(testItemName!=null && testItemName.size()>0) {
						return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Entered Category Name already used\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
					}
					categoryEntity.setUpdatedBy( categoryRequest.getUserId());
					categoryEntity.setUpdatedOn(new Date());
					categoryEntity.setCreatedBy(oldEntity.getCreatedBy());
					categoryEntity.setCreatedOn(oldEntity.getCreatedOn());
					message="Category details updated successfully..! ";
				} else {
					return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Please enter valid data\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				List<CategoryEntity> testItemName = categoryRepository.findByCategoryName(categoryEntity.getCategoryName());
				if(testItemName!=null && testItemName.size()>0) {
					return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Entered Category Name already used\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
				}
				categoryEntity.setCreatedBy(categoryRequest.getUserId());
				categoryEntity.setCreatedOn(new Date());
			}
			categoryRepository.save(categoryEntity);
			response = new ResponseEntity<>("{\"status\": \"success\", \"message\": \""+message+" \"}",	new HttpHeaders(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			log.info("error is ==" + e.getMessage());
			response = new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Error Occurred\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	@Override
	public Page<CategoryEntity> getCategoryList(SearchRequest searchListPageRequest) {
		log.info("In getCategoryList page ");
		Pageable pageable = PageRequest.of((searchListPageRequest.getPageNo() - 1), searchListPageRequest.getPageSize(), Sort.by("categoryId").descending());

		if (searchListPageRequest.getSearchText() != null && searchListPageRequest.getSearchText().length() > 0) {
			Page<CategoryEntity> pageResult = categoryRepository.findAllWithSearchText(searchListPageRequest.getSearchText(), pageable);
			return pageResult;
		} else {
			Page<CategoryEntity> pageResult = categoryRepository.findAll(pageable);
			return pageResult;
		}
	}

	@Override
	public List<SubCategoryEntity> getSubCategoriesByCategoryIds(List<Integer> categoryIds) {
		if (categoryIds == null || categoryIds.isEmpty()) {
			return Collections.emptyList();
		}
		return subCategoryRepository.findActiveByCategoryIds(categoryIds);
	}

	@Override
	public CategoryEntity findByCategoryId(Integer id) {
		log.info("In findByCategoryId page ");
		Optional<CategoryEntity> kk = categoryRepository.findByCategoryIdAndIsDeleted(id, false);
		CategoryEntity categoryEntity = null;
		if (kk.isPresent()) {
			categoryEntity = kk.get();
		}
		return categoryEntity;
	}

	@Override
	public ResponseEntity<Object> categoryDelete(DeleteRequest deleteRequest) {
		log.info("In categoryDelete page ");
		ResponseEntity<Object> response = null;
		HttpHeaders header = new HttpHeaders();
		header.set("Content-Type", "application/json");
		
		try {
			categoryRepository.deleteData(deleteRequest.getIds(), deleteRequest.getUserId());
			response = new ResponseEntity<>("{\"status\": \"success\", \"message\": \"Selected category has been deleted successfully..! \"}", new HttpHeaders(), HttpStatus.OK);
		} catch (Exception e) {
			response = new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Error Occurred\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}
	
	// Sub-Category Master APIs

	@Override
	public ResponseEntity<Object> subcategorySave(SubCategoryRequest categoryRequest) {
		log.info("In subcategorySave page ");
		ResponseEntity<Object> response = null;
		HttpHeaders header = new HttpHeaders();
		header.set("Content-Type", "application/json");
		String message="Sub-Category details saved successfully..! ";
		try {
			SubCategoryEntity subcategoryEntity = new SubCategoryEntity();
			BeanUtils.copyProperties(categoryRequest, subcategoryEntity);
			subcategoryEntity.setIsDeleted(false);
			if (subcategoryEntity.getSubcategoryId() != null && subcategoryEntity.getSubcategoryId() > 0) {
				SubCategoryEntity oldEntity = null;

				Optional<SubCategoryEntity> kk = subCategoryRepository.findById(subcategoryEntity.getSubcategoryId());
				if (kk.isPresent()) {
					oldEntity = kk.get();
				}
				if(oldEntity!=null && oldEntity.getSubcategoryId()>0) {
					
					List<SubCategoryEntity> testItemName = subCategoryRepository.findBySubCategoryNameforUpdate(subcategoryEntity.getSubcategoryName(), subcategoryEntity.getCategoryId(), oldEntity.getSubcategoryId());
					if(testItemName!=null && testItemName.size()>0) {
						return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Entered Sub-Category Name already used\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
					}
					subcategoryEntity.setUpdatedBy( categoryRequest.getUserId());
					subcategoryEntity.setUpdatedOn(new Date());
					subcategoryEntity.setCreatedBy(oldEntity.getCreatedBy());
					subcategoryEntity.setCreatedOn(oldEntity.getCreatedOn());
					message="Sub-Category details updated successfully..! ";
				} else {
					return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Please enter valid data\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				List<SubCategoryEntity> testItemName = subCategoryRepository.findBySubCategoryNameforInsert(subcategoryEntity.getSubcategoryName(), subcategoryEntity.getCategoryId());
				if(testItemName!=null && testItemName.size()>0) {
					return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Entered Sub-Category Name already used\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
				}
				subcategoryEntity.setCreatedBy(categoryRequest.getUserId());
				subcategoryEntity.setCreatedOn(new Date());
			}
			subCategoryRepository.save(subcategoryEntity);
			response = new ResponseEntity<>("{\"status\": \"success\", \"message\": \""+message+" \"}",	new HttpHeaders(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			log.info("error is ==" + e.getMessage());
			response = new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Error Occurred\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	@Override
	public Page<Object[]> getSubCategoryList(SearchRequest searchListPageRequest) {
		log.info("In getSubCategoryList page ");
		Pageable pageable = PageRequest.of((searchListPageRequest.getPageNo() - 1), searchListPageRequest.getPageSize());
		Page<Object[]> pageResult = subCategoryRepository.findAllWithSearchText(searchListPageRequest.getSearchText(), pageable);
		return pageResult;
	}

	@Override
	public SubCategoryEntity findBySubCategoryId(Integer id) {
		log.info("In findBySubCategoryId page ");
		Optional<SubCategoryEntity> kk = subCategoryRepository.findBySubcategoryIdAndIsDeleted(id, false);
		SubCategoryEntity categoryEntity = null;
		if (kk.isPresent()) {
			categoryEntity = kk.get();
		}
		return categoryEntity;
	}

	@Override
	public ResponseEntity<Object> subcategoryDelete(DeleteRequest deleteRequest) {
		log.info("In subcategoryDelete page ");
		ResponseEntity<Object> response = null;
		HttpHeaders header = new HttpHeaders();
		header.set("Content-Type", "application/json");
		
		try {
			subCategoryRepository.deleteData(deleteRequest.getIds(), deleteRequest.getUserId());
			response = new ResponseEntity<>("{\"status\": \"success\", \"message\": \"Selected Sub-category has been deleted successfully..! \"}", new HttpHeaders(), HttpStatus.OK);
		} catch (Exception e) {
			response = new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Error Occurred\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}
	
	// Manufacturer Master APIs
	
	@Override
	public ResponseEntity<Object> manufacturerSave(ManufacturerRequest manufacturerRequest) {
		log.info("In manufacturerSave page ");
		ResponseEntity<Object> response = null;
		HttpHeaders header = new HttpHeaders();
		header.set("Content-Type", "application/json");
		String message="Manufacturer details saved successfully..! ";
		try {
			ManufacturerEntity manufacturerEntity = new ManufacturerEntity();
			BeanUtils.copyProperties(manufacturerRequest, manufacturerEntity);
			manufacturerEntity.setIsDeleted(false);
			if (manufacturerEntity.getManufacturerId() != null && manufacturerEntity.getManufacturerId() > 0) {
				ManufacturerEntity oldEntity = null;

				Optional<ManufacturerEntity> kk = manufacturerRepository.findById(manufacturerEntity.getManufacturerId());
				if (kk.isPresent()) {
					oldEntity = kk.get();
				}
				if(oldEntity!=null && oldEntity.getManufacturerId()>0) {
					List<ManufacturerEntity> testItemName = manufacturerRepository.findByManufacturerName(manufacturerEntity.getManufacturerName(), oldEntity.getManufacturerId());
					if(testItemName!=null && testItemName.size()>0) {
						return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Entered Manufacturer Name already used\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
					}
					manufacturerEntity.setUpdatedBy( manufacturerRequest.getUserId());
					manufacturerEntity.setUpdatedOn(new Date());
					manufacturerEntity.setCreatedBy(oldEntity.getCreatedBy());
					manufacturerEntity.setCreatedOn(oldEntity.getCreatedOn());
					message="Manufacturer details updated successfully..! ";
				} else {
					return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Please enter valid data\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				List<ManufacturerEntity> testItemName = manufacturerRepository.findByManufacturerName(manufacturerEntity.getManufacturerName());
				if(testItemName!=null && testItemName.size()>0) {
					return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Entered Manufacturer Name already used\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
				}
				manufacturerEntity.setCreatedBy(manufacturerRequest.getUserId());
				manufacturerEntity.setCreatedOn(new Date());
			}
			manufacturerRepository.save(manufacturerEntity);
			response = new ResponseEntity<>("{\"status\": \"success\", \"message\": \""+message+" \"}",	new HttpHeaders(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			log.info("error is ==" + e.getMessage());
			response = new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Error Occurred\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	@Override
	public Page<ManufacturerEntity> getManufacturerList(SearchRequest searchListPageRequest) {
		log.info("In getManufacturerList page ");
		Pageable pageable = PageRequest.of((searchListPageRequest.getPageNo() - 1), searchListPageRequest.getPageSize(), Sort.by("manufacturerId").descending());

		if (searchListPageRequest.getSearchText() != null && searchListPageRequest.getSearchText().length() > 0) {
			Page<ManufacturerEntity> pageResult = manufacturerRepository.findAllWithSearchText(searchListPageRequest.getSearchText(), pageable);
			return pageResult;
		} else {
			Page<ManufacturerEntity> pageResult = manufacturerRepository.findAll(pageable);
			return pageResult;
		}
	}

	@Override
	public ManufacturerEntity findByManufacturerId(Integer id) {
		log.info("In findByManufacturerId page ");
		Optional<ManufacturerEntity> kk = manufacturerRepository.findByManufacturerIdAndIsDeleted(id, false);
		ManufacturerEntity categoryEntity = null;
		if (kk.isPresent()) {
			categoryEntity = kk.get();
		}
		return categoryEntity;
	}

	@Override
	public ResponseEntity<Object> manufacturerDelete(DeleteRequest deleteRequest) {
		log.info("In manufacturerDelete page ");
		ResponseEntity<Object> response = null;
		HttpHeaders header = new HttpHeaders();
		header.set("Content-Type", "application/json");
		
		try {
			manufacturerRepository.deleteData(deleteRequest.getIds(), deleteRequest.getUserId());
			response = new ResponseEntity<>("{\"status\": \"success\", \"message\": \"Selected Manufacturer has been deleted successfully..! \"}", new HttpHeaders(), HttpStatus.OK);
		} catch (Exception e) {
			response = new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Error Occurred\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	// Brand Master APIs
	
	@Override
	public ResponseEntity<Object> brandSave(BrandRequest brandRequest) {
		log.info("In brandSave page ");
		ResponseEntity<Object> response = null;
		HttpHeaders header = new HttpHeaders();
		header.set("Content-Type", "application/json");
		String message="Brand details saved successfully..! ";
		try {
			BrandEntity brandEntity = new BrandEntity();
			BeanUtils.copyProperties(brandRequest, brandEntity);
			brandEntity.setIsDeleted(false);
			if (brandEntity.getBrandId() != null && brandEntity.getBrandId() > 0) {
				BrandEntity oldEntity = null;

				Optional<BrandEntity> kk = brandRepository.findById(brandEntity.getBrandId());
				if (kk.isPresent()) {
					oldEntity = kk.get();
				}
				if(oldEntity!=null && oldEntity.getBrandId()>0) {
					List<BrandEntity> testItemName = brandRepository.findByBrandName(brandEntity.getBrandName(), oldEntity.getBrandId());
					if(testItemName!=null && testItemName.size()>0) {
						return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Entered Brand Name already used\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
					}
					brandEntity.setUpdatedBy( brandRequest.getUserId());
					brandEntity.setUpdatedOn(new Date());
					brandEntity.setCreatedBy(oldEntity.getCreatedBy());
					brandEntity.setCreatedOn(oldEntity.getCreatedOn());
					message="Brand details updated successfully..! ";
				} else {
					return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Please enter valid data\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				List<BrandEntity> testItemName = brandRepository.findByBrandName(brandEntity.getBrandName());
				if(testItemName!=null && testItemName.size()>0) {
					return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Entered Brand Name already used\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
				}
				brandEntity.setCreatedBy(brandRequest.getUserId());
				brandEntity.setCreatedOn(new Date());
			}
			brandRepository.save(brandEntity);
			response = new ResponseEntity<>("{\"status\": \"success\", \"message\": \""+message+" \"}",	new HttpHeaders(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			log.info("error is ==" + e.getMessage());
			response = new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Error Occurred\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	@Override
	public Page<BrandEntity> getBrandList(SearchRequest searchListPageRequest) {
		log.info("In getBrandList page ");
		Pageable pageable = PageRequest.of((searchListPageRequest.getPageNo() - 1), searchListPageRequest.getPageSize(), Sort.by("brandId").descending());

		if (searchListPageRequest.getSearchText() != null && searchListPageRequest.getSearchText().length() > 0) {
			Page<BrandEntity> pageResult = brandRepository.findAllWithSearchText(searchListPageRequest.getSearchText(), pageable);
			return pageResult;
		} else {
			Page<BrandEntity> pageResult = brandRepository.findAll(pageable);
			return pageResult;
		}
	}

	@Override
	public BrandEntity findByBrandId(Integer id) {
		log.info("In findByBrandId page ");
		Optional<BrandEntity> kk = brandRepository.findByBrandIdAndIsDeleted(id, false);
		BrandEntity brandEntity = null;
		if (kk.isPresent()) {
			brandEntity = kk.get();
		}
		return brandEntity;
	}

	@Override
	public ResponseEntity<Object> brandDelete(DeleteRequest deleteRequest) {
		log.info("In brandDelete page ");
		ResponseEntity<Object> response = null;
		HttpHeaders header = new HttpHeaders();
		header.set("Content-Type", "application/json");
		
		try {
			brandRepository.deleteData(deleteRequest.getIds(), deleteRequest.getUserId());
			response = new ResponseEntity<>("{\"status\": \"success\", \"message\": \"Selected Brand has been deleted successfully..! \"}", new HttpHeaders(), HttpStatus.OK);
		} catch (Exception e) {
			response = new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Error Occurred\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}
	
	

	// itemgrade Master APIs
	
	@Override
	public ResponseEntity<Object> itemgradeSave(ItemgradeRequest itemgradeRequest) {
		log.info("In itemgradeSave page ");
		ResponseEntity<Object> response = null;
		HttpHeaders header = new HttpHeaders();
		header.set("Content-Type", "application/json");
		String message="ItemGrade details saved successfully..! ";
		try {
			ItemgradeEntity itemgradeEntity = new ItemgradeEntity();
			BeanUtils.copyProperties(itemgradeRequest, itemgradeEntity);
			itemgradeEntity.setIsDeleted(false);
			if (itemgradeEntity.getItemgradeId() != null && itemgradeEntity.getItemgradeId() > 0) {
				ItemgradeEntity oldEntity = null;

				Optional<ItemgradeEntity> kk = itemgradeRepository.findById(itemgradeEntity.getItemgradeId());
				if (kk.isPresent()) {
					oldEntity = kk.get();
				}
				if(oldEntity!=null && oldEntity.getItemgradeId()>0) {
					List<ItemgradeEntity> testItemName = itemgradeRepository.findByItemgradeName(itemgradeEntity.getItemgradeName(), oldEntity.getItemgradeId());
					if(testItemName!=null && testItemName.size()>0) {
						return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Entered ItemGrade already used\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
					}
					itemgradeEntity.setUpdatedBy( itemgradeRequest.getUserId());
					itemgradeEntity.setUpdatedOn(new Date());
					itemgradeEntity.setCreatedBy(oldEntity.getCreatedBy());
					itemgradeEntity.setCreatedOn(oldEntity.getCreatedOn());
					message="ItemGrade details updated successfully..! ";
				} else {
					return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Please enter valid data\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				List<ItemgradeEntity> testItemName = itemgradeRepository.findByItemgradeName(itemgradeEntity.getItemgradeName());
				if(testItemName!=null && testItemName.size()>0) {
					return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Entered ItemGrade already used\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
				}
				itemgradeEntity.setCreatedBy(itemgradeRequest.getUserId());
				itemgradeEntity.setCreatedOn(new Date());
			}
			itemgradeRepository.save(itemgradeEntity);
			response = new ResponseEntity<>("{\"status\": \"success\", \"message\": \""+message+" \"}",	new HttpHeaders(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			log.info("error is ==" + e.getMessage());
			response = new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Error Occurred\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	@Override
	public Page<ItemgradeEntity> getItemgradeList(SearchRequest searchListPageRequest) {
		log.info("In getItemgradeList page ");
		Pageable pageable = PageRequest.of((searchListPageRequest.getPageNo() - 1), searchListPageRequest.getPageSize(), Sort.by("itemgradeId").descending());

		if (searchListPageRequest.getSearchText() != null && searchListPageRequest.getSearchText().length() > 0) {
			Page<ItemgradeEntity> pageResult = itemgradeRepository.findAllWithSearchText(searchListPageRequest.getSearchText(), pageable);
			return pageResult;
		} else {
			Page<ItemgradeEntity> pageResult = itemgradeRepository.findAll(pageable);
			return pageResult;
		}
	}

	@Override
	public ItemgradeEntity findByItemgradeId(Integer id) {
		log.info("In findByBrandId page ");
		Optional<ItemgradeEntity> kk = itemgradeRepository.findByItemgradeIdAndIsDeleted(id, false);
		ItemgradeEntity brandEntity = null;
		if (kk.isPresent()) {
			brandEntity = kk.get();
		}
		return brandEntity;
	}

	@Override
	public ResponseEntity<Object> itemgradeDelete(DeleteRequest deleteRequest) {
		log.info("In itemgradeDelete page ");
		ResponseEntity<Object> response = null;
		HttpHeaders header = new HttpHeaders();
		header.set("Content-Type", "application/json");
		
		try {
			itemgradeRepository.deleteData(deleteRequest.getIds(), deleteRequest.getUserId());
			response = new ResponseEntity<>("{\"status\": \"success\", \"message\": \"Selected ItemGrade has been deleted successfully..! \"}", new HttpHeaders(), HttpStatus.OK);
		} catch (Exception e) {
			response = new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Error Occurred\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

}
