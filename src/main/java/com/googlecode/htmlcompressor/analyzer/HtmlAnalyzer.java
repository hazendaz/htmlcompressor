/*
 *    Copyright 2009-2025 the original author or authors.
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
package com.googlecode.htmlcompressor.analyzer;

import com.googlecode.htmlcompressor.compressor.ClosureJavaScriptCompressor;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;

import java.text.NumberFormat;
import java.util.Formatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that compresses provided source with different compression settings and displays page size gains in a report.
 */
public class HtmlAnalyzer {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(HtmlAnalyzer.class);

    /** The js compressor. */
    private String jsCompressor = HtmlCompressor.JS_COMPRESSOR_YUI;

    /**
     * Instantiates a new html analyzer.
     */
    public HtmlAnalyzer() {
        // Required for override
    }

    /**
     * Instantiates a new html analyzer.
     *
     * @param jsCompressor
     *            the js compressor
     */
    public HtmlAnalyzer(String jsCompressor) {
        this.jsCompressor = jsCompressor;
    }

    /**
     * Analyze.
     *
     * @param source
     *            the source
     */
    public void analyze(String source) {
        int originalSize = source.length();

        HtmlCompressor compressor = getCleanCompressor();
        String compResult = compressor.compress(source);

        printHeader();

        if (logger.isInfoEnabled()) {
            logger.info(formatLine("Compression disabled", originalSize, originalSize, originalSize));
        }
        int prevSize = originalSize;

        // spaces inside tags
        if (logger.isInfoEnabled()) {
            logger.info(formatLine("All settings disabled", originalSize, compResult.length(), prevSize));
        }
        prevSize = compResult.length();

        // remove comments
        compressor.setRemoveComments(true);
        compResult = compressor.compress(source);
        if (logger.isInfoEnabled()) {
            logger.info(formatLine("Comments removed", originalSize, compResult.length(), prevSize));
        }
        prevSize = compResult.length();

        // remove mulispaces
        compressor.setRemoveMultiSpaces(true);
        compResult = compressor.compress(source);
        if (logger.isInfoEnabled()) {
            logger.info(formatLine("Multiple spaces removed", originalSize, compResult.length(), prevSize));
        }
        prevSize = compResult.length();

        // remove intertag spaces
        compressor.setRemoveIntertagSpaces(true);
        compResult = compressor.compress(source);
        if (logger.isInfoEnabled()) {
            logger.info(formatLine("No spaces between tags", originalSize, compResult.length(), prevSize));
        }
        prevSize = compResult.length();

        // remove min surrounding spaces
        compressor.setRemoveSurroundingSpaces(HtmlCompressor.BLOCK_TAGS_MIN);
        compResult = compressor.compress(source);
        if (logger.isInfoEnabled()) {
            logger.info(formatLine("No surround spaces (min)", originalSize, compResult.length(), prevSize));
        }
        prevSize = compResult.length();

        // remove max surrounding spaces
        compressor.setRemoveSurroundingSpaces(HtmlCompressor.BLOCK_TAGS_MAX);
        compResult = compressor.compress(source);
        if (logger.isInfoEnabled()) {
            logger.info(formatLine("No surround spaces (max)", originalSize, compResult.length(), prevSize));
        }
        prevSize = compResult.length();

        // remove all surrounding spaces
        compressor.setRemoveSurroundingSpaces(HtmlCompressor.ALL_TAGS);
        compResult = compressor.compress(source);
        if (logger.isInfoEnabled()) {
            logger.info(formatLine("No surround spaces (all)", originalSize, compResult.length(), prevSize));
        }
        prevSize = compResult.length();

        // remove quotes
        compressor.setRemoveQuotes(true);
        compResult = compressor.compress(source);
        if (logger.isInfoEnabled()) {
            logger.info(formatLine("Quotes removed from tags", originalSize, compResult.length(), prevSize));
        }
        prevSize = compResult.length();

        // link attrib
        compressor.setRemoveLinkAttributes(true);
        compResult = compressor.compress(source);
        if (logger.isInfoEnabled()) {
            logger.info(formatLine("<link> attr. removed", originalSize, compResult.length(), prevSize));
        }
        prevSize = compResult.length();

        // style attrib
        compressor.setRemoveStyleAttributes(true);
        compResult = compressor.compress(source);
        if (logger.isInfoEnabled()) {
            logger.info(formatLine("<style> attr. removed", originalSize, compResult.length(), prevSize));
        }
        prevSize = compResult.length();

        // script attrib
        compressor.setRemoveScriptAttributes(true);
        compResult = compressor.compress(source);
        if (logger.isInfoEnabled()) {
            logger.info(formatLine("<script> attr. removed", originalSize, compResult.length(), prevSize));
        }
        prevSize = compResult.length();

        // form attrib
        compressor.setRemoveFormAttributes(true);
        compResult = compressor.compress(source);
        if (logger.isInfoEnabled()) {
            logger.info(formatLine("<form> attr. removed", originalSize, compResult.length(), prevSize));
        }
        prevSize = compResult.length();

        // input attrib
        compressor.setRemoveInputAttributes(true);
        compResult = compressor.compress(source);
        if (logger.isInfoEnabled()) {
            logger.info(formatLine("<input> attr. removed", originalSize, compResult.length(), prevSize));
        }
        prevSize = compResult.length();

        // simple bool
        compressor.setSimpleBooleanAttributes(true);
        compResult = compressor.compress(source);
        if (logger.isInfoEnabled()) {
            logger.info(formatLine("Simple boolean attributes", originalSize, compResult.length(), prevSize));
        }
        prevSize = compResult.length();

        // simple doctype
        compressor.setSimpleDoctype(true);
        compResult = compressor.compress(source);
        if (logger.isInfoEnabled()) {
            logger.info(formatLine("Simple doctype", originalSize, compResult.length(), prevSize));
        }
        prevSize = compResult.length();

        // js protocol
        compressor.setRemoveJavaScriptProtocol(true);
        compResult = compressor.compress(source);
        if (logger.isInfoEnabled()) {
            logger.info(formatLine("Remove js pseudo-protocol", originalSize, compResult.length(), prevSize));
        }
        prevSize = compResult.length();

        // http protocol
        compressor.setRemoveHttpProtocol(true);
        compResult = compressor.compress(source);
        if (logger.isInfoEnabled()) {
            logger.info(formatLine("Remove http protocol", originalSize, compResult.length(), prevSize));
        }
        prevSize = compResult.length();

        // https protocol
        compressor.setRemoveHttpsProtocol(true);
        compResult = compressor.compress(source);
        if (logger.isInfoEnabled()) {
            logger.info(formatLine("Remove https protocol", originalSize, compResult.length(), prevSize));
        }
        prevSize = compResult.length();

        // inline css
        try {
            compressor.setCompressCss(true);
            compResult = compressor.compress(source);
            if (logger.isInfoEnabled()) {
                logger.info(formatLine("Compress inline CSS (YUI)", originalSize, compResult.length(), prevSize));
            }
            prevSize = compResult.length();
        } catch (NoClassDefFoundError e) {
            logger.info(formatEmptyLine("Compress inline CSS (YUI)"));
            logger.trace("", e);
        }

        if (jsCompressor.equals(HtmlCompressor.JS_COMPRESSOR_YUI)) {
            // inline js yui
            try {
                compressor.setCompressJavaScript(true);
                compResult = compressor.compress(source);
                if (logger.isInfoEnabled()) {
                    logger.info(formatLine("Compress inline JS (YUI)", originalSize, compResult.length(), prevSize));
                }
            } catch (NoClassDefFoundError e) {
                logger.info(formatEmptyLine("Compress inline JS (YUI)"));
                logger.trace("", e);
            }
        } else {
            // inline js yui
            try {
                compressor.setCompressJavaScript(true);
                compressor.setJavaScriptCompressor(new ClosureJavaScriptCompressor());
                compResult = compressor.compress(source);
                if (logger.isInfoEnabled()) {
                    logger.info(formatLine("Compress JS (Closure)", originalSize, compResult.length(), prevSize));
                }
            } catch (NoClassDefFoundError e) {
                logger.info(formatEmptyLine("Compress JS (Closure)"));
                logger.trace("", e);
            }
        }

        printFooter();

    }

