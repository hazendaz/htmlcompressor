/*
 *    Copyright 2009-2026 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.el.ELContext;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.BodyContent;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Collections;
import java.util.Enumeration;

import org.junit.jupiter.api.Test;

/**
 * Tests for the four JSP compression tags.
 * <p>
 * A minimal JSP infrastructure ({@link StubJspWriter}, {@link StubBodyContent}, {@link StubPageContext}) is provided as
 * static inner classes so that {@code doEndTag()} can be exercised without a running servlet container.
 */
class CompressorTagsTest {

    private static final String HTML_CONTENT = "<html>  <!-- comment -->  <body>  <p>hello  world</p>  </body>  </html>";
    private static final String XML_CONTENT = "<root>  <!-- comment -->  <child>  text  </child>  </root>";
    private static final String CSS_CONTENT = "body  {  color :  red ;  margin :  0  ;  }";
    private static final String JS_CONTENT = "function  add ( a , b )  {  return  a + b ;  }";

    // -----------------------------------------------------------------------
    // HtmlCompressorTag
    // -----------------------------------------------------------------------

    @Test
    void testHtmlTagSetters() {
        HtmlCompressorTag tag = new HtmlCompressorTag();
        // exercise every setter so they are all covered
        tag.setEnabled(true);
        tag.setRemoveComments(true);
        tag.setRemoveMultiSpaces(true);
        tag.setRemoveIntertagSpaces(true);
        tag.setRemoveQuotes(true);
        tag.setPreserveLineBreaks(false);
        tag.setCompressJavaScript(false);
        tag.setCompressCss(false);
        tag.setJsCompressor("yui");
        tag.setYuiJsNoMunge(false);
        tag.setYuiJsPreserveAllSemiColons(false);
        tag.setYuiJsDisableOptimizations(false);
        tag.setYuiJsLineBreak(-1);
        tag.setYuiCssLineBreak(-1);
        tag.setClosureOptLevel("simple");
        tag.setSimpleDoctype(false);
        tag.setRemoveScriptAttributes(false);
        tag.setRemoveStyleAttributes(false);
        tag.setRemoveLinkAttributes(false);
        tag.setRemoveFormAttributes(false);
        tag.setRemoveInputAttributes(false);
        tag.setSimpleBooleanAttributes(false);
        tag.setRemoveJavaScriptProtocol(false);
        tag.setRemoveHttpProtocol(false);
        tag.setRemoveHttpsProtocol(false);
        // no assertion needed – if setters throw that is a failure
    }

    @Test
    void testHtmlTagDoEndTagCompressesDefaultSettings() throws Exception {
        StubJspWriter writer = new StubJspWriter();
        StubBodyContent body = new StubBodyContent(writer, HTML_CONTENT);
        StubPageContext ctx = new StubPageContext(writer);

        HtmlCompressorTag tag = new HtmlCompressorTag();
        tag.setPageContext(ctx);
        tag.setBodyContent(body);

        tag.doEndTag();

        String output = writer.getOutput();
        assertNotNull(output);
        // default removes comments and multi-spaces
        assertFalse(output.contains("<!--"), "Expected HTML comment to be removed");
        assertFalse(output.contains("  "), "Expected multiple spaces to be removed");
        assertTrue(output.contains("<html>"));
    }

    @Test
    void testHtmlTagDoEndTagWithPreserveComments() throws Exception {
        StubJspWriter writer = new StubJspWriter();
        StubBodyContent body = new StubBodyContent(writer, HTML_CONTENT);
        StubPageContext ctx = new StubPageContext(writer);

        HtmlCompressorTag tag = new HtmlCompressorTag();
        tag.setRemoveComments(false);
        tag.setPageContext(ctx);
        tag.setBodyContent(body);

        tag.doEndTag();

        assertTrue(writer.getOutput().contains("<!--"), "Expected comment to be preserved when removeComments=false");
    }

    @Test
    void testHtmlTagDoEndTagDisabled() throws Exception {
        StubJspWriter writer = new StubJspWriter();
        StubBodyContent body = new StubBodyContent(writer, HTML_CONTENT);
        StubPageContext ctx = new StubPageContext(writer);

        HtmlCompressorTag tag = new HtmlCompressorTag();
        tag.setEnabled(false);
        tag.setPageContext(ctx);
        tag.setBodyContent(body);

        tag.doEndTag();

        // disabled → content returned as-is
        assertTrue(writer.getOutput().contains(HTML_CONTENT),
                "Expected content to pass through unchanged when enabled=false");
    }

