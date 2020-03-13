package no.nav.syfo.dokarkiv;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@JsonSerialize
public class Bruker {
    public String id;
    public String idType;
}
