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

package fr.imag.adele.cadse.core.impl.attribute;

import java.util.HashMap;
import java.util.Map;

import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.attribute.IComplexAttributeType;
import fr.imag.adele.cadse.core.delta.ItemDelta;
import fr.imag.adele.cadse.core.impl.CollectedReflectLink;
import fr.imag.adele.cadse.core.impl.ReflectLink;
import fr.imag.adele.cadse.core.CadseRootCST;

/**
 * The Class MapAttributeType.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class MapAttributeType<X, Y> extends AttributeType implements IAttributeType<Map<X, Y>>, IComplexAttributeType {

	/** The Constant KEY_TYPE. */
	final private static String					KEY_TYPE	= "key-type";

	/** The Constant VALUE_TYPE. */
	final private static String					VALUE_TYPE	= "value-type";

	/** The key type. */
	private IAttributeType<? extends Object>	keyType;

	/** The value type. */
	private IAttributeType<? extends Object>	valueType;

	/**
	 * Instantiates a new map attribute type.
	 * 
	 * @param id
	 *            the id
	 * @param min
	 *            the min
	 * @param keytype
	 *            the keytype
	 * @param valuetype
	 *            the valuetype
	 */
	public MapAttributeType(CompactUUID id, String name, int min, IAttributeType<? extends Object> keytype,
			IAttributeType<? extends Object> valuetype) {
		super(id, name, min == 1 ? MUST_BE_INITIALIZED_AT_CREATION_TIME : 0);
		this.keyType = keytype;
		this.valueType = valuetype;
		this.keyType.setParent(this, CadseRootCST.MAP_ATTRIBUTE_TYPE_lt_KEY_TYPE);
		this.valueType.setParent(this, CadseRootCST.MAP_ATTRIBUTE_TYPE_lt_VALUE_TYPE);
	}

	public MapAttributeType(ItemDelta item) {
		super(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IAttributeType#getDefaultValue()
	 */
	@Override
	public Map getDefaultValue() {
		return new HashMap<Object, Object>();
	}

	@Override
	protected void collectOutgoingLinks(LinkType linkType, CollectedReflectLink ret) {
		if (linkType == CadseRootCST.MAP_ATTRIBUTE_TYPE_lt_KEY_TYPE) {
			ret.addOutgoing(linkType, keyType);
		}
		if (linkType == CadseRootCST.MAP_ATTRIBUTE_TYPE_lt_VALUE_TYPE) {
			ret.addOutgoing(linkType, valueType);
		}
		super.collectOutgoingLinks(linkType, ret);
	}

	@Override
	public Link commitLoadCreateLink(LinkType lt, Item destination) {
		if (lt == CadseRootCST.MAP_ATTRIBUTE_TYPE_lt_KEY_TYPE) {
			keyType = (IAttributeType<?>) destination;
			return new ReflectLink(lt, this, keyType, 0);
		}
		if (lt == CadseRootCST.MAP_ATTRIBUTE_TYPE_lt_VALUE_TYPE) {
			valueType = (IAttributeType<?>) destination;
			return new ReflectLink(lt, this, valueType, 0);
		}
		return super.commitLoadCreateLink(lt, destination);
	}

	@Override
	public void removeOutgoingLink(Link link, boolean notifie) {
		Item destination = link.getDestination();
		LinkType lt = link.getLinkType();

		if (lt == CadseRootCST.MAP_ATTRIBUTE_TYPE_lt_KEY_TYPE && destination.isResolved()) {
			keyType = null;
			return;
		}
		if (lt == CadseRootCST.MAP_ATTRIBUTE_TYPE_lt_VALUE_TYPE && destination.isResolved()) {
			valueType = null;
			return;
		}
		super.removeOutgoingLink(link, notifie);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IAttributeType#getAttributeType()
	 */
	public Class<Map<X, Y>> getAttributeType() {
		return (Class<Map<X, Y>>) (Class<?>) Map.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IComplexAttributeType#getAttributeType(java.lang.String)
	 */
	public IAttributeType<? extends Object> getAttributeType(String id) {
		if (KEY_TYPE.equals(id)) {
			return keyType;
		}
		if (VALUE_TYPE.equals(id)) {
			return valueType;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IComplexAttributeType#getAttributeTypeIds()
	 */
	public String[] getAttributeTypeIds() {
		return new String[] { KEY_TYPE, VALUE_TYPE };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.INamed#getIntID()
	 */
	public int getIntID() {
		// TODO Auto-generated method stub
		return 0;
	}

	public ItemType getType() {
		return CadseRootCST.MAP_ATTRIBUTE_TYPE;
	}
}