    @Test
    void testHtmlTagDoEndTagRemoveIntertagSpaces() throws Exception {
        StubJspWriter writer = new StubJspWriter();
        StubBodyContent body = new StubBodyContent(writer, "<html>  <body>  <p>text</p>  </body>  </html>");
        StubPageContext ctx = new StubPageContext(writer);

        HtmlCompressorTag tag = new HtmlCompressorTag();
        tag.setRemoveIntertagSpaces(true);
        tag.setPageContext(ctx);
        tag.setBodyContent(body);

        tag.doEndTag();

        assertTrue(writer.getOutput().contains("><"), "Expected intertag spaces to be removed");
    }

    @Test
    void testHtmlTagDoEndTagWithClosureCompressor() throws Exception {
        StubJspWriter writer = new StubJspWriter();
        StubBodyContent body = new StubBodyContent(writer, "<html><body><script>var x=1;</script></body></html>");
        StubPageContext ctx = new StubPageContext(writer);

        HtmlCompressorTag tag = new HtmlCompressorTag();
        tag.setCompressJavaScript(true);
        tag.setJsCompressor("closure");
        tag.setClosureOptLevel("simple");
        tag.setPageContext(ctx);
        tag.setBodyContent(body);

        assertDoesNotThrow(tag::doEndTag);
    }

    @Test
    void testHtmlTagDoEndTagClosureAdvancedLevel() throws Exception {
        StubJspWriter writer = new StubJspWriter();
        StubBodyContent body = new StubBodyContent(writer, "<html><body><script>var x=1;</script></body></html>");
        StubPageContext ctx = new StubPageContext(writer);

        HtmlCompressorTag tag = new HtmlCompressorTag();
        tag.setCompressJavaScript(true);
        tag.setJsCompressor("closure");
        tag.setClosureOptLevel("advanced");
        tag.setPageContext(ctx);
        tag.setBodyContent(body);

        assertDoesNotThrow(tag::doEndTag);
    }

    @Test
    void testHtmlTagDoEndTagClosureWhitespaceLevel() throws Exception {
        StubJspWriter writer = new StubJspWriter();
        StubBodyContent body = new StubBodyContent(writer, "<html><body><script>var x=1;</script></body></html>");
        StubPageContext ctx = new StubPageContext(writer);

        HtmlCompressorTag tag = new HtmlCompressorTag();
        tag.setCompressJavaScript(true);
        tag.setJsCompressor("closure");
        tag.setClosureOptLevel("whitespace");
        tag.setPageContext(ctx);
        tag.setBodyContent(body);

        assertDoesNotThrow(tag::doEndTag);
    }

    // -----------------------------------------------------------------------
    // XmlCompressorTag
    // -----------------------------------------------------------------------

    @Test
    void testXmlTagSetters() {
        XmlCompressorTag tag = new XmlCompressorTag();
        tag.setEnabled(true);
        tag.setRemoveComments(true);
        tag.setRemoveIntertagSpaces(true);
    }

    @Test
    void testXmlTagDoEndTagRemovesCommentsByDefault() throws Exception {
        StubJspWriter writer = new StubJspWriter();
        StubBodyContent body = new StubBodyContent(writer, XML_CONTENT);
        StubPageContext ctx = new StubPageContext(writer);

        XmlCompressorTag tag = new XmlCompressorTag();
        tag.setPageContext(ctx);
        tag.setBodyContent(body);

        tag.doEndTag();

        String output = writer.getOutput();
        assertFalse(output.contains("<!--"), "Expected XML comment to be removed");
        assertTrue(output.contains("<child>"));
    }

    @Test
    void testXmlTagDoEndTagPreserveComments() throws Exception {
        StubJspWriter writer = new StubJspWriter();
        StubBodyContent body = new StubBodyContent(writer, XML_CONTENT);
        StubPageContext ctx = new StubPageContext(writer);

        XmlCompressorTag tag = new XmlCompressorTag();
        tag.setRemoveComments(false);
        tag.setPageContext(ctx);
        tag.setBodyContent(body);

        tag.doEndTag();

        assertTrue(writer.getOutput().contains("<!--"), "Expected comment to be preserved when removeComments=false");
    }

