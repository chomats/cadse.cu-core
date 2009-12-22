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
package fr.imag.adele.cadse.core.impl.ui.ic;


import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.Item_Descriptor;
import fr.imag.adele.cadse.core.ui.UIField;

public class IC_Descriptor extends Item_Descriptor  {
	
	


	public IC_Descriptor(ItemType it, Object... keyvalues) {
		super(it, keyvalues);
	}

	@Override
	public String getName() {
		return "ic";
	}

	public Item getModelController() {
		return ((UIField) _parent).getModelController();
	}

	public UIField getUIField() {
		return ((UIField) _parent);
	}

	public void setParent(Item parent, LinkType lt) {
		 _parent = (UIField) parent;
	}

	@Override
	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {
		if (CadseGCST.ITEM_at_NAME_ == type) {
			return (T) "ic";
		}
		return super.internalGetOwnerAttribute(type);
	}

}
