package no.nav.syfo.aareg;

import java.math.BigDecimal;

import static java.math.RoundingMode.HALF_UP;

public class AaregUtils {
    public static BigDecimal stillingsprosentWithMaxScale(double percent) {
        BigDecimal percentAsBigDecimal = new BigDecimal(percent);

        if (percentAsBigDecimal.scale() > 1) {
            return percentAsBigDecimal.setScale(1, HALF_UP);
        }

        return percentAsBigDecimal;
    }
}
