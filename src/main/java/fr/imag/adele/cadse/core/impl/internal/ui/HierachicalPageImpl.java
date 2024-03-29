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
package fr.imag.adele.cadse.core.impl.internal.ui;

import java.util.HashSet;
import java.util.List;

import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.internal.AbstractGeneratedItem;
import fr.imag.adele.cadse.core.impl.ui.PageImpl;
import fr.imag.adele.cadse.core.ui.HierarchicalPage;
import fr.imag.adele.cadse.core.ui.IActionPage;
import fr.imag.adele.cadse.core.ui.IPage;
import fr.imag.adele.cadse.util.ArraysUtil;

public class HierachicalPageImpl extends AbstractGeneratedItem implements IPage, HierarchicalPage {
	protected IPage[] _bloc = null;
	private ItemType _it;
	boolean _modificationPage;
	boolean _groupPage;
	
	public HierachicalPageImpl(ItemType it, boolean modificationPage) {
		_it = it;
		_modificationPage = modificationPage;
	}
	
	
	public IPage insertNewBloc(String label) {
		PageImpl p = new PageImpl(label, label, label, label, true);
		_bloc = ArraysUtil.add(IPage.class, _bloc, p);
		return p;
	}
	
	@Override
	public void addAfter(IAttributeType<?> afterAttr,
			IAttributeType<?> attributeToInsert) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void addBefore(IAttributeType<?> beforeAttr,
			IAttributeType<?> attributeToInsert) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void addLast(List<IAttributeType<?>> attrs) {
		throw new UnsupportedOperationException();
	}
	
	public void addLast(IAttributeType<?>... attr) {
		throw new UnsupportedOperationException();
	}


	@Override
	public IActionPage getActionPage() {
		return null;
	}


	@Override
	public IAttributeType<?>[] getAttributes() {
		return EMPTY_UIFIELD;
	}


	@Override
	public String getDescription() {
		return _it.getDisplayName();
	}


	@Override
	public String getLabel() {
		return _it.getDisplayName();
	}


	@Override
	public IPage[] getOverwritePage() {
		return null;
	}


	@Override
	public ItemType getParentItemType() {
		return _it;
	}


	@Override
	public String getTitle() {
		return _it.getDisplayName();
	}


	@Override
	public boolean isEmptyPage() {
		if (_bloc != null) {
			for (IPage p : _bloc) {
				if (!p.isEmptyPage())
					return false;
			}
		}
		return true;
	}


	@Override
	public boolean isModificationPage() {
		return _modificationPage;
	}


	@Override
	public ItemType getType() {
		return _it;
	}


	@Override
	public boolean isLast(IAttributeType<?> attributeDefinition) {
		return false;
	}


	@Override
	public IPage[] getBlocks() {
		return _bloc;
	}


	@Override
	public void getAllAttributes(HashSet<IAttributeType<?>> allAttributes) {
		if (_bloc != null)
			for (IPage p : _bloc) {
				p.getAllAttributes(allAttributes);
			}
	}


	@Override
	public IAttributeType<?>[] getHiddenAttributes() {
		throw new UnsupportedOperationException();
	}


	@Override
	public void addHiddenAttributes(IAttributeType<?>... attr) {
		throw new UnsupportedOperationException();
	}


	@Override
	public void addReadOnlyAttributes(IAttributeType<?>... attr) {
		throw new UnsupportedOperationException();
	}


	@Override
	public IAttributeType<?>[] getReadOnlyAttributes() {
		throw new UnsupportedOperationException();
	}


	@Override
	public boolean isGroupPage() {
		return _groupPage;
	}
	
	public void setGroupPage(boolean groupPage) {
		_groupPage = groupPage;
	}


	@Override
	public void addOverridePage(IPage... pages) {
		throw new UnsupportedOperationException();
	}


	@Override
	public void setTitle(String title) {
		// do nothing : cannot change
	}
	
	@Override
	public void setLabel(String label) {
		// do nothing : cannot change
	}
	
}
