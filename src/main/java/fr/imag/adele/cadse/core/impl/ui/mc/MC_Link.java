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

import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.ChangeID;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.attribute.EnumAttributeType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.CadseCore;
import fr.imag.adele.cadse.core.impl.CadseIllegalArgumentException;
import fr.imag.adele.cadse.core.oper.WSODeleteLink;
import fr.imag.adele.cadse.core.ui.RunningModelController;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.ui.UIPlatform;
import fr.imag.adele.cadse.core.util.Convert;

public class MC_Link extends MC_AttributesItem implements RunningModelController {

	private boolean					mandatory	= false;
	private String					msg			= null;
	boolean							init;
//	Object defaultValue = null;
	
	

	public MC_Link(boolean mandatory, String msg) {
		this.mandatory = mandatory;
		this.msg = msg;
		init = false;
	}

	public MC_Link() {
		init = true;
	}
	
	@Override
	public void init(UIPlatform uiPlatform) {
		super.init(uiPlatform);
		Item item = _uiPlatform.getItem(getUIField());
		if (item == null) {
			throw new CadseIllegalArgumentException("No item in the context.");
		}
		IAttributeType<?> attRef = getUIField().getAttributeDefinition();
		if (attRef == null) {
			throw new CadseIllegalArgumentException("Cannot find the attribute type {0} in the item type {1}.",
					getUIField().getLabel(), item.getType().getName());
		}			
		
		
		if (attRef.getType() == CadseGCST.LINK_TYPE) {
			LinkType lt = (LinkType)attRef;
			if (!item.isInstanceOf(lt.getSource())) {
				throw new CadseIllegalArgumentException("The link type {0} in the item type {1} is bad.", attRef.getName(), item.getType().getName());
			}
			
			if (init) {
				mandatory = lt.getMin()>0;
				msg = _desc == null ? null : _desc.getAttribute(CadseGCST.MC_LINK_at_ERROR_MESSAGE_);
				if (msg == null || msg.length() == 0)
					msg = "The link " + lt.getName() + " must be set";
			}
			// removed old api
			// item.getWorkspaceDomain().addListener(this);
			_uiPlatform.addListener(item, new ItemLinkTypeWorkspaceListener(_uiPlatform, item, getUIField(), lt),
					ChangeID.CREATE_OUTGOING_LINK.ordinal() + ChangeID.ORDER_OUTGOING_LINK.ordinal()
					+ ChangeID.DELETE_OUTGOING_LINK.ordinal());
		}
	}
	
	
	

	@Override
	public void initAfterUI(UIField field) {
		IAttributeType<?> attRef = getUIField().getAttributeDefinition();
		if (attRef.getType() == CadseGCST.LINK_TYPE) {
			LinkType lt = (LinkType)attRef;
			if (lt.isPart()) {
				_uiPlatform.setEnabled(getUIField(), false);
			}
		}
	}

	
	
	@Override
	protected Object modelToVisual(Object ret) {
		IAttributeType<?> attRef = getUIField().getAttributeDefinition();
		if (attRef.getType() == CadseGCST.LINK_TYPE) {
			LinkType lt = (LinkType)attRef;
			List<Link> ret2 = (List<Link>) ret;
			if (lt.getMax() == 1) {
				return ret2.size() >= 1 ? ret2.get(0) : null;
			}
			return ret;
		}
		return ret;
	}
	

	@Override
	public Object getValue() {
		Item item = _uiPlatform.getItem(getUIField());
		if (item == null) {
			throw new CadseIllegalArgumentException("No item in the context.");
		}
		IAttributeType<?> attRef = getUIField().getAttributeDefinition();
		if (attRef.getType() == CadseGCST.LINK_TYPE) {
			LinkType lt = (LinkType)attRef;
			List<Link> ret = item.getOutgoingLinks(lt);

			if (lt.getMax() == 1) {
				return ret.size() >= 1 ? ret.get(0) : null;
			}
			return ret;
		}
		if (attRef.getType() == CadseGCST.BOOLEAN) {
			Object value = super.getValue();
			if (value == null) {
				Object _defaultValue = defaultValue();
				if (_defaultValue == null) {
					_defaultValue = Boolean.FALSE;
				}
	
				super.notifieValueChanged(getUIField(), _defaultValue.toString());
				return _defaultValue;
			}
			return Convert.toBoolean(value);
		}
		if (attRef.getType() == CadseGCST.ENUM) {
			Object value = super.getValue();
			if (value == null ) {
				//if (defaultValue == null)
					return null;
				//value = toEnum(value);
				//super.notifieValueChanged( getUIField(), value);
				//return value;
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
	public void notifieValueChanged(UIField field, Object value) {
		// do nothing for link...
		IAttributeType<?> attRef = getUIField().getAttributeDefinition();
		if (attRef.getType() == CadseGCST.BOOLEAN) {
			super.notifieValueChanged( field, Convert.toBoolean(value));
		}
		if (attRef.getType() == CadseGCST.ENUM) {
			super.notifieValueChanged( field, toEnum(value));
		}
	}

	@Override
	public boolean validValueChanged(UIField field, Object value) {
		IAttributeType<?> attRef = getUIField().getAttributeDefinition();
		if (mandatory && value == null) {
			if (msg != null) {
				_uiPlatform.setMessageError(msg);
			} else {
				if (attRef.getType() == CadseGCST.LINK_TYPE) {
					LinkType lt = (LinkType)attRef;
					_uiPlatform.setMessageError("The link " + lt.getName() + " must be set");
				}
			}
			return true;
		}
		return super.validValueChanged(field, value);
	}

	@Override
	public void notifieValueDeleted(UIField field, Object oldvalue) {
		if (oldvalue instanceof Link) {
			Link l = (Link) oldvalue;
			WSODeleteLink oper = new WSODeleteLink(l);
			oper.execute();
			CadseCore.registerInTestIfNeed(oper);
		}
	}

	public ItemType getType() {
		return CadseGCST.MC_LINK;
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
