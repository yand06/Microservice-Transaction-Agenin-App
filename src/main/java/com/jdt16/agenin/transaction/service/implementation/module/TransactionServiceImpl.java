package com.jdt16.agenin.transaction.service.implementation.module;

import com.jdt16.agenin.transaction.configuration.security.SecurityConfig;
import com.jdt16.agenin.transaction.dto.entity.TransactionEntityDTO;
import com.jdt16.agenin.transaction.dto.entity.TransactionOpenBankAccountEntityDTO;
import com.jdt16.agenin.transaction.dto.entity.UserEntityDTO;
import com.jdt16.agenin.transaction.dto.entity.UserBalanceEntityDTO;
import com.jdt16.agenin.transaction.dto.entity.UserWalletEntityDTO;
import com.jdt16.agenin.transaction.dto.entity.UserBalanceHistoricalEntityDTO;
import com.jdt16.agenin.transaction.dto.entity.UserWalletHistoricalEntityDTO;
import com.jdt16.agenin.transaction.dto.entity.ProductsEntityDTO;
import com.jdt16.agenin.transaction.dto.exception.CoreThrowHandlerException;
import com.jdt16.agenin.transaction.dto.request.CommissionToWalletRequest;
import com.jdt16.agenin.transaction.dto.request.TransactionRequest;
import com.jdt16.agenin.transaction.dto.response.RestApiResponse;
import com.jdt16.agenin.transaction.dto.response.TransactionResponse;
import com.jdt16.agenin.transaction.dto.response.CustomerOpenBankAccountResponse;
import com.jdt16.agenin.transaction.dto.response.UserBalanceResponse;
import com.jdt16.agenin.transaction.dto.response.UserBalanceAndWalletResponse;
import com.jdt16.agenin.transaction.dto.response.ProductsResponse;
import com.jdt16.agenin.transaction.model.repository.MUserRepositories;
import com.jdt16.agenin.transaction.model.repository.TTransactionRepositories;
import com.jdt16.agenin.transaction.model.repository.MProductsRepositories;
import com.jdt16.agenin.transaction.model.repository.MUserBalanceRepositories;
import com.jdt16.agenin.transaction.model.repository.MUserWalletRepositories;
import com.jdt16.agenin.transaction.model.repository.TUsersBalanceHistoricalRepositories;
import com.jdt16.agenin.transaction.model.repository.MCommissionRepositories;
import com.jdt16.agenin.transaction.model.repository.TTransactionOpenBankAccountRepositories;
import com.jdt16.agenin.transaction.model.repository.TUsersReferralRepositories;
import com.jdt16.agenin.transaction.model.repository.TUsersWalletHistoricalRepositories;
import com.jdt16.agenin.transaction.service.interfacing.module.CommissionValueProjection;
import com.jdt16.agenin.transaction.service.interfacing.module.ProductProjection;
import com.jdt16.agenin.transaction.service.interfacing.module.TransactionService;
import com.jdt16.agenin.transaction.utility.RequestContextUtil;
import com.jdt16.agenin.transaction.utility.TableNameEntityUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RequiredArgsConstructor
@Slf4j
@Service
public class TransactionServiceImpl implements TransactionService {

    private final MUserRepositories mUserRepositories;
    private final TTransactionRepositories tTransactionRepositories;
    private final MProductsRepositories mProductsRepositories;
    private final MUserBalanceRepositories mUserBalanceRepositories;
    private final MUserWalletRepositories mUserWalletRepositories;
    private final TUsersBalanceHistoricalRepositories tUsersBalanceHistoricalRepositories;
    private final MCommissionRepositories mCommissionRepositories;
    private final AuditLogProducerService auditLogProducerService;
    private final TTransactionOpenBankAccountRepositories tTransactionOpenBankAccountRepositories;
    private final TUsersReferralRepositories tUsersReferralRepositories;
    private final SecurityConfig securityConfig = new SecurityConfig();
    private final TUsersWalletHistoricalRepositories tUsersWalletHistoricalRepositories;
    private static final String TRANSACTION_CODE_PREFIX = "TRX_";
    private static final String TRANSACTION_STATUS_SUCCESS = "SUCCESS";
    private static final String TRANSACTION_STATUS_FAILED = "FAILED";

