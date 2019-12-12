package no.nav.syfo.azuread;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;

@Data
@Accessors(fluent = true)
class AzureAdResponse {
    public String access_token;
    public String token_type;
    public String expires_in;
    public String ext_expires_in;
    public Instant expires_on;
    public String not_before;
    public String resource;
}
