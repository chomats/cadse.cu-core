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

import java.util.List;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.transaction.delta.LinkDelta;
import fr.imag.adele.cadse.core.transaction.delta.OrderOperation;
import fr.imag.adele.cadse.core.transaction.delta.SetAttributeOperation;
import fr.imag.adele.cadse.core.transaction.delta.WLWCOperation;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.enumdef.TWDestEvol;
import fr.imag.adele.cadse.core.enumdef.TWEvol;
import fr.imag.adele.cadse.core.transaction.AbstractLogicalWorkspaceTransactionListener;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransaction;

/**
 * State propagation algorithm of TeamWork. Propagation are : TODO
 * 
 * @author Thomas
 * 
 */
public class TeamWorkStatePropagationWLWCListener extends AbstractLogicalWorkspaceTransactionListener {

	@Override
	public void notifyCancelCreatedItem(LogicalWorkspaceTransaction wc, ItemDelta item) throws CadseException,
			CadseException {
		// do nothing
	}

	@Override
	public void notifyCancelCreatedLink(LogicalWorkspaceTransaction wc, LinkDelta link) throws CadseException,
			CadseException {
//		LinkType linkType = link.getLinkType();
//		ItemDelta modifiedItem = link.getSource();
//
//		// manage old cadse
//		if (linkType == null) {
//			return;
//		}
//
//		boolean modified = false;
//		for (LinkDelta linkOp : modifiedItem.getOutgoingLinkOperations(linkType)) {
//			modified |= (linkOp.isAdded() || linkOp.isDeleted());
//		}
//		// TODO change to use getOrderOperations(linkType)
//		for (OrderOperation orderOp : modifiedItem.getOrdersOperation()) {
//			modified |= (linkType.equals(orderOp.getFrom().getLinkType()));
//		}
//		setAttrModified(modifiedItem, linkType, modified);
//
//		// TODO propagate state
	}

	@Override
	public void notifyChangeAttribute(LogicalWorkspaceTransaction wc, ItemDelta modifiedItem,
			SetAttributeOperation attOperation) throws CadseException {
		if (modifiedItem.isDeleted()) { 
			return;
		}
		IAttributeType<?> attrDef = attOperation.getAttributeDefinition();
		if (CadseGCST.ITEM_at_REV_MODIFIED_ == attrDef) {
			return;
		}
		if (attrDef != null && !attrDef.isResolved()) {
			return;
		}
		computeAndSetAttrModified(attOperation, modifiedItem, attrDef);

		// manage case of item state: X -> RequireNewRev
		if ((CadseGCST.ITEM_at_REQUIRE_NEW_REV_ == attrDef) && TWUtil.isRequireNewRev(modifiedItem)) {
			boolean isOldRequireNewRev = false;
			Item oldItem = modifiedItem.getBaseItem();
			if (oldItem != null) {
				isOldRequireNewRev = TWUtil.isRequireNewRev(oldItem);
			}
			if (isOldRequireNewRev) {
				return; // it is not case of X -> RequireNewRev but RequireNewRev -> RequireNewRev
				// should not happen
			}

			// State propagation :
			// compute and set if necessary state of items
			// which have links pointing to this item
			for (Link incomingLink : modifiedItem.getIncomingLinks()) {
				LinkType incomingLinkType = incomingLink.getLinkType();
				TWDestEvol ltDestEvol = TWUtil.getDestEvol(incomingLinkType);
				Item sourceItem = incomingLink.getSource();

				if (TWDestEvol.finalDest.equals(ltDestEvol)) {
					; // TODO should notify of a warning
				} else if (TWDestEvol.immutable.equals(ltDestEvol)) {
					TWUtil.setRequireNewRev(sourceItem);
				} else if (TWDestEvol.mutable.equals(ltDestEvol)) {
					TWUtil.setRevModified(sourceItem);
				} else if (TWDestEvol.effective.equals(ltDestEvol)) {
					incomingLink.clearCompatibleVersions();
					TWUtil.setRevModified(sourceItem);
				}
				// no state propagation if link is BranchDestination 
			}

			return;
		}
		
		// manage case of item state: X -> RequireNewRev
		if ((CadseGCST.CONTENT_ITEM_at_SCM_MODIFIED_.equals(attrDef)) && !TWUtil.isRequireNewRev(modifiedItem)) {
			TWEvol contentEvol = TWUtil.getContentEvol(modifiedItem);
			boolean isRevSpecific = TWUtil.isContentRevSpecific(modifiedItem);

			if (!TWUtil.isRequireNewRev(modifiedItem)) {
				if (contentEvol.equals(TWEvol.twMutable)) {
					TWUtil.setRevModified(modifiedItem);
				}

				if (contentEvol.equals(TWEvol.twImmutable) && isRevSpecific) {
					TWUtil.setRequireNewRev(modifiedItem);
				}

				if (contentEvol.equals(TWEvol.twImmutable) && (!isRevSpecific)) {
					// TODO change item id, set version to null, set item state
					// to newRev

					TWUtil.setRequireNewRev(modifiedItem);
					modifiedItem.setVersion(0);
				}
			}

			return;
		}
	}

