// The MIT License
//
// Copyright (c) 2009 Thorsten Möller - University of Basel Switzerland
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
package org.mindswap.owls.validator;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.mindswap.owls.service.Service;

/**
 * A collection of {@link ValidationMessage validation messages}, grouped by
 * the service whose validation caused a message.
 *
 * @author unascribed
 * @version $Rev: 2396 $; $Author: thorsten $; $Date: 2010-01-15 12:34:01 +0200 (Fri, 15 Jan 2010) $
 */
public class ValidationReport implements Iterable<Entry<Service, Set<ValidationMessage>>>
{
	private final Map<Service, Set<ValidationMessage>> messageMap;

	public ValidationReport(final Map<Service, Set<ValidationMessage>> theMsgs)
	{
		messageMap = theMsgs;
	}

	/**
	 * @return <code>true</code> if this validation report does not contain any
	 * 	{@link ValidationMessage}.
	 */
	public boolean isEmpty()
	{
		for (Entry<Service, Set<ValidationMessage>> e : messageMap.entrySet())
		{
			if (e.getValue().size() > 0) return false;
		}
		return true;
	}

	/**
	 * Check if the given service is considered valid according to this report,
	 * that is, if there are no validation messages about the service.
	 *
	 * @param service The service to check.
	 * @return <code>true</code> if there is no validation message about the
	 * 	given service. <code>false</code> in case at least one message exists,
	 * 	or if this report is not about the service.
	 */
	public boolean isValid(final Service service)
	{
		return messageMap.containsKey(service) && messageMap.get(service).isEmpty();
	}

	/* @see java.lang.Iterable#iterator() */
	public Iterator<Entry<Service, Set<ValidationMessage>>> iterator()
	{
		return messageMap.entrySet().iterator();
	}

	public void print(final PrintStream theOut)
	{
		theOut.println("Validation Report");
		if (messageMap.isEmpty())
		{
			theOut.println("Valid:\ttrue");
			return;
		}

		for (Entry<Service, Set<ValidationMessage>> entry : messageMap.entrySet())
		{
			Service aService = entry.getKey();
			Set<ValidationMessage> msgSet = entry.getValue();
			boolean valid = msgSet.isEmpty();

			theOut.println("Service:\t" + aService);
			theOut.println("Valid:\t\t" + valid);
			if (!valid)
			{
				theOut.println("Validation messages: ");
				for (ValidationMessage msg : msgSet)
				{
					theOut.println(msg);
				}
			}
		}
	}

	public Map<Service, Set<ValidationMessage>> getMessages()
	{
		return messageMap;
	}

}
