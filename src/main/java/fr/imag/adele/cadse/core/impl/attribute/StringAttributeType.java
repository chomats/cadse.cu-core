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

import java.util.UUID;

import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.attribute.CheckStatus;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.ui.UIFieldImpl;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.ui.EPosLabel;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.ui.UIPlatform;
import fr.imag.adele.cadse.core.util.Convert;

/**
 * The Class StringAttributeType.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class StringAttributeType extends AttributeType implements
		fr.imag.adele.cadse.core.attribute.StringAttributeType {

	/** The value. */
	private String value;

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
	public StringAttributeType(UUID id, String name, int min, String value) {
		this(id, min != 0 ? SHOW_IN_DEFAULT_CP : 0, name, value);
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
	public StringAttributeType(UUID id, int flag, String name, String value) {
		super(id, name, flag);
		this.value = value;
	}

	public StringAttributeType(ItemDelta item) {
		super(item);
	}

	public StringAttributeType() {
	}

	/*
	 * (non-Javadoc)
	 * @see fr.imag.adele.cadse.core.IAttributeType#getDefaultValue()
	 */
	@Override
	public String getDefaultValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.imag.adele.cadse.core.IAttributeType#getAttributeType()
	 */
	public Class<String> getAttributeType() {
		return String.class;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.imag.adele.cadse.core.INamed#getIntID()
	 */
	public int getIntID() {
		return 0;
	}

	@Override
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
	public boolean commitSetAttribute(IAttributeType<?> type, Object value) {
		if (CadseGCST.ATTRIBUTE_at_DEFAULT_VALUE_ == type) {
			setValue(Convert.toString(value));
			return true;
		}
		if (CadseGCST.STRING_at_NOT_EMPTY_ == type) {
			setIsNotEmpty(Convert.toBoolean(value));
			return true;
		}
		return super.commitSetAttribute(type, value);
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setIsNotEmpty(boolean flag) {
		setFlag(NOT_EMPTY, flag);
	}

	@Override
	public CheckStatus check(Item item, Object value) {
		if (!getFlag(CAN_BE_UNDEFINED) && value == VALUE_NOT_DEFINED) {
			return new CheckStatus(UIPlatform.ERROR, Messages.cannot_be_undefined);
		}
		if (value == null) {
			return null;
		}
		if (!(value instanceof String)) {
			return new CheckStatus(UIPlatform.ERROR, Messages.must_be_a_string);
		}
		if (getFlag(NOT_EMPTY) && value.toString().length() == 0) {
			return new CheckStatus(UIPlatform.ERROR, Messages.cannot_be_empty);
		}
		return null;
	}

	@Override
	public String convertTo(Object v) {
		if (v instanceof String) {
			return (String) v;
		}
		return null;
	}

	@Override
	public UIField generateDefaultField() {
		return new UIFieldImpl(CadseGCST.DTEXT, UUID.randomUUID(), this, getDisplayName(), EPosLabel.left, null, null);
	}
}
