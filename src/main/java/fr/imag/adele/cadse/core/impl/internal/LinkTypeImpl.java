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

import java.util.Collection;
import java.util.UUID;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.CadseIllegalArgumentException;
import fr.imag.adele.cadse.core.CadseRuntime;
import fr.imag.adele.cadse.core.ILinkTypeManager;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.TypeDefinition;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.enumdef.TWDestEvol;
import fr.imag.adele.cadse.core.impl.AbstractLinkTypeManager;
import fr.imag.adele.cadse.core.impl.CadseCore;
import fr.imag.adele.cadse.core.impl.CollectedReflectLink;
import fr.imag.adele.cadse.core.impl.ReflectLink;
import fr.imag.adele.cadse.core.impl.attribute.AttributeType;
import fr.imag.adele.cadse.core.impl.internal.delta.ItemTypeItemDeltaAdapter;
import fr.imag.adele.cadse.core.impl.internal.delta.LinkTypeItemDeltaAdapter;
import fr.imag.adele.cadse.core.impl.ui.UIFieldImpl;
import fr.imag.adele.cadse.core.impl.ui.ic.IC_Descriptor;
import fr.imag.adele.cadse.core.impl.ui.mc.MC_Descriptor;
import fr.imag.adele.cadse.core.internal.attribute.IInternalTWLink;
import fr.imag.adele.cadse.core.path.EvaluatePath;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransactionListener;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.ui.EPosLabel;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.util.Convert;
import fr.imag.adele.cadse.util.ArraysUtil;

/**
 * D�finit un type de lien particulier. Attributs : L'attribut id est une cl�
 * par rapport � la source. Il contient le nom de la relation.
 * 
 * @author nguyent
 * @version 2.0
 * @date 27/02/07
 */
public class LinkTypeImpl extends AttributeType implements LinkType, Item, IInternalTWLink {

	// public static final String LT_PARENT_SN_PREFIX = "#parent:";

	/** The int id. */
	private int					_intID;

	/** The kind. */
	private int					_kind;

	/** The source. Attribute.parent */

	/** The destination. */
	private TypeDefinition			_destination;
	/** The min. */
	private int					_min;

	/** The max. */
	private int					_max;

	/** The selection. */
	private String				_selection;

	/** The manager. */
	private ILinkTypeManager	_fManager;

	/** The inverse. */
	LinkType					_inverse;

	/** The link type. */
	private LinkType			_linkType;

	/** The info. */
	private String				_displayName;

	TWDestEvol					_twdestEvol	= TWDestEvol.immutable;

        public LinkTypeImpl() {
        }

	// TWCoupled is a flag

        
	/**
	 * The Constructor.
	 * 
	 * @param source
	 *            the source
	 * @param name
	 *            the name
	 * @param min
	 *            the min
	 * @param max
	 *            the max
	 * @param destination
	 *            the destination
	 * @param kind
	 *            the kind
	 * @param intID
	 *            the int id
	 * @param selection
	 *            the selection
	 * 
	 * @version 2.0
	 */
	LinkTypeImpl(UUID id, int kind, TypeDefinition source, String name, int intID, int min, int max, String selection,
			TypeDefinition destination) {
		super(id, name, min != 0 ? MUST_BE_INITIALIZED_AT_CREATION_TIME : 0);

		if (CadseCore.theLinkType == null) {
			this._linkType = this;
			setFlag(NATIF, true);
		} else {
			this._linkType = CadseCore.theLinkType;
		}

		this._kind = kind;
		setParent(source, CadseGCST.TYPE_DEFINITION_lt_ATTRIBUTES);

		this._min = min;
		this._max = max;
		this._destination = destination;
		this._selection = selection;
		this._intID = intID;

		if (!source.isOrphan()) {
			destination.addIncomingLink(this, true);
		}
	}

