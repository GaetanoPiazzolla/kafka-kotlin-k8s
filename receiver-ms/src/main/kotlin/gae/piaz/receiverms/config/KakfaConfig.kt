package gae.piaz.receiverms.config

import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer


@Configuration
class KakfaConfig(
    private val kafkaProperties: KafkaProperties,
    @Value("\${tpd.topic-name}") private val topicName: String
) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    // JSON consumer factory
    @Bean
    fun consumerFactory(): ConsumerFactory<String?, Any> {
        log.info("initializing consumerFactory")
        val jsonDeserializer = JsonDeserializer<Any>()
        jsonDeserializer.addTrustedPackages("*")
        return DefaultKafkaConsumerFactory(
            kafkaProperties.buildConsumerProperties(), StringDeserializer(), jsonDeserializer
        )
    }
    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Any>? {
        log.info("initializing kafkaListenerContainerFactory")
        val factory = ConcurrentKafkaListenerContainerFactory<String, Any>()
        factory.consumerFactory = consumerFactory()
        return factory
    }

    // String Consumer Configuration
    @Bean
    fun stringConsumerFactory(): ConsumerFactory<String?, String?> {
        return DefaultKafkaConsumerFactory(
            kafkaProperties.buildConsumerProperties(), StringDeserializer(), StringDeserializer()
        )
    }
    @Bean
    fun kafkaListenerStringContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String>? {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = stringConsumerFactory()
        return factory
    }

}