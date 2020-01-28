package no.nav.syfo.testhelper;

import no.nav.syfo.model.Naermesteleder;

import static no.nav.syfo.testhelper.UserConstants.LEDER_AKTORID;

public class NarmesteLederGenerator {

    private final Naermesteleder naermesteleder = new Naermesteleder()
            .naermesteLederAktoerId(LEDER_AKTORID);

    public Naermesteleder generateNarmesteLeder() {
        return naermesteleder;
    }
}
