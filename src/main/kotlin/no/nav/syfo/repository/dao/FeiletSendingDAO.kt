package no.nav.syfo.repository.dao

import no.nav.syfo.domain.FeiletSending
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
    fun findByOppfolgingsplanId(oppfolgingsplanId: Long?): FeiletSending {
        val pFeiletSending = jdbcTemplate.queryForObject("select * from feilet_sending where max_retries > number_of_tries", FeiletSendingRowMapper())
        if(pFeiletSending != null) {
            return mapPFeiletSendingToFeiletSending(pFeiletSending)
        } else {
            throw RuntimeException("No FeiletSending was found in database with given ID")
        }
    }

    fun hentFeiledeSendinger(): List<FeiletSending> {
        val feiletSendingList = Optional.ofNullable(jdbcTemplate.query("select * from feilet_sending where max_retries > number_of_tries", FeiletSendingRowMapper())).orElse(emptyList())
        return feiletSendingList.stream()
                .map { pFeiletSending: PFeiletSending -> mapPFeiletSendingToFeiletSending(pFeiletSending) }
                .collect(Collectors.toList())
    }

    fun create(
            oppfolgingsplanId: Long,
            max_retries: Int
    ) {
        val feiletSendingId = DbUtil.nesteSekvensverdi("FEILET_SENDING_ID_SEQ", jdbcTemplate)
        val namedParameters = MapSqlParameterSource()
                .addValue("id", feiletSendingId)
                .addValue("oppfolgingsplanlps_id", oppfolgingsplanId)
                .addValue("number_of_tries", 1)
                .addValue("max_retries", max_retries)
                .addValue("opprettetDato", DbUtil.convert(LocalDateTime.now()))
                .addValue("sistEndretDato", DbUtil.convert(LocalDateTime.now()))
        namedParameterJdbcTemplate.update("insert into feilet_sending " +
                "(id, oppfolgingsplanlps_id, number_of_tries, max_retries, opprettetDato, sistEndretDato) values" +
                "(:id, :oppfolgingsplanlps_id, :number_of_tries, :max_retries, :opprettetDato, :sistEndretDato)", namedParameters)
    }

    fun updateAfterRetry(feiletSending: FeiletSending) {
        val namedParameters = MapSqlParameterSource()
                .addValue("id", feiletSending.id)
                .addValue("number_of_tries", feiletSending.number_of_tries)
                .addValue("sistEndretDato", DbUtil.convert(LocalDateTime.now()))
        namedParameterJdbcTemplate.update("update feilet_sending " +
                "set number_of_tries=:number_of_tries, sistEndretDato=:sistEndretDato" +
                "where id=:id", namedParameters)
    }

    fun remove(oppfolgingsplanId: Long?) {
        val namedParameters = MapSqlParameterSource()
                .addValue("oppfolgingsplanlps_id", oppfolgingsplanId)
        namedParameterJdbcTemplate.update("delete from feilet_sending " +
                "where oppfolgingsplanlps_id=:oppfolgingsplanlps_id", namedParameters)
    }

    private inner class FeiletSendingRowMapper : RowMapper<PFeiletSending> {
        @Throws(SQLException::class)
        override fun mapRow(rs: ResultSet, rowNum: Int): PFeiletSending {
            return PFeiletSending(
                    id = rs.getLong("id"),
                    oppfolgingsplanId = rs.getLong("oppfoelgingsplanlps_id"),
                    number_of_tries = rs.getInt("number_of_tries"),
                    max_retries = rs.getInt("max_retries"),
                    opprettet = DbUtil.convert(rs.getTimestamp("opprettetDato")),
                    sist_endret = DbUtil.convert(rs.getTimestamp("sistEndretDato")))
        }
    }
}

private fun mapPFeiletSendingToFeiletSending(pFeiletSending: PFeiletSending): FeiletSending {
    return FeiletSending(
            id = pFeiletSending.id,
            oppfolgingsplanId = pFeiletSending.oppfolgingsplanId,
            number_of_tries = pFeiletSending.number_of_tries,
            max_retries = pFeiletSending.max_retries,
            sist_endret = pFeiletSending.sist_endret,
            opprettet = pFeiletSending.opprettet
    )
}
