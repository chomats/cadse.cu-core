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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.IItemNode;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.impl.CadseIllegalArgumentException;
import fr.imag.adele.cadse.core.impl.CollectedReflectLink;
import fr.imag.adele.cadse.core.ui.IPage;
import fr.imag.adele.cadse.core.ui.IPageFactory;

/**
 * A factory for creating ConfigurablePage objects.
 */
public class ConfigurablePageFactory extends PageImpl implements IPageFactory {

	/** The cas. */
	private int								cas;

	/** The m. */
	private Constructor<? extends IPage>	m;

	/**
	 * Instantiates a new configurable page factory.
	 * 
	 * @param cas
	 *            the cas
	 * @param id
	 *            the id
	 * @param clazz
	 *            the clazz
	 * 
	 * @throws SecurityException
	 *             the security exception
	 * @throws NoSuchMethodException
	 *             the no such method exception
	 */
	public ConfigurablePageFactory(ItemType parent, CompactUUID id, int cas, String shortname,
			Class<? extends IPage> clazz) throws SecurityException, NoSuchMethodException {
		super(id, shortname, parent);
		this.cas = cas;
		switch (cas) {
			case PAGE_CREATION_ITEM:
				this.m = clazz.getConstructor(Item.class, ItemType.class, LinkType.class);
				break;
			case PAGE_PROPERTY_ITEM:
				this.m = clazz.getConstructor(Item.class);
				break;
			case PAGE_PROPERTY_VIEW_ITEM:
				this.m = clazz.getConstructor(Item.class, IItemNode.class);
				break;
			default:
				break;
		}
		if (m == null) {
			throw new NoSuchMethodException();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.PageFactory#createPage(int,
	 *      fr.imag.adele.cadse.core.Link, fr.imag.adele.cadse.core.Item,
	 *      fr.imag.adele.cadse.core.IItemNode,
	 *      fr.imag.adele.cadse.core.ItemType,
	 *      fr.imag.adele.cadse.core.LinkType)
	 */
	public IPage createPage(int cas, Link l, Item item, IItemNode node, ItemType type, LinkType lt) {
		if (this.cas != cas) {
			return null;
		}

		try {
			IPage newPage;
			switch (cas) {
				case PAGE_CREATION_ITEM:
					newPage = this.m.newInstance(item, type, lt);
					newPage.setParent(type, CadseGCST.TYPE_DEFINITION_lt_CREATION_PAGES);
					return newPage;
				case PAGE_PROPERTY_ITEM:
					newPage = this.m.newInstance(item);
					newPage.setParent(type, CadseGCST.TYPE_DEFINITION_lt_MODIFICATION_PAGES);
					return newPage;
				case PAGE_PROPERTY_VIEW_ITEM:
					newPage = this.m.newInstance(item, node);
					newPage.setParent(type, CadseGCST.TYPE_DEFINITION_lt_MODIFICATION_PAGES);
					return newPage;
			}
		} catch (IllegalArgumentException e) {
			throw new CadseIllegalArgumentException(e.getMessage(), e);
		} catch (SecurityException e) {
			throw new CadseIllegalArgumentException(e.getMessage(), e);
		} catch (InstantiationException e) {
			throw new CadseIllegalArgumentException(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new CadseIllegalArgumentException(e.getMessage(), e);
		} catch (InvocationTargetException e) {
			throw new CadseIllegalArgumentException(e.getTargetException().getMessage(), e.getTargetException());
		}
		return null;
	}

	@Override
	public ItemType getType() {
		return CadseGCST.PAGE;
	}

	public boolean isEmptyPage() {
		return false;
	}

	

	IPage	cachedPage	= null;

	private IPage getCachedPage() {
		if (cachedPage != null) {
			cachedPage = createPage(cas, null, null, null, getParentItemType(), null);
		}
		return cachedPage;
	}

}
