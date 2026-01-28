package com.medusa.mall.mapper;

import com.medusa.mall.domain.vendor.VendorMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VendorMemberMapper {

    VendorMember selectById(@Param("id") Long id);

    VendorMember selectByUsername(@Param("username") String username);

    int insertVendorMember(VendorMember member);

    int updateVendorMember(VendorMember member);

    List<VendorMember> selectVendorMemberList(VendorMember query);
}

