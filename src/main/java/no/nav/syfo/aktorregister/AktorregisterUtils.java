package no.nav.syfo.aktorregister;

import no.nav.syfo.aktorregister.exceptions.*;
import java.util.Map;

public class AktorregisterUtils {
    public static String currentIdentFromAktorregisterResponse(Map<String, IdentinfoListe> response, String desiredUsersIdent, String desiredIdentGroup) {
        IdentinfoListe identinfoListe = response.get(desiredUsersIdent);
        throwExceptionIfErrorOrNoUser(identinfoListe);

        Identinfo currentIdentinfo = identinfoListe.identer.stream()
                .filter(identinfo -> identinfo.gjeldende && desiredIdentGroup.equals(identinfo.identgruppe))
                .findAny()
                .orElse(null);

        if (currentIdentinfo == null || currentIdentinfo.ident.isEmpty()) {
            throw new NoCurrentIdentForAktor("Tried getting ident for aktor");
        }
        return currentIdentinfo.ident;
    }

    private static void throwExceptionIfErrorOrNoUser(IdentinfoListe identinfoListe) {
        if (identinfoListe == null) {
            throw new NoResponseForDesiredUser("Tried getting info about user from aktorregisteret. Tremendous FAIL!");
        }
        if (identinfoListe.feilmelding != null) {
            throw new AktorGotError(identinfoListe.feilmelding);
        }
    }
}
