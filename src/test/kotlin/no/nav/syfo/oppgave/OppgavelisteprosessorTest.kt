package no.nav.syfo.oppgave

import no.nav.syfo.LocalApplication
import no.nav.syfo.domain.AsynkOppgave
import no.nav.syfo.metric.Metrikk
import no.nav.syfo.oppgave.exceptions.OppgaveFinnerIkkeElementException
import no.nav.syfo.repository.dao.AsynkOppgaveDAO
import no.nav.syfo.util.Toggle
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDateTime

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [LocalApplication::class])
@DirtiesContext
class OppgavelisteprosessorTest {
    @Mock
    private lateinit var oppgaveelementprosessor: Oppgaveelementprosessor

    @Mock
    private lateinit var asynkOppgaveDAO: AsynkOppgaveDAO

    @Mock
    private lateinit var oppgaveIterator: OppgaveIterator

    @Mock
    private lateinit var metrikk: Metrikk

    @Mock
    private lateinit var toggle: Toggle

    @InjectMocks
    private lateinit var oppgavelisteprosessor: Oppgavelisteprosessor

    @Before
    fun setup() {
        Mockito.`when`(toggle.erPreprod()).thenReturn(true)
    }

    @Test
    @Throws(Exception::class)
    fun runIngenOppgaver() {
        Mockito.`when`(asynkOppgaveDAO.finnOppgaver()).thenReturn(emptyList())
        oppgavelisteprosessor.run()
        Mockito.verify(oppgaveelementprosessor, Mockito.never()).runTransactional(ArgumentMatchers.any(AsynkOppgave::class.java))
        Mockito.verify(asynkOppgaveDAO, Mockito.never()).create(ArgumentMatchers.any(AsynkOppgave::class.java))
    }

    @Test
    @Throws(Exception::class)
    fun runEnOppgave() {
        Mockito.`when`(oppgaveIterator.hasNext()).thenReturn(true, false)
        Mockito.`when`(oppgaveIterator.next()).thenReturn(AsynkOppgave()
            .id(1L)
            .oppgavetype(Oppgavetype.OPPFOELGINGSDIALOG_SEND.name))
        oppgavelisteprosessor.run()
        Mockito.verify(oppgaveelementprosessor, Mockito.times(1)).runTransactional(ArgumentMatchers.any(AsynkOppgave::class.java))
        Mockito.verify(asynkOppgaveDAO, Mockito.never()).create(ArgumentMatchers.any(AsynkOppgave::class.java))
    }

    @Test
    @Throws(Exception::class)
    fun runEnOppgaveFeilIProsessering() {
        val oppgave = AsynkOppgave()
            .id(1L)
            .oppgavetype(Oppgavetype.OPPFOELGINGSDIALOG_SEND.name)
        Mockito.`when`(oppgaveIterator.hasNext()).thenReturn(true, false)
        Mockito.`when`(oppgaveIterator.next()).thenReturn(oppgave)
        Mockito.doThrow(OppgaveFinnerIkkeElementException::class.java).`when`(oppgaveelementprosessor).runTransactional(oppgave)
        oppgavelisteprosessor.run()
        Mockito.verify(oppgaveelementprosessor).runTransactional(ArgumentMatchers.any(AsynkOppgave::class.java))
        val captor = ArgumentCaptor.forClass(AsynkOppgave::class.java)
        Mockito.verify(asynkOppgaveDAO).update(captor.capture())
        Assertions.assertThat(captor.value.id).isEqualTo(1L)
        Assertions.assertThat(captor.value.antallForsoek).isEqualTo(1)
    }

