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
package com.googlecode.htmlcompressor.compressor;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic JavaScript compressor implementation using <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI
 * Compressor</a> that could be used by {@link HtmlCompressor} for inline JavaScript compression.
 *
 * @see HtmlCompressor#setJavaScriptCompressor(Compressor)
 * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
 */
public class YuiJavaScriptCompressor implements Compressor {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(YuiJavaScriptCompressor.class);

    // YUICompressor default settings

    /** The no munge. */
    private boolean noMunge;

    /** The preserve all semi colons. */
    private boolean preserveAllSemiColons;

    /** The disable optimizations. */
    private boolean disableOptimizations;

    /** The line break. */
    private int lineBreak = -1;

    /** The error reporter. */
    private ErrorReporter errorReporter = new DefaultErrorReporter();

    @Override
    public String compress(String source) {

        StringWriter result = new StringWriter();
        try {
            JavaScriptCompressor compressor = new JavaScriptCompressor(new StringReader(source), errorReporter);
            compressor.compress(result, lineBreak, !noMunge, false, preserveAllSemiColons, disableOptimizations);
        } catch (IOException e) {
            result.write(source);
            logger.error("", e);
        }
        return result.toString();

    }

    /**
     * Default <code>ErrorReporter</code> implementation that uses <code>System.err</code> stream for error reporting.
     * Used by YUI Compressor to log errors during JavaScript compression.
     *
     * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
     * @see <a href="http://www.mozilla.org/rhino/apidocs/org/mozilla/javascript/ErrorReporter.html">ErrorReporter
     *      Interface</a>
     */
    public static class DefaultErrorReporter implements ErrorReporter {

        @Override
        public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
            if (line < 0) {
                logger.error("[WARNING] HtmlCompressor: '{}' during JavaScript compression", message);
            } else {
                logger.error("[WARNING] HtmlCompressor: '{}' at line [{}:{}] during JavaScript compression {}", message,
                        line, lineOffset, lineSource != null ? ": " + lineSource : "");
            }
        }

        @Override
        public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
            if (line < 0) {
                logger.error("[ERROR] HtmlCompressor: '{}' during JavaScript compression", message);
            } else {
                logger.error("[ERROR] HtmlCompressor: '{}' at line [{}:{}] during JavaScript compression {}", message,
                        line, lineOffset, lineSource != null ? ": " + lineSource : "");
            }
        }

        @Override
        public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource,
                int lineOffset) {
            error(message, sourceName, line, lineSource, lineOffset);
            return new EvaluatorException(message);
        }
    }

    /**
     * Returns <code>true</code> if Yahoo YUI Compressor will only minify javascript without obfuscating local symbols.
     * This corresponds to <code>--nomunge</code> command line option.
     *
     * @return <code>nomunge</code> parameter value used for JavaScript compression.
     *
     * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
     */
    public boolean isNoMunge() {
        return noMunge;
    }

    /**
     * Tells Yahoo YUI Compressor to only minify javascript without obfuscating local symbols. This corresponds to
     * <code>--nomunge</code> command line option. This option has effect only if JavaScript compression is enabled.
     * Default is <code>false</code>.
     *
     * @param noMunge
     *            set <code>true</code> to enable <code>nomunge</code> mode
     *
     * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
     */
    public void setNoMunge(boolean noMunge) {
        this.noMunge = noMunge;
    }

    /**
     * Returns <code>true</code> if Yahoo YUI Compressor will preserve unnecessary semicolons during JavaScript
     * compression. This corresponds to <code>--preserve-semi</code> command line option.
     *
     * @return <code>preserve-semi</code> parameter value used for JavaScript compression.
     *
     * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
     */
    public boolean isPreserveAllSemiColons() {
        return preserveAllSemiColons;
    }

    /**
     * Tells Yahoo YUI Compressor to preserve unnecessary semicolons during JavaScript compression. This corresponds to
     * <code>--preserve-semi</code> command line option. This option has effect only if JavaScript compression is
     * enabled. Default is <code>false</code>.
     *
     * @param preserveAllSemiColons
     *            set <code>true</code> to enable <code>preserve-semi</code> mode
     *
     * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
     */
    public void setPreserveAllSemiColons(boolean preserveAllSemiColons) {
        this.preserveAllSemiColons = preserveAllSemiColons;
    }

    /**
     * Returns <code>true</code> if Yahoo YUI Compressor will disable all the built-in micro optimizations during
     * JavaScript compression. This corresponds to <code>--disable-optimizations</code> command line option.
     *
     * @return <code>disable-optimizations</code> parameter value used for JavaScript compression.
     *
     * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
     */
    public boolean isDisableOptimizations() {
        return disableOptimizations;
    }

    /**
     * Tells Yahoo YUI Compressor to disable all the built-in micro optimizations during JavaScript compression. This
     * corresponds to <code>--disable-optimizations</code> command line option. This option has effect only if
     * JavaScript compression is enabled. Default is <code>false</code>.
     *
     * @param disableOptimizations
     *            set <code>true</code> to enable <code>disable-optimizations</code> mode
     *
     * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
     */
    public void setDisableOptimizations(boolean disableOptimizations) {
        this.disableOptimizations = disableOptimizations;
    }

    /**
     * Returns number of symbols per line Yahoo YUI Compressor will use during JavaScript compression. This corresponds
     * to <code>--line-break</code> command line option.
     *
     * @return <code>line-break</code> parameter value used for JavaScript compression.
     *
     * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
     */
    public int getLineBreak() {
        return lineBreak;
    }

    /**
     * Tells Yahoo YUI Compressor to break lines after the specified number of symbols during JavaScript compression.
     * This corresponds to <code>--line-break</code> command line option. This option has effect only if JavaScript
     * compression is enabled. Default is <code>-1</code> to disable line breaks.
     *
     * @param lineBreak
     *            set number of symbols per line
     *
     * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
     */
    public void setLineBreak(int lineBreak) {
        this.lineBreak = lineBreak;
    }

    /**
     * Returns <code>ErrorReporter</code> used by YUI Compressor to log error messages during JavasSript compression.
     *
     * @return <code>ErrorReporter</code> used by YUI Compressor to log error messages during JavasSript compression
     *
     * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
     * @see <a href="http://www.mozilla.org/rhino/apidocs/org/mozilla/javascript/ErrorReporter.html">Error Reporter
     *      Interface</a>
     */
    public ErrorReporter getErrorReporter() {
        return errorReporter;
    }

    /**
     * Sets <code>ErrorReporter</code> that YUI Compressor will use for reporting errors during JavaScript compression.
     * If no <code>ErrorReporter</code> was provided {@link YuiJavaScriptCompressor.DefaultErrorReporter} will be used
     * which reports errors to <code>System.err</code> stream.
     *
     * @param errorReporter
     *            <code>ErrorReporter</code> that will be used by YUI Compressor
     *
     * @see YuiJavaScriptCompressor.DefaultErrorReporter
     * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
     * @see <a href="http://www.mozilla.org/rhino/apidocs/org/mozilla/javascript/ErrorReporter.html">ErrorReporter
     *      Interface</a>
     */
    public void setErrorReporter(ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
    }

}
