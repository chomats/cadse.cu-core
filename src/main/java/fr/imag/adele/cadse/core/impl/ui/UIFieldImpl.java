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


import java.util.UUID;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.CollectedReflectLink;
import fr.imag.adele.cadse.core.impl.Item_Descriptor;
import fr.imag.adele.cadse.core.impl.ReflectLink;
import fr.imag.adele.cadse.core.ui.EPosLabel;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.util.Convert;

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
public class UIFieldImpl extends Item_Descriptor implements
		UIField {

	private int					_style	= 0;
	
	/** The _mc. */
	protected Item _mc;

	/** The _ic. */
	protected Item _ic;

	/** The _label. */
	protected String _label;

	protected boolean editable = true;

	/** The _pos label. */
	protected EPosLabel _posLabel;

	public IAttributeType<?> _attributeRef;
	
	
	public UIField[] _children = null;

	private int	_hspan;

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
	public UIFieldImpl(ItemType it, UUID uuid, IAttributeType<?> attr, String label,
			EPosLabel poslabel, Item mc, Item ic, Object ... keyvalues) {
		super(uuid, it, keyvalues);
		//assert mc != null;
		this._ic = ic;
		if (_ic != null)
			_ic.setParent(this, CadseGCST.FIELD_lt_IC);
		this._mc = mc;
		this._label = label;
		this._posLabel = poslabel;
		this._attributeRef = attr;
		_hspan = 1;
		if (it == CadseGCST.DBROWSER || it == CadseGCST.DLIST)
			_hspan = 2;
	}

	public UIFieldImpl(ItemType it, UUID id) {
		super(id, it);
		_hspan = 1;
		if (it == CadseGCST.DBROWSER || it == CadseGCST.DLIST)
			_hspan = 2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#getPosLabel()
	 */
	final public EPosLabel getPosLabel() {
		EPosLabel ret = null;
		ret = this._posLabel;

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
		return this._label + "(" + _attributeRef.getName() + ")";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#getAttributeName()
	 */
	final public String getAttributeName() {
		return _attributeRef == null ? null : _attributeRef.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#getInteractionController()
	 */
	public Item getInteractionController() {
		return _ic;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#getModelController()
	 */
	public Item getModelController() {
		return _mc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#getHSpan()
	 */
	public int getHSpan() {
		return _hspan;
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
	 * @see fr.imag.adele.cadse.core.ui.UIField#setEditable(boolean)
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#setVisible(boolean)
	 */
	public void setVisible(boolean v) {
		setHidden(!v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIField#setHidden(boolean)
	 */
	public void setHidden(boolean v) {
		setFlag(Item.IS_HIDDEN, v);
	}

	@Override
	protected void collectOutgoingLinks(LinkType linkType,
			CollectedReflectLink ret) {
		if (linkType == CadseGCST.FIELD_lt_IC) {
			ret.addOutgoing(CadseGCST.FIELD_lt_IC, _ic);
		}
		if (linkType == CadseGCST.FIELD_lt_MC && _mc != null) {
			ret.addOutgoing(CadseGCST.FIELD_lt_MC, _mc);
		}
		if (linkType == CadseGCST.FIELD_lt_ATTRIBUTE) {
			ret.addOutgoing(CadseGCST.FIELD_lt_ATTRIBUTE, _attributeRef);
		}
		super.collectOutgoingLinks(linkType, ret);
	}

	@Override
	public Link commitLoadCreateLink(LinkType lt, Item destination)
			throws CadseException {
		if (lt == CadseGCST.FIELD_lt_IC) {
			_ic = castItem(destination, Item.class);
			return new ReflectLink(lt, this, destination, 0);
		}
		if (lt == CadseGCST.FIELD_lt_MC) {
			_mc = castItem(destination, (Item.class));
			return new ReflectLink(lt, this, destination, 0);
		}
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
		if (lt == CadseGCST.FIELD_lt_IC && destination.isResolved()) {
			_ic = null;
			return;
		}
		if (lt == CadseGCST.FIELD_lt_MC && destination.isResolved()) {
			_mc = null;
			return;
		}
		if (lt == CadseGCST.FIELD_lt_ATTRIBUTE && destination.isResolved()) {
			_attributeRef = null;
			return;
		}
		super.removeOutgoingLink(link, notifie);
	}

	@Override
	public boolean commitSetAttribute(IAttributeType<?> type, Object value) {
		if (type == CadseGCST.FIELD_at_EDITABLE_) {
			editable = Convert.toBoolean(value, (Boolean) type
					.getDefaultValue());
			return true;
		}

		if (type == CadseGCST.FIELD_at_LABEL_) {
			_label = (String) value;
			return true;
		}
		if (type == CadseGCST.FIELD_at_POSITION_) {
			this._posLabel = (EPosLabel) type.convertTo(value);
			return true;
		}

		return super.commitSetAttribute(type, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.ui.UIField#internalGetOwnerAttribute(fr.imag
	 * .adele.cadse.core.attribute.IAttributeType)
	 */
	@Override
	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {
		if (CadseGCST.FIELD_at_POSITION_ == type) {
			return (T) this._posLabel;
		}
		if (type == CadseGCST.FIELD_at_EDITABLE_) {
			return (T) Boolean.valueOf(editable);
		}
		if (type == CadseGCST.FIELD_at_LABEL_) {
			return (T) this._label;
		}
		if (CadseGCST.ITEM_at_NAME_ == type) {
			return (T) getName();
		}
		if (CadseGCST.ITEM_at_DISPLAY_NAME_ == type) {
			return (T) getName();
		}

		return super.internalGetOwnerAttribute(type);
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
		return _attributeRef;
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
		return _children;
	}

	@Override
	public boolean isEditable() {
		return editable;
	}


	/**
	 * return a desciption of this field
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("uifield ").append(getName());
		sb.append(" of type ");
		ItemType it = getType();
		if (it != null) {
			sb.append(it.getDisplayName());
		} else {
			sb.append(getClass().getSimpleName());
		}
		Item inItemType = getPartParent();
		if (inItemType != null) {
			sb.append("in item type ").append(inItemType.getDisplayName());
		}

		return sb.toString();
	}

	@Override
	public Item getInteractionControllerBASE() {
		return _ic;
	}

	@Override
	public int getStyle() {
		return _style;
	}

	@Override
	public void setModelController(Item modelController) {
		_mc = modelController;
	}

	@Override
	public void setPositionLabel(EPosLabel poslabel) {
		_posLabel = poslabel;
	}

	@Override
	public void setStyle(int style) {
		_style = style;
	}

}
