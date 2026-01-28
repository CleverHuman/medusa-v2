package com.medusa.mall.mapper;

import com.medusa.mall.domain.vendor.VendorProductApproval;
import java.util.List;

/**
 * Vendor Product Approval Mapper
 */
public interface VendorProductApprovalMapper {
    /**
     * Insert approval record
     */
    int insertVendorProductApproval(VendorProductApproval approval);

    /**
     * Update approval record
     */
    int updateVendorProductApproval(VendorProductApproval approval);

    /**
     * Query approval by product ID
     */
    VendorProductApproval selectByProductId(Long productId);

    /**
     * Query approval by vendor ID
     */
    List<VendorProductApproval> selectByVendorId(Long vendorId);

    /**
     * Query pending approvals
     */
    List<VendorProductApproval> selectPendingApprovals();
}

