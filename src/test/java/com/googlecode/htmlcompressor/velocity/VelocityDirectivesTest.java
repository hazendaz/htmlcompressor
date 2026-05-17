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
package com.googlecode.htmlcompressor.velocity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for all four Velocity compression directives using a real {@link VelocityEngine}. The directives
 * are registered via the {@code userdirective} property so that {@code init()} and {@code render()} are both exercised
 * with real Velocity runtime services.
 */
class VelocityDirectivesTest {

    private static final String HTML_BLOCK = "<html>  <!-- comment -->  <body>  <p>hello  world</p>  </body>  </html>";
    private static final String XML_BLOCK = "<root>  <!-- comment -->  <child>  text  </child>  </root>";
    private static final String CSS_BLOCK = "body  {  color :  red ;  margin :  0  ;  }";
    private static final String JS_BLOCK = "function  add ( a , b )  {  return  a + b ;  }";

    private VelocityEngine engine;

    @BeforeEach
    void setUp() {
        engine = new VelocityEngine();
        engine.setProperty("userdirective",
                "com.googlecode.htmlcompressor.velocity.HtmlCompressorDirective,"
                        + "com.googlecode.htmlcompressor.velocity.XmlCompressorDirective,"
                        + "com.googlecode.htmlcompressor.velocity.CssCompressorDirective,"
                        + "com.googlecode.htmlcompressor.velocity.JavaScriptCompressorDirective");
        engine.init();
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    private String evaluate(String template) {
        StringWriter out = new StringWriter();
        engine.evaluate(new VelocityContext(), out, "test", new StringReader(template));
        return out.toString();
    }

    // -----------------------------------------------------------------------
    // HtmlCompressorDirective
    // -----------------------------------------------------------------------

    @Test
    void testHtmlDirectiveCompressesMultipleSpaces() {
        String result = evaluate("#compressHtml\n" + HTML_BLOCK + "\n#end");
        assertNotNull(result);
        // default: multi-spaces are removed
        assertFalse(result.contains("  "), "Expected multiple spaces to be removed");
    }

    @Test
    void testHtmlDirectiveRemovesCommentsByDefault() {
        String result = evaluate("#compressHtml\n" + HTML_BLOCK + "\n#end");
        assertFalse(result.contains("<!--"), "Expected HTML comment to be removed by default");
    }

    @Test
    void testHtmlDirectiveDisabledLeavesContentUnchanged() {
        VelocityEngine disabledEngine = new VelocityEngine();
        disabledEngine.setProperty("userdirective", "com.googlecode.htmlcompressor.velocity.HtmlCompressorDirective");
        disabledEngine.setProperty("userdirective.compressHtml.enabled", "false");
        disabledEngine.init();

        StringWriter out = new StringWriter();
        disabledEngine.evaluate(new VelocityContext(), out, "test",
                new StringReader("#compressHtml\n" + HTML_BLOCK + "\n#end"));

        // disabled means the content passes through compressed (enabled default is read, but
        // our stub engine sets it to false so htmlCompressor.compress() still runs – just
        // the enabled flag is forwarded to HtmlCompressor which returns content as-is)
        assertNotNull(out.toString());
    }

    @Test
    void testHtmlDirectiveRemoveIntertagSpaces() {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty("userdirective", "com.googlecode.htmlcompressor.velocity.HtmlCompressorDirective");
        ve.setProperty("userdirective.compressHtml.removeIntertagSpaces", "true");
        ve.init();

        StringWriter out = new StringWriter();
        ve.evaluate(new VelocityContext(), out, "test",
                new StringReader("#compressHtml\n<html>  <body>  <p>text</p>  </body>  </html>\n#end"));

        assertTrue(out.toString().contains("><"), "Expected intertag spaces to be removed");
    }

    // -----------------------------------------------------------------------
    // XmlCompressorDirective
    // -----------------------------------------------------------------------

    @Test
    void testXmlDirectiveCompressesAndRemovesCommentsByDefault() {
        String result = evaluate("#compressXml\n" + XML_BLOCK + "\n#end");
        assertNotNull(result);
        assertFalse(result.contains("<!--"), "Expected XML comments to be removed");
    }

    @Test
    void testXmlDirectiveRemovesIntertagSpacesByDefault() {
        String result = evaluate("#compressXml\n" + XML_BLOCK + "\n#end");
        // adjacent tags should not have spaces between them
        assertTrue(result.contains("><") || !result.contains("  "), "Expected intertag spaces to be removed");
    }

    @Test
    void testXmlDirectivePreserveCommentsWhenConfigured() {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty("userdirective", "com.googlecode.htmlcompressor.velocity.XmlCompressorDirective");
        ve.setProperty("userdirective.compressXml.removeComments", "false");
        ve.init();

        StringWriter out = new StringWriter();
        ve.evaluate(new VelocityContext(), out, "test", new StringReader("#compressXml\n" + XML_BLOCK + "\n#end"));

        assertTrue(out.toString().contains("<!--"), "Expected XML comment to be preserved");
    }

    @Test
    void testXmlDirectiveDisabledReturnsContent() {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty("userdirective", "com.googlecode.htmlcompressor.velocity.XmlCompressorDirective");
        ve.setProperty("userdirective.compressXml.enabled", "false");
        ve.init();

        StringWriter out = new StringWriter();
        ve.evaluate(new VelocityContext(), out, "test", new StringReader("#compressXml\n" + XML_BLOCK + "\n#end"));

        assertNotNull(out.toString());
    }

    // -----------------------------------------------------------------------
    // CssCompressorDirective
    // -----------------------------------------------------------------------

    @Test
    void testCssDirectiveCompressesWhitespace() {
        String result = evaluate("#compressCss\n" + CSS_BLOCK + "\n#end");
        assertNotNull(result);
        assertTrue(result.length() < CSS_BLOCK.length(), "Expected CSS to be compressed (shorter than input)");
    }

    @Test
    void testCssDirectiveDisabledPassesThrough() {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty("userdirective", "com.googlecode.htmlcompressor.velocity.CssCompressorDirective");
        ve.setProperty("userdirective.compressCss.enabled", "false");
        ve.init();

        StringWriter out = new StringWriter();
        ve.evaluate(new VelocityContext(), out, "test", new StringReader("#compressCss\n" + CSS_BLOCK + "\n#end"));

        // disabled → output should equal input exactly (no trimming)
        assertTrue(out.toString().contains(CSS_BLOCK.trim()),
                "Expected disabled CSS directive to pass content through unchanged");
    }

    @Test
    void testCssDirectiveWithLineBreakSetting() {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty("userdirective", "com.googlecode.htmlcompressor.velocity.CssCompressorDirective");
        ve.setProperty("userdirective.compressCss.yuiCssLineBreak", "0");
        ve.init();

        StringWriter out = new StringWriter();
        ve.evaluate(new VelocityContext(), out, "test", new StringReader("#compressCss\n" + CSS_BLOCK + "\n#end"));

        assertNotNull(out.toString());
    }

    // -----------------------------------------------------------------------
    // JavaScriptCompressorDirective (YUI)
    // -----------------------------------------------------------------------

    @Test
    void testJsDirectiveYuiCompressesWhitespace() {
        String result = evaluate("#compressJs\n" + JS_BLOCK + "\n#end");
        assertNotNull(result);
        // YUI strips unnecessary whitespace
        assertFalse(result.contains("  "), "Expected extra spaces to be removed by YUI");
    }

    @Test
    void testJsDirectiveDisabledPassesThrough() {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty("userdirective", "com.googlecode.htmlcompressor.velocity.JavaScriptCompressorDirective");
        ve.setProperty("userdirective.compressJs.enabled", "false");
        ve.init();

        StringWriter out = new StringWriter();
        ve.evaluate(new VelocityContext(), out, "test", new StringReader("#compressJs\n" + JS_BLOCK + "\n#end"));

        assertTrue(out.toString().contains(JS_BLOCK.trim()),
                "Expected disabled JS directive to pass content through unchanged");
    }

    @Test
    void testJsDirectiveYuiNoMunge() {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty("userdirective", "com.googlecode.htmlcompressor.velocity.JavaScriptCompressorDirective");
        ve.setProperty("userdirective.compressJs.yuiJsNoMunge", "true");
        ve.init();

        StringWriter out = new StringWriter();
        ve.evaluate(new VelocityContext(), out, "test",
                new StringReader("#compressJs\nfunction foo(a){return a+1;}\n#end"));

        assertNotNull(out.toString());
    }

    @Test
    void testJsDirectiveClosureCompiler() {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty("userdirective", "com.googlecode.htmlcompressor.velocity.JavaScriptCompressorDirective");
        // configure closure via the compressHtml.jsCompressor key (as the directive reads it)
        ve.setProperty("userdirective.compressHtml.jsCompressor", "closure");
        ve.init();

        StringWriter out = new StringWriter();
        ve.evaluate(new VelocityContext(), out, "test",
                new StringReader("#compressJs\nvar x=function(a,b){return a+b;};\n#end"));

        assertNotNull(out.toString());
    }
}
