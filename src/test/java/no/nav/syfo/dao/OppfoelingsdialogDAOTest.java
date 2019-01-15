package no.nav.syfo.dao;

import no.nav.syfo.domain.Godkjenning;
import no.nav.syfo.domain.GodkjentPlan;
import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.repository.dao.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OppfoelingsdialogDAOTest {

    @Mock
    private ArbeidsoppgaveDAO arbeidsoppgaveDAO;
    @Mock
    private TiltakDAO tiltakDAO;
    @Mock
    private GodkjenningerDAO godkjenningerDAO;
    @Mock
    private GodkjentplanDAO godkjentplanDAO;

    @InjectMocks
    private OppfoelingsdialogDAO oppfoelingsdialogDAO;

    @Test
    public void populateMedGodkjenningerIngenGodkjent() {
        List<Godkjenning> godkjenningListe = singletonList(new Godkjenning());
        when(godkjenningerDAO.godkjenningerByOppfoelgingsdialogId(anyLong())).thenReturn(godkjenningListe);
        when(godkjentplanDAO.godkjentPlanByOppfoelgingsdialogId(anyLong())).thenReturn(empty());

        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.populate(new Oppfoelgingsdialog().id(1L));

        verify(godkjenningerDAO, never()).deleteAllByOppfoelgingsdialogId(anyLong());
        assertThat(oppfoelgingsdialog.godkjenninger).isSameAs(godkjenningListe).isNotEmpty();
        assertThat(oppfoelgingsdialog.godkjentPlan).isEmpty();
    }

    @Test
    public void populateIngenGodkjenningerMedGodkjent() {
        List<Godkjenning> godkjenningListe = emptyList();
        when(godkjenningerDAO.godkjenningerByOppfoelgingsdialogId(anyLong())).thenReturn(godkjenningListe);
        when(godkjentplanDAO.godkjentPlanByOppfoelgingsdialogId(anyLong())).thenReturn(of(new GodkjentPlan()));

        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.populate(new Oppfoelgingsdialog().id(1L));

        verify(godkjenningerDAO, never()).deleteAllByOppfoelgingsdialogId(anyLong());
        assertThat(oppfoelgingsdialog.godkjenninger).isSameAs(godkjenningListe).isEmpty();
        assertThat(oppfoelgingsdialog.godkjentPlan).isNotEmpty();
    }

    @Test
    public void populateMedGodkjenningerMedGodkjent() {
        List<Godkjenning> godkjenningListe = singletonList(new Godkjenning());
        when(godkjenningerDAO.godkjenningerByOppfoelgingsdialogId(anyLong())).thenReturn(godkjenningListe);
        when(godkjentplanDAO.godkjentPlanByOppfoelgingsdialogId(anyLong())).thenReturn(of(new GodkjentPlan()));

        Oppfoelgingsdialog oppfoelgingsdialog = oppfoelingsdialogDAO.populate(new Oppfoelgingsdialog().id(1L));

        verify(godkjenningerDAO).deleteAllByOppfoelgingsdialogId(anyLong());
        assertThat(oppfoelgingsdialog.godkjenninger).isNotSameAs(godkjenningListe).isEmpty();
        assertThat(oppfoelgingsdialog.godkjentPlan).isNotEmpty();
    }
}
