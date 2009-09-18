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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.delta.ItemDelta;
import fr.imag.adele.cadse.core.CadseRootCST;

public class SymbolicBitMapAttributeType extends IntegerAttributeType implements
		fr.imag.adele.cadse.core.attribute.SymbolicBitMapAttributeType {

	String[]	flags;

	/**
	 * Instantiates a new integer attribute type.
	 * 
	 * @param id
	 *            the id
	 * @param min
	 *            the min
	 * @param value
	 *            the value
	 */
	public SymbolicBitMapAttributeType(CompactUUID id, String name, int flag, String value) {
		super(id, flag, name, null, null, value);
	}

	public SymbolicBitMapAttributeType(ItemDelta item) {
		super(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.INamed#getIntID()
	 */
	@Override
	public int getIntID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ItemType getType() {
		return CadseRootCST.FLAG_ATTRIBUTE_TYPE;
	}

	@Override
	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {
		if (CadseRootCST.FLAG_ATTRIBUTE_TYPE_at_VALUES_ == type) {
			return (T) ((flags == null) ? Collections.EMPTY_LIST : Arrays.asList(flags));
		}
		return super.internalGetOwnerAttribute(type);
	}

	@Override
	public boolean commitSetAttribute(IAttributeType<?> type, String key, Object value) {
		if (CadseRootCST.FLAG_ATTRIBUTE_TYPE_at_VALUES_ == type) {
			if (value instanceof List) {
				flags = (String[]) ((List) value).toArray(new String[((List) value).size()]);
			}
			return true;
		}

		return super.commitSetAttribute(type, key, value);
	}

	@Override
	public Object convertTo(Object v) {

		return super.convertTo(v);
	}
}
