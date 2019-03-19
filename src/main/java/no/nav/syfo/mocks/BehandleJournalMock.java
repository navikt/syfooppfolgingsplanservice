package no.nav.syfo.mocks;

import no.nav.tjeneste.virksomhet.behandlejournal.v2.*;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import static no.nav.syfo.config.ws.wsconfig.BehandleJournalConfig.MOCK_KEY;

@Service
@ConditionalOnProperty(value = MOCK_KEY, havingValue = "true")
public class BehandleJournalMock implements BehandleJournalV2 {
    @Override
    public WSArkiverUstrukturertKravResponse arkiverUstrukturertKrav(WSArkiverUstrukturertKravRequest wsArkiverUstrukturertKravRequest) {
        return null;
    }

    @Override
    public WSJournalfoerUtgaaendeHenvendelseResponse journalfoerUtgaaendeHenvendelse(WSJournalfoerUtgaaendeHenvendelseRequest wsJournalfoerUtgaaendeHenvendelseRequest) {
        return null;
    }

    @Override
    public void ferdigstillDokumentopplasting(WSFerdigstillDokumentopplastingRequest wsFerdigstillDokumentopplastingRequest) throws FerdigstillDokumentopplastingFerdigstillDokumentopplastingjournalpostIkkeFunnet {

    }

    @Override
    public WSJournalfoerNotatResponse journalfoerNotat(WSJournalfoerNotatRequest wsJournalfoerNotatRequest) {
        return null;
    }

    @Override
    public WSLagreVedleggPaaJournalpostResponse lagreVedleggPaaJournalpost(WSLagreVedleggPaaJournalpostRequest wsLagreVedleggPaaJournalpostRequest) throws LagreVedleggPaaJournalpostLagreVedleggPaaJournalpostjournalpostIkkeFunnet {
        return null;
    }

    @Override
    public void ping() {

    }

    @Override
    public WSJournalfoerInngaaendeHenvendelseResponse journalfoerInngaaendeHenvendelse(WSJournalfoerInngaaendeHenvendelseRequest wsJournalfoerInngaaendeHenvendelseRequest) {
        return null;
    }
}
