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
package fr.imag.adele.cadse.core.impl.internal.delta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import fr.imag.adele.cadse.core.CPackage;
import fr.imag.adele.cadse.core.CadseDomain;
import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.CadseRuntime;
import fr.imag.adele.cadse.core.ExtendedType;
import fr.imag.adele.cadse.core.GroupType;
import fr.imag.adele.cadse.core.IItemFactory;
import fr.imag.adele.cadse.core.IItemManager;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemFilter;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.TypeDefinition;
import fr.imag.adele.cadse.core.TypeDefinition.Internal;
import fr.imag.adele.cadse.core.attribute.GroupOfAttributes;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.content.ContentItem;
import fr.imag.adele.cadse.core.impl.internal.Accessor;
import fr.imag.adele.cadse.core.impl.internal.ItemTypeImpl;
import fr.imag.adele.cadse.core.key.KeyDefinition;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransaction;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransactionListener;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.transaction.delta.LinkDelta;
import fr.imag.adele.cadse.core.ui.HierarchicalPage;
import fr.imag.adele.cadse.core.ui.IActionContributor;
import fr.imag.adele.cadse.core.ui.IActionPage;
import fr.imag.adele.cadse.core.ui.IPage;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.ui.UIValidator;
import fr.imag.adele.cadse.core.ui.view.FilterContext;
import fr.imag.adele.cadse.core.ui.view.NewContext;

