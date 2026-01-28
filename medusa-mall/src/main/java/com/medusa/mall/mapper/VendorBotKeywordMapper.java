package com.medusa.mall.mapper;

import java.util.List;
import com.medusa.mall.domain.vendor.VendorBotKeyword;

/**
 * Vendor Bot Keyword Mapper Interface
 */
public interface VendorBotKeywordMapper {
    
    /**
     * Query keyword list
     */
    List<VendorBotKeyword> selectVendorBotKeywordList(VendorBotKeyword keyword);

    /**
     * Query keyword by ID
     */
    VendorBotKeyword selectVendorBotKeywordById(Long id);

    /**
     * Insert keyword
     */
    int insertVendorBotKeyword(VendorBotKeyword keyword);

    /**
     * Update keyword
     */
    int updateVendorBotKeyword(VendorBotKeyword keyword);

    /**
     * Delete keyword by ID
     */
    int deleteVendorBotKeywordById(Long id);
}
