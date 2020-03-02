package no.nav.syfo.service;

import no.nav.syfo.aktorregister.AktorregisterConsumer;
import no.nav.syfo.pdl.PdlConsumer;
import no.nav.syfo.pdl.exceptions.NameFromPDLIsNull;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Optional;

@Service
public class BrukerprofilService {

    private AktorregisterConsumer aktorregisterConsumer;
    private final PdlConsumer pdlConsumer;

    @Inject
    public BrukerprofilService(
            AktorregisterConsumer aktorregisterConsumer,
            PdlConsumer pdlConsumer
    ) {
        this.aktorregisterConsumer = aktorregisterConsumer;
        this.pdlConsumer = pdlConsumer;
    }

    public String hentNavnByAktoerId(String aktoerId) {
        if (!aktoerId.matches("\\d{13}$")) {
            throw new RuntimeException();
        }
        String fnr = aktorregisterConsumer.hentFnrForAktor(aktoerId);
        return Optional.ofNullable(pdlConsumer.personName(fnr)).orElseThrow(() -> new NameFromPDLIsNull("Name of leader was null"));
    }
}
