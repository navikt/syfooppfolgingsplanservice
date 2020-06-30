package no.nav.syfo.repository.dao

import no.nav.syfo.domain.Kommentar
import no.nav.syfo.domain.Tiltak
import no.nav.syfo.repository.DbUtil
import no.nav.syfo.repository.domain.PTiltak
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.support.SqlLobValue
import org.springframework.stereotype.Repository
import java.sql.*
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Collectors
import javax.inject.Inject

@Repository
class TiltakDAO @Inject constructor(
    private val jdbcTemplate: JdbcTemplate,
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
    private val kommentarDAO: KommentarDAO
) {
    fun finnTiltakByOppfoelgingsdialogId(oppfoelgingsdialogId: Long): List<Tiltak> {
        val tiltakListe = Optional.ofNullable(jdbcTemplate.query("SELECT * FROM tiltak WHERE oppfoelgingsdialog_id = ?", TiltakRowMapper(), oppfoelgingsdialogId)).orElse(emptyList())
        return tiltakListe
            .stream()
            .map { pTiltak: PTiltak -> mapPTiltakToTiltak(pTiltak, kommentarDAO.finnKommentarerByTiltakId(pTiltak.id)) }
            .collect(Collectors.toList())
    }

    fun finnTiltakById(id: Long): Tiltak {
        val pTiltak = jdbcTemplate.queryForObject("SELECT * FROM tiltak WHERE tiltak_id = ?", TiltakRowMapper(), id)
        if (pTiltak != null) {
            return mapPTiltakToTiltak(pTiltak, null)
        } else {
            throw RuntimeException("No Tiltak was found in database with given ID")
        }
    }

    fun create(tiltak: Tiltak): Tiltak {
        val id = DbUtil.nesteSekvensverdi("TILTAK_ID_SEQ", jdbcTemplate)
        val namedParameters = MapSqlParameterSource()
            .addValue("tiltak_id", id)
            .addValue("oppfoelgingsdialog_id", tiltak.oppfoelgingsdialogId)
            .addValue("navn", DbUtil.sanitizeUserInput(tiltak.navn))
            .addValue("fom", DbUtil.convert(tiltak.fom))
            .addValue("tom", DbUtil.convert(tiltak.tom))
            .addValue("beskrivelse", SqlLobValue(DbUtil.sanitizeUserInput(tiltak.beskrivelse)), Types.CLOB)
            .addValue("beskrivelse_ikke_aktuelt", SqlLobValue(DbUtil.sanitizeUserInput(tiltak.beskrivelseIkkeAktuelt)), Types.CLOB)
            .addValue("opprettet_av", tiltak.opprettetAvAktoerId)
            .addValue("sist_endret_av", tiltak.sistEndretAvAktoerId)
            .addValue("opprettet_dato", DbUtil.convert(LocalDateTime.now()))
            .addValue("sist_endret_dato", DbUtil.convert(LocalDateTime.now()))
            .addValue("status", tiltak.status)
            .addValue("gjennomfoering", SqlLobValue(DbUtil.sanitizeUserInput(tiltak.gjennomfoering)), Types.CLOB)
        namedParameterJdbcTemplate.update("INSERT INTO tiltak (tiltak_id, oppfoelgingsdialog_id, navn, fom, tom, beskrivelse, beskrivelse_ikke_aktuelt, " +
            "opprettet_av, sist_endret_av, opprettet_dato, sist_endret_dato, status, gjennomfoering ) " +
            "VALUES(:tiltak_id, :oppfoelgingsdialog_id, :navn, :fom, :tom, :beskrivelse, :beskrivelse_ikke_aktuelt, " +
            ":opprettet_av, :sist_endret_av, :opprettet_dato, :sist_endret_dato, :status, :gjennomfoering)", namedParameters)
        tiltak.id = id
        return tiltak
    }

    fun update(tiltak: Tiltak): Tiltak {
        val namedParameters = MapSqlParameterSource()
            .addValue("tiltak_id", tiltak.id)
            .addValue("navn", DbUtil.sanitizeUserInput(tiltak.navn))
            .addValue("fom", DbUtil.convert(tiltak.fom))
            .addValue("tom", DbUtil.convert(tiltak.tom))
            .addValue("beskrivelse", SqlLobValue(DbUtil.sanitizeUserInput(tiltak.beskrivelse)), Types.CLOB)
            .addValue("beskrivelse_ikke_aktuelt", SqlLobValue(DbUtil.sanitizeUserInput(tiltak.beskrivelseIkkeAktuelt)), Types.CLOB)
            .addValue("sist_endret_av", tiltak.sistEndretAvAktoerId)
            .addValue("sist_endret_dato", DbUtil.convert(LocalDateTime.now()))
            .addValue("status", tiltak.status)
            .addValue("gjennomfoering", SqlLobValue(DbUtil.sanitizeUserInput(tiltak.gjennomfoering)), Types.CLOB)
        namedParameterJdbcTemplate.update("UPDATE tiltak SET navn = :navn, fom = :fom, tom = :tom, beskrivelse = :beskrivelse, beskrivelse_ikke_aktuelt = :beskrivelse_ikke_aktuelt, " +
            "sist_endret_av = :sist_endret_av, sist_endret_dato = :sist_endret_dato, status = :status, gjennomfoering = :gjennomfoering WHERE " +
            "tiltak_id = :tiltak_id", namedParameters)
        return tiltak
    }

    fun deleteById(id: Long?) {
        jdbcTemplate.update("DELETE FROM tiltak WHERE tiltak_id = ?", id)
    }

    private inner class TiltakRowMapper : RowMapper<PTiltak> {
        @Throws(SQLException::class)
        override fun mapRow(rs: ResultSet, rowNum: Int): PTiltak {
            val pTiltak = PTiltak()
            pTiltak.id = rs.getLong("tiltak_id")
            pTiltak.oppfoelgingsdialogId = rs.getLong("oppfoelgingsdialog_id")
            pTiltak.navn = rs.getString("navn")
            pTiltak.fom = DbUtil.convert(rs.getTimestamp("fom"))
            pTiltak.tom = DbUtil.convert(rs.getTimestamp("tom"))
            pTiltak.beskrivelse = rs.getString("beskrivelse")
            pTiltak.beskrivelseIkkeAktuelt = rs.getString("beskrivelse_ikke_aktuelt")
            pTiltak.opprettetAvAktoerId = rs.getString("opprettet_av")
            pTiltak.sistEndretAvAktoerId = rs.getString("sist_endret_av")
            pTiltak.sistEndretDato = DbUtil.convert(rs.getTimestamp("sist_endret_dato"))
            pTiltak.opprettetDato = DbUtil.convert(rs.getTimestamp("opprettet_dato"))
            pTiltak.status = rs.getString("status")
            pTiltak.gjennomfoering = rs.getString("gjennomfoering")
            return pTiltak
        }
    }
}


