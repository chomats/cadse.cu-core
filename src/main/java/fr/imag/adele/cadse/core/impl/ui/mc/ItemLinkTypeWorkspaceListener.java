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

import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.WorkspaceListener;
import fr.imag.adele.cadse.core.transaction.delta.ImmutableItemDelta;
import fr.imag.adele.cadse.core.transaction.delta.ImmutableWorkspaceDelta;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.ui.UIPlatform;

public class ItemLinkTypeWorkspaceListener extends WorkspaceListener {
	Item		item;
	UIField		uifield;
	LinkType	lt;
	UIPlatform uiPlatform;
	
	public ItemLinkTypeWorkspaceListener(UIPlatform uiPlatform, Item item, UIField uifield, LinkType lt) {
		super();
		this.item = item;
		this.uifield = uifield;
		this.lt = lt;
		this.uiPlatform = uiPlatform;
	}

	@Override
	public void workspaceChanged(ImmutableWorkspaceDelta delta) {
		ImmutableItemDelta _id = delta.getItem(item);
		if (_id != null) {
			if (_id.hasAddedOutgoingLink()) {
				for (Link l : _id.getLinksAdded()) {
					if (l.getLinkType().equals(lt)) {
						uiPlatform.broadcastThisFieldHasChanged(uifield);
						return;
					}
				}
			}
			if (_id.hasRemovedOutgoingLink()) {
				for (Link l : _id.getLinksRemoved()) {
					if (l.getLinkType().equals(lt)) {
						uiPlatform.broadcastThisFieldHasChanged(uifield);
						return;
					}
				}
			}
		}
	}

}
