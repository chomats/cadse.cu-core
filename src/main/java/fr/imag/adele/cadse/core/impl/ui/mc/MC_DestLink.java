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

import java.util.ArrayList;
import java.util.List;

import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransaction;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.ui.UIField;

public class MC_DestLink extends MC_Link {

	public MC_DestLink(boolean mandatory, String msg) {
		super(mandatory, msg);
	}

	public MC_DestLink() {
	}

	@Override
	public Object getValue() {
		Object ret = super.getValue();
		if (ret == null) {
			return null;
		}
		LinkType lt = (LinkType) getAttributeDefinition();
		if (lt.getMax() == 1) {
			return ((Link) ret).getDestination();
		}
		ArrayList<Item> retItems = new ArrayList<Item>();
		List<Link> goodret = (List<Link>) ret;
		for (Link link : goodret) {
			retItems.add(link.getDestination());
		}
		return retItems;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.AbstractModelController#notifieSubValueRemoved(fr.imag.adele.cadse.core.ui.UIField,
	 *      java.lang.Object)
	 */
	@Override
	public void notifieSubValueRemoved(UIField field, Object removed) {
		try {
			Item item = _uiPlatform.getItem(field);
			LinkType lt = (LinkType) getAttributeDefinition();
			if (removed instanceof Object[]) {
				Object[] arrayremoved = (Object[]) removed;
				LogicalWorkspaceTransaction copy = item.getLogicalWorkspace().createTransaction();
				ItemDelta copyItem = copy.getItem(item.getId());
				for (int i = 0; i < arrayremoved.length; i++) {
					Item dest = (Item) arrayremoved[i];
					Link l = copyItem.getOutgoingLink(lt, dest.getId());
					if (l != null) {
						l.delete();
					}
				}
				copy.commit();
			} else {
				LogicalWorkspaceTransaction copy = item.getLogicalWorkspace().createTransaction();
				ItemDelta copyItem = copy.getItem(item.getId());
				Item dest = (Item) removed;
				Link l = copyItem.getOutgoingLink(lt, dest.getId());
				if (l != null) {
					l.delete();
				}

				copy.commit();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void notifieSubValueAdded(UIField field, Object added) {
		try {
			Item item = _uiPlatform.getItem(field);
			LinkType lt = (LinkType) getAttributeDefinition();
			if (added instanceof Object[]) {
				Object[] arrayadded = (Object[]) added;
				LogicalWorkspaceTransaction copy = item.getLogicalWorkspace().createTransaction();
				ItemDelta copyItem = copy.getItem(item.getId());
				for (int i = 0; i < arrayadded.length; i++) {
					Item dest = (Item) arrayadded[i];
					copyItem.createLink(lt, dest);
				}
				copy.commit();
			} else {
				LogicalWorkspaceTransaction copy = item.getLogicalWorkspace().createTransaction();
				ItemDelta copyItem = copy.getItem(item.getId());
				Item dest = (Item) added;
				copyItem.createLink(lt, dest);
				copy.commit();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
