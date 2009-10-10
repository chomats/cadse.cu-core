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
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.CollectedReflectLink;
import fr.imag.adele.cadse.core.impl.ReflectLink;
import fr.imag.adele.cadse.core.impl.internal.AbstractGeneratedItem;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransaction;
import fr.imag.adele.cadse.core.ui.EPosLabel;
import fr.imag.adele.cadse.core.ui.IEventListener;
import fr.imag.adele.cadse.core.ui.IFedeFormToolkit;
import fr.imag.adele.cadse.core.ui.IInteractionController;
import fr.imag.adele.cadse.core.ui.IModelController;
import fr.imag.adele.cadse.core.ui.IPage;
import fr.imag.adele.cadse.core.ui.IPageController;
import fr.imag.adele.cadse.core.ui.IPageObject;
import fr.imag.adele.cadse.core.ui.IValidateContributor;
import fr.imag.adele.cadse.core.ui.Pages;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.ui.view.FilterContext;
import fr.imag.adele.cadse.core.util.Convert;
import fr.imag.adele.cadse.core.util.ObjectMap;

/**
 * Represente a graphic field which display a value of an attribute definition
 * 
 * Represente un champ graphique, il y a un identifiant (key). Il est �
 * l'intérieure d'une page, il peut avoir un label et la position de celui-ci
 * par rapport à lui (posLabel) Il a un model controller (mc) et peut avoir un
 * interaction controller dans le but de spécialiser le comportement du field
 * 
 * Il est possible de lui associé des validator (synchrone) et des listeners
 * (synchrone) Il peut recevoir des attributs supplémentaires...
 * 
 * @author chomats
 */
public abstract class UIFieldImpl extends AbstractGeneratedItem implements UIField {
	UIFieldImpl							_ui			= null;

	/** The getPage()s. */
	private Pages						_pages;

	/** The _mc. */
	protected IModelController			_mc;

	/** The getPage(). */
	private IPage						_page;

	/** The _ic. */
	protected IInteractionController	_ic;

	/** The _label. */
	protected String					_label;

	protected boolean					editable	= true;
	/** The _key. */
	private String						_name;

	/** The _pos label. */
	protected EPosLabel					_posLabel;

	/** The listeners. */
	private IEventListener[]			listeners	= null;

	/** The validators. */
	private IValidateContributor[]		validators	= null;

	IAttributeType<?>					_attributeRef;

	/** The cxts. */
	ObjectMap<String, Object>			cxts		= null;

	/** The global controller. */
	protected IPageController			globalController;

	/**
	 * Instantiates a new uI field.
	 * 
	 * @param key
	 *            the key
	 * @param label
	 *            the label
	 * @param poslabel
	 *            the poslabel
	 * @param mc
	 *            the mc
	 * @param ic
	 *            the ic
	 */
	public UIFieldImpl(CompactUUID uuid, String key, String label, EPosLabel poslabel, IModelController mc,
			IInteractionController ic) {
		super(uuid);
		assert mc != null;
		this._ic = ic;
		this._mc = mc;
		this._label = label;
		this._name = key;
		this._posLabel = poslabel;
	}

