package no.nav.syfo.lps.database

import no.nav.syfo.domain.Fodselsnummer
import no.nav.syfo.repository.DbUtil
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.support.SqlLobValue
import org.springframework.stereotype.Repository
import java.sql.Date
import java.sql.Types
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@Repository
class OppfolgingsplanLPSDAO @Inject constructor(
    private val jdbcTemplate: JdbcTemplate,
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
) {
    fun get(fnr: Fodselsnummer): List<POppfolgingsplanLPS> {
        val query = """
            SELECT *
            FROM OPPFOLGINGSPLANLPS
            WHERE fnr = :fnr
        """.trimIndent()
        val mapSql = MapSqlParameterSource()
            .addValue("fnr", fnr.value)
        return namedParameterJdbcTemplate.query(
            query,
            mapSql,
            oppfolgingsplanLPSRowMapper
        )
    }

    fun get(uuid: UUID): POppfolgingsplanLPS {
        val query = """
            SELECT *
            FROM OPPFOLGINGSPLANLPS
            WHERE oppfolgingsplanlps_uuid = :uuid
        """.trimIndent()
        val mapSql = MapSqlParameterSource()
            .addValue("uuid", uuid.toString())
        return namedParameterJdbcTemplate.query(
            query,
            mapSql,
            oppfolgingsplanLPSRowMapper
        ).first()
    }

    fun get(id: Long): POppfolgingsplanLPS {
        val query = """
            SELECT *
            FROM OPPFOLGINGSPLANLPS
            WHERE oppfolgingsplanlps_id = :id
        """.trimIndent()
        val mapSql = MapSqlParameterSource()
            .addValue("id", id.toString())
        return namedParameterJdbcTemplate.query(
            query,
            mapSql,
            oppfolgingsplanLPSRowMapper
        ).first()
    }

    fun getPlansWithoutPDF(): List<POppfolgingsplanLPS> {
        val query = """
            SELECT *
            FROM OPPFOLGINGSPLANLPS
            WHERE pdf IS NULL
        """.trimIndent()
        return namedParameterJdbcTemplate.query(
            query,
            oppfolgingsplanLPSRowMapper
        )
    }

    fun getPlanListToJournalpost(): List<POppfolgingsplanLPS> {
        val query = """
            SELECT *
            FROM OPPFOLGINGSPLANLPS
            WHERE journalpost_id IS NULL AND delt_med_nav = 1 AND pdf IS NOT NULL
        """.trimIndent()
        return namedParameterJdbcTemplate.query(
            query,
            oppfolgingsplanLPSRowMapper
        )
    }

    fun updateJournalpostId(id: Long, journalpostId: String) {
        val updated = DbUtil.convert(LocalDateTime.now())

        val query = """
            UPDATE OPPFOLGINGSPLANLPS
            SET journalpost_id = :journalpost_id, sist_endret = :sist_endret
            WHERE oppfolgingsplanlps_id = :id
        """.trimIndent()

        val mapSaveSql = MapSqlParameterSource()
            .addValue("journalpost_id", journalpostId)
            .addValue("sist_endret", updated)
            .addValue("id", id)
        namedParameterJdbcTemplate.update(query, mapSaveSql)
    }

    fun create(
        arbeidstakerFnr: Fodselsnummer,
        virksomhetsnummer: String,
        xml: String,
        deltMedNAV: Boolean,
        delMedFastlege: Boolean,
        deltMedFastlege: Boolean,
        archiveReference: String
    ): Pair<Long, UUID> {
        val id = DbUtil.nesteSekvensverdi("OPPFOLGINGSPLANLPS_ID_SEQ", jdbcTemplate)
        val uuid = UUID.randomUUID()
        val query = """
            INSERT INTO OPPFOLGINGSPLANLPS (
                oppfolgingsplanlps_id,
                oppfolgingsplanlps_uuid,
                fnr,
                virksomhetsnummer,
                opprettet,
                sist_endret,
                xml,
                delt_med_nav,
                del_med_fastlege,
                delt_med_fastlege,
                archive_reference
            )
            VALUES (
                :oppfolgingsplanlps_id,
                :oppfolgingsplanlps_uuid,
                :fnr,
                :virksomhetsnummer,
                :opprettet,
                :sist_endret,
                :xml,
                :delt_med_nav,
                :del_med_fastlege,
                :delt_med_fastlege,
                :archive_reference
            )
        """.trimIndent()

        val created = DbUtil.convert(LocalDateTime.now())

        val mapSaveSql = MapSqlParameterSource()
            .addValue("oppfolgingsplanlps_id", id)
            .addValue("oppfolgingsplanlps_uuid", uuid.toString())
            .addValue("fnr", arbeidstakerFnr.value)
            .addValue("virksomhetsnummer", virksomhetsnummer)
            .addValue("opprettet", created)
            .addValue("sist_endret", created)
            .addValue("xml", SqlLobValue(xml), Types.CLOB)
            .addValue("delt_med_nav", deltMedNAV)
            .addValue("del_med_fastlege", delMedFastlege)
            .addValue("delt_med_fastlege", deltMedFastlege)
            .addValue("archive_reference", archiveReference)
        namedParameterJdbcTemplate.update(query, mapSaveSql)
        return Pair(id, uuid)
    }

    fun updatePdf(
        id: Long,
        pdf: ByteArray
    ) {
        val updated = DbUtil.convert(LocalDateTime.now())

        val query = """
            UPDATE OPPFOLGINGSPLANLPS
            SET pdf = :pdf, sist_endret = :sist_endret
            WHERE oppfolgingsplanlps_id = :id
        """.trimIndent()

        val mapSaveSql = MapSqlParameterSource()
            .addValue("pdf", SqlLobValue(pdf), Types.BLOB)
            .addValue("sist_endret", updated)
            .addValue("id", id)
        namedParameterJdbcTemplate.update(query, mapSaveSql)
    }

    fun updateSharedWithFastlege(
        id: Long
    ) {
        val updated = DbUtil.convert(LocalDateTime.now())

        val query = """
            UPDATE OPPFOLGINGSPLANLPS
            SET delt_med_fastlege = :delt_med_fastlege, sist_endret = :sist_endret
            WHERE oppfolgingsplanlps_id = :id
        """.trimIndent()

        val mapSaveSql = MapSqlParameterSource()
            .addValue("delt_med_fastlege", true)
            .addValue("sist_endret", updated)
            .addValue("id", id)
        namedParameterJdbcTemplate.update(query, mapSaveSql)
    }

    fun getPlansNotYetMigrated(
            cutoffDate: LocalDate,
            batchSize: Int,
    ): List<POppfolgingsplanLPSWithJournalpostId> {
        val query = """
            SELECT *
            FROM OPPFOLGINGSPLANLPS
            WHERE migrert = 0
            AND opprettet < :cutoff
            AND rownum <= :batch_size
        """.trimIndent()
        val mapSql = MapSqlParameterSource()
            .addValue("cutoff", Date.valueOf(cutoffDate))
            .addValue("batch_size", batchSize)
        return namedParameterJdbcTemplate.query(
                query,
                mapSql,
                oppfolgingsplanLPSWithJournalpostIdRowMapper,
        )
    }

    fun updateMigrationStatus(oppfolgingsplanUUID: UUID) {
        val updated = DbUtil.convert(LocalDateTime.now())

        val query = """
            UPDATE OPPFOLGINGSPLANLPS
            SET migrert = :migrert, sist_endret = :sist_endret
            WHERE oppfolgingsplanlps_uuid = :uuid
        """.trimIndent()

        val mapSaveSql = MapSqlParameterSource()
                .addValue("migrert", 1)
                .addValue("sist_endret", updated)
                .addValue("uuid", oppfolgingsplanUUID.toString())
        namedParameterJdbcTemplate.update(query, mapSaveSql)
    }

    val oppfolgingsplanLPSWithJournalpostIdRowMapper = RowMapper { resultSet, _ ->
        POppfolgingsplanLPSWithJournalpostId(
                id = resultSet.getLong("oppfolgingsplanlps_id"),
                uuid = UUID.fromString(resultSet.getString("oppfolgingsplanlps_uuid")),
                fnr = resultSet.getString("fnr"),
                virksomhetsnummer = resultSet.getString("virksomhetsnummer"),
                opprettet = resultSet.getTimestamp("opprettet").toLocalDateTime(),
                sistEndret = resultSet.getTimestamp("sist_endret").toLocalDateTime(),
                pdf = resultSet.getBytes("pdf"),
                xml = resultSet.getString("xml"),
                deltMedNav = resultSet.getBoolean("delt_med_nav"),
                delMedFastlege = resultSet.getBoolean("del_med_fastlege"),
                deltMedFastlege = resultSet.getBoolean("delt_med_fastlege"),
                archiveReference = resultSet.getString("archive_reference"),
                journalpostId = resultSet.getString("journalpost_id"),
        )
    }

    val oppfolgingsplanLPSRowMapper = RowMapper { resultSet, _ ->
        POppfolgingsplanLPS(
            id = resultSet.getLong("oppfolgingsplanlps_id"),
            uuid = UUID.fromString(resultSet.getString("oppfolgingsplanlps_uuid")),
            fnr = resultSet.getString("fnr"),
            virksomhetsnummer = resultSet.getString("virksomhetsnummer"),
            opprettet = resultSet.getTimestamp("opprettet").toLocalDateTime(),
            sistEndret = resultSet.getTimestamp("sist_endret").toLocalDateTime(),
            pdf = resultSet.getBytes("pdf"),
            xml = resultSet.getString("xml"),
            deltMedNav = resultSet.getBoolean("delt_med_nav"),
            delMedFastlege = resultSet.getBoolean("del_med_fastlege"),
            deltMedFastlege = resultSet.getBoolean("delt_med_fastlege"),
            archiveReference = resultSet.getString("archive_reference")
        )
    }
}
