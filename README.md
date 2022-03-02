# kafka-kotlin-k8s

Example project working with two Kotlin microservices communicating through Kafka. 

Kafka has been deployed with 3 brokers and the Yahoo manager:

![image](https://user-images.githubusercontent.com/20367555/156206780-f7e00aba-9abd-4f53-82d6-9ff5d522d9f2.png)

Only one of the two kotlin Microservices are exposed to the outside trough a LoadBalancer service:

![image](https://user-images.githubusercontent.com/20367555/156207260-77877933-fe81-4d14-b431-f335e3ce2dc5.png)

# Key Snippets

### Kafka Producer Configuration

```kotlin
@Configuration
class KakfaConfig(
    private val kafkaProperties: KafkaProperties,
    @Value("\${tpd.topic-name}") private val topicName: String
) {

    @Bean
    fun producerConfigs(): Map<String, Any> {
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
```

### Kafka Consumer Configuration

```kotlin
@Configuration
class KakfaConfig(
    private val kafkaProperties: KafkaProperties,
    @Value("\${tpd.topic-name}") private val topicName: String
) {
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
```

### Listener:

```kotlin
@Service
class OrderTopicListener {

    private val log = LoggerFactory.getLogger(this.javaClass)
    
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
```

### Producer:

```kotlin
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
```

