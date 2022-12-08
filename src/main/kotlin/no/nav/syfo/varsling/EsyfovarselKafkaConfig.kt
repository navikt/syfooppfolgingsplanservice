package no.nav.syfo.varsling


import no.nav.syfo.kafka.AivenKafkaConfig
import no.nav.syfo.varsling.domain.EsyfovarselHendelse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate


@EnableKafka
@Configuration
class EsyfovarselKafkaConfig(
   private val aivenKafkaConfig: AivenKafkaConfig
) {
    @Bean
    fun esyfovarselKafkaTemplate(): KafkaTemplate<String, EsyfovarselHendelse> {
        return KafkaTemplate(DefaultKafkaProducerFactory(aivenKafkaConfig.producerProperties()))
    }
}