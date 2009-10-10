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

import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.attribute.CheckStatus;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.delta.ItemDelta;
import fr.imag.adele.cadse.core.ui.IPageController;
import fr.imag.adele.cadse.core.util.Convert;

/**
 * The Class BooleanAttributeType.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class BooleanAttributeType extends AttributeType implements
		fr.imag.adele.cadse.core.attribute.BooleanAttributeType {

	/** The value. */
	private boolean	_defaultValue;

	/**
	 * Instantiates a new boolean attribute type.
	 * 
	 * @param id
	 *            the id
	 * @param min
	 *            the min
	 * @param value
	 *            the value
	 */
	public BooleanAttributeType(CompactUUID id, int flag, String name, String value) {
		super(id, name, flag);
		this._defaultValue = value == null ? CadseGCST.ATTRIBUTE_at_DEFAULT_VALUE_ == null ? false
				: Boolean.valueOf(CadseGCST.ATTRIBUTE_at_DEFAULT_VALUE_.getDefaultValue()) : Boolean.valueOf(value);
	}

	public BooleanAttributeType(ItemDelta item) {
		super(item);
		this._defaultValue = Convert.toBoolean(item.getAttribute(CadseGCST.ATTRIBUTE_at_DEFAULT_VALUE_),
				false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IAttributeType#getDefaultValue()
	 */
	@Override
	public Boolean getDefaultValue() {
		return _defaultValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IAttributeType#getAttributeType()
	 */
	public Class<Boolean> getAttributeType() {
		return Boolean.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.INamed#getIntID()
	 */
	public int getIntID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {
		if (CadseGCST.ATTRIBUTE_at_DEFAULT_VALUE_ == type) {
			return (T) (_defaultValue ? Boolean.TRUE.toString() : Boolean.FALSE.toString());
		}
		return super.internalGetOwnerAttribute(type);
	}

	@Override
	public boolean commitSetAttribute(IAttributeType<?> type, String key, Object value) {
		if (CadseGCST.ATTRIBUTE_at_DEFAULT_VALUE_ == type) {
			value = Convert.toBoolean(value,  false);
			return true;
		}
		return super.commitSetAttribute(type, key, value);
	}

	public ItemType getType() {
		return CadseGCST.BOOLEAN;
	}

	@Override
	public Object convertTo(Object v) {
		return Convert.toBoolean(v);
	}

	@Override
	public CheckStatus check(Item item, Object value) {
		CheckStatus ret = super.check(item, value);
		if (ret != null) {
			return ret;
		}

		if (value == null) {
			return null;
		}

		if (value instanceof String) {
			value = Boolean.parseBoolean((String) value);
		}
		if (!(value instanceof Boolean)) {
			return new CheckStatus(IPageController.ERROR, "Must be a boolean");
		}
		return null;
	}

}
