package no.nav.syfo.dokarkiv;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(fluent = true)
@JsonSerialize
public class JournalpostRequest {
    public AvsenderMottaker avsenderMottaker;
    public String tittel;
    public Bruker bruker;
    public List<Dokument> dokumenter;
    public Integer journalfoerendeEnhet;
    public String journalpostType;
    public String kanal;
    public Sak sak;
    public String tema;
    public String eksternReferanseId;
}

