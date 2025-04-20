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

import com.google.common.io.ByteStreams;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.WarningLevel;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic JavaScript compressor implementation using <a href="http://code.google.com/closure/compiler/">Google Closure
 * Compiler</a> that could be used by {@link HtmlCompressor} for inline JavaScript compression.
 *
 * @author <a href="mailto:serg472@gmail.com">Sergiy Kovalchuk</a>
 *
 * @see HtmlCompressor#setJavaScriptCompressor(Compressor)
 * @see <a href="http://code.google.com/closure/compiler/">Google Closure Compiler</a>
 */
public class ClosureJavaScriptCompressor implements Compressor {

    /** The constant LOGGER. */
    private static final Logger logger = LoggerFactory.getLogger(ClosureJavaScriptCompressor.class);

    /** The Constant COMPILATION_LEVEL_SIMPLE. */
    public static final String COMPILATION_LEVEL_SIMPLE = "simple";

    /** The Constant COMPILATION_LEVEL_ADVANCED. */
    public static final String COMPILATION_LEVEL_ADVANCED = "advanced";

    /** The Constant COMPILATION_LEVEL_WHITESPACE. */
    public static final String COMPILATION_LEVEL_WHITESPACE = "whitespace";

    // Closure compiler default settings

    /** The compiler options. */
    private CompilerOptions compilerOptions = new CompilerOptions();

    /** The compilation level. */
    private CompilationLevel compilationLevel = CompilationLevel.SIMPLE_OPTIMIZATIONS;

    /** The logging level (note: closure compiler still uses java util logging, don't import Level. */
    private java.util.logging.Level loggingLevel = java.util.logging.Level.SEVERE;

    /** The warning level. */
    private WarningLevel warningLevel = WarningLevel.DEFAULT;

    /** The custom externs only. */
    private boolean customExternsOnly;

    /** The externs. */
    private List<SourceFile> externs;

    /**
     * Instantiates a new closure java script compressor.
     */
    public ClosureJavaScriptCompressor() {
        // Required for override.
    }

    /**
     * Instantiates a new closure java script compressor.
     *
     * @param compilationLevel
     *            the compilation level
     */
    public ClosureJavaScriptCompressor(CompilationLevel compilationLevel) {
        this.compilationLevel = compilationLevel;
    }

    @Override
    public String compress(String source) {

        StringWriter writer = new StringWriter();

        // prepare source
        List<SourceFile> input = new ArrayList<>();
        input.add(SourceFile.fromCode("source.js", source));

        // prepare externs
        List<SourceFile> externsList = new ArrayList<>();
        if (compilationLevel.equals(CompilationLevel.ADVANCED_OPTIMIZATIONS)) {
            // default externs
            if (!customExternsOnly) {
                try {
                    externsList = getDefaultExterns();
                } catch (IOException e) {
                    logger.error("", e);
                }
            }
            // add user defined externs
            if (externs != null) {
                for (SourceFile extern : externs) {
                    externsList.add(extern);
                }
            }
            // add empty externs
            if (externsList.isEmpty()) {
                externsList.add(SourceFile.fromCode("externs.js", ""));
            }
        } else {
            // empty externs
            externsList.add(SourceFile.fromCode("externs.js", ""));
        }

        Compiler.setLoggingLevel(loggingLevel);

        Compiler compiler = new Compiler();
        compiler.disableThreads();

        compilationLevel.setOptionsForCompilationLevel(compilerOptions);
        warningLevel.setOptionsForWarningLevel(compilerOptions);

        Result result = compiler.compile(externsList, input, compilerOptions);

        if (result.success) {
            writer.write(compiler.toSource());
        } else {
            writer.write(source);
        }

        return writer.toString();

    }

    /**
     * Gets the default externs.
     *
     * @return the default externs
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    // read default externs from closure.jar
    private List<SourceFile> getDefaultExterns() throws IOException {
        InputStream input = ClosureJavaScriptCompressor.class.getResourceAsStream("/externs.zip");
        List<SourceFile> externList = new ArrayList<>();
        try (ZipInputStream zip = new ZipInputStream(input)) {
            for (ZipEntry entry; (entry = zip.getNextEntry()) != null;) {
                externList.add(SourceFile.builder().withCharset(Charset.defaultCharset())
                        .withContent(ByteStreams.limit(zip, entry.getSize())).withPath(entry.getName()).build());
            }
        }
        return externList;
    }

    /**
     * Returns level of optimization that is applied when compiling JavaScript code.
     *
     * @return <code>CompilationLevel</code> that is applied when compiling JavaScript code.
     *
     * @see <a href=
     *      "http://closure-compiler.googlecode.com/svn/trunk/javadoc/com/google/javascript/jscomp/CompilationLevel.html">CompilationLevel</a>
     */
    public CompilationLevel getCompilationLevel() {
        return compilationLevel;
    }

