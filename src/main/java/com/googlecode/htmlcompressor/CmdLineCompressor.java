/**
 *    Copyright 2009-2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.googlecode.htmlcompressor;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.SourceFile;
import com.googlecode.htmlcompressor.analyzer.HtmlAnalyzer;
import com.googlecode.htmlcompressor.compressor.ClosureJavaScriptCompressor;
import com.googlecode.htmlcompressor.compressor.Compressor;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import com.googlecode.htmlcompressor.compressor.XmlCompressor;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.Option;
import jargs.gnu.CmdLineParser.OptionException;

/**
 * Wrapper for HTML and XML compressor classes that allows using them from a command line.
 * 
 * <p>
 * Usage: <code>java -jar htmlcompressor.jar [options] [input]</code>
 * <p>
 * To view a list of all available parameters please run with <code>-?</code> option:
 * <p>
 * <code>java -jar htmlcompressor.jar -?</code>
 * 
 * @author <a href="mailto:serg472@gmail.com">Sergiy Kovalchuk</a>
 */
public class CmdLineCompressor {

    /** The Constant logger. */
    private static final Logger  logger     = LoggerFactory.getLogger(CmdLineCompressor.class);

    /** The Constant urlPattern. */
    private static final Pattern urlPattern = Pattern.compile("^https?://.*$", Pattern.CASE_INSENSITIVE);

    /** The help opt. */
    private boolean              helpOpt;

    /** The analyze opt. */
    private boolean              analyzeOpt;

    /** The charset opt. */
    private Charset              charsetOpt;

    /** The output filename opt. */
    private String               outputFilenameOpt;

    /** The patterns filename opt. */
    private String               patternsFilenameOpt;

    /** The type opt. */
    private String               typeOpt;

    /** The filemask opt. */
    private String               filemaskOpt;

    /** The recursive opt. */
    private boolean              recursiveOpt;

    /** The preserve comments opt. */
    private boolean              preserveCommentsOpt;

    /** The preserve intertag spaces opt. */
    private boolean              preserveIntertagSpacesOpt;

    /** The preserve multi spaces opt. */
    private boolean              preserveMultiSpacesOpt;

    /** The remove intertag spaces opt. */
    private boolean              removeIntertagSpacesOpt;

    /** The remove quotes opt. */
    private boolean              removeQuotesOpt;

    /** The remove surrounding spaces opt. */
    private String               removeSurroundingSpacesOpt;

    /** The preserve line breaks opt. */
    private boolean              preserveLineBreaksOpt;

    /** The preserve php tags opt. */
    private boolean              preservePhpTagsOpt;

    /** The preserve server script tags opt. */
    private boolean              preserveServerScriptTagsOpt;

    /** The preserve ssi tags opt. */
    private boolean              preserveSsiTagsOpt;

    /** The compress js opt. */
    private boolean              compressJsOpt;

    /** The compress css opt. */
    private boolean              compressCssOpt;

    /** The js compressor opt. */
    private String               jsCompressorOpt;

    /** The simple doctype opt. */
    private boolean              simpleDoctypeOpt;

    /** The remove script attributes opt. */
    private boolean              removeScriptAttributesOpt;

    /** The remove style attributes opt. */
    private boolean              removeStyleAttributesOpt;

    /** The remove link attributes opt. */
    private boolean              removeLinkAttributesOpt;

    /** The remove form attributes opt. */
    private boolean              removeFormAttributesOpt;

    /** The remove input attributes opt. */
    private boolean              removeInputAttributesOpt;

    /** The simple boolean attributes opt. */
    private boolean              simpleBooleanAttributesOpt;

    /** The remove java script protocol opt. */
    private boolean              removeJavaScriptProtocolOpt;

    /** The remove http protocol opt. */
    private boolean              removeHttpProtocolOpt;

    /** The remove https protocol opt. */
    private boolean              removeHttpsProtocolOpt;

    /** The nomunge opt. */
    private boolean              nomungeOpt;

    /** The linebreak opt. */
    private int                  linebreakOpt;

    /** The preserve semi opt. */
    private boolean              preserveSemiOpt;

    /** The disable optimizations opt. */
    private boolean              disableOptimizationsOpt;

