package no.nav.syfo.mocks;

import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.*;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.meldinger.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static java.time.LocalDate.now;
import static java.util.Arrays.asList;

public class OppfolgingMock implements SykefravaersoppfoelgingV1 {


    @Override
    public WSHentNaermesteLederListeResponse hentNaermesteLederListe(WSHentNaermesteLederListeRequest request) {
        return new WSHentNaermesteLederListeResponse()
                .withNaermesteLederListe(
                        asList(
                                new WSNaermesteLeder()
                                        .withNavn("Are Arbeidsgiver")
                                        .withEpost("are@arbeisgiver.no")
                                        .withMobil("12345678")
                                        .withNaermesteLederAktoerId("1010101010100")
                                        .withNaermesteLederId(16L)
                                        .withNaermesteLederStatus(new WSNaermesteLederStatus()
                                                .withErAktiv(true)
                                                .withAktivFom(LocalDate.now().minusMonths(1))
                                        )
                                        .withOrgnummer("123456789")));
    }

    @Override
    public WSHentSykeforlopperiodeResponse hentSykeforlopperiode(WSHentSykeforlopperiodeRequest wsHentSykeforlopperiodeRequest) {
        return new WSHentSykeforlopperiodeResponse()
                .withSykeforlopperiodeListe(new WSSykeforlopperiode()
                        .withAktivitet("Aktivitet")
                        .withGrad(10)
                        .withFom(now().minusDays(2))
                        .withTom(now().plusDays(2)));
    }

    @Override
    public WSHentHendelseListeResponse hentHendelseListe(WSHentHendelseListeRequest wsHentHendelseListeRequest) {
        return new WSHentHendelseListeResponse()
                .withHendelseListe(new WSHendelse()
                        .withAktoerId("1010101010101")
                        .withId(1)
                        .withTidspunkt(LocalDateTime.now())
                        .withType("Type"));
    }

    @Override
    public WSHentNaermesteLedersHendelseListeResponse hentNaermesteLedersHendelseListe(WSHentNaermesteLedersHendelseListeRequest wsHentNaermesteLedersHendelseListeRequest) {
        return new WSHentNaermesteLedersHendelseListeResponse()
                .withHendelseListe(new WSHendelseNyNaermesteLeder()
                        .withAktoerId("1010101010100")
                        .withId(1)
                        .withTidspunkt(LocalDateTime.now())
                        .withType("Type"));
    }

    @Override
    public WSBerikNaermesteLedersAnsattBolkResponse berikNaermesteLedersAnsattBolk(WSBerikNaermesteLedersAnsattBolkRequest wsBerikNaermesteLedersAnsattBolkRequest) throws BerikNaermesteLedersAnsattBolkSikkerhetsbegrensning {
        return null;
    }

    @Override
    public void ping() {
    }

    @Override
    public WSHentNaermesteLederResponse hentNaermesteLeder(WSHentNaermesteLederRequest request) {
        return new WSHentNaermesteLederResponse()
                .withNaermesteLeder(
                        new WSNaermesteLeder()
                                .withNavn("Frode Bjelland")
                                .withOrgnummer("123456789")
                                .withNaermesteLederId(123)
                                .withNaermesteLederStatus(new WSNaermesteLederStatus()
                                        .withErAktiv(true)
                                        .withAktivFom(LocalDate.now().minusDays(22))
                                )
                                .withNaermesteLederAktoerId("1010101010100")
                                .withEpost("frode@bjelland.no")
                                .withMobil("12345678"));
    }

    @Override
    public WSHentNaermesteLedersAnsattListeResponse hentNaermesteLedersAnsattListe(WSHentNaermesteLedersAnsattListeRequest request) {
        return new WSHentNaermesteLedersAnsattListeResponse().withAnsattListe(Arrays.asList(
                new WSAnsatt()
                        .withNaermesteLederStatus(new WSNaermesteLederStatus().withAktivFom(now().minusDays(10)).withErAktiv(true))
                        .withAktoerId("1010101010101")
                        .withNaermesteLederId(345)
                        .withNavn("Test Testesen")
                        .withOrgnummer("123456789"),
                new WSAnsatt()
                        .withNaermesteLederStatus(new WSNaermesteLederStatus().withAktivTom(now().minusDays(10)).withAktivFom(now().minusDays(20)).withErAktiv(false))
                        .withAktoerId("1010101010110")
                        .withNaermesteLederId(234)
                        .withNavn("Test Testesen")
                        .withOrgnummer("123456781"),
                new WSAnsatt()
                        .withNaermesteLederStatus(new WSNaermesteLederStatus().withAktivFom(now().minusDays(10)).withErAktiv(true))
                        .withAktoerId("1010101010111")
                        .withNaermesteLederId(346)
                        .withNavn("Test Testesen")
                        .withOrgnummer("123456781")

        ));
    }
}
