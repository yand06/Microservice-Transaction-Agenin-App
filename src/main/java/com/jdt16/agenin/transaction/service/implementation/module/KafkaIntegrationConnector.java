package com.jdt16.agenin.transaction.service.implementation.module;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jdt16.agenin.transaction.dto.exception.CoreThrowHandlerException;
import com.jdt16.agenin.transaction.dto.response.RestApiResponse;
import com.jdt16.agenin.transaction.service.interfacing.module.KafkaConnector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

/**
 * Implementasi dari interface {@link KafkaConnector} yang mengatur komunikasi dengan Kafka.
 *
 * <p>Kelas ini menyediakan metode untuk mengirim pesan ke Kafka baik secara asinkron maupun sinkron,
 * serta menangani parsing dan response message pada komunikasi sinkron.
 *
 * <p>Pengiriman sinkron dilakukan dengan mengirimkan request kemudian menunggu balasan pada topik reply tertentu.
 * Jika terjadi error saat proses sinkron, exception khusus {@link CoreThrowHandlerException} dilemparkan.
 *
 * <p>Logging juga diterapkan untuk membantu pelacakan pesan dan error selama pengiriman.
 *
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaIntegrationConnector implements KafkaConnector {
    private final KafkaMessageProducer kafkaMessageProducer;
    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    /**
     * Mengirim pesan ke Kafka secara asinkron.
     *
     * <p>Metode ini mengirimkan {@code request} ke topik Kafka yang ditentukan tanpa menunggu balasan.
     * Jika terjadi kegagalan, exception {@link CoreThrowHandlerException} dapat dilemparkan.
     *
     * @param request isi pesan yang akan dikirim ke Kafka dalam format string
     * @param topic   topik Kafka tujuan pengiriman pesan
     * @throws CoreThrowHandlerException jika terjadi kegagalan pengiriman pesan
     */
    @Override
    public void kafkaAsync(String request, String topic) throws CoreThrowHandlerException {
        log.info("messsage --- {} topic --- {}", request, topic);
        kafkaMessageProducer.sendToKafka(topic, request);
    }

    /**
     * Mengirim pesan ke Kafka dan menunggu balasan secara sinkron.
     *
     * <p>Metode ini mengirimkan {@code request} ke topik Kafka {@code topic} dan menunggu balasan dari {@code replyTopic}.
     * Response akan diparsing menjadi {@link RestApiResponse} secara generik.
     *
     * <p>Jika terjadi error selama proses pengiriman atau parsing, exception
     * {@link CoreThrowHandlerException} dilemparkan dengan pesan error {@code INTERNAL_SERVER_ERROR}.
     *
     * @param request    isi pesan yang akan dikirim ke Kafka dalam format string
     * @param topic      topik Kafka tujuan pesan dikirim
     * @param replyTopic topik Kafka untuk menerima balasan
     * @return response hasil dari Kafka dalam bentuk objek {@link RestApiResponse}
     * @throws CoreThrowHandlerException jika terjadi kesalahan pengiriman atau parsing response
     */
    @Override
    public RestApiResponse<?> kafkaSync(String request, String topic, String replyTopic) throws CoreThrowHandlerException {
        log.info("messsage --- {} topic --- {} replyTopic --- {}", request, topic, replyTopic);
        try {
            TypeReference<RestApiResponse<Object>> typeReference = new TypeReference<>() {
            };

            String response = kafkaMessageProducer.sendToKafkaSync(topic, replyTopic, request);

            return objectMapper.readValue(response, typeReference);
        } catch (Exception e) {
            log.error("kafka sync error", e);
            throw new CoreThrowHandlerException("INTERNAL_SERVER_ERROR");
        }

    }

    /**
     * Membuat dan menyiapkan objek {@link ProducerRecord} Kafka dari topik dan pesan yang diberikan.
     *
     * <p>Metode ini saat ini hanya membuat objek record dan tidak melakukan pengiriman.
     *
     * @param topic   topik Kafka tujuan pesan
     * @param message isi pesan yang ingin dikirim
     */
    public void sendToKafka(String topic, String message) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, message);
    }
}
