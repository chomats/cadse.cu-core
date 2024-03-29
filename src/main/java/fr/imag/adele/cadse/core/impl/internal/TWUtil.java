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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import fr.imag.adele.cadse.as.scm.SCMException;
import fr.imag.adele.cadse.as.scm.SCMService;
import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.content.ContentItem;
import fr.imag.adele.cadse.core.enumdef.TWCommitKind;
import fr.imag.adele.cadse.core.enumdef.TWDestEvol;
import fr.imag.adele.cadse.core.enumdef.TWEvol;
import fr.imag.adele.cadse.core.enumdef.TWUpdateKind;
import fr.imag.adele.cadse.core.impl.CadseCore;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransaction;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;

/**
 * Couple of methods useful for TeamWork implementation.
 * 
 * @author Thomas
 *
 */
public class TWUtil {

	/**
	 * Sets RevModified flag to true.
	 * Specified item will be considered as modified by TeamWork. 
	 * 
	 * @param item an item
	 * @throws CadseException
	 */
	public static void setRevModified(Item item) throws CadseException {
		item.setAttribute(CadseGCST.ITEM_at_REV_MODIFIED_, true);
	}

	/**
	 * Sets requireNewRev flag to true.
	 * Specified item will be considered as modified by TeamWork 
	 * and next commit will create a new revision of this item. 
	 * 
	 * @param item an item
	 * @throws CadseException
	 */
	public static void setRequireNewRev(Item item) throws CadseException {
		// NewRev implies Modified flag set to true
		setRevModified(item);
		item.setAttribute(CadseGCST.ITEM_at_REQUIRE_NEW_REV_, true);
	}

	/**
	 * Returns impact flag of a change on a destination.
	 * 
	 * @param attrDef an attribute definition
	 * @return impact flag of a change on a destination.
	 */
	public static TWDestEvol getDestEvol(IAttributeType<?> attrDef) {
		return attrDef.getAttribute(CadseGCST.LINK_TYPE_at_TWDEST_EVOL_);
	}

	/**
	 * Returns impact flag of a change on an item of type defining the specified attribute.
	 * 
	 * @param attrDef an attribute definition
	 * @return impact flag of a change on an item of type defining the specified attribute.
	 */
	public static TWEvol getEvol(IAttributeType<?> attrDef) {
		return attrDef.getAttribute(CadseGCST.ATTRIBUTE_at_TWEVOL_);
	}
	
	/**
	 * Returns commit politic flag of the specified attribute.
	 * 
	 * @param attrDef an attribute definition
	 * @return commit politic flag of the specified attribute.
	 */
	public static TWCommitKind getCommitPolitic(IAttributeType<?> attrDef) {
		return attrDef.getAttribute(CadseGCST.ATTRIBUTE_at_TWCOMMIT_KIND_);
	}
	
	/**
	 * Returns update politic flag of the specified attribute.
	 * 
	 * @param attrDef an attribute definition
	 * @return update politic flag of the specified attribute.
	 */
	public static TWUpdateKind getUpdatePolitic(IAttributeType<?> attrDef) {
		return attrDef.getAttribute(CadseGCST.ATTRIBUTE_at_TWUPDATE_KIND_);
	}

	/**
	 * Returns true if next commit of this item will create a new revision of it.
	 * 
	 * @param modifiedItem an item
	 * @return true if next commit of this item will create a new revision of it.
	 */
	public static boolean isRequireNewRev(Item modifiedItem) {
		// TODO change to default value....
		Object ret = modifiedItem.getAttribute(CadseGCST.ITEM_at_REQUIRE_NEW_REV_);
		if (ret == null) {
			return true;
		}
		return (Boolean) ret;
	}
	
	/**
	 * Returns true if next commit of this item will modify current revision of it.
	 * 
	 * @param modifiedItem an item
	 * @return true if next commit of this item will modify current revision of it.
	 */
	public static boolean isInChangedState(Item modifiedItem) {
		
		return !isRequireNewRev(modifiedItem) && isModified(modifiedItem);
	}
	
	/**
	 * Returns true if this item is considered as modified by TeamWork.
	 * 
	 * @param modifiedItem an item
	 * @return true if this item is considered as modified by TeamWork.
	 */
	public static boolean isModified(Item modifiedItem) {
		//TODO change to default value....
		Object ret = modifiedItem.getAttribute(CadseGCST.ITEM_at_REV_MODIFIED_);
		if (ret == null)
			return true;
		return (Boolean) ret;
	}
	
