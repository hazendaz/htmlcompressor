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
package com.googlecode.htmlcompressor.compressor;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.javascript.ErrorReporter;

/**
 * Class that compresses given HTML source by removing comments, extra spaces and lin&lt;pre&gt; while preserving
 * content within &lt;pre&gt;, &lt;textarea&gt;, &lt;script&gt; and &lt;style&gt; tags.
 * <p>
 * Blocks that should be additionally preserved could be marked with: <br>
 * <code>&lt;!-- {{{ --&gt;
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;...
 * <br>&lt;!-- }}} --&gt;</code> <br>
 * or any number of user defined patterns.
 * <p>
 * Content inside &lt;script&gt; or &lt;style&gt; tags could be optionally compressed using
 * <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a> or
 * <a href="http://code.google.com/closure/compiler/">Google Closure Compiler</a> libraries.
 *
 * @author <a href="mailto:serg472@gmail.com">Sergiy Kovalchuk</a>
 */
public class HtmlCompressor implements Compressor {

    /** The Constant JS_COMPRESSOR_YUI. */
    public static final String JS_COMPRESSOR_YUI = "yui";

    /** The Constant JS_COMPRESSOR_CLOSURE. */
    public static final String JS_COMPRESSOR_CLOSURE = "closure";

    /**
     * Predefined pattern that matches <code>&lt;?php ... ?&gt;</code> tags. Could be passed inside a list to
     * {@link #setPreservePatterns(List) setPreservePatterns} method.
     */
    public static final Pattern PHP_TAG_PATTERN = Pattern.compile("<\\?php.*?\\?>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /**
     * Predefined pattern that matches <code>&lt;% ... %&gt;</code> tags. Could be passed inside a list to
     * {@link #setPreservePatterns(List) setPreservePatterns} method.
     */
    public static final Pattern SERVER_SCRIPT_TAG_PATTERN = Pattern.compile("<%.*?%>", Pattern.DOTALL);

    /**
     * Predefined pattern that matches <code>&lt;--# ... --&gt;</code> tags. Could be passed inside a list to
     * {@link #setPreservePatterns(List) setPreservePatterns} method.
     */
    public static final Pattern SERVER_SIDE_INCLUDE_PATTERN = Pattern.compile("<!--\\s*#.*?-->", Pattern.DOTALL);

    /**
     * Predefined list of tags that are very likely to be block-level. Could be passed to
     * {@link #setRemoveSurroundingSpaces(String) setRemoveSurroundingSpaces} method.
     */
    public static final String BLOCK_TAGS_MIN = "html,head,body,br,p";

    /**
     * Predefined list of tags that are block-level by default, excluding <code>&lt;div&gt;</code> and
     * <code>&lt;li&gt;</code> tags. Table tags are also included. Could be passed to
     * {@link #setRemoveSurroundingSpaces(String) setRemoveSurroundingSpaces} method.
     */
    public static final String BLOCK_TAGS_MAX = BLOCK_TAGS_MIN
            + ",h1,h2,h3,h4,h5,h6,blockquote,center,dl,fieldset,form,frame,frameset,hr,noframes,ol,table,tbody,tr,td,th,tfoot,thead,ul";

    /**
     * Could be passed to {@link #setRemoveSurroundingSpaces(String) setRemoveSurroundingSpaces} method to remove all
     * surrounding spaces (not recommended).
     */
    public static final String ALL_TAGS = "all";

    /** The enabled. */
    private boolean enabled = true;

    // javascript and css compressor implementations

    /** The java script compressor. */
    private Compressor javaScriptCompressor;

    /** The css compressor. */
    private Compressor cssCompressor;

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

    /** The compress java script. */
    private boolean compressJavaScript;

    /** The compress css. */
    private boolean compressCss;

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

    /** The preserve line breaks. */
    private boolean preserveLineBreaks;

    /** The remove surrounding spaces. */
    private String removeSurroundingSpaces;

    /** The preserve patterns. */
    private List<Pattern> preservePatterns;

    // statistics

    /** The generate statistics. */
    private boolean generateStatistics;

    /** The statistics. */
    private HtmlCompressorStatistics statistics;

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

    /** The yui error reporter. */
    // error reporter implementation for YUI compressor
    private ErrorReporter yuiErrorReporter;

    /** The Constant tempCondCommentBlock. */
    // temp replacements for preserved blocks
    protected static final String TEMP_COND_COMMENT_BLOCK = "%%%~COMPRESS~COND~{0,number,#}~%%%";

    /** The Constant tempPreBlock. */
    protected static final String TEMP_PRE_BLOCK = "%%%~COMPRESS~PRE~{0,number,#}~%%%";

    /** The Constant tempTextAreaBlock. */
    protected static final String TEMP_TEXT_AREA_BLOCK = "%%%~COMPRESS~TEXTAREA~{0,number,#}~%%%";

    /** The Constant tempScriptBlock. */
    protected static final String TEMP_SCRIPT_BLOCK = "%%%~COMPRESS~SCRIPT~{0,number,#}~%%%";

    /** The Constant tempStyleBlock. */
    protected static final String TEMP_STYLE_BLOCK = "%%%~COMPRESS~STYLE~{0,number,#}~%%%";

    /** The Constant tempEventBlock. */
    protected static final String TEMP_EVENT_BLOCK = "%%%~COMPRESS~EVENT~{0,number,#}~%%%";

    /** The Constant tempLineBreakBlock. */
    protected static final String TEMP_LINE_BREAK_BLOCK = "%%%~COMPRESS~LT~{0,number,#}~%%%";

    /** The Constant tempSkipBlock. */
    protected static final String TEMP_SKIP_BLOCK = "%%%~COMPRESS~SKIP~{0,number,#}~%%%";

    /** The Constant tempUserBlock. */
    protected static final String TEMP_USER_BLOCK = "%%%~COMPRESS~USER{0,number,#}~{1,number,#}~%%%";

    /** The Constant emptyPattern. */
    // compiled regex patterns
    protected static final Pattern emptyPattern = Pattern.compile("\\s");

    /** The Constant skipPattern. */
    protected static final Pattern skipPattern = Pattern.compile(
            "<!--\\s*\\{\\{\\{\\s*-->(.*?)<!--\\s*\\}\\}\\}\\s*-->", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant condCommentPattern. */
    protected static final Pattern condCommentPattern = Pattern
            .compile("(<!(?:--)?\\[[^\\]]+?]>)(.*?)(<!\\[[^\\]]+]-->)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant commentPattern. */
    protected static final Pattern commentPattern = Pattern.compile("<!---->|<!--[^\\[].*?-->",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant intertagPattern_TagTag. */
    protected static final Pattern intertagPattern_TagTag = Pattern.compile(">\\s+<",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant intertagPattern_TagCustom. */
    protected static final Pattern intertagPattern_TagCustom = Pattern.compile(">\\s+%%%~",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant intertagPattern_CustomTag. */
    protected static final Pattern intertagPattern_CustomTag = Pattern.compile("~%%%\\s+<",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant intertagPattern_CustomCustom. */
    protected static final Pattern intertagPattern_CustomCustom = Pattern.compile("~%%%\\s+%%%~",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant multispacePattern. */
    protected static final Pattern multispacePattern = Pattern.compile("\\s+",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant tagEndSpacePattern. */
    protected static final Pattern tagEndSpacePattern = Pattern.compile("(<(?:[^>]+?))(?:\\s+?)(/?>)",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant tagLastUnquotedValuePattern. */
    protected static final Pattern tagLastUnquotedValuePattern = Pattern.compile("=\\s*[a-z0-9-_]+$",
            Pattern.CASE_INSENSITIVE);

    /** The Constant tagQuotePattern. */
    protected static final Pattern tagQuotePattern = Pattern.compile("\\s*=\\s*([\"'])([a-z0-9-_]+?)\\1(/?)(?=[^<]*?>)",
            Pattern.CASE_INSENSITIVE);

    /** The Constant prePattern. */
    protected static final Pattern prePattern = Pattern.compile("(<pre[^>]*?>)(.*?)(</pre>)",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant taPattern. */
    protected static final Pattern taPattern = Pattern.compile("(<textarea[^>]*?>)(.*?)(</textarea>)",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant scriptPattern. */
    protected static final Pattern scriptPattern = Pattern.compile("(<script[^>]*?>)(.*?)(</script>)",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant stylePattern. */
    protected static final Pattern stylePattern = Pattern.compile("(<style[^>]*?>)(.*?)(</style>)",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant tagPropertyPattern. */
    protected static final Pattern tagPropertyPattern = Pattern.compile("(\\s\\w+)\\s*=\\s*(?=[^<]*?>)",
            Pattern.CASE_INSENSITIVE);

    /** The Constant cdataPattern. */
    protected static final Pattern cdataPattern = Pattern.compile("\\s*<!\\[CDATA\\[(.*?)\\]\\]>\\s*",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant doctypePattern. */
    protected static final Pattern doctypePattern = Pattern.compile("<!DOCTYPE[^>]*>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant typeAttrPattern. */
    protected static final Pattern typeAttrPattern = Pattern.compile("type\\s*=\\s*([\\\"']*)(.+?)\\1",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant jsTypeAttrPattern. */
    protected static final Pattern jsTypeAttrPattern = Pattern.compile(
            "(<script[^>]*)type\\s*=\\s*([\"']*)(?:text|application)/javascript\\2([^>]*>)",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant jsLangAttrPattern. */
    protected static final Pattern jsLangAttrPattern = Pattern.compile(
            "(<script[^>]*)language\\s*=\\s*([\"']*)javascript\\2([^>]*>)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant styleTypeAttrPattern. */
    protected static final Pattern styleTypeAttrPattern = Pattern.compile(
            "(<style[^>]*)type\\s*=\\s*([\"']*)text/style\\2([^>]*>)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant linkTypeAttrPattern. */
    protected static final Pattern linkTypeAttrPattern = Pattern.compile(
            "(<link[^>]*)type\\s*=\\s*([\"']*)text/(?:css|plain)\\2([^>]*>)",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant linkRelAttrPattern. */
    protected static final Pattern linkRelAttrPattern = Pattern.compile(
            "<link(?:[^>]*)rel\\s*=\\s*([\"']*)(?:alternate\\s+)?stylesheet\\1(?:[^>]*)>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant formMethodAttrPattern. */
    protected static final Pattern formMethodAttrPattern = Pattern
            .compile("(<form[^>]*)method\\s*=\\s*([\"']*)get\\2([^>]*>)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant inputTypeAttrPattern. */
    protected static final Pattern inputTypeAttrPattern = Pattern
            .compile("(<input[^>]*)type\\s*=\\s*([\"']*)text\\2([^>]*>)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant booleanAttrPattern. */
    protected static final Pattern booleanAttrPattern = Pattern.compile(
            "(<\\w+[^>]*)(checked|selected|disabled|readonly)\\s*=\\s*([\"']*)\\w*\\3([^>]*>)",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant eventJsProtocolPattern. */
    protected static final Pattern eventJsProtocolPattern = Pattern.compile("^javascript:\\s*(.+)",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant httpProtocolPattern. */
    protected static final Pattern httpProtocolPattern = Pattern.compile(
            "(<[^>]+?(?:href|src|cite|action)\\s*=\\s*['\"])http:(//[^>]+?>)",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant httpsProtocolPattern. */
    protected static final Pattern httpsProtocolPattern = Pattern.compile(
            "(<[^>]+?(?:href|src|cite|action)\\s*=\\s*['\"])https:(//[^>]+?>)",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant relExternalPattern. */
    protected static final Pattern relExternalPattern = Pattern.compile(
            "<(?:[^>]*)rel\\s*=\\s*([\"']*)(?:alternate\\s+)?external\\1(?:[^>]*)>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant eventPattern1 (unmasked). */
    protected static final Pattern eventPattern1 = Pattern.compile(
            "(\\son[a-z]+\\s*=\\s*\")([^\"\\\\\\r\\n]*(?:\\\\.[^\"\\\\\\r\\n]*)*)(\")", Pattern.CASE_INSENSITIVE);

    /** The Constant eventPattern2. */
    // \son[a-z]+\s*=\s*"[^"\\\r\n]*(?:\\.[^"\\\r\n]*)*"
    protected static final Pattern eventPattern2 = Pattern
            .compile("(\\son[a-z]+\\s*=\\s*')([^'\\\\\\r\\n]*(?:\\\\.[^'\\\\\\r\\n]*)*)(')", Pattern.CASE_INSENSITIVE);

    /** The Constant lineBreakPattern. */
    protected static final Pattern lineBreakPattern = Pattern.compile("(?:\\p{Blank}*(\\r?\\n)\\p{Blank}*)+");

    /** The Constant surroundingSpacesMinPattern. */
    protected static final Pattern surroundingSpacesMinPattern = Pattern.compile(
            "\\s*(</?(?:" + BLOCK_TAGS_MIN.replace(",", "|") + ")(?:>|[\\s/][^>]*>))\\s*",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant surroundingSpacesMaxPattern. */
    protected static final Pattern surroundingSpacesMaxPattern = Pattern.compile(
            "\\s*(</?(?:" + BLOCK_TAGS_MAX.replace(",", "|") + ")(?:>|[\\s/][^>]*>))\\s*",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant surroundingSpacesAllPattern. */
    protected static final Pattern surroundingSpacesAllPattern = Pattern.compile("\\s*(<[^>]+>)\\s*",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** The Constant tempCondCommentPattern. */
    // patterns for searching for temporary replacements
    protected static final Pattern tempCondCommentPattern = Pattern.compile("%%%~COMPRESS~COND~(\\d+?)~%%%");

    /** The Constant tempPrePattern. */
    protected static final Pattern tempPrePattern = Pattern.compile("%%%~COMPRESS~PRE~(\\d+?)~%%%");

    /** The Constant tempTextAreaPattern. */
    protected static final Pattern tempTextAreaPattern = Pattern.compile("%%%~COMPRESS~TEXTAREA~(\\d+?)~%%%");

    /** The Constant tempScriptPattern. */
    protected static final Pattern tempScriptPattern = Pattern.compile("%%%~COMPRESS~SCRIPT~(\\d+?)~%%%");

    /** The Constant tempStylePattern. */
    protected static final Pattern tempStylePattern = Pattern.compile("%%%~COMPRESS~STYLE~(\\d+?)~%%%");

    /** The Constant tempEventPattern. */
    protected static final Pattern tempEventPattern = Pattern.compile("%%%~COMPRESS~EVENT~(\\d+?)~%%%");

    /** The Constant tempSkipPattern. */
    protected static final Pattern tempSkipPattern = Pattern.compile("%%%~COMPRESS~SKIP~(\\d+?)~%%%");

    /** The Constant tempLineBreakPattern. */
    protected static final Pattern tempLineBreakPattern = Pattern.compile("%%%~COMPRESS~LT~(\\d+?)~%%%");

    /**
     * The main method that compresses given HTML source and returns compressed result.
     *
     * @param html
     *            HTML content to compress
     *
     * @return compressed content.
     */
    @Override
    public String compress(String html) {
        if (!enabled || html == null || html.length() == 0) {
            return html;
        }

        // calculate uncompressed statistics
        initStatistics(html);

        // preserved block containers
        List<String> condCommentBlocks = new ArrayList<>();
        List<String> preBlocks = new ArrayList<>();
        List<String> taBlocks = new ArrayList<>();
        List<String> scriptBlocks = new ArrayList<>();
        List<String> styleBlocks = new ArrayList<>();
        List<String> eventBlocks = new ArrayList<>();
        List<String> skipBlocks = new ArrayList<>();
        List<String> lineBreakBlocks = new ArrayList<>();
        List<List<String>> userBlocks = new ArrayList<>();

        // preserve blocks
        html = preserveBlocks(html, preBlocks, taBlocks, scriptBlocks, styleBlocks, eventBlocks, condCommentBlocks,
                skipBlocks, lineBreakBlocks, userBlocks);

        // process pure html
        html = processHtml(html);

        // process preserved blocks
        processPreservedBlocks(preBlocks, taBlocks, scriptBlocks, styleBlocks, eventBlocks, condCommentBlocks,
                skipBlocks, lineBreakBlocks, userBlocks);

        // put preserved blocks back
        html = returnBlocks(html, preBlocks, taBlocks, scriptBlocks, styleBlocks, eventBlocks, condCommentBlocks,
                skipBlocks, lineBreakBlocks, userBlocks);

        // calculate compressed statistics
        endStatistics(html);

        return html;
    }

    /**
     * Inits the statistics.
     *
     * @param html
     *            the html
     */
    protected void initStatistics(String html) {
        // create stats
        if (generateStatistics) {
            statistics = new HtmlCompressorStatistics();
            statistics.setTime(System.currentTimeMillis());
            statistics.getOriginalMetrics().setFilesize(html.length());

            // calculate number of empty chars
            Matcher matcher = emptyPattern.matcher(html);
            while (matcher.find()) {
                statistics.getOriginalMetrics().setEmptyChars(statistics.getOriginalMetrics().getEmptyChars() + 1);
            }
        } else {
            statistics = null;
        }
    }

    /**
     * End statistics.
     *
     * @param html
     *            the html
     */
    protected void endStatistics(String html) {
        // calculate compression time
        if (generateStatistics) {
            statistics.setTime(System.currentTimeMillis() - statistics.getTime());
            statistics.getCompressedMetrics().setFilesize(html.length());

            // calculate number of empty chars
            Matcher matcher = emptyPattern.matcher(html);
            while (matcher.find()) {
                statistics.getCompressedMetrics().setEmptyChars(statistics.getCompressedMetrics().getEmptyChars() + 1);
            }
        }
    }

    /**
     * Preserve blocks.
     *
     * @param html
     *            the html
     * @param preBlocks
     *            the pre blocks
     * @param taBlocks
     *            the ta blocks
     * @param scriptBlocks
     *            the script blocks
     * @param styleBlocks
     *            the style blocks
     * @param eventBlocks
     *            the event blocks
     * @param condCommentBlocks
     *            the cond comment blocks
     * @param skipBlocks
     *            the skip blocks
     * @param lineBreakBlocks
     *            the line break blocks
     * @param userBlocks
     *            the user blocks
     *
     * @return the string
     */
    protected String preserveBlocks(String html, List<String> preBlocks, List<String> taBlocks,
            List<String> scriptBlocks, List<String> styleBlocks, List<String> eventBlocks,
            List<String> condCommentBlocks, List<String> skipBlocks, List<String> lineBreakBlocks,
            List<List<String>> userBlocks) {

        // preserve user blocks
        if (preservePatterns != null) {
            for (int p = 0; p < preservePatterns.size(); p++) {
                List<String> userBlock = new ArrayList<>();

                Matcher matcher = preservePatterns.get(p).matcher(html);
                int index = 0;
                StringBuilder sb = new StringBuilder();
                while (matcher.find()) {
                    if (matcher.group(0).trim().length() > 0) {
                        userBlock.add(matcher.group(0));
                        matcher.appendReplacement(sb, MessageFormat.format(TEMP_USER_BLOCK, p, index++));
                    }
                }
                matcher.appendTail(sb);
                html = sb.toString();
                userBlocks.add(userBlock);
            }
        }

        // preserve <!-- {{{ ---><!-- }}} ---> skip blocks
        Matcher matcher = skipPattern.matcher(html);
        int skipBlockIndex = 0;
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            if (matcher.group(1).trim().length() > 0) {
                skipBlocks.add(matcher.group(1));
                matcher.appendReplacement(sb, MessageFormat.format(TEMP_SKIP_BLOCK, skipBlockIndex++));
            }
        }
        matcher.appendTail(sb);
        html = sb.toString();

        // preserve conditional comments
        HtmlCompressor condCommentCompressor = createCompressorClone();
        matcher = condCommentPattern.matcher(html);
        int index = 0;
        sb = new StringBuilder();
        while (matcher.find()) {
            if (matcher.group(2).trim().length() > 0) {
                condCommentBlocks
                        .add(matcher.group(1) + condCommentCompressor.compress(matcher.group(2)) + matcher.group(3));
                matcher.appendReplacement(sb, MessageFormat.format(TEMP_COND_COMMENT_BLOCK, index++));
            }
        }
        matcher.appendTail(sb);
        html = sb.toString();

        // preserve inline events
        matcher = eventPattern1.matcher(html);
        index = 0;
        sb = new StringBuilder();
        while (matcher.find()) {
            if (matcher.group(2).trim().length() > 0) {
                eventBlocks.add(matcher.group(2));
                matcher.appendReplacement(sb, "$1" + MessageFormat.format(TEMP_EVENT_BLOCK, index++) + "$3");
            }
        }
        matcher.appendTail(sb);
        html = sb.toString();

        matcher = eventPattern2.matcher(html);
        sb = new StringBuilder();
        while (matcher.find()) {
            if (matcher.group(2).trim().length() > 0) {
                eventBlocks.add(matcher.group(2));
                matcher.appendReplacement(sb, "$1" + MessageFormat.format(TEMP_EVENT_BLOCK, index++) + "$3");
            }
        }
        matcher.appendTail(sb);
        html = sb.toString();

        // preserve PRE tags
        matcher = prePattern.matcher(html);
        index = 0;
        sb = new StringBuilder();
        while (matcher.find()) {
            if (matcher.group(2).trim().length() > 0) {
                preBlocks.add(matcher.group(2));
                matcher.appendReplacement(sb, "$1" + MessageFormat.format(TEMP_PRE_BLOCK, index++) + "$3");
            }
        }
        matcher.appendTail(sb);
        html = sb.toString();

        // preserve SCRIPT tags
        matcher = scriptPattern.matcher(html);
        index = 0;
        sb = new StringBuilder();
        while (matcher.find()) {
            // ignore empty scripts
            if (matcher.group(2).trim().length() > 0) {

                // check type
                String type = "";
                Matcher typeMatcher = typeAttrPattern.matcher(matcher.group(1));
                if (typeMatcher.find()) {
                    type = typeMatcher.group(2).toLowerCase();
                }

                if (type.length() == 0 || "text/javascript".equals(type) || "application/javascript".equals(type)) {
                    // javascript block, preserve and compress with js compressor
                    scriptBlocks.add(matcher.group(2));
                    matcher.appendReplacement(sb, "$1" + MessageFormat.format(TEMP_SCRIPT_BLOCK, index++) + "$3");
                } else if ("text/x-jquery-tmpl".equals(type)) {
                    // jquery template, ignore so it gets compressed with the rest of html
                } else {
                    // some custom script, preserve it inside "skip blocks" so it won't be compressed with js compressor
                    skipBlocks.add(matcher.group(2));
                    matcher.appendReplacement(sb,
                            "$1" + MessageFormat.format(TEMP_SKIP_BLOCK, skipBlockIndex++) + "$3");
                }

            }
        }
        matcher.appendTail(sb);
        html = sb.toString();

        // preserve STYLE tags
        matcher = stylePattern.matcher(html);
        index = 0;
        sb = new StringBuilder();
        while (matcher.find()) {
            if (matcher.group(2).trim().length() > 0) {
                styleBlocks.add(matcher.group(2));
                matcher.appendReplacement(sb, "$1" + MessageFormat.format(TEMP_STYLE_BLOCK, index++) + "$3");
            }
        }
        matcher.appendTail(sb);
        html = sb.toString();

        // preserve TEXTAREA tags
        matcher = taPattern.matcher(html);
        index = 0;
        sb = new StringBuilder();
        while (matcher.find()) {
            if (matcher.group(2).trim().length() > 0) {
                taBlocks.add(matcher.group(2));
                matcher.appendReplacement(sb, "$1" + MessageFormat.format(TEMP_TEXT_AREA_BLOCK, index++) + "$3");
            }
        }
        matcher.appendTail(sb);
        html = sb.toString();

        // preserve line breaks
        if (preserveLineBreaks) {
            matcher = lineBreakPattern.matcher(html);
            index = 0;
            sb = new StringBuilder();
            while (matcher.find()) {
                lineBreakBlocks.add(matcher.group(1));
                matcher.appendReplacement(sb, MessageFormat.format(TEMP_LINE_BREAK_BLOCK, index++));
            }
            matcher.appendTail(sb);
            html = sb.toString();
        }

        return html;
    }

    /**
     * Return blocks.
     *
     * @param html
     *            the html
     * @param preBlocks
     *            the pre blocks
     * @param taBlocks
     *            the ta blocks
     * @param scriptBlocks
     *            the script blocks
     * @param styleBlocks
     *            the style blocks
     * @param eventBlocks
     *            the event blocks
     * @param condCommentBlocks
     *            the cond comment blocks
     * @param skipBlocks
     *            the skip blocks
     * @param lineBreakBlocks
     *            the line break blocks
     * @param userBlocks
     *            the user blocks
     *
     * @return the string
     */
    protected String returnBlocks(String html, List<String> preBlocks, List<String> taBlocks, List<String> scriptBlocks,
            List<String> styleBlocks, List<String> eventBlocks, List<String> condCommentBlocks, List<String> skipBlocks,
            List<String> lineBreakBlocks, List<List<String>> userBlocks) {

        // put line breaks back
        if (preserveLineBreaks) {
            Matcher matcher = tempLineBreakPattern.matcher(html);
            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                int i = Integer.parseInt(matcher.group(1));
                if (lineBreakBlocks.size() > i) {
                    matcher.appendReplacement(sb, lineBreakBlocks.get(i));
                }
            }
            matcher.appendTail(sb);
            html = sb.toString();
        }

        // put TEXTAREA blocks back
        Matcher matcher = tempTextAreaPattern.matcher(html);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            int i = Integer.parseInt(matcher.group(1));
            if (taBlocks.size() > i) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(taBlocks.get(i)));
            }
        }
        matcher.appendTail(sb);
        html = sb.toString();

        // put STYLE blocks back
        matcher = tempStylePattern.matcher(html);
        sb = new StringBuilder();
        while (matcher.find()) {
            int i = Integer.parseInt(matcher.group(1));
            if (styleBlocks.size() > i) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(styleBlocks.get(i)));
            }
        }
        matcher.appendTail(sb);
        html = sb.toString();

        // put SCRIPT blocks back
        matcher = tempScriptPattern.matcher(html);
        sb = new StringBuilder();
        while (matcher.find()) {
            int i = Integer.parseInt(matcher.group(1));
            if (scriptBlocks.size() > i) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(scriptBlocks.get(i)));
            }
        }
        matcher.appendTail(sb);
        html = sb.toString();

        // put PRE blocks back
        matcher = tempPrePattern.matcher(html);
        sb = new StringBuilder();
        while (matcher.find()) {
            int i = Integer.parseInt(matcher.group(1));
            if (preBlocks.size() > i) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(preBlocks.get(i)));
            }
        }
        matcher.appendTail(sb);
        html = sb.toString();

        // put event blocks back
        matcher = tempEventPattern.matcher(html);
        sb = new StringBuilder();
        while (matcher.find()) {
            int i = Integer.parseInt(matcher.group(1));
            if (eventBlocks.size() > i) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(eventBlocks.get(i)));
            }
        }
        matcher.appendTail(sb);
        html = sb.toString();

        // put conditional comments back
        matcher = tempCondCommentPattern.matcher(html);
        sb = new StringBuilder();
        while (matcher.find()) {
            int i = Integer.parseInt(matcher.group(1));
            if (condCommentBlocks.size() > i) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(condCommentBlocks.get(i)));
            }
        }
        matcher.appendTail(sb);
        html = sb.toString();

        // put skip blocks back
        matcher = tempSkipPattern.matcher(html);
        sb = new StringBuilder();
        while (matcher.find()) {
            int i = Integer.parseInt(matcher.group(1));
            if (skipBlocks.size() > i) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(skipBlocks.get(i)));
            }
        }
        matcher.appendTail(sb);
        html = sb.toString();

        // put user blocks back
        if (preservePatterns != null) {
            for (int p = preservePatterns.size() - 1; p >= 0; p--) {
                Pattern tempUserPattern = Pattern.compile("%%%~COMPRESS~USER" + p + "~(\\d+?)~%%%");
                matcher = tempUserPattern.matcher(html);
                sb = new StringBuilder();
                while (matcher.find()) {
                    int i = Integer.parseInt(matcher.group(1));
                    if (userBlocks.size() > p && userBlocks.get(p).size() > i) {
                        matcher.appendReplacement(sb, Matcher.quoteReplacement(userBlocks.get(p).get(i)));
                    }
                }
                matcher.appendTail(sb);
                html = sb.toString();
            }
        }

        return html;
    }

    /**
     * Process html.
     *
     * @param html
     *            the html
     *
     * @return the string
     */
    protected String processHtml(String html) {

        // remove comments
        html = removeComments(html);

        // simplify doctype
        html = simpleDoctype(html);

        // remove script attributes
        html = removeScriptAttributes(html);

        // remove style attributes
        html = removeStyleAttributes(html);

        // remove link attributes
        html = removeLinkAttributes(html);

        // remove form attributes
        html = removeFormAttributes(html);

        // remove input attributes
        html = removeInputAttributes(html);

        // simplify boolean attributes
        html = simpleBooleanAttributes(html);

        // remove http from attributes
        html = removeHttpProtocol(html);

        // remove https from attributes
        html = removeHttpsProtocol(html);

        // remove inter-tag spaces
        html = removeIntertagSpaces(html);

        // remove multi whitespace characters
        html = removeMultiSpaces(html);

        // remove spaces around equals sign and ending spaces
        html = removeSpacesInsideTags(html);

        // remove quotes from tag attributes
        html = removeQuotesInsideTags(html);

        // remove surrounding spaces
        html = removeSurroundingSpaces(html);

        return html.trim();
    }

    /**
     * Removes the surrounding spaces.
     *
     * @param html
     *            the html
     *
     * @return the string
     */
    protected String removeSurroundingSpaces(String html) {
        // remove spaces around provided tags
        if (removeSurroundingSpaces != null) {
            Pattern pattern;
            if (removeSurroundingSpaces.equalsIgnoreCase(BLOCK_TAGS_MIN)) {
                pattern = surroundingSpacesMinPattern;
            } else if (removeSurroundingSpaces.equalsIgnoreCase(BLOCK_TAGS_MAX)) {
                pattern = surroundingSpacesMaxPattern;
            }
            if (removeSurroundingSpaces.equalsIgnoreCase(ALL_TAGS)) {
                pattern = surroundingSpacesAllPattern;
            } else {
                pattern = Pattern.compile(
                        "\\s*(</?(?:" + removeSurroundingSpaces.replaceAll(",", "|") + ")(?:>|[\\s/][^>]*>))\\s*",
                        Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            }

            Matcher matcher = pattern.matcher(html);
            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                matcher.appendReplacement(sb, "$1");
            }
            matcher.appendTail(sb);
            html = sb.toString();

        }
        return html;
    }

    /**
     * Removes the quotes inside tags.
     *
     * @param html
     *            the html
     *
     * @return the string
     */
    protected String removeQuotesInsideTags(String html) {
        // remove quotes from tag attributes
        if (removeQuotes) {
            Matcher matcher = tagQuotePattern.matcher(html);
            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                // if quoted attribute is followed by "/" add extra space
                if (matcher.group(3).trim().length() == 0) {
                    matcher.appendReplacement(sb, "=$2");
                } else {
                    matcher.appendReplacement(sb, "=$2 $3");
                }
            }
            matcher.appendTail(sb);
            html = sb.toString();

        }
        return html;
    }

    /**
     * Removes the spaces inside tags.
     *
     * @param html
     *            the html
     *
     * @return the string
     */
    protected String removeSpacesInsideTags(String html) {
        // remove spaces around equals sign inside tags
        html = tagPropertyPattern.matcher(html).replaceAll("$1=");

        // remove ending spaces inside tags
        // html = tagEndSpacePattern.matcher(html).replaceAll("$1$2");
        Matcher matcher = tagEndSpacePattern.matcher(html);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            // keep space if attribute value is unquoted before trailing slash
            if (matcher.group(2).startsWith("/") && tagLastUnquotedValuePattern.matcher(matcher.group(1)).find()) {
                matcher.appendReplacement(sb, "$1 $2");
            } else {
                matcher.appendReplacement(sb, "$1$2");
            }
        }
        matcher.appendTail(sb);
        html = sb.toString();

        return html;
    }

    /**
     * Removes the multi spaces.
     *
     * @param html
     *            the html
     *
     * @return the string
     */
    protected String removeMultiSpaces(String html) {
        // collapse multiple spaces
        if (removeMultiSpaces) {
            html = multispacePattern.matcher(html).replaceAll(" ");
        }
        return html;
    }

    /**
     * Removes the intertag spaces.
     *
     * @param html
     *            the html
     *
     * @return the string
     */
    protected String removeIntertagSpaces(String html) {
        // remove inter-tag spaces
        if (removeIntertagSpaces) {
            html = intertagPattern_TagTag.matcher(html).replaceAll("><");
            html = intertagPattern_TagCustom.matcher(html).replaceAll(">%%%~");
            html = intertagPattern_CustomTag.matcher(html).replaceAll("~%%%<");
            html = intertagPattern_CustomCustom.matcher(html).replaceAll("~%%%%%%~");
        }
        return html;
    }

    /**
     * Removes the comments.
     *
     * @param html
     *            the html
     *
     * @return the string
     */
    protected String removeComments(String html) {
        // remove comments
        if (removeComments) {
            html = commentPattern.matcher(html).replaceAll("");
        }
        return html;
    }

    /**
     * Simple doctype.
     *
     * @param html
     *            the html
     *
     * @return the string
     */
    protected String simpleDoctype(String html) {
        // simplify doctype
        if (simpleDoctype) {
            html = doctypePattern.matcher(html).replaceAll("<!DOCTYPE html>");
        }
        return html;
    }

    /**
     * Removes the script attributes.
     *
     * @param html
     *            the html
     *
     * @return the string
     */
    protected String removeScriptAttributes(String html) {

        if (removeScriptAttributes) {
            // remove type from script tags
            html = jsTypeAttrPattern.matcher(html).replaceAll("$1$3");

            // remove language from script tags
            html = jsLangAttrPattern.matcher(html).replaceAll("$1$3");
        }
        return html;
    }

    /**
     * Removes the style attributes.
     *
     * @param html
     *            the html
     *
     * @return the string
     */
    protected String removeStyleAttributes(String html) {
        // remove type from style tags
        if (removeStyleAttributes) {
            html = styleTypeAttrPattern.matcher(html).replaceAll("$1$3");
        }
        return html;
    }

    /**
     * Removes the link attributes.
     *
     * @param html
     *            the html
     *
     * @return the string
     */
    protected String removeLinkAttributes(String html) {
        // remove type from link tags with rel=stylesheet
        if (removeLinkAttributes) {
            Matcher matcher = linkTypeAttrPattern.matcher(html);
            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                // if rel=stylesheet
                if (linkRelAttrPattern.matcher(matcher.group(0)).matches()) {
                    matcher.appendReplacement(sb, "$1$3");
                } else {
                    matcher.appendReplacement(sb, "$0");
                }
            }
            matcher.appendTail(sb);
            html = sb.toString();
        }
        return html;
    }

    /**
     * Removes the form attributes.
     *
     * @param html
     *            the html
     *
     * @return the string
     */
    protected String removeFormAttributes(String html) {
        // remove method from form tags
        if (removeFormAttributes) {
            html = formMethodAttrPattern.matcher(html).replaceAll("$1$3");
        }
        return html;
    }

    /**
     * Removes the input attributes.
     *
     * @param html
     *            the html
     *
     * @return the string
     */
    protected String removeInputAttributes(String html) {
        // remove type from input tags
        if (removeInputAttributes) {
            html = inputTypeAttrPattern.matcher(html).replaceAll("$1$3");
        }
        return html;
    }

    /**
     * Simple boolean attributes.
     *
     * @param html
     *            the html
     *
     * @return the string
     */
    protected String simpleBooleanAttributes(String html) {
        // simplify boolean attributes
        if (simpleBooleanAttributes) {
            html = booleanAttrPattern.matcher(html).replaceAll("$1$2$4");
        }
        return html;
    }

    /**
     * Removes the http protocol.
     *
     * @param html
     *            the html
     *
     * @return the string
     */
    protected String removeHttpProtocol(String html) {
        // remove http protocol from tag attributes
        if (removeHttpProtocol) {
            Matcher matcher = httpProtocolPattern.matcher(html);
            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                // if rel!=external
                if (!relExternalPattern.matcher(matcher.group(0)).matches()) {
                    matcher.appendReplacement(sb, "$1$2");
                } else {
                    matcher.appendReplacement(sb, "$0");
                }
            }
            matcher.appendTail(sb);
            html = sb.toString();
        }
        return html;
    }

    /**
     * Removes the https protocol.
     *
     * @param html
     *            the html
     *
     * @return the string
     */
    protected String removeHttpsProtocol(String html) {
        // remove https protocol from tag attributes
        if (removeHttpsProtocol) {
            Matcher matcher = httpsProtocolPattern.matcher(html);
            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                // if rel!=external
                if (!relExternalPattern.matcher(matcher.group(0)).matches()) {
                    matcher.appendReplacement(sb, "$1$2");
                } else {
                    matcher.appendReplacement(sb, "$0");
                }
            }
            matcher.appendTail(sb);
            html = sb.toString();
        }
        return html;
    }

    /**
     * Process preserved blocks.
     *
     * @param preBlocks
     *            the pre blocks
     * @param taBlocks
     *            the ta blocks
     * @param scriptBlocks
     *            the script blocks
     * @param styleBlocks
     *            the style blocks
     * @param eventBlocks
     *            the event blocks
     * @param condCommentBlocks
     *            the cond comment blocks
     * @param skipBlocks
     *            the skip blocks
     * @param lineBreakBlocks
     *            the line break blocks
     * @param userBlocks
     *            the user blocks
     */
    protected void processPreservedBlocks(List<String> preBlocks, List<String> taBlocks, List<String> scriptBlocks,
            List<String> styleBlocks, List<String> eventBlocks, List<String> condCommentBlocks, List<String> skipBlocks,
            List<String> lineBreakBlocks, List<List<String>> userBlocks) {
        processPreBlocks(preBlocks);
        processTextAreaBlocks(taBlocks);
        processScriptBlocks(scriptBlocks);
        processStyleBlocks(styleBlocks);
        processEventBlocks(eventBlocks);
        processCondCommentBlocks(condCommentBlocks);
        processSkipBlocks(skipBlocks);
        processUserBlocks(userBlocks);
        processLineBreakBlocks(lineBreakBlocks);
    }

    /**
     * Process pre blocks.
     *
     * @param preBlocks
     *            the pre blocks
     */
    protected void processPreBlocks(List<String> preBlocks) {
        if (generateStatistics) {
            for (String block : preBlocks) {
                statistics.setPreservedSize(statistics.getPreservedSize() + block.length());
            }
        }
    }

    /**
     * Process text area blocks.
     *
     * @param taBlocks
     *            the ta blocks
     */
    protected void processTextAreaBlocks(List<String> taBlocks) {
        if (generateStatistics) {
            for (String block : taBlocks) {
                statistics.setPreservedSize(statistics.getPreservedSize() + block.length());
            }
        }
    }

    /**
     * Process cond comment blocks.
     *
     * @param condCommentBlocks
     *            the cond comment blocks
     */
    protected void processCondCommentBlocks(List<String> condCommentBlocks) {
        if (generateStatistics) {
            for (String block : condCommentBlocks) {
                statistics.setPreservedSize(statistics.getPreservedSize() + block.length());
            }
        }
    }

    /**
     * Process skip blocks.
     *
     * @param skipBlocks
     *            the skip blocks
     */
    protected void processSkipBlocks(List<String> skipBlocks) {
        if (generateStatistics) {
            for (String block : skipBlocks) {
                statistics.setPreservedSize(statistics.getPreservedSize() + block.length());
            }
        }
    }

    /**
     * Process line break blocks.
     *
     * @param lineBreakBlocks
     *            the line break blocks
     */
    protected void processLineBreakBlocks(List<String> lineBreakBlocks) {
        if (generateStatistics) {
            for (String block : lineBreakBlocks) {
                statistics.setPreservedSize(statistics.getPreservedSize() + block.length());
            }
        }
    }

    /**
     * Process user blocks.
     *
     * @param userBlocks
     *            the user blocks
     */
    protected void processUserBlocks(List<List<String>> userBlocks) {
        if (generateStatistics) {
            for (List<String> blockList : userBlocks) {
                for (String block : blockList) {
                    statistics.setPreservedSize(statistics.getPreservedSize() + block.length());
                }
            }
        }
    }

    /**
     * Process event blocks.
     *
     * @param eventBlocks
     *            the event blocks
     */
    protected void processEventBlocks(List<String> eventBlocks) {

        if (generateStatistics) {
            for (String block : eventBlocks) {
                statistics.getOriginalMetrics()
                        .setInlineEventSize(statistics.getOriginalMetrics().getInlineEventSize() + block.length());
            }
        }

        if (removeJavaScriptProtocol) {
            for (int i = 0; i < eventBlocks.size(); i++) {
                eventBlocks.set(i, removeJavaScriptProtocol(eventBlocks.get(i)));
            }
        } else if (generateStatistics) {
            for (String block : eventBlocks) {
                statistics.setPreservedSize(statistics.getPreservedSize() + block.length());
            }
        }

        if (generateStatistics) {
            for (String block : eventBlocks) {
                statistics.getCompressedMetrics()
                        .setInlineEventSize(statistics.getCompressedMetrics().getInlineEventSize() + block.length());
            }
        }
    }

    /**
     * Removes the java script protocol.
     *
     * @param source
     *            the source
     *
     * @return the string
     */
    protected String removeJavaScriptProtocol(String source) {
        // remove javascript: from inline events
        String result = source;

        Matcher matcher = eventJsProtocolPattern.matcher(source);
        if (matcher.matches()) {
            result = matcher.replaceFirst("$1");
        }

        if (generateStatistics) {
            statistics.setPreservedSize(statistics.getPreservedSize() + result.length());
        }

        return result;
    }

    /**
     * Process script blocks.
     *
     * @param scriptBlocks
     *            the script blocks
     */
    protected void processScriptBlocks(List<String> scriptBlocks) {

        if (generateStatistics) {
            for (String block : scriptBlocks) {
                statistics.getOriginalMetrics()
                        .setInlineScriptSize(statistics.getOriginalMetrics().getInlineScriptSize() + block.length());
            }
        }

        if (compressJavaScript) {
            for (int i = 0; i < scriptBlocks.size(); i++) {
                scriptBlocks.set(i, compressJavaScript(scriptBlocks.get(i)));
            }
        } else if (generateStatistics) {
            for (String block : scriptBlocks) {
                statistics.setPreservedSize(statistics.getPreservedSize() + block.length());
            }
        }

        if (generateStatistics) {
            for (String block : scriptBlocks) {
                statistics.getCompressedMetrics()
                        .setInlineScriptSize(statistics.getCompressedMetrics().getInlineScriptSize() + block.length());
            }
        }
    }

    /**
     * Process style blocks.
     *
     * @param styleBlocks
     *            the style blocks
     */
    protected void processStyleBlocks(List<String> styleBlocks) {

        if (generateStatistics) {
            for (String block : styleBlocks) {
                statistics.getOriginalMetrics()
                        .setInlineStyleSize(statistics.getOriginalMetrics().getInlineStyleSize() + block.length());
            }
        }

        if (compressCss) {
            for (int i = 0; i < styleBlocks.size(); i++) {
                styleBlocks.set(i, compressCssStyles(styleBlocks.get(i)));
            }
        } else if (generateStatistics) {
            for (String block : styleBlocks) {
                statistics.setPreservedSize(statistics.getPreservedSize() + block.length());
            }
        }

        if (generateStatistics) {
            for (String block : styleBlocks) {
                statistics.getCompressedMetrics()
                        .setInlineStyleSize(statistics.getCompressedMetrics().getInlineStyleSize() + block.length());
            }
        }
    }

    /**
     * Compress java script.
     *
     * @param source
     *            the source
     *
     * @return the string
     */
    protected String compressJavaScript(String source) {

        // set default javascript compressor
        if (javaScriptCompressor == null) {
            YuiJavaScriptCompressor yuiJsCompressor = new YuiJavaScriptCompressor();
            yuiJsCompressor.setNoMunge(yuiJsNoMunge);
            yuiJsCompressor.setPreserveAllSemiColons(yuiJsPreserveAllSemiColons);
            yuiJsCompressor.setDisableOptimizations(yuiJsDisableOptimizations);
            yuiJsCompressor.setLineBreak(yuiJsLineBreak);

            if (yuiErrorReporter != null) {
                yuiJsCompressor.setErrorReporter(yuiErrorReporter);
            }

            javaScriptCompressor = yuiJsCompressor;
        }

        // detect CDATA wrapper
        boolean cdataWrapper = false;
        Matcher matcher = cdataPattern.matcher(source);
        if (matcher.matches()) {
            cdataWrapper = true;
            source = matcher.group(1);
        }

        String result = javaScriptCompressor.compress(source);

        if (cdataWrapper) {
            result = "<![CDATA[" + result + "]]>";
        }

        return result;

    }

    /**
     * Compress css styles.
     *
     * @param source
     *            the source
     *
     * @return the string
     */
    protected String compressCssStyles(String source) {

        // set default css compressor
        if (cssCompressor == null) {
            YuiCssCompressor yuiCssCompressor = new YuiCssCompressor();
            yuiCssCompressor.setLineBreak(yuiCssLineBreak);

            cssCompressor = yuiCssCompressor;
        }

        // detect CDATA wrapper
        boolean cdataWrapper = false;
        Matcher matcher = cdataPattern.matcher(source);
        if (matcher.matches()) {
            cdataWrapper = true;
            source = matcher.group(1);
        }

        String result = cssCompressor.compress(source);

        if (cdataWrapper) {
            result = "<![CDATA[" + result + "]]>";
        }

        return result;

    }

    /**
     * Creates the compressor clone.
     *
     * @return the html compressor
     */
    protected HtmlCompressor createCompressorClone() {
        HtmlCompressor clone = new HtmlCompressor();
        clone.setJavaScriptCompressor(javaScriptCompressor);
        clone.setCssCompressor(cssCompressor);
        clone.setRemoveComments(removeComments);
        clone.setRemoveMultiSpaces(removeMultiSpaces);
        clone.setRemoveIntertagSpaces(removeIntertagSpaces);
        clone.setRemoveQuotes(removeQuotes);
        clone.setCompressJavaScript(compressJavaScript);
        clone.setCompressCss(compressCss);
        clone.setSimpleDoctype(simpleDoctype);
        clone.setRemoveScriptAttributes(removeScriptAttributes);
        clone.setRemoveStyleAttributes(removeStyleAttributes);
        clone.setRemoveLinkAttributes(removeLinkAttributes);
        clone.setRemoveFormAttributes(removeFormAttributes);
        clone.setRemoveInputAttributes(removeInputAttributes);
        clone.setSimpleBooleanAttributes(simpleBooleanAttributes);
        clone.setRemoveJavaScriptProtocol(removeJavaScriptProtocol);
        clone.setRemoveHttpProtocol(removeHttpProtocol);
        clone.setRemoveHttpsProtocol(removeHttpsProtocol);
        clone.setPreservePatterns(preservePatterns);
        clone.setYuiJsNoMunge(yuiJsNoMunge);
        clone.setYuiJsPreserveAllSemiColons(yuiJsPreserveAllSemiColons);
        clone.setYuiJsDisableOptimizations(yuiJsDisableOptimizations);
        clone.setYuiJsLineBreak(yuiJsLineBreak);
        clone.setYuiCssLineBreak(yuiCssLineBreak);
        clone.setYuiErrorReporter(yuiErrorReporter);

        return clone;

    }

    /**
     * Returns <code>true</code> if JavaScript compression is enabled.
     *
     * @return current state of JavaScript compression.
     */
    public boolean isCompressJavaScript() {
        return compressJavaScript;
    }

    /**
     * Enables JavaScript compression within &lt;script&gt; tags using
     * <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a> if set to <code>true</code>.
     * Default is <code>false</code> for performance reasons.
     * <p>
     * <b>Note:</b> Compressing JavaScript is not recommended if pages are compressed dynamically on-the-fly because of
     * performance impact. You should consider putting JavaScript into a separate file and compressing it using
     * standalone YUICompressor for example.
     * </p>
     *
     * @param compressJavaScript
     *            set <code>true</code> to enable JavaScript compression. Default is <code>false</code>
     *
     * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
     */
    public void setCompressJavaScript(boolean compressJavaScript) {
        this.compressJavaScript = compressJavaScript;
    }

    /**
     * Returns <code>true</code> if CSS compression is enabled.
     *
     * @return current state of CSS compression.
     */
    public boolean isCompressCss() {
        return compressCss;
    }

    /**
     * Enables CSS compression within &lt;style&gt; tags using
     * <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a> if set to <code>true</code>.
     * Default is <code>false</code> for performance reasons.
     * <p>
     * <b>Note:</b> Compressing CSS is not recommended if pages are compressed dynamically on-the-fly because of
     * performance impact. You should consider putting CSS into a separate file and compressing it using standalone
     * YUICompressor for example.
     * </p>
     *
     * @param compressCss
     *            set <code>true</code> to enable CSS compression. Default is <code>false</code>
     *
     * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
     */
    public void setCompressCss(boolean compressCss) {
        this.compressCss = compressCss;
    }

    /**
     * Returns <code>true</code> if Yahoo YUI Compressor will only minify javascript without obfuscating local symbols.
     * This corresponds to <code>--nomunge</code> command line option.
     *
     * @return <code>nomunge</code> parameter value used for JavaScript compression.
     *
     * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
     */
    public boolean isYuiJsNoMunge() {
        return yuiJsNoMunge;
    }

    /**
     * Tells Yahoo YUI Compressor to only minify javascript without obfuscating local symbols. This corresponds to
     * <code>--nomunge</code> command line option. This option has effect only if JavaScript compression is enabled.
     * Default is <code>false</code>.
     *
     * @param yuiJsNoMunge
     *            set <code>true</code> to enable <code>nomunge</code> mode
     *
     * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
     */
    public void setYuiJsNoMunge(boolean yuiJsNoMunge) {
        this.yuiJsNoMunge = yuiJsNoMunge;
    }

    /**
     * Returns <code>true</code> if Yahoo YUI Compressor will preserve unnecessary semicolons during JavaScript
     * compression. This corresponds to <code>--preserve-semi</code> command line option.
     *
     * @return <code>preserve-semi</code> parameter value used for JavaScript compression.
     *
     * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
     */
    public boolean isYuiJsPreserveAllSemiColons() {
        return yuiJsPreserveAllSemiColons;
    }

    /**
     * Tells Yahoo YUI Compressor to preserve unnecessary semicolons during JavaScript compression. This corresponds to
     * <code>--preserve-semi</code> command line option. This option has effect only if JavaScript compression is
     * enabled. Default is <code>false</code>.
     *
     * @param yuiJsPreserveAllSemiColons
     *            set <code>true</code> to enable <code>preserve-semi</code> mode
     *
     * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
     */
    public void setYuiJsPreserveAllSemiColons(boolean yuiJsPreserveAllSemiColons) {
        this.yuiJsPreserveAllSemiColons = yuiJsPreserveAllSemiColons;
    }

    /**
     * Returns <code>true</code> if Yahoo YUI Compressor will disable all the built-in micro optimizations during
     * JavaScript compression. This corresponds to <code>--disable-optimizations</code> command line option.
     *
     * @return <code>disable-optimizations</code> parameter value used for JavaScript compression.
     *
     * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
     */
    public boolean isYuiJsDisableOptimizations() {
        return yuiJsDisableOptimizations;
    }

    /**
     * Tells Yahoo YUI Compressor to disable all the built-in micro optimizations during JavaScript compression. This
     * corresponds to <code>--disable-optimizations</code> command line option. This option has effect only if
     * JavaScript compression is enabled. Default is <code>false</code>.
     *
     * @param yuiJsDisableOptimizations
     *            set <code>true</code> to enable <code>disable-optimizations</code> mode
     *
     * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
     */
    public void setYuiJsDisableOptimizations(boolean yuiJsDisableOptimizations) {
        this.yuiJsDisableOptimizations = yuiJsDisableOptimizations;
    }

    /**
     * Returns number of symbols per line Yahoo YUI Compressor will use during JavaScript compression. This corresponds
     * to <code>--line-break</code> command line option.
     *
     * @return <code>line-break</code> parameter value used for JavaScript compression.
     *
     * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
     */
    public int getYuiJsLineBreak() {
        return yuiJsLineBreak;
    }

    /**
     * Tells Yahoo YUI Compressor to break lines after the specified number of symbols during JavaScript compression.
     * This corresponds to <code>--line-break</code> command line option. This option has effect only if JavaScript
     * compression is enabled. Default is <code>-1</code> to disable line breaks.
     *
     * @param yuiJsLineBreak
     *            set number of symbols per line
     *
     * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
     */
    public void setYuiJsLineBreak(int yuiJsLineBreak) {
        this.yuiJsLineBreak = yuiJsLineBreak;
    }

    /**
     * Returns number of symbols per line Yahoo YUI Compressor will use during CSS compression. This corresponds to
     * <code>--line-break</code> command line option.
     *
     * @return <code>line-break</code> parameter value used for CSS compression.
     *
     * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
     */
    public int getYuiCssLineBreak() {
        return yuiCssLineBreak;
    }

    /**
     * Tells Yahoo YUI Compressor to break lines after the specified number of symbols during CSS compression. This
     * corresponds to <code>--line-break</code> command line option. This option has effect only if CSS compression is
     * enabled. Default is <code>-1</code> to disable line breaks.
     *
     * @param yuiCssLineBreak
     *            set number of symbols per line
     *
     * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
     */
    public void setYuiCssLineBreak(int yuiCssLineBreak) {
        this.yuiCssLineBreak = yuiCssLineBreak;
    }

    /**
     * Returns <code>true</code> if all unnecessary quotes will be removed from tag attributes.
     *
     * @return true, if is removes the quotes
     */
    public boolean isRemoveQuotes() {
        return removeQuotes;
    }

    /**
     * If set to <code>true</code> all unnecessary quotes will be removed from tag attributes. Default is
     * <code>false</code>.
     * <p>
     * <b>Note:</b> Even though quotes are removed only when it is safe to do so, it still might break strict HTML
     * validation. Turn this option on only if a page validation is not very important or to squeeze the most out of the
     * compression. This option has no performance impact.
     *
     * @param removeQuotes
     *            set <code>true</code> to remove unnecessary quotes from tag attributes
     */
    public void setRemoveQuotes(boolean removeQuotes) {
        this.removeQuotes = removeQuotes;
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
     * Returns <code>true</code> if all HTML comments will be removed.
     *
     * @return <code>true</code> if all HTML comments will be removed
     */
    public boolean isRemoveComments() {
        return removeComments;
    }

    /**
     * If set to <code>true</code> all HTML comments will be removed. Default is <code>true</code>.
     *
     * @param removeComments
     *            set <code>true</code> to remove all HTML comments
     */
    public void setRemoveComments(boolean removeComments) {
        this.removeComments = removeComments;
    }

    /**
     * Returns <code>true</code> if all multiple whitespace characters will be replaced with single spaces.
     *
     * @return <code>true</code> if all multiple whitespace characters will be replaced with single spaces.
     */
    public boolean isRemoveMultiSpaces() {
        return removeMultiSpaces;
    }

    /**
     * If set to <code>true</code> all multiple whitespace characters will be replaced with single spaces. Default is
     * <code>true</code>.
     *
     * @param removeMultiSpaces
     *            set <code>true</code> to replace all multiple whitespace characters will single spaces.
     */
    public void setRemoveMultiSpaces(boolean removeMultiSpaces) {
        this.removeMultiSpaces = removeMultiSpaces;
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
     * If set to <code>true</code> all inter-tag whitespace characters will be removed. Default is <code>false</code>.
     * <p>
     * <b>Note:</b> It is fairly safe to turn this option on unless you rely on spaces for page formatting. Even if you
     * do, you can always preserve required spaces with <code>&amp;nbsp;</code>. This option has no performance impact.
     *
     * @param removeIntertagSpaces
     *            set <code>true</code> to remove all inter-tag whitespace characters
     */
    public void setRemoveIntertagSpaces(boolean removeIntertagSpaces) {
        this.removeIntertagSpaces = removeIntertagSpaces;
    }

    /**
     * Returns a list of Patterns defining custom preserving block rules.
     *
     * @return list of <code>Pattern</code> objects defining rules for preserving block rules
     */
    public List<Pattern> getPreservePatterns() {
        return preservePatterns;
    }

    /**
     * This method allows setting custom block preservation rules defined by regular expression patterns. Blocks that
     * match provided patterns will be skipped during HTML compression.
     * <p>
     * Custom preservation rules have higher priority than default rules. Priority between custom rules are defined by
     * their position in a list (beginning of a list has higher priority).
     * <p>
     * Besides custom patterns, you can use 3 predefined patterns: {@link #PHP_TAG_PATTERN PHP_TAG_PATTERN},
     * {@link #SERVER_SCRIPT_TAG_PATTERN SERVER_SCRIPT_TAG_PATTERN}, {@link #SERVER_SIDE_INCLUDE_PATTERN
     * SERVER_SIDE_INCLUDE_PATTERN}.
     *
     * @param preservePatterns
     *            List of <code>Pattern</code> objects that will be used to skip matched blocks during compression
     */
    public void setPreservePatterns(List<Pattern> preservePatterns) {
        this.preservePatterns = preservePatterns;
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
    public ErrorReporter getYuiErrorReporter() {
        return yuiErrorReporter;
    }

    /**
     * Sets <code>ErrorReporter</code> that YUI Compressor will use for reporting errors during JavaScript compression.
     * If no <code>ErrorReporter</code> was provided {@link YuiJavaScriptCompressor.DefaultErrorReporter} will be used
     * which reports errors to <code>System.err</code> stream.
     *
     * @param yuiErrorReporter
     *            <code>ErrorReporter</code> that will be used by YUI Compressor
     *
     * @see YuiJavaScriptCompressor.DefaultErrorReporter
     * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
     * @see <a href="http://www.mozilla.org/rhino/apidocs/org/mozilla/javascript/ErrorReporter.html">ErrorReporter
     *      Interface</a>
     */
    public void setYuiErrorReporter(ErrorReporter yuiErrorReporter) {
        this.yuiErrorReporter = yuiErrorReporter;
    }

    /**
     * Returns JavaScript compressor implementation that will be used to compress inline JavaScript in HTML.
     *
     * @return <code>Compressor</code> implementation that will be used to compress inline JavaScript in HTML.
     *
     * @see YuiJavaScriptCompressor
     * @see ClosureJavaScriptCompressor
     * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
     * @see <a href="http://code.google.com/closure/compiler/">Google Closure Compiler</a>
     */
    public Compressor getJavaScriptCompressor() {
        return javaScriptCompressor;
    }

    /**
     * Sets JavaScript compressor implementation that will be used to compress inline JavaScript in HTML.
     * <p>
     * HtmlCompressor currently comes with basic implementations for
     * <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a> (called
     * {@link YuiJavaScriptCompressor}) and <a href="http://code.google.com/closure/compiler/">Google Closure
     * Compiler</a> (called {@link ClosureJavaScriptCompressor}) that should be enough for most cases, but users can
     * also create their own JavaScript compressors for custom needs.
     * <p>
     * If no compressor is set {@link YuiJavaScriptCompressor} will be used by default.
     *
     * @param javaScriptCompressor
     *            {@link Compressor} implementation that will be used for inline JavaScript compression
     *
     * @see YuiJavaScriptCompressor
     * @see ClosureJavaScriptCompressor
     * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
     * @see <a href="http://code.google.com/closure/compiler/">Google Closure Compiler</a>
     */
    public void setJavaScriptCompressor(Compressor javaScriptCompressor) {
        this.javaScriptCompressor = javaScriptCompressor;
    }

    /**
     * Returns CSS compressor implementation that will be used to compress inline CSS in HTML.
     *
     * @return <code>Compressor</code> implementation that will be used to compress inline CSS in HTML.
     *
     * @see YuiCssCompressor
     * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
     */
    public Compressor getCssCompressor() {
        return cssCompressor;
    }

    /**
     * Sets CSS compressor implementation that will be used to compress inline CSS in HTML.
     * <p>
     * HtmlCompressor currently comes with basic implementation for
     * <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a> (called {@link YuiCssCompressor}),
     * but users can also create their own CSS compressors for custom needs.
     * <p>
     * If no compressor is set {@link YuiCssCompressor} will be used by default.
     *
     * @param cssCompressor
     *            {@link Compressor} implementation that will be used for inline CSS compression
     *
     * @see YuiCssCompressor
     * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
     */
    public void setCssCompressor(Compressor cssCompressor) {
        this.cssCompressor = cssCompressor;
    }

    /**
     * Returns <code>true</code> if existing DOCTYPE declaration will be replaced with simple
     * <code>&lt;!DOCTYPE html&gt;</code> declaration.
     *
     * @return <code>true</code> if existing DOCTYPE declaration will be replaced with simple
     *         <code>&lt;!DOCTYPE html&gt;</code> declaration.
     */
    public boolean isSimpleDoctype() {
        return simpleDoctype;
    }

    /**
     * If set to <code>true</code>, existing DOCTYPE declaration will be replaced with simple
     * <code>&lt;!DOCTYPE html&gt;</code> declaration. Default is <code>false</code>.
     *
     * @param simpleDoctype
     *            set <code>true</code> to replace existing DOCTYPE declaration with <code>&lt;!DOCTYPE html&gt;</code>
     */
    public void setSimpleDoctype(boolean simpleDoctype) {
        this.simpleDoctype = simpleDoctype;
    }

    /**
     * Returns <code>true</code> if unnecessary attributes will be removed from <code>&lt;script&gt;</code> tags.
     *
     * @return <code>true</code> if unnecessary attributes will be removed from <code>&lt;script&gt;</code> tags
     */
    public boolean isRemoveScriptAttributes() {
        return removeScriptAttributes;
    }

    /**
     * If set to <code>true</code>, following attributes will be removed from <code>&lt;script&gt;</code> tags:
     * <ul>
     * <li>type="text/javascript"</li>
     * <li>type="application/javascript"</li>
     * <li>language="javascript"</li>
     * </ul>
     * <p>
     * Default is <code>false</code>.
     *
     * @param removeScriptAttributes
     *            set <code>true</code> to remove unnecessary attributes from <code>&lt;script&gt;</code> tags
     */
    public void setRemoveScriptAttributes(boolean removeScriptAttributes) {
        this.removeScriptAttributes = removeScriptAttributes;
    }

    /**
     * Returns <code>true</code> if <code>type="text/style"</code> attributes will be removed from
     * <code>&lt;style&gt;</code> tags.
     *
     * @return <code>true</code> if <code>type="text/style"</code> attributes will be removed from
     *         <code>&lt;style&gt;</code> tags
     */
    public boolean isRemoveStyleAttributes() {
        return removeStyleAttributes;
    }

    /**
     * If set to <code>true</code>, <code>type="text/style"</code> attributes will be removed from
     * <code>&lt;style&gt;</code> tags. Default is <code>false</code>.
     *
     * @param removeStyleAttributes
     *            set <code>true</code> to remove <code>type="text/style"</code> attributes from
     *            <code>&lt;style&gt;</code> tags
     */
    public void setRemoveStyleAttributes(boolean removeStyleAttributes) {
        this.removeStyleAttributes = removeStyleAttributes;
    }

    /**
     * Returns <code>true</code> if unnecessary attributes will be removed from <code>&lt;link&gt;</code> tags.
     *
     * @return <code>true</code> if unnecessary attributes will be removed from <code>&lt;link&gt;</code> tags
     */
    public boolean isRemoveLinkAttributes() {
        return removeLinkAttributes;
    }

    /**
     * If set to <code>true</code>, following attributes will be removed from <code>&lt;link rel="stylesheet"&gt;</code>
     * and <code>&lt;link rel="alternate stylesheet"&gt;</code> tags:
     * <ul>
     * <li>type="text/css"</li>
     * <li>type="text/plain"</li>
     * </ul>
     * <p>
     * Default is <code>false</code>.
     *
     * @param removeLinkAttributes
     *            set <code>true</code> to remove unnecessary attributes from <code>&lt;link&gt;</code> tags
     */
    public void setRemoveLinkAttributes(boolean removeLinkAttributes) {
        this.removeLinkAttributes = removeLinkAttributes;
    }

    /**
     * Returns <code>true</code> if <code>method="get"</code> attributes will be removed from <code>&lt;form&gt;</code>
     * tags.
     *
     * @return <code>true</code> if <code>method="get"</code> attributes will be removed from <code>&lt;form&gt;</code>
     *         tags
     */
    public boolean isRemoveFormAttributes() {
        return removeFormAttributes;
    }

    /**
     * If set to <code>true</code>, <code>method="get"</code> attributes will be removed from <code>&lt;form&gt;</code>
     * tags. Default is <code>false</code>.
     *
     * @param removeFormAttributes
     *            set <code>true</code> to remove <code>method="get"</code> attributes from <code>&lt;form&gt;</code>
     *            tags
     */
    public void setRemoveFormAttributes(boolean removeFormAttributes) {
        this.removeFormAttributes = removeFormAttributes;
    }

    /**
     * Returns <code>true</code> if <code>type="text"</code> attributes will be removed from <code>&lt;input&gt;</code>
     * tags.
     *
     * @return <code>true</code> if <code>type="text"</code> attributes will be removed from <code>&lt;input&gt;</code>
     *         tags
     */
    public boolean isRemoveInputAttributes() {
        return removeInputAttributes;
    }

    /**
     * If set to <code>true</code>, <code>type="text"</code> attributes will be removed from <code>&lt;input&gt;</code>
     * tags. Default is <code>false</code>.
     *
     * @param removeInputAttributes
     *            set <code>true</code> to remove <code>type="text"</code> attributes from <code>&lt;input&gt;</code>
     *            tags
     */
    public void setRemoveInputAttributes(boolean removeInputAttributes) {
        this.removeInputAttributes = removeInputAttributes;
    }

    /**
     * Returns <code>true</code> if boolean attributes will be simplified.
     *
     * @return <code>true</code> if boolean attributes will be simplified
     */
    public boolean isSimpleBooleanAttributes() {
        return simpleBooleanAttributes;
    }

    /**
     * If set to <code>true</code>, any values of following boolean attributes will be removed:
     * <ul>
     * <li>checked</li>
     * <li>selected</li>
     * <li>disabled</li>
     * <li>readonly</li>
     * </ul>
     * <p>
     * For example, <code>&lt;input readonly="readonly"&gt;</code> would become <code>&lt;input readonly&gt;</code>
     * <p>
     * Default is <code>false</code>.
     *
     * @param simpleBooleanAttributes
     *            set <code>true</code> to simplify boolean attributes
     */
    public void setSimpleBooleanAttributes(boolean simpleBooleanAttributes) {
        this.simpleBooleanAttributes = simpleBooleanAttributes;
    }

    /**
     * Returns <code>true</code> if <code>javascript:</code> pseudo-protocol will be removed from inline event handlers.
     *
     * @return <code>true</code> if <code>javascript:</code> pseudo-protocol will be removed from inline event handlers.
     */
    public boolean isRemoveJavaScriptProtocol() {
        return removeJavaScriptProtocol;
    }

    /**
     * If set to <code>true</code>, <code>javascript:</code> pseudo-protocol will be removed from inline event handlers.
     * <p>
     * For example, <code>&lt;a onclick="javascript:alert()"&gt;</code> would become
     * <code>&lt;a onclick="alert()"&gt;</code>
     * <p>
     * Default is <code>false</code>.
     *
     * @param removeJavaScriptProtocol
     *            set <code>true</code> to remove <code>javascript:</code> pseudo-protocol from inline event handlers.
     */
    public void setRemoveJavaScriptProtocol(boolean removeJavaScriptProtocol) {
        this.removeJavaScriptProtocol = removeJavaScriptProtocol;
    }

    /**
     * Returns <code>true</code> if <code>HTTP</code> protocol will be removed from <code>href</code>, <code>src</code>,
     * <code>cite</code>, and <code>action</code> tag attributes.
     *
     * @return <code>true</code> if <code>HTTP</code> protocol will be removed from <code>href</code>, <code>src</code>,
     *         <code>cite</code>, and <code>action</code> tag attributes.
     */
    public boolean isRemoveHttpProtocol() {
        return removeHttpProtocol;
    }

    /**
     * If set to <code>true</code>, <code>HTTP</code> protocol will be removed from <code>href</code>, <code>src</code>,
     * <code>cite</code>, and <code>action</code> tag attributes. URL without a protocol would make a browser use
     * document's current protocol instead.
     * <p>
     * Tags marked with <code>rel="external"</code> will be skipped.
     * <p>
     * For example:
     * <p>
     * <code>&lt;a href="http://example.com"&gt; &lt;script src="http://google.com/js.js" rel="external"&gt;</code>
     * <p>
     * would become:
     * <p>
     * <code>&lt;a href="//example.com"&gt; &lt;script src="http://google.com/js.js" rel="external"&gt;</code>
     * <p>
     * Default is <code>false</code>.
     *
     * @param removeHttpProtocol
     *            set <code>true</code> to remove <code>HTTP</code> protocol from tag attributes
     */
    public void setRemoveHttpProtocol(boolean removeHttpProtocol) {
        this.removeHttpProtocol = removeHttpProtocol;
    }

    /**
     * Returns <code>true</code> if <code>HTTPS</code> protocol will be removed from <code>href</code>, <code>src</code>
     * , <code>cite</code>, and <code>action</code> tag attributes.
     *
     * @return <code>true</code> if <code>HTTPS</code> protocol will be removed from <code>href</code>, <code>src</code>
     *         , <code>cite</code>, and <code>action</code> tag attributes.
     */
    public boolean isRemoveHttpsProtocol() {
        return removeHttpsProtocol;
    }

    /**
     * If set to <code>true</code>, <code>HTTPS</code> protocol will be removed from <code>href</code>, <code>src</code>
     * , <code>cite</code>, and <code>action</code> tag attributes. URL without a protocol would make a browser use
     * document's current protocol instead.
     * <p>
     * Tags marked with <code>rel="external"</code> will be skipped.
     * <p>
     * For example:
     * <p>
     * <code>&lt;a href="https://example.com"&gt; &lt;script src="https://google.com/js.js" rel="external"&gt;</code>
     * <p>
     * would become:
     * <p>
     * <code>&lt;a href="//example.com"&gt; &lt;script src="https://google.com/js.js" rel="external"&gt;</code>
     * <p>
     * Default is <code>false</code>.
     *
     * @param removeHttpsProtocol
     *            set <code>true</code> to remove <code>HTTP</code> protocol from tag attributes
     */
    public void setRemoveHttpsProtocol(boolean removeHttpsProtocol) {
        this.removeHttpsProtocol = removeHttpsProtocol;
    }

    /**
     * Returns <code>true</code> if HTML compression statistics is generated.
     *
     * @return <code>true</code> if HTML compression statistics is generated
     */
    public boolean isGenerateStatistics() {
        return generateStatistics;
    }

    /**
     * If set to <code>true</code>, HTML compression statistics will be generated.
     * <p>
     * <strong>Important:</strong> Enabling statistics makes HTML compressor not thread safe.
     * <p>
     * Default is <code>false</code>.
     *
     * @param generateStatistics
     *            set <code>true</code> to generate HTML compression statistics
     *
     * @see #getStatistics()
     */
    public void setGenerateStatistics(boolean generateStatistics) {
        this.generateStatistics = generateStatistics;
    }

    /**
     * Returns {@link HtmlCompressorStatistics} object containing statistics of the last HTML compression, if enabled.
     * Should be called after {@link #compress(String)}
     *
     * @return {@link HtmlCompressorStatistics} object containing last HTML compression statistics
     *
     * @see HtmlCompressorStatistics
     * @see #setGenerateStatistics(boolean)
     */
    public HtmlCompressorStatistics getStatistics() {
        return statistics;
    }

    /**
     * Returns <code>true</code> if line breaks will be preserved.
     *
     * @return <code>true</code> if line breaks will be preserved.
     */
    public boolean isPreserveLineBreaks() {
        return preserveLineBreaks;
    }

    /**
     * If set to <code>true</code>, line breaks will be preserved.
     * <p>
     * Default is <code>false</code>.
     *
     * @param preserveLineBreaks
     *            set <code>true</code> to preserve line breaks
     */
    public void setPreserveLineBreaks(boolean preserveLineBreaks) {
        this.preserveLineBreaks = preserveLineBreaks;
    }

    /**
     * Returns a comma separated list of tags around which spaces will be removed.
     *
     * @return a comma separated list of tags around which spaces will be removed.
     */
    public String getRemoveSurroundingSpaces() {
        return removeSurroundingSpaces;
    }

    /**
     * Enables surrounding spaces removal around provided comma separated list of tags.
     * <p>
     * Besides custom defined lists, you can pass one of 3 predefined lists of tags: {@link #BLOCK_TAGS_MIN
     * BLOCK_TAGS_MIN}, {@link #BLOCK_TAGS_MAX BLOCK_TAGS_MAX}, {@link #ALL_TAGS ALL_TAGS}.
     *
     * @param tagList
     *            a comma separated list of tags around which spaces will be removed
     */
    public void setRemoveSurroundingSpaces(String tagList) {
        if (tagList != null && tagList.length() == 0) {
            tagList = null;
        }
        this.removeSurroundingSpaces = tagList;
    }

}
