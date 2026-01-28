package com.medusa.mall.controller;

import com.medusa.common.core.domain.AjaxResult;
import com.medusa.common.core.domain.entity.SysDictData;
import com.medusa.common.utils.StringUtils;
import com.medusa.system.service.ISysDictTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static com.medusa.common.core.domain.AjaxResult.success;

@RestController
@RequestMapping("/api/mall/system")
public class ApiSystemController {

    @Autowired
    private ISysDictTypeService dictTypeService;

    /**
     * 根据字典类型查询字典数据信息
     */
    @GetMapping(value = "/dict/{dictType}")
    public AjaxResult dictType(@PathVariable String dictType)
    {
        List<SysDictData> data = dictTypeService.selectDictDataByType(dictType);
        if (StringUtils.isNull(data))
        {
            data = new ArrayList<SysDictData>();
        }
        return success(data);
    }
}
