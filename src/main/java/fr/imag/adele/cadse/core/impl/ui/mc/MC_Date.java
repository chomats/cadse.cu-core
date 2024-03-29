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
package fr.imag.adele.cadse.core.impl.ui.mc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.ui.UIField;

public class MC_Date extends MC_AttributesItem {

	private static final String DD_MM_YY = "yyyy-MM-dd'T'HH:mm:ss";
	private String _pattern = DD_MM_YY;

	public MC_Date() {
	}

	public MC_Date(Item id) {
		super(id);
	}

	public MC_Date(String pattern) {
		_pattern = pattern;
	}

	@Override
	public Object getValue() {
		Object value = super.getValue();
		try {
//			if (value == null) {
//				IAttributeType<?> attDef = getUIField().getAttributeDefinition();
//				if (attDef != null) {
//					value = attDef.getDefaultValue();
//				}
//			}
			if (value == null) {
				return "";
			}
			return getDateFormat().format(value);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return value;
		}
	}

	@Override
	public boolean validValueChanged(UIField field, Object value) {
		if (getUIField().getAttributeDefinition().canBeUndefined()
				&& (value == null || value.equals(""))) {
			value = null;
			return false;// it's ok
		}
		try {
			value = getDateFormat().parse((String) value);
		} catch (ParseException e) {
			_uiPlatform.setMessageError(field.getName() + ": invalid date,"
					+ e.getMessage() + ", " + _pattern);
			return true;
		}
		return super.validValueChanged(field, value);
	}

	protected SimpleDateFormat getDateFormat() {
		return new SimpleDateFormat(_pattern, getLocale());
	}

	protected Locale getLocale() {
		return Locale.getDefault();
	}

}
