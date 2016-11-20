/**
 *    Copyright 2009-2016 the original author or authors.
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
package com.googlecode.htmlcompressor.compressor;

/**
 * Class that stores metrics about HTML documents.
 * 
 * @author <a href="mailto:serg472@gmail.com">Sergiy Kovalchuk</a>
 */
public class HtmlMetrics {

    /** The filesize. */
    private int filesize         = 0;

    /** The empty chars. */
    private int emptyChars       = 0;

    /** The inline script size. */
    private int inlineScriptSize = 0;

    /** The inline style size. */
    private int inlineStyleSize  = 0;

    /** The inline event size. */
    private int inlineEventSize  = 0;

    /**
     * Returns total filesize of a document.
     *
     * @return total filesize of a document, in bytes
     */
    public int getFilesize() {
        return filesize;
    }

    /**
     * Sets the filesize.
     *
     * @param filesize
     *            the filesize to set
     */
    public void setFilesize(int filesize) {
        this.filesize = filesize;
    }

    /**
     * Returns number of empty characters (spaces, tabs, end of lines) in a document.
     *
     * @return number of empty characters in a document
     */
    public int getEmptyChars() {
        return emptyChars;
    }

    /**
     * Sets the empty chars.
     *
     * @param emptyChars
     *            the emptyChars to set
     */
    public void setEmptyChars(int emptyChars) {
        this.emptyChars = emptyChars;
    }

    /**
     * Returns total size of inline <code>&lt;script&gt;</code> tags.
     *
     * @return total size of inline <code>&lt;script&gt;</code> tags, in bytes
     */
    public int getInlineScriptSize() {
        return inlineScriptSize;
    }

    /**
     * Sets the inline script size.
     *
     * @param inlineScriptSize
     *            the inlineScriptSize to set
     */
    public void setInlineScriptSize(int inlineScriptSize) {
        this.inlineScriptSize = inlineScriptSize;
    }

    /**
     * Returns total size of inline <code>&lt;style&gt;</code> tags.
     *
     * @return total size of inline <code>&lt;style&gt;</code> tags, in bytes
     */
    public int getInlineStyleSize() {
        return inlineStyleSize;
    }

    /**
     * Sets the inline style size.
     *
     * @param inlineStyleSize
     *            the inlineStyleSize to set
     */
    public void setInlineStyleSize(int inlineStyleSize) {
        this.inlineStyleSize = inlineStyleSize;
    }

    /**
     * Returns total size of inline event handlers (<code>onclick</code>, etc).
     *
     * @return total size of inline event handlers, in bytes
     */
    public int getInlineEventSize() {
        return inlineEventSize;
    }

    /**
     * Sets the inline event size.
     *
     * @param inlineEventSize
     *            the inlineEventSize to set
     */
    public void setInlineEventSize(int inlineEventSize) {
        this.inlineEventSize = inlineEventSize;
    }

    @Override
    public String toString() {
        return String.format("Filesize=%d, Empty Chars=%d, Script Size=%d, Style Size=%d, Event Handler Size=%d",
                filesize, emptyChars, inlineScriptSize, inlineStyleSize, inlineEventSize);
    }

}
