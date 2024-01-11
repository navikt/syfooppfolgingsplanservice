package no.nav.syfo.lps.kafka

import no.nav.syfo.kafka.AivenKafkaConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate

@EnableKafka
@Configuration
class OppfolgingsplanLPSConfig(
    private val aivenKafkaConfig: AivenKafkaConfig,
) {
    @Bean
    fun oppfolgingsplanLPSKafkaTemplate(): KafkaTemplate<String, KOppfolgingsplanLPS> {
        return KafkaTemplate(DefaultKafkaProducerFactory(aivenKafkaConfig.producerProperties()))
    }

    @Bean
    fun lpsMigrationKafkaTemplate(): KafkaTemplate<String, AltinnLpsOppfolgingsplan> {
        return KafkaTemplate(DefaultKafkaProducerFactory(aivenKafkaConfig.producerProperties()))
    }
}
