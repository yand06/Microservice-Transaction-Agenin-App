package com.jdt16.agenin.transaction.controller.module;

import com.jdt16.agenin.transaction.dto.request.TransactionRequest;
import com.jdt16.agenin.transaction.dto.response.RestApiResponse;
import com.jdt16.agenin.transaction.dto.response.TransactionResponse;
import com.jdt16.agenin.transaction.service.interfacing.module.TransactionService;
import com.jdt16.agenin.transaction.utility.RestApiPathUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Kelas {@code TransactionController} berfungsi sebagai pengendali (controller)
 * untuk menangani permintaan HTTP yang terkait dengan transaksi.
 * Controller ini mengatur endpoint transaksi berdasarkan path dasar
 * yang didefinisikan dalam {@link RestApiPathUtility}.
 *
 * <p>Controller ini menggunakan layanan {@link TransactionService}
 * untuk memproses logika bisnis transaksi seperti permintaan inquiry.
 *
 * <p>Dilengkapi dengan anotasi:
 * <ul>
 *   <li>{@code @RestController} – Menandakan bahwa kelas ini adalah controller REST.</li>
 *   <li>{@code @RequestMapping} – Menentukan path dasar dari seluruh endpoint dalam controller ini.</li>
 *   <li>{@code @RequiredArgsConstructor} – Menghasilkan konstruktor untuk dependency injection otomatis.</li>
 *   <li>{@code @Slf4j} – Menyediakan logging menggunakan Lombok.</li>
 * </ul>
 *
 * @author
 * @since 1.0
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(RestApiPathUtility.API_PATH + RestApiPathUtility.API_VERSION + RestApiPathUtility.API_PATH_TRANSACTION)
public class TransactionController {

    /**
     * Service yang menangani logika bisnis transaksi.
     */
    private final TransactionService transactionService;

    /**
     * Endpoint untuk melakukan inquiry transaksi berdasarkan data permintaan pengguna.
     *
     * <p>Metode ini menerima header {@code X-USER-ID} yang berisi identitas pengguna,
     * serta body permintaan berupa {@link TransactionRequest} yang telah divalidasi.
     * Metode ini akan memanggil {@link TransactionService# inquiry(UUID, TransactionRequest)}
     * untuk memproses permintaan dan mengembalikan hasil dalam bentuk {@link RestApiResponse}.
     *
     * @param userId             ID unik pengguna yang dikirim melalui header {@code X-USER-ID}.
     * @param transactionRequest Objek yang merepresentasikan permintaan transaksi
     *                           dan harus lolos validasi {@code @Valid}.
     * @return Objek {@link ResponseEntity} yang berisi {@link RestApiResponse}
     * dengan data hasil inquiry, serta status HTTP 201 (Created).
     */
    @PostMapping(RestApiPathUtility.API_PATH_MOCK_TRANSACTION_OPEN_BANK_ACCOUNT)
    public ResponseEntity<RestApiResponse<?>> inquiry(
            @RequestHeader("X-USER-ID") UUID userId,
            @RequestHeader("X-PRODUCT-ID") UUID productId,
            @Valid @RequestBody TransactionRequest transactionRequest) {

        RestApiResponse<TransactionResponse> response = transactionService.inquiry(userId, productId, transactionRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
