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
import fr.imag.adele.cadse.core.IItemNode;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.ui.IActionPage;
import fr.imag.adele.cadse.core.ui.IPageController;

/**
 * The Class ModificationAction.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class ModificationAction extends AbstractActionPage implements IActionPage {

	private ItemType	it;
	private Item item;

	/**
	 * Instantiates a new modification action.
	 * 
	 * @param node
	 *            the node
	 */
	public ModificationAction(Item item) {
		this.item = item;
		this.it = item.getType();
	}

	/**
	 * Instantiates a new modification action.
	 * 
	 * @param node
	 *            the node
	 */
	public ModificationAction(ItemType it, IItemNode node) {
		this.it = it;
	}

	/**
	 * Instantiates a new modification action.
	 * 
	 * @param selected
	 *            the selected
	 */
	public ModificationAction(ItemType it, Item selected) {
		this.it = it;
	}

	@Override
	public String getTypeId() {
		return it.getId().toString();
	}

	public ItemType getItemType() {
		return it;
	}
	
	@Override
	public void init(IPageController uiPlatform) throws CadseException {
		uiPlatform.setVariable(getTypeId(), item);
	}

}
