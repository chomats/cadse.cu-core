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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.attribute.CheckStatus;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.CollectedReflectLink;
import fr.imag.adele.cadse.core.impl.ReflectLink;
import fr.imag.adele.cadse.core.impl.ui.UIFieldImpl;
import fr.imag.adele.cadse.core.impl.ui.ic.IC_Descriptor;
import fr.imag.adele.cadse.core.impl.ui.mc.MC_DefaultForList;
import fr.imag.adele.cadse.core.impl.ui.mc.MC_Descriptor;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.ui.EPosLabel;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.ui.UIPlatform;

/**
 * The Class ListAttributeType.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class ListAttributeType<X> extends AttributeType implements
		fr.imag.adele.cadse.core.attribute.ListAttributeType<X> {

	/** The Constant SUBTYPE. */
	private static final String	SUBTYPE	= "subtype";

	/** The min. */
	private int					min;

	/** The max. */
	private int					max;

	/** The subtype. */
	IAttributeType<X>			subtype;

	/**
	 * Instantiates a new list attribute type.
	 * 
	 * @param id
	 *            the id
	 * @param min
	 *            the min
	 * @param max
	 *            the max
	 * @param subtype
	 *            the subtype
	 */
	public ListAttributeType(UUID id, int flag, String name, int min, int max, IAttributeType<X> subtype) {
		super(id, name, min > 0 ? MUST_BE_INITIALIZED_AT_CREATION_TIME : 0 | flag);
		this.min = min;
		this.max = max;
		if (subtype != null) {
			this.subtype = subtype;
			this.subtype.setParent(this, CadseGCST.LIST_lt_SUB_TYPE);
		}
	}

	public ListAttributeType(ItemDelta item) {
		super(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IAttributeType#getDefaultValue()
	 */
	@Override
	public List getDefaultValue() {
		return new ArrayList<Object>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IAttributeType#getMax()
	 */
	@Override
	public int getMax() {
		return max;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IAttributeType#getMin()
	 */
	@Override
	public int getMin() {
		return min;
	}

	public IAttributeType<X> getSubAttributeType() {
		return subtype;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IAttributeType#getAttributeType()
	 */
	public Class<List<X>> getAttributeType() {

		Class<List> c = List.class;
		return (Class<List<X>>) (Class<?>) c;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IComplexAttributeType#getAttributeType(java.lang.String)
	 */
	public IAttributeType<? extends Object> getAttributeType(String id) {
		if (SUBTYPE.equals(id)) {
			return subtype;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IComplexAttributeType#getAttributeTypeIds()
	 */
	public String[] getAttributeTypeIds() {
		return new String[] { SUBTYPE };
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

	@Override
	public CheckStatus check(Item item, Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof List) {
			int i = 0;
			for (Object o : (List) value) {
				CheckStatus error = subtype.check(item, o);
				if (error != null) {
					return new CheckStatus(error.getType(), "In element {0} : {1}", i, error);
				}
				i++;
			}
			return null; // no error
		}
		return new CheckStatus(UIPlatform.ERROR, "Must be a list of {0}", subtype.getClass().getSimpleName());
	}

	public ItemType getType() {
		return CadseGCST.LIST;
	}

	@Override
	protected void collectOutgoingLinks(LinkType linkType, CollectedReflectLink ret) {
		if (CadseGCST.LIST_lt_SUB_TYPE == linkType) {
			ret.addOutgoing(CadseGCST.LIST_lt_SUB_TYPE, subtype);
		}
		super.collectOutgoingLinks(linkType, ret);
	}

	@Override
	public Link commitLoadCreateLink(LinkType lt, Item destination) throws CadseException {
		if (lt == CadseGCST.LIST_lt_SUB_TYPE) {
			subtype = (IAttributeType<X>) destination;
			this.subtype.setParent(this, CadseGCST.LIST_lt_SUB_TYPE);
			return new ReflectLink(lt, this, destination, 0);
		}
		return super.commitLoadCreateLink(lt, destination);
	}

	@Override
	public void removeOutgoingLink(Link link, boolean notifie) {
		Item destination = link.getDestination();
		LinkType lt = link.getLinkType();

		if (lt == CadseGCST.LIST_lt_SUB_TYPE && destination.isResolved()) {
			subtype = null;
			return;
		}
		super.removeOutgoingLink(link, notifie);
	}

	@Override
	public List convertTo(Object v) {
		// TODO Auto-generated method stub
		return (List) v;
	}
	
	@Override
	public UIField generateDefaultField() {
		return new UIFieldImpl(CadseGCST.DLIST, UUID.randomUUID(), this, getDisplayName(), EPosLabel.top, 
				new MC_Descriptor(CadseGCST.MC_LIST_OF_STRING), 
				null);
		
	}
}