    /**
     * Gets the clean compressor.
     *
     * @return the clean compressor
     */
    private HtmlCompressor getCleanCompressor() {
        HtmlCompressor compressor = new HtmlCompressor();
        compressor.setRemoveComments(false);
        compressor.setRemoveMultiSpaces(false);

        return compressor;
    }

    /**
     * Format line.
     *
     * @param descr
     *            the descr
     * @param originalSize
     *            the original size
     * @param compressedSize
     *            the compressed size
     * @param prevSize
     *            the prev size
     *
     * @return the string
     */
    private String formatLine(String descr, int originalSize, int compressedSize, int prevSize) {
        String result;
        try (Formatter fmt = new Formatter()) {
            fmt.format("%-25s | %16s | %16s | %12s |", descr, formatDecrease(prevSize, compressedSize),
                    formatDecrease(originalSize, compressedSize), formatSize(compressedSize));
            result = fmt.toString();
        }
        return result;
    }

    /**
     * Format empty line.
     *
     * @param descr
     *            the descr
     *
     * @return the string
     */
    private String formatEmptyLine(String descr) {
        String result;
        try (Formatter fmt = new Formatter()) {
            fmt.format("%-25s | %16s | %16s | %12s |", descr, "-", "-", "-");
            result = fmt.toString();
        }
        return result;
    }

    /**
     * Prints the header.
     */
    private void printHeader() {
        if (logger.isInfoEnabled()) {
            logger.info("\n");
            logger.info("=".repeat(80));
            logger.info(String.format("%-25s | %-16s | %-16s | %-12s |", "         Setting", "Incremental Gain",
                    "   Total Gain", " Page Size"));
            logger.info("\n");
            logger.info("=".repeat(80));
        }
    }

    /**
     * Prints the footer.
     */
    private void printFooter() {
        if (logger.isInfoEnabled()) {
            logger.info("=".repeat(80));
            logger.info("\n");
            logger.info("Each consecutive compressor setting is applied on top of previous ones.");
            logger.info("In order to see JS and CSS compression results, YUI jar file must be present.");
            logger.info("All sizes are in bytes.");
        }
    }

    /**
     * Format decrease.
     *
     * @param originalSize
     *            the original size
     * @param compressedSize
     *            the compressed size
     *
     * @return the string
     */
    private String formatDecrease(int originalSize, int compressedSize) {
        NumberFormat nf = NumberFormat.getPercentInstance();
        nf.setGroupingUsed(true);
        nf.setMinimumFractionDigits(1);
        nf.setMaximumFractionDigits(1);

        return formatSize(originalSize - compressedSize) + " (" + nf.format(1 - (double) compressedSize / originalSize)
                + ")";
    }

    /**
     * Format size.
     *
     * @param size
     *            the size
     *
     * @return the string
     */
    private String formatSize(int size) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(true);
        nf.setParseIntegerOnly(true);
        return nf.format(size);
    }
}
