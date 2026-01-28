package com.medusa.mall.service.impl;

import com.medusa.mall.domain.order.Order;
import com.medusa.mall.domain.vendor.Vendor;
import com.medusa.mall.domain.vendor.VendorLevelHistory;
import com.medusa.mall.mapper.OrderMapper;
import com.medusa.mall.mapper.VendorLevelHistoryMapper;
import com.medusa.mall.mapper.VendorMapper;
import com.medusa.mall.service.IVendorBondService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Vendor Bond & Level Service Implementation
 */
@Service
public class VendorBondServiceImpl implements IVendorBondService {

    @Autowired
    private VendorMapper vendorMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private VendorLevelHistoryMapper vendorLevelHistoryMapper;

    @Override
    public BigDecimal calculateMaxSalesLimit(Long vendorId) {
        Vendor vendor = vendorMapper.selectVendorById(vendorId);
        if (vendor == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal bond = vendor.getBond() != null ? vendor.getBond() : BigDecimal.ZERO;
        Integer level = vendor.getLevel() != null && vendor.getLevel() > 0 ? vendor.getLevel() : 1;
        
        return bond.multiply(new BigDecimal(level));
    }

    @Override
    public BigDecimal calculateCurrentPendingSales(Long vendorId) {
        // Calculate sum of all paid/fulfilled orders that haven't been shipped
        // Status: 1=paid, 2=fulfilled (both are paid but not shipped yet)
        Order query = new Order();
        query.setVendorId(vendorId);
        List<Order> orders = orderMapper.selectOrderList(query);
        
        BigDecimal total = BigDecimal.ZERO;
        for (Order order : orders) {
            Integer status = order.getStatus();
            // Include orders that are paid or fulfilled but not shipped
            // Status 1 = Paid: order is paid but not yet fulfilled/shipped
            // Status 2 = Fulfilled: order is paid and fulfilled but not yet shipped
            // Both represent orders that are paid but not shipped, so they occupy sales capacity
            if (status != null && (status == 1 || status == 2)) {
                BigDecimal amount = order.getTotalAmount();
                if (amount != null) {
                    total = total.add(amount);
                }
            }
        }
        
        return total;
    }

    @Override
    public boolean checkSalesLimit(Long vendorId, BigDecimal orderAmount) {
        BigDecimal maxLimit = calculateMaxSalesLimit(vendorId);
        if (maxLimit.compareTo(BigDecimal.ZERO) <= 0) {
            // No bond set, reject order
            return false;
        }
        
        BigDecimal currentPending = calculateCurrentPendingSales(vendorId);
        BigDecimal afterOrder = currentPending.add(orderAmount);
        
        // Check if after adding this order, it's still within limit
        return afterOrder.compareTo(maxLimit) <= 0;
    }

    @Override
    @Transactional
    public boolean updateSalesPointsAndLevel(Long vendorId, String orderId, BigDecimal orderAmount) {
        Vendor vendor = vendorMapper.selectVendorById(vendorId);
        if (vendor == null) {
            return false;
        }
        
        // Convert order amount to points (1 point per $1, rounded down)
        long pointsToAdd = orderAmount.longValue();
        
        Long currentPoints = vendor.getSalesPoints() != null ? vendor.getSalesPoints() : 0L;
        Integer oldLevel = vendor.getLevel() != null && vendor.getLevel() > 0 ? vendor.getLevel() : 1;
        
        Long newPoints = currentPoints + pointsToAdd;
        
        // Calculate new level based on points
        // Level 1: 0-999 points
        // Level 2: 1,000-9,999 points
        // Level 3: 10,000-99,999 points
        // Level 4: 100,000-999,999 points
        // Level 5: 1,000,000+ points
        Integer newLevel;
        if (newPoints < 1000) {
            newLevel = 1;
        } else if (newPoints < 10000) {
            newLevel = 2;
        } else if (newPoints < 100000) {
            newLevel = 3;
        } else if (newPoints < 1000000) {
            newLevel = 4;
        } else {
            newLevel = 5;
        }
        
        // Update vendor
        vendor.setSalesPoints(newPoints);
        vendor.setLevel(newLevel);
        vendorMapper.updateVendor(vendor);
        
        // Record level history if level changed
        boolean levelUpgraded = false;
        if (!oldLevel.equals(newLevel)) {
            levelUpgraded = true;
            VendorLevelHistory history = new VendorLevelHistory();
            history.setVendorId(vendorId);
            history.setOldLevel(oldLevel);
            history.setNewLevel(newLevel);
            history.setOldPoints(currentPoints);
            history.setNewPoints(newPoints);
            history.setTriggerOrderId(orderId);
            history.setTriggerAmount(orderAmount);
            vendorLevelHistoryMapper.insertVendorLevelHistory(history);
        }
        
        return levelUpgraded;
    }

    @Override
    public Integer getVendorLevel(Long vendorId) {
        Vendor vendor = vendorMapper.selectVendorById(vendorId);
        if (vendor == null || vendor.getLevel() == null || vendor.getLevel() <= 0) {
            return 1; // Default level
        }
        return vendor.getLevel();
    }

    @Override
    public BigDecimal getVendorBond(Long vendorId) {
        Vendor vendor = vendorMapper.selectVendorById(vendorId);
        if (vendor == null || vendor.getBond() == null) {
            return BigDecimal.ZERO;
        }
        return vendor.getBond();
    }

    @Override
    public Long getVendorSalesPoints(Long vendorId) {
        Vendor vendor = vendorMapper.selectVendorById(vendorId);
        if (vendor == null || vendor.getSalesPoints() == null) {
            return 0L;
        }
        return vendor.getSalesPoints();
    }
}

