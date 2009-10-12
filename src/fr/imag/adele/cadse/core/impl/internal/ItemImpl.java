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

import java.text.MessageFormat;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import fr.imag.adele.cadse.core.CadseDomain;
import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.ChangeID;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.DerivedLink;
import fr.imag.adele.cadse.core.DerivedLinkDescription;
import fr.imag.adele.cadse.core.DerivedLinkType;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemDescription;
import fr.imag.adele.cadse.core.ItemDescriptionRef;
import fr.imag.adele.cadse.core.ItemState;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.Messages;
import fr.imag.adele.cadse.core.WSModelState;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.delta.ItemDelta;
import fr.imag.adele.cadse.core.delta.LinkDelta;
import fr.imag.adele.cadse.core.impl.CadseIllegalArgumentException;
import fr.imag.adele.cadse.core.impl.CollectedReflectLink;
import fr.imag.adele.cadse.core.impl.ReflectLink;
import fr.imag.adele.cadse.core.internal.IWorkingLoadingItems;
import fr.imag.adele.cadse.core.util.IErrorCollector;
import fr.imag.adele.cadse.core.util.OrderWay;

/**
 * An item is an element of workspace. It has certain attributes composed by a
 * pair of key and of value. Key is a string, and value is an serialized object.
 * Each item has a type defined in workspace model. (See WorkspaceType and
 * ItemType). Item has two list "outgoings" and "incomings" which contain the
 * link comming to and going from item. Chaque Item a un type d�finit dans le
 * mod�le de workspace (voir WorkspaceType et ItemType).
 * 
 * @author nguyent
 * @version 6
 * @date 26/09/05
 */

public class ItemImpl extends AbstractItem implements Item {

	// /**
	// * The Class RenameItemChange.
	// */
	// public static class RenameItemChange extends Change {
	//
	// /** The item. */
	// ItemImpl item;
	//
	// /** The old short name. */
	// String oldShortName;
	//
	// /** The old unique name. */
	// String oldUniqueName;
	//
	// /** The unique name. */
	// String uniqueName;
	//
	// /** The modif short name. */
	// boolean modifShortName;
	//
	// /** The modif unique name. */
	// boolean modifUniqueName;
	//
	// /** The short name. */
	// String shortName;
	//
	// /** The old modif short name. */
	// boolean oldModifShortName;
	//
	// /** The old modif unique name. */
	// boolean oldModifUniqueName;
	//
	// /**
	// * Instantiates a new rename item change.
	// *
	// * @param item
	// * the item
	// * @param oldShortName
	// * the old short name
	// * @param oldUniqueName
	// * the old unique name
	// * @param shortName
	// * the short name
	// * @param uniqueName
	// * the unique name
	// * @param modifShortName
	// * the modif short name
	// * @param modifUniqueName
	// * the modif unique name
	// * @param oldModifShortName
	// * the old modif short name
	// * @param oldModifUniqueName
	// * the old modif unique name
	// */
	// public RenameItemChange(ItemImpl item, String oldShortName, String
	// oldUniqueName, String shortName,
	// String uniqueName, boolean modifShortName, boolean modifUniqueName,
	// boolean oldModifShortName,
	// boolean oldModifUniqueName) {
	// super();
	// this.item = item;
	// this.oldShortName = oldShortName;
	// this.oldUniqueName = oldUniqueName;
	// this.uniqueName = uniqueName;
	// this.shortName = shortName;
	// this.modifShortName = modifShortName;
	// this.modifUniqueName = modifUniqueName;
	// this.oldModifShortName = oldModifShortName;
	// this.oldModifUniqueName = oldModifUniqueName;
	// }
	//
	// /*
	// * (non-Javadoc)
	// *
	// * @see org.eclipse.ltk.core.refactoring.Change#getName()
	// */
	// @Override
	// public String getName() {
	// return "Rename Item to ("; //$NON-NLS-1$
	// };
	//
	// /*
	// * (non-Javadoc)
	// *
	// * @see org.eclipse.ltk.core.refactoring.Change#getModifiedElement()
	// */
	// @Override
	// public Object getModifiedElement() {
	// return item;
	// }
	//
	// /*
	// * (non-Javadoc)
	// *
	// * @see
	// org.eclipse.ltk.core.refactoring.Change#initializeValidationData(org.eclipse.core.runtime.IProgressMonitor)
	// */
	// @Override
	// public void initializeValidationData(IProgressMonitor pm) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// /*
	// * (non-Javadoc)
	// *
	// * @see
	// org.eclipse.ltk.core.refactoring.Change#isValid(org.eclipse.core.runtime.IProgressMonitor)
	// */
	// @Override
	// public RefactoringStatus isValid(IProgressMonitor pm) throws
	// CadseException, OperationCanceledException {
	// return new RefactoringStatus();
	// }
	//
	// /*
	// * (non-Javadoc)
	// *
	// * @see
	// org.eclipse.ltk.core.refactoring.Change#perform(org.eclipse.core.runtime.IProgressMonitor)
	// */
	// @Override
	// public Change perform(IProgressMonitor pm) throws CadseException {
	// item.getWorkspaceDomain().beginOperation("rename to " + shortName);
	// //$NON-NLS-1$
	// try {
	// if (modifShortName) {
	//
	// item.setShortName(shortName);
	// }
	// if (modifUniqueName) {
	// try {
	// item.setUniqueName(uniqueName);
	// } catch (CadseException e) {
	// throw new CadseException(new Status(IStatus.ERROR,
	// "CU.Workspace.Workspace", e.getMessage())); //$NON-NLS-1$
	// }
	// }
	// } finally {
	// item.getWorkspaceDomain().endOperation();
	// }
	// return new RenameItemChange(item, shortName, uniqueName, oldShortName,
	// oldUniqueName, oldModifShortName,
	// oldModifUniqueName, modifShortName, modifUniqueName);
	// };
	//
	// }

	/** The is valid. */
	private boolean				isValid		= true;	// by
	// default
	// is
	// true
	// at
	// fisrt
	// time.

	/** The outgoings. */
	protected ArrayList<Link>	m_outgoings;

	/** The attributes. */
	private Map<String, Object>	attributes;

	/** The composants. */
	Map<CompactUUID, Item>		_composants	= null;

	/** The derived links. */
	Set<DerivedLink>			_derivedLinks;



	private boolean				_isRecomputeComponants;

	/**
	 * Instantiates a new item impl.
	 * 
	 * @param wl
	 *            the wl
	 * @param it
	 *            the id
	 * @param _type
	 *            the type
	 * @param uniqueName
	 *            the unique name
	 * @param item
	 *            the short name
	 */
	public ItemImpl(LogicalWorkspace wl, CompactUUID id, ItemType it, String uniqueName, String shortName) {
		super(wl, id, it, uniqueName, shortName);
		// this.incomings = new ArrayList<Link>();
		this.m_outgoings = new ArrayList<Link>();
		this.attributes = new HashMap<String, Object>();
		this._state = ItemState.NOT_IN_WORKSPACE;

		setIsStatic(false);
	}

	public ItemImpl(LogicalWorkspace wl, CompactUUID id, ItemType type, String uniqueName, String shortName,
			Item parent, LinkType lt) throws CadseException {
		super(wl, id, type, uniqueName, shortName);
		// this.incomings = new ArrayList<Link>();
		this.m_outgoings = new ArrayList<Link>();
		this.attributes = new HashMap<String, Object>();
		this._state = ItemState.NOT_IN_WORKSPACE;

		setIsStatic(false);
		// remove this lines : use outgoing link instead
		// if (parent != null) {
		// setParentAndLinkType(parent, lt);
		// }
	}

