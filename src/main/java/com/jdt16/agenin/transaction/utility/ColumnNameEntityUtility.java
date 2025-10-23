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
    public static final String COLUMN_TRANSACTION_PRODUCT_NAME = "product_name";
    public static final String COLUMN_TRANSACTION_PRODUCT_PRICE = "product_price";
    public static final String COLUMN_TRANSACTION_DATE = "transaction_date";
    public static final String COLUMN_TRANSACTION_STATUS = "transaction_status";

    /* M_PRODUCT */
    public static final String COLUMN_PRODUCT_ID = "product_id";
    public static final String COLUMN_PRODUCT_NAME = "product_name";
    public static final String COLUMN_PRODUCT_CODE = "product_code";
    public static final String COLUMN_PRODUCT_DESC = "product_desc";
    public static final String COLUMN_PRODUCT_PRICE = "product_price";
    public static final String COLUMN_PRODUCT_STATUS = "product_status";
    public static final String COLUMN_PRODUCT_CREATED_AT = "product_created_at";
    public static final String COLUMN_PRODUCT_UPDATED_AT = "product_updated_at";

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
    public static final String COLUMN_USERS_BALANCE_ID = "user_balance_id";
    public static final String COLUMN_USERS_BALANCE_USER_ID = "id_user";
    public static final String COLUMN_USER_BALANCE_AMOUNT = "user_balance_amount";
    public static final String COLUMN_USER_BALANCE_LAST_UPDATE = "user_balance_last_updated";

    /* M_USERS_BALANCE_HISTORICAL */
    public static final String COLUMN_USERS_BALANCE_HISTORICAL_ID = "users_balance_historical_id";
    public static final String COLUMN_USERS_BALANCE_HISTORICAL_AMOUNT = "balance_amount";
    public static final String COLUMN_USER_BALANCE_HISTORICAL_TRANSACTION_ID = "transaction_id";
    public static final String COLUMN_USER_BALANCE_HISTORICAL_CREATED_DATE = "created_date";
    public static final String COLUMN_USER_BALANCE_HISTORICAL_USER_BALANCE_ID = "user_balance_id";

    /* M_USERS_WALLET */
    public static final String COLUMN_USERS_WALLET_ID = "user_wallet_id";
    public static final String COLUMN_USERS_WALLET_USER_ID = "id_user";
    public static final String COLUMN_USER_WALLET_AMOUNT = "user_wallet_amount";
    public static final String COLUMN_USER_WALLET_LAST_UPDATE = "user_wallet_last_updated";

    /* M_USERS_WALLET_HISTORICAL */
    public static final String COLUMN_USERS_WALLET_HISTORICAL_ID = "users_wallet_historical_id";
    public static final String COLUMN_USERS_WALLET_HISTORICAL_AMOUNT = "user_wallet_amount";
    public static final String COLUMN_USER_WALLET_HISTORICAL_CREATED_DATE = "user_wallet_created_date";
    public static final String COLUMN_USER_WALLET_HISTORICAL_USER_WALLET_ID = "user_wallet_id";

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

    /* T_USERS_REFERRAL */
    public static final String COLUMN_USERS_REFERRAL_ID = "users_referral_id";
    public static final String COLUMN_INVITEE_USER_EMAIL = "invitee_user_email";
    public static final String COLUMN_INVITEE_USER_PHONENUMBER = "invitee_user_phonenumber";
    public static final String COLUMN_INVITEE_USER_FULLNAME = "invitee_user_fullname";
    public static final String COLUMN_INVITEE_USER_ID = "invitee_user_id";
    public static final String COLUMN_REFERENCE_USER_ID = "reference_user_id";
    public static final String COLUMN_REFERENCE_USER_FULLNAME = "reference_user_fullname";
    public static final String COLUMN_REFERENCE_USER_PHONENUMBER = "reference_user_phonenumber";
    public static final String COLUMN_REFERENCE_USER_EMAIL = "reference_user_email";
    public static final String COLUMN_REFERRAL_CODE = "referral_code";

    /* T_TRANSACTION_OPEN_BANK_ACCOUNT */
    public static final String COLUMN_TRANSACTION_OPEN_BANK_ACCOUNT_ID = "open_bank_account_id";
    public static final String COLUMN_TRANSACTION_OPEN_BANK_ACCOUNT_TRANSACTION_ID = "transaction_id";
    public static final String COLUMN_TRANSACTION_OPEN_BANK_ACCOUNT_CUSTOMER_NAME = "customer_name";
    public static final String COLUMN_TRANSACTION_OPEN_BANK_ACCOUNT_CUSTOMER_IDENTITY_NUMBER = "customer_identity_number";
    public static final String COLUMN_TRANSACTION_OPEN_BANK_ACCOUNT_CUSTOMER_PHONE_NUMBER = "customer_phonenumber";
    public static final String COLUMN_TRANSACTION_OPEN_BANK_ACCOUNT_CUSTOMER_EMAIL = "customer_email";
    public static final String COLUMN_TRANSACTION_OPEN_BANK_ACCOUNT_CUSTOMER_ADDRESS = "customer_address";

    /* T_AUDIT_LOGS */
    public static final String COLUMN_LOGGING_ID = "audit_logs_id";
    public static final String COLUMN_LOGGING_TABLE_NAME = "table_name";
    public static final String COLUMN_LOGGING_RECORD_ID = "record_id";
    public static final String COLUMN_LOGGING_ACTION = "action";
    public static final String COLUMN_LOGGING_OLD_DATA = "old_data";
    public static final String COLUMN_LOGGING_NEW_DATA = "new_data";
    public static final String COLUMN_LOGGING_USER_AGENT = "user_agent";
    public static final String COLUMN_LOGGING_IP_ADDRESS = "ip_address";
    public static final String COLUMN_LOGGING_CHANGED_AT = "changed_at";
    public static final String COLUMN_LOGGING_ROLE_ID = "role_id";
    public static final String COLUMN_LOGGING_ROLE_NAME = "role_name";
    public static final String COLUMN_LOGGING_USER_ID = "id_user";
    public static final String COLUMN_LOGGING_USER_FULLNAME = "user_fullname";
}
