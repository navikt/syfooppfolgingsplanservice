package no.nav.syfo.lps.kafka

import no.nav.syfo.kafka.AivenKafkaConfig
import no.nav.syfo.oppfolgingsplan.avro.KOppfolgingsplanLPSNAV
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate

@EnableKafka
@Configuration
class OppfolgingsplanLPSNAVConfig(
    private val aivenKafkaConfig: AivenKafkaConfig,
) {
    @Bean
    fun oppfolgingsplanLPSNAVkafkaTemplate(): KafkaTemplate<String, KOppfolgingsplanLPSNAV> {
        return KafkaTemplate(DefaultKafkaProducerFactory(aivenKafkaConfig.producerProperties()))
    }
}
