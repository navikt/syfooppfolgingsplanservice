package no.nav.syfo.repository;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.slf4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.Optional.*;
import static org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4;
import static org.slf4j.LoggerFactory.getLogger;

public class DbUtil {
    private static final Logger LOG = getLogger(DbUtil.class);

    public static <T> Optional<T> queryOptional(JdbcTemplate jdbcTemplate, String sql, RowMapper<T> rowMapper, Object... args) {
        try {
            return of(jdbcTemplate.queryForObject(sql, rowMapper, args));
        } catch (EmptyResultDataAccessException e) {
            return empty();
        } catch (Exception e) {
            LOG.error("Feil i queryOptional", e);
            throw e;
        }
    }

    public static long nesteSekvensverdi(String sekvensnavn, JdbcTemplate jdbcTemplate) {
        return jdbcTemplate.queryForObject("select " + sekvensnavn + ".nextval from dual", (rs, rowNum) -> rs.getLong(1));
    }

    public static Timestamp convert(LocalDate timestamp) {
        return ofNullable(timestamp).map(LocalDate::atStartOfDay).map(Timestamp::valueOf).orElse(null);
    }

    public static Timestamp convert(LocalDateTime timestamp) {
        return ofNullable(timestamp).map(Timestamp::valueOf).orElse(null);
    }

    public static LocalDateTime convert(Timestamp timestamp) {
        return ofNullable(timestamp).map(Timestamp::toLocalDateTime).orElse(null);
    }

    private static PolicyFactory sanitizer = new HtmlPolicyBuilder().toFactory();

    public static String sanitizeUserInput(String userinput) {
        String sanitizedInput = unescapeHtml4(sanitizer.sanitize(unescapeHtml4(userinput)));
        if (!sanitizedInput.equals(userinput) && userinput != null) {
            LOG.warn("Dette er ikke en feil, men burde vært stoppet av regexen i frontend. Finn ut hvorfor og evt. oppdater regex. \n" +
                    "Det ble strippet vekk innhold slik at denne teksten: {} \n" +
                    "ble til denne teksten: {}", userinput, sanitizedInput);
        }
        return sanitizedInput;
    }

}
