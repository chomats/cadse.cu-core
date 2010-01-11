/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package fr.imag.adele.cadse.core.impl.var;

import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.var.ContextVariable;

/**
 * The Class PatternVariable.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class PatternVariable extends VariableImpl {

	/** The pattern. */
	private String	pattern;

	/** The item. */
	private Item	item;

	/**
	 * Instantiates a new pattern variable.
	 * 
	 * @param pattern
	 *            the pattern
	 * @param item
	 *            the item
	 */
	public PatternVariable(String pattern, Item item) {
		this.pattern = pattern;
		this.item = item;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.var.Variable#compute(fr.imag.adele.cadse.core.var.ContextVariable,
	 *      fr.imag.adele.cadse.core.Item)
	 */
	public String compute(ContextVariable context, Item itemCurrent) {
		// TODO Auto-generated method stub
		return null;
	}

}