    /** The closure opt level opt. */
    private String               closureOptLevelOpt;

    /** The closure custom externs only opt. */
    private boolean              closureCustomExternsOnlyOpt;

    /** The closure externs opt. */
    private List<String>         closureExternsOpt;

    /** The file args opt. */
    private List<String>         fileArgsOpt;

    /**
     * Instantiates a new cmd line compressor.
     *
     * @param args
     *            the args
     */
    public CmdLineCompressor(String[] args) {
        CmdLineParser parser = new CmdLineParser();

        Option helpOption = parser.addBooleanOption('h', "help");
        Option helpOptAlt = parser.addBooleanOption('?', "help_alt");
        Option analyzeOption = parser.addBooleanOption('a', "analyze");
        Option recursiveOption = parser.addBooleanOption('r', "recursive");
        Option charsetOption = parser.addStringOption('c', "charset");
        Option outputFilenameOption = parser.addStringOption('o', "output");
        Option patternsFilenameOption = parser.addStringOption('p', "preserve");
        Option typeOption = parser.addStringOption('t', "type");
        Option filemaskOption = parser.addStringOption('m', "mask");
        Option preserveCommentsOption = parser.addBooleanOption("preserve-comments");
        Option preserveIntertagSpacesOption = parser.addBooleanOption("preserve-intertag-spaces");
        Option preserveMultiSpacesOption = parser.addBooleanOption("preserve-multi-spaces");
        Option removeIntertagSpacesOption = parser.addBooleanOption("remove-intertag-spaces");
        Option removeSurroundingSpacesOption = parser.addStringOption("remove-surrounding-spaces");
        Option removeQuotesOption = parser.addBooleanOption("remove-quotes");
        Option preserveLineBreaksOption = parser.addBooleanOption("preserve-line-breaks");
        Option preservePhpTagsOption = parser.addBooleanOption("preserve-php");
        Option preserveServerScriptTagsOption = parser.addBooleanOption("preserve-server-script");
        Option preserveSsiTagsOption = parser.addBooleanOption("preserve-ssi");
        Option compressJsOption = parser.addBooleanOption("compress-js");
        Option compressCssOption = parser.addBooleanOption("compress-css");
        Option jsCompressorOption = parser.addStringOption("js-compressor");

        Option simpleDoctypeOption = parser.addBooleanOption("simple-doctype");
        Option removeScriptAttributesOption = parser.addBooleanOption("remove-script-attr");
        Option removeStyleAttributesOption = parser.addBooleanOption("remove-style-attr");
        Option removeLinkAttributesOption = parser.addBooleanOption("remove-link-attr");
        Option removeFormAttributesOption = parser.addBooleanOption("remove-form-attr");
        Option removeInputAttributesOption = parser.addBooleanOption("remove-input-attr");
        Option simpleBooleanAttributesOption = parser.addBooleanOption("simple-bool-attr");
        Option removeJavaScriptProtocolOption = parser.addBooleanOption("remove-js-protocol");
        Option removeHttpProtocolOption = parser.addBooleanOption("remove-http-protocol");
        Option removeHttpsProtocolOption = parser.addBooleanOption("remove-https-protocol");

        Option nomungeOption = parser.addBooleanOption("nomunge");
        Option linebreakOption = parser.addStringOption("line-break");
        Option preserveSemiOption = parser.addBooleanOption("preserve-semi");
        Option disableOptimizationsOption = parser.addBooleanOption("disable-optimizations");

        Option closureOptLevelOption = parser.addStringOption("closure-opt-level");
        Option closureCustomExternsOnlyOption = parser.addBooleanOption("closure-custom-externs-only");
        Option closureExternsOption = parser.addStringOption("closure-externs");

        try {
            parser.parse(args);

            this.helpOpt = (Boolean) parser.getOptionValue(helpOption, false)
                    || (Boolean) parser.getOptionValue(helpOptAlt, false);
            this.analyzeOpt = (Boolean) parser.getOptionValue(analyzeOption, false);
            this.recursiveOpt = (Boolean) parser.getOptionValue(recursiveOption, false);
            this.charsetOpt = Charset.forName((String) parser.getOptionValue(charsetOption, "UTF-8"));
            this.outputFilenameOpt = (String) parser.getOptionValue(outputFilenameOption);
            this.patternsFilenameOpt = (String) parser.getOptionValue(patternsFilenameOption);
            this.typeOpt = (String) parser.getOptionValue(typeOption);
            this.filemaskOpt = (String) parser.getOptionValue(filemaskOption);
            this.preserveCommentsOpt = (Boolean) parser.getOptionValue(preserveCommentsOption, false);
            this.preserveIntertagSpacesOpt = (Boolean) parser.getOptionValue(preserveIntertagSpacesOption, false);
            this.preserveMultiSpacesOpt = (Boolean) parser.getOptionValue(preserveMultiSpacesOption, false);
            this.removeIntertagSpacesOpt = (Boolean) parser.getOptionValue(removeIntertagSpacesOption, false);
            this.removeQuotesOpt = (Boolean) parser.getOptionValue(removeQuotesOption, false);
            this.preserveLineBreaksOpt = (Boolean) parser.getOptionValue(preserveLineBreaksOption, false);
            this.preservePhpTagsOpt = (Boolean) parser.getOptionValue(preservePhpTagsOption, false);
            this.preserveServerScriptTagsOpt = (Boolean) parser.getOptionValue(preserveServerScriptTagsOption, false);
            this.preserveSsiTagsOpt = (Boolean) parser.getOptionValue(preserveSsiTagsOption, false);
            this.compressJsOpt = (Boolean) parser.getOptionValue(compressJsOption, false);
            this.compressCssOpt = (Boolean) parser.getOptionValue(compressCssOption, false);
            this.jsCompressorOpt = (String) parser.getOptionValue(jsCompressorOption, HtmlCompressor.JS_COMPRESSOR_YUI);

            this.simpleDoctypeOpt = (Boolean) parser.getOptionValue(simpleDoctypeOption, false);
            this.removeScriptAttributesOpt = (Boolean) parser.getOptionValue(removeScriptAttributesOption, false);
            this.removeStyleAttributesOpt = (Boolean) parser.getOptionValue(removeStyleAttributesOption, false);
            this.removeLinkAttributesOpt = (Boolean) parser.getOptionValue(removeLinkAttributesOption, false);
            this.removeFormAttributesOpt = (Boolean) parser.getOptionValue(removeFormAttributesOption, false);
            this.removeInputAttributesOpt = (Boolean) parser.getOptionValue(removeInputAttributesOption, false);
            this.simpleBooleanAttributesOpt = (Boolean) parser.getOptionValue(simpleBooleanAttributesOption, false);
            this.removeJavaScriptProtocolOpt = (Boolean) parser.getOptionValue(removeJavaScriptProtocolOption, false);
            this.removeHttpProtocolOpt = (Boolean) parser.getOptionValue(removeHttpProtocolOption, false);
            this.removeHttpsProtocolOpt = (Boolean) parser.getOptionValue(removeHttpsProtocolOption, false);

            this.nomungeOpt = (Boolean) parser.getOptionValue(nomungeOption, false);
            this.linebreakOpt = (Integer) parser.getOptionValue(linebreakOption, -1);
            this.preserveSemiOpt = (Boolean) parser.getOptionValue(preserveSemiOption, false);
            this.disableOptimizationsOpt = (Boolean) parser.getOptionValue(disableOptimizationsOption, false);

            this.closureOptLevelOpt = (String) parser.getOptionValue(closureOptLevelOption,
                    ClosureJavaScriptCompressor.COMPILATION_LEVEL_SIMPLE);
            this.closureCustomExternsOnlyOpt = (Boolean) parser.getOptionValue(closureCustomExternsOnlyOption, false);

            this.closureExternsOpt = parser.getOptionValues(closureExternsOption);

            this.removeSurroundingSpacesOpt = (String) parser.getOptionValue(removeSurroundingSpacesOption);
            if (this.removeSurroundingSpacesOpt != null) {
                if ("min".equalsIgnoreCase(this.removeSurroundingSpacesOpt)) {
                    this.removeSurroundingSpacesOpt = HtmlCompressor.BLOCK_TAGS_MIN;
                } else if ("max".equalsIgnoreCase(this.removeSurroundingSpacesOpt)) {
                    this.removeSurroundingSpacesOpt = HtmlCompressor.BLOCK_TAGS_MAX;
                } else if ("all".equalsIgnoreCase(this.removeSurroundingSpacesOpt)) {
                    this.removeSurroundingSpacesOpt = HtmlCompressor.ALL_TAGS;
                }
            }

            // input file
            this.fileArgsOpt = parser.getRemainingArgs();

            // charset
            this.charsetOpt = Charset.isSupported(this.charsetOpt.name()) ? this.charsetOpt : StandardCharsets.UTF_8;

            // look for "/?"
            for (int i = 0; i < args.length; i++) {
                if ("/?".equals(args[i])) {
                    this.helpOpt = true;
                    break;
                }
            }

        } catch (OptionException e) {
            logger.info("{}" + e.getMessage());
            logger.trace("", e);
            printUsage();
        }

    }

