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

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseGCST;
import java.util.UUID;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemState;
import fr.imag.adele.cadse.core.TypeDefinition;
import fr.imag.adele.cadse.core.attribute.CheckStatus;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.enumdef.TWCommitKind;
import fr.imag.adele.cadse.core.enumdef.TWEvol;
import fr.imag.adele.cadse.core.enumdef.TWUpdateKind;
import fr.imag.adele.cadse.core.impl.db.DBLogicalWorkspace;
import fr.imag.adele.cadse.core.impl.internal.AbstractGeneratedItem;
import fr.imag.adele.cadse.core.internal.attribute.IInternalTWAttribute;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.ui.UIPlatform;
import fr.imag.adele.cadse.core.util.Convert;
import fr.imag.adele.teamwork.db.ModelVersionDBException;

public abstract class AttributeType extends AbstractGeneratedItem implements IInternalTWAttribute, Comparable<AttributeType> {

	private String			_name;
	String					_cstName;
	private TWEvol			_evol			= TWEvol.twImmutable;
	private boolean			_TWRevSpecific	= true;
	private TWCommitKind	_TWCommitKind	= TWCommitKind.conflict;
	private TWUpdateKind	_TWUpdateKind	= TWUpdateKind.merge;

        public AttributeType() {
        }


	public AttributeType(UUID id, String name, int flag) {
		super(id, flag);
		this._name = name;
		if ((flag & DEFAULT_FLAG) != 0) {
			setDefaultFlag();
		}
	}

	protected void setDefaultFlag() {
		setFlag(DEFAULT_FLAG, false);
		setFlag(CAN_BE_UNDEFINED, true);
	}

	public AttributeType(ItemDelta item) {
		super(item);
		_name = item.getName();
	}

	@Override
	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {
		if (CadseGCST.ATTRIBUTE_at_TWEVOL_ == type) {
			return (T) _evol;
		}
		if (CadseGCST.ATTRIBUTE_at_CANNOT_BE_UNDEFINED_ == type) {
			return (T) Boolean.valueOf(!getFlag(CAN_BE_UNDEFINED));
		}
		if (CadseGCST.ATTRIBUTE_at_MUST_BE_INITIALIZED_ == type) {
			return (T) Boolean.valueOf(getFlag(MUST_BE_INITIALIZED_AT_CREATION_TIME));
		}
		/*
		 * if (CadseGCST.ATTRIBUTE_at_IS_META_ATTRIBUTE_ == type) { return (T)
		 * Boolean.valueOf(getFlag(IS_META_ATTRIBUTE)); }
		 */
		/*
		 * if (CadseGCST.ATTRIBUTE_at_OVERWRITABLE_ == type) { return (T)
		 * Boolean.valueOf(getFlag(OVERWRITABLE)); }
		 */
		if (CadseGCST.ITEM_at_NAME_ == type) {
			return (T) _name;
		}
		if (CadseGCST.ITEM_at_DISPLAY_NAME_ == type) {
			return (T) _name;
		}
		/*
		 * if (CadseGCST.ITEM_at_QUALIFIED_DISPLAY_NAME_ == type) { return (T)
		 * _name; }
		 */
		if (CadseGCST.ITEM_at_QUALIFIED_NAME_ == type) {
			return (T) _name;
		}

		if (CadseGCST.ATTRIBUTE_at_TWREV_SPECIFIC_ == type) {
			return (T) Boolean.valueOf(_TWRevSpecific);
		}
		if (CadseGCST.ATTRIBUTE_at_TWCOMMIT_KIND_ == type) {
			return (T) _TWCommitKind;
		}
		if (CadseGCST.ATTRIBUTE_at_TWUPDATE_KIND_ == type) {
			return (T) _TWUpdateKind;
		}
		return super.internalGetOwnerAttribute(type);
	}

	@Override
	public boolean commitSetAttribute(IAttributeType<?> type, Object value) {
		if (CadseGCST.ATTRIBUTE_at_CANNOT_BE_UNDEFINED_ == type) {
			return setFlag(CAN_BE_UNDEFINED, !Convert.toBoolean(value));
		}

		if (CadseGCST.ATTRIBUTE_at_MUST_BE_INITIALIZED_ == type) {
			return setFlag(MUST_BE_INITIALIZED_AT_CREATION_TIME, Convert.toBoolean(value, false));
		}
		/*
		 * if (CadseGCST.ATTRIBUTE_at_IS_META_ATTRIBUTE_ == type) { return
		 * setFlag(IS_META_ATTRIBUTE, Convert.toBoolean(value)); } if
		 * (CadseGCST.ATTRIBUTE_at_OVERWRITABLE_ == type) { return
		 * setFlag(OVERWRITABLE, Convert.toBoolean(value, false)); }
		 */
		if (CadseGCST.ITEM_at_NAME_ == type) {
			_name = Convert.toString(value);
			return true;
		}
		if (CadseGCST.ATTRIBUTE_at_TWEVOL_ == type) {
			_evol = CadseGCST.ATTRIBUTE_at_TWEVOL_.convertTo(value);
			if (_evol == null) {
				_evol = TWEvol.twImmutable;
			}
			return true;
		}
		if (CadseGCST.ATTRIBUTE_at_TWREV_SPECIFIC_ == type) {
			_TWRevSpecific = Convert.toBoolean(value, true);
			return true;
		}
		if (CadseGCST.ATTRIBUTE_at_TWCOMMIT_KIND_ == type) {
			_TWCommitKind = CadseGCST.ATTRIBUTE_at_TWCOMMIT_KIND_.convertTo(value);
			if (_TWCommitKind == null) {
				_TWCommitKind = TWCommitKind.conflict;
			}
			return true;
		}
		if (CadseGCST.ATTRIBUTE_at_TWUPDATE_KIND_ == type) {
			_TWUpdateKind = CadseGCST.ATTRIBUTE_at_TWUPDATE_KIND_.convertTo(value);
			if (_TWUpdateKind == null) {
				_TWUpdateKind = TWUpdateKind.merge;
			}
			return true;
		}

		return super.commitSetAttribute(type, value);
	}

