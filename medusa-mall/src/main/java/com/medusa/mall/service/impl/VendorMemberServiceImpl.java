package com.medusa.mall.service.impl;

import com.medusa.common.utils.SecurityUtils;
import com.medusa.mall.domain.vendor.VendorMember;
import com.medusa.mall.mapper.VendorMemberMapper;
import com.medusa.mall.service.IVendorMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class VendorMemberServiceImpl implements IVendorMemberService {

    @Autowired
    private VendorMemberMapper vendorMemberMapper;

    @Override
    public VendorMember selectById(Long id) {
        return vendorMemberMapper.selectById(id);
    }

    @Override
    public VendorMember selectByUsername(String username) {
        return vendorMemberMapper.selectByUsername(username);
    }

    @Override
    public int register(VendorMember member) {
        member.setStatus(1);
        member.setPasswordHash(SecurityUtils.encryptPassword(member.getPasswordHash()));
        member.setCreateBy(member.getUsername());
        return vendorMemberMapper.insertVendorMember(member);
    }

    @Override
    public int update(VendorMember member) {
        member.setUpdateTime(new Date());
        return vendorMemberMapper.updateVendorMember(member);
    }
}