    @Transactional(rollbackFor = CoreThrowHandlerException.class)
    @Override
    @CacheEvict(value = "userBalance", key = "#userId")
    public RestApiResponse<TransactionResponse> inquiry(
            UUID userId,
            UUID productId,
            TransactionRequest transactionRequest
    ) {
        log.info("Starting transaction inquiry for userId: {}, productId: {}", userId, productId);

        UUID transactionId = generateTransactionId();
        LocalDateTime transactionDate = LocalDateTime.now();
        TransactionEntityDTO transactionEntityDTO = createTransaction(userId, productId, transactionId, transactionDate);

        tTransactionRepositories.save(transactionEntityDTO);

        TransactionOpenBankAccountEntityDTO bankAccountDetail = saveTransactionOpenBankAccount(transactionId, transactionRequest);
        UUID userBalanceId = processUserCommission(userId, productId);

        saveUserBalanceHistorical(userBalanceId, transactionId, productId);

        UserEntityDTO userEntityDTO = mUserRepositories.findByUserEntityDTOId(userId)
                .orElseThrow(() -> new CoreThrowHandlerException("User not found for user: " + userId));

        if (userEntityDTO.getUserEntityDTORoleName().equals("SUB_AGENT")) {
            processReferralCommission(userId, transactionId, productId);
            logTransactionSuccess(transactionId, userEntityDTO, transactionEntityDTO);
            TransactionResponse transactionResponse = buildTransactionResponse(transactionEntityDTO, bankAccountDetail);
            return createRestApiResponse(HttpStatus.OK, TRANSACTION_STATUS_SUCCESS, transactionResponse);
        } else if (userEntityDTO.getUserEntityDTORoleName().equals("AGENT")) {
            logTransactionSuccess(transactionId, userEntityDTO, transactionEntityDTO);
            TransactionResponse transactionResponse = buildTransactionResponse(transactionEntityDTO, bankAccountDetail);
            return createRestApiResponse(HttpStatus.OK, TRANSACTION_STATUS_SUCCESS, transactionResponse);
        } else {
            logTransactionFailed(transactionEntityDTO, userEntityDTO);
            throw new CoreThrowHandlerException("Transaction FAILED.");
        }
    }

    private UUID generateTransactionId() {
        return UUID.randomUUID();
    }

    private TransactionEntityDTO createTransaction(
            UUID userId,
            UUID productId,
            UUID transactionId,
            LocalDateTime transactionDate
    ) {
        return TransactionEntityDTO.builder()
                .transactionEntityDTOId(transactionId)
                .transactionEntityDTOCode(generateTransactionCode(transactionId, transactionDate))
                .transactionEntityDTOUserId(userId)
                .transactionEntityDTOProductId(productId)
                .transactionEntityDTOProductName(getProductName(productId))
                .transactionEntityDTOProductPrice(getProductPrice(productId))
                .transactionEntityDTODate(transactionDate)
                .transactionEntityDTOStatus(TRANSACTION_STATUS_SUCCESS)
                .build();
    }

    private String generateTransactionCode(UUID transactionId, LocalDateTime transactionDate) {
        return TRANSACTION_CODE_PREFIX + transactionId.toString().toUpperCase() + "_" + transactionDate.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
    }

