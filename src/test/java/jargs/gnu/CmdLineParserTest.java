/*
 *    Copyright 2009-2026 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package jargs.gnu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

class CmdLineParserTest {

    @Test
    void testParseShortAndLongOptionsWithValues() throws Exception {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option verbose = parser.addBooleanOption('v', "verbose");
        CmdLineParser.Option count = parser.addIntegerOption('c', "count");
        CmdLineParser.Option ratio = parser.addDoubleOption("ratio");
        CmdLineParser.Option name = parser.addStringOption('n', "name");

        parser.parse(new String[] { "-v", "-c", "7", "--ratio=1.5", "--name", "sample" }, Locale.US);

        assertEquals(Boolean.TRUE, parser.getOptionValue(verbose));
        assertEquals(7, parser.getOptionValue(count));
        assertEquals(1.5d, (Double) parser.getOptionValue(ratio), 0.0001d);
        assertEquals("sample", parser.getOptionValue(name));
    }

    @Test
    void testParseCombinedFlagsAndMultipleOccurrences() throws Exception {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option alpha = parser.addBooleanOption('a', "alpha");
        CmdLineParser.Option beta = parser.addBooleanOption('b', "beta");
        CmdLineParser.Option include = parser.addStringOption('i', "include");

        parser.parse(new String[] { "-ab", "-i", "first", "--include=second" });

        assertEquals(Boolean.TRUE, parser.getOptionValue(alpha));
        assertEquals(Boolean.TRUE, parser.getOptionValue(beta));
        assertEquals(List.of("first", "second"), parser.getOptionValues(include));
    }

    @Test
    void testUnknownOptionAndSuboptionAreRejected() {
        CmdLineParser parser = new CmdLineParser();
        parser.addBooleanOption('a', "alpha");

        CmdLineParser.UnknownOptionException unknownOption = assertThrows(CmdLineParser.UnknownOptionException.class,
                () -> parser.parse(new String[] { "--unknown" }));
        assertEquals("--unknown", unknownOption.getOptionName());

        CmdLineParser.UnknownSuboptionException unknownSuboption = assertThrows(
                CmdLineParser.UnknownSuboptionException.class, () -> parser.parse(new String[] { "-az" }));
        assertEquals('z', unknownSuboption.getSuboption());
        assertEquals("-az", unknownSuboption.getOptionName());
    }

    @Test
    void testNotFlagAndIllegalValueAreRejected() {
        CmdLineParser parser = new CmdLineParser();
        parser.addBooleanOption('a', "alpha");
        parser.addStringOption('c', "config");
        CmdLineParser.Option count = parser.addIntegerOption('n', "num");

        CmdLineParser.NotFlagException notFlag = assertThrows(CmdLineParser.NotFlagException.class,
                () -> parser.parse(new String[] { "-ac" }));
        assertEquals('c', notFlag.getOptionChar());

        CmdLineParser.IllegalOptionValueException invalidValue = assertThrows(
                CmdLineParser.IllegalOptionValueException.class,
                () -> parser.parse(new String[] { "-n", "not-a-number" }));
        assertEquals(count, invalidValue.getOption());
        assertEquals("not-a-number", invalidValue.getValue());
    }

    @Test
    void testOptionConstructorsAndBooleanLongOnlyOption() throws Exception {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option dryRun = parser.addBooleanOption("dry-run");
        parser.parse(new String[] { "--dry-run" });

        assertTrue((Boolean) parser.getOptionValue(dryRun));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new CmdLineParser.Option.StringOption((String) null));
        assertEquals("Null longForm not allowed", exception.getMessage());
    }

    @Test
    void testLongOptionShortAndLongForm() throws Exception {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option sizeShort = parser.addLongOption('s', "size");
        CmdLineParser.Option sizeLong = parser.addLongOption("max-size");

        parser.parse(new String[] { "-s", "100", "--max-size", "200" });

        assertEquals(100L, parser.getOptionValue(sizeShort));
        assertEquals(200L, parser.getOptionValue(sizeLong));
    }

    @Test
    void testLongOptionIllegalValue() {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option count = parser.addLongOption('n', "num");

        CmdLineParser.IllegalOptionValueException ex = assertThrows(CmdLineParser.IllegalOptionValueException.class,
                () -> parser.parse(new String[] { "-n", "not-a-long" }));
        assertEquals(count, ex.getOption());
        assertEquals("not-a-long", ex.getValue());
    }

    @Test
    void testDoubleDashEndsOptionParsing() throws Exception {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option flag = parser.addBooleanOption('f', "flag");

        // after "--", "--flag" is treated as a plain argument, not an option
        parser.parse(new String[] { "--", "--flag", "extra-arg" });

        // the flag must NOT have been set (it was not consumed as an option)
        assertEquals(Boolean.FALSE, parser.getOptionValue(flag, Boolean.FALSE));
    }

    @Test
    void testGetOptionValuesReturnsAllOccurrences() throws Exception {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option include = parser.addStringOption('i', "include");

        parser.parse(new String[] { "-i", "one", "-i", "two", "--include", "three" });

        assertEquals(java.util.List.of("one", "two", "three"), parser.getOptionValues(include));
    }

    @Test
    void testExceptionMessageAndOptionAccessors() throws Exception {
        CmdLineParser parser = new CmdLineParser();

        CmdLineParser.UnknownOptionException uoe = assertThrows(CmdLineParser.UnknownOptionException.class,
                () -> parser.parse(new String[] { "--bogus" }));
        assertNotNull(uoe.getMessage());
        assertEquals("--bogus", uoe.getOptionName());

        // IllegalOptionValueException getMessage
        parser.addIntegerOption('n', "num");
        CmdLineParser.IllegalOptionValueException ive = assertThrows(CmdLineParser.IllegalOptionValueException.class,
                () -> parser.parse(new String[] { "-n", "NaN" }));
        assertNotNull(ive.getMessage());
    }
}
