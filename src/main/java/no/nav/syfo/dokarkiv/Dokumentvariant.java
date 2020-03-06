package no.nav.syfo.dokarkiv;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@JsonSerialize
public class Dokumentvariant {
    public String filnavn;
    public String filtype;
    public byte[] fysiskDokument;
    public String variantformat;
}
