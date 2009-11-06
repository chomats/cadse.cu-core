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

import java.util.List;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.ContentChangeInfo;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.attribute.CheckStatus;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.attribute.IntegerAttributeType;
import fr.imag.adele.cadse.core.delta.ItemDelta;
import fr.imag.adele.cadse.core.delta.LinkDelta;
import fr.imag.adele.cadse.core.delta.MappingOperation;
import fr.imag.adele.cadse.core.delta.OrderOperation;
import fr.imag.adele.cadse.core.delta.SetAttributeOperation;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransaction;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransactionListener;
import fr.imag.adele.cadse.core.ui.IModelController;
import fr.imag.adele.cadse.core.ui.IPageController;
import fr.imag.adele.cadse.core.ui.UIField;

/**
 * The Class MC_AttributesItem.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 * @version 2.0
 */
public class MC_AttributesItem extends AbstractModelController implements IModelController {

//	class Listener extends WorkspaceListener {
//
//		private ImmutableItemDelta	itemDelta;
//		IPageController uiPlatform;
//		@Override
//		public void workspaceChanged(ImmutableWorkspaceDelta delta) {
//			itemDelta = delta.getItem(uiPlatform.getItem(getUIField()));
//			if (itemDelta == null) {
//				return;
//			}
//			if ((itemDelta.getSetAttributes() != null)
//					&& (itemDelta.getSetAttributes().get(getAttributeDefinition()) != null)) {
//				uiPlatform.resetVisualValue(getUIField());
//			}
//		}
//	}

	boolean			_anonymous	= true;
	private boolean	_inibNotification;

	public MC_AttributesItem(CompactUUID id) {
		super(id);
		_anonymous = false;
	}

	public MC_AttributesItem() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IModelController#getValue()
	 */
	public Object getValue(IPageController uiPlatform) {
		Item item = uiPlatform.getItem(getUIField());
		if (item == null) {
			return null;
		}
		if (item instanceof ItemDelta) {
			return ((ItemDelta) item).getAttribute(getAttributeDefinition(), false);
		}
		return item.getAttribute(getAttributeDefinition());
	}

	@Override
	public void init(IPageController uiPlatform) throws CadseException {
		uiPlatform.addLogicalWorkspaceTransactionListener(new MYWCWL(uiPlatform));
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.ui.IEventListener#notifieValueChanged(fr.imag
	 * .adele.cadse.core.ui.UIField, java.lang.Object)
	 */
	public void notifieValueChanged(IPageController uiPlatform, UIField field, Object value) {
		Item item = uiPlatform.getItem(getUIField());
		if (item.isReadOnly() || item.isStatic())
			return;
		// item.setAttribute(getUIField().getKey(),value);
		IAttributeType<?> attrType = getUIField().getAttributeDefinition();
		if (attrType != null) {
			value = attrType.convertTo(value);
			_inibNotification = true;
			try {
				item.setAttribute(attrType, value);
			} catch (CadseException e) {
				e.printStackTrace();
			} finally {
				_inibNotification = false;
			}
		}
	}

	@Override
	public boolean validValueChanged(IPageController uiPlatform, UIField field, Object visualValue) {
		IAttributeType<?> attRef = field.getAttributeDefinition();
		if (attRef != null) {
			Object value = convertToModelValue(visualValue);
			CheckStatus status = attRef.check(uiPlatform.getItem(getUIField()), value);
			if (status != null) {
				if (status.getType() == IPageController.ERROR) {
					uiPlatform.setMessage(attRef.getName() + ": " + status.getFormatedMessage(), IPageController.ERROR);
					return true;
				} else {
					uiPlatform.setMessage(attRef.getName() + ": " + status.getFormatedMessage(),
							status.getType());
				}
			}
		}
		// return super.validValueChanged(field, value);
		return false;
	}

	public Object convertToModelValue(Object visualValue) {
		return visualValue;
	}

	public ItemType getType() {
		return null;
	}

	@Override
	public boolean isAnonymous() {
		return _anonymous;
	}
	class MYWCWL implements LogicalWorkspaceTransactionListener {
		IPageController uiPlatform;
	public MYWCWL(IPageController uiPlatformParam) {
		uiPlatform = uiPlatformParam;
		}

	public void notifyAddMappingOperation(LogicalWorkspaceTransaction workspaceLogiqueWorkingCopy, ItemDelta item,
			MappingOperation mappingOperation) {
		// no thing

	}

