package no.nav.syfo.repository.dao;

import no.nav.syfo.domain.AsynkOppgave;
import no.nav.syfo.repository.domain.PAsynkoppgave;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static no.nav.syfo.mappers.persistency.PAsynkoppgaveMapper.p2asynkoppgave;
import static no.nav.syfo.repository.DbUtil.convert;
import static no.nav.syfo.repository.DbUtil.nesteSekvensverdi;
import static no.nav.syfo.util.MapUtil.mapListe;

@Transactional
public class AsynkOppgaveDAO {

    private static final double FEM_MINUTTER = 5.0 / (24.0 * 60.0);

    @Inject
    private JdbcTemplate jdbcTemplate;
    @Inject
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public AsynkOppgave create(AsynkOppgave asynkOppgave) {
        long id = nesteSekvensverdi("ASYNK_OPPGAVE_ID_SEQ", jdbcTemplate);

        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("asynk_oppgave_id", id)
                .addValue("opprettet_tidspunkt", convert(now()))
                .addValue("oppgavetype", asynkOppgave.oppgavetype)
                .addValue("avhengig_av", asynkOppgave.avhengigAv)
                .addValue("antall_forsoek", asynkOppgave.antallForsoek)
                .addValue("ressurs_id", asynkOppgave.ressursId);

        namedParameterJdbcTemplate.update("" +
                "INSERT INTO ASYNK_OPPGAVE (" +
                "asynk_oppgave_id," +
                "opprettet_tidspunkt," +
                "oppgavetype," +
                "avhengig_av," +
                "antall_forsoek," +
                "ressurs_id)" +
                "VALUES(" +
                ":asynk_oppgave_id," +
                ":opprettet_tidspunkt," +
                ":oppgavetype," +
                ":avhengig_av," +
                ":antall_forsoek," +
                ":ressurs_id)", namedParameters);

        return asynkOppgave.id(id);
    }

    public AsynkOppgave update(AsynkOppgave asynkOppgave) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("asynk_oppgave_id", asynkOppgave.id)
                .addValue("oppgavetype", asynkOppgave.oppgavetype)
                .addValue("avhengig_av", asynkOppgave.avhengigAv)
                .addValue("antall_forsoek", asynkOppgave.antallForsoek)
                .addValue("ressurs_id", asynkOppgave.ressursId);

        namedParameterJdbcTemplate.update("" +
                "UPDATE ASYNK_OPPGAVE " +
                "SET oppgavetype = :oppgavetype," +
                "avhengig_av = :avhengig_av," +
                "antall_forsoek = :antall_forsoek," +
                "ressurs_id = :ressurs_id " +
                "WHERE asynk_oppgave_id = :asynk_oppgave_id", namedParameters);

        return asynkOppgave;
    }

    public void delete(Long id) {
        jdbcTemplate.update("" +
                "DELETE " +
                "FROM ASYNK_OPPGAVE " +
                "WHERE asynk_oppgave_id = ?", id);
    }

    public List<AsynkOppgave> finnOppgaver() {
        return mapListe(jdbcTemplate.query("" +
                        "SELECT *" +
                        "FROM ASYNK_OPPGAVE"
                , new AsynkoppgaveRowMapper()), p2asynkoppgave);
    }

    public Optional<AsynkOppgave> finnFoersteOppgaveUtenAvhengighet() {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("femMinutter", FEM_MINUTTER);
        String query = "" +
                "SELECT * FROM ASYNK_OPPGAVE a " +
                "WHERE (a.avhengig_av IS NULL OR a.avhengig_av NOT IN (SELECT ao.ASYNK_OPPGAVE_ID FROM ASYNK_OPPGAVE ao WHERE a.avhengig_av = ao.ASYNK_OPPGAVE_ID))" +
                "AND SYSDATE > (a.opprettet_tidspunkt + (:femMinutter * a.antall_forsoek))" +
                "ORDER BY a.antall_forsoek ASC";
        return mapListe(namedParameterJdbcTemplate.query(query, namedParameters, new AsynkoppgaveRowMapper()), p2asynkoppgave).stream().findFirst();
    }

    private class AsynkoppgaveRowMapper implements RowMapper<PAsynkoppgave> {
        public PAsynkoppgave mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new PAsynkoppgave()
                    .id(rs.getLong("asynk_oppgave_id"))
                    .opprettetTidspunkt(convert(rs.getTimestamp("opprettet_tidspunkt")))
                    .oppgavetype(rs.getString("oppgavetype"))
                    .avhengigAv(rs.getLong("avhengig_av"))
                    .antallForsoek(rs.getInt("antall_forsoek"))
                    .ressursId(rs.getString("ressurs_id"));
        }
    }
}
