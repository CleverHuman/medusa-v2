package com.medusa.mall.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.medusa.mall.domain.vendor.Vendor;
import com.medusa.mall.mapper.VendorMapper;
import com.medusa.mall.service.IVendorService;

/**
 * Vendor Service Implementation
 */
@Service
public class VendorServiceImpl implements IVendorService {
    
    @Autowired
    private VendorMapper vendorMapper;

    /**
     * Query vendor list
     * 
     * @param vendor Vendor query params
     * @return Vendor collection
     */
    @Override
    public List<Vendor> selectVendorList(Vendor vendor) {
        return vendorMapper.selectVendorList(vendor);
    }

    /**
     * Query vendor by ID
     * 
     * @param id Vendor ID
     * @return Vendor
     */
    @Override
    public Vendor selectVendorById(Long id) {
        return vendorMapper.selectVendorById(id);
    }

    /**
     * Query vendor by vendor code
     * 
     * @param vendorCode Vendor code
     * @return Vendor
     */
    @Override
    public Vendor selectByVendorCode(String vendorCode) {
        return vendorMapper.selectByVendorCode(vendorCode);
    }

    /**
     * Insert vendor
     * 
     * @param vendor Vendor
     * @return Result
     */
    @Override
    public int insertVendor(Vendor vendor) {
        // Generate vendor code if not provided
        if (vendor.getVendorCode() == null || vendor.getVendorCode().isEmpty()) {
            vendor.setVendorCode("VD" + System.currentTimeMillis());
        }
        
        // Set default status
        if (vendor.getStatus() == null) {
            vendor.setStatus(1); // Enabled
        }
        
        return vendorMapper.insertVendor(vendor);
    }

    /**
     * Update vendor
     * 
     * @param vendor Vendor
     * @return Result
     */
    @Override
    public int updateVendor(Vendor vendor) {
        return vendorMapper.updateVendor(vendor);
    }

    /**
     * Delete vendor by ID
     * 
     * @param id Vendor ID
     * @return Result
     */
    @Override
    public int deleteVendorById(Long id) {
        return vendorMapper.deleteVendorById(id);
    }

    /**
     * Batch delete vendors
     * 
     * @param ids Vendor IDs to delete
     * @return Result
     */
    @Override
    public int deleteVendorByIds(Long[] ids) {
        return vendorMapper.deleteVendorByIds(ids);
    }
}

