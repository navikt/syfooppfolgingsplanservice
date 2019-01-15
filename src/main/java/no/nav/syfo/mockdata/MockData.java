package no.nav.syfo.mockdata;

import no.nav.syfo.api.intern.domain.*;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.Response.ok;
import static org.apache.commons.io.IOUtils.toByteArray;

public class MockData {
    public static List<RSOppfoelgingsdialog> mockedOppfoelgingsdialoger() {
        return asList(
                new RSOppfoelgingsdialog()
                        .id(1L)
                        .virksomhet(new RSVirksomhet()
                                .virksomhetsnummer("981566378")
                        )
                        .oppgaver(asList(
                                new RSVeilederOppgave()
                                        .id(2L)
                        ))
                        .godkjentPlan(new RSGodkjentPlan()
                                .deltMedNAV(true)
                                .deltMedNAVTidspunkt(LocalDateTime.now())
                                .deltMedFastlege(true)
                                .deltMedFastlegeTidspunkt(LocalDateTime.now())
                                .dokumentUuid("1")
                                .gyldighetstidspunkt(new RSGyldighetstidspunkt()
                                        .fom(LocalDate.now().plusDays(1))
                                        .tom(LocalDate.now().plusWeeks(5))))
        );
    }

    public static Response mockPdf() {
        byte[] dokument;
        try {
            dokument = toByteArray(MockData.class.getResourceAsStream("/mock.pdf"));
        } catch (IOException e) {
            throw new RuntimeException();
        }
        return ok()
                .type("application/pdf")
                .entity(dokument)
                .build();
    }

    public static byte[] mockPdfBytes() {
        try {
            return toByteArray(MockData.class.getResourceAsStream("/mock.pdf"));
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
}
