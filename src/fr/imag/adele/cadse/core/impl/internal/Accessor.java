/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding transactionright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a transaction of the License at
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.Messages;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.delta.ItemDelta;
import fr.imag.adele.cadse.core.delta.LinkDelta;
import fr.imag.adele.cadse.core.delta.SetAttributeOperation;
import fr.imag.adele.cadse.core.impl.CadseIllegalArgumentException;
import fr.imag.adele.cadse.core.impl.internal.delta.DeleteOperationImpl;
import fr.imag.adele.cadse.core.key.AbstractSpaceKey;
import fr.imag.adele.cadse.core.key.ISpaceKey;
import fr.imag.adele.cadse.core.key.SpaceKeyType;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransaction;
import fr.imag.adele.cadse.core.util.IErrorCollector;

public class Accessor {

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#getPartParentLink()
	 */
	final static public LinkType getPartParentLinkType(Item child) {
		Item parentItem = child.getPartParent(false);
		if (parentItem == null) {
			return null;
		}

		return getPartParentLinkType(child, parentItem);
	}

	public static LinkType getPartParentLinkType(Item child, Item parentItem) {
		Link l = getPartLinkParent(parentItem, child);
		if (l != null) {
			l.getLinkType();
		}

		for (Link childLink : child.getOutgoingLinks()) {
			if (childLink.getLinkType().isInversePart()) {
				return childLink.getLinkType().getInverse();
			}
		}
		return child.getType().getIncomingPart(parentItem.getType());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#getPartParentLink()
	 */
	final static public Link getPartParentLink(Item child) {
		Item parentItem = child.getPartParent(false);
		if (parentItem == null) {
			return null;
		}

		return getPartLinkParent(parentItem, child);
	}

	/**
	 * Gets the part link between parent and child.
	 * 
	 * @parent A parent of child...
	 * @child An item which has a parent (parent)
	 * @return a link between parent and child, may by it's part
	 */
	final static private Link getPartLinkParent(Item parent, Item child) {
		Link firstLink = null;
		Link firstLinkPart = null;

		for (Link l : child.getIncomingLinks()) {
			if (l.getSource() == parent) {
				firstLink = l;
				if (l.getLinkType().isPart()) {
					firstLinkPart = l;
				}
			}
		}
		if (firstLinkPart != null) {
			return firstLinkPart;
		}
		return firstLink;
	}

	final static public int indexOf(Link link) {
		Item source = link.getSource();
		List<Link> links = source.getOutgoingLinks(link.getLinkType());
		for (int i = 0; i < links.size(); i++) {
			if (links.get(i).getDestination().equals(link.getDestination())) {
				return i;
			}
		}
		return -1;
	}

	public static String	ATTR_PARENT_ITEM_ID			= "#ws:private:parent:id";
	public static String	ATTR_PARENT_ITEM			= "#ws:private:parent:item";
	public static String	ATTR_PARENT_ITEM_TYPE_ID	= "#ws:private:parent-type:id";

	final static public ItemType loadAttributes(Item item, ItemDelta desc, IErrorCollector errorCollector) {
		ItemType type = item.getType();
		if (type != null) {
			Map<String, IAttributeType<?>> all = new HashMap<String, IAttributeType<?>>();
			type.getAllAttributeTypes(all, true);
			HashSet<SetAttributeOperation> visited = new HashSet<SetAttributeOperation>();
			for (IAttributeType<?> att : all.values()) {
				try {
					if (att instanceof LinkType) {
						continue;
					}

					SetAttributeOperation v = desc.getSetAttributeOperation(att.getName(), false);
					if (v == null) {
						continue;
					}
					visited.add(v);
					if (!v.isModified() && !v.isLoaded()) {
						continue;
					}

					Object value = v.getCurrentValue();
					Object goodV;
					try {
						goodV = att.convertTo(value);
					} catch (Throwable e) {
						errorCollector.addError(desc, "Cannot load attribute " + att.getName() + " of type "
								+ att.getType().getName() + " : " + v);
						goodV = null;
					}
					// if (goodV == null) continue;
					item.commitSetAttribute(att, att.getName(), goodV);
				} catch (Throwable e) {
					e.printStackTrace();
					errorCollector.addError(item, "Cannot load attribute from " + att.getName() + ", exception : "
							+ e.getMessage());
				}
			}
			// il faut ajouter les attributs non défini...
			// ou don't la definition n'est pas présente
			for (SetAttributeOperation v : desc.getSetAttributeOperation()) {
				if (visited.contains(v)) {
					continue;
				}
				try {
					visited.add(v);
					if (v.getAttributeName().equals(ATTR_PARENT_ITEM)) {
						continue;
					}
					if (v.getAttributeName().equals(ATTR_PARENT_ITEM_ID)) {
						continue;
					}
					if (v.getAttributeName().equals(ATTR_PARENT_ITEM_TYPE_ID)) {
						continue;
					}

					if (!v.isModified() && !v.isLoaded()) {
						continue;
					}

					Object value = v.getCurrentValue();
					item.commitSetAttribute(null, v.getAttributeName(), value);
				} catch (Throwable e) {
					e.printStackTrace();
					errorCollector.addError(item, "Cannot load attribute from " + v.getAttributeName()
							+ ", exception : " + e.getMessage());
				}
			}
		}
		return type;
	}

	static public boolean isAncestorOf(Item source, Item item2) {

		for (Link l : item2.getIncomingLinks()) {
			if (l.getLinkType().isPart()) {
				// la relation part convert vers un parent, elle ne boucle pas.
				Item ancestorByContainement = l.getSource();
				if (ancestorByContainement == source) {
					return true;
				}
				if (isAncestorOf(source, ancestorByContainement)) {
					return true;
				}
			}
		}
		for (Link l : item2.getIncomingLinks()) {
			if (l.getLinkType().isAggregation()) {
				Item ancestorByAggregation = l.getSource();
				if (ancestorByAggregation == source) {
					return true;
				}
				if (isAncestorOf(source, ancestorByAggregation)) {
					return true;
				}
			}
		}

		return false;
	}

	static public ISpaceKey computekey(ISpaceKey key, ItemType type, Item item) {
		if (key == AbstractSpaceKey.NO_INIT_KEY) {
			if (type == null) {
				key = null;
			} else {
				SpaceKeyType keyType = type.getSpaceKeyType();
				if (keyType != null) {
					try {
						key = keyType.computeKey(item);
					} catch (Throwable e) {
						type.getCadseDomain().error(item,
								"Cannot compute key " + type.getName() + "::" + item.getName(), e);
						key = null; // sinon erreur !!!
					}
				} else {
					key = null;
				}
			}
		}
		return key;
	}

	static public Link createLink(Item source, LinkType linkType, Item destination) throws CadseException {
		LogicalWorkspaceTransaction transaction = source.getLogicalWorkspace().createTransaction();
		transaction.getItem(source.getId()).createLink(linkType, destination);
		transaction.commit();
		return source.getOutgoingLink(linkType, destination.getId());
	}

	public static void delete(Link l) throws CadseException {
		LogicalWorkspaceTransaction transaction = l.getSource().getLogicalWorkspace().createTransaction();
		LinkDelta lOper = transaction.getLink(l);
		if (lOper == null) {
			throw new CadseException("Link " + l + " doesn't exist");
		}
		lOper.delete();
		transaction.commit();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Link#moveAfter(fr.imag.adele.cadse.core.Link)
	 */
	public static void moveAfter(Link linkOne, Link linkTwo) throws CadseException {
		LogicalWorkspaceTransaction transaction = linkOne.getSource().getLogicalWorkspace().createTransaction();
		transaction.getItem(linkOne.getSourceId()).getOutgoingLinkOperation(linkOne).moveAfter(linkTwo);
		transaction.commit();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Link#moveBefore(fr.imag.adele.cadse.core.Link)
	 */
	public static void moveBefore(Link linkOne, Link linkTwo) throws CadseException {
		LogicalWorkspaceTransaction transaction = linkOne.getSource().getLogicalWorkspace().createTransaction();
		transaction.getItem(linkOne.getSourceId()).getOutgoingLinkOperation(linkOne).moveBefore(linkTwo);
		transaction.commit();
	}

	public static Link removeOutgoingItem(Item source, LinkType linkType, Item destination) throws CadseException {
		Link l = source.getOutgoingLink(linkType, destination.getId());
		if (l != null) {
			delete(l);
		}
		return l;
	}

	public static void delete(Item item, boolean deleteContent) throws CadseException {
		LogicalWorkspaceTransaction transaction = item.getLogicalWorkspace().createTransaction();
		transaction.getItem(item.getId()).delete(deleteContent);
		transaction.commit();
	}

	/**
	 * delete annotation link delete outgoing link delete part link
	 * 
	 * @param item
	 * @param deleteContent
	 * @throws CadseException
	 */
	public static void shadow(Item item, boolean deleteContent) throws CadseException {
		LogicalWorkspaceTransaction transaction = item.getLogicalWorkspace().createTransaction();
		transaction.getItem(item.getId()).delete(
				null,
				deleteContent ? DeleteOperationImpl.DELETE_CONTENT : 0 | DeleteOperationImpl.DELETE_ANNOTATION_LINK
						| DeleteOperationImpl.DELETE_PART_LINK);
		transaction.commit();
	}

	public static Collection<Link> setOutgoingItem(Item source, LinkType lt, Collection<Item> value)
			throws CadseException {

		List<Link> removed = new ArrayList<Link>();
		List<Link> modified = new ArrayList<Link>();
		LogicalWorkspaceTransaction transaction = source.getLogicalWorkspace().createTransaction();
		ItemDelta sourceOperation = transaction.getItem(source.getId());
		for (Link ol : source.getOutgoingLinks(lt)) {
			if (!ol.isLinkResolved()) {
				continue;
			}
			Item dest = ol.getResolvedDestination();
			if (value.remove(dest)) {
				modified.add(ol);
			} else {
				removed.add(ol);
			}
		}
		for (Link deleteLink : removed) {
			sourceOperation.getOutgoingLinkOperation(deleteLink).delete();
		}
		for (Item item : value) {
			sourceOperation.createLink(lt, item);
		}
		transaction.commit();
		// find the created links
		for (Link ol : source.getOutgoingLinks(lt)) {
			if (!ol.isLinkResolved()) {
				continue;
			}
			Item dest = ol.getResolvedDestination();
			if (value.remove(dest)) {
				modified.add(ol);
			}
		}
		return modified;

	}

	public static Link setOutgoingItemOne(Item source, LinkType lt, Item destination) throws CadseException {
		if (lt.getMax() != 1) {
			throw new CadseIllegalArgumentException(Messages.error_bad_link_type_max_not_equal_to_one, lt.getName());
		}
		Link l = source.getOutgoingLink(lt);
		if (l != null && l.getResolvedDestination() == destination) {
			return l;
		}
		LogicalWorkspaceTransaction transaction = source.getLogicalWorkspace().createTransaction();
		ItemDelta sourceOperation = transaction.getItem(source.getId());
		if (l != null) {
			sourceOperation.getOutgoingLinkOperation(l).delete();
		}
		sourceOperation.createLink(lt, destination);
		transaction.commit();
		return source.getOutgoingLink(lt, destination.getId());

	}

	public static boolean isInstanceOf(Item item, ItemType it) {
		ItemType item_itemType = item.getType();
		return item_itemType.equals(it) || it.isSuperTypeOf(item_itemType);
		// return this.type.equals(it) || it.isSuperTypeOf(this.type);
	}

	/**
	 * Seek links by kind.
	 * 
	 * NOTE: Seek all links same kind given in a link list given.
	 * 
	 * @param linkList :
	 *            link list to search.
	 * @param kind
	 *            the kind
	 * 
	 * @return list links have same kind.
	 */
	public static List<Link> getLinksByKind(List<Link> linkList, int kind) {
		List<Link> ret = new ArrayList<Link>();

		for (Link l : linkList) {
			// If link has same kind with kind, then add link to return list.
			if (l.getLinkType().getKind() == kind) {
				ret.add(l);
			}
		}
		return ret;
	}

	public static Collection<Item> getIncomingItem(List<? extends Link> incomings) {
		ArrayList<Item> ret = new ArrayList<Item>();
		for (Link l : incomings) {
			ret.add(l.getSource());
		}
		return ret;
	}

	public static ArrayList<Item> getOutgoingItems(List<Link> links, LinkType lt, boolean resovledOnly) {
		ArrayList<Item> ret = new ArrayList<Item>();
		for (Link l : links) {
			// Select link which linktype is equal to linkNameID and is
			// resolved.
			if (l.getLinkType() == lt) {
				Item item = l.getDestination(resovledOnly);
				if (item != null) {
					ret.add(item);
				}
			}
		}
		return ret;
	}

	public static ArrayList<Item> getOutgoingItems(List<Link> links, boolean resovledOnly) {
		ArrayList<Item> ret = new ArrayList<Item>();
		for (Link l : links) {
			// resolved.
			Item item = l.getDestination(resovledOnly);
			if (item != null) {
				ret.add(item);
			}
		}
		return ret;
	}

	public static ArrayList<Item> getOutgoingItems(List<Link> links, String linkType, boolean resovledOnly) {
		ArrayList<Item> ret = new ArrayList<Item>();
		for (Link l : links) {
			// Select link which linktype is equal to linkNameID and is
			// resolved.
			if (l.getLinkType().getName().equals(linkType)) {
				Item item = l.getDestination(resovledOnly);
				if (item != null) {
					ret.add(item);
				}
			}
		}
		return ret;
	}

	public static Item getOutgoingItems(List<Link> links, String linkTypeName, CompactUUID itemId, boolean resovledOnly) {
		for (Link l : links) {
			// Select link has kind Part and destination.
			if (l.getLinkType().getName().equals(linkTypeName) && l.getDestinationId().equals(itemId)) {
				return l.getDestination(resovledOnly);
			}
		}
		return null;
	}

	public static Item getOutgoingItem(List<Link> links, LinkType lt, boolean resovledOnly) {
		for (Link l : links) {
			// Select link has kind Part and destination.
			if (l.getLinkType() == lt) {
				return l.getDestination(resovledOnly);
			}
		}
		return null;
	}

	public static Item getOutgoingItem(List<Link> links, String linkTypeName, boolean resovledOnly) {
		for (Link l : links) {
			// Select link which linktype is equal to linkTypeName and is
			// resolved.
			if (l.getLinkType().getName().equals(linkTypeName)) {
				return l.getDestination(resovledOnly);
			}
		}
		return null;
	}

	public static Link getOutgoingLink(CompactUUID destId, List<Link> links) {
		for (Link link : links) {
			if (link.getDestinationId().equals(destId)) {
				return link;
			}
		}
		return null;
	}

	public static Link getOutgoingLink(List<Link> links, Item item) {
		for (Link link : links) {
			if (link.getDestinationId().equals(item.getId())) {
				return link;
			}
		}
		return null;
	}

	public static Link getOutgoingLink(List<Link> links, LinkType linkType) {
		for (Link l : links) {
			// Select link which linktype is equal to linkNameID and is
			// resolved.
			if (l.getLinkType() == linkType) {
				return l;
			}
		}
		return null;
	}

	public static Collection<Item> getParts(List<Link> links) {
		ArrayList<Item> ret = new ArrayList<Item>();
		for (Link l : links) {
			// Select link has kind Containement
			if (l.getLinkType().isPart()) {
				Item destination = l.getResolvedDestination();
				// if dest not null, take this destination to return list.
				if (destination != null) {
					ret.add(destination);
				}
			}
		}
		return ret;
	}

	public static Collection<Item> getParts(List<Link> links, LinkType linkNameID) {
		ArrayList<Item> ret = new ArrayList<Item>();
		for (Link l : links) {
			// Select link has kind Part and destination.
			if (l.getLinkType().isPart() && l.getLinkType() == linkNameID) {
				Item destination = l.getResolvedDestination();
				// if dest not null, take this destination to return list.
				if (destination != null) {
					ret.add(destination);
				}
			}
		}
		return ret;
	}

	public static Item getParts(List<Link> links, CompactUUID id) {
		for (Link l : links) {
			// If link has kind Containement and its destination's id is equal
			// id parameter. Return this object destination.
			if (l.getLinkType().isPart() && l.isLinkResolved() && l.getDestinationId().equals(id)) {
				return l.getResolvedDestination();
			}
		}
		// if not found, return null;
		return null;
	}

	public static Item getPartParent(Item item, ItemType typeID) {
		while (item != null) {
			if (item.isInstanceOf(typeID)) {
				return item;
			}
			// if (item == item.getPartParent()) {
			// return null;
			// }
			item = item.getPartParent();
		}

		return null;
	}

	public static Item getPartParentByName(Item item, String typeName) {
		while (item != null) {
			if (item.getType().getName().equals(typeName)) {
				return item;
			}
			item = item.getPartParent();
		}

		return null;
	}

	/**
	 * return the first item which name is equals to name parameter
	 * @param list a list of item
	 * @param name the name which search
	 * @return the item found or null if not found
	 */
	public static <T extends Item> T filterName(List<T> list, String name) {
		for (Iterator<T> incomers = list.iterator(); incomers.hasNext();) {
			T lt = incomers.next();
			if (lt.getName().equals(name)) {
				return lt;
			}
		}
		return null;
	}

}
