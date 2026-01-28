package com.medusa.mall.service;

import com.medusa.mall.domain.vendor.VendorBondConfig;
import java.util.List;

/**
 * Vendor Bond Configuration Service Interface
 */
public interface IVendorBondConfigService {
    
    /**
     * Query bond config by ID
     *
     * @param id Config ID
     * @return VendorBondConfig
     */
    VendorBondConfig selectVendorBondConfigById(Long id);

    /**
     * Query bond config by level
     *
     * @param level Vendor level
     * @return VendorBondConfig
     */
    VendorBondConfig selectVendorBondConfigByLevel(Integer level);

    /**
     * Query bond config list
     *
     * @param vendorBondConfig VendorBondConfig
     * @return Collection
     */
    List<VendorBondConfig> selectVendorBondConfigList(VendorBondConfig vendorBondConfig);

    /**
     * Query all active bond configs
     *
     * @return List of active configs
     */
    List<VendorBondConfig> selectActiveConfigs();

    /**
     * Insert bond config
     *
     * @param vendorBondConfig VendorBondConfig
     * @return Result
     */
    int insertVendorBondConfig(VendorBondConfig vendorBondConfig);

    /**
     * Update bond config
     *
     * @param vendorBondConfig VendorBondConfig
     * @return Result
     */
    int updateVendorBondConfig(VendorBondConfig vendorBondConfig);

    /**
     * Delete bond config by ID
     *
     * @param id Config ID
     * @return Result
     */
    int deleteVendorBondConfigById(Long id);

    /**
     * Batch delete bond configs
     *
     * @param ids IDs to delete
     * @return Result
     */
    int deleteVendorBondConfigByIds(Long[] ids);
}
