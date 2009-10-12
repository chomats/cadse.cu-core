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

package fr.imag.adele.cadse.core.impl.attribute;

import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.attribute.CheckStatus;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.delta.ItemDelta;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.ui.IPageController;
import fr.imag.adele.cadse.core.util.Convert;

/**
 * The Class StringAttributeType.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class StringAttributeType extends AttributeType implements fr.imag.adele.cadse.core.attribute.StringAttributeType {

	/** The value. */
	private String	value;

	/**
	 * Instantiates a new string attribute type.
	 * 
	 * @param id
	 *            the id
	 * @param min
	 *            the min
	 * @param value
	 *            the value
	 */
	public StringAttributeType(CompactUUID id, String name, int min, String value) {
		this(id, min != 0 ? MUST_BE_INITIALIZED_AT_CREATION_TIME : 0, name, value);
	}

	/**
	 * Instantiates a new string attribute type.
	 * 
	 * @param id
	 *            the id
	 * @param min
	 *            the min
	 * @param value
	 *            the value
	 */
	public StringAttributeType(CompactUUID id, int flag, String name, String value) {
		super(id, name, flag);
		this.value = value;
	}

	public StringAttributeType(ItemDelta item) {
		super(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IAttributeType#getDefaultValue()
	 */
	@Override
	public String getDefaultValue() {
		return value;
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
	 * @see fr.imag.adele.cadse.core.INamed#getIntID()
	 */
	public int getIntID() {
		return 0;
	}

	public ItemType getType() {
		return CadseGCST.STRING;
	}

	@Override
	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {
		if (CadseGCST.ATTRIBUTE_at_DEFAULT_VALUE_ == type) {
			return (T) value;
		}
		if (CadseGCST.STRING_at_NOT_EMPTY_ == type) {
			return (T) Boolean.valueOf(getFlag(NOT_EMPTY));
		}
		return super.internalGetOwnerAttribute(type);
	}

	@Override
	public boolean commitSetAttribute(IAttributeType<?> type, String key, Object value) {
		if (CadseGCST.ATTRIBUTE_at_DEFAULT_VALUE_ == type) {
			setValue(Convert.toString(value));
			return true;
		}
		if (CadseGCST.STRING_at_NOT_EMPTY_ == type) {
			setIsNotEmpty(Convert.toBoolean(value));
			return true;
		}
		return super.commitSetAttribute(type, key, value);
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setIsNotEmpty(boolean flag) {
		setflag(flag, NOT_EMPTY);
	}

	@Override
	public CheckStatus check(Item item, Object value) {
		if (!getFlag(CAN_BE_UNDEFINED) && value == null) {
			return new CheckStatus(IPageController.ERROR, Messages.cannot_be_undefined);
		}
		if (value == null) {
			return null;
		}
		if (!(value instanceof String)) {
			return new CheckStatus(IPageController.ERROR, Messages.must_be_a_string);
		}
		if (getFlag(NOT_EMPTY) && value.toString().length() == 0) {
			return new CheckStatus(IPageController.ERROR, Messages.cannot_be_empty);
		}
		return null;
	}

	@Override
	public Object convertTo(Object v) {
		if (v instanceof String) {
			return v;
		}
		return null;
	}
}