    @Test
    void testXmlTagDoEndTagDisabled() throws Exception {
        StubJspWriter writer = new StubJspWriter();
        StubBodyContent body = new StubBodyContent(writer, XML_CONTENT);
        StubPageContext ctx = new StubPageContext(writer);

        XmlCompressorTag tag = new XmlCompressorTag();
        tag.setEnabled(false);
        tag.setPageContext(ctx);
        tag.setBodyContent(body);

        tag.doEndTag();

        assertTrue(writer.getOutput().contains(XML_CONTENT),
                "Expected content to pass through unchanged when enabled=false");
    }

    // -----------------------------------------------------------------------
    // CssCompressorTag
    // -----------------------------------------------------------------------

    @Test
    void testCssTagSetters() {
        CssCompressorTag tag = new CssCompressorTag();
        tag.setEnabled(true);
        tag.setYuiCssLineBreak(-1);
    }

    @Test
    void testCssTagDoEndTagCompressesCss() throws Exception {
        StubJspWriter writer = new StubJspWriter();
        StubBodyContent body = new StubBodyContent(writer, CSS_CONTENT);
        StubPageContext ctx = new StubPageContext(writer);

        CssCompressorTag tag = new CssCompressorTag();
        tag.setPageContext(ctx);
        tag.setBodyContent(body);

        tag.doEndTag();

        String output = writer.getOutput();
        assertNotNull(output);
        assertTrue(output.length() < CSS_CONTENT.length(), "Expected CSS to be compressed (shorter output)");
    }

    @Test
    void testCssTagDoEndTagDisabledPassesThrough() throws Exception {
        StubJspWriter writer = new StubJspWriter();
        StubBodyContent body = new StubBodyContent(writer, CSS_CONTENT);
        StubPageContext ctx = new StubPageContext(writer);

        CssCompressorTag tag = new CssCompressorTag();
        tag.setEnabled(false);
        tag.setPageContext(ctx);
        tag.setBodyContent(body);

        tag.doEndTag();

        assertTrue(writer.getOutput().contains(CSS_CONTENT), "Expected content to pass through when enabled=false");
    }

    // -----------------------------------------------------------------------
    // JavaScriptCompressorTag
    // -----------------------------------------------------------------------

    @Test
    void testJsTagSetters() {
        JavaScriptCompressorTag tag = new JavaScriptCompressorTag();
        tag.setEnabled(true);
        tag.setJsCompressor("yui");
        tag.setYuiJsNoMunge(false);
        tag.setYuiJsPreserveAllSemiColons(false);
        tag.setYuiJsDisableOptimizations(false);
        tag.setYuiJsLineBreak(-1);
        tag.setClosureOptLevel("simple");
    }

    @Test
    void testJsTagDoEndTagYuiCompressesJs() throws Exception {
        StubJspWriter writer = new StubJspWriter();
        StubBodyContent body = new StubBodyContent(writer, JS_CONTENT);
        StubPageContext ctx = new StubPageContext(writer);

        JavaScriptCompressorTag tag = new JavaScriptCompressorTag();
        tag.setJsCompressor("yui");
        tag.setPageContext(ctx);
        tag.setBodyContent(body);

        tag.doEndTag();

        String output = writer.getOutput();
        assertNotNull(output);
        assertFalse(output.contains("  "), "Expected extra spaces to be removed by YUI");
    }

    @Test
    void testJsTagDoEndTagDisabledPassesThrough() throws Exception {
        StubJspWriter writer = new StubJspWriter();
        StubBodyContent body = new StubBodyContent(writer, JS_CONTENT);
        StubPageContext ctx = new StubPageContext(writer);

        JavaScriptCompressorTag tag = new JavaScriptCompressorTag();
        tag.setEnabled(false);
        tag.setPageContext(ctx);
        tag.setBodyContent(body);

        tag.doEndTag();

        assertTrue(writer.getOutput().contains(JS_CONTENT), "Expected content to pass through when enabled=false");
    }

    @Test
    void testJsTagDoEndTagClosureSimple() throws Exception {
        StubJspWriter writer = new StubJspWriter();
        StubBodyContent body = new StubBodyContent(writer, "function add(a,b){return a+b;}");
        StubPageContext ctx = new StubPageContext(writer);

        JavaScriptCompressorTag tag = new JavaScriptCompressorTag();
        tag.setJsCompressor("closure");
        tag.setClosureOptLevel("simple");
        tag.setPageContext(ctx);
        tag.setBodyContent(body);

        assertDoesNotThrow(tag::doEndTag);
        assertFalse(writer.getOutput().isBlank());
    }

