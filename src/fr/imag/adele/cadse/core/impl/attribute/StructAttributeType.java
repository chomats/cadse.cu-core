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
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.attribute.IComplexAttributeType;
import fr.imag.adele.cadse.core.impl.CollectedReflectLink;
import fr.imag.adele.cadse.core.CadseRootCST;

/**
 * The Class StructAttributeType.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class StructAttributeType extends AttributeType implements IAttributeType<Map>, IComplexAttributeType {

	/** The defs. */
	final private IAttributeType<? extends Object>[]	defs;

	/**
	 * Instantiates a new struct attribute type.
	 * 
	 * @param id
	 *            the id
	 * @param min
	 *            the min
	 * @param defs
	 *            the defs
	 */
	public StructAttributeType(CompactUUID id, String name, int min, IAttributeType<? extends Object>[] defs) {
		super(id, name, min == 1 ? MUST_BE_INITIALIZED_AT_CREATION_TIME : 0);
		this.defs = defs;
		if (this.defs != null) {
			for (IAttributeType<? extends Object> att : defs) {
				att.setParent(this, CadseRootCST.STRUCT_ATTRIBUTE_TYPE_lt_FIELDS);
			}
		}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IAttributeType#getAttributeType()
	 */
	public Class<Map> getAttributeType() {
		return Map.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IComplexAttributeType#getAttributeType(java.lang.String)
	 */
	public IAttributeType<? extends Object> getAttributeType(String id) {
		for (IAttributeType<?> d : defs) {
			if (d.getName().equals(id)) {
				return d;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IComplexAttributeType#getAttributeTypeIds()
	 */
	public String[] getAttributeTypeIds() {
		String[] ret = new String[defs.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = defs[i].getName();
		}
		return ret;
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
		return CadseRootCST.STRUCT_ATTRIBUTE_TYPE;
	}

	@Override
	protected void collectOutgoingLinks(LinkType linkType, CollectedReflectLink ret) {
		if (linkType == CadseRootCST.STRUCT_ATTRIBUTE_TYPE_lt_FIELDS) {
			ret.addOutgoing(CadseRootCST.STRUCT_ATTRIBUTE_TYPE_lt_FIELDS, defs);
		}
		super.collectOutgoingLinks(linkType, ret);
	}

}