    /**
     * The main method.
     *
     * @param args
     *            the arguments
     */
    public static void main(String[] args) {
        CmdLineCompressor cmdLineCompressor = new CmdLineCompressor(args);
        cmdLineCompressor.process();
    }

    /**
     * Process.
     */
    public void process() {
        try {

            // help
            if (helpOpt) {
                printUsage();
                return;
            }

            // type
            String type = typeOpt;
            if (type != null && !"html".equalsIgnoreCase(type) && !"xml".equalsIgnoreCase(type)) {
                throw new IllegalArgumentException("Unknown type: " + type);
            }

            if (fileArgsOpt.isEmpty()) {
                // html by default for stdin
                if (type == null) {
                    type = "html";
                }
            } else if (type == null) {
                // detect type from extension
                if (fileArgsOpt.get(0).toLowerCase().endsWith(".xml")) {
                    type = "xml";
                } else {
                    type = "html";
                }
            }

            if (analyzeOpt) {
                // analyzer mode
                HtmlAnalyzer analyzer = new HtmlAnalyzer(
                        HtmlCompressor.JS_COMPRESSOR_CLOSURE.equalsIgnoreCase(jsCompressorOpt)
                                ? HtmlCompressor.JS_COMPRESSOR_CLOSURE
                                : HtmlCompressor.JS_COMPRESSOR_YUI);
                analyzer.analyze(readResource(buildReader(fileArgsOpt.isEmpty() ? null : fileArgsOpt.get(0))));
            } else {
                // compression mode
                Compressor compressor = "xml".equalsIgnoreCase(type) ? createXmlCompressor() : createHtmlCompressor();
                Map<String, String> ioMap = buildInputOutputMap();
                for (Map.Entry<String, String> entry : ioMap.entrySet()) {
                    writeResource(compressor.compress(readResource(buildReader(entry.getKey()))),
                            buildWriter(entry.getValue()));
                }
            }

        } catch (NoClassDefFoundError e) {
            if (HtmlCompressor.JS_COMPRESSOR_CLOSURE.equalsIgnoreCase(jsCompressorOpt)) {
                logger.info("ERROR: For JavaScript compression using Google Closure Compiler\n"
                        + "additional jar file called compiler.jar must be present\n"
                        + "in the same directory as HtmlCompressor jar");
            } else {
                logger.info("ERROR: For CSS or JavaScript compression using YUICompressor additional jar file \n"
                        + "called yuicompressor.jar must be present\n" + "in the same directory as HtmlCompressor jar");
            }
            logger.trace("", e);
        } catch (OptionException e) {
            logger.info("{}" + e.getMessage());
            logger.trace("", e);
            printUsage();
        } catch (IOException | IllegalArgumentException e) {
            logger.info("{}" + e.getMessage());
            logger.trace("", e);
        }

    }

