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
package fr.imag.adele.cadse.core.impl;

import fr.imag.adele.cadse.core.CadseRootCST;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.IItemFactory;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.delta.ItemDelta;
import fr.imag.adele.cadse.core.impl.attribute.BooleanAttributeType;
import fr.imag.adele.cadse.core.impl.attribute.DateAttributeType;
import fr.imag.adele.cadse.core.impl.attribute.DoubleAttributeType;
import fr.imag.adele.cadse.core.impl.attribute.EnumAttributeType;
import fr.imag.adele.cadse.core.impl.attribute.FlagIntegerAttributeType;
import fr.imag.adele.cadse.core.impl.attribute.IntegerAttributeType;
import fr.imag.adele.cadse.core.impl.attribute.ListAttributeType;
import fr.imag.adele.cadse.core.impl.attribute.LongAttributeType;
import fr.imag.adele.cadse.core.impl.attribute.MapAttributeType;
import fr.imag.adele.cadse.core.impl.attribute.StringAttributeType;
import fr.imag.adele.cadse.core.impl.attribute.TimeAttributeType;
import fr.imag.adele.cadse.core.impl.attribute.URLAttributeType;
import fr.imag.adele.cadse.core.impl.attribute.UUIDAttributeType;
import fr.imag.adele.cadse.core.impl.code.ScriptItem;
import fr.imag.adele.cadse.core.impl.internal.ItemImpl;
import fr.imag.adele.cadse.core.impl.internal.ItemTypeImpl;
import fr.imag.adele.cadse.core.impl.internal.LinkTypeImpl;
import fr.imag.adele.cadse.core.impl.internal.ui.IPage2;
import fr.imag.adele.cadse.core.impl.ui.UIField2;
import fr.imag.adele.cadse.core.impl.ui.UIListenerImpl;

public class ItemFactory implements IItemFactory {
	static final public ItemFactory	SINGLETON	= new ItemFactory();

	private boolean isThisOrSubType(ItemType it, ItemType superType) {
		return superType.isSuperTypeOf(it) || it == superType;
	}

	public Item newForCommitItem(LogicalWorkspace wl, ItemType it, ItemDelta item) {
		if (isThisOrSubType(it, CadseRootCST.META_ITEM_TYPE)) {
			return new ItemTypeImpl(wl, it, item);
		}
		CompactUUID id = item.getId();
		String shortName = item.getName();
		if (it == CadseRootCST.BOOLEAN_ATTRIBUTE_TYPE) {
			return new BooleanAttributeType(item);
		}
		if (it == CadseRootCST.STRING_ATTRIBUTE_TYPE) {
			return new StringAttributeType(item);
		}
		if (it == CadseRootCST.DOUBLE_ATTRIBUTE_TYPE) {
			return new DoubleAttributeType(item);
		}
		if (it == CadseRootCST.ENUM_ATTRIBUTE_TYPE) {
			return new EnumAttributeType(item);
		}
		if (it == CadseRootCST.FLAG_ATTRIBUTE_TYPE) {
			return new FlagIntegerAttributeType(item);
		}
		if (it == CadseRootCST.INTEGER_ATTRIBUTE_TYPE) {
			return new IntegerAttributeType(item);
		}
		if (it == CadseRootCST.LINK_DEFINITION_ATTIBUTE_TYPE) {
			return new LinkTypeImpl(item);
		}
		if (it == CadseRootCST.LIST_ATTRIBUTE_TYPE) {
			return new ListAttributeType(item);
		}
		if (it == CadseRootCST.MAP_ATTRIBUTE_TYPE) {
			return new MapAttributeType(item);
		}
		if (it == CadseRootCST.VARIABLE_ATTRIBUTE_TYPE) {
			// return new MapAttributeType(id,shortName, 0, null, null);
		}
		if (it == CadseRootCST.PAGE_DEFINITION_TYPE) {
			return new IPage2(id, shortName, null);
		}
		if (it == CadseRootCST.FIELD_TYPE) {
			return new UIField2(id, shortName);
		}
		if (it == CadseRootCST.UILISTENER) {
			return new UIListenerImpl(id);
		}
		if (it == CadseRootCST.SCRIPT) {
			return new ScriptItem(id, shortName);
		}
		if (it == CadseRootCST.UUIDATTRIBUTE_TYPE) {
			return new UUIDAttributeType(id, shortName);
		}
		if (it == CadseRootCST.URLATTRIBUTE_TYPE) {
			return new URLAttributeType(id, shortName, 0);
		}
		if (it == CadseRootCST.DATE_ATTRIBUTE_TYPE) {
			return new DateAttributeType(id, shortName, 0);
		}
		if (it == CadseRootCST.TIME_ATTRIBUTE_TYPE) {
			return new TimeAttributeType(id, shortName, 0);
		}
		if (it == CadseRootCST.LONG_ATTRIBUTE_TYPE) {
			return new LongAttributeType(id, shortName, 0);
		}

		return new ItemImpl(wl, it, item);
	}

}
