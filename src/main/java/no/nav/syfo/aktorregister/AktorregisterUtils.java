package no.nav.syfo.aktorregister;

import no.nav.syfo.aktorregister.exceptions.*;

public class AktorregisterUtils {
    public static String currentIdentFromAktorregisterResponse(AktorregisterResponse response, String desiredUsersIdent, String desiredIdentGroup) {
        IdentinfoForAktoer identinfoForAktoer = response.aktorMap.get(desiredUsersIdent);
        throwExceptionIfErrorOrNoUser(identinfoForAktoer);

        Identinfo currentIdentinfo = identinfoForAktoer.identer.stream()
                .filter(identinfo -> identinfo.gjeldende && desiredIdentGroup.equals(identinfo.identGruppe))
                .findAny()
                .orElse(null);

        if (currentIdentinfo == null || currentIdentinfo.ident.isEmpty()) {
            throw new NoCurrentIdentForAktor("Tried getting ident for aktor");
        }
        return currentIdentinfo.ident;
    }

    private static void throwExceptionIfErrorOrNoUser(IdentinfoForAktoer identinfoForAktoer) {
        if (identinfoForAktoer == null) {
            throw new NoResponseForDesiredUser("Tried getting info about user from aktorregisteret. Tremendous FAIL!");
        }
        if (identinfoForAktoer.feilmelding != null) {
            throw new AktorGotError(identinfoForAktoer.feilmelding);
        }
    }
}
