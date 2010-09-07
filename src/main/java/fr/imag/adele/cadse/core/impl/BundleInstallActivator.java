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

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransaction;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;

public class BundleInstallActivator implements BundleActivator {

	//public static final String CADSE_ZIP = "cadse.zip";
	Set<UUID> itemsloaded = new HashSet<UUID>();
	
	@Override
	public void start(BundleContext context) throws Exception {
		Enumeration en = context.getBundle().findEntries("/", null, true);
		
		final ExportImportCadseFunction exportImportCadseFunction = new ExportImportCadseFunction();
		exportImportCadseFunction.importCadseItems(en);
		
		for (Item item : exportImportCadseFunction.getItemsHash()) {
			itemsloaded.add(item.getId());
		};
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		//delete items;
		LogicalWorkspaceTransaction t = CadseCore.getLogicalWorkspace().createTransaction();
		for (UUID key : itemsloaded) {
			ItemDelta item = t.getItem(key);
			if (item == null || item.isDeleted()) continue;
			item.unload();
		}
		t.commit();
	}

}
