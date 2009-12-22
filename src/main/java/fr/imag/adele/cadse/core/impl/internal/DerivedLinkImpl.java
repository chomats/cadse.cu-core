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

import fr.imag.adele.cadse.core.DerivedLink;
import fr.imag.adele.cadse.core.DerivedLinkType;
import fr.imag.adele.cadse.core.Item;

/**
 * The Class DerivedLinkImpl.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class DerivedLinkImpl extends LinkImpl implements DerivedLink {

	/**
	 * Instantiates a new derived link impl.
	 * 
	 * @param source
	 *            the source
	 * @param lt
	 *            the lt
	 * @param destination
	 *            the destination
	 * @param addInIncommingList
	 *            the add in incomming list
	 */
	public DerivedLinkImpl(Item source, DerivedLinkType lt, Item destination, boolean addInIncommingList) {
		super(source, lt, destination, addInIncommingList);
	}

	/**
	 * Instantiates a new derived link impl.
	 * 
	 * @param source
	 *            the source
	 * @param lt
	 *            the lt
	 * @param destination
	 *            the destination
	 */
	public DerivedLinkImpl(Item source, DerivedLinkType lt, Item destination) {
		super(source, lt, destination);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.DerivedLink#getDerivedType()
	 */
	public DerivedLinkType getDerivedType() {
		return (DerivedLinkType) getLinkType();
	}
}
