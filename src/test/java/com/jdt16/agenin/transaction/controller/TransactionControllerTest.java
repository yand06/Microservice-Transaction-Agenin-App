package com.jdt16.agenin.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jdt16.agenin.transaction.controller.module.TransactionController;
import com.jdt16.agenin.transaction.dto.request.CommissionToWalletRequest;
import com.jdt16.agenin.transaction.dto.request.TransactionRequest;
import com.jdt16.agenin.transaction.dto.response.*;
import com.jdt16.agenin.transaction.service.interfacing.module.TransactionService;
import com.jdt16.agenin.transaction.utility.RestApiPathUtility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    private static final String HEADER_USER = "X-USER-ID";
    private static final String HEADER_PRODUCT = "X-PRODUCT-ID";

    private static <T> RestApiResponse<?> okResponseTyped(T results) {
        RestApiResponse<T> body = new RestApiResponse<>();
        body.setRestApiResponseCode(200);
        body.setRestApiResponseMessage("OK");
        body.setRestApiResponseResults(results);
        return body;
    }

    private static String api(String path) {
        return RestApiPathUtility.API_PATH
                + RestApiPathUtility.API_VERSION
                + RestApiPathUtility.API_PATH_TRANSACTION
                + path;
    }

    @Nested
    @DisplayName("POST inquiry (mock open bank account)")
    class InquiryTests {

        @Test
        @DisplayName("200: success – forwards headers & body to service")
        void inquiry_success() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();

            TransactionRequest transactionRequest = new TransactionRequest();

            TransactionResponse transactionResponse = mock(TransactionResponse.class);

            when(transactionService.inquiry(
                    eq(userId),
                    eq(productId),
                    any(TransactionRequest.class)
            ))
                    .thenReturn((RestApiResponse<TransactionResponse>) okResponseTyped(transactionResponse));

            mockMvc.perform(post(api(RestApiPathUtility.API_PATH_MOCK_TRANSACTION_OPEN_BANK_ACCOUNT))
                            .header(HEADER_USER, userId.toString())
                            .header(HEADER_PRODUCT, productId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(transactionRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.results", notNullValue()));

            ArgumentCaptor<TransactionRequest> captor = ArgumentCaptor.forClass(TransactionRequest.class);
            verify(transactionService).inquiry(eq(userId), eq(productId), captor.capture());
        }

        @Test
        @DisplayName("500: missing X-USER-ID header (mapped by Global Advice)")
        void inquiry_missing_user_header() throws Exception {
            UUID productId = UUID.randomUUID();

            mockMvc.perform(post(api(RestApiPathUtility.API_PATH_MOCK_TRANSACTION_OPEN_BANK_ACCOUNT))
                            .header(HEADER_PRODUCT, productId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isInternalServerError());

            verifyNoInteractions(transactionService);
        }

        @Test
        @DisplayName("500: missing X-PRODUCT-ID header (mapped by Global Advice)")
        void inquiry_missing_product_header() throws Exception {
            UUID userId = UUID.randomUUID();

            mockMvc.perform(post(api(RestApiPathUtility.API_PATH_MOCK_TRANSACTION_OPEN_BANK_ACCOUNT))
                            .header(HEADER_USER, userId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isInternalServerError());

            verifyNoInteractions(transactionService);
        }
    }

    @Nested
    @DisplayName("GET product list by user")
    class GetProductListTests {

        @Test
        @DisplayName("200: success")
        void get_product_list_success() throws Exception {
            ProductsResponse productsResponse = Mockito.mock(ProductsResponse.class);

            when(transactionService.getListProducts()).thenReturn((RestApiResponse<List<ProductsResponse>>) okResponseTyped(List.of(productsResponse)));

            mockMvc.perform(get(api(RestApiPathUtility.API_PATH_GET_PRODUCTS)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.results").isArray());

            verify(transactionService).getListProducts();
        }
    }

    @Nested
    @DisplayName("GET customer list by user")
    class GetCustomerListTests {

        @Test
        @DisplayName("200: success")
        void get_customer_list_success() throws Exception {
            UUID userId = UUID.randomUUID();

            CustomerOpenBankAccountResponse customerOpenBankAccountResponse = mock(CustomerOpenBankAccountResponse.class);

            when(transactionService.getAllTransactionsByUser(eq(userId)))
                    .thenReturn((RestApiResponse<List<CustomerOpenBankAccountResponse>>) okResponseTyped(List.of(customerOpenBankAccountResponse)));

            mockMvc.perform(get(api(RestApiPathUtility.API_PATH_GET_CUSTOMER_LIST))
                            .header(HEADER_USER, userId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.results").isArray());

            verify(transactionService).getAllTransactionsByUser(eq(userId));
        }


        @Test
        @DisplayName("500: missing X-USER-ID header (mapped by Global Advice)")
        void get_customer_list_missing_header() throws Exception {
            mockMvc.perform(get(api(RestApiPathUtility.API_PATH_GET_CUSTOMER_LIST)))
                    .andExpect(status().isInternalServerError());

            verifyNoInteractions(transactionService);
        }
    }

    @Nested
    @DisplayName("PATCH transfer commission to wallet")
    class PatchTransferCommissionTests {

        @Test
        @DisplayName("200: success – valid body & header, forwarded to service")
        void patch_transfer_success() throws Exception {
            UUID userId = UUID.randomUUID();

            CommissionToWalletRequest commissionToWalletRequest = new CommissionToWalletRequest();
            commissionToWalletRequest.setCommissionToWalletAmount(new BigDecimal("10000"));
            commissionToWalletRequest.setUserEntityDTOPassword("secret-password");

            UserBalanceAndWalletResponse userBalanceAndWalletResponse = mock(UserBalanceAndWalletResponse.class);
            when(transactionService.transactionCommissionToWallet(
                    eq(userId),
                    any(CommissionToWalletRequest.class)
            ))
                    .thenReturn((RestApiResponse<UserBalanceResponse>) okResponseTyped(userBalanceAndWalletResponse));

            mockMvc.perform(patch(api(RestApiPathUtility.API_PATH_MODULE_TRANSFER_TO_WALLET))
                            .header(HEADER_USER, userId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(commissionToWalletRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.results", notNullValue()));

            ArgumentCaptor<CommissionToWalletRequest> captor = ArgumentCaptor.forClass(CommissionToWalletRequest.class);
            verify(transactionService).transactionCommissionToWallet(eq(userId), captor.capture());
        }

        @Test
        @DisplayName("500: missing X-USER-ID header (mapped by Global Advice)")
        void patch_transfer_missing_header() throws Exception {
            mockMvc.perform(patch(api(RestApiPathUtility.API_PATH_MODULE_TRANSFER_TO_WALLET))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isInternalServerError());

            verifyNoInteractions(transactionService);
        }
    }

    @Nested
    @DisplayName("GET balance and wallet")
    class GetBalanceAndWalletTests {
        @Test
        @DisplayName("200: success")
        void get_balance_wallet_success() throws Exception {
            UUID userId = UUID.randomUUID();

            UserBalanceAndWalletResponse userBalanceAndWalletResponse = mock(UserBalanceAndWalletResponse.class);
            when(transactionService.getUserBalanceAndWallet(eq(userId)))
                    .thenReturn((RestApiResponse<UserBalanceAndWalletResponse>) okResponseTyped(userBalanceAndWalletResponse));

            mockMvc.perform(get(api(RestApiPathUtility.API_PATH_GET_BALANCE_AND_WALLET))
                            .header(HEADER_USER, userId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.results", notNullValue()));

            verify(transactionService).getUserBalanceAndWallet(eq(userId));
        }

        @Test
        @DisplayName("500: missing X-USER-ID header (mapped by Global Advice)")
        void get_balance_wallet_missing_header() throws Exception {
            mockMvc.perform(get(api(RestApiPathUtility.API_PATH_GET_BALANCE_AND_WALLET)))
                    .andExpect(status().isInternalServerError());

            verifyNoInteractions(transactionService);
        }
    }
}
