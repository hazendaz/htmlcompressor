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
package com.googlecode.htmlcompressor.velocity;

import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import com.googlecode.htmlcompressor.compressor.YuiCssCompressor;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

/**
 * Velocity directive that compresses an CSS content within #compressCss ... #end block. All CSS-related properties from
 * {@link HtmlCompressor} are supported.
 *
 * @author <a href="mailto:serg472@gmail.com">Sergiy Kovalchuk</a>
 *
 * @see HtmlCompressor
 * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
 */
public class CssCompressorDirective extends Directive {

    /** The enabled. */
    private boolean enabled = true;

    // YUICompressor settings

    /** The yui css line break. */
    private int yuiCssLineBreak = -1;

    @Override
    public String getName() {
        return "compressCss";
    }

    @Override
    public int getType() {
        return BLOCK;
    }

    @Override
    public void init(RuntimeServices rs, InternalContextAdapter context, Node node) throws TemplateInitException {
        super.init(rs, context, node);
        log = rs.getLog();

        // set compressor properties
        enabled = rs.getBoolean("userdirective.compressCss.enabled", true);
        yuiCssLineBreak = rs.getInt("userdirective.compressCss.yuiCssLineBreak", -1);
    }

    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node)
            throws IOException, MethodInvocationException {

        // render content
        StringWriter content = new StringWriter();
        node.jjtGetChild(0).render(context, content);

        // compress
        if (enabled) {
            try {

                YuiCssCompressor compressor = new YuiCssCompressor();
                compressor.setLineBreak(yuiCssLineBreak);
                String result = compressor.compress(content.toString());

                writer.write(result);
            } catch (Exception e) {
                writer.write(content.toString());
                String msg = "Failed to compress content: " + content.toString();
                log.error(msg, e);
                throw new RuntimeException(msg, e);

            }
        } else {
            writer.write(content.toString());
        }

        return true;

    }

}
