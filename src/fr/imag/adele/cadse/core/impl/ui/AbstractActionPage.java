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
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.impl.internal.AbstractGeneratedItem;
import fr.imag.adele.cadse.core.ui.IActionPage;
import fr.imag.adele.cadse.core.ui.IFieldDescription;
import fr.imag.adele.cadse.core.ui.IPage;
import fr.imag.adele.cadse.core.ui.IPageObject;
import fr.imag.adele.cadse.core.ui.Pages;

/**
 * .
 */

abstract public class AbstractActionPage extends AbstractGeneratedItem implements IActionPage {

	/** The page object. */
	protected IPageObject	pageObject;

	/** The item. */
	private Item			item	= null;

	/**
	 * Instantiates a new abstract action page.
	 */
	public AbstractActionPage() {
	}

	/**
	 * Instantiates a new abstract action page.
	 * 
	 * @param item
	 *            the item
	 */
	public AbstractActionPage(Item item) {
		this.item = item;
	}

	/**
	 * Gets the parent item.
	 * 
	 * @return the parent item
	 */
	protected Item getParentItem() {
		return (Item) pageObject.get(IFieldDescription.PARENT_CONTEXT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IActionPage#doCancel(java.lang.Object)
	 */
	public void doCancel(Object monitor) {
		if (pageObject instanceof Pages) {
			for (IPage p : ((Pages) pageObject).getPages()) {
				IActionPage a = p.getActionPage();
				if (a != null) {
					try {
						a.doCancel(monitor);
					} catch (Throwable e) {
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IActionPage#doFinish(java.lang.Object)
	 */
	public void doFinish(Object monitor) throws Exception {
		if (pageObject instanceof Pages) {
			for (IPage p : ((Pages) pageObject).getPages()) {
				IActionPage a = p.getActionPage();
				if (a != null) {
					try {
						a.doFinish(monitor);
					} catch (Throwable e) {
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IActionPage#doNextPageAction(java.lang.Object,
	 *      fr.imag.adele.cadse.core.ui.Pages, int)
	 */
	public void doNextPageAction(Object monitor, Pages pages, int currentPage) throws Exception {
		if (pageObject instanceof Pages) {
			IPage p = pages.getPage(currentPage);
			IActionPage a = p.getActionPage();
			if (a != null) {
				a.doNextPageAction(monitor, pages, currentPage);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IActionPage#doPrevPageAction(java.lang.Object,
	 *      fr.imag.adele.cadse.core.ui.Pages, int)
	 */
	public void doPrevPageAction(Object monitor, Pages pages, int currentPage) throws Exception {
		if (pageObject instanceof Pages) {
			IPage p = pages.getPage(currentPage);
			IActionPage a = p.getActionPage();
			if (a != null) {
				a.doPrevPageAction(monitor, pages, currentPage);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IActionPage#getNextPageIndex(fr.imag.adele.cadse.core.ui.Pages,
	 *      int)
	 */
	public int getNextPageIndex(Pages pages, int currentPage) throws Exception {
		if (pageObject instanceof Pages) {
			IPage p = pages.getPage(currentPage);
			IActionPage a = p.getActionPage();
			if (a != null) {
				return a.getNextPageIndex(pages, currentPage);
			}
		}
		if (pages.size() == currentPage + 1) {
			return -1;
		}
		return currentPage + 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IActionPage#getPrevPageIndex(fr.imag.adele.cadse.core.ui.Pages,
	 *      int)
	 */
	public int getPrevPageIndex(Pages pages, int currentPage) throws Exception {
		if (pageObject instanceof Pages) {
			IPage p = pages.getPage(currentPage);
			IActionPage a = p.getActionPage();
			if (a != null) {
				return a.getPrevPageIndex(pages, currentPage);
			}
		}
		if (0 == currentPage) {
			return -1;
		}
		return currentPage - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IActionPage#init(fr.imag.adele.cadse.core.ui.IPageObject)
	 */
	public void init(IPageObject pageObject) throws CadseException {
		this.pageObject = pageObject;
		if (item != null) {
			pageObject.setItem(getTypeId(), item);
		}
	}

	public Item getItem() {
		return this.pageObject.getItem();
	}

	/**
	 * Inits the after ui.
	 */
	public void initAfterUI() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IActionPage#dispose()
	 */
	public void dispose() {
	}

	public String getTypeId() {
		return getClass().getName();
	}

	public ItemType getType() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setParent(Item parent, LinkType lt) {
		// TODO Auto-generated method stub

	}

}