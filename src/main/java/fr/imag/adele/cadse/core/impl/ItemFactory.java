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

import fr.imag.adele.cadse.core.CadseGCST;
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
import fr.imag.adele.cadse.core.impl.attribute.IntegerAttributeType;
import fr.imag.adele.cadse.core.impl.attribute.LongAttributeType;
import fr.imag.adele.cadse.core.impl.attribute.StringAttributeType;
import fr.imag.adele.cadse.core.impl.attribute.TimeAttributeType;
import fr.imag.adele.cadse.core.impl.attribute.URLAttributeType;
import fr.imag.adele.cadse.core.impl.attribute.UUIDAttributeType;
import fr.imag.adele.cadse.core.impl.internal.ExtItemTypeImpl;
import fr.imag.adele.cadse.core.impl.internal.ItemImpl;
import fr.imag.adele.cadse.core.impl.internal.ItemTypeImpl;
import fr.imag.adele.cadse.core.impl.internal.LinkTypeImpl;
import fr.imag.adele.cadse.core.impl.ui.PageImpl;
import fr.imag.adele.cadse.core.impl.ui.UIFieldImpl;

public class ItemFactory implements IItemFactory {
	static final public ItemFactory	SINGLETON	= new ItemFactory();

	private boolean isThisOrSubType(ItemType it, ItemType superType) {
		return superType.isSuperTypeOf(it) || it == superType;
	}

	public Item newForCommitItem(LogicalWorkspace wl, ItemType it, ItemDelta item) {
		if (isThisOrSubType(it, CadseGCST.ITEM_TYPE)) {
			return new ItemTypeImpl(wl, it, item);
		}
		CompactUUID id = item.getId();
		String shortName = item.getName();
		if (it == CadseGCST.BOOLEAN) {
			return new BooleanAttributeType(item);
		}
		if (it == CadseGCST.STRING) {
			return new StringAttributeType(item);
		}
		if (it == CadseGCST.DOUBLE) {
			return new DoubleAttributeType(item);
		}
		if (it == CadseGCST.ENUM) {
			return new EnumAttributeType(item);
		}
		/*if (it == CadseGCST.FLAG_ATTRIBUTE_TYPE) {
			return new FlagIntegerAttributeType(item);
		}*/
		if (it == CadseGCST.INTEGER) {
			return new IntegerAttributeType(item);
		}
		if (it == CadseGCST.LINK) {
			return new LinkTypeImpl(item);
		}
		/*if (it == CadseGCST.LIST_ATTRIBUTE_TYPE) {
			return new ListAttributeType(item);
		}
		if (it == CadseGCST.MAP_ATTRIBUTE_TYPE) {
			return new MapAttributeType(item);
		}
		if (it == CadseGCST.VARIABLE) {
			// return new MapAttributeType(id,shortName, 0, null, null);
		}*/
		if (it == CadseGCST.PAGE) {
			return new PageImpl(id, shortName, null);
		}
		if (it == CadseGCST.FIELD || CadseGCST.FIELD.isSuperTypeOf(it)) {
			return new UIFieldImpl(it, id);
		}
		if (it == CadseGCST.UUID) {
			return new UUIDAttributeType(id, shortName);
		}
		if (it == CadseGCST.URL) {
			return new URLAttributeType(id, shortName, 0);
		}
		if (it == CadseGCST.DATE) {
			return new DateAttributeType(id, shortName, 0);
		}
		if (it == CadseGCST.TIME) {
			return new TimeAttributeType(id, shortName, 0);
		}
		if (it == CadseGCST.LONG) {
			return new LongAttributeType(id, shortName, 0);
		}

		if (it == CadseGCST.EXT_ITEM_TYPE) {
			return new ExtItemTypeImpl(wl, it, item); 
		} 
		
		if (it == CadseGCST.CADSE_DEFINITION) {
			return new CadseDefinitionImpl(item.getQualifiedName(), id, id);
		}
		
		return new ItemImpl(wl, it, item);
	}

}
