package com.medusa.web.controller.mall;

import com.medusa.common.annotation.Log;
import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.common.core.page.TableDataInfo;
import com.medusa.common.enums.BusinessType;
import com.medusa.common.utils.poi.ExcelUtil;
import com.medusa.mall.domain.PgpKey;
import com.medusa.mall.service.IPgpKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * PGP密钥Controller
 * 
 * @author medusa
 * @date 2025-01-16
 */
@RestController
@RequestMapping("/admin/mall/pgpKey")
public class PgpKeyController extends BaseController
{
    @Autowired
    private IPgpKeyService pgpKeyService;

    /**
     * 查询PGP密钥列表
     */
    @PreAuthorize("@ss.hasPermi('mall:pgpKey:list')")
    @GetMapping("/list")
    public TableDataInfo list(PgpKey pgpKey)
    {
        startPage();
        List<PgpKey> list = pgpKeyService.selectPgpKeyList(pgpKey);
        return getDataTable(list);
    }

    /**
     * 导出PGP密钥列表
     */
    @PreAuthorize("@ss.hasPermi('mall:pgpKey:export')")
    @Log(title = "PGP密钥", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, PgpKey pgpKey)
    {
        List<PgpKey> list = pgpKeyService.selectPgpKeyList(pgpKey);
        ExcelUtil<PgpKey> util = new ExcelUtil<PgpKey>(PgpKey.class);
        util.exportExcel(response, list, "PGP密钥数据");
    }

    /**
     * 获取PGP密钥详细信息
     */
    @PreAuthorize("@ss.hasPermi('mall:pgpKey:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(pgpKeyService.selectPgpKeyById(id));
    }

    /**
     * 新增PGP密钥
     */
    @PreAuthorize("@ss.hasPermi('mall:pgpKey:add')")
    @Log(title = "PGP密钥", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PgpKey pgpKey)
    {
        return toAjax(pgpKeyService.insertPgpKey(pgpKey));
    }

    /**
     * 修改PGP密钥
     */
    @PreAuthorize("@ss.hasPermi('mall:pgpKey:edit')")
    @Log(title = "PGP密钥", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PgpKey pgpKey)
    {
        return toAjax(pgpKeyService.updatePgpKey(pgpKey));
    }

    /**
     * 删除PGP密钥
     */
    @PreAuthorize("@ss.hasPermi('mall:pgpKey:remove')")
    @Log(title = "PGP密钥", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(pgpKeyService.deletePgpKeyByIds(ids));
    }

    /**
     * 生成新的PGP密钥对
     */
    @PreAuthorize("@ss.hasPermi('mall:pgpKey:generate')")
    @Log(title = "生成PGP密钥", businessType = BusinessType.INSERT)
    @PostMapping("/generate")
    public AjaxResult generateKeyPair(@RequestParam String keyName, @RequestParam(defaultValue = "2048") Integer keySize)
    {
        PgpKey pgpKey = pgpKeyService.generatePgpKeyPair(keyName, keySize);
        return success(pgpKey);
    }

    /**
     * 设置默认密钥
     */
    @PreAuthorize("@ss.hasPermi('mall:pgpKey:setDefault')")
    @Log(title = "设置默认PGP密钥", businessType = BusinessType.UPDATE)
    @PutMapping("/setDefault/{id}")
    public AjaxResult setDefault(@PathVariable Long id)
    {
        return toAjax(pgpKeyService.setDefaultPgpKey(id));
    }

    /**
     * 激活/禁用密钥
     */
    @PreAuthorize("@ss.hasPermi('mall:pgpKey:toggle')")
    @Log(title = "切换PGP密钥状态", businessType = BusinessType.UPDATE)
    @PutMapping("/toggle/{id}/{isActive}")
    public AjaxResult toggleStatus(@PathVariable Long id, @PathVariable Integer isActive)
    {
        return toAjax(pgpKeyService.togglePgpKeyStatus(id, isActive));
    }

    /**
     * 获取默认密钥
     */
    @GetMapping("/default")
    public AjaxResult getDefaultKey()
    {
        return success(pgpKeyService.selectDefaultPgpKey());
    }

    /**
     * 获取激活的密钥列表
     */
    @GetMapping("/active")
    public AjaxResult getActiveKeys()
    {
        return success(pgpKeyService.selectActivePgpKeys());
    }

    /**
     * 获取第一个可用的公钥（用于前端默认填入）
     */
    @GetMapping("/first-public-key")
    public AjaxResult getFirstPublicKey()
    {
        try {
            // 直接获取默认公钥
            PgpKey defaultKey = pgpKeyService.selectDefaultPgpKey();
            if (defaultKey != null && "public".equals(defaultKey.getKeyType())) {
                return success(defaultKey);
            }
            
            // 如果没有默认公钥，获取第一个激活的公钥
            List<PgpKey> activeKeys = pgpKeyService.selectActivePgpKeys();
            for (PgpKey key : activeKeys) {
                if ("public".equals(key.getKeyType())) {
                    return success(key);
                }
            }
            
            // 如果没有任何激活的公钥，返回空
            return success(null);
        } catch (Exception e) {
            return error("获取公钥失败: " + e.getMessage());
        }
    }

    /**
     * 获取默认公钥（用于前端默认填入）
     */
    @GetMapping("/default-public-key")
    public AjaxResult getDefaultPublicKey()
    {
        try {
            // 直接获取默认公钥
            PgpKey defaultKey = pgpKeyService.selectDefaultPgpKey();
            if (defaultKey != null && "public".equals(defaultKey.getKeyType())) {
                return success(defaultKey);
            }
            
            // 如果没有默认公钥，返回空
            return success(null);
        } catch (Exception e) {
            return error("获取默认公钥失败: " + e.getMessage());
        }
    }
} 