package no.nav.syfo.altinnkanal

import no.nav.syfo.kafka.AivenKafkaConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory

@EnableKafka
@Configuration
class AltinnKanalKafkaConfig(
    private val aivenKafkaConfig: AivenKafkaConfig,
) {
    @Bean
    fun consumerFactory(): ConsumerFactory<String, String> {
        val aivenConsumerProperties = aivenKafkaConfig.consumerProperties()
        aivenConsumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "syfooppfolgingsplanservice")
        return DefaultKafkaConsumerFactory(aivenConsumerProperties)
    }

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = consumerFactory()
        return factory
    }
}
