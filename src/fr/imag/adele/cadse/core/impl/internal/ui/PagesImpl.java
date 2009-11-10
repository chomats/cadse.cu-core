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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.CadseCore;
import fr.imag.adele.cadse.core.impl.ui.CreationAction;
import fr.imag.adele.cadse.core.impl.ui.ModificationAction;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransaction;
import fr.imag.adele.cadse.core.ui.IActionPage;
import fr.imag.adele.cadse.core.ui.IPage;
import fr.imag.adele.cadse.core.ui.UIPlatform;
import fr.imag.adele.cadse.core.ui.IPageObject;
import fr.imag.adele.cadse.core.ui.Pages;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.ui.UIRunningValidator;
import fr.imag.adele.cadse.core.ui.UIValidator;
import fr.imag.adele.cadse.core.ui.view.FilterContext;
import fr.imag.adele.cadse.core.util.ArraysUtil;
import fr.imag.adele.cadse.core.util.ObjectMap;

/**
 * The Class Pages.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public final class PagesImpl implements Pages {

	/** The _pages. */
	IPage[]								_pages;

	/** The action. */
	IActionPage							_action;

	private FilterContext				_filterContext;
	
	boolean 							_ismodificationpage = false;

	private Map<IAttributeType<?>, UIField> _fields;

	private List<UIRunningValidator> _validators;

	/**
	 * Instantiates a new pages.
	 * 
	 * @param action
	 *            the action
	 * @param fiedls 
	 * @param genericPage 
	 * @param pages
	 *            the pages
	 * @param validators 
	 */
	public PagesImpl(boolean ismodificationpage, IActionPage action, Map<IAttributeType<?>, UIField> fiedls, IPage[] pages, List<UIRunningValidator> validators) {
		this._pages = pages;
		this._action = action;
		this._ismodificationpage = ismodificationpage;
		this._fields = fiedls;
		if (this._fields == null)
			this._fields = new HashMap<IAttributeType<?>, UIField>();
		this._validators = validators;
	}

	public PagesImpl() {
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
	 * @see fr.imag.adele.cadse.core.ui.Pages#getField(java.lang.String,
	 *      java.lang.String)
	 */
	public UIField getField(IAttributeType<?> att) {
		return _fields.get(att);
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

	@Override
	public boolean isModificationPages() {
		return _ismodificationpage;
	}

	@Override
	public UIField getUIField(IAttributeType<?> at) {
		if (_fields != null)
			return _fields.get(at);
		return null;
	}
	
	@Override
	public void setUIField(IAttributeType<?> key, UIField field) {
		if (_fields == null)
			_fields = new HashMap<IAttributeType<?>, UIField>();
		_fields.put(key, field);
	}

	@Override
	public List<UIRunningValidator> getUIValidators() {
		return _validators;
	}

	@Override
	public IActionPage getAction() {
		return this._action;
	}

	@Override
	public void addPage(IPage page) {
		_pages = ArraysUtil.add(IPage.class, _pages, page);
	}

	@Override
	public void setAction(IActionPage newAction) {
		this._action = newAction;
	}

	@Override
	public void addUIValidator(UIRunningValidator v) {
		if (_validators == null)
			_validators = new ArrayList<UIRunningValidator>();
		_validators.add(v);
	}
}