    @Test
    @Throws(Exception::class)
    fun runToOppgaverFeilIProsesseringAv1() {
        val oppgave1 = AsynkOppgave()
            .id(1L)
            .oppgavetype(Oppgavetype.OPPFOELGINGSDIALOG_SEND.name)
        val oppgave2 = AsynkOppgave()
            .id(2L)
            .oppgavetype(Oppgavetype.OPPFOELGINGSDIALOG_SEND.name)
        Mockito.`when`(oppgaveIterator.hasNext()).thenReturn(true, true, false)
        Mockito.`when`(oppgaveIterator.next()).thenReturn(oppgave1, oppgave2)
        Mockito.doThrow(OppgaveFinnerIkkeElementException::class.java).`when`(oppgaveelementprosessor).runTransactional(oppgave1)
        oppgavelisteprosessor.run()
        Mockito.verify(oppgaveelementprosessor, Mockito.times(2)).runTransactional(ArgumentMatchers.any(AsynkOppgave::class.java))
        val captor = ArgumentCaptor.forClass(AsynkOppgave::class.java)
        Mockito.verify(asynkOppgaveDAO).update(captor.capture())
        Assertions.assertThat(captor.value.id).isEqualTo(1L)
        Assertions.assertThat(captor.value.antallForsoek).isEqualTo(1)
    }

    @Test
    fun utfoererMaksLimitOppgaverIEnIterasjon() {
        val oppgave1 = AsynkOppgave()
            .id(1L)
            .oppgavetype(Oppgavetype.OPPFOELGINGSDIALOG_SEND.name)
        Mockito.`when`(oppgaveIterator.hasNext()).thenReturn(true)
        Mockito.`when`(oppgaveIterator.next()).thenReturn(oppgave1)
        oppgavelisteprosessor.run()
        Mockito.verify(oppgaveelementprosessor, Mockito.times(100)).runTransactional(ArgumentMatchers.any(AsynkOppgave::class.java))
    }

    @Test
    fun sletterOppgaverSomHarFeilet100ganger() {
        val oppgave1 = AsynkOppgave()
            .id(1L)
            .oppgavetype(Oppgavetype.OPPFOELGINGSDIALOG_SEND.name)
            .antallForsoek(100)
            .opprettetTidspunkt(LocalDateTime.now().minusMinutes(500))
        Mockito.`when`(oppgaveIterator.hasNext()).thenReturn(true, false)
        Mockito.`when`(oppgaveIterator.next()).thenReturn(oppgave1)
        Mockito.doThrow(OppgaveFinnerIkkeElementException::class.java).`when`(oppgaveelementprosessor).runTransactional(oppgave1)
        oppgavelisteprosessor.run()
        Mockito.verify(asynkOppgaveDAO, Mockito.times(1)).delete(ArgumentMatchers.any())
    }

    @Test
    fun sletterIkkeOppgaverHvisViKjorerIProduksjon() {
        Mockito.`when`(toggle.erPreprod()).thenReturn(false)
        val oppgave1 = AsynkOppgave()
            .id(1L)
            .oppgavetype(Oppgavetype.OPPFOELGINGSDIALOG_SEND.name)
            .antallForsoek(100)
            .opprettetTidspunkt(LocalDateTime.now().minusMinutes(500))
        Mockito.`when`(oppgaveIterator.hasNext()).thenReturn(true, false)
        Mockito.`when`(oppgaveIterator.next()).thenReturn(oppgave1)
        Mockito.doThrow(OppgaveFinnerIkkeElementException::class.java).`when`(oppgaveelementprosessor).runTransactional(oppgave1)
        oppgavelisteprosessor.run()
        Mockito.verify(asynkOppgaveDAO, Mockito.never()).delete(ArgumentMatchers.any())
    }

    @Test
    fun sletterIkkeOppgaverSomHarFeiletMindreEnn100Ganger() {
        val oppgave1 = AsynkOppgave()
            .id(1L)
            .oppgavetype(Oppgavetype.OPPFOELGINGSDIALOG_SEND.name)
            .antallForsoek(10)
            .opprettetTidspunkt(LocalDateTime.now().minusMinutes(500))
        Mockito.`when`(oppgaveIterator.hasNext()).thenReturn(true, false)
        Mockito.`when`(oppgaveIterator.next()).thenReturn(oppgave1)
        Mockito.doThrow(OppgaveFinnerIkkeElementException::class.java).`when`(oppgaveelementprosessor).runTransactional(oppgave1)
        oppgavelisteprosessor.run()
        Mockito.verify(asynkOppgaveDAO, Mockito.never()).delete(ArgumentMatchers.any())
    }
}
