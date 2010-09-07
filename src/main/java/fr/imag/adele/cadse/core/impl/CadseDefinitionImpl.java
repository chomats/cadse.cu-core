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
package fr.imag.adele.cadse.core.impl;

import java.util.UUID;

import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.util.Convert;

public class CadseDefinitionImpl extends CadseRuntimeImpl {

	public CadseDefinitionImpl(String name, UUID runtimeId,
			UUID definitionId) {
		super(name, runtimeId, definitionId);
	}

	
	@Override
	public ItemType getType() {
		return CadseGCST.CADSE_DEFINITION;
	}
	
	@Override
	public boolean commitSetAttribute(IAttributeType<?> type, Object value) {
		if (type == CadseGCST.CADSE_DEFINITION_at_CADSE_NAME_) {
			_cadseName = Convert.toString(value);
			return true;
		}
		
		if (type == CadseGCST.CADSE_at_DESCRIPTION_) {
			_description = Convert.toString(value);
			return true;
		}
		
		return super.commitSetAttribute(type, value);
	}
	
	@Override
	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {
		if (CadseGCST.CADSE_DEFINITION_at_CADSE_NAME_ == type) {
			return (T) _cadseName;
		}
		return super.internalGetOwnerAttribute(type);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#getDisplayName()
	 */
	public String getDisplayName() {
		return getType().getItemManager().getDisplayName(this);
	}

}
