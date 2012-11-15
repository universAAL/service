/*
 * Created on 03.06.2010
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
package org.mindswap.common;

/**
 * Enumeration of the various Description Logic based matching types
 * for any two classes (concepts) <tt>C<sub>1</sub></tt>, <tt>C<sub>2</sub></tt>;
 * or any two data ranges <tt>D<sub>1</sub></tt>, <tt>D<sub>2</sub></tt>.
 *
 * @author unascribed
 * @version $Rev: 2475 $; $Author: thorsten $; $Date: 2010-06-10 15:11:55 +0300 (Thu, 10 Jun 2010) $
 */
public enum DLMatch
{
	/**
	 * <tt>C<sub>1</sub></tt> is equivalent to <tt>C<sub>2</sub></tt>, or
	 * <tt>D<sub>1</sub></tt> is equivalent to <tt>D<sub>2</sub></tt>.
	 */
	EXACT,

	/**
	 * <tt>C<sub>1</sub></tt> is subsumed by (is a subclass of) <tt>C<sub>2</sub></tt>, or
	 * <tt>D<sub>1</sub></tt> is subsumed by (is a sub data range of) <tt>D<sub>2</sub></tt>.
	 */
	SUBSUME,

	/**
	 * <tt>C<sub>1</sub></tt> is a super class of <tt>C<sub>2</sub></tt>, or
	 * <tt>D<sub>1</sub></tt> is a super data range of <tt>D<sub>2</sub></tt>.
	 */
	PLUG_IN,

	/**
	 * <tt>C<sub>1</sub></tt> and <tt>C<sub>2</sub></tt> are disjoint, or
	 * <tt>D<sub>1</sub></tt> and <tt>D<sub>2</sub></tt> are disjoint.
	 */
	DISJOINT,

	/**
	 * None of {@link #EXACT}, {@link #SUBSUME}, {@link #PLUG_IN}, {@link #DISJOINT}
	 * holds for <tt>C<sub>1</sub></tt>, <tt>C<sub>2</sub></tt> and
	 * <tt>D<sub>1</sub></tt>, <tt>D<sub>2</sub></tt> respectively.
	 */
	FAIL
}