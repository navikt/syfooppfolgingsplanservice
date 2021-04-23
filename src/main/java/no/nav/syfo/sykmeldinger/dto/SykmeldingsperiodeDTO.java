package no.nav.syfo.sykmeldinger.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.Data;
import lombok.experimental.Accessors;
import org.joda.time.LocalDate;

@Data
@Accessors(fluent = true)
public class SykmeldingsperiodeDTO {
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    public LocalDate fom;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    public LocalDate tom;
}
