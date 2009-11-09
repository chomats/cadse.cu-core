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

import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.attribute.CheckStatus;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.delta.ItemDelta;
import fr.imag.adele.cadse.core.ui.UIPlatform;
import fr.imag.adele.cadse.core.util.Convert;

/**
 * The Class DoubleAttributeType.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class DoubleAttributeType extends AttributeType implements
		fr.imag.adele.cadse.core.attribute.DoubleAttributeType {

	/** The value. */
	private double	value;

	private Double	minValue;
	private Double	maxValue;

	/**
	 * Instantiates a new double attribute type.
	 * 
	 * @param id
	 *            the id
	 * @param min
	 *            the min
	 * @param value
	 *            the value
	 */
	public DoubleAttributeType(CompactUUID id, int flag, String name, Double minValue, Double maxValue, String value) {
		super(id, name, flag);
		this.value = Convert.toDouble(value, Double.NaN);
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	public DoubleAttributeType(ItemDelta item) {
		super(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IAttributeType#getDefaultValue()
	 */
	@Override
	public Double getDefaultValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IAttributeType#getAttributeType()
	 */
	public Class<Double> getAttributeType() {
		return Double.class;
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

	public void setValue(Double value) {
		this.value = value;
	}

	public Double getMinValue() {
		return minValue;
	}

	public void setMinValue(Double minValue) {
		this.minValue = minValue;
	}

	public Double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(Double maxValue) {
		this.maxValue = maxValue;
	}

	public double getValue() {
		return value;
	}

	@Override
	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {
		if (CadseGCST.ATTRIBUTE_at_DEFAULT_VALUE_ == type) {
			return (T) new Double(value).toString();
		}
//		if (CadseGCST.DOUBLE_at_MIN_ == type) {
//			return (T) minValue;
//		}
//		if (CadseGCST.DOUBLE_at_MAX_ == type) {
//			return (T) maxValue;
//		}
		return super.internalGetOwnerAttribute(type);
	}

	@Override
	public boolean commitSetAttribute(IAttributeType<?> type, Object value) {
		if (CadseGCST.ATTRIBUTE_at_DEFAULT_VALUE_ == type) {
			setValue(Convert.toDouble(value, 0));
			return true;
		}
//		if (CadseGCST.DOUBLE_at_MIN_ == type) {
//			setMinValue(Convert.toDouble(value));
//			return true;
//		}
//		if (CadseGCST.DOUBLE_at_MAX_ == type) {
//			setMaxValue(Convert.toDouble(value));
//			return true;
//		}
		return super.commitSetAttribute(type, value);
	}

	public ItemType getType() {
		return CadseGCST.DOUBLE;
	}

	@Override
	public CheckStatus check(Item item, Object value) {
		CheckStatus ret = super.check(item, value);
		if (ret != null) {
			return ret;
		}

		if (!getFlag(CAN_BE_UNDEFINED) && value == null) {
			return new CheckStatus(UIPlatform.ERROR, Messages.cannot_be_undefined);
		}
		if (value == null) {
			return null;
		}
		if (value instanceof String) {
			try {
				value = Double.parseDouble((String) value);
			} catch (NumberFormatException e) {
				return new CheckStatus(UIPlatform.ERROR, e.getMessage());
			}
		}
		if (!(value instanceof Double)) {
			return new CheckStatus(UIPlatform.ERROR, Messages.must_be_a_double);
		}
		double v = ((Double) value).doubleValue();
		if (minValue != null) {
			if (v < minValue.doubleValue()) {
				return new CheckStatus(UIPlatform.ERROR, Messages.value_must_be_upper , minValue);
			}
		}
		if (maxValue != null) {
			if (v > maxValue.doubleValue()) {
				return new CheckStatus(UIPlatform.ERROR, Messages.value_must_be_lower , maxValue);
			}
		}

		return null;
	}

	@Override
	public Object convertTo(Object v) {
		if (v instanceof String) {
			return new Double((String) v);
		}
		return v;
	}

}
