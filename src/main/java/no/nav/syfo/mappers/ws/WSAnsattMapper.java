package no.nav.syfo.mappers.ws;

import no.nav.syfo.model.Ansatt;
import no.nav.tjeneste.virksomhet.sykefravaersoppfoelging.v1.informasjon.WSAnsatt;

import java.util.function.Function;

public class WSAnsattMapper {

    public static Function<WSAnsatt, Ansatt> ws2ansatt = wsAnsatt -> new Ansatt()
            .virksomhetsnummer(wsAnsatt.getOrgnummer())
            .aktoerId(wsAnsatt.getAktoerId());
}
