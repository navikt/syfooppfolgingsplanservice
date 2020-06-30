package no.nav.syfo.repository.dao

import no.nav.syfo.domain.Kommentar
import no.nav.syfo.mappers.persistency.POppfoelgingsdialogMapper
import no.nav.syfo.repository.DbUtil
import no.nav.syfo.repository.domain.PKommentar
import no.nav.syfo.util.MapUtil
import org.omg.SendingContext.RunTime
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.support.SqlLobValue
import org.springframework.stereotype.Repository
import java.lang.RuntimeException
import java.sql.*
import java.time.LocalDateTime
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors
import javax.inject.Inject

@Repository
class KommentarDAO @Inject constructor(
    private val jdbcTemplate: JdbcTemplate,
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
) {
    fun finnKommentar(id: Long): Kommentar {
        val pKommentar = (jdbcTemplate.queryForObject("SELECT * FROM kommentar WHERE kommentar_id = ?", KommentarRowMapper(), id))
        if (pKommentar != null) {
            return mapPKommentarToKommentar(pKommentar)
        } else {
            throw RuntimeException("No Kommentar was found in database with given ID")
        }
    }

    fun finnKommentarerByTiltakId(tiltakId: Long): List<Kommentar> {
        val kommentarList =  Optional.ofNullable(jdbcTemplate.query("SELECT * FROM kommentar WHERE tiltak_id = ?", KommentarRowMapper(), tiltakId)).orElse(emptyList())
        return kommentarList.stream()
            .map { pKommentar: PKommentar -> mapPKommentarToKommentar(pKommentar) }
            .collect(Collectors.toList())
    }

    fun create(kommentar: Kommentar): Kommentar {
        val id = DbUtil.nesteSekvensverdi("KOMMENTAR_ID_SEQ", jdbcTemplate)
        val namedParameters = MapSqlParameterSource()
            .addValue("kommentar_id", id)
            .addValue("tiltak_id", kommentar.tiltakId)
            .addValue("tekst", SqlLobValue(DbUtil.sanitizeUserInput(kommentar.tekst)), Types.CLOB)
            .addValue("sist_endret_av", kommentar.sistEndretAvAktoerId)
            .addValue("sist_endret_dato", DbUtil.convert(LocalDateTime.now()))
            .addValue("opprettet_av", kommentar.opprettetAvAktoerId)
            .addValue("opprettet_dato", DbUtil.convert(LocalDateTime.now()))
        namedParameterJdbcTemplate.update("INSERT INTO kommentar (kommentar_id, tiltak_id, tekst, sist_endret_av, sist_endret_dato, opprettet_av, opprettet_dato) " +
            "VALUES(:kommentar_id, :tiltak_id, :tekst, :sist_endret_av, :sist_endret_dato, :opprettet_av, :opprettet_dato)", namedParameters)
        kommentar.id = id
        return kommentar
    }

    fun delete(id: Long?) {
        jdbcTemplate.update("DELETE FROM kommentar WHERE kommentar_id = ?", id)
    }

    fun update(kommentar: Kommentar): Kommentar {
        val namedParameters = MapSqlParameterSource()
            .addValue("kommentar_id", kommentar.id)
            .addValue("tekst", SqlLobValue(DbUtil.sanitizeUserInput(kommentar.tekst)), Types.CLOB)
            .addValue("sist_endret_av", kommentar.sistEndretAvAktoerId)
            .addValue("sist_endret_dato", DbUtil.convert(LocalDateTime.now()))
        namedParameterJdbcTemplate.update("UPDATE kommentar " +
            "SET tekst = :tekst, sist_endret_av = :sist_endret_av, sist_endret_dato = :sist_endret_dato " +
            "WHERE kommentar_id = :kommentar_id", namedParameters)
        return kommentar
    }

    private inner class KommentarRowMapper : RowMapper<PKommentar> {
        @Throws(SQLException::class)
        override fun mapRow(rs: ResultSet, rowNum: Int): PKommentar {
            val pKommentar = PKommentar()
            pKommentar.id = rs.getLong("kommentar_id")
            pKommentar.tiltakId = rs.getLong("tiltak_id")
            pKommentar.tekst = rs.getString("tekst")
            pKommentar.sistEndretAvAktoerId =rs.getString("sist_endret_av")
            pKommentar.sistEndretDato = DbUtil.convert(rs.getTimestamp("sist_endret_dato"))
            pKommentar.opprettetAvAktoerId = rs.getString("opprettet_av")
            pKommentar.opprettetDato = DbUtil.convert(rs.getTimestamp("opprettet_dato"))
            return pKommentar
        }
    }
}

private fun mapPKommentarToKommentar(pKommentar: PKommentar): Kommentar {
    val kommentar = Kommentar()
    kommentar.id = pKommentar.id
    kommentar.tiltakId = pKommentar.tiltakId
    kommentar.tekst = pKommentar.tekst
    kommentar.sistEndretAvAktoerId = pKommentar.sistEndretAvAktoerId
    kommentar.sistEndretDato = pKommentar.sistEndretDato
    kommentar.opprettetAvAktoerId = pKommentar.opprettetAvAktoerId
    kommentar.opprettetDato = pKommentar.opprettetDato
    return kommentar
}
