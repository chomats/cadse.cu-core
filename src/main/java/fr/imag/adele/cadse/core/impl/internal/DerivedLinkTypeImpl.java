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

import java.util.UUID;

import fr.imag.adele.cadse.core.DerivedLinkType;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.LinkType;

/**
 * The Class DerivedLinkTypeImpl.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class DerivedLinkTypeImpl extends LinkTypeImpl implements DerivedLinkType {

	/**
	 * Instantiates a new derived link type impl.
	 * 
	 * @param kind
	 *            the kind
	 * @param source
	 *            the source
	 * @param name
	 *            the name
	 * @param intID
	 *            the int id
	 * @param min
	 *            the min
	 * @param max
	 *            the max
	 * @param selection
	 *            the selection
	 * @param destination
	 *            the destination
	 */
	public DerivedLinkTypeImpl(UUID id, int kind, ItemType source, String name, int intID, int min, int max,
			String selection, LinkType destination) {
		super(id, kind, source, name, intID, min, max, selection, (ItemTypeImpl) destination.getDestination());
		lt = destination;
	}

	/**
	 * Instantiates a new derived link type impl.
	 * 
	 * @param kind
	 *            the kind
	 * @param source
	 *            the source
	 * @param name
	 *            the name
	 * @param min
	 *            the min
	 * @param max
	 *            the max
	 * @param selection
	 *            the selection
	 * @param destination
	 *            the destination
	 */
	public DerivedLinkTypeImpl(UUID id, int kind, ItemType source, String name, int min, int max,
			String selection, LinkType destination) {
		super(id, kind, source, name, min, max, selection, (ItemTypeImpl) destination.getDestination());
		lt = destination;
	}

	/** The lt. */
	LinkType	lt;

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.DerivedLinkType#getOriginLinkType()
	 */
	public LinkType getOriginLinkType() {
		return lt;
	}

}
