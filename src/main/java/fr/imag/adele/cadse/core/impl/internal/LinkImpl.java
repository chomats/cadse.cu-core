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

import java.util.UUID;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CommonMethods;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransaction;
import fr.imag.adele.cadse.core.util.Convert;
import fr.imag.adele.cadse.util.ArraysUtil;

/**
 * A link is a relation between two items. Each link has a type <tt>lt</tt>
 * and a kind <tt>kind</tt>. Value of <tt>kind</tt> is determined following
 * value <tt>kind</tt> of type <tt>lt</tt>. A link has origin is
 * <tt>source</tt> and destination is <tt>destination</tt>. It has also two
 * caracteristics hidden and readOnly.
 * 
 * @author nguyent
 * @version 6
 * @date 26/09/05
 */

/*
 * 2/09/05 - correction du champ type qui a �t� chang� fortuitement en LinkType. -
 * ajout du concept de item orphelin : un lien ayant un item orphelin comme
 * source ne peut etre r�solu. - ajout de la method getItemSourceId (racourci de
 * source.getId())
 * 
 */

public class LinkImpl extends DBObject implements Link {

	
	/** The source. */
	private final Item		source;

	/** The destination. */
	private Item			destination;	// can not be null but
	// can be not resolved

	/** The link type. */
	private final LinkType	linkType;

	/** The kind. */
	private int				kind;

	/** The info. */
	private String			info;

	/** The version. */
	private int				version;
	
	int[]	compatibleVersions	= null;
	/**
	 * Instanciate an unresolved link. <br/>
	 * 
	 * NOTE: This constructor is package visible only, as it SHOULD only be used
	 * by Item in order to assure model's global coherence when creating a new
	 * link.
	 * 
	 * @param source :
	 *            link's origin.
	 * @param lt :
	 *            link type.
	 * @param destination :
	 *            link's destination id.
	 */
	LinkImpl(int objectId, Item source, LinkType lt, Item destination) {
		this(objectId, source, lt, destination, true);
	}

	/**
	 * Instanciate an unresolved link. <br/>
	 * 
	 * NOTE: This constructor is package visible only, as it SHOULD only be used
	 * by Item in order to assure model's global coherence when creating a new
	 * link.
	 * 
	 * @param source :
	 *            link's origin.
	 * @param lt :
	 *            link type.
	 * @param destination
	 *            the destination
	 * @param addInIncommingList
	 *            the add in incomming list
	 * @param dblw 
	 */
	LinkImpl(int objectId, 
			Item source, LinkType lt, Item destination, boolean addInIncommingList) {
		super(objectId);
        assert source != null;
		assert destination != null;
		if (lt == null && this instanceof LinkType) {
			lt = (LinkType) this;
		}
		assert lt != null;
		if (lt.isNatif() && destination.isResolved()) {
			throw new IllegalArgumentException("cannot create a link natif resolved " + lt.getName());
		}
		if (lt == null) {
			throw new NullPointerException("link " + source + " -- >" + destination + " null type");
		}
		this.source = source;
		this.destination = destination;

		this.linkType = lt;
		this.kind = lt.getKind();

		if (!source.isOrphan() && addInIncommingList) {
			destination.addIncomingLink(this, false);
		}
	}

	/**
	 * Gets the type.
	 * 
	 * @return link type.
	 */
	public LinkType getLinkType() {
		return linkType;
	}

	/**
	 * Gets the source.
	 * 
	 * @return source of this link.
	 */
	public Item getSource() {
		return source;
	}

