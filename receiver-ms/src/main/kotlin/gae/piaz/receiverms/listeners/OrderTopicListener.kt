package gae.piaz.receiverms.listeners

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.Headers
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import java.util.stream.StreamSupport

@Service
class OrderTopicListener {

    private val log = LoggerFactory.getLogger(this.javaClass)

    // TODO direct Json serialization does not work for a ClassNotFound. Maybe MessageData should have the same package.
//    @KafkaListener(
//        topics = ["order-topic-1"],
//        containerFactory = "kafkaListenerContainerFactory"
//    )
//    fun kafkaListener(
//        cr: ConsumerRecord<String?, MessageData?>,
//        @Payload payload: MessageData?
//    ) {
//        log.info(
//            "kafkaListener - received key {}: Type [{}] | Payload: {} | Record: {}", cr.key(),
//            typeIdHeader(cr.headers()), payload, cr.toString()
//        )
//    }

    @KafkaListener(
        topics = ["order-topic-1"],
        containerFactory = "kafkaListenerStringContainerFactory"
    )
    fun kafkaListener(
        cr: ConsumerRecord<String, String>,
        @Payload payload: String
    ) {
        log.info(
            "kafkaListener - received key {}: Type [{}] | Payload: {} | Record: {}", cr.key(),
            typeIdHeader(cr.headers()), payload, cr.toString()
        )
    }


    fun typeIdHeader(headers: Headers): String {
        return StreamSupport.stream(headers.spliterator(), false)
            .filter {
                it.key().equals("__TypeId__")
            }.findFirst().map {
                String(it.value())
            }.orElse("N/A")
    }


}