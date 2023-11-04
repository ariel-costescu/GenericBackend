package api;

import api.annotation.MethodType;
import api.annotation.RequestParam;
import api.annotation.RestMethod;
import com.sun.net.httpserver.HttpExchange;

public interface ShopAPI {

    record Product(Integer productId, Integer gemsAmount) {
        @Override
        public String toString() {
            return '{' +
                    "\"productId\": " + productId + ", " +
                    "\"gemsAmount\": " + gemsAmount +
                    '}';
        }
    }

    @RestMethod(methodType = MethodType.GET, pathPattern = "/shop/products")
    void getRegisteredProducts(HttpExchange exchange, Integer authenticatedUserId);

    @RestMethod(methodType = MethodType.POST, pathPattern = "/shop/product" +
            "/(?<productId>\\d+)" +
            "/(?<gemsAmount>\\d+)", authenticated = false)
    void registerProduct(HttpExchange exchange,
                                @RequestParam(name = "productId") String productIdParam,
                                @RequestParam(name = "gemsAmount") String gemsAmountParam);

    @RestMethod(methodType = MethodType.DELETE, pathPattern = "/shop/product" +
            "/(?<productId>\\d+)", authenticated = false)
    void unregisterProduct(HttpExchange exchange,
                         @RequestParam(name = "productId") String productIdParam);

    @RestMethod(methodType = MethodType.POST, pathPattern = "/shop/purchase")
    void purchaseProduct(HttpExchange exchange, Integer authenticatedUserId);

    @RestMethod(methodType = MethodType.GET, pathPattern = "/shop/gems")
    void getGemsBalance(HttpExchange exchange, Integer authenticatedUserId);

    @RestMethod(methodType = MethodType.POST, pathPattern = "/shop/gems/spend/(?<gemsAmount>\\d+)")
    void spendGems(HttpExchange exchange, Integer authenticatedUserId,
                   @RequestParam(name = "gemsAmount") String gemsAmountParam);

}
