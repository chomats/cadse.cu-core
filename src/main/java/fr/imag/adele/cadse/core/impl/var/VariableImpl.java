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
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.impl.attribute.AttributeType;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.var.ContextVariable;

/**
 * The Class Variable.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public abstract class VariableImpl extends AttributeType implements fr.imag.adele.cadse.core.var.Variable {

	/**
	 * Instantiates a new variable.
	 * 
	 * @param id
	 *            the id
	 */
	public VariableImpl(UUID id, String name) {
		super(id, name, 0);
	}

	/**
	 * Instantiates a new variable.
	 * 
	 * @param operationId
	 *            the id
	 */
	public VariableImpl() {
		super(null, null, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IAttributeType#getAttributeType()
	 */
	public Class<String> getAttributeType() {
		return String.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IAttributeType#getDefaultValue()
	 */
	@Override
	public String getDefaultValue() {
		return null;
	}

	public ItemType getType() {
		return null; //CadseGCST.VARIABLE_ATTRIBUTE_TYPE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.INamed#getIntID()
	 */
	public int getIntID() {
		return 0;
	}

	/**
	 * Checks if is null.
	 * 
	 * @return true, if is null
	 */
	public boolean isNull() {
		return false;
	}

	@Override
	public String convertTo(Object v) {
		return (String) v;
	}

	@Override
	public UIField generateDefaultField() {
		return null;
	}

}
