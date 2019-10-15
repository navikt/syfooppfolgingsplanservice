package no.nav.syfo.util;

import no.nav.melding.virksomhet.servicemeldingmedkontaktinformasjon.v1.servicemeldingmedkontaktinformasjon.WSServicemeldingMedKontaktinformasjon;
import no.nav.melding.virksomhet.varsel.v1.varsel.XMLVarsel;
import no.nav.melding.virksomhet.varsel.v1.varsel.XMLVarslingstyper;
import no.nav.syfo.pdf.domain.OppfoelgingsdialogXML;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

import static java.lang.Boolean.TRUE;
import static javax.xml.bind.JAXBContext.newInstance;
import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import static javax.xml.bind.Marshaller.JAXB_FRAGMENT;

public class JAXB {

    public static final JAXBContext VARSEL_CONTEXT;
    public static final JAXBContext TREDJEPARTS_SERVICEMELDING_CONTEXT;
    private static final JAXBContext OPPFOELGINGSDIALOG_CONTEXT;

    static {
        try {
            VARSEL_CONTEXT = newInstance(
                    XMLVarsel.class,
                    XMLVarslingstyper.class
            );
            OPPFOELGINGSDIALOG_CONTEXT = newInstance(
                    OppfoelgingsdialogXML.class
            );
            TREDJEPARTS_SERVICEMELDING_CONTEXT = newInstance(
                    WSServicemeldingMedKontaktinformasjon.class
            );
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public static String marshallVarsel(Object element) {
        try {
            StringWriter writer = new StringWriter();
            Marshaller marshaller = VARSEL_CONTEXT.createMarshaller();
            marshaller.setProperty(JAXB_FORMATTED_OUTPUT, TRUE);
            marshaller.setProperty(JAXB_FRAGMENT, true);
            marshaller.marshal(element, new StreamResult(writer));
            return writer.toString();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public static String marshallDialog(Object element) {
        try {
            StringWriter writer = new StringWriter();
            Marshaller marshaller = OPPFOELGINGSDIALOG_CONTEXT.createMarshaller();
            marshaller.setProperty(JAXB_FORMATTED_OUTPUT, TRUE);
            marshaller.setProperty(JAXB_FRAGMENT, true);
            marshaller.marshal(element, new StreamResult(writer));
            return writer.toString();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public static String marshallTredjepartsServiceMelding(Object element) {
        try {
            StringWriter writer = new StringWriter();
            Marshaller marshaller = TREDJEPARTS_SERVICEMELDING_CONTEXT.createMarshaller();
            marshaller.setProperty(JAXB_FORMATTED_OUTPUT, TRUE);
            marshaller.setProperty(JAXB_FRAGMENT, true);
            marshaller.marshal(element, new StreamResult(writer));
            return writer.toString();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

}
