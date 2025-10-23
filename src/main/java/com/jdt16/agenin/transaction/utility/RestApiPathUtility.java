package com.jdt16.agenin.transaction.utility;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RestApiPathUtility {
    public static final String API_PATH = "/api";
    public static final String API_VERSION = "/v1";
    public static final String API_PATH_USER = "/user";
    public static final String API_PATH_GET = "/get";
    public static final String API_PATH_USER_PAGINATION = "/page";
    public static final String API_PATH_BY_ID = "/{id}";
    public static final String API_PATH_CREATE = "/create";
    public static final String API_PATH_MODULE_LOGIN = "/login";
    public static final String API_PATH_MODULE_PROFILE = "/profile";
    public static final String API_PATH_MODULE_TRANSFER_TO_WALLET = "/transfer-to-wallet";
    public static final String API_PATH_TRANSACTION = "/transaction";
    public static final String API_PATH_MOCK_TRANSACTION_OPEN_BANK_ACCOUNT = "/mock-open-bank-account";
    public static final String API_PATH_INQUIRY = "/inquiry";
    public static final String API_PATH_GET_CUSTOMER_LIST = "/customers";
    public static final String API_PATH_GET_PRODUCTS = "/products";
    public static final String API_PATH_GET_BALANCE_AND_WALLET = "/balance-and-wallet";
}
