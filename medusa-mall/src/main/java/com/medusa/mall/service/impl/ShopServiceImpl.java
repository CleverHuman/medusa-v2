package com.medusa.mall.service.impl;

import com.medusa.mall.domain.shop.Shop;
import com.medusa.mall.mapper.ShopMapper;
import com.medusa.mall.service.IShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商店信息 Service 业务层处理
 */
@Service
public class ShopServiceImpl implements IShopService {

    @Autowired
    private ShopMapper shopMapper;

    @Override
    public Shop selectShopById(Long id) {
        return shopMapper.selectShopById(id);
    }

    @Override
    public Shop selectShopByCode(String shopCode) {
        return shopMapper.selectShopByCode(shopCode);
    }

    @Override
    public Shop selectShopByStoreId(String storeId) {
        return shopMapper.selectShopByStoreId(storeId);
    }

    @Override
    public List<Shop> selectShopList(Shop shop) {
        return shopMapper.selectShopList(shop);
    }

    @Override
    public List<Shop> selectEnabledShopList() {
        return shopMapper.selectEnabledShopList();
    }

    @Override
    public int insertShop(Shop shop) {
        return shopMapper.insertShop(shop);
    }

    @Override
    public int updateShop(Shop shop) {
        return shopMapper.updateShop(shop);
    }

    @Override
    public int deleteShopById(Long id) {
        return shopMapper.deleteShopById(id);
    }

    @Override
    public int deleteShopByIds(Long[] ids) {
        return shopMapper.deleteShopByIds(ids);
    }

    @Override
    public boolean checkShopCodeUnique(String shopCode) {
        return shopMapper.checkShopCodeUnique(shopCode) == 0;
    }

    @Override
    public boolean checkStoreIdUnique(String storeId) {
        return shopMapper.checkStoreIdUnique(storeId) == 0;
    }

    @Override
    public Shop getDefaultShop() {
        // 获取默认商店（shop_code = 'DEFAULT'）
        return shopMapper.selectShopByCode("DEFAULT");
    }
} 