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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class XmlCompressorTest.
 */
public class XmlCompressorTest {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(XmlCompressorTest.class);

    /** The Constant resPath. */
    private static final String resPath = "./src/test/resources/xml/";

    /**
     * Test compress.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void testCompress() throws Exception {
        String source = readResource("testCompress.xml");
        String result = readResource("testCompressResult.xml");

        XmlCompressor compressor = new XmlCompressor();

        assertEquals(result, compressor.compress(source));
    }

    /**
     * Test enabled.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void testEnabled() throws Exception {
        String source = readResource("testEnabled.xml");
        String result = readResource("testEnabledResult.xml");

        XmlCompressor compressor = new XmlCompressor();
        compressor.setEnabled(false);

        assertEquals(result, compressor.compress(source));
    }

    /**
     * Test remove comments.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void testRemoveComments() throws Exception {
        String source = readResource("testRemoveComments.xml");
        String result = readResource("testRemoveCommentsResult.xml");

        XmlCompressor compressor = new XmlCompressor();
        compressor.setRemoveComments(true);

        assertEquals(result, compressor.compress(source));
    }

    /**
     * Test remove intertag spaces.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void testRemoveIntertagSpaces() throws Exception {
        String source = readResource("testRemoveIntertagSpaces.xml");
        String result = readResource("testRemoveIntertagSpacesResult.xml");

        XmlCompressor compressor = new XmlCompressor();
        compressor.setRemoveIntertagSpaces(true);

        assertEquals(result, compressor.compress(source));
    }

    /**
     * Read resource.
     *
     * @param filename
     *            the filename
     *
     * @return the string
     */
    private String readResource(String filename) {
        StringBuilder builder = new StringBuilder();
        try (Reader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(new File(resPath + filename)), StandardCharsets.UTF_8))) {

            char[] buffer = new char[8192];
            int read;
            while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
                builder.append(buffer, 0, read);
            }

        } catch (IOException e) {
            logger.error("", e);
        }

        return builder.toString();
    }

}