    private TransactionOpenBankAccountEntityDTO saveTransactionOpenBankAccount(
            UUID transactionId,
            TransactionRequest transactionRequest
    ) {
        TransactionOpenBankAccountEntityDTO bankAccount = new TransactionOpenBankAccountEntityDTO();
        bankAccount.setTransactionOpenBankAccountEntityDTOId(UUID.randomUUID());
        bankAccount.setTransactionOpenBankAccountEntityDTOTransactionId(transactionId);
        bankAccount.setTransactionOpenBankAccountEntityDTOCustomerName(
                transactionRequest.getTransactionEntityDTOCustomerName()
        );
        bankAccount.setTransactionOpenBankAccountEntityDTOCustomerIdentityNumber(
                transactionRequest.getTransactionEntityDTOCustomerIdentityNumber()
        );
        bankAccount.setTransactionOpenBankAccountEntityDTOCustomerPhoneNumber(
                transactionRequest.getTransactionEntityDTOCustomerPhoneNumber()
        );
        bankAccount.setTransactionOpenBankAccountEntityDTOCustomerEmail(
                transactionRequest.getTransactionEntityDTOCustomerEmail()
        );
        bankAccount.setTransactionOpenBankAccountEntityDTOCustomerAddress(
                transactionRequest.getTransactionEntityDTOCustomerAddress()
        );

        return tTransactionOpenBankAccountRepositories.save(bankAccount);
    }

    private UUID processUserCommission(UUID userId, UUID productId) {
        BigDecimal commissionValue = getCommissionsValue(productId);

        UserBalanceEntityDTO userBalance = mUserBalanceRepositories
                .findByUserBalanceEntityDTOUserId(userId)
                .orElseGet(() -> createNewUserBalance(userId));

        updateUserBalance(userBalance, commissionValue);

        UserBalanceEntityDTO savedBalance = mUserBalanceRepositories.save(userBalance);
        return savedBalance.getUserBalanceEntityDTOId();
    }

    private UserBalanceEntityDTO createNewUserBalance(UUID userId) {
        return UserBalanceEntityDTO.builder()
                .userBalanceEntityDTOId(UUID.randomUUID())
                .userBalanceEntityDTOUserId(userId)
                .userBalanceEntityDTOBalanceAmount(BigDecimal.ZERO)
                .userBalanceEntityDTOBalanceLastUpdate(LocalDateTime.now())
                .build();
    }

    private void updateUserBalance(UserBalanceEntityDTO userBalance, BigDecimal commissionValue) {
        BigDecimal currentBalance = userBalance.getUserBalanceEntityDTOBalanceAmount();
        BigDecimal newBalance = currentBalance.add(commissionValue);
        userBalance.setUserBalanceEntityDTOBalanceAmount(newBalance);
        userBalance.setUserBalanceEntityDTOBalanceLastUpdate(LocalDateTime.now());
    }

    private void saveUserBalanceHistorical(
            UUID userBalanceId,
            UUID transactionId,
            UUID productId
    ) {
        BigDecimal commissionValue = getCommissionsValue(productId);
        UserBalanceHistoricalEntityDTO historical = UserBalanceHistoricalEntityDTO.builder()
                .userBalanceHistoricalEntityDTOId(UUID.randomUUID())
                .userBalanceHistoricalEntityDTOUserBalanceId(userBalanceId)
                .userBalanceHistoricalEntityDTOTransactionId(transactionId)
                .userBalanceHistoricalEntityDTOAmount(commissionValue)
                .userBalanceHistoricalEntityDTOCreatedDate(LocalDateTime.now())
                .build();
        tUsersBalanceHistoricalRepositories.save(historical);
    }

    @Transactional(rollbackFor = CoreThrowHandlerException.class)
    private void processReferralCommission(
            UUID inviteeUserId,
            UUID transactionId,
            UUID productId
    ) {
        UserEntityDTO inviteeUser = mUserRepositories.findByUserEntityDTOId(inviteeUserId)
                .orElseThrow(() -> new CoreThrowHandlerException(
                        "Invitee user not found: " + inviteeUserId
                ));

        UUID referenceUserId = tUsersReferralRepositories
                .findReferenceUserIdByInviteeUserId(inviteeUserId)
                .orElseThrow(() -> createReferralNotFoundException(inviteeUserId, inviteeUser));

        BigDecimal commissionValue = getCommissionsValue(productId);
        UUID referenceUserBalanceId = processUserCommission(referenceUserId, productId);
        saveUserBalanceHistorical(referenceUserBalanceId, transactionId, productId);

        logReferralCommissionSuccess(referenceUserId, inviteeUserId, commissionValue, inviteeUser, transactionId);
    }

