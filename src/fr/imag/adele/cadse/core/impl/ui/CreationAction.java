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

package fr.imag.adele.cadse.core.impl.ui;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseUtil;
import fr.imag.adele.cadse.core.ContentItem;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemState;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.impl.CadseCore;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransaction;
import fr.imag.adele.cadse.core.ui.IActionPage;
import fr.imag.adele.cadse.core.ui.IPageObject;

/**
 * The Class CreationAction.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class CreationAction extends AbstractActionPage implements IActionPage {

	/** The default name. */
	private final String	defaultName;

	/** The parent item. */
	private Item			parentItem;

	/** The parent lt. */
	private LinkType		parentLT;

	/** The type. */
	private final ItemType	type;

	/**
	 * Instantiates a new creation action.
	 * 
	 * @param parent
	 *            the parent
	 * @param type
	 *            the type
	 * @param lt
	 *            the lt
	 * @param defaultName
	 *            the default name
	 */
	public CreationAction(Item parent, ItemType type, LinkType lt, final String defaultName) {
		super();
		this.defaultName = defaultName;
		this.type = type;
		this.parentLT = lt;
		this.parentItem = parent;
	}

	/**
	 * Instantiates a new creation action.
	 * 
	 * @param parent
	 *            the parent
	 * @param type
	 *            the type
	 * @param lt
	 *            the lt
	 */
	public CreationAction(Item parent, ItemType type, LinkType lt) {
		super();
		this.defaultName = null;
		this.type = type;
		this.parentLT = lt;
		this.parentItem = parent;
	}

	// TODO : il faut garder avant
	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.impl.ui.AbstractActionPage#doCancel(java.lang.Object)
	 */
	@Override
	public void doCancel(Object monitor) {
		super.doCancel(monitor);
		Item item = getItem();
		if ((parentItem != null) && (parentLT != null)) {
			// for (Link l : new ArrayList<Link>(parentItem.getOutgoingLinks()))
			// {
			// if (l.getType().equals(parentLT) &&
			// l.getDestinationId().equals(item.getId())) {
			// l.delete();
			// }
			// }
		}
		if (item.getState() == ItemState.NOT_IN_WORKSPACE) {
			cancelContent(item);
		}
		// if (item != null) {
		// if (theLinkType != null)
		// for (Link l : createdItem.getIncomingLinks()) {
		// if (l.getType().equals(theLinkType)) {
		// l.delete();
		// }
		// }
		// try {
		// createdItem.shadow(true);
		// } catch (CadseException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }

	}

	/**
	 * Cancel content.
	 * 
	 * @param creatingItem
	 *            the creating item
	 */
	private void cancelContent(Item creatingItem) {
		if (!creatingItem.contentIsLoaded()) {
			return;
		}
		ContentItem cm = creatingItem.getContentItem();
		if (cm != null) {
			try {
				cm.delete();
			} catch (CadseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.impl.ui.AbstractActionPage#getParentItem()
	 */
	@Override
	protected Item getParentItem() {
		return parentItem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.impl.ui.AbstractActionPage#doFinish(java.lang.Object)
	 */
	@Override
	public void doFinish(Object monitor) throws Exception {

		super.doFinish(monitor);
		String shortname = getFinishAutomaticShortName();
		if (shortname != null) {
			CadseCore.setName(getItem(), shortname);
		}
		// Item creatingItem = getItem();

		// /pageObject.putLocal(IFieldDescription.CREATED_ITEM, creatingItem);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.impl.ui.AbstractActionPage#init(fr.imag.adele.cadse.core.ui.IPageObject)
	 */
	@Override
	public void init(IPageObject pageObject) throws CadseException {
		super.init(pageObject);
		parentItem = getParentItem();

		if (type.isPartType()) {
			if (parentItem == null) {
				ItemType[] parentsTypes = CadseUtil.getIncomingPartsType(type);
				if (parentsTypes.length == 1) {
					throw new CadseException("Not find an item parent of type {0}", parentsTypes[0].getName());
				}
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < parentsTypes.length; i++) {
					if (i != 0) {
						sb.append(", ");
					}
					sb.append(parentsTypes[i].getName());
				}

				throw new CadseException("Not find an item parent of typed {0}", sb.toString());

			}
			if (parentLT == null) {
				parentLT = type.getIncomingPart(parentItem.getType());
			}
			if (parentLT == null) {
				throw new CadseException("Not find a linktype from {0} to {1} of type part.", parentItem.getType()
						.getName(), type.getName());
			}
			if (!parentLT.isPart()) {
				throw new CadseException("Not find a linktype from {0} to {1} of type part.", parentItem.getType()
						.getName(), type.getName());
			}
		}

		pageObject.setItem(getTypeId(), createItem());
		String shortname = getInitAutomaticShortName();
		if (shortname != null) {
			CadseCore.setName(getItem(), shortname);
		}
	}

	/**
	 * Compute the default or generic short name;.
	 * 
	 * @return the inits the automatic short name
	 */
	protected String getInitAutomaticShortName() {
		return this.defaultName;
	}

	/**
	 * Compute the default or generic short name;.
	 * 
	 * @return the finish automatic short name
	 */
	protected String getFinishAutomaticShortName() {
		return null;
	}

	/**
	 * Creates the internal item.
	 * 
	 * @return the item
	 * 
	 * @throws CadseException
	 *             the melusine exception
	 */
	protected Item createItem() throws CadseException {
		// return
		// type.getWorkspaceLogique().createOrphanItem(type,parentItem,parentLT);
		return getCopy().createItem(type, parentItem, parentLT);
	}

	/**
	 * Gets the item type.
	 * 
	 * @return the item type
	 * 
	 * @throws CadseException
	 *             the melusine exception
	 */
	public ItemType getItemType() {
		return type;
	}

	public String getDefaultName() {
		return defaultName;
	}

	public LinkType getParentLT() {
		return parentLT;
	}

	@Override
	public ItemType getType() {
		return type;
	}

	public LogicalWorkspaceTransaction getCopy() {
		return pageObject.getCopy();
	}

	@Override
	public String getTypeId() {
		return type.getId().toString();
	}

}
