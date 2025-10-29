package com.jdt16.agenin.transaction.service;

import com.jdt16.agenin.transaction.configuration.security.SecurityConfig;
import com.jdt16.agenin.transaction.dto.entity.*;
import com.jdt16.agenin.transaction.dto.exception.CoreThrowHandlerException;
import com.jdt16.agenin.transaction.dto.request.CommissionToWalletRequest;
import com.jdt16.agenin.transaction.dto.request.TransactionRequest;
import com.jdt16.agenin.transaction.dto.response.*;
import com.jdt16.agenin.transaction.model.repository.*;
import com.jdt16.agenin.transaction.service.implementation.module.TransactionServiceImpl;
import com.jdt16.agenin.transaction.service.interfacing.module.CommissionValueProjection;
import com.jdt16.agenin.transaction.service.interfacing.module.ProductProjection;
import com.jdt16.agenin.transaction.service.interfacing.module.TransactionService;
import com.jdt16.agenin.transaction.service.implementation.module.AuditLogProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransactionServiceImplTest {

    @Mock
    private MUserRepositories mUserRepositories;
    @Mock
    private TTransactionRepositories tTransactionRepositories;
    @Mock
    private MProductsRepositories mProductsRepositories;
    @Mock
    private MUserBalanceRepositories mUserBalanceRepositories;
    @Mock
    private MUserWalletRepositories mUserWalletRepositories;
    @Mock
    private TUsersBalanceHistoricalRepositories tUsersBalanceHistoricalRepositories;
    @Mock
    private MCommissionRepositories mCommissionRepositories;
    @Mock
    private TTransactionOpenBankAccountRepositories tTransactionOpenBankAccountRepositories;
    @Mock
    private TUsersReferralRepositories tUsersReferralRepositories;
    @Mock
    private TUsersWalletHistoricalRepositories tUsersWalletHistoricalRepositories;
    @Mock
    private AuditLogProducerService auditLogProducerService;
    @Mock
    private SecurityConfig securityConfig;

    private TransactionService service;

    private UUID userId;
    private UUID parentUserId;
    private UUID productId;

    private final String RAW_PASSWORD = "Password123!";
    private PasswordEncoder realEncoder;

    @BeforeEach
    void setUp() {
        service = new TransactionServiceImpl(
                mUserRepositories,
                tTransactionRepositories,
                mProductsRepositories,
                mUserBalanceRepositories,
                mUserWalletRepositories,
                tUsersBalanceHistoricalRepositories,
                mCommissionRepositories,
                auditLogProducerService,
                tTransactionOpenBankAccountRepositories,
                tUsersReferralRepositories,
                tUsersWalletHistoricalRepositories
        );

        realEncoder = new BCryptPasswordEncoder();

        ReflectionTestUtils.setField(service, "securityConfig", securityConfig);
        when(securityConfig.passwordEncoder()).thenReturn(realEncoder);

        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
        parentUserId = UUID.randomUUID();
    }

    private UserEntityDTO mockExistingUser() {
        UserEntityDTO userEntityDTO = new UserEntityDTO();
        userEntityDTO.setUserEntityDTOId(userId);
        userEntityDTO.setUserEntityDTOFullName("Agent A");
        userEntityDTO.setUserEntityDTORoleId(UUID.randomUUID());
        userEntityDTO.setUserEntityDTORoleName("AGENT");

        String encoded = realEncoder.encode(RAW_PASSWORD);
        userEntityDTO.setUserEntityDTOPassword(encoded);

        assert encoded != null && !encoded.isEmpty();

        when(mUserRepositories.findByUserEntityDTOId(eq(userId)))
                .thenReturn(Optional.of(userEntityDTO));
        return userEntityDTO;
    }

    private CommissionToWalletRequest makeRequest(BigDecimal amount, String rawPassword) {
        CommissionToWalletRequest commissionToWalletRequest = new CommissionToWalletRequest();
        commissionToWalletRequest.setCommissionToWalletAmount(amount);
        commissionToWalletRequest.setUserEntityDTOPassword(rawPassword);
        return commissionToWalletRequest;
    }


    @BeforeEach
    void commonSaves() {
        when(mUserBalanceRepositories.save(any(UserBalanceEntityDTO.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(mUserWalletRepositories.save(any(UserWalletEntityDTO.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(tUsersWalletHistoricalRepositories.save(any(UserWalletHistoricalEntityDTO.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }


    private TransactionRequest buildTransactionRequest() {
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setTransactionEntityDTOCustomerName("Customer A");
        transactionRequest.setTransactionEntityDTOCustomerIdentityNumber("1234567890");
        transactionRequest.setTransactionEntityDTOCustomerPhoneNumber("08123456789");
        transactionRequest.setTransactionEntityDTOCustomerEmail("cust@example.com");
        transactionRequest.setTransactionEntityDTOCustomerAddress("123 Main Street");
        return transactionRequest;
    }

    /**
     * Product (projection) + commission (projection).
     */
    private void mockProductAndCommission() {
        ProductProjection productProjection = mock(ProductProjection.class);
        when(productProjection.getProductEntityDTOName()).thenReturn("Open Bank Account BCA");
        when(productProjection.getProductEntityDTOPrice()).thenReturn(new BigDecimal("100000"));
        when(mProductsRepositories.findByProductEntityDTOId(eq(productId)))
                .thenReturn(Optional.of(productProjection));

        CommissionValueProjection commissionProjection = mock(CommissionValueProjection.class);
        when(commissionProjection.getCommissionsEntityDTOValue()).thenReturn(new BigDecimal("5000"));
        when(mCommissionRepositories.findByCommissionsEntityDTOProductId(eq(productId)))
                .thenReturn(Optional.of(commissionProjection));
    }

    /**
     * Balance creation/update for all user.
     */
    private void mockUserBalanceCreation() {
        when(mUserBalanceRepositories.findByUserBalanceEntityDTOUserId(any(UUID.class)))
                .thenReturn(Optional.empty());

        when(mUserBalanceRepositories.save(any(UserBalanceEntityDTO.class)))
                .thenAnswer(inv -> {
                    UserBalanceEntityDTO userBalanceEntityDTO = inv.getArgument(0);
                    if (userBalanceEntityDTO.getUserBalanceEntityDTOId() == null) {
                        userBalanceEntityDTO.setUserBalanceEntityDTOId(UUID.randomUUID());
                    }
                    if (userBalanceEntityDTO.getUserBalanceEntityDTOBalanceAmount() == null) {
                        userBalanceEntityDTO.setUserBalanceEntityDTOBalanceAmount(BigDecimal.ZERO);
                    }
                    userBalanceEntityDTO.setUserBalanceEntityDTOBalanceLastUpdate(LocalDateTime.now());
                    return userBalanceEntityDTO;
                });
    }

    /**
     * Echo saves for TX / OpenBank / BalanceHistorical.
     */
    private void mockTransactionSaves() {
        when(tTransactionRepositories.save(any(TransactionEntityDTO.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(tTransactionOpenBankAccountRepositories.save(any(TransactionOpenBankAccountEntityDTO.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(tUsersBalanceHistoricalRepositories.save(any(UserBalanceHistoricalEntityDTO.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    /**
     * Mock role user.
     */
    private void mockUserRole(String role) {
        UserEntityDTO userEntityDTO = new UserEntityDTO();
        userEntityDTO.setUserEntityDTOId(userId);
        userEntityDTO.setUserEntityDTOEmail("agent@example.com");
        userEntityDTO.setUserEntityDTOFullName("Agent A");
        userEntityDTO.setUserEntityDTORoleName(role);
        userEntityDTO.setUserEntityDTORoleId(UUID.randomUUID());
        when(mUserRepositories.findByUserEntityDTOId(eq(userId))).thenReturn(Optional.of(userEntityDTO));
    }

    /**
     * Referral for SUB_AGENT (lookup parent).
     */
    private void mockReferralForSubAgent() {
        this.parentUserId = UUID.randomUUID();
        when(tUsersReferralRepositories.findReferenceUserIdByInviteeUserId(eq(userId)))
                .thenReturn(Optional.of(parentUserId));
    }

    @Nested
    @DisplayName("Inquiry")
    class Inquiry {

        @Nested
        @DisplayName("Success Cases")
        class Positive {

            @Test
            @DisplayName("200 OK inquiry(): AGENT → SUCCESS without referral")
            void inquiry_agent_success() {
                mockProductAndCommission();
                mockUserBalanceCreation();
                mockTransactionSaves();
                mockUserRole("AGENT");

                TransactionRequest transactionRequest = buildTransactionRequest();

                RestApiResponse<TransactionResponse> restApiResponse = service.inquiry(userId, productId, transactionRequest);

                assertThat(restApiResponse).isNotNull();
                assertThat(restApiResponse.getRestApiResponseCode()).isEqualTo(200);
                assertThat(restApiResponse.getRestApiResponseMessage()).isEqualTo("SUCCESS");
                assertThat(restApiResponse.getRestApiResponseResults()).isNotNull();

                verify(tTransactionRepositories).save(any(TransactionEntityDTO.class));
                verify(tTransactionOpenBankAccountRepositories).save(any(TransactionOpenBankAccountEntityDTO.class));

                verify(mCommissionRepositories, atLeast(2))
                        .findByCommissionsEntityDTOProductId(eq(productId));

                verify(mUserBalanceRepositories, atLeastOnce()).save(any(UserBalanceEntityDTO.class));
                verify(tUsersBalanceHistoricalRepositories).save(any(UserBalanceHistoricalEntityDTO.class));

                verify(mProductsRepositories, atLeastOnce()).findByProductEntityDTOId(eq(productId));

                verifyNoInteractions(mUserWalletRepositories, tUsersWalletHistoricalRepositories);
            }

            @Test
            @DisplayName("200 OK inquiry(): SUB_AGENT → SUCCESS + referral commission to parent")
            void inquiry_subAgent_success() {
                mockProductAndCommission();
                mockUserBalanceCreation();
                mockTransactionSaves();
                mockUserRole("SUB_AGENT");
                mockReferralForSubAgent();

                TransactionRequest transactionRequest = buildTransactionRequest();

                RestApiResponse<TransactionResponse> restApiResponse =
                        service.inquiry(userId, productId, transactionRequest);

                assertThat(restApiResponse).isNotNull();
                assertThat(restApiResponse.getRestApiResponseCode()).isEqualTo(200);
                assertThat(restApiResponse.getRestApiResponseMessage()).isEqualTo("SUCCESS");
                assertThat(restApiResponse.getRestApiResponseResults()).isNotNull();
                assertThat(restApiResponse.getRestApiResponseResults().getTransactionEntityDTOCode()).isNotBlank();

                verify(tTransactionRepositories).save(any(TransactionEntityDTO.class));
                verify(tTransactionOpenBankAccountRepositories).save(any(TransactionOpenBankAccountEntityDTO.class));

                verify(mUserBalanceRepositories, times(2)).save(any(UserBalanceEntityDTO.class));
                verify(tUsersBalanceHistoricalRepositories, atLeast(2))
                        .save(any(UserBalanceHistoricalEntityDTO.class));

                verify(tUsersReferralRepositories).findReferenceUserIdByInviteeUserId(eq(userId));

                verify(mProductsRepositories, atLeastOnce()).findByProductEntityDTOId(eq(productId));
                verify(mCommissionRepositories, atLeast(2))
                        .findByCommissionsEntityDTOProductId(eq(productId));

                ArgumentCaptor<UserBalanceEntityDTO> balCaptor = ArgumentCaptor.forClass(UserBalanceEntityDTO.class);
                verify(mUserBalanceRepositories, atLeast(2)).save(balCaptor.capture());
                UserBalanceEntityDTO finalSave = balCaptor.getAllValues().get(balCaptor.getAllValues().size() - 1);
                assertThat(finalSave.getUserBalanceEntityDTOBalanceAmount())
                        .isNotNull()
                        .isGreaterThan(BigDecimal.ZERO);

                verifyNoInteractions(mUserWalletRepositories, tUsersWalletHistoricalRepositories);
            }
        }

        @Nested
        @DisplayName("Negative")
        class Negative {

            @Test
            @DisplayName("404 inquiry(): user not found")
            void inquiry_userNotFound_throws() {
                mockProductAndCommission();
                mockUserBalanceCreation();
                mockTransactionSaves();

                when(mUserRepositories.findByUserEntityDTOId(eq(userId)))
                        .thenReturn(Optional.empty());

                TransactionRequest transactionRequest = buildTransactionRequest();

                assertThatThrownBy(() -> service.inquiry(userId, productId, transactionRequest))
                        .isInstanceOf(CoreThrowHandlerException.class)
                        .hasMessageContaining("User not found");

                verify(mUserRepositories).findByUserEntityDTOId(eq(userId));
                verify(mProductsRepositories, atLeastOnce()).findByProductEntityDTOId(eq(productId));

                verifyNoInteractions(mUserWalletRepositories, tUsersWalletHistoricalRepositories);
            }

            @Test
            @DisplayName("400 Bad Request ('Transaction FAILED.')")
            void inquiry_unknownRole_throws() {
                mockProductAndCommission();
                mockUserBalanceCreation();
                mockTransactionSaves();
                mockUserRole("VIEWER");

                TransactionRequest transactionRequest = buildTransactionRequest();

                assertThatThrownBy(() -> service.inquiry(userId, productId, transactionRequest))
                        .isInstanceOf(CoreThrowHandlerException.class)
                        .hasMessage("Transaction FAILED.");

                verify(tTransactionRepositories).save(any(TransactionEntityDTO.class));
                verify(tTransactionOpenBankAccountRepositories).save(any(TransactionOpenBankAccountEntityDTO.class));
                verify(mProductsRepositories, atLeastOnce()).findByProductEntityDTOId(eq(productId));

                verifyNoInteractions(mUserWalletRepositories, tUsersWalletHistoricalRepositories);
            }
        }
    }

    @Nested
    @DisplayName("getListProducts")
    class GetListProducts {
        @Test
        @DisplayName("200 OK: (negative) empty → return empty list")
        void getListProducts_empty() {
            when(mProductsRepositories.findAll()).thenReturn(Collections.emptyList());

            RestApiResponse<List<ProductsResponse>> restApiResponse = service.getListProducts();

            assertThat(restApiResponse).isNotNull();
            assertThat(restApiResponse.getRestApiResponseCode()).isEqualTo(200);
            assertThat(restApiResponse.getRestApiResponseMessage()).isEqualTo("SUCCESS GET Products");
            assertThat(restApiResponse.getRestApiResponseResults()).isNotNull().isEmpty();

            verify(mProductsRepositories, times(1)).findAll();
            verifyNoMoreInteractions(mProductsRepositories);
        }

        @Test
        @DisplayName("200 OK: non-empty → return list of products (with commission projections)")
        void getListProducts_nonEmpty_withCommissionProjection() {
            UUID uuid = UUID.randomUUID();
            UUID uuid1 = UUID.randomUUID();

            ProductsEntityDTO productsEntityDTO = mock(ProductsEntityDTO.class);
            ProductsEntityDTO productsEntityDTO1 = mock(ProductsEntityDTO.class);

            when(productsEntityDTO.getProductEntityDTOId()).thenReturn(uuid);
            when(productsEntityDTO1.getProductEntityDTOId()).thenReturn(uuid1);

            when(productsEntityDTO.getProductEntityDTOName()).thenReturn("Open Bank Account BCA");
            when(productsEntityDTO1.getProductEntityDTOName()).thenReturn("Open Bank Account BRI");

            when(productsEntityDTO.getProductEntityDTOPrice()).thenReturn(new BigDecimal("1000000"));
            when(productsEntityDTO1.getProductEntityDTOPrice()).thenReturn(new BigDecimal("2000000"));

            when(mProductsRepositories.findAll()).thenReturn(List.of(productsEntityDTO, productsEntityDTO1));

            CommissionValueProjection commissionValueProjection = mock(CommissionValueProjection.class);
            when(commissionValueProjection.getCommissionsEntityDTOProductId()).thenReturn(uuid);
            when(commissionValueProjection.getCommissionsEntityDTOValue()).thenReturn(new BigDecimal("150000"));

            CommissionValueProjection commissionValueProjection1 = mock(CommissionValueProjection.class);
            when(commissionValueProjection1.getCommissionsEntityDTOProductId()).thenReturn(uuid1);
            when(commissionValueProjection1.getCommissionsEntityDTOValue()).thenReturn(new BigDecimal("175000"));

            when(mCommissionRepositories.findByCommissionsEntityDTOProductId(uuid))
                    .thenReturn(Optional.of(commissionValueProjection));
            when(mCommissionRepositories.findByCommissionsEntityDTOProductId(uuid1))
                    .thenReturn(Optional.of(commissionValueProjection1));
            RestApiResponse<List<ProductsResponse>> listRestApiResponse = service.getListProducts();

            assertThat(listRestApiResponse).isNotNull();
            assertThat(listRestApiResponse.getRestApiResponseCode()).isEqualTo(200);
            assertThat(listRestApiResponse.getRestApiResponseMessage()).isEqualTo("SUCCESS GET Products");
            assertThat(listRestApiResponse.getRestApiResponseResults()).isNotNull().hasSize(2);

            List<ProductsResponse> results = listRestApiResponse.getRestApiResponseResults();
            ProductsResponse productsResponse = results.stream().filter(r -> "Open Bank Account BCA".equals(r.getProductEntityDTOName())).findFirst().orElseThrow();
            ProductsResponse productsResponse1 = results.stream().filter(r -> "Open Bank Account BRI".equals(r.getProductEntityDTOName())).findFirst().orElseThrow();

            assertThat(productsResponse.getProductEntityDTOPrice()).isEqualByComparingTo("1000000");
            assertThat(productsResponse1.getProductEntityDTOPrice()).isEqualByComparingTo("2000000");

            verify(mProductsRepositories, times(1)).findAll();
            verify(mCommissionRepositories, times(1)).findByCommissionsEntityDTOProductId(uuid);
            verify(mCommissionRepositories, times(1)).findByCommissionsEntityDTOProductId(uuid1);
            verifyNoMoreInteractions(mProductsRepositories, mCommissionRepositories);
        }
    }

    @Nested
    @DisplayName("getAllTransactionsByUser()")
    class GetAllTransactionsByUser {

        @Test
        @DisplayName("200 OK (Positive): transactions exist → return sorted list with details")
        void getAllTransactionsByUser_positive_success() {
            UUID uuid = userId;
            UUID trx1 = UUID.randomUUID();
            TransactionEntityDTO transactionEntityDTO = TransactionEntityDTO.builder()
                    .transactionEntityDTOId(trx1)
                    .transactionEntityDTOUserId(uuid)
                    .transactionEntityDTOProductName("Open Bank Account BCA")
                    .transactionEntityDTOProductPrice(new BigDecimal("100000"))
                    .transactionEntityDTODate(LocalDateTime.now().minusDays(1))
                    .transactionEntityDTOStatus("SUCCESS")
                    .build();

            UUID trx2 = UUID.randomUUID();
            TransactionEntityDTO transactionEntityDTO1 = TransactionEntityDTO.builder()
                    .transactionEntityDTOId(trx2)
                    .transactionEntityDTOUserId(uuid)
                    .transactionEntityDTOProductName("Open Bank Account BRI")
                    .transactionEntityDTOProductPrice(new BigDecimal("50000"))
                    .transactionEntityDTODate(LocalDateTime.now().minusDays(3))
                    .transactionEntityDTOStatus("SUCCESS")
                    .build();

            when(tTransactionRepositories.findByTransactionEntityDTOUserId(eq(uuid)))
                    .thenReturn(List.of(transactionEntityDTO1, transactionEntityDTO));

            TransactionOpenBankAccountEntityDTO transactionOpenBankAccountEntityDTO = new TransactionOpenBankAccountEntityDTO();
            transactionOpenBankAccountEntityDTO.setTransactionOpenBankAccountEntityDTOId(UUID.randomUUID());
            transactionOpenBankAccountEntityDTO.setTransactionOpenBankAccountEntityDTOTransactionId(trx1);
            transactionOpenBankAccountEntityDTO.setTransactionOpenBankAccountEntityDTOCustomerName("Alice");
            transactionOpenBankAccountEntityDTO.setTransactionOpenBankAccountEntityDTOCustomerIdentityNumber("111222333");
            transactionOpenBankAccountEntityDTO.setTransactionOpenBankAccountEntityDTOCustomerPhoneNumber("08123456789");
            transactionOpenBankAccountEntityDTO.setTransactionOpenBankAccountEntityDTOCustomerEmail("alice@example.com");
            transactionOpenBankAccountEntityDTO.setTransactionOpenBankAccountEntityDTOCustomerAddress("Jl. Mawar No. 1");

            when(tTransactionOpenBankAccountRepositories
                    .findByTransactionOpenBankAccountEntityDTOTransactionId(eq(trx1)))
                    .thenReturn(Optional.of(transactionOpenBankAccountEntityDTO));

            when(tTransactionOpenBankAccountRepositories
                    .findByTransactionOpenBankAccountEntityDTOTransactionId(eq(trx2)))
                    .thenReturn(Optional.empty());

            RestApiResponse<List<CustomerOpenBankAccountResponse>> restApiResponse =
                    service.getAllTransactionsByUser(uuid);

            assertThat(restApiResponse).isNotNull();
            assertThat(restApiResponse.getRestApiResponseCode()).isEqualTo(200);
            assertThat(restApiResponse.getRestApiResponseMessage()).isEqualTo("SUCCESS GET all transactions by user");
            assertThat(restApiResponse.getRestApiResponseResults()).isNotNull().hasSize(2);

            CustomerOpenBankAccountResponse customerOpenBankAccountResponse = restApiResponse.getRestApiResponseResults().get(0);
            CustomerOpenBankAccountResponse customerOpenBankAccountResponse1 = restApiResponse.getRestApiResponseResults().get(1);

            assertThat(customerOpenBankAccountResponse.getCustomerOpenBankAccountProductName()).isEqualTo("Open Bank Account BCA");
            assertThat(customerOpenBankAccountResponse.getCustomerOpenBankAccountProductPrice()).isEqualByComparingTo("100000");
            assertThat(customerOpenBankAccountResponse.getCustomerOpenBankAccountTransactionStatus()).isEqualTo("SUCCESS");
            assertThat(customerOpenBankAccountResponse.getCustomerOpenBankAccountName()).isEqualTo("Alice");
            assertThat(customerOpenBankAccountResponse.getCustomerOpenBankAccountIdentityNumber()).isEqualTo("111222333");
            assertThat(customerOpenBankAccountResponse.getCustomerOpenBankAccountPhoneNumber()).isEqualTo("08123456789");
            assertThat(customerOpenBankAccountResponse.getCustomerOpenBankAccountEmail()).isEqualTo("alice@example.com");
            assertThat(customerOpenBankAccountResponse.getCustomerOpenBankAccountAddress()).isEqualTo("Jl. Mawar No. 1");

            assertThat(customerOpenBankAccountResponse1.getCustomerOpenBankAccountProductName()).isEqualTo("Open Bank Account BRI");
            assertThat(customerOpenBankAccountResponse1.getCustomerOpenBankAccountProductPrice()).isEqualByComparingTo("50000");
            assertThat(customerOpenBankAccountResponse1.getCustomerOpenBankAccountTransactionStatus()).isEqualTo("SUCCESS");
            assertThat(customerOpenBankAccountResponse1.getCustomerOpenBankAccountName()).isNull();
            assertThat(customerOpenBankAccountResponse1.getCustomerOpenBankAccountIdentityNumber()).isNull();
            assertThat(customerOpenBankAccountResponse1.getCustomerOpenBankAccountPhoneNumber()).isNull();
            assertThat(customerOpenBankAccountResponse1.getCustomerOpenBankAccountEmail()).isNull();
            assertThat(customerOpenBankAccountResponse1.getCustomerOpenBankAccountAddress()).isNull();

            verify(tTransactionRepositories, times(1))
                    .findByTransactionEntityDTOUserId(eq(uuid));
            verify(tTransactionOpenBankAccountRepositories, times(1))
                    .findByTransactionOpenBankAccountEntityDTOTransactionId(eq(trx1));
            verify(tTransactionOpenBankAccountRepositories, times(1))
                    .findByTransactionOpenBankAccountEntityDTOTransactionId(eq(trx2));
            verifyNoMoreInteractions(tTransactionRepositories, tTransactionOpenBankAccountRepositories);
        }

        @Test
        @DisplayName("200 OK (Negative): No transactions → return empty list")
        void getAllTransactionsByUser_negative_empty() {
            UUID uid = userId;
            when(tTransactionRepositories.findByTransactionEntityDTOUserId(eq(uid)))
                    .thenReturn(Collections.emptyList());

            RestApiResponse<List<CustomerOpenBankAccountResponse>> restApiResponse =
                    service.getAllTransactionsByUser(uid);

            assertThat(restApiResponse).isNotNull();
            assertThat(restApiResponse.getRestApiResponseCode()).isEqualTo(200);
            assertThat(restApiResponse.getRestApiResponseMessage()).isEqualTo("FAILED GET all transactions by user");
            assertThat(restApiResponse.getRestApiResponseResults()).isNotNull().isEmpty();

            verify(tTransactionRepositories, times(1))
                    .findByTransactionEntityDTOUserId(eq(uid));
            verifyNoInteractions(tTransactionOpenBankAccountRepositories);
            verifyNoMoreInteractions(tTransactionRepositories);
        }
    }

    @Nested
    @DisplayName("transactionCommissionToWallet()")
    class TransactionCommissionToWallet {

        @Nested
        @DisplayName("Positive Cases")
        class Positive {

            @Test
            @DisplayName("200: successful transfer → decrease balance, increase wallet (existing wallet)")
            void success_existing_wallet() {
                mockExistingUser();

                BigDecimal transfer = new BigDecimal("25000");
                CommissionToWalletRequest commissionToWalletRequest = makeRequest(transfer, RAW_PASSWORD);

                UserBalanceEntityDTO userBalanceEntityDTO = UserBalanceEntityDTO.builder()
                        .userBalanceEntityDTOId(UUID.randomUUID())
                        .userBalanceEntityDTOUserId(userId)
                        .userBalanceEntityDTOBalanceAmount(new BigDecimal("100000"))
                        .userBalanceEntityDTOBalanceLastUpdate(LocalDateTime.now().minusDays(1))
                        .build();

                UserWalletEntityDTO userWalletEntityDTO = new UserWalletEntityDTO();
                userWalletEntityDTO.setUserWalletEntityDTOId(UUID.randomUUID());
                userWalletEntityDTO.setUserWalletEntityDTOUserId(userId);
                userWalletEntityDTO.setUserWalletEntityDTOAmount(new BigDecimal("5000"));
                userWalletEntityDTO.setUserWalletEntityDTOLastUpdate(LocalDateTime.now().minusDays(2));

                when(mUserBalanceRepositories.findByUserBalanceEntityDTOUserId(eq(userId)))
                        .thenReturn(Optional.of(userBalanceEntityDTO));
                when(mUserWalletRepositories.findByUserWalletEntityDTOUserId(eq(userId)))
                        .thenReturn(Optional.of(userWalletEntityDTO));

                RestApiResponse<UserBalanceResponse> restApiResponse =
                        service.transactionCommissionToWallet(userId, commissionToWalletRequest);

                assertThat(restApiResponse).isNotNull();
                assertThat(restApiResponse.getRestApiResponseCode()).isEqualTo(200);
                assertThat(restApiResponse.getRestApiResponseMessage())
                        .isEqualTo("SUCCESS transfer commission to wallet");
                assertThat(restApiResponse.getRestApiResponseResults()).isNotNull();
                assertThat(restApiResponse.getRestApiResponseResults().getUserBalanceEntityDTOUserId())
                        .isEqualTo(userId);
                assertThat(restApiResponse.getRestApiResponseResults().getUserBalanceEntityDTOUserAmount())
                        .isEqualByComparingTo("75000");
                assertThat(restApiResponse.getRestApiResponseResults().getUserBalanceEntityDTOUserWalletAmount())
                        .isEqualByComparingTo("30000");

                ArgumentCaptor<UserBalanceEntityDTO> balCap = ArgumentCaptor.forClass(UserBalanceEntityDTO.class);
                verify(mUserBalanceRepositories).save(balCap.capture());
                assertThat(balCap.getValue().getUserBalanceEntityDTOBalanceAmount())
                        .isEqualByComparingTo("75000");
                assertThat(balCap.getValue().getUserBalanceEntityDTOBalanceLastUpdate())
                        .isNotNull();

                ArgumentCaptor<UserWalletEntityDTO> walCap = ArgumentCaptor.forClass(UserWalletEntityDTO.class);
                verify(mUserWalletRepositories).save(walCap.capture());
                assertThat(walCap.getValue().getUserWalletEntityDTOAmount())
                        .isEqualByComparingTo("30000");
                assertThat(walCap.getValue().getUserWalletEntityDTOLastUpdate())
                        .isNotNull();

                verify(tUsersWalletHistoricalRepositories)
                        .save(any(UserWalletHistoricalEntityDTO.class));

                verify(mUserRepositories).findByUserEntityDTOId(eq(userId));
                verify(mUserBalanceRepositories).findByUserBalanceEntityDTOUserId(eq(userId));
                verify(mUserWalletRepositories).findByUserWalletEntityDTOUserId(eq(userId));
                verifyNoMoreInteractions(mUserRepositories, mUserBalanceRepositories, mUserWalletRepositories);
            }
        }

        @Nested
        @DisplayName("Negative Cases")
        class Negative {

            @Test
            @DisplayName("401/400: invalid password → stop before reading balance/wallet")
            void invalid_password_throws() {
                mockExistingUser();

                CommissionToWalletRequest commissionToWalletRequest = makeRequest(new BigDecimal("10000"), "WrongPassword!");

                assertThatThrownBy(() -> service.transactionCommissionToWallet(userId, commissionToWalletRequest))
                        .isInstanceOf(CoreThrowHandlerException.class)
                        .hasMessageContaining("Invalid password");

                verify(mUserRepositories).findByUserEntityDTOId(eq(userId));
                verifyNoInteractions(mUserBalanceRepositories, mUserWalletRepositories, tUsersWalletHistoricalRepositories);
            }

            @Test
            @DisplayName("400: amount null/zero")
            void amount_invalid_throws() {
                mockExistingUser();

                CommissionToWalletRequest commissionToWalletRequest = makeRequest(BigDecimal.ZERO, RAW_PASSWORD);

                assertThatThrownBy(() -> service.transactionCommissionToWallet(userId, commissionToWalletRequest))
                        .isInstanceOf(CoreThrowHandlerException.class)
                        .hasMessageContaining("greater than zero");

                verify(mUserRepositories).findByUserEntityDTOId(eq(userId));
                verifyNoInteractions(mUserBalanceRepositories, mUserWalletRepositories, tUsersWalletHistoricalRepositories);
            }

            @Test
            @DisplayName("404: user balance not found")
            void balance_not_found_throws() {
                mockExistingUser();

                CommissionToWalletRequest commissionToWalletRequest = makeRequest(new BigDecimal("10000"), RAW_PASSWORD);

                when(mUserBalanceRepositories.findByUserBalanceEntityDTOUserId(eq(userId)))
                        .thenReturn(Optional.empty());

                assertThatThrownBy(() -> service.transactionCommissionToWallet(userId, commissionToWalletRequest))
                        .isInstanceOf(CoreThrowHandlerException.class)
                        .hasMessageContaining("User balance not found");

                verify(mUserRepositories).findByUserEntityDTOId(eq(userId));
                verify(mUserBalanceRepositories).findByUserBalanceEntityDTOUserId(eq(userId));
                verifyNoMoreInteractions(mUserBalanceRepositories);
                verifyNoInteractions(mUserWalletRepositories, tUsersWalletHistoricalRepositories);
            }

            @Test
            @DisplayName("400: commission balance insufficient (Your balance is less than the transfer amount.)")
            void insufficient_balance_throws() {
                mockExistingUser();

                CommissionToWalletRequest commissionToWalletRequest = makeRequest(new BigDecimal("20000"), RAW_PASSWORD);

                UserBalanceEntityDTO userBalanceEntityDTO = UserBalanceEntityDTO.builder()
                        .userBalanceEntityDTOId(UUID.randomUUID())
                        .userBalanceEntityDTOUserId(userId)
                        .userBalanceEntityDTOBalanceAmount(new BigDecimal("15000"))
                        .userBalanceEntityDTOBalanceLastUpdate(LocalDateTime.now().minusDays(1))
                        .build();

                when(mUserBalanceRepositories.findByUserBalanceEntityDTOUserId(eq(userId)))
                        .thenReturn(Optional.of(userBalanceEntityDTO));

                assertThatThrownBy(() -> service.transactionCommissionToWallet(userId, commissionToWalletRequest))
                        .isInstanceOf(CoreThrowHandlerException.class)
                        .hasMessageContaining("Insufficient commission balance");

                verify(mUserRepositories).findByUserEntityDTOId(eq(userId));
                verify(mUserBalanceRepositories).findByUserBalanceEntityDTOUserId(eq(userId));
                verifyNoMoreInteractions(mUserBalanceRepositories);
                verifyNoInteractions(mUserWalletRepositories, tUsersWalletHistoricalRepositories);
            }
        }
    }

    @Nested
    @DisplayName("getUserBalanceAndWallet")
    class GetUserBalanceAndWallet {
        @Nested
        @DisplayName("Positive Cases")
        class Positive {
            @Test
            @DisplayName("200 OK: returns balance & wallet amounts")
            void getUserBalanceAndWallet_success() {
                UUID uid = userId;
                UserBalanceEntityDTO userBalanceEntityDTO = UserBalanceEntityDTO.builder()
                        .userBalanceEntityDTOId(UUID.randomUUID())
                        .userBalanceEntityDTOUserId(uid)
                        .userBalanceEntityDTOBalanceAmount(new BigDecimal("150000"))
                        .userBalanceEntityDTOBalanceLastUpdate(LocalDateTime.now().minusHours(1))
                        .build();

                UserWalletEntityDTO userWalletEntityDTO = UserWalletEntityDTO.builder()
                        .userWalletEntityDTOId(UUID.randomUUID())
                        .userWalletEntityDTOUserId(uid)
                        .userWalletEntityDTOAmount(new BigDecimal("27500"))
                        .userWalletEntityDTOLastUpdate(LocalDateTime.now().minusMinutes(10))
                        .build();

                when(mUserBalanceRepositories.findByUserBalanceEntityDTOUserId(eq(uid)))
                        .thenReturn(Optional.of(userBalanceEntityDTO));
                when(mUserWalletRepositories.findByUserWalletEntityDTOUserId(eq(uid)))
                        .thenReturn(Optional.of(userWalletEntityDTO));

                RestApiResponse<UserBalanceAndWalletResponse> restApiResponse =
                        service.getUserBalanceAndWallet(uid);

                assertThat(restApiResponse).isNotNull();
                assertThat(restApiResponse.getRestApiResponseCode()).isEqualTo(200);
                assertThat(restApiResponse.getRestApiResponseMessage()).isEqualTo("SUCCESS GET user balance and wallet");
                assertThat(restApiResponse.getRestApiResponseResults()).isNotNull();
                assertThat(restApiResponse.getRestApiResponseResults().getUserBalanceEntityDTOAmount())
                        .isEqualByComparingTo("150000");
                assertThat(restApiResponse.getRestApiResponseResults().getUserWalletEntityDTOAmount())
                        .isEqualByComparingTo("27500");

                verify(mUserBalanceRepositories).findByUserBalanceEntityDTOUserId(eq(uid));
                verify(mUserWalletRepositories).findByUserWalletEntityDTOUserId(eq(uid));
                verifyNoMoreInteractions(mUserBalanceRepositories, mUserWalletRepositories);
            }
        }

        @Nested
        @DisplayName("Negative Cases")
        class Negative {
            @Test
            @DisplayName("404: throws when user balance not found")
            void getUserBalanceAndWallet_balanceNotFound_throws() {
                UUID uid = userId;
                when(mUserBalanceRepositories.findByUserBalanceEntityDTOUserId(eq(uid)))
                        .thenReturn(Optional.empty());

                assertThatThrownBy(() -> service.getUserBalanceAndWallet(uid))
                        .isInstanceOf(CoreThrowHandlerException.class)
                        .hasMessageContaining("User balance not found for user: " + uid);

                verify(mUserBalanceRepositories).findByUserBalanceEntityDTOUserId(eq(uid));
                verifyNoInteractions(mUserWalletRepositories);
            }

            @Test
            @DisplayName("404: throws when user wallet not found")
            void getUserBalanceAndWallet_walletNotFound_throws() {
                UUID uid = userId;
                UserBalanceEntityDTO userBalanceEntityDTO = UserBalanceEntityDTO.builder()
                        .userBalanceEntityDTOId(UUID.randomUUID())
                        .userBalanceEntityDTOUserId(uid)
                        .userBalanceEntityDTOBalanceAmount(new BigDecimal("50000"))
                        .userBalanceEntityDTOBalanceLastUpdate(LocalDateTime.now())
                        .build();

                when(mUserBalanceRepositories.findByUserBalanceEntityDTOUserId(eq(uid)))
                        .thenReturn(Optional.of(userBalanceEntityDTO));
                when(mUserWalletRepositories.findByUserWalletEntityDTOUserId(eq(uid)))
                        .thenReturn(Optional.empty());

                assertThatThrownBy(() -> service.getUserBalanceAndWallet(uid))
                        .isInstanceOf(CoreThrowHandlerException.class)
                        .hasMessageContaining("User wallet not found for user: " + uid);

                verify(mUserBalanceRepositories).findByUserBalanceEntityDTOUserId(eq(uid));
                verify(mUserWalletRepositories).findByUserWalletEntityDTOUserId(eq(uid));
            }
        }
    }
}