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
import fr.imag.adele.cadse.core.CadseRootCST;
import fr.imag.adele.cadse.core.CadseRuntime;
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
public class ItemTypeImpl extends ItemImpl implements ItemType {

	/**
	 * old string
	 * 
	 * @deprecated use {@link CadseRootCST#ITEM_TYPE_at_QUALIFIED_NAME_}
	 */
	@Deprecated
	static final String							UNIQUE_NAME_KEY				= "#unique-name";

	/**
	 * old string
	 * 
	 * @deprecated use {@link CadseRootCST#ITEM_TYPE_at_DISPLAY_NAME_}
	 */
	@Deprecated
	static final String							DISPLAY_NAME_KEY			= "#display-name";

	/**
	 * old string
	 * 
	 * @deprecated use {@link CadseRootCST#ITEM_TYPE_at_NAME_}
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
	private int									intId;

	/** The super type. */
	ItemTypeImpl								superType;

	/** The sub types. */
	private List<ItemTypeImpl>					subTypes;

	IAttributeType<?>[]							attributesDefinitions		= null;

	/** The attribute types. */
	private Map<String, IAttributeType<?>>		__cache_attributeTypes		= null;

	/** The outgoings lt. */
	private LinkType[]							outgoingsLT;

	/** The incomings lt. */
	private LinkType[]							incomingsLT;

	/** The has content. */
	private boolean								fHasContent;												// contenu

	/** The kind. */
	private int									kind;

	/** The item manager. */
	private IItemManager						itemManager					= null;

	/** The space keytype. */
	private SpaceKeyType						spaceKeytype				= null;

	/** The display name. */
	private String								displayName;

	/** The action contributors. */
	IActionContributor[]						actionContributors			= null;
	// cache
	/** The __action contributors. */
	IActionContributor[]						__actionContributors		= null;

	/** The creation pages. */
	IPageFactory[]								creationPagesFactories		= null;

	/** The modification pages. */
	IPageFactory[]								modificationPagesFactories	= null;
	// cache
	/** The __creation pages. */
	IPageFactory[]								__creationPages				= null;

	// IPage[] pages = null;
	IPage2[]									creationPages				= null;
	IPage2[]									modificationPages			= null;

	// // cache
	/** The __modification pages. */
	IPageFactory[]								__modificationPages			= null;

	/** The icon. */
	private URL									icon;

	/** The clazz action. */
	private Class<? extends IActionPage>		clazzAction;

	/** The default short name action. */
	private String								defaultShortNameAction;

	private String								cadseName					= NO_VALUE_STRING;

	private String								packageName					= NO_VALUE_STRING;

	private IItemFactory						itemFactory;

