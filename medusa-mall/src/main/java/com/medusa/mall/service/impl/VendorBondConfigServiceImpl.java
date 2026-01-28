package com.medusa.mall.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.medusa.mall.mapper.VendorBondConfigMapper;
import com.medusa.mall.domain.vendor.VendorBondConfig;
import com.medusa.mall.service.IVendorBondConfigService;

/**
 * Vendor Bond Configuration Service Implementation
 */
@Service
public class VendorBondConfigServiceImpl implements IVendorBondConfigService {
    
    @Autowired
    private VendorBondConfigMapper vendorBondConfigMapper;

    /**
     * Query bond config by ID
     *
     * @param id Config ID
     * @return VendorBondConfig
     */
    @Override
    public VendorBondConfig selectVendorBondConfigById(Long id) {
        return vendorBondConfigMapper.selectVendorBondConfigById(id);
    }

    /**
     * Query bond config by level
     *
     * @param level Vendor level
     * @return VendorBondConfig
     */
    @Override
    public VendorBondConfig selectVendorBondConfigByLevel(Integer level) {
        return vendorBondConfigMapper.selectVendorBondConfigByLevel(level);
    }

    /**
     * Query bond config list
     *
     * @param vendorBondConfig VendorBondConfig
     * @return Collection
     */
    @Override
    public List<VendorBondConfig> selectVendorBondConfigList(VendorBondConfig vendorBondConfig) {
        return vendorBondConfigMapper.selectVendorBondConfigList(vendorBondConfig);
    }

    /**
     * Query all active bond configs
     *
     * @return List of active configs
     */
    @Override
    public List<VendorBondConfig> selectActiveConfigs() {
        return vendorBondConfigMapper.selectActiveConfigs();
    }

    /**
     * Insert bond config
     *
     * @param vendorBondConfig VendorBondConfig
     * @return Result
     */
    @Override
    public int insertVendorBondConfig(VendorBondConfig vendorBondConfig) {
        return vendorBondConfigMapper.insertVendorBondConfig(vendorBondConfig);
    }

    /**
     * Update bond config
     *
     * @param vendorBondConfig VendorBondConfig
     * @return Result
     */
    @Override
    public int updateVendorBondConfig(VendorBondConfig vendorBondConfig) {
        return vendorBondConfigMapper.updateVendorBondConfig(vendorBondConfig);
    }

    /**
     * Delete bond config by ID
     *
     * @param id Config ID
     * @return Result
     */
    @Override
    public int deleteVendorBondConfigById(Long id) {
        return vendorBondConfigMapper.deleteVendorBondConfigById(id);
    }

    /**
     * Batch delete bond configs
     *
     * @param ids IDs to delete
     * @return Result
     */
    @Override
    public int deleteVendorBondConfigByIds(Long[] ids) {
        return vendorBondConfigMapper.deleteVendorBondConfigByIds(ids);
    }
}