    /**
     * Sets level of optimization that should be applied when compiling JavaScript code. If none is provided,
     * <code>CompilationLevel.SIMPLE_OPTIMIZATIONS</code> will be used by default.
     * <p>
     * <b>Warning:</b> Using <code>CompilationLevel.ADVANCED_OPTIMIZATIONS</code> could break inline JavaScript if
     * externs are not set properly.
     *
     * @param compilationLevel
     *            Optimization level to use, could be set to <code>CompilationLevel.ADVANCED_OPTIMIZATIONS</code>,
     *            <code>CompilationLevel.SIMPLE_OPTIMIZATIONS</code>, <code>CompilationLevel.WHITESPACE_ONLY</code>
     *
     * @see <a href="http://code.google.com/closure/compiler/docs/api-tutorial3.html">Advanced Compilation and
     *      Externs</a>
     * @see <a href="http://code.google.com/closure/compiler/docs/compilation_levels.html">Closure Compiler Compilation
     *      Levels</a>
     * @see <a href=
     *      "http://closure-compiler.googlecode.com/svn/trunk/javadoc/com/google/javascript/jscomp/CompilationLevel.html">CompilationLevel</a>
     */
    public void setCompilationLevel(CompilationLevel compilationLevel) {
        this.compilationLevel = compilationLevel;
    }

    /**
     * Returns options that are used by the Closure compiler.
     *
     * @return <code>CompilerOptions</code> that are used by the compiler
     *
     * @see <a href=
     *      "http://closure-compiler.googlecode.com/svn/trunk/javadoc/com/google/javascript/jscomp/CompilerOptions.html">CompilerOptions</a>
     */
    public CompilerOptions getCompilerOptions() {
        return compilerOptions;
    }

    /**
     * Sets options that will be used by the Closure compiler. If none is provided, default options constructor will be
     * used: <code>new CompilerOptions()</code>.
     *
     * @param compilerOptions
     *            <code>CompilerOptions</code> that will be used by the compiler
     *
     * @see <a href=
     *      "http://closure-compiler.googlecode.com/svn/trunk/javadoc/com/google/javascript/jscomp/CompilerOptions.html">CompilerOptions</a>
     */
    public void setCompilerOptions(CompilerOptions compilerOptions) {
        this.compilerOptions = compilerOptions;
    }

    /**
     * Returns logging level used by the Closure compiler (note: closure compiler still uses java util logging, don't
     * import Level).
     *
     * @return <code>Level</code> of logging used by the Closure compiler
     */
    public java.util.logging.Level getLoggingLevel() {
        return loggingLevel;
    }

    /**
     * Sets logging level for the Closure compiler (note: closure compiler still uses java util logging, don't import
     * Level).
     *
     * @param loggingLevel
     *            logging level for the Closure compiler.
     *
     * @see java.util.logging.Level
     */
    public void setLoggingLevel(java.util.logging.Level loggingLevel) {
        this.loggingLevel = loggingLevel;
    }

    /**
     * Returns <code>SourceFile</code> used as a reference during the compression at
     * <code>CompilationLevel.ADVANCED_OPTIMIZATIONS</code> level.
     *
     * @return <code>SourceFile</code> used as a reference during compression
     */
    public List<SourceFile> getExterns() {
        return externs;
    }

    /**
     * Sets external JavaScript files that are used as a reference for function declarations if
     * <code>CompilationLevel.ADVANCED_OPTIMIZATIONS</code> compression level is used.
     * <p>
     * A number of default externs defined inside Closure's jar will be used besides user defined ones, to use only user
     * defined externs set {@link #setCustomExternsOnly(boolean) setCustomExternsOnly(true)}
     * <p>
     * <b>Warning:</b> Using <code>CompilationLevel.ADVANCED_OPTIMIZATIONS</code> could break inline JavaScript if
     * externs are not set properly.
     *
     * @param externs
     *            <code>SourceFile</code> to use as a reference during compression
     *
     * @see #setCompilationLevel(CompilationLevel)
     * @see #setCustomExternsOnly(boolean)
     * @see <a href="http://code.google.com/closure/compiler/docs/api-tutorial3.html">Advanced Compilation and
     *      Externs</a>
     * @see <a href=
     *      "http://closure-compiler.googlecode.com/svn/trunk/javadoc/com/google/javascript/jscomp/SourceFile.html">SourceFile</a>
     */
    public void setExterns(List<SourceFile> externs) {
        this.externs = externs;
    }

    /**
     * Returns <code>WarningLevel</code> used by the Closure compiler.
     *
     * @return <code>WarningLevel</code> used by the Closure compiler
     */
    public WarningLevel getWarningLevel() {
        return warningLevel;
    }

    /**
     * Indicates the amount of information you want from the compiler about possible problems in your code.
     *
     * @param warningLevel
     *            <code>WarningLevel</code> to use
     *
     * @see <a href="http://code.google.com/closure/compiler/docs/api-ref.html">Google Closure Compiler</a>
     */
    public void setWarningLevel(WarningLevel warningLevel) {
        this.warningLevel = warningLevel;
    }

    /**
     * Returns <code>true</code> if default externs defined inside Closure's jar are ignored and only user defined ones
     * are used.
     *
     * @return <code>true</code> if default externs defined inside Closure's jar are ignored and only user defined ones
     *         are used
     */
    public boolean isCustomExternsOnly() {
        return customExternsOnly;
    }

    /**
     * If set to <code>true</code>, default externs defined inside Closure's jar will be ignored and only user defined
     * ones will be used.
     *
     * @param customExternsOnly
     *            <code>true</code> to skip default externs and use only user defined ones
     *
     * @see #setExterns(List)
     * @see #setCompilationLevel(CompilationLevel)
     */
    public void setCustomExternsOnly(boolean customExternsOnly) {
        this.customExternsOnly = customExternsOnly;
    }

}
