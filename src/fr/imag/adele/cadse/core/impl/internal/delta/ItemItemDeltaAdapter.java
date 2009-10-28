package fr.imag.adele.cadse.core.impl.internal.delta;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.imag.adele.cadse.core.CadseDomain;
import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.ContentItem;
import fr.imag.adele.cadse.core.DerivedLink;
import fr.imag.adele.cadse.core.DerivedLinkDescription;
import fr.imag.adele.cadse.core.EventFilter;
import fr.imag.adele.cadse.core.GroupType;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemDescription;
import fr.imag.adele.cadse.core.ItemDescriptionRef;
import fr.imag.adele.cadse.core.ItemFilter;
import fr.imag.adele.cadse.core.ItemState;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.WorkspaceListener;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.delta.ImmutableWorkspaceDelta;
import fr.imag.adele.cadse.core.delta.ItemDelta;
import fr.imag.adele.cadse.core.internal.IWorkingLoadingItems;
import fr.imag.adele.cadse.core.internal.IWorkspaceNotifier;
import fr.imag.adele.cadse.core.key.ISpaceKey;
import fr.imag.adele.cadse.core.util.IErrorCollector;
import fr.imag.adele.cadse.core.util.OrderWay;

public class ItemItemDeltaAdapter implements Item {

	protected ItemDelta	_delta;

	public ItemItemDeltaAdapter(ItemDelta itemDelta) {
		_delta = itemDelta;
	}

	public void addIncomingLink(Link link, boolean notify) {
		_delta.addIncomingLink(link, notify);
	}

	public void addListener(WorkspaceListener listener, EventFilter eventFilter) {
		_delta.addListener(listener, eventFilter);
	}

	public void addListener(WorkspaceListener listener, int eventFilter) {
		_delta.addListener(listener, eventFilter);
	}

	public Link addOutgoingItem(LinkType lt, Item destination) throws CadseException {
		return _delta.addOutgoingItem(lt, destination);
	}

	public void buildComposite() throws CadseException {
		_delta.buildComposite();
	}

	public boolean canCreateLink(LinkType linkType, CompactUUID destItemId) {
		return _delta.canCreateLink(linkType, destItemId);
	}

	public boolean canEditContent(String slashedPath) {
		return _delta.canEditContent(slashedPath);
	}

	public boolean canSetAttribute(String attrName, Object value) {
		return _delta.canSetAttribute(attrName, value);
	}

	public Link commitLoadCreateLink(LinkType lt, Item destination) throws CadseException {
		return _delta.commitLoadCreateLink(lt, destination);
	}

	public boolean commitMove(OrderWay kind, Link l1, Link l2) throws CadseException {
		return _delta.commitMove(kind, l1, l2);
	}

	public boolean commitSetAttribute(IAttributeType<?> type, Object value) {
		return _delta.commitSetAttribute(type, value);
	}

	public void computeAttribute(String attributeName, Object theirsValue, Object baseValue, Object mineValue) {
		_delta.computeAttribute(attributeName, theirsValue, baseValue, mineValue);
	}

	public void computeAttributes() {
		_delta.computeAttributes();
	}

	public boolean containsComponent(CompactUUID itemId) {
		return _delta.containsComponent(itemId);
	}

	public boolean containsPartChild(Item item) {
		return _delta.containsPartChild(item);
	}

	public boolean contentIsLoaded() {
		return _delta.contentIsLoaded();
	}

	public Link createLink(LinkType lt, Item destination) throws CadseException {
		return _delta.createLink(lt, destination);
	}

	public void delete(boolean deleteContent) throws CadseException {
		_delta.delete(deleteContent);
	}

	public boolean exists() {
		return _delta.exists();
	}

	public List<WorkspaceListener> filter(int eventFilter, ImmutableWorkspaceDelta delta) {
		return _delta.filter(eventFilter, delta);
	}

	public void finishLoad() {
		_delta.finishLoad();
	}

	public void forceState(ItemState state) {
		_delta.forceState(state);
	}

	public List<Link> getAggregations() {
		return _delta.getAggregations();
	}

	public ContentItem getAndCreateContentManager() {
		return _delta.getAndCreateContentManager();
	}

	public <T> T getAttribute(IAttributeType<T> att) {
		return _delta.getAttribute(att);
	}

	public <T> T getAttribute(String att) {
		return _delta.getAttribute(att);
	}

	public <T> T getAttributeH(String att, boolean fromSuperIfNull) {
		return _delta.getAttributeH(att, fromSuperIfNull);
	}

	public String[] getAttributeKeys() {
		return _delta.getAttributeKeys();
	}

