package no.nav.syfo.sts;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
public class StsToken {
    public String access_token;
    public String token_type;
    public int expires_in;

    LocalDateTime expirationTime;

    public static void setExpirationTime(StsToken token) {
        if (token == null) {
            return;
        }
        token.expirationTime = LocalDateTime.now().plusSeconds(token.expires_in - 10L);
    }

    public static boolean shouldRenew(StsToken token) {
        if (token == null) {
            return true;
        }
        return isExpired(token);
    }

    private static boolean isExpired(StsToken token) {
        return token.expirationTime.isBefore(LocalDateTime.now());
    }
}
