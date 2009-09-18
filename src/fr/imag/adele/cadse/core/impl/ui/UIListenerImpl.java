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
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.impl.CollectedReflectLink;
import fr.imag.adele.cadse.core.impl.ReflectLink;
import fr.imag.adele.cadse.core.impl.code.ScriptItem;
import fr.imag.adele.cadse.core.impl.internal.AbstractGeneratedItem;
import fr.imag.adele.cadse.core.impl.internal.delta.ItemDeltaImpl;
import fr.imag.adele.cadse.core.impl.internal.ui.IPage2;
import fr.imag.adele.cadse.core.CadseRootCST;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.ui.UIListener;
import fr.imag.adele.cadse.core.util.ArraysUtil;

public class UIListenerImpl extends AbstractGeneratedItem implements UIListener {

	IPage2		page;
	ScriptItem	script;
	UIField[]	listen;

	public UIListenerImpl() {
		super();
	}

	public UIListenerImpl(CompactUUID id) {
		super(id);
	}

	public UIListenerImpl(ItemDeltaImpl item) {
		super(item);
	}

	public ItemType getType() {
		return CadseRootCST.UILISTENER;
	}

	public void setParent(Item parent, LinkType lt) {
		page = (IPage2) parent;
	}

	@Override
	public Item getPartParent(boolean attemptToRecreate) {
		return this.getPage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIListener#getPage()
	 */
	public IPage2 getPage() {
		return page;
	}

	@Override
	protected void collectOutgoingLinks(LinkType linkType, CollectedReflectLink ret) {
		// if (linkType == CadseRootCST.FIELD_TYPE_lt_ATTRIBUTE) {
		// ret.addOutgoing(CadseRootCST.FIELD_TYPE_lt_ATTRIBUTE, attributeRef);
		// }
		if (linkType == CadseRootCST.UILISTENER_lt_LISTEN) {
			ret.addOutgoing(CadseRootCST.UILISTENER_lt_LISTEN, listen);
		}
		if (linkType == CadseRootCST.UILISTENER_lt_SCRIPT) {
			ret.addOutgoing(CadseRootCST.UILISTENER_lt_SCRIPT, script);
		}
		super.collectOutgoingLinks(linkType, ret);
	}

	@Override
	public Link commitLoadCreateLink(LinkType lt, Item destination) throws CadseException {
		if (lt == CadseRootCST.UILISTENER_lt_LISTEN) {
			listen = ArraysUtil.add(UIField.class, listen, (UIField) destination);
			return new ReflectLink(lt, this, destination, 0);
		}
		if (lt == CadseRootCST.UILISTENER_lt_SCRIPT) {
			this.script = (ScriptItem) destination;
			return new ReflectLink(lt, this, destination, 0);
		}
		return super.commitLoadCreateLink(lt, destination);
	}

	@Override
	public void removeOutgoingLink(Link link, boolean notifie) {
		Item destination = link.getDestination();
		LinkType lt = link.getLinkType();
		if (lt == CadseRootCST.UILISTENER_lt_SCRIPT && destination.isResolved()) {
			script = null;
			return;
		}
		if (lt == CadseRootCST.UILISTENER_lt_LISTEN && destination.isResolved()) {
			listen = ArraysUtil.remove(UIField.class, listen, (UIField) destination);
			return;
		}
		super.removeOutgoingLink(link, notifie);
	}

	public void init(UIField field) {

	}

	public void notifieSubValueAdded(UIField field, Object added) {

	}

	public void notifieSubValueRemoved(UIField field, Object removed) {

	}

	public void notifieValueChanged(UIField field, Object value) {

	}

	public void notifieValueDeleted(UIField field, Object oldvalue) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIListener#initAndResgister()
	 */
	public void initAndResgister() {
		if (listen != null) {
			for (UIField f : listen) {
				f.addListener(this);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.UIListener#disposeAndUnregister()
	 */
	public void disposeAndUnregister() {
		if (listen != null) {
			for (UIField f : listen) {
				f.removeListener(this);
			}
		}
	}

}
