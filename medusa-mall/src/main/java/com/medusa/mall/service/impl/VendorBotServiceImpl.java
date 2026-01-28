package com.medusa.mall.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.medusa.mall.domain.vendor.VendorBot;
import com.medusa.mall.mapper.VendorBotMapper;
import com.medusa.mall.service.IVendorBotService;

/**
 * Vendor Bot Service Implementation
 */
@Service
public class VendorBotServiceImpl implements IVendorBotService {
    
    @Autowired
    private VendorBotMapper vendorBotMapper;

    @Override
    public List<VendorBot> selectVendorBotList(VendorBot vendorBot) {
        return vendorBotMapper.selectVendorBotList(vendorBot);
    }

    @Override
    public VendorBot selectVendorBotById(Long id) {
        return vendorBotMapper.selectVendorBotById(id);
    }

    @Override
    public VendorBot selectVendorBotByVendorId(Long vendorId) {
        return vendorBotMapper.selectVendorBotByVendorId(vendorId);
    }

    @Override
    public int insertVendorBot(VendorBot vendorBot) {
        // Set default values
        if (vendorBot.getStatus() == null) {
            vendorBot.setStatus(1); // Enabled
        }
        return vendorBotMapper.insertVendorBot(vendorBot);
    }

    @Override
    public int updateVendorBot(VendorBot vendorBot) {
        return vendorBotMapper.updateVendorBot(vendorBot);
    }

    @Override
    public int deleteVendorBotById(Long id) {
        return vendorBotMapper.deleteVendorBotById(id);
    }
}
