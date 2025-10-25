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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that compresses given XML source by removing comments, extra spaces and line breaks while preserving content
 * within CDATA blocks.
 */
public class XmlCompressor implements Compressor {

    /** The enabled. */
    private boolean enabled = true;

    /** The remove comments. */
    // default settings
    private boolean removeComments = true;

    /** The remove intertag spaces. */
    private boolean removeIntertagSpaces = true;

    /** The Constant tempCdataBlock. */
    // temp replacements for preserved blocks
    protected static final String TEMP_CD_DATA_BLOCK = "%%%COMPRESS~CDATA~{0,number,#}%%%";

    /** The Constant cdataPattern. */
    // compiled regex patterns
    protected static final Pattern cdataPattern = Pattern.compile("<!\\[CDATA\\[.*?\\]\\]>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant commentPattern. */
    protected static final Pattern commentPattern = Pattern.compile("<!--.*?-->",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant intertagPattern. */
    protected static final Pattern intertagPattern = Pattern.compile(">\\s+<",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant tagEndSpacePattern. */
    protected static final Pattern tagEndSpacePattern = Pattern.compile("(<(?:[^>]+?))(?:\\s+?)(/?>)",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant multispacePattern. */
    protected static final Pattern multispacePattern = Pattern.compile("\\s+(?=[^<]*?>)",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant tagPropertyPattern. */
    protected static final Pattern tagPropertyPattern = Pattern.compile("(\\s\\w+)\\s*=\\s*(?=[^<]*?>)",
            Pattern.CASE_INSENSITIVE);

    /** The Constant tempCdataPattern. */
    protected static final Pattern tempCdataPattern = Pattern.compile("%%%COMPRESS~CDATA~(\\d+?)%%%",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /**
     * The main method that compresses given XML source and returns compressed result.
     *
     * @param xml
     *            XML content to compress
     *
     * @return compressed content.
     */
    @Override
    public String compress(String xml) {
        if (!enabled || xml == null || xml.isEmpty()) {
            return xml;
        }

        // preserved block containers
        List<String> cdataBlocks = new ArrayList<>();

        // preserve blocks
        xml = preserveBlocks(xml, cdataBlocks);

        // process pure xml
        xml = processXml(xml);

        // return preserved blocks
        xml = returnBlocks(xml, cdataBlocks);

        return xml.trim();
    }

    /**
     * Preserve blocks.
     *
     * @param xml
     *            the xml
     * @param cdataBlocks
     *            the cdata blocks
     *
     * @return the string
     */
    protected String preserveBlocks(String xml, List<String> cdataBlocks) {
        // preserve CDATA blocks
        Matcher matcher = cdataPattern.matcher(xml);
        int index = 0;
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            cdataBlocks.add(matcher.group(0));
            matcher.appendReplacement(sb, MessageFormat.format(TEMP_CD_DATA_BLOCK, index++));
        }
        matcher.appendTail(sb);
        xml = sb.toString();

        return xml;
    }

    /**
     * Return blocks.
     *
     * @param xml
     *            the xml
     * @param cdataBlocks
     *            the cdata blocks
     *
     * @return the string
     */
    protected String returnBlocks(String xml, List<String> cdataBlocks) {
        // put CDATA blocks back
        Matcher matcher = tempCdataPattern.matcher(xml);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb,
                    Matcher.quoteReplacement(cdataBlocks.get(Integer.parseInt(matcher.group(1)))));
        }
        matcher.appendTail(sb);
        xml = sb.toString();

        return xml;
    }

    /**
     * Process xml.
     *
     * @param xml
     *            the xml
     *
     * @return the string
     */
    protected String processXml(String xml) {
        // remove comments
        xml = removeComments(xml);

        // remove inter-tag spaces
        xml = removeIntertagSpaces(xml);

        // remove unneeded spaces inside tags
        xml = removeSpacesInsideTags(xml);

        return xml;
    }

    /**
     * Removes the spaces inside tags.
     *
     * @param xml
     *            the xml
     *
     * @return the string
     */
    protected String removeSpacesInsideTags(String xml) {
        // replace miltiple spaces inside tags with single spaces
        xml = multispacePattern.matcher(xml).replaceAll(" ");

        // remove spaces around equal sign inside tags
        xml = tagPropertyPattern.matcher(xml).replaceAll("$1=");

        // remove ending spaces inside tags
        xml = tagEndSpacePattern.matcher(xml).replaceAll("$1$2");
        return xml;
    }

    /**
     * Removes the intertag spaces.
     *
     * @param xml
     *            the xml
     *
     * @return the string
     */
    protected String removeIntertagSpaces(String xml) {
        // remove inter-tag spaces
        if (removeIntertagSpaces) {
            xml = intertagPattern.matcher(xml).replaceAll("><");
        }
        return xml;
    }

    /**
     * Removes the comments.
     *
     * @param xml
     *            the xml
     *
     * @return the string
     */
    protected String removeComments(String xml) {
        // remove comments
        if (removeComments) {
            xml = commentPattern.matcher(xml).replaceAll("");
        }
        return xml;
    }

    /**
     * Returns <code>true</code> if compression is enabled.
     *
     * @return <code>true</code> if compression is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * If set to <code>false</code> all compression will be bypassed. Might be useful for testing purposes. Default is
     * <code>true</code>.
     *
     * @param enabled
     *            set <code>false</code> to bypass all compression
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns <code>true</code> if all XML comments will be removed.
     *
     * @return <code>true</code> if all XML comments will be removed
     */
    public boolean isRemoveComments() {
        return removeComments;
    }

    /**
     * If set to <code>true</code> all XML comments will be removed. Default is <code>true</code>.
     *
     * @param removeComments
     *            set <code>true</code> to remove all XML comments
     */
    public void setRemoveComments(boolean removeComments) {
        this.removeComments = removeComments;
    }

    /**
     * Returns <code>true</code> if all inter-tag whitespace characters will be removed.
     *
     * @return <code>true</code> if all inter-tag whitespace characters will be removed.
     */
    public boolean isRemoveIntertagSpaces() {
        return removeIntertagSpaces;
    }

    /**
     * If set to <code>true</code> all inter-tag whitespace characters will be removed. Default is <code>true</code>.
     *
     * @param removeIntertagSpaces
     *            set <code>true</code> to remove all inter-tag whitespace characters
     */
    public void setRemoveIntertagSpaces(boolean removeIntertagSpaces) {
        this.removeIntertagSpaces = removeIntertagSpaces;
    }

}
