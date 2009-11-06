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

import java.util.ArrayList;
import java.util.List;

import fr.imag.adele.cadse.core.attribute.CheckStatus;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.attribute.ListAttributeType;
import fr.imag.adele.cadse.core.impl.ui.MC_AttributesItem;
import fr.imag.adele.cadse.core.ui.IPageController;
import fr.imag.adele.cadse.core.ui.UIField;

public class MC_DefaultForList extends MC_AttributesItem {

	int	min;
	int	max;

	public MC_DefaultForList(int min, int max) {
		this.min = min;
		this.max = max;
	}

	@Override
	public Object getValue(IPageController uiPlatform) {
		Object value = super.getValue(uiPlatform);
		if (value != null) {
			return new ArrayList<Object>((ArrayList<Object>) value);
		}
		return value;
	}

	@Override
	public void notifieValueChanged(IPageController uiPlatform, UIField field, Object value) {
		value = new ArrayList<Object>((ArrayList<Object>) value);
		super.notifieValueChanged(uiPlatform, field, value);
	}

	@Override
	public Object defaultValue() {
		return new ArrayList<Object>();
	}

	@Override
	public boolean validValueChanged(IPageController uiPlatform, UIField field, Object value) {

		List l = (List) value;
		if (l == null) {
			l = new ArrayList<Object>();
		}
		if (!isEnable()) {
			if (l.size() == 0) {
				return false;

			}
			uiPlatform.setMessageError(getUIField().getLabel() + ": No values is needed");
			return true;
		}
		if (l.size() < min) {
			uiPlatform.setMessageError("The mininum of elements for the field '" + getUIField().getName() + "' is " + min);
			return true;
		}
		if (max != -1 && l.size() > max) {
			uiPlatform.setMessageError("The maximun of elements for the field '" + getUIField().getName() + "' is " + max);
			return true;
		}
		ListAttributeType<?> att = (ListAttributeType<?>) getUIField().getAttributeDefinition();
		IAttributeType<?> subatt = att == null ? null : att.getSubAttributeType();
		if (subatt != null) {
			for (Object o : l) {
				CheckStatus error = subatt.check(uiPlatform.getItem(getUIField()), o);
				if (error != null) {
					if (error.getType() == IPageController.ERROR) {
						uiPlatform.setMessageError(error.getFormatedMessage());
						return true;
					}
				}
			}
		}
		return super.validValueChanged(uiPlatform, field, value);
	}

	protected boolean isEnable() {
		return true;
	}
	
	

}
