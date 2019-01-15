package no.nav.syfo.mappers.ws;

import no.nav.syfo.model.NaermesteLederStatus;
import no.nav.syfo.model.Naermesteleder;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.informasjon.WSNaermesteLeder;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.informasjon.WSNaermesteLederStatus;

import java.util.function.Function;

import static no.nav.syfo.util.MapUtil.map;

public class WSNaermesteLederMapper {

    private static Function<WSNaermesteLederStatus, NaermesteLederStatus> ws2naermesteLederStatus = wsNaermesteLederStatus -> new NaermesteLederStatus()
            .erAktiv(wsNaermesteLederStatus.isErAktiv())
            .aktivFom(wsNaermesteLederStatus.getAktivFom())
            .aktivTom(wsNaermesteLederStatus.getAktivTom());

    public static Function<WSNaermesteLeder, Naermesteleder> ws2naermesteLeder = wsNaermesteLeder -> new Naermesteleder()
            .naermesteLederId(wsNaermesteLeder.getNaermesteLederId())
            .naermesteLederAktoerId(wsNaermesteLeder.getNaermesteLederAktoerId())
            .naermesteLederStatus(map(wsNaermesteLeder.getNaermesteLederStatus(), ws2naermesteLederStatus))
            .orgnummer(wsNaermesteLeder.getOrgnummer())
            .epost(wsNaermesteLeder.getEpost())
            .mobil(wsNaermesteLeder.getMobil())
            .navn(wsNaermesteLeder.getNavn());
}
