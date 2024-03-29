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

import java.util.Date;
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
import fr.imag.adele.cadse.util.NLS;

public class LongAttributeType extends AttributeType implements
		fr.imag.adele.cadse.core.attribute.LongAttributeType {
	/** The value. */
	private Long _value;

	private Long _minValue;
	private Long _maxValue;

	public LongAttributeType(UUID id, String name, int flag) {
		super(id, name, flag);
	}

	public LongAttributeType(ItemDelta item) {
		super(item);
	}

	public LongAttributeType() {

	}

	public LongAttributeType(UUID id, String name, int flag, String value) {
		super(id, name, flag);
		_value = convertTo(value);
	}

	public Class<Long> getAttributeType() {
		return Long.class;
	}

	public ItemType getType() {
		return CadseGCST.LONG;
	}

	public int getIntID() {
		return 0;
	}

	@Override
	public Long getDefaultValue() {
		return _value;
	}

	@Override
	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {
		if (CadseGCST.ATTRIBUTE_at_DEFAULT_VALUE_ == type) {
			return (T) (_value == null ? null : _value.toString());
		}
		return super.internalGetOwnerAttribute(type);
	}

	@Override
	public boolean commitSetAttribute(IAttributeType<?> type, Object value) {
		if (CadseGCST.ATTRIBUTE_at_DEFAULT_VALUE_ == type) {
			this._value = (Convert.toLong(value, null));
			return true;
		}
		return super.commitSetAttribute(type, value);
	}

	@Override
	public CheckStatus check(Item item, Object value) {
		if (!getFlag(CAN_BE_UNDEFINED)
				&& (value == VALUE_NOT_DEFINED || value.toString().length() == 0 || value
						.equals("null"))) { //$NON-NLS-1$
			return new CheckStatus(UIPlatform.ERROR,
					Messages.cannot_be_undefined);
		}
		if (value == null) {
			return null;
		}
		if (value instanceof String) {
			if ("null".equals(value) || "unbounded".equals(value) || "".equals(value)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return null;
			}

			try {
				value = Long.parseLong((String) value);
			} catch (NumberFormatException e) {
				return new CheckStatus(UIPlatform.ERROR, e.getMessage());
			}
		}
		if (!(value instanceof Long)) {
			return new CheckStatus(UIPlatform.ERROR, Messages.must_be_a_long);
		}
		long v = ((Long) value).longValue();
		if (_minValue != null) {
			if (v < _minValue.longValue()) {
				return new CheckStatus(UIPlatform.ERROR,
						Messages.value_must_be_upper, _minValue.intValue());
			}
		}
		if (_maxValue != null) {
			if (v > _maxValue.longValue()) {
				return new CheckStatus(UIPlatform.ERROR,
						Messages.value_must_be_lower, _maxValue.intValue());
			}
		}

		return null;
	}

	@Override
	public Long convertTo(Object v) {
		if (v == null || "".equals(v) || "null".equals(v)) { //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
		if (v instanceof String) {
			return new Long((String) v);
		}
		if (v instanceof Long) {
			return (Long) v;
		}
		if (v instanceof Date) {
			return ((Date) v).getTime();
		}
		if (v instanceof Number)
			return ((Number) v).longValue();
		throw new ClassCastException(NLS.bind(Messages.cannot_convert_to_long,
				v.getClass()));
	}

	@Override
	public UIField generateDefaultField() {
		return new UIFieldImpl(CadseGCST.DTEXT, UUID.randomUUID(), this, getDisplayName(), EPosLabel.left, null, null);
	}
}
