package com.medusa.mall.service.impl;

import com.medusa.common.utils.uuid.IdUtils;
import com.medusa.mall.domain.Product;
import com.medusa.mall.domain.Product2;
import com.medusa.mall.domain.dto.VendorProductDTO;
import com.medusa.mall.domain.vendor.VendorApplication;
import com.medusa.mall.domain.vendor.VendorProductApproval;
import com.medusa.mall.mapper.ProductMapper;
import com.medusa.mall.mapper.Product2Mapper;
import com.medusa.mall.mapper.VendorApplicationMapper;
import com.medusa.mall.mapper.VendorProductApprovalMapper;
import com.medusa.mall.service.IVendorProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medusa.mall.domain.vo.VendorProductVO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Vendor Product Service Implementation
 */
@Service
public class VendorProductServiceImpl implements IVendorProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private Product2Mapper product2Mapper;

    @Autowired
    private VendorProductApprovalMapper approvalMapper;

    @Autowired
    private VendorApplicationMapper vendorApplicationMapper;

    @Override
    public List<VendorProductVO> selectVendorProductVOList(Long vendorId) {
        List<Product> products = selectVendorProductList(vendorId);
        List<VendorProductVO> voList = new ArrayList<>();
        
        for (Product product : products) {
            // Get all Product2 (SKU) info for this product
            Product2 skuFilter = new Product2();
            skuFilter.setProductId(product.getProductId());
            List<Product2> allSkus = product2Mapper.selectProduct2List(skuFilter);
            
            // Get first SKU for backward compatibility (price, stock, etc.)
            Product2 firstSku = allSkus != null && !allSkus.isEmpty() ? allSkus.get(0) : null;
            
            VendorProductVO vo = new VendorProductVO(product, firstSku);
            
            // Set all SKUs list
            vo.setSkus(allSkus != null ? allSkus : new ArrayList<>());
            
            voList.add(vo);
        }
        
        return voList;
    }

    @Override
    public List<Product> selectVendorProductList(Long vendorId) {
        Product query = new Product();
        query.setProductOrigin(1); // Vendor product
        query.setOriginId(vendorId);
        return productMapper.selectProductList(query);
    }

    @Override
    public VendorProductVO selectVendorProductVOById(Long productId, Long vendorId) {
        Product product = selectVendorProductById(productId, vendorId);
        if (product == null) {
            return null;
        }
        
        // Get all Product2 (SKU) info for this product
        Product2 skuFilter = new Product2();
        skuFilter.setProductId(product.getProductId());
        List<Product2> allSkus = product2Mapper.selectProduct2List(skuFilter);
        
        // Get first SKU for backward compatibility (price, stock, etc.)
        Product2 firstSku = allSkus != null && !allSkus.isEmpty() ? allSkus.get(0) : null;
        
        VendorProductVO vo = new VendorProductVO(product, firstSku);
        
        // Set all SKUs list
        vo.setSkus(allSkus != null ? allSkus : new ArrayList<>());
        
        return vo;
    }

    @Override
    public Product selectVendorProductById(Long productId, Long vendorId) {
        Product product = productMapper.selectProductById(productId);
        if (product == null) {
            return null;
        }
        // Check if product belongs to vendor
        if (product.isVendorProduct() && product.getOriginId() != null && product.getOriginId().equals(vendorId)) {
            return product;
        }
        return null;
    }

    @Override
    @Transactional
    public int createVendorProduct(VendorProductDTO productDTO, Long vendorId) {
        // Check if product with same name already exists for this vendor
        Product existingProductQuery = new Product();
        existingProductQuery.setName(productDTO.getName());
        existingProductQuery.setProductOrigin(1); // Vendor product
        existingProductQuery.setOriginId(vendorId);
        List<Product> existingProducts = productMapper.selectProductList(existingProductQuery);
        
        Product product;
        String productId;
        boolean isNewProduct = false;
        
        // Find exact name match (selectProductList uses LIKE, so we need to check exact match)
        Product existingProduct = null;
        for (Product p : existingProducts) {
            if (p.getName() != null && p.getName().equals(productDTO.getName())) {
                existingProduct = p;
                break;
            }
        }
        
        if (existingProduct != null) {
            // Use existing product and productId
            product = existingProduct;
            productId = existingProduct.getProductId();
        } else {
            // Create new product
            isNewProduct = true;
            // Generate unique product_id using 8-character UUID
            // Format: VP-XXXXXXXX (e.g., VP-A1B2C3D4)
            productId = "VP-" + IdUtils.fastSimpleUUID().substring(0, 8).toUpperCase();
            
            product = new Product();
            product.setProductId(productId);
            product.setName(productDTO.getName());
            product.setCategory(productDTO.getCategory());
            product.setDescription(productDTO.getDescription());
            product.setImageUrl(productDTO.getImageUrl());
            product.setBrand(productDTO.getBrand()); // Set brand from DTO
            product.setProductOrigin(1); // Vendor product
            product.setOriginId(vendorId);
            product.setStatus(0); // Inactive until approved
            product.setApprovalStatus("PENDING_APPROVAL"); // Set initial approval status
            product.setChannel("3"); // Set channel to OS/TG (0=OS, 1=TG, 3=OS/TG)
            
            // Insert product
            int result = productMapper.insertProduct(product);
            if (result <= 0 || product.getId() == null) {
                return result; // Failed to insert
            }
            
            // Create approval record only for new products
            VendorProductApproval approval = new VendorProductApproval();
            approval.setProductId(product.getId());
            approval.setVendorId(vendorId);
            approval.setApprovalStatus("PENDING_APPROVAL");
            approvalMapper.insertVendorProductApproval(approval);
        }
        
        // Generate SKU if not provided
        // Format: VP-XXXXXXXX-001 (max 18 characters, well within varchar(32) limit)
        String sku = productDTO.getSku();
        if (sku == null || sku.trim().isEmpty()) {
            // Generate a unique SKU based on productId and a sequence number
            // Check existing SKUs for this product to determine the next sequence number
            Product2 skuFilter = new Product2();
            skuFilter.setProductId(productId);
            List<Product2> existingSkus = product2Mapper.selectProduct2List(skuFilter);
            int nextSequence = existingSkus.size() + 1;
            sku = productId + "-" + String.format("%03d", nextSequence); // e.g., VP-XXXXXXXX-001
        }
        
        // Check if SKU already exists for this product
        Product2 existingSkuFilter = new Product2();
        existingSkuFilter.setProductId(productId);
        existingSkuFilter.setSku(sku);
        List<Product2> existingSkusWithSameSku = product2Mapper.selectProduct2List(existingSkuFilter);
        if (!existingSkusWithSameSku.isEmpty()) {
            throw new RuntimeException("SKU already exists for this product: " + sku);
        }
        
        // Create Product2 (SKU/variant info)
        Product2 product2 = new Product2();
        product2.setProductId(productId);
        product2.setSku(sku);
        product2.setPrice(productDTO.getPrice() != null ? productDTO.getPrice() : BigDecimal.ZERO);
        product2.setInventory(productDTO.getStock() != null ? productDTO.getStock() : 0);
        product2.setCurrency("AUD");
        product2.setStatus(0); // Inactive until approved
        
        // Set model (amount) and unit from DTO
        if (productDTO.getModel() != null) {
            product2.setModel(productDTO.getModel());
        } else {
            product2.setModel(BigDecimal.ONE); // Default to 1
        }
        
        // Set unit (g, ml, pills, ea, months)
        if (productDTO.getUnit() != null && !productDTO.getUnit().isEmpty()) {
            product2.setUnit(productDTO.getUnit());
        } else {
            product2.setUnit("ea"); // Default to each
        }
        
        int result = product2Mapper.insertProduct2(product2);
        
        return isNewProduct ? 1 : result; // Return success if product already existed or SKU was created
    }

    @Override
    @Transactional
    public int updateVendorProduct(Product product, Long vendorId) {
        // Verify ownership
        Product existing = productMapper.selectProductById(product.getId());
        if (existing == null || !existing.isVendorProduct() || 
            existing.getOriginId() == null || !existing.getOriginId().equals(vendorId)) {
            throw new RuntimeException("Product not found or access denied");
        }
        
        // Preserve vendor product attributes
        product.setProductOrigin(1);
        product.setOriginId(vendorId);
        // Ensure channel is set to OS/TG for vendor products (0=OS, 1=TG, 3=OS/TG)
        if (product.getChannel() == null || "Vendor".equals(product.getChannel())) {
            product.setChannel("3"); // Set to OS/TG
        }
        
        // If product was approved and now being edited, set back to pending
        VendorProductApproval approval = approvalMapper.selectByProductId(product.getId());
        if (approval != null && "approved".equals(approval.getApprovalStatus())) {
            approval.setApprovalStatus("pending_approval");
            approval.setApproverId(null);
            approval.setApproverName(null);
            approval.setApprovedTime(null);
            approval.setRejectionReason(null);
            approvalMapper.updateVendorProductApproval(approval);
            product.setStatus(0); // Inactive until re-approved
        }
        
        return productMapper.updateProduct(product);
    }

    @Override
    @Transactional
    public int deleteVendorProduct(Long productId, Long vendorId) {
        // Verify ownership
        Product product = productMapper.selectProductById(productId);
        if (product == null || !product.isVendorProduct() || 
            product.getOriginId() == null || !product.getOriginId().equals(vendorId)) {
            throw new RuntimeException("Product not found or access denied");
        }
        
        // Soft delete (set status to 2)
        product.setStatus(2);
        return productMapper.updateProduct(product);
    }

    @Override
    public List<VendorProductApproval> selectPendingApprovals() {
        return approvalMapper.selectPendingApprovals();
    }

    @Override
    @Transactional
    public int approveProduct(Long productId, Long approverId, String approverName, String notes) {
        Product product = productMapper.selectProductById(productId);
        if (product == null || !product.isVendorProduct()) {
            throw new RuntimeException("Product not found or not a vendor product");
        }
        
        // Update approval record
        VendorProductApproval approval = approvalMapper.selectByProductId(productId);
        if (approval == null) {
            approval = new VendorProductApproval();
            approval.setProductId(productId);
            approval.setVendorId(product.getOriginId());
            approval.setApprovalStatus("approved");
            approval.setApproverId(approverId);
            approval.setApproverName(approverName);
            approval.setApprovalNotes(notes);
            approval.setApprovedTime(new Date());
            approvalMapper.insertVendorProductApproval(approval);
        } else {
            approval.setApprovalStatus("approved");
            approval.setApproverId(approverId);
            approval.setApproverName(approverName);
            approval.setApprovalNotes(notes);
            approval.setApprovedTime(new Date());
            approval.setRejectionReason(null);
            approvalMapper.updateVendorProductApproval(approval);
        }
        
        // Activate product and update approval status in product table
        product.setStatus(1);
        product.setApprovalStatus("APPROVED");
        product.setApprovedTime(new Date());
        product.setApprovedBy(approverName);
        product.setRejectionReason(null); // Clear any previous rejection reason
        // Ensure channel is set to OS/TG when approved (0=OS, 1=TG, 3=OS/TG)
        if (product.getChannel() == null || "Vendor".equals(product.getChannel())) {
            product.setChannel("3"); // Set to OS/TG
        }
        int result = productMapper.updateProduct(product);
        
        // Activate Product2 (SKU) when product is approved
        Product2 product2 = product2Mapper.selectProduct2ByProductId(product.getProductId());
        if (product2 != null) {
            product2.setStatus(1); // Activate SKU
            product2Mapper.updateProduct2(product2);
        }
        
        return result;
    }

    @Override
    @Transactional
    public int rejectProduct(Long productId, Long approverId, String approverName, String reason) {
        Product product = productMapper.selectProductById(productId);
        if (product == null || !product.isVendorProduct()) {
            throw new RuntimeException("Product not found or not a vendor product");
        }
        
        // Update approval record
        VendorProductApproval approval = approvalMapper.selectByProductId(productId);
        if (approval == null) {
            approval = new VendorProductApproval();
            approval.setProductId(productId);
            approval.setVendorId(product.getOriginId());
            approval.setApprovalStatus("REJECTED");
            approval.setApproverId(approverId);
            approval.setApproverName(approverName);
            approval.setRejectionReason(reason);
            approval.setApprovedTime(new Date());
            approvalMapper.insertVendorProductApproval(approval);
        } else {
            approval.setApprovalStatus("REJECTED");
            approval.setApproverId(approverId);
            approval.setApproverName(approverName);
            approval.setRejectionReason(reason);
            approval.setApprovedTime(new Date());
            approvalMapper.updateVendorProductApproval(approval);
        }
        
        // Keep product inactive and update rejection status in product table
        product.setStatus(0);
        product.setApprovalStatus("REJECTED");
        product.setRejectionReason(reason);
        product.setApprovedTime(new Date());
        product.setApprovedBy(approverName);
        return productMapper.updateProduct(product);
    }

    @Override
    public Long getVendorIdByMemberId(Long memberId) {
        // Find approved application for this member
        VendorApplication query = new VendorApplication();
        query.setMemberId(memberId);
        query.setStatus("approved");
        List<VendorApplication> applications = vendorApplicationMapper.selectVendorApplicationList(query);
        
        if (applications != null && !applications.isEmpty()) {
            // Return the first approved application's vendor ID
            VendorApplication app = applications.get(0);
            return app.getVendorId();
        }
        
        return null;
    }
}

