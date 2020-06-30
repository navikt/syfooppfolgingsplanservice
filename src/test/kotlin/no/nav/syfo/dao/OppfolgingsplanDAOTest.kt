package no.nav.syfo.dao

import no.nav.syfo.domain.*
import no.nav.syfo.repository.dao.*
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class OppfolgingsplanDAOTest {
    @Mock
    private lateinit var arbeidsoppgaveDAO: ArbeidsoppgaveDAO

    @Mock
    private lateinit var tiltakDAO: TiltakDAO

    @Mock
    private lateinit var godkjenningerDAO: GodkjenningerDAO

    @Mock
    private lateinit var godkjentplanDAO: GodkjentplanDAO

    @InjectMocks
    private lateinit var oppfolgingsplanDAO: OppfolgingsplanDAO

    @Test
    fun populateMedGodkjenningerIngenGodkjent() {
        val godkjenningListe = listOf(Godkjenning())
        Mockito.`when`(godkjenningerDAO.godkjenningerByOppfoelgingsdialogId(ArgumentMatchers.anyLong())).thenReturn(godkjenningListe)
        Mockito.`when`(godkjentplanDAO.godkjentPlanByOppfolgingsplanId(ArgumentMatchers.anyLong())).thenReturn(Optional.empty())
        val oppfolgingsplan = oppfolgingsplanDAO.populate(Oppfolgingsplan().id(1L))
        Mockito.verify(godkjenningerDAO, Mockito.never()).deleteAllByOppfoelgingsdialogId(ArgumentMatchers.anyLong())
        Assertions.assertThat(oppfolgingsplan.godkjenninger).isSameAs(godkjenningListe).isNotEmpty
        Assertions.assertThat(oppfolgingsplan.godkjentPlan).isEmpty
    }

    @Test
    fun populateIngenGodkjenningerMedGodkjent() {
        val godkjenningListe: List<Godkjenning> = emptyList()
        Mockito.`when`(godkjenningerDAO.godkjenningerByOppfoelgingsdialogId(ArgumentMatchers.anyLong())).thenReturn(godkjenningListe)
        Mockito.`when`(godkjentplanDAO.godkjentPlanByOppfolgingsplanId(ArgumentMatchers.anyLong())).thenReturn(Optional.of(GodkjentPlan()))
        val oppfolgingsplan = oppfolgingsplanDAO.populate(Oppfolgingsplan().id(1L))
        Mockito.verify(godkjenningerDAO, Mockito.never()).deleteAllByOppfoelgingsdialogId(ArgumentMatchers.anyLong())
        Assertions.assertThat(oppfolgingsplan.godkjenninger).isSameAs(godkjenningListe).isEmpty()
        Assertions.assertThat(oppfolgingsplan.godkjentPlan).isNotEmpty
    }

    @Test
    fun populateMedGodkjenningerMedGodkjent() {
        val godkjenningListe = listOf(Godkjenning())
        Mockito.`when`(godkjenningerDAO.godkjenningerByOppfoelgingsdialogId(ArgumentMatchers.anyLong())).thenReturn(godkjenningListe)
        Mockito.`when`(godkjentplanDAO.godkjentPlanByOppfolgingsplanId(ArgumentMatchers.anyLong())).thenReturn(Optional.of(GodkjentPlan()))
        val oppfolgingsplan = oppfolgingsplanDAO.populate(Oppfolgingsplan().id(1L))
        Mockito.verify(godkjenningerDAO).deleteAllByOppfoelgingsdialogId(ArgumentMatchers.anyLong())
        Assertions.assertThat(oppfolgingsplan.godkjenninger).isNotSameAs(godkjenningListe).isEmpty()
        Assertions.assertThat(oppfolgingsplan.godkjentPlan).isNotEmpty
    }
}
