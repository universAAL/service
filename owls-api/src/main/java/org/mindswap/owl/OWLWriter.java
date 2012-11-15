// The MIT License
//
// Copyright (c) 2004 Evren Sirin
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to
// deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.

/*
 * Created on Dec 27, 2003
 */
package org.mindswap.owl;

import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;


/**
 * OWL writer implement functionality to serialize {@link OWLModel OWL models}
 * by writing them to a {@link Writer} or {@link OutputStream} using some
 * standardized {@link OWLSyntax format} such as {@link OWLSyntax#RDFXML RDF/XML}
 * or {@link OWLSyntax#TURTLE Turtle}.
 *
 * @author unascribed
 * @version $Rev: 2530 $; $Author: thorsten $; $Date: 2010-07-30 20:46:00 +0300 (Fri, 30 Jul 2010) $
 */
public interface OWLWriter
{

    public void write(Writer writer, URI baseURI);

    public void write(OutputStream out, URI baseURI);

    public void setNsPrefix(String prefix, String uri);

    public void addPrettyType(OWLClass c);

    public boolean isShowXmlDeclaration();

    public void setShowXmlDeclaration(boolean showXmlDeclaration);

   /**
    * @param syntax The target format in which OWL models will be written.
    * @throws IllegalArgumentException If the given format is not supported by
    * 	this writer.
    */
   public void setTargetFormat(OWLSyntax syntax);
}
