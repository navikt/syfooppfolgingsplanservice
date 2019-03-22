package no.nav.syfo.repository.dao;

import no.nav.syfo.repository.domain.Dokument;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.sql.*;

@Repository
public class DokumentDAO {

    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Inject
    public DokumentDAO(
            JdbcTemplate jdbcTemplate,
            NamedParameterJdbcTemplate namedParameterJdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public byte[] hent(String uuid) {
        return jdbcTemplate.queryForObject("select * from dokument where dokument_uuid = ?", new DokumentRowMapper(), uuid).pdf;
    }

    public void lagre(Dokument dokument) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("dokument_uuid", dokument.uuid)
                .addValue("pdf", new SqlLobValue(dokument.pdf), Types.BLOB)
                .addValue("xml", new SqlLobValue(dokument.xml), Types.CLOB);

        namedParameterJdbcTemplate.update("insert into dokument (dokument_uuid, pdf, xml) values(:dokument_uuid, :pdf, :xml)", namedParameters);
    }

    private class DokumentRowMapper implements RowMapper<Dokument> {
        public Dokument mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Dokument()
                    .xml(rs.getString("xml"))
                    .uuid(rs.getString("dokument_uuid"))
                    .pdf(rs.getBytes("pdf"));
        }
    }
}
