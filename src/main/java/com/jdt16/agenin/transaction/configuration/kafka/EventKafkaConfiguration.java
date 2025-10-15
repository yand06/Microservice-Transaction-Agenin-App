package com.jdt16.agenin.transaction.configuration.kafka;

import com.jdt16.agenin.transaction.configuration.kafka.properties.RequestReplyTopicProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Kelas konfigurasi Kafka untuk modul transaksi.
 *
 * <p>Kelas ini bertugas mengatur konfigurasi berbagai komponen Kafka yang digunakan dalam aplikasi,
 * seperti consumer dan producer factories, template untuk pengiriman pesan, serta listener container
 * untuk menerima dan membalas pesan secara asinkron.
 *
 * <p>Konfigurasi ini menggunakan properti yang diambil dari file <code>application.yml</code> seperti
 * <code>kafka.bootstrap-servers</code> dan <code>kafka.consumer-group</code>, serta properti khusus
 * untuk request-reply topic.
 *
 * <p>Beberapa bean yang dikonfigurasi antara lain:
 * <ul>
 *     <li><b>ConsumerFactory:</b> untuk membuat consumer Kafka yang menangani deserialisasi pesan.</li>
 *     <li><b>ProducerFactory:</b> untuk membuat producer Kafka yang sudah dioptimalkan dengan retry, idempotence, dan acks=all.</li>
 *     <li><b>KafkaTemplate:</b> template yang digunakan untuk mengirim pesan ke Kafka.</li>
 *     <li><b>ReplyingKafkaTemplate:</b> mendukung pola request-reply dalam komunikasi Kafka.</li>
 *     <li><b>ConcurrentMessageListenerContainer:</b> container listener yang khusus menerima balasan pesan.</li>
 *     <li><b>ConcurrentKafkaListenerContainerFactory:</b> factory untuk membuat listener asynchronous dengan KafkaListener.</li>
 * </ul>
 *
 * @author
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class EventKafkaConfiguration {

    @Value("${kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    @Value("${kafka.consumer-group}")
    private String kafkaConsumerGroup;

    private final RequestReplyTopicProperties requestReplyTopicProperties;

    /**
     * Membuat konfigurasi {@link ConsumerFactory} untuk mengkonsumsi pesan dari Kafka.
     * Konfigurasi ini mengatur bootstrap server, group ID, serta deserializer kunci dan nilai.
     *
     * @return ConsumerFactory yang digunakan untuk membuat consumer Kafka
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConsumerGroup);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Membuat konfigurasi {@link ProducerFactory} untuk memproduksi pesan ke Kafka.
     * Konfigurasi ini mendukung retry otomatis, idempotensi, dan pengaturan acknowledgment untuk keamanan pengiriman.
     *
     * @return ProducerFactory yang digunakan untuk membuat producer Kafka
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.RETRIES_CONFIG, 5);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 100);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);

        return new DefaultKafkaProducerFactory<>(props);
    }

    /**
     * Membuat {@link KafkaTemplate} yang digunakan aplikasi untuk mengirim pesan ke Kafka.
     *
     * @return KafkaTemplate untuk komunikasi pengiriman pesan
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Membuat {@link ReplyingKafkaTemplate} untuk mendukung pola komunikasi request-reply pada Kafka.
     * Template ini memungkinkan pengiriman pesan serta penerimaan balasan secara sinkron dengan timeout default 1 menit.
     *
     * @param pf        producer factory yang digunakan untuk pengiriman pesan
     * @param container listener container yang menerima balasan pesan
     * @return ReplyingKafkaTemplate untuk komunikasi request-reply
     */
    @Bean
    public ReplyingKafkaTemplate<String, String, String> replyKafkaTemplate(
            ProducerFactory<String, String> pf,
            ConcurrentMessageListenerContainer<String, String> container) {

        ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate = new ReplyingKafkaTemplate<>(pf, container);
        replyingKafkaTemplate.setDefaultReplyTimeout(Duration.ofMinutes(1));
        return replyingKafkaTemplate;
    }

    /**
     * Membuat {@link ConcurrentMessageListenerContainer} yang digunakan untuk menerima balasan (reply)
     * sesuai topik yang didefinisikan pada {@link RequestReplyTopicProperties}.
     *
     * @param cf consumer factory yang digunakan untuk membuat consumer
     * @return ConcurrentMessageListenerContainer untuk menerima pesan balasan
     */
    @Bean
    public ConcurrentMessageListenerContainer<String, String> replyContainer(ConsumerFactory<String, String> cf) {
        ContainerProperties containerProperties = new ContainerProperties(requestReplyTopicProperties.getTopics());
        return new ConcurrentMessageListenerContainer<>(cf, containerProperties);
    }

    /**
     * Membuat {@link ConcurrentKafkaListenerContainerFactory} yang digunakan oleh anotasi {@code @KafkaListener}
     * agar dapat membuat listener Kafka yang bersifat concurrent dan dapat membalas pesan menggunakan KafkaTemplate.
     *
     * @return ConcurrentKafkaListenerContainerFactory untuk listener Kafka
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setReplyTemplate(kafkaTemplate());
        return factory;
    }
}
