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

package fr.imag.adele.cadse.core.impl.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemDescriptionRef;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.CadseCore;
import fr.imag.adele.cadse.core.key.Key;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.util.OrderWay;

/**
 * The Class AbstractItem.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public abstract class AbstractItem extends AbstractGeneratedItem implements Item {

	/** The unique name. */
	protected String		_qualifiedName;

	/** The shortname. */
	protected String		_name;

	/** The type. */
	protected ItemType		_type;

	/** The incomings. */
	protected List<Link>	_incomings;

        public AbstractItem() {
        }


	/**
	 * Instantiates a new abstract item.
	 * 
	 * @param wl
	 *            the wl
	 * @param type
	 *            the type
	 */
	public AbstractItem(DBLogicalWorkspace wl, ItemType type) {
		this._dblw = wl;
		this._type = type;
		this._incomings = new ArrayList<Link>();
		this._qualifiedName = NO_VALUE_STRING;
		this._name = NO_VALUE_STRING;

	}

	/**
	 * Type can be null for mit...
	 * 
	 * @param wl
	 *            the wl
	 * @param the
	 *            id
	 * @param type
	 *            the type
	 * @param uniqueName
	 *            the unique name
	 * @param shortName
	 *            the short name
	 */
	protected AbstractItem(LogicalWorkspace wl, UUID id, ItemType type, String uniqueName, String shortName) {
		super(wl, id);
		this._type = type;
		this._incomings = new ArrayList<Link>();

		if (type == null || type.hasQualifiedNameAttribute()) {
			this._qualifiedName = uniqueName == null ? NO_VALUE_STRING : uniqueName;
		} else {
			this._qualifiedName = Item.NO_VALUE_STRING;
		}
		if (type == null || type.hasShortNameAttribute()) {
			this._name = shortName == null ? NO_VALUE_STRING : shortName;
		} else {
			this._name = Item.NO_VALUE_STRING;
		}
	}

	/**
	 * Instantiates a new abstract item.
	 * 
	 * @param wl
	 *            the wl
	 * @param type
	 *            the type
	 * @param desc
	 *            the desc
	 */
	protected AbstractItem(LogicalWorkspace wl, ItemType type, ItemDescriptionRef desc) {
		super(wl, desc.getId());
		this._type = type;
		this._incomings = new ArrayList<Link>();
		if (type.hasQualifiedNameAttribute()) {
			this._qualifiedName = desc.getQualifiedName();
		} else {
			this._qualifiedName = Item.NO_VALUE_STRING;
		}
		if (type.hasShortNameAttribute()) {
			this._name = desc.getName();
		} else {
			this._name = Item.NO_VALUE_STRING;
		}
	}

	public AbstractItem(LogicalWorkspace wl, ItemType type, ItemDelta desc) {
		this._dblw = (DBLogicalWorkspace) wl;
		this._objectId = desc.getObjectID();
		
		this._type = type;
		this._incomings = new ArrayList<Link>();
		if (type.hasQualifiedNameAttribute()) {
			this._qualifiedName = desc.getQualifiedName();
		} else {
			this._qualifiedName = Item.NO_VALUE_STRING;
		}
		if (type.hasShortNameAttribute()) {
			this._name = desc.getName();
		} else {
			this._name = Item.NO_VALUE_STRING;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#getUniqueName()
	 */
	@Override
	public String getQualifiedName() {
		if ((_qualifiedName == null || _qualifiedName == NO_VALUE_STRING) && isResolved()) {
			Key key = getKey();
			if (key != null) {
				return key.getQualifiedString();
			}
		}
		return _qualifiedName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#getShortName()
	 */
	@Override
	public String getName() {
		return _name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#getType()
	 */
	public ItemType getType() {
		return _type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#getQualifiedDisplayName()
	 */
	@Override
	public String getQualifiedDisplayName() {
		return getType().getItemManager().getQualifiedDisplayName(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#isComposite()
	 */
	@Override
	public boolean isComposite() {
		return getType().isComposite();
	}

	/**
	 * Get all links <tt>incomings</tt>. jamais null
	 * 
	 * @return a unmodifiable list all <tt>incomings</tt> links.
	 */
	@Override
	public List<Link> getIncomingLinks() {
		return Collections.unmodifiableList(_incomings);
	}

	/**
	 * R�soudre un lien. Ajouter le lien � la liste <tt>incomings</tt> de cet
	 * item.
	 * 
	 * @param link
	 *            the link
	 */
	@Override
	public synchronized void addIncomingLink(Link link, boolean notifie) {
		if (link.getLinkType() == CadseCore.theLinkType && !(link instanceof LinkType)) {
			Logger.getLogger("fr.imag.adele.cadse.runtime.lw").log(Level.SEVERE, "add bad incomming : not instance of link type", new CadseException());
		}
		_incomings.add(link);
	}

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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#setOutgoingItemOne(fr.imag.adele.cadse.core.LinkType,
	 *      fr.imag.adele.cadse.core.Item)
	 */
	@Override
	public Link setOutgoingItem(LinkType lt, Item destination) throws CadseException {
		return Accessor.setOutgoingItemOne(this, lt, destination);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#setOutgoingItem(fr.imag.adele.cadse.core.LinkType,
	 *      java.util.Collection)
	 */
	@Override
	public Collection<Link> setOutgoingItems(LinkType lt, Collection<Item> value) throws CadseException {
		return Accessor.setOutgoingItem(this, lt, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#addOutgoingItem(fr.imag.adele.cadse.core.LinkType,
	 *      fr.imag.adele.cadse.core.Item)
	 */
	@Override
	public Link addOutgoingItem(LinkType linkType, Item destination) throws CadseException {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#removeOutgoingItem(fr.imag.adele.cadse.core.LinkType,
	 *      fr.imag.adele.cadse.core.Item)
	 */
	@Override
	public Link removeOutgoingItem(LinkType linkType, Item destination) throws CadseException {
		throw new UnsupportedOperationException();
	}

	

	@Override
	public boolean commitMove(OrderWay kind, Link l1, Link l2) {
		throw new UnsupportedOperationException();
	}

	public boolean commitSetAttribute(IAttributeType<?> type, Object value) {
		if (type == CadseGCST.ITEM_at_QUALIFIED_NAME_) {
			_qualifiedName = (String) value;
		}
		return super.commitSetAttribute(type, value);
	}
}
