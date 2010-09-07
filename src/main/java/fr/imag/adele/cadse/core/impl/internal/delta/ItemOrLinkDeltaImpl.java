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
package fr.imag.adele.cadse.core.impl.internal.delta;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.internal.delta.InternalItemOrLinkDelta;
import fr.imag.adele.cadse.core.transaction.delta.CreateOperation;
import fr.imag.adele.cadse.core.transaction.delta.DeleteOperation;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.transaction.delta.ItemOrLinkDelta;
import fr.imag.adele.cadse.core.transaction.delta.OperationType;
import fr.imag.adele.cadse.core.transaction.delta.SetAttributeOperation;
import fr.imag.adele.cadse.core.transaction.delta.WLWCOperationImpl;
import fr.imag.adele.cadse.util.Assert;

public abstract class ItemOrLinkDeltaImpl extends WLWCOperationImpl implements ItemOrLinkDelta, InternalItemOrLinkDelta {

	CreateOperation						_createOperation	= null;
	DeleteOperation						_deleteOperation	= null;
	Map<IAttributeType<?>, SetAttributeOperation>	_attributes			= null;

	public ItemOrLinkDeltaImpl(OperationType type, WLWCOperationImpl parent) {
		super(type, parent);
	}

	@Override
	public void removeInParent() {
		if (_createOperation != null) {
			_createOperation.removeInParent();
		}
		if (_deleteOperation != null) {
			((DeleteOperationImpl) _deleteOperation).removeInParent();
		}
		if (_attributes != null) {
			for (SetAttributeOperation a : _attributes.values()) {
				a.removeInParent();
			}
		}
		_createOperation = null;
		_deleteOperation = null;
		_attributes = null;
		super.removeInParent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.InternalAttributeOperation#setCreateOperation(fr.imag.adele.cadse.core.internal.delta.CreateOperationImpl)
	 */
	public void setCreateOperation(CreateOperation createItemOperation) {
		Assert.isNotNull(createItemOperation);
		Assert.isTrue(_deleteOperation == null);
		Assert.isTrue(_createOperation == null);
		this._createOperation = createItemOperation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.InternalAttributeOperation#setDeleteOperation(fr.imag.adele.cadse.core.delta.DeleteOperation)
	 */
	public void setDeleteOperation(DeleteOperation deleteItemOperation) {
		if (deleteItemOperation == null) {
			this._deleteOperation = null;
			return;
		}
		Assert.isTrue(_deleteOperation == null);
		Assert.isTrue(_createOperation == null);
		this._deleteOperation = deleteItemOperation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.InternalAttributeOperation#add(fr.imag.adele.cadse.core.delta.SetAttributeOperation)
	 */
	public void add(SetAttributeOperation setAttributeOperation) {
		getWorkingCopy().check_write();

		if (this._attributes == null) {
			this._attributes = new HashMap<IAttributeType<?>, SetAttributeOperation>();
		}

		SetAttributeOperation old = _attributes.get(setAttributeOperation.getAttributeName());
		if (old != null) {
			old.removeInParent();
		}

		this._attributes.put(setAttributeOperation.getAttributeDefinition(), setAttributeOperation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.InternalAttributeOperation#add(fr.imag.adele.cadse.core.delta.SetAttributeOperation,
	 *      boolean)
	 */
	public void add(SetAttributeOperation setAttributeOperation, boolean check) {
		if (check) {
			getWorkingCopy().check_write();
		}

		if (this._attributes == null) {
			this._attributes = new HashMap<IAttributeType<?>, SetAttributeOperation>();
		}

		SetAttributeOperation old = _attributes.get(setAttributeOperation.getAttributeDefinition());
		if (old != null) {
			old.removeInParent();
		}

		this._attributes.put(setAttributeOperation.getAttributeDefinition(), setAttributeOperation);
	}

	public Collection<SetAttributeOperation> getSetAttributeOperation() {
		if (this._attributes == null) {
			return Collections.emptyList();
		}
		return _attributes.values();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.IAttributeOperation#getCreateOperation()
	 */
	public CreateOperation getCreateOperation() {
		return _createOperation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.IAttributeOperation#getDeleteOperation()
	 */
	public DeleteOperation getDeleteOperation() {
		return _deleteOperation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.IAttributeOperation#isModified()
	 */
	@Override
	public boolean isModified() {
		if (getDeleteOperation() != null) {
			return true;
		}
		if (getCreateOperation() != null) {
			return true;
		}
		if (_attributes != null) {
			for (SetAttributeOperation k : _attributes.values()) {
				if (k.isModified()) {
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.IAttributeOperation#isAdded()
	 */
	public boolean isAdded() {
		return getCreateOperation() != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.IAttributeOperation#isDeleted()
	 */
	public boolean isDeleted() {
		return getDeleteOperation() != null;
	}

	public <T> T getAttribute(IAttributeType<T> key) {
		return getAttribute(key, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.InternalAttributeOperation#getAttribute(java.lang.String)
	 */
	public <T> T getAttribute(IAttributeType<T> key, boolean returnDefault) {
		if (this._attributes != null) {
			SetAttributeOperation oa = this._attributes.get(key);
			if (oa != null) {
				return (T) oa.getCurrentValue();
			}
		}
		if (returnDefault)
			return key.getDefaultValue();
		return null;
	}
	
	@Override
	public <T> T getAttributeWithDefaultValue(IAttributeType<T> att,
			T defaultValue) {
		T v = getAttribute(att,false);
		if (v == null)
			v = defaultValue;
		
		return v;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.InternalAttributeOperation#getSetAttributeOperation(java.lang.String)
	 */
	public SetAttributeOperation getSetAttributeOperation(IAttributeType<?> key) {
		if (this._attributes != null) {
			SetAttributeOperation oa = this._attributes.get(key);
			return oa;
		}
		return null;
	}


	protected void toStringAttributes(StringBuilder sb, String tab) {
		if (_attributes != null) {
			for (IAttributeType<?> k : _attributes.keySet()) {
				SetAttributeOperation v = _attributes.get(k);
				if (!v.isModified()) {
					continue;
				}
				sb.append(tab);
				sb.append(" - ").append(k.getName()).append("=");
				toString(sb, v, tab);
				sb.append("\n");
			}
		}
	}

	protected static void toString(StringBuilder sb, SetAttributeOperation v, String tab) {
		SetAttributeOperation setAtt = v;
		toString(sb, setAtt.getCurrentValue(), tab);
		sb.append("(");
		toString(sb, setAtt.getOldValue(), tab);
		sb.append(")");
	}

	protected static void toString(StringBuilder sb, Object v, String tab) {
		if (v == null) {
			sb.append("null");
			return;
		}
		if (v instanceof ItemDeltaImpl) {
			((ItemDelta) v).toStringShort(sb);
			return;
		}
		if (v instanceof Object[]) {
			sb.append(Arrays.asList((Object[]) v).toString());
			return;
		}
		sb.append(v.toString());
	}

}
