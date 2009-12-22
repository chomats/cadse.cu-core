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

import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.attribute.CheckStatus;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.ui.UIFieldImpl;
import fr.imag.adele.cadse.core.impl.ui.mc.MC_Descriptor;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.ui.EPosLabel;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.ui.UIPlatform;
import fr.imag.adele.cadse.core.util.Convert;

/**
 * The Class IntegerAttributeType.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class IntegerAttributeType extends AttributeType implements
		fr.imag.adele.cadse.core.attribute.IntegerAttributeType {

	/** The min. */
	private Integer	minValue;
	/** The max. */
	private Integer	maxValue;

	/** The value. */
	private Integer	value;

        public IntegerAttributeType() {
        }


        
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
	public IntegerAttributeType(UUID id, int flag, String name, Integer min, Integer max, String value) {
		super(id, name, flag);
		this.value = value == null ? null : Convert.toInteger(value);
		this.minValue = min;
		this.maxValue = max;
	}

	public IntegerAttributeType(ItemDelta item) {
		super(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IAttributeType#getDefaultValue()
	 */
	@Override
	public Integer getDefaultValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IAttributeType#getAttributeType()
	 */
	public Class<Integer> getAttributeType() {
		return Integer.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.INamed#getIntID()
	 */
	public int getIntID() {
		// TODO Auto-generated method stub
		return 0;
	}

	public ItemType getType() {
		return CadseGCST.INTEGER;
	}

	@Override
	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {
		if (CadseGCST.ATTRIBUTE_at_DEFAULT_VALUE_ == type) {
			if (value == null)
				return null;
			return (T) value.toString();
		}
//		if (CadseGCST.INTEGER_ATTRIBUTE_TYPE_at_MIN_ == type) {
//			return (T) minValue;
//		}
//		if (CadseGCST.INTEGER_ATTRIBUTE_TYPE_at_MAX_ == type) {
//			return (T) maxValue;
//		}
		return super.internalGetOwnerAttribute(type);
	}

	@Override
	public boolean commitSetAttribute(IAttributeType<?> type, Object value) {
		if (CadseGCST.ATTRIBUTE_at_DEFAULT_VALUE_ == type) {
			setValue(Convert.toInteger(value));
			return true;
		}
//		if (CadseGCST.INTEGER_ATTRIBUTE_TYPE_at_MIN_ == type) {
//			setMinValue(Convert.toInteger(value));
//			return true;
//		}
//		if (CadseGCST.INTEGER_ATTRIBUTE_TYPE_at_MAX_ == type) {
//			setMaxValue(Convert.toInteger(value));
//			return true;
//		}
		return super.commitSetAttribute(type, value);
	}

	public Integer getMinValue() {
		return minValue;
	}

	public void setMinValue(Integer minValue) {
		this.minValue = minValue;
	}

	public Integer getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(Integer maxValue) {
		this.maxValue = maxValue;
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}

	@Override
	public CheckStatus check(Item item, Object value) {
		if (!getFlag(CAN_BE_UNDEFINED) && (value == NULL || value.toString().length() == 0 || value.equals("null"))) { //$NON-NLS-1$
			return new CheckStatus(UIPlatform.ERROR, Messages.cannot_be_undefined);
		}
		if (value == null) {
			return null;
		}
		if (value instanceof String) {
			if ("null".equals(value) || "unbounded".equals(value) || "".equals(value)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return null;
			}

			try {
				value = Integer.parseInt((String) value);
			} catch (NumberFormatException e) {
				return new CheckStatus(UIPlatform.ERROR, e.getMessage());
			}
		}
		if (!(value instanceof Integer)) {
			return new CheckStatus(UIPlatform.ERROR, Messages.must_be_an_integer);
		}
		int v = ((Integer) value).intValue();
		if (minValue != null) {
			if (v < minValue.intValue()) {
				return new CheckStatus(UIPlatform.ERROR, Messages.value_must_be_upper , minValue.intValue());
			}
		}
		if (maxValue != null) {
			if (v > maxValue.intValue()) {
				return new CheckStatus(UIPlatform.ERROR, Messages.value_must_be_lower , maxValue.intValue());
			}
		}

		return null;
	}

	@Override
	public Integer convertTo(Object v) {
		if (v == null || "".equals(v) || "null".equals(v)) { //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
		if (v instanceof String) {
			return new Integer((String) v);
		}
		if (v instanceof Integer) {
			return (Integer) v;
		}
		if (v instanceof Number)
			return ((Number)v).intValue();
		throw new ClassCastException(NLS.bind(Messages.cannot_convert_to_int, v.getClass()));
	}

	@Override
	public UIField generateDefaultField() {
		return new UIFieldImpl(CadseGCST.DTEXT, UUID.randomUUID(), this, getDisplayName(), EPosLabel.defaultpos, 
				new MC_Descriptor(CadseGCST.MC_INTEGER), null);
	}
}
