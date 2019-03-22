package no.nav.syfo.repository.dao;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.repository.domain.POppfoelgingsdialog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static java.time.LocalDateTime.now;
import static java.util.Collections.emptyList;
import static no.nav.syfo.mappers.persistency.POppfoelgingsdialogMapper.p2oppfoelgingsdialog;
import static no.nav.syfo.repository.DbUtil.convert;
import static no.nav.syfo.repository.DbUtil.nesteSekvensverdi;
import static no.nav.syfo.util.MapUtil.map;
import static no.nav.syfo.util.MapUtil.mapListe;
import static no.nav.syfo.util.OppfoelgingsdialogUtil.erArbeidstakeren;

@Slf4j
@Repository
public class OppfoelingsdialogDAO {

    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private ArbeidsoppgaveDAO arbeidsoppgaveDAO;
    private GodkjenningerDAO godkjenningerDAO;
    private GodkjentplanDAO godkjentplanDAO;
    private TiltakDAO tiltakDAO;

    @Inject
    public OppfoelingsdialogDAO(
            JdbcTemplate jdbcTemplate,
            NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            ArbeidsoppgaveDAO arbeidsoppgaveDAO,
            GodkjenningerDAO godkjenningerDAO,
            GodkjentplanDAO godkjentplanDAO,
            TiltakDAO tiltakDAO
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.arbeidsoppgaveDAO = arbeidsoppgaveDAO;
        this.godkjenningerDAO = godkjenningerDAO;
        this.godkjentplanDAO = godkjentplanDAO;
        this.tiltakDAO = tiltakDAO;
    }

    public Oppfoelgingsdialog create(Oppfoelgingsdialog oppfoelgingsdialog) {
        long id = nesteSekvensverdi("OPPFOELGINGSDIALOG_ID_SEQ", jdbcTemplate);
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("oppfoelgingsdialog_id", id)
                .addValue("uuid", UUID.randomUUID().toString())
                .addValue("aktoer_id", oppfoelgingsdialog.arbeidstaker.aktoerId)
                .addValue("virksomhetsnummer", oppfoelgingsdialog.virksomhet.virksomhetsnummer)
                .addValue("opprettet_av", oppfoelgingsdialog.opprettetAvAktoerId)
                .addValue("created", convert(now()))
                .addValue("arbeidsgiver_sist_innlogget", convert(oppfoelgingsdialog.arbeidsgiver.sistInnlogget))
                .addValue("sykmeldt_sist_innlogget", convert(oppfoelgingsdialog.arbeidstaker.sistInnlogget))
                .addValue("sist_endret_av", oppfoelgingsdialog.sistEndretAvAktoerId)
                .addValue("sist_endret", convert(now()))
                .addValue("arbeidsgiver_sist_aksessert", convert(oppfoelgingsdialog.arbeidsgiver.sistAksessert))
                .addValue("sykmeldt_sist_aksessert", convert(oppfoelgingsdialog.arbeidstaker.sistInnlogget))
                .addValue("arbeidsgiver_sist_endret", convert(oppfoelgingsdialog.arbeidsgiver.sisteEndring))
                .addValue("sykmeldt_sist_endret", convert(oppfoelgingsdialog.arbeidstaker.sisteEndring))
                .addValue("samtykke_sykmeldt", null)
                .addValue("samtykke_arbeidsgiver", null);

        namedParameterJdbcTemplate.update("insert into oppfoelgingsdialog " +
                "(oppfoelgingsdialog_id, uuid, aktoer_id, virksomhetsnummer, opprettet_av, created, arbeidsgiver_sist_innlogget, " +
                "sykmeldt_sist_innlogget, sist_endret_av, sist_endret, arbeidsgiver_sist_aksessert, sykmeldt_sist_aksessert, " +
                "arbeidsgiver_sist_endret, sykmeldt_sist_endret, samtykke_sykmeldt, samtykke_arbeidsgiver) " +
                "values(:oppfoelgingsdialog_id, :uuid, :aktoer_id, :virksomhetsnummer, :opprettet_av, :created, :arbeidsgiver_sist_innlogget, " +
                ":sykmeldt_sist_innlogget, :sist_endret_av, :sist_endret, :arbeidsgiver_sist_aksessert, :sykmeldt_sist_aksessert, " +
                ":arbeidsgiver_sist_endret, :sykmeldt_sist_endret, :samtykke_sykmeldt, :samtykke_arbeidsgiver)", namedParameters);

        return oppfoelgingsdialog.id(id);
    }


