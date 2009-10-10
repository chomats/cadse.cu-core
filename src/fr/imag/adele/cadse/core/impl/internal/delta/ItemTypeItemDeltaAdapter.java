package fr.imag.adele.cadse.core.impl.internal.delta;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.imag.adele.cadse.core.CadseDomain;
import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.CadseRuntime;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.IItemFactory;
import fr.imag.adele.cadse.core.IItemManager;
import fr.imag.adele.cadse.core.IItemNode;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemFilter;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.delta.ItemDelta;
import fr.imag.adele.cadse.core.delta.LinkDelta;
import fr.imag.adele.cadse.core.impl.CadseCore;
import fr.imag.adele.cadse.core.impl.internal.Accessor;
import fr.imag.adele.cadse.core.impl.internal.ItemTypeImpl;
import fr.imag.adele.cadse.core.key.SpaceKeyType;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransaction;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransactionListener;
import fr.imag.adele.cadse.core.ui.IActionContributor;
import fr.imag.adele.cadse.core.ui.IActionPage;
import fr.imag.adele.cadse.core.ui.IPage;
import fr.imag.adele.cadse.core.ui.IPageFactory;
import fr.imag.adele.cadse.core.ui.Pages;

public class ItemTypeItemDeltaAdapter extends ItemItemDeltaAdapter implements ItemType {

	
	
	public ItemTypeItemDeltaAdapter(ItemDelta itemDelta) {
		super(itemDelta);
	}

