/*
 * Created 10.07.2010
 *
 * (c) 2010 Thorsten Möller - University of Basel Switzerland
 *
 * The MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
package org.mindswap.owl;


/**
 * Enumeration of syntax formats available for OWL. Not all of them might be
 * actually supported by {@link OWLReader readers} and {@link OWLWriter writers}
 * respectively.
 *
 * @author unascribed
 * @version $Rev: 2530 $; $Author: thorsten $; $Date: 2010-07-30 20:46:00 +0300 (Fri, 30 Jul 2010) $
 */
public enum OWLSyntax
{
	/** <a href="http://www.w3.org/TR/owl2-syntax/">Functional-style</a>. */
	FUNCTIONAL,
	/** <a href="http://www.w3.org/TR/owl2-manchester-syntax/">Manchester</a>. */
	MANCHESTER,
	/** <a href="http://www.w3.org/TR/owl2-xml-serialization/">OWL/XML</a>. */
	OWLXML,
	/** <a href="http://www.w3.org/TR/rdf-syntax-grammar/">RDF/XML</a>. */
	RDFXML,
	/** <a href="http://www.w3.org/TeamSubmission/turtle/">Turtle</a>. */
	TURTLE
}
