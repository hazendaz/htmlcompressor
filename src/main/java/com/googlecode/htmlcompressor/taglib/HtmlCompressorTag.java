/**
 *    Copyright 2009-2018 the original author or authors.
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
package com.googlecode.htmlcompressor.taglib;

import com.google.javascript.jscomp.CompilationLevel;
import com.googlecode.htmlcompressor.compressor.ClosureJavaScriptCompressor;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import com.googlecode.htmlcompressor.compressor.YuiJavaScriptCompressor;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSP tag that compresses an HTML content within &lt;compress:html&gt;. Compression parameters are set by default (no
 * JavaScript and CSS compression).
 *
 * @author <a href="mailto:serg472@gmail.com">Sergiy Kovalchuk</a>
 * @see HtmlCompressor
 */
public class HtmlCompressorTag extends BodyTagSupport {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(HtmlCompressorTag.class);

    /** The enabled. */
    private boolean enabled = true;

    // default settings

    /** The remove comments. */
    private boolean removeComments = true;

    /** The remove multi spaces. */
    private boolean removeMultiSpaces = true;

    // optional settings

    /** The remove intertag spaces. */
    private boolean removeIntertagSpaces;

    /** The remove quotes. */
    private boolean removeQuotes;

    /** The preserve line breaks. */
    private boolean preserveLineBreaks;

    /** The simple doctype. */
    private boolean simpleDoctype;

    /** The remove script attributes. */
    private boolean removeScriptAttributes;

    /** The remove style attributes. */
    private boolean removeStyleAttributes;

    /** The remove link attributes. */
    private boolean removeLinkAttributes;

    /** The remove form attributes. */
    private boolean removeFormAttributes;

    /** The remove input attributes. */
    private boolean removeInputAttributes;

    /** The simple boolean attributes. */
    private boolean simpleBooleanAttributes;

    /** The remove java script protocol. */
    private boolean removeJavaScriptProtocol;

    /** The remove http protocol. */
    private boolean removeHttpProtocol;

    /** The remove https protocol. */
    private boolean removeHttpsProtocol;

    /** The compress java script. */
    private boolean compressJavaScript;

    /** The compress css. */
    private boolean compressCss;

    /** The js compressor. */
    private String jsCompressor = HtmlCompressor.JS_COMPRESSOR_YUI;

    // YUICompressor settings

    /** The yui js no munge. */
    private boolean yuiJsNoMunge;

    /** The yui js preserve all semi colons. */
    private boolean yuiJsPreserveAllSemiColons;

    /** The yui js disable optimizations. */
    private boolean yuiJsDisableOptimizations;

    /** The yui js line break. */
    private int yuiJsLineBreak = -1;

    /** The yui css line break. */
    private int yuiCssLineBreak = -1;

    // Closure compressor settings

    /** The closure opt level. */
    private String closureOptLevel = ClosureJavaScriptCompressor.COMPILATION_LEVEL_SIMPLE;

    @Override
    public int doEndTag() throws JspException {

        BodyContent bodyContent = getBodyContent();
        String content = bodyContent.getString();

        HtmlCompressor htmlCompressor = new HtmlCompressor();
        htmlCompressor.setEnabled(enabled);
        htmlCompressor.setRemoveComments(removeComments);
        htmlCompressor.setRemoveMultiSpaces(removeMultiSpaces);
        htmlCompressor.setRemoveIntertagSpaces(removeIntertagSpaces);
        htmlCompressor.setRemoveQuotes(removeQuotes);
        htmlCompressor.setPreserveLineBreaks(preserveLineBreaks);
        htmlCompressor.setCompressJavaScript(compressJavaScript);
        htmlCompressor.setCompressCss(compressCss);
        htmlCompressor.setYuiJsNoMunge(yuiJsNoMunge);
        htmlCompressor.setYuiJsPreserveAllSemiColons(yuiJsPreserveAllSemiColons);
        htmlCompressor.setYuiJsDisableOptimizations(yuiJsDisableOptimizations);
        htmlCompressor.setYuiJsLineBreak(yuiJsLineBreak);
        htmlCompressor.setYuiCssLineBreak(yuiCssLineBreak);

        htmlCompressor.setSimpleDoctype(simpleDoctype);
        htmlCompressor.setRemoveScriptAttributes(removeScriptAttributes);
        htmlCompressor.setRemoveStyleAttributes(removeStyleAttributes);
        htmlCompressor.setRemoveLinkAttributes(removeLinkAttributes);
        htmlCompressor.setRemoveFormAttributes(removeFormAttributes);
        htmlCompressor.setRemoveInputAttributes(removeInputAttributes);
        htmlCompressor.setSimpleBooleanAttributes(simpleBooleanAttributes);
        htmlCompressor.setRemoveJavaScriptProtocol(removeJavaScriptProtocol);
        htmlCompressor.setRemoveHttpProtocol(removeHttpProtocol);
        htmlCompressor.setRemoveHttpsProtocol(removeHttpsProtocol);

        if (compressJavaScript && jsCompressor.equalsIgnoreCase(HtmlCompressor.JS_COMPRESSOR_CLOSURE)) {
            ClosureJavaScriptCompressor closureCompressor = new ClosureJavaScriptCompressor();
            if (closureOptLevel.equalsIgnoreCase(ClosureJavaScriptCompressor.COMPILATION_LEVEL_ADVANCED)) {
                closureCompressor.setCompilationLevel(CompilationLevel.ADVANCED_OPTIMIZATIONS);
            } else if (closureOptLevel.equalsIgnoreCase(ClosureJavaScriptCompressor.COMPILATION_LEVEL_WHITESPACE)) {
                closureCompressor.setCompilationLevel(CompilationLevel.WHITESPACE_ONLY);
            } else {
                closureCompressor.setCompilationLevel(CompilationLevel.SIMPLE_OPTIMIZATIONS);
            }
            htmlCompressor.setJavaScriptCompressor(closureCompressor);
        }

        try {
            bodyContent.clear();
            bodyContent.append(htmlCompressor.compress(content));
            bodyContent.writeOut(pageContext.getOut());
        } catch (IOException e) {
            logger.error("", e);
        }

        return super.doEndTag();
    }