    private CoreThrowHandlerException createReferralNotFoundException(UUID inviteeUserId, UserEntityDTO userEntityDTO) {
        String errorMessage = String.format(
                "Data integrity error: Parent user not found for invitee: %s. " +
                        "The transaction will be rolled back to maintain referral data consistency.",
                inviteeUserId
        );

        log.error(errorMessage);

        Map<String, Object> oldData = Collections.emptyMap();
        Map<String, Object> newData = Map.of(
                "inviteeUserId", inviteeUserId,
                "timestamp", LocalDateTime.now(),
                "error", errorMessage
        );
        auditLogProducerService.logUpdate(
                TableNameEntityUtility.TABLE_USERS_REFERRAL,
                inviteeUserId,
                oldData,
                newData,
                userEntityDTO.getUserEntityDTOId(),
                userEntityDTO.getUserEntityDTOFullName(),
                userEntityDTO.getUserEntityDTORoleId(),
                userEntityDTO.getUserEntityDTORoleName(),
                RequestContextUtil.getUserAgent(),
                RequestContextUtil.getClientIpAddress()
        );

        return new CoreThrowHandlerException(errorMessage);
    }

    private String getProductName(UUID productId) {
        return mProductsRepositories.findByProductEntityDTOId(productId)
                .map(ProductProjection::getProductEntityDTOName)
                .orElseThrow(() -> new CoreThrowHandlerException(
                        "Product with ID " + productId + " not found"
                ));
    }

    private BigDecimal getProductPrice(UUID productId) {
        return mProductsRepositories.findByProductEntityDTOId(productId)
                .map(ProductProjection::getProductEntityDTOPrice)
                .orElseThrow(() -> new CoreThrowHandlerException(
                        "Product with ID " + productId + " not found"
                ));
    }

    @Cacheable(value = "commissions", key = "#productId")
    private BigDecimal getCommissionsValue(UUID productId) {
        return mCommissionRepositories.findByCommissionsEntityDTOProductId(productId)
                .map(CommissionValueProjection::getCommissionsEntityDTOValue)
                .orElseThrow(() -> new CoreThrowHandlerException(
                        "Commission value for ID products " + productId + " not found"
                ));
    }

    private TransactionResponse buildTransactionResponse(
            TransactionEntityDTO transaction,
            TransactionOpenBankAccountEntityDTO bankAccountDetail
    ) {
        return TransactionResponse.builder()
                .transactionEntityDTOId(transaction.getTransactionEntityDTOId())
                .transactionEntityDTOCode(transaction.getTransactionEntityDTOCode())
                .transactionEntityDTOUserId(transaction.getTransactionEntityDTOUserId())
                .transactionEntityDTOProductId(transaction.getTransactionEntityDTOProductId())
                .transactionEntityDTOProductCode(transaction.getTransactionEntityDTOProductName())
                .transactionEntityDTOCustomerName(bankAccountDetail.getTransactionOpenBankAccountEntityDTOCustomerName())
                .transactionEntityDTOCustomerIdentityNumber(bankAccountDetail.getTransactionOpenBankAccountEntityDTOCustomerIdentityNumber())
                .transactionEntityDTOCustomerPhoneNumber(bankAccountDetail.getTransactionOpenBankAccountEntityDTOCustomerPhoneNumber())
                .transactionEntityDTOCustomerEmail(bankAccountDetail.getTransactionOpenBankAccountEntityDTOCustomerEmail())
                .transactionEntityDTOCustomerAddress(bankAccountDetail.getTransactionOpenBankAccountEntityDTOCustomerAddress())
                .transactionEntityDTODate(transaction.getTransactionEntityDTODate())
                .transactionEntityDTOStatus(transaction.getTransactionEntityDTOStatus())
                .build();
    }

    private <T> RestApiResponse<T> createRestApiResponse(
            HttpStatus httpStatus,
            String message,
            T result
    ) {
        return RestApiResponse.<T>builder()
                .restApiResponseCode(httpStatus.value())
                .restApiResponseMessage(message)
                .restApiResponseResults(result)
                .build();
    }