	public <T> T getAttributeOwner(IAttributeType<T> att) {
		return _delta.getAttributeOwner(att);
	}

	public <T> T getAttributeWithDefaultValue(IAttributeType<T> att, T defaultValue) {
		return _delta.getAttributeWithDefaultValue(att, defaultValue);
	}

	public <T> T getAttributeWithDefaultValue(String att, T defaultValue) {
		return _delta.getAttributeWithDefaultValue(att, defaultValue);
	}

	public Item getBaseItem() {
		return _delta.getBaseItem();
	}

	public CadseDomain getCadseDomain() {
		return _delta.getCadseDomain();
	}

	public Set<CompactUUID> getComponentIds() {
		return _delta.getComponentIds();
	}

	public Item getComponentInfo(CompactUUID itemId) {
		return _delta.getComponentInfo(itemId);
	}

	public Set<Item> getComponents() {
		return _delta.getComponents();
	}

	public List<Item> getCompositeParent() {
		return _delta.getCompositeParent();
	}

	public ContentItem getContentItem() {
		return _delta.getContentItem();
	}

	public Set<DerivedLinkDescription> getDerivedLinkDescriptions(ItemDescription source) {
		return _delta.getDerivedLinkDescriptions(source);
	}

	public Set<DerivedLink> getDerivedLinks() {
		return _delta.getDerivedLinks();
	}

	public String getDisplayName() {
		return _delta.getDisplayName();
	}

	public CompactUUID getId() {
		return _delta.getId();
	}

	public Item getIncomingItem(LinkType linkType) {
		return _delta.getIncomingItem(linkType);
	}

	public Collection<Item> getIncomingItems() {
		return _delta.getIncomingItems();
	}

	public Collection<Item> getIncomingItems(LinkType linkType) {
		return _delta.getIncomingItems(linkType);
	}

	public Link getIncomingLink(LinkType linkType, CompactUUID srcId) {
		return _delta.getIncomingLink(linkType, srcId);
	}

	public List<Link> getIncomingLinks() {
		return _delta.getIncomingLinks();
	}

	public List<Link> getIncomingLinks(LinkType linkType) {
		return _delta.getIncomingLinks(linkType);
	}

	public ISpaceKey getKey() {
		return _delta.getKey();
	}

	public int getLastVersion() {
		return _delta.getLastVersion();
	}

	public IAttributeType<?>[] getLocalAllAttributeTypes() {
		return _delta.getLocalAllAttributeTypes();
	}

	public void getLocalAllAttributeTypes(List<IAttributeType<?>> allLocalAttrDefs, ItemFilter filter) {
		_delta.getLocalAllAttributeTypes(allLocalAttrDefs, filter);
	}

	public void getLocalAllAttributeTypes(List<IAttributeType<?>> allLocalAttrDefs) {
		_delta.getLocalAllAttributeTypes(allLocalAttrDefs);
	}

	public void getLocalAllAttributeTypes(Map<String, IAttributeType<?>> allLocalAttrDefs, boolean keepLastAttribute,
			ItemFilter filter) {
		_delta.getLocalAllAttributeTypes(allLocalAttrDefs, keepLastAttribute, filter);
	}

	public void getLocalAllAttributeTypes(Map<String, IAttributeType<?>> allLocalAttrDefs, boolean keepLastAttribute) {
		_delta.getLocalAllAttributeTypes(allLocalAttrDefs, keepLastAttribute);
	}

	public void getLocalAllAttributeTypesKeys(Set<String> allLocalAttrDefs, ItemFilter filter) {
		_delta.getLocalAllAttributeTypesKeys(allLocalAttrDefs, filter);
	}

	public IAttributeType<?> getLocalAttributeType(String attrName) {
		return _delta.getLocalAttributeType(attrName);
	}

	public File getLocation() {
		return _delta.getLocation();
	}

	public LogicalWorkspace getLogicalWorkspace() {
		return _delta.getLogicalWorkspace();
	}

	public <T> T getMainMappingContent(Class<T> clazz) {
		return _delta.getMainMappingContent(clazz);
	}

	public List<?> getMappingContents() {
		return _delta.getMappingContents();
	}

	public <T> List<T> getMappingContents(Class<T> clazz) {
		return _delta.getMappingContents(clazz);
	}

	public String getName() {
		return _delta.getName();
	}

	public Item getOutgoingItem(LinkType lt, boolean resovledOnly) {
		return _delta.getOutgoingItem(lt, resovledOnly);
	}

