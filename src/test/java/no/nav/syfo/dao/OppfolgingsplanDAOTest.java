package no.nav.syfo.dao;

import no.nav.syfo.domain.Godkjenning;
import no.nav.syfo.domain.GodkjentPlan;
import no.nav.syfo.domain.Oppfolgingsplan;
import no.nav.syfo.repository.dao.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OppfolgingsplanDAOTest {

    @Mock
    private ArbeidsoppgaveDAO arbeidsoppgaveDAO;
    @Mock
    private TiltakDAO tiltakDAO;
    @Mock
    private GodkjenningerDAO godkjenningerDAO;
    @Mock
    private GodkjentplanDAO godkjentplanDAO;

    @InjectMocks
    private OppfolgingsplanDAO oppfolgingsplanDAO;

    @Test
    public void populateMedGodkjenningerIngenGodkjent() {
        List<Godkjenning> godkjenningListe = singletonList(new Godkjenning());
        when(godkjenningerDAO.godkjenningerByOppfoelgingsdialogId(anyLong())).thenReturn(godkjenningListe);
        when(godkjentplanDAO.godkjentPlanByOppfolgingsplanId(anyLong())).thenReturn(empty());

        Oppfolgingsplan oppfolgingsplan = oppfolgingsplanDAO.populate(new Oppfolgingsplan().id(1L));

        verify(godkjenningerDAO, never()).deleteAllByOppfoelgingsdialogId(anyLong());
        assertThat(oppfolgingsplan.godkjenninger).isSameAs(godkjenningListe).isNotEmpty();
        assertThat(oppfolgingsplan.godkjentPlan).isEmpty();
    }

    @Test
    public void populateIngenGodkjenningerMedGodkjent() {
        List<Godkjenning> godkjenningListe = emptyList();
        when(godkjenningerDAO.godkjenningerByOppfoelgingsdialogId(anyLong())).thenReturn(godkjenningListe);
        when(godkjentplanDAO.godkjentPlanByOppfolgingsplanId(anyLong())).thenReturn(of(new GodkjentPlan()));

        Oppfolgingsplan oppfolgingsplan = oppfolgingsplanDAO.populate(new Oppfolgingsplan().id(1L));

        verify(godkjenningerDAO, never()).deleteAllByOppfoelgingsdialogId(anyLong());
        assertThat(oppfolgingsplan.godkjenninger).isSameAs(godkjenningListe).isEmpty();
        assertThat(oppfolgingsplan.godkjentPlan).isNotEmpty();
    }

    @Test
    public void populateMedGodkjenningerMedGodkjent() {
        List<Godkjenning> godkjenningListe = singletonList(new Godkjenning());
        when(godkjenningerDAO.godkjenningerByOppfoelgingsdialogId(anyLong())).thenReturn(godkjenningListe);
        when(godkjentplanDAO.godkjentPlanByOppfolgingsplanId(anyLong())).thenReturn(of(new GodkjentPlan()));

        Oppfolgingsplan oppfolgingsplan = oppfolgingsplanDAO.populate(new Oppfolgingsplan().id(1L));

        verify(godkjenningerDAO).deleteAllByOppfoelgingsdialogId(anyLong());
        assertThat(oppfolgingsplan.godkjenninger).isNotSameAs(godkjenningListe).isEmpty();
        assertThat(oppfolgingsplan.godkjentPlan).isNotEmpty();
    }
}
