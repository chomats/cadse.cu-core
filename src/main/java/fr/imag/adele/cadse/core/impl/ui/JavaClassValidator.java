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

import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.impl.ui.mc.UIValidator_Descriptor;
import fr.imag.adele.cadse.core.ui.UIRunningValidator;

public class JavaClassValidator extends UIValidator_Descriptor {
	
	
	public JavaClassValidator(ItemType it, Object... keyvalues) {
		super(it, keyvalues);
	}

	Class<? extends UIRunningValidator> _clazz;
	
	
	@Override
	public UIRunningValidator create() {
		try {
			if (_clazz != null)
				return _clazz.newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		return null;
	}

	public Class<? extends UIRunningValidator> getClazz() {
		return _clazz;
	}

	public void setClazz(Class<? extends UIRunningValidator> clazz) {
		_clazz = clazz;
	}


}
