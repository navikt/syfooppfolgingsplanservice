package no.nav.syfo.api.selvbetjening.controller;

import no.nav.syfo.api.intern.ressurs.AbstractRessursTilgangTest;
import no.nav.syfo.api.selvbetjening.domain.RSArbeidsoppgave;
import no.nav.syfo.api.selvbetjening.domain.RSGjennomfoering;
import no.nav.syfo.domain.Arbeidsoppgave;
import no.nav.syfo.service.ArbeidsoppgaveService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.inject.Inject;

import static no.nav.syfo.api.selvbetjening.mapper.RSArbeidsoppgaveMapper.rs2arbeidsoppgave;
import static no.nav.syfo.domain.Gjennomfoering.KanGjennomfoeres.KAN;
import static no.nav.syfo.domain.Gjennomfoering.KanGjennomfoeres.TILRETTELEGGING;
import static no.nav.syfo.testhelper.OidcTestHelper.loggInnBruker;
import static no.nav.syfo.testhelper.OidcTestHelper.loggUtAlle;
import static no.nav.syfo.testhelper.UserConstants.ARBEIDSTAKER_FNR;
import static no.nav.syfo.util.MapUtil.map;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OppfolgingsplanControllerTest extends AbstractRessursTilgangTest {

    @Inject
    private OppfolgingsplanController oppfolgingsplanController;

    @MockBean
    ArbeidsoppgaveService arbeidsoppgaveService;

    private static Long oppfolgingsplanId = 1L;

    @Before
    public void setup() {
        loggInnBruker(oidcRequestContextHolder, ARBEIDSTAKER_FNR);
    }

    @Test
    public void lagrer_ny_arbeidsoppgave_som_bruker() {
        Long ressursId = 1L;
        RSArbeidsoppgave rsArbeidsoppgave = new RSArbeidsoppgave()
                .arbeidsoppgavenavn("Arbeidsoppgavenavn")
                .gjennomfoering(new RSGjennomfoering()
                        .kanGjennomfoeres(TILRETTELEGGING.name())
                        .medHjelp(true)
                        .medMerTid(true)
                        .paaAnnetSted(true)
                );

        Arbeidsoppgave arbeidsoppgave = map(rsArbeidsoppgave, rs2arbeidsoppgave);

        when(arbeidsoppgaveService.lagreArbeidsoppgave(oppfolgingsplanId, arbeidsoppgave, ARBEIDSTAKER_FNR)).thenReturn(ressursId);

        Long res = oppfolgingsplanController.lagreArbeidsoppgave(oppfolgingsplanId, rsArbeidsoppgave);

        verify(arbeidsoppgaveService).lagreArbeidsoppgave(eq(oppfolgingsplanId), any(Arbeidsoppgave.class), eq(ARBEIDSTAKER_FNR));

        assertEquals(res, ressursId);
    }

    @Test
    public void lagrer_eksiterende_arbeidsoppgave_som_bruker() {
        Long arbeidsoppgaveId = 2L;
        RSArbeidsoppgave rsArbeidsoppgave = new RSArbeidsoppgave()
                .arbeidsoppgaveId(arbeidsoppgaveId)
                .arbeidsoppgavenavn("Arbeidsoppgavenavn")
                .gjennomfoering(new RSGjennomfoering()
                        .kanGjennomfoeres(KAN.name())
                        .kanBeskrivelse("Denne kan gjennomfoeres")
                );

        Arbeidsoppgave arbeidsoppgave = map(rsArbeidsoppgave, rs2arbeidsoppgave);

        when(arbeidsoppgaveService.lagreArbeidsoppgave(oppfolgingsplanId, arbeidsoppgave, ARBEIDSTAKER_FNR)).thenReturn(arbeidsoppgaveId);

        Long res = oppfolgingsplanController.lagreArbeidsoppgave(oppfolgingsplanId, rsArbeidsoppgave);

        verify(arbeidsoppgaveService).lagreArbeidsoppgave(eq(oppfolgingsplanId), any(Arbeidsoppgave.class), eq(ARBEIDSTAKER_FNR));

        assertEquals(res, arbeidsoppgaveId);
    }

    @Test(expected = RuntimeException.class)
    public void finner_ikke_innlogget_bruker_lagre_arbeidsoppgave() {
        loggUtAlle(oidcRequestContextHolder);
        RSArbeidsoppgave rsArbeidsoppgave = new RSArbeidsoppgave();

        oppfolgingsplanController.lagreArbeidsoppgave(oppfolgingsplanId, rsArbeidsoppgave);
    }
}
