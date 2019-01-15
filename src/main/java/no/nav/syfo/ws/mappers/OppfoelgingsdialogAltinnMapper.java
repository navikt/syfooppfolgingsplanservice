package no.nav.syfo.ws.mappers;

import no.altinn.schemas.services.serviceengine.correspondence._2010._10.AttachmentsV2;
import no.altinn.schemas.services.serviceengine.correspondence._2010._10.ExternalContentV2;
import no.altinn.schemas.services.serviceengine.correspondence._2010._10.InsertCorrespondenceV2;
import no.altinn.services.serviceengine.reporteeelementlist._2010._10.BinaryAttachmentExternalBEV2List;
import no.altinn.services.serviceengine.reporteeelementlist._2010._10.BinaryAttachmentV2;
import no.nav.syfo.domain.Gyldighetstidspunkt;
import no.nav.syfo.domain.Oppfoelgingsdialog;
import no.nav.syfo.domain.OppfoelgingsdialogAltinn;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.time.format.DateTimeFormatter;

import static no.altinn.schemas.services.serviceengine.correspondence._2010._10.UserTypeRestriction.SHOW_TO_ALL;
import static no.altinn.schemas.services.serviceengine.subscription._2009._10.AttachmentFunctionType.UNSPECIFIED;
import static no.nav.syfo.util.MockUtil.getOrgnummerForSendingTilAltinn;

public final class OppfoelgingsdialogAltinnMapper {

    private static final String OPPFOELGINGSDIALOG_TJENESTEKODE = "5062"; // OBS! VIKTIG! Denne må ikke endres, da kan feil personer få tilgang til oppfoelgingsplan i Altinn!
    private static final String OPPFOELGINGSDIALOG_TJENESTEVERSJON = "1";
    private static final String NORSK_BOKMAL = "1044";

    public static InsertCorrespondenceV2 oppfoelgingsdialogTilCorrespondence(OppfoelgingsdialogAltinn oppfoelgingsdialogAltinn, String brukersNavn) {
        String namespace = "http://schemas.altinn.no/services/ServiceEngine/Correspondence/2010/10";
        String binaryNamespace = "http://www.altinn.no/services/ServiceEngine/ReporteeElementList/2010/10";

        return new InsertCorrespondenceV2()
                .withAllowForwarding(new JAXBElement<>(new QName(namespace, "AllowForwarding"), Boolean.class, false))
                .withReportee(new JAXBElement<>(new QName(namespace, "Reportee"), String.class, getOrgnummerForSendingTilAltinn(oppfoelgingsdialogAltinn.oppfoelgingsdialog.virksomhet.virksomhetsnummer)))
                .withMessageSender(new JAXBElement<>(new QName(namespace, "MessageSender"), String.class,
                        byggMessageSender(oppfoelgingsdialogAltinn.oppfoelgingsdialog, brukersNavn)))
                .withServiceCode(new JAXBElement<>(new QName(namespace, "ServiceCode"), String.class, OPPFOELGINGSDIALOG_TJENESTEKODE))
                .withServiceEdition(new JAXBElement<>(new QName(namespace, "ServiceEdition"), String.class, OPPFOELGINGSDIALOG_TJENESTEVERSJON))
                .withContent(
                        new JAXBElement<>(new QName(namespace, "Content"), ExternalContentV2.class, new ExternalContentV2()
                                .withLanguageCode(new JAXBElement<>(new QName(namespace, "LanguageCode"), String.class, NORSK_BOKMAL))
                                .withMessageTitle(new JAXBElement<>(
                                        new QName(namespace, "MessageTitle"),
                                        String.class,
                                        byggTittel(oppfoelgingsdialogAltinn.oppfoelgingsdialog, brukersNavn)))
                                .withCustomMessageData(null)
                                .withAttachments(
                                        new JAXBElement<>(new QName(namespace, "Attachments"), AttachmentsV2.class, new AttachmentsV2()
                                                .withBinaryAttachments(
                                                        new JAXBElement<>(new QName(namespace, "BinaryAttachments"), BinaryAttachmentExternalBEV2List.class,
                                                                new BinaryAttachmentExternalBEV2List()
                                                                        .withBinaryAttachmentV2(
                                                                                opprettBinaertVedlegg(binaryNamespace,
                                                                                        oppfoelgingsdialogAltinn.getOppfoelgingsdialogPDF(),
                                                                                        "oppfoelgingsdialog.pdf",
                                                                                        "oppfoelgingsdialog",
                                                                                        oppfoelgingsdialogAltinn.oppfoelgingsdialog.uuid + ".pdf")
                                                                        )
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .withArchiveReference(null);
    }

    private static BinaryAttachmentV2 opprettBinaertVedlegg(String binaryNamespace, byte[] fil, String filnavn, String navn, String sendersRef) {
        return new BinaryAttachmentV2()
                .withDestinationType(SHOW_TO_ALL)
                .withFileName(new JAXBElement<>(new QName(binaryNamespace, "FileName"), String.class, filnavn))
                .withName(new JAXBElement<>(new QName(binaryNamespace, "Name"), String.class, navn))
                .withFunctionType(UNSPECIFIED)
                .withEncrypted(false)
                .withSendersReference(new JAXBElement<>(new QName(binaryNamespace, "SendersReference"), String.class, sendersRef))
                .withData(new JAXBElement<>(new QName("http://www.altinn.no/services/ServiceEngine/ReporteeElementList/2010/10", "Data"), byte[].class, fil));
    }

    private static String byggTittel(Oppfoelgingsdialog oppfoelgingsdialog, String brukersNavn) {
        String fnr = oppfoelgingsdialog.arbeidstaker.fnr;
        return "Oppfølgingsplan - " + gyldighetstidspunktSomTekst(oppfoelgingsdialog.godkjentPlan.get().gyldighetstidspunkt) + " - " + brukersNavn + " (" + fnr + ")";
    }

    private static String byggMessageSender(Oppfoelgingsdialog oppfoelgingsdialog, String brukersNavn) {
        String fnr = oppfoelgingsdialog.arbeidstaker.fnr;
        return brukersNavn + " - " + fnr;
    }

    private static String gyldighetstidspunktSomTekst(Gyldighetstidspunkt gyldighetstidspunkt) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String fom = dateTimeFormatter.format(gyldighetstidspunkt.tom);
        String tom = dateTimeFormatter.format(gyldighetstidspunkt.fom);
        return fom + "-" + tom;
    }
}
