package no.nav.syfo.util

import no.nav.syfo.repository.DbUtil
import org.apache.commons.lang3.StringEscapeUtils
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
class DbUtilTest {
    @Test
    fun test1() {
        val sanitize = DbUtil.sanitizeUserInput("<html />")
        Assertions.assertThat(sanitize).isEqualTo("")
    }

    @Test
    fun test2() {
        val sanitize = DbUtil.sanitizeUserInput("<p>test<p/>")
        Assertions.assertThat(sanitize).isEqualTo("test")
    }

    @Test
    fun test3() {
        val sanitize = DbUtil.sanitizeUserInput("<br />")
        Assertions.assertThat(sanitize).isEqualTo("")
    }

    @Test
    fun test4() {
        val sanitize = DbUtil.sanitizeUserInput("test <no er som skummelt> kommer test")
        Assertions.assertThat(sanitize).isEqualTo("test  kommer test")
    }

    @Test
    fun test5() {
        val sanitize = DbUtil.sanitizeUserInput("3 er > enn fire men mindre enn <")
        Assertions.assertThat(sanitize).isEqualTo("3 er > enn fire men mindre enn <")
    }

    @Test
    fun test6() {
        val sanitize = DbUtil.sanitizeUserInput("3 er < enn fire men mindre enn >")
        Assertions.assertThat(sanitize).isEqualTo("3 er < enn fire men mindre enn >")
    }

    @Test
    fun test7() {
        val sanitize = DbUtil.sanitizeUserInput("man får lov til masse tegn æøå!==?||''\"\"///\\``")
        Assertions.assertThat(sanitize).isEqualTo("man får lov til masse tegn æøå!==?||''\"\"///\\``")
    }

    @Test
    fun test8() {
        val sanitize = DbUtil.sanitizeUserInput("<div test />")
        Assertions.assertThat(sanitize).isEqualTo("")
    }

    @Test
    fun test9() {
        val komplekstekst = """"Dette er en ! #test 'for' å teste div. prosent % tegn & kanskje / eller´ men også \" og tegn som ; og : større enn >, men mindre enn < test@nav.no ^^\n"+
            "\n"+
            "\n"+
            "Og også linjeskift bør fungere fint (ja) eller §3.1 -_- ** \n"+
            "\n"+
            "???""""
        val sanitize = DbUtil.sanitizeUserInput(komplekstekst)
        Assertions.assertThat(sanitize).isEqualTo(komplekstekst)
    }

    @Test
    fun test10() {
        val sanitize = DbUtil.sanitizeUserInput(StringEscapeUtils.escapeHtml4("<script>test</script>"))
        Assertions.assertThat(sanitize).isEqualTo("")
    }
}
