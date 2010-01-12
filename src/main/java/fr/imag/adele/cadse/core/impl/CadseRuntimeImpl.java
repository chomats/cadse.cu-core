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
package fr.imag.adele.cadse.core.impl;

import java.net.URL;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.CadseRuntime;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.internal.AbstractGeneratedItem;
import fr.imag.adele.cadse.core.impl.ui.view.TreeView;
import fr.imag.adele.cadse.core.internal.InternalCadseRuntime;
import fr.imag.adele.cadse.core.ui.view.DefineNewContext;
import fr.imag.adele.cadse.core.util.ArraysUtil;
import fr.imag.adele.cadse.core.util.Convert;

public class CadseRuntimeImpl extends AbstractGeneratedItem implements CadseRuntime, InternalCadseRuntime {
	protected String		_cadseName;
	private ItemType[]		_itemTypes;
	private CadseRuntime[]	_requiredCadses;
	private TreeView		_views;
	private CompactUUID		_idCadseDefinition;
	protected String		_name;
	protected String		_qname;
	protected String		_description;
	private String			_repoLogin;
	private URL				_item_repos_url;
	private URL				_defaultContentRepoURL;

	boolean					_executed	= false;
	boolean					_cadseroot	= false;

	private String[]		_errors;
	private String			_cstClassName;
	DefineNewContext[]		_defineNewContext;

	public CadseRuntimeImpl(String name, CompactUUID runtimeId, CompactUUID definitionId) {
		super(runtimeId);
		_idCadseDefinition = definitionId;
		internalSetName(name);
		this._cadseName = "Cadse " + name;
	}

	private void internalSetName(String name) {
		if (name.startsWith(CADSE_NAME_SUFFIX)) {
			this._name = name.substring(CADSE_NAME_SUFFIX.length());
			this._qname = name;
		} else if (name.contains(".")) {
			this._name = name;
			this._qname = name;
		} else {
			this._name = name;
			this._qname = CADSE_NAME_SUFFIX + name;
		}
	}

	public ItemType getType() {
		return CadseGCST.CADSE;
	}

	public void setParent(Item parent, LinkType lt) {
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public String getQualifiedName() {
		return _qname;
	}

	@Override
	public String getDisplayName() {
		return _cadseName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.CadseRuntime#addItemType(fr.imag.adele.cadse
	 * .core.internal.ItemTypeImpl)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.InternalCadseRuntime#addItemType(fr.imag.adele
	 * .cadse.core.ItemType)
	 */
	public void addItemType(ItemType it) {
		_itemTypes = ArraysUtil.add(ItemType.class, _itemTypes, it);
	}

	@Override
	public String toString() {
		return getDisplayName();
	}

