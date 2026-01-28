package com.medusa.web.controller.mall;

import com.medusa.common.annotation.Log;
import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.common.core.page.TableDataInfo;
import com.medusa.common.enums.BusinessType;
import com.medusa.mall.domain.Product;
import com.medusa.mall.domain.Product2;
import com.medusa.mall.domain.vendor.Vendor;
import com.medusa.mall.domain.vendor.VendorProductApproval;
import com.medusa.mall.domain.vo.VendorProductVO;
import com.medusa.mall.mapper.ProductMapper;
import com.medusa.mall.mapper.Product2Mapper;
import com.medusa.mall.mapper.VendorMapper;
import com.medusa.mall.service.IVendorProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin Vendor Product Approval Controller
 */
@RestController
@RequestMapping("/admin/mall/vendor/product")
public class VendorProductController extends BaseController {

    @Autowired
    private IVendorProductService vendorProductService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private VendorMapper vendorMapper;

    @Autowired
    private Product2Mapper product2Mapper;

    /**
     * Get all vendor products (for Admin management)
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:product:list')")
    @GetMapping("/list")
    public TableDataInfo list(Product product) {
        startPage();
        // Only query vendor products (product_origin = 1)
        product.setProductOrigin(1);
        List<Product> productList = productMapper.selectProductList(product);
        
        // Convert to VendorProductVO list with price and stock from Product2
        List<VendorProductVO> list = new ArrayList<>();
        for (Product p : productList) {
            // Get Product2 info for price and stock
            Product2 product2 = product2Mapper.selectProduct2ByProductId(p.getProductId());
            VendorProductVO vo = new VendorProductVO(p, product2);
            
            // Fill vendor name
            if (p.getOriginId() != null) {
                Vendor vendor = vendorMapper.selectVendorById(p.getOriginId());
                if (vendor != null) {
                    vo.setVendorName(vendor.getVendorName());
                }
            }
            
            list.add(vo);
        }
        
        return getDataTable(list);
    }

    /**
     * Get vendor product by ID
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:product:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        Product product = productMapper.selectProductById(id);
        if (product == null || !product.isVendorProduct()) {
            return AjaxResult.error("Product not found or not a vendor product");
        }
        
        // Get Product2 info for price and stock
        Product2 product2 = product2Mapper.selectProduct2ByProductId(product.getProductId());
        VendorProductVO vo = new VendorProductVO(product, product2);
        
        // Fill vendor name
        if (product.getOriginId() != null) {
            Vendor vendor = vendorMapper.selectVendorById(product.getOriginId());
            if (vendor != null) {
                vo.setVendorName(vendor.getVendorName());
            }
        }
        
        return AjaxResult.success(vo);
    }

    /**
     * Get pending approvals
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:product:approve')")
    @GetMapping("/pending")
    public TableDataInfo getPendingApprovals() {
        startPage();
        List<VendorProductApproval> approvals = vendorProductService.selectPendingApprovals();
        return getDataTable(approvals);
    }

    /**
     * Get vendor product statistics
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:product:list')")
    @GetMapping("/stats")
    public AjaxResult getStats() {
        // Count by approval status
        Product query = new Product();
        query.setProductOrigin(1); // Only vendor products
        List<Product> allProducts = productMapper.selectProductList(query);
        
        int pending = 0;
        int approved = 0;
        int rejected = 0;
        int total = allProducts.size();
        
        for (Product p : allProducts) {
            String status = p.getApprovalStatus();
            if ("PENDING_APPROVAL".equals(status)) {
                pending++;
            } else if ("APPROVED".equals(status)) {
                approved++;
            } else if ("REJECTED".equals(status)) {
                rejected++;
            }
        }
        
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("approved", approved);
        stats.put("rejected", rejected);
        
        return AjaxResult.success(stats);
    }

    /**
     * Approve product
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:product:approve')")
    @Log(title = "Approve Vendor Product", businessType = BusinessType.UPDATE)
    @PostMapping("/approve/{id}")
    public AjaxResult approve(@PathVariable("id") Long id, @RequestParam(required = false) String notes) {
        try {
            Long approverId = getUserId();
            String approverName = getUsername();
            int result = vendorProductService.approveProduct(id, approverId, approverName, notes);
            return toAjax(result);
        } catch (Exception e) {
            return AjaxResult.error("Failed to approve product: " + e.getMessage());
        }
    }

    /**
     * Reject product
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:product:approve')")
    @Log(title = "Reject Vendor Product", businessType = BusinessType.UPDATE)
    @PostMapping("/reject/{id}")
    public AjaxResult reject(@PathVariable("id") Long id, @RequestParam(required = false) String reason) {
        try {
            Long approverId = getUserId();
            String approverName = getUsername();
            int result = vendorProductService.rejectProduct(id, approverId, approverName, reason);
            return toAjax(result);
        } catch (Exception e) {
            return AjaxResult.error("Failed to reject product: " + e.getMessage());
        }
    }

    /**
     * Batch approve products
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:product:approve')")
    @Log(title = "Batch Approve Vendor Products", businessType = BusinessType.UPDATE)
    @PostMapping("/batch-approve")
    public AjaxResult batchApprove(@RequestBody Map<String, Object> params) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> ids = (List<Integer>) params.get("ids");
            String notes = (String) params.get("notes");
            
            if (ids == null || ids.isEmpty()) {
                return AjaxResult.error("No products selected");
            }
            
            Long approverId = getUserId();
            String approverName = getUsername();
            int successCount = 0;
            
            for (Integer id : ids) {
                try {
                    vendorProductService.approveProduct(id.longValue(), approverId, approverName, notes);
                    successCount++;
                } catch (Exception e) {
                    logger.error("Failed to approve product {}: {}", id, e.getMessage());
                }
            }
            
            return AjaxResult.success("Approved " + successCount + " of " + ids.size() + " products");
        } catch (Exception e) {
            return AjaxResult.error("Batch approve failed: " + e.getMessage());
        }
    }

    /**
     * Batch reject products
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:product:approve')")
    @Log(title = "Batch Reject Vendor Products", businessType = BusinessType.UPDATE)
    @PostMapping("/batch-reject")
    public AjaxResult batchReject(@RequestBody Map<String, Object> params) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> ids = (List<Integer>) params.get("ids");
            String reason = (String) params.get("reason");
            
            if (ids == null || ids.isEmpty()) {
                return AjaxResult.error("No products selected");
            }
            
            Long approverId = getUserId();
            String approverName = getUsername();
            int successCount = 0;
            
            for (Integer id : ids) {
                try {
                    vendorProductService.rejectProduct(id.longValue(), approverId, approverName, reason);
                    successCount++;
                } catch (Exception e) {
                    logger.error("Failed to reject product {}: {}", id, e.getMessage());
                }
            }
            
            return AjaxResult.success("Rejected " + successCount + " of " + ids.size() + " products");
        } catch (Exception e) {
            return AjaxResult.error("Batch reject failed: " + e.getMessage());
        }
    }

    /**
     * Export vendor products
     */
    @PreAuthorize("@ss.hasPermi('mall:vendor:product:export')")
    @Log(title = "Export Vendor Products", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(Product product) {
        product.setProductOrigin(1); // Only vendor products
        List<Product> list = productMapper.selectProductList(product);
        // For now, just return the data as JSON
        // In production, you would use Excel export utility
        return AjaxResult.success(list);
    }
}