	public UIFieldImpl(CompactUUID id) {
		super(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#getPages()
	 */
	final public Pages getPages() {
		if (this._pages == null && _ui != null) {
			_pages = _ui.getPages();
		}
		return this._pages;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#getPosLabel()
	 */
	final public EPosLabel getPosLabel() {
		EPosLabel ret = null;
		if (_ui != null) {
			ret = _ui._posLabel;
		} else {
			ret = this._posLabel;
		}

		if (ret == null || ret == EPosLabel.defaultpos) {
			ret = getDefaultPosLabel();
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#getLabel()
	 */
	final public String getLabel() {
		if (_ui != null) {
			return _ui._label;
		}
		return this._label;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#setLabel(java.lang.String)
	 */
	public void setLabel(String label) {
		this._label = label;
	}

	/**
	 * Gets the name of the attribute definition stored in this object. It's
	 * better to use {@link #getAttributeName()}
	 * 
	 * @return the key
	 */
	@Override
	final public String getName() {
		return this._name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#getAttributeName()
	 */
	final public String getAttributeName() {
		if (_ui != null) {
			IAttributeType<?> ret = _ui.getAttributeDefinition();
			if (ret != null) {
				return ret.getName();
			}
			return _ui.getName();
		}
		return getName();
	}

	/**
	 * return null
	 */
	public UIField getField(String fieldid) {
		return null;
	}

	/**
	 * Return the parent page object
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPageObject#getParent()
	 */
	public IPageObject getParent() {
		return this.getPage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#getPage()
	 */
	public IPage getPage() {
		if (_ui != null && _page == null) {
			_page = _ui.getPage();
		}
		return _page;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPageObject#getPageController()
	 */
	public IPageController getPageController() {
		return globalController;
	}

	/**
	 * Sets the page.
	 * 
	 * @param p
	 *            the new page
	 */
	public void setPage(IPage p) {
		this._page = p;
		this._pages = p.getPages();
		if (hasChildren()) {
			for (UIField c : getChildren()) {
				c.setPage(p);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#getInteractionController()
	 */
	public IInteractionController getInteractionController() {
		return _ic;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#getModelController()
	 */
	public IModelController getModelController() {
		return _mc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#init(fr.imag.adele.cadse.core.ui.IPageController)
	 */
	public void init(IPageController globalController) {
		this.globalController = globalController;
		if (hasChildren()) {
			for (UIField c : getChildren()) {
				c.init(globalController);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#getVisualValue()
	 */
	public abstract Object getVisualValue();

	public void setVisualValue(Object visualValue) {
		setVisualValue(visualValue, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#setValue(java.lang.Object)
	 */
	public void setValue(Object visualValue) {
		setVisualValue(visualValue);
		getModelController().notifieValueChanged(this, visualValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#getHSpan()
	 */
	public int getHSpan() {
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#getVSpan()
	 */
	public int getVSpan() {
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#thisFieldHasChanged()
	 */
	public void thisFieldHasChanged() {
		if (isRunning()) {
			globalController.broadcastThisFieldHasChanged(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#isRunning()
	 */
	public boolean isRunning() {
		return getFlag(Item.UI_RUNNING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#forceChange()
	 */
	public void forceChange() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#setEnabled(boolean)
	 */
	public abstract void setEnabled(boolean v);

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#setEditable(boolean)
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
		if (isRunning()) {
			internalSetEditable(editable);
		}

	}

	/**
	 * Sets the editable.
	 * 
	 * @param v
	 *            the new editable
	 */
	public abstract void internalSetEditable(boolean v);

	/**
	 * Sets the internale the set visible.
	 * 
	 * @param v
	 *            the new visible
	 */
	public abstract void internalSetVisible(boolean v);

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#setVisible(boolean)
	 */
	public void setVisible(boolean v) {
		if (isRunning()) {
			internalSetVisible(v);
		} else {
			setHidden(!v);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#setHidden(boolean)
	 */
	public void setHidden(boolean v) {
		setFlag(Item.IS_HIDDEN, v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#createControl(fr.imag.adele.cadse.core.ui.IPageController,
	 *      fr.imag.adele.cadse.core.ui.IFedeFormToolkit, java.lang.Object, int)
	 */
	public abstract Object createControl(IPageController globalUIController, IFedeFormToolkit toolkit,
			Object container, int hspan);

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#getUIObject(int)
	 */
	public abstract Object getUIObject(int index);

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#init()
	 */
	public void init() throws CadseException {
		if (_mc != null) {
			_mc.setUIField(this);
			_mc.init();
		}
		if (_ic != null) {
			_ic.setUIField(this);
			_ic.init();
		}

		if (hasChildren()) {
			for (UIField c : getChildren()) {
				c.init();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#initAfterUI()
	 */
	public void initAfterUI() {
		try {
			if (isHidden()) {
				internalSetVisible(false);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		if (_mc != null) {
			_mc.initAfterUI();
		}

		if (_ic != null) {
			_ic.initAfterUI();
		}

		broadcastInit();

		if (hasChildren()) {
			for (UIField c : getChildren()) {
				c.initAfterUI();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#put(java.lang.String,
	 *      java.lang.Object)
	 */
	public void put(String key, Object value) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#getValueForVisual()
	 */
	public Object getValueForVisual() {
		IModelController getFct = getModelController();
		if (getFct == null) {
			return null;
		}
		Object abstratcObject = getFct.getValue();
		if (abstratcObject == null) {
			Object defaultValue = getFct.defaultValue();
			if (defaultValue != null) {
				getFct.notifieValueChanged(this, defaultValue);
			}
			return defaultValue;
		}
		return abstratcObject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#updateValue()
	 */
	public abstract void updateValue();

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPageObject#putLocal(java.lang.String,
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
		return (T) getPage().get(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPageObject#get(java.lang.String,
	 *      java.lang.Object)
	 */
	public <T> T get(String key, T d) {
		T ret = (T) getLocal(key);
		if (ret != null) {
			return ret;
		}
		return getPage().get(key, d);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#addListener(fr.imag.adele.cadse.core.ui.IEventListener)
	 */
	public synchronized void addListener(IEventListener l) {
		if (l == null) {
			// In an ideal world, we would do an assertion here
			// to help developers know they are probably doing
			// something wrong
			return;
		}

		if (listeners == null) {
			// if this is the first listener added,
			// initialize the lists
			listeners = new IEventListener[] { l };
		} else {
			// Otherwise copy the array and add the new listener
			int i = listeners.length;
			IEventListener[] tmp = new IEventListener[i + 1];
			System.arraycopy(listeners, 0, tmp, 0, i);

			tmp[i] = l;

			listeners = tmp;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#addValidateContributor(fr.imag.adele.cadse.core.ui.IValidateContributor)
	 */
	public synchronized void addValidateContributor(IValidateContributor l) {
		if (l == null) {
			// In an ideal world, we would do an assertion here
			// to help developers know they are probably doing
			// something wrong
			return;
		}

		if (validators == null) {
			// if this is the first listener added,
			// initialize the lists
			validators = new IValidateContributor[] { l };
		} else {
			// Otherwise copy the array and add the new listener
			int i = validators.length;
			IValidateContributor[] tmp = new IValidateContributor[i + 1];
			System.arraycopy(validators, 0, tmp, 0, i);

			tmp[i] = l;

			validators = tmp;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#removeListener(fr.imag.adele.cadse.core.ui.IEventListener)
	 */
	public synchronized void removeListener(IEventListener l) {
		if (l == null) {
			// In an ideal world, we would do an assertion here
			// to help developers know they are probably doing
			// something wrong
			return;
		}
		if (listeners == null) {
			return;
		}

		// Is l on the list?
		int index = -1;
		for (int i = listeners.length - 1; i >= 0; i -= 1) {
			if ((listeners[i].equals(l) == true)) {
				index = i;
				break;
			}
		}

		// If so, remove it
		if (index != -1) {
			if (listeners.length == 1) {
				listeners = null;
				return;
			}

			IEventListener[] tmp = new IEventListener[listeners.length - 1];
			// Copy the list up to index
			System.arraycopy(listeners, 0, tmp, 0, index);
			// Copy from two past the index, up to
			// the end of tmp (which is two elements
			// shorter than the old list)
			if (index < tmp.length) {
				System.arraycopy(listeners, index + 1, tmp, index, tmp.length - index);
			}
			// set the listener array to the new array or null
			listeners = tmp;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#resetVisualValue()
	 */
	public void resetVisualValue() {
		setFlag(UI_RUNNING, true);
		if (!isEditable()) {
			internalSetEditable(false);
		}

		setVisualValue(getValueForVisual(), false);

		if (hasChildren()) {
			for (UIField c : getChildren()) {
				c.resetVisualValue();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#validateField()
	 */
	public boolean validateField() {
		if (isDisposed()) {
			return true;
		}

		Object visualValue = getVisualValue();
		boolean error = _mc.validValue(this, visualValue);
		if (error) {
			return true;
		}

		if (validators != null) {
			for (IValidateContributor v : validators) {
				error = v.validValue(this, visualValue);
				if (error) {
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#isDisposed()
	 */
	public boolean isDisposed() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#broadcastValueChanged(fr.imag.adele.cadse.core.ui.IPageController,
	 *      java.lang.Object)
	 */
	public boolean broadcastValueChanged(IPageController globalController, Object visualValue) {
		try {
			try {
				if (validateValueChanged(visualValue)) {
					return true;
				}
				sendChangedValue(visualValue);
			} catch (RuntimeException e) {
				getCadseDomain().log(null, "Exception raised ! messages = " + e.getMessage(), e);
				getPage().setMessageError("Exception raised ! messages = " + e.getMessage());
				return true;
			}
			getPages().validateFields(this, getPage());
			return false;
		} catch (RuntimeException e) {
			getCadseDomain().log(null, "Exception raised ! messages = " + e.getMessage(), e);
			getPage().setMessageError("Exception raised ! messages = " + e.getMessage());
			return false;
		}
	}

	protected void sendChangedValue(Object visualValue) {
		_mc.notifieValueChanged(this, visualValue);
		if (listeners != null) {
			for (int i = 0; i < listeners.length; i++) {
				try {
					listeners[i].notifieValueChanged(this, visualValue);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Return true if error. Validate this field, if no error, call validator
	 * associated to this field. Other fields are not revalidate to test if
	 * error
	 * 
	 * @param visualValue
	 *            the visual value of this field
	 * @return true if error
	 */

	protected boolean validateValueChanged(Object visualValue) {
		boolean error;
		error = _mc.validValueChanged(this, visualValue);
		if (error) {
			return true;
		}
		if (validators != null) {
			for (IValidateContributor v : validators) {
				error = v.validValueChanged(this, visualValue);
				if (error) {
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#broadcastInit()
	 */
	public void broadcastInit() {
		if (listeners != null) {
			for (int i = 0; i < listeners.length; i++) {
				try {
					listeners[i].init(this);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#broadcastValueDeleted(fr.imag.adele.cadse.core.ui.IPageController,
	 *      java.lang.Object)
	 */
	public boolean broadcastValueDeleted(IPageController globalController, Object oldvalue) {

		try {
			boolean error = _mc.validValueDeleted(this, oldvalue);
			if (error) {
				return true;
			}
			if (validators != null) {
				for (IValidateContributor v : validators) {
					error = v.validValueDeleted(this, oldvalue);
					if (error) {
						return true;
					}
				}
			}
			_mc.notifieValueDeleted(this, oldvalue);
			if (listeners != null) {
				for (int i = 0; i < listeners.length; i++) {
					try {
						listeners[i].notifieValueDeleted(this, oldvalue);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}

			return getPages().validateFields(this, getPage());
		} catch (Throwable e) {
			getCadseDomain().log(null, "Exception raised ! messages = " + e.getMessage(), e);
			getPage().setMessageError("Exception raised ! messages = " + e.getMessage());
			return true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#broadcastSubValueAdded(fr.imag.adele.cadse.core.ui.IPageController,
	 *      java.lang.Object)
	 */
	public boolean broadcastSubValueAdded(IPageController globalController, Object added) {

		try {
			boolean error;
			error = _mc.validSubValueAdded(this, added);
			if (error) {
				return true;
			}
			if (validators != null) {
				for (IValidateContributor v : validators) {
					error = v.validSubValueAdded(this, added);
					if (error) {
						return true;
					}
				}
			}
			_mc.notifieSubValueAdded(this, added);
			if (listeners != null) {
				for (int i = 0; i < listeners.length; i++) {
					try {
						listeners[i].notifieSubValueAdded(this, added);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}
			return getPages().validateFields(this, getPage());
		} catch (Throwable e) {
			getCadseDomain().log(null, "Exception raised ! messages = " + e.getMessage(), e);
			getPage().setMessageError("Exception raised ! messages = " + e.getMessage());
			return true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#broadcastSubValueRemoved(fr.imag.adele.cadse.core.ui.IPageController,
	 *      java.lang.Object)
	 */
	public boolean broadcastSubValueRemoved(IPageController globalController, Object removed) {

		try {
			boolean error;
			error = _mc.validSubValueRemoved(this, removed);
			if (error) {
				return true;
			}
			if (validators != null) {
				for (IValidateContributor v : validators) {
					error = v.validSubValueRemoved(this, removed);
					if (error) {
						return true;
					}
				}
			}
			_mc.notifieSubValueRemoved(this, removed);
			if (listeners != null) {
				for (int i = 0; i < listeners.length; i++) {
					try {
						listeners[i].notifieSubValueRemoved(this, removed);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}
			return getPages().validateFields(this, getPage());
		} catch (Throwable e) {
			getCadseDomain().log(null, "Exception raised ! messages = " + e.getMessage(), e);
			getPage().setMessageError("Exception raised ! messages = " + e.getMessage());
			return true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#dispose()
	 */
	public void dispose() {
		setFlag(UI_RUNNING, false);
		if (_mc != null) {
			_mc.dispose();
		}

		if (_ic != null) {
			_ic.dispose();
		}
		_filterContext = null;
		_pages = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#getContext()
	 */
	public Object getContext() {
		Object ret = getLocal(getContextItemKey());
		if (ret == null) {
			ret = this.getPage().getContext();
		}
		return ret;
	}

	protected String getContextItemKey() {
		// ItemType it = getPage().getParentItemType();
		// // if (it == null) return this.getId().toString();
		// return it.getId().toString();
		return "UI-ITEM-CONTEXT";
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
	 * @see fr.imag.adele.cadse.core.ui.UIField#isEditable()
	 */
	public boolean isEditable() {
		if (_ui != null) {
			return _ui.editable;
		}
		return editable;
	}

	@Override
	protected void collectOutgoingLinks(LinkType linkType, CollectedReflectLink ret) {
		if (linkType == CadseGCST.FIELD_lt_ATTRIBUTE) {
			ret.addOutgoing(CadseGCST.FIELD_lt_ATTRIBUTE, _attributeRef);
		}
		super.collectOutgoingLinks(linkType, ret);
	}

	@Override
	public Link commitLoadCreateLink(LinkType lt, Item destination) throws CadseException {
		if (lt == CadseGCST.FIELD_lt_ATTRIBUTE) {
			_attributeRef = (IAttributeType<?>) destination;
			return new ReflectLink(lt, this, destination, 0);
		}

		return super.commitLoadCreateLink(lt, destination);
	}

	@Override
	public void removeOutgoingLink(Link link, boolean notifie) {
		Item destination = link.getDestination();
		LinkType lt = link.getLinkType();
		if (lt == CadseGCST.FIELD_lt_ATTRIBUTE && destination.isResolved()) {
			_attributeRef = null;
			return;
		}
		super.removeOutgoingLink(link, notifie);
	}

	@Override
	public boolean commitSetAttribute(IAttributeType<?> type, String key, Object value) {
		if (key.equals(CadseGCST.ITEM_at_NAME)) {
			this._name = Convert.toString(value);
			return true;
		}

		return super.commitSetAttribute(type, key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#internalGetOwnerAttribute(fr.imag.adele.cadse.core.attribute.IAttributeType)
	 */
	@Override
	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {
		if (type == CadseGCST.FIELD_at_EDITABLE_) {
			return (T) Boolean.valueOf(editable);
		}
		if (type == CadseGCST.FIELD_at_LABEL_) {
			return (T) this._label;
		}
		if (CadseGCST.ITEM_at_NAME_ == type) {
			return (T) this._name;
		}
		if (CadseGCST.ITEM_at_DISPLAY_NAME_ == type) {
			return (T) this._name;
		}
		return super.internalGetOwnerAttribute(type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IEventListener#init(fr.imag.adele.cadse.core.ui.UIField)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#init(fr.imag.adele.cadse.core.ui.UIField)
	 */
	public void init(UIField field) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IEventListener#notifieSubValueAdded(fr.imag.adele.cadse.core.ui.UIField,
	 *      java.lang.Object)
	 */
	public void notifieSubValueAdded(UIField field, Object added) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IEventListener#notifieSubValueRemoved(fr.imag.adele.cadse.core.ui.UIField,
	 *      java.lang.Object)
	 */
	public void notifieSubValueRemoved(UIField field, Object removed) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IEventListener#notifieValueChanged(fr.imag.adele.cadse.core.ui.UIField,
	 *      java.lang.Object)
	 */
	public void notifieValueChanged(UIField field, Object value) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IEventListener#notifieValueDeleted(fr.imag.adele.cadse.core.ui.UIField,
	 *      java.lang.Object)
	 */
	public void notifieValueDeleted(UIField field, Object oldvalue) {
	}

	public void setParent(Item parent, LinkType lt) {
		if (parent instanceof PageImpl) {
			_page = (PageImpl) parent;
			if (getPage() != null) {
				_ui = null;
				_pages = getPage().getPages();
			}
		}
		if (parent instanceof UIField2) {
			_ui = (UIField2) parent;
			if (_ui != null) {
				_page = _ui.getPage();
				if (getPage() != null) {
					_pages = getPage().getPages();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#getPartParent()
	 */
	@Override
	public Item getPartParent() {
		if (_ui != null) {
			return _ui;
		}
		return getPage();
	}

	@Override
	public void setShortName(String shortname) {
		this._name = shortname;
	}

	/**
	 * Return the attribute definition which this field display the value
	 * 
	 * @return the attribute definition
	 * @deprecated Use {@link #getAttributeDefinition()} instead
	 */

	@Deprecated
	public IAttributeType<?> getAttributeRef() {
		return getAttributeDefinition();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#getAttributeDefinition()
	 */

	public IAttributeType<?> getAttributeDefinition() {
		if (_ui != null) {
			return _ui.getAttributeDefinition();
		}
		if (_attributeRef == null) {
			Item item = getItem();
			if (item != null) {
				_attributeRef = item.getLocalAttributeType(this._name);
			}
		}
		return _attributeRef;
	}

	public LogicalWorkspaceTransaction getCopy() {
		return getPage().getCopy();
	}

	public void setItem(String it_id, Item item) {
		putLocal(it_id, item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#setItem(fr.imag.adele.cadse.core.Item)
	 */
	public void setItem(Item item) {
		String it_id = getContextItemKey().toString();
		this.setItem(it_id, item);
	}

	/**
	 * Gets the default pos label.
	 * 
	 * @return the default pos label
	 */
	protected EPosLabel getDefaultPosLabel() {
		return EPosLabel.left;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#hasChildren()
	 */
	public boolean hasChildren() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#getChildren()
	 */

	public UIField[] getChildren() {
		return null;
	}

	FilterContext	_filterContext	= null;

	public FilterContext getFilterContext() {
		if (_filterContext == null) {
			_filterContext = new FilterContext(getPage().getFilterContext(), this);
		}
		return _filterContext;
	}

	public void setFilterContext(FilterContext filterContext) {
		this._filterContext = filterContext;
	}

}
