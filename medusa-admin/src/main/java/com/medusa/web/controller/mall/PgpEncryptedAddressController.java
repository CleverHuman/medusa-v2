package com.medusa.web.controller.mall;

import com.medusa.common.annotation.Log;
import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.common.core.page.TableDataInfo;
import com.medusa.common.enums.BusinessType;
import com.medusa.common.utils.poi.ExcelUtil;
import com.medusa.mall.domain.PgpEncryptedAddress;
import com.medusa.mall.service.IPgpEncryptedAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * PGP加密地址Controller
 * 
 * @author medusa
 * @date 2025-01-16
 */
@RestController
@RequestMapping("/admin/mall/pgpEncryptedAddress")
public class PgpEncryptedAddressController extends BaseController
{
    @Autowired
    private IPgpEncryptedAddressService pgpEncryptedAddressService;

    /**
     * 查询PGP加密地址列表
     */
    @PreAuthorize("@ss.hasPermi('mall:pgpEncryptedAddress:list')")
    @GetMapping("/list")
    public TableDataInfo list(PgpEncryptedAddress pgpEncryptedAddress)
    {
        startPage();
        List<PgpEncryptedAddress> list = pgpEncryptedAddressService.selectPgpEncryptedAddressList(pgpEncryptedAddress);
        return getDataTable(list);
    }

    /**
     * 导出PGP加密地址列表
     */
    @PreAuthorize("@ss.hasPermi('mall:pgpEncryptedAddress:export')")
    @Log(title = "PGP加密地址", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, PgpEncryptedAddress pgpEncryptedAddress)
    {
        List<PgpEncryptedAddress> list = pgpEncryptedAddressService.selectPgpEncryptedAddressList(pgpEncryptedAddress);
        ExcelUtil<PgpEncryptedAddress> util = new ExcelUtil<PgpEncryptedAddress>(PgpEncryptedAddress.class);
        util.exportExcel(response, list, "PGP加密地址数据");
    }

    /**
     * 获取PGP加密地址详细信息
     */
    @PreAuthorize("@ss.hasPermi('mall:pgpEncryptedAddress:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(pgpEncryptedAddressService.selectPgpEncryptedAddressById(id));
    }

    /**
     * 新增PGP加密地址
     */
    @PreAuthorize("@ss.hasPermi('mall:pgpEncryptedAddress:add')")
    @Log(title = "PGP加密地址", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PgpEncryptedAddress pgpEncryptedAddress)
    {
        return toAjax(pgpEncryptedAddressService.insertPgpEncryptedAddress(pgpEncryptedAddress));
    }

    /**
     * 修改PGP加密地址
     */
    @PreAuthorize("@ss.hasPermi('mall:pgpEncryptedAddress:edit')")
    @Log(title = "PGP加密地址", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PgpEncryptedAddress pgpEncryptedAddress)
    {
        return toAjax(pgpEncryptedAddressService.updatePgpEncryptedAddress(pgpEncryptedAddress));
    }

    /**
     * 删除PGP加密地址
     */
    @PreAuthorize("@ss.hasPermi('mall:pgpEncryptedAddress:remove')")
    @Log(title = "PGP加密地址", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(pgpEncryptedAddressService.deletePgpEncryptedAddressByIds(ids));
    }

    /**
     * 根据订单ID查询加密地址
     */
    @GetMapping("/order/{orderId}")
    public AjaxResult getByOrderId(@PathVariable String orderId)
    {
        return success(pgpEncryptedAddressService.selectPgpEncryptedAddressByOrderId(orderId));
    }

    /**
     * 根据用户ID查询加密地址列表
     */
    @GetMapping("/user/{userId}")
    public AjaxResult getByUserId(@PathVariable Long userId)
    {
        return success(pgpEncryptedAddressService.selectPgpEncryptedAddressByUserId(userId));
    }

    /**
     * 根据密钥ID查询加密地址列表
     */
    @GetMapping("/key/{keyId}")
    public AjaxResult getByKeyId(@PathVariable Long keyId)
    {
        return success(pgpEncryptedAddressService.selectPgpEncryptedAddressByKeyId(keyId));
    }

    /**
     * 加密地址
     */
    @PreAuthorize("@ss.hasPermi('mall:pgpEncryptedAddress:encrypt')")
    @Log(title = "加密地址", businessType = BusinessType.INSERT)
    @PostMapping("/encrypt")
    public AjaxResult encryptAddress(@RequestParam String orderId, @RequestParam Long userId, @RequestParam String address)
    {
        try {
            PgpEncryptedAddress result = pgpEncryptedAddressService.encryptAddress(orderId, userId, address);
            return success(result);
        } catch (Exception e) {
            return error("加密失败：" + e.getMessage());
        }
    }

    /**
     * 解密地址
     */
    @PreAuthorize("@ss.hasPermi('mall:pgpEncryptedAddress:decrypt')")
    @Log(title = "解密地址", businessType = BusinessType.OTHER)
    @PostMapping("/decrypt")
    public AjaxResult decryptAddress(@RequestParam String encryptedAddress)
    {
        try {
            String result = pgpEncryptedAddressService.decryptAddress(encryptedAddress);
            return success(result);
        } catch (Exception e) {
            return error("解密失败：" + e.getMessage());
        }
    }
} 