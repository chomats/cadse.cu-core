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
package fr.imag.adele.cadse.core.impl.attribute;

import java.util.UUID;

import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.attribute.CheckStatus;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.ui.UIFieldImpl;
import fr.imag.adele.cadse.core.impl.ui.mc.MC_Descriptor;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.ui.EPosLabel;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.ui.UIPlatform;
import fr.imag.adele.cadse.core.util.Convert;

/**
 * The Class BooleanAttributeType.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class BooleanAttributeType extends AttributeType implements
		fr.imag.adele.cadse.core.attribute.BooleanAttributeType {

	/** The value. */
	private Boolean _defaultValue;

	public BooleanAttributeType() {
	}

	/**
	 * Instantiates a new boolean attribute type.
	 * 
	 * @param id
	 *        the id
	 * @param min
	 *        the min
	 * @param value
	 *        the value
	 */
	public BooleanAttributeType(UUID id, int flag, String name, String value) {
		super(id, name, flag);
		this._defaultValue = Convert.toBoolean(value, null);
	}

	public BooleanAttributeType(ItemDelta item) {
		super(item);
		this._defaultValue = Convert.toBoolean(item.getAttribute(CadseGCST.ATTRIBUTE_at_DEFAULT_VALUE_), null);
	}

	/*
	 * (non-Javadoc)
	 * @see fr.imag.adele.cadse.core.IAttributeType#getDefaultValue()
	 */
	@Override
	public Boolean getDefaultValue() {
		if (_defaultValue == null && !canBeUndefined()) {
			return Boolean.FALSE;
		}
		return _defaultValue;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.imag.adele.cadse.core.IAttributeType#getAttributeType()
	 */
	public Class<Boolean> getAttributeType() {
		return Boolean.class;
	}

	@Override
	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {
		if (CadseGCST.ATTRIBUTE_at_DEFAULT_VALUE_ == type) {
			return (T) (_defaultValue == null ? null : _defaultValue.toString());
		}
		return super.internalGetOwnerAttribute(type);
	}

	@Override
	public boolean commitSetAttribute(IAttributeType<?> type, Object value) {
		if (CadseGCST.ATTRIBUTE_at_DEFAULT_VALUE_ == type) {
			Boolean dv = Convert.toBoolean(value, null);
			boolean ret = Convert.equals(dv, _defaultValue);
			_defaultValue = dv;
			return !ret;
		}
		return super.commitSetAttribute(type, value);
	}

	@Override
	public ItemType getType() {
		return CadseGCST.BOOLEAN;
	}

	@Override
	public Boolean convertTo(Object v) {
		return Convert.toBoolean(v, null);
	}

	@Override
	public CheckStatus check(Item item, Object value) {
		CheckStatus ret = super.check(item, value);
		if (ret != null) {
			return ret;
		}

		if (value == null || "".equals(value)) {
			return null;
		}

		if (value instanceof String) {
			value = Boolean.parseBoolean((String) value);
		}
		if (!(value instanceof Boolean)) {
			return new CheckStatus(UIPlatform.ERROR, Messages.must_be_a_boolean);
		}
		return null;
	}

	@Override
	public UIField generateDefaultField() {
		return new UIFieldImpl(CadseGCST.DCHECK_BOX, UUID.randomUUID(), this, getDisplayName(), EPosLabel.none,
				new MC_Descriptor(CadseGCST.MC_BOOLEAN), null);
	}
}
