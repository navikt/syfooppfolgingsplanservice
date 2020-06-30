package no.nav.syfo.oppgave;

import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.domain.*;
import no.nav.syfo.model.Ansatt;
import no.nav.syfo.narmesteleder.NarmesteLederConsumer;
import no.nav.syfo.repository.dao.GodkjentplanDAO;
import no.nav.syfo.repository.dao.OppfolgingsplanDAO;
import no.nav.syfo.service.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static no.nav.syfo.api.selvbetjening.domain.BrukerkontekstConstant.ARBEIDSGIVER;
import static no.nav.syfo.testhelper.UserConstants.LEDER_FNR;
import static no.nav.syfo.util.OppfoelgingsdialogUtil.fjernEldsteGodkjenning;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OppfolgingsplanServiceTest {

    @Mock
    private OppfolgingsplanDAO oppfolgingsplanDAO;
    @Mock
    private NarmesteLederConsumer narmesteLederConsumer;
    @Mock
    private TilgangskontrollService tilgangskontrollService;
    @Mock
    private AktorregisterConsumer aktorregisterConsumer;
    @Mock
    private GodkjentplanDAO godkjentplanDAO;
    @Mock
    private BrukerprofilService brukerprofilService;
    @InjectMocks
    private OppfolgingsplanService oppfolgingsplanService;

    @Test
    public void oppfolgingsplanerFraAndreBedrifterBlirFiltrertBort() {
        Oppfolgingsplan dialog1 = new Oppfolgingsplan().id(1L).arbeidstaker(new Person().aktoerId("sykmeldt")).virksomhet(new Virksomhet().virksomhetsnummer("1"));
        Oppfolgingsplan dialog2 = new Oppfolgingsplan().id(2L).arbeidstaker(new Person().aktoerId("sykmeldt")).virksomhet(new Virksomhet().virksomhetsnummer("2"));
        when(aktorregisterConsumer.hentAktorIdForFnr("123")).thenReturn(LEDER_FNR);
        when(narmesteLederConsumer.ansatte(anyString())).thenReturn(asList(new Ansatt().aktoerId("sykmeldt").virksomhetsnummer("1")));
        when(oppfolgingsplanDAO.oppfolgingsplanerKnyttetTilSykmeldt(anyString())).thenReturn(asList(
                dialog1,
                dialog2
        ));
        when(oppfolgingsplanDAO.populate(dialog1)).thenReturn(dialog1);
        List<Oppfolgingsplan> dialoger = oppfolgingsplanService.hentAktorsOppfolgingsplaner(ARBEIDSGIVER, "123");
        assertThat(dialoger.size()).isEqualTo(1);
        assertThat(dialoger.get(0).id).isEqualTo(1L);
    }


    @Test
    public void fjernerDenEneGodkjenningen() {
        List<Godkjenning> godkjenninger = fjernEldsteGodkjenning(asList(
                new Godkjenning().godkjenningsTidspunkt(now())
        ));
        assertThat(godkjenninger.size()).isEqualTo(0);
    }

    @Test
    public void fjernerEldsteGodkjenning() {
        List<Godkjenning> godkjenninger = fjernEldsteGodkjenning(asList(
                new Godkjenning().godkjentAvAktoerId("1").godkjenningsTidspunkt(now()),
                new Godkjenning().godkjentAvAktoerId("2").godkjenningsTidspunkt(now().minusDays(1))
        ));
        assertThat(godkjenninger.size()).isEqualTo(1);
        assertThat(godkjenninger.get(0).godkjentAvAktoerId).isEqualTo("1");

    }
}
