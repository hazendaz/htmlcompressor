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
package com.googlecode.htmlcompressor.taglib;

import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import com.googlecode.htmlcompressor.compressor.YuiCssCompressor;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSP tag that compresses an CSS content within &lt;compress:css&gt; using
 * <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>. All CSS-related properties from
 * {@link HtmlCompressor} are supported.
 *
 * @author <a href="mailto:serg472@gmail.com">Sergiy Kovalchuk</a>
 *
 * @see HtmlCompressor
 * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
 */
@SuppressWarnings("serial")
public class CssCompressorTag extends BodyTagSupport {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(CssCompressorTag.class);

    /** The enabled. */
    private boolean enabled = true;

    // YUICompressor settings

    /** The yui css line break. */
    private int yuiCssLineBreak = -1;

    @Override
    public int doEndTag() throws JspException {

        BodyContent bodyContent = getBodyContent();
        String content = bodyContent.getString();

        try {
            if (enabled) {
                // call YUICompressor
                YuiCssCompressor compressor = new YuiCssCompressor();
                compressor.setLineBreak(yuiCssLineBreak);
                String result = compressor.compress(content);

                bodyContent.clear();
                bodyContent.append(result);
                bodyContent.writeOut(pageContext.getOut());
            } else {
                bodyContent.clear();
                bodyContent.append(content);
                bodyContent.writeOut(pageContext.getOut());
            }
        } catch (IOException e) {
            logger.error("", e);
        }

        return super.doEndTag();
    }

    /**
     * Sets the yui css line break.
     *
     * @param yuiCssLineBreak
     *            the new yui css line break
     *
     * @see HtmlCompressor#setYuiCssLineBreak(int)
     */
    public void setYuiCssLineBreak(int yuiCssLineBreak) {
        this.yuiCssLineBreak = yuiCssLineBreak;
    }

    /**
     * Sets the enabled.
     *
     * @param enabled
     *            the new enabled
     *
     * @see HtmlCompressor#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
