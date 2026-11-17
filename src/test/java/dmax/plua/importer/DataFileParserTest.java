package dmax.plua.importer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import dmax.plua.domain.Language;

public class DataFileParserTest {

    // version

    @Test
    public void version_valid() {
        assertEquals(1, DataFileParser.parseVersion("version|1"));
        assertEquals(42, DataFileParser.parseVersion("version|42"));
    }

    @Test
    public void version_nullEmptyAndInvalid() {
        assertEquals(-1, DataFileParser.parseVersion(null));
        assertEquals(-1, DataFileParser.parseVersion(""));
        assertEquals(-1, DataFileParser.parseVersion("version|abc"));
        assertEquals(-1, DataFileParser.parseVersion("version"));
        assertEquals(-1, DataFileParser.parseVersion("foo|1"));
        assertEquals(-1, DataFileParser.parseVersion("version|1|2"));
        assertEquals(-1, DataFileParser.parseVersion("version|99999999999"));
    }

    // languages

    @Test
    public void languages_valid() {
        assertArrayEquals(new Language[] { Language.UKRAINIAN, Language.POLISH },
                DataFileParser.parseLanguages("UKRAINIAN|POLISH"));
    }

    @Test
    public void languages_invalid() {
        assertNull(DataFileParser.parseLanguages(null));
        assertNull(DataFileParser.parseLanguages("UKRAINIAN"));
        assertNull(DataFileParser.parseLanguages("UKRAINIAN|POLISH|POLISH"));
        assertNull(DataFileParser.parseLanguages("UKRAINIAN|FRENCH"));
        assertNull(DataFileParser.parseLanguages("ukrainian|polish"));
        assertNull(DataFileParser.parseLanguages(""));
    }

    // pairs

    @Test
    public void pair_normal() {
        assertArrayEquals(new String[] { "a", "b" }, DataFileParser.parsePair("a|b"));
    }

    @Test
    public void pair_withSpacesIsNotTrimmed() {
        assertArrayEquals(new String[] { "a b", " c d" }, DataFileParser.parsePair("a b| c d"));
    }

    @Test
    public void pair_wrongFieldCount() {
        assertNull(DataFileParser.parsePair("a"));
        assertNull(DataFileParser.parsePair("a|b|c"));
        assertNull(DataFileParser.parsePair(""));
        assertNull(DataFileParser.parsePair(null));
    }

    @Test
    public void pair_emptyFields_followStringSplit() {
        // String.split drops trailing empty strings: "a|" -> ["a"] -> rejected
        assertNull(DataFileParser.parsePair("a|"));
        assertNull(DataFileParser.parsePair("|"));
        // but a leading empty string is kept: "|b" -> ["", "b"] -> accepted
        assertArrayEquals(new String[] { "", "b" }, DataFileParser.parsePair("|b"));
    }

    @Test
    public void pair_keepsCyrillicAndPolishDiacritics() {
        assertArrayEquals(new String[] { "Брехати", "kłamać" }, DataFileParser.parsePair("Брехати|kłamać"));
        assertArrayEquals(new String[] { "їжак", "jeż ąęśćźżółń" }, DataFileParser.parsePair("їжак|jeż ąęśćźżółń"));
    }

    // real asset

    @Test
    public void realAssetFile_parses() throws IOException {
        // Gradle runs unit tests with the module directory as the working directory
        List<String> lines = Files.readAllLines(Paths.get("src/main/assets/data"), StandardCharsets.UTF_8);
        assertTrue(lines.size() > 2);

        assertEquals(1, DataFileParser.parseVersion(lines.get(0)));
        Language[] languages = DataFileParser.parseLanguages(lines.get(1));
        assertNotNull(languages);
        assertArrayEquals(new Language[] { Language.UKRAINIAN, Language.POLISH }, languages);

        int pairs = 0;
        for (String line : lines.subList(2, lines.size())) {
            if (DataFileParser.parsePair(line) != null) pairs++;
        }
        assertTrue("pairs parsed: " + pairs, pairs >= 460);
    }
}
