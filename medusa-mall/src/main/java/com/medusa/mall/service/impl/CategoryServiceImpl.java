package com.medusa.mall.service.impl;

import com.medusa.mall.domain.Category;
import com.medusa.mall.mapper.CategoryMapper;
import com.medusa.mall.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Category Service implementation
 */
@Service
public class CategoryServiceImpl implements ICategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * Query category by ID
     * 
     * @param id Category ID
     * @return Category
     */
    @Override
    public Category selectCategoryById(Long id) {
        return categoryMapper.selectCategoryById(id);
    }

    /**
     * Query category by code
     * 
     * @param categoryCode Category code
     * @return Category
     */
    @Override
    public Category selectCategoryByCode(String categoryCode) {
        return categoryMapper.selectCategoryByCode(categoryCode);
    }

    /**
     * Query category list
     * 
     * @param category Category filter
     * @return Category list
     */
    @Override
    public List<Category> selectCategoryList(Category category) {
        return categoryMapper.selectCategoryList(category);
    }

    /**
     * Query all active categories
     * 
     * @return Active category list
     */
    @Override
    public List<Category> selectActiveCategories() {
        return categoryMapper.selectActiveCategories();
    }

    /**
     * Insert category
     * 
     * @param category Category
     * @return Result
     */
    @Override
    public int insertCategory(Category category) {
        return categoryMapper.insertCategory(category);
    }

    /**
     * Update category
     * 
     * @param category Category
     * @return Result
     */
    @Override
    public int updateCategory(Category category) {
        return categoryMapper.updateCategory(category);
    }

    /**
     * Delete category by ID
     * 
     * @param id Category ID
     * @return Result
     */
    @Override
    public int deleteCategoryById(Long id) {
        return categoryMapper.deleteCategoryById(id);
    }

    /**
     * Batch delete categories
     * 
     * @param ids Category ID array
     * @return Result
     */
    @Override
    public int deleteCategoryByIds(Long[] ids) {
        return categoryMapper.deleteCategoryByIds(ids);
    }
} 