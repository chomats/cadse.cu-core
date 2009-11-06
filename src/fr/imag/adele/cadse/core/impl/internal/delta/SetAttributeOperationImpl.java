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
package fr.imag.adele.cadse.core.impl.internal.delta;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.delta.ItemOrLinkDelta;
import fr.imag.adele.cadse.core.delta.OperationTypeCst;
import fr.imag.adele.cadse.core.delta.SetAttributeOperation;
import fr.imag.adele.cadse.core.delta.WLWCOperationImpl;
import fr.imag.adele.cadse.core.internal.delta.InternalSetAttributeOperation;

public final class SetAttributeOperationImpl extends WLWCOperationImpl implements SetAttributeOperation,
		InternalSetAttributeOperation {

	private final IAttributeType<?>	_attribute;
	private final String	_attributeName;
	private Object			_currentValue;
	private final Object	_oldValue;
	private Object			_precValue;

	
	public SetAttributeOperationImpl(ItemOrLinkDelta parent, String key, Object value, Object oldValue)
	throws CadseException {
		this(parent, key, value, oldValue, true);
	}
	public SetAttributeOperationImpl(ItemOrLinkDelta parent, String key, Object value, Object oldValue, boolean add)
			throws CadseException {
		super(OperationTypeCst.SET_ATTRIBUTE_OPERATION, parent);
		this._attributeName = key;
		this._currentValue = value;
		this._precValue = this._oldValue = oldValue;
		_attribute = null;
		if (add) addInParent();
	}
	
	public SetAttributeOperationImpl(ItemOrLinkDelta parent, IAttributeType<?> key, Object value, Object oldValue)
	throws CadseException {
		this(parent, key, value, oldValue, true);
	}
	public SetAttributeOperationImpl(ItemOrLinkDelta parent, IAttributeType<?> key, Object value, Object oldValue, boolean add)
			throws CadseException {
		super(OperationTypeCst.SET_ATTRIBUTE_OPERATION, parent);
		this._attributeName = key.getName();
		this._attribute = key;
		this._currentValue = value;
		this._precValue = this._oldValue = oldValue;
		if (add) addInParent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.SetAttributeOperation2#getAttributeName()
	 */
	public String getAttributeName() {
		return _attributeName;
	}

	/**
	 * Return current value of attribute. Null it the value is deleted
	 * (undefined)
	 * 
	 * @return current value of attribute.
	 * @deprecated use {@link #getCurrentValue()}.
	 */
	@Deprecated
	public Object getValue() {
		return _currentValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.SetAttributeOperation2#getCurrentValue()
	 */
	public Object getCurrentValue() {
		return _currentValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.SetAttributeOperation2#getOldValue()
	 */
	public Object getOldValue() {
		return _oldValue;
	}

	@Override
	public ItemOrLinkDeltaImpl getParent() {
		return (ItemOrLinkDeltaImpl) super.getParent();
	}

	public IAttributeType<?> getType() {
		return getParent().getAttributeType(this);
	}

	@Override
	public String toString() {
		return _attributeName + "=" + _currentValue + " (prec value:" + _precValue + ", old value:" + _oldValue + " )";
	}

	@Override
	public boolean isModified() {
		if (_oldValue == null && _currentValue == null) {
			return false;
		}
		if (_oldValue != null && _currentValue != null && _oldValue.equals(_currentValue)) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.SetAttributeOperation2#isAdded()
	 */
	public boolean isAdded() {
		return _oldValue == null && _currentValue != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.SetAttributeOperation2#isChanged()
	 */
	public boolean isChanged() {
		return _oldValue != null && _currentValue != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.SetAttributeOperation2#isRemoved()
	 */
	public boolean isRemoved() {
		return _oldValue != null && _currentValue == null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.SetAttributeOperation2#getAttributeDefinition()
	 */
	public IAttributeType<?> getAttributeDefinition() {
		if (_attribute != null)
			return _attribute;
		return getType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.InternalSetAttributeOperation#setCurrentValue(java.lang.Object)
	 */
	public void setCurrentValue(Object newValue) {
		changeTimeStamp();
		this._currentValue = newValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.InternalSetAttributeOperation#setPrecCurrentValue(java.lang.Object)
	 */
	public void setPrecCurrentValue(Object currentValue) {
		this._precValue = currentValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.SetAttributeOperation2#getPrecCurrentValue()
	 */
	public Object getPrecCurrentValue() {
		return _precValue;
	}
}
