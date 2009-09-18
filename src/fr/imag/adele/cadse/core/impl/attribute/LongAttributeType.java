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

import fr.imag.adele.cadse.core.CadseRootCST;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.attribute.CheckStatus;
import fr.imag.adele.cadse.core.delta.ItemDelta;
import fr.imag.adele.cadse.core.ui.IPageController;

public class LongAttributeType extends AttributeType implements fr.imag.adele.cadse.core.attribute.LongAttributeType {
	/** The value. */
	private long	value;

	private Long	minValue;
	private Long	maxValue;

	public LongAttributeType(CompactUUID id, String name, int flag) {
		super(id, name, flag);
	}

	public LongAttributeType(ItemDelta item) {
		super(item);
	}

	public Class<Long> getAttributeType() {
		return Long.class;
	}

	public ItemType getType() {
		return CadseRootCST.LONG_ATTRIBUTE_TYPE;
	}

	public int getIntID() {
		return 0;
	}

	@Override
	public Long getDefaultValue() {
		return value;
	}

	@Override
	public CheckStatus check(Item item, Object value) {
		if (!getFlag(CAN_BE_UNDEFINED) && (value == null || value.toString().length() == 0 || value.equals("null"))) {
			return new CheckStatus(IPageController.ERROR, "Cannot be null or empty");
		}
		if (value == null) {
			return null;
		}
		if (value instanceof String) {
			if ("null".equals(value) || "unbounded".equals(value) || "".equals(value)) {
				return null;
			}

			try {
				value = Long.parseLong((String) value);
			} catch (NumberFormatException e) {
				return new CheckStatus(IPageController.ERROR, e.getMessage());
			}
		}
		if (!(value instanceof Long)) {
			return new CheckStatus(IPageController.ERROR, "Must be a integer");
		}
		long v = ((Long) value).longValue();
		if (minValue != null) {
			if (v < minValue.longValue()) {
				return new CheckStatus(IPageController.ERROR, "Value must be > " + minValue.intValue());
			}
		}
		if (maxValue != null) {
			if (v > maxValue.longValue()) {
				return new CheckStatus(IPageController.ERROR, "Value must be < " + maxValue.intValue());
			}
		}

		return null;
	}

	@Override
	public Object convertTo(Object v) {
		if (v == null || "".equals(v) || "null".equals(v)) {
			return null;
		}
		if (v instanceof String) {
			return new Long((String) v);
		}
		if (v instanceof Long) {
			return v;
		}
		throw new ClassCastException("Can't convert to primive long value the type " + v.getClass());
	}
}
