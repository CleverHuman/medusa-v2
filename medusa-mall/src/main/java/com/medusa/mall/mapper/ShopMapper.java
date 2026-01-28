package com.medusa.mall.mapper;

import com.medusa.mall.domain.shop.Shop;
import java.util.List;

/**
 * 商店信息 Mapper 接口
 */
public interface ShopMapper {

    /**
     * 查询商店信息
     * 
     * @param id 商店ID
     * @return 商店信息
     */
    Shop selectShopById(Long id);

    /**
     * 根据商店代码查询商店信息
     * 
     * @param shopCode 商店代码
     * @return 商店信息
     */
    Shop selectShopByCode(String shopCode);

    /**
     * 根据 store_id 查询商店信息
     * 
     * @param storeId 商户收款 store_id
     * @return 商店信息
     */
    Shop selectShopByStoreId(String storeId);

    /**
     * 查询商店列表
     * 
     * @param shop 查询条件
     * @return 商店列表
     */
    List<Shop> selectShopList(Shop shop);

    /**
     * 查询启用的商店列表
     * 
     * @return 启用的商店列表
     */
    List<Shop> selectEnabledShopList();

    /**
     * 新增商店信息
     * 
     * @param shop 商店信息
     * @return 结果
     */
    int insertShop(Shop shop);

    /**
     * 修改商店信息
     * 
     * @param shop 商店信息
     * @return 结果
     */
    int updateShop(Shop shop);

    /**
     * 删除商店信息
     * 
     * @param id 商店ID
     * @return 结果
     */
    int deleteShopById(Long id);

    /**
     * 批量删除商店信息
     * 
     * @param ids 需要删除的商店ID数组
     * @return 结果
     */
    int deleteShopByIds(Long[] ids);

    /**
     * 检查商店代码是否存在
     * 
     * @param shopCode 商店代码
     * @return 结果
     */
    int checkShopCodeUnique(String shopCode);

    /**
     * 检查 store_id 是否存在
     * 
     * @param storeId 商户收款 store_id
     * @return 结果
     */
    int checkStoreIdUnique(String storeId);
} 