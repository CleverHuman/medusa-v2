package com.medusa.mall.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.medusa.mall.domain.vendor.VendorBotViolation;
import com.medusa.mall.mapper.VendorBotViolationMapper;
import com.medusa.mall.service.IVendorBotViolationService;

/**
 * Vendor Bot Violation Service Implementation
 */
@Service
public class VendorBotViolationServiceImpl implements IVendorBotViolationService {
    
    @Autowired
    private VendorBotViolationMapper vendorBotViolationMapper;

    @Override
    public List<VendorBotViolation> selectVendorBotViolationList(VendorBotViolation violation) {
        return vendorBotViolationMapper.selectVendorBotViolationList(violation);
    }

    @Override
    public VendorBotViolation selectVendorBotViolationById(Long id) {
        return vendorBotViolationMapper.selectVendorBotViolationById(id);
    }

    @Override
    public int countTodayViolationsByVendorId(Long vendorId) {
        return vendorBotViolationMapper.countTodayViolationsByVendorId(vendorId);
    }

    @Override
    public int insertVendorBotViolation(VendorBotViolation violation) {
        // Set default values
        if (violation.getPenaltyStatus() == null || violation.getPenaltyStatus().isEmpty()) {
            violation.setPenaltyStatus("pending");
        }
        if (violation.getViolationType() == null || violation.getViolationType().isEmpty()) {
            violation.setViolationType("keyword");
        }
        return vendorBotViolationMapper.insertVendorBotViolation(violation);
    }

    @Override
    public int updateVendorBotViolation(VendorBotViolation violation) {
        return vendorBotViolationMapper.updateVendorBotViolation(violation);
    }

    @Override
    public int updateVendorBotViolationStatus(Long id, String status) {
        return vendorBotViolationMapper.updateVendorBotViolationStatus(id, status);
    }
}
