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
package com.googlecode.htmlcompressor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CmdLineCompressorTest {

    /** Reusable HTML snippet with extra whitespace and a comment. */
    private static final String SAMPLE_HTML = "<html>  <!-- comment -->  <body>  <p>hello</p>  </body>  </html>";

    /** Reusable XML snippet. */
    private static final String SAMPLE_XML = "<root>  <!-- comment -->  <child>  text  </child>  </root>";

    @TempDir
    Path tmpDir;

    // -----------------------------------------------------------------------
    // Help / usage
    // -----------------------------------------------------------------------

    @Test
    void testHelpShortOption() {
        CmdLineCompressor c = new CmdLineCompressor(new String[] { "-h" });
        assertDoesNotThrow(c::process);
    }

    @Test
    void testHelpLongOption() {
        CmdLineCompressor c = new CmdLineCompressor(new String[] { "--help" });
        assertDoesNotThrow(c::process);
    }

    @Test
    void testHelpQuestionMarkShort() {
        CmdLineCompressor c = new CmdLineCompressor(new String[] { "-?" });
        assertDoesNotThrow(c::process);
    }

    @Test
    void testHelpWindowsSlashQuestion() {
        // "/?" is handled by the manual arg-scan loop inside the constructor
        CmdLineCompressor c = new CmdLineCompressor(new String[] { "/?" });
        assertDoesNotThrow(c::process);
    }

    // -----------------------------------------------------------------------
    // HTML compression
    // -----------------------------------------------------------------------

    @Test
    void testCompressHtmlFileToOutputFile() throws IOException {
        Path input = tmpDir.resolve("input.html");
        Path output = tmpDir.resolve("output.html");
        Files.writeString(input, SAMPLE_HTML);

        CmdLineCompressor c = new CmdLineCompressor(new String[] { "-o", output.toString(), input.toString() });
        c.process();

        String result = Files.readString(output);
        assertNotNull(result);
        assertTrue(result.contains("<html>"));
        // multi-spaces should have been removed
        assertFalse(result.contains("  "));
    }

    @Test
    void testCompressHtmlWithPreserveComments() throws IOException {
        Path input = tmpDir.resolve("in.html");
        Path output = tmpDir.resolve("out.html");
        Files.writeString(input, SAMPLE_HTML);

        CmdLineCompressor c = new CmdLineCompressor(
                new String[] { "--preserve-comments", "-o", output.toString(), input.toString() });
        c.process();

        String result = Files.readString(output);
        assertTrue(result.contains("<!--"));
    }

    @Test
    void testCompressHtmlWithPreserveMultiSpaces() throws IOException {
        Path input = tmpDir.resolve("in.html");
        Path output = tmpDir.resolve("out.html");
        Files.writeString(input, SAMPLE_HTML);

        CmdLineCompressor c = new CmdLineCompressor(
                new String[] { "--preserve-multi-spaces", "-o", output.toString(), input.toString() });
        c.process();

        String result = Files.readString(output);
        // comment removed (default) but multi-spaces preserved
        assertFalse(result.contains("<!--"));
        assertTrue(result.contains("  "));
    }

    @Test
    void testCompressHtmlWithRemoveIntertagSpaces() throws IOException {
        Path input = tmpDir.resolve("in.html");
        Path output = tmpDir.resolve("out.html");
        Files.writeString(input, "<html>  <body>  <p>text</p>  </body>  </html>");

        CmdLineCompressor c = new CmdLineCompressor(
                new String[] { "--remove-intertag-spaces", "-o", output.toString(), input.toString() });
        c.process();

        String result = Files.readString(output);
        // no space between adjacent tags
        assertTrue(result.contains("><"));
    }

    @Test
    void testCompressHtmlWithRemoveQuotes() throws IOException {
        Path input = tmpDir.resolve("in.html");
        Path output = tmpDir.resolve("out.html");
        Files.writeString(input, "<html><body class=\"main\"><p>text</p></body></html>");

        CmdLineCompressor c = new CmdLineCompressor(
                new String[] { "--remove-quotes", "-o", output.toString(), input.toString() });
        c.process();

        String result = Files.readString(output);
        // quotes around simple attribute values should be gone
        assertTrue(result.contains("class=main"));
    }

    @Test
    void testRemoveSurroundingSpacesMin() throws IOException {
        Path input = tmpDir.resolve("in.html");
        Path output = tmpDir.resolve("out.html");
        Files.writeString(input, "<html>  <body>  <div>  text  </div>  </body>  </html>");

        CmdLineCompressor c = new CmdLineCompressor(
                new String[] { "--remove-surrounding-spaces", "min", "-o", output.toString(), input.toString() });
        c.process();

        assertTrue(Files.exists(output));
    }

    @Test
    void testRemoveSurroundingSpacesMax() throws IOException {
        Path input = tmpDir.resolve("in.html");
        Path output = tmpDir.resolve("out.html");
        Files.writeString(input, "<html>  <body>  <p>  text  </p>  </body>  </html>");

        CmdLineCompressor c = new CmdLineCompressor(
                new String[] { "--remove-surrounding-spaces", "max", "-o", output.toString(), input.toString() });
        c.process();

        assertTrue(Files.exists(output));
    }

    @Test
    void testRemoveSurroundingSpacesAll() throws IOException {
        Path input = tmpDir.resolve("in.html");
        Path output = tmpDir.resolve("out.html");
        Files.writeString(input, "<html>  <body>  <p>  text  </p>  </body>  </html>");

        CmdLineCompressor c = new CmdLineCompressor(
                new String[] { "--remove-surrounding-spaces", "all", "-o", output.toString(), input.toString() });
        c.process();

        assertTrue(Files.exists(output));
    }

    @Test
    void testRemoveSurroundingSpacesCustomList() throws IOException {
        Path input = tmpDir.resolve("in.html");
        Path output = tmpDir.resolve("out.html");
        Files.writeString(input, "<html>  <body>  <p>  text  </p>  </body>  </html>");

        CmdLineCompressor c = new CmdLineCompressor(
                new String[] { "--remove-surrounding-spaces", "p,body", "-o", output.toString(), input.toString() });
        c.process();

        assertTrue(Files.exists(output));
    }

    @Test
    void testCompressHtmlWithAdvancedFlags() throws IOException {
        Path input = tmpDir.resolve("in.html");
        Path output = tmpDir.resolve("out.html");
        Files.writeString(input,
                "<!doctype html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">"
                        + "<html><body>" + "<script language=\"javascript\" type=\"text/javascript\">var x=1;</script>"
                        + "<link rel=\"stylesheet\" type=\"text/css\" href=\"a.css\">"
                        + "<a href=\"javascript:void(0)\">click</a>" + "</body></html>");

        CmdLineCompressor c = new CmdLineCompressor(
                new String[] { "--simple-doctype", "--remove-script-attr", "--remove-link-attr", "--remove-js-protocol",
                        "--simple-bool-attr", "-o", output.toString(), input.toString() });
        c.process();

        String result = Files.readString(output);
        assertTrue(result.contains("<!DOCTYPE html>"));
        assertFalse(result.contains("type=\"text/javascript\""));
    }

    // -----------------------------------------------------------------------
    // XML compression
    // -----------------------------------------------------------------------

    @Test
    void testCompressXmlFileByExtension() throws IOException {
        Path input = tmpDir.resolve("input.xml");
        Path output = tmpDir.resolve("output.xml");
        Files.writeString(input, SAMPLE_XML);

        CmdLineCompressor c = new CmdLineCompressor(new String[] { "-o", output.toString(), input.toString() });
        c.process();

        String result = Files.readString(output);
        // comment and intertag spaces removed by default in XML mode
        assertFalse(result.contains("<!--"));
        assertTrue(result.contains("<child>"));
    }

    @Test
    void testCompressXmlExplicitType() throws IOException {
        Path input = tmpDir.resolve("input.html"); // wrong extension on purpose
        Path output = tmpDir.resolve("output.txt");
        Files.writeString(input, SAMPLE_XML);

        CmdLineCompressor c = new CmdLineCompressor(
                new String[] { "--type", "xml", "-o", output.toString(), input.toString() });
        c.process();

        String result = Files.readString(output);
        assertFalse(result.contains("<!--"));
    }

    @Test
    void testCompressXmlWithPreserveComments() throws IOException {
        Path input = tmpDir.resolve("in.xml");
        Path output = tmpDir.resolve("out.xml");
        Files.writeString(input, SAMPLE_XML);

        CmdLineCompressor c = new CmdLineCompressor(
                new String[] { "--type", "xml", "--preserve-comments", "-o", output.toString(), input.toString() });
        c.process();

        String result = Files.readString(output);
        assertTrue(result.contains("<!--"));
    }

    // -----------------------------------------------------------------------
    // Invalid / edge-case inputs
    // -----------------------------------------------------------------------

    @Test
    void testUnknownTypeIsHandledGracefully() {
        CmdLineCompressor c = new CmdLineCompressor(new String[] { "--type", "pdf", "somefile.pdf" });
        // IllegalArgumentException is caught inside process() and logged; no throw
        assertDoesNotThrow(c::process);
    }

    // -----------------------------------------------------------------------
    // Output to a file vs. directory
    // -----------------------------------------------------------------------

    @Test
    void testSingleFileToOutputFileWithNoDirectory() throws IOException {
        Path input = tmpDir.resolve("in.html");
        Path output = tmpDir.resolve("result.html");
        Files.writeString(input, SAMPLE_HTML);

        CmdLineCompressor c = new CmdLineCompressor(new String[] { "-o", output.toString(), input.toString() });
        c.process();

        assertTrue(Files.exists(output));
        assertFalse(Files.readString(output).isBlank());
    }

    // -----------------------------------------------------------------------
    // Analyze mode
    // -----------------------------------------------------------------------

    @Test
    void testAnalyzeModeWithHtmlFile() throws IOException {
        Path input = tmpDir.resolve("analyze.html");
        Files.writeString(input, SAMPLE_HTML);

        CmdLineCompressor c = new CmdLineCompressor(new String[] { "--analyze", input.toString() });
        assertDoesNotThrow(c::process);
    }

    @Test
    void testAnalyzeModeWithClosureJsCompressor() throws IOException {
        Path input = tmpDir.resolve("analyze.html");
        Files.writeString(input, SAMPLE_HTML);

        CmdLineCompressor c = new CmdLineCompressor(
                new String[] { "--analyze", "--js-compressor", "closure", input.toString() });
        assertDoesNotThrow(c::process);
    }

    // -----------------------------------------------------------------------
    // Directory / CompressorFileFilter paths
    // -----------------------------------------------------------------------

    @Test
    void testCompressHtmlDirectory() throws IOException {
        Path inputDir = tmpDir.resolve("indir");
        Path outputDir = tmpDir.resolve("outdir/");
        Files.createDirectories(inputDir);

        Files.writeString(inputDir.resolve("a.html"), SAMPLE_HTML);
        Files.writeString(inputDir.resolve("b.html"), SAMPLE_HTML);

        CmdLineCompressor c = new CmdLineCompressor(new String[] { "-o", outputDir.toString(), inputDir.toString() });
        c.process();

        assertTrue(Files.exists(outputDir));
        long outputCount = Files.list(outputDir).count();
        assertTrue(outputCount >= 2);
    }

    @Test
    void testCompressHtmlDirectoryRecursive() throws IOException {
        Path inputDir = tmpDir.resolve("recIn");
        Path subDir = inputDir.resolve("sub");
        Path outputDir = tmpDir.resolve("recOut/");
        Files.createDirectories(subDir);

        Files.writeString(inputDir.resolve("root.html"), SAMPLE_HTML);
        Files.writeString(subDir.resolve("child.html"), SAMPLE_HTML);

        CmdLineCompressor c = new CmdLineCompressor(
                new String[] { "-r", "-o", outputDir.toString(), inputDir.toString() });
        c.process();

        // Recursively collected at least one file at the root level
        assertTrue(Files.exists(outputDir));
    }

    @Test
    void testCompressHtmlDirectoryWithFileMask() throws IOException {
        Path inputDir = tmpDir.resolve("masked");
        Path outputDir = tmpDir.resolve("maskedOut/");
        Files.createDirectories(inputDir);

        Files.writeString(inputDir.resolve("keep.html"), SAMPLE_HTML);
        Files.writeString(inputDir.resolve("skip.xml"), SAMPLE_XML);

        CmdLineCompressor c = new CmdLineCompressor(
                new String[] { "-m", "*.html", "-o", outputDir.toString(), inputDir.toString() });
        c.process();

        // only .html file should be in output
        assertTrue(Files.exists(outputDir));
        long count = Files.list(outputDir).filter(p -> p.toString().endsWith(".html")).count();
        assertTrue(count >= 1);
    }

    @Test
    void testCompressXmlDirectoryWithXmlType() throws IOException {
        Path inputDir = tmpDir.resolve("xmlin");
        Path outputDir = tmpDir.resolve("xmlout/");
        Files.createDirectories(inputDir);

        Files.writeString(inputDir.resolve("a.xml"), SAMPLE_XML);

        CmdLineCompressor c = new CmdLineCompressor(
                new String[] { "--type", "xml", "-o", outputDir.toString(), inputDir.toString() });
        c.process();

        assertTrue(Files.exists(outputDir));
    }

    @Test
    void testMultipleInputFilesToDirectoryOutput() throws IOException {
        Path f1 = tmpDir.resolve("one.html");
        Path f2 = tmpDir.resolve("two.html");
        Path outputDir = tmpDir.resolve("multi/");
        Files.createDirectories(outputDir);

        Files.writeString(f1, SAMPLE_HTML);
        Files.writeString(f2, SAMPLE_HTML);

        CmdLineCompressor c = new CmdLineCompressor(
                new String[] { "-o", outputDir.toString(), f1.toString(), f2.toString() });
        c.process();

        long count = Files.list(outputDir).count();
        assertTrue(count >= 2);
    }

    // -----------------------------------------------------------------------
    // Preserve special tag blocks
    // -----------------------------------------------------------------------

    @Test
    void testPreservePhpTags() throws IOException {
        Path input = tmpDir.resolve("php.html");
        Path output = tmpDir.resolve("php_out.html");
        Files.writeString(input, "<html>  <?php echo 'hi';  ?>  </html>");

        CmdLineCompressor c = new CmdLineCompressor(
                new String[] { "--preserve-php", "-o", output.toString(), input.toString() });
        c.process();

        String result = Files.readString(output);
        assertTrue(result.contains("<?php"));
    }

    @Test
    void testPreserveServerScript() throws IOException {
        Path input = tmpDir.resolve("ss.html");
        Path output = tmpDir.resolve("ss_out.html");
        Files.writeString(input, "<html>  <% String x = \"hi\"; %>  </html>");

        CmdLineCompressor c = new CmdLineCompressor(
                new String[] { "--preserve-server-script", "-o", output.toString(), input.toString() });
        c.process();

        String result = Files.readString(output);
        assertTrue(result.contains("<%"));
    }

    @Test
    void testPreserveSsi() throws IOException {
        Path input = tmpDir.resolve("ssi.html");
        Path output = tmpDir.resolve("ssi_out.html");
        Files.writeString(input, "<html>  <!--# include file=\"header.html\" -->  </html>");

        CmdLineCompressor c = new CmdLineCompressor(
                new String[] { "--preserve-ssi", "-o", output.toString(), input.toString() });
        c.process();

        String result = Files.readString(output);
        assertTrue(result.contains("<!--#"));
    }

    // -----------------------------------------------------------------------
    // Custom patterns file
    // -----------------------------------------------------------------------

    @Test
    void testCustomPatternsFile() throws IOException {
        Path input = tmpDir.resolve("patt.html");
        Path output = tmpDir.resolve("patt_out.html");
        Path patterns = tmpDir.resolve("patterns.txt");

        Files.writeString(input, "<html>  <!-- preserve:start -->  content  <!-- preserve:end -->  </html>");
        Files.writeString(patterns, "<!--\\s*preserve:start\\s*-->.*?<!--\\s*preserve:end\\s*-->");

        CmdLineCompressor c = new CmdLineCompressor(
                new String[] { "-p", patterns.toString(), "-o", output.toString(), input.toString() });
        c.process();

        assertTrue(Files.exists(output));
    }

    @Test
    void testInvalidPatternsFileLogsError() throws IOException {
        Path input = tmpDir.resolve("in.html");
        Path output = tmpDir.resolve("out.html");
        Files.writeString(input, SAMPLE_HTML);

        // File that does not exist → triggers IllegalArgumentException in createHtmlCompressor
        CmdLineCompressor c = new CmdLineCompressor(new String[] { "-p", tmpDir.resolve("nonexistent.txt").toString(),
                "-o", output.toString(), input.toString() });
        assertDoesNotThrow(c::process);
    }

    // -----------------------------------------------------------------------
    // Single file written to stdout (no -o flag) – verify it doesn't throw
    // -----------------------------------------------------------------------

    @Test
    void testCompressHtmlFileToStdout() throws IOException {
        Path input = tmpDir.resolve("in.html");
        Files.writeString(input, SAMPLE_HTML);

        // No -o option: output goes to System.out
        CmdLineCompressor c = new CmdLineCompressor(new String[] { input.toString() });
        assertDoesNotThrow(c::process);
    }

    @Test
    void testCompressXmlFileToStdout() throws IOException {
        Path input = tmpDir.resolve("in.xml");
        Files.writeString(input, SAMPLE_XML);

        CmdLineCompressor c = new CmdLineCompressor(new String[] { input.toString() });
        assertDoesNotThrow(c::process);
    }

    // -----------------------------------------------------------------------
    // Edge-case: directory input without a directory output should log error
    // -----------------------------------------------------------------------

    @Test
    void testDirectoryInputWithoutDirectoryOutputIsHandled() throws IOException {
        Path inputDir = tmpDir.resolve("alone");
        Files.createDirectories(inputDir);
        Files.writeString(inputDir.resolve("f.html"), SAMPLE_HTML);

        // Output is NOT a directory → causes IllegalArgumentException, handled internally
        CmdLineCompressor c = new CmdLineCompressor(
                new String[] { "-o", tmpDir.resolve("single.html").toString(), inputDir.toString() });
        assertDoesNotThrow(c::process);
    }

    // -----------------------------------------------------------------------
    // main() entry point
    // -----------------------------------------------------------------------

    @Test
    void testMainMethodWithHelp() {
        assertDoesNotThrow(() -> CmdLineCompressor.main(new String[] { "-h" }));
    }

    // -----------------------------------------------------------------------
    // File-filter with explicit type=xml and mask
    // -----------------------------------------------------------------------

    @Test
    void testXmlTypeDirectoryNoMaskFiltersXmlFiles() throws IOException {
        Path inputDir = tmpDir.resolve("xmlmix");
        Path outputDir = tmpDir.resolve("xmlmixout/");
        Files.createDirectories(inputDir);

        Files.writeString(inputDir.resolve("data.xml"), SAMPLE_XML);
        Files.writeString(inputDir.resolve("page.html"), SAMPLE_HTML);

        CmdLineCompressor c = new CmdLineCompressor(
                new String[] { "--type", "xml", "-o", outputDir.toString(), inputDir.toString() });
        c.process();

        // Only .xml file should appear in output
        assertTrue(Files.exists(outputDir));
        File[] outputs = outputDir.toFile().listFiles();
        assertNotNull(outputs);
        for (File f : outputs) {
            assertTrue(f.getName().endsWith(".xml"), "Expected only .xml files: " + f.getName());
        }
    }
}
