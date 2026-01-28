package com.medusa.mall.service;

import com.medusa.mall.domain.Product;
import com.medusa.mall.domain.dto.VendorProductDTO;
import com.medusa.mall.domain.vendor.VendorProductApproval;
import com.medusa.mall.domain.vo.VendorProductVO;
import java.util.List;

/**
 * Vendor Product Service Interface
 */
public interface IVendorProductService {
    /**
     * Query vendor's own products with full details
     * 
     * @param vendorId Vendor ID
     * @return Product VO list (with Product2 info)
     */
    List<VendorProductVO> selectVendorProductVOList(Long vendorId);

    /**
     * Query vendor's own products
     * 
     * @param vendorId Vendor ID
     * @return Product list
     */
    List<Product> selectVendorProductList(Long vendorId);

    /**
     * Query product by ID with full details (including Product2)
     * 
     * @param productId Product ID
     * @param vendorId Vendor ID
     * @return VendorProductVO
     */
    VendorProductVO selectVendorProductVOById(Long productId, Long vendorId);

    /**
     * Query product by ID (with vendor authorization check)
     * 
     * @param productId Product ID
     * @param vendorId Vendor ID
     * @return Product
     */
    Product selectVendorProductById(Long productId, Long vendorId);

    /**
     * Create vendor product (status: pending_approval)
     * 
     * @param productDTO Product DTO with all information
     * @param vendorId Vendor ID
     * @return Result
     */
    int createVendorProduct(VendorProductDTO productDTO, Long vendorId);

    /**
     * Update vendor product (only own products)
     * 
     * @param product Product
     * @param vendorId Vendor ID
     * @return Result
     */
    int updateVendorProduct(Product product, Long vendorId);

    /**
     * Delete vendor product (only own products)
     * 
     * @param productId Product ID
     * @param vendorId Vendor ID
     * @return Result
     */
    int deleteVendorProduct(Long productId, Long vendorId);

    /**
     * Query pending approvals (for Primary CS)
     * 
     * @return Approval list
     */
    List<VendorProductApproval> selectPendingApprovals();

    /**
     * Approve product (Primary CS)
     * 
     * @param productId Product ID
     * @param approverId Approver ID
     * @param approverName Approver name
     * @param notes Approval notes
     * @return Result
     */
    int approveProduct(Long productId, Long approverId, String approverName, String notes);

    /**
     * Reject product (Primary CS)
     * 
     * @param productId Product ID
     * @param approverId Approver ID
     * @param approverName Approver name
     * @param reason Rejection reason
     * @return Result
     */
    int rejectProduct(Long productId, Long approverId, String approverName, String reason);

    /**
     * Get vendor ID from member ID
     * 
     * @param memberId Member ID
     * @return Vendor ID (null if not approved)
     */
    Long getVendorIdByMemberId(Long memberId);
}