public class ItemTypeItemDeltaAdapter extends ItemItemDeltaAdapter implements
		ItemType, TypeDefinition.Internal {

	public ItemTypeItemDeltaAdapter(ItemDelta itemDelta) {
		super(itemDelta);
	}

	@Override
	public void addActionContributeur(IActionContributor contributor) {
	}
	
	@Override
	public void removeActionContributeur(IActionContributor contributor) {
	}

	@Override
	public LinkType createLinkType(UUID id, int intID, String name, int kind,
			int min, int max, String selection, LinkType inverse)
			throws CadseException {
		return null;
	}

	/*
	 * id-runtime == id if item is static else attribute 'id-runtime'
	 */
	@Override
	public LinkType createLinkType(UUID id, int intID, String name, int kind,
			int min, int max, String selection, TypeDefinition destination)
			throws CadseException {
		ItemDelta linktypedelta = null;
		LogicalWorkspaceTransaction copy = _delta.getCopy();
		if (id == null) {
			linktypedelta = copy.createItem(CadseGCST.LINK_TYPE, _delta,
					CadseGCST.TYPE_DEFINITION_lt_ATTRIBUTES);
			linktypedelta.createLink(CadseGCST.LINK_TYPE_lt_DESTINATION,
					destination);
		} else {
			linktypedelta = copy.loadItem(id, CadseGCST.LINK_TYPE.getId());
			linktypedelta.setParent(_delta,
					CadseGCST.TYPE_DEFINITION_lt_ATTRIBUTES);
			linktypedelta.loadLink(CadseGCST.LINK_TYPE_lt_DESTINATION, copy
					.loadItem(destination));

		}
		linktypedelta.setAttribute(CadseGCST.LINK_TYPE_at_KIND_, kind,
				id != null);
		if (selection != null)
			linktypedelta.setAttribute(CadseGCST.LINK_TYPE_at_SELECTION_,
					selection, id != null);
		linktypedelta.setAttribute(CadseGCST.ITEM_at_NAME_, name, id != null);
		linktypedelta
				.setAttribute(CadseGCST.LINK_TYPE_at_MIN_, min, id != null);
		linktypedelta
				.setAttribute(CadseGCST.LINK_TYPE_at_MAX_, max, id != null);

		return linktypedelta.getAdapter(LinkType.class);
	}

	@Override
	public IActionContributor[] getActionContribution() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getCadseName() {
		return _delta.getAttribute(CadseGCST.CADSE_DEFINITION_at_CADSE_NAME_);
	}

	@Override
	public CadseRuntime getCadse() {
		ItemDelta cadseDelta = _delta.getOutgoingItem(
				CadseGCST.TYPE_DEFINITION_lt_CADSE, true);
		if (cadseDelta == null)
			return null;
		return cadseDelta.getAdapter(CadseRuntime.class);
	}

	@Override
	public IPage[] getCreationPage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CadseDomain getDomain() {
		return _delta.getCadseDomain();
	}

	@Override
	public String getImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkType getIncomingLinkType(String name) {
		return Accessor.filterName(getIncomingLinkTypes(), name);
	}

	/**
	 * Get all incoming link types.
	 * 
	 * @return an list all incoming link types.
	 */
	public List<LinkType> getIncomingLinkTypes() {
		if (_delta.getBaseItem() != null
				&& _delta.getBaseItem() instanceof ItemType) {
			return ((ItemType) _delta.getBaseItem()).getIncomingLinkTypes();
		}
		List<LinkType> ret = new ArrayList<LinkType>();
		ItemType superType = getSuperType();
		if (superType != null) {
			ret.addAll(superType.getIncomingLinkTypes());
		}

		// for (Link l : _delta.getIncomingLinks()) {
		// if (l.getLinkType() == CadseCore.theLinkType) {
		// if (l instanceof LinkType)
		// ret.add((LinkType) l);
		// else if (l instanceof LinkDelta) {
		// System.out.println(l);
		// new
		// ItemTypeItemDeltaAdapter(l.getSource()).getOutgoingLinkType(((LinkDelta)
		// l).getLinkTypeName())
		// }
		// }
		// }
		return ret;
	}

	@Override
	public LinkType getIncomingOne(ItemType type) throws CadseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkType getIncomingPart(ItemType typeParent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IItemFactory getItemFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IItemManager getItemManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Item> getItems() {
		return new ArrayList<Item>(_delta
				.getIncomingItems(CadseGCST.ITEM_lt_INSTANCE_OF));
	}

	@Override
	public IPage[] getModificationPage() {
		// TODO Auto-generated method stub
		return null;
	}

	// @Override
	// @Deprecated
	// public List<LinkType> getOugoingLinkTypes() {
	// return getOutgoingLinkTypes();
	// }

	@Override
	public LinkType getOutgoingLinkType(String name) {
		return ItemTypeImpl._getOutgoingLinkType(this, name);
	}

	@Override
	public LinkType getOutgoingLinkType(TypeDefinition destination, String name) {
		return ItemTypeImpl._getOutgoingLinkType(this, destination, name);
	}

	@Override
	public LinkType getOutgoingLinkType(TypeDefinition destination, int kind) {
		return ItemTypeImpl._getOutgoingLinkType(this, destination, kind);
	}

	@Override
	public List<LinkType> getOutgoingLinkTypes() {
		List<LinkType> ret = new ArrayList<LinkType>();
		ItemType _this = this;

		while (true) {
			ret.addAll(_this.getOwnerOutgoingLinkTypes());
			ItemType s = _this.getSuperType();
			if (s == null || s == _this)
				break;
			_this = s;
		}

		return ret;
	}

	@Override
	public List<LinkType> getOwnerOutgoingLinkTypes() {
		List<LinkType> ret = new ArrayList<LinkType>();
		List<LinkDelta> deltaLinks = _delta
				.getOutgoingLinkOperations(CadseGCST.TYPE_DEFINITION_lt_ATTRIBUTES);
		for (LinkDelta linkDelta : deltaLinks) {
			ItemDelta destination = linkDelta.getDestination();
			if (destination.isInstanceOf(CadseGCST.LINK_TYPE)) {
				ret.add(destination.getAdapter(LinkType.class));
			}
		}
		return ret;
	}

	@Override
	public String getPackageName() {
		return _delta.getAttribute(CadseGCST.ITEM_TYPE_at_PACKAGE_NAME_);
	}

	@Override
	public ItemType[] getSubTypes() {
		List<ItemType> ret = new ArrayList<ItemType>();
		List<LinkDelta> deltaLinks = _delta
				.getOutgoingLinkOperations(CadseGCST.ITEM_TYPE_lt_SUB_TYPES);
		for (LinkDelta linkDelta : deltaLinks) {
			ItemDelta destination = linkDelta.getDestination();
			ret.add(destination.getAdapter(ItemType.class));
		}
		return (ItemType[]) ret.toArray(new ItemType[ret.size()]);
	}

	@Override
	public ItemType getSuperType() {
		ItemDelta superDelta = _delta.getOutgoingItem(
				CadseGCST.ITEM_TYPE_lt_SUPER_TYPE, true);
		if (superDelta == null)
			return null;
		return superDelta.getAdapter(ItemType.class);
	}

	@Override
	public boolean hasContent() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasIncomingParts() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasShortNameAttribute() {
		return _delta.getAttribute(CadseGCST.ITEM_TYPE_at_HAS_SHORT_NAME_);
	}

	@Override
	public boolean hasQualifiedNameAttribute() {
		return _delta.getAttribute(CadseGCST.ITEM_TYPE_at_HAS_UNIQUE_NAME_);
	}

	@Override
	public boolean isAbstract() {
		Boolean ret = _delta
				.getAttribute(CadseGCST.ITEM_TYPE_at_IS_INSTANCE_ABSTRACT_);
		return ret == null ? CadseGCST.ITEM_TYPE_at_IS_INSTANCE_ABSTRACT_
				.getDefaultValue() : ret;
	}

	@Override
	public boolean isPartType() {
		return ItemTypeImpl._isPartType(this);
	}

	@Override
	public boolean isRootElement() {
		Boolean ret = _delta
				.getAttribute(CadseGCST.ITEM_TYPE_at_IS_ROOT_ELEMENT_);
		return ret == null ? CadseGCST.ITEM_TYPE_at_IS_ROOT_ELEMENT_
				.getDefaultValue() : ret;
	}

	@Override
	public boolean isRuntime() {
		return false;
	}

	@Override
	public boolean isSuperTypeOf(ItemType it) {
		return ItemTypeImpl._isSuperTypeOf(this, it);
	}

	@Override
	public void setCreationAction(Class<? extends IActionPage> clazz,
			String defaultShortName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setHasNameAttribute(boolean val) {
		try {
			_delta.setAttribute(CadseGCST.ITEM_TYPE_at_HAS_SHORT_NAME_, val);
		} catch (CadseException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setHasQualifiedNameAttribute(boolean val) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIcon(String url) {
		try {
			_delta.setAttribute(CadseGCST.ITEM_TYPE_at_ICON_, url);
		} catch (CadseException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setItemFactory(IItemFactory factory) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPackageName(String packageName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRootElement(boolean val) {
		try {
			_delta.setAttribute(CadseGCST.ITEM_TYPE_at_IS_ROOT_ELEMENT_, val);
		} catch (CadseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public <T> Link addAttributeType(IAttributeType<T> type) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void getAllAttributeTypes(List<IAttributeType<?>> all) {
		all.addAll(Arrays.asList(getAllAttributeTypes()));
	}

	@Override
	public void getAllAttributeTypes(Map<String, IAttributeType<?>> all,
			boolean keepLastAttribute) {
		getAllAttributeTypes(all, keepLastAttribute, null);
	}

	@Override
	public void getAllAttributeTypes(List<IAttributeType<?>> all,
			ItemFilter<IAttributeType<?>> filter) {
		for(Item item : _delta.getOutgoingItems(CadseGCST.TYPE_DEFINITION_lt_ATTRIBUTES,true)) {
			if (item instanceof ItemDelta)
				item = item.getBaseItem();
			if (item instanceof IAttributeType<?>) {
				if (filter == null || filter.accept((IAttributeType<?>) item))
					all.add((IAttributeType<?>) item);
			}
		}
		//TODO Extension.
		
		ItemType superType = getSuperType();
		if (superType != null) {
			superType.getAllAttributeTypes(all, filter);
		}

	}

	@Override
	public IAttributeType<?>[] getAllAttributeTypes() {
		HashSet<IAttributeType<?>> ret = new HashSet<IAttributeType<?>>();
		for(Item item : _delta.getOutgoingItems(CadseGCST.TYPE_DEFINITION_lt_ATTRIBUTES,true)) {
			if (item instanceof ItemDelta)
				item = item.getBaseItem();
			if (item instanceof IAttributeType<?>)
				ret.add((IAttributeType<?>) item);
		}
		//TODO Extension.
		ItemType superType = getSuperType();
		if (superType != null) {
			IAttributeType<?>[] ss = superType.getAllAttributeTypes();
			ret.addAll(Arrays.asList(ss));
		}
		return (IAttributeType<?>[]) ret.toArray(new IAttributeType<?>[ret.size()]);
	}

	@Override
	public void getAllAttributeTypes(Map<String, IAttributeType<?>> all,
			boolean keepLastAttribute, ItemFilter<IAttributeType<?>> filter) {
		for(Item item : _delta.getOutgoingItems(CadseGCST.TYPE_DEFINITION_lt_ATTRIBUTES,true)) {
			if (item instanceof ItemDelta)
				item = item.getBaseItem();
			if (item instanceof IAttributeType<?>) {
				if (keepLastAttribute && all.containsKey(item.getName())) continue;
				if (filter == null || filter.accept((IAttributeType<?>) item))
					all.put(item.getName(), (IAttributeType<?>) item);
			}
		}
		//TODO Extension.
		
		ItemType superType = getSuperType();
		if (superType != null) {
			superType.getAllAttributeTypes(all, keepLastAttribute, filter);
		}
	}

	@Override
	public void getAllAttributeTypesKeys(Set<String> all, ItemFilter<IAttributeType<?>> filter) {
		// TODO Auto-generated method stub

	}

	@Override
	public IAttributeType<?> getAttributeType(String name) {
		for(Item item : _delta.getOutgoingItems(CadseGCST.TYPE_DEFINITION_lt_ATTRIBUTES,true)) {
			if (!item.getName().equals(name))
				continue;
			if (item instanceof ItemDelta) {
				return (IAttributeType<?>) item.getBaseItem();
			} else if (item instanceof IAttributeType<?>) {
				return (IAttributeType<?>) item;
			}
		}
		ItemType superType = getSuperType();
		if (superType != null) {
			return superType.getAttributeType(name);
		}
		return null;
	}

	@Override
	public String[] getAttributeTypeIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addLogicalWorkspaceTransactionListener(
			LogicalWorkspaceTransactionListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public LogicalWorkspaceTransactionListener[] getLogicalWorkspaceTransactionListener() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeLogicalWorkspaceTransactionListener(
			LogicalWorkspaceTransactionListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addSubItemType(ItemType itemType) {
		try {
			_delta.createLink(CadseGCST.ITEM_TYPE_lt_SUB_TYPES, itemType);
		} catch (CadseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void removeSubItemType(ItemType itemType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSuperType(ItemType it) {
		try {
			LinkDelta oldSuperLink = _delta
					.getOutgoingLink(CadseGCST.ITEM_TYPE_lt_SUPER_TYPE);
			if (oldSuperLink != null)
				oldSuperLink.delete();
			_delta.createLink(CadseGCST.ITEM_TYPE_lt_SUPER_TYPE, it);
		} catch (CadseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public <T> T getApdapter(Item instance, Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getItemManagerClass() {
		return _delta.getAttribute(CadseGCST.ITEM_TYPE_at_MANAGER_CLASS_);
	}

	@Override
	public String getCSTName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCSTName(String cst) {
		// TODO Auto-generated method stub

	}

	@Override
	public Link addOutgoingLinkType(LinkType ret) {
		try {
			return _delta.createLink(CadseGCST.TYPE_DEFINITION_lt_ATTRIBUTES,
					ret);
		} catch (CadseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public List<LinkType> getGroupOutgoingLinkTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public ItemType getGroupType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Item> getMembers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isGroupHead() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isGroupOf(GroupType groupType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isGroupType() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSuperGroupTypeOf(GroupType gt) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ItemType[] getAllSubGroupType() {
		return getSubTypes();
	}

	@Override
	public boolean isMemberType() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canCreateItem(NewContext newContext) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canCreateItem(NewContext newContext, LinkType lt, Item src) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addCreationPages(IPage... creationPages) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addModificationPages(IPage... modificationPages) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDefaultInstanceName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addValidators(UIValidator v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addField(UIField v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addGroupOfAttributes(GroupOfAttributes g) {
		// TODO Auto-generated method stub

	}

	@Override
	public GroupOfAttributes[] getGroupOfAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UIField findField(IAttributeType<?> att) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isExtendedType() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isMainType() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void computeGroup(Set<GroupOfAttributes> groups, Set<TypeDefinition> visited) {
		// TODO Auto-generated method stub

	}

	@Override
	public IActionPage createDefaultCreationAction(NewContext context)
			throws CadseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setKeyDefinition(KeyDefinition keyDefinition) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public KeyDefinition getKeyDefinition() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public ExtendedType[] getExtendedType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CPackage getPackage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IAttributeType<?> getCStructuralFeatures(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPackage(CPackage p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void computeGenericPage(FilterContext context,
			HierarchicalPage genericPage,
			HashSet<IAttributeType<?>> inSpecificPages,
			Set<IAttributeType<?>> ro, Set<TypeDefinition> visited, IAttributeType<?>... firstAttributes) {
		// TODO Auto-generated method stub

	}

	@Override
	public void computeValidators(FilterContext context,
			List<UIValidator> validators, Set<TypeDefinition> visited) {
		// TODO Auto-generated method stub

	}

	@Override
	public IActionPage createDefaultModificationAction(FilterContext context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void recurcifComputeCreationPage(FilterContext context,
			List<IPage> list, Set<TypeDefinition> visited) {
		// TODO Auto-generated method stub

	}

	@Override
	public void recurcifComputeModificationPage(FilterContext context,
			List<IPage> list, Set<IAttributeType<?>> ro, Set<TypeDefinition> visited) {
		// TODO Auto-generated method stub

	}

	@Override
	public LinkType getOutgoingLinkType(UUID idLinkType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addExtendedType(ExtendedType et) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeExtendedType(ExtendedType et) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<IActionContributor> getAllActionContribution() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public void setIsAbstract(boolean b) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public void computeIncomingLinkTypes(List<LinkType> ret,
			Set<TypeDefinition> visited) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public boolean isDelegatedAttribute(IAttributeType<?> attr) {
		// FIXME: DELEGATE : implements
		return false;
	}

	@Override
	public void setDelegatedAttribute(IAttributeType<?> attr, boolean val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean canBeDelegatedAttribute(IAttributeType<?> attr) {
		// TODO Auto-generated method stub
		return false;
	}

	public ItemDelta delta() {
		return _delta;
	}

	@Override
	public List<LinkType> getOutgoingLinkTypes(int flag,
			ItemFilter<LinkType> filter) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public List<LinkType> computeOutgoingLinkTypes(int flag, ItemFilter<LinkType> filter,
			List<LinkType> ret, Set<TypeDefinition> visited) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public Class<? extends ContentItem> getContentItemClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setContentItemClass(Class<? extends ContentItem> cf) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ItemType[] getAllConcreteType() {
		TreeSet<ItemType> set = new TreeSet<ItemType>(new Comparator<ItemType>() {

			@Override
			public int compare(ItemType o1, ItemType o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		computeAllContcreteType(set, new HashSet<TypeDefinition>());
		return (ItemType[]) set.toArray(new ItemType[set.size()]);
	}

	@Override
	public void computeAllContcreteType(TreeSet<ItemType> set,
			HashSet<TypeDefinition> visiteur) {
		if (visiteur.contains(this))
			return;
		visiteur.add(this);
		if( !isAbstract())
			set.add(this);
		for (ItemType it : getSubTypes()) {
			((Internal) it).computeAllContcreteType(set, visiteur);
		}
	}
}
