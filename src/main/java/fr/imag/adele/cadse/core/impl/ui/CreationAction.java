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
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemState;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.content.ContentItem;
import fr.imag.adele.cadse.core.impl.CadseCore;
import fr.imag.adele.cadse.core.ui.IActionPage;
import fr.imag.adele.cadse.core.ui.UIPlatform;
import fr.imag.adele.cadse.core.ui.view.NewContext;

/**
 * The Class CreationAction.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class CreationAction extends AbstractActionPage implements IActionPage {


	private NewContext _context;

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
	public CreationAction(NewContext c) {
		super();
		this._context = c;
	}

	

	// TODO : il faut garder avant
	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.impl.ui.AbstractActionPage#doCancel(java.lang.Object)
	 */
	@Override
	public void doCancel(UIPlatform uiPlatform, Object monitor) {
		Item item = uiPlatform.getItem(null);
		
		if (item != null && item.getState() == ItemState.NOT_IN_WORKSPACE) {
			try {
				cancelContent(item);
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
	protected Item getParentItem() {
		return _context.getPartParent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.impl.ui.AbstractActionPage#doFinish(java.lang.Object)
	 */
	@Override
	public void doFinish(UIPlatform uiPlatform, Object monitor) throws Exception {
		String shortname = getFinishAutomaticShortName();
		if (shortname != null) {
			CadseCore.setName(uiPlatform.getItem(null), shortname);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.impl.ui.AbstractActionPage#init(fr.imag.adele.cadse.core.ui.IPageObject)
	 */
	@Override
	public void init(UIPlatform uiPlatform) throws CadseException {
		Item parentItem = _context.getPartParent();

		ItemType type = _context.getDestinationType();
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
			if ( _context.getPartLinkType() == null) {
				//_context.set = type.getIncomingPart(parentItem.getType());
			}
			if ( _context.getPartLinkType() == null) {
				throw new CadseException("Not find a linktype from {0} to {1} of type part.", parentItem.getType()
						.getName(), type.getName());
			}
			if (! _context.getPartLinkType().isPart()) {
				throw new CadseException("Not find a linktype from {0} to {1} of type part.", parentItem.getType()
						.getName(), type.getName());
			}
		}

		Item createItem = createItem(uiPlatform);
		uiPlatform.setVariable(getTypeId(), createItem);
		String shortname = getInitAutomaticShortName();
		if (shortname != null) {
			CadseCore.setName(createItem, shortname);
		}
	}

	/**
	 * Compute the default or generic short name;.
	 * 
	 * @return the inits the automatic short name
	 */
	protected String getInitAutomaticShortName() {
		return _context.getDefaultName();
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
	protected Item createItem(UIPlatform uiPlatform) throws CadseException {
		return this._context.getNewItem();
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
		return _context.getDestinationType();
	}

	public String getDefaultName() {
		return _context.getDefaultName();
	}

	public LinkType getParentLT() {
		return _context.getPartLinkType();
	}
	

	@Override
	public String getTypeId() {
		return _context.getDestinationType().getId().toString();
	}

	public NewContext getContext() {
		return _context;
	}
}
