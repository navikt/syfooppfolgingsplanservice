package no.nav.syfo.lps.database

import no.nav.syfo.repository.DbUtil
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.support.SqlLobValue
import org.springframework.stereotype.Repository
import java.sql.Types
import java.time.LocalDateTime
import javax.inject.Inject

@Repository
class OppfolgingsplanLPSRetryDAO @Inject constructor(
    private val jdbcTemplate: JdbcTemplate,
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
) {
    fun create(
        archiveReference: String,
        xml: String
    ): Long {
        val id = DbUtil.nesteSekvensverdi("PLANLPS_RETRY_ID_SEQ", jdbcTemplate)
        val query = """
            INSERT INTO OPPFOLGINGSPLANLPS_RETRY (
                oppfolgingsplanlps_retry_id,
                archive_reference,
                xml,
                opprettet
            )
            VALUES (
                :oppfolgingsplanlpsRetryId,
                :archiveReference,
                :xml,
                :opprettet
            )
            """.trimIndent()

        val created = DbUtil.convert(LocalDateTime.now())

        val mapSaveSql = MapSqlParameterSource()
            .addValue("oppfolgingsplanlpsRetryId", id)
            .addValue("archiveReference", archiveReference)
            .addValue("xml", SqlLobValue(xml), Types.CLOB)
            .addValue("opprettet", created)
        namedParameterJdbcTemplate.update(query, mapSaveSql)
        return id
    }
}
