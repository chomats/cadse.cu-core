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
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.CollectedReflectLink;
import fr.imag.adele.cadse.core.impl.ReflectLink;
import fr.imag.adele.cadse.core.impl.internal.AbstractGeneratedItem;
import fr.imag.adele.cadse.core.ui.IActionPage;
import fr.imag.adele.cadse.core.ui.IPage;
import fr.imag.adele.cadse.core.util.Convert;
import fr.imag.adele.cadse.util.ArraysUtil;
import fr.imag.adele.cadse.util.OrderWay;

/**
 * The Class IPage. Represente une page graphique. Elle contient UIField,
 * eventuellement une action page
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class PageImpl extends AbstractGeneratedItem implements IPage {

	IAttributeType<?>[] 		_attributes;

	/** The _label. */
	String					_label;

	/** The _action, null si pas necessaire. */
	IActionPage				_action;

	/** The _id. */
	private String			shortName;

	/** The _title. */
	private String			_title;

	/** The _is page complete. */
	private boolean			_isPageComplete;

	/** The _description. */
	private String			_description;


	private ItemType		_parentItemType;


	private IPage[]			_owPages;

	public PageImpl(UUID id, String name, ItemType parent) {
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
	public PageImpl(UUID id, String name, String label, String title, String description,
			boolean isPageComplete, IActionPage action, IAttributeType<?>... fields) {
		super(id);
		_attributes = fields;
		shortName = name;
		_label = label;
		_action = action;
		_title = title;
		_description = description;
		_isPageComplete = isPageComplete;

	}

	public PageImpl(String name, String label, String title, String description, boolean isPageComplete) {
		super();
		_attributes = EMPTY_UIFIELD;
		shortName = name;
		_label = label;
		_action = null;
		_title = title;
		_description = description;
		_isPageComplete = isPageComplete;
	}

	public PageImpl(String name, String label, String title, String description, boolean isPageComplete, int hspan,
			IActionPage action, IAttributeType<?>... fields) {
		super();
		_attributes = fields;
		shortName = name;
		_label = label;
		_action = action;
		_title = title;
		_description = description;
		_isPageComplete = isPageComplete;

	}

	public PageImpl(UUID id, String name, String label, String title, String description,
			boolean isPageComplete, int hspan) {
		super(id);
		_attributes = EMPTY_UIFIELD;
		shortName = name;
		_label = label;
		_action = null;
		_title = title;
		_description = description;
		_isPageComplete = isPageComplete;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPage#getFields()
	 */
	public IAttributeType<?>[] getAttributes() {
		if (_attributes == null) {
			return EMPTY_UIFIELD;
		}
		return _attributes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.ui.IPage#addLast(fr.imag.adele.cadse.core.ui
	 * .UIField)
	 */
	public void addLast(IAttributeType<?>... attrs) {
		_attributes = ArraysUtil.addList(IAttributeType.class, _attributes, attrs);
	}
	
	@Override
	public void addLast(List<IAttributeType> attrs) {
		_attributes = ArraysUtil.addList(IAttributeType.class, _attributes, attrs);	
	}

	
	@Override
	public void addBefore(IAttributeType<?> beforeAttr,
			IAttributeType<?> attributeToInsert) {
		// TODO Auto-generated method stub
		int i = indexOf(beforeAttr);
		if (i == -1) {
			throw new NoSuchElementException("Not found attribute " + beforeAttr);
		}
		_attributes = ArraysUtil.add(IAttributeType.class, _attributes, attributeToInsert, i);
	}

	private int indexOf(IAttributeType<?> key) {
		if (_attributes != null) {
			for (int i = 0; i < _attributes.length; i++) {
				if (_attributes[i] == key) {
					return i;
				}
			}
		}
		return -1;
	}

	@Override
	public void addAfter(IAttributeType<?> afterAttr,
			IAttributeType<?> attributeToInsert) {
		int i = indexOf(afterAttr);
		if (i == -1) {
			throw new NoSuchElementException("Not found attribute " + afterAttr);
		}
		_attributes = ArraysUtil.add(IAttributeType.class, _attributes, attributeToInsert, i + 1);
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
	 * @see fr.imag.adele.cadse.core.ui.IPage#getLabel()
	 */
	public String getLabel() {
		return _label;
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
	 * @see fr.imag.adele.cadse.core.ui.IPage#getParentItemType()
	 */
	public ItemType getParentItemType() {
		return _parentItemType;
	}

	public void setParent(Item parent, LinkType lt) {
		_parentItemType = (ItemType) parent;
	}


	@Override
	public Link commitLoadCreateLink(LinkType lt, Item destination) throws CadseException {
		if (lt == CadseGCST.PAGE_lt_ATTRIBUTES && destination.isResolved()) {
			addLast((IAttributeType<?>) destination);
			return new ReflectLink(lt, this, destination, this._attributes.length - 1);
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

		if (lt == CadseGCST.PAGE_lt_ATTRIBUTES && destination.isResolved()) {
			_attributes = ArraysUtil.remove(IAttributeType.class, _attributes, (IAttributeType<?>) destination);
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
		if (linkType == CadseGCST.PAGE_lt_ATTRIBUTES) {
			ret.addOutgoing(CadseGCST.PAGE_lt_ATTRIBUTES, this._attributes);
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


	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IPage#isModificationPage()
	 */
	public boolean isModificationPage() {
		return getPartParentLinkType() == CadseGCST.TYPE_DEFINITION_lt_MODIFICATION_PAGES;
	}

	@Override
	public boolean commitMove(OrderWay kind, Link l1, Link l2) {
		if (l1.getLinkType() == CadseGCST.PAGE_lt_ATTRIBUTES && l2.getLinkType() == CadseGCST.PAGE_lt_ATTRIBUTES
				&& l1.isLinkResolved() && l2.isLinkResolved()) {
			return moveField(kind, (IAttributeType<?>) l1.getDestination(), (IAttributeType<?>) l2.getDestination());
		}
		return super.commitMove(kind, l1, l2);
	}

	private boolean moveField(OrderWay kind, IAttributeType f1, IAttributeType f2) {
		if (_attributes == null) {
			return false;
		}
		return ArraysUtil.move(kind, _attributes, f1, f2);
	}

	@Override
	public boolean isEmptyPage() {
		return _attributes == null || _attributes.length == 0;
	}

	@Override
	public IPage[] getOverwritePage() {
		return _owPages;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.ui.IPage#isLast(fr.imag.adele.cadse.core.ui.
	 * UIField)
	 */
	public boolean isLast(IAttributeType<?> field) {
		if (_attributes == null) {
			return false;
		}	

		return _attributes[_attributes.length - 1] == field;
	}

	@Override
	public void getAllAttributes(HashSet<IAttributeType<?>> allAttributes) {
		if (_attributes != null)
			allAttributes.addAll(Arrays.asList(_attributes));
	}

}
