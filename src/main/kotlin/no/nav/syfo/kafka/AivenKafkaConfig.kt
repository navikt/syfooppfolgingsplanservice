package no.nav.syfo.kafka

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import kotlin.collections.HashMap

@EnableKafka
@Configuration
class AivenKafkaConfig(
    @Value("\${kafka.brokers}") private val aivenBrokers: String,
    @Value("\${kafka.truststore.path}") private val truststorePath: String,
    @Value("\${kafka.keystore.path}") private val keystorePath: String,
    @Value("\${kafka.credstore.password}") private val credstorePassword: String,
    @Value("\${kafka.schema.registry}") private val schemaRegistryUrl: String,
    @Value("\${kafka.schema.registry.user}") private val registryUsername: String,
    @Value("\${kafka.schema.registry.password}") private val registryPassword: String,
) {
    private val JAVA_KEYSTORE = "JKS"
    private val PKCS12 = "PKCS12"
    private val SSL = "SSL"
    private val USER_INFO = "USER_INFO"
    private val BASIC_AUTH_CREDENTIALS_SOURCE = "basic.auth.credentials.source"
    private val userinfoConfig = "$registryUsername:$registryPassword"

    fun producerProperties(): HashMap<String, Any> {
        val producerProperties = HashMap<String, Any>().apply {
            put(ProducerConfig.ACKS_CONFIG, "all")
            put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SSL)
            put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "")
            put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, JAVA_KEYSTORE)
            put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, PKCS12)
            put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, truststorePath)
            put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, credstorePassword)
            put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keystorePath)
            put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, credstorePassword)
            put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, credstorePassword)
            remove(SaslConfigs.SASL_MECHANISM)
            remove(SaslConfigs.SASL_JAAS_CONFIG)

            put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, aivenBrokers)
            put(BASIC_AUTH_CREDENTIALS_SOURCE, USER_INFO)

            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonKafkaSerializer::class.java)
        }
        return producerProperties
    }

    fun consumerProperties(): HashMap<String, Any> {
        val consumerProperties = HashMap<String, Any>().apply {
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
            put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1")
            put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")

            put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SSL)
            put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "")
            put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, JAVA_KEYSTORE)
            put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, PKCS12)
            put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, truststorePath)
            put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, credstorePassword)
            put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keystorePath)
            put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, credstorePassword)
            put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, credstorePassword)
            remove(SaslConfigs.SASL_MECHANISM)
            remove(SaslConfigs.SASL_JAAS_CONFIG)

            put(BASIC_AUTH_CREDENTIALS_SOURCE, USER_INFO)
            put(SchemaRegistryClientConfig.USER_INFO_CONFIG, userinfoConfig)
            put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl)
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroDeserializer")
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroDeserializer")
            put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true)

            put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, aivenBrokers)
        }

        return consumerProperties
    }
}
