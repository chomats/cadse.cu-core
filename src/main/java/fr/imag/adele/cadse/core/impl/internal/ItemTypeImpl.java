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
/*
 * Adele/LIG/ Grenoble University, France
 * 2006-2008
 */
package fr.imag.adele.cadse.core.impl.internal;

import fr.imag.adele.cadse.core.CadseException;
import java.util.UUID;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.imag.adele.cadse.core.CPackage;
import fr.imag.adele.cadse.core.CadseDomain;
import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.CadseRuntime;
import fr.imag.adele.cadse.core.DerivedLinkType;
import fr.imag.adele.cadse.core.ExtendedType;
import fr.imag.adele.cadse.core.GroupType;
import fr.imag.adele.cadse.core.IItemFactory;
import fr.imag.adele.cadse.core.IItemManager;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemFilter;
import fr.imag.adele.cadse.core.ItemState;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.Messages;
import fr.imag.adele.cadse.core.TypeDefinition;
import fr.imag.adele.cadse.core.attribute.GroupOfAttributes;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.build.Exporter;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.impl.CadseCore;
import fr.imag.adele.cadse.core.CadseIllegalArgumentException;
import fr.imag.adele.cadse.core.impl.CollectedReflectLink;
import fr.imag.adele.cadse.core.impl.ItemFactory;
import fr.imag.adele.cadse.core.impl.ReflectLink;
import fr.imag.adele.cadse.core.internal.IWorkingLoadingItems;
import fr.imag.adele.cadse.core.internal.ItemTypeInternal;
import fr.imag.adele.cadse.core.key.DefaultKeyDefinitionImpl;
import fr.imag.adele.cadse.core.key.KeyDefinition;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransactionListener;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.ui.HierarchicPage;
import fr.imag.adele.cadse.core.ui.IActionContributor;
import fr.imag.adele.cadse.core.ui.IActionPage;
import fr.imag.adele.cadse.core.ui.IPage;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.ui.UIValidator;
import fr.imag.adele.cadse.core.ui.view.FilterContext;
import fr.imag.adele.cadse.core.ui.view.NewContext;
import fr.imag.adele.cadse.core.util.Convert;
import fr.imag.adele.cadse.core.util.IErrorCollector;
import fr.imag.adele.cadse.core.util.LinkPathUtil;
import fr.imag.adele.cadse.util.ArraysUtil;
import fr.imag.adele.cadse.util.Assert;

/**
 * A item type is type of an element in the workspace. Each item type has an id.
 * A item type has two list "outgoings" and "incoming" containing the link types
 * incoming to and going from item type (see LinkType). A item type can be
 * defined as a content, it means all item created by this type are the contents
 * (of others items).
 * 
 * @author Team adle
 * @version 2.2
 * @date 26/09/05
 */
