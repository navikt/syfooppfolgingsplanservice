package no.nav.syfo.repository.dao

import no.nav.syfo.domain.FeiletSending
import no.nav.syfo.domain.MAX_RETRIES
import no.nav.syfo.repository.DbUtil
import no.nav.syfo.repository.domain.PFeiletSending
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.SQLException
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Collectors
import javax.inject.Inject

@Repository
class FeiletSendingDAO @Inject constructor(
    private val jdbcTemplate: JdbcTemplate,
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
) {
    fun findByOppfolgingsplanId(oppfolgingsplanId: Long): FeiletSending {
        val pFeiletSending = jdbcTemplate.queryForObject("select * from feilet_sending where oppfolgingsplanlps_id = ?", FeiletSendingRowMapper(), oppfolgingsplanId)
        if(pFeiletSending != null) {
            return mapPFeiletSendingToFeiletSending(pFeiletSending)
        } else {
            throw RuntimeException("No FeiletSending was found in database with given ID")
        }
    }

    fun hentFeiledeSendinger(): List<FeiletSending> {
        val query = """
            SELECT * 
            FROM feilet_sending 
            WHERE number_of_tries <= $MAX_RETRIES
            """.trimIndent()
        val feiletSendingList = Optional.ofNullable(jdbcTemplate.query(query, FeiletSendingRowMapper())).orElse(emptyList())
        return feiletSendingList.stream()
                .map { pFeiletSending: PFeiletSending -> mapPFeiletSendingToFeiletSending(pFeiletSending) }
                .collect(Collectors.toList())
    }

    fun create(oppfolgingsplanId: Long) {
        val feiletSendingId = DbUtil.nesteSekvensverdi("FEILET_SENDING_ID_SEQ", jdbcTemplate)

        val query = """
            INSERT INTO feilet_sending (id, oppfolgingsplanlps_id, number_of_tries, opprettet, sist_endret)
            values (:id, :oppfolgingsplanlps_id, :number_of_tries, :opprettet, :sist_endret)
            """.trimIndent()

        val namedParameters = MapSqlParameterSource()
                .addValue("id", feiletSendingId)
                .addValue("oppfolgingsplanlps_id", oppfolgingsplanId)
                .addValue("number_of_tries", 1)
                .addValue("opprettet", DbUtil.convert(LocalDateTime.now()))
                .addValue("sist_endret", DbUtil.convert(LocalDateTime.now()))

        namedParameterJdbcTemplate.update(query, namedParameters)
    }

    fun updateAfterRetry(feiletSending: FeiletSending) {
        val query = """
            UPDATE feilet_sending
            SET number_of_tries=:number_of_tries, sist_endret=:sist_endret
            where id=:id
            """.trimIndent()

        val namedParameters = MapSqlParameterSource()
                .addValue("id", feiletSending.id)
                .addValue("number_of_tries", feiletSending.number_of_tries)
                .addValue("sist_endret", DbUtil.convert(LocalDateTime.now()))

        namedParameterJdbcTemplate.update(query, namedParameters)
    }

    fun remove(oppfolgingsplanId: Long?) {
        val query = """
            DELETE FROM feilet_sending
            WHERE oppfolgingsplanlps_id=:oppfolgingsplanlps_id
            """.trimIndent()

        val namedParameters = MapSqlParameterSource()
                .addValue("oppfolgingsplanlps_id", oppfolgingsplanId)

        namedParameterJdbcTemplate.update(query, namedParameters)
    }

    private inner class FeiletSendingRowMapper : RowMapper<PFeiletSending> {
        @Throws(SQLException::class)
        override fun mapRow(rs: ResultSet, rowNum: Int): PFeiletSending {
            return PFeiletSending(
                    id = rs.getLong("id"),
                    oppfolgingsplanId = rs.getLong("oppfolgingsplanlps_id"),
                    number_of_tries = rs.getInt("number_of_tries"),
                    opprettet = DbUtil.convert(rs.getTimestamp("opprettet")),
                    sist_endret = DbUtil.convert(rs.getTimestamp("sist_endret")))
        }
    }
}

private fun mapPFeiletSendingToFeiletSending(pFeiletSending: PFeiletSending): FeiletSending {
    return FeiletSending(
            id = pFeiletSending.id,
            oppfolgingsplanId = pFeiletSending.oppfolgingsplanId,
            number_of_tries = pFeiletSending.number_of_tries,
            sist_endret = pFeiletSending.sist_endret,
            opprettet = pFeiletSending.opprettet
    )
}
