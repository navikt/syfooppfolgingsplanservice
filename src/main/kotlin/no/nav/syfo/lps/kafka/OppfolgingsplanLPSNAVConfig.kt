package no.nav.syfo.lps.kafka

import org.apache.kafka.common.config.SaslConfigs
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.*
import java.util.*

@EnableKafka
@Configuration
class OppfolgingsplanLPSNAVConfig(
    @Value("\${srv.username}") private val kafkaUsername: String,
    @Value("\${srv.password}") private val kafkaPassword: String,
    @Value("\${kafka.bootstrap.servers.url}") private val kafkaBootstrapServers: String
) {
    @Bean
    fun producerFactory(): ProducerFactory<String, Any> {
        val producerProperties = HashMap<String, Any>()
        producerProperties["group.id"] = "syfoopservice"
        producerProperties["schema.registry.url"] = "http://kafka-schema-registry.tpa:8081"
        producerProperties["security.protocol"] = "SASL_SSL"
        producerProperties["sasl.mechanism"] = "PLAIN"
        producerProperties[SaslConfigs.SASL_JAAS_CONFIG] = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"$kafkaUsername\" password=\"$kafkaPassword\";"
        producerProperties["bootstrap.servers"] = kafkaBootstrapServers
        producerProperties["key.serializer"] = "io.confluent.kafka.serializers.KafkaAvroSerializer"
        producerProperties["value.serializer"] = "io.confluent.kafka.serializers.KafkaAvroSerializer"
        producerProperties["specific.avro.reader"] = true
        return DefaultKafkaProducerFactory(producerProperties)
    }

    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, Any>): KafkaTemplate<String, Any> {
        return KafkaTemplate(producerFactory)
    }
}
