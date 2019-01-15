package no.nav.syfo.repository.dao;

import no.nav.syfo.domain.Kommentar;
import no.nav.syfo.repository.domain.PKommentar;
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
import static no.nav.syfo.util.MapUtil.map;
import static no.nav.syfo.util.MapUtil.mapListe;
import static no.nav.syfo.mappers.persistency.POppfoelgingsdialogMapper.p2kommentar;
import static no.nav.syfo.repository.DbUtil.*;

public class KommentarDAO {
    @Inject
    private JdbcTemplate jdbcTemplate;
    @Inject
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public Kommentar finnKommentar(long id) {
        return map(jdbcTemplate.queryForObject("SELECT * FROM kommentar WHERE kommentar_id = ?", new KommentarRowMapper(), id), p2kommentar);
    }

    public List<Kommentar> finnKommentarerByTiltakId(long tiltakId) {
        return mapListe(jdbcTemplate.query("SELECT * FROM kommentar WHERE tiltak_id = ?", new KommentarRowMapper(), tiltakId), p2kommentar);
    }

    public Kommentar create(Kommentar kommentar) {
        long id = nesteSekvensverdi("KOMMENTAR_ID_SEQ", jdbcTemplate);
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("kommentar_id", id)
                .addValue("tiltak_id", kommentar.tiltakId)
                .addValue("tekst", new SqlLobValue(sanitizeUserInput(kommentar.tekst)), Types.CLOB)
                .addValue("sist_endret_av", kommentar.sistEndretAvAktoerId)
                .addValue("sist_endret_dato", convert(now()))
                .addValue("opprettet_av", kommentar.opprettetAvAktoerId)
                .addValue("opprettet_dato", convert(now()));
        namedParameterJdbcTemplate.update("INSERT INTO kommentar (kommentar_id, tiltak_id, tekst, sist_endret_av, sist_endret_dato, opprettet_av, opprettet_dato) " +
                "VALUES(:kommentar_id, :tiltak_id, :tekst, :sist_endret_av, :sist_endret_dato, :opprettet_av, :opprettet_dato)", namedParameters);
        return kommentar.id(id);
    }

    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM kommentar WHERE kommentar_id = ?", id);
    }

    public Kommentar update(Kommentar kommentar) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("kommentar_id", kommentar.id)
                .addValue("tekst", new SqlLobValue(sanitizeUserInput(kommentar.tekst)), Types.CLOB)
                .addValue("sist_endret_av", kommentar.sistEndretAvAktoerId)
                .addValue("sist_endret_dato", convert(now()));
        namedParameterJdbcTemplate.update("UPDATE kommentar " +
                "SET tekst = :tekst, sist_endret_av = :sist_endret_av, sist_endret_dato = :sist_endret_dato " +
                "WHERE kommentar_id = :kommentar_id", namedParameters);
        return kommentar;
    }

    private class KommentarRowMapper implements RowMapper<PKommentar> {
        public PKommentar mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new PKommentar()
                    .id(rs.getLong("kommentar_id"))
                    .tiltakId(rs.getLong("tiltak_id"))
                    .tekst(rs.getString("tekst"))
                    .sistEndretAvAktoerId(rs.getString("sist_endret_av"))
                    .sistEndretDato(convert(rs.getTimestamp("sist_endret_dato")))
                    .opprettetAvAktoerId(rs.getString("opprettet_av"))
                    .opprettetDato(convert(rs.getTimestamp("opprettet_dato")));
        }
    }

}

