package no.nav.syfo.repository.dao;

import no.nav.syfo.domain.Tiltak;
import no.nav.syfo.repository.domain.PTiltak;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.support.SqlLobValue;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toList;
import static no.nav.syfo.util.MapUtil.map;
import static no.nav.syfo.util.MapUtil.mapListe;
import static no.nav.syfo.mappers.persistency.POppfoelgingsdialogMapper.p2tiltak;
import static no.nav.syfo.repository.DbUtil.*;

public class TiltakDAO {

    @Inject
    private JdbcTemplate jdbcTemplate;
    @Inject
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Inject
    private KommentarDAO kommentarDAO;

    public List<Tiltak> finnTiltakByOppfoelgingsdialogId(long oppfoelgingsdialogId) {
        return mapListe(jdbcTemplate.query("SELECT * FROM tiltak WHERE oppfoelgingsdialog_id = ?", new TiltakRowMapper(), oppfoelgingsdialogId), p2tiltak)
                .stream().map(tiltak -> tiltak.kommentarer(kommentarDAO.finnKommentarerByTiltakId(tiltak.id))).collect(toList());
    }

    public Tiltak finnTiltakById(long id) {
        return map(jdbcTemplate.queryForObject("SELECT * FROM tiltak WHERE tiltak_id = ?", new TiltakRowMapper(), id), p2tiltak);
    }

    public Tiltak create(Tiltak tiltak) {
        long id = nesteSekvensverdi("TILTAK_ID_SEQ", jdbcTemplate);
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("tiltak_id", id)
                .addValue("oppfoelgingsdialog_id", tiltak.oppfoelgingsdialogId)
                .addValue("navn", sanitizeUserInput(tiltak.navn))
                .addValue("fom", convert(tiltak.fom))
                .addValue("tom", convert(tiltak.tom))
                .addValue("beskrivelse", new SqlLobValue(sanitizeUserInput(tiltak.beskrivelse)), Types.CLOB)
                .addValue("beskrivelse_ikke_aktuelt", new SqlLobValue(sanitizeUserInput(tiltak.beskrivelseIkkeAktuelt)), Types.CLOB)
                .addValue("opprettet_av", tiltak.opprettetAvAktoerId)
                .addValue("sist_endret_av", tiltak.sistEndretAvAktoerId)
                .addValue("opprettet_dato", convert(now()))
                .addValue("sist_endret_dato", convert(now()))
                .addValue("status", tiltak.status)
                .addValue("gjennomfoering", new SqlLobValue(sanitizeUserInput(tiltak.gjennomfoering)), Types.CLOB);
        namedParameterJdbcTemplate.update("INSERT INTO tiltak (tiltak_id, oppfoelgingsdialog_id, navn, fom, tom, beskrivelse, beskrivelse_ikke_aktuelt, " +
                "opprettet_av, sist_endret_av, opprettet_dato, sist_endret_dato, status, gjennomfoering ) " +
                "VALUES(:tiltak_id, :oppfoelgingsdialog_id, :navn, :fom, :tom, :beskrivelse, :beskrivelse_ikke_aktuelt, " +
                ":opprettet_av, :sist_endret_av, :opprettet_dato, :sist_endret_dato, :status, :gjennomfoering)", namedParameters);
        return tiltak.id(id);
    }

    public Tiltak update(Tiltak tiltak) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("tiltak_id", tiltak.id)
                .addValue("navn", sanitizeUserInput(tiltak.navn))
                .addValue("fom", convert(tiltak.fom))
                .addValue("tom", convert(tiltak.tom))
                .addValue("beskrivelse", new SqlLobValue(sanitizeUserInput(tiltak.beskrivelse)), Types.CLOB)
                .addValue("beskrivelse_ikke_aktuelt", new SqlLobValue(sanitizeUserInput(tiltak.beskrivelseIkkeAktuelt)), Types.CLOB)
                .addValue("sist_endret_av", tiltak.sistEndretAvAktoerId)
                .addValue("sist_endret_dato", convert(now()))
                .addValue("status", tiltak.status)
                .addValue("gjennomfoering", new SqlLobValue(sanitizeUserInput(tiltak.gjennomfoering)), Types.CLOB);
        namedParameterJdbcTemplate.update("UPDATE tiltak SET navn = :navn, fom = :fom, tom = :tom, beskrivelse = :beskrivelse, beskrivelse_ikke_aktuelt = :beskrivelse_ikke_aktuelt, " +
                "sist_endret_av = :sist_endret_av, sist_endret_dato = :sist_endret_dato, status = :status, gjennomfoering = :gjennomfoering WHERE " +
                "tiltak_id = :tiltak_id", namedParameters);
        return tiltak;
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM tiltak WHERE tiltak_id = ?", id);
    }

    private class TiltakRowMapper implements RowMapper<PTiltak> {
        public PTiltak mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new PTiltak()
                    .id(rs.getLong("tiltak_id"))
                    .oppfoelgingsdialogId(rs.getLong("oppfoelgingsdialog_id"))
                    .navn(rs.getString("navn"))
                    .fom(convert(rs.getTimestamp("fom")))
                    .tom(convert(rs.getTimestamp("tom")))
                    .beskrivelse(rs.getString("beskrivelse"))
                    .beskrivelseIkkeAktuelt(rs.getString("beskrivelse_ikke_aktuelt"))
                    .opprettetAvAktoerId(rs.getString("opprettet_av"))
                    .sistEndretAvAktoerId(rs.getString("sist_endret_av"))
                    .sistEndretDato(convert(rs.getTimestamp("sist_endret_dato")))
                    .opprettetDato(convert(rs.getTimestamp("opprettet_dato")))
                    .status(rs.getString("status"))
                    .gjennomfoering(rs.getString("gjennomfoering"));
        }
    }
}
