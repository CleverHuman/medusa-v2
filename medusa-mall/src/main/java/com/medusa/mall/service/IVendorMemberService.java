package com.medusa.mall.service;

import com.medusa.mall.domain.vendor.VendorMember;

public interface IVendorMemberService {

    VendorMember selectById(Long id);

    VendorMember selectByUsername(String username);

    int register(VendorMember member);

    int update(VendorMember member);
}

