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
package fr.imag.adele.cadse.core.impl.internal;

import java.util.List;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.delta.ItemDelta;
import fr.imag.adele.cadse.core.delta.LinkDelta;
import fr.imag.adele.cadse.core.delta.OrderOperation;
import fr.imag.adele.cadse.core.delta.SetAttributeOperation;
import fr.imag.adele.cadse.core.delta.WLWCOperation;
import fr.imag.adele.cadse.core.CadseRootCST;
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
		LinkType linkType = link.getLinkType();
		ItemDelta modifiedItem = link.getSource();

		// manage old cadse
		if (linkType == null) {
			return;
		}

		boolean modified = false;
		for (LinkDelta linkOp : modifiedItem.getOutgoingLinkOperations(linkType)) {
			modified |= (linkOp.isAdded() || linkOp.isDeleted());
		}
		// TODO change to use getOrderOperations(linkType)
		for (OrderOperation orderOp : modifiedItem.getOrdersOperation()) {
			modified |= (linkType.equals(orderOp.getFrom().getLinkType()));
		}
		setAttrModified(modifiedItem, linkType, modified);

		// TODO propagate state
	}

	@Override
	public void notifyChangeAttribute(LogicalWorkspaceTransaction wc, ItemDelta modifiedItem,
			SetAttributeOperation attOperation) throws CadseException {
		if (modifiedItem.isDeleted()) { // TODO if item is deleted ???
			return;
		}
		String attrName = attOperation.getAttributeName();
		IAttributeType<?> attrDef = attOperation.getAttributeDefinition();
		// TODO use raw type
		if (CadseRootCST.ITEM_TYPE_at_REQUIRE_NEW_REV_ == attrDef) {
			return;
		}
		if (attrDef != null && !attrDef.isResolved()) {
			return;
		}
		computeAndSetAttrModified(attOperation, modifiedItem, attrDef);

		// manage case of item state: X -> RequireNewRev
		if (isRequireNewRev(modifiedItem)) {
			boolean isOldRequireNewRev = false;
			Item oldItem = modifiedItem.getBaseItem();
			if (oldItem != null) {
				isOldRequireNewRev = isRequireNewRev(oldItem);
			}
			if (!isOldRequireNewRev) {
				return; // it is not case of X -> RequireNewRev
			}

			// compute and set if necessary state of items
			// which have links pointing to this item
			for (Link incomingLink : modifiedItem.getIncomingLinks()) {
				LinkType incomingLinkType = incomingLink.getLinkType();
				TWDestEvol ltDestEvol = getDestEvol(incomingLinkType);
				Item sourceItem = incomingLink.getSource();

				if (TWDestEvol.finalDest.equals(ltDestEvol)) {
					; // TODO should notify of a warning
				} else if (TWDestEvol.immutable.equals(ltDestEvol)) {
					setRequireNewRev(sourceItem);
				} else if (TWDestEvol.mutable.equals(ltDestEvol) || TWDestEvol.effective.equals(ltDestEvol)) {
					setRevModified(sourceItem);
				}
			}

			return;
		}
	}

	private void setRevModified(Item sourceItem) throws CadseException {
		sourceItem.setAttribute(CadseRootCST.ITEM_TYPE_at_REV_MODIFIED_, true);
	}

	private void setRequireNewRev(Item sourceItem) throws CadseException {
		sourceItem.setAttribute(CadseRootCST.ITEM_TYPE_at_REQUIRE_NEW_REV_, true);
	}

	private TWDestEvol getDestEvol(IAttributeType attrDef) {
		return attrDef.getAttribute(CadseRootCST.LINK_DEFINITION_ATTIBUTE_TYPE_at_TWDEST_EVOL_);
	}

	private TWEvol getEvol(IAttributeType attrDef) {
		return attrDef.getAttribute(CadseRootCST.ATTRIBUTE_TYPE_at_TWEVOL_);
	}

	private boolean isRequireNewRev(Item modifiedItem) {
		// TODO change to default value....
		Object ret = modifiedItem.getAttribute(CadseRootCST.ITEM_TYPE_at_REQUIRE_NEW_REV_);
		if (ret == null) {
			return true;
		}
		return (Boolean) ret;
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
			IAttributeType attrDef) throws CadseException {
		// manage old cadse
		if (attrDef == null) {
			return;
		}

		// ignore if it is already considered as modified
		if (modifiedItem.isTWAttributeModified(attrDef)) {
			return;
		}

		boolean isModified = computeAttrModified(attOperation, attrDef);

		if (isModified) {
			setAttrModified(modifiedItem, attrDef, true);
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
		TWEvol attrEvol = getEvol(attrDef);
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

	/**
	 * Set attribute modified state.
	 * 
	 * @param item
	 *            item on which we want to change state
	 * @param attrDef
	 *            attribute definition
	 * @param modifiedVal
	 *            new modified flag value
	 * @throws CadseException
	 */
	private void setAttrModified(ItemDelta item, IAttributeType<?> attrDef, boolean modifiedVal)
			throws CadseException {
		item.setTWAttributeModified(attrDef, modifiedVal);
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
		setRequireNewRev(item);
		setRevModified(item);
	}

	@Override
	public void notifyCreatedLink(LogicalWorkspaceTransaction wc, LinkDelta link) throws CadseException,
			CadseException {
		// manage case of attribute modified attribute value
		LinkType linkType = link.getLinkType();
		ItemDelta modifiedItem = link.getSource();
		if (linkType.equals(CadseRootCST.ITEM_TYPE_lt_MODIFIED_ATTRIBUTES)) {
			// get attribute (link type) which has been modified
			ItemDelta attrDef = link.getDestination();
			TWEvol attrEvol = attrDef.getAttribute(CadseRootCST.ATTRIBUTE_TYPE_at_TWEVOL_);

			if (!isRequireNewRev(modifiedItem)) {
				if (attrEvol.equals(TWEvol.twMutable)) {
					setRevModified(modifiedItem);
				}

				if (attrEvol.equals(TWEvol.twImmutable)
						&& ((Boolean) attrDef.getAttribute(CadseRootCST.ATTRIBUTE_TYPE_at_TWREV_SPECIFIC_))) {
					setRequireNewRev(modifiedItem);
				}

				if (attrEvol.equals(TWEvol.twImmutable)
						&& (!(Boolean) attrDef.getAttribute(CadseRootCST.ATTRIBUTE_TYPE_at_TWREV_SPECIFIC_))) {
					// TODO change item id, set version to null, set item state
					// to newRev

					setRequireNewRev(modifiedItem);
				}
			}

			return;
		}

		// a link has been added
		computeAndSetAttrModified(link, modifiedItem, linkType);

		// manage state propagation
		TWDestEvol ltDestEvol = getDestEvol(linkType);
		Item destItem = link.getDestination();
		if (destItem == null) {
			return;
		}

		if (!isRequireNewRev(destItem)) {
			return;
		}

		if (TWDestEvol.finalDest.equals(ltDestEvol)) {
			; // TODO should notify of a warning
		} else if (TWDestEvol.immutable.equals(ltDestEvol)) {
			setRequireNewRev(modifiedItem);
		} else if (TWDestEvol.mutable.equals(ltDestEvol) || TWDestEvol.effective.equals(ltDestEvol)) {
			setRevModified(modifiedItem);
		}
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
		if (linkType.equals(CadseRootCST.ITEM_TYPE_lt_MODIFIED_ATTRIBUTES)) {
			// manage case Modified -> Unmodified
			// TODO implement it

			return;
		}

		// ignore deleting items
		ItemDelta sourceItem = link.getSource();
		if ((sourceItem == null) || (sourceItem.isDeleted())) {
			return;
		}

		// a link has been added
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
}
