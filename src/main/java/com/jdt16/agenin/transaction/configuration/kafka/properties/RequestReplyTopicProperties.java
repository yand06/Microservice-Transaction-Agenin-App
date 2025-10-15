package com.jdt16.agenin.transaction.configuration.kafka.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Kelas konfigurasi properti untuk topik Kafka yang digunakan dalam mekanisme request-reply.
 *
 * <p>Properti ini memetakan nilai dari file konfigurasi (application.yml atau application.properties)
 * dengan prefix <code>request-reply-topic</code> ke dalam variabel kelas ini.
 *
 * <p>Berisi nama topik untuk pembuatan transaksi dan pembuatan log.
 * Metode {@link #getTopics()} mengembalikan array topik yang digunakan untuk request-reply.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "request-reply-topic")
public class RequestReplyTopicProperties {

    /**
     * Nama topik Kafka untuk proses pembuatan transaksi.
     */
    private String createTransactionTopic;

    /**
     * Nama topik Kafka untuk proses pembuatan log.
     */
    private String createLogTopic;

    /**
     * Mengambil daftar topik Kafka yang digunakan untuk komunikasi request-reply.
     *
     * @return array nama topik yang aktif digunakan dalam request-reply
     */
    public String[] getTopics() {
        return new String[]{
                createTransactionTopic
        };
    }
}