	public Item getOutgoingItem(String linkNameID, boolean resovledOnly) {
		return _delta.getOutgoingItem(linkNameID, resovledOnly);
	}

	public Item getOutgoingItem(String linkTypeName, CompactUUID itemId, boolean resolvedOnly) {
		return _delta.getOutgoingItem(linkTypeName, itemId, resolvedOnly);
	}

	public Collection<Item> getOutgoingItems(boolean resolvedOnly) {
		return _delta.getOutgoingItems(resolvedOnly);
	}

	public Collection<Item> getOutgoingItems(LinkType lt, boolean resolvedOnly) {
		return _delta.getOutgoingItems(lt, resolvedOnly);
	}

	public Collection<Item> getOutgoingItems(String typesLink, boolean resolvedOnly) {
		return _delta.getOutgoingItems(typesLink, resolvedOnly);
	}

	public Link getOutgoingLink(Item item) {
		return _delta.getOutgoingLink(item);
	}

	public Link getOutgoingLink(LinkType lt, CompactUUID destId) {
		return _delta.getOutgoingLink(lt, destId);
	}

	public Link getOutgoingLink(LinkType linkType) {
		return _delta.getOutgoingLink(linkType);
	}

	public List<Link> getOutgoingLinks() {
		return _delta.getOutgoingLinks();
	}

	public List<Link> getOutgoingLinks(LinkType linkType) {
		return _delta.getOutgoingLinks(linkType);
	}

	public Item getPartChild(CompactUUID destItemId) {
		return _delta.getPartChild(destItemId);
	}

	public Collection<Item> getPartChildren() {
		return _delta.getPartChildren();
	}

	public Collection<Item> getPartChildren(LinkType linkType) {
		return _delta.getPartChildren(linkType);
	}

	public Item getPartParent() {
		return _delta.getPartParent();
	}

	public Item getPartParent(boolean attemptToRecreate) {
		return _delta.getPartParent(attemptToRecreate);
	}

	public Item getPartParent(ItemType itemType) {
		return _delta.getPartParent(itemType);
	}

	public Item getPartParent(LinkType lt, boolean attemptToRecreate) throws CadseException {
		return _delta.getPartParent(lt, attemptToRecreate);
	}

	public Item getPartParent(LinkType lt) {
		return _delta.getPartParent(lt);
	}

	public Item getPartParentByName(String typeName) {
		return _delta.getPartParentByName(typeName);
	}

	public LinkType getPartParentLink() {
		return _delta.getPartParentLink();
	}

	public LinkType getPartParentLinkType() {
		return _delta.getPartParentLinkType();
	}

	public String getQualifiedDisplayName() {
		return _delta.getQualifiedDisplayName();
	}

	public String getQualifiedName() {
		return _delta.getQualifiedName();
	}

	public String getQualifiedName(boolean recompute) throws CadseException {
		return _delta.getQualifiedName(recompute);
	}

	public String getShortName() {
		return _delta.getShortName();
	}

	public ItemState getState() {
		return _delta.getState();
	}

	public ItemType getType() {
		return _delta.getType();
	}

	public String getUniqueName() {
		return _delta.getUniqueName();
	}

	public String getUniqueName(boolean recompute) throws CadseException {
		return _delta.getUniqueName(recompute);
	}

	public int getVersion() {
		return _delta.getVersion();
	}

	public int indexOf(Link link) {
		return _delta.indexOf(link);
	}

	public <T> T internalGetGenericOwnerAttribute(IAttributeType<T> type) {
		return _delta.internalGetGenericOwnerAttribute(type);
	}

	public <T> T internalGetGenericOwnerAttribute(String key) {
		return _delta.internalGetGenericOwnerAttribute(key);
	}

	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {
		return _delta.internalGetOwnerAttribute(type);
	}

	public <T> T internalGetOwnerAttribute(String key) {
		return _delta.internalGetOwnerAttribute(key);
	}

	public boolean isAccessible() {
		return _delta.isAccessible();
	}

	public boolean isAncestorOf(Item item) {
		return _delta.isAncestorOf(item);
	}

	public boolean isComposite() {
		return _delta.isComposite();
	}

	public boolean isHidden() {
		return _delta.isHidden();
	}

	public boolean isInIncomingLinks(Link l) {
		return _delta.isInIncomingLinks(l);
	}

	public boolean isInOutgoingLinks(Link l) {
		return _delta.isInOutgoingLinks(l);
	}

	public boolean isInstanceOf(ItemType it) {
		return _delta.isInstanceOf(it);
	}

	public boolean isOrphan() {
		return _delta.isOrphan();
	}