	/**
	 * Instanciate a new item. <br/>
	 * 
	 * @param type :
	 *            type of item
	 * @param wl
	 *            the wl
	 * 
	 * @NOTE: This constructor is package visible only, as it SHOULD only be
	 *        used by Workspace in order to validate global constraints when
	 *        creating a new item in workspace.
	 */
	protected ItemImpl(LogicalWorkspaceImpl wl, ItemType type) {
		super(wl, type);
		// this.incomings = new ArrayList<Link>();
		this.m_outgoings = new ArrayList<Link>();
		this.attributes = new HashMap<String, Object>();
		this._state = ItemState.NOT_IN_WORKSPACE;

	}

	public ItemImpl(LogicalWorkspace wl, ItemType itemtype, ItemDelta desc) {
		super(wl, itemtype, desc);
		assert wl != null && itemtype != null && desc != null;
		this.isValid = desc.isValid();

		this.m_outgoings = new ArrayList<Link>();
		this.attributes = new HashMap<String, Object>();
		this._state = ItemState.MODIFING;

		this._isRecomputeComponants = true;
		if (desc.isReadOnly()) {
			setReadOnly(desc.isReadOnly());
		}
		setModified(true);
	}

	@Override
	public void finishLoad() {
		this._state = ItemState.CREATED;
	}

