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
import fr.imag.adele.cadse.core.ui.IActionPage;
import fr.imag.adele.cadse.core.ui.UIPlatform;

/**
 * .
 */

public class AbstractActionPage implements IActionPage {
	UIPlatform _uiPlatform;
	
	/**
	 * Instantiates a new abstract action page.
	 */
	public AbstractActionPage() {
	}

	public String getTypeId() {
		return getClass().getName();
	}

	@Override
	public void dispose(UIPlatform uiPlatform) {
	}

	@Override
	public void doCancel(UIPlatform uiPlatform, Object monitor) {
	}

	@Override
	public void doFinish(UIPlatform uiPlatform, Object monitor)
			throws Exception {
	}

	@Override
	public void init(UIPlatform uiPlatform) throws CadseException  {
		_uiPlatform = uiPlatform;
	}

	@Override
	public void initAfterUI(UIPlatform uiPlatform) {
	}

}