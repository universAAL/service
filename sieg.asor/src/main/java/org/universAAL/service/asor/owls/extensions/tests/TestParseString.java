/**
 * 
 *  OCO Source Materials 
 *      © Copyright IBM Corp. 2012 
 *
 *      See the NOTICE file distributed with this work for additional 
 *      information regarding copyright ownership 
 *       
 *      Licensed under the Apache License, Version 2.0 (the "License"); 
 *      you may not use this file except in compliance with the License. 
 *      You may obtain a copy of the License at 
 *       	http://www.apache.org/licenses/LICENSE-2.0 
 *       
 *      Unless required by applicable law or agreed to in writing, software 
 *      distributed under the License is distributed on an "AS IS" BASIS, 
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *      See the License for the specific language governing permissions and 
 *      limitations under the License. 
 *
 */
package org.universAAL.service.asor.owls.extensions.tests;

import java.io.IOException;


/**
 * 
 *  @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 *	
 *  Aug 28, 2012
 *
 */
public class TestParseString {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String fileContent = FileReader.readFile("C:/Users/noamsh/Desktop/NonWorkingSR.txt");
		String parsedString = fileContent.replaceAll("\\[ rdf:first", "(");
		
		StringBuffer newFileContent = new StringBuffer();
		
		String[] arr = parsedString.split("\n");
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].contains("rdf:rest")) {
				newFileContent.delete(newFileContent.length() -  (arr[i - 1] + "\n").length(), newFileContent.length());
				newFileContent.append(arr[i - 1].substring(0, arr[i - 1].length() - 2) + "\n");
				newFileContent.append(");\n");
				i++;
			} else {
				newFileContent.append(arr[i] + "\n");
			}
		}

		System.out.println("Old file content");
		System.out.println("--------------------------------");
		System.out.println(fileContent);
		System.out.println("--------------------------------");
		System.out.println("New file content");
		System.out.println("--------------------------------");
		System.out.println(newFileContent.toString());

	}

}
