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
 *
 * Copyright (C) 2006-2010 Adele Team/LIG/Grenoble University, France
 */
package fr.imag.adele.cadse.core.impl;

import java.text.MessageFormat;
import java.util.Collection;

import fr.imag.adele.cadse.core.ILinkTypeManager;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.Messages;

/**
 * The default implementation for the link type manager. It's used to manage all
 * the life cycle of links of a given type. It contains a lot of callbacks
 * called automatically before creation, after creation... It's also used to
 * allow or forbid operations such as link creation or delete operation.
 * 
 * @author Cadse Team
 */
public class AbstractLinkTypeManager implements ILinkTypeManager {

	/** The link type. */
	private LinkType	linkType;

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ILinkTypeManager#compare(fr.imag.adele.cadse.core.Link,
	 *      fr.imag.adele.cadse.core.Link)
	 */
	public int compare(Link l1, Link l2) {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ILinkTypeManager#getSelectingDestination(fr.imag.adele.cadse.core.Item)
	 */
	public Collection<Item> getSelectingDestination(Item source) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ILinkTypeManager#setLinkType(fr.imag.adele.cadse.core.LinkType)
	 */
	public void setLinkType(LinkType lt) {
		linkType = lt;
		if (linkType == null) {
			CadseCore.getCadseDomain().error(getLinkType(), "linkType is null", new NullPointerException());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ILinkTypeManager#getLinkType()
	 */
	public LinkType getLinkType() {

		return linkType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ILinkTypeManager#isOutgoingLinkSorted()
	 */
	public boolean isOutgoingLinkSorted() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ILinkTypeManager#canCreateLink(fr.imag.adele.cadse.core.Item,
	 *      fr.imag.adele.cadse.core.Item, fr.imag.adele.cadse.core.LinkType)
	 */
	public String canCreateLink(Item source, Item destination, LinkType lt) {
		Link l = source.getOutgoingLink(lt, destination.getId());
		if (l != null) {
			return MessageFormat.format(Messages.error_link_alreay_exist, l);
		}
		if (this.equals(destination)) {
			return Messages.error_item_cannot_reference_itself;
		}

		return linkType.getSource().getItemManager().canCreateLink(source, destination, lt);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ILinkTypeManager#canDeleteLink(fr.imag.adele.cadse.core.Link)
	 */
	public String canDeleteLink(Link l) {
		if (l.isAnnotation()) {
			return Messages.error_cannot_delete_annotation_link;
		}
		if (l.getLinkType().isPart() && l.isLinkResolved()) {
			return Messages.error_cannot_delete_resolved_part_link;
		}
		return linkType.getSource().getItemManager().canDeleteLink(l);
	}

}
