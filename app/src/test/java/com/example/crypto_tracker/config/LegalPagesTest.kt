package com.example.crypto_tracker.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.net.URI

/** Keeps `AppConfig.Legal` and the static pages in `docs/` (served by GitHub Pages) in sync. */
class LegalPagesTest {

    private val docs = listOf(File("../docs"), File("docs")).first { it.isDirectory }

    /** Path inside `docs/`: GitHub Pages serves a project site under `/<repo>/`, so the first segment is dropped. */
    private fun pathOf(url: String) = "/" + URI(url).path.trimStart('/').substringAfter('/')

    @Test
    fun legalUrls_areHttpsWithTrailingSlash() {
        listOf(AppConfig.Legal.PRIVACY_POLICY_URL, AppConfig.Legal.TERMS_OF_USE_URL).forEach {
            assertTrue(it, it.startsWith("https://") && it.endsWith("/"))
        }
    }

    @Test
    fun legalUrls_mapToExistingPages() {
        assertTrue(File(docs, pathOf(AppConfig.Legal.PRIVACY_POLICY_URL) + "index.html").isFile)
        assertTrue(File(docs, pathOf(AppConfig.Legal.TERMS_OF_USE_URL) + "index.html").isFile)
    }

    @Test
    fun pages_existInEnglishAndPortugueseAndLinkToEachOther() {
        for ((prefix, lang, up) in listOf(Triple("", "en", "../"), Triple("pt-br/", "pt-BR", "../../"))) {
            for (dir in listOf("privacy-policy", "terms-of-use")) {
                val html = File(docs, "$prefix$dir/index.html").readText()
                assertTrue(html.startsWith("<!doctype html>"))
                assertTrue(html.contains("<html lang=\"$lang\">"))
                assertTrue(html.contains("href=\"${up}${prefix}privacy-policy/\""))
                assertTrue(html.contains("href=\"${up}${prefix}terms-of-use/\""))
            }
        }
    }

    @Test
    fun relativeLinks_resolveToExistingFiles() {
        val href = Regex("""(?:href|src)="([^"#:]+)"""")
        fun resolve(page: File, link: String) =
            if (link.startsWith("/")) File(docs, link.drop(1)) else File(page.parentFile, link)
        docs.walkTopDown().filter { it.extension == "html" }.forEach { page ->
            href.findAll(page.readText()).forEach { m ->
                val target = resolve(page, m.groupValues[1]).normalize()
                val resolved = if (target.isDirectory) File(target, "index.html") else target
                assertEquals("${page.name} -> ${m.groupValues[1]}", true, resolved.isFile)
            }
        }
    }
}