	@Override
	public Item getPartParent(boolean attemptToRecreate) {
		return _parent;
	}

	@Override
	public String getQualifiedName() {
		return NO_VALUE_STRING;
	}

	@Override
	public String getDisplayName() {
		return _name;
	}

	@Override
	public String getQualifiedDisplayName() {
		// TODO getType.get..+"::"+name
		return _name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IAttributeType#getMax()
	 */
	public int getMax() {
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IAttributeType#getMin()
	 */
	public int getMin() {
		return getFlag(MUST_BE_INITIALIZED_AT_CREATION_TIME) ? 1 : 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IAttributeType#isMandatory()
	 */
	public boolean mustBeInitializedAtCreationTime() {
		return getFlag(MUST_BE_INITIALIZED_AT_CREATION_TIME);
	}

	public boolean isNatif() {
		return getFlag(NATIF);
	}

	public boolean setIsNatif(boolean v) {
		return setFlag(NATIF, v);
	}

	public boolean isTransient() {
		return getFlag(TRANSIENT);
	}

	public boolean setIsTransient(boolean v) {
		return setFlag(TRANSIENT, v);
	}
	
	public boolean isFinal() {
		return getFlag(FINAL);
	}

	public boolean isHiddenInComputedPages() {
		return getFlag(HIDDEN_IN_COMPUTED_PAGES);
	}

	public void setFinal(boolean flag) {
		setFlag(FINAL, flag);
	}

	public void setHiddenInComputedPages(boolean flag) {
		setFlag(HIDDEN_IN_COMPUTED_PAGES, flag);
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.INamed#getShortName()
	 */
	@Override
	public String getName() {
		return _name;
	}

	public Item getParent() {
		return _parent;
	}

	public Object convertTo(Object v) {
		return v;
	}

	public Object getDefaultValue() {
		return null;
	}

	public boolean canBeUndefined() {
		return getFlag(CAN_BE_UNDEFINED);
	}

	public boolean cannotBeUndefined() {
		return !getFlag(CAN_BE_UNDEFINED);
	}

	public CheckStatus check(Item item, Object value) {
		if (!getFlag(CAN_BE_UNDEFINED) && value == IAttributeType.NULL) {
			return new CheckStatus(UIPlatform.ERROR, Messages.cannot_be_undefined);
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.attribute.IInternalTWAttribute#setEvol(fr.imag
	 * .adele.cadse.core.enumdef.TWEvol)
	 */
	public void setEvol(TWEvol evol) {
		this._evol = evol;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.attribute.IInternalTWAttribute#setTWRevSpecific
	 * (boolean)
	 */
	public void setTWRevSpecific(boolean revSpecific) {
		_TWRevSpecific = revSpecific;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.attribute.IInternalTWAttribute#setTWCommitKind
	 * (fr.imag.adele.cadse.core.enumdef.TWCommitKind)
	 */
	public void setTWCommitKind(TWCommitKind commitKind) {
		_TWCommitKind = commitKind;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.attribute.IInternalTWAttribute#setTWUpdateKind
	 * (fr.imag.adele.cadse.core.enumdef.TWUpdateKind)
	 */
	public void setTWUpdateKind(TWUpdateKind updateKind) {
		_TWUpdateKind = updateKind;
	}

	public boolean isTWValueModified(Object oldValue, Object newValue) {
		return !Convert.equals(oldValue, newValue);
	}

	public boolean isValueModified(Object oldValue, Object newValue) {
		return !Convert.equals(oldValue, newValue);
	}

	public boolean isTWRevSpecific() {
		return _TWRevSpecific;
	}

	public boolean isPersistentCache() {
		return getFlag(Item.PERSISTENCE_CACHE);
	}

	public Object createNewValueFor(Item anItem) throws CadseException {
		return null;
	}

	public boolean mustBeCreateNewValueAtCreationTimeOfItem() {
		return false;
	}

	public String getCSTName() {
		return _cstName;
	}

	public void setCSTName(String cst) {
		_cstName = cst;
	}

	public IAttributeType<?>[] getChildren() {
		return null;
	}
	
	public UIField generateDefaultField() {
		return null;
	}
	
	public TypeDefinition getSource() {
		if (_parent instanceof TypeDefinition)
			return (TypeDefinition) _parent;
		return null;
	}


	@Override
	public int compareTo(AttributeType o) {
		return getName().compareTo(o.getName());
	}
	

	public boolean isAttributeHead() {
		return getFlag(ATTRIBUTE_HEAD);
	}
}