	/**
	 * implementation of extension ...
	 */
	private ItemTypeImpl[]						extendedBy;

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.ItemImpl#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		if (displayName == null) {
			return super.getDisplayName();
		}
		return displayName;
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

		this.superType = superType;
		this.__cache_attributeTypes = null;
		this.incomingsLT = null;
		this.outgoingsLT = null;
		this.fHasContent = hasContent;
		this.kind = 0;
		this.displayName = displayName == null ? shortname : displayName;
		this.kind = 0;
		if (isAbstract) {
			kind |= ItemType.ABSTRACT;
		}
		this.intId = intId;
		subTypes = null;
		if (this.superType != null) {
			this.superType.addSubItemType(this);
		}
		this._state = ItemState.CREATED;
		setHasShortNameAttribute(true);
		setHasUniqueNameAttribute(true);
	}

	public ItemTypeImpl(LogicalWorkspace wl, ItemType it, ItemDelta desc) {
		super(wl, it, desc);
		this.__cache_attributeTypes = null;
		this.incomingsLT = null;
		this.outgoingsLT = null;
		this.kind = 0;

		subTypes = null;
		superType = null;

		Item superTypeLoaded = desc.getOutgoingItem(CadseRootCST.META_ITEM_TYPE_lt_SUPER_TYPE, true);
		if (superTypeLoaded != null) {
			superTypeLoaded = wl.getItem(superTypeLoaded.getId());
			if (superTypeLoaded instanceof ItemTypeImpl) {
				this.superType = (ItemTypeImpl) superTypeLoaded;
				if (superType != null) {
					superType.addSubItemType(this);
				}
			}
		}
	}

	@Override
	public void loadItem(IWorkingLoadingItems wl, ItemDelta itemOperation, IErrorCollector errorCollector)
			throws CadseException {
		super.loadItem(wl, itemOperation, errorCollector);

		this.fHasContent = itemOperation.getBooleanAttribut(CadseRootCST.META_ITEM_TYPE_at_HAS_CONTENT_, false);
		this.displayName = itemOperation.getStringAttribut(CadseRootCST.ITEM_TYPE_at_DISPLAY_NAME_, null);
		try {
			this.icon = itemOperation.getAttribute(CadseRootCST.META_ITEM_TYPE_at_ICON);
		} catch (Throwable e) {
			errorCollector.addError(itemOperation.getId(), "Cannot load the url attribute "
					+ itemOperation.getAttribute(CadseRootCST.META_ITEM_TYPE_at_ICON_));
		}
	}

	/**
	 * Adds the sub item type.
	 * 
	 * @param sType
	 *            the sub item type
	 */
	void addSubItemType(ItemTypeImpl sType) {
		if (subTypes == null) {
			subTypes = new ArrayList<ItemTypeImpl>();
		}
		subTypes.add(sType);
	}

	/**
	 * Adds the sub item type.
	 * 
	 * @param sType
	 *            the s type
	 */
	void removeSubItemType(ItemTypeImpl sType) {
		if (subTypes == null) {
			return;
		}
		subTypes.remove(sType);
	}

	/**
	 * Gets the int id.
	 * 
	 * @return the int id
	 */
	public int getIntID() {
		return intId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#getItemManager()
	 */
	public IItemManager getItemManager() {
		if (itemManager == null && superType != null) {
			return superType.getItemManager();
		}
		if (itemManager == null && this != CadseRootCST.ITEM_TYPE) {
			return CadseRootCST.ITEM_TYPE.getItemManager();
		}
		return itemManager;
	}

	/**
	 * Sets the item manager.
	 * 
	 * @param itemManager
	 *            the new item manager
	 */
	public void setItemManager(IItemManager itemManager) {
		this.itemManager = itemManager;
	}

	@Override
	public Link commitLoadCreateLink(LinkType lt, Item destination) throws CadseException {
		if (lt == CadseRootCST.META_ITEM_TYPE_lt_ATTRIBUTES_DEFINITION) {
			if (destination.getType() == CadseRootCST.UNRESOLVED_ATTRIBUTE_TYPE) {
				return null;
			}
			if (destination.getType() == CadseRootCST.LINK_DEFINITION_ATTIBUTE_TYPE) {
				if (!(destination instanceof LinkType)) {
					throw new CadseException("Destination is not a LinkType : {0}", destination
							.getQualifiedDisplayName());
				}
				LinkType atlt = (LinkType) destination;
				if (!this.m_outgoings.contains(atlt)) {
					this.m_outgoings.add(atlt);
				}
				if (lt.isComposition()) {
					this.kind |= COMPOSITE;
				}
				resetOutgoingLinkType();
			}

			if (!(destination instanceof IAttributeType)) {
				throw new CadseException("Destination is not an IAttributeType : {0}", destination
						.getQualifiedDisplayName());
			}
			return _addAttributeType((IAttributeType<?>) destination);
		}
		if (lt == CadseRootCST.META_ITEM_TYPE_lt_SUPER_TYPE) {
			if (isStatic() && superType != null) {
				throw new CadseException("Read only");
			}
			if (LinkPathUtil.checkNonCicular(destination, this, CadseRootCST.META_ITEM_TYPE_lt_SUPER_TYPE)) {
				throw new CadseException("Circular link");
			}
			// allready set in constructor
			// superType = (ItemTypeImpl) destination;
			// if (superType != null) {
			// superType.addSubItemType(this);
			// }
			return new ReflectLink(lt, this, destination, 0);
		}
		if (lt == CadseRootCST.META_ITEM_TYPE_lt_CREATION_PAGES) {
			this.creationPages = ArraysUtil.add(IPage2.class, this.creationPages, (IPage2) destination);
			resetCreationPages();
			return new ReflectLink(lt, this, destination, this.creationPages.length - 1);
		}
		if (lt == CadseRootCST.META_ITEM_TYPE_lt_MODIFICATION_PAGES) {
			this.modificationPages = ArraysUtil.add(IPage2.class, this.modificationPages, (IPage2) destination);
			resetModificationPages();
			return new ReflectLink(lt, this, destination, this.modificationPages.length - 1);
		}
		if (lt == CadseRootCST.META_ITEM_TYPE_lt_SUB_TYPES) {
		}

		if (lt == CadseRootCST.META_ITEM_TYPE_lt_META_LINK_TYPE) {

		}

		return super.commitLoadCreateLink(lt, destination);
	}

	@Override
	public synchronized void removeOutgoingLink(Link link, boolean notifie) {
		LinkType lt = link.getLinkType();
		Item destination = link.getDestination();

		if (lt == CadseRootCST.META_ITEM_TYPE_lt_ATTRIBUTES_DEFINITION && destination.isResolved()) {
			removeAttributeType((IAttributeType<?>) destination);
			if (destination.getType() == CadseRootCST.LINK_DEFINITION_ATTIBUTE_TYPE) {
				LinkType atlt = (LinkType) destination;
				this.m_outgoings.remove(atlt);
				resetOutgoingLinkType();
				if (atlt.isComposition()) {
					for (LinkType outgoing_lt : getOugoingLinkTypes()) {
						if (outgoing_lt.isComposite()) {
							return;
						}
					}
					this.kind &= ~COMPOSITE;
				}
			}
			return;
		}
		if (lt == CadseRootCST.META_ITEM_TYPE_lt_SUPER_TYPE && destination.isResolved()) {
			if (isStatic() && superType != null) {
				throw new CadseIllegalArgumentException("Read only");
			}
			if (LinkPathUtil.checkNonCicular(destination, this, CadseRootCST.META_ITEM_TYPE_lt_SUPER_TYPE)) {
				throw new CadseIllegalArgumentException("Circular link");
			}

			if (destination == superType) {
				if (superType != null) {
					superType.removeSubItemType(this);
				}
				superType = null;
			}
			return;
		}
		if (lt == CadseRootCST.META_ITEM_TYPE_lt_CREATION_PAGES && destination.isResolved()) {
			this.creationPages = ArraysUtil.remove(IPage2.class, this.creationPages, (IPage2) destination);
			resetCreationPages();
			return;
		}
		if (lt == CadseRootCST.META_ITEM_TYPE_lt_MODIFICATION_PAGES && destination.isResolved()) {
			this.modificationPages = ArraysUtil.remove(IPage2.class, this.modificationPages, (IPage2) destination);
			resetModificationPages();
			return;
		}
		if (lt == CadseRootCST.META_ITEM_TYPE_lt_SUB_TYPES) {
			return;
		}

		if (lt == CadseRootCST.META_ITEM_TYPE_lt_META_LINK_TYPE) {
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
		if (superType != null) {
			ret = (LinkType) superType.getAttributeType(name, false);
			if (ret != null) {
				// this.outgoingsLT.add(ret);
				// addAttributeType(ret);
				if (ret.isComposition()) {
					this.kind |= COMPOSITE;
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
			this.kind |= COMPOSITE;
		}
		resetOutgoingLinkType();

		addInconmmingLink(ret, ret.getDestination(), CadseRootCST.LINK_DEFINITION_ATTIBUTE_TYPE_lt_DESTINATION);
		addInconmmingLink(ret, ret.getInverse(), CadseRootCST.LINK_DEFINITION_ATTIBUTE_TYPE_lt_INVERSE);
		addInconmmingLink(ret, ret.getSource(), CadseRootCST.LINK_DEFINITION_ATTIBUTE_TYPE_lt_SOURCE);
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
			for (LinkType lt : getOugoingLinkTypes()) {
				if (lt.isComposite()) {
					return;
				}
			}

			this.kind &= ~COMPOSITE;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.ItemImpl#addIncomingLink(fr.imag.adele.cadse.core.Link)
	 */
	@Override
	public synchronized void addIncomingLink(Link link, boolean notifie) {
		if (link.getLinkType() == CadseCore.mLT) {
			resetIncomingLinkType();
		}
		super.addIncomingLink(link, notifie);
	}

	@Override
	public synchronized void removeIncomingLink(Link link, boolean notifie) {
		if (link.getLinkType() == CadseCore.mLT) {
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
		IAttributeType<? extends Object> a = getAttributeType(name, false);
		if (a instanceof LinkType) {
			return (LinkType) a;
		}
		for (Iterator<LinkType> outgoers = getOutgoingLinkTypes().iterator(); outgoers.hasNext();) {
			LinkType lt = outgoers.next();
			if (lt.getName().equals(name)) {
				return lt;
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#getOutgoingLinkType(fr.imag.adele.cadse.core.ItemType,
	 *      java.lang.String)
	 */
	public LinkType getOutgoingLinkType(ItemType destination, String name) {
		Iterator<LinkType> iter = getOutgoingLinkTypes().iterator();
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
		Iterator<LinkType> iter = getOutgoingLinkTypes().iterator();
		while (iter.hasNext()) {
			LinkType lt = iter.next();
			if (lt.getDestination().equals(dest) && (lt.getKind() == kind)) {
				return lt;
			}
		}
		return null;
	}

	public List<LinkType> getOugoingLinkTypes() {
		return getOutgoingLinkTypes();
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
		for (Iterator incomers = getIncomingLinkTypes().iterator(); incomers.hasNext();) {
			LinkType lt = (LinkType) incomers.next();
			if (lt.getName().equals(name)) {
				return lt;
			}
		}

		return null;
	}

	/**
	 * Get all outgoing link types.
	 * 
	 * @return an unmodifiable list all outgoing link types.
	 */
	public List<LinkType> getOutgoingLinkTypes() {
		if (outgoingsLT == null) {
			computeOutgoingLinkTypes();
		}
		return Arrays.asList(outgoingsLT);
	}

	/**
	 * Get all owned outgoing link types, not hierarchical.
	 * 
	 * @return an unmodifiable list all owned outgoing link types.
	 */
	public List<LinkType> getOwnerOutgoingLinkTypes() {
		List<LinkType> ret = new ArrayList<LinkType>();

		for (Link l : this.m_outgoings) {
			if (l.getLinkType() == CadseCore.mLT) {
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
		if (superType != null) {
			ret.addAll(superType.getOutgoingLinkTypes());
		}
		for (Link l : this.m_outgoings) {
			if (l.getLinkType() == CadseCore.mLT) {
				ret.add((LinkType) l);
			}
		}
		this.outgoingsLT = ret.toArray(new LinkType[ret.size()]);
	}

	/**
	 * Get all incoming link types.
	 * 
	 * @return an unmodifiable list all incoming link types.
	 */
	public List<LinkType> getIncomingLinkTypes() {
		if (incomingsLT == null) {
			computeIncomingLinkTypes();
		}
		return Arrays.asList(incomingsLT);
	}

	/**
	 * Compute incoming link types.
	 */
	private void computeIncomingLinkTypes() {
		List<LinkType> ret = new ArrayList<LinkType>();
		if (superType != null) {
			ret.addAll(superType.getIncomingLinkTypes());
		}
		for (Link l : this._incomings) {
			if (l.getLinkType() == CadseCore.mLT) {
				ret.add((LinkType) l);
			}
		}
		this.incomingsLT = ret.toArray(new LinkType[ret.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#hasContent()
	 */
	public boolean hasContent() {
		return fHasContent;
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
		return getId() + " " + getName() + (fHasContent ? (Messages.has_content) : Messages.no_content);
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
		for (Iterator outgoers = getOugoingLinkTypes().iterator(); outgoers.hasNext();) {
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
			kind |= UNIQUE_NAME;
		} else {
			kind &= ~UNIQUE_NAME;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#hasUniqueNameAttribute()
	 */
	@Override
	public boolean hasUniqueNameAttribute() {
		return (kind & UNIQUE_NAME) != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#setHasShortNameAttribute(boolean)
	 */
	public void setHasShortNameAttribute(boolean val) {
		if (val) {
			kind |= SHORT_NAME;
		} else {
			kind &= ~SHORT_NAME;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#hasShortNameAttribute()
	 */
	public boolean hasShortNameAttribute() {
		return (kind & SHORT_NAME) != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#setRootElement(boolean)
	 */
	public void setRootElement(boolean val) {
		if (val) {
			kind |= ROOT_ELEMENT;
		} else {
			kind &= ~ROOT_ELEMENT;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#isRootElement()
	 */
	public boolean isRootElement() {
		return (kind & ROOT_ELEMENT) != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.AbstractItem#isComposite()
	 */
	@Override
	public boolean isComposite() {
		if ((kind & COMPOSITE) != 0) {
			return true;
		}
		if (superType != null) {
			if (superType.isComposite()) {
				this.kind |= COMPOSITE;
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
		return (kind & ItemType.ABSTRACT) != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#isPartType()
	 */
	public boolean isPartType() {

		for (LinkType lt : getIncomingLinkTypes()) {
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
					throw new CadseException(Messages.error_linktype_more_one_link_found, typeParent.getId(), getId());
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
		return subTypes == null ? NO_SUB_TYPES : subTypes.toArray(new ItemType[subTypes.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#getSuperType()
	 */
	public ItemType getSuperType() {
		return this.superType;
	}

	/**
	 * Est-ce que moi je suis le super type de it
	 * 
	 * @param it
	 *            is an subtype of this
	 * @see fr.imag.adele.cadse.core.ItemType#isSuperTypeOf(fr.imag.adele.cadse.core.ItemType)
	 */
	public boolean isSuperTypeOf(ItemType it) {
		if (it == null) {
			return false;
		}
		ItemType super_it = it;
		while ((super_it = super_it.getSuperType()) != null) {
			if (super_it == this) {
				return true;
			}
		}
		return false;
	}

	@Override
	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {
		if (CadseRootCST.ITEM_TYPE_at_DISPLAY_NAME_ == type) {
			return (T) displayName;
		}
		if (CadseRootCST.META_ITEM_TYPE_at_CADSE_NAME_ == type) {
			return (T) getCadseName();
		}
		if (CadseRootCST.META_ITEM_TYPE_at_HAS_CONTENT_ == type) {
			return (T) Boolean.valueOf(this.fHasContent);
		}
		if (CadseRootCST.META_ITEM_TYPE_at_ROOT_ELEMENT_ == type) {
			return (T) Boolean.valueOf((this.kind & ItemType.ROOT_ELEMENT) != 0);
		}
		if (CadseRootCST.META_ITEM_TYPE_at_ICON_ == type) {
			return (T) icon;
		}
		if (CadseRootCST.META_ITEM_TYPE_at_ITEM_FACTORY_ == type) {
			return (T) itemFactory;
		}
		if (CadseRootCST.META_ITEM_TYPE_at_ITEM_MANAGER_ == type) {
			return (T) itemManager;
		}

		if (CadseRootCST.META_ITEM_TYPE_at_PACKAGE_NAME_ == type) {
			return (T) packageName;
		}
		return super.internalGetOwnerAttribute(type);
	}

	// /**
	// * Get an attribute value by key.
	// *
	// * @param key :
	// * key of attribute we want search.
	// *
	// * @return attribute value.
	// */
	// @SuppressWarnings("unchecked")
	// public <T> T getAttributeH(String key, boolean fromSuperIfNull) {
	//
	// if (CadseRootCST.ITEM_TYPE_at_DISPLAY_NAME.equals(key)) {
	// return (T) displayName;
	// }
	// if (CadseRootCST.META_ITEM_TYPE_at_CADSE_NAME.equals(key)) {
	// return (T) getCadseName();
	// }
	// if (CadseRootCST.META_ITEM_TYPE_at_HAS_CONTENT.equals(key)) {
	// return (T) Boolean.valueOf(this.fHasContent);
	// }
	// if (CadseRootCST.META_ITEM_TYPE_at_ROOT_ELEMENT.equals(key)) {
	// return (T) Boolean.valueOf((this.kind & ItemType.ROOT_ELEMENT) != 0);
	// }
	// if (CadseRootCST.META_ITEM_TYPE_at_ICON.equals(key)) {
	// if (icon != null)
	// return (T) icon;
	// }
	// if (CadseRootCST.META_ITEM_TYPE_at_ITEM_FACTORY.equals(key)) {
	// if (itemFactory != null)
	// return (T) itemFactory;
	// }
	// if (CadseRootCST.META_ITEM_TYPE_at_ITEM_MANAGER.equals(key)) {
	// return (T) itemManager;
	// }
	//
	// if (CadseRootCST.META_ITEM_TYPE_at_PACKAGE_NAME.equals(key)) {
	// return (T) packageName;
	// }
	// T ret = (T) super.getAttributeH(key, fromSuperIfNull);
	// if (fromSuperIfNull && ret == null && superType != null)
	// return (T) superType.getAttributeH(key, fromSuperIfNull);
	// return ret;
	// }

	@Override
	public Iterator<Item> propagateValue(IAttributeType<?> type) {
		if (superType != null) {
			return Collections.singletonList((Item) superType).iterator();
		}
		return null;
	}

	@Override
	public Iterator<Item> propagateValue(String key) {
		if (superType != null) {
			return Collections.singletonList((Item) superType).iterator();
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
		if (CadseRootCST.ITEM_TYPE_at_DISPLAY_NAME.equals(key)) {
			displayName = Convert.toString(value);
			return true;
		}
		if (CadseRootCST.META_ITEM_TYPE_at_CADSE_NAME.equals(key)) {
			if (value == null) {
				value = NO_VALUE_STRING;
			}
			cadseName = Convert.toString(value);
			return true;
		}
		if (CadseRootCST.META_ITEM_TYPE_at_HAS_CONTENT.equals(key)) {
			this.fHasContent = Convert.toBoolean(value);
			return true;
		}
		if (CadseRootCST.META_ITEM_TYPE_at_ROOT_ELEMENT.equals(key)) {
			if (value == null) {
				value = type.getDefaultValue();
			}
			setRootElement(Convert.toBoolean(value));
			return true;
		}
		if (CadseRootCST.META_ITEM_TYPE_at_PACKAGE_NAME.equals(key)) {
			if (value == null) {
				value = NO_VALUE_STRING;
			}
			setPackageName(Convert.toString(value));
			return true;
		}
		if (CadseRootCST.META_ITEM_TYPE_at_ICON.equals(key)) {
			try {
				icon = Convert.toURL(value);
				return true;
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (CadseRootCST.META_ITEM_TYPE_at_ITEM_FACTORY.equals(key)) {
			if (value instanceof String) {
				// && (value.toString().length() == 0) {
				return false;
			}
			itemFactory = (IItemFactory) value;
			return true;
		}
		if (CadseRootCST.META_ITEM_TYPE_at_ITEM_MANAGER.equals(key)) {
			if (value instanceof String) {
				// && (value.toString().length() == 0) {
				return false;
			}
			itemManager = (IItemManager) value;
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
		int index = ArraysUtil.indexOf(attributesDefinitions, type);
		if (index != -1) {
			return new ReflectLink(CadseRootCST.META_ITEM_TYPE_lt_ATTRIBUTES_DEFINITION, this, type, index);
		}
		;
		attributesDefinitions = ArraysUtil.add(IAttributeType.class, attributesDefinitions, type);
		type.setParent(this, CadseRootCST.META_ITEM_TYPE_lt_ATTRIBUTES_DEFINITION);
		index = attributesDefinitions.length - 1;
		if (CadseRootCST.META_ITEM_TYPE_lt_ATTRIBUTES_DEFINITION == null) {
			// le model root n'est pas encore charg�.
			return null;
		}
		return new ReflectLink(CadseRootCST.META_ITEM_TYPE_lt_ATTRIBUTES_DEFINITION, this, type, index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IAttributableType#addAttributeType(fr.imag.adele.cadse.core.IAttributeType)
	 */
	public <T> int removeAttributeType(IAttributeType<T> type) {
		int index = ArraysUtil.indexOf(attributesDefinitions, type);
		if (index == -1) {
			return index;
		}
		attributesDefinitions = ArraysUtil.remove(IAttributeType.class, attributesDefinitions, index);
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
		if (superType != null) {
			superType.getAllAttributeTypes(all, filter);
		}
		if (attributesDefinitions != null) {
			if (filter == null) {
				all.addAll(Arrays.asList(attributesDefinitions));
			} else {
				for (IAttributeType<?> at : attributesDefinitions) {
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
		if (attributesDefinitions != null) {
			for (IAttributeType<?> att : attributesDefinitions) {
				if (keepLastAttribute && all.containsKey(att.getName())) {
					continue;
				}
				if (filter == null || filter.accept(att)) {
					all.put(att.getName(), att);
				}
			}
		}

		if (superType != null) {
			superType.getAllAttributeTypes(all, keepLastAttribute, filter);
		}
	}

	public void getAllAttributeTypesKeys(Set<String> all, ItemFilter filter) {
		if (attributesDefinitions != null) {
			for (IAttributeType<?> att : attributesDefinitions) {
				if (filter == null || filter.accept(att)) {
					all.add(att.getName());
				}
			}
		}

		if (superType != null) {
			superType.getAllAttributeTypesKeys(all, filter);
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
			return CadseRootCST.ITEM_TYPE_at_NAME_;
		}
		if (UNIQUE_NAME_KEY.equals(name) || Item.ATTR_LONG_NAME.equals(name)) {
			return CadseRootCST.ITEM_TYPE_at_QUALIFIED_NAME_;
		}
		if (Item.IS_READ_ONLY_KEY.equals(name)) {
			return CadseRootCST.ITEM_TYPE_at_READ_ONLY_;
		}
		if (DISPLAY_NAME_KEY.equals(name)) {
			return CadseRootCST.ITEM_TYPE_at_DISPLAY_NAME_;
		}

		IAttributeType<?> ret = null;
		if (__cache_attributeTypes != null) {
			ret = __cache_attributeTypes.get(name);
			if (ret != null) {
				return ret;
			}
		}

		if (attributesDefinitions != null) {
			for (IAttributeType<?> att : attributesDefinitions) {
				if (att.getName().equals(name)) {
					if (__cache_attributeTypes == null) {
						__cache_attributeTypes = new HashMap<String, IAttributeType<?>>();
					}
					__cache_attributeTypes.put(name, att);
					return att;
				}
			}
		}
		if (ret == null && superType != null) {
			ret = superType.getAttributeType(name, false);
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
		if (getPartParent() instanceof CadseRuntime) {
			return (CadseRuntime) getPartParent();
		}
		return _wl.getCadseRuntime(getCadseName());
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
		actionContributors = ArraysUtil.add(IActionContributor.class, actionContributors, contributor);
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
		this.creationPagesFactories = ArraysUtil
				.addList(IPageFactory.class, this.creationPagesFactories, creationPages);
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
		this.modificationPagesFactories = ArraysUtil.addList(IPageFactory.class, this.modificationPagesFactories,
				modificationPages);
		resetModificationPages();
	}

	/**
	 * reset du cache de modification de pages.
	 */
	private void resetModificationPages() {
		__modificationPages = null;
		if (subTypes != null) {
			for (ItemTypeImpl subT : subTypes) {
				subT.resetModificationPages();

			}
		}
	}

	/**
	 * reset du cache de outgoing link type.
	 */
	private void resetOutgoingLinkType() {
		if (outgoingsLT == null) {
			return;
		}
		outgoingsLT = null;
		if (subTypes != null) {
			for (ItemTypeImpl subT : subTypes) {
				subT.resetOutgoingLinkType();

			}
		}
	}

	/**
	 * reset du cache de incoming link type.
	 */
	private void resetIncomingLinkType() {
		if (incomingsLT == null) {
			return;
		}
		incomingsLT = null;
		if (subTypes != null) {
			for (ItemTypeImpl subT : subTypes) {
				subT.resetIncomingLinkType();

			}
		}
	}

	/**
	 * reset du cache des pages de creation.
	 */
	private void resetCreationPages() {
		__creationPages = null;
		if (subTypes != null) {
			for (ItemTypeImpl subT : subTypes) {
				subT.resetCreationPages();

			}
		}
	}

	/**
	 * reset du cache des contributions.
	 */
	private void resetContributions() {
		if (__actionContributors == null) {
			return;
		}
		__actionContributors = null;
		if (subTypes != null) {
			for (ItemTypeImpl subT : subTypes) {
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
		this.clazzAction = clazz;
		this.defaultShortNameAction = defaultShortName;
	}

	/**
	 * return les actions contributions du propre du type. cf
	 * {@link #getAllActionContribution()};
	 * 
	 * @return the action contribution
	 */
	public IActionContributor[] getActionContribution() {
		return actionContributors == null ? EMPTY_ACTION_CONTRIBUTORS : actionContributors;
	}

	/**
	 * return toutes les actions contributions du type et des sous type ...
	 * 
	 * @return the all action contribution
	 */
	public IActionContributor[] getAllActionContribution() {
		if (__actionContributors == null) {
			if (superType != null) {
				if (actionContributors == null || actionContributors.length == 0) {
					__actionContributors = superType.getAllActionContribution();
				} else {
					IActionContributor[] super_a = superType.getAllActionContribution();
					if (super_a.length == 0) {
						__actionContributors = getActionContribution();
					} else {
						__actionContributors = ArraysUtil.merge(IActionContributor.class, super_a, actionContributors);
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
		return this.creationPagesFactories == null ? EMPTY_PAGE_FACTORIES : this.creationPagesFactories;
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
	void computegetGoodCreationPage(Map<String, IPageFactory> map, List<IPageFactory> list) {
		if (superType != null) {
			superType.computegetGoodCreationPage(map, list);
		}
		if (creationPagesFactories != null) {
			for (IPageFactory f : creationPagesFactories) {
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
		if (this.creationPages != null) {
			for (IPageFactory f : creationPages) {
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
		if (superType != null) {
			superType.computeGoodModificationPage(map, list);
		}
		if (modificationPagesFactories != null) {
			for (IPageFactory f : modificationPagesFactories) {
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
		if (this.modificationPages != null) {
			for (IPageFactory f : modificationPages) {
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
		return this.modificationPagesFactories == null ? EMPTY_PAGE_FACTORIES : this.modificationPagesFactories;
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
				if (createdPage != null) {
					createdPage.setParent(type, CadseRootCST.META_ITEM_TYPE_lt_ATTRIBUTES_DEFINITION);
					ret.add(createdPage);
				}
			} catch (Throwable e) {
				_wl.getCadseDomain().log("error", "Cannot create page " + pf[i], e);
			}
		}
		return ret.toArray(new PageImpl[ret.size()]);
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
		if (clazzAction == null) {
			if (defaultShortNameAction == null) {
				return new CreationAction(parent, type, lt);
			} else {
				return new CreationAction(parent, type, lt, defaultShortNameAction);
			}
		}
		Constructor<?> c = null;
		if (defaultShortNameAction != null) {
			try {
				c = clazzAction.getConstructor(Item.class, ItemType.class, LinkType.class, String.class);
				return (IActionPage) c.newInstance(parent, type, lt, defaultShortNameAction);
			} catch (NoSuchMethodException e) {

			} catch (IllegalArgumentException e) {
				throw new CadseException("Cannot create creation action {1} : {0}.", e, clazzAction, c);
			} catch (InstantiationException e) {
				throw new CadseException("Cannot create creation action {1} : {0}.", e, clazzAction, c);
			} catch (IllegalAccessException e) {
				throw new CadseException("Cannot create creation action {1} : {0}.", e, clazzAction, c);
			} catch (InvocationTargetException e) {
				throw new CadseException("Cannot create creation action {1} : {0}.", e, clazzAction, c);
			}
		}
		if (defaultShortNameAction == null) {
			try {
				c = clazzAction.getConstructor(Item.class, ItemType.class, LinkType.class);
				return (IActionPage) c.newInstance(parent, type, lt);
			} catch (NoSuchMethodException e) {

			} catch (IllegalArgumentException e) {
				throw new CadseException("Cannot create creation action {1} : {0}.", e, clazzAction, c);
			} catch (InstantiationException e) {
				throw new CadseException("Cannot create creation action {1} : {0}.", e, clazzAction, c);
			} catch (IllegalAccessException e) {
				throw new CadseException("Cannot create creation action {1} : {0}.", e, clazzAction, c);
			} catch (InvocationTargetException e) {
				throw new CadseException("Cannot create creation action {1} : {0}.", e, clazzAction, c);
			}
		}
		try {
			return clazzAction.newInstance();
		} catch (InstantiationException e) {
			throw new CadseException("Cannot create creation action {1} with default constructor.", e, clazzAction);
		} catch (IllegalAccessException e) {
			throw new CadseException("Cannot create creation action {1} with default constructor.", e, clazzAction);
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
		if (icon == null && superType != null) {
			return superType.getImage();
		}
		return icon;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#setIcon(java.net.URL)
	 */
	public void setIcon(URL url) {
		icon = url;
		Logger.getLogger("icon").log(Level.INFO, "set icon to " + url + " of ItemType " + getDisplayName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#setSpaceKeyType(fr.imag.adele.cadse.core.key.SpaceKeyType)
	 */
	public void setSpaceKeyType(SpaceKeyType spaceKeytype) {
		this.spaceKeytype = spaceKeytype;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#getSpaceKeyType()
	 */
	public SpaceKeyType getSpaceKeyType() {
		if (this.spaceKeytype == null && superType != null) {
			return superType.getSpaceKeyType();
		}
		return spaceKeytype;
	}

	public <T> T getApdapterManager(Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCadseName() {
		if (getPartParent() == null) {
			return cadseName;
		}
		return getPartParent().getName();
	}

	public void setCadseName(String cadseName) {
		if (getPartParent() != null) {
			return;
		}

		if (cadseName == null) {
			this.cadseName = NO_VALUE_STRING;
		} else {
			this.cadseName = cadseName;
		}
	}

	@Override
	protected void collectOutgoingLinks(LinkType linkType, CollectedReflectLink ret) {
		if (linkType == CadseRootCST.META_ITEM_TYPE_lt_SUPER_TYPE) {
			ret.addOutgoing(CadseRootCST.META_ITEM_TYPE_lt_SUPER_TYPE, getSuperType());
		}
		if (linkType == CadseRootCST.META_ITEM_TYPE_lt_CADSE_RUNTIME) {
			ret.addOutgoing(CadseRootCST.META_ITEM_TYPE_lt_CADSE_RUNTIME, getPartParent());
		}
		if (linkType == CadseRootCST.META_ITEM_TYPE_lt_SUB_TYPES) {
			ret.addOutgoing(CadseRootCST.META_ITEM_TYPE_lt_SUB_TYPES, this.subTypes);
		}
		if (linkType == CadseRootCST.META_ITEM_TYPE_lt_ATTRIBUTES_DEFINITION) {
			ret.addOutgoing(CadseRootCST.META_ITEM_TYPE_lt_ATTRIBUTES_DEFINITION, attributesDefinitions);
		}
		if (linkType == CadseRootCST.META_ITEM_TYPE_lt_CREATION_PAGES) {
			ret.addOutgoing(CadseRootCST.META_ITEM_TYPE_lt_CREATION_PAGES, creationPages);
		}
		if (linkType == CadseRootCST.META_ITEM_TYPE_lt_MODIFICATION_PAGES) {
			ret.addOutgoing(CadseRootCST.META_ITEM_TYPE_lt_MODIFICATION_PAGES, modificationPages);
		}
		super.collectOutgoingLinks(linkType, ret);
	}

	public IItemFactory getItemFactory() {
		if (itemFactory == null && this.itemManager instanceof IItemFactory) {
			itemFactory = (IItemFactory) itemManager;
		}
		if (itemFactory == null && superType != null) {
			return superType.getItemFactory();
		}
		if (itemFactory == null) {
			return ItemFactory.SINGLETON;
		}
		return itemFactory;
	}

	public void setItemFactory(IItemFactory factory) {
		itemFactory = factory;
	}

	public PageImpl getFirstCreatedPage() {
		if (creationPages == null || creationPages.length == 0) {
			return null;
		}
		return creationPages[0];
	}

	public PageImpl getFirstModificationPage() {
		if (modificationPages == null || modificationPages.length == 0) {
			return null;
		}
		return modificationPages[0];
	}

	public void setPackageName(String packageName) {
		if (packageName == null) {
			this.packageName = NO_VALUE_STRING;
		} else {
			this.packageName = packageName;
		}
	}

	public String getPackageName() {
		return packageName;
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
		ItemTypeImpl localSuperIT = this.superType;
		LogicalWorkspaceTransactionListener[] ret = workspaceLogiqueCopyListeners;
		while (localSuperIT != null) {
			if (localSuperIT.workspaceLogiqueCopyListeners != null) {
				ret = ArraysUtil.addList(LogicalWorkspaceTransactionListener.class, ret,
						localSuperIT.workspaceLogiqueCopyListeners);
			}
			localSuperIT = localSuperIT.superType;
		}
		return ret;

	}
}
