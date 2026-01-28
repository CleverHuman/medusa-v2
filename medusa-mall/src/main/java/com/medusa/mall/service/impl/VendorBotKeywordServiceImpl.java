package com.medusa.mall.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.medusa.mall.domain.vendor.VendorBotKeyword;
import com.medusa.mall.mapper.VendorBotKeywordMapper;
import com.medusa.mall.service.IVendorBotKeywordService;

/**
 * Vendor Bot Keyword Service Implementation
 */
@Service
public class VendorBotKeywordServiceImpl implements IVendorBotKeywordService {
    
    @Autowired
    private VendorBotKeywordMapper vendorBotKeywordMapper;

    @Override
    public List<VendorBotKeyword> selectVendorBotKeywordList(VendorBotKeyword keyword) {
        return vendorBotKeywordMapper.selectVendorBotKeywordList(keyword);
    }

    @Override
    public VendorBotKeyword selectVendorBotKeywordById(Long id) {
        return vendorBotKeywordMapper.selectVendorBotKeywordById(id);
    }

    @Override
    public int insertVendorBotKeyword(VendorBotKeyword keyword) {
        // Set default values
        if (keyword.getIsActive() == null) {
            keyword.setIsActive(1); // Active
        }
        if (keyword.getKeywordType() == null || keyword.getKeywordType().isEmpty()) {
            keyword.setKeywordType("exact");
        }
        if (keyword.getSeverity() == null) {
            keyword.setSeverity(2); // Default to penalty
        }
        return vendorBotKeywordMapper.insertVendorBotKeyword(keyword);
    }

    @Override
    public int updateVendorBotKeyword(VendorBotKeyword keyword) {
        return vendorBotKeywordMapper.updateVendorBotKeyword(keyword);
    }

    @Override
    public int deleteVendorBotKeywordById(Long id) {
        return vendorBotKeywordMapper.deleteVendorBotKeywordById(id);
    }
}
