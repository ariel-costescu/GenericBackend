package service.impl;

import api.ShopAPI;
import service.ShopService;

import java.util.List;

public class ShopServiceImpl implements ShopService {
    @Override
    public List<ShopAPI.Product> getRegisteredProducts(Integer authenticatedUserId) {
        return null;
    }

    @Override
    public boolean isProductRegistered(Integer productId) {
        return false;
    }

    @Override
    public void registerProduct(ShopAPI.Product product) {

    }

    @Override
    public void unregisterProduct(Integer productId) {

    }

    @Override
    public ShopAPI.Product verifyReceipt(String receipt) {
        return null;
    }

    @Override
    public void creditGemsAmount(Integer authenticatedUserId, Integer integer) {

    }

    @Override
    public Integer getGemsBalance(Integer authenticatedUserId) {
        return null;
    }

    @Override
    public void debitGemsAmount(Integer authenticatedUserId, Integer gemsAmount) {

    }
}