	/**
	 * The Constructor.
	 * 
	 * @param source
	 *            the source
	 * @param name
	 *            the name
	 * @param min
	 *            the min
	 * @param max
	 *            the max
	 * @param destination
	 *            the destination
	 * @param kind
	 *            the kind
	 * @param selection
	 *            the selection
	 */
	LinkTypeImpl(UUID id, int kind, TypeDefinition source, String name, int min, int max, String selection,
			TypeDefinition destination) {
		super(id, name, min != 0 ? MUST_BE_INITIALIZED_AT_CREATION_TIME : 0);

		this._linkType = CadseCore.theLinkType;
		this._kind = kind;
		setParent(source, CadseGCST.TYPE_DEFINITION_lt_ATTRIBUTES);

		this._min = min;
		this._max = max;
		this._destination = destination;
		this._selection = selection;
		this._intID = -1;

		if (!source.isOrphan()) {
			destination.addIncomingLink(this, true);
		}
	}

	public LinkTypeImpl(UUID id, TypeDefinition source, String name) {
		super(id, name, 0);

		this._linkType = CadseCore.theLinkType;
		this._kind = 0;
		setParent(source, CadseGCST.TYPE_DEFINITION_lt_ATTRIBUTES);

		this._min = 0;
		this._max = -1;
		this._destination = null;
		this._selection = null;
		this._intID = -1;

	}

	public LinkTypeImpl(ItemDelta item) {
		super(item);
		this._linkType = CadseCore.theLinkType;
		LinkTypeItemDeltaAdapter _delta = new LinkTypeItemDeltaAdapter(item);
		this._parent = _delta.getSource();
		this._destination = _delta.getDestination();
		this._kind = _delta.getKind();
		this._min = _delta.getMin();
		this._max = _delta.getMax();
		this._selection = null;
		this._intID = -1;
	}

