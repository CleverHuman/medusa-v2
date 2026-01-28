package com.medusa.mall.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.medusa.mall.domain.vendor.VendorSupportSession;
import com.medusa.mall.mapper.VendorSupportSessionMapper;
import com.medusa.mall.service.IVendorSupportSessionService;

/**
 * Vendor Support Session Service Implementation
 */
@Service
public class VendorSupportSessionServiceImpl implements IVendorSupportSessionService {
    
    @Autowired
    private VendorSupportSessionMapper vendorSupportSessionMapper;

    @Override
    public List<VendorSupportSession> selectVendorSupportSessionList(VendorSupportSession session) {
        return vendorSupportSessionMapper.selectVendorSupportSessionList(session);
    }

    @Override
    public VendorSupportSession selectVendorSupportSessionById(Long id) {
        return vendorSupportSessionMapper.selectVendorSupportSessionById(id);
    }

    @Override
    public VendorSupportSession selectActiveSession(VendorSupportSession session) {
        return vendorSupportSessionMapper.selectActiveSession(session);
    }

    @Override
    public VendorSupportSession selectVendorSupportSessionByTopicId(Long vendorId, Long topicId) {
        return vendorSupportSessionMapper.selectVendorSupportSessionByTopicId(vendorId, topicId);
    }

    @Override
    public int insertVendorSupportSession(VendorSupportSession session) {
        // Set default values
        if (session.getStatus() == null || session.getStatus().isEmpty()) {
            session.setStatus("active");
        }
        if (session.getMessageCount() == null) {
            session.setMessageCount(0);
        }
        return vendorSupportSessionMapper.insertVendorSupportSession(session);
    }

    @Override
    public int updateVendorSupportSession(VendorSupportSession session) {
        return vendorSupportSessionMapper.updateVendorSupportSession(session);
    }

    @Override
    public int updateLastMessageTime(Long id) {
        return vendorSupportSessionMapper.updateLastMessageTime(id);
    }

    @Override
    public int closeSession(Long vendorId, Long topicId) {
        return vendorSupportSessionMapper.closeSession(vendorId, topicId);
    }
}
