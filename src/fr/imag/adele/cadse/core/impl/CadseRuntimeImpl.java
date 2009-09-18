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
import fr.imag.adele.cadse.core.CadseRootCST;
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
import fr.imag.adele.cadse.core.util.ArraysUtil;

public class CadseRuntimeImpl extends AbstractGeneratedItem implements CadseRuntime, InternalCadseRuntime {
	private String			_displayName;
	private ItemType[]		_itemTypes;
	private CadseRuntime[]	_requiredCadses;
	private TreeView		_views;
	private CompactUUID		_idCadseDefintiion;
	private String			_name;
	private String			_description;
	private String			_repoLogin;
	private URL				_item_repos_url;
	private URL				_defaultContentRepoURL;

	boolean					_executed	= false;
	boolean					_cadseroot	= false;

	private String[]		_errors;

	public CadseRuntimeImpl(String name, CompactUUID runtimeId, CompactUUID definitionId) {
		super(runtimeId);
		_idCadseDefintiion = definitionId;
		this._name = name;
		this._displayName = "Cadse " + name;
	}

	public ItemType getType() {
		return CadseRootCST.CADSE_RUNTIME;
	}

	public void setParent(Item parent, LinkType lt) {
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public String getDisplayName() {
		return _displayName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.CadseRuntime#addItemType(fr.imag.adele.cadse.core.internal.ItemTypeImpl)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.InternalCadseRuntime#addItemType(fr.imag.adele.cadse.core.ItemType)
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
		if (linkType == CadseRootCST.CADSE_RUNTIME_lt_ITEM_TYPES) {
			ret.addOutgoing(CadseRootCST.CADSE_RUNTIME_lt_ITEM_TYPES, _itemTypes);
		}
		if (linkType == CadseRootCST.CADSE_RUNTIME_lt_EXTENDS) {
			ret.addOutgoing(CadseRootCST.CADSE_RUNTIME_lt_EXTENDS, _requiredCadses);
		}
		super.collectOutgoingLinks(linkType, ret);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.InternalCadseRuntime#setExtendsCadse(fr.imag.adele.cadse.core.CadseRuntimeImpl[])
	 */
	public void setRequiredCadses(CadseRuntime[] extendsCadse) {

		this._requiredCadses = extendsCadse;
		// add incomings links manuellement ...
		// Ne pas apeler cette methode à l'interieur de la methode commit...
		// sinon les liens incomings vont etre creer deux fois...
		if (extendsCadse != null) {
			if (CadseRootCST.CADSE_RUNTIME_lt_EXTENDS == null) {
				throw new CadseIllegalArgumentException("Cadse root is not loaded");
			}
			for (CadseRuntime c : extendsCadse) {
				c.addIncomingLink(new ReflectLink(CadseRootCST.CADSE_RUNTIME_lt_EXTENDS, this, c, -1), false);
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
		if (CadseRootCST.ITEM_TYPE_at_NAME_ == type) {
			return (T) _name;
		}
		if (CadseRootCST.CADSE_RUNTIME_at_DESCRIPTION_ == type) {
			return (T) _description;
		}
		if (CadseRootCST.CADSE_RUNTIME_at_ITEM_REPO_LOGIN_ == type) {
			return (T) _repoLogin;
		}
		if (CadseRootCST.CADSE_RUNTIME_at_ITEM_REPO_URL_ == type) {
			return (T) _item_repos_url;
		}
		if (CadseRootCST.CADSE_RUNTIME_at_DEFAULT_CONTENT_REPO_URL_ == type) {
			return (T) _defaultContentRepoURL;
		}
		if (CadseRootCST.CADSE_RUNTIME_at_EXCUTED_ == type) {
			return (T) Boolean.valueOf(_executed);
		}
		if (type == CadseRootCST.ITEM_TYPE_at_DISPLAY_NAME_) {
			return (T) getDisplayName();
		}

		return super.internalGetOwnerAttribute(type);
	}

	@Override
	public boolean commitSetAttribute(IAttributeType<?> type, String key, Object value) {

		// TODO
		if (CadseRootCST.CADSE_RUNTIME_at_ITEM_REPO_LOGIN_ == type) {
			return false;
		}
		if (CadseRootCST.CADSE_RUNTIME_at_ITEM_REPO_URL_ == type) {
			return false;
		}
		if (CadseRootCST.CADSE_RUNTIME_at_DEFAULT_CONTENT_REPO_URL_ == type) {
			return false;
		}
		return super.commitSetAttribute(type, key, value);
	}

	@Override
	public Link commitLoadCreateLink(LinkType lt, Item destination) throws CadseException {
		if (lt == CadseRootCST.CADSE_RUNTIME_lt_ITEM_TYPES) {
			this._itemTypes = ArraysUtil.add(ItemType.class, this._itemTypes, (ItemType) destination);
			return new ReflectLink(lt, this, destination, this._itemTypes.length - 1);
		}
		if (lt == CadseRootCST.CADSE_RUNTIME_lt_EXTENDS) {
			this._requiredCadses = ArraysUtil.add(CadseRuntime.class, this._requiredCadses, (CadseRuntime) destination);
			return new ReflectLink(lt, this, destination, this._requiredCadses.length - 1);
		}
		return super.commitLoadCreateLink(lt, destination);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.InternalCadseRuntime#setDescription(java.lang.String)
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
	 * @see fr.imag.adele.cadse.core.CadseRuntime#thisExtendsThat(fr.imag.adele.cadse.core.CadseRuntime)
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.InternalCadseRuntime#setDisplayName(java.lang.String)
	 */
	public void setDisplayName(String displayName) {
		if (displayName == null) {
			return;
		}
		this._displayName = displayName;
	}

}
