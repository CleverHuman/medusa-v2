package com.medusa.web.controller.mall;

import com.medusa.common.core.controller.BaseController;
import com.medusa.common.core.domain.AjaxResult;
import com.medusa.common.core.page.TableDataInfo;
import com.medusa.mall.domain.MallPageConfig;
import com.medusa.mall.service.IMallPageConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/mall/page-config")
public class MallPageConfigController extends BaseController {
    @Autowired
    private IMallPageConfigService mallPageConfigService;

    @GetMapping("/list")
    public TableDataInfo list(MallPageConfig config) {
        startPage();
        List<MallPageConfig> list = mallPageConfigService.selectMallPageConfigList(config);
        return getDataTable(list);
    }

    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(mallPageConfigService.selectMallPageConfigById(id));
    }

    @PostMapping
    public AjaxResult add(@RequestBody MallPageConfig config) {
        return toAjax(mallPageConfigService.insertMallPageConfig(config));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody MallPageConfig config) {
        return toAjax(mallPageConfigService.updateMallPageConfig(config));
    }

    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(mallPageConfigService.deleteMallPageConfigByIds(ids));
    }
} 