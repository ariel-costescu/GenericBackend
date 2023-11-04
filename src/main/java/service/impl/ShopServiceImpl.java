package service.impl;

import api.ShopAPI;
import service.ShopService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.Logger.Level.ERROR;

public class ShopServiceImpl implements ShopService {
    private static final System.Logger LOGGER = System.getLogger("service.impl.ShopServiceImpl");

    private final Map<Integer, ShopAPI.Product> registeredProductsbyProductId = new HashMap<>();
    private final Map<Integer, Long> gemBalanceByUserId = new HashMap<>();

    @Override
    public synchronized List<ShopAPI.Product> getRegisteredProducts(Integer authenticatedUserId) {
        return registeredProductsbyProductId.values().stream().toList();
    }

    @Override
    public synchronized boolean isProductRegistered(Integer productId) {
        return registeredProductsbyProductId.containsKey(productId);
    }

    @Override
    public synchronized void registerProduct(ShopAPI.Product product) {
        registeredProductsbyProductId.put(product.productId(), product);
    }

    @Override
    public synchronized void unregisterProduct(Integer productId) {
        registeredProductsbyProductId.remove(productId);
    }

    @Override
    public synchronized ShopAPI.Product verifyReceipt(Integer authenticatedUserId, String receipt) {
        if (receipt != null) {
            Pattern receiptPattern = Pattern.compile("\\s*\\{" +
                    "\\s*\"(?<key1>[a-zA-Z]+)\"\\s*:" +
                    "\\s*(?<value1>\\d+)" +
                    "\\s*," +
                    "\\s*\"(?<key2>[a-zA-Z]+)\"\\s*:" +
                    "\\s*(?<value2>\\d+)" +
                    "\\s*\\}");
            Matcher receiptMatcher = receiptPattern.matcher(receipt);
            if (receiptMatcher.matches()) {
                String key1 = receiptMatcher.group("key1");
                String key2 = receiptMatcher.group("key2");
                int value1;
                try {
                    value1 = Integer.parseInt(receiptMatcher.group("value1"));
                } catch (NumberFormatException e) {
                    LOGGER.log(ERROR, "Unable to parse receipt");
                    return null;
                }
                int value2;
                try {
                    value2 = Integer.parseInt(receiptMatcher.group("value2"));
                } catch (NumberFormatException e) {
                    LOGGER.log(ERROR, "Unable to parse receipt");
                    return null;
                }
                Map<String, Integer> receiptFields = new HashMap<>();
                receiptFields.put(key1, value1);
                receiptFields.put(key2, value2);
                int userId = receiptFields.get("userId");
                int productId = receiptFields.get("productId");
                if (userId == authenticatedUserId && isProductRegistered(productId)) {
                    return registeredProductsbyProductId.get(productId);
                }
            }
        }
        return null;
    }

    @Override
    public synchronized Long getGemsBalance(Integer authenticatedUserId) {
        return gemBalanceByUserId.computeIfAbsent(authenticatedUserId, userId -> 0L);
    }

    @Override
    public synchronized Long creditGemsAmount(Integer authenticatedUserId, Integer gemsAmount) {
        Long currentGemsBalance = getGemsBalance(authenticatedUserId);
        if (Long.MAX_VALUE - gemsAmount >= currentGemsBalance) {
            gemBalanceByUserId.put(authenticatedUserId, currentGemsBalance + gemsAmount);
            return getGemsBalance(authenticatedUserId);
        } else {
            return null;
        }
    }

    @Override
    public synchronized Long debitGemsAmount(Integer authenticatedUserId, Integer gemsAmount) {
        Long currentGemsBalance = getGemsBalance(authenticatedUserId);
        if (currentGemsBalance - gemsAmount >= 0L) {
            gemBalanceByUserId.put(authenticatedUserId, currentGemsBalance - gemsAmount);
            return getGemsBalance(authenticatedUserId);
        } else {
            return null;
        }
    }
}
