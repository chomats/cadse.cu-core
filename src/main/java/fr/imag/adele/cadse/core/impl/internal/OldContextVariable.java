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
package fr.imag.adele.cadse.core.impl.internal;

import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransaction;
import fr.imag.adele.cadse.core.transaction.delta.SetAttributeOperation;
import fr.imag.adele.cadse.core.var.ContextVariable;
import fr.imag.adele.cadse.core.var.ContextVariableImpl;

public class OldContextVariable extends ContextVariableImpl {
	LogicalWorkspaceTransaction	_copy;

	@Override
	public String getName(Item item) {
		return getAttribute(item, CadseGCST.ITEM_at_NAME_);
	}

	@Override
	public String getQualifiedName(Item item) {
		return getAttribute(item, CadseGCST.ITEM_at_QUALIFIED_NAME_);
	}

	@Override
	public String getValue(Item item, IAttributeType<String> key) {
			SetAttributeOperation attOper = _copy.getItem(item.getId()).getSetAttributeOperation(key, false);
		if (attOper != null) {
			return (String) attOper.getOldValue();
		}
		return item.getAttribute(key);
	}

	@Override
	public String getAttribute(Item item, IAttributeType<String> att) {
		SetAttributeOperation attOper = _copy.getItem(item.getId()).getSetAttributeOperation(att, false);
		if (attOper != null) {
			return (String) attOper.getOldValue();
		}
		return item.getAttribute(att);
	}
	
	@Override
	public void putValue(Item item, IAttributeType<String> attr, String value) {
		throw new UnsupportedOperationException("Context is readonly");
	}

	public OldContextVariable(LogicalWorkspaceTransaction copy) {
		this._copy = copy;
	}
}
