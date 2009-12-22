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

import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.util.ArraysUtil;

public class OverwrittenItem extends AbstractGeneratedItem {
	Item		source;
	Item		parent					= null;
	Object[]	attributeValuesArray	= null;

	public ItemType getType() {
		return source.getType();
	}

	public void setParent(Item parent, LinkType lt) {
		this.parent = parent;
	}

	@Override
	public boolean commitSetAttribute(IAttributeType<?> type, Object value) {
		if (value == null) {
			removeAttribute(type);
		} else {
			addAttribute(type, value);
		}
		return super.commitSetAttribute(type, value);
	}

	private void addAttribute(IAttributeType<?> type, Object value) {
		this.attributeValuesArray = ArraysUtil.addList2(Object.class, this.attributeValuesArray, type, value);
	}

	private void removeAttribute(IAttributeType<?> type) {
		int index = ArraysUtil.indexOf(this.attributeValuesArray, type);
		if (index != -1) {
			this.attributeValuesArray = ArraysUtil.remove(Object.class, this.attributeValuesArray, index, 2);
		}
	}

}
