package com.medusa.mall.mapper;

import org.apache.ibatis.annotations.Param;
import com.medusa.mall.domain.MallAkSk;

public interface AkSkMapper {

    /**
     * 通过accessKey查询记录
     */
    MallAkSk selectByAccessKey(String accessKey);
}