	public boolean isPartItem() {
		return _delta.isPartItem();
	}

	public boolean isReadOnly() {
		return _delta.isReadOnly();
	}

	public boolean isRequierNewRev() {
		return _delta.isRequierNewRev();
	}

	public boolean isResolved() {
		return _delta.isResolved();
	}

	public boolean isRevModified() {
		return _delta.isRevModified();
	}

	public boolean isStatic() {
		return _delta.isStatic();
	}

	public boolean isTWAttributeModified(IAttributeType<?> att) {
		return _delta.isTWAttributeModified(att);
	}

	public boolean isValid() {
		return _delta.isValid();
	}

	public boolean itemHasContent() {
		return _delta.itemHasContent();
	}

	public void loadItem(IWorkingLoadingItems wl, ItemDelta itemOperation, IErrorCollector errorCollector)
			throws CadseException {
		_delta.loadItem(wl, itemOperation, errorCollector);
	}

	public Iterator<Item> propagateValue(IAttributeType<?> type) {
		return _delta.propagateValue(type);
	}

	public Iterator<Item> propagateValue(String key) {
		return _delta.propagateValue(key);
	}

	public void refresh() {
		_delta.refresh();
	}

	public void removeContentItem() {
		_delta.removeContentItem();
	}

	public void removeIncomingLink(Link link, boolean notify) {
		_delta.removeIncomingLink(link, notify);
	}

	public void removeListener(WorkspaceListener listener) {
		_delta.removeListener(listener);
	}

	public Link removeOutgoingItem(LinkType linkType, Item destination) throws CadseException {
		return _delta.removeOutgoingItem(linkType, destination);
	}

	public void removeOutgoingLink(Link link, boolean notify) {
		_delta.removeOutgoingLink(link, notify);
	}

	public void setAttribute(IAttributeType<?> att, Object value) throws CadseException {
		_delta.setAttribute(att, value);
	}

	public void setAttribute(String att, Object value) throws CadseException {
		_delta.setAttribute(att, value);
	}

	public void setComponents(Set<ItemDescriptionRef> comp) throws CadseException {
		_delta.setComponents(comp);
	}

	public void setDerivedLinks(Set<DerivedLinkDescription> derivedLinks) {
		_delta.setDerivedLinks(derivedLinks);
	}

	public boolean setFlag(int f, boolean flag) {
		return _delta.setFlag(f, flag);
	}

	public void setIsStatic(boolean isStatic) {
		_delta.setIsStatic(isStatic);
	}

	public void setKey(ISpaceKey newkey) throws CadseException {
		_delta.setKey(newkey);
	}

	public void setName(String name) throws CadseException {
		_delta.setName(name);
	}

	public Link setOutgoingItem(LinkType linkType, Item dest) throws CadseException {
		return _delta.setOutgoingItem(linkType, dest);
	}

	public Collection<Link> setOutgoingItems(LinkType linkType, Collection<Item> value) throws CadseException {
		return _delta.setOutgoingItems(linkType, value);
	}

	public void setParent(Item parent, LinkType lt) {
		_delta.setParent(parent, lt);
	}

	public void setQualifiedName(String qualifiedName) throws CadseException {
		_delta.setQualifiedName(qualifiedName);
	}

	public void setReadOnly(boolean readOnly) throws CadseException {
		_delta.setReadOnly(readOnly);
	}

	public void setShortName(String name) throws CadseException {
		_delta.setShortName(name);
	}

	public void setState(ItemState newState) {
		_delta.setState(newState);
	}

	public void setType(ItemType itemType) {
		_delta.setType(itemType);
	}

	public void setUniqueName(String qualifiedName) throws CadseException {
		_delta.setUniqueName(qualifiedName);
	}

	public void setValid(boolean isValid) {
		_delta.setValid(isValid);
	}

	public void setVersion(int version) {
		_delta.setVersion(version);
	}

	public void shadow(boolean deleteContent) throws CadseException {
		_delta.shadow(deleteContent);
	}

	public String toString() {
		return _delta.toString();
	}

	public void unload() throws CadseException {
		_delta.unload();
	}

	public void update(IWorkingLoadingItems items, ItemDelta desc, IWorkspaceNotifier notifie) {
		_delta.update(items, desc, notifie);
	}

	@Override
	public ContentItem _getContentItem() {
		return _delta._getContentItem();
	}

	@Override
	public boolean isMember() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isMemberOf(Item item) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public GroupType getGroup() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<LinkType> getInstanceOutgoingLinkTypes() {
		return _delta.getInstanceOutgoingLinkTypes();
	}

}
