package gae.piaz.orderms.controller

import gae.piaz.orderms.MessageData
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.stream.IntStream


@RestController
class OrderController(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    @Value("\${tpd.topic-name}") private val topicName: String,
    @Value("\${tpd.messages-per-request}") private val messagesPerRequest: Int
) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    @GetMapping("/")
    fun home(): String {
        log.info("trying to send {} messages", messagesPerRequest)

        IntStream.range(0, messagesPerRequest)
            .forEach { i: Int ->
                this.kafkaTemplate.send(
                    topicName, i.toString(),
                    MessageData("Message Data", i)
                )
                log.info("message {} sent", i)
            }

        return "Hello World";
    }

}