    private void logTransactionSuccess(UUID transactionId, UserEntityDTO userEntityDTO, TransactionEntityDTO transactionEntityDTO) {
        String successMessage = "Transaction successfully created with ID: " + transactionId;
        log.info(successMessage);

        Map<String, Object> newData = buildTransactionDataMap(transactionEntityDTO);

        auditLogProducerService.logCreate(
                TableNameEntityUtility.TABLE_TRANSACTION,
                transactionId,
                newData,
                userEntityDTO.getUserEntityDTOId(),
                userEntityDTO.getUserEntityDTOFullName(),
                userEntityDTO.getUserEntityDTORoleId(),
                userEntityDTO.getUserEntityDTORoleName(),
                RequestContextUtil.getUserAgent(),
                RequestContextUtil.getClientIpAddress()
        );
    }

    private void logReferralCommissionSuccess(
            UUID referenceUserId,
            UUID inviteeUserId,
            BigDecimal commissionValue,
            UserEntityDTO userEntityDTO,
            UUID transactionId
    ) {
        String successMessage = String.format(
                "Referral commission successfully processed for parent user: %s "
                        + "from invitee: %s with amount: %s",
                referenceUserId, inviteeUserId, commissionValue
        );
        log.info(successMessage);
        Map<String, Object> oldData = Collections.emptyMap();
        Map<String, Object> newData = Map.of(
                "referenceUserId", referenceUserId,
                "inviteeUserId", inviteeUserId,
                "commissionValue", commissionValue,
                "transactionId", transactionId,
                "timestamp", LocalDateTime.now()
        );
        auditLogProducerService.logUpdate(
                TableNameEntityUtility.TABLE_USERS_REFERRAL,
                transactionId,
                oldData,
                newData,
                userEntityDTO.getUserEntityDTOId(),
                userEntityDTO.getUserEntityDTOFullName(),
                userEntityDTO.getUserEntityDTORoleId(),
                userEntityDTO.getUserEntityDTORoleName(),
                RequestContextUtil.getUserAgent(),
                RequestContextUtil.getClientIpAddress()
        );
    }

    private void logTransactionFailed(TransactionEntityDTO transactionEntityDTO, UserEntityDTO userEntityDTO) {
        String failedMessage = "Transaction failed created with ID: " + transactionEntityDTO.getTransactionEntityDTOId();
        log.info(failedMessage);
        Map<String, Object> oldData = Collections.emptyMap();
        Map<String, Object> newData = buildTransactionDataMap(transactionEntityDTO);

        auditLogProducerService.logUpdate(
                TableNameEntityUtility.TABLE_TRANSACTION,
                UUID.randomUUID(),
                oldData,
                newData,
                userEntityDTO.getUserEntityDTOId(),
                userEntityDTO.getUserEntityDTOFullName(),
                userEntityDTO.getUserEntityDTORoleId(),
                userEntityDTO.getUserEntityDTORoleName(),
                RequestContextUtil.getUserAgent(),
                RequestContextUtil.getClientIpAddress()
        );
    }

    @Override
    @Cacheable(value = "products", unless = "#result.restApiResponseResults.isEmpty()")
    public RestApiResponse<List<ProductsResponse>> getListProducts() {
        List<ProductsEntityDTO> productsEntityDTOList = mProductsRepositories.findAll();

        List<ProductsResponse> productsResponseList = productsEntityDTOList.stream()
                .map(this::toProductsResponse)
                .toList();

        return RestApiResponse.<List<ProductsResponse>>builder()
                .restApiResponseCode(HttpStatus.OK.value())
                .restApiResponseMessage("SUCCESS GET Products")
                .restApiResponseResults(productsResponseList)
                .build();
    }

