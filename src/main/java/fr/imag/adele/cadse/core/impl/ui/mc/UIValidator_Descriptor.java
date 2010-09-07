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
package fr.imag.adele.cadse.core.impl.ui.mc;

import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.Item_Descriptor;
import fr.imag.adele.cadse.core.ui.UIValidator;

public abstract class UIValidator_Descriptor extends Item_Descriptor implements UIValidator {
	
	
	private int _error;
	private UIValidator[] _ow;
	private IAttributeType<?>[] _listen;
	
	public UIValidator_Descriptor(ItemType it, Object ... keyvalues) {
		super(it, keyvalues);
	}
	

	@Override
	public IAttributeType<?>[] getListenAttributeType() {
		return _listen;
	}

	@Override
	public UIValidator[] getOverwriteValidator() {
		return _ow;
	}

	@Override
	public int incrementError() {
		return ++_error;
	}


	public int getError() {
		return _error;
	}

	public void setError(int error) {
		_error = error;
	}

	public UIValidator[] getOw() {
		return _ow;
	}

	public void setOw(UIValidator... ow) {
		_ow = ow;
	}

	public IAttributeType<?>[] getListenAttributes() {
		return _listen;
	}

	public void setListenAttributes(IAttributeType<?>... listenAttributes) {
		_listen = listenAttributes;
	}
}
