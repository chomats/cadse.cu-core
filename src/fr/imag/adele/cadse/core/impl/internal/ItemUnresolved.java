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

import java.util.Set;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.ContentItem;
import fr.imag.adele.cadse.core.DerivedLinkDescription;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.LinkType;

/**
 * The Class ItemUnresolved.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class ItemUnresolved extends AbstractItem implements Item {

	/**
	 * Instantiates a new item unresolved.
	 * 
	 * @param modelInstance
	 *            the model instance
	 * @param id
	 *            the id
	 * @param type
	 *            the type
	 * @param uniqueName
	 *            the unique name
	 * @param shortName
	 *            the short name
	 */
	public ItemUnresolved(LogicalWorkspaceImpl modelInstance, CompactUUID id, ItemType type, String uniqueName,
			String shortName) {
		super(modelInstance, id, type, uniqueName, shortName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#buildComposite()
	 */
	@Override
	public void buildComposite() throws CadseException {
		throw new UnsupportedOperationException();
	}

	public void setParent(Item parent, LinkType lt) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#delete(boolean)
	 */
	@Override
	public void delete(boolean deleteContent) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#isModified()
	 */
	@Override
	public boolean isModified() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#isOrphan()
	 */
	@Override
	public boolean isOrphan() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#isReadOnly()
	 */
	@Override
	public boolean isReadOnly() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#isValid()
	 */
	@Override
	public boolean isValid() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#shadow(boolean)
	 */
	@Override
	public void shadow(boolean deleteContent) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#unload()
	 */
	@Override
	public void unload() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.AbstractItem#isResolved()
	 */
	@Override
	public boolean isResolved() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#getContentItem()
	 */
	@Override
	public ContentItem getContentItem() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#isAccessible()
	 */
	@Override
	public boolean isAccessible() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#setUniqueName(java.lang.String)
	 */
	@Override
	public void setUniqueName(String longname) {
		throw new UnsupportedOperationException();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#setShortName(java.lang.String)
	 */
	@Override
	public void setShortName(String shortname) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#setDerivedLinks(java.util.Set)
	 */
	@Override
	public void setDerivedLinks(Set<DerivedLinkDescription> derivedLinks) {
		throw new UnsupportedOperationException();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#itemHasContent()
	 */
	@Override
	public boolean itemHasContent() {
		return false;
	}

	@Override
	public boolean contentIsLoaded() {
		return false;
	}

}
