package no.nav.syfo.altinnkanal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.beans.factory.annotation.Value

@EnableKafka
@Configuration
class AltinnKanalKafkaConfig (
        @Value("\${srv.username}") private val kafkaUsername : String,
        @Value("\${srv.password}") private val kafkaPassword : String,
        @Value("\${kafka.bootstrap.servers.url}") private val kafkaBootstrapServers: String
) {
    @Bean
    fun consumerFactory() : ConsumerFactory<String, String> {
        val consumerProperties = HashMap<String,Any>()
        consumerProperties["group.id"] = "kafka2jms-oppfolgingsplan"
        consumerProperties["schema.registry.url"] = "http://kafka-schema-registry.tpa:8081"
        consumerProperties["key.deserializer"] = "io.confluent.kafka.serializers.KafkaAvroDeserializer"
        consumerProperties["value.deserializer"] = "io.confluent.kafka.serializers.KafkaAvroDeserializer"
        consumerProperties["specific.avro.reader"] = true
        consumerProperties["max.poll.records"] = "1"
        consumerProperties["auto.offset.reset"] = "earliest"
        consumerProperties["enable.auto.commit"] = "false"
        consumerProperties["security.protocol"] = "SASL_SSL"
        consumerProperties["sasl.mechanism"] = "PLAIN"
        consumerProperties[SASL_JAAS_CONFIG] = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"$kafkaUsername\" password=\"$kafkaPassword\";"
        consumerProperties["bootstrap.servers"] = kafkaBootstrapServers
        return DefaultKafkaConsumerFactory(consumerProperties)
    }

    @Bean
    fun kafkaListenerContainerFactory() : ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = consumerFactory()
        return factory
    }
}