	/**
	 * Gets the resolved destination.
	 * 
	 * @return destination of this link.
	 * 
	 * NOTE: Si ce lien est non-resolu, avant de retouner l'objet, essayer de
	 * r�cup�rer cet item dans le workspace. Si non trouv�, retourner null.
	 */
	public Item getResolvedDestination() {
		return getDestination(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Link#getDestination()
	 */
	public Item getDestination() {
		return destination;
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
		if (source.isOrphan() && !(destination.isResolved() && destination.isOrphan())) {
			return source.getLogicalWorkspace().getItem(destination.getId());
		}
		return isLinkResolved() ? destination : null;
	}

	/**
	 * Gets the source id.
	 * 
	 * @return id source.
	 */
	public UUID getSourceId() {
		return source.getId();
	}

	/**
	 * Gets the destination id.
	 * 
	 * @return id destination
	 */
	public UUID getDestinationId() {
		return destination.getId();
	}

	/**
	 * Sets the read only.
	 * 
	 * @param readOnly
	 *            the read only
	 */
	public void setReadOnly(boolean readOnly) {
		if ((getLinkType().getKind() & LinkType.READ_ONLY) == 0) {
			if (readOnly) {
				kind |= LinkType.READ_ONLY;
			} else {
				kind &= ~LinkType.READ_ONLY;
			}
		}
	}

	/**
	 * Checks if is read only.
	 * 
	 * @return readOnly
	 */
	public boolean isReadOnly() {
		if (source.isReadOnly()) {
			return true;
		}
		return (kind & LinkType.READ_ONLY) != 0;
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
				kind |= LinkType.HIDDEN;
			} else {
				kind &= ~LinkType.HIDDEN;
			}
		}
	}

	/**
	 * Checks if is hidden.
	 * 
	 * @return hidden
	 */
	public boolean isHidden() {
		return (kind & LinkType.HIDDEN) != 0;
	}

	/**
	 * Delete a link.
	 * 
	 * NOTE: D�tacher les references de la source et de la destination point� �
	 * ce lien. Si la destination de ce lien est un contenu, supprimer le aussi.
	 */
	@Deprecated
	public void delete() throws CadseException {
		Accessor.delete(this);
	};

	public void destroy() throws CadseException {
		((ItemImpl) source).removeOutgoingLink(this);
		if (!source.isOrphan()) {
			((AbstractItem) destination).removeIncomingLink(this, true);
		}
	}