	/**
	 * Compute and set if necessary the modified flag of an attribute (which can
	 * be a link type).
	 * 
	 * @param attOperation
	 *            the performed set attribute operation
	 * @param modifiedItem
	 *            item which has been modified
	 * @param attrDef
	 *            the attribute definition
	 * @throws CadseException
	 */
	private void computeAndSetAttrModified(WLWCOperation attOperation, ItemDelta modifiedItem,
			IAttributeType<?> attrDef) throws CadseException {
		// manage old cadse
		if (attrDef == null || !attrDef.isResolved()) {
			return;
		}
		
		// ignore TeamWork attributes
		if (TWUtil.isTWAttribute(attrDef))
			return;
		
		// ignore internal attributes of CADSE meta model
		if (TWUtil.isInternalCadseAttribute(attrDef))
			return;
		
		// ignore if it is already considered as modified
		if (modifiedItem.isTWAttributeModified(attrDef)) {
			return;
		}

		boolean isModified = computeAttrModified(attOperation, attrDef);

		if (isModified) {
			TWUtil.setAttrModified(modifiedItem, attrDef, true);
		}
	}

	/**
	 * Return true if an attribute (which can be a link type) should be
	 * considered as modified.
	 * 
	 * @param attOperation
	 *            the performed operation on an attribute
	 * @param attrDef
	 *            attribute definition
	 * @return true if an attribute (which can be a link type) should be
	 *         considered as modified.
	 */
	private boolean computeAttrModified(WLWCOperation attOperation, IAttributeType<?> attrDef) {
		// ignore transient
		TWEvol attrEvol = TWUtil.getEvol(attrDef);
		if (attrEvol.equals(TWEvol.twTransient)) {
			return false;
		}

		// compute if the attribute must be considered as modified
		if (attOperation instanceof SetAttributeOperation) {
			SetAttributeOperation setAttrOp = (SetAttributeOperation) attOperation;
			return attrDef.isTWValueModified(setAttrOp.getOldValue(), setAttrOp.getCurrentValue());
		}

		if (attOperation instanceof LinkDelta) {
			LinkDelta linkAttrOp = (LinkDelta) attOperation;
			if (linkAttrOp.isDerived()) {
				return false; // ignore derived links
			}

			return linkAttrOp.isAdded() || linkAttrOp.isDeleted();
		}

		if (attOperation instanceof OrderOperation) {
			return true;
		}

		return false;
	}

	@Override
	public void notifyChangeAttribute(LogicalWorkspaceTransaction wc, LinkDelta link,
			SetAttributeOperation attOperation) throws CadseException {
		// do nothing
	}

