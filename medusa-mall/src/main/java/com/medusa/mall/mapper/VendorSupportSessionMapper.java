package com.medusa.mall.mapper;

import java.util.List;
import com.medusa.mall.domain.vendor.VendorSupportSession;

/**
 * Vendor Support Session Mapper Interface
 */
public interface VendorSupportSessionMapper {
    
    /**
     * Query session list
     */
    List<VendorSupportSession> selectVendorSupportSessionList(VendorSupportSession session);

    /**
     * Query session by ID
     */
    VendorSupportSession selectVendorSupportSessionById(Long id);

    /**
     * Query active session by vendor, order and user
     */
    VendorSupportSession selectActiveSession(VendorSupportSession session);

    /**
     * Query session by topic ID
     */
    VendorSupportSession selectVendorSupportSessionByTopicId(Long vendorId, Long topicId);

    /**
     * Insert session
     */
    int insertVendorSupportSession(VendorSupportSession session);

    /**
     * Update session
     */
    int updateVendorSupportSession(VendorSupportSession session);

    /**
     * Update last message time
     */
    int updateLastMessageTime(Long id);

    /**
     * Close session
     */
    int closeSession(Long vendorId, Long topicId);
}
