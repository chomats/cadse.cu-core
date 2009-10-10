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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.imag.adele.cadse.core.CadseDomain;
import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.CadseRuntime;
import fr.imag.adele.cadse.core.CadseUtil;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.DerivedLinkType;
import fr.imag.adele.cadse.core.IItemFactory;
import fr.imag.adele.cadse.core.IItemManager;
import fr.imag.adele.cadse.core.IItemNode;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemFilter;
import fr.imag.adele.cadse.core.ItemState;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.Messages;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.delta.ItemDelta;
import fr.imag.adele.cadse.core.impl.CadseCore;
import fr.imag.adele.cadse.core.impl.CadseIllegalArgumentException;
import fr.imag.adele.cadse.core.impl.CollectedReflectLink;
import fr.imag.adele.cadse.core.impl.ItemFactory;
import fr.imag.adele.cadse.core.impl.ReflectLink;
import fr.imag.adele.cadse.core.impl.internal.ui.IPage2;
import fr.imag.adele.cadse.core.impl.internal.ui.PagesImpl;
import fr.imag.adele.cadse.core.impl.ui.CreationAction;
import fr.imag.adele.cadse.core.impl.ui.ModificationAction;
import fr.imag.adele.cadse.core.impl.ui.PageImpl;
import fr.imag.adele.cadse.core.internal.IWorkingLoadingItems;
import fr.imag.adele.cadse.core.internal.ItemTypeInternal;
import fr.imag.adele.cadse.core.key.SpaceKeyType;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransactionListener;
import fr.imag.adele.cadse.core.ui.IActionContributor;
import fr.imag.adele.cadse.core.ui.IActionPage;
import fr.imag.adele.cadse.core.ui.IPage;
import fr.imag.adele.cadse.core.ui.IPageFactory;
import fr.imag.adele.cadse.core.ui.Pages;
import fr.imag.adele.cadse.core.util.ArraysUtil;
import fr.imag.adele.cadse.core.util.Convert;
import fr.imag.adele.cadse.core.util.IErrorCollector;
import fr.imag.adele.cadse.core.util.LinkPathUtil;

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
public class ItemTypeImpl extends ItemImpl implements ItemType, ItemTypeInternal {

	/**
	 * old string
	 * 
	 * @deprecated use {@link CadseGCST#ITEM_TYPE_at_QUALIFIED_NAME_}
	 */
	@Deprecated
	static final String							UNIQUE_NAME_KEY				= "#unique-name";

	/**
	 * old string
	 * 
	 * @deprecated use {@link CadseGCST#ITEM_TYPE_at_DISPLAY_NAME_}
	 */
	@Deprecated
	static final String							DISPLAY_NAME_KEY			= "#display-name";

	/**
	 * old string
	 * 
	 * @deprecated use {@link CadseGCST#ITEM_TYPE_at_NAME_}
	 */
	@Deprecated
	public static final String					SHORT_NAME_KEY				= "#short-name";

	/*
	 * old string
	 */
	@Deprecated
	public static final String					ATTR_SHORT_NAME				= "ws::private::short-name";

	/** The Constant EMPTY_ACTION_CONTRIBUTORS. */
	public static final IActionContributor[]	EMPTY_ACTION_CONTRIBUTORS	= new IActionContributor[0];

	/** The Constant EMPTY_PAGE_FACTORIES. */
	public static final IPageFactory[]			EMPTY_PAGE_FACTORIES		= new IPageFactory[0];

	/** The Constant NO_SUB_TYPES. */
	private final static ItemType[]				NO_SUB_TYPES				= new ItemType[0];

	/** The int id. */
	private int									_intId;

	/** The super type. */
	ItemType									_superType 					= null;

	/** The sub types. */
	private List<ItemType>	  					_subTypes;

	IAttributeType<?>[]							_attributesDefinitions		= null;

	/** The attribute types. */
	private Map<String, IAttributeType<?>>		__cache_attributeTypes		= null;

	/** The outgoings lt. */
	private LinkType[]							_outgoingsLT;

	/** The incomings lt. */
	private LinkType[]							_incomingsLT;

	/** The has content. */
	private boolean								_hasContent;												// contenu

	/** The kind. */
	private int									_kind;

	/** The item manager. */
	private IItemManager						_itemManager					= null;

	/** The space keytype. */
	private SpaceKeyType						_spaceKeytype				= null;

	/** The display name. */
	private String								_displayName;

	/** The action contributors. */
	IActionContributor[]						_actionContributors			= null;
	// cache
	/** The __action contributors. */
	IActionContributor[]						__actionContributors		= null;

	/** The creation pages. */
	IPageFactory[]								_creationPagesFactories		= null;

	/** The modification pages. */
	IPageFactory[]								_modificationPagesFactories	= null;
	// cache
	/** The __creation pages. */
	IPageFactory[]								__creationPages				= null;

	// IPage[] pages = null;
	IPage2[]									_creationPages				= null;
	IPage2[]									_modificationPages			= null;

	// // cache
	/** The __modification pages. */
	IPageFactory[]								__modificationPages			= null;

	/** The icon. */
	private URL									_icon;

	/** The clazz action. */
	private Class<? extends IActionPage>		_clazzAction;

	/** The default short name action. */
	private String								_defaultShortNameAction;

	private String								_cadseName					= NO_VALUE_STRING;

	private String								_packageName					= NO_VALUE_STRING;

	private IItemFactory						_itemFactory;

	/**
	 * implementation of extension ...
	 */
	private ItemType[]							_extendedBy;

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
	protected ItemTypeImpl(ItemType metaType, LogicalWorkspace wl, ItemTypeImpl superType, CompactUUID id, int intId,
			boolean hasContent, boolean isAbstract, String shortname, String displayName) {
		super(wl, id, metaType, null, shortname);
		if (id == null) {
			throw new CadseIllegalArgumentException(Messages.error_id_is_null);
		}

		this._superType = superType == null ? CadseGCST.ITEM : superType;
		this.__cache_attributeTypes = null;
		this._incomingsLT = null;
		this._outgoingsLT = null;
		this._hasContent = hasContent;
		this._kind = 0;
		this._displayName = displayName == null ? shortname : displayName;
		this._kind = 0;
		if (isAbstract) {
			_kind |= ItemType.ABSTRACT;
		}
		this._intId = intId;
		_subTypes = null;
		if (this._superType != null) {
			this._superType.addSubItemType(this);
		}
		this._state = ItemState.CREATED;
		setHasShortNameAttribute(true);
		setHasUniqueNameAttribute(true);
	}

	public ItemTypeImpl(LogicalWorkspace wl, ItemType it, ItemDelta desc) {
		super(wl, it, desc);
		this.__cache_attributeTypes = null;
		this._incomingsLT = null;
		this._outgoingsLT = null;
		this._kind = 0;

		_subTypes = null;
		_superType = null;

		//remove set super type from here, because super type can be not loaded at this time.
		// move this at commitLoadCreateLink and a finishLoad
	}

