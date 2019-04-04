package no.nav.syfo.api.selvbetjening.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class RSArbeidsgiver {
    public RSNaermesteLeder naermesteLeder;
    public RSNaermesteLeder forrigeNaermesteLeder;
}
