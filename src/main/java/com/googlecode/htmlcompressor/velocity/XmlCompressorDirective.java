/**
 *    Copyright 2009-2018 the original author or authors.
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
package com.googlecode.htmlcompressor.velocity;

import com.googlecode.htmlcompressor.compressor.XmlCompressor;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.parser.node.Node;

/**
 * Velocity directive that compresses an XML content within #compressXml ... #end block. Compression parameters are set
 * by default.
 *
 * @author <a href="mailto:serg472@gmail.com">Sergiy Kovalchuk</a>
 * @see XmlCompressor
 */
public class XmlCompressorDirective extends Directive {

    /** The Constant xmlCompressor. */
    private static final XmlCompressor xmlCompressor = new XmlCompressor();

    /** The log. */
    private Log log;

    @Override
    public String getName() {
        return "compressXml";
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
        xmlCompressor.setEnabled(rs.getBoolean("userdirective.compressXml.enabled", true));
        xmlCompressor.setRemoveComments(rs.getBoolean("userdirective.compressXml.removeComments", true));
        xmlCompressor.setRemoveIntertagSpaces(rs.getBoolean("userdirective.compressXml.removeIntertagSpaces", true));
    }

    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node)
            throws IOException, MethodInvocationException {

        // render content
        StringWriter content = new StringWriter();
        node.jjtGetChild(0).render(context, content);

        // compress
        try {
            writer.write(xmlCompressor.compress(content.toString()));
        } catch (Exception e) {
            writer.write(content.toString());
            String msg = "Failed to compress content: " + content.toString();
            log.error(msg, e);
            throw new RuntimeException(msg, e);

        }
        return true;

    }

}
