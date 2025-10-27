package com.jdt16.agenin.transaction.service;

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
    private MUserWalletRepositories mUserWalletRepositories; // tidak dipakai di inquiry()
    @Mock
    private TUsersBalanceHistoricalRepositories tUsersBalanceHistoricalRepositories;
    @Mock
    private MCommissionRepositories mCommissionRepositories;
    @Mock
    private TTransactionOpenBankAccountRepositories tTransactionOpenBankAccountRepositories;
    @Mock
    private TUsersReferralRepositories tUsersReferralRepositories;
    @Mock
    private TUsersWalletHistoricalRepositories tUsersWalletHistoricalRepositories; // tidak dipakai di inquiry()
    @Mock
    private AuditLogProducerService auditLogProducerService;

    private TransactionService service;

    private UUID userId;
    private UUID parentUserId;
    private UUID productId;

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
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
        parentUserId = UUID.randomUUID();
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
        TransactionRequest req = new TransactionRequest();
        req.setTransactionEntityDTOCustomerName("Customer A");
        req.setTransactionEntityDTOCustomerIdentityNumber("1234567890");
        req.setTransactionEntityDTOCustomerPhoneNumber("08123456789");
        req.setTransactionEntityDTOCustomerEmail("cust@example.com");
        req.setTransactionEntityDTOCustomerAddress("123 Main Street");
        return req;
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
     * Balance creation/update aman untuk SEMUA user (termasuk parent).
     */
    private void mockUserBalanceCreation() {
        when(mUserBalanceRepositories.findByUserBalanceEntityDTOUserId(any(UUID.class)))
                .thenReturn(Optional.empty());

        when(mUserBalanceRepositories.save(any(UserBalanceEntityDTO.class)))
                .thenAnswer(inv -> {
                    UserBalanceEntityDTO b = inv.getArgument(0);
                    if (b.getUserBalanceEntityDTOId() == null) {
                        b.setUserBalanceEntityDTOId(UUID.randomUUID());
                    }
                    if (b.getUserBalanceEntityDTOBalanceAmount() == null) {
                        b.setUserBalanceEntityDTOBalanceAmount(BigDecimal.ZERO);
                    }
                    b.setUserBalanceEntityDTOBalanceLastUpdate(LocalDateTime.now());
                    return b;
                });
    }

    /**
     * Echo saves untuk TX / OpenBank / BalanceHistorical.
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
        UserEntityDTO user = new UserEntityDTO();
        user.setUserEntityDTOId(userId);
        user.setUserEntityDTOEmail("agent@example.com");
        user.setUserEntityDTOFullName("Agent A");
        user.setUserEntityDTORoleName(role);
        user.setUserEntityDTORoleId(UUID.randomUUID());
        when(mUserRepositories.findByUserEntityDTOId(eq(userId))).thenReturn(Optional.of(user));
    }

    /**
     * Referral untuk SUB_AGENT (hanya lookup parent).
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

                TransactionRequest req = buildTransactionRequest();

                RestApiResponse<TransactionResponse> res = service.inquiry(userId, productId, req);

                assertThat(res).isNotNull();
                assertThat(res.getRestApiResponseCode()).isEqualTo(200);
                assertThat(res.getRestApiResponseMessage()).isEqualTo("SUCCESS");
                assertThat(res.getRestApiResponseResults()).isNotNull();

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

                RestApiResponse<TransactionResponse> res =
                        service.inquiry(userId, productId, transactionRequest);

                assertThat(res).isNotNull();
                assertThat(res.getRestApiResponseCode()).isEqualTo(200);
                assertThat(res.getRestApiResponseMessage()).isEqualTo("SUCCESS");
                assertThat(res.getRestApiResponseResults()).isNotNull();
                assertThat(res.getRestApiResponseResults().getTransactionEntityDTOCode()).isNotBlank();

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

                TransactionRequest req = buildTransactionRequest();

                assertThatThrownBy(() -> service.inquiry(userId, productId, req))
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

                TransactionRequest req = buildTransactionRequest();

                assertThatThrownBy(() -> service.inquiry(userId, productId, req))
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

            RestApiResponse<List<ProductsResponse>> res = service.getListProducts();

            assertThat(res).isNotNull();
            assertThat(res.getRestApiResponseCode()).isEqualTo(200);
            assertThat(res.getRestApiResponseMessage()).isEqualTo("SUCCESS GET Products");
            assertThat(res.getRestApiResponseResults()).isNotNull().isEmpty();

            verify(mProductsRepositories, times(1)).findAll();
            verifyNoMoreInteractions(mProductsRepositories);
        }

        @Test
        @DisplayName("200 OK: non-empty → return list of products (with commission projections)")
        void getListProducts_nonEmpty_withCommissionProjection() {
            UUID p1 = UUID.randomUUID();
            UUID p2 = UUID.randomUUID();

            ProductsEntityDTO e1 = mock(ProductsEntityDTO.class);
            ProductsEntityDTO e2 = mock(ProductsEntityDTO.class);

            when(e1.getProductEntityDTOId()).thenReturn(p1);
            when(e2.getProductEntityDTOId()).thenReturn(p2);

            when(e1.getProductEntityDTOName()).thenReturn("Open Bank Account BCA");
            when(e2.getProductEntityDTOName()).thenReturn("Open Bank Account BRI");

            when(e1.getProductEntityDTOPrice()).thenReturn(new BigDecimal("1000000"));
            when(e2.getProductEntityDTOPrice()).thenReturn(new BigDecimal("2000000"));

            when(mProductsRepositories.findAll()).thenReturn(List.of(e1, e2));

            CommissionValueProjection proj1 = mock(CommissionValueProjection.class);
            when(proj1.getCommissionsEntityDTOProductId()).thenReturn(p1);
            when(proj1.getCommissionsEntityDTOValue()).thenReturn(new BigDecimal("150000"));

            CommissionValueProjection proj2 = mock(CommissionValueProjection.class);
            when(proj2.getCommissionsEntityDTOProductId()).thenReturn(p2);
            when(proj2.getCommissionsEntityDTOValue()).thenReturn(new BigDecimal("175000"));

            when(mCommissionRepositories.findByCommissionsEntityDTOProductId(p1))
                    .thenReturn(Optional.of(proj1));
            when(mCommissionRepositories.findByCommissionsEntityDTOProductId(p2))
                    .thenReturn(Optional.of(proj2));
            RestApiResponse<List<ProductsResponse>> res = service.getListProducts();

            assertThat(res).isNotNull();
            assertThat(res.getRestApiResponseCode()).isEqualTo(200);
            assertThat(res.getRestApiResponseMessage()).isEqualTo("SUCCESS GET Products");
            assertThat(res.getRestApiResponseResults()).isNotNull().hasSize(2);

            List<ProductsResponse> results = res.getRestApiResponseResults();
            ProductsResponse r1 = results.stream().filter(r -> "Open Bank Account BCA".equals(r.getProductEntityDTOName())).findFirst().orElseThrow();
            ProductsResponse r2 = results.stream().filter(r -> "Open Bank Account BRI".equals(r.getProductEntityDTOName())).findFirst().orElseThrow();

            assertThat(r1.getProductEntityDTOPrice()).isEqualByComparingTo("1000000");
            assertThat(r2.getProductEntityDTOPrice()).isEqualByComparingTo("2000000");

            verify(mProductsRepositories, times(1)).findAll();
            verify(mCommissionRepositories, times(1)).findByCommissionsEntityDTOProductId(p1);
            verify(mCommissionRepositories, times(1)).findByCommissionsEntityDTOProductId(p2);
            verifyNoMoreInteractions(mProductsRepositories, mCommissionRepositories);
        }
    }

    @Nested
    @DisplayName("getAllTransactionsByUser()")
    class GetAllTransactionsByUser {

        @Test
        @DisplayName("200 OK (Positive): transactions exist → return sorted list with details")
        void getAllTransactionsByUser_positive_success() {
            UUID uid = userId;
            UUID trx1 = UUID.randomUUID();
            TransactionEntityDTO tx1 = TransactionEntityDTO.builder()
                    .transactionEntityDTOId(trx1)
                    .transactionEntityDTOUserId(uid)
                    .transactionEntityDTOProductName("Open Bank Account BCA")
                    .transactionEntityDTOProductPrice(new BigDecimal("100000"))
                    .transactionEntityDTODate(LocalDateTime.now().minusDays(1))
                    .transactionEntityDTOStatus("SUCCESS")
                    .build();

            UUID trx2 = UUID.randomUUID();
            TransactionEntityDTO tx2 = TransactionEntityDTO.builder()
                    .transactionEntityDTOId(trx2)
                    .transactionEntityDTOUserId(uid)
                    .transactionEntityDTOProductName("Open Bank Account BRI")
                    .transactionEntityDTOProductPrice(new BigDecimal("50000"))
                    .transactionEntityDTODate(LocalDateTime.now().minusDays(3))
                    .transactionEntityDTOStatus("SUCCESS")
                    .build();

            when(tTransactionRepositories.findByTransactionEntityDTOUserId(eq(uid)))
                    .thenReturn(List.of(tx2, tx1));

            TransactionOpenBankAccountEntityDTO oba1 = new TransactionOpenBankAccountEntityDTO();
            oba1.setTransactionOpenBankAccountEntityDTOId(UUID.randomUUID());
            oba1.setTransactionOpenBankAccountEntityDTOTransactionId(trx1);
            oba1.setTransactionOpenBankAccountEntityDTOCustomerName("Alice");
            oba1.setTransactionOpenBankAccountEntityDTOCustomerIdentityNumber("111222333");
            oba1.setTransactionOpenBankAccountEntityDTOCustomerPhoneNumber("08123456789");
            oba1.setTransactionOpenBankAccountEntityDTOCustomerEmail("alice@example.com");
            oba1.setTransactionOpenBankAccountEntityDTOCustomerAddress("Jl. Mawar No. 1");

            when(tTransactionOpenBankAccountRepositories
                    .findByTransactionOpenBankAccountEntityDTOTransactionId(eq(trx1)))
                    .thenReturn(Optional.of(oba1));

            when(tTransactionOpenBankAccountRepositories
                    .findByTransactionOpenBankAccountEntityDTOTransactionId(eq(trx2)))
                    .thenReturn(Optional.empty());

            RestApiResponse<List<CustomerOpenBankAccountResponse>> res =
                    service.getAllTransactionsByUser(uid);

            assertThat(res).isNotNull();
            assertThat(res.getRestApiResponseCode()).isEqualTo(200);
            assertThat(res.getRestApiResponseMessage()).isEqualTo("SUCCESS GET all transactions by user");
            assertThat(res.getRestApiResponseResults()).isNotNull().hasSize(2);

            CustomerOpenBankAccountResponse r1 = res.getRestApiResponseResults().get(0);
            CustomerOpenBankAccountResponse r2 = res.getRestApiResponseResults().get(1);

            assertThat(r1.getCustomerOpenBankAccountProductName()).isEqualTo("Open Bank Account BCA");
            assertThat(r1.getCustomerOpenBankAccountProductPrice()).isEqualByComparingTo("100000");
            assertThat(r1.getCustomerOpenBankAccountTransactionStatus()).isEqualTo("SUCCESS");
            assertThat(r1.getCustomerOpenBankAccountName()).isEqualTo("Alice");
            assertThat(r1.getCustomerOpenBankAccountIdentityNumber()).isEqualTo("111222333");
            assertThat(r1.getCustomerOpenBankAccountPhoneNumber()).isEqualTo("08123456789");
            assertThat(r1.getCustomerOpenBankAccountEmail()).isEqualTo("alice@example.com");
            assertThat(r1.getCustomerOpenBankAccountAddress()).isEqualTo("Jl. Mawar No. 1");

            assertThat(r2.getCustomerOpenBankAccountProductName()).isEqualTo("Open Bank Account BRI");
            assertThat(r2.getCustomerOpenBankAccountProductPrice()).isEqualByComparingTo("50000");
            assertThat(r2.getCustomerOpenBankAccountTransactionStatus()).isEqualTo("SUCCESS");
            assertThat(r2.getCustomerOpenBankAccountName()).isNull();
            assertThat(r2.getCustomerOpenBankAccountIdentityNumber()).isNull();
            assertThat(r2.getCustomerOpenBankAccountPhoneNumber()).isNull();
            assertThat(r2.getCustomerOpenBankAccountEmail()).isNull();
            assertThat(r2.getCustomerOpenBankAccountAddress()).isNull();

            verify(tTransactionRepositories, times(1))
                    .findByTransactionEntityDTOUserId(eq(uid));
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

            RestApiResponse<List<CustomerOpenBankAccountResponse>> res =
                    service.getAllTransactionsByUser(uid);

            assertThat(res).isNotNull();
            assertThat(res.getRestApiResponseCode()).isEqualTo(200);
            assertThat(res.getRestApiResponseMessage()).isEqualTo("FAILED GET all transactions by user");
            assertThat(res.getRestApiResponseResults()).isNotNull().isEmpty();

            verify(tTransactionRepositories, times(1))
                    .findByTransactionEntityDTOUserId(eq(uid));
            verifyNoInteractions(tTransactionOpenBankAccountRepositories);
            verifyNoMoreInteractions(tTransactionRepositories);
        }
    }

    @Nested
    @DisplayName("transactionCommissionToWallet()")
    class TransactionCommissionToWallet {

        private UserEntityDTO mockExistingUser() {
            UserEntityDTO user = new UserEntityDTO();
            user.setUserEntityDTOId(userId);
            user.setUserEntityDTOFullName("Agent A");
            user.setUserEntityDTORoleId(UUID.randomUUID());
            user.setUserEntityDTORoleName("AGENT");
            when(mUserRepositories.findByUserEntityDTOId(eq(userId))).thenReturn(Optional.of(user));
            return user;
        }

        @Nested
        @DisplayName("Positive Cases")
        class Positive {
            @Test
            @DisplayName("200: successful transfer → decrease balance, increase wallet")
            void success_existing_wallet() {
                mockExistingUser();

                BigDecimal transfer = new BigDecimal("25000");
                CommissionToWalletRequest req = new CommissionToWalletRequest();
                req.setCommissionToWalletAmount(transfer);

                UserBalanceEntityDTO balance = UserBalanceEntityDTO.builder()
                        .userBalanceEntityDTOId(UUID.randomUUID())
                        .userBalanceEntityDTOUserId(userId)
                        .userBalanceEntityDTOBalanceAmount(new BigDecimal("100000"))
                        .userBalanceEntityDTOBalanceLastUpdate(LocalDateTime.now().minusDays(1))
                        .build();

                UserWalletEntityDTO wallet = new UserWalletEntityDTO();
                wallet.setUserWalletEntityDTOId(UUID.randomUUID());
                wallet.setUserWalletEntityDTOUserId(userId);
                wallet.setUserWalletEntityDTOAmount(new BigDecimal("5000"));
                wallet.setUserWalletEntityDTOLastUpdate(LocalDateTime.now().minusDays(2));

                when(mUserBalanceRepositories.findByUserBalanceEntityDTOUserId(eq(userId)))
                        .thenReturn(Optional.of(balance));
                when(mUserWalletRepositories.findByUserWalletEntityDTOUserId(eq(userId)))
                        .thenReturn(Optional.of(wallet));

                RestApiResponse<UserBalanceResponse> res =
                        service.transactionCommissionToWallet(userId, req);

                assertThat(res).isNotNull();
                assertThat(res.getRestApiResponseCode()).isEqualTo(200);
                assertThat(res.getRestApiResponseMessage()).isEqualTo("SUCCESS transfer commission to wallet");
                assertThat(res.getRestApiResponseResults()).isNotNull();
                assertThat(res.getRestApiResponseResults().getUserBalanceEntityDTOUserId()).isEqualTo(userId);
                assertThat(res.getRestApiResponseResults().getUserBalanceEntityDTOUserAmount())
                        .isEqualByComparingTo("75000"); // 100000 - 25000

                ArgumentCaptor<UserBalanceEntityDTO> balCap = ArgumentCaptor.forClass(UserBalanceEntityDTO.class);
                verify(mUserBalanceRepositories).save(balCap.capture());
                assertThat(balCap.getValue().getUserBalanceEntityDTOBalanceAmount())
                        .isEqualByComparingTo("75000");

                ArgumentCaptor<UserWalletEntityDTO> walCap = ArgumentCaptor.forClass(UserWalletEntityDTO.class);
                verify(mUserWalletRepositories).save(walCap.capture());
                assertThat(walCap.getValue().getUserWalletEntityDTOAmount())
                        .isEqualByComparingTo("30000"); // 5000 + 25000

                verify(tUsersWalletHistoricalRepositories)
                        .save(any(UserWalletHistoricalEntityDTO.class));

                verify(mUserBalanceRepositories).findByUserBalanceEntityDTOUserId(eq(userId));
                verify(mUserWalletRepositories).findByUserWalletEntityDTOUserId(eq(userId));
            }
        }

        @Nested
        @DisplayName("Negative Cases")
        class Negative {
            @Test
            @DisplayName("400: amount null/zero")
            void amount_invalid_throws() {
                mockExistingUser();

                CommissionToWalletRequest req = new CommissionToWalletRequest();
                req.setCommissionToWalletAmount(BigDecimal.ZERO);

                assertThatThrownBy(() -> service.transactionCommissionToWallet(userId, req))
                        .isInstanceOf(CoreThrowHandlerException.class)
                        .hasMessageContaining("greater than zero");

                verifyNoInteractions(mUserBalanceRepositories, mUserWalletRepositories, tUsersWalletHistoricalRepositories);
            }

            @Test
            @DisplayName("404: user balance not found")
            void balance_not_found_throws() {
                mockExistingUser();

                CommissionToWalletRequest req = new CommissionToWalletRequest();
                req.setCommissionToWalletAmount(new BigDecimal("10000"));

                when(mUserBalanceRepositories.findByUserBalanceEntityDTOUserId(eq(userId)))
                        .thenReturn(Optional.empty());

                assertThatThrownBy(() -> service.transactionCommissionToWallet(userId, req))
                        .isInstanceOf(CoreThrowHandlerException.class)
                        .hasMessageContaining("User balance not found");

                verify(mUserBalanceRepositories).findByUserBalanceEntityDTOUserId(eq(userId));
                verifyNoMoreInteractions(mUserBalanceRepositories);
                verifyNoInteractions(mUserWalletRepositories, tUsersWalletHistoricalRepositories);
            }

            @Test
            @DisplayName("400: commission balance insufficient")
            void insufficient_balance_throws() {
                mockExistingUser();

                CommissionToWalletRequest req = new CommissionToWalletRequest();
                req.setCommissionToWalletAmount(new BigDecimal("20000"));

                UserBalanceEntityDTO balance = UserBalanceEntityDTO.builder()
                        .userBalanceEntityDTOId(UUID.randomUUID())
                        .userBalanceEntityDTOUserId(userId)
                        .userBalanceEntityDTOBalanceAmount(new BigDecimal("15000"))
                        .userBalanceEntityDTOBalanceLastUpdate(LocalDateTime.now().minusDays(1))
                        .build();

                when(mUserBalanceRepositories.findByUserBalanceEntityDTOUserId(eq(userId)))
                        .thenReturn(Optional.of(balance));

                assertThatThrownBy(() -> service.transactionCommissionToWallet(userId, req))
                        .isInstanceOf(CoreThrowHandlerException.class)
                        .hasMessageContaining("Insufficient commission balance");

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
                UUID uid = userId; // reuse
                UserBalanceEntityDTO balance = UserBalanceEntityDTO.builder()
                        .userBalanceEntityDTOId(UUID.randomUUID())
                        .userBalanceEntityDTOUserId(uid)
                        .userBalanceEntityDTOBalanceAmount(new BigDecimal("150000"))
                        .userBalanceEntityDTOBalanceLastUpdate(LocalDateTime.now().minusHours(1))
                        .build();

                UserWalletEntityDTO wallet = UserWalletEntityDTO.builder()
                        .userWalletEntityDTOId(UUID.randomUUID())
                        .userWalletEntityDTOUserId(uid)
                        .userWalletEntityDTOAmount(new BigDecimal("27500"))
                        .userWalletEntityDTOLastUpdate(LocalDateTime.now().minusMinutes(10))
                        .build();

                when(mUserBalanceRepositories.findByUserBalanceEntityDTOUserId(eq(uid)))
                        .thenReturn(Optional.of(balance));
                when(mUserWalletRepositories.findByUserWalletEntityDTOUserId(eq(uid)))
                        .thenReturn(Optional.of(wallet));

                RestApiResponse<UserBalanceAndWalletResponse> res =
                        service.getUserBalanceAndWallet(uid);

                assertThat(res).isNotNull();
                assertThat(res.getRestApiResponseCode()).isEqualTo(200);
                assertThat(res.getRestApiResponseMessage()).isEqualTo("SUCCESS GET user balance and wallet");
                assertThat(res.getRestApiResponseResults()).isNotNull();
                assertThat(res.getRestApiResponseResults().getUserBalanceEntityDTOAmount())
                        .isEqualByComparingTo("150000");
                assertThat(res.getRestApiResponseResults().getUserWalletEntityDTOAmount())
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
                UserBalanceEntityDTO balance = UserBalanceEntityDTO.builder()
                        .userBalanceEntityDTOId(UUID.randomUUID())
                        .userBalanceEntityDTOUserId(uid)
                        .userBalanceEntityDTOBalanceAmount(new BigDecimal("50000"))
                        .userBalanceEntityDTOBalanceLastUpdate(LocalDateTime.now())
                        .build();

                when(mUserBalanceRepositories.findByUserBalanceEntityDTOUserId(eq(uid)))
                        .thenReturn(Optional.of(balance));
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