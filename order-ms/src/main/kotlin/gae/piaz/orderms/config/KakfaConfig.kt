package gae.piaz.orderms.config

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class KakfaConfig(
    private val kafkaProperties: KafkaProperties,
    @Value("\${tpd.topic-name}") private val topicName: String
) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    // Producer configuration
    @Bean
    fun producerConfigs(): Map<String, Any> {
        log.info("initializing producerConfigs")
        val props: MutableMap<String, Any> = HashMap(
            kafkaProperties.buildProducerProperties()
        )
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        return props
    }

    @Bean
    fun producerFactory(): ProducerFactory<String?, Any?> {
        log.info("initializing producerFactory")
        return DefaultKafkaProducerFactory(producerConfigs())
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String?, Any?> {
        log.info("initializing template")
        return KafkaTemplate(producerFactory())
    }

    @Bean
    fun orderTopic(): NewTopic {
        log.info("initializing topic")
        return NewTopic(topicName, 1, 1.toShort())
    }
}