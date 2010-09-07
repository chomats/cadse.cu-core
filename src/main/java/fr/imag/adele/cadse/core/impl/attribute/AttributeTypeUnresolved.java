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
package fr.imag.adele.cadse.core.impl.attribute;

import java.util.UUID;

import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.enumdef.TWEvol;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.ui.UIField;

public class AttributeTypeUnresolved extends AttributeType implements IAttributeType<Object> {
	ItemType _it;
	
	public AttributeTypeUnresolved(UUID id, String name, ItemType it, int flag) {
		super(id, name, flag);
		setEvol(TWEvol.twTransient);
		_it = it;
		if (_it == null)
			it = CadseGCST.UNRESOLVED_ATTRIBUTE_TYPE;
	}

	public AttributeTypeUnresolved(ItemDelta item) {
		super(item);
	}

	public ItemType getType() {
		return _it;
	}

	public Class<Object> getAttributeType() {
		return Object.class;
	}

	public int getIntID() {
		return 0;
	}
	
	@Override
	public UIField generateDefaultField() {
		return null;
	}

}
