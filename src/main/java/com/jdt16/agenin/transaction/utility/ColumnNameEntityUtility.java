package com.jdt16.agenin.transaction.utility;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ColumnNameEntityUtility {
    /* M_TRANSACTION */
    public static final String COLUMN_TRANSACTION_ID = "transaction_id";
    public static final String COLUMN_TRANSACTION_CODE = "transaction_code";
    public static final String COLUMN_TRANSACTION_USER_ID = "id_user";
    public static final String COLUMN_TRANSACTION_PRODUCT_ID = "id_product";
    public static final String COLUMN_TRANSACTION_QUANTITY = "transaction_quantity";
    public static final String COLUMN_TRANSACTION_TOTAL_AMOUNT = "transaction_total_amount";
    public static final String COLUMN_TRANSACTION_DATE = "transaction_date";
    public static final String COLUMN_TRANSACTION_STATUS = "transaction_status";

    /* M_PRODUCT */
    public static final String COLUMN_PRODUCT_ID = "product_id";
    public static final String COLUMN_PRODUCT_NAME = "product_name";
    public static final String COLUMN_PRODUCT_CODE = "product_code";
    public static final String COLUMN_PRODUCT_DESC = "product_desc";
    public static final String COLUMN_PRODUCT_PRICE = "product_price";
    public static final String COLUMN_PRODUCT_STATUS = "product_status";
    public static final String COLUMN_PRODUCT_CREATED_DATE = "product_created_date";
    public static final String COLUMN_PRODUCT_UPDATED_DATE = "product_updated_date";

    /* M_COMMISSION */
    public static final String COLUMN_COMMISSIONS_ID = "commissions_id";
    public static final String COLUMN_COMMISSIONS_NAME = "commissions_name";
    public static final String COLUMN_COMMISSIONS_VALUE = "commissions_value";
    public static final String COLUMN_COMMISSIONS_SETUP = "commissions_setup";
    public static final String COLUMN_COMMISSIONS_PRODUCT_ID = "product_id";
    public static final String COLUMN_COMMISSIONS_PRODUCT_NAME = "product_name";
    public static final String COLUMN_COMMISSIONS_CREATED_DATE = "commissions_created_date";
    public static final String COLUMN_COMMISSIONS_UPDATED_DATE = "commissions_updated_date";

    /* M_USERS_BALANCE */
    public static final String COLUMN_USERS_BALANCE_ID = "users_balance_id";
    public static final String COLUMN_USERS_BALANCE_USER_ID = "id_user";
    public static final String COLUMN_USER_BALANCE_AMOUNT = "users_balance_amount";
    public static final String COLUMN_USER_BALANCE_LAST_UPDATE = "users_balance_last_update";

    /* M_USERS */
    public static final String COLUMN_USERS_ID = "user_id";
    public static final String COLUMN_USERS_FULLNAME = "user_fullname";
    public static final String COLUMN_USERS_PHONE_NUMBER = "user_phone_number";
    public static final String COLUMN_USERS_EMAIL = "user_email";
    public static final String COLUMN_USERS_PASSWORD = "user_password";
    public static final String COLUMN_USERS_ROLE_ID = "role_id";
    public static final String COLUMN_USERS_ROLE_NAME = "role_name";
    public static final String COLUMN_USERS_CREATED_DATE = "user_created_date";
    public static final String COLUMN_USERS_UPDATED_DATE = "user_updated_date";

    /* T_TRANSACTION_OPEN_BANK_ACCOUNT */
    public static final String COLUMN_TRANSACTION_OPEN_BANK_ACCOUNT_ID = "open_bank_account_id";
    public static final String COLUMN_TRANSACTION_OPEN_BANK_ACCOUNT_TRANSACTION_ID = "transaction_id";
    public static final String COLUMN_TRANSACTION_OPEN_BANK_ACCOUNT_CUSTOMER_NAME = "customer_name";
    public static final String COLUMN_TRANSACTION_OPEN_BANK_ACCOUNT_CUSTOMER_IDENTITY_NUMBER = "customer_identity_number";
    public static final String COLUMN_TRANSACTION_OPEN_BANK_ACCOUNT_CUSTOMER_PHONE_NUMBER = "customer_phone_number";
    public static final String COLUMN_TRANSACTION_OPEN_BANK_ACCOUNT_CUSTOMER_EMAIL = "customer_email";
    public static final String COLUMN_TRANSACTION_OPEN_BANK_ACCOUNT_CUSTOMER_ADDRESS = "customer_address";
}