	@Override
	public void loadItem(IWorkingLoadingItems wl, ItemDelta itemOperation, IErrorCollector errorCollector)
			throws CadseException {
		//correct link type Attributes is natif 
		//TODO correct in model
		if (CadseGCST.ABSTRACT_ITEM_TYPE_lt_ATTRIBUTES != null)
			CadseGCST.ABSTRACT_ITEM_TYPE_lt_ATTRIBUTES.setIsNatif(true);
		else {
			//
			System.out.println("NOT GO HERE");
		}
		CadseGCST.ITEM_TYPE_lt_SUPER_TYPE.setIsNatif(true);
		super.loadItem(wl, itemOperation, errorCollector);

		this._hasContent = itemOperation.getBooleanAttribut(CadseGCST.ITEM_TYPE_at_HAS_CONTENT_, false);
		this._displayName = itemOperation.getStringAttribut(CadseGCST.ITEM_at_DISPLAY_NAME_, null);
		try {
			this._icon = itemOperation.getAttribute(CadseGCST.ITEM_TYPE_at_ICON);
		} catch (Throwable e) {
			errorCollector.addError(itemOperation.getId(), "Cannot load the url attribute "
					+ itemOperation.getAttribute(CadseGCST.ITEM_TYPE_at_ICON_));
		}
		
		if (this.getId().equals(CadseDomain.EXT_ITEM_ID) || this.getId().equals(CadseDomain.ITEM_ID)) {
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
	public Link commitLoadCreateLink(LinkType lt, Item destination) throws CadseException {
		if (lt == CadseGCST.ABSTRACT_ITEM_TYPE_lt_ATTRIBUTES) {
			if (destination.getType() == CadseGCST.UNRESOLVED_ATTRIBUTE_TYPE) {
				return null;
			}
			if (destination.getType() == CadseGCST.LINK) {
				if (!(destination instanceof LinkType)) {
					throw new CadseException("Destination is not a LinkType : {0}", destination
							.getQualifiedDisplayName());
				}
				LinkType atlt = (LinkType) destination;
				if (!this.m_outgoings.contains(atlt)) {
					this.m_outgoings.add(atlt);
				}
				if (lt.isComposition()) {
					this._kind |= COMPOSITE;
				}
				resetOutgoingLinkType();
			}

			if (!(destination instanceof IAttributeType)) {
				throw new CadseException("Destination is not an IAttributeType : {0}", destination
						.getQualifiedDisplayName());
			}
			return _addAttributeType((IAttributeType<?>) destination);
		}
		if (lt == CadseGCST.ITEM_TYPE_lt_SUPER_TYPE) {
			if (isStatic() && _superType != null) {
				throw new CadseException("Read only");
			}
			if (LinkPathUtil.checkNonCicular(destination, this, CadseGCST.ITEM_TYPE_lt_SUPER_TYPE)) {
				throw new CadseException("Circular link");
			}
			_superType = (ItemType) destination;
			if (_superType != null) {
				_superType.addSubItemType(this);
			}
			return new ReflectLink(lt, this, destination, 0);
		}
		if (lt == CadseGCST.ITEM_TYPE_lt_CREATION_PAGES) {
			this._creationPages = ArraysUtil.add(IPage2.class, this._creationPages, (IPage2) destination);
			resetCreationPages();
			return new ReflectLink(lt, this, destination, this._creationPages.length - 1);
		}
		if (lt == CadseGCST.ITEM_TYPE_lt_MODIFICATION_PAGES) {
			this._modificationPages = ArraysUtil.add(IPage2.class, this._modificationPages, (IPage2) destination);
			resetModificationPages();
			return new ReflectLink(lt, this, destination, this._modificationPages.length - 1);
		}
		if (lt == CadseGCST.ITEM_TYPE_lt_SUB_TYPES) {
		}

		if (lt == CadseGCST.ITEM_TYPE_lt_LINK_TYPE) {

		}
		if (lt == CadseGCST.ITEM_TYPE_lt_CADSE_RUNTIME) {
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

		if (lt == CadseGCST.ABSTRACT_ITEM_TYPE_lt_ATTRIBUTES && destination.isResolved()) {
			removeAttributeType((IAttributeType<?>) destination);
			if (destination.getType() == CadseGCST.LINK) {
				LinkType atlt = (LinkType) destination;
				this.m_outgoings.remove(atlt);
				resetOutgoingLinkType();
				if (atlt.isComposition()) {
					for (LinkType outgoing_lt : getOutgoingLinkTypes()) {
						if (outgoing_lt.isComposite()) {
							return;
						}
					}
					this._kind &= ~COMPOSITE;
				}
			}
			return;
		}
		if (lt == CadseGCST.ITEM_TYPE_lt_SUPER_TYPE && destination.isResolved()) {
			if (isStatic() && _superType != null) {
				throw new CadseIllegalArgumentException("Read only");
			}
			if (LinkPathUtil.checkNonCicular(destination, this, CadseGCST.ITEM_TYPE_lt_SUPER_TYPE)) {
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
		if (lt == CadseGCST.ITEM_TYPE_lt_CREATION_PAGES && destination.isResolved()) {
			this._creationPages = ArraysUtil.remove(IPage2.class, this._creationPages, (IPage2) destination);
			resetCreationPages();
			return;
		}
		if (lt == CadseGCST.ITEM_TYPE_lt_MODIFICATION_PAGES && destination.isResolved()) {
			this._modificationPages = ArraysUtil.remove(IPage2.class, this._modificationPages, (IPage2) destination);
			resetModificationPages();
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
	 * Create a link type. <br/> <br/>
	 * 
	 * Pr�conditions: <br/> - 1. <tt>name</tt> cannot be null.<br/> - 2.
	 * <tt>name</tt> cannot be empty. - 3. <tt>destination</tt> cannot be
	 * null.<br/> - 4. <tt>name</tt> muqt be unique.<br/> - 5.
	 * <tt>destination</tt> cannot be type workspace.<br/> - 6. <tt>min</tt>
	 * must greater or equal 0; <tt>max</tt> either equal -1 (means the
	 * instance's number of this link type is undefined), or either greater than
	 * <tt>min</tt>.
	 * 
	 * @param intID
	 *            the int id
	 * @param min :
	 *            the minimum instances of this link type that we want create.
	 * @param max :
	 *            the maximum instances of this link type that we want create.
	 * @param selection
	 *            the selection
	 * @param destination :
	 *            link type's destination.<br/> <br/>
	 * @param name :
	 *            name of link type to create.
	 * @param _kind
	 *            the _kind
	 * 
	 * @return the link type
	 * 
	 * @OCL: pre: name <> null pre: id <> '' pre: destination <> null pre:
	 *       self.to->forAll(rt | rt.name <> id) -- id must be unique. pre: not
	 *       destination.oclIsTypeOf(WorkspaceType) -- destination cannot be a
	 *       Workspace Type. pre: ((max>=min)||(max==-1))&&(min>=0)) <br/>
	 * @exception IllegalArgumentException:
	 *                Invalid assignment, <tt>name</tt> can not be null.<br/>
	 *                IllegalArgumentException: Invalid assignment,
	 *                <tt>name</tt> can not be empty.<br/>
	 *                IllegalArgumentException: Invalid assignment, item type
	 *                <tt>$name</tt> can not be null.<br/>
	 *                IllegalArgumentException: Invalid assignment, this link
	 *                type <tt>destination</tt> already exist.<br/>
	 *                IllegalArgumentException: Invalid assignment, you can not
	 *                create a link type whose destination is an object of
	 *                WorkspaceType.<br/> IllegalArgumentException: Invalid
	 *                assignment, verify the values min and max.<br/> <br/>
	 */
	public LinkType createLinkType(CompactUUID uuid, int intID, String name, int _kind, int min, int max,
			String selection, ItemType destination) {
		LinkType ret = null;
		if (_superType != null) {
			ret = (LinkType) _superType.getAttributeType(name, false);
			if (ret != null) {
				// this.outgoingsLT.add(ret);
				// addAttributeType(ret);
				if (ret.isComposition()) {
					this._kind |= COMPOSITE;
				}
				return ret;
			}
		}

		preconditions_createLinkType(name, _kind, min, max, destination);

		ret = new LinkTypeImpl(uuid, _kind, this, name, intID, min, max, selection, destination);
		Link l = addOutgoingLinkType(ret);
		return ret;
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
	public DerivedLinkType createDerivedLinkType(CompactUUID uuid, int intID, String name, int _kind, int min, int max,
			String selection, LinkType lt) {
		DerivedLinkTypeImpl ret = null;
		preconditions_createLinkType(name, _kind, min, max, lt.getDestination());
		ret = new DerivedLinkTypeImpl(uuid, _kind, this, name, intID, min, max, selection, lt);
		addOutgoingLinkType(ret);
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#createLinkType(int,
	 *      java.lang.String, int, int, int, java.lang.String,
	 *      fr.imag.adele.cadse.core.LinkType)
	 */
	public LinkType createLinkType(CompactUUID uuid, int intID, String id, int kind, int min, int max,
			String selection, LinkType inverse) throws CadseException {
		if (!inverse.getDestination().equals(this)) {
			throw new CadseException(Messages.error_destination_bad_inverse_link, getName(), inverse.getDestination()
					.getName());
		}
		LinkTypeImpl lt = (LinkTypeImpl) createLinkType(uuid, intID, id, kind, min, max, selection, inverse.getSource());
		lt.setInverseLinkType(inverse);
		((LinkTypeImpl) inverse).setInverseLinkType(lt);
		return lt;
	}

	/**
	 * Creates the link type.
	 * 
	 * @param uuid
	 *            TODO
	 * @param id
	 *            the id
	 * @param kind
	 *            the kind
	 * @param min
	 *            the min
	 * @param max
	 *            the max
	 * @param selection
	 *            the selection
	 * @param destination
	 *            the destination
	 * 
	 * @return the link type
	 */
	public LinkType createLinkType(CompactUUID uuid, String id, int kind, int min, int max, String selection,
			ItemType destination) {
		return createLinkType(uuid, -1, id, kind, min, max, selection, destination);
	}

	/**
	 * Creates the link type.
	 * 
	 * @param uuid
	 *            TODO
	 * @param id
	 *            the id
	 * @param kind
	 *            the kind
	 * @param min
	 *            the min
	 * @param max
	 *            the max
	 * @param selection
	 *            the selection
	 * @param inverse
	 *            the inverse
	 * 
	 * @return the link type
	 * @throws CadseException
	 */
	public LinkType createLinkType(CompactUUID uuid, String id, int kind, int min, int max, String selection,
			LinkType inverse) throws CadseException {
		return createLinkType(uuid, -1, id, kind, min, max, selection, inverse);
	}

	/**
	 * Adds the outgoing link type.
	 * 
	 * @param ret
	 *            the ret
	 */
	Link addOutgoingLinkType(LinkType ret) {
		this.m_outgoings.add(ret);
		Link l = addAttributeType(ret);
		if (ret.isComposition()) {
			this._kind |= COMPOSITE;
		}
		resetOutgoingLinkType();

		//addInconmmingLink(ret, ret.getDestination(), CadseGCST.LINK_lt_DESTINATION);
		//addInconmmingLink(ret, ret.getInverse(), CadseGCST.LINK_lt_INVERSE_LINK);
		//addInconmmingLink(ret, ret.getSource(), CadseGCST.LINK_lt_SOURCE);
		return l;
	}

	public void addInconmmingLink(Item src, Item dst, LinkType lt) {
		if (lt == null || src == null || dst == null) {
			return;
		}

		Link l = src.getOutgoingLink(lt, dst.getId());
		if (l != null) {
			dst.addIncomingLink(l, false);
		}
	}

	void removeOutgoingLinkType(LinkType ret) {
		this.m_outgoings.remove(ret);
		removeAttributeType(ret);
		resetOutgoingLinkType();
		if (ret.isComposition()) {
			for (LinkType lt : getOutgoingLinkTypes()) {
				if (lt.isComposite()) {
					return;
				}
			}

			this._kind &= ~COMPOSITE;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.ItemImpl#addIncomingLink(fr.imag.adele.cadse.core.Link)
	 */
	@Override
	public synchronized void addIncomingLink(Link link, boolean notifie) {
		if (link.getLinkType() == CadseCore.theLinkType) {
			resetIncomingLinkType();
		}
		super.addIncomingLink(link, notifie);
	}

	@Override
	public synchronized void removeIncomingLink(Link link, boolean notifie) {
		if (link.getLinkType() == CadseCore.theLinkType) {
			resetIncomingLinkType();
		}

		super.removeIncomingLink(link, notifie);
	}

	/**
	 * Get an ougoing link type by id.
	 * 
	 * @param name
	 *            the name
	 * 
	 * @return a link type if found; null if not found.
	 */
	public LinkType getOutgoingLinkType(String name) {
		return _getOutgoingLinkType(this, name);
	}
	
	static public LinkType _getOutgoingLinkType(ItemType _this, String name) {
		IAttributeType<? extends Object> a = _this.getAttributeType(name, false);
		if (a instanceof LinkType) {
			return (LinkType) a;
		}

		return Accessor.filterName(_this.getOutgoingLinkTypes(), name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#getOutgoingLinkType(fr.imag.adele.cadse.core.ItemType,
	 *      java.lang.String)
	 */
	public LinkType getOutgoingLinkType(ItemType destination, String name) {
		return _getOutgoingLinkType(this, destination, name);
	}
	
	static public LinkType _getOutgoingLinkType(ItemType _this, ItemType destination, String name) {
		Iterator<LinkType> iter = _this.getOutgoingLinkTypes().iterator();
		while (iter.hasNext()) {
			LinkType lt = iter.next();
			if (lt.getDestination().equals(destination) && (lt.getName().equals(name))) {
				return lt;
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#getOutgoingLinkType(fr.imag.adele.cadse.core.ItemType,
	 *      int)
	 */
	public LinkType getOutgoingLinkType(ItemType dest, int kind) {
		return _getOutgoingLinkType(this, dest, kind);
	}

	static public LinkType _getOutgoingLinkType(ItemType _this, ItemType dest, int kind) {
		Iterator<LinkType> iter = _this.getOutgoingLinkTypes().iterator();
		while (iter.hasNext()) {
			LinkType lt = iter.next();
			if (lt.getDestination().equals(dest) && (lt.getKind() == kind)) {
				return lt;
			}
		}
		return null;
	}

	public List<LinkType> getOwnerOugoingLinkTypes() {
		return getOwnerOutgoingLinkTypes();
	}

	/**
	 * Get an incoming link type by id.
	 * 
	 * @param name
	 *            the name
	 * 
	 * @return a link type if found; null if not found.
	 */
	public LinkType getIncomingLinkType(String name) {
		return Accessor.filterName(getIncomingLinkTypes(), name);
	}

	/**
	 * Get all outgoing link types.
	 * 
	 * @return an unmodifiable list all outgoing link types.
	 */
	public List<LinkType> getOutgoingLinkTypes() {
		if (_outgoingsLT == null) {
			computeOutgoingLinkTypes();
		}
		return Arrays.asList(_outgoingsLT);
	}

	/**
	 * Get all owned outgoing link types, not hierarchical.
	 * 
	 * @return an unmodifiable list all owned outgoing link types.
	 */
	public List<LinkType> getOwnerOutgoingLinkTypes() {
		List<LinkType> ret = new ArrayList<LinkType>();

		for (Link l : this.m_outgoings) {
			if (l.getLinkType() == CadseCore.theLinkType) {
				ret.add((LinkType) l);
			}
		}
		return ret;
	}

	/**
	 * Compute ougoing link types.
	 */
	private void computeOutgoingLinkTypes() {
		List<LinkType> ret = new ArrayList<LinkType>();
		if (_superType != null) {
			ret.addAll(_superType.getOutgoingLinkTypes());
		}
		for (Link l : this.m_outgoings) {
			if (l.getLinkType() == CadseCore.theLinkType) {
				ret.add((LinkType) l);
			}
		}
		this._outgoingsLT = ret.toArray(new LinkType[ret.size()]);
	}

	/**
	 * Get all incoming link types.
	 * 
	 * @return an unmodifiable list all incoming link types.
	 */
	public List<LinkType> getIncomingLinkTypes() {
		if (_incomingsLT == null) {
			computeIncomingLinkTypes();
		}
		return Arrays.asList(_incomingsLT);
	}

	/**
	 * Compute incoming link types.
	 */
	private void computeIncomingLinkTypes() {
		List<LinkType> ret = new ArrayList<LinkType>();
		if (_superType != null) {
			ret.addAll(_superType.getIncomingLinkTypes());
		}
		for (Link l : this._incomings) {
			if (l.getLinkType() == CadseCore.theLinkType) {
				ret.add((LinkType) l);
			}
		}
		this._incomingsLT = ret.toArray(new LinkType[ret.size()]);
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
		return getId() + " " + getName() + (_hasContent ? (Messages.has_content) : Messages.no_content);
	}

	// -------------------------------------------------//

	/* PRIVATE METHODS */

	/**
	 * Test preconditions before creating a link type.<br/><br/>
	 * 
	 * Preconditions: <br/> - 1. <tt>name</tt> cannot be null.<br/> - 2.
	 * <tt>name</tt> cannot be empty. - 3. <tt>destination</tt> cannot be
	 * null.<br/> - 4. <tt>name</tt> muqt be unique.<br/> - 5.
	 * <tt>destination</tt> cannot be type workspace.<br/> - 6. <tt>min</tt>
	 * must greater or equal 0; <tt>max</tt> either equal -1 (means the
	 * instance's number of this link type is undefined), or either greater than
	 * <tt>min</tt>.
	 * 
	 * @param name :
	 *            name of link type to create.
	 * @param kind :
	 *            kind of link type, can be a Aggregation, or a Contaiment, or
	 *            Other.
	 * @param min :
	 *            the minimum instances of this link type that we want create.
	 * @param max :
	 *            the maximum instances of this link type that we want create.
	 * @param destination :
	 *            link type's destination.<br/> <br/>
	 * 
	 * @OCL: pre: name <> null pre: id <> '' pre: destination <> null pre:
	 *       self.to->forAll(rt | rt.name <> id) -- id must be unique. pre: not
	 *       destination.oclIsTypeOf(WorkspaceType) -- destination cannot be a
	 *       Workspace Type. pre: ((max>=min)||(max==-1))&&(min>=0))
	 * @exception IllegalArgumentException:
	 *                Invalid assignment, <tt>name</tt> can not be null.<br/>
	 *                IllegalArgumentException: Invalid assignment,
	 *                <tt>name</tt> can not be empty.<br/>
	 *                IllegalArgumentException: Invalid assignment, item type
	 *                <tt>$name</tt> can not be null.<br/>
	 *                IllegalArgumentException: Invalid assignment, this link
	 *                type <tt>destination</tt> already exist.<br/>
	 *                IllegalArgumentException: Invalid assignment, you can not
	 *                create a link type whose destination is an object of
	 *                WorkspaceType.<br/> IllegalArgumentException: Invalid
	 *                assignment, verify the values min and max.<br/>
	 */
	private void preconditions_createLinkType(String name, int kind, int min, int max, ItemType destination) {

		// 1. pre: name <> null
		if (name == null) {
			throw new CadseIllegalArgumentException(Messages.error_linktype_id_is_null);
		}

		// 2. pre: id <> ''
		if (name.length() == 0) {
			throw new CadseIllegalArgumentException(Messages.error_linktype_id_is_empty);
		}

		// 3. pre: destination <> null
		if (destination == null) {
			throw new CadseIllegalArgumentException(Messages.error_item_type_can_not_be_null);
		}

		// 4. pre: self.to->forAll(rt | rt.name <> id)
		for (Iterator outgoers = getOutgoingLinkTypes().iterator(); outgoers.hasNext();) {
			LinkType lt = (LinkType) outgoers.next();
			if (lt.getName().equals(name)) {
				throw new CadseIllegalArgumentException(Messages.error_linktype_id_already_exits, name, getId());
			}
		}

		// 6. pre: ((max>=min)||(max==-1))&&(min>=0))
		if (!(((max >= min) || (max == -1)) && (min >= 0))) {
			throw new CadseIllegalArgumentException(Messages.error_linktype_min_max);
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
	public void setHasUniqueNameAttribute(boolean val) {
		if (val) {
			_kind |= UNIQUE_NAME;
		} else {
			_kind &= ~UNIQUE_NAME;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#hasUniqueNameAttribute()
	 */
	@Override
	public boolean hasUniqueNameAttribute() {
		return (_kind & UNIQUE_NAME) != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#setHasShortNameAttribute(boolean)
	 */
	public void setHasShortNameAttribute(boolean val) {
		if (val) {
			_kind |= SHORT_NAME;
		} else {
			_kind &= ~SHORT_NAME;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#hasShortNameAttribute()
	 */
	public boolean hasShortNameAttribute() {
		return (_kind & SHORT_NAME) != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#setRootElement(boolean)
	 */
	public void setRootElement(boolean val) {
		if (val) {
			_kind |= ROOT_ELEMENT;
		} else {
			_kind &= ~ROOT_ELEMENT;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#isRootElement()
	 */
	public boolean isRootElement() {
		return (_kind & ROOT_ELEMENT) != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.AbstractItem#isComposite()
	 */
	@Override
	public boolean isComposite() {
		if ((_kind & COMPOSITE) != 0) {
			return true;
		}
		if (_superType != null) {
			if (_superType.isComposite()) {
				this._kind |= COMPOSITE;
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
		return (_kind & ItemType.ABSTRACT) != 0;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#getIncomingPart(fr.imag.adele.cadse.core.ItemType)
	 */
	public LinkType getIncomingPart(ItemType typeParent) {

		for (LinkType lt : getIncomingLinkTypes()) {
			if (lt.isPart() && lt.getSource().equals(typeParent)) {
				return lt;
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#getIncomingOne(fr.imag.adele.cadse.core.ItemType)
	 */
	public LinkType getIncomingOne(ItemType typeParent) throws CadseException {
		LinkType found = null;
		for (LinkType lt : getIncomingLinkTypes()) {
			if (lt.getSource().equals(typeParent)) {
				if (found == null) {
					found = lt;
				} else {
					return null;
					
					//throw new CadseException(Messages.error_linktype_more_one_link_found, typeParent.getId(), getId());
				}
			}
		}
		return found;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#hasIncomingParts()
	 */
	public boolean hasIncomingParts() {
		for (LinkType lt : getIncomingLinkTypes()) {
			if (lt.isPart()) {
				return true;
			}
		}
		return false;
	}

	// /////////

	// -------------------------------------------------//

	/* PRIVATE METHODS */

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#getItems()
	 */
	public List<Item> getItems() {
		return _wl.getItems(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#getSubTypes()
	 */
	public ItemType[] getSubTypes() {
		return _subTypes == null ? NO_SUB_TYPES : _subTypes.toArray(new ItemType[_subTypes.size()]);
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
			return (T) _displayName;
		}
		/*if (CadseGCST.ITEM_TYPE_at_CADSE_NAME_ == type) {
			return (T) getCadseName();
		}*/
		if (CadseGCST.ITEM_TYPE_at_HAS_CONTENT_ == type) {
			return (T) Boolean.valueOf(this._hasContent);
		}
		if (CadseGCST.ITEM_TYPE_at_IS_ROOT_ELEMENT_ == type) {
			return (T) Boolean.valueOf((this._kind & ItemType.ROOT_ELEMENT) != 0);
		}
		if (CadseGCST.ITEM_TYPE_at_ICON_ == type) {
			if (_icon == null) return null;
			return (T) _icon.getPath();
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
		
		if (CadseGCST.ABSTRACT_ITEM_TYPE_at_ID_RUNTIME_ == type) {
			if (isRuntime())
				return (T) getId().toString();
			
				
		}
		return super.internalGetOwnerAttribute(type);
	}

	
	public boolean isRuntime() {
		CadseRuntime cr = getCadseRuntime();
		if (cr == null) return false;
		return cr.getType() == CadseGCST.CADSE_RUNTIME;
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
	public Iterator<Item> propagateValue(String key) {
		if (_superType != null) {
			return Collections.singletonList((Item) _superType).iterator();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.ItemImpl#setAttribute(java.lang.String,
	 *      java.lang.Object)
	 */

	@Override
	public boolean commitSetAttribute(IAttributeType<?> type, String key, Object value) {
		if (CadseGCST.ITEM_at_DISPLAY_NAME.equals(key)) {
			_displayName = Convert.toString(value);
			return true;
		}
		if (CadseGCST.ITEM_TYPE_at_HAS_CONTENT.equals(key)) {
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
		if (CadseGCST.ITEM_TYPE_at_PACKAGE_NAME_ == type) {
			if (value == null) {
				value = NO_VALUE_STRING;
			}
			setPackageName(Convert.toString(value));
			return true;
		}
		if (CadseGCST.ITEM_TYPE_at_ICON_ == type) {
			try {
				_icon = Convert.toURL(value);
				return true;
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (CadseGCST.ITEM_TYPE_at_ITEM_FACTORY.equals(key)) {
			if (value instanceof String) {
				// && (value.toString().length() == 0) {
				return false;
			}
			_itemFactory = (IItemFactory) value;
			return true;
		}
		if (CadseGCST.ITEM_TYPE_at_ITEM_MANAGER.equals(key)) {
			if (value instanceof String) {
				// && (value.toString().length() == 0) {
				return false;
			}
			_itemManager = (IItemManager) value;
			return true;
		}

		return super.commitSetAttribute(type, key, value);
	}

	public <T> Link addAttributeType(IAttributeType<T> type) {
		Link l = _addAttributeType(type);
		this._wl.registerItem(type);
		if (l != null) {
			type.addIncomingLink(l, false);
		}
		return l;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IAttributableType#addAttributeType(fr.imag.adele.cadse.core.IAttributeType)
	 */
	public <T> Link _addAttributeType(IAttributeType<T> type) {
		int index = ArraysUtil.indexOf(_attributesDefinitions, type);
		if (index != -1) {
			_wl._wd.log(getDisplayName(), "Cannot add "+type, null);
			return null;
		}
		
		_attributesDefinitions = ArraysUtil.add(IAttributeType.class, _attributesDefinitions, type);
		type.setParent(this, CadseGCST.ABSTRACT_ITEM_TYPE_lt_ATTRIBUTES);
		index = _attributesDefinitions.length - 1;
		
		if (CadseGCST.ABSTRACT_ITEM_TYPE_lt_ATTRIBUTES == null) {
			// le model root n'est pas encore charg�.
			return null;
		}
		
		
		return new ReflectLink(CadseGCST.ABSTRACT_ITEM_TYPE_lt_ATTRIBUTES, this, type, index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IAttributableType#addAttributeType(fr.imag.adele.cadse.core.IAttributeType)
	 */
	public <T> int removeAttributeType(IAttributeType<T> type) {
		int index = ArraysUtil.indexOf(_attributesDefinitions, type);
		if (index == -1) {
			return index;
		}
		_attributesDefinitions = ArraysUtil.remove(IAttributeType.class, _attributesDefinitions, index);
		// Pose un probleme lors de la destruction d'un lien
		// type.setParent(null); (note 22)
		return index;
	}

	public IAttributeType<?>[] getAllAttributeTypes() {
		Map<String, IAttributeType<?>> all = new HashMap<String, IAttributeType<?>>();
		getAllAttributeTypes(all, true);
		Collection<IAttributeType<?>> ret = all.values();
		return ret.toArray(new IAttributeType<?>[ret.size()]);
	}

	public void getAllAttributeTypes(List<IAttributeType<?>> all) {
		getAllAttributeTypes(all, null);
	}

	public void getAllAttributeTypes(List<IAttributeType<?>> all, ItemFilter filter) {
		if (_superType != null) {
			_superType.getAllAttributeTypes(all, filter);
		}
		if (_attributesDefinitions != null) {
			if (filter == null) {
				all.addAll(Arrays.asList(_attributesDefinitions));
			} else {
				for (IAttributeType<?> at : _attributesDefinitions) {
					if (filter.accept(at)) {
						all.add(at);
					}
				}
			}
		}
	}

	public void getAllAttributeTypes(Map<String, IAttributeType<?>> all, boolean keepLastAttribute) {
		getAllAttributeTypes(all, keepLastAttribute, null);
	}

	public void getAllAttributeTypes(Map<String, IAttributeType<?>> all, boolean keepLastAttribute, ItemFilter filter) {
		if (_attributesDefinitions != null) {
			for (IAttributeType<?> att : _attributesDefinitions) {
				if (keepLastAttribute && all.containsKey(att.getName())) {
					continue;
				}
				if (filter == null || filter.accept(att)) {
					all.put(att.getName(), att);
				}
			}
		}

		if (_superType != null) {
			_superType.getAllAttributeTypes(all, keepLastAttribute, filter);
		}
	}

	public void getAllAttributeTypesKeys(Set<String> all, ItemFilter filter) {
		if (_attributesDefinitions != null) {
			for (IAttributeType<?> att : _attributesDefinitions) {
				if (filter == null || filter.accept(att)) {
					all.add(att.getName());
				}
			}
		}

		if (_superType != null) {
			_superType.getAllAttributeTypesKeys(all, filter);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IAttributableType#getAttributeType(java.lang.String)
	 */

	public IAttributeType<?> getAttributeType(String name) {
		return getAttributeType(name, true);
	}

	public IAttributeType<?> getAttributeType(String name, boolean createUnresolvedDefinition) {
		if (SHORT_NAME_KEY.equals(name) || ATTR_SHORT_NAME.equals(name)) {
			return CadseGCST.ITEM_at_NAME_;
		}
		if (UNIQUE_NAME_KEY.equals(name) || Item.ATTR_LONG_NAME.equals(name)) {
			return CadseGCST.ITEM_at_QUALIFIED_NAME_;
		}
		if (Item.IS_READ_ONLY_KEY.equals(name)) {
			return CadseGCST.ITEM_at_ITEM_READONLY_;
		}
		if (DISPLAY_NAME_KEY.equals(name)) {
			return CadseGCST.ITEM_at_DISPLAY_NAME_;
		}
		if ("UUID_ATTRIBUTE".equals(name)) {
			if (this == CadseGCST.CADSE_DEFINITION)
				return CadseGCST.CADSE_DEFINITION_at_ID_RUNTIME_;
			if (this == CadseGCST.ATTRIBUTE)
				return CadseGCST.ATTRIBUTE_at_ID_RUNTIME_;
			if (this == CadseGCST.ABSTRACT_ITEM_TYPE)
				return CadseGCST.ABSTRACT_ITEM_TYPE_at_ID_RUNTIME_;
		}

		IAttributeType<?> ret = null;
		if (__cache_attributeTypes != null) {
			ret = __cache_attributeTypes.get(name);
			if (ret != null) {
				return ret;
			}
		}

		if (_attributesDefinitions != null) {
			for (IAttributeType<?> att : _attributesDefinitions) {
				if (att.getName().equals(name)) {
					if (__cache_attributeTypes == null) {
						__cache_attributeTypes = new HashMap<String, IAttributeType<?>>();
					}
					__cache_attributeTypes.put(name, att);
					return att;
				}
			}
		}
		if (ret == null && _superType != null) {
			ret = _superType.getAttributeType(name, false);
			if (ret != null) {
				if (__cache_attributeTypes == null) {
					__cache_attributeTypes = new HashMap<String, IAttributeType<?>>();
				}
				__cache_attributeTypes.put(name, ret);
			}
		}
		if (createUnresolvedDefinition && ret == null) {
			return _wl.createUnresolvedAttributeType(this, name);
		}
		return ret;
	}

	public CadseRuntime getCadseRuntime() {
		Item parent = _parent;
		while (parent != null) {
			if (parent.isInstanceOf(CadseGCST.CADSE_RUNTIME) && parent instanceof CadseRuntime)
				return (CadseRuntime) parent;
			parent = parent.getPartParent();
		}
		if (_cadseName != null)
			return _wl.getCadseRuntime(_cadseName);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IAttributableType#getAttributeTypeIds()
	 */
	public String[] getAttributeTypeIds() {
		HashSet<String> returnKeys = new HashSet<String>();
		getAllAttributeTypesKeys(returnKeys, new FilterOutLinkType());
		return returnKeys.toArray(new String[returnKeys.size()]);
	}

	/**
	 * Ajoute un contributeur d'actions.
	 * 
	 * @param contributor
	 *            the contributor
	 */
	public synchronized void addActionContributeur(IActionContributor contributor) {
		_actionContributors = ArraysUtil.add(IActionContributor.class, _actionContributors, contributor);
		resetContributions();
	}

	/**
	 * Ajoute des pages de creation.
	 * 
	 * @param creationPages
	 *            the creation pages
	 */
	public synchronized void addCreationPages(List<IPageFactory> creationPages) {
		if (creationPages == null || creationPages.size() == 0) {
			return; // todo nothing
		}
		this._creationPagesFactories = ArraysUtil
				.addList(IPageFactory.class, this._creationPagesFactories, creationPages);
		resetCreationPages();
	}

	/**
	 * Ajoute des pages de modification.
	 * 
	 * @param modificationPages
	 *            the modification pages
	 */
	public void addModificationPages(List<IPageFactory> modificationPages) {
		if (modificationPages == null || modificationPages.size() == 0) {
			return; // todo nothing
		}
		this._modificationPagesFactories = ArraysUtil.addList(IPageFactory.class, this._modificationPagesFactories,
				modificationPages);
		resetModificationPages();
	}

	/**
	 * reset du cache de modification de pages.
	 */
	public void resetModificationPages() {
		__modificationPages = null;
		if (_subTypes != null) {
			for (ItemType subT : _subTypes) {
				subT.resetModificationPages();
			}
		}
	}

	/**
	 * reset du cache de outgoing link type.
	 */
	public void resetOutgoingLinkType() {
		if (_outgoingsLT == null) {
			return;
		}
		_outgoingsLT = null;
		if (_subTypes != null) {
			for (ItemType subT : _subTypes) {
				subT.resetOutgoingLinkType();

			}
		}
	}

	/**
	 * reset du cache de incoming link type.
	 */
	public void resetIncomingLinkType() {
		if (_incomingsLT == null) {
			return;
		}
		_incomingsLT = null;
		if (_subTypes != null) {
			for (ItemType subT : _subTypes) {
				subT.resetIncomingLinkType();

			}
		}
	}

	/**
	 * reset du cache des pages de creation.
	 */
	public void resetCreationPages() {
		__creationPages = null;
		if (_subTypes != null) {
			for (ItemType subT : _subTypes) {
				subT.resetCreationPages();

			}
		}
	}

	/**
	 * reset du cache des contributions.
	 */
	public void resetContributions() {
		if (__actionContributors == null) {
			return;
		}
		__actionContributors = null;
		if (_subTypes != null) {
			for (ItemType subT : _subTypes) {
				subT.resetContributions();

			}
		}
	}

	/**
	 * Red�finition de la class d'action lors de la creation d'un item avec les
	 * page de creation...
	 * 
	 * @param clazz
	 *            the clazz
	 * @param defaultShortName
	 *            the default short name
	 */
	public void setCreationAction(Class<? extends IActionPage> clazz, String defaultShortName) {
		this._clazzAction = clazz;
		this._defaultShortNameAction = defaultShortName;
	}

	/**
	 * return les actions contributions du propre du type. cf
	 * {@link #getAllActionContribution()};
	 * 
	 * @return the action contribution
	 */
	public IActionContributor[] getActionContribution() {
		return _actionContributors == null ? EMPTY_ACTION_CONTRIBUTORS : _actionContributors;
	}

	/**
	 * return toutes les actions contributions du type et des sous type ...
	 * 
	 * @return the all action contribution
	 */
	public IActionContributor[] getAllActionContribution() {
		if (__actionContributors == null) {
			if (_superType != null) {
				if (_actionContributors == null || _actionContributors.length == 0) {
					__actionContributors = _superType.getAllActionContribution();
				} else {
					IActionContributor[] super_a = _superType.getAllActionContribution();
					if (super_a.length == 0) {
						__actionContributors = getActionContribution();
					} else {
						__actionContributors = ArraysUtil.merge(IActionContributor.class, super_a, _actionContributors);
					}
				}
			} else {
				__actionContributors = getActionContribution();
			}
		}
		return __actionContributors;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#getCreationPage()
	 */
	public IPageFactory[] getCreationPage() {
		return this._creationPagesFactories == null ? EMPTY_PAGE_FACTORIES : this._creationPagesFactories;
	}

	/**
	 * Gets the good creation page_.
	 * 
	 * @return the good creation page_
	 */
	public IPageFactory[] getGoodCreationPage_() {
		if (__creationPages == null) {
			Map<String, IPageFactory> map = new HashMap<String, IPageFactory>();
			List<IPageFactory> list = new ArrayList<IPageFactory>();
			computegetGoodCreationPage(map, list);
			int count = list.size();
			for (IPageFactory factory : list) {
				if (factory.isEmptyPage()) {
					count--;
				}
			}
			if (count == 0) {
				__creationPages = EMPTY_PAGE_FACTORIES;
			} else {
				__creationPages = new IPageFactory[count];
				int i = 0;
				for (IPageFactory factory : list) {
					if (factory.isEmptyPage()) {
						continue;
					}
					__creationPages[i++] = factory;
				}
				assert i == count;
			}
		}
		return __creationPages;
	}

	/**
	 * Computeget good creation page.
	 * 
	 * @param map
	 *            the map
	 * @param list
	 *            the list
	 */
	public void computegetGoodCreationPage(Map<String, IPageFactory> map, List<IPageFactory> list) {
		if (_superType != null) {
			_superType.computegetGoodCreationPage(map, list);
		}
		if (_creationPagesFactories != null) {
			for (IPageFactory f : _creationPagesFactories) {
				IPageFactory oldF = null;
				if ((oldF = map.get(f.getName())) != null) {
					for (int i = 0; i < list.size(); i++) {
						if (list.get(i) == oldF) {
							list.set(i, f);
							map.put(f.getName(), f);
							break;
						}
					}

				} else {
					list.add(f);
					map.put(f.getName(), f);
				}
			}
		}
		if (this._creationPages != null) {
			for (IPageFactory f : _creationPages) {
				IPageFactory oldF = null;
				if ((oldF = map.get(f.getName())) != null) {
					for (int i = 0; i < list.size(); i++) {
						if (list.get(i) == oldF) {
							list.set(i, f);
							map.put(f.getName(), f);
							break;
						}
					}

				} else {
					list.add(f);
					map.put(f.getName(), f);
				}
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
	void computeGoodModificationPage(Map<String, IPageFactory> map, List<IPageFactory> list) {
		if (_superType != null) {
			((ItemTypeImpl) _superType).computeGoodModificationPage(map, list);
		}
		if (_modificationPagesFactories != null) {
			for (IPageFactory f : _modificationPagesFactories) {
				IPageFactory oldF = null;
				if ((oldF = map.get(f.getName())) != null) {
					for (int i = 0; i < list.size(); i++) {
						if (list.get(i) == oldF) {
							list.set(i, f);
							map.put(f.getName(), f);
							break;
						}
					}

				} else {
					list.add(f);
					map.put(f.getName(), f);
				}
			}
		}
		if (this._modificationPages != null) {
			for (IPageFactory f : _modificationPages) {
				IPageFactory oldF = null;
				if ((oldF = map.get(f.getName())) != null) {
					for (int i = 0; i < list.size(); i++) {
						if (list.get(i) == oldF) {
							list.set(i, f);
							map.put(f.getName(), f);
							break;
						}
					}

				} else {
					list.add(f);
					map.put(f.getName(), f);
				}
			}
		}
	}

	/**
	 * Gets the good modification page_.
	 * 
	 * @return the good modification page_
	 */
	public IPageFactory[] getGoodModificationPage_() {
		if (__modificationPages == null) {
			Map<String, IPageFactory> map = new HashMap<String, IPageFactory>();
			List<IPageFactory> list = new ArrayList<IPageFactory>();
			computeGoodModificationPage(map, list);
			int count = list.size();
			for (IPageFactory factory : list) {
				if (factory.isEmptyPage()) {
					count--;
				}
			}
			if (count == 0) {
				__modificationPages = EMPTY_PAGE_FACTORIES;
			} else {
				__modificationPages = new IPageFactory[count];
				IPageFactory evolPage = null;
				int i = 0;
				for (IPageFactory factory : list) {
					if (factory.isEmptyPage()) {
						continue;
					}
					if (factory.getName().equals("evolution-page")) {
						evolPage = factory;
						continue;
					}
					__modificationPages[i++] = factory;
				}
				if (evolPage != null) {
					__modificationPages[i++] = evolPage;
				}
				assert i == count;
			}
		}
		return __modificationPages;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#getModificationPage()
	 */
	public IPageFactory[] getModificationPage() {
		return this._modificationPagesFactories == null ? EMPTY_PAGE_FACTORIES : this._modificationPagesFactories;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#getGoodCreationPage(fr.imag.adele.cadse.core.Item,
	 *      fr.imag.adele.cadse.core.ItemType,
	 *      fr.imag.adele.cadse.core.LinkType)
	 */
	public Pages getGoodCreationPage(Item parent, ItemType type, LinkType lt) throws CadseException {
		return createCreationPage(parent, type, lt);
	}

	/**
	 * Creates the creation page.
	 * 
	 * @param parent
	 *            the parent
	 * @param type
	 *            the type
	 * @param lt
	 *            the lt
	 * 
	 * @return the pages
	 * 
	 * @throws CadseException
	 *             the melusine exception
	 */
	protected Pages createCreationPage(Item parent, ItemType type, LinkType lt) throws CadseException {
		IPageFactory[] pf = getGoodCreationPage_();
		IPage[] p = createPages(IPageFactory.PAGE_CREATION_ITEM, parent, null, type, lt, pf);
		return new PagesImpl(createDefaultCreationAction(parent, type, lt), p);
	}

	/**
	 * Creates the pages.
	 * 
	 * @param cas
	 *            the cas
	 * @param item
	 *            the item
	 * @param node
	 *            the node
	 * @param type
	 *            the type
	 * @param lt
	 *            the lt
	 * @param pf
	 *            the pf
	 * 
	 * @return the i page[]
	 */
	protected IPage[] createPages(int cas, Item item, IItemNode node, ItemType type, LinkType lt, IPageFactory[] pf) {
		ArrayList<IPage> ret = new ArrayList<IPage>();
		for (int i = 0; i < pf.length; i++) {
			try {
				IPage createdPage = pf[i].createPage(cas, null, item, node, type, lt);
				if (createdPage != null && !createdPage.isEmpty()) {
					createdPage.setParent(type, CadseGCST.ABSTRACT_ITEM_TYPE_lt_ATTRIBUTES);
					ret.add(createdPage);
				}
			} catch (Throwable e) {
				_wl.getCadseDomain().log("error", "Cannot create page " + pf[i], e);
			}
		}
		return ret.toArray(new IPage[ret.size()]);
	}

	/**
	 * Creates the default creation action.
	 * 
	 * @param parent
	 *            the parent
	 * @param type
	 *            the type
	 * @param lt
	 *            the lt
	 * 
	 * @return the i action page
	 * 
	 * @throws CadseException
	 *             the melusine exception
	 */
	protected IActionPage createDefaultCreationAction(Item parent, ItemType type, LinkType lt) throws CadseException {
		if (_clazzAction == null) {
			if (_defaultShortNameAction == null) {
				return new CreationAction(parent, type, lt);
			} else {
				return new CreationAction(parent, type, lt, _defaultShortNameAction);
			}
		}
		Constructor<?> c = null;
		if (_defaultShortNameAction != null) {
			try {
				c = _clazzAction.getConstructor(Item.class, ItemType.class, LinkType.class, String.class);
				return (IActionPage) c.newInstance(parent, type, lt, _defaultShortNameAction);
			} catch (NoSuchMethodException e) {

			} catch (IllegalArgumentException e) {
				throw new CadseException("Cannot create creation action {1} : {0}.", e, _clazzAction, c);
			} catch (InstantiationException e) {
				throw new CadseException("Cannot create creation action {1} : {0}.", e, _clazzAction, c);
			} catch (IllegalAccessException e) {
				throw new CadseException("Cannot create creation action {1} : {0}.", e, _clazzAction, c);
			} catch (InvocationTargetException e) {
				throw new CadseException("Cannot create creation action {1} : {0}.", e, _clazzAction, c);
			}
		}
		if (_defaultShortNameAction == null) {
			try {
				c = _clazzAction.getConstructor(Item.class, ItemType.class, LinkType.class);
				return (IActionPage) c.newInstance(parent, type, lt);
			} catch (NoSuchMethodException e) {

			} catch (IllegalArgumentException e) {
				throw new CadseException("Cannot create creation action {1} : {0}.", e, _clazzAction, c);
			} catch (InstantiationException e) {
				throw new CadseException("Cannot create creation action {1} : {0}.", e, _clazzAction, c);
			} catch (IllegalAccessException e) {
				throw new CadseException("Cannot create creation action {1} : {0}.", e, _clazzAction, c);
			} catch (InvocationTargetException e) {
				throw new CadseException("Cannot create creation action {1} : {0}.", e, _clazzAction, c);
			}
		}
		try {
			return _clazzAction.newInstance();
		} catch (InstantiationException e) {
			throw new CadseException("Cannot create creation action {1} with default constructor.", e, _clazzAction);
		} catch (IllegalAccessException e) {
			throw new CadseException("Cannot create creation action {1} with default constructor.", e, _clazzAction);
		}
	}

	/**
	 * Creates the default modification action.
	 * 
	 * @param node
	 *            the node
	 * 
	 * @return the i action page
	 */
	protected IActionPage createDefaultModificationAction(ItemType it, IItemNode node) {
		return new ModificationAction(it, node);
	}

	/**
	 * Creates the default modification action.
	 * 
	 * @param selected
	 *            the selected
	 * 
	 * @return the i action page
	 */
	protected IActionPage createDefaultModificationAction(ItemType it, Item selected) {
		return new ModificationAction(it, selected);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#getGoodModificationPage(fr.imag.adele.cadse.core.Item)
	 */
	public Pages getGoodModificationPage(Item selected) {
		IPageFactory[] pf = getGoodModificationPage_();
		IPage[] p = createPages(IPageFactory.PAGE_PROPERTY_ITEM, selected, null, this, null, pf);
		return new PagesImpl(createDefaultModificationAction(this, selected), p);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#getGoodModificationPage(fr.imag.adele.cadse.core.IItemNode)
	 */
	public Pages getGoodModificationPage(IItemNode node) {
		IPageFactory[] pf = getGoodModificationPage_();
		IPage[] p = createPages(IPageFactory.PAGE_PROPERTY_VIEW_ITEM, node.getItem(), node, this, null, pf);
		return new PagesImpl(createDefaultModificationAction(this, node), p);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#getIcon()
	 */
	public URL getImage() {
		if (_icon == null && _superType != null) {
			return _superType.getImage();
		}
		return _icon;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#setIcon(java.net.URL)
	 */
	public void setIcon(URL url) {
		_icon = url;
		Logger.getLogger("icon").log(Level.INFO, "set icon to " + url + " of ItemType " + getDisplayName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#setSpaceKeyType(fr.imag.adele.cadse.core.key.SpaceKeyType)
	 */
	public void setSpaceKeyType(SpaceKeyType spaceKeytype) {
		this._spaceKeytype = spaceKeytype;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#getSpaceKeyType()
	 */
	public SpaceKeyType getSpaceKeyType() {
		if (this._spaceKeytype == null && _superType != null) {
			return _superType.getSpaceKeyType();
		}
		return _spaceKeytype;
	}

	public <T> T getApdapter(Item instance, Class<T> clazz) {
		return null;
	}

	public String getCadseName() {
		CadseRuntime cr = getCadseRuntime();
		if (cr != null) {
			_cadseName = cr.getQualifiedName();
			return cr.getQualifiedName();
		}
		return _cadseName;
	}

	public void setCadseName(String cadseName) {
		CadseRuntime cr = getCadseRuntime();
		if (cr != null) {
			_cadseName = cr.getQualifiedName();
		}
		this._cadseName = cadseName;		
	}

	@Override
	protected void collectOutgoingLinks(LinkType linkType, CollectedReflectLink ret) {
		if (linkType == CadseGCST.ITEM_TYPE_lt_SUPER_TYPE) {
			ret.addOutgoing(CadseGCST.ITEM_TYPE_lt_SUPER_TYPE, getSuperType());
		}
		if (linkType == CadseGCST.ITEM_TYPE_lt_CADSE_RUNTIME) {
			ret.addOutgoing(CadseGCST.ITEM_TYPE_lt_CADSE_RUNTIME, getCadseRuntime(), Item.IS_HIDDEN);
		}
		if (linkType == CadseGCST.ITEM_TYPE_lt_SUB_TYPES) {
			ret.addOutgoing(CadseGCST.ITEM_TYPE_lt_SUB_TYPES, Item.IS_HIDDEN, this._subTypes);
		}
		if (linkType == CadseGCST.ABSTRACT_ITEM_TYPE_lt_ATTRIBUTES) {
			ret.addOutgoing(CadseGCST.ABSTRACT_ITEM_TYPE_lt_ATTRIBUTES, _attributesDefinitions);
		}
		if (linkType == CadseGCST.ITEM_TYPE_lt_CREATION_PAGES) {
			ret.addOutgoing(CadseGCST.ITEM_TYPE_lt_CREATION_PAGES, Item.IS_HIDDEN, _creationPages);
		}
		if (linkType == CadseGCST.ITEM_TYPE_lt_MODIFICATION_PAGES) {
			ret.addOutgoing(CadseGCST.ITEM_TYPE_lt_MODIFICATION_PAGES,Item.IS_HIDDEN, _modificationPages);
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

	public PageImpl getFirstCreatedPage() {
		if (_creationPages == null || _creationPages.length == 0) {
			return null;
		}
		return _creationPages[0];
	}

	public PageImpl getFirstModificationPage() {
		if (_modificationPages == null || _modificationPages.length == 0) {
			return null;
		}
		return _modificationPages[0];
	}

	public void setPackageName(String packageName) {
		if (packageName == null) {
			this._packageName = NO_VALUE_STRING;
		} else {
			this._packageName = packageName;
		}
	}

	public String getPackageName() {
		return _packageName;
	}

	private LogicalWorkspaceTransactionListener[]	workspaceLogiqueCopyListeners;

	public void addLogicalWorkspaceTransactionListener(LogicalWorkspaceTransactionListener l) {
		workspaceLogiqueCopyListeners = ArraysUtil.add(LogicalWorkspaceTransactionListener.class,
				workspaceLogiqueCopyListeners, l);
	}

	public void removeLogicalWorkspaceTransactionListener(LogicalWorkspaceTransactionListener l) {
		workspaceLogiqueCopyListeners = ArraysUtil.remove(LogicalWorkspaceTransactionListener.class,
				workspaceLogiqueCopyListeners, l);
	}

	public LogicalWorkspaceTransactionListener[] getLogicalWorkspaceTransactionListener() {
		ItemTypeImpl localSuperIT = (ItemTypeImpl) this._superType;
		LogicalWorkspaceTransactionListener[] ret = workspaceLogiqueCopyListeners;
		while (localSuperIT != null) {
			if (localSuperIT.workspaceLogiqueCopyListeners != null) {
				ret = ArraysUtil.addList(LogicalWorkspaceTransactionListener.class, ret,
						localSuperIT.workspaceLogiqueCopyListeners);
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
}
