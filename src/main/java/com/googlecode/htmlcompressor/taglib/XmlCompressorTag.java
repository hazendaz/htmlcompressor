/*
 *    Copyright 2009-2022 the original author or authors.
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

import com.googlecode.htmlcompressor.compressor.XmlCompressor;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSP tag that compresses an XML content within &lt;compress:xml&gt;. Compression parameters are set by default.
 *
 * @author <a href="mailto:serg472@gmail.com">Sergiy Kovalchuk</a>
 *
 * @see XmlCompressor
 */
public class XmlCompressorTag extends BodyTagSupport {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(XmlCompressorTag.class);

    /** The enabled. */
    private boolean enabled = true;

    /** The remove comments. */
    // default settings
    private boolean removeComments = true;

    /** The remove intertag spaces. */
    private boolean removeIntertagSpaces = true;

    @Override
    public int doEndTag() throws JspException {

        BodyContent bodyContent = getBodyContent();
        String content = bodyContent.getString();

        XmlCompressor compressor = new XmlCompressor();
        compressor.setEnabled(enabled);
        compressor.setRemoveComments(removeComments);
        compressor.setRemoveIntertagSpaces(removeIntertagSpaces);

        try {
            bodyContent.clear();
            bodyContent.append(compressor.compress(content));
            bodyContent.writeOut(pageContext.getOut());
        } catch (IOException e) {
            logger.error("", e);
        }

        return super.doEndTag();
    }

    /**
     * Sets the enabled.
     *
     * @param enabled
     *            the new enabled
     *
     * @see XmlCompressor#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Sets the removes the comments.
     *
     * @param removeComments
     *            the new removes the comments
     *
     * @see XmlCompressor#setRemoveComments(boolean)
     */
    public void setRemoveComments(boolean removeComments) {
        this.removeComments = removeComments;
    }

    /**
     * Sets the removes the intertag spaces.
     *
     * @param removeIntertagSpaces
     *            the new removes the intertag spaces
     *
     * @see XmlCompressor#setRemoveIntertagSpaces(boolean)
     */
    public void setRemoveIntertagSpaces(boolean removeIntertagSpaces) {
        this.removeIntertagSpaces = removeIntertagSpaces;
    }

}
