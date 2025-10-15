package com.jdt16.agenin.transaction.service.implementation.module;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jdt16.agenin.transaction.configuration.kafka.properties.RequestTopicProperties;
import com.jdt16.agenin.transaction.dto.entity.TransactionEntityDTO;
import com.jdt16.agenin.transaction.dto.exception.CoreThrowHandlerException;
import com.jdt16.agenin.transaction.dto.request.LogRequestDTO;
import com.jdt16.agenin.transaction.dto.request.TransactionRequest;
import com.jdt16.agenin.transaction.dto.response.RestApiResponse;
import com.jdt16.agenin.transaction.dto.response.TransactionResponse;
import com.jdt16.agenin.transaction.service.interfacing.module.KafkaConnector;
import com.jdt16.agenin.transaction.service.interfacing.module.TransactionService;
import com.jdt16.agenin.transaction.model.repository.MTransactionRepositories;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementasi layanan transaksi {@link TransactionService}.
 *
 * <p>Kelas ini bertanggung jawab untuk mengelola proses inquiry transaksi,
 * menyimpan data transaksi ke repository, serta mengirim log ke Kafka.
 *
 * <p>Metode inquiry dibuat dengan transactional agar operasi basis data konsisten.
 * Log aktivitas dikirim secara asynchronous menggunakan KafkaConnector.
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class TransactionServiceImpl implements TransactionService {

    private final MTransactionRepositories mTransactionRepositories;
    private final KafkaConnector kafkaConnector;
    private final ObjectMapper objectMapper;
    private final RequestTopicProperties requestTopicProperties;

    /**
     * Membuat response API generik dengan status, pesan, dan data hasil.
     *
     * @param httpStatus status HTTP yang merepresentasikan hasil response
     * @param message    pesan hasil response
     * @param result     data hasil yang dikembalikan pada response
     * @param <T>        tipe data hasil response
     * @return instance {@link RestApiResponse} yang berisi informasi response
     */
    private <T> RestApiResponse<T> createRestApiResponse(HttpStatus httpStatus, String message, T result) {
        return RestApiResponse.<T>builder()
                .restApiResponseCode(httpStatus.value())
                .restApiResponseMessage(message)
                .restApiResponseResults(result)
                .build();
    }

    /**
     * Mengirim log aktivitas transaksi ke topik Kafka log secara asynchronous.
     *
     * @param description deskripsi log yang menjelaskan aktivitas
     * @param name        nama log aktivitas
     * @param type        tipe atau kategori log (misalnya SUCCESS, ERROR)
     * @throws CoreThrowHandlerException jika terjadi kegagalan serialisasi JSON pada log
     */
    private void sendLogKafka(String description, String name, String type) {
        LogRequestDTO logRequestDTO = new LogRequestDTO();
        logRequestDTO.setLogDescription(description);
        logRequestDTO.setLogName(name);
        logRequestDTO.setLogType(type);

        try {
            kafkaConnector.kafkaAsync(objectMapper.writeValueAsString(logRequestDTO), requestTopicProperties.getCreateLogTopic());
        } catch (JsonProcessingException e) {
            log.error("Failed to send log to Kafka: {}", e.getMessage(), e);
            throw new CoreThrowHandlerException("ERROR: " + e.getMessage());
        }
    }

    /**
     * Mengonversi {@link TransactionEntityDTO} menjadi {@link TransactionResponse} yang akan dikirim ke client.
     *
     * @param transactionEntityDTO entitas data transaksi
     * @return objek response transaksi yang berisi data hasil inquiry transaksi
     */
    private TransactionResponse transactionEntityDTOToTransactionResponse(TransactionEntityDTO transactionEntityDTO) {
        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setTransactionEntityDTOId(transactionEntityDTO.getTransactionEntityDTOId());
        transactionResponse.setTransactionEntityDTOCode(transactionEntityDTO.getTransactionEntityDTOCode());
        transactionResponse.setTransactionEntityDTOUserId(transactionEntityDTO.getTransactionEntityDTOUserId());
        transactionResponse.setTransactionEntityDTOProductId(transactionEntityDTO.getTransactionEntityDTOProductId());
        transactionResponse.setTransactionEntityDTOQuantity(transactionEntityDTO.getTransactionEntityDTOQuantity());
        transactionResponse.setTransactionEntityDTOTotalAmount(transactionEntityDTO.getTransactionEntityDTOTotalAmount());
        transactionResponse.setTransactionEntityDTODate(transactionEntityDTO.getTransactionEntityDTODate());
        transactionResponse.setTransactionEntityDTOStatus(transactionEntityDTO.getTransactionEntityDTOStatus());
        return transactionResponse;
    }

    /**
     * Melakukan inquiry transaksi baru dan menyimpan data transaksi dengan status {@code PENDING}.
     *
     * <p>Data transaksi baru dibuat dengan ID unik dan kode transaksi yang digenerate otomatis.
     * Setelah penyimpanan berhasil, log transaksi dikirim ke Kafka.
     *
     * @param userId             UUID pengguna yang membuat transaksi
     * @param transactionRequest data permintaan transaksi dari client
     * @return response REST API yang berisi data transaksi yang baru dibuat dengan status HTTP 200 (OK)
     */
    @Transactional
    @Override
    public RestApiResponse<TransactionResponse> inquiry(UUID userId, UUID productId, TransactionRequest transactionRequest) {

        UUID transactionId = UUID.randomUUID();
        LocalDateTime transactionDate = LocalDateTime.now();

        TransactionEntityDTO transactionEntityDTO = new TransactionEntityDTO();
        transactionEntityDTO.setTransactionEntityDTOId(transactionId);
        transactionEntityDTO.setTransactionEntityDTOCode("TRX_" + transactionId + "_" + transactionDate);
        transactionEntityDTO.setTransactionEntityDTOUserId(userId);
        transactionEntityDTO.setTransactionEntityDTOProductId(productId);
        transactionEntityDTO.setTransactionEntityDTOQuantity(transactionRequest.getTransactionEntityDTOQuantity());
        transactionEntityDTO.setTransactionEntityDTOTotalAmount(transactionRequest.getTransactionEntityDTOTotalAmount());
        transactionEntityDTO.setTransactionEntityDTODate(transactionDate);
        transactionEntityDTO.setTransactionEntityDTOStatus("PENDING");

        mTransactionRepositories.save(transactionEntityDTO);

        log.info("Transaction inquiry success with ID: {}", transactionId);
        sendLogKafka("Transaction inquiry success with ID: " + transactionId, "INQUIRY", "SUCCESS");

        TransactionResponse transactionResponse = transactionEntityDTOToTransactionResponse(transactionEntityDTO);

        return createRestApiResponse(HttpStatus.OK, "SUCCESS", transactionResponse);
    }
}
