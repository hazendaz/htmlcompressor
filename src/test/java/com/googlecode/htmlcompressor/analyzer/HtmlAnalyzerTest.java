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
package com.googlecode.htmlcompressor.analyzer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.googlecode.htmlcompressor.compressor.HtmlCompressor;

import org.junit.jupiter.api.Test;

class HtmlAnalyzerTest {

    private static final String HTML_SOURCE = """
            <!doctype html>
            <html>
              <head>
                <style type=\"text/css\">body { color: red; margin: 0; }</style>
                <script type=\"text/javascript\">function add(a, b) { return a + b; }</script>
              </head>
              <body onclick=\"javascript:return add(1, 2);\">
                <a href=\"https://example.com\"> example </a>
              </body>
            </html>
            """;

    @Test
    void testAnalyzeWithYuiJavaScriptCompressor() {
        HtmlAnalyzer analyzer = new HtmlAnalyzer(HtmlCompressor.JS_COMPRESSOR_YUI);

        assertDoesNotThrow(() -> analyzer.analyze(HTML_SOURCE));
    }

    @Test
    void testAnalyzeWithClosureJavaScriptCompressor() {
        HtmlAnalyzer analyzer = new HtmlAnalyzer(HtmlCompressor.JS_COMPRESSOR_CLOSURE);

        assertDoesNotThrow(() -> analyzer.analyze(HTML_SOURCE));
    }
}