	/**
	 * Set TW attribute modified state.
	 * 
	 * @param item
	 *            item on which we want to change state
	 * @param attrDef
	 *            attribute definition
	 * @param modifiedVal
	 *            new modified flag value
	 * @throws CadseException
	 */
	public static void setAttrModified(ItemDelta item, IAttributeType<?> attrDef, boolean modifiedVal)
			throws CadseException {
		item.setTWAttributeModified(attrDef, modifiedVal);
	}
	
	/**
	 * Defines specified item and all its attributes as not modified.
	 * 
	 * @param modifiedItem
	 */
	public static void resetTWState(Item modifiedItem) {
		try {
			modifiedItem.setAttribute(CadseGCST.ITEM_at_REV_MODIFIED_, false);
			modifiedItem.setAttribute(CadseGCST.ITEM_at_REQUIRE_NEW_REV_, false);
			
			List<Link> modifiedLinks = modifiedItem.getOutgoingLinks(CadseGCST.ITEM_lt_MODIFIED_ATTRIBUTES);
			if (modifiedLinks == null)
				return;
			for (Link modifiedLink : modifiedLinks) {
				try {
					modifiedLink.delete();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			// set content as no modified
			setContentModifiedFlag(modifiedItem, false);
			
		} catch (CadseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns true if specified attribute is an attribute used for TeamWork component.
	 * 
	 * @return true if specified attribute is an attribute used for TeamWork component.
	 */
	public static boolean isTWAttribute(IAttributeType<?> attrDef) {
		
		if ((attrDef == CadseGCST.ITEM_lt_MODIFIED_ATTRIBUTES) ||
				(attrDef == CadseGCST.ITEM_at_COMMITTED_BY_) ||
				(attrDef == CadseGCST.ITEM_at_COMMITTED_DATE_) ||
				(attrDef == CadseGCST.ITEM_at_REQUIRE_NEW_REV_) ||
				(attrDef == CadseGCST.ITEM_at_REV_MODIFIED_) ||
				(attrDef == CadseGCST.ITEM_at_TW_VERSION_) ||
				(attrDef == CadseGCST.ITEM_at_TWLAST_COMMENT_))
			return true;
		
		return false;
	}
	
	/**
	 * Returns true if specified item cannot be imported.
	 * Import an item which is already present in the workspace is forbidden.
	 * 
	 * @param item an item
	 * @return true if specified item cannot be imported.
	 */
	public static boolean cannotImport(UUID itemId, LogicalWorkspace lw) {
		return (itemId != null) && isPresentInWorkspace(itemId, lw);
	}

	/**
	 * Returns true if specified item is present in specified workspace.
	 * 
	 * @param itemId id of an item 
	 * @param lw     a logical workspace
	 * @return true if specified item is present in specified workspace.
	 */
	public static boolean isPresentInWorkspace(UUID itemId, LogicalWorkspace lw) {
		return (lw.getItem(itemId) != null);
	}

	/**
	 * Returns true if specified item is ignored for an update operation.
	 * 
	 * @param item an item
	 * @return true if specified item is ignored for an update operation.
	 */
	public static boolean cannotUpdate(Item item) {
		return (item == null) || item.isRuntime() || hasNeverBeenCommited(item) || representsContent(item);
	}

	/**
	 * Returns true if specified item has never been commited.
	 * 
	 * @param item an item	
	 * @return true if specified item has never been commited.
	 */
	public static boolean hasNeverBeenCommited(Item item) {
		return (item.getVersion() == 0);
	}
	
	/**
	 * Returns true if specified item is ignored for a commit operation.
	 * 
	 * @param item an item
	 * @return true if specified item is ignored for a commit operation.
	 */
	public static boolean cannotCommit(Item item) {
		return (item == null) || item.isRuntime() || !item.isRevModified() || representsContent(item);
	}

	/**
	 * Returns true if specified represents content.
	 * 
	 * @param item an item
	 * @return true if specified represents content.
	 */
	private static boolean representsContent(Item item) {
		return CadseGCST.CONTENT_ITEM.equals(item.getType());
	}
	
	/**
	 * Returns true if specified item is ignored for a revert operation.
	 * 
	 * @param item an item
	 * @return true if specified item is ignored for a revert operation.
	 */
	public static boolean cannotRevert(Item item) {
		return (item == null) || item.isRuntime() || !item.isRevModified() || hasNeverBeenCommited(item);
	}

	/**
	 * Returns true if specified attribute is an attribute used for CADSE internal purposes.
	 * 
	 * @return true if specified attribute is an attribute used for CADSE internal purposes.
	 */
	public static boolean isInternalCadseAttribute(IAttributeType<?> attrDef) {
		
		if ((attrDef == CadseGCST.ITEM_lt_INSTANCE_OF) ||
				(attrDef == CadseGCST.ITEM_lt_CONTENTS) ||
				(attrDef == CadseGCST.ITEM_lt_PARENT) ||
				(attrDef == CadseGCST.ITEM_at_ID_))
			return true;
		
		return false;
	}

	/**
	 * Returns true if specified link type has BranchDestination evolution politic.
	 * 
	 * @param linkType a link type
	 * @return true if specified link type has BranchDestination evolution politic.
	 */
	public static boolean isBranchDestination(IAttributeType<?> linkType) {
		return TWDestEvol.branch.equals(getDestEvol(linkType));
	}
	
	/**
	 * Returns true if specified link type has ImmutableDestination evolution politic.
	 * 
	 * @param linkType a link type
	 * @return true if specified link type has ImmutableDestination evolution politic.
	 */
	public static boolean isImmutableDestination(IAttributeType<?> linkType) {
		return TWDestEvol.immutable.equals(getDestEvol(linkType));
	}
	
	/**
	 * Returns true if specified link type has FinalDestination evolution politic.
	 * 
	 * @param linkType a link type
	 * @return true if specified link type has FinalDestination evolution politic.
	 */
	public static boolean isFinalDestination(IAttributeType<?> linkType) {
		return TWDestEvol.finalDest.equals(getDestEvol(linkType));
	}
	
	/**
	 * Returns true if specified attribute type has Final evolution politic.
	 * 
	 * @param linkType an attribute definition
	 * @return true if specified attribute type has Final evolution politic.
	 */
	public static boolean isFinal(IAttributeType<?> attrType) {
		return TWEvol.twFinal.equals(getEvol(attrType));
	}
	
	/**
	 * Returns true if specified link type has MutableDestination evolution politic.
	 * 
	 * @param linkType a link type
	 * @return true if specified link type has MutableDestination evolution politic.
	 */
	public static boolean isMutableDestination(IAttributeType<?> linkType) {
		return TWDestEvol.mutable.equals(getDestEvol(linkType));
	}
	
	/**
	 * Returns true if specified link type has EffectiveDestination evolution politic.
	 * 
	 * @param linkType a link type
	 * @return true if specified link type has EffectiveDestination evolution politic.
	 */
	public static boolean isEffectiveDestination(IAttributeType<?> linkType) {
		return TWDestEvol.effective.equals(getDestEvol(linkType));
	}
	
	/**
	 * Returns true if specified link type is defined coupled.
	 * 
	 * @param linkType a link type
	 * @return true if specified link type is defined coupled.
	 */
	public static boolean isCoupled(LinkType linkType) {
		return linkType.getAttribute(CadseGCST.LINK_TYPE_at_TWCOUPLED_);
	}
	
	/**
	 * Returns true if specified attribute type has replace commit politic.
	 * 
	 * @param attrType an attribute type
	 * @return true if specified attribute type has replace commit politic.
	 */
	public static boolean hasReplaceCommitPolitic(IAttributeType<?> attrType) {
		return TWCommitKind.none.equals(getCommitPolitic(attrType));
	}
	
	/**
	 * Returns true if specified attribute type has conflict commit politic.
	 * 
	 * @param attrType an attribute type
	 * @return true if specified attribute type has conflict commit politic.
	 */
	public static boolean hasConflictCommitPolitic(IAttributeType<?> attrType) {
		return TWCommitKind.conflict.equals(getCommitPolitic(attrType));
	}
	
	/**
	 * Returns true if specified attribute type has reconcile commit politic.
	 * 
	 * @param attrType an attribute type
	 * @return true if specified attribute type has reconcile commit politic.
	 */
	public static boolean hasReconcileCommitPolitic(IAttributeType<?> attrType) {
		return TWCommitKind.reconcile.equals(getCommitPolitic(attrType));
	}
	
	/**
	 * Returns true if specified attribute type has replace update politic.
	 * 
	 * @param attrType an attribute type
	 * @return true if specified attribute type has replace update politic.
	 */
	public static boolean hasReplaceUpdatePolitic(IAttributeType<?> attrType) {
		return TWUpdateKind.none.equals(getUpdatePolitic(attrType));
	}
	
	/**
	 * Returns true if specified attribute type has merge update politic.
	 * 
	 * @param attrType an attribute type
	 * @return true if specified attribute type has merge update politic.
	 */
	public static boolean hasMergeUpdatePolitic(IAttributeType<?> attrType) {
		return TWUpdateKind.merge.equals(getUpdatePolitic(attrType));
	}
	
	/**
	 * Returns true if specified attribute type has compute update politic.
	 * 
	 * @param attrType an attribute type
	 * @return true if specified attribute type has compute update politic.
	 */
	public static boolean hasComputeUpdatePolitic(IAttributeType<?> attrType) {
		return TWUpdateKind.compute.equals(getUpdatePolitic(attrType));
	}

	/**
	 * Returns true if specified attribute type has transient evolution politic.
	 * 
	 * @param attrType an attribute type
	 * @return true if specified attribute type has transient evolution politic.
	 */
	public static boolean isTransient(IAttributeType<?> attrType) {
		return TWEvol.twTransient.equals(getEvol(attrType));
	}

	/**
	 * Returns true if specified attribute must not be saved by TeamWork.
	 * 
	 * @param attrType an attribute type
	 * @return true if specified attribute must not be saved by TeamWork.
	 */
	public static boolean isToIgnoreForCommit(IAttributeType attrType) {
		if (isTWAttribute(attrType))
			return true;
		if (isInternalCadseAttribute(attrType))
			return true;
		
		return false;
	}
	
	/**
	 * Returns true if specified attribute not saved by TeamWork or needs a specific treatment.
	 * 
	 * @param attrType an attribute type
	 * @return true if specified attribute not saved by TeamWork or needs a specific treatment.
	 */
	public static boolean isToIgnoreForUpdate(IAttributeType attrType) {
		if (isTWAttribute(attrType))
			return true;
		if (isInternalCadseAttribute(attrType))
			return true;
		
		return false;
	}

	/**
	 * Returns a list which is 3 way merge.
	 * In case of conflicts, newValue2 is considered as master.
	 * 
	 * @param oldValue  old list of values (common ancestor of two new values)
	 * @param newValue  new list of values
	 * @param newValue2 new list of values
	 * @return a list which is a 3 way merge.
	 */
	public static List mergeLists(Object oldValue, Object newValue, Object newValue2) {
		if (!(oldValue instanceof List) || !(newValue instanceof List) || !(newValue2 instanceof List))
			throw new IllegalArgumentException("one of the parameters is not a list.");
		
		List oldList = (List) oldValue;
		List newList = (List) newValue;
		List newList2 = (List) newValue2;
		
		List resultList = new ArrayList();
		resultList.addAll(newList2);
		
		// compute removal in newList and newList2
		List removedInNewList = new ArrayList();
		List removedInNewList2 = new ArrayList();
		for (Object obj : oldList) {
			if (!newList.contains(obj))
				removedInNewList.add(obj);
			if (!newList2.contains(obj))
				removedInNewList2.add(obj);
		}
		
		// compute addition in newList
		List addedInNewList = new ArrayList();
		for (Object obj : newList) {
			if (!oldList.contains(obj))
				addedInNewList.add(obj);
		}
		
		// compute addition in newList2
		List addedInNewList2 = new ArrayList();
		for (Object obj : newList2) {
			if (!oldList.contains(obj))
				addedInNewList2.add(obj);
		}
		
		// compute merge
		for (Object obj : addedInNewList) {
			if (!addedInNewList2.contains(obj) && !removedInNewList2.contains(obj))
				resultList.add(obj);
		}
		for (Object obj : removedInNewList) {
			if (!addedInNewList2.contains(obj) && !removedInNewList2.contains(obj))
				resultList.remove(obj);
		}
		
		//TODO define a better 3 way merge algo for lists
		
		return resultList;
	}

	/**
	 * Returns local items which must be committed if specified item is committed.
	 * A local item is source of an incomming link of specified item or
	 * a destination of an outgoing link of specified item.
	 * 
	 * @param item an item 
	 * @return local items which must be committed if specified item is committed.
	 */
	public static Set<Item> computeLocalItemsToCommit(Item item) {
		Set<Item> itemsToCommit = new HashSet<Item>();
		
		for (Link link : item.getOutgoingLinks()) {
			Item destItem = link.getDestination(true);
			if (destItem == null)
				continue;
			
			if (cannotCommit(destItem)) {
				continue;
			}

			if (isRequireNewRev(destItem) || isCoupled(link.getLinkType())) {
				itemsToCommit.add(destItem);
			}
		}
		
		for (Link link : item.getIncomingLinks()) {
			Item sourceItem = link.getSource();
			if (sourceItem == null)
				continue;
			
			if (cannotCommit(sourceItem)) {
				continue;
			}

			if (isCoupled(link.getLinkType())) {
				itemsToCommit.add(sourceItem);
			}
		}
		
		return itemsToCommit;
	}
	
	/**
	 * Returns a workspace transaction where teamwork state is not computed on live.
	 * 
	 * @return a workspace transaction where teamwork state is not computed on live.
	 */
	public static LogicalWorkspaceTransaction createWorkspaceTransactionForTWoperation() {
		final LogicalWorkspaceTransaction transaction = CadseCore.getLogicalWorkspace().createTransaction();
		transaction.removeLogicalWorkspaceTransactionListener(
				((LogicalWorkspaceTransactionImpl) transaction).getTeamWorkLWCListener());
		return transaction;
	}
	
	/**
	 * Returns true if this attribute definition is a link type.
	 * 
	 * @param attrType an attribute definition
	 * @return true if this attribute definition is a link type.
	 */
	public static boolean isLinkType(IAttributeType<?> attrType) {
		return attrType.isInstanceOf(CadseGCST.LINK_TYPE);
	}

	/**
	 * Returns true if this item is an item type.
	 * 
	 * @param item
	 *            an item
	 * @return true if this item is an item type.
	 */
	public static boolean isItemType(Item item) {
		return item.isInstanceOf(CadseGCST.ITEM_TYPE);
	}

	/**
	 * Call underlying SCM to update modified status for specified item content. 
	 * 
	 * @param item an item
	 * @return true if content is considered as modified
	 */
	public static boolean refreshContentStatus(Item item) {
		ContentItem contentItem = item.getContentItem();
		if (contentItem == null)
			return false;
		
		SCMService scmService = ((CadseDomainImpl) CadseCore.getCadseDomain()).getSCMService();
		
		// recover scm repository url if needed
		String scmRepoUrl = contentItem.getSCMRepoUrl();
		if ((scmRepoUrl == null) || (scmRepoUrl.trim().equals(""))) {
			try {
				String newScmRepoUrl = scmService.getSCMRepositoryURL(contentItem);
				if (newScmRepoUrl != null)
					contentItem.setSCMRepoUrl(newScmRepoUrl);
			} catch (SCMException e) {
				e.printStackTrace();
			}
		}
		
		try {
			boolean contentHasChanged = scmService.contentHasBeenChanged(contentItem);
			setContentModifiedFlag(item, contentHasChanged);
			return contentHasChanged;
		} catch (SCMException e) {
			if (e.getMessage().equals(SCMService.CANNOT_RETRIEVE_SCM_URL)) {
				Logger l = Logger.getLogger("fr.imag.adele.tw");
				final Item ownerItem = contentItem.getOwnerItem();
				l.warning(SCMService.CANNOT_RETRIEVE_SCM_URL+" for "+(ownerItem == null? "??": ownerItem.getDisplayName()));
				return false;
			}
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * After call it, content of specified item will be considered as modified by TeamWork.
	 * 
	 * @param item an item
	 */
	public static void setContentAsModified(Item item) {
		setContentModifiedFlag(item, true);
	}
	
	public static void setContentModifiedFlag(Item item, boolean isModified) {
		ContentItem contentItem = item.getContentItem();
		if (contentItem == null)
			return;
		
		contentItem.setSCMModified(isModified);
	}

	/**
	 * Returns true if each item revision can have a different content.
	 * 
	 * @param item an item
	 * @return true if each item revision can have a different content.
	 */
	public static boolean isContentRevSpecific(Item item) {
		// TODO implement it
		return true;
	}

	/**
	 * Returns impact flag of a change on content of specified item.
	 * 
	 * @param item an item
	 * @return impact flag of a change on content of specified item.
	 */
	public static TWEvol getContentEvol(Item modifiedItem) {
		// TODO implement it
		return TWEvol.twImmutable;
	}

	public static String getSCMRepoUrl(Item item) {
		ContentItem contentItem = item.getContentItem();
		if (contentItem == null)
			return null;
		
		return contentItem.getSCMRepoUrl();
	}

	/**
	 * Returns the CADSE which defines specified item type.
	 * 
	 * @param itemType an item type
	 * @return the CADSE which defines specified item type.
	 */
	public static String getCadse(ItemType itemType) {
		Item cadseRuntime = itemType.getOutgoingItem(
				CadseGCST.TYPE_DEFINITION_lt_CADSE, true);
		String cadseName = cadseRuntime.getName();
		return cadseName;
	}
}
