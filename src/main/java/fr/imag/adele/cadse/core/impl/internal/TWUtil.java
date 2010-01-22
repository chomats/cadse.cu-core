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
package fr.imag.adele.cadse.core.impl.internal;

import java.util.List;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.enumdef.TWDestEvol;
import fr.imag.adele.cadse.core.enumdef.TWEvol;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;

/**
 * Couple of methods useful for TeamWork implementation.
 * 
 * @author Thomas
 *
 */
public class TWUtil {

	/**
	 * Sets RevModified flag to true.
	 * Specified item will be considered as modified by TeamWork. 
	 * 
	 * @param item an item
	 * @throws CadseException
	 */
	public static void setRevModified(Item item) throws CadseException {
		item.setAttribute(CadseGCST.ITEM_at_REV_MODIFIED_, true);
	}

	/**
	 * Sets requireNewRev flag to true.
	 * Specified item will be considered as modified by TeamWork 
	 * and next commit will create a new revision of this item. 
	 * 
	 * @param item an item
	 * @throws CadseException
	 */
	public static void setRequireNewRev(Item item) throws CadseException {
		// NewRev implies Modified flag set to true
		setRevModified(item);
		item.setAttribute(CadseGCST.ITEM_at_REQUIRE_NEW_REV_, true);
	}

	/**
	 * Returns impact flag of a change on a destination.
	 * 
	 * @param attrDef an attribute definition
	 * @return impact flag of a change on a destination.
	 */
	public static TWDestEvol getDestEvol(IAttributeType<?> attrDef) {
		return attrDef.getAttribute(CadseGCST.LINK_TYPE_at_TWDEST_EVOL_);
	}

	/**
	 * Returns impact flag of a change on an item of type defining the specified attribute.
	 * 
	 * @param attrDef an attribute definition
	 * @return impact flag of a change on an item of type defining the specified attribute.
	 */
	public static TWEvol getEvol(IAttributeType<?> attrDef) {
		return attrDef.getAttribute(CadseGCST.ATTRIBUTE_at_TWEVOL_);
	}

	/**
	 * Returns true if next commit of this item will create a new revision of it.
	 * 
	 * @param modifiedItem an item
	 * @return true if next commit of this item will create a new revision of it.
	 */
	public static boolean isRequireNewRev(Item modifiedItem) {
		// TODO change to default value....
		Object ret = modifiedItem.getAttribute(CadseGCST.ITEM_at_REQUIRE_NEW_REV_);
		if (ret == null) {
			return true;
		}
		return (Boolean) ret;
	}
	
	/**
	 * Returns true if next commit of this item will modify current revision of it.
	 * 
	 * @param modifiedItem an item
	 * @return true if next commit of this item will modify current revision of it.
	 */
	public static boolean isInChangedState(Item modifiedItem) {
		
		return !isRequireNewRev(modifiedItem) && isModified(modifiedItem);
	}
	
	/**
	 * Returns true if this item is considered as modified by TeamWork.
	 * 
	 * @param modifiedItem an item
	 * @return true if this item is considered as modified by TeamWork.
	 */
	public static boolean isModified(Item modifiedItem) {
		//TODO change to default value....
		Object ret = modifiedItem.getAttribute(CadseGCST.ITEM_at_REV_MODIFIED_);
		if (ret == null)
			return true;
		return (Boolean) ret;
	}
	
	/**
	 * Set TW attribute modified state.
	 * 
	 * @param item
	 *            item on which we want to change state
	 * @param attrDef
	 *            attribute definition
	 * @param modifiedVal
	 *            new modified flag value
	 * @throws CadseException
	 */
	public static void setAttrModified(ItemDelta item, IAttributeType<?> attrDef, boolean modifiedVal)
			throws CadseException {
		item.setTWAttributeModified(attrDef, modifiedVal);
	}
	
	/**
	 * Defines specified item and all its attributes as not modified.
	 * 
	 * @param modifiedItem
	 */
	public static void resetTWState(Item modifiedItem) {
		try {
			modifiedItem.setAttribute(CadseGCST.ITEM_at_REV_MODIFIED_, false);
			modifiedItem.setAttribute(CadseGCST.ITEM_at_REQUIRE_NEW_REV_, false);
			
			List<Link> modifiedLinks = modifiedItem.getOutgoingLinks(CadseGCST.ITEM_lt_MODIFIED_ATTRIBUTES);
			if (modifiedLinks == null)
				return;
			for (Link modifiedLink : modifiedLinks) {
				try {
					modifiedLink.delete();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (CadseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns true if specified attribute is an attribute used for TeamWork component.
	 * 
	 * @return true if specified attribute is an attribute used for TeamWork component.
	 */
	public static boolean isTWAttribute(IAttributeType<?> attrDef) {
		
		if ((attrDef == CadseGCST.ITEM_lt_MODIFIED_ATTRIBUTES) ||
				(attrDef == CadseGCST.ITEM_at_COMMITTED_BY_) ||
				(attrDef == CadseGCST.ITEM_at_COMMITTED_DATE_) ||
				(attrDef == CadseGCST.ITEM_at_REQUIRE_NEW_REV_) ||
				(attrDef == CadseGCST.ITEM_at_REV_MODIFIED_) ||
				(attrDef == CadseGCST.ITEM_at_TW_VERSION_) ||
				(attrDef == CadseGCST.ITEM_at_TWLAST_COMMENT_))
			return true;
		
		return false;
	}
}
