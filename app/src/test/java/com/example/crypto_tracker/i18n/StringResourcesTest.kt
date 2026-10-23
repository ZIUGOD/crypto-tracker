package com.example.crypto_tracker.i18n

import com.example.crypto_tracker.data.preferences.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guards the localization setup: every [AppLanguage] needs a complete `strings.xml` with the same
 * keys and placeholders as the English default (which is also the runtime fallback).
 * Runs against the source files, so a forgotten translation fails the build instead of the UI.
 */
class StringResourcesTest {

    private val resDir = File("src/main/res")

    private class Entry(val name: String, val translatable: Boolean, val text: String)

    private fun parse(file: File): Map<String, Entry> {
        val pattern = Regex("""<string\s+name="([^"]+)"([^>]*)>(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
        return pattern.findAll(file.readText()).associate { match ->
            val name = match.groupValues[1]
            name to Entry(name, !match.groupValues[2].contains("translatable=\"false\""), match.groupValues[3])
        }
    }

    /** "pt-BR" -> "values-pt-rBR", "en" -> "values-en". */
    private fun folderFor(language: AppLanguage): String {
        val parts = language.tag.split("-")
        return if (parts.size == 2) "values-${parts[0]}-r${parts[1]}" else "values-${parts[0]}"
    }

    private fun placeholders(text: String): List<String> =
        Regex("""%\d+\$[sd]""").findAll(text).map { it.value }.sorted().toList()

    private val translatedLanguages = AppLanguage.entries.filter { it != AppLanguage.Default }

    @Test
    fun englishIsTheDefaultLanguage() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.Default)
        assertEquals(AppLanguage.Default, AppLanguage.fromTag(null))
        assertEquals(AppLanguage.Default, AppLanguage.fromTag("xx"))
    }

    @Test
    fun everyLanguageIsFoundByItsTag() {
        AppLanguage.entries.forEach { assertEquals(it, AppLanguage.fromTag(it.tag)) }
    }

    @Test
    fun everyLanguageHasAStringsFile() {
        translatedLanguages.forEach { language ->
            assertTrue("Missing ${folderFor(language)}/strings.xml", File(resDir, "${folderFor(language)}/strings.xml").exists())
        }
    }

    @Test
    fun translationsHaveExactlyTheDefaultKeys() {
        val default = parse(File(resDir, "values/strings.xml")).filterValues { it.translatable }.keys

        translatedLanguages.forEach { language ->
            val keys = parse(File(resDir, "${folderFor(language)}/strings.xml")).keys
            assertEquals("Missing in ${language.tag}", emptySet<String>(), default - keys)
            assertEquals("Not in the default file (${language.tag})", emptySet<String>(), keys - default)
        }
    }

    @Test
    fun translationsKeepTheSamePlaceholders() {
        val default = parse(File(resDir, "values/strings.xml"))

        translatedLanguages.forEach { language ->
            parse(File(resDir, "${folderFor(language)}/strings.xml")).forEach { (name, entry) ->
                assertEquals(
                    "Placeholders of '$name' differ in ${language.tag}",
                    placeholders(default.getValue(name).text),
                    placeholders(entry.text)
                )
            }
        }
    }
}