    public Oppfoelgingsdialog oppfoelgingsdialogByTiltakId(Long tiltakId) {
        return map(jdbcTemplate.queryForObject("select * from oppfoelgingsdialog join tiltak " +
                "on tiltak.oppfoelgingsdialog_id = oppfoelgingsdialog.oppfoelgingsdialog_id " +
                "where tiltak_id = ?", new OppfoelgingsdialogRowMapper(), tiltakId), p2oppfoelgingsdialog);
    }

    public Oppfoelgingsdialog finnOppfoelgingsdialogMedId(Long oppfoelgingsdialogId) {
        return map(jdbcTemplate.queryForObject("select * from oppfoelgingsdialog where oppfoelgingsdialog_id = ?", new OppfoelgingsdialogRowMapper(), oppfoelgingsdialogId), p2oppfoelgingsdialog);
    }

    public List<Oppfoelgingsdialog> oppfoelgingsdialogerKnyttetTilSykmeldt(String aktoerId) {
        return mapListe(jdbcTemplate.query("select * from oppfoelgingsdialog where aktoer_id = ?", new OppfoelgingsdialogRowMapper(), aktoerId), p2oppfoelgingsdialog);
    }

    public List<Oppfoelgingsdialog> oppfoelgingsdialogerKnyttetTilSykmeldtogVirksomhet(String sykmeldtAktoerId, String virksomhetsnummer) {
        return mapListe(jdbcTemplate.query("select * from oppfoelgingsdialog where aktoer_id = ? and virksomhetsnummer = ?", new OppfoelgingsdialogRowMapper(), sykmeldtAktoerId, virksomhetsnummer), p2oppfoelgingsdialog);
    }

    public Oppfoelgingsdialog populate(Oppfoelgingsdialog oppfoelgingsdialog) {
        oppfoelgingsdialog.arbeidsoppgaveListe = arbeidsoppgaveDAO.arbeidsoppgaverByOppfoelgingsdialogId(oppfoelgingsdialog.id);
        oppfoelgingsdialog.tiltakListe = tiltakDAO.finnTiltakByOppfoelgingsdialogId(oppfoelgingsdialog.id);
        oppfoelgingsdialog.godkjenninger = godkjenningerDAO.godkjenningerByOppfoelgingsdialogId(oppfoelgingsdialog.id);
        oppfoelgingsdialog.godkjentPlan = godkjentplanDAO.godkjentPlanByOppfoelgingsdialogId(oppfoelgingsdialog.id);

        oppfoelgingsdialog.godkjentPlan
                .filter(godkjentPlan -> !oppfoelgingsdialog.godkjenninger.isEmpty())
                .ifPresent(godkjentPlan -> {
                    log.warn("Sletter godkjenning som finnes selv om oppfølgingsplanen allerede er godkjent");
                    godkjenningerDAO.deleteAllByOppfoelgingsdialogId(godkjentPlan.oppfoelgingsdialogId);
                    oppfoelgingsdialog.godkjenninger = emptyList();
                });

        return oppfoelgingsdialog;

    }

    public String aktorIdByOppfolgingsplanId(long oppfolgingsplanId) {
        return jdbcTemplate.queryForObject("SELECT aktoer_id FROM oppfoelgingsdialog WHERE oppfoelgingsdialog_id = ?", (rs, rowNum) -> rs.getString("aktoer_id"), oppfolgingsplanId);
    }

    public void sistEndretAv(Long oppfoelgingsdialogId, String innloggetAktoerId) {
        Oppfoelgingsdialog oppfoelgingsdialog = finnOppfoelgingsdialogMedId(oppfoelgingsdialogId);
        if (erArbeidstakeren(oppfoelgingsdialog, innloggetAktoerId)) {
            jdbcTemplate.update("update oppfoelgingsdialog set sykmeldt_sist_endret = ?, sist_endret = ?, sist_endret_av = ? where oppfoelgingsdialog_id = ?",
                    convert(now()), convert(now()), innloggetAktoerId, oppfoelgingsdialogId);
        } else {
            jdbcTemplate.update("update oppfoelgingsdialog set arbeidsgiver_sist_endret = ?, sist_endret = ?, sist_endret_av = ? where oppfoelgingsdialog_id = ?",
                    convert(now()), convert(now()), innloggetAktoerId, oppfoelgingsdialogId);
        }
    }

    public void lagreSamtykkeArbeidsgiver(long oppfoelgingsdialogId, boolean samtykke) {
        jdbcTemplate.update("update oppfoelgingsdialog set samtykke_arbeidsgiver = ? where oppfoelgingsdialog_id = ?", samtykke, oppfoelgingsdialogId);
    }