    @Test
    void testJsTagDoEndTagClosureWhitespace() throws Exception {
        StubJspWriter writer = new StubJspWriter();
        StubBodyContent body = new StubBodyContent(writer, "var x = 1 ;");
        StubPageContext ctx = new StubPageContext(writer);

        JavaScriptCompressorTag tag = new JavaScriptCompressorTag();
        tag.setJsCompressor("closure");
        tag.setClosureOptLevel("whitespace");
        tag.setPageContext(ctx);
        tag.setBodyContent(body);

        assertDoesNotThrow(tag::doEndTag);
    }

    @Test
    void testJsTagDoEndTagClosureAdvanced() throws Exception {
        StubJspWriter writer = new StubJspWriter();
        StubBodyContent body = new StubBodyContent(writer, "var x = 1;");
        StubPageContext ctx = new StubPageContext(writer);

        JavaScriptCompressorTag tag = new JavaScriptCompressorTag();
        tag.setJsCompressor("closure");
        tag.setClosureOptLevel("advanced");
        tag.setPageContext(ctx);
        tag.setBodyContent(body);

        assertDoesNotThrow(tag::doEndTag);
    }

    // ======================================================================
    // Minimal JSP test infrastructure
    // ======================================================================

    /** Minimal {@link JspWriter} that captures all output to an internal {@link StringBuilder}. */
    static class StubJspWriter extends JspWriter {

        private final StringBuilder sink = new StringBuilder();

        StubJspWriter() {
            super(0, false);
        }

        String getOutput() {
            return sink.toString();
        }

        // Writer abstract
        @Override
        public void write(char[] cbuf, int off, int len) {
            sink.append(cbuf, off, len);
        }

        @Override
        public void write(String s) {
            sink.append(s);
        }

        // JspWriter abstract methods
        @Override
        public void newLine() {
        }

        @Override
        public void print(boolean b) {
            sink.append(b);
        }

        @Override
        public void print(char c) {
            sink.append(c);
        }

        @Override
        public void print(int i) {
            sink.append(i);
        }

        @Override
        public void print(long l) {
            sink.append(l);
        }

        @Override
        public void print(float f) {
            sink.append(f);
        }

        @Override
        public void print(double d) {
            sink.append(d);
        }

        @Override
        @SuppressWarnings("ql/confusing-overloading")
        public void print(char[] s) {
            if (s != null)
                sink.append(s);
        }

        @Override
        public void print(String s) {
            if (s != null)
                sink.append(s);
        }

        @Override
        @SuppressWarnings("ql/confusing-overloading")
        public void print(Object obj) {
            sink.append(obj);
        }

        @Override
        public void println() {
            sink.append('\n');
        }

        @Override
        public void println(boolean x) {
            sink.append(x).append('\n');
        }

        @Override
        public void println(char x) {
            sink.append(x).append('\n');
        }

        @Override
        public void println(int x) {
            sink.append(x).append('\n');
        }

        @Override
        public void println(long x) {
            sink.append(x).append('\n');
        }

        @Override
        public void println(float x) {
            sink.append(x).append('\n');
        }

        @Override
        public void println(double x) {
            sink.append(x).append('\n');
        }

        @Override
        @SuppressWarnings("ql/confusing-overloading")
        public void println(char[] x) {
            if (x != null)
                sink.append(x);
            sink.append('\n');
        }

        @Override
        public void println(String x) {
            if (x != null)
                sink.append(x);
            sink.append('\n');
        }

        @Override
        @SuppressWarnings("ql/confusing-overloading")
        public void println(Object x) {
            sink.append(x).append('\n');
        }

        @Override
        public void clear() {
        }

        @Override
        public void clearBuffer() {
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }

        @Override
        public int getRemaining() {
            return Integer.MAX_VALUE;
        }
    }

    /**
     * Minimal {@link BodyContent} backed by a mutable {@link StringBuilder}.
     * <p>
     * After {@code clear()} + {@code append(result)} the new content is immediately visible via {@code getString()}.
     * {@code writeOut(Writer)} copies the buffer to the supplied writer (normally the page's {@link JspWriter}).
     */
    static class StubBodyContent extends BodyContent {

        private final StringBuilder buf;

        StubBodyContent(JspWriter enclosing, String initialContent) {
            super(enclosing);
            this.buf = new StringBuilder(initialContent);
        }

