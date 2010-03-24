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
package fr.imag.adele.cadse.core.impl.ui.mc;

import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.util.Convert;

public class MC_StringToBoolean extends MC_AttributesItem {

	public MC_StringToBoolean() {
	}

	public MC_StringToBoolean(Item id) {
		super(id);
	}

	@Override
	public Object getValue() {
		Object value = super.getValue();
		if (value == null) {
			Object _defaultValue = defaultValue();
			if (_defaultValue == null) {
				_defaultValue = Boolean.FALSE;
			}

			super.notifieValueChanged(getUIField(), _defaultValue.toString());
			return _defaultValue;
		}
		return Convert.toBoolean(value);
	}

	@Override
	public void notifieValueChanged(UIField field, Object value) {
		super.notifieValueChanged(field, Convert.toBoolean(value));
	}

	@Override
	public Object defaultValue() {
		return Boolean.FALSE;
	}

}