    @Override
    public RestApiResponse<List<CustomerOpenBankAccountResponse>> getAllTransactionsByUser(UUID userId) {
        List<TransactionEntityDTO> transactionEntityDTOS = tTransactionRepositories.findByTransactionEntityDTOUserId(userId);
        if (transactionEntityDTOS.isEmpty()) {
            return RestApiResponse.<List<CustomerOpenBankAccountResponse>>builder()
                    .restApiResponseCode(HttpStatus.OK.value())
                    .restApiResponseMessage("FAILED GET all transactions by user")
                    .restApiResponseResults(Collections.emptyList())
                    .build();
        }
        List<CustomerOpenBankAccountResponse> results = transactionEntityDTOS.stream()
                .sorted(Comparator.comparing(TransactionEntityDTO::getTransactionEntityDTODate).reversed())
                .map(transactionEntityDTO -> {
                    TransactionOpenBankAccountEntityDTO transactionOpenBankAccountEntityDTO = tTransactionOpenBankAccountRepositories
                            .findByTransactionOpenBankAccountEntityDTOTransactionId(transactionEntityDTO.getTransactionEntityDTOId())
                            .orElse(null);

                    CustomerOpenBankAccountResponse customerOpenBankAccountResponse = new CustomerOpenBankAccountResponse();
                    customerOpenBankAccountResponse.setCustomerOpenBankAccountProductName(transactionEntityDTO.getTransactionEntityDTOProductName());
                    customerOpenBankAccountResponse.setCustomerOpenBankAccountProductPrice(transactionEntityDTO.getTransactionEntityDTOProductPrice());
                    customerOpenBankAccountResponse.setCustomerOpenBankAccountTransactionDate(transactionEntityDTO.getTransactionEntityDTODate());
                    customerOpenBankAccountResponse.setCustomerOpenBankAccountTransactionStatus(transactionEntityDTO.getTransactionEntityDTOStatus());

                    if (transactionOpenBankAccountEntityDTO != null) {
                        customerOpenBankAccountResponse.setCustomerOpenBankAccountName(transactionOpenBankAccountEntityDTO.getTransactionOpenBankAccountEntityDTOCustomerName());
                        customerOpenBankAccountResponse.setCustomerOpenBankAccountIdentityNumber(transactionOpenBankAccountEntityDTO.getTransactionOpenBankAccountEntityDTOCustomerIdentityNumber());
                        customerOpenBankAccountResponse.setCustomerOpenBankAccountPhoneNumber(transactionOpenBankAccountEntityDTO.getTransactionOpenBankAccountEntityDTOCustomerPhoneNumber());
                        customerOpenBankAccountResponse.setCustomerOpenBankAccountEmail(transactionOpenBankAccountEntityDTO.getTransactionOpenBankAccountEntityDTOCustomerEmail());
                        customerOpenBankAccountResponse.setCustomerOpenBankAccountAddress(transactionOpenBankAccountEntityDTO.getTransactionOpenBankAccountEntityDTOCustomerAddress());
                    }
                    return customerOpenBankAccountResponse;
                })
                .toList();

        return RestApiResponse.<List<CustomerOpenBankAccountResponse>>builder()
                .restApiResponseCode(HttpStatus.OK.value())
                .restApiResponseMessage("SUCCESS GET all transactions by user")
                .restApiResponseResults(results)
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    @CacheEvict(value = "userBalance", key = "#userId")
    public RestApiResponse<UserBalanceResponse> transactionCommissionToWallet(
            UUID userId,
            CommissionToWalletRequest commissionToWalletRequest
    ) {
        UserEntityDTO userEntityDTO = mUserRepositories.findByUserEntityDTOId(userId)
                .orElseThrow(() -> new CoreThrowHandlerException("User not found"));
        String userFullName = userEntityDTO.getUserEntityDTOFullName();
        UUID roleId = userEntityDTO.getUserEntityDTORoleId();
        String roleName = userEntityDTO.getUserEntityDTORoleName();

        validatePassword(
                commissionToWalletRequest.getUserEntityDTOPassword(),
                userEntityDTO.getUserEntityDTOPassword(),
                userId,
                commissionToWalletRequest.getCommissionToWalletAmount()
        );

        BigDecimal transferAmount = commissionToWalletRequest.getCommissionToWalletAmount();
        if (transferAmount == null || transferAmount.compareTo(BigDecimal.ZERO) <= 0) {
            logFailedCommissionToWallet(userId, transferAmount, "Transfer amount must be greater than zero");
            throw new CoreThrowHandlerException("Transfer amount must be greater than zero");
        }

        UserBalanceEntityDTO userBalance = mUserBalanceRepositories
                .findByUserBalanceEntityDTOUserId(userId)
                .orElseThrow(() -> {
                    logFailedCommissionToWallet(userId, transferAmount, "User balance not found");
                    return new CoreThrowHandlerException(
                            "User balance not found for user: " + userId
                    );
                });

        BigDecimal balanceBefore = nullToZero(userBalance.getUserBalanceEntityDTOBalanceAmount());

        if (balanceBefore.compareTo(transferAmount) < 0) {
            logFailedCommissionToWallet(userId, transferAmount, "Insufficient commission balance");
            throw new CoreThrowHandlerException(
                    "Insufficient commission balance. Available: " + balanceBefore +
                            ", requested: " + transferAmount
            );
        }

        BigDecimal balanceAfter = balanceBefore.subtract(transferAmount);
        userBalance.setUserBalanceEntityDTOBalanceAmount(balanceAfter);
        userBalance.setUserBalanceEntityDTOBalanceLastUpdate(LocalDateTime.now());
        mUserBalanceRepositories.save(userBalance);

        UserWalletEntityDTO userWallet = mUserWalletRepositories
                .findByUserWalletEntityDTOUserId(userId)
                .orElseGet(() -> createNewWallet(userId));

        BigDecimal walletBefore = nullToZero(userWallet.getUserWalletEntityDTOAmount());
        BigDecimal walletAfter = walletBefore.add(transferAmount);

        userWallet.setUserWalletEntityDTOAmount(walletAfter);
        userWallet.setUserWalletEntityDTOLastUpdate(LocalDateTime.now());
        UserWalletEntityDTO savedWallet = mUserWalletRepositories.save(userWallet);

        saveWalletHistorical(savedWallet.getUserWalletEntityDTOId(), transferAmount);

        logSuccessCommissionToWallet(userId, transferAmount, balanceBefore, balanceAfter, walletBefore, walletAfter, userFullName, roleId, roleName);

        UserBalanceResponse userBalanceResponse = UserBalanceResponse.builder()
                .userBalanceEntityDTOUserId(userId)
                .userBalanceEntityDTOUserAmount(balanceAfter)
                .userBalanceEntityDTOUserWalletAmount(walletAfter)
                .userBalanceEntityDTOLastUpdated(userBalance.getUserBalanceEntityDTOBalanceLastUpdate())
                .build();

        return createRestApiResponse(
                HttpStatus.OK,
                "SUCCESS transfer commission to wallet",
                userBalanceResponse
        );
    }

    @Override
    @Cacheable(value = "userBalance", key = "#userId")
    public RestApiResponse<UserBalanceAndWalletResponse> getUserBalanceAndWallet(UUID userId) {
        UserBalanceEntityDTO userBalance = mUserBalanceRepositories.findByUserBalanceEntityDTOUserId(userId)
                .orElseThrow(() -> new CoreThrowHandlerException("User balance not found for user: " + userId));
        UserWalletEntityDTO userWallet = mUserWalletRepositories.findByUserWalletEntityDTOUserId(userId)
                .orElseThrow(() -> new CoreThrowHandlerException("User wallet not found for user: " + userId));
        UserBalanceAndWalletResponse userBalanceAndWalletResponse = UserBalanceAndWalletResponse.builder()
                .userBalanceEntityDTOAmount(userBalance.getUserBalanceEntityDTOBalanceAmount())
                .userWalletEntityDTOAmount(userWallet.getUserWalletEntityDTOAmount())
                .build();
        return createRestApiResponse(
                HttpStatus.OK,
                "SUCCESS GET user balance and wallet",
                userBalanceAndWalletResponse
        );
    }

    private UserWalletEntityDTO createNewWallet(UUID userId) {
        return UserWalletEntityDTO.builder()
                .userWalletEntityDTOId(UUID.randomUUID())
                .userWalletEntityDTOUserId(userId)
                .userWalletEntityDTOAmount(BigDecimal.ZERO)
                .userWalletEntityDTOLastUpdate(LocalDateTime.now())
                .build();
    }

    private void saveWalletHistorical(UUID walletId, BigDecimal amount) {
        UserWalletHistoricalEntityDTO historical = UserWalletHistoricalEntityDTO.builder()
                .userWalletHistoricalEntityDTOId(UUID.randomUUID())
                .userWalletHistoricalEntityDTOUserWalletId(walletId)
                .userWalletHistoricalEntityDTOAmount(amount)
                .userWalletHistoricalEntityDTOCreatedDate(LocalDateTime.now())
                .build();
        tUsersWalletHistoricalRepositories.save(historical);
    }

    private static BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private ProductsResponse toProductsResponse(ProductsEntityDTO productsEntityDTO) {
        return ProductsResponse.builder()
                .productEntityDTOId(productsEntityDTO.getProductEntityDTOId())
                .productEntityDTOName(productsEntityDTO.getProductEntityDTOName())
                .productEntityDTOCode(productsEntityDTO.getProductEntityDTOCode())
                .productEntityDTODesc(productsEntityDTO.getProductEntityDTODesc())
                .productEntityDTOPrice(productsEntityDTO.getProductEntityDTOPrice())
                .commissionsEntityDTOValue(getCommissionsValue(productsEntityDTO.getProductEntityDTOId()))
                .build();
    }

    private Map<String, Object> buildTransactionDataMap(TransactionEntityDTO transactionEntityDTO) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", transactionEntityDTO.getTransactionEntityDTOId().toString());
        data.put("transactionCode", transactionEntityDTO.getTransactionEntityDTOCode());
        data.put("userId", transactionEntityDTO.getTransactionEntityDTOUserId().toString());
        data.put("productId", transactionEntityDTO.getTransactionEntityDTOProductId().toString());
        data.put("productName", transactionEntityDTO.getTransactionEntityDTOProductName());
        data.put("productPrice", transactionEntityDTO.getTransactionEntityDTOProductPrice());
        data.put("date", transactionEntityDTO.getTransactionEntityDTODate().toString());
        data.put("status", transactionEntityDTO.getTransactionEntityDTOStatus());
        return data;
    }

