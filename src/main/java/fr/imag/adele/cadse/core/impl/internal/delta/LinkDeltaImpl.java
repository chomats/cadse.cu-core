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
package fr.imag.adele.cadse.core.impl.internal.delta;

import java.util.UUID;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.WSModelState;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.CadseIllegalArgumentException;
import fr.imag.adele.cadse.core.impl.db.DBObject;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransaction;
import fr.imag.adele.cadse.core.transaction.delta.DeleteOperation;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.transaction.delta.LinkDelta;
import fr.imag.adele.cadse.core.transaction.delta.OperationTypeCst;
import fr.imag.adele.cadse.core.transaction.delta.SetAttributeOperation;
import fr.imag.adele.cadse.util.ArraysUtil;

public final class LinkDeltaImpl extends ItemOrLinkDeltaImpl implements Link,
		LinkDelta {

	/** The type. */
	private LinkType _lt;

	/** The destination. */
	private ItemDelta _destination;
	private int[] _compatibleVersions = null;

	private int _objectID;

	public LinkDeltaImpl(ItemDeltaImpl parent, LinkType lt,
			ItemDelta destination, Link original, int index, boolean loaded) {
		super(OperationTypeCst.LINK_OPERATION, parent);
		this._destination = destination;
		this._lt = lt;
		addInParent();
		if (parent.getWorkingCopy().getState() != WSModelState.COPY_WRITE) {
			return;
		}

		if (original != null) {
			setAttribute(CadseGCST.LINK_TYPE_TYPE_at_VERSION_, original
					.getVersion(), original.getVersion(), loaded);
			setAttribute(CadseGCST.LINK_TYPE_TYPE_at_READ_ONLY_, original
					.isReadOnly(), original.isReadOnly(), loaded);
		}
		this.setAttribute(CadseGCST.LINK_TYPE_TYPE_at_INDEX_OF_, index, index,
				loaded);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#getLinkType()
	 */
	public String getLinkTypeName() {
		return _lt.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#equals(java.lang
	 * .Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof LinkDeltaImpl) {
			LinkDeltaImpl lk = (LinkDeltaImpl) obj;
			return this._parent.equals(lk._parent) && _lt.equals(lk._lt)
					&& _destination.getId().equals(lk._destination.getId());
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#hashCode()
	 */
	@Override
	public int hashCode() {
		return _parent.hashCode() ^ _lt.hashCode()
				^ _destination.getId().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#delete(fr.imag.
	 * adele.cadse.core.delta.DeleteOperation)
	 */
	public void delete(DeleteOperation options) throws CadseException {
		getWorkingCopy().check_write();
		getWorkingCopy().validateDeleteLink(this);
		// isCreated -> remove,
		// isDelted -> nothing
		// else -> add detele operation
		if (this._createOperation != null) {
			removeInParent();
			getWorkingCopy().notifyCancelCreatedLink(this);
		} else if (this._deleteOperation != null) {
			; // nothing
		} else {
			if (isLoaded())
				setLoaded(false);

			DeleteOperationImpl deleteItemOperation = new DeleteOperationImpl(
					this, (DeleteOperationImpl) options);
			setDeleteOperation(deleteItemOperation);
			deleteItemOperation.addInParent();
			getItemOperationParent().removeInOrderArray(this);
			setLoaded(false);
			getWorkingCopy().notifyDeletedLink(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#delete()
	 */
	public void delete() throws CadseException {
		delete(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#getItemOperationParent
	 * ()
	 */
	public ItemDeltaImpl getItemOperationParent() {
		return (ItemDeltaImpl) this._parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#getWLC()
	 */
	public LogicalWorkspaceTransaction getWLC() {
		return ((ItemDelta) this._parent).getWorkingCopy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#getDestination()
	 */
	public ItemDelta getDestination() {
		return this._destination;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#getDestination(
	 * boolean)
	 */
	public ItemDelta getDestination(boolean resolved) {
		return getDestination();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#getDestinationId()
	 */
	public UUID getDestinationId() {
		return this._destination.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#getDestinationShortName
	 * ()
	 */
	/**
	 * @deprecated Use {@link #getDestinationName()} instead
	 */
	public String getDestinationShortName() {
		return getDestinationName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#getDestinationShortName
	 * ()
	 */
	public String getDestinationName() {
		return this._destination.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#getDestinationType
	 * ()
	 */
	public ItemType getDestinationType() {
		return _destination.getType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seefr.imag.adele.cadse.core.internal.delta.LinkOperation#
	 * getDestinationUniqueName()
	 */
	public String getDestinationQualifiedName() {
		return this._destination.getQualifiedName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#getIndex()
	 */
	public int getIndex() {
		int index = (Integer) getAttributeWithDefaultValue(
				CadseGCST.LINK_TYPE_TYPE_at_INDEX_OF_, -1);
		if (index == -1) {
			index = getItemOperationParent().indexOf(this);
		}
		return index;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#setIndex(int,
	 * boolean)
	 */
	public void setIndex(int index, boolean loaded) {
		setAttribute(CadseGCST.LINK_TYPE_TYPE_at_INDEX_OF_, index, null, loaded);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#getInfo()
	 */
	public String getInfo() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#getResolvedDestination
	 * ()
	 */
	public ItemDelta getResolvedDestination() {
		return getDestination(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#getSource()
	 */
	public ItemDeltaImpl getSource() {
		return (ItemDeltaImpl) _parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#getSourceId()
	 */
	public UUID getSourceId() {
		return getSource().getId();
	}

	@Override
	public void removeInParent() {
		super.removeInParent();
		getSource().removeLink(this);
		_destination.removeIncomingLink(this, false);
	}

	@Override
	public void addInParent() {
		super.addInParent();
		getSource().addLink(this);
		_destination.addIncomingLink(this, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#getVersion()
	 */
	public int getVersion() {
		return (Integer) getAttributeWithDefaultValue(
				CadseGCST.LINK_TYPE_TYPE_at_VERSION_, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#isAggregation()
	 */
	public boolean isAggregation() {
		return getLinkType().isAggregation();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#isAnnotation()
	 */
	public boolean isAnnotation() {
		return getLinkType().isAnnotation();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#isComposition()
	 */
	public boolean isComposition() {
		return getLinkType().isComposition();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#isDerived()
	 */
	public boolean isDerived() {
		return getLinkType().isDerived();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#isHidden()
	 */
	public boolean isHidden() {
		LinkType linkTypeObject = getLinkType();
		if (linkTypeObject == null) {
			return false;
		}
		return linkTypeObject.isHidden();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#isOther()
	 */
	public boolean isOther() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#isPart()
	 */
	public boolean isPart() {
		LinkType linkTypeObject = getLinkType();
		if (linkTypeObject == null) {
			return false;
		}
		return linkTypeObject.isPart();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#isReadOnly()
	 */
	public boolean isReadOnly() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#isRequire()
	 */
	public boolean isRequire() {
		return getLinkType().isRequire();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#isLinkResolved()
	 */
	public boolean isLinkResolved() {
		ItemDelta dest = getDestination();
		return dest != null && !dest.isDeleted();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#moveAfter(fr.imag
	 * .adele.cadse.core.Link)
	 */
	public void moveAfter(Link link) throws CadseException {
		getItemOperationParent().moveAfter(this, link);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#moveBefore(fr.imag
	 * .adele.cadse.core.Link)
	 */
	public void moveBefore(Link link) throws CadseException {
		getItemOperationParent().moveBefore(this, link);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#restore()
	 */
	public void restore() throws CadseException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#setHidden(boolean)
	 */
	public void setHidden(boolean hidden) {
		setAttribute(CadseGCST.LINK_TYPE_TYPE_at_HIDDEN_, hidden, null, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#setInfo(java.lang
	 * .String)
	 */
	public void setInfo(String info) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#setInfo(java.lang
	 * .String, boolean)
	 */
	public void setInfo(String info, boolean loaded) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#setAttribute(java
	 * .lang.String, java.lang.Object, java.lang.Object, boolean)
	 */
	public void setAttribute(IAttributeType<?> key, Object v, Object oldValue,
			boolean loaded) {
		getWorkingCopy().check_write();
		try {
			SetAttributeOperation attrOld = getSetAttributeOperation(key);
			if (attrOld != null && attrOld.getOldValue() != null) {
				oldValue = attrOld.getOldValue();
			}

			SetAttributeOperationImpl attr = new SetAttributeOperationImpl(
					this, key, v, oldValue);
			attr.setLoaded(loaded);
			add(attr);
			if (!loaded) {
				getWorkingCopy().notifyChangeAttribute(this, attr);
			}
		} catch (CadseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#setReadOnly(boolean
	 * )
	 */
	public void setReadOnly(boolean readOnly) {
		setAttribute(CadseGCST.LINK_TYPE_TYPE_at_READ_ONLY_, readOnly, null,
				false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#setVersion(int)
	 */
	public void setVersion(int version) {
		setAttribute(CadseGCST.LINK_TYPE_TYPE_at_VERSION_, version, null, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#setVersion(int,
	 * boolean)
	 */
	public void setVersion(int version, boolean loaded) {
		setAttribute(CadseGCST.LINK_TYPE_TYPE_at_VERSION_, version, null,
				loaded);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#getLinkType()
	 */
	public LinkType getLinkType() {
		return _lt;
	}

	public static LinkType getLinkByName(String name, ItemType sourceType,
			ItemType destType) {
		LinkType _lt_object = sourceType.getOutgoingLinkType(name);
		if (_lt_object == null) {
			return DBObject._dblw.createUnresolvedLinkType(null, name,
					sourceType, destType);
		}
		return _lt_object;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#getHandleIdentifier
	 * ()
	 */
	public String getHandleIdentifier() {
		// source.getHandleIdentifier()+
		// dest.getHandleIdentifier()+
		// lt
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#toString(java.lang
	 * .String, java.lang.StringBuilder, java.lang.String)
	 */
	public void toString(String begin, StringBuilder sb, String tab) {
		sb.append(tab);
		sb.append(begin);
		if (isDeleted()) {
			sb.append("Delete ");
		}
		if (isAdded()) {
			sb.append("Added ");
		}
		if (isLoaded()) {
			sb.append("Loaded ");
		}
		sb.append("Link ");
		sb.append("(").append(getSource().getId()).append(",").append(_lt)
				.append(",");
		sb.append(getDestinationName()).append("-");
		sb.append(getDestinationQualifiedName()).append("-");
		sb.append(getDestinationId()).append(")").append("\n");
		toStringAttributes(sb, tab + "  ");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString("", sb, "");
		return sb.toString();
	}

	@Override
	public LogicalWorkspaceTransaction getWorkingCopy() {
		return getSourceOperation().getWorkingCopy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#getSourceOperation
	 * ()
	 */
	public ItemDelta getSourceOperation() {
		return (ItemDelta) _parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#getDestinationOperation
	 * ()
	 */
	public ItemDelta getDestinationOperation() throws CadseException {
		return _destination;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#mustDeleteDestination
	 * ()
	 */
	public boolean mustDeleteDestination() {
		// TODO Auto-generated method stub
		ItemDelta dest;
		try {
			dest = getDestinationOperation();
		} catch (CadseException e) {
			return false;
		}
		return isPart() && !(dest.isDeleted());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#mustDeleteSource()
	 */
	public boolean mustDeleteSource() {
		return isAnnotation() && !(getSourceOperation().isDeleted());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#getAttributeType
	 * (fr.imag.adele.cadse.core.delta.SetAttributeOperation)
	 */
	@Override
	public IAttributeType<?> getAttributeType(
			SetAttributeOperation setAttributeOperation) {
		LinkType lt = getLinkType();
		return lt.getLinkTypeAttributeType(setAttributeOperation
				.getAttributeName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#destroy()
	 */
	public void destroy() throws CadseException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#commitDelete()
	 */
	public void commitDelete() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#resolve()
	 */
	public boolean resolve() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#commitSetAttribute
	 * (fr.imag.adele.cadse.core.attribute.IAttributeType, java.lang.String,
	 * java.lang.Object)
	 */
	public boolean commitSetAttribute(IAttributeType<?> type, String key,
			Object value) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#addCompatibleVersions
	 * (int)
	 */
	public void addCompatibleVersions(int... versions) {
		_compatibleVersions = ArraysUtil.add(_compatibleVersions, versions);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#clearCompatibleVersions
	 * ()
	 */
	public void clearCompatibleVersions() {
		_compatibleVersions = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#getCompatibleVersions
	 * ()
	 */
	public int[] getCompatibleVersions() {
		return _compatibleVersions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#isMappingLink()
	 */
	public boolean isMappingLink() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#getInverseLink()
	 */
	public LinkDelta getInverseLink() {
		LinkDelta ret = null;
		LinkType lt = getLinkType();
		if (lt != null) {
			LinkType inverselt = lt.getInverse();
			if (inverselt != null) {
				ItemDelta itemDestination = getDestination();
				if (itemDestination == null) {
					return null;
				}
				ret = itemDestination.getOutgoingLink(inverselt, getSourceId());
			}
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.delta.LinkOperation#isCreatedLink()
	 */
	public boolean isCreatedLink() {
		return getCreateOperation() != null;
	}

	// return the link in the workspace logique
	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#getBaseLink()
	 */
	public Link getBaseLink() {
		Item itemBase = getSource().getBaseItem();
		if (itemBase == null) {
			return null;
		}
		LinkType linkType = getLinkType();
		if (linkType == null) {
			return null;
		}
		return itemBase.getOutgoingLink(linkType, getDestinationId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#isStatic()
	 */
	public boolean isStatic() {
		Link linkBase = getBaseLink();
		return linkBase != null && linkBase.isStatic();
	}

	@Override
	public void changeDestination(Item att) {
		// destination is key in parent.
		// probleme si le lien exist deja
		LinkDelta l;
		try {
			l = getSource().getOutgoingLink(getLinkType().getId(),
					getLinkTypeName(), att.getId());
		} catch (CadseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		if (l != null) {
			throw new CadseIllegalArgumentException(
					"link allready exist : cannot change destination to "
							+ att.getName(), this, l, att);
		}
		removeInParent();
		_destination = getWLC().loadItem(att);
		addInParent();
	}

	@Override
	public UUID getDestinationCadseId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getLinkAttributeOwner(IAttributeType<T> attDef) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UUID getSourceCadseId() {
		if (getSource() == null)
			return null;
		if (getSource().getCadse() == null)
			return null;
		return getSource().getCadse().getId();
	}

	@Override
	public boolean isInterCadseLink() {
		return false;
	}

	@Override
	public UUID getId() {
		return null;
	}

	@Override
	public int getObjectId() {
		return _objectID;
	}

	@Override
	public void setUUID(long uuidMsb, long uuidLsb) {
	}

	@Override
	public void setUUID(UUID uuid) {
	}

	@Override
	public <T> T adapt(Class<T> clazz) {
		return null;
	}

	@Override
	public void setObjectID(int linkId) {
		_objectID = linkId;
	}

}