	public void notifyCancelCreatedItem(LogicalWorkspaceTransaction wc, ItemDelta item) throws CadseException,
			CadseException {
		// no thing
	}

	public void notifyCancelCreatedLink(LogicalWorkspaceTransaction wc, LinkDelta link) throws CadseException,
			CadseException {
		// no thing

	}

	public void notifyChangeAttribute(LogicalWorkspaceTransaction wc, ItemDelta item, SetAttributeOperation attOperation)
			throws CadseException {
		if (_inibNotification) {
			return;
		}

		if (uiPlatform.getItem(getUIField()) != item) {
			return;
		}

		IAttributeType<?> attRef = getUIField().getAttributeDefinition();
		if (attRef != null) {
			if (attOperation.getAttributeDefinition() == attRef) {
				uiPlatform.setVisualValue(getAttributeDefinition(), attOperation.getCurrentValue(), false);
			}
		}
	}

	public void notifyChangeAttribute(LogicalWorkspaceTransaction wc, LinkDelta link, SetAttributeOperation attOperation)
			throws CadseException {
		// no thing
	}

	public void notifyChangeLinkOrder(LogicalWorkspaceTransaction wc, LinkDelta link, OrderOperation orderOperation)
			throws CadseException {
		// no thing
	}

	public void notifyCreatedItem(LogicalWorkspaceTransaction wc, ItemDelta item) throws CadseException, CadseException {
		// no thing
	}

	public void notifyCreatedLink(LogicalWorkspaceTransaction wc, LinkDelta link) throws CadseException, CadseException {
		// no thing
	}

	public void notifyDeletedItem(LogicalWorkspaceTransaction wc, ItemDelta item) throws CadseException, CadseException {
		// no thing
	}

	public void notifyDeletedLink(LogicalWorkspaceTransaction wc, LinkDelta link) throws CadseException, CadseException {
		// no thing
	}

	public void notifyDoubleClick(LogicalWorkspaceTransaction wc, ItemDelta item) {
		// no thing
	}

	public void notifyLoadedItem(LogicalWorkspaceTransaction workspaceLogiqueWorkingCopy, List<ItemDelta> loadedItems) {
		// no thing
	}

	public void validateCancelCreatedItem(LogicalWorkspaceTransaction wc, ItemDelta item) throws CadseException,
			CadseException {
		// no thing
	}

	public void validateCancelCreatedLink(LogicalWorkspaceTransaction wc, LinkDelta link) throws CadseException,
			CadseException {
		// no thing
	}

	public void validateChangeAttribute(LogicalWorkspaceTransaction wc, ItemDelta item,
			SetAttributeOperation attOperation) throws CadseException {
		// no thing
	}

	public void validateChangeAttribute(LogicalWorkspaceTransaction wc, LinkDelta link,
			SetAttributeOperation attOperation) throws CadseException {
		// no thing
	}

	public void validateChangeLinkOrder(LogicalWorkspaceTransaction wc, LinkDelta link, OrderOperation orderOperation)
			throws CadseException {
		// no thing
	}

	public void validateCreatedItem(LogicalWorkspaceTransaction wc, ItemDelta item) throws CadseException,
			CadseException {
		// no thing
	}

	public void validateCreatedLink(LogicalWorkspaceTransaction wc, LinkDelta link) throws CadseException,
			CadseException {
		// no thing
	}

	public void validateDeletedItem(LogicalWorkspaceTransaction wc, ItemDelta item) throws CadseException,
			CadseException {
		// no thing
	}

	public void validateDeletedLink(LogicalWorkspaceTransaction wc, LinkDelta link) throws CadseException,
			CadseException {
		// no thing
	}

	public void notifyChangedContent(LogicalWorkspaceTransaction workspaceLogiqueWorkingCopy, ItemDelta item,
			ContentChangeInfo[] change) {
		// TODO Auto-generated method stub

	}

	public void notifyAbortTransaction(LogicalWorkspaceTransaction wc) throws CadseException {
		// TODO Auto-generated method stub

	}

	public void notifyBeginTransaction(LogicalWorkspaceTransaction wc) throws CadseException {
		// TODO Auto-generated method stub

	}

	public void notifyCommitTransaction(LogicalWorkspaceTransaction wc) throws CadseException {
		// TODO Auto-generated method stub

	}

	public void notifyMigratePartLink(LogicalWorkspaceTransaction wc, ItemDelta childItem, ItemDelta newPartParent,
			LinkType lt, LinkDelta newPartLink, LinkDelta oldPartLink) {
		// TODO Auto-generated method stub

	}
	}
}