    private void logFailedCommissionToWallet(UUID userId, BigDecimal transferAmount, String reason) {
        Map<String, Object> newData = Map.of(
                "userId", userId,
                "transferAmount", transferAmount,
                "status", TRANSACTION_STATUS_FAILED,
                "reason", reason,
                "timestamp", LocalDateTime.now()
        );
        auditLogProducerService.logUpdate(
                TableNameEntityUtility.TABLE_USER_WALLET,
                UUID.randomUUID(),
                Collections.emptyMap(),
                newData,
                userId,
                null,
                null,
                null,
                RequestContextUtil.getUserAgent(),
                RequestContextUtil.getClientIpAddress()
        );
    }

    private void logSuccessCommissionToWallet(
            UUID userId,
            BigDecimal transferAmount,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter,
            BigDecimal walletBefore,
            BigDecimal walletAfter,
            String userFullname,
            UUID roleId,
            String roleName) {
        Map<String, Object> newData = Map.of(
                "userId", userId,
                "transferAmount", transferAmount,
                "status", TRANSACTION_STATUS_SUCCESS,
                "balanceBefore", balanceBefore,
                "balanceAfter", balanceAfter,
                "walletBefore", walletBefore,
                "walletAfter", walletAfter,
                "timestamp", LocalDateTime.now()
        );
        auditLogProducerService.logUpdate(
                TableNameEntityUtility.TABLE_USER_WALLET,
                UUID.randomUUID(),
                Collections.emptyMap(),
                newData,
                userId,
                userFullname,
                roleId,
                roleName,
                RequestContextUtil.getUserAgent(),
                RequestContextUtil.getClientIpAddress()
        );
    }

    private void validatePassword(String rawPassword, String encodedPassword, UUID userId, BigDecimal amount) {
        if (!securityConfig.passwordEncoder().matches(rawPassword, encodedPassword)) {
            logFailedCommissionToWallet(userId, amount, "Invalid password");
            throw new CoreThrowHandlerException("Invalid password");
        }
    }

}
