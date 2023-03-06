package no.nav.syfo.model;

import lombok.Data;
import lombok.experimental.Accessors;
import java.io.Serializable;

@Data
@Accessors(fluent = true)
public class Naermesteleder implements Serializable {
    public String naermesteLederId;
    public String ansattFnr;
    public String naermesteLederFnr;
    public String orgnummer;
    public NaermesteLederStatus naermesteLederStatus;
    public String navn;
    public String mobil;
    public String epost;
}
