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
package fr.imag.adele.cadse.core.impl.internal.ui;

import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.IItemNode;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.impl.ui.PageImpl;
import fr.imag.adele.cadse.core.impl.ui.UIFieldImpl;
import fr.imag.adele.cadse.core.CadseRootCST;
import fr.imag.adele.cadse.core.ui.IActionPage;
import fr.imag.adele.cadse.core.ui.IPageFactory;

public class IPage2 extends PageImpl implements IPageFactory {
	Item parentItem;
	public IPage2(CompactUUID id, String name, ItemType parent) {
		super(id, name, parent);
	}

	public IPage2(CompactUUID id, String name, String label, String title,
			String description, boolean isPageComplete, int hspan,
			IActionPage action, UIFieldImpl... fields) {
		super(id, name, label, title, description, isPageComplete, hspan, action,
				fields);
	}

	public IPage2(CompactUUID id, String name, String label, String title,
			String description, boolean isPageComplete, int hspan) {
		super(id, name, label, title, description, isPageComplete, hspan);
	}

	public IPage2(String name, String label, String title, String description,
			boolean isPageComplete, int hspan, IActionPage action,
			UIFieldImpl... fields) {
		super(name, label, title, description, isPageComplete, hspan, action, fields);
		// TODO Auto-generated constructor stub
	}

	public IPage2(String name, String label, String title, String description,
			boolean isPageComplete, int hspan) {
		super(name, label, title, description, isPageComplete, hspan);
		// TODO Auto-generated constructor stub
	}

	public PageImpl createPage(int cas, Link l, Item item, IItemNode node,
			ItemType type, LinkType lt) {
		setParent(type, cas == PAGE_CREATION_ITEM ? CadseRootCST.META_ITEM_TYPE_lt_CREATION_PAGES: CadseRootCST.META_ITEM_TYPE_lt_MODIFICATION_PAGES);
		parentItem = item;
		return this;
	}

	public boolean isEmptyPage() {
		return false;
	}
	
	

}
