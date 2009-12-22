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

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.IItemManager;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemState;
import fr.imag.adele.cadse.core.impl.CadseCore;
import fr.imag.adele.cadse.core.key.Key;
import fr.imag.adele.cadse.core.ui.UIField;

/**
 * MC controller to valid the name field.
 */

public class MC_Name extends MC_AttributesItem {

	private static final String	EMPTY_STRING	= "";


	@Override
	public void initAfterUI() {
		super.initAfterUI();
		Item item = getItem();
		_uiPlatform.setEditable(getUIField(), isEditable(item));
	}

	private boolean isEditable(Item item) {
		return item.getState() == ItemState.NOT_IN_WORKSPACE;
	}

	@Override
	public Object getValue() {
		String name = EMPTY_STRING;
		Item item = getItem();
		try {
			name = item.getName();
			if (name == Item.NO_VALUE_STRING) {
				name = EMPTY_STRING;
			}
		} catch (Throwable e) {
		}
		return name;
	}

	@Override
	public void notifieValueChanged(UIField field, Object value) {
		Item item = getItem();

		if (isEditable(item)) {
			item.setName((String) value);
		}
	}

	@Override
	public boolean validValue(UIField field, Object value) {
		Item item = getItem();
		if (!isEditable(item)) {
			return false;
		}

		final String shortId = getItem().getName();

		if (shortId.length() == 0 && item.getType().getSpaceKeyType() != null) {
			_uiPlatform.setMessageError(Messages.mc_name_must_be_specified);
			return true;
		}

		IItemManager im = item.getType().getItemManager();
		String message = im.validateShortName(item, shortId);
		if (message != null) {
			_uiPlatform.setMessageError(message);
			return true;
		}

		return super.validValue(field, value);
	}

	@Override
	public boolean validValueChanged(UIField field, Object value) {
		Item item = getItem();
		if (!isEditable(item)) {
			return false;
		}
		final String shortId = (String) value;

		if (item.getState() != ItemState.NOT_IN_WORKSPACE) {
			return false;
		}
		
		if (item.isReadOnly()) {
			return false;
		}

		if (shortId.length() == 0) { // && item.getType().getSpaceKeyType()
			_uiPlatform.setMessageError(Messages.mc_name_must_be_specified);
			return true;
		}

		IItemManager im = item.getType().getItemManager();
		String message = im.validateShortName(item, shortId);
		if (message != null) {
			_uiPlatform.setMessageError(message);
			return true;
		}
		
		if (item.getType().hasQualifiedNameAttribute()) {
			String un = im.computeQualifiedName(item, shortId, item.getPartParent(), item.getPartParentLinkType());
			Item foundItem = item.getLogicalWorkspace().getItem(un);
			if (foundItem != null && foundItem != item) {
				_uiPlatform.setMessageError(Messages.mc_name_already_exists);
				return true;
			}
		}
		
		if (item.getType().getSpaceKeyType() != null) {
			Key key;
			try {
				key = item.getType().getSpaceKeyType().computeKey(item);
				key.setName(shortId);
			} catch (CadseException e) {
				CadseCore.getCadseDomain().log("MC_Name", "cannot generate key", e);
				_uiPlatform.setMessageError(e.getMessage());
				return true;
			}
			Item foundItem = item.getLogicalWorkspace().getItem(key);
			if (foundItem != null && foundItem != item) {
				_uiPlatform.setMessageError(Messages.mc_name_already_exists);
				return true;
			}
			
		}

		return false;
	}

	@Override
	public void notifieValueDeleted(UIField field, Object oldvalue) {
	}

	@Override
	public boolean validValueDeleted(UIField field, Object removed) {
		return validValueChanged(field, EMPTY_STRING);
	}

	@Override
	public Object defaultValue() {
		return EMPTY_STRING;
	}

}