	@Override
	public void notifyChangeLinkOrder(LogicalWorkspaceTransaction wc, LinkDelta link, OrderOperation orderOperation)
			throws CadseException {
		LinkType linkType = link.getLinkType();
		ItemDelta modifiedItem = link.getSource();

		computeAndSetAttrModified(link, modifiedItem, linkType);
	}

	@Override
	public void notifyCreatedItem(LogicalWorkspaceTransaction wc, ItemDelta item) throws CadseException,
			CadseException {
		/*
		 * A created item is RequireNewRev and RevModified.
		 */
		TWUtil.setRequireNewRev(item);
		TWUtil.setRevModified(item);
		TWUtil.setContentModifiedFlag(item, true);
	}

	@Override
	public void notifyCreatedLink(LogicalWorkspaceTransaction wc, LinkDelta link) throws CadseException,
			CadseException {
		// manage case of attribute modified attribute value
		LinkType linkType = link.getLinkType();
		ItemDelta modifiedItem = link.getSource();
		
		if (linkType.equals(CadseGCST.ITEM_lt_MODIFIED_ATTRIBUTES)) {
			// get attribute (link type) which has been modified
			ItemDelta attrDef = link.getDestination();
			TWEvol attrEvol = attrDef.getAttribute(CadseGCST.ATTRIBUTE_at_TWEVOL_);

			if (!TWUtil.isRequireNewRev(modifiedItem)) {
				if (attrEvol.equals(TWEvol.twMutable)) {
					TWUtil.setRevModified(modifiedItem);
				}

				if (attrEvol.equals(TWEvol.twImmutable)
						&& ((Boolean) attrDef.getAttribute(CadseGCST.ATTRIBUTE_at_TWREV_SPECIFIC_))) {
					TWUtil.setRequireNewRev(modifiedItem);
				}

				if (attrEvol.equals(TWEvol.twImmutable)
						&& (!(Boolean) attrDef.getAttribute(CadseGCST.ATTRIBUTE_at_TWREV_SPECIFIC_))) {
					// TODO change item id, set version to null, set item state
					// to newRev

					TWUtil.setRequireNewRev(modifiedItem);
					modifiedItem.setVersion(0);
				}
			}

			return;
		}

		// a link has been added
		computeAndSetAttrModified(link, modifiedItem, linkType);
	}

	@Override
	public void notifyDeletedItem(LogicalWorkspaceTransaction wc, ItemDelta item) throws CadseException,
			CadseException {
		// do nothing
	}

	@Override
	public void notifyDeletedLink(LogicalWorkspaceTransaction wc, LinkDelta link) throws CadseException,
			CadseException {
		// manage case of attribute modified attribute value
		LinkType linkType = link.getLinkType();
		ItemDelta modifiedItem = link.getSource();
		if (linkType.equals(CadseGCST.ITEM_lt_MODIFIED_ATTRIBUTES)) {
			return;
		}

		// ignore deleting items
		ItemDelta sourceItem = link.getSource();
		if ((sourceItem == null) || (sourceItem.isDeleted())) {
			return;
		}

		// a link has been deleted
		computeAndSetAttrModified(link, modifiedItem, linkType);
	}

	@Override
	public void notifyLoadedItem(LogicalWorkspaceTransaction workspaceLogiqueWorkingCopy,
			List<ItemDelta> loadedItems) {
		// TODO implement it
	}

	/*
	 * TeamWork never forbid an action on the workspace but only control the
	 * evolution actions (commit, revert, update, import).
	 */
	
	@Override
	public void notifyCommitTransaction(LogicalWorkspaceTransaction wc)
			throws CadseException {
		for (ItemDelta itemDelta : wc.getItemOperations()) {
			if (!itemDelta.isModified() || itemDelta.isDeleted() || itemDelta.isRuntime() || !itemDelta.isResolved())
				continue;
			
			TWUtil.refreshContentStatus(itemDelta);
		}
	}
}