private fun mapPTiltakToTiltak(
    pTiltak: PTiltak,
    kommentarList: List<Kommentar>?
): Tiltak {
    val tiltak = Tiltak()
    tiltak.id = pTiltak.id
    tiltak.oppfoelgingsdialogId = pTiltak.oppfoelgingsdialogId
    tiltak.navn = pTiltak.navn
    tiltak.fom = Optional.ofNullable(pTiltak.fom).map { obj: LocalDateTime -> obj.toLocalDate() }.orElse(null)
    tiltak.tom = Optional.ofNullable(pTiltak.tom).map { obj: LocalDateTime -> obj.toLocalDate() }.orElse(null)
    tiltak.beskrivelse = pTiltak.beskrivelse
    tiltak.opprettetAvAktoerId = pTiltak.opprettetAvAktoerId
    tiltak.opprettetDato = pTiltak.opprettetDato
    tiltak.sistEndretAvAktoerId = pTiltak.sistEndretAvAktoerId
    tiltak.sistEndretDato = pTiltak.sistEndretDato
    tiltak.status = pTiltak.status
    tiltak.gjennomfoering = pTiltak.gjennomfoering
    tiltak.beskrivelseIkkeAktuelt = pTiltak.beskrivelseIkkeAktuelt
    tiltak.kommentarer = kommentarList
    return tiltak
}