    public void lagreSamtykkeSykmeldt(long oppfoelgingsdialogId, boolean samtykke) {
        jdbcTemplate.update("update oppfoelgingsdialog set samtykke_sykmeldt = ? where oppfoelgingsdialog_id = ?", samtykke, oppfoelgingsdialogId);
    }

    public void oppdaterSistInnlogget(Oppfoelgingsdialog oppfoelgingsdialog, String innloggetAktoerId) {
        if (innloggetAktoerId.equals(oppfoelgingsdialog.arbeidstaker.aktoerId)) {
            jdbcTemplate.update("update oppfoelgingsdialog set sykmeldt_sist_innlogget = ? where oppfoelgingsdialog_id = ?", convert(now()), oppfoelgingsdialog.id);
        } else {
            jdbcTemplate.update("update oppfoelgingsdialog set arbeidsgiver_sist_innlogget = ? where oppfoelgingsdialog_id = ?", convert(now()), oppfoelgingsdialog.id);
        }
    }

    public void oppdaterSistAksessert(Oppfoelgingsdialog oppfoelgingsdialog, String innloggetAktoerId) {
        if (innloggetAktoerId.equals(oppfoelgingsdialog.arbeidstaker.aktoerId)) {
            jdbcTemplate.update("update oppfoelgingsdialog set sykmeldt_sist_aksessert = ? where oppfoelgingsdialog_id = ?", convert(now()), oppfoelgingsdialog.id);
        } else {
            jdbcTemplate.update("update oppfoelgingsdialog set arbeidsgiver_sist_aksessert = ? where oppfoelgingsdialog_id = ?", convert(now()), oppfoelgingsdialog.id);
        }
    }

    public void avbryt(long oppfoelgingsdialogId, String innloggetAktoerId) {
        jdbcTemplate.update("update godkjentplan set avbrutt_av = ?, avbrutt_tidspunkt = ? where oppfoelgingsdialog_id = ?",
                innloggetAktoerId, convert(now()), oppfoelgingsdialogId);
    }

    public void nullstillSamtykke(long oppfoelgingsdialogId) {
        jdbcTemplate.update("update oppfoelgingsdialog set samtykke_sykmeldt = ? where oppfoelgingsdialog_id = ?", null, oppfoelgingsdialogId);
        jdbcTemplate.update("update oppfoelgingsdialog set samtykke_arbeidsgiver = ? where oppfoelgingsdialog_id = ?", null, oppfoelgingsdialogId);
    }

    private class OppfoelgingsdialogRowMapper implements RowMapper<POppfoelgingsdialog> {
        public POppfoelgingsdialog mapRow(ResultSet rs, int rowNum) throws SQLException {
            Boolean samtykke_sykmeldt = rs.getBoolean("samtykke_sykmeldt");
            samtykke_sykmeldt = rs.wasNull() ? null : samtykke_sykmeldt;
            Boolean samtykke_arbeidsgiver = rs.getBoolean("samtykke_arbeidsgiver");
            samtykke_arbeidsgiver = rs.wasNull() ? null : samtykke_arbeidsgiver;
            return new POppfoelgingsdialog()
                    .id(rs.getLong("oppfoelgingsdialog_id"))
                    .uuid(rs.getString("uuid"))
                    .aktoerId(rs.getString("aktoer_id"))
                    .virksomhetsnummer(rs.getString("virksomhetsnummer"))
                    .opprettetAv(rs.getString("opprettet_av"))
                    .created(convert(rs.getTimestamp("created")))
                    .sisteInnloggingArbeidsgiver(convert(rs.getTimestamp("arbeidsgiver_sist_innlogget")))
                    .sisteInnloggingSykmeldt(convert(rs.getTimestamp("sykmeldt_sist_innlogget")))
                    .sistEndretAv(rs.getString("sist_endret_av"))
                    .sistEndret(convert(rs.getTimestamp("sist_endret")))
                    .sistAksessertArbeidsgiver(convert(rs.getTimestamp("arbeidsgiver_sist_aksessert")))
                    .sistAksessertSykmeldt(convert(rs.getTimestamp("sykmeldt_sist_aksessert")))
                    .sistEndretArbeidsgiver(convert(rs.getTimestamp("arbeidsgiver_sist_endret")))
                    .sistEndretSykmeldt(convert(rs.getTimestamp("sykmeldt_sist_endret")))
                    .samtykkeSykmeldt(samtykke_sykmeldt)
                    .samtykkeArbeidsgiver(samtykke_arbeidsgiver);
        }
    }
}
