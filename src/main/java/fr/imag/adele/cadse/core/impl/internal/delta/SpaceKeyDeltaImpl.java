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
package fr.imag.adele.cadse.core.impl.internal.delta;

import fr.imag.adele.cadse.core.key.Key;
import fr.imag.adele.cadse.core.key.KeyDefinition;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.transaction.delta.OperationTypeCst;
import fr.imag.adele.cadse.core.transaction.delta.SetAttributeOperation;
import fr.imag.adele.cadse.core.transaction.delta.SpaceKeyDelta;
import fr.imag.adele.cadse.core.transaction.delta.WLWCOperationImpl;
import fr.imag.adele.cadse.core.util.Convert;

public class SpaceKeyDeltaImpl extends WLWCOperationImpl implements SpaceKeyDelta {

	public SpaceKeyDeltaImpl(ItemDelta parent) {
		super(OperationTypeCst.KEY_DELTA, parent);
	}

	SetAttributeOperation[]	_attOperation;
	Key				_newKey;
	Key				_oldKey;

	public SetAttributeOperation[] getAttributeOperations() {
		return _attOperation;
	}

	public KeyDefinition getKeyType() {
		return _newKey != null ? _newKey.getType() : _oldKey != null ? _oldKey.getType() : null;
	}

	public Key getNewKey() {
		return _newKey;
	}

	public Key getOldKey() {
		return _oldKey;
	}

	public void toString(StringBuilder sb, String tab) {
		if (_oldKey != null) {
			sb.append(tab).append("oldKey=").append(_oldKey).append("\n");
		}
		if (_newKey != null) {
			sb.append(tab).append("newKey=").append(_newKey).append("\n");
		}
		if (isChangeParent()) {
			sb.append(tab).append("change parent\n");
		}
	}

	@Override
	public boolean isModified() {
		return _newKey != null && _oldKey != null && (!(_newKey.equals(_oldKey))) || isChangeParent()
				|| (_oldKey == null && _newKey != null) || (_oldKey != null && _newKey == null);
	}

	public void setNewKey(Key key) {
		_newKey = key;
	}

	public void setOldKey(Key key) {
		_oldKey = key;
	}

	public boolean isChangeParent() {
		return _newKey != null && _oldKey != null
				&& !Convert.equals(_newKey.getParentKey(), _oldKey.getParentKey());
	}

}
