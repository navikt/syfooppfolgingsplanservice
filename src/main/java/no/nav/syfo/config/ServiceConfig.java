package no.nav.syfo.config;

import no.nav.syfo.scheduler.ProsesserInnkomnePlaner;
import no.nav.syfo.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class ServiceConfig {

    @Bean
    public OppfoelgingsdialogService oppfoelgingsdialogService() {
        return new OppfoelgingsdialogService();
    }

    @Bean
    public ArbeidsforholdService arbeidsforholdService() {
        return new ArbeidsforholdService();
    }

    @Bean
    public NaermesteLederService naermesteLederService() {
        return new NaermesteLederService();
    }

    @Bean
    public AktoerService aktoerService() {
        return new AktoerService();
    }

    @Bean
    public ArbeidsfordelingService arbeidsfordelingService() {
        return new ArbeidsfordelingService();
    }

    @Bean
    public EgenAnsattService egenAnsattService() {
        return new EgenAnsattService();
    }

    @Bean
    public NorgService norgService() {
        return new NorgService();
    }

    @Bean
    public PersonService personService() {
        return new PersonService();
    }

    @Bean
    public TilgangskontrollService tilgangskontrollService() {
        return new TilgangskontrollService();
    }

    @Bean
    public DkifService dkifService() {
        return new DkifService();
    }

    @Bean
    public PdfService pdfService() {
        return new PdfService();
    }

    @Bean
    public BrukerprofilService brukerprofilService() {
        return new BrukerprofilService();
    }

    @Bean
    public OrganisasjonService organisasjonService() {
        return new OrganisasjonService();
    }

    @Bean
    public GodkjenningService godkjenningService() {
        return new GodkjenningService();
    }

    @Bean
    public DokumentService dokumentService() {
        return new DokumentService();
    }

    @Bean
    public SamtykkeService samtykkeService() {
        return new SamtykkeService();
    }

    @Bean
    public TiltakService tiltakService() {
        return new TiltakService();
    }

    @Bean
    public ArbeidsoppgaveService arbeidsoppgaveService() {
        return new ArbeidsoppgaveService();
    }

    @Bean
    public KommentarService kommentarService() {
        return new KommentarService();
    }

    @Bean
    public JournalService journalService() {
        return new JournalService();
    }

    @Bean
    public BehandleSakService behandleSakService() {
        return new BehandleSakService();
    }

    @Bean
    public ProsesserInnkomnePlaner opprettSakOgJournalpostJobb() {
        return new ProsesserInnkomnePlaner();
    }

    @Bean
    public SakService sakService() {
        return new SakService();
    }

    @Bean
    public SakService sakServiceTest() {
        return new SakService();
    }

    @Bean
    public VeilederOppgaverService veilederOppgaverService() {
        return new VeilederOppgaverService();
    }

    @Bean
    public JuridiskLoggService juridiskLoggService() {
        return new JuridiskLoggService();
    }

    @Bean
    public SykeforloepService sykeforloepService() {
        return new SykeforloepService();
    }

    @Bean
    public VeilederBehandlingService veilederBehandlingService() {
        return new VeilederBehandlingService();
    }

}