        @Override
        public String getString() {
            return buf.toString();
        }

        @Override
        public Reader getReader() {
            return new StringReader(buf.toString());
        }

        @Override
        public void writeOut(Writer out) throws IOException {
            out.write(buf.toString());
        }

        // Writer abstract – appends to our buffer (used by append() / write(String))
        @Override
        public void write(char[] cbuf, int off, int len) {
            buf.append(cbuf, off, len);
        }

        // JspWriter abstract methods
        @Override
        public void newLine() {
        }

        @Override
        public void print(boolean b) {
        }

        @Override
        public void print(char c) {
            buf.append(c);
        }

        @Override
        public void print(int i) {
        }

        @Override
        public void print(long l) {
        }

        @Override
        public void print(float f) {
        }

        @Override
        public void print(double d) {
        }

        @Override
        @SuppressWarnings("ql/confusing-overloading")
        public void print(char[] s) {
        }

        @Override
        public void print(String s) {
            if (s != null)
                buf.append(s);
        }

        @Override
        @SuppressWarnings("ql/confusing-overloading")
        public void print(Object obj) {
        }

        @Override
        public void println() {
        }

        @Override
        public void println(boolean x) {
        }

        @Override
        public void println(char x) {
        }

        @Override
        public void println(int x) {
        }

        @Override
        public void println(long x) {
        }

        @Override
        public void println(float x) {
        }

        @Override
        public void println(double x) {
        }

        @Override
        @SuppressWarnings("ql/confusing-overloading")
        public void println(char[] x) {
        }

        @Override
        public void println(String x) {
        }

        @Override
        @SuppressWarnings("ql/confusing-overloading")
        public void println(Object x) {
        }

        @Override
        public void clear() {
            buf.setLength(0);
        }

        @Override
        public void clearBuffer() {
            buf.setLength(0);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }

        @Override
        public int getRemaining() {
            return Integer.MAX_VALUE;
        }
    }

    /**
     * Minimal {@link PageContext} whose only meaningful method is {@link #getOut()}. All other abstract methods are
     * stubs that return {@code null} or do nothing.
     */
    static class StubPageContext extends PageContext {

        private final JspWriter out;

        StubPageContext(JspWriter out) {
            this.out = out;
        }

        @Override
        public JspWriter getOut() {
            return out;
        }

        // JspContext abstract methods
        @Override
        public void setAttribute(String name, Object value) {
        }

        @Override
        public void setAttribute(String name, Object value, int scope) {
        }

        @Override
        public Object getAttribute(String name) {
            return null;
        }

        @Override
        public Object getAttribute(String name, int scope) {
            return null;
        }

        @Override
        public Object findAttribute(String name) {
            return null;
        }

        @Override
        public void removeAttribute(String name) {
        }

        @Override
        public void removeAttribute(String name, int scope) {
        }

        @Override
        public int getAttributesScope(String name) {
            return 0;
        }

        @Override
        public Enumeration<String> getAttributeNamesInScope(int scope) {
            return Collections.emptyEnumeration();
        }

        @Override
        public ELContext getELContext() {
            return null;
        }

        // PageContext abstract methods
        @Override
        public void initialize(Servlet servlet, ServletRequest request, ServletResponse response, String errorPageURL,
                boolean needsSession, int bufferSize, boolean autoFlush) {
        }

        @Override
        public void release() {
        }

        @Override
        public HttpSession getSession() {
            return null;
        }

        @Override
        public Object getPage() {
            return null;
        }

        @Override
        public ServletRequest getRequest() {
            return null;
        }

        @Override
        public ServletResponse getResponse() {
            return null;
        }

        @Override
        public Exception getException() {
            return null;
        }

        @Override
        public ServletConfig getServletConfig() {
            return null;
        }

        @Override
        public ServletContext getServletContext() {
            return null;
        }

        @Override
        public void forward(String relativeUrlPath) throws ServletException, IOException {
        }

        @Override
        public void include(String relativeUrlPath) throws ServletException, IOException {
        }

        @Override
        public void include(String relativeUrlPath, boolean flush) throws ServletException, IOException {
        }

        @Override
        public void handlePageException(Exception e) throws ServletException, IOException {
        }

        @Override
        @SuppressWarnings("ql/confusing-overloading")
        public void handlePageException(Throwable t) throws ServletException, IOException {
        }
    }
}
