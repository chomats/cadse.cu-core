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
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.impl.internal.AbstractGeneratedItem;
import fr.imag.adele.cadse.core.ui.IActionPage;
import fr.imag.adele.cadse.core.ui.IFieldDescription;
import fr.imag.adele.cadse.core.ui.IPage;
import fr.imag.adele.cadse.core.ui.IPageController;
import fr.imag.adele.cadse.core.ui.IPageObject;
import fr.imag.adele.cadse.core.ui.Pages;

/**
 * .
 */

public class AbstractActionPage implements IActionPage {

	/**
	 * Instantiates a new abstract action page.
	 */
	public AbstractActionPage() {
	}

	public String getTypeId() {
		return getClass().getName();
	}

	@Override
	public void dispose(IPageController uiPlatform) {
	}

	@Override
	public void doCancel(IPageController uiPlatform, Object monitor) {
	}

	@Override
	public void doFinish(IPageController uiPlatform, Object monitor)
			throws Exception {
	}

	@Override
	public void init(IPageController uiPlatform) throws CadseException {
	}

	@Override
	public void initAfterUI(IPageController uiPlatform) {
	}

}