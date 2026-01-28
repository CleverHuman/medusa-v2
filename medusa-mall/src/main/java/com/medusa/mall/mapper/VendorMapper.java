package com.medusa.mall.mapper;

import java.util.List;
import com.medusa.mall.domain.vendor.Vendor;

/**
 * Vendor Mapper Interface
 */
public interface VendorMapper {
    
    /**
     * Query vendor list
     * 
     * @param vendor Vendor query params
     * @return Vendor collection
     */
    List<Vendor> selectVendorList(Vendor vendor);

    /**
     * Query vendor by ID
     * 
     * @param id Vendor ID
     * @return Vendor
     */
    Vendor selectVendorById(Long id);

    /**
     * Query vendor by vendor code
     * 
     * @param vendorCode Vendor code
     * @return Vendor
     */
    Vendor selectByVendorCode(String vendorCode);

    /**
     * Query vendor by application ID
     * 
     * @param applicationId Application ID
     * @return Vendor
     */
    Vendor selectByApplicationId(Long applicationId);

    /**
     * Insert vendor
     * 
     * @param vendor Vendor
     * @return Result
     */
    int insertVendor(Vendor vendor);

    /**
     * Update vendor
     * 
     * @param vendor Vendor
     * @return Result
     */
    int updateVendor(Vendor vendor);

    /**
     * Delete vendor by ID
     * 
     * @param id Vendor ID
     * @return Result
     */
    int deleteVendorById(Long id);

    /**
     * Batch delete vendors
     * 
     * @param ids Vendor IDs to delete
     * @return Result
     */
    int deleteVendorByIds(Long[] ids);
}