    /**
     * Sets the compress java script.
     *
     * @param compressJavaScript
     *            the new compress java script
     * @see HtmlCompressor#setCompressJavaScript(boolean)
     */
    public void setCompressJavaScript(boolean compressJavaScript) {
        this.compressJavaScript = compressJavaScript;
    }

    /**
     * Sets the compress css.
     *
     * @param compressCss
     *            the new compress css
     * @see HtmlCompressor#setCompressCss(boolean)
     */
    public void setCompressCss(boolean compressCss) {
        this.compressCss = compressCss;
    }

    /**
     * Sets the yui js no munge.
     *
     * @param yuiJsNoMunge
     *            the new yui js no munge
     * @see HtmlCompressor#setYuiJsNoMunge(boolean)
     */
    public void setYuiJsNoMunge(boolean yuiJsNoMunge) {
        this.yuiJsNoMunge = yuiJsNoMunge;
    }

    /**
     * Sets the yui js preserve all semi colons.
     *
     * @param yuiJsPreserveAllSemiColons
     *            the new yui js preserve all semi colons
     * @see HtmlCompressor#setYuiJsPreserveAllSemiColons(boolean)
     */
    public void setYuiJsPreserveAllSemiColons(boolean yuiJsPreserveAllSemiColons) {
        this.yuiJsPreserveAllSemiColons = yuiJsPreserveAllSemiColons;
    }

    /**
     * Sets the yui js disable optimizations.
     *
     * @param yuiJsDisableOptimizations
     *            the new yui js disable optimizations
     * @see HtmlCompressor#setYuiJsDisableOptimizations(boolean)
     */
    public void setYuiJsDisableOptimizations(boolean yuiJsDisableOptimizations) {
        this.yuiJsDisableOptimizations = yuiJsDisableOptimizations;
    }

    /**
     * Sets the yui js line break.
     *
     * @param yuiJsLineBreak
     *            the new yui js line break
     * @see HtmlCompressor#setYuiJsLineBreak(int)
     */
    public void setYuiJsLineBreak(int yuiJsLineBreak) {
        this.yuiJsLineBreak = yuiJsLineBreak;
    }

    /**
     * Sets the yui css line break.
     *
     * @param yuiCssLineBreak
     *            the new yui css line break
     * @see HtmlCompressor#setYuiCssLineBreak(int)
     */
    public void setYuiCssLineBreak(int yuiCssLineBreak) {
        this.yuiCssLineBreak = yuiCssLineBreak;
    }

    /**
     * Sets the removes the quotes.
     *
     * @param removeQuotes
     *            the new removes the quotes
     * @see HtmlCompressor#setRemoveQuotes(boolean)
     */
    public void setRemoveQuotes(boolean removeQuotes) {
        this.removeQuotes = removeQuotes;
    }

    /**
     * Sets the preserve line breaks.
     *
     * @param preserveLineBreaks
     *            the new preserve line breaks
     * @see HtmlCompressor#setPreserveLineBreaks(boolean)
     */
    public void setPreserveLineBreaks(boolean preserveLineBreaks) {
        this.preserveLineBreaks = preserveLineBreaks;
    }

    /**
     * Sets the enabled.
     *
     * @param enabled
     *            the new enabled
     * @see HtmlCompressor#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Sets the removes the comments.
     *
     * @param removeComments
     *            the new removes the comments
     * @see HtmlCompressor#setRemoveComments(boolean)
     */
    public void setRemoveComments(boolean removeComments) {
        this.removeComments = removeComments;
    }

    /**
     * Sets the removes the multi spaces.
     *
     * @param removeMultiSpaces
     *            the new removes the multi spaces
     * @see HtmlCompressor#setRemoveMultiSpaces(boolean)
     */
    public void setRemoveMultiSpaces(boolean removeMultiSpaces) {
        this.removeMultiSpaces = removeMultiSpaces;
    }

