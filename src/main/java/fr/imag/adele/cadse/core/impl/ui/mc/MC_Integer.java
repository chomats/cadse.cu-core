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

import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.ui.UIPlatform;
import fr.imag.adele.cadse.core.util.Convert;

public class MC_Integer extends MC_AttributesItem {
	int		min;
	int		max;
	String	msg_min;
	String	msg_max;

	public MC_Integer(int min, int max, String msg_min, String msg_max, Integer defaultValue) {
		this.min = min;
		this.max = max;
		this.msg_min = msg_min;
		this.msg_max = msg_max;
	}

	public MC_Integer() {
		super();
	}
	
	@Override
	public void init(UIPlatform uiPlatform) {
		super.init(uiPlatform);
		min = _desc.getAttributeWithDefaultValue(CadseGCST.MC_INTEGER_at_MIN_, Integer.MIN_VALUE);
		max = _desc.getAttributeWithDefaultValue(CadseGCST.MC_INTEGER_at_MAX_, Integer.MAX_VALUE);
		msg_min = _desc.getAttribute(CadseGCST.MC_INTEGER_at_ERROR_MSG_MIN_);
		msg_max = _desc.getAttribute(CadseGCST.MC_INTEGER_at_ERROR_MSG_MAX_);
	}

	@Override
	public boolean validValueChanged(UIField field, Object visualValue) {
		boolean ret = super.validValueChanged(field, visualValue);
		if (ret)
			return true;
		if (getAttributeDefinition().canBeUndefined() && visualValue == null || visualValue.toString().length() == 0)
			return false;
		try {
			int intValue = Integer.parseInt((String) visualValue);
			if (msg_min != null && intValue < min) {
				_uiPlatform.setMessageError(msg_min);
				return true;
			}

			if (msg_max != null && intValue > max) {
				_uiPlatform.setMessageError(msg_max);
				return true;
			}
		} catch (NumberFormatException e) {
			_uiPlatform.setMessageError(getAttributeDefinition().getName()+": integer parsing error for input value '"+visualValue+"'");
			return true;
		}
		return false;
	}
	
	@Override
	public Object visualToModel(Object ret) {
		IAttributeType<?> attDef = getAttributeDefinition();
		if (attDef.isInstanceOf(CadseGCST.INTEGER))
			try {
				return Convert.toInteger(ret);
			} catch (NumberFormatException e) {
			}
		if (attDef.isInstanceOf(CadseGCST.DOUBLE))
			try {
				return Convert.toDouble(ret);
			} catch (NumberFormatException e) {
			}
		if (attDef.isInstanceOf(CadseGCST.LONG))
			try {
				return Convert.toLong(ret, null);
			} catch (NumberFormatException e) {
			}
		return ret;
	}
}
