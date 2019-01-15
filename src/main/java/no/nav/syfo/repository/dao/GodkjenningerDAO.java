package no.nav.syfo.repository.dao;

import no.nav.syfo.domain.Godkjenning;
import no.nav.syfo.repository.domain.PGodkjenning;
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
import static no.nav.syfo.util.MapUtil.mapListe;
import static no.nav.syfo.mappers.persistency.POppfoelgingsdialogMapper.p2godkjenning;
import static no.nav.syfo.repository.DbUtil.*;

public class GodkjenningerDAO {

    @Inject
    private JdbcTemplate jdbcTemplate;
    @Inject
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public List<Godkjenning> godkjenningerByOppfoelgingsdialogId(long oppfoelgingsdialogId) {
        return mapListe(jdbcTemplate.query("select * from godkjenning where oppfoelgingsdialog_id = ?", new GodkjenningerRowMapper(), oppfoelgingsdialogId), p2godkjenning);
    }

    public void deleteAllByOppfoelgingsdialogId(long oppfoelgingsdialogId) {
        jdbcTemplate.update("delete from godkjenning where oppfoelgingsdialog_id = ?", oppfoelgingsdialogId);
    }

    public void create(Godkjenning godkjenning) {
        long godkjenningId = nesteSekvensverdi("GODKJENNING_ID_SEQ", jdbcTemplate);
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("godkjenning_id", godkjenningId)
                .addValue("oppfoelgingsdialog_id", godkjenning.oppfoelgingsdialogId)
                .addValue("aktoer_id", godkjenning.godkjentAvAktoerId)
                .addValue("godkjent", godkjenning.godkjent)
                .addValue("beskrivelse", new SqlLobValue(sanitizeUserInput(godkjenning.beskrivelse)), Types.CLOB)
                .addValue("fom", convert(godkjenning.gyldighetstidspunkt.fom))
                .addValue("tom", convert(godkjenning.gyldighetstidspunkt.tom))
                .addValue("evalueres", convert(godkjenning.gyldighetstidspunkt.evalueres))
                .addValue("created", convert(now()));
        namedParameterJdbcTemplate.update("insert into godkjenning " +
                "(godkjenning_id, oppfoelgingsdialog_id, aktoer_id, godkjent, beskrivelse, fom, tom, evalueres, created) values" +
                "(:godkjenning_id, :oppfoelgingsdialog_id, :aktoer_id, :godkjent, :beskrivelse, :fom, :tom, :evalueres, :created)", namedParameters);
    }

    private class GodkjenningerRowMapper implements RowMapper<PGodkjenning> {
        public PGodkjenning mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new PGodkjenning()
                    .id(rs.getLong("godkjenning_id"))
                    .oppfoelgingsdialogId(rs.getLong("oppfoelgingsdialog_id"))
                    .aktoerId(rs.getString("aktoer_id"))
                    .godkjent(rs.getBoolean("godkjent"))
                    .beskrivelse(rs.getString("beskrivelse"))
                    .fom(convert(rs.getTimestamp("fom")))
                    .tom(convert(rs.getTimestamp("tom")))
                    .evalueres(convert(rs.getTimestamp("evalueres")))
                    .created(convert(rs.getTimestamp("created")));
        }
    }
}
