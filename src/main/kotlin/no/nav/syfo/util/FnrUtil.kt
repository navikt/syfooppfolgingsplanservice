package no.nav.syfo.util

object FnrUtil {
    fun fodtEtterDagIMaaned(fnr: String, grensedag: Int) : Boolean {
        val fodselsdatoDag = fnr.replace("\\s".toRegex(), "").substring(0, 2).toInt()
        return fodselsdatoDag > grensedag
    }
}
