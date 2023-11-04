package api.handler;

import api.ShopAPI;
import api.annotation.MethodType;
import api.annotation.RequestParam;
import api.annotation.RestMethod;
import com.sun.net.httpserver.HttpExchange;
import service.LoginService;
import service.ShopService;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.System.Logger.Level.ERROR;

public class ShopHandler extends AbstractHandler implements ShopAPI {

    private static final System.Logger LOGGER = System.getLogger("api.handler.ShopHandler");
    private final ShopService shopService;

    public ShopHandler(LoginService loginService, ShopService shopService) {
        this.loginService = loginService;
        this.shopService = shopService;
    }

    @Override
    public System.Logger getLogger() {
        return LOGGER;
    }

    @Override
    @RestMethod(methodType = MethodType.GET, pathPattern = "/shop/products")
    public void getRegisteredProducts(HttpExchange exchange, Integer authenticatedUserId) {
        List<Product> registeredProducts = shopService.getRegisteredProducts(authenticatedUserId);
        String response = String.format("[%s]",
                registeredProducts.stream()
                        .map(Product::toString)
                        .collect(Collectors.joining(", ")));
        sendResponse(exchange, response);
    }

    @Override
    @RestMethod(methodType = MethodType.POST, pathPattern = "/shop/product" +
            "/(?<productId>\\d+)" +
            "/(?<gemsAmount>\\d+)", authenticated = false)
    public void registerProduct(HttpExchange exchange,
                         @RequestParam(name = "productId") String productIdParam,
                         @RequestParam(name = "gemsAmount") String gemsAmountParam) {
        Product product = checkProductIsNotRegistered(exchange, productIdParam, gemsAmountParam);
        if (product != null) {
            shopService.registerProduct(product);
            String response = product.productId().toString();
            sendResponse(exchange, response);
        }
    }

    @Override
    @RestMethod(methodType = MethodType.DELETE, pathPattern = "/shop/product" +
            "/(?<productId>\\d+)", authenticated = false)
    public void unregisterProduct(HttpExchange exchange,
                           @RequestParam(name = "productId") String productIdParam) {
        Integer productId = getProductId(productIdParam);
        if (productId == null) {
            handleBadRequest(exchange);
        } else {
            shopService.unregisterProduct(productId);
            respondWithStatusCode(exchange, 200);
        }
    }

    @Override
    @RestMethod(methodType = MethodType.POST, pathPattern = "/shop/purchase")
    public void purchaseProduct(HttpExchange exchange, Integer authenticatedUserId) {
        String receipt = getRequestBody(exchange);
        Product product = shopService.verifyReceipt(authenticatedUserId, receipt);
        if (product == null) {
            LOGGER.log(ERROR, "Unable to validate purchase: bad receipt");
            handleBadRequest(exchange);
        } else {
            Long gemsBalance = shopService.creditGemsAmount(authenticatedUserId, product.gemsAmount());
            if (gemsBalance == null) {
                LOGGER.log(ERROR, "Unable to credit gems amount");
                handleBadRequest(exchange);
            } else {
                String response = product.productId().toString();
                sendResponse(exchange, response);
            }
        }
    }

    @Override
    @RestMethod(methodType = MethodType.GET, pathPattern = "/shop/gems")
    public void getGemsBalance(HttpExchange exchange, Integer authenticatedUserId) {
        Long gemsBalance = shopService.getGemsBalance(authenticatedUserId);
        String response = gemsBalance.toString();
        sendResponse(exchange, response);
    }

    @Override
    @RestMethod(methodType = MethodType.POST, pathPattern = "/shop/gems/spend/(?<gemsAmount>\\d+)")
    public void spendGems(HttpExchange exchange, Integer authenticatedUserId,
                          @RequestParam(name = "gemsAmount") String gemsAmountParam) {
        Integer gemsAmount = getGemsAmount(gemsAmountParam);
        if (gemsAmount == null) {
            handleBadRequest(exchange);
        } else {
            Long gemsBalance = shopService.debitGemsAmount(authenticatedUserId, gemsAmount);
            if (gemsBalance == null) {
                LOGGER.log(ERROR, "Unable to debit gems amount");
                handleBadRequest(exchange);
            } else {
                String response = gemsBalance.toString();
                sendResponse(exchange, response);
            }
        }
    }

    private Integer getProductId(String productIdParam) {
        if (productIdParam == null) {
            LOGGER.log(ERROR, "Bad product id ");
            return null;
        } else {
            int productId;
            try {
                productId = Integer.parseInt(productIdParam);
            } catch (NumberFormatException e) {
                LOGGER.log(ERROR, "Couldn't parse productId {0}", productIdParam, e);
                return null;
            }
            return productId;
        }
    }

    private Integer getGemsAmount(String gemsAmountParam) {
        if (gemsAmountParam == null) {
            LOGGER.log(ERROR, "Bad product id ");
            return null;
        } else {
            int gemsAmount;
            try {
                gemsAmount = Integer.parseInt(gemsAmountParam);
            } catch (NumberFormatException e) {
                LOGGER.log(ERROR, "Couldn't parse gemsAmount {0}", gemsAmountParam, e);
                return null;
            }
            return gemsAmount;
        }
    }

    private Product checkProductIsNotRegistered(HttpExchange exchange,
                                                String productIdParam,
                                                String gemsAmountParam) {
        Integer productId = getProductId(productIdParam);
        Integer gemsAmount = getGemsAmount(gemsAmountParam);
        if (productId == null || gemsAmount == null) {
            handleBadRequest(exchange);
            return null;
        } else {
            if (shopService.isProductRegistered(productId)) {
                LOGGER.log(ERROR, "Product {0} is already registered", productId);
                handleBadRequest(exchange);
                return null;
            }
        }
        return new Product(productId, gemsAmount);
    }
}