	public void commitDelete() throws CadseException {
		source.removeOutgoingLink(this, false);
		if (!source.isOrphan()) {
			destination.removeIncomingLink(this, false);
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
		if (!destination.isResolved()) {
			LogicalWorkspace ws = source.getLogicalWorkspace();
			Item it = ws.getItem(this.destination.getId());
			if (it != null && it != destination) {
				destination.removeIncomingLink(this, true);
				this.destination = it;
				destination.addIncomingLink(this, true);
				return true;
			}
		}
		return false;
	}

	/**
	 * Unresolve a link.
	 */
	public void unresolve() {
		if (getSource().isOrphan()) {
			return;
		}

		try {
			destination = ((LogicalWorkspaceImpl) source.getLogicalWorkspace()).getItem(destination.getId(),
					destination.getType(), destination.getQualifiedName(), destination.getName());
		} catch (CadseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		destination.addIncomingLink(this, false);
	}

	/**
	 * NOTE: This method is not implemented here, it will be implemented in
	 * vetical.
	 * 
	 * @return .
	 */
	public boolean isModified() {
		return (kind & (LinkType.MODIFIED)) != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return CommonMethods.toStringLink(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Link#isAnnotation()
	 */
	public boolean isAnnotation() {
		return (kind & (LinkType.ANNOTATION)) != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Link#isAggregation()
	 */
	public boolean isAggregation() {
		return (kind & (LinkType.AGGREGATION)) != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Link#isPart()
	 */
	public boolean isPart() {
		return (kind & LinkType.PART) != 0;
	}

	/**
	 * Recompute destination.
	 * 
	 * @param aDestination
	 *            the a destination
	 */
	void recomputeDestination(ItemImpl aDestination) {
		this.destination = aDestination;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Link#isComposition()
	 */
	public boolean isComposition() {
		return (kind & LinkType.COMPOSITION) != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Link#isRequire()
	 */
	public boolean isRequire() {
		return (kind & LinkType.REQUIRE) != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Link#isDerived()
	 */
	public boolean isDerived() {
		return (kind & LinkType.DERIVED) != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Link) {
			Link l = (Link) obj;
			return l.getSource().equals(getSource()) && l.getLinkType().equals(getLinkType())
					&& l.getDestinationId().equals(getDestinationId());
		}
		return super.equals(obj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getSource().getObjectID()^ getLinkType().getObjectID() ^ getDestinationId().hashCode();
	}

	/**
	 * test si le lien est resolu.
	 * 
	 * @return true, if checks if is resolved
	 */
	public boolean isLinkResolved() {
		return destination.isResolved();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Link#getDestinationType()
	 */
	public ItemType getDestinationType() {
		return destination.getType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Link#getDestinationUniqueName()
	 */
	public String getDestinationQualifiedName() {
		return destination.getQualifiedName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Link#getDestinationShortName()
	 */
	public String getDestinationName() {
		return destination.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Link#moveAfter(fr.imag.adele.cadse.core.Link)
	 */
	public void moveAfter(Link link) throws CadseException {

		// ((ItemImpl)this.source).moveAfter(this,link);

		LogicalWorkspaceTransaction copy = getSource().getLogicalWorkspace().createTransaction();
		copy.getItem(getSourceId()).getOutgoingLinkOperation(this).moveAfter(link);
		copy.commit();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Link#moveBefore(fr.imag.adele.cadse.core.Link)
	 */
	public void moveBefore(Link link) throws CadseException {
		LogicalWorkspaceTransaction copy = getSource().getLogicalWorkspace().createTransaction();
		copy.getItem(getSourceId()).getOutgoingLinkOperation(this).moveBefore(link);
		copy.commit();
		// ((ItemImpl)this.source).moveBefore(this,link);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Link#getIndex()
	 */
	public int getIndex() {
		return source.indexOf(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Link#getVersion()
	 */
	public int getVersion() {
		return version;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Link#setVersion(int)
	 */
	public void setVersion(int version) {
		this.version = version;
	}

	public boolean commitSetAttribute(IAttributeType<?> type, String key, Object value) {
		if (key.equals(CadseGCST.VERSION_KEY)) {
			this.version = Convert.toInt(value, null, -1);
			return true;
		}
		return false;
	}

	

	public void addCompatibleVersions(int... versions) {
		compatibleVersions = ArraysUtil.add(compatibleVersions, versions);
	}

	public void clearCompatibleVersions() {
		compatibleVersions = null;
	}

	public int[] getCompatibleVersions() {
		return compatibleVersions;
	}

	public boolean isStatic() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	@Deprecated
	public UUID getDestinationCadseId() {
		if (destination == null)
			return null;
		CadseRuntime cr = destination.getCadse();
		if (cr == null)
			return null;
		return cr.getId();
	}

	@Override
	@Deprecated
	public UUID getSourceCadseId() {
		if (source == null)
			return null;
		CadseRuntime cr = source.getCadse();
		if (cr == null)
			return null;
		return cr.getId();
	}

	@Override
	public boolean isInterCadseLink() {
		UUID scId = getSourceCadseID();
		UUID dcId = getDestinationCadseID();
		return !((scId == null && dcId == null) 
				|| (scId != null && dcId != null && scId.equals(dcId)));
	}

	@Override
	public <T> T getLinkAttributeOwner(IAttributeType<T> attDef) {
		try {
			return (T) _dblw.getDB().getObjectValue(getObjectID(), attDef.getObjectID());
		} catch (ModelVersionDBException e) {
			throw new CadseIllegalArgumentException("Cannot get attribute of {0}", e, attDef);
		}
	}

	@Override
	public UUID getDestinationCadseID() {
		if (destination == null)
			return null;
		CadseRuntime cr = destination.getCadse();
		if (cr == null)
			return null;
		return cr.getId();
	}

	@Override
	public UUID getDestinationID() {
		return source.getId();
	}

	@Override
	public UUID getSourceCadseID() {
		if (source == null)
			return null;
		CadseRuntime cr = source.getCadse();
		if (cr == null)
			return null;
		return cr.getId();
	}

	@Override
	public UUID getSourceID() {
		return source.getId();
	}

	
}
