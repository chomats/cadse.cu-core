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
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.CollectedReflectLink;
import fr.imag.adele.cadse.core.impl.ReflectLink;
import fr.imag.adele.cadse.core.ui.EPosLabel;
import fr.imag.adele.cadse.core.ui.IFedeFormToolkit;
import fr.imag.adele.cadse.core.ui.IPage;
import fr.imag.adele.cadse.core.ui.IPageController;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.util.Convert;

public class UIField2 extends UIFieldImpl {

	UIFieldImpl	display;

	public UIField2(CompactUUID uuid, String key) {
		super(uuid, key, key, EPosLabel.defaultpos, null, null);
	}

	@Override
	public Object createControl(IPageController globalUIController, IFedeFormToolkit toolkit, Object container,
			int hspan) {
		return display.createControl(globalUIController, toolkit, container, hspan);
	}

	@Override
	public Object getUIObject(int index) {
		return display.getUIObject(index);
	}

	@Override
	public Object getVisualValue() {
		return display.getVisualValue();
	}

	@Override
	public void internalSetEditable(boolean v) {
		display.internalSetEditable(v);
	}

	@Override
	public void init() throws CadseException {
		display.init();
	}
	
	@Override
	public void setPage(IPage p) {
		super.setPage(p);
		display.setPage(p);
	}

	@Override
	public void init(IPageController globalController) {
		super.init(globalController);
		if (display._mc != null) {
			_mc = display._mc;
		} else {
			display._mc = _mc = new MC_AttributesItem();
			display._mc.setUIField(this.display);

		}
		_ic = display._ic;
		display.init(globalController);
	}

	@Override
	public void init(UIField field) {
		display.init(field);
	}

	@Override
	public Object getValueForVisual() {
		return display.getValueForVisual();
	}

	@Override
	public void setEnabled(boolean v) {
		display.setEnabled(v);
	}
	
	@Override
	public boolean setFlag(int f, boolean flag) {
		if (f == Item.UI_RUNNING)
			display.setFlag(f, flag);
		return super.setFlag(f, flag);
	}

	@Override
	public void setVisible(boolean v) {
		// super.setHidden(!v);
		display.setVisible(v);
	}

	@Override
	public void setHidden(boolean v) {
		super.setHidden(v);
		display.setHidden(v);
	}

	@Override
	public void setVisualValue(Object visualValue) {
		display.setVisualValue(visualValue);
	}

	@Override
	public void updateValue() {
		display.updateValue();
	}

	public ItemType getType() {
		return CadseGCST.FIELD;
	}

	@Override
	protected void collectOutgoingLinks(LinkType linkType, CollectedReflectLink ret) {
		// if (linkType == CadseGCST.FIELD_lt_ATTRIBUTE) {
		// ret.addOutgoing(CadseGCST.FIELD_lt_ATTRIBUTE, attributeRef);
		// }
		if (linkType == CadseGCST.FIELD_lt_DISPLAY) {
			ret.addOutgoing(CadseGCST.FIELD_lt_DISPLAY, display);
		}
		super.collectOutgoingLinks(linkType, ret);
	}

	@Override
	public Link commitLoadCreateLink(LinkType lt, Item destination) throws CadseException {
		if (lt == CadseGCST.FIELD_lt_DISPLAY) {
			this.display = (UIFieldImpl) destination;
			return new ReflectLink(lt, this, destination, 0);
		}
		return super.commitLoadCreateLink(lt, destination);
	}

	@Override
	public void removeOutgoingLink(Link link, boolean notifie) {
		Item destination = link.getDestination();
		LinkType lt = link.getLinkType();
		if (lt == CadseGCST.FIELD_lt_DISPLAY && destination.isResolved()) {
			display = null;
			return;
		}
		super.removeOutgoingLink(link, notifie);
	}

	@Override
	public boolean commitSetAttribute(IAttributeType<?> type, String key, Object value) {
		if (key.equals(CadseGCST.FIELD_at_EDITABLE)) {
			editable = Convert.toBoolean(value, (Boolean) type.getDefaultValue());
			return true;
		}

		if (key.equals(CadseGCST.FIELD_at_LABEL)) {
			_label = (String) value;
			return true;
		}
		if (key.equals(CadseGCST.FIELD_at_POSITION)) {
			this._posLabel = (EPosLabel) type.convertTo(value);
			return true;
		}
		return super.commitSetAttribute(type, key, value);
	}

	@Override
	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {
		if (CadseGCST.FIELD_at_EDITABLE_ == type) {
			return (T) Boolean.valueOf(editable);
		}
		if (CadseGCST.FIELD_at_LABEL_ == type) {
			return (T) this._label;
		}
		if (CadseGCST.FIELD_at_POSITION_ == type) {
			return (T) this._posLabel;
		}
		return super.internalGetOwnerAttribute(type);
	}

	@Override
	public Item getPartParent() {
		return getPage();
	}
	
	@Override
	public Item getPartParent(boolean attemptToRecreate) {
		return this.getPage();
	}

	@Override
	protected EPosLabel getDefaultPosLabel() {
		if (display != null) {
			return display.getDefaultPosLabel();
		}
		return super.getDefaultPosLabel();
	}

	@Override
	public void internalSetVisible(boolean v) {
		display.internalSetVisible(v);
	}

	public void setVisualValue(Object visualValue, boolean sendNotification) {
		display.setVisualValue(visualValue, sendNotification);

	}

}
