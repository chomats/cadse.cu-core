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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.attribute.CheckStatus;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.ui.UIPlatform;

public class DateAttributeType extends AttributeType implements fr.imag.adele.cadse.core.attribute.DateAttributeType {

	Date	_defaultValue;
	String	_format	= "EEE MMM dd HH:mm:ss zzz yyyy";

	public DateAttributeType(UUID id, String name, int flag) {
		super(id, name, flag);
	}

	public DateAttributeType(UUID id, int flag, String name, String defaultValue) {
		super(id, name, flag);
		if (defaultValue != null) {
			this._defaultValue = convertTo(defaultValue);
		}
	}

	public DateAttributeType(ItemDelta item) {
		super(item);
	}

    public DateAttributeType() {

    }

	public ItemType getType() {
		return CadseGCST.DATE;
	}

	public DateFormat getFormat() {
		return new SimpleDateFormat(_format, Locale.US);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IAttributeType#getDefaultValue()
	 */
	@Override
	public Date getDefaultValue() {
		return _defaultValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IAttributeType#getAttributeType()
	 */
	public Class<Date> getAttributeType() {
		return Date.class;
	}

	public int getIntID() {
		return 0;
	}

	@Override
	public Date convertTo(Object v) {
		if (v == null || "".equals(v)) {
			return null;
		}
		if (v instanceof Date) {
			return (Date) v;
		}
		if (v instanceof String) {
			try {
				return getFormat().parse((String) v);
			} catch (ParseException e) {
				throw new IllegalArgumentException("Cannot convert value '" + v + "'of type Date attribute '"
						+ getName() + "'", e);
			}
		}
		throw new ClassCastException("Cannot convert value '" + v + "'of type Date attribute '" + getName());
	}

	@Override
	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {
		if (CadseGCST.ATTRIBUTE_at_DEFAULT_VALUE_ == type) {
			return (T) _defaultValue;
		}
		return super.internalGetOwnerAttribute(type);
	}

	public void setDefaultValue(Date value) {
		this._defaultValue = value;
	}

	@Override
	public boolean commitSetAttribute(IAttributeType<?> type, Object value) {
		if (CadseGCST.ATTRIBUTE_at_DEFAULT_VALUE_ == type) {
			setDefaultValue(convertTo(value));
			return true;
		}
		return super.commitSetAttribute(type, value);
	}

	@Override
	public CheckStatus check(Item item, Object value) {
		if ("".equals(value)) {
			value = null;
		}

		CheckStatus s = super.check(item, value);
		if (s != null) {
			return s;
		}

		if (value == null) {
			return null; // null value is acceptable if super call ne donne
			// pas d'erreur (accept une valeur non définie)
		}

		if (value instanceof Date) {
			return null;
		}
		if (value instanceof String) {
			try {
				getFormat().parse((String) value);
				return null;
			} catch (ParseException e) {
				new CheckStatus(UIPlatform.ERROR, "Date attribute ''{0}'' : {1}" , getName(), e.getMessage());
			}
		}
		return new CheckStatus(UIPlatform.ERROR, "Date attribute ''{0}'' : invalid value ''{1}''" , getName(), value);

	}

}
