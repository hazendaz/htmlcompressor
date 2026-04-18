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
package com.googlecode.htmlcompressor.compressor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HtmlMetricsStatisticsTest {

    @Test
    void testHtmlMetricsAccessorsAndToString() {
        HtmlMetrics metrics = new HtmlMetrics();
        metrics.setFilesize(1000);
        metrics.setEmptyChars(250);
        metrics.setInlineScriptSize(300);
        metrics.setInlineStyleSize(150);
        metrics.setInlineEventSize(50);

        assertEquals(1000, metrics.getFilesize());
        assertEquals(250, metrics.getEmptyChars());
        assertEquals(300, metrics.getInlineScriptSize());
        assertEquals(150, metrics.getInlineStyleSize());
        assertEquals(50, metrics.getInlineEventSize());
        assertEquals("Filesize=1000, Empty Chars=250, Script Size=300, Style Size=150, Event Handler Size=50",
                metrics.toString());
    }

    @Test
    void testHtmlCompressorStatisticsAccessorsAndToString() {
        HtmlMetrics original = new HtmlMetrics();
        original.setFilesize(1200);
        original.setEmptyChars(400);

        HtmlMetrics compressed = new HtmlMetrics();
        compressed.setFilesize(800);
        compressed.setEmptyChars(150);

        HtmlCompressorStatistics statistics = new HtmlCompressorStatistics();
        statistics.setOriginalMetrics(original);
        statistics.setCompressedMetrics(compressed);
        statistics.setTime(42L);
        statistics.setPreservedSize(123);

        assertEquals(original, statistics.getOriginalMetrics());
        assertEquals(compressed, statistics.getCompressedMetrics());
        assertEquals(42L, statistics.getTime());
        assertEquals(123, statistics.getPreservedSize());
        assertEquals("Time=42, Preserved=123, Original={Filesize=1200, Empty Chars=400, Script Size=0, Style Size=0, "
                + "Event Handler Size=0}, Compressed={Filesize=800, Empty Chars=150, Script Size=0, Style Size=0, "
                + "Event Handler Size=0}", statistics.toString());
    }
}
