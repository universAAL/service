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
 * Created on Oct 26, 2004
 */
package org.mindswap.owl;

import java.net.URI;

/**
 * Abstraction of data values a.k.a. literals. Data values can be typed. If
 * typed then the a data value comprises a {@link #getDatatypeURI() datatype},
 * a {@link #getLexicalValue() lexical form} and a {@link #getValue() value}
 * (together with an optional {@link #getLanguage() xml:lang string}).
 * Untyped data values are termed "plain".
 *
 * @author unascribed
 * @version $Rev: 2394 $; $Author: thorsten $; $Date: 2010-01-15 12:27:20 +0200 (Fri, 15 Jan 2010) $
 */
public interface OWLDataValue extends OWLValue
{
    public URI getDatatypeURI();
    public String getLanguage();
    public String getLexicalValue();
    public Object getValue();
}
