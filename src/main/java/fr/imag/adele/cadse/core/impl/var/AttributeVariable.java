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

import java.util.UUID;

import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.var.ContextVariable;

/**
 * The Class AttributeVariable.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class AttributeVariable extends VariableImpl {

	/** The key. */
	IAttributeType<String>	key;

	/**
	 * Instantiates a new attribute variable.
	 * 
	 * @param key
	 *            the key
	 */
	public AttributeVariable(IAttributeType<String> key) {
		super();
		this.key = key;
	}

	/**
	 * Instantiates a new attribute variable.
	 * 
	 * @param id
	 *            the id
	 * @param key
	 *            the key
	 */
	public AttributeVariable(UUID id, String name, IAttributeType<String> key) {
		super(id, name);
		this.key = key;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.var.Variable#compute(fr.imag.adele.cadse.core.var.ContextVariable,
	 *      fr.imag.adele.cadse.core.Item)
	 */
	public String compute(ContextVariable context, Item itemCurrent) {
		return context.getAttribute(itemCurrent, key);
	}

}