    /**
     * Sets the removes the intertag spaces.
     *
     * @param removeIntertagSpaces
     *            the new removes the intertag spaces
     * @see HtmlCompressor#setRemoveIntertagSpaces(boolean)
     */
    public void setRemoveIntertagSpaces(boolean removeIntertagSpaces) {
        this.removeIntertagSpaces = removeIntertagSpaces;
    }

    /**
     * Sets JavaScript compressor implementation that will be used to compress inline JavaScript in HTML.
     * 
     * @param jsCompressor
     *            Could be either <code>"yui"</code> for using {@link YuiJavaScriptCompressor} (used by default if none
     *            provided) or <code>"closure"</code> for using {@link ClosureJavaScriptCompressor}
     * 
     * @see YuiJavaScriptCompressor
     * @see ClosureJavaScriptCompressor
     * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
     * @see <a href="http://code.google.com/closure/compiler/">Google Closure Compiler</a>
     */
    public void setJsCompressor(String jsCompressor) {
        this.jsCompressor = jsCompressor;
    }

    /**
     * Sets level of optimization if <a href="http://code.google.com/closure/compiler/">Google Closure Compiler</a> is
     * used for compressing inline JavaScript.
     * 
     * @param closureOptLevel
     *            Could be either <code>"simple"</code> (used by default), <code>"whitespace"</code> or
     *            <code>"advanced"</code>
     * 
     * @see ClosureJavaScriptCompressor#setCompilationLevel(CompilationLevel)
     */
    public void setClosureOptLevel(String closureOptLevel) {
        this.closureOptLevel = closureOptLevel;
    }

    /**
     * Sets the simple doctype.
     *
     * @param simpleDoctype
     *            the new simple doctype
     * @see HtmlCompressor#setSimpleDoctype(boolean)
     */
    public void setSimpleDoctype(boolean simpleDoctype) {
        this.simpleDoctype = simpleDoctype;
    }

    /**
     * Sets the removes the script attributes.
     *
     * @param removeScriptAttributes
     *            the new removes the script attributes
     * @see HtmlCompressor#setRemoveScriptAttributes(boolean)
     */
    public void setRemoveScriptAttributes(boolean removeScriptAttributes) {
        this.removeScriptAttributes = removeScriptAttributes;
    }

    /**
     * Sets the removes the style attributes.
     *
     * @param removeStyleAttributes
     *            the new removes the style attributes
     * @see HtmlCompressor#setRemoveStyleAttributes(boolean)
     */
    public void setRemoveStyleAttributes(boolean removeStyleAttributes) {
        this.removeStyleAttributes = removeStyleAttributes;
    }

    /**
     * Sets the removes the link attributes.
     *
     * @param removeLinkAttributes
     *            the new removes the link attributes
     * @see HtmlCompressor#setRemoveLinkAttributes(boolean)
     */
    public void setRemoveLinkAttributes(boolean removeLinkAttributes) {
        this.removeLinkAttributes = removeLinkAttributes;
    }

    /**
     * Sets the removes the form attributes.
     *
     * @param removeFormAttributes
     *            the new removes the form attributes
     * @see HtmlCompressor#setRemoveFormAttributes(boolean)
     */
    public void setRemoveFormAttributes(boolean removeFormAttributes) {
        this.removeFormAttributes = removeFormAttributes;
    }

    /**
     * Sets the removes the input attributes.
     *
     * @param removeInputAttributes
     *            the new removes the input attributes
     * @see HtmlCompressor#setRemoveInputAttributes(boolean)
     */
    public void setRemoveInputAttributes(boolean removeInputAttributes) {
        this.removeInputAttributes = removeInputAttributes;
    }

    /**
     * Sets the simple boolean attributes.
     *
     * @param simpleBooleanAttributes
     *            the new simple boolean attributes
     * @see HtmlCompressor#setSimpleBooleanAttributes(boolean)
     */
    public void setSimpleBooleanAttributes(boolean simpleBooleanAttributes) {
        this.simpleBooleanAttributes = simpleBooleanAttributes;
    }

    /**
     * Sets the removes the java script protocol.
     *
     * @param removeJavaScriptProtocol
     *            the new removes the java script protocol
     * @see HtmlCompressor#setRemoveJavaScriptProtocol(boolean)
     */
    public void setRemoveJavaScriptProtocol(boolean removeJavaScriptProtocol) {
        this.removeJavaScriptProtocol = removeJavaScriptProtocol;
    }

    /**
     * Sets the removes the http protocol.
     *
     * @param removeHttpProtocol
     *            the new removes the http protocol
     * @see HtmlCompressor#setRemoveHttpProtocol(boolean)
     */
    public void setRemoveHttpProtocol(boolean removeHttpProtocol) {
        this.removeHttpProtocol = removeHttpProtocol;
    }

    /**
     * Sets the removes the https protocol.
     *
     * @param removeHttpsProtocol
     *            the new removes the https protocol
     * @see HtmlCompressor#setRemoveHttpsProtocol(boolean)
     */
    public void setRemoveHttpsProtocol(boolean removeHttpsProtocol) {
        this.removeHttpsProtocol = removeHttpsProtocol;
    }

}
