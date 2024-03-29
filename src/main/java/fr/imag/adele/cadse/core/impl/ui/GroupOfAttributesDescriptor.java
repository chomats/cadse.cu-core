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
 *
 * Copyright (C) 2006-2010 Adele Team/LIG/Grenoble University, France
 */
package fr.imag.adele.cadse.core.impl.ui;

import java.util.UUID;

import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.attribute.CheckStatus;
import fr.imag.adele.cadse.core.attribute.GroupOfAttributes;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.attribute.AttributeType;
import fr.imag.adele.cadse.core.ui.EPosLabel;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.util.ArraysUtil;

public class GroupOfAttributesDescriptor extends AttributeType implements
		GroupOfAttributes {

	private GroupOfAttributes _ow = null;
	private IAttributeType<?>[] _attr = null;
	private int _column;
	private String _label;
	private boolean _hasBoxGroup;

	public GroupOfAttributesDescriptor(UUID id, String label, int column,
			Object[] keyvalues) {
		super(id, label, 0);
		_column = column;
		_label = label;
		_hasBoxGroup = true;
	}

	public boolean isHasBoxGroup() {
		return _hasBoxGroup;
	}
	
	public void setHasBoxGroup(boolean hasBoxGroup) {
		_hasBoxGroup = hasBoxGroup;
	}

	/**
	 * 
	 * @param label the group label
	 * @param column the number column
	 * @param keyvalues attributes (key,value)
	 */
	public GroupOfAttributesDescriptor(String label, int column,
			Object... keyvalues) {
		this(UUID.randomUUID(), label, column, keyvalues);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.impl.ui.GroupAttributes#getAttributes()
	 */
	public IAttributeType<?>[] getAttributes() {
		return _attr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.impl.ui.GroupAttributes#add(fr.imag.adele.cadse
	 * .core.attribute.IAttributeType)
	 */
	public void add(IAttributeType<?> attributes) {
		_attr = ArraysUtil.add(IAttributeType.class, _attr, attributes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.impl.ui.GroupAttributes#getOverWriteGroup()
	 */
	public GroupOfAttributes getOverWriteGroup() {
		return _ow;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.impl.ui.GroupAttributes#setOverWriteGroup(fr
	 * .imag.adele.cadse.core.impl.ui.GroupAttributes)
	 */
	public void setOverWriteGroup(GroupOfAttributes ow) {
		_ow = ow;
	}

	@Override
	public int getColumn() {
		return _column;
	}

	@Override
	public String getLabel() {
		return _label;
	}

	@Override
	public String getName() {
		return _label;
	}

	@Override
	public String getDisplayName() {
		return _label;
	}

	@Override
	public Class<Object> getAttributeType() {
		return Object.class;
	}

	@Override
	public ItemType getType() {
		return CadseGCST.GROUP_OF_ATTRIBUTES;
	}

	@Override
	public IAttributeType<?>[] getChildren() {
		return _attr;
	}
	
	

	@Override
	public CheckStatus check(Item item, Object value) {
		return null;
	}

	@Override
	public UIField generateDefaultField() {
		UIFieldImpl uiFieldImpl = new UIFieldImpl(CadseGCST.DGROUP, UUID.randomUUID(), this,
				_hasBoxGroup? getName() : null, EPosLabel.none, null, null,
				CadseGCST.DGROUP_at_COLUMN_, _column,
				CadseGCST.DGROUP_at_MAKE_COLUMNS_EQUAL_WIDTH_, false);
		uiFieldImpl.setFlag(UI_NO_BORDER, !_hasBoxGroup);
		return uiFieldImpl;
	}
}
