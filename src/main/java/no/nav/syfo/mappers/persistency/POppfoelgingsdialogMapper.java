package no.nav.syfo.mappers.persistency;

import no.nav.syfo.domain.*;
import no.nav.syfo.repository.domain.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static no.nav.syfo.util.MapUtil.map;

public class POppfoelgingsdialogMapper {


    public static Function<PArbeidsoppgave, Gjennomfoering> p2gjennomfoering = pArbeidsoppgave ->
            new Gjennomfoering()
                    .gjennomfoeringStatus(pArbeidsoppgave.gjennomfoeringStatus)
                    .kanIkkeBeskrivelse(pArbeidsoppgave.kanIkkeBeskrivelse)
                    .kanBeskrivelse(pArbeidsoppgave.kanBeskrivelse)
                    .medMerTid(pArbeidsoppgave.medMerTid)
                    .paaAnnetSted(pArbeidsoppgave.paaAnnetSted)
                    .medHjelp(pArbeidsoppgave.medHjelp);


    public static Function<PArbeidsoppgave, Arbeidsoppgave> p2arbeidsoppgave = pArbeidsoppgave ->
            new Arbeidsoppgave()
                    .id(pArbeidsoppgave.id)
                    .oppfoelgingsdialogId(pArbeidsoppgave.oppfoelgingsdialogId)
                    .navn(pArbeidsoppgave.navn)
                    .erVurdertAvSykmeldt(pArbeidsoppgave.erVurdertAvSykmeldt)
                    .sistEndretAvAktoerId(pArbeidsoppgave.sistEndretAvAktoerId)
                    .sistEndretDato(pArbeidsoppgave.sistEndretDato)
                    .opprettetAvAktoerId(pArbeidsoppgave.opprettetAvAktoerId)
                    .opprettetDato(pArbeidsoppgave.opprettetDato)
                    .gjennomfoering(map(pArbeidsoppgave, p2gjennomfoering));

    public static Function<PGodkjenning, Godkjenning> p2godkjenning = pGodkjenning ->
            new Godkjenning()
                    .id(pGodkjenning.id)
                    .oppfoelgingsdialogId(pGodkjenning.oppfoelgingsdialogId)
                    .godkjentAvAktoerId(pGodkjenning.aktoerId)
                    .godkjent(pGodkjenning.godkjent)
                    .beskrivelse(pGodkjenning.beskrivelse)
                    .delMedNav(pGodkjenning.delMedNav)
                    .gyldighetstidspunkt(new Gyldighetstidspunkt()
                            .fom(ofNullable(pGodkjenning.fom).map(LocalDateTime::toLocalDate).orElse(null))
                            .tom(ofNullable(pGodkjenning.tom).map(LocalDateTime::toLocalDate).orElse(null))
                            .evalueres(ofNullable(pGodkjenning.evalueres).map(LocalDateTime::toLocalDate).orElse(null))
                    )
                    .godkjenningsTidspunkt(pGodkjenning.created);

    public static Function<POppfoelgingsdialog, Oppfolgingsplan> p2oppfoelgingsdialog = pOppfoelgingsdialog ->
            new Oppfolgingsplan()
                    .id(pOppfoelgingsdialog.id)
                    .uuid(pOppfoelgingsdialog.uuid)
                    .arbeidstaker(new Person()
                            .aktoerId(pOppfoelgingsdialog.aktoerId)
                            .fnr(pOppfoelgingsdialog.smFnr)
                            .sisteEndring(pOppfoelgingsdialog.sistEndretSykmeldt)
                            .sistAksessert(pOppfoelgingsdialog.sistAksessertSykmeldt)
                            .sistInnlogget(pOppfoelgingsdialog.sisteInnloggingSykmeldt)
                            .samtykke(pOppfoelgingsdialog.samtykkeSykmeldt)
                    )
                    .arbeidsgiver(new Person()
                            .sistAksessert(pOppfoelgingsdialog.sistAksessertArbeidsgiver)
                            .sisteEndring(pOppfoelgingsdialog.sistEndretArbeidsgiver)
                            .sistInnlogget(pOppfoelgingsdialog.sisteInnloggingArbeidsgiver)
                            .samtykke(pOppfoelgingsdialog.samtykkeArbeidsgiver)
                    )
                    .sistEndretAvAktoerId(pOppfoelgingsdialog.sistEndretAv)
                    .sistEndretDato(pOppfoelgingsdialog.sistEndret)
                    .opprettetAvAktoerId(pOppfoelgingsdialog.opprettetAv)
                    .opprettetAvFnr(pOppfoelgingsdialog.opprettetAvFnr)
                    .opprettet(pOppfoelgingsdialog.created)
                    .virksomhet(new Virksomhet()
                            .virksomhetsnummer(pOppfoelgingsdialog.virksomhetsnummer)
                    );


    private static Optional<Avbruttplan> avbruttplan(PGodkjentPlan pGodkjentPlan) {
        if (pGodkjentPlan.avbruttTidspunkt == null) {
            return Optional.empty();
        }
        return of(new Avbruttplan()
                .oppfoelgingsdialogId(pGodkjentPlan.oppfoelgingsdialogId)
                .tidspunkt(pGodkjentPlan.avbruttTidspunkt)
                .avAktoerId(pGodkjentPlan.avbruttAv)
        );
    }

    public static Function<PGodkjentPlan, GodkjentPlan> p2godkjentplan = pGodkjentPlan ->
            new GodkjentPlan()
                    .id(pGodkjentPlan.id)
                    .oppfoelgingsdialogId(pGodkjentPlan.oppfoelgingsdialogId)
                    .dokumentUuid(pGodkjentPlan.dokumentUuid)
                    .opprettetTidspunkt(pGodkjentPlan.created)
                    .gyldighetstidspunkt(new Gyldighetstidspunkt()
                            .fom(pGodkjentPlan.fom.toLocalDate())
                            .tom(pGodkjentPlan.tom.toLocalDate())
                            .evalueres(pGodkjentPlan.evalueres.toLocalDate())
                    )
                    .tvungenGodkjenning(pGodkjentPlan.tvungenGodkjenning)
                    .deltMedNAVTidspunkt(pGodkjentPlan.deltMedNavTidspunkt)
                    .deltMedNAV(pGodkjentPlan.deltMedNav)
                    .deltMedFastlege(pGodkjentPlan.deltMedFastlege)
                    .deltMedFastlegeTidspunkt(pGodkjentPlan.deltMedFastlegeTidspunkt)
                    .sakId(pGodkjentPlan.sakId)
                    .journalpostId(pGodkjentPlan.journalpostId)
                    .tildeltEnhet(pGodkjentPlan.tildeltEnhet)
                    .avbruttPlan(avbruttplan(pGodkjentPlan));

    public static Function<PVeilederBehandling, VeilederBehandling> p2veilederbehandling = pVeilederBehandling ->
            new VeilederBehandling()
                    .oppgaveId(pVeilederBehandling.oppgaveId())
                    .oppgaveUUID(pVeilederBehandling.oppgaveUUID())
                    .godkjentplanId(pVeilederBehandling.godkjentplanId())
                    .tildeltEnhet(pVeilederBehandling.tildeltEnhet())
                    .tildeltIdent(pVeilederBehandling.tildeltIdent())
                    .opprettetDato(pVeilederBehandling.opprettetDato())
                    .sistEndret(pVeilederBehandling.sistEndret())
                    .sistEndretAv(pVeilederBehandling.sistEndretAv())
                    .status(VeilederBehandlingStatus.valueOf(pVeilederBehandling.status()));
}
