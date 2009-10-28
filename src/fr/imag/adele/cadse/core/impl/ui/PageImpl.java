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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.CollectedReflectLink;
import fr.imag.adele.cadse.core.impl.ReflectLink;
import fr.imag.adele.cadse.core.impl.internal.AbstractGeneratedItem;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransaction;
import fr.imag.adele.cadse.core.ui.IActionPage;
import fr.imag.adele.cadse.core.ui.IPage;
import fr.imag.adele.cadse.core.ui.IPageController;
import fr.imag.adele.cadse.core.ui.IPageObject;
import fr.imag.adele.cadse.core.ui.IUIFieldContainer;
import fr.imag.adele.cadse.core.ui.Pages;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.ui.UIListener;
import fr.imag.adele.cadse.core.ui.view.FilterContext;
import fr.imag.adele.cadse.core.util.ArraysUtil;
import fr.imag.adele.cadse.core.util.Convert;
import fr.imag.adele.cadse.core.util.ObjectMap;
import fr.imag.adele.cadse.core.util.OrderWay;

/**
 * The Class IPage. Represente une page graphique. Elle contient UIField,
 * eventuellement une action page
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class PageImpl extends AbstractGeneratedItem implements IPage {

	/** The _pages le parent � l'execution regroupant l'ensemble des pages. */
	Pages					_pages;

	/** La list des fields */
	UIField[]				_fields;

	/** The _label. */
	String					_label;

	/** The _action, null si pas necessaire. */
	IActionPage				_action;

	/** The _id. */
	private String			shortName;

	/** The nombre de collonne. */
	private int				_hspan;

	/** The _title. */
	private String			_title;

	/** The _is page complete. */
	private boolean			_isPageComplete;

	/** The _description. */
	private String			_description;

	/** The cxts. */
	Map<String, Object>		cxts		= null;

	/** The page controller. */
	private IPageController	pageController;

	private ItemType		_parentItemType;

	private UIListener[]	listeners	= null;

	private FilterContext	_filterContext;

	private IPage[]			_superPages;

	public PageImpl(CompactUUID id, String name, ItemType parent) {
		super(id);
		this.shortName = name;
		this._parentItemType = parent;
	}

	/**
	 * Instantiates a new i page.
	 * 
	 * @param id
	 *            the id
	 * @param label
	 *            the label
	 * @param title
	 *            the title
	 * @param description
	 *            the description
	 * @param isPageComplete
	 *            the is page complete
	 * @param hspan
	 *            the hspan
	 * @param action
	 *            the action
	 * @param fields
	 *            the fields
	 */
	public PageImpl(CompactUUID id, String name, String label, String title, String description,
			boolean isPageComplete, int hspan, IActionPage action, UIField... fields) {
		super(id);
		_fields = fields;
		shortName = name;
		_label = label;
		_action = action;
		_hspan = hspan;
		_title = title;
		_description = description;
		_isPageComplete = isPageComplete;

	}

	public PageImpl(String name, String label, String title, String description, boolean isPageComplete, int hspan) {
		super();
		_fields = EMPTY_UIFIELD;
		shortName = name;
		_label = label;
		_action = null;
		_hspan = hspan;
		_title = title;
		_description = description;
		_isPageComplete = isPageComplete;
	}

	public PageImpl(String name, String label, String title, String description, boolean isPageComplete, int hspan,
			IActionPage action, UIField... fields) {
		super();
		_fields = fields;
		shortName = name;
		_label = label;
		_action = action;
		_hspan = hspan;
		_title = title;
		_description = description;
		_isPageComplete = isPageComplete;

	}

	public PageImpl(CompactUUID id, String name, String label, String title, String description,
			boolean isPageComplete, int hspan) {
		super(id);
		_fields = EMPTY_UIFIELD;
		shortName = name;
		_label = label;
		_action = null;
		_hspan = hspan;
		_title = title;
		_description = description;
		_isPageComplete = isPageComplete;
	}

	/**
	 * Sets the pages.
	 * 
	 * @param _pages
	 *            the new pages
	 */
	public void setPages(Pages pages) {
		this._pages = pages;
		for (UIField f : _fields) {
			f.setPage(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPage#getFields()
	 */
	public UIField[] getFields() {
		if (_fields == null) {
			return EMPTY_UIFIELD;
		}
		return _fields;
	}

	/**
	 * Inits the.
	 * 
	 * @param pageController
	 *            the page controller
	 * 
	 * @throws CadseException
	 *             the melusine exception
	 */
	public void init(IPageController pageController) throws CadseException {
		this.pageController = pageController;
		if (_action != null) {
			_action.init(this);
		}
		for (UIField f : _fields) {
			f.init();
		}
		if (this.listeners != null) {
			for (UIListener l : this.listeners) {
				l.initAndResgister();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.ui.IPage#addLast(fr.imag.adele.cadse.core.ui
	 * .UIField)
	 */
	public void addLast(UIField... field) {
		_fields = ArraysUtil.addList(UIField.class, _fields, field);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.ui.IPage#addUIListner(fr.imag.adele.cadse.core
	 * .ui.UIListener)
	 */
	public void addUIListner(UIListener l) {
		listeners = ArraysUtil.add(UIListener.class, listeners, l);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPage#addBefore(java.lang.String,
	 * fr.imag.adele.cadse.core.ui.UIField)
	 */
	public void addBefore(String key, UIField field) {
		int i = indexOf(key);
		if (i == -1) {
			throw new NoSuchElementException("Not found " + key);
		}
		_fields = ArraysUtil.add(UIField.class, _fields, field, i);
	}

	private int indexOf(String key) {
		if (_fields != null) {
			for (int i = 0; i < _fields.length; i++) {
				UIField ui = _fields[i];
				if (ui.getName().equals(key)) {
					return i;
				}
			}
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPage#addAfter(java.lang.String,
	 * fr.imag.adele.cadse.core.ui.UIField)
	 */
	public void addAfter(String key, UIField field) {
		int i = indexOf(key);
		if (i == -1) {
			throw new NoSuchElementException("Not found " + key);
		}
		_fields = ArraysUtil.add(UIField.class, _fields, field, i + 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPageObject#getPageController()
	 */
	public IPageController getPageController() {
		return pageController;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPage#getActionPage()
	 */
	public IActionPage getActionPage() {
		return _action;
	}

	public void setActionPage(IActionPage _action) {
		this._action = _action;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPage#getHSpan()
	 */
	public int getHSpan() {
		return _hspan;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPage#getLabel()
	 */
	public String getLabel() {
		return _label;
	}

	/**
	 * Inits the after ui.
	 */
	public void initAfterUI() {
		if (_action != null) {
			_action.initAfterUI();
		}
		for (UIField f : _fields) {
			f.initAfterUI();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPageObject#getParent()
	 */
	public IPageObject getParent() {
		return _pages;
	}

	@Override
	public String getName() {
		return shortName;
	}

	public ItemType getType() {
		return CadseGCST.PAGE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPage#getField(java.lang.String)
	 */
	public UIField getField(String... keyPath) {
		if (keyPath.length <= 0) {
			return null;
		}

		UIField[] descs = getFields();
		UIField root = null;
		ONE: for (int i = 0; i < keyPath.length; i++) {
			if (descs == null) {
				return null;
			}
			for (UIField d : descs) {
				if (d.getName().equals(keyPath[i])) {
					root = d;
					if (root instanceof IUIFieldContainer) {
						descs = ((IUIFieldContainer) root).getFields();
					} else {
						descs = null;
					}
					continue ONE;
				}
			}
			return null;
		}
		return root;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPage#getPages()
	 */
	public Pages getPages() {
		return _pages;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPageObject#getField(java.lang.String)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPage#getField(java.lang.String)
	 */
	public UIField getField(String fieldid) {
		UIField[] descs = getFields();
		if (descs == null) {
			return null;
		}
		for (UIField d : descs) {
			if (d.getName().equals(fieldid)) {
				return d;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPage#getTitle()
	 */
	public String getTitle() {
		return _title;
	}

	/**
	 * Sets the title of this page.
	 * 
	 * @param title
	 */
	public void setTitle(String title) {
		_title = title;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPage#getDescription()
	 */
	public String getDescription() {
		return _description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPage#isPageComplete()
	 */
	public boolean isPageComplete() {
		return _isPageComplete;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPage#resetVisualValue()
	 */
	public void resetVisualValue() {
		for (UIField f : _fields) { // avant, il avait deux boucles.
			// set is running avant reset value pour utiliser
			// ce flag dans la methode setvisualvalue durant le reset
			f.setFlag(UI_RUNNING, true);
			if (!f.isEditable()) {
				f.internalSetEditable(false);
			}
			try {
				f.resetVisualValue();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPage#dispose()
	 */
	public void dispose() {
		if (_action != null) {
			_action.dispose();
		}
		if (_fields != null) {
			for (UIField f : _fields) {
				f.dispose();
			}
		}
		if (this.listeners != null) {
			for (UIListener l : this.listeners) {
				l.disposeAndUnregister();
			}
		}
		_filterContext = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPage#getContext()
	 */
	public Object getContext() {
		String it_id = getContextItemKey();
		Object ret = getLocal(it_id);
		if (ret == null) {
			ret = this._pages.getLocal(it_id);
		}
		return ret;
	}

	private String getContextItemKey() {
		ItemType it = getParentItemType();
		if (it == null) {
			return "ITEM_CONTEXT";
		}
		return it.getId().toString();
	}

	/**
	 * Gets the item.
	 * 
	 * @return the item
	 */
	public Item getItem() {
		return (Item) getContext();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPageObject#putLocal(java.lang.String,
	 * java.lang.Object)
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
	 * java.lang.Object)
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
	 * @see fr.imag.adele.cadse.core.ui.IPageObject#get(java.lang.String)
	 */
	public <T> T get(String key) {
		T ret = (T) getLocal(key);
		if (ret != null) {
			return ret;
		}
		return (T) _pages.getLocal(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPageObject#get(java.lang.String,
	 * java.lang.Object)
	 */
	public <T> T get(String key, T d) {
		T ret = (T) getLocal(key);
		if (ret != null) {
			return ret;
		}
		return _pages.getLocal(key, d);
	}

	/**
	 * Validate fields.
	 * 
	 * @param currentField
	 *            the current field
	 * 
	 * @return true, if successful
	 */
	public boolean validateFields(UIField currentField) {
		for (UIField f : _fields) {
			if (f == currentField) {
				continue;
			}
			if (f.validateField()) {
				return true;
			}
		}
		return false;
	}

	public boolean validSubValueAdded(UIField field, Object addedElement) {
		return false;
	}

	public boolean validSubValueRemoved(UIField field, Object removedElement) {
		return false;
	}

	public boolean validValue(UIField field, Object value) {
		return false;
	}

	public boolean validValueChanged(UIField field, Object value) {
		return false;
	}

	public boolean validValueDeleted(UIField field, Object deletedValue) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.ui.IEventListener#init(fr.imag.adele.cadse.core
	 * .ui.UIField)
	 */
	public void init(UIField field) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.ui.IEventListener#notifieSubValueAdded(fr.imag
	 * .adele.cadse.core.ui.UIField, java.lang.Object)
	 */
	public void notifieSubValueAdded(UIField field, Object added) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.ui.IEventListener#notifieSubValueRemoved(fr.
	 * imag.adele.cadse.core.ui.UIField, java.lang.Object)
	 */
	public void notifieSubValueRemoved(UIField field, Object removed) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.ui.IEventListener#notifieValueChanged(fr.imag
	 * .adele.cadse.core.ui.UIField, java.lang.Object)
	 */
	public void notifieValueChanged(UIField field, Object value) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.ui.IEventListener#notifieValueDeleted(fr.imag
	 * .adele.cadse.core.ui.UIField, java.lang.Object)
	 */
	public void notifieValueDeleted(UIField field, Object oldvalue) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPage#getParentItemType()
	 */
	public ItemType getParentItemType() {
		if (_parentItemType == null && _pages != null) {
			return _pages.getParentItemType();
		}
		return _parentItemType;
	}

	public void setParent(Item parent, LinkType lt) {
		_parentItemType = (ItemType) parent;
	}

	public boolean runCreationPage() {
		return getPages().runCreationPage();
	}

	@Override
	public Link commitLoadCreateLink(LinkType lt, Item destination) throws CadseException {
		if (lt == CadseGCST.PAGE_lt_FIELDS && destination.isResolved()) {
			addLast((UIField) destination);
			return new ReflectLink(lt, this, destination, this._fields.length - 1);
		}
		/*
		 * if (lt == CadseGCST.PAGE_lt_LISTENER && destination.isResolved()) {
		 * addUIListner((UIListener) destination); return new ReflectLink(lt,
		 * this, destination, this.listeners.length - 1); }
		 */
		return super.commitLoadCreateLink(lt, destination);
	}

	@Override
	public void removeOutgoingLink(Link link, boolean notifie) {
		Item destination = link.getDestination();
		LinkType lt = link.getLinkType();

		if (lt == CadseGCST.PAGE_lt_FIELDS && destination.isResolved()) {
			_fields = ArraysUtil.remove(UIField.class, _fields, (UIField) destination);
			return;
		}
		/*
		 * if (lt == CadseGCST.PAGE_lt_LISTENER && destination.isResolved()) {
		 * listeners = ArraysUtil.remove(UIListener.class, listeners,
		 * (UIListener) destination); return; }
		 */
		super.removeOutgoingLink(link, notifie);
	}

	@Override
	protected void collectOutgoingLinks(LinkType linkType, CollectedReflectLink ret) {
		if (linkType == CadseGCST.PAGE_lt_FIELDS) {
			ret.addOutgoing(CadseGCST.PAGE_lt_FIELDS, this._fields);
		}
		/*
		 * if (linkType == CadseGCST.PAGE_lt_LISTENER) {
		 * ret.addOutgoing(CadseGCST.PAGE_lt_LISTENER, this.listeners); }
		 */
		super.collectOutgoingLinks(linkType, ret);
	}

	@Override
	public boolean commitSetAttribute(IAttributeType<?> type, Object value) {
		if (CadseGCST.ITEM_at_NAME_ == type) {
			this.shortName = Convert.toString(value);
			return true;
		}
		if (CadseGCST.PAGE_at_DESCRIPTION_ == type) {
			this._description = Convert.toString(value);
			return true;
		}
		if (CadseGCST.PAGE_at_HSPAN_ == type) {
			this._hspan = Convert.toInt(value, type);
			return true;
		}
		if (CadseGCST.PAGE_at_TITLE_ == type) {
			this._title = Convert.toString(value);
			return true;
		}
		if (CadseGCST.PAGE_at_LABEL_ == type) {
			this._label = Convert.toString(value);
			return true;
		}

		return super.commitSetAttribute(type, value);
	}

	@Override
	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {
		if (CadseGCST.ITEM_at_NAME_ == type) {
			return (T) this.shortName;
		}
		if (CadseGCST.ITEM_at_DISPLAY_NAME_ == type) {
			return (T) this.shortName;
		}
		if (CadseGCST.ITEM_at_QUALIFIED_NAME_ == type) {
			return (T) getQualifiedName();
		}
		if (CadseGCST.PAGE_at_DESCRIPTION_ == type) {
			return (T) this._description;
		}
		if (CadseGCST.PAGE_at_HSPAN_ == type) {
			return (T) new Integer(this._hspan);
		}
		if (CadseGCST.PAGE_at_TITLE_ == type) {
			return (T) this._title;
		}
		if (CadseGCST.PAGE_at_LABEL_ == type) {
			return (T) this._label;
		}
		return super.internalGetOwnerAttribute(type);
	}

	@Override
	public Item getPartParent(boolean attemptToRecreate) {
		if (_parentItemType == null) {
			findPartParentFromIncoming(true);
		}
		return _parentItemType;
	}

	public LogicalWorkspaceTransaction getCopy() {
		return this._pages.getCopy();
	}

	public void setItem(String it_id, Item item) {
		putLocal(it_id, item);
	}

	public void setItem(Item item) {
		String it_id = getContextItemKey();
		this.setItem(it_id, item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPage#isModificationPage()
	 */
	public boolean isModificationPage() {
		return getPartParentLinkType() == CadseGCST.ITEM_TYPE_lt_MODIFICATION_PAGES;
	}

	@Override
	public boolean commitMove(OrderWay kind, Link l1, Link l2) {
		if (l1.getLinkType() == CadseGCST.PAGE_lt_FIELDS && l2.getLinkType() == CadseGCST.PAGE_lt_FIELDS
				&& l1.isLinkResolved() && l2.isLinkResolved()) {
			return moveField(kind, (UIField) l1.getDestination(), (UIField) l2.getDestination());
		}
		return super.commitMove(kind, l1, l2);
	}

	private boolean moveField(OrderWay kind, UIField f1, UIField f2) {
		if (_fields == null) {
			return false;
		}
		return ArraysUtil.move(kind, _fields, f1, f2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.ui.IPage#isLast(fr.imag.adele.cadse.core.ui.
	 * UIField)
	 */
	public boolean isLast(UIField field) {
		if (_fields == null) {
			return false;
		}
		if (((UIFieldImpl) field)._ui != null) {
			field = ((UIFieldImpl) field)._ui;
		}

		return _fields[_fields.length - 1] == field;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.ui.IPage#isFirst(fr.imag.adele.cadse.core.ui
	 * .UIField)
	 */
	public boolean isFirst(UIField field) {
		if (_fields == null) {
			return false;
		}
		if (((UIFieldImpl) field)._ui != null) {
			field = ((UIFieldImpl) field)._ui;
		}

		return _fields[0] == field;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPage#setMessageError(java.lang.String)
	 */
	public void setMessageError(String msg) {
		getPageController().setMessage(msg, IPageController.ERROR);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.ui.IPage#setMessageWarning(java.lang.String)
	 */
	public void setMessageWarning(String msg) {
		getPageController().setMessage(msg, IPageController.WARNING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPage#setMessageInfo(java.lang.String)
	 */
	public void setMessageInfo(String msg) {
		getPageController().setMessage(msg, IPageController.INFORMATION);
	}

	public FilterContext getFilterContext() {
		if (_filterContext == null) {
			_filterContext = new FilterContext(getPages().getFilterContext(), this);
		}
		return _filterContext;
	}

	public void setFilterContext(FilterContext filterContext) {
		this._filterContext = filterContext;
	}

	@Override
	public boolean isEmpty() {
		return _fields == null || _fields.length == 0;
	}

	@Override
	public List<UIField> getGoodFields() {
		return Arrays.asList(getFields());
	}

	@Override
	public IPage[] getSuperPage() {
		return _superPages;
	}
}