    /**
     * Creates the html compressor.
     *
     * @return the compressor
     * @throws OptionException
     *             the option exception
     */
    private Compressor createHtmlCompressor() throws OptionException {

        boolean useClosureCompressor = HtmlCompressor.JS_COMPRESSOR_CLOSURE.equalsIgnoreCase(jsCompressorOpt);

        // custom preserve patterns
        List<Pattern> preservePatterns = new ArrayList<>();

        // predefined
        if (preservePhpTagsOpt) {
            preservePatterns.add(HtmlCompressor.PHP_TAG_PATTERN);
        }

        if (preserveServerScriptTagsOpt) {
            preservePatterns.add(HtmlCompressor.SERVER_SCRIPT_TAG_PATTERN);
        }

        if (preserveSsiTagsOpt) {
            preservePatterns.add(HtmlCompressor.SERVER_SIDE_INCLUDE_PATTERN);
        }

        if (patternsFilenameOpt != null) {

            try (FileInputStream stream = new FileInputStream(patternsFilenameOpt);
                    BufferedReader patternsIn = new BufferedReader(new InputStreamReader(stream, charsetOpt))) {

                String line = null;
                while ((line = patternsIn.readLine()) != null) {
                    if (line.length() > 0) {
                        try {
                            preservePatterns.add(Pattern.compile(line));
                        } catch (PatternSyntaxException e) {
                            logger.trace("", e);
                            throw new IllegalArgumentException(
                                    "Regular expression compilation error: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                logger.trace("", e);
                throw new IllegalArgumentException("Unable to read custom pattern definitions file: " + e.getMessage());
            }
        }

        // set compressor options
        HtmlCompressor htmlCompressor = new HtmlCompressor();

        htmlCompressor.setRemoveComments(!preserveCommentsOpt);
        htmlCompressor.setRemoveMultiSpaces(!preserveMultiSpacesOpt);
        htmlCompressor.setRemoveIntertagSpaces(removeIntertagSpacesOpt);
        htmlCompressor.setRemoveQuotes(removeQuotesOpt);
        htmlCompressor.setPreserveLineBreaks(preserveLineBreaksOpt);
        htmlCompressor.setCompressJavaScript(compressJsOpt);
        htmlCompressor.setCompressCss(compressCssOpt);

        htmlCompressor.setSimpleDoctype(simpleDoctypeOpt);
        htmlCompressor.setRemoveScriptAttributes(removeScriptAttributesOpt);
        htmlCompressor.setRemoveStyleAttributes(removeStyleAttributesOpt);
        htmlCompressor.setRemoveLinkAttributes(removeLinkAttributesOpt);
        htmlCompressor.setRemoveFormAttributes(removeFormAttributesOpt);
        htmlCompressor.setRemoveInputAttributes(removeInputAttributesOpt);
        htmlCompressor.setSimpleBooleanAttributes(simpleBooleanAttributesOpt);
        htmlCompressor.setRemoveJavaScriptProtocol(removeJavaScriptProtocolOpt);
        htmlCompressor.setRemoveHttpProtocol(removeHttpProtocolOpt);
        htmlCompressor.setRemoveHttpsProtocol(removeHttpsProtocolOpt);
        htmlCompressor.setRemoveSurroundingSpaces(removeSurroundingSpacesOpt);

        htmlCompressor.setPreservePatterns(preservePatterns);

        htmlCompressor.setYuiJsNoMunge(nomungeOpt);
        htmlCompressor.setYuiJsPreserveAllSemiColons(preserveSemiOpt);
        htmlCompressor.setYuiJsDisableOptimizations(disableOptimizationsOpt);
        htmlCompressor.setYuiJsLineBreak(linebreakOpt);
        htmlCompressor.setYuiCssLineBreak(linebreakOpt);

        // switch js compressor to closure
        if (compressJsOpt && useClosureCompressor) {
            ClosureJavaScriptCompressor closureCompressor = new ClosureJavaScriptCompressor();

            if (closureOptLevelOpt.equalsIgnoreCase(ClosureJavaScriptCompressor.COMPILATION_LEVEL_ADVANCED)) {
                closureCompressor.setCompilationLevel(CompilationLevel.ADVANCED_OPTIMIZATIONS);
                closureCompressor.setCustomExternsOnly(closureCustomExternsOnlyOpt);

                // get externs
                if (!closureExternsOpt.isEmpty()) {
                    List<SourceFile> externs = new ArrayList<>();
                    for (String externFile : closureExternsOpt) {
                        externs.add(SourceFile.fromFile(externFile));
                    }
                    closureCompressor.setExterns(externs);
                }
            } else if (closureOptLevelOpt.equalsIgnoreCase(ClosureJavaScriptCompressor.COMPILATION_LEVEL_WHITESPACE)) {
                closureCompressor.setCompilationLevel(CompilationLevel.WHITESPACE_ONLY);
            } else {
                closureCompressor.setCompilationLevel(CompilationLevel.SIMPLE_OPTIMIZATIONS);
            }

            htmlCompressor.setJavaScriptCompressor(closureCompressor);
        }

        return htmlCompressor;
    }

    /**
     * Creates the xml compressor.
     *
     * @return the compressor
     * @throws IllegalArgumentException
     *             the illegal argument exception
     * @throws OptionException
     *             the option exception
     */
    private Compressor createXmlCompressor() throws IllegalArgumentException, OptionException {
        XmlCompressor xmlCompressor = new XmlCompressor();
        xmlCompressor.setRemoveComments(!preserveCommentsOpt);
        xmlCompressor.setRemoveIntertagSpaces(!preserveIntertagSpacesOpt);

        return xmlCompressor;
    }

    /**
     * Builds the input output map.
     *
     * @return the map
     * @throws IllegalArgumentException
     *             the illegal argument exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private Map<String, String> buildInputOutputMap() throws IllegalArgumentException, IOException {
        Map<String, String> map = new HashMap<>();

        File outpuFile = null;
        if (outputFilenameOpt != null) {
            outpuFile = new File(outputFilenameOpt);

            // make dirs
            if (outputFilenameOpt.endsWith("/") || outputFilenameOpt.endsWith("\\")) {
                outpuFile.mkdirs();
            } else {
                (new File(outpuFile.getCanonicalFile().getParent())).mkdirs();
            }
        }

        if (fileArgsOpt.size() > 1 && (outpuFile == null || !outpuFile.isDirectory())) {
            throw new IllegalArgumentException("Output must be a directory and end with a slash (/)");
        }

        if (fileArgsOpt.isEmpty()) {
            map.put(null, outputFilenameOpt);
        } else {
            for (int i = 0; i < fileArgsOpt.size(); i++) {
                if (!urlPattern.matcher(fileArgsOpt.get(i)).matches()) {
                    File inputFile = new File(fileArgsOpt.get(i));
                    if (inputFile.isDirectory()) {
                        // is dir
                        if (outpuFile != null && outpuFile.isDirectory()) {
                            if (!recursiveOpt) {
                                // non-recursive
                                for (File file : inputFile
                                        .listFiles(new CompressorFileFilter(typeOpt, filemaskOpt, false))) {
                                    if (!file.isDirectory()) {
                                        String from = file.getCanonicalPath();
                                        String to = from.replaceFirst(escRegEx(inputFile.getCanonicalPath()),
                                                Matcher.quoteReplacement(outpuFile.getCanonicalPath()));
                                        map.put(from, to);
                                    }
                                }
                            } else {
                                // recursive
                                Stack<File> fileStack = new Stack<>();
                                fileStack.push(inputFile);
                                while (!fileStack.isEmpty()) {
                                    File child = fileStack.pop();
                                    if (child.isDirectory()) {
                                        for (File f : child
                                                .listFiles(new CompressorFileFilter(typeOpt, filemaskOpt, true))) {
                                            fileStack.push(f);
                                        }
                                    } else if (child.isFile()) {
                                        String from = child.getCanonicalPath();
                                        String to = from.replaceFirst(escRegEx(inputFile.getCanonicalPath()),
                                                Matcher.quoteReplacement(outpuFile.getCanonicalPath()));
                                        map.put(from, to);
                                        // make dirs
                                        (new File((new File(to)).getCanonicalFile().getParent())).mkdirs();
                                    }
                                }
                            }
                        } else {
                            throw new IllegalArgumentException("Output must be a directory and end with a slash (/)");
                        }
                    } else {
                        // is file
                        if (outpuFile != null && outpuFile.isDirectory()) {
                            String from = inputFile.getCanonicalPath();
                            String to = from.replaceFirst(
                                    escRegEx(inputFile.getCanonicalFile().getParentFile().getCanonicalPath()),
                                    Matcher.quoteReplacement(outpuFile.getCanonicalPath()));
                            map.put(fileArgsOpt.get(i), to);
                        } else {
                            map.put(fileArgsOpt.get(i), outputFilenameOpt);
                        }

                    }
                } else {
                    // is url
                    if (fileArgsOpt.size() == 1 && (outpuFile == null || !outpuFile.isDirectory())) {
                        map.put(fileArgsOpt.get(i), outputFilenameOpt);
                    } else {
                        throw new IllegalArgumentException(
                                "Input URL should be single and cannot have directory as output");
                    }
                }
            }
        }

        return map;
    }

    /**
     * Builds the reader.
     *
     * @param filename
     *            the filename
     * @return the buffered reader
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private BufferedReader buildReader(String filename) throws IOException {
        if (filename == null) {
            return new BufferedReader(new InputStreamReader(System.in, charsetOpt));
        } else if (urlPattern.matcher(filename).matches()) {
            return new BufferedReader(new InputStreamReader((new URL(filename)).openConnection().getInputStream()));
        } else {
            return new BufferedReader(new InputStreamReader(new FileInputStream(filename), charsetOpt));
        }
    }

    /**
     * Builds the writer.
     *
     * @param filename
     *            the filename
     * @return the writer
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private Writer buildWriter(String filename) throws IOException {
        if (filename == null) {
            return new OutputStreamWriter(System.out, charsetOpt);
        } else {
            return new OutputStreamWriter(new FileOutputStream(filename), charsetOpt);
        }
    }

    /**
     * Read resource.
     *
     * @param input
     *            the input
     * @return the string
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private String readResource(BufferedReader input) throws IOException {
        StringBuilder source = new StringBuilder();
        try {
            String line = null;
            while ((line = input.readLine()) != null) {
                source.append(line);
                source.append(System.getProperty("line.separator"));
            }
        } finally {
            closeStream(input);
        }
        return source.toString();
    }

    /**
     * Write resource.
     *
     * @param content
     *            the content
     * @param output
     *            the output
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void writeResource(String content, Writer output) throws IOException {
        try {
            output.write(content);
        } finally {
            closeStream(output);
        }
    }

    /**
     * Close stream.
     *
     * @param stream
     *            the stream
     */
    private void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                logger.trace("", e);
            }
        }
    }

    /**
     * Esc reg ex.
     *
     * @param inStr
     *            the in str
     * @return the string
     */
    private String escRegEx(String inStr) {
        return inStr.replaceAll("([\\\\*+\\[\\](){}\\$.?\\^|])", "\\\\$1");
    }

    /**
     * Prints the usage.
     */
    private void printUsage() {
        logger.info("Usage: java -jar htmlcompressor.jar [options] [input]\n\n"

                + "[input]                        URL, filename, directory, or space separated list\n"
                + "                               of files and directories to compress.\n"
                + "                               If none provided reads from <stdin>\n\n"

                + "Global Options:\n" + " -?, /?, -h, --help            Displays this help screen\n"
                + " -t, --type <html|xml>         If not provided autodetects from file extension\n"
                + " -r, --recursive               Process files inside subdirectories\n"
                + " -c, --charset <charset>       Charset for reading files, UTF-8 by default\n"
                + " -m, --mask <filemask>         Filter input files inside directories by mask\n"
                + " -o, --output <path>           Filename or directory for compression results.\n"
                + "                               If none provided outputs result to <stdout>\n"
                + " -a, --analyze                 Tries different settings and displays report.\n"
                + "                               All settings except --js-compressor are ignored\n\n"

                + "XML Compression Options:\n" + " --preserve-comments           Preserve comments\n"
                + " --preserve-intertag-spaces    Preserve intertag spaces\n\n"

                + "HTML Compression Options:\n" + " --preserve-comments           Preserve comments\n"
                + " --preserve-multi-spaces       Preserve multiple spaces\n"
                + " --preserve-line-breaks        Preserve line breaks\n"
                + " --remove-intertag-spaces      Remove intertag spaces\n"
                + " --remove-quotes               Remove unneeded quotes\n"
                + " --simple-doctype              Change doctype to <!DOCTYPE html>\n"
                + " --remove-style-attr           Remove TYPE attribute from STYLE tags\n"
                + " --remove-link-attr            Remove TYPE attribute from LINK tags\n"
                + " --remove-script-attr          Remove TYPE and LANGUAGE from SCRIPT tags\n"
                + " --remove-form-attr            Remove METHOD=\"GET\" from FORM tags\n"
                + " --remove-input-attr           Remove TYPE=\"TEXT\" from INPUT tags\n"
                + " --simple-bool-attr            Remove values from boolean tag attributes\n"
                + " --remove-js-protocol          Remove \"javascript:\" from inline event handlers\n"
                + " --remove-http-protocol        Remove \"http:\" from tag attributes\n"
                + " --remove-https-protocol       Remove \"https:\" from tag attributes\n"
                + " --remove-surrounding-spaces <min|max|all|custom_list>\n"
                + "                               Predefined or custom comma separated list of tags\n"
                + " --compress-js                 Enable inline JavaScript compression\n"
                + " --compress-css                Enable inline CSS compression using YUICompressor\n"
                + " --js-compressor <yui|closure> Switch inline JavaScript compressor between\n"
                + "                               YUICompressor (default) and Closure Compiler\n\n"

                + "JavaScript Compression Options for YUI Compressor:\n"
                + " --nomunge                     Minify only, do not obfuscate\n"
                + " --preserve-semi               Preserve all semicolons\n"
                + " --disable-optimizations       Disable all micro optimizations\n"
                + " --line-break <column num>     Insert a line break after the specified column\n\n"

                + "JavaScript Compression Options for Google Closure Compiler:\n"
                + " --closure-opt-level <simple|advanced|whitespace>\n"
                + "                               Sets level of optimization (simple by default)\n"
                + " --closure-externs <file>      Sets custom externs file, repeat for each file\n"
                + " --closure-custom-externs-only Disable default built-in externs\n\n"

                + "CSS Compression Options for YUI Compressor:\n"
                + " --line-break <column num>     Insert a line break after the specified column\n\n"

                + "Custom Block Preservation Options:\n" + " --preserve-php                Preserve <?php ... ?> tags\n"
                + " --preserve-server-script      Preserve <% ... %> tags\n"
                + " --preserve-ssi                Preserve <!--# ... --> tags\n"
                + " -p, --preserve <path>         Read regular expressions that define\n"
                + "                               custom preservation rules from a file\n\n"

                + "Please note that if you enable CSS or JavaScript compression, additional\n"
                + "YUI Compressor or Google Closure Compiler jar files must be present\n"
                + "in the same directory as this jar."

        );
    }

    /**
     * The Class CompressorFileFilter.
     */
    private class CompressorFileFilter implements FileFilter {

        /** The filemask pattern. */
        private Pattern filemaskPattern;

        /** The with dirs. */
        private boolean withDirs;

        /**
         * Instantiates a new compressor file filter.
         *
         * @param type
         *            the type
         * @param filemask
         *            the filemask
         * @param withDirs
         *            the with dirs
         */
        public CompressorFileFilter(String type, String filemask, boolean withDirs) {

            this.withDirs = withDirs;

            if (filemask == null) {
                if (type != null && "xml".equalsIgnoreCase(type)) {
                    filemaskPattern = Pattern.compile("^.*\\.xml$", Pattern.CASE_INSENSITIVE);
                } else {
                    filemaskPattern = Pattern.compile("^.*\\.html?$", Pattern.CASE_INSENSITIVE);
                }
            } else {
                // turn mask into regexp
                filemask = filemask.replaceAll(escRegEx("."), Matcher.quoteReplacement("\\."));
                filemask = filemask.replaceAll(escRegEx("*"), Matcher.quoteReplacement(".*"));
                filemask = filemask.replaceAll(escRegEx("?"), Matcher.quoteReplacement("."));
                filemask = filemask.replaceAll(escRegEx(";"), Matcher.quoteReplacement("$|^"));
                filemask = "^" + filemask + "$";

                filemaskPattern = Pattern.compile(filemask, Pattern.CASE_INSENSITIVE);
            }
        }

        @Override
        public boolean accept(File file) {
            if (!withDirs) {
                // take only matching non-dirs
                if (!file.isDirectory()) {
                    return filemaskPattern.matcher(file.getName()).matches();
                }
            } else {
                // take matching files and dirs
                return file.isDirectory() || filemaskPattern.matcher(file.getName()).matches();
            }
            return false;
        }

    }

}