	@Override
	public void addActionContributeur(IActionContributor contributor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addCreationPages(List<IPageFactory> creationPages) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addModificationPages(List<IPageFactory> modificationPages) {
		// TODO Auto-generated method stub

	}

	@Override
	public LinkType createLinkType(CompactUUID id, int intID, String name,
			int kind, int min, int max, String selection, LinkType inverse)
			throws CadseException {
		// TODO Auto-generated method stub
		return null;
	}

	//TODO LINK TYPE RUNTIME or LINK TYPE EDITION (CADSEG) ?
	/* id-runtime == id if item is static
	 * else attribute 'id-runtime'
	 * 
	 * */
	@Override
	public LinkType createLinkType(CompactUUID id, int intID, String name,
			int kind, int min, int max, String selection, ItemType destination) throws CadseException {
		ItemDelta linktypedelta = null;
		LogicalWorkspaceTransaction copy = _delta.getCopy();
		if (id == null) {
			linktypedelta = copy.createItem(CadseGCST.LINK, _delta, CadseGCST.ABSTRACT_ITEM_TYPE_lt_ATTRIBUTES);
			linktypedelta.createLink(CadseGCST.LINK_lt_DESTINATION, destination);
		} else {
			linktypedelta = copy.loadItem(id, CadseGCST.LINK.getId());
			linktypedelta.setParent(_delta, CadseGCST.ABSTRACT_ITEM_TYPE_lt_ATTRIBUTES);
			linktypedelta.loadLink(CadseGCST.LINK_lt_DESTINATION.getName(), copy.loadItem(destination));
			
		}
		linktypedelta.setAttribute(CadseGCST.LINK_at_KIND_, null, kind, id != null);
		if (selection != null)
			linktypedelta.setAttribute(CadseGCST.LINK_at_SELECTION_, null, selection, id != null);
		linktypedelta.setAttribute(CadseGCST.ITEM_at_NAME_, null, name, id != null);
		linktypedelta.setAttribute(CadseGCST.LINK_at_MIN_, null, min, id != null);
		linktypedelta.setAttribute(CadseGCST.LINK_at_MAX_, null, max, id != null);
		
		return linktypedelta.getAdapter(LinkType.class);
	}

	@Override
	public IActionContributor[] getActionContribution() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IActionContributor[] getAllActionContribution() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCadseName() {
		return _delta.getAttribute(CadseGCST.CADSE_DEFINITION_at_CADSE_NAME_);
	}

	@Override
	public CadseRuntime getCadseRuntime() {
		ItemDelta cadseDelta = _delta.getOutgoingItem(CadseGCST.ITEM_TYPE_lt_CADSE_RUNTIME, true);
		if (cadseDelta == null)
			return null;
		return cadseDelta.getAdapter(CadseRuntime.class);
	}

	@Override
	public IPageFactory[] getCreationPage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CadseDomain getDomain() {
		return _delta.getCadseDomain();
	}

	@Override
	public IPage getFirstCreatedPage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPage getFirstModificationPage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Pages getGoodCreationPage(Item parent, ItemType type, LinkType lt)
			throws CadseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Pages getGoodModificationPage(Item selected) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Pages getGoodModificationPage(IItemNode selected) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URL getImage() {
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
		List<LinkType> ret = new ArrayList<LinkType>();
		ItemType superType = getSuperType();
		if (superType != null) {
			ret.addAll(superType.getIncomingLinkTypes());
		}
		for (Link l : _delta.getIncomingLinks()) {
			if (l.getLinkType() == CadseCore.theLinkType) {
				ret.add((LinkType) l);
			}
		}
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
		return new ArrayList<Item>(_delta.getIncomingItems(CadseGCST.ITEM_lt_INSTANCE_OF));
	}

	@Override
	public IPageFactory[] getModificationPage() {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	@Deprecated
//	public List<LinkType> getOugoingLinkTypes() {
//		return getOutgoingLinkTypes();
//	}

	@Override
	public LinkType getOutgoingLinkType(String name) {
		return ItemTypeImpl._getOutgoingLinkType(this, name);
	}

	@Override
	public LinkType getOutgoingLinkType(ItemType destination, String name) {
		return ItemTypeImpl._getOutgoingLinkType(this, destination, name);
	}

	@Override
	public LinkType getOutgoingLinkType(ItemType destination, int kind) {
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
		List<LinkDelta> deltaLinks = _delta.getOutgoingLinkOperations(CadseGCST.ABSTRACT_ITEM_TYPE_lt_ATTRIBUTES);
		for (LinkDelta linkDelta : deltaLinks) {
			ItemDelta destination = linkDelta.getDestination();
			if (destination.isInstanceOf(CadseGCST.LINK)) {
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
	public SpaceKeyType getSpaceKeyType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ItemType[] getSubTypes() {
		List<ItemType> ret = new ArrayList<ItemType>();
		List<LinkDelta> deltaLinks = _delta.getOutgoingLinkOperations(CadseGCST.ITEM_TYPE_lt_SUB_TYPES);
		for (LinkDelta linkDelta : deltaLinks) {
			ItemDelta destination = linkDelta.getDestination();
			ret.add(destination.getAdapter(ItemType.class));
		}
		return (ItemType[]) ret.toArray(new ItemType[ret.size()]);
	}

	@Override
	public ItemType getSuperType() {
		ItemDelta superDelta = _delta.getOutgoingItem(CadseGCST.ITEM_TYPE_lt_SUPER_TYPE, true);
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasUniqueNameAttribute() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAbstract() {
		Boolean ret = _delta.getAttribute(CadseGCST.ITEM_TYPE_at_IS_ABSTRACT_);
		return ret == null? CadseGCST.ITEM_TYPE_at_IS_ABSTRACT_.getDefaultValue() : ret;
	}

	@Override
	public boolean isPartType() {
		return ItemTypeImpl._isPartType(this);
	}

	@Override
	public boolean isRootElement() {
		Boolean ret = _delta.getAttribute(CadseGCST.ITEM_TYPE_at_IS_ROOT_ELEMENT_);
		return ret == null? CadseGCST.ITEM_TYPE_at_IS_ROOT_ELEMENT_.getDefaultValue() : ret;
	}

	@Override
	public boolean isRuntime() {
		// TODO Auto-generated method stub
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
	public void setHasShortNameAttribute(boolean val) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setHasUniqueNameAttribute(boolean val) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIcon(URL url) {
		// TODO Auto-generated method stub

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
	public void setSpaceKeyType(SpaceKeyType spaceKeytype) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> Link addAttributeType(IAttributeType<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void getAllAttributeTypes(List<IAttributeType<?>> all) {
		// TODO Auto-generated method stub

	}

	@Override
	public void getAllAttributeTypes(Map<String, IAttributeType<?>> all,
			boolean keepLastAttribute) {
		// TODO Auto-generated method stub

	}

	@Override
	public void getAllAttributeTypes(List<IAttributeType<?>> all,
			ItemFilter filter) {
		// TODO Auto-generated method stub

	}

	@Override
	public IAttributeType<?>[] getAllAttributeTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void getAllAttributeTypes(Map<String, IAttributeType<?>> all,
			boolean keepLastAttribute, ItemFilter filter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void getAllAttributeTypesKeys(Set<String> all, ItemFilter filter) {
		// TODO Auto-generated method stub

	}

	@Override
	public IAttributeType<?> getAttributeType(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IAttributeType<?> getAttributeType(String name,
			boolean createUnresolvedDefinition) {
		// TODO Auto-generated method stub
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
	public void computegetGoodCreationPage(Map<String, IPageFactory> map,
			List<IPageFactory> list) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeSubItemType(ItemType itemType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void resetContributions() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resetCreationPages() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resetIncomingLinkType() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resetModificationPages() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resetOutgoingLinkType() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSuperType(ItemType it) {
		try {
			LinkDelta oldSuperLink = _delta.getOutgoingLink(CadseGCST.ITEM_TYPE_lt_SUPER_TYPE);
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


}
