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
 *
 * Copyright (C) 2006-2010 Adele Team/LIG/Grenoble University, France
 */
package fr.imag.adele.cadse.core.impl.var;

import java.util.UUID;

import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.var.ContextVariable;

/**
 * The Class StringVariable.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class StringVariable extends VariableImpl {

	/** The cst. */
	private String	cst;

	/**
	 * Instantiates a new string variable.
	 * 
	 * @param id
	 *            the id
	 * @param cst
	 *            the cst
	 */
	public StringVariable(UUID id, String name, String cst) {
		super(id, name);
		this.cst = cst;
	}

	/**
	 * Instantiates a new string variable.
	 * 
	 * @param cst
	 *            the cst
	 */
	public StringVariable(String cst) {
		super();
		this.cst = cst;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.var.Variable#compute(fr.imag.adele.cadse.core.var.ContextVariable,
	 *      fr.imag.adele.cadse.core.Item)
	 */
	public String compute(ContextVariable context, Item itemCurrent) {
		return cst;
	}

}