	@Override
	protected void collectOutgoingLinks(LinkType linkType, CollectedReflectLink ret) {
		if (linkType == CadseGCST.CADSE_lt_ITEM_TYPES) {
			ret.addOutgoing(CadseGCST.CADSE_lt_ITEM_TYPES, _itemTypes);
		}
		if (linkType == CadseGCST.CADSE_lt_EXTENDS) {
			ret.addOutgoing(CadseGCST.CADSE_lt_EXTENDS, _requiredCadses);
		}
		super.collectOutgoingLinks(linkType, ret);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.InternalCadseRuntime#setExtendsCadse(fr.imag
	 * .adele.cadse.core.CadseRuntimeImpl[])
	 */
	public void setRequiredCadses(CadseRuntime[] extendsCadse) {

		this._requiredCadses = extendsCadse;
		// add incomings links manuellement ...
		// Ne pas apeler cette methode à l'interieur de la methode commit...
		// sinon les liens incomings vont etre creer deux fois...
		if (extendsCadse != null) {
			if (CadseGCST.CADSE_lt_EXTENDS == null) {
				throw new CadseIllegalArgumentException("Cadse root is not loaded");
			}
			for (CadseRuntime c : extendsCadse) {
				c.addIncomingLink(new ReflectLink(CadseGCST.CADSE_lt_EXTENDS, this, c, -1), false);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.CadseRuntime#getNeededCadses()
	 */
	public CadseRuntime[] getRequiredCadses() {
		return _requiredCadses;
	}

	@Override
	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {

		if (CadseGCST.CADSE_at_ID_DEFINITION_ == type) {
			return (T) _idCadseDefinition.toString();
		}
		if (CadseGCST.ITEM_at_NAME_ == type) {
			return (T) _name;
		}
		if (CadseGCST.ITEM_at_QUALIFIED_NAME_ == type) {
			return (T) _qname;
		}
		if (CadseGCST.CADSE_at_DESCRIPTION_ == type) {
			return (T) _description;
		}
		if (CadseGCST.CADSE_at_ITEM_REPO_LOGIN_ == type) {
			return (T) _repoLogin;
		}
		if (CadseGCST.CADSE_at_ITEM_REPO_URL_ == type) {
			return (T) _item_repos_url;
		}
		if (CadseGCST.CADSE_at_DEFAULT_CONTENT_REPO_URL_ == type) {
			return (T) _defaultContentRepoURL;
		}
		if (CadseGCST.CADSE_at_EXECUTED_ == type) {
			return (T) Boolean.valueOf(_executed);
		}
		if (type == CadseGCST.ITEM_at_DISPLAY_NAME_) {
			return (T) getDisplayName();
		}

		return super.internalGetOwnerAttribute(type);
	}

	@Override
	public String getQualifiedName(boolean recompute) {
		return getQualifiedName();
	}

	@Override
	public boolean commitSetAttribute(IAttributeType<?> type, Object value) {
		if (CadseGCST.ITEM_at_NAME_ == type) {
			_name = Convert.toString(value);
			return true;
		}

		if (CadseGCST.ITEM_at_QUALIFIED_NAME_ == type) {
			_qname = Convert.toString(value);
			return true;
		}
		// TODO
		if (CadseGCST.CADSE_at_ITEM_REPO_LOGIN_ == type) {
			return false;
		}
		if (CadseGCST.CADSE_at_ITEM_REPO_URL_ == type) {
			return false;
		}
		if (CadseGCST.CADSE_at_DEFAULT_CONTENT_REPO_URL_ == type) {
			return false;
		}
		return super.commitSetAttribute(type, value);
	}

	@Override
	public Link commitLoadCreateLink(LinkType lt, Item destination) throws CadseException {
		if (lt == CadseGCST.CADSE_lt_ITEM_TYPES) {
			this._itemTypes = ArraysUtil.add(ItemType.class, this._itemTypes, (ItemType) destination);
			return new ReflectLink(lt, this, destination, this._itemTypes.length - 1);
		}
		if (lt == CadseGCST.CADSE_lt_EXTENDS) {
			this._requiredCadses = ArraysUtil.add(CadseRuntime.class, this._requiredCadses, (CadseRuntime) destination);
			return new ReflectLink(lt, this, destination, this._requiredCadses.length - 1);
		}
		return super.commitLoadCreateLink(lt, destination);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.InternalCadseRuntime#setDescription(java.lang
	 * .String)
	 */
	public void setDescription(String description) {
		this._description = description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.CadseRuntime#addError(java.lang.String)
	 */
	public void addError(String msg) {
		this._errors = ArraysUtil.add(String.class, this._errors, msg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.CadseRuntime#getErrors()
	 */
	public String[] getErrors() {
		return _errors;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.CadseRuntime#isExecuted()
	 */
	public boolean isExecuted() {
		return _executed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.InternalCadseRuntime#setExecuted(boolean)
	 */
	public void setExecuted(boolean executed) {
		this._executed = executed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.CadseRuntime#isCadseroot()
	 */
	public boolean isCadseRoot() {
		return _cadseroot;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.InternalCadseRuntime#setCadseroot(boolean)
	 */
	public void setCadseroot(boolean cadseroot) {
		this._cadseroot = cadseroot;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.CadseRuntime#thisExtendsThat(fr.imag.adele.cadse
	 * .core.CadseRuntime)
	 */
	public boolean isRequired(CadseRuntime cr) {
		if (this._requiredCadses == null) {
			return false; // cr n'est pas etendu par this
		}
		for (CadseRuntime eCr : this._requiredCadses) {
			if (eCr == cr) {
				return true; // this extends eCr
			}
		}
		for (CadseRuntime eCr : this._requiredCadses) {
			if (eCr.isRequired(cr)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean canBeExecuted() {
		if (!isResolved())
			return false;
		if (_requiredCadses == null)
			return true;
		for (CadseRuntime eCr : this._requiredCadses) {
			if (!eCr.isResolved()) {
				return false; // this extends eCr
			}
		}
		for (CadseRuntime eCr : this._requiredCadses) {
			if (!eCr.canBeExecuted()) {
				return false; // this extends eCr
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.InternalCadseRuntime#setDisplayName(java.lang
	 * .String)
	 */
	public void setDisplayName(String displayName) {
		if (displayName == null) {
			return;
		}
		this._cadseName = displayName;
	}

	public CompactUUID getIdCadseDefinition() {
		return _idCadseDefinition;
	}

	public void setIdCadseDefinition(CompactUUID idCadseDefintiion) {
		_idCadseDefinition = idCadseDefintiion;
	}

	@Override
	public String getCstQualifiedClassName() {
		return _cstClassName;
	}

	@Override
	public void setCstQualifiedClassName(String cstClass) {
		_cstClassName = cstClass;
	}

	@Override
	public String getCSTName() {
		int index = _cstClassName.lastIndexOf('.');
		if (index == -1)
			return _cstClassName;
		return _cstClassName.substring(index + 1);
	}

	@Override
	public void addDefineNewContext(DefineNewContext d) {
		_defineNewContext = ArraysUtil.add(DefineNewContext.class, _defineNewContext, d);
	}

	@Override
	public DefineNewContext[] getDefineNewContexts() {
		return _defineNewContext;
	}

	@Override
	public void removeDefineNewContext(DefineNewContext d) {
		_defineNewContext = ArraysUtil.remove(DefineNewContext.class, _defineNewContext, d);
	}

}
