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
package fr.imag.adele.cadse.core.impl.attribute;

import java.util.UUID;

import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.impl.internal.delta.ItemDeltaImpl;
import fr.imag.adele.cadse.core.CadseGCST;

public class UUIDAttributeType extends AttributeType implements fr.imag.adele.cadse.core.attribute.UUIDAttributeType {

	boolean	automatic_generate	= false;

	public UUIDAttributeType(CompactUUID id, String name, int flag) {
		super(id, name, flag);
	}

	public UUIDAttributeType(ItemDeltaImpl item) {
		super(item);
	}

	public UUIDAttributeType(CompactUUID id, String shortName) {
		this(id, shortName, 0);
	}

	public Class<CompactUUID> getAttributeType() {
		return CompactUUID.class;
	}

	public ItemType getType() {
		return CadseGCST.UUID;
	}

	public int getIntID() {
		return 0;
	}

	@Override
	public CompactUUID getDefaultValue() {
		if (automatic_generate) {
			return CompactUUID.randomUUID();
		}
		return null;
	}
	
	@Override
	public CompactUUID convertTo(Object v) {
		// TODO Auto-generated method stub
		return (CompactUUID) super.convertTo(v);
	}
}
