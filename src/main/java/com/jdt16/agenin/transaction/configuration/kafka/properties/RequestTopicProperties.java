package com.jdt16.agenin.transaction.configuration.kafka.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Kelas konfigurasi properti untuk topik Kafka yang digunakan dalam pengiriman request.
 *
 * <p>Kelas ini memetakan properti dari file konfigurasi (application.yml atau application.properties)
 * dengan prefix <code>request-topic</code> ke dalam variabel kelas ini.
 *
 * <p>Berisi nama topik untuk proses pembuatan transaksi dan pembuatan log.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "request-topic")
public class RequestTopicProperties {

    /**
     * Nama topik Kafka untuk proses pembuatan transaksi.
     */
    private String createTransactionTopic;

    /**
     * Nama topik Kafka untuk proses pembuatan log.
     */
    private String createLogTopic;
}
