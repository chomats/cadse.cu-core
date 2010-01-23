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

import java.util.UUID;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CommonMethods;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.db.DBObject;
import fr.imag.adele.cadse.core.impl.internal.AbstractGeneratedItem;
import fr.imag.adele.cadse.core.impl.internal.Accessor;
import fr.imag.adele.teamwork.db.ModelVersionDBException;

public class ReflectLink extends DBObject implements Link {
	int			_flag;
	LinkType	_linkType;
	Item		_source;
	Item		_destination;
	int			_index;

	public ReflectLink(LinkType lt, Item source, Item dest, int index, int flag) {
		if (lt == null) {
			throw new NullPointerException("link " + source + " -- >" + dest + " null type");
		}
		if (dest == null) {
			throw new NullPointerException("link " + source + " --" + lt + "-- > null destination");
		}
		if (source == null) {
			throw new NullPointerException("link null source --" + lt + "-- > " + dest);
		}
		if (!(dest instanceof AbstractGeneratedItem)) {
			// throw new IllegalArgumentException("Bad type of dest type " +
			// dest.getClass());
		}
		this._linkType = lt;
		this._source = source;
		this._destination = dest;
		this._index = index;
		this._flag = flag;
	}

	public ReflectLink(LinkType lt, Item source, Item dest, int index) {
		this(lt, source, dest, index, 0);
	}

	public void delete() throws CadseException {
		Accessor.delete(this);
	}

	public Item getDestination() {
		return _destination;
	}

	public Item getDestination(boolean resolved) {
		return _destination;
	}

	public UUID getDestinationId() {
		return _destination.getId();
	}

	/**
	 * @deprecated Use {@link #getDestinationName()} instead
	 */
	public String getDestinationShortName() {
		return getDestinationName();
	}

	public String getDestinationName() {
		return _destination.getName();
	}

	public ItemType getDestinationType() {
		return _destination.getType();
	}

	public String getDestinationQualifiedName() {
		return _destination.getQualifiedName();
	}

	public int getIndex() {
		if (_index == -1) {
			return _source.indexOf(this);
		}
		return _index;
	}

	public LinkType getLinkType() {
		return _linkType;
	}

	public Item getResolvedDestination() {
		return _destination;
	}

	public Item getSource() {
		return _source;
	}

	public UUID getSourceId() {
		return _source.getId();
	}

	public int getVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isAggregation() {
		return _linkType.isAggregation();
	}

	public boolean isAnnotation() {
		return _linkType.isAnnotation();
	}

	public boolean isComposition() {
		return _linkType.isComposition();
	}

	public boolean isDerived() {
		return _linkType.isDerived();
	}

	public boolean isHidden() {
		return _linkType.isHidden() || (_flag & Item.IS_HIDDEN) != 0;
	}

	public boolean isPart() {
		return _linkType.isPart();
	}

	public boolean isReadOnly() {
		return _linkType.isReadOnly();
	}

	public boolean isRequire() {
		return _linkType.isRequire();
	}

	public boolean isLinkResolved() {
		return _destination != null && _destination.isResolved();
	}

	public void moveAfter(Link link) throws CadseException {
		// assert link.getLinkType() == lt;
		// if (link.getIndex() != index && source instanceof IManageReflectLink)
		// ((IManageReflectLink)source).moveAfter(this, link);
		Accessor.moveAfter(this, link);
	}

	public void moveBefore(Link link) throws CadseException {
		// assert link.getLinkType() == lt;
		// if (link.getIndex() != index && source instanceof IManageReflectLink)
		// ((IManageReflectLink)source).moveBefore(this, link);
		Accessor.moveBefore(this, link);
	}

	public void restore() throws CadseException {
		// TODO Auto-generated method stub

	}

	public void setHidden(boolean hidden) {
		// TODO Auto-generated method stub

	}

	public void setReadOnly(boolean readOnly) {
		// TODO Auto-generated method stub

	}

	public void setVersion(int version) {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		return CommonMethods.toStringLink(this);
	}

	public void destroy() throws CadseException {
		commitDelete();
	}

	public void commitDelete() throws CadseException {
		_source.removeOutgoingLink(this, false);
		// if (!source.isOrphan())
		_destination.removeIncomingLink(this, false);
	}

	public boolean resolve() {
		if (getSource().isOrphan()) {
			return false;
		}
		if (!_destination.isResolved()) {
			LogicalWorkspace ws = _source.getLogicalWorkspace();
			Item it = ws.getItem(this._destination.getId());
			if (it != null && it != _destination) {
				_destination.removeIncomingLink(this, true);
				this._destination = it;
				_destination.addIncomingLink(this, true);
				return true;
			}
		}
		return false;
	}

	public boolean commitSetAttribute(IAttributeType<?> type, String key, Object value) {
		return false;
	}

	public void addCompatibleVersions(int... versions) {
		

	}

	public void clearCompatibleVersions() {
		// TODO Auto-generated method stub

	}

	public int[] getCompatibleVersions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ReflectLink) {
			ReflectLink otherLink = (ReflectLink) obj;
			return this._linkType.equals(otherLink._linkType) && this._source.equals(otherLink._source)
					&& this._destination.equals(otherLink._destination);

		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return _linkType.hashCode() ^ _destination.hashCode() ^ _source.hashCode();
	}

	public boolean isStatic() {
		return false;
	}


	@Override
	public UUID getDestinationCadseId() {
		return getDestination().getCadse().getId();
	}


	@Override
	public <T> T getLinkAttributeOwner(IAttributeType<T> attDef) {
		try {
			return (T) _dblw.getDB().getObjectValue(getObjectId(), attDef.getObjectId());
		} catch (ModelVersionDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public UUID getSourceCadseId() {
		return getSource().getCadse().getId();
	}

	@Override
	public boolean isInterCadseLink() {
		return getSource().getCadse() != getDestination().getCadse();
	}
}
