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

package fr.imag.adele.cadse.core.impl.internal.ui;

import java.util.Map;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.impl.CadseCore;
import fr.imag.adele.cadse.core.impl.ui.CreationAction;
import fr.imag.adele.cadse.core.impl.ui.ModificationAction;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransaction;
import fr.imag.adele.cadse.core.ui.IActionPage;
import fr.imag.adele.cadse.core.ui.IPage;
import fr.imag.adele.cadse.core.ui.IPageController;
import fr.imag.adele.cadse.core.ui.IPageObject;
import fr.imag.adele.cadse.core.ui.Pages;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.ui.view.FilterContext;
import fr.imag.adele.cadse.core.util.ObjectMap;

/**
 * The Class Pages.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public final class PagesImpl implements Pages {

	/** The _pages. */
	IPage[]								_pages;

	/** The cxts. */
	Map<String, Object>					cxts	= null;

	/** The action. */
	IActionPage							action;

	/** The page controller. */
	private IPageController				pageController;

	private LogicalWorkspaceTransaction	copy;

	private FilterContext				_filterContext;

	/**
	 * Instantiates a new pages.
	 * 
	 * @param action
	 *            the action
	 * @param pages
	 *            the pages
	 */
	public PagesImpl(IActionPage action, IPage... pages) {
		this._pages = pages;
		for (IPage p : pages) {
			p.setPages(this);
		}
		this.action = action;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#getPage(int)
	 */
	public IPage getPage(int index) {
		return _pages[index];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#getPages()
	 */
	public IPage[] getPages() {
		return _pages;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPageObject#getParent()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#getParent()
	 */
	public IPageObject getParent() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPageObject#putLocal(java.lang.String,
	 *      java.lang.Object)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#putLocal(java.lang.String,
	 *      java.lang.Object)
	 */
	public void putLocal(String key, Object value) {
		if (cxts == null) {
			cxts = new ObjectMap<String, Object>(4);
		}
		cxts.put(key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPageObject#getLocal(java.lang.String)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#getLocal(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public <T> T getLocal(String key) {
		if (cxts != null) {
			return (T) cxts.get(key);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPageObject#getLocal(java.lang.String,
	 *      java.lang.Object)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#getLocal(java.lang.String, T)
	 */
	@SuppressWarnings("unchecked")
	public <T> T getLocal(String key, T d) {
		if (cxts != null) {
			T ret = (T) cxts.get(key);
			if (ret != null) {
				return ret;
			}
		}
		return d;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#getField(java.lang.String,
	 *      java.lang.String)
	 */
	public UIField getField(String pageid, String... keyPath) {
		if (keyPath.length <= 0) {
			return null;
		}
		IPage p = getPage(pageid);
		if (p == null) {
			return null;
		}
		return p.getField(keyPath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#getPage(java.lang.String)
	 */
	public IPage getPage(String pageid) {
		for (IPage p : _pages) {
			if (p.getName().equals(pageid)) {
				return p;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#updateField(java.lang.String,
	 *      java.lang.String)
	 */
	public void updateField(String pageid, String... keyPath) {
		final UIField fd = getField(pageid, keyPath);
		if (fd != null) {
			fd.updateValue();

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#initLocal(fr.imag.adele.cadse.core.ui.IPageController)
	 */
	public void initLocal(IPageController pageController) throws CadseException {
		if (copy == null) {
			copy = CadseCore.getLogicalWorkspace().createTransaction();
		}
		if (action != null) {
			action.init(this);
		}
		this.pageController = pageController;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#init(fr.imag.adele.cadse.core.ui.IPageController)
	 */
	public void init(IPageController pageController) throws CadseException {
		if (copy == null) {
			copy = CadseCore.getLogicalWorkspace().createTransaction();
		}

		if (action != null) {
			action.init(this);
		}
		this.pageController = pageController;
		for (IPage page : _pages) {
			page.init(pageController);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPageObject#getPageController()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#getPageController()
	 */
	public IPageController getPageController() {
		return pageController;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#dispose()
	 */
	public void dispose() {
		if (action != null) {
			action.dispose();
		}
		for (IPage page : _pages) {
			page.dispose();
		}
		_filterContext = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPageObject#getField(java.lang.String)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#getField(java.lang.String)
	 */
	public UIField getField(String fieldid) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#doFinish(java.lang.Object)
	 */
	public void doFinish(Object monitor) throws Exception {
		action.doFinish(monitor);
		copy.commit();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#doCancel(java.lang.Object)
	 */
	public void doCancel(Object monitor) {
		action.doCancel(monitor);
		copy.rollback();
	}

	// /**
	// * Gets the action.
	// *
	// * @return the action
	// */
	// public IActionPage getAction() {
	// return action;
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#size()
	 */
	public int size() {
		return _pages.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPageObject#get(java.lang.String)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#get(java.lang.String)
	 */
	public <T> T get(String key) {
		return (T) getLocal(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPageObject#get(java.lang.String,
	 *      java.lang.Object)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#get(java.lang.String, T)
	 */
	public <T> T get(String key, T d) {
		return getLocal(key, d);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#validateFields(fr.imag.adele.cadse.core.ui.UIField,
	 *      fr.imag.adele.cadse.core.ui.IPage)
	 */
	public boolean validateFields(UIField currentField, IPage currentPage) {
		boolean error = false;
		if (currentPage != null) {
			error = currentPage.validateFields(currentField);
		}
		if (error) {
			return error;
		}
		for (IPage page : _pages) {
			if (page == currentPage) {
				continue;
			}
			error = page.validateFields(null);
			if (error) {
				return error;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#getCopy()
	 */
	public LogicalWorkspaceTransaction getCopy() {
		return copy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#runCreationPage()
	 */
	public boolean runCreationPage() {
		return action instanceof CreationAction;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#doNextPageAction(java.lang.Object,
	 *      int)
	 */
	public void doNextPageAction(Object monitor, int currentPage) throws Exception {
		action.doNextPageAction(monitor, this, currentPage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#doPrevPageAction(java.lang.Object,
	 *      int)
	 */
	public void doPrevPageAction(Object monitor, int currentPage) throws Exception {
		action.doPrevPageAction(monitor, this, currentPage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#getNextPageIndex(int)
	 */
	public int getNextPageIndex(int currentPage) throws Exception {
		return action.getNextPageIndex(this, currentPage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#getPrevPageIndex(int)
	 */
	public int getPrevPageIndex(int currentPage) throws Exception {
		return action.getPrevPageIndex(this, currentPage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#getItem()
	 */
	public Item getItem() {
		String typeid = this.action.getTypeId();
		return getLocal(typeid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#setItem(java.lang.String,
	 *      fr.imag.adele.cadse.core.Item)
	 */
	public void setItem(String it, Item item) {
		putLocal(it, item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#setItem(fr.imag.adele.cadse.core.Item)
	 */
	public void setItem(Item item) {
		String typeid = this.action.getTypeId();
		putLocal(typeid, item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#getParentItemType()
	 */
	public ItemType getParentItemType() {
		if (action instanceof ModificationAction) {
			return ((ModificationAction) action).getItemType();
		}
		if (action instanceof CreationAction) {
			return ((CreationAction) action).getItemType();
		}
		return null;
		// throw new UnsupportedOperationException("Cannot find item type
		// "+action);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#setFilterContext(fr.imag.adele.cadse.core.ui.view.FilterContext)
	 */
	public void setFilterContext(FilterContext filterContext) {
		_filterContext = filterContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.Pages#getFilterContext()
	 */
	public FilterContext getFilterContext() {
		return _filterContext;
	}

}
