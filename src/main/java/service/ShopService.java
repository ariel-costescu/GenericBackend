package service;

import api.ShopAPI;

import java.util.List;

public interface ShopService {
    List<ShopAPI.Product> getRegisteredProducts(Integer authenticatedUserId);

    boolean isProductRegistered(Integer productId);

    void registerProduct(ShopAPI.Product product);

    void unregisterProduct(Integer productId);

    ShopAPI.Product verifyReceipt(Integer authenticatedUserId, String receipt);

    Long getGemsBalance(Integer authenticatedUserId);

    Long creditGemsAmount(Integer authenticatedUserId, Integer gemsAmount);

    Long debitGemsAmount(Integer authenticatedUserId, Integer gemsAmount);
}
