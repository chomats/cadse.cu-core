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
package fr.imag.adele.cadse.core.impl.code;

import fr.imag.adele.cadse.core.CadseRootCST;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.enumdef.ScriptType;
import fr.imag.adele.cadse.core.impl.internal.AbstractGeneratedItem;
import fr.imag.adele.cadse.core.impl.internal.delta.ItemDeltaImpl;

public class ScriptItem extends AbstractGeneratedItem {
	Item		parent;
	String		code;
	String[]	importPackages;
	ScriptType	kind	= ScriptType.groovy;
	String		shortName;

	public ScriptItem() {
		super();
	}

	public ScriptItem(CompactUUID id, String shortName) {
		super(id);
		this.shortName = shortName;
	}

	public ScriptItem(ItemDeltaImpl item) {
		super(item);
	}

	public ItemType getType() {
		return CadseRootCST.SCRIPT;
	}

	public void setParent(Item parent, LinkType lt) {
		this.parent = parent;
	}

	@Override
	public Item getPartParent(boolean attemptToRecreate) {
		return null;
	}

	@Override
	public String getName() {
		return shortName;
	}

	@Override
	public void setShortName(String shortname) {
		this.shortName = shortname;
	}

	@Override
	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {
		if (CadseRootCST.SCRIPT_at_CODE_ == type) {
			return (T) code;
		}
		if (CadseRootCST.ITEM_TYPE_at_NAME_ == type) {
			return (T) shortName;
		}
		return super.internalGetOwnerAttribute(type);
	}

	@Override
	public boolean commitSetAttribute(IAttributeType<?> type, String key, Object value) {
		if (CadseRootCST.SCRIPT_at_CODE_ == type) {
			if (code != null && code.equals(value)) {
				return false;
			}
			if (code == null && (value == null || value.toString().length() == 0)) {
				return false;
			}
			code = value == null ? null : value.toString();
			return true;
		}
		if (CadseRootCST.SCRIPT_at_IMPORT_PACKAGES_ == type) {
			if (value instanceof String[]) {
				this.importPackages = (String[]) value;
				return true;
			}
			return false;
		}

		if (CadseRootCST.SCRIPT_at_KIND_ == type) {
			kind = CadseRootCST.SCRIPT_at_KIND_.convertTo(value);
			return true;
		}
		return super.commitSetAttribute(type, key, value);
	}
}
