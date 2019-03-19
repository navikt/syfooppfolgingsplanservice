package no.nav.syfo;

import no.nav.syfo.domain.*;
import no.nav.syfo.model.Ansatt;
import no.nav.syfo.repository.dao.GodkjentplanDAO;
import no.nav.syfo.repository.dao.OppfoelingsdialogDAO;
import no.nav.syfo.service.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static no.nav.syfo.util.OppfoelgingsdialogUtil.fjernEldsteGodkjenning;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OppfoelgingsdialogServiceTest {

    @Mock
    private OppfoelingsdialogDAO oppfoelingsdialogDAO;
    @Mock
    private NaermesteLederService naermesteLederService;
    @Mock
    private TilgangskontrollService tilgangskontrollService;
    @Mock
    private AktoerService aktoerService;
    @Mock
    private GodkjentplanDAO godkjentplanDAO;
    @Mock
    private BrukerprofilService brukerprofilService;
    @InjectMocks
    private OppfoelgingsdialogService oppfoelgingsdialogService;

    @Test
    public void oppfoelgingsdialogerFraAndreBedrifterBlirFiltrertBort() {
        Oppfoelgingsdialog dialog1 = new Oppfoelgingsdialog().id(1L).arbeidstaker(new Person().aktoerId("sykmeldt")).virksomhet(new Virksomhet().virksomhetsnummer("1"));
        Oppfoelgingsdialog dialog2 = new Oppfoelgingsdialog().id(2L).arbeidstaker(new Person().aktoerId("sykmeldt")).virksomhet(new Virksomhet().virksomhetsnummer("2"));
        when(naermesteLederService.hentAnsatte(anyString())).thenReturn(asList(new Ansatt().aktoerId("sykmeldt").virksomhetsnummer("1")));
        when(oppfoelingsdialogDAO.oppfoelgingsdialogerKnyttetTilSykmeldt(anyString())).thenReturn(asList(
                dialog1,
                dialog2
        ));
        when(oppfoelingsdialogDAO.populate(dialog1)).thenReturn(dialog1);
        List<Oppfoelgingsdialog> dialoger = oppfoelgingsdialogService.hentAktoersOppfoelgingsdialoger("aktoerId", "ARBEIDSGIVER", "123");
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
