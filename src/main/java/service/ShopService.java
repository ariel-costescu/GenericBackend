package service;

import api.ShopAPI;

import java.util.List;

public interface ShopService {
    List<ShopAPI.Product> getRegisteredProducts(Integer authenticatedUserId);

    boolean isProductRegistered(Integer productId);

    void registerProduct(ShopAPI.Product product);

    void unregisterProduct(Integer productId);

    ShopAPI.Product verifyReceipt(String receipt);

    void creditGemsAmount(Integer authenticatedUserId, Integer integer);

    Integer getGemsBalance(Integer authenticatedUserId);

    void debitGemsAmount(Integer authenticatedUserId, Integer gemsAmount);
}
