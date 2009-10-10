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
import fr.imag.adele.cadse.core.impl.internal.AbstractGeneratedItem;
import fr.imag.adele.cadse.core.ui.IModelController;
import fr.imag.adele.cadse.core.ui.IPageController;
import fr.imag.adele.cadse.core.ui.UIField;

/**
 * The Class AbstractModelController.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public abstract class AbstractModelController extends AbstractGeneratedItem implements IModelController {

	
	public AbstractModelController() {
		// TODO Auto-generated constructor stub
	}

	public AbstractModelController(CompactUUID id) {
		super(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IModelController#getUIField()
	 */
	public UIField getUIField() {
		return (UIField) _parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IModelController#setUIField(fr.imag.adele.cadse.core.ui.UIField)
	 */
	public void setUIField(UIField ui) {
		_parent = ui;
	}

	

	public void setParent(Item parent, LinkType lt) {
		_parent = (UIField) parent;
	}

	@Override
	public String getName() {
		return "mc";
	}

	@Override
	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {
		if (CadseGCST.ITEM_at_DISPLAY_NAME_ == type) {
			return (T) ("Mode controller " + (getType() == null ? "Anonymous" : getType().getDisplayName()));
		}
		if (CadseGCST.ITEM_at_NAME_ == type) {
			return (T) "mc";
		}
		return super.internalGetOwnerAttribute(type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IModelController#init()
	 */
	public void init() throws CadseException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IModelController#initAfterUI()
	 */
	public void initAfterUI() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IModelController#dispose()
	 */
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IModelController#defaultValue()
	 */
	public Object defaultValue() {
		UIField uiLocal = getUIField();
		if (uiLocal == null) {
			return null;
		}
		IAttributeType<?> attrDef = uiLocal.getAttributeDefinition();
		if (attrDef != null) {
			return attrDef.getDefaultValue();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IValidateContributor#validValue(fr.imag.adele.cadse.core.ui.UIField,
	 *      java.lang.Object)
	 */
	public boolean validValue(UIField field, Object value) {
		return validValueChanged(field, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IValidateContributor#validSubValueAdded(fr.imag.adele.cadse.core.ui.UIField,
	 *      java.lang.Object)
	 */
	public boolean validSubValueAdded(UIField field, Object added) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IValidateContributor#validSubValueRemoved(fr.imag.adele.cadse.core.ui.UIField,
	 *      java.lang.Object)
	 */
	public boolean validSubValueRemoved(UIField field, Object removed) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IValidateContributor#validValueChanged(fr.imag.adele.cadse.core.ui.UIField,
	 *      java.lang.Object)
	 */
	public boolean validValueChanged(UIField field, Object value) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IValidateContributor#validValueDeleted(fr.imag.adele.cadse.core.ui.UIField,
	 *      java.lang.Object)
	 */
	public boolean validValueDeleted(UIField field, Object removed) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IEventListener#notifieSubValueAdded(fr.imag.adele.cadse.core.ui.UIField,
	 *      java.lang.Object)
	 */
	public void notifieSubValueAdded(UIField field, Object added) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IEventListener#notifieSubValueRemoved(fr.imag.adele.cadse.core.ui.UIField,
	 *      java.lang.Object)
	 */
	public void notifieSubValueRemoved(UIField field, Object removed) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IEventListener#notifieValueDeleted(fr.imag.adele.cadse.core.ui.UIField,
	 *      java.lang.Object)
	 */
	public void notifieValueDeleted(UIField field, Object oldvalue) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Gets the item.
	 * 
	 * @return the item
	 */
	public Item getItem() {
		if (_parent != null) {
			return (Item) ((UIField) _parent).getContext();
		}
		return null;
	}

	/**
	 * Gets the key.
	 * 
	 * @return the key
	 */
	public String getAttributeName() {
		return ((UIField) _parent).getAttributeName();
	}

	/** The old default short name. */
	private String	oldDefaultShortName	= null;

	/**
	 * Sets the default short name.
	 * 
	 * @param shortName
	 *            the new default short name
	 * @throws CadseException
	 */
	public void setDefaultShortName(String shortName) throws CadseException {

		Item theCurrentItem = getItem();
		String sn = theCurrentItem.getName();
		if (sn != null && sn.length() != 0 && !sn.equals(oldDefaultShortName)) {
			return;
		}

		oldDefaultShortName = shortName;
		theCurrentItem.setName(shortName);
		setVisualShortName(shortName);
	}

	/**
	 * Sets the visual short name.
	 * 
	 * @param shortName
	 *            the new visual short name
	 */
	public void setVisualShortName(String shortName) {
		getUIController("id").setVisualValue(shortName);
	}

	/**
	 * Sets the visual field.
	 * 
	 * @param desc
	 *            the desc
	 * @param fieldid
	 *            the fieldid
	 * @param visualValue
	 *            the visual value
	 */
	static public void setVisualField(UIField desc, String fieldid, Object visualValue) {
		UIField field_ui = getUIController(desc, fieldid);
		if (field_ui != null) {
			field_ui.setVisualValue(visualValue);
		}
	}

	/**
	 * Sets the visual field.
	 * 
	 * @param fieldid
	 *            the fieldid
	 * @param visualValue
	 *            the visual value
	 */
	public void setVisualField(String fieldid, Object visualValue) {
		UIField field_ui = getUIController(fieldid);
		if (field_ui != null) {
			field_ui.setVisualValue(visualValue);
		}
	}

	/**
	 * Gets the uI controller.
	 * 
	 * @param fieldid
	 *            the fieldid
	 * 
	 * @return the uI controller
	 */
	public UIField getUIController(String fieldid) {
		return ((UIField) _parent).getParent().getField(fieldid);
	}

	/**
	 * Gets the uI controller.
	 * 
	 * @param field
	 *            the field
	 * @param fieldid
	 *            the fieldid
	 * 
	 * @return the uI controller
	 */
	static public UIField getUIController(UIField field, String fieldid) {
		return field.getParent().getField(fieldid);
	}

	/**
	 * Sets the message error.
	 * 
	 * @param msg
	 *            the new message error
	 */
	public void setMessageError(String msg) {
		((UIField) _parent).getPageController().setMessage(msg, IPageController.ERROR);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IEventListener#init(fr.imag.adele.cadse.core.ui.UIField)
	 */
	public void init(UIField field) {
	}

	public boolean isAnonymous() {
		return false;
	}
}
