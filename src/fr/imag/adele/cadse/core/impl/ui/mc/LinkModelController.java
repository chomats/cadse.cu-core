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
package fr.imag.adele.cadse.core.impl.ui.mc;

import java.util.List;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.ChangeID;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.attribute.EnumAttributeType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.CadseCore;
import fr.imag.adele.cadse.core.impl.CadseIllegalArgumentException;
import fr.imag.adele.cadse.core.impl.ui.AbstractModelController;
import fr.imag.adele.cadse.core.impl.ui.MC_AttributesItem;
import fr.imag.adele.cadse.core.oper.WSODeleteLink;
import fr.imag.adele.cadse.core.ui.IModelController;
import fr.imag.adele.cadse.core.ui.IPageController;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.util.Convert;

public class LinkModelController extends MC_AttributesItem implements IModelController {

	private boolean					mandatory	= false;
	private String					msg			= null;
	boolean							init;
	Object defaultValue = null;

	public LinkModelController(boolean mandatory, String msg) {
		this.mandatory = mandatory;
		this.msg = msg;
		init = false;
	}

	public LinkModelController(CompactUUID id) {
		super(id);
		init = true;
	}

	@Override
	public void init(IPageController uiPlatform) throws CadseException {
		super.init(uiPlatform);
		Item item = uiPlatform.getItem(getUIField());
		if (item == null) {
			throw new CadseIllegalArgumentException("No item in the context.");
		}
		IAttributeType<?> attRef = getUIField().getAttributeDefinition();
		if (attRef == null) {
			throw new CadseIllegalArgumentException("Cannot find the attribute type {0} in the item type {1}.",
					getUIField().getLabel(), item.getType().getName());
		}			
		
		if (init) {
			mandatory = attRef.mustBeInitializedAtCreationTime();
		}
		if (attRef.getType() == CadseGCST.LINK) {
			LinkType lt = (LinkType)attRef;
			if (!item.isInstanceOf(lt.getSource())) {
				throw new CadseIllegalArgumentException("The link type {0} in the item type {1} is bad.", attRef.getName(), item.getType().getName());
			}
			// removed old api
			// item.getWorkspaceDomain().addListener(this);
			uiPlatform.addListener(item, new ItemLinkTypeWorkspaceListener(uiPlatform, item, getUIField(), lt),
					ChangeID.CREATE_OUTGOING_LINK.ordinal() + ChangeID.ORDER_OUTGOING_LINK.ordinal()
					+ ChangeID.DELETE_OUTGOING_LINK.ordinal());
		}
	}
	
	
	

	@Override
	public void initAfterUI(IPageController uiPlatform) {
		IAttributeType<?> attRef = getUIField().getAttributeDefinition();
		if (attRef.getType() == CadseGCST.LINK) {
			LinkType lt = (LinkType)attRef;
			if (lt.isPart()) {
				uiPlatform.setEnabled(getUIField(), false);
			}
		}
	}


	@Override
	public Object getValue(IPageController uiPlatform) {
		Item item = uiPlatform.getItem(getUIField());
		if (item == null) {
			throw new CadseIllegalArgumentException("No item in the context.");
		}
		IAttributeType<?> attRef = getUIField().getAttributeDefinition();
		if (attRef.getType() == CadseGCST.LINK) {
			LinkType lt = (LinkType)attRef;
			List<Link> ret = item.getOutgoingLinks(lt);

			if (lt.getMax() == 1) {
				return ret.size() >= 1 ? ret.get(0) : null;
			}
			return ret;
		}
		if (attRef.getType() == CadseGCST.BOOLEAN) {
			Object value = super.getValue(uiPlatform);
			if (value == null) {
				Object _defaultValue = defaultValue();
				if (_defaultValue == null) {
					_defaultValue = Boolean.FALSE;
				}
	
				super.notifieValueChanged(uiPlatform, getUIField(), _defaultValue.toString());
				return _defaultValue;
			}
			return Convert.toBoolean(value);
		}
		if (attRef.getType() == CadseGCST.ENUM) {
			Object value = super.getValue(uiPlatform);
			if (value == null ) {
				if (defaultValue == null)
					return null;
				value = toEnum(value);
				super.notifieValueChanged(uiPlatform, getUIField(), value);
				return value;
			}
			if (value instanceof String)
				return toEnum(value);
			return value;
		}
			
		return null;
	}

	private Object toEnum(Object value) {
		IAttributeType<?> attRef = getUIField().getAttributeDefinition();
		if (attRef.getType() == CadseGCST.ENUM) {
			EnumAttributeType<?> type = (EnumAttributeType<?>) attRef;
			return type.toEnum(value);
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public void notifieValueChanged(IPageController uiPlatform, UIField field, Object value) {
		// do nothing for link...
		IAttributeType<?> attRef = getUIField().getAttributeDefinition();
		if (attRef.getType() == CadseGCST.BOOLEAN) {
			super.notifieValueChanged(uiPlatform, field, Convert.toBoolean(value));
		}
		if (attRef.getType() == CadseGCST.ENUM) {
			super.notifieValueChanged(uiPlatform, field, toEnum(value));
		}
	}

	@Override
	public boolean validValueChanged(IPageController uiPlatform, UIField field, Object value) {
		IAttributeType<?> attRef = getUIField().getAttributeDefinition();
		if (mandatory && value == null) {
			if (msg != null) {
				uiPlatform.setMessageError(msg);
			} else {
				if (attRef.getType() == CadseGCST.LINK) {
					LinkType lt = (LinkType)attRef;
					uiPlatform.setMessageError("The link " + lt.getName() + " must be set");
				}
			}
			return true;
		}
		return super.validValueChanged(uiPlatform, field, value);
	}

	@Override
	public void notifieValueDeleted(IPageController uiPlatform, UIField field, Object oldvalue) {
		if (oldvalue instanceof Link) {
			Link l = (Link) oldvalue;
			WSODeleteLink oper = new WSODeleteLink(l);
			oper.execute();
			CadseCore.registerInTestIfNeed(oper);
		}
	}

	public ItemType getType() {
		return CadseGCST.LINK_MODEL_CONTROLLER;
	}

	@Override
	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {
		if (CadseGCST.LINK_MODEL_CONTROLLER_at_ERROR_MESSAGE_ == type) {
			return (T) msg;
		}
		return super.internalGetOwnerAttribute(type);
	}

	@Override
	public boolean commitSetAttribute(IAttributeType<?> type, Object value) {
		if (CadseGCST.LINK_MODEL_CONTROLLER_at_ERROR_MESSAGE_ == type) {
			msg = Convert.toString(value);
			return true;
		}
		return super.commitSetAttribute(type, value);
	}

	@Override
	public Object defaultValue() {
		IAttributeType<?> attRef = getUIField().getAttributeDefinition();
		if (attRef.getType() == CadseGCST.BOOLEAN) {
			if (attRef.getDefaultValue() != null) {
				return attRef.getDefaultValue();
			}
			return Boolean.FALSE;
		}
		return super.defaultValue();
	}
}
