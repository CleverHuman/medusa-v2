package com.medusa.mall.service;

import com.medusa.mall.domain.Category;
import java.util.List;

/**
 * Category Service interface
 */
public interface ICategoryService {
    /**
     * Query category by ID
     * 
     * @param id Category ID
     * @return Category
     */
    public Category selectCategoryById(Long id);

    /**
     * Query category by code
     * 
     * @param categoryCode Category code
     * @return Category
     */
    public Category selectCategoryByCode(String categoryCode);

    /**
     * Query category list
     * 
     * @param category Category filter
     * @return Category list
     */
    public List<Category> selectCategoryList(Category category);

    /**
     * Query all active categories
     * 
     * @return Active category list
     */
    public List<Category> selectActiveCategories();

    /**
     * Insert category
     * 
     * @param category Category
     * @return Result
     */
    public int insertCategory(Category category);

    /**
     * Update category
     * 
     * @param category Category
     * @return Result
     */
    public int updateCategory(Category category);

    /**
     * Delete category by ID
     * 
     * @param id Category ID
     * @return Result
     */
    public int deleteCategoryById(Long id);

    /**
     * Batch delete categories
     * 
     * @param ids Category ID array
     * @return Result
     */
    public int deleteCategoryByIds(Long[] ids);
} 