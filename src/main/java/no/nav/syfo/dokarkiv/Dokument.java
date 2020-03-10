package no.nav.syfo.dokarkiv;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(fluent = true)
@JsonSerialize
public class Dokument {
    public String brevkode;
    public String dokumentKategori;
    public List<Dokumentvariant> dokumentvarianter;
    public String tittel;
}