public class ItemTypeImpl extends TypeDefinitionImpl implements ItemType,
		ItemTypeInternal {

	/** The Constant EMPTY_PAGE_FACTORIES. */

	/** The Constant NO_SUB_TYPES. */
	private final static ItemType[] NO_SUB_TYPES = new ItemType[0];

	/** The int id. */
	private int _intId;

	/** The super type. */
	ItemType _superType = null;

	/** The sub types. */
	private List<ItemType> _subTypes;

	/** The has content. */
	private boolean _hasContent; // contenu

	/** The kind. */
	private int _kind;

	/** The item manager. */
	private IItemManager _itemManager = null;

	/** The space keytype. */
	private KeyDefinition _spaceKeytype = null;

	/** The display name. */
	private String _displayName;

	/** The clazz action. */
	private Class<? extends IActionPage> _clazzAction;

	/** The default short name action. */
	protected String _defaultInstanceName;

	

	private IItemFactory _itemFactory;

	/**
	 * implementation of extension ...
	 */
	private ExtendedType[] _extendedBy;

	private String _managerClass;

	public ItemTypeImpl() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.ItemImpl#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		if (_displayName == null) {
			return super.getDisplayName();
		}
		return _displayName;
	}

	/**
	 * Instantiates a new item type impl.
	 * 
	 * @param wl
	 *            the wl
	 * @param superType
	 *            the super type
	 * @param id
	 *            the id
	 * @param intId
	 *            the int id
	 * @param hasContent
	 *            the has content
	 * @param isAbstract
	 *            the is abstract
	 * @param shortname
	 *            the shortname
	 * @param displayName
	 *            the display name
	 */
	protected ItemTypeImpl(ItemType metaType, ItemTypeImpl superType, UUID id,
			int intId, boolean hasContent, boolean isAbstract,
			String shortname, String displayName) {
		super(id, metaType, null, shortname);
		if (id == null) {
			throw new CadseIllegalArgumentException(Messages.error_id_is_null);
		}

		this._superType = superType == null ? CadseGCST.ITEM : superType;

		this._hasContent = hasContent;
		this._kind = 0;
		this._displayName = displayName == null ? shortname : displayName;
		this._kind = 0;
		if (isAbstract) {
			_kind |= ItemType.IT_ABSTRACT;
		}
		this._intId = intId;
		_subTypes = null;
		if (this._superType != null) {
			this._superType.addSubItemType(this);
		}
		this._state = ItemState.CREATED;
		setHasNameAttribute(true);
		setHasQualifiedNameAttribute(true);
	}

	public ItemTypeImpl(ItemType it, ItemDelta desc) {
		super(it, desc);
		this._kind = 0;

		_subTypes = null;
		_superType = null;

		// remove set super type from here, because super type can be not loaded
		// at this time.
		// move this at commitLoadCreateLink and a finishLoad
	}

	@Override
	public void loadItem(IWorkingLoadingItems wl, ItemDelta itemOperation,
			IErrorCollector errorCollector) throws CadseException {
		// correct link type Attributes is natif
		// TODO correct in model
		if (CadseGCST.TYPE_DEFINITION_lt_ATTRIBUTES != null)
			CadseGCST.TYPE_DEFINITION_lt_ATTRIBUTES.setIsNatif(true);
		else {
			//
			System.out.println("NOT GO HERE");
		}
		CadseGCST.ITEM_TYPE_lt_SUPER_TYPE.setIsNatif(true);
		super.loadItem(wl, itemOperation, errorCollector);

		this._hasContent = itemOperation.getBooleanAttribut(
				CadseGCST.ITEM_TYPE_at_HAS_CONTENT_, false);
		this._displayName = itemOperation.getStringAttribut(
				CadseGCST.ITEM_at_DISPLAY_NAME_, null);
		try {
			String icon = itemOperation
					.getAttribute(CadseGCST.ITEM_TYPE_at_ICON_);
			this._image = icon;
		} catch (Throwable e) {
			errorCollector
					.addError(
							itemOperation.getId(),
							"Cannot load the url attribute "
									+ itemOperation
											.getAttribute(CadseGCST.ITEM_TYPE_at_ICON_));
		}

		if (this.getId().equals(CadseDomain.EXT_ITEM_ID)
				|| this.getId().equals(CadseDomain.ITEM_ID)) {
			_superType = null;
		} else {
			if (_superType == null) {
				_superType = CadseGCST.ITEM;
			}
		}
	}

	/**
	 * Adds the sub item type.
	 * 
	 * @param sType
	 *            the sub item type
	 */
	public void addSubItemType(ItemType sType) {
		if (_subTypes == null) {
			_subTypes = new ArrayList<ItemType>();
		}
		_subTypes.add(sType);
	}

	/**
	 * Adds the sub item type.
	 * 
	 * @param sType
	 *            the s type
	 */
	public void removeSubItemType(ItemType sType) {
		if (_subTypes == null) {
			return;
		}
		_subTypes.remove(sType);
	}

	/**
	 * Gets the int id.
	 * 
	 * @return the int id
	 */
	public int getIntID() {
		return _intId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#getItemManager()
	 */
	public IItemManager getItemManager() {
		if (_itemManager == null && _superType != null) {
			return _superType.getItemManager();
		}
		if (_itemManager == null && this != CadseGCST.ITEM) {
			return CadseGCST.ITEM.getItemManager();
		}
		return _itemManager;
	}

	/**
	 * Sets the item manager.
	 * 
	 * @param itemManager
	 *            the new item manager
	 */
	public void setItemManager(IItemManager itemManager) {
		this._itemManager = itemManager;
	}

	@Override
	public Link commitLoadCreateLink(LinkType lt, Item destination)
			throws CadseException {
		if (lt == CadseGCST.ITEM_TYPE_lt_SUPER_TYPE) {
			if (isStatic() && _superType != null) {
				throw new CadseException("Read only");
			}
			if (LinkPathUtil.checkNonCicular(destination, this,
					CadseGCST.ITEM_TYPE_lt_SUPER_TYPE)) {
				throw new CadseException("Circular link");
			}
			_superType = (ItemType) destination;
			if (_superType != null) {
				_superType.addSubItemType(this);
			}
			return new ReflectLink(lt, this, destination, 0);
		}

		if (lt == CadseGCST.ITEM_TYPE_lt_SUB_TYPES) {
		}

		if (lt == CadseGCST.ITEM_TYPE_lt_LINK_TYPE) {
			return null;
		}
		if (lt == CadseGCST.TYPE_DEFINITION_lt_CADSE) {
			_cadseName = destination.getQualifiedName();
			// _cadseRuntime = destination
			return new ReflectLink(lt, this, destination, 0, Item.IS_HIDDEN);
		}

		return super.commitLoadCreateLink(lt, destination);
	}

	@Override
	public synchronized void removeOutgoingLink(Link link, boolean notifie) {
		LinkType lt = link.getLinkType();
		Item destination = link.getDestination();

		if (lt == CadseGCST.ITEM_TYPE_lt_SUPER_TYPE && destination.isResolved()) {
			if (isStatic() && _superType != null) {
				throw new CadseIllegalArgumentException("Read only");
			}
			if (LinkPathUtil.checkNonCicular(destination, this,
					CadseGCST.ITEM_TYPE_lt_SUPER_TYPE)) {
				throw new CadseIllegalArgumentException("Circular link");
			}

			if (destination == _superType) {
				if (_superType != null) {
					_superType.removeSubItemType(this);
				}
				_superType = null;
			}
			return;
		}

		if (lt == CadseGCST.ITEM_TYPE_lt_SUB_TYPES) {
			return;
		}

		if (lt == CadseGCST.ITEM_TYPE_lt_LINK_TYPE) {
			removeOutgoingLinkType((LinkType) link);
		}

		super.removeOutgoingLink(link, notifie);
	}

	/**
	 * Creates the derived link type.
	 * 
	 * @param uuid
	 *            TODO
	 * @param intID
	 *            the int id
	 * @param name
	 *            the name
	 * @param _kind
	 *            the _kind
	 * @param min
	 *            the min
	 * @param max
	 *            the max
	 * @param selection
	 *            the selection
	 * @param lt
	 *            the lt
	 * 
	 * @return the derived link type
	 */
	public DerivedLinkType createDerivedLinkType(UUID uuid, int intID,
			String name, int _kind, int min, int max, String selection,
			LinkType lt) {
		DerivedLinkTypeImpl ret = null;
		preconditions_createLinkType(name, _kind, min, max, lt.getDestination());
		ret = new DerivedLinkTypeImpl(uuid, _kind, this, name, intID, min, max,
				selection, lt);
		addOutgoingLinkType(ret);
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#hasContent()
	 */
	public boolean hasContent() {
		return _hasContent;
	}

	/**
	 * Get domain.
	 * 
	 * @return the domain
	 */
	public CadseDomain getDomain() {
		return CadseCore.getCadseDomain();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.ItemImpl#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ItemType) {
			return getId().equals(((ItemType) obj).getId());
		}
		return super.equals(obj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.ItemImpl#hashCode()
	 */
	@Override
	public int hashCode() {
		return getId().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getId() + " " + getName()
				+ (_hasContent ? (Messages.has_content) : Messages.no_content);
	}

	// -------------------------------------------------//

	/* PRIVATE METHODS */

	/**
	 * Test preconditions before creating a link type.<br/>
	 * <br/>
	 * 
	 * Preconditions: <br/>
	 * - 1. <tt>name</tt> cannot be null.<br/>
	 * - 2. <tt>name</tt> cannot be empty. - 3. <tt>destination</tt> cannot be
	 * null.<br/>
	 * - 4. <tt>name</tt> muqt be unique.<br/>
	 * - 5. <tt>destination</tt> cannot be type workspace.<br/>
	 * - 6. <tt>min</tt> must greater or equal 0; <tt>max</tt> either equal -1
	 * (means the instance's number of this link type is undefined), or either
	 * greater than <tt>min</tt>.
	 * 
	 * @param name
	 *            : name of link type to create.
	 * @param kind
	 *            : kind of link type, can be a Aggregation, or a Contaiment, or
	 *            Other.
	 * @param min
	 *            : the minimum instances of this link type that we want create.
	 * @param max
	 *            : the maximum instances of this link type that we want create.
	 * @param destination
	 *            : link type's destination.<br/>
	 * <br/>
	 * 
	 * @OCL: pre: name <> null pre: id <> '' pre: destination <> null pre:
	 *       self.to->forAll(rt | rt.name <> id) -- id must be unique. pre: not
	 *       destination.oclIsTypeOf(WorkspaceType) -- destination cannot be a
	 *       Workspace Type. pre: ((max>=min)||(max==-1))&&(min>=0))
	 * @exception IllegalArgumentException
	 *                : Invalid assignment, <tt>name</tt> can not be null.<br/>
	 *                IllegalArgumentException: Invalid assignment,
	 *                <tt>name</tt> can not be empty.<br/>
	 *                IllegalArgumentException: Invalid assignment, item type
	 *                <tt>$name</tt> can not be null.<br/>
	 *                IllegalArgumentException: Invalid assignment, this link
	 *                type <tt>destination</tt> already exist.<br/>
	 *                IllegalArgumentException: Invalid assignment, you can not
	 *                create a link type whose destination is an object of
	 *                WorkspaceType.<br/>
	 *                IllegalArgumentException: Invalid assignment, verify the
	 *                values min and max.<br/>
	 */
	private void preconditions_createLinkType(String name, int kind, int min,
			int max, ItemType destination) {

		// 1. pre: name <> null
		if (name == null) {
			throw new CadseIllegalArgumentException(
					Messages.error_linktype_id_is_null);
		}

		// 2. pre: id <> ''
		if (name.length() == 0) {
			throw new CadseIllegalArgumentException(
					Messages.error_linktype_id_is_empty);
		}

		// 3. pre: destination <> null
		if (destination == null) {
			throw new CadseIllegalArgumentException(
					Messages.error_item_type_can_not_be_null);
		}

		// 4. pre: self.to->forAll(rt | rt.name <> id)
		for (Iterator outgoers = getOutgoingLinkTypes().iterator(); outgoers
				.hasNext();) {
			LinkType lt = (LinkType) outgoers.next();
			if (lt.getName().equals(name)) {
				throw new CadseIllegalArgumentException(
						Messages.error_linktype_id_already_exits, name, getId());
			}
		}

		// 6. pre: ((max>=min)||(max==-1))&&(min>=0))
		if (!(((max >= min) || (max == -1)) && (min >= 0))) {
			throw new CadseIllegalArgumentException(
					Messages.error_linktype_min_max);
		}

		// in Item not in ItemType
		// // 7. one relation of containment by destination.
		// if ((kind & LinkType.CONTAINMENT) != 0) {
		// if ( destination.isContainmentType())
		// exception("Cannot create a containment link form {0} to {1}, the
		// destination has allready a link of type
		// containement.",getId(),destination.getId());
		// if ( max != 1 && min != 1)
		// exception("Cannot create a containment link form {0} to {1}, the
		// cardinality must be 1:1.",getId(),destination.getId());
		// }
	}

	// /**
	// * Cette methode est appeler uniquement par createLinkType.
	// *
	// * @param type
	// */
	// private void addFrom(LinkType type) {
	// incomings.add(type);
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#setHasUniqueNameAttribute(boolean)
	 */
	public void setHasQualifiedNameAttribute(boolean val) {
		if (val) {
			_kind |= IT_HAS_QUALIFIED_NAME;
		} else {
			_kind &= ~IT_HAS_QUALIFIED_NAME;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#hasUniqueNameAttribute()
	 */
	@Override
	public boolean hasQualifiedNameAttribute() {
		return (_kind & IT_HAS_QUALIFIED_NAME) != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#setHasShortNameAttribute(boolean)
	 */
	public void setHasNameAttribute(boolean val) {
		if (val) {
			_kind |= IT_HAS_NAME;
		} else {
			_kind &= ~IT_HAS_NAME;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#hasShortNameAttribute()
	 */
	public boolean hasShortNameAttribute() {
		return (_kind & IT_HAS_NAME) != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#setRootElement(boolean)
	 */
	public void setRootElement(boolean val) {
		if (val) {
			_kind |= IT_INSTACES_IS_ROOT_ELEMENT;
		} else {
			_kind &= ~IT_INSTACES_IS_ROOT_ELEMENT;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#isRootElement()
	 */
	public boolean isRootElement() {
		return (_kind & IT_INSTACES_IS_ROOT_ELEMENT) != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.AbstractItem#isComposite()
	 */
	@Override
	public boolean isComposite() {
		if ((_kind & IT_COMPOSITE) != 0) {
			return true;
		}
		if (_superType != null) {
			if (_superType.isComposite()) {
				this._kind |= IT_COMPOSITE;
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#isAbstract()
	 */
	public boolean isAbstract() {
		return (_kind & ItemType.IT_ABSTRACT) != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#isPartType()
	 */
	public boolean isPartType() {
		return _isPartType(this);
	}

	static public boolean _isPartType(ItemType _this) {
		for (LinkType lt : _this.getIncomingLinkTypes()) {
			if (lt.isPart()) {
				return true;
			}
		}

		return false;
	}

	@Override
	protected void computeIncomingLinkTypes(List<LinkType> ret) {
		if (_superType != null) {
			((TypeDefinitionImpl) _superType).computeIncomingLinkTypes(ret);
		}
		super.computeIncomingLinkTypes(ret);
		if (_extendedBy != null) {
			for (TypeDefinition ext : _extendedBy) {
				((TypeDefinitionImpl) ext).computeIncomingLinkTypes(ret);
			}
		}
	}

	@Override
	protected void computeOutgoingLinkTypes(List<LinkType> ret) {
		if (_superType != null) {
			((TypeDefinitionImpl) _superType).computeOutgoingLinkTypes(ret);
		}
		super.computeOutgoingLinkTypes(ret);
		if (_extendedBy != null) {
			for (TypeDefinition ext : _extendedBy) {
				((TypeDefinitionImpl) ext).computeOutgoingLinkTypes(ret);
			}
		}
	}

	// /////////

	// -------------------------------------------------//

	/* PRIVATE METHODS */

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#getSubTypes()
	 */
	public ItemType[] getSubTypes() {
		return _subTypes == null ? NO_SUB_TYPES : _subTypes
				.toArray(new ItemType[_subTypes.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#getSuperType()
	 */
	public ItemType getSuperType() {
		return this._superType;
	}

	/**
	 * Est-ce que moi je suis le super type de it
	 * 
	 * @param it
	 *            is an subtype of this
	 * @see fr.imag.adele.cadse.core.ItemType#isSuperTypeOf(fr.imag.adele.cadse.core.ItemType)
	 */
	public boolean isSuperTypeOf(ItemType it) {
		return _isSuperTypeOf(this, it);
	}

	static public boolean _isSuperTypeOf(ItemType _this, ItemType it) {
		if (it == null) {
			return false;
		}
		ItemType super_it = it;
		while ((super_it = super_it.getSuperType()) != null) {
			if (super_it == _this) {
				return true;
			}
		}
		return false;
	}

	@Override
	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {
		if (CadseGCST.ITEM_at_DISPLAY_NAME_ == type) {
			return (T) getDisplayName();
		}
		if (CadseGCST.ITEM_TYPE_at_MANAGER_CLASS_ == type) {
			return (T) _managerClass;
		}
		/*
		 * if (CadseGCST.ITEM_TYPE_at_CADSE_NAME_ == type) { return (T)
		 * getCadseName(); }
		 */
		if (CadseGCST.ITEM_TYPE_at_HAS_CONTENT_ == type) {
			return (T) Boolean.valueOf(this._hasContent);
		}
		if (CadseGCST.ITEM_TYPE_at_IS_ROOT_ELEMENT_ == type) {
			return (T) Boolean
					.valueOf((this._kind & ItemType.IT_INSTACES_IS_ROOT_ELEMENT) != 0);
		}
		if (CadseGCST.ITEM_TYPE_at_ICON_ == type) {
			if (_image == null)
				return null;
			return (T) _image;
		}
		if (CadseGCST.ITEM_TYPE_at_ITEM_FACTORY_ == type) {
			if (_itemFactory == null)
				return null;
			return (T) _itemFactory.getClass().getName();
		}
		if (CadseGCST.ITEM_TYPE_at_ITEM_MANAGER_ == type) {
			if (_itemManager == null)
				return null;
			return (T) _itemManager.getClass().getName();
		}
		if (CadseGCST.ITEM_TYPE_at_PACKAGE_NAME_ == type) {
			return (T) _packageName;
		}

		if (CadseGCST.TYPE_DEFINITION_at_ID_RUNTIME_ == type) {
			if (isRuntime())
				return (T) getId().toString();

		}
		return super.internalGetOwnerAttribute(type);
	}

	@Override
	public Iterator<Item> propagateValue(IAttributeType<?> type) {
		if (type == CadseGCST.ITEM_at_DISPLAY_NAME_)
			return null;
		if (_superType != null) {
			return Collections.singletonList((Item) _superType).iterator();
		}
		return null;
	}

	@Override
	public IAttributeType<?> getAttributeType(String name) {
		if (_superType != null) {
			IAttributeType<?> ret = _superType.getAttributeType(name);
			if (ret != null)
				return ret;
		}
		IAttributeType<?> ret = super.getAttributeType(name);
		if (ret != null)
			return ret;
		if (_extendedBy != null) {
			for (TypeDefinition ext : _extendedBy) {
				ret = ext.getAttributeType(name);
				if (ret != null)
					return ret;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.ItemImpl#setAttribute(java.lang.String,
	 * java.lang.Object)
	 */

	@Override
	public boolean commitSetAttribute(IAttributeType<?> type, Object value) {
		if (CadseGCST.ITEM_at_DISPLAY_NAME_ == type) {
			_displayName = Convert.toString(value);
			if (_displayName != null && _displayName.length() == 0)
				_displayName = null;
			return true;
		}
		if (CadseGCST.ITEM_TYPE_at_HAS_CONTENT_ == type) {
			this._hasContent = Convert.toBoolean(value);
			return true;
		}
		if (CadseGCST.ITEM_TYPE_at_IS_ROOT_ELEMENT_ == type) {
			if (value == null) {
				value = type.getDefaultValue();
			}
			setRootElement(Convert.toBoolean(value));
			return true;
		}
		if (CadseGCST.ITEM_TYPE_at_MANAGER_CLASS_ == type) {
			if ("".equals(value))
				return false;
			if (isRuntime())
				return false;
			_managerClass = Convert.toString(value);
			return true;
		}
		if (CadseGCST.ITEM_TYPE_at_PACKAGE_NAME_ == type) {
			if (value == null) {
				value = NO_VALUE_STRING;
			}
			setPackageName(Convert.toString(value));
			return true;
		}
		if (CadseGCST.ITEM_TYPE_at_ICON_ == type) {
			_image = Convert.toString(value);
			return true;
		}
		if (CadseGCST.ITEM_TYPE_at_ITEM_FACTORY_ == type) {
			if (value instanceof String) {
				// && (value.toString().length() == 0) {
				return false;
			}
			_itemFactory = (IItemFactory) value;
			return true;
		}
		if (CadseGCST.ITEM_TYPE_at_ITEM_MANAGER_ == type) {
			if (value instanceof String) {
				// && (value.toString().length() == 0) {
				return false;
			}
			_itemManager = (IItemManager) value;
			return true;
		}

		return super.commitSetAttribute(type, value);
	}

	public void getAllAttributeTypes(List<IAttributeType<?>> all,
			ItemFilter filter) {
		if (_superType != null) {
			_superType.getAllAttributeTypes(all, filter);
		}
		if (_extendedBy != null) {
			for (TypeDefinition ext : _extendedBy) {
				ext.getAllAttributeTypes(all, filter);
			}
		}
		super.getAllAttributeTypes(all, filter);
	}

	public void getAllAttributeTypes(Map<String, IAttributeType<?>> all,
			boolean keepLastAttribute, ItemFilter filter) {
		super.getAllAttributeTypes(all, keepLastAttribute, filter);

		if (_superType != null) {
			_superType.getAllAttributeTypes(all, keepLastAttribute, filter);
		}
	}

	public void getAllAttributeTypesKeys(Set<String> all, ItemFilter filter) {
		super.getAllAttributeTypesKeys(all, filter);

		if (_superType != null) {
			_superType.getAllAttributeTypesKeys(all, filter);
		}
	}


	/**
	 * Red�finition de la class d'action lors de la creation d'un item avec
	 * les page de creation...
	 * 
	 * @param clazz
	 *            the clazz
	 * @param defaultShortName
	 *            the default short name
	 */
	public void setCreationAction(Class<? extends IActionPage> clazz,
			String defaultShortName) {
		this._clazzAction = clazz;
		this._defaultInstanceName = defaultShortName;
	}
	
	@Override
	protected void computeAllActionContribution(Set<IActionContributor> action,
			Set<IActionContributor> overwrittenAction) {
		if (_superType != null) {
			((TypeDefinitionImpl) _superType).computeAllActionContribution(action, overwrittenAction);
		}
		super.computeAllActionContribution(action, overwrittenAction);
		if (_extendedBy != null) {
			for (TypeDefinition ext : _extendedBy) {
				((TypeDefinitionImpl) ext).computeAllActionContribution(action, overwrittenAction);
			}
		}
	}

	/**
	 * Cette method est appeler pour calculer l'ensemble des pages spécifique
	 * à afficher.
	 * 
	 * @param map
	 *            the map
	 * @param list
	 *            the list
	 */
	public void recurcifComputeCreationPage(FilterContext context,
			List<IPage> list) {
		if (_superType != null) {
			((ItemTypeImpl) _superType).recurcifComputeCreationPage(context,
					list);
		}
		super.recurcifComputeCreationPage(context, list);
		if (_extendedBy != null) {
			for (TypeDefinition ext : _extendedBy) {
				ext.recurcifComputeCreationPage(context, list);
			}
		}
	}

	/**
	 * Compute good modification page.
	 * 
	 * @param map
	 *            the map
	 * @param list
	 *            the list
	 */
	public void recurcifComputeModificationPage(FilterContext context,
			List<IPage> list, Set<IAttributeType<?>> ro) {
		if (_superType != null) {
			((ItemTypeImpl) _superType).recurcifComputeModificationPage(
					context, list, ro);
		}
		super.recurcifComputeModificationPage(context, list, ro);
		if (_extendedBy != null) {
			for (TypeDefinition ext : _extendedBy) {
				ext.recurcifComputeModificationPage(context, list, ro);
			}
		}
	}

	@Override
	public void computeValidators(FilterContext context,
			List<UIValidator> validators) {
		if (_superType != null) {
			((ItemTypeImpl) _superType).computeValidators(context, validators);
		}
		super.computeValidators(context, validators);
		if (_extendedBy != null) {
			for (TypeDefinition ext : _extendedBy) {
				ext.computeValidators(context, validators);
			}
		}
	}

	public UIField findField(IAttributeType<?> att) {
		UIField ret;
		if (_extendedBy != null) {
			for (TypeDefinition ext : _extendedBy) {
				ret = ext.findField(att);
				if (ret != null)
					return ret;
			}
		}
		ret = super.findField(att);
		if (ret != null)
			return ret;
		if (_superType != null) {
			return ((ItemTypeImpl) _superType).findField(att);
		}
		return null;
	}

	@Override
	public void computeGenericPage(FilterContext context,
			HierarchicPage genericPage,
			HashSet<IAttributeType<?>> inSpecificPages,
			Set<IAttributeType<?>> ro, IAttributeType<?>... firstAttributes) {
		super.computeGenericPage(context, genericPage, inSpecificPages, ro,
				firstAttributes);
		if (_extendedBy != null) {
			for (TypeDefinition ext : _extendedBy) {
				ext.computeGenericPage(context, genericPage, inSpecificPages,
						ro);
			}
		}
		if (_superType != null) {
			((ItemTypeImpl) _superType).computeGenericPage(context,
					genericPage, inSpecificPages, ro);
		}
	}

	public void computeGroup(Set<GroupOfAttributes> groups) {
		if (_superType != null) {
			((ItemTypeImpl) _superType).computeGroup(groups);
		}
		if (_extendedBy != null) {
			for (TypeDefinition ext : _extendedBy) {
				ext.computeGroup(groups);
			}
		}
		super.computeGroup(groups);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#getIcon()
	 */
	public String getImage() {
		if (_image == null && _superType != null) {
			return _superType.getImage();
		}
		return _image;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#setIcon(java.net.URL)
	 */
	public void setIcon(String url) {
		_image = url;
		Logger.getLogger("icon").log(Level.INFO,
				"set icon to " + url + " of ItemType " + getDisplayName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.ItemType#setSpaceKeyType(fr.imag.adele.cadse
	 * .core.key.SpaceKeyType)
	 */
	public void setSpaceKeyType(DefaultKeyDefinitionImpl spaceKeytype) {
		this._spaceKeytype = spaceKeytype;
	}

	@Override
	public void setKeyDefinition(KeyDefinition keyDefinition) {
		this._spaceKeytype = keyDefinition;
	}

	@Override
	public KeyDefinition getKeyDefinition() {
		if (this._spaceKeytype == null && _superType != null) {
			return _superType.getKeyDefinition();
		}
		return _spaceKeytype;
	}

	public <T> T getApdapter(Item instance, Class<T> clazz) {
		return null;
	}

	@Override
	protected void collectOutgoingLinks(LinkType linkType,
			CollectedReflectLink ret) {
		if (linkType == CadseGCST.ITEM_TYPE_lt_SUPER_TYPE) {
			ret.addOutgoing(CadseGCST.ITEM_TYPE_lt_SUPER_TYPE, getSuperType());
			return;
		}
		if (linkType == CadseGCST.TYPE_DEFINITION_lt_CADSE) {
			ret.addOutgoing(CadseGCST.TYPE_DEFINITION_lt_CADSE, getCadse(),
					Item.IS_HIDDEN);
			return;
		}
		if (linkType == CadseGCST.ITEM_TYPE_lt_SUB_TYPES) {
			ret.addOutgoing(CadseGCST.ITEM_TYPE_lt_SUB_TYPES, Item.IS_HIDDEN,
					this._subTypes);
			return;
		}
		if (linkType == CadseGCST.TYPE_DEFINITION_lt_ATTRIBUTES) {
			ret.addOutgoing(CadseGCST.TYPE_DEFINITION_lt_ATTRIBUTES,
					_attributesDefinitions);
			return;
		}
		if (linkType == CadseGCST.TYPE_DEFINITION_lt_CREATION_PAGES) {
			ret.addOutgoing(CadseGCST.TYPE_DEFINITION_lt_CREATION_PAGES,
					Item.IS_HIDDEN, _creationPages);
			return;
		}
		if (linkType == CadseGCST.TYPE_DEFINITION_lt_MODIFICATION_PAGES) {
			ret.addOutgoing(CadseGCST.TYPE_DEFINITION_lt_MODIFICATION_PAGES,
					Item.IS_HIDDEN, _modificationPages);
			return;
		}
		super.collectOutgoingLinks(linkType, ret);
	}

	public IItemFactory getItemFactory() {
		if (_itemFactory == null && this._itemManager instanceof IItemFactory) {
			_itemFactory = (IItemFactory) _itemManager;
		}
		if (_itemFactory == null && _superType != null) {
			return _superType.getItemFactory();
		}
		if (_itemFactory == null) {
			return ItemFactory.SINGLETON;
		}
		return _itemFactory;
	}

	public void setItemFactory(IItemFactory factory) {
		_itemFactory = factory;
	}

	

	public LogicalWorkspaceTransactionListener[] getLogicalWorkspaceTransactionListener() {
		ItemTypeImpl localSuperIT = (ItemTypeImpl) this;
		LogicalWorkspaceTransactionListener[] ret = null;
		while (localSuperIT != null) {
			if (localSuperIT.workspaceLogiqueCopyListeners != null) {
				ret = ArraysUtil.addList(
						LogicalWorkspaceTransactionListener.class, ret,
						localSuperIT.workspaceLogiqueCopyListeners);
			}
			if (_extendedBy != null) {
				for (ExtendedType e : _extendedBy) {
					ret = ArraysUtil.addList(
							LogicalWorkspaceTransactionListener.class, ret, e
									.getLogicalWorkspaceTransactionListener());
				}
			}
			localSuperIT = (ItemTypeImpl) localSuperIT._superType;
		}
		return ret;

	}

	@Override
	public void setSuperType(ItemType it) {
		_superType = (ItemTypeImpl) it;
		((ItemTypeImpl) it).addSubItemType(this);
	}

	@Override
	public String getItemManagerClass() {
		if (_managerClass == null && getItemManager() != null)
			_managerClass = getItemManager().getClass().getName();

		return _managerClass;
	}

	@Override
	public List<LinkType> getGroupOutgoingLinkTypes() {
		ArrayList<LinkType> ret = new ArrayList<LinkType>();
		for (LinkType lt : getOutgoingLinkTypes())
			if (lt.isGroup())
				ret.add(lt);
		return ret;
	}

	@Override
	public ItemType getGroupType() {
		if (_type == this)
			return null;
		if (getType().isGroupType())
			return getType();
		return null;
	}

	@Override
	public boolean isGroupOf(GroupType groupType) {
		if (groupType == null)
			return false;

		GroupType gt = getGroupType();
		if (gt == null)
			return false;
		if (gt == groupType)
			return true;
		return groupType.isSuperGroupTypeOf(gt);
	}

	@Override
	public boolean isGroupType() {
		if (isGroupHead())
			return true;
		for (LinkType l : getOutgoingLinkTypes()) {
			if (l.isGroup())
				return true;
		}
		return false;
	}

	@Override
	public boolean isSuperGroupTypeOf(GroupType gt) {
		if (gt == null) {
			return false;
		}
		GroupType super_it = gt;
		while ((super_it = super_it.getGroupType()) != null) {
			if (super_it == this) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<Item> getMembers() {
		return new ArrayList<Item>(getOutgoingItems(
				CadseGCST.GROUP_EXT_ITEM_lt_MEMBERS, true));
	}

	@Override
	public boolean isGroupHead() {
		return getGroupType() != null;
	}

	@Override
	public ItemType[] getAllSubGroupType() {
		return isGroupType() ? getSubTypes() : new ItemType[0];
	}

	@Override
	public boolean isMemberType() {
		for (LinkType lt : getIncomingLinkTypes()) {
			if (lt.isGroup())
				return true;
		}
		return false;
	}

	@Override
	public boolean canCreateItem(NewContext newContext) {
		Assert.isTrue(newContext.getDestinationType() == this, "");
		{
			Item[] incSrc = newContext.getIncomingSources();
			LinkType[] incLT = newContext.getIncomingLinkType();
			if (incSrc != null) {
				for (int i = 0; i < incLT.length; i++) {
					LinkType lt = incLT[i];
					Item src = incSrc[i];
					if (!src.getType().canCreateItem(newContext, lt, src)) {
						return false;
					}
				}
			}
		}
		{
			Item[] outDest = newContext.getOutgoingDestinations();
			LinkType[] outLT = newContext.getOutgoingLinkType();
			if (outDest != null) {
				for (int i = 0; i < outLT.length; i++) {
					LinkType lt = outLT[i];
					Item dst = outDest[i];
					if (!dst.getType().canCreateItem(newContext, lt, dst)) {
						return false;
					}
				}
			}
		}

		if (getItemManager().canCreateMeItem(newContext.getPartParent(),
				newContext.getPartLinkType(), this) != null) {
			return false;
		}
		return true;
	}

	@Override
	public boolean canCreateItem(NewContext newContext, LinkType lt, Item src) {
		if (lt == CadseGCST.ITEM_lt_PARENT) {
			if (src.getType().getItemManager().canCreateChildItem(src,
					newContext.getPartLinkType(),
					newContext.getDestinationType()) != null)
				return false;
		}
		if (lt == CadseGCST.ITEM_lt_INSTANCE_OF) {
			if (!(src instanceof ItemType) || ((ItemType) src).isAbstract()) {
				return false;
			}
		}
		return true;
	}

	public String getDefaultInstanceName() {
		return _defaultInstanceName;
	}

	@Override
	public ExtendedType[] getExtendedType() {
		return _extendedBy;
	}
	
	@Override
	public boolean isMainType() {
		return true;
	}

	@Override
	public void addExtendedType(ExtendedType et) {
		int index = ArraysUtil.indexOf(_extendedBy, et);
		if (index == -1) {
			_extendedBy = ArraysUtil.add(ExtendedType.class, _extendedBy, et);
		}
	}

	@Override
	public void removeExtendedType(ExtendedType et) {
		_extendedBy = ArraysUtil.remove(ExtendedType.class, _extendedBy, et);
	}
	
}
