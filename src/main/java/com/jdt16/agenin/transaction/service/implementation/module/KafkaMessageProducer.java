package com.jdt16.agenin.transaction.service.implementation.module;

import com.jdt16.agenin.transaction.dto.exception.CoreThrowHandlerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.context.MessageSource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

/**
 * Komponen untuk mengirim pesan ke Kafka, baik secara asinkron maupun sinkron.
 *
 * <p>Kelas ini menyediakan metode untuk pengiriman pesan ke topic Kafka tanpa menunggu balasan (asinkron),
 * serta pengiriman pesan dengan menunggu balasan pada topic tertentu (sinkron).
 *
 * <p>Dalam pengiriman sinkron, jika terjadi kesalahan pada pengiriman atau penerimaan balasan,
 * kelas ini akan melempar exception {@link CoreThrowHandlerException}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaMessageProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ReplyingKafkaTemplate<String, String, String> kafkaReplyTemplate;
    private MessageSource messageSource;

    /**
     * Mengirim pesan ke Kafka secara asinkron ke topik yang ditentukan.
     *
     * @param topic   nama topik Kafka tujuan pengiriman pesan
     * @param message isi pesan yang akan dikirim ke Kafka
     */
    public void sendToKafka(String topic, String message) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, message);
        kafkaTemplate.send(record);
    }

    /**
     * Mengirim pesan ke Kafka secara sinkron dan menunggu balasan pada topik reply.
     *
     * <p>Metode ini mengirim pesan ke topik tujuan dan menetapkan topik balasan (replyTopic)
     * pada header pesan. Proses akan menunggu hingga response diterima.
     *
     * <p>Jika terjadi error saat pengiriman atau penerimaan pesan,
     * exception {@link CoreThrowHandlerException} dengan kode {@code INTERNAL_SERVER_ERROR}
     * akan dilemparkan.
     *
     * @param topic      nama topik Kafka tujuan pengiriman pesan
     * @param replyTopic nama topik Kafka untuk menerima balasan
     * @param message    isi pesan yang akan dikirim
     * @return pesan balasan dalam bentuk string yang diterima dari topik reply
     * @throws CoreThrowHandlerException jika terjadi kesalahan selama pengiriman atau penerimaan balasan
     */
    public String sendToKafkaSync(String topic, String replyTopic, String message) {
        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, replyTopic, message);

            Headers headers = record.headers();
            headers.add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, replyTopic.getBytes()));

            RequestReplyFuture<String, String, String> future = kafkaReplyTemplate.sendAndReceive(record);

            ConsumerRecord<String, String> response = future.get();

            return response.value();

        } catch (ExecutionException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            throw new CoreThrowHandlerException("INTERNAL_SERVER_ERROR");
        }
    }
}
