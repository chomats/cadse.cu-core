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

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemDescriptionRef;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.WSModelState;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.delta.DeleteOperation;
import fr.imag.adele.cadse.core.delta.ItemDelta;
import fr.imag.adele.cadse.core.delta.LinkDelta;
import fr.imag.adele.cadse.core.delta.OperationTypeCst;
import fr.imag.adele.cadse.core.delta.SetAttributeOperation;
import fr.imag.adele.cadse.core.impl.CadseIllegalArgumentException;
import fr.imag.adele.cadse.core.impl.internal.LinkTypeImpl;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransaction;
import fr.imag.adele.cadse.core.util.ArraysUtil;

public final class LinkDeltaImpl extends ItemOrLinkDeltaImpl implements Link, LinkDelta {

	/** The type. */
	private String				_lt;

	/** The destination. */
	private ItemDelta			_destination;
	private int[]				_compatibleVersions	= null;

	public LinkDeltaImpl(ItemDeltaImpl parent, String lt, ItemDelta destination, Link original, int index,
			boolean loaded)  {
		super(OperationTypeCst.LINK_OPERATION, parent);
		this._destination = destination;
		this._lt = lt;
		addInParent();
		if (parent.getWorkingCopy().getState() != WSModelState.COPY_WRITE) {
			return;
		}

		if (original != null) {
			setAttribute(Item.VERSION_KEY, original.getVersion(), original.getVersion(), loaded);
			setAttribute(Item.IS_READ_ONLY_KEY, original.isReadOnly(), original.isReadOnly(), loaded);
		}
		this.setAttribute(Item.LINK_INDEX_KEY, index, index, loaded);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#getLinkType()
	 */
	public String getLinkTypeName() {
		return _lt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#equals(java.lang.Object)
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
		return _parent.hashCode() ^ _lt.hashCode() ^ _destination.getId().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#delete(fr.imag.adele.cadse.core.delta.DeleteOperation)
	 */
	public void delete(DeleteOperation options) throws CadseException {
		getWorkingCopy().check_write();
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
			
			DeleteOperationImpl deleteItemOperation = new DeleteOperationImpl(this, (DeleteOperationImpl) options);
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
		try {
			delete(null);
		} catch (CadseException e) {
			throw new CadseException(e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#getItemOperationParent()
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
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#getDestination()
	 */
	public ItemDelta getDestination() {
		return this._destination;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#getDestination(boolean)
	 */
	public ItemDelta getDestination(boolean resolved) {
		return getDestination();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#getDestinationId()
	 */
	public CompactUUID getDestinationId() {
		return this._destination.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#getDestinationShortName()
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
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#getDestinationShortName()
	 */
	public String getDestinationName() {
		return this._destination.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#getDestinationType()
	 */
	public ItemType getDestinationType() {
		return _destination.getType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#getDestinationUniqueName()
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
		int index = (Integer) getAttribute(Item.LINK_INDEX_KEY, -1);
		if (index == -1) {
			index = getItemOperationParent().indexOf(this);
		}
		return index;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#setIndex(int,
	 *      boolean)
	 */
	public void setIndex(int index, boolean loaded) {
		setAttribute(Item.LINK_INDEX_KEY, index, null, loaded);
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
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#getResolvedDestination()
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
	public CompactUUID getSourceId() {
		return getSource().getId();
	}

	
	@Override
	public void removeInParent() {
		super.removeInParent();
		getSource().removeLink(this);
		_destination.removeIncomingLink(this, false);
	}
	
	@Override
	public void addInParent()  {
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
		return (Integer) getAttribute(Item.VERSION_KEY, 0);
	}

	private Object getAttribute(String key, Object defaultValue) {
		Object value = getAttribute(key);
		if (value != null) {
			return value;
		}
		return defaultValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#isAggregation()
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
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#isComposition()
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
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#isLinkResolved()
	 */
	public boolean isLinkResolved() {
		ItemDelta dest = getDestination();
		return dest != null && !dest.isDeleted();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#moveAfter(fr.imag.adele.cadse.core.Link)
	 */
	public void moveAfter(Link link) throws CadseException {
		getItemOperationParent().moveAfter(this, link);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#moveBefore(fr.imag.adele.cadse.core.Link)
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
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#setHidden(boolean)
	 */
	public void setHidden(boolean hidden) {
		setAttribute(Item.HIDDEN_ATTRIBUTE, hidden, null, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#setInfo(java.lang.String)
	 */
	public void setInfo(String info) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#setInfo(java.lang.String,
	 *      boolean)
	 */
	public void setInfo(String info, boolean loaded) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#setAttribute(java.lang.String,
	 *      java.lang.Object, java.lang.Object, boolean)
	 */
	public void setAttribute(String key, Object v, Object oldValue, boolean loaded) {
		getWorkingCopy().check_write();
		try {
			SetAttributeOperation attrOld = getSetAttributeOperation(key);
			if (attrOld != null && attrOld.getOldValue() != null) {
				oldValue = attrOld.getOldValue();
			}

			SetAttributeOperationImpl attr = new SetAttributeOperationImpl(this, key, v, oldValue);
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
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#setReadOnly(boolean)
	 */
	public void setReadOnly(boolean readOnly) {
		setAttribute(Item.IS_READ_ONLY_KEY, readOnly, null, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#setVersion(int)
	 */
	public void setVersion(int version) {
		setAttribute(Item.VERSION_KEY, version, null, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#setVersion(int,
	 *      boolean)
	 */
	public void setVersion(int version, boolean loaded) {
		setAttribute(Item.VERSION_KEY, version, null, loaded);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#getLinkType()
	 */
	public LinkType getLinkType() {
		ItemType sourceType = getItemOperationParent().getType();
		if (sourceType == null) {
			return null;
		}

		LinkType _lt_object = sourceType.getOutgoingLinkType(this._lt);
		if (_lt_object == null) {
			ItemType destType = getDestination().getType();
//			// parent			
//			if (_lt.startsWith(LinkTypeImpl.LT_PARENT_SN_PREFIX)) {
//				String inverse_lt = _lt.substring(LinkTypeImpl.LT_PARENT_SN_PREFIX.length() + CompactUUID.STRING_LENGTH
//						+ 1);
//				CompactUUID invid = new CompactUUID(_lt.substring(LinkTypeImpl.LT_PARENT_SN_PREFIX.length(),
//						LinkTypeImpl.LT_PARENT_SN_PREFIX.length() + CompactUUID.STRING_LENGTH));
//				if (destType != null) {
//					LinkType _lt_inverse = destType.getOutgoingLinkType(inverse_lt);
//					if (_lt_inverse != null) {
//						final LinkType retLinkType = ((LinkTypeImpl) _lt_inverse).getInverse(invid);
//						// the name can be change...
//						this._lt = retLinkType.getName();
//						return retLinkType;
//					}
//				}
//			}
			return getWLC().createUnresolvedLinkType(_lt, sourceType, destType);
			// derived ??
		}
		return _lt_object;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#getHandleIdentifier()
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
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#toString(java.lang.String,
	 *      java.lang.StringBuilder, java.lang.String)
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
		sb.append("Link ");
		sb.append("(").append(getSource().getId()).append(",").append(_lt).append(",");
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
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#getSourceOperation()
	 */
	public ItemDelta getSourceOperation() {
		return (ItemDelta) _parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#getDestinationOperation()
	 */
	public ItemDelta getDestinationOperation() throws CadseException {
		return _destination;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#mustDeleteDestination()
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
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#mustDeleteSource()
	 */
	public boolean mustDeleteSource() {
		return isAnnotation() && !(getSourceOperation().isDeleted());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#getAttributeType(fr.imag.adele.cadse.core.delta.SetAttributeOperation)
	 */
	@Override
	public IAttributeType<?> getAttributeType(SetAttributeOperation setAttributeOperation) {
		LinkType lt = getLinkType();
		return lt.getLinkTypeAttributeType(setAttributeOperation.getAttributeName());
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
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#commitSetAttribute(fr.imag.adele.cadse.core.attribute.IAttributeType,
	 *      java.lang.String, java.lang.Object)
	 */
	public boolean commitSetAttribute(IAttributeType<?> type, String key, Object value) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#addCompatibleVersions(int)
	 */
	public void addCompatibleVersions(int... versions) {
		_compatibleVersions = ArraysUtil.add(_compatibleVersions, versions);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#clearCompatibleVersions()
	 */
	public void clearCompatibleVersions() {
		_compatibleVersions = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#getCompatibleVersions()
	 */
	public int[] getCompatibleVersions() {
		return _compatibleVersions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#isMappingLink()
	 */
	public boolean isMappingLink() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#getInverseLink()
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
	 * @see fr.imag.adele.cadse.core.internal.delta.LinkOperation#isCreatedLink()
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
		LinkDelta l = getSource().getOutgoingLink(getLinkTypeName(),att.getId());
		if (l != null) {
			throw new CadseIllegalArgumentException("link allready exist : cannot change destination to "+att.getName(), this, l, att);
		}
		removeInParent();
		_destination = getWLC().loadItem(att);
		addInParent();
	}

}