	@Override
	public void finishLoad() {
		if (_destination instanceof ItemTypeItemDeltaAdapter) {
			_destination = getLogicalWorkspace().getItemType(_destination.getId());
		}
		if (_parent instanceof ItemTypeItemDeltaAdapter) {
			_parent = getLogicalWorkspace().getItemType(_parent.getId());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.INamed#getIntID()
	 */
	public int getIntID() {
		return _intID;
	}

	/**
	 * Gets the type.
	 * 
	 * @return link type.
	 */
	public LinkType getLinkType() {
		return _linkType;
	}

	/**
	 * Get max.
	 * 
	 * @return the number max
	 */
	@Override
	public int getMax() {
		return _max;
	}

	/**
	 * Get min.
	 * 
	 * @return the number min
	 */
	@Override
	public int getMin() {
		return _min;
	}

	/**
	 * Get destination.
	 * 
	 * @return the destination type.
	 */
	public TypeDefinition getDestination() {
		return _destination;
	}

	@Override
	public TypeDefinition getParent() {
		if (super.getParent() instanceof ItemType) {
			return (TypeDefinition) super.getParent();
		}
		return null;
	}

	/**
	 * Get source.
	 * 
	 * @return the source type
	 */
	public TypeDefinition getSource() {
		return getParent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.LinkType#getKind()
	 */
	public int getKind() {
		return _kind;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.LinkImpl#isAggregation()
	 */
	public boolean isAggregation() {
		return (_kind & (LinkType.AGGREGATION)) != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.LinkImpl#isAnnotation()
	 */
	public boolean isAnnotation() {
		return (_kind & (LinkType.ANNOTATION)) != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.LinkImpl#isPart()
	 */
	public boolean isPart() {
		return (_kind & LinkType.PART) != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.LinkType#isInversePart()
	 */
	public boolean isInversePart() {
		if ((_kind & LinkType.INVERSE_PART) != 0) {
			return true;
		}

		if (_inverse != null && _inverse.isPart()) {
			_kind |= LinkType.INVERSE_PART;
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.LinkImpl#isComposition()
	 */
	public boolean isComposition() {
		return (_kind & LinkType.COMPOSITION) != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.LinkImpl#isRequire()
	 */
	public boolean isRequire() {
		return (_kind & LinkType.REQUIRE) != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.LinkImpl#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof LinkType) {
			if (getSource() != null && getSource().equals(((LinkType) obj).getSource())) {
				return getName() != null && getName().equals(((LinkType) obj).getName());
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.LinkImpl#hashCode()
	 */
	@Override
	public int hashCode() {
		return (getSource() != null ? getSource().hashCode() : -1) ^ getName().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.LinkImpl#toString()
	 */
	@Override
	public String toString() {
		String kindStr = "";
		if (isAggregation()) {
			kindStr += "a";
		}
		if (isComposition()) {
			kindStr += "c";
		}
		if (isPart()) {
			kindStr += "p";
		}
		if (isInversePart()) {
			kindStr += "i";
		}

		if (isRequire()) {
			kindStr += "r";
		}
		if (isDerived()) {
			kindStr += "d";
		}
		if (isGroup()) {
			kindStr += "g";
		}
		if (isAnnotation()) {
			kindStr += "*";
		}

		return "LinkType " + getName() + "[" + kindStr + "] : " + getSource() + " -> " + getDestination();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.LinkImpl#isDerived()
	 */
	public boolean isDerived() {
		return (_kind & LinkType.DERIVED) != 0;
	}

	/**
	 * Gets the selection.
	 * 
	 * @return the selection
	 */
	public String getSelection() {
		return _selection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.LinkType#getInverse()
	 */
	public LinkType getInverse() {
		return this._inverse;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.LinkType#getSelectingDestination(fr.imag.adele
	 * .cadse.core.Item)
	 */
	public Collection<Item> getSelectingDestination(Item source) {
		Collection<Item> ret = null;
		if (_fManager != null) {
			ret = _fManager.getSelectingDestination(source);
			if (ret != null) {
				return ret;
			}
		}

		String sel = _selection;
		if (sel == null) {
			sel = getDestination().getName() + "*";
		}
		EvaluatePath ep = new EvaluatePath(source, this, sel);
		return ep.getResult();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.LinkType#getManager()
	 */
	public ILinkTypeManager getManager() {
		if (_fManager == null) {
			_fManager = new AbstractLinkTypeManager();
			_fManager.setLinkType(this);
		}
		return _fManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.LinkType#setManager(fr.imag.adele.cadse.core
	 * .ILinkTypeManager)
	 */
	public void setManager(ILinkTypeManager manager) {
		_fManager = manager;
		_fManager.setLinkType(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IAttributeType#isMandatory()
	 */
	@Override
	public boolean mustBeInitializedAtCreationTime() {
		if (isDefinedFlag(MUST_BE_INITIALIZED_AT_CREATION_TIME))
			return super.mustBeInitializedAtCreationTime();
		return getMin() > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IAttributeType#getDefaultValue()
	 */
	@Override
	public Link getDefaultValue() {
		return null;
	}

	@Override
	public String getQualifiedDisplayName() {
		if (getSource() == null) {
			return "error no source of " + getName();
		}
		return getSource().getQualifiedDisplayName() + "::" + getName();
	}

	public ItemType getType() {
		return CadseGCST.LINK_TYPE;
	}

	@Override
	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {
		if (CadseGCST.LINK_TYPE_at_AGGREGATION_ == type) {
			return (T) (Boolean) isAggregation();
		}
		if (CadseGCST.LINK_TYPE_at_ANNOTATION_ == type) {
			return (T) (Boolean) isAnnotation();
		}
		if (CadseGCST.LINK_TYPE_at_COMPOSITION_ == type) {
			return (T) (Boolean) isComposition();
		}
		if (CadseGCST.LINK_TYPE_at_PART_ == type) {
			return (T) (Boolean) isPart();
		}
		if (CadseGCST.LINK_TYPE_at_REQUIRE_ == type) {
			return (T) (Boolean) isRequire();
		}
		if (CadseGCST.LINK_TYPE_at_KIND_ == type) {
			return (T) new Integer(_kind);
		}
		if (CadseGCST.LINK_TYPE_at_MAX_ == type) {
			return (T) new Integer(_max);
		}
		if (CadseGCST.LINK_TYPE_at_MIN_ == type) {
			return (T) new Integer(_min);
		}
		if (CadseGCST.LINK_TYPE_at_SELECTION_ == type) {
			return (T) (_selection);
		}
		if (CadseGCST.LINK_TYPE_at_TWCOUPLED_ == type) {
			return (T) Boolean.valueOf(getFlag(Item.EVOL_LINK_TYPE_COUPLED));
		}
		if (CadseGCST.LINK_TYPE_at_TWDEST_EVOL_ == type) {
			if (_twdestEvol == null) {
				_twdestEvol = TWDestEvol.immutable;
			}
			return (T) _twdestEvol;
		}
		return super.internalGetOwnerAttribute(type);
	}

	@Override
	public boolean commitSetAttribute(IAttributeType<?> type, Object value) {
		if (CadseGCST.LINK_TYPE_at_AGGREGATION_ == type) {
			return setKind(LinkType.AGGREGATION, Convert.toBoolean(value, CadseGCST.LINK_TYPE_at_AGGREGATION_, false));
		}
		if (CadseGCST.LINK_TYPE_at_ANNOTATION_ == type) {
			return setKind(LinkType.ANNOTATION, Convert.toBoolean(value, CadseGCST.LINK_TYPE_at_ANNOTATION_, false));
		}
		if (CadseGCST.LINK_TYPE_at_COMPOSITION_ == type) {
			return setKind(LinkType.COMPOSITION, Convert.toBoolean(value, CadseGCST.LINK_TYPE_at_COMPOSITION_, false));
		}
		if (CadseGCST.LINK_TYPE_at_PART_ == type) {
			return setKind(LinkType.PART, Convert.toBoolean(value, CadseGCST.LINK_TYPE_at_PART_, false));
		}
		if (CadseGCST.LINK_TYPE_at_REQUIRE_ == type) {
			return setKind(LinkType.REQUIRE, Convert.toBoolean(value, CadseGCST.LINK_TYPE_at_REQUIRE_, false));
		}
		if (CadseGCST.LINK_TYPE_at_KIND_ == type) {
			_kind = Convert.toInteger(value);
			return true;
		}
		if (CadseGCST.LINK_TYPE_at_MAX_ == type) {
			_max = Convert.toInt(value, null, -1);
			return true;
		}
		if (CadseGCST.LINK_TYPE_at_MIN_ == type) {
			_min = Convert.toInt(value, null, 0);
			return true;
		}
		if (CadseGCST.LINK_TYPE_at_SELECTION_ == type) {
			_selection = Convert.toString(value);
			return true;
		}
		if (CadseGCST.LINK_TYPE_at_TWCOUPLED_ == type) {
			boolean localCoupled = Convert.toBoolean(value, CadseGCST.LINK_TYPE_at_TWCOUPLED_, false);
			if (localCoupled == getFlag(Item.EVOL_LINK_TYPE_COUPLED)) {
				return false;
			}
			setFlag(Item.EVOL_LINK_TYPE_COUPLED, localCoupled);
			return true;
		}
		if (CadseGCST.LINK_TYPE_at_TWDEST_EVOL_ == type) {
			this._twdestEvol = CadseGCST.LINK_TYPE_at_TWDEST_EVOL_.convertTo(value);
			return true;
		}
		return super.commitSetAttribute(type, value);
	}

	private boolean getKind(int f) {
		return (_kind & f) != 0;
	}

	private boolean setKind(int f, boolean flag) {
		boolean oldv = getKind(f);
		if (flag) {
			this._kind |= f;
		} else {
			this._kind &= ~f;
		}
		return oldv != flag;
	}

	@Override
	protected void collectOutgoingLinks(LinkType linkType, CollectedReflectLink ret) {
		if (linkType == CadseGCST.LINK_TYPE_lt_DESTINATION) {
			ret.addOutgoing(CadseGCST.LINK_TYPE_lt_DESTINATION, _destination);
		}
		if (linkType == CadseGCST.LINK_TYPE_lt_SOURCE) {
			ret.addOutgoing(CadseGCST.LINK_TYPE_lt_SOURCE, getSource());
		}
		if (linkType == CadseGCST.LINK_TYPE_lt_INVERSE_LINK) {
			ret.addOutgoing(CadseGCST.LINK_TYPE_lt_INVERSE_LINK, _inverse);
		}
		super.collectOutgoingLinks(linkType, ret);
	}

	@Override
	public Link commitLoadCreateLink(LinkType lt, Item destination) throws CadseException {
		if (lt == CadseGCST.LINK_TYPE_lt_DESTINATION) {
			this._destination = (ItemType) destination;
			if (this.getSource() != null && !this.getSource().isOrphan()) {
				_destination.addIncomingLink(this, true);
			}
			return new ReflectLink(lt, this, destination, 0);
		}
		if (lt == CadseGCST.LINK_TYPE_lt_SOURCE) {
			setParent(destination, lt);
			return new ReflectLink(lt, this, destination, 0);
		}
		if (lt == CadseGCST.LINK_TYPE_lt_INVERSE_LINK) {
			this._inverse = (LinkType) destination;
			return new ReflectLink(lt, this, destination, 0);
		}
		return super.commitLoadCreateLink(lt, destination);
	}

	@Override
	public void removeOutgoingLink(Link linkImpl, boolean notifie) {
		LinkType linkType = linkImpl.getLinkType();
		if (linkType == CadseGCST.LINK_TYPE_lt_DESTINATION) {
			if (!this.getSource().isOrphan()) {
				_destination.removeIncomingLink(this, notifie);
			}
			// Do not set to null. The destination must be keep for information
			// : _destination = null;
		}
		if (linkType == CadseGCST.LINK_TYPE_lt_SOURCE) {
			// source = null;
		}
		if (linkType == CadseGCST.LINK_TYPE_lt_INVERSE_LINK) {
			_inverse = null;
		}
	}

	public void setInverseLinkType(LinkType lt) {
		this._inverse = lt;
	}

	public Class<Link> getAttributeType() {
		return Link.class;
	}

	/**
	 * Gets the resolved destination.
	 * 
	 * @return destination of this link.
	 * 
	 *         NOTE: Si ce lien est non-resolu, avant de retouner l'objet,
	 *         essayer de r�cup�rer cet item dans le workspace. Si non trouv�,
	 *         retourner null.
	 */
	public Item getResolvedDestination() {
		return getDestination(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Link#getDestination(boolean)
	 */
	public Item getDestination(boolean resolved) {
		if (!resolved) {
			return getDestination();
		}
		// cas particulier si la source est dans l'etat new, le lien n'est pas
		// resolu.
		// nous faisons une resolution lazy.
		if (getSource().isOrphan() && !(_destination.isResolved() && _destination.isOrphan())) {
			return getLogicalWorkspace().getItem(_destination.getId());
		}
		return isResolved() ? _destination : null;
	}

	/**
	 * Gets the source id.
	 * 
	 * @return id source.
	 */
	public UUID getSourceId() {
		return getSource().getId();
	}

	/**
	 * Gets the destination id.
	 * 
	 * @return id destination
	 */
	public UUID getDestinationId() {
		if (_destination == null)
			return null;
		return _destination.getId();
	}

	/**
	 * Sets the read only.
	 * 
	 * @param readOnly
	 *            the read only
	 */
	@Override
	public void setReadOnly(boolean readOnly) {
		if ((getLinkType().getKind() & LinkType.READ_ONLY) == 0) {
			if (readOnly) {
				_kind |= LinkType.READ_ONLY;
			} else {
				_kind &= ~LinkType.READ_ONLY;
			}
		}
	}

	/**
	 * Checks if is read only.
	 * 
	 * @return readOnly
	 */
	@Override
	public boolean isReadOnly() {
		if (getSource() != null && getSource().isReadOnly()) {
			return true;
		}
		return (_kind & LinkType.READ_ONLY) != 0;
	}

	/**
	 * Sets the hidden.
	 * 
	 * @param hidden
	 *            the hidden
	 */
	public void setHidden(boolean hidden) {
		if ((getLinkType().getKind() & LinkType.HIDDEN) == 0) {
			if (hidden) {
				_kind |= LinkType.HIDDEN;
			} else {
				_kind &= ~LinkType.HIDDEN;
			}
		}
	}
	
	@Override
	public void setIsGroup(boolean b) {
		if (b) {
			_kind |= LinkType.GROUP;
		} else {
			_kind &= ~LinkType.GROUP;
		}		
	}

	@Override
	public boolean isGroup() {
		return (_kind & LinkType.GROUP) != 0;
	}
	
	/**
	 * Checks if is hidden.
	 * 
	 * @return hidden
	 */
	@Override
	public boolean isHidden() {
		return (_kind & LinkType.HIDDEN) != 0;
	}

	/**
	 * Delete a link.
	 * 
	 * NOTE: D�tacher les references de la source et de la destination point� �
	 * ce lien. Si la destination de ce lien est un contenu, supprimer le aussi.
	 */
	public void delete() {
		if (isReadOnly()) {
			throw new CadseIllegalArgumentException(" It is not possible to delete link "
					+ "{0} of entity {1} to {2} because it readonly.", _linkType.getName(), getSource().getId(),
					getDestinationId());
		}
		try {
			Accessor.delete(this, true);
		} catch (CadseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Resolve a link.
	 * 
	 * NOTE: Ce m�thode essaie de r�cup�rer par id dans le workspace l'object
	 * destination de ce lien .
	 * 
	 * @return true, if resolve
	 */
	public boolean resolve() {
		if (getSource().isOrphan()) {
			return false;
		}
		if (!_destination.isResolved()) {
			LogicalWorkspace ws = getLogicalWorkspace();
			Item it = ws.getItem(this._destination.getId());
			if (it != null && it != _destination) {
				_destination.removeIncomingLink(this, true);
				this._destination = (ItemType) it;
				_destination.addIncomingLink(this, true);
				return true;
			}
		}
		return false;
	}

	/**
	 * Recompute destination.
	 * 
	 * @param aDestination
	 *            the a destination
	 */
	void recomputeDestination(ItemImpl aDestination) {
		this._destination = (ItemType) aDestination;
	}

	/**
	 * test si le lien est resolu.
	 * 
	 * @return true, if checks if is resolved
	 */
	public boolean isLinkResolved() {
		return _destination != null && _destination.isResolved();
	}

	/**
	 * restore un lien non r�solu.
	 * 
	 * @throws CadseException
	 *             the melusine exception
	 */
	public void restore() throws CadseException {
	}

	@Override
	public String getDisplayName() {
		if (this._displayName != null) {
			return _displayName;
		}
		return super.getDisplayName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Link#getDestinationType()
	 */
	public ItemType getDestinationType() {
		return _destination.getType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Link#getDestinationUniqueName()
	 */
	public String getDestinationQualifiedName() {
		return _destination.getQualifiedName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Link#getDestinationShortName()
	 */
	/**
	 * @deprecated Use {@link #getDestinationName()} instead
	 */
	@Deprecated
	public String getDestinationShortName() {
		return getDestinationName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Link#getDestinationShortName()
	 */
	public String getDestinationName() {
		return _destination.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Link#getIndex()
	 */
	public int getIndex() {
		return getSource().getOutgoingLinks(getLinkType()).indexOf(this);
	}

	public boolean isNonCircular() {
		return (_kind & (LinkType.NON_CIRCULAR)) != 0;
	}

	public boolean isOrdered() {
		return (_kind & (LinkType.ORDERED)) != 0;
	}

	public IAttributeType<?> getLinkTypeAttributeType(String key) {
		return null;
	}

	public void destroy() throws CadseException {
		// TODO Auto-generated method stub

	}

	public void commitDelete() throws CadseException {
		getSource().removeOutgoingLink(this, false);
		if (!getSource().isOrphan()) {
			_destination.removeIncomingLink(this, false);
		}
	}

	public void addCompatibleVersions(int... versions) {
		// TODO Auto-generated method stub

	}

	public void clearCompatibleVersions() {
		// TODO Auto-generated method stub

	}

	public int[] getCompatibleVersions() {
		// TODO Auto-generated method stub
		return null;
	}

	private LogicalWorkspaceTransactionListener[]	workspaceLogiqueCopyListeners;

	private IAttributeType<?>[] _attributeDefinitions;

	public void addLogicalWorkspaceTransactionListener(LogicalWorkspaceTransactionListener l) {
		workspaceLogiqueCopyListeners = ArraysUtil.add(LogicalWorkspaceTransactionListener.class,
				workspaceLogiqueCopyListeners, l);
	}

	public void removeLogicalWorkspaceTransactionListener(LogicalWorkspaceTransactionListener l) {
		workspaceLogiqueCopyListeners = ArraysUtil.remove(LogicalWorkspaceTransactionListener.class,
				workspaceLogiqueCopyListeners, l);
	}

	public LogicalWorkspaceTransactionListener[] getLogicalWorkspaceTransactionListener() {
		return workspaceLogiqueCopyListeners;
	}

	public void setTWDestEvol(TWDestEvol destEvol) {
		_twdestEvol = destEvol;
	}

	public void setTWCoupled(boolean twCoupled) {
		setFlag(Item.EVOL_LINK_TYPE_COUPLED, twCoupled);
	}

	@Override
	public boolean isTWValueModified(Object oldValue, Object newValue) {
		return !Convert.equals(oldValue, newValue);
	}

	@Override
	public void setParent(Item parent, LinkType lt) {
		if (parent != null && !(parent instanceof ItemType)) {
			throw new CadseIllegalArgumentException("parent is not of class ItemType : linktype is " + parent.getName()
					+ "::" + getName());
		}
		super.setParent(parent, lt);
	}

	/**
	 * method du link
	 */
	
	@Override
	public boolean commitSetAttribute(IAttributeType<?> type, String key,
			Object value) {
		return false;
	}

	@Override
	public UIField generateDefaultField() {
		if (getMax() != 1) {
			return new UIFieldImpl(CadseGCST.DLIST, UUID.randomUUID(), this, getDisplayName(), EPosLabel.defaultpos, 
					new MC_Descriptor(CadseGCST.MC_LINK), 
					new IC_Descriptor(CadseGCST.IC_LINK_FOR_BROWSER_COMBO_LIST));
		}
		return new UIFieldImpl(CadseGCST.DBROWSER, UUID.randomUUID(), this, getDisplayName(), EPosLabel.defaultpos, 
				new MC_Descriptor(CadseGCST.MC_LINK), 
				new IC_Descriptor(CadseGCST.IC_LINK_FOR_BROWSER_COMBO_LIST));
	}
	
	@Override
	public Link convertTo(Object v) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UUID getDestinationCadseId() {
		if (_destination == null)
			return null;
		CadseRuntime cr = _destination.getCadse();
		if (cr == null)
			return null;
		return cr.getId();
	}

	@Override
	public UUID getSourceCadseId() {
		if (getSource() == null)
			return null;
		CadseRuntime cr = getSource().getCadse();
		if (cr == null)
			return null;
		return cr.getId();
	}

	@Override
	public boolean isInterCadseLink() {
		UUID scId = getSourceCadseId();
		UUID dcId = getDestinationCadseId();
		return !((scId == null && dcId == null) 
				|| (scId != null && dcId != null && scId.equals(dcId)));
	}

	@Override
	public IAttributeType<?>[] getLinkTypeAttributeTypes() {
		if (_attributeDefinitions == null)
			return new IAttributeType[0];
		return _attributeDefinitions;
	}

	@Override
	public <T> T getLinkAttributeOwner(IAttributeType<T> attDef) {
		return getAttribute(attDef);
	}
	
	public void addLinkTypeAttributeType(IAttributeType<?> att) {
		_attributeDefinitions = ArraysUtil.add(IAttributeType.class, _attributeDefinitions, att);
	}

}
