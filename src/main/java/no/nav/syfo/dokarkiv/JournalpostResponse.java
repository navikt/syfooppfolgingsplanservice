package no.nav.syfo.dokarkiv;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(fluent = true)
@JsonSerialize
public class JournalpostResponse {
    public List<DokumentInfo> dokumenter;
    public Integer journalpostId;
    public Boolean journalpostferdigstilt;
    public String journalstatus;
    public String melding;
}