	@Override
	public void loadItem(IWorkingLoadingItems wl, ItemDelta itemOperation, IErrorCollector errorCollector)
			throws CadseException {
		CadseGCST.ITEM_lt_MODIFIED_ATTRIBUTES.setIsNatif(true);
		
		Accessor.loadAttributes(this, itemOperation, errorCollector);
		for (LinkDelta ldesc : itemOperation.getOutgoingLinkOperations()) {
			try {
				if (ldesc.isDerived() || ldesc.isDeleted()) {
					continue;
				}
				if (!ldesc.isLoaded() && !ldesc.isAdded()) {
					continue;
				}
				Item dest = wl.loadItem(ldesc.getDestination());

				if (ldesc.getLinkType() == null) {
					errorCollector.addError(this, "Cannot load link " + ldesc);
				}
				

				LinkType lt = _type.getOutgoingLinkType(ldesc.getLinkTypeName());
				if (lt == null) {
					lt = wl.getLogicalWorkspace().createUnresolvedLinkType(ldesc.getLinkTypeName(), getType(),
							dest.getType());
					errorCollector.addError(this, MessageFormat.format(Messages.error_cannot_create_link, ldesc
							.getLinkType(), dest.getName()));
					if (lt == null) {
						continue;
					}
				}
				if (lt == CadseGCST.ITEM_lt_CONTENTS || lt == CadseGCST.ITEM_lt_PARENT) continue;
				
				
				if (lt.isNatif() && dest.isResolved()) {
					Link goodlink = commitLoadCreateLink(lt, dest);
					if (goodlink != null) {
						dest.addIncomingLink(goodlink, false);
					}
					continue;
				}
				if (lt.isNatif() && !dest.isResolved()) {
					Item goodDest = wl.getItem(dest.getId());
					if (goodDest != null && goodDest.isResolved()) {
						Link goodlink = commitLoadCreateLink(lt, goodDest);
						if (goodlink != null) {
							dest.addIncomingLink(goodlink, false);
						}
						continue;
					}
					if (lt == CadseGCST.ITEM_lt_MODIFIED_ATTRIBUTES) {
						continue;
					}
					errorCollector.addError(this, lt.getSource().getName() + "::" + lt.getName()
							+ " is natif and destination is not resolved... " + new ItemDescriptionRef(dest));
				}

				if (lt.getMax() == 1 && getOutgoingLink(lt) != null) {
					continue;
				}
				// create a link
				LinkImpl l = new LinkImpl(this, lt, dest, true);
				// add link l into the list "outgoings" of source "this".
				if (!m_outgoings.contains(l)) {
					m_outgoings.add(l);
				}

			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {
		if (type == CadseGCST.ITEM_at_NAME_) {
			return (T) getName();
		}
		if (type == CadseGCST.ITEM_at_QUALIFIED_NAME_) {
			return (T) getQualifiedName();
		}
		if (type == CadseGCST.ITEM_at_DISPLAY_NAME_) {
			return (T) getDisplayName();
		}
		/*if (CadseGCST.ITEM_at_PARENT_ITEM_ID_ == type) {
			return (T) internalGetGenericOwnerAttribute(CadseGCST.ITEM_at_PARENT_ITEM_ID_);
		}
		if (CadseGCST.ITEM_at_PARENT_ITEM_TYPE_ID_ == type) {
			return (T) internalGetGenericOwnerAttribute(CadseGCST.ITEM_at_PARENT_ITEM_TYPE_ID_);
		}*/
		return super.internalGetOwnerAttribute(type);
	}

	@Override
	public <T> T internalGetGenericOwnerAttribute(String key) {
		IAttributeType<? extends Object> attType = getType().getAttributeType(key, false);
		if (attType == null) return null;
		if (attType instanceof LinkType) {
			return (T) getOutgoingLinks((LinkType) attType);
		}
		return (T) attributes.get(key);
	}

	@Override
	public <T> T internalGetGenericOwnerAttribute(IAttributeType<T> type) {
		if (type instanceof LinkType) {
			return (T) getOutgoingLinks((LinkType) type);
		}
		return (T) attributes.get(type.getName());
	}

	/**
	 * Get keys all attributes . jamais null
	 * 
	 * @return keys all attributes.
	 */
	@Override
	public String[] getAttributeKeys() {
		HashSet<String> returnKeys = new HashSet<String>();
		returnKeys.addAll(attributes.keySet());
		ItemType it = getType();
		if (it != null) {
			it.getAllAttributeTypesKeys(returnKeys, new FilterOutLinkType());
		}
		return returnKeys.toArray(new String[returnKeys.size()]);
	}

	@Override
	public List<Link> getOutgoingLinks() {
		return getOutgoingLinks(null);
	}

	public List<Link> getOwnerOutgoingLinks() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Get <tt>outgoings</tt> links by link type.<br/><br/>
	 * 
	 * @param lt :
	 *            link type used as filter to seek.
	 * 
	 * @return list outgoing links have the same type. jamais null
	 * 
	 * @exception IllegalArgumentException:
	 *                Link type is null.<br/> IllegalArgumentException: Link
	 *                type <tt>$name</tt> is not selected in this workspace
	 *                type. <br/><br/>
	 */
	@Override
	public List<Link> getOutgoingLinks(LinkType lt) {
		CollectedReflectLink ret = new CollectedReflectLink(this);
		if (lt == null) {
			List<LinkType> lts = getType().getOutgoingLinkTypes();
			for (LinkType linkType : lts) {
				collectOutgoingLinks(linkType, ret);
			}
			ret.addAll(m_outgoings);
		} else {
			collectOutgoingLinks(lt, ret);
			filterByLinkType(m_outgoings, lt, ret);
		}
		return ret;
	}

	/**
	 * Get <tt>incomings</tt> links by type. jamais null
	 * 
	 * @param lt :
	 *            link type used as filter to seek.
	 * 
	 * @return list incoming links have the same type.
	 * 
	 * @exception IllegalArgumentException:
	 *                Link type is null.<br/> IllegalArgumentException: Link
	 *                type <tt>$name</tt> is not selected in this workspace
	 *                type. <br/><br/>
	 */
	@Override
	public List<Link> getIncomingLinks(LinkType lt) {
		return getLinksByType(_incomings, lt);
	}

	@Override
	public Link getIncomingLink(LinkType lt, CompactUUID srcId) {
		List<Link> links = _incomings;
		for (Link l : links) {
			if (l.getLinkType() == lt && l.getSourceId().equals(srcId)) {
				return l;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#getOutgoingLink(fr.imag.adele.cadse.core.LinkType)
	 */
	@Override
	public Link getOutgoingLink(LinkType lt) {
		preconditions_getLink(lt);
		if (lt.getMax() != 1) {
			throw new CadseIllegalArgumentException(Messages.error_maximum_cardinality_must_be_one, lt.getName());
		}
		
		List<Link> ret = getOutgoingLinks(lt);
		
		if (ret.size() >= 1) {
			return ret.get(0);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#itemHasContent()
	 */
	@Override
	public boolean itemHasContent() {
		return _type.hasContent() && _type.getItemManager() != null && _type.getItemManager().hasContent(this);
	}

	// /**
	// * Preconsitions_delete item.
	// *
	// * @throws IllegalArgumentException:
	// * It is not possible to delete item <tt>$this.getId()</tt>
	// * because of it has an incomming link with a read only item
	// * <tt>$source.id</tt> <br/>
	// *
	// * @contraints: - 1. The sources of all incomming links are not in read
	// only
	// * state. <br/>
	// * @OCL: <b>pre:</b> not <tt>self.from</tt>->exist(<tt>l</tt> |
	// * <tt>l.source.isReadOnly = true </tt><br/>
	// */
	// private void preconsitions_deleteItem() {
	// if (getState() == ItemState.ORPHAN)
	// throw new CadseIllegalArgumentException(
	// Messages.error_not_possible_delete_orphan, getId()); //$NON-NLS-1$
	//
	// for (Link l : this.getIncomingLinks()) {
	// Item source = l.getSource();
	// if (source.isReadOnly()) {
	// throw new CadseIllegalArgumentException(
	// "It is not possible to delete item {0}" //$NON-NLS-1$
	// + " because of it has an incomming link with a read only item {1}.",
	// //$NON-NLS-1$
	// getId(), source.getId());
	//
	// }
	// }
	// }

	/**
	 * Supprimer un lien dans la liste <tt>incomings</tt> de cet item.<br/>
	 * This method is called by method delete() of Link
	 * 
	 * @param link :
	 *            lien � supprimer.
	 */
	@Override
	public synchronized void removeIncomingLink(Link link, boolean notifie) {
		_incomings.remove(link);
		if (notifie) {
			_wl.getCadseDomain().notifieChangeEvent(ChangeID.UNRESOLVE_INCOMING_LINK, this, link);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.AbstractItem#addIncomingLink(fr.imag.adele.cadse.core.Link)
	 */
	@Override
	public synchronized void addIncomingLink(Link link, boolean notifie) {
		if (link.getLinkType() != null && link.getLinkType().isPart()) {
			setParent(link.getSource(), link.getLinkType());
		}
		super.addIncomingLink(link, notifie);
	}

	/**
	 * Supprimer un lien dans la liste <tt>outgoings</tt> de cet item. <br/>
	 * This method is called by method delete() of Link
	 * 
	 * @param link :
	 *            lien � supprimer.
	 */
	synchronized void removeOutgoingLink(Link link) {
		removeOutgoingLink(link, _state != ItemState.NOT_IN_WORKSPACE && _state != ItemState.MODIFING);
	}

	/**
	 * Supprimer un lien dans la liste <tt>outgoings</tt> de cet item. <br/>
	 * This method is called by method delete() of Link
	 * 
	 * @param link :
	 *            lien � supprimer.
	 */
	@Override
	public synchronized void removeOutgoingLink(Link link, boolean notifie) {
		m_outgoings.remove(link);
		if (notifie) {
			_wl.getCadseDomain().notifieChangeEvent(ChangeID.DELETE_OUTGOING_LINK, link);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#addOutgoingItem(fr.imag.adele.cadse.core.LinkType,
	 *      fr.imag.adele.cadse.core.Item)
	 */
	@Override
	public Link addOutgoingItem(LinkType linkType, Item destination) throws CadseException {
		return createLink(linkType, destination);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#removeOutgoingItem(fr.imag.adele.cadse.core.LinkType,
	 *      fr.imag.adele.cadse.core.Item)
	 */
	@Override
	public Link removeOutgoingItem(LinkType linkType, Item destination) throws CadseException {
		return Accessor.removeOutgoingItem(this, linkType, destination);
	}

	/**
	 * Set attribute for this item. If the value is null, remove the attribute
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @throws CadseException
	 */
	@Override
	public void setAttribute(String key, Object value) throws CadseException {
		_wl.setAttribute(this, key, value);
	}

	@Override
	public boolean commitSetAttribute(IAttributeType<?> type, String key, Object value) {
		if (CadseGCST.ITEM_at_NAME_ == type) {
			if (value == null) {
				value = NO_VALUE_STRING;
			}
			this._name = (String) value;
			return true;
		}
		if (CadseGCST.ITEM_at_QUALIFIED_NAME_ == type) {
			if (value == null) {
				value = NO_VALUE_STRING;
			}
			this._qualifiedName = (String) value;
			return true;
		}

		return super.commitSetAttribute(type, key, value);

	}

	@Override
	protected boolean commitGenericSetAttribute(IAttributeType<?> type, String key, Object value) {
		Object oldValue = attributes.get(key);
		boolean notifie = false;
		if (value == null) {
			if (attributes.containsKey(key)) {
				attributes.remove(key);
				if (!key.startsWith("#")) {
					notifie = true;
				}
			}
		} else {
			if (!(value.equals(oldValue))) {
				attributes.put(key, value);
				if (!key.startsWith("#")) {
					notifie = true;
				}
			}
		}
		return notifie;
	}

	/**
	 * Set status valid for this item.
	 * 
	 * @param valid :
	 *            un bool�en indique le status de cet item est valide ou pas.
	 */
	@Override
	public void setValid(boolean valid) {
		boolean oldvalue = isValid;
		this.isValid = valid;
		if (_state != ItemState.NOT_IN_WORKSPACE && _state != ItemState.MODIFING) {
			_wl.getCadseDomain().notifieChangeEvent(ChangeID.VALID, this, oldvalue, valid);
		}
	}

	/**
	 * Ask if this item is an orphan.
	 * 
	 * @return true if item is orphan, false if otherwise.
	 */
	@Override
	public boolean isOrphan() {
		return (_state == ItemState.NOT_IN_WORKSPACE);
	}

	/**
	 * Ask if this item is valid.
	 * 
	 * @return true if item is valid, false if otherwise.
	 */
	@Override
	public boolean isValid() {
		return this.isValid;
	}

	/**
	 * Ask if link <tt>l</tt> is an incoming link of item.
	 * 
	 * @param l :
	 *            link we want ask.
	 * 
	 * @return true if link <tt>l</tt> is an incoming link of item, false if
	 *         otherwise.
	 */
	@Override
	public boolean isInIncomingLinks(Link l) {
		assert l != null;
		return l.getDestination() == this;
		// return incomings.contains(l);
	}

	/**
	 * Ask if link <tt>l</tt> is an outgoing link of item.
	 * 
	 * @param l :
	 *            link we want ask.
	 * 
	 * @return true if link <tt>l</tt> is an outgoing link of item, false if
	 *         otherwise.
	 */
	@Override
	public boolean isInOutgoingLinks(Link l) {
		assert l != null;
		return l.getSource() == this;
		// return outgoings.contains(l);
	}

	// ------------------------------------------------- //

	/* PRIVATE METHODS */

	/**
	 * Called before creating a link. Verify contrainsts on link type
	 * <tt>lt</tt>. <br/>
	 * 
	 * NOTE: This method is used in both methods: Create normal link and Create
	 * non resolved link. In two case, it must always verify contrainsts on link
	 * type.
	 * 
	 * Constraints: - 1. Link type <tt>lt</tt> cannot be null. <br/> - 2. Link
	 * type <tt>lt</tt> must be selected in workspace type. <br/>
	 * 
	 * @param lt :
	 *            type of link to create. <br/><br/>
	 * 
	 * @throws CadseIllegalArgumentException:
	 *             Link type <tt>lt</tt> is null.<br/>
	 * 
	 * @OCL: <b>context:</b> Item::createLink(String id, LinkType lt, Item
	 *       destination) : Link </br> <b>pre:</b> <tt>lt</tt> <> null <i> //
	 *       Link type <tt>lt</tt> cannot be null. <br/> <b>pre:</b>
	 *       <tt>self.workspace.type.selectedLinkTypes</tt>->include(<tt>lt</tt>)
	 *       <i> // Link type <tt>lt</tt> must be selected in workspace type.
	 *       <br/>
	 */
	private void constraints_LinkType(LinkType lt) {
		// 1. Link type lt cannot be null.
		if (lt == null) {
			throw new CadseIllegalArgumentException(Messages.error_linktype_is_null);
		}
	}

	/**
	 * Test preconditions when getting a link.
	 * 
	 * @param lt :
	 *            link type.
	 * 
	 * @exception CadseIllegalArgumentException:
	 *                Link type is null.<br/> CadseIllegalArgumentException:
	 *                Link type <tt>$name</tt> is not selected in this
	 *                workspace type. <br/><br/>
	 */
	private void preconditions_getLink(LinkType lt) {
		constraints_LinkType(lt);
	}

	/**
	 * Seek links by type.
	 * 
	 * @param linkList :
	 *            link list to search.
	 * @param linkType :
	 *            link type used as filter to search.
	 * 
	 * @return list links have the same type. jamais null
	 * 
	 * @NOTE: Seek all links same type given in a link list given.
	 * @exception IllegalArgumentException:
	 *                Link type is null.<br/> IllegalArgumentException: Link
	 *                type <tt>$name</tt> is not selected in this workspace
	 *                type. <br/><br/>
	 */
	private List<Link> getLinksByType(List<Link> linkList, LinkType linkType) {
		preconditions_getLink(linkType);

		List<Link> ret = new ArrayList<Link>();

		filterByLinkType(linkList, linkType, ret);
		return ret;
	}

	private void filterByLinkType(List<Link> linkList, LinkType linkType, List<Link> ret) {
		for (Link l : linkList) {
			// If link has type linkType, then add link to return list.
			if (l.getLinkType().equals(linkType)) {
				ret.add(l);
			}
		}
	}

	/**
	 * instantiate an unresolved link. No notification if the state of item is
	 * new or modifing or if the the of the model is not run
	 * 
	 * @param lt :
	 *            link type.
	 * @param destination :
	 *            new link's destination.
	 * @param computeInverse
	 *            the compute inverse
	 * @param notification
	 *            the notification
	 * 
	 * @return new link l.
	 * 
	 * @throws CadseException
	 *             the melusine exception
	 */
	@Deprecated
	private Link newLinkWithNotification(LinkType lt, Item destination, boolean computeInverse, boolean notification)
			throws CadseException {

		Link l = null;
		Link inverseLink = null;

		// find a good destination.
		Item good_destination = this._wl.getItem(destination.getId());
		if (good_destination == null) {
			good_destination = destination;
		}

		boolean addInIncomming = _state != ItemState.NOT_IN_WORKSPACE && _state != ItemState.MODIFING;

		// est-ce qu'il faut creer le lien inverse ?
		// Par d�faut non
		computeInverse = computeInverse && addInIncomming;

		LinkType inverseLt = lt.getInverse();
		boolean createInverseLink = false;

		l = primitifCreateLink(lt, good_destination, addInIncomming);

		if (computeInverse && inverseLt != null && good_destination.isResolved()) {
			Link findInverseLink = good_destination.getOutgoingLink(inverseLt, this.getId());
			if (findInverseLink == null) {
				// OUI
				createInverseLink = true;
			}
		}

		if (createInverseLink) {
			inverseLink = primitifCreateInverseLink(good_destination, addInIncomming, inverseLt);
		}

		// add link l into the list "outgoings" of source "this".
		if (!(l instanceof ReflectLink)) {
			m_outgoings.add(l);
		}
		if (createInverseLink && (!(inverseLink instanceof ReflectLink))) {
			((ItemImpl) good_destination).m_outgoings.add(inverseLink);
		}

		if (!notification) {
			return l;
		}

		if (getState() == ItemState.NOT_IN_WORKSPACE || getState() == ItemState.MODIFING) {
			return l;
		}
		if (getLogicalWorkspace().getState() != WSModelState.RUN) {
			return l;
		}

		_wl.getCadseDomain().notifieChangeEvent(ChangeID.CREATE_OUTGOING_LINK, l);
		if (l.isLinkResolved()) {
			_wl.getCadseDomain().notifieChangeEvent(ChangeID.RESOLVE_INCOMING_LINK, l.getResolvedDestination(), l);
		}
		if (createInverseLink) {
			_wl.getCadseDomain().notifieChangeEvent(ChangeID.CREATE_OUTGOING_LINK, inverseLink);
			if (inverseLink.isLinkResolved()) {
				_wl.getCadseDomain().notifieChangeEvent(ChangeID.RESOLVE_INCOMING_LINK,
						inverseLink.getResolvedDestination(), inverseLink);
			}
		}
		return l;
	}

	protected Link primitifCreateInverseLink(Item good_destination, boolean addInIncomming, LinkType inverseLt) {
		Link inverseLink;
		if (inverseLt.isDerived()) {
			inverseLink = new DerivedLinkImpl(good_destination, (DerivedLinkType) inverseLt, this, addInIncomming);
		} else {
			inverseLink = new LinkImpl(good_destination, inverseLt, this, addInIncomming);
		}
		return inverseLink;
	}

	protected Link primitifCreateLink(LinkType lt, Item destination, boolean addInIncomming) throws CadseException {

		if (lt.isNatif() && destination.isResolved()) {
			return null;
		}
		Link l;
		// create a new link l whom source is object "this"
		// and destination is parameter "destination".
		// set the default destination type...

		if (lt.isDerived()) {
			l = new DerivedLinkImpl(this, (DerivedLinkType) lt, destination, addInIncomming);
		} else {
			l = new LinkImpl(this, lt, destination, addInIncomming);
		}
		return l;
	}

	/**
	 * Create a new link and .
	 * 
	 * @param lt :
	 *            the type of the link
	 * @param destination :
	 *            new link's destination id.
	 * 
	 * @return new link l.
	 * 
	 * @throws no. *
	 * @throws CadseException
	 *             the melusine exception
	 */

	@Override
	protected Link createDefaultLink(LinkType lt, Item destination) throws CadseException {
		if (lt.isNatif() && destination.isResolved()) {
			return null;
		}
		Link l;
		// create a new link l whom source is object "this"
		// and destination is parameter "destination".
		// set the default destination type...

		if (lt.isDerived()) {
			l = new DerivedLinkImpl(this, (DerivedLinkType) lt, (AbstractItem) destination, true);
		} else {
			l = new LinkImpl(this, lt, destination, true);
		}

		// add link l into the list "outgoings" of source "this".
		if (l != null && !(l instanceof ReflectLink)) {
			if (m_outgoings.contains(l)) {
				return l;
			}
			m_outgoings.add(l);
		}
		return l;
	}

	// // TODO key
	// /*
	// * (non-Javadoc)
	// *
	// * @see
	// fr.imag.adele.cadse.core.Item#computeRenameChange(org.eclipse.ltk.core.refactoring.CompositeChange,
	// * java.lang.String, fr.imag.adele.cadse.core.var.ContextVariable,
	// * fr.imag.adele.cadse.core.var.ContextVariable)
	// */
	// @Override
	// public RefactoringStatus computeRenameChange(CompositeChange change,
	// String newShortName, ContextVariable newCxt,
	// ContextVariable oldCxt) {
	// RefactoringStatus ret = new RefactoringStatus();
	//
	// try {
	// String oldShortName = getShortName();
	// String oldUniqueName = getUniqueName();
	// boolean modifShortName = !oldShortName.equals(newShortName);
	// IItemManager im = getType().getItemManager();
	// if (modifShortName) {
	// String error = im.validateShortName(this, newShortName);
	// if (error != null) {
	// ret.addFatalError(error);
	// return ret;
	// }
	// }
	// String newUniqueName = null;
	// boolean modifUniqueName = false;
	// if (_type.hasUniqueNameAttribute()) {
	// newUniqueName = im.computeUniqueName(this, newShortName, getPartParent(),
	// getPartParentLinkType());
	// if (newUniqueName == null) {
	// ret.addFatalError(Messages.error_unique_name_is_null);
	// return ret;
	// }
	// modifUniqueName = !oldUniqueName.equals(newUniqueName);
	// }
	//
	// if (!modifShortName && !modifUniqueName) {
	// return ret;
	// }
	//
	// if (modifUniqueName) {
	// try {
	// _wl.checkUniqueNameForRename(this, newShortName, newUniqueName);
	// } catch (CadseIllegalArgumentException e) {
	// ret.addFatalError(e.getMessage());
	// return ret;
	// } catch (CadseException e) {
	// ret.addFatalError(e.getMessage());
	// return ret;
	// }
	// }
	//
	// if (_state != ItemState.NOT_IN_WORKSPACE && _state != ItemState.MODIFING)
	// {
	// RefactoringStatus imcheck = im.checkRenameItem(this, newShortName,
	// newUniqueName);
	// ret.merge(imcheck);
	// }
	// if (!ret.isOK()) {
	// return ret;
	// }
	//
	// newCxt.putValue(this, Item.SHORT_NAME_ATTRIBUTE, newShortName);
	// newCxt.putValue(this, Item.UNIQUE_NAME_ATTRIBUTE, newUniqueName);
	// oldCxt.putValue(this, Item.SHORT_NAME_ATTRIBUTE, oldShortName);
	// oldCxt.putValue(this, Item.UNIQUE_NAME_ATTRIBUTE, oldUniqueName);
	//
	// change.add(new RenameItemChange(this, oldShortName, oldUniqueName,
	// newShortName, newUniqueName,
	// modifShortName, modifUniqueName, true, true));
	//
	// for (Link l : m_outgoings) {
	// if (l.isPart() && l.isLinkResolved()) {
	// ret.merge(l.getResolvedDestination().computeRenameChange(change,
	// l.getResolvedDestination().getShortName(), newCxt, oldCxt));
	// if (!ret.isOK()) {
	// return ret;
	// }
	// }
	// }
	// for (Link l : _incomings) {
	// if (l.isAnnotation()) {
	// ret.merge(l.getSource().getType().getItemManager().computeRenameAnnotationChange(change,
	// l.getSource(), this, newCxt, oldCxt));
	// if (!ret.isOK()) {
	// return ret;
	// }
	// }
	// }
	//
	// if (_state != ItemState.NOT_IN_WORKSPACE && _state != ItemState.MODIFING)
	// {
	// if (_contentmanager != ContentManager.NO_CONTENT && _contentmanager !=
	// ContentManager.INVALID_CONTENT
	// && _contentmanager != null) {
	// ret.merge(_contentmanager.computeRenameChange(change, newCxt, oldCxt));
	// if (!ret.isOK()) {
	// return ret;
	// }
	// }
	//
	// ret.merge(im.computeRenameChange(change, this, newCxt, oldCxt));
	// if (!ret.isOK()) {
	// return ret;
	// }
	// }
	// } catch (RuntimeException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// ret.addFatalError(e.getMessage());
	// }
	// return ret;
	// }
	//
	// /*
	// * (non-Javadoc)
	// *
	// * @see fr.imag.adele.cadse.core.Item#rename(java.lang.String,
	// * org.eclipse.core.runtime.IProgressMonitor)
	// */
	// @Override
	// public void rename(String shortName, IProgressMonitor progress) throws
	// CadseException {
	// this.getWorkspaceDomain().beginOperation("rename to " + shortName);
	// try {
	// CompositeChange change = new CompositeChange("rename Item " +
	// getShortName() + " to " + shortName);
	// ContextVariable newCxt = new ContextVariable();
	// ContextVariable oldCxt = new ContextVariable();
	//
	// RefactoringStatus ret = computeRenameChange(change, shortName, newCxt,
	// oldCxt);
	// if (ret.isOK()) {
	// PerformChangeOperation operation = new PerformChangeOperation(change);
	// change.initializeValidationData(progress);
	// try {
	// operation.run(progress);
	// } catch (CadseException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	//
	// } catch (RuntimeException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } finally {
	// this.getWorkspaceDomain().endOperation();
	// }
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#getOutgoingLink(fr.imag.adele.cadse.core.LinkType,
	 *      fr.imag.adele.cadse.core.CompactUUID)
	 */
	@Override
	public Link getOutgoingLink(LinkType lt, CompactUUID destId) {
		List<Link> links = getOutgoingLinks();
		for (Link l : links) {
			if (l.getLinkType() == lt && l.getDestinationId().equals(destId)) {
				return l;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#getOutgoingLink(fr.imag.adele.cadse.core.Item)
	 */
	@Override
	public Link getOutgoingLink(Item item) {
		List<Link> links = getOutgoingLinks();
		for (Link l : links) {
			if (l.getDestinationId().equals(item.getId())) {
				return l;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#buildComposite()
	 */
	@Override
	public void buildComposite() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Gets the composition link.
	 * 
	 * @return the composition link
	 */
	public List<Link> getCompositionLink() {
		List<Link> links = new ArrayList<Link>();
		for (Link link : getOutgoingLinks()) {
			if (link.isComposition()) {
				links.add(link);
			}
		}
		return links;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#getDerivedLinks()
	 */
	@Override
	public Set<DerivedLink> getDerivedLinks() {
		if (_derivedLinks == null) {
			return Collections.emptySet();
		}
		return this._derivedLinks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#setDerivedLinks(java.util.Set)
	 */
	@Override
	public void setDerivedLinks(Set<DerivedLinkDescription> derivedLinks) {
		if (derivedLinks == null || derivedLinks.size() == 0) {
			return;
		}

		this._derivedLinks = new HashSet<DerivedLink>();
		for (DerivedLinkDescription idl : derivedLinks) {
			createOneDerivedLink(idl);
		}
	}

	/**
	 * Creates the derived link type.
	 * 
	 * @param level
	 *            the level
	 * @param derivedLinks
	 *            the derived links
	 * 
	 * @return true, if successful
	 */
	public boolean createDerivedLinkType(int level, Set<DerivedLinkDescription> derivedLinks) {
		boolean ret = false;
		for (DerivedLinkDescription idl : derivedLinks) {
			int idl_level = idl.getType().lastIndexOf('#') + 1;
			if (level == idl_level) {
				createDerivedLinkTypeIfNeed(idl);
				ret = true;
			}
		}
		return ret;
	}

	/**
	 * Compute composants.
	 * 
	 * @param notThrow
	 *            the not throw
	 * 
	 * @return the map< compact uui d, item>
	 * 
	 * @throws CadseIllegalArgumentException
	 *             the melusine error
	 */
	public Map<CompactUUID, Item> computeComponents(boolean notThrow) throws CadseIllegalArgumentException {
		// if (!isOpen) {
		// if (notThrow)
		// return null;
		// throw new CadseIllegalArgumentException(Messages.error_not_open,
		// getId());
		// //$NON-NLS-1$
		// }
		HashMap<CompactUUID, Item> ret = new HashMap<CompactUUID, Item>();

		for (Link link : getOutgoingLinks()) {
			if (link.isComposition()) {
				Item dest = link.getDestination();
				ret.put(dest.getId(), dest);
				if (!dest.isResolved()) {
					if (!notThrow) {
						throw new CadseIllegalArgumentException(Messages.error_not_open_but_unresolved_composant,
								getId(), link.getDestinationId());
					}
					continue;
				}
				((ItemImpl) dest).tryToRecomputeComponent();
				Map<CompactUUID, Item> composantsDest = ((ItemImpl) dest)._composants;
				if (composantsDest != null) {
					ret.putAll(composantsDest);
				}
			}
		}
		return ret;
	}

	/**
	 * Compute derived links.
	 * 
	 * @return the set< derived link>
	 */
	Set<DerivedLink> computeDerivedLinks() {
		HashSet<DerivedLink> derivedLinks = new HashSet<DerivedLink>();
		for (Link el : new ArrayList<Link>(getOutgoingLinks())) {
			if (!el.isComposition()) {
				continue;
			}

			Item comp = el.getResolvedDestination();
			if (comp == null) {
				continue;
			}

			// traiter outgoing links
			for (Link outgoing : comp.getOutgoingLinks()) {
				// composites entities are considered to be part of the
				// composite.
				// and if the destination is in the composants set.
				if (outgoing.isComposition() || containsComponent(outgoing.getDestinationId())) {
					continue;
				}
				DerivedLink createOneDerivedLink = createOneDerivedLink((outgoing));
				if (createOneDerivedLink != null) {
					derivedLinks.add(createOneDerivedLink);
				}
			}
		}

		return derivedLinks;
	}

	/**
	 * Re compute derived link.
	 * 
	 * @param rec
	 *            the rec
	 */
	void reComputeDerivedLink(boolean rec) {
		if (this.isClosed()) {
			throw new CadseIllegalArgumentException(Messages.error_internal_connot_call_closed_method, getId());
		}

		// garde une copy pour voir ceux qui ont �t� supprim�s.
		Set<DerivedLink> copy = this._derivedLinks;
		if (copy == null) {
			copy = new HashSet<DerivedLink>();
		}

		if (rec) { // algo recurcif au chargement et quand on force le recalcul
			// si possible.
			for (Link el : new ArrayList<Link>(getOutgoingLinks())) {
				if (!el.isComposition()) {
					continue;
				}

				Item comp = el.getResolvedDestination();
				if (comp == null) {
					continue;
				}
				ItemImpl compImpl = (ItemImpl) comp;

				if (compImpl._derivedLinks == null) {
					compImpl.reComputeDerivedLink(true);
				}
			}
		}
		this._derivedLinks = this.computeDerivedLinks();

		// remove the bad link.
		copy.removeAll(this._derivedLinks);
		for (Link idl : copy) {
			// remove the found link.
			(idl.getDestination()).removeIncomingLink(idl, true);
			this.removeOutgoingLink(idl);
		}
	}

	/**
	 * Restore item.
	 * 
	 * @param toImport
	 *            the to import
	 * 
	 * @throws CadseException
	 *             the melusine exception
	 */
	public void restoreItem(List<Link> toImport) throws CadseException {
		for (Link link : toImport) {
			internalRestoreItem(link);
		}
	}

	/**
	 * Internal restore item.
	 * 
	 * @param link
	 *            the link
	 * 
	 * @throws CadseException
	 *             the melusine exception
	 */
	protected void internalRestoreItem(Link link) throws CadseException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Creates the one derived link.
	 * 
	 * @param link
	 *            the link
	 * 
	 * @return the link
	 */
	Link createOneDerivedLink(DerivedLinkDescription link) {
		try {
			// find the link type of the derived link and create it if need.
			// create derived link with origin root, name #L, destination
			// destId, and char of L.
			// If link type does not exist create it.
			LinkType lt = createDerivedLinkTypeIfNeed(link);
			if (lt == null) {
				return null;
			}

			Item destination = _wl.loadItem(link.getDestination());

			Link ret = null;
			// create the derived link if need.
			if ((ret = findDerivedLink(lt, destination)) == null) {
				ret = newLinkWithNotification(lt, destination, false, true);
			}
			return ret;
		} catch (CadseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Creates the one derived link.
	 * 
	 * @param link
	 *            the link
	 * 
	 * @return the derived link
	 */
	DerivedLink createOneDerivedLink(Link link) {
		try {
			// find the link type of the derived link and create it if need.
			// create derived link with origin root, name #L, destination
			// destId, and char of L.
			// If link type does not exist create it.
			LinkType lt = createDerivedLinkTypeIfNeed(link.getLinkType());
			if (lt == null) {
				return null;
			}

			DerivedLink ret = null;
			Item destination = link.getDestination();
			// create the derived link if need.
			if ((ret = findDerivedLink(lt, destination)) == null) {
				ret = (DerivedLink) newLinkWithNotification(lt, destination, false, true);
			}
			return ret;
		} catch (CadseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	// DerivedLink createOneDerivedLinkLoad(Link link) {
	//
	//
	// // find the link type of the derived link and create it if need.
	// // create derived link with origin root, name #L, destination destId, and
	// char of L. (TODO: correction)
	// // If link type does not exist create it.
	// LinkType lt = createDerivedLinkTypeIfNeed(link.getType());
	//
	//
	// // create the derived link if need.
	// try {
	// Item destination = link.getDestination();
	// return (DerivedLink) newLinkWithoutNotificationLoad(lt, destination);
	// } catch (CadseException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// return null;
	// }
	//
	// }

	/**
	 * Return the name of derived link Type.<br/> The name begin with '#' next
	 * the item type of the source and '_' and the name of orignal link type
	 * name. The number of '#' indique la profondeur.
	 * 
	 * @param lt
	 *            the lt
	 * 
	 * @return The name of the derived link type
	 */
	static private String getDerivedType(LinkType lt) {
		String ln = lt.getName();
		if (lt.isDerived()) {
			return "#" + ln; //$NON-NLS-1$
		}
		return "#" + lt.getSource().getName() + "_" + ln; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Cette methode cre� un LinkType en fonction d'un autre LinkType. Il est
	 * possible de recup�rer l'ItemType source, l'itemType destination, et le
	 * LinkType du lien qui provoque ce lien d�riv�.<br>
	 * <code>
	 * ItemType destType = getModel().getModelType().getItemType(link.getDestTypeName());<br>
	 * ItemType sourceType = getModel().getModelType().getItemType(link.getTypeSourceName());<br>
	 * LinkType sourceLinkType = sourceType.getOutgoingLinkType(link.getLinkName());<br>
	 * </code>
	 * 
	 * @param link
	 *            Le lien deriv� interne � partir du quel doit etre creer le
	 *            type de lien deriv�
	 * 
	 * @return le type de lien deriv�
	 */
	protected DerivedLinkType createDerivedLinkTypeIfNeed(LinkType link) {
		ItemType destType = link.getDestination();

		String derivedTypeName = getDerivedType(link);

		LinkType lt = getType().getOutgoingLinkType(destType, derivedTypeName);

		if (lt != null) {
			return (DerivedLinkType) lt;
		}

		int kindLinkType = LinkType.DERIVED;
		if (link.isAggregation()) {
			kindLinkType |= LinkType.AGGREGATION;
		}
		if (link.isRequire()) {
			kindLinkType |= LinkType.REQUIRE;
		}

		// TODO
		// lt = getType().getOutgoingLinkType(derivedTypeName);
		// if (lt != null) {
		// if (destType.isSuperTypeOf(lt.getDestination())) {
		// lt.setDestinationType(destType);
		// return (DerivedLinkType) lt;
		// }
		// return null;
		// }
		try {
			lt = ((ItemTypeImpl) _type).createDerivedLinkType(null, -3, derivedTypeName, kindLinkType, 0, -1, null,
					link);
		} catch (CadseIllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		return (DerivedLinkType) lt;
	}

	/**
	 * Cette methode cre� un derived LinkType � partir d'un
	 * DerivedLinkDescription.
	 * 
	 * @param link
	 *            La description d'un lien deriv�
	 * 
	 * @return le type de lien deriv�
	 */
	protected DerivedLinkType createDerivedLinkTypeIfNeed(DerivedLinkDescription link) {

		ItemType originSourceType = _wl.getItemType(link.getOriginLinkSourceTypeID());
		if (originSourceType == null) {
			return null;
		}
		LinkType originLinkType = originSourceType.getOutgoingLinkType(link.getOriginLinkTypeID());
		if (originLinkType == null) {
			return null;
		}
		return createDerivedLinkTypeIfNeed(originLinkType);
	}

	// /**
	// * (non-Javadoc) mise en cache + shadow
	// */
	// @Override
	// public void unload() throws CadseException {
	// // if (isComposite() && isOpen())
	// // throw new CadseException(Messages.error_you_must_close_composite);
	// // //$NON-NLS-1$
	// for (Link l : getIncomingLinks()) {
	// if (l.getLinkType().isComposition()) {
	// throw new CadseException(Messages.error_cannot_unload_composant);
	// }
	// }
	// try {
	// // _wl.getCadseDomain().beginOperation("Item.unload");
	// // remove this line because unload ne fonctionne pas à cause d'un
	// // test dans la methode delete
	// // forceState(ItemState.PRE_SHADOW_OR_DELETE);
	// internalHiddenItem(false, this);
	// shadow(true);
	// } finally {
	// // _wl.getCadseDomain().endOperation();
	// }
	// }

	/**
	 * Internal hidden item.
	 * 
	 * @param referenced
	 *            the referenced
	 * @param item
	 *            the item
	 */
	protected void internalHiddenItem(boolean referenced, Item item) {
	}

	/**
	 * Find derived link.
	 * 
	 * @param lt
	 *            the lt
	 * @param destination
	 *            the destination
	 * 
	 * @return the derived link
	 */
	protected DerivedLink findDerivedLink(LinkType lt, Item destination) {
		// getOutgoingLink(lt, destinationId)
		for (Link l : getOutgoingLinks()) {
			if ((l.getLinkType().equals(lt) || l.getLinkType().getName().equals(lt.getName()))
					&& l.getDestination().equals(destination)) {
				return (DerivedLink) l;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#getCompositeParent()
	 */
	@Override
	public List<Item> getCompositeParent() {
		List<Item> ret = new ArrayList<Item>();
		for (Link l : getIncomingLinks()) {
			if (l.getLinkType() == null) {
				getCadseDomain().error(this, "Type of link is null : dest = " + getId() + " source = " + l.getSource(),
						null);
				continue;
			}
			if (l.getLinkType().isComposition()) {
				ret.add(l.getSource());
			}
		}
		return ret;
	}

	@Override
	public Item getPartParent(boolean attemptToRecreate) {
		return getPartParent(attemptToRecreate, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#getPartParent()
	 */
	public Item getPartParent(boolean attemptToRecreate, LinkType lt) {
		if (_parent != null) {
			return _parent;
		}

		// TODO stack overflow
		// this.m_parent = findPartParentFromOutgoing(lt, false);
		// if (m_parent != null) {
		// return m_parent;
		// }

		Item parent = findPartParentFromIncoming(lt, false);
		if (parent != null) {
			setParent(parent, lt);
			return _parent;
		}

		_parent = getParentInStorage();

		if (_parent == null) {
			return null;
		}

		if (lt == null) {
			lt = Accessor.getPartParentLinkType(this, _parent);
		}
		if (lt == null) {
			lt = getType().getIncomingPart(_parent.getType());
		}
		if (lt == null || !lt.isPart()) {
			return null;
		}
		setParent(parent, lt);

		if (attemptToRecreate && (getState() != ItemState.NOT_IN_WORKSPACE && getState() != ItemState.MODIFING)) {
			// cherche a reparer une anomalie : un lien est manquant...
			// on fait ceci que si ce n'est pas un nouveau item ou si l'item
			// n'est pas en modification(todo)

			Link l = _parent.getOutgoingLink(lt, getId());
			if (l != null) {
				l.resolve();
			} else {
				if (lt.getMax() == 1) {
					Link findOtherLink = _parent.getOutgoingLink(lt);
					if (findOtherLink != null) {
						getCadseDomain().error(this, "Cannot recreate Item children alldready exist", null);
						return null;
					}
				}
				try {
					_parent.createLink(lt, this);
				} catch (CadseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return _parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#getPartParent(fr.imag.adele.cadse.core.ItemType)
	 */
	@Override
	public Item getPartParent(ItemType typeID) {
		Item item = getPartParent(false);

		while (item != null) {
			if (item.isInstanceOf(typeID)) {
				return item;
			}
			item = item.getPartParent(false);
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#getPartParentByName(java.lang.String)
	 */
	@Override
	public Item getPartParentByName(String typeName) {
		Item item = getPartParent();

		while (item != null) {
			if (item.getType().getName().equals(typeName)) {
				return item;
			}
			item = item.getPartParent();
		}

		return null;
	}

	/**
	 * Gets the parent in storage.
	 * 
	 * @return the parent in storage
	 */
	public Item getParentInStorage() {
		if (this._parent != null) {
			return _parent;
		}
		
		return getOutgoingItem(CadseGCST.ITEM_lt_PARENT, true);
//
//		Object value = getAttribute(CadseGCST.ITEM_at_PARENT_ITEM_ID_);
//		if (value instanceof UUID) {
//			// migration
//			value = new CompactUUID((UUID) value);
//			// try {
//			// remove this lines : use outgoing link instead
//			// setAttribute(CadseGCST.ITEM_at_PARENT_ITEM_ID_,
//			// value);
//			// } catch (CadseException e) {
//			// // TODO Auto-generated catch block
//			// e.printStackTrace();
//			// }
//		}
//		CompactUUID parentId = (CompactUUID) value;
//		if (parentId == null) {
//			return null;
//		}
//		Item retItem = _wl.getItem(parentId);
//		// /setAttribute(ATTR_PARENT_ITEM_ID, retItem.getId());
//		return retItem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#computeAttribute(java.lang.String,
	 *      java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void computeAttribute(String attributeName, Object theirsValue, Object baseValue, Object mineValue) {

	}

	// ///////////

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#getComponents()
	 */
	@Override
	public Set<Item> getComponents() {
		tryToRecomputeComponent();
		if (_composants == null) {
			return Collections.emptySet();
		}
		return new ComponentsSet();
	}

	/**
	 * The Class ComponentsSet.
	 */
	class ComponentsSet extends AbstractSet<Item> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.AbstractCollection#contains(java.lang.Object)
		 */
		@Override
		public boolean contains(Object o) {
			return _composants.containsValue(o);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.AbstractCollection#retainAll(java.util.Collection)
		 */
		@Override
		public boolean retainAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.AbstractCollection#addAll(java.util.Collection)
		 */
		@Override
		public boolean addAll(Collection<? extends Item> c) {
			throw new UnsupportedOperationException();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.AbstractCollection#iterator()
		 */
		@Override
		public Iterator<Item> iterator() {
			return Collections.unmodifiableCollection(_composants.values()).iterator();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.AbstractCollection#size()
		 */
		@Override
		public int size() {
			return _composants.size();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.AbstractCollection#toArray()
		 */
		@Override
		public Object[] toArray() {
			return _composants.values().toArray();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.AbstractCollection#toArray(T[])
		 */
		@Override
		public <T> T[] toArray(T[] a) {
			return _composants.values().toArray(a);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#getComponentsId()
	 */
	@Override
	public Set<CompactUUID> getComponentIds() {
		Set<Item> c = getComponents();
		Set<CompactUUID> ids = new HashSet<CompactUUID>();
		for (Item il : c) {
			ids.add(il.getId());
		}
		return ids;
	}

	// -------------------------------------------------//

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#isAncestorOf(fr.imag.adele.cadse.core.Item)
	 */
	@Override
	public boolean isAncestorOf(Item item2) {
		return Accessor.isAncestorOf(this, item2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#getWorkspaceDomain()
	 */
	@Override
	public CadseDomain getCadseDomain() {
		return _wl.getCadseDomain();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#setComponents(java.util.Set)
	 */
	@Override
	public void setComponents(Set<ItemDescriptionRef> comp) throws CadseException {
		if (comp == null || comp.size() == 0) {
			_composants = null;
			return;
		}
		_composants = new HashMap<CompactUUID, Item>();
		for (ItemDescriptionRef c : comp) {
			try {
				Item i = this._wl.loadItem(c);
				_composants.put(i.getId(), i);
			} catch (Throwable e) {
				System.err.println("This composant is ignored.\n+" + c.toString());
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#containsComponent(fr.imag.adele.cadse.core.CompactUUID)
	 */
	@Override
	public boolean containsComponent(CompactUUID id) {
		tryToRecomputeComponent();
		return _composants == null ? false : _composants.containsKey(id);
	}

	private void tryToRecomputeComponent() {
		// recom
		if (needRecomputeComponent()) {
			_composants = computeComponents(true);
			_isRecomputeComponants = false;
			setModified(true);
		}
	}

	private boolean needRecomputeComponent() {
		if (_isRecomputeComponants) {
			return true;
		}
		if (_composants == null && hasCompositionLinks()) {
			return false;
		}
		return false;
	}

	private boolean hasCompositionLinks() {
		for (Link l : this.m_outgoings) {
			if (l.isComposition()) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#getComponentInfo(fr.imag.adele.cadse.core.CompactUUID)
	 */
	@Override
	public Item getComponentInfo(CompactUUID id) {
		return _composants == null ? null : _composants.get(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.AbstractItem#isInstanceOf(fr.imag.adele.cadse.core.ItemType)
	 */
	@Override
	public boolean isInstanceOf(ItemType it) {
		// return this.type.equals(it) || it.isSuperTypeOf(this.type);
		return Accessor.isInstanceOf(this, it);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#canCreateLink(fr.imag.adele.cadse.core.LinkType,
	 *      fr.imag.adele.cadse.core.CompactUUID)
	 */
	@Override
	public boolean canCreateLink(LinkType lt, CompactUUID destination) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#canSetAttribute(java.lang.String,
	 *      java.lang.Object)
	 */
	@Override
	public boolean canSetAttribute(String key, Object value) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#setType(fr.imag.adele.cadse.core.ItemType)
	 */
	@Override
	public void setType(ItemType selectedItemType) {
		this._type = selectedItemType;
		setModified(true);
	}

	@Override
	public boolean commitMove(OrderWay kind, Link l1, Link l2) {
		if (kind == OrderWay.move_after) {
			return moveAfter(l1, l2);
		} else {
			return moveBefore(l1, l2);
		}
	}

	/**
	 * Move after.
	 * 
	 * @param linkToMove
	 *            the link to move
	 * @param linkAfterToMove
	 *            the link after to move
	 */
	private boolean moveAfter(Link linkToMove, Link linkAfterToMove) {
		int indexLinkToMove = m_outgoings.indexOf(linkToMove);
		if (indexLinkToMove == -1) {
			throw new IllegalArgumentException("Bad link: " + linkToMove); //$NON-NLS-1$
		}
		int indexLinkAfterToMove = m_outgoings.indexOf(linkAfterToMove);
		if (indexLinkAfterToMove == -1) {
			throw new IllegalArgumentException("Bad link: " + linkAfterToMove); //$NON-NLS-1$
		}
		if (indexLinkToMove == indexLinkAfterToMove + 1) {
			return false; // nothing to do;
		}

		m_outgoings.remove(indexLinkToMove);
		m_outgoings.add(indexLinkToMove < indexLinkAfterToMove ? indexLinkAfterToMove : indexLinkAfterToMove + 1,
				linkToMove);
		return true;
	}

	/**
	 * Move before.
	 * 
	 * @param linkToMove
	 *            the link to move
	 * @param linkBeforeToMove
	 *            the link before to move
	 */
	private boolean moveBefore(Link linkToMove, Link linkBeforeToMove) {
		int indexLinkToMove = m_outgoings.indexOf(linkToMove);
		if (indexLinkToMove == -1) {
			throw new IllegalArgumentException("Bad link: " + linkToMove); //$NON-NLS-1$
		}
		int indexLinkBeforeToMove = m_outgoings.indexOf(linkBeforeToMove);
		if (indexLinkBeforeToMove == -1) {
			throw new IllegalArgumentException("Bad link: " + linkBeforeToMove); //$NON-NLS-1$
		}
		if (indexLinkToMove == indexLinkBeforeToMove - 1) {
			return false; // nothing to do;
		}

		m_outgoings.remove(indexLinkToMove);
		m_outgoings.add(indexLinkToMove < indexLinkBeforeToMove ? indexLinkBeforeToMove - 1 : indexLinkBeforeToMove,
				linkToMove);
		return true;
	}

	// /*
	// * (non-Javadoc)
	// *
	// * @see fr.imag.adele.cadse.core.Item#setUniqueName(java.lang.String)
	// */
	// @Override
	// public void setUniqueName(String uniqueName) throws CadseException {
	//
	// if (!_type.hasUniqueNameAttribute()) {
	// return;
	// }
	//
	// // pre: items->forAll(item | item.id <> id )
	// Item i = _wl.getItem(uniqueName);
	// if (i != null && i != this && i.isResolved()) {
	// throw new CadseException(Messages.error_unique_name_alreay_exist,
	// uniqueName);
	// }
	//
	// String oldValue = uniqueName;
	// this._uniqueName = uniqueName;
	// if (_state != ItemState.NOT_IN_WORKSPACE && _state != ItemState.MODIFING)
	// {
	// _wl.renameUniqueName(this, oldValue, uniqueName);
	// _wl.getCadseDomain().notifieChangeEvent(ChangeID.SET_ATTRIBUTE, this,
	// CadseGCST.ITEM_at_QUALIFIED_NAME_, oldValue, uniqueName);
	// }
	//
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#getDerivedLinkDescriptions(fr.imag.adele.cadse.core.ItemDescription)
	 */
	@Override
	public Set<DerivedLinkDescription> getDerivedLinkDescriptions(ItemDescription source) {
		HashSet<DerivedLinkDescription> ret = new HashSet<DerivedLinkDescription>();
		if (_derivedLinks != null) {
			for (DerivedLink dl : _derivedLinks) {
				ret.add(new DerivedLinkDescription(source, dl));
			}
		}

		return ret;
	}

	/**
	 * Resetderivedlink.
	 */
	public void resetderivedlink() {
		if (_derivedLinks == null) {
			reComputeDerivedLink(true);
		}
	}

	/**
	 * Resolve component.
	 * 
	 * @param newitem
	 *            the newitem
	 */
	public void resolveComponent(Item newitem) {
		if (_composants == null) {
			return;
		}

		Item olditem = _composants.get(newitem.getId());
		if (olditem != null) {
			_composants.put(newitem.getId(), newitem);
		}
	}

	@Override
	public void computeAttributes() {
		super.computeAttributes();
		if (_type != null) {
			if (_type.hasQualifiedNameAttribute()) {
				this._qualifiedName = _type.getItemManager().computeQualifiedName(this, _name, getPartParent(false),
						getPartParentLinkType());
			}
		}
	}

	public boolean hasQualifiedNameAttribute() {
		return _type.hasQualifiedNameAttribute();
	}

	public void setParent(Item parent, LinkType lt) {
		if (parent != null && !parent.isResolved())
			throw new CadseIllegalArgumentException("Cannot set unresolved parent", this, parent);
		checkCycle(parent, this);
		this._parent = parent;
		setModified(true);
	}

	/**
	 * Create a link part from source to dest. Check no path from dest to source
	 * or source not equal to dest
	 * 
	 * @param source
	 *            the source of the link which is created
	 * @param dest
	 *            the dest of the link which is created TODO put this validation
	 *            in the transaction !!!
	 */
	private void checkCycle(Item source, Item dest) {
		if (source == null) {
			throw new CadseIllegalArgumentException("source is null");
		}

		if (dest == null) {
			throw new CadseIllegalArgumentException("dest is null");
		}

		if (dest == source || source.getId().equals(dest.getId())) {
			throw new CadseIllegalArgumentException("cannot be parent of it self : {0} ", source.getName());
		}

		while (true) {
			source = source.getPartParent(false);
			if (source == null) {
				return;
			}
			if (dest == source || source.getId().equals(dest.getId())) {
				throw new CadseIllegalArgumentException("cannot be cycle from source to dest");
			}
		}

	}

}
