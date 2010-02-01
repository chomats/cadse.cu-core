package fr.imag.adele.cadse.core.impl.internal.delta;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import fr.imag.adele.cadse.core.CadseDomain;
import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseRuntime;
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
import fr.imag.adele.cadse.core.TypeDefinition;
import fr.imag.adele.cadse.core.WorkspaceListener;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.build.Composer;
import fr.imag.adele.cadse.core.build.Exporter;
import fr.imag.adele.cadse.core.build.IBuildingContext;
import fr.imag.adele.cadse.core.content.ContentItem;
import fr.imag.adele.cadse.core.transaction.delta.ImmutableWorkspaceDelta;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.internal.IWorkingLoadingItems;
import fr.imag.adele.cadse.core.internal.IWorkspaceNotifier;
import fr.imag.adele.cadse.core.key.Key;
import fr.imag.adele.cadse.core.transaction.delta.ImmutableWorkspaceDelta;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.ui.Pages;
import fr.imag.adele.cadse.core.ui.view.FilterContext;
import fr.imag.adele.cadse.core.ui.view.NewContext;
import fr.imag.adele.cadse.core.util.IErrorCollector;
import fr.imag.adele.cadse.util.OrderWay;

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

	public boolean canCreateLink(LinkType linkType, UUID destItemId) {
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

	public boolean containsComponent(UUID itemId) {
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

	public <T> T getAttributeOwner(IAttributeType<T> att) {
		return _delta.getAttributeOwner(att);
	}

	public <T> T getAttributeWithDefaultValue(IAttributeType<T> att, T defaultValue) {
		return _delta.getAttributeWithDefaultValue(att, defaultValue);
	}

	public Item getBaseItem() {
		return _delta.getBaseItem();
	}

	public CadseDomain getCadseDomain() {
		return _delta.getCadseDomain();
	}

	public Set<UUID> getComponentIds() {
		return _delta.getComponentIds();
	}

	public Item getComponentInfo(UUID itemId) {
		return _delta.getComponentInfo(itemId);
	}

	public Set<Item> getComponents() {
		return _delta.getComponents();
	}

	public ContentItem getContentItem() {
		return _delta.getContentItem();
	}

	public String getDisplayName() {
		return _delta.getDisplayName();
	}

	public UUID getId() {
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

	public Link getIncomingLink(LinkType linkType, UUID srcId) {
		return _delta.getIncomingLink(linkType, srcId);
	}

	public List<Link> getIncomingLinks() {
		return _delta.getIncomingLinks();
	}

	public List<Link> getIncomingLinks(LinkType linkType) {
		return _delta.getIncomingLinks(linkType);
	}

	public Key getKey() {
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

	public Item getOutgoingItem(String linkTypeName, UUID itemId, boolean resolvedOnly) {
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

	public Link getOutgoingLink(LinkType lt, UUID destId) {
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

	public Item getPartChild(UUID destItemId) {
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

	public int getVersion() {
		return _delta.getVersion();
	}

	public int indexOf(Link link) {
		return _delta.indexOf(link);
	}

	public <T> T internalGetGenericOwnerAttribute(IAttributeType<T> type) {
		return _delta.internalGetGenericOwnerAttribute(type);
	}

	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {
		return _delta.internalGetOwnerAttribute(type);
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

	public boolean isRequireNewRev() {
		return _delta.isRequireNewRev();
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

	public boolean setFlag(int f, boolean flag) {
		return _delta.setFlag(f, flag);
	}

	public void setIsStatic(boolean isStatic) {
		_delta.setIsStatic(isStatic);
	}

	public void setKey(Key newkey) throws CadseException {
		_delta.setKey(newkey);
	}

	public void setName(String name) {
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

	public void setQualifiedName(String qualifiedName) {
		_delta.setQualifiedName(qualifiedName);
	}

	public void setReadOnly(boolean readOnly) throws CadseException {
		_delta.setReadOnly(readOnly);
	}

	public void setState(ItemState newState) {
		_delta.setState(newState);
	}

	public void setType(ItemType itemType) {
		_delta.setType(itemType);
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
	public ItemType getGroup() {
		return _delta.getGroup();
	}

	@Override
	public Pages getCreationPages(NewContext context) throws CadseException {
		return _delta.getCreationPages(context);
	}

	@Override
	public Pages getModificationPages(FilterContext context) {
		return _delta.getModificationPages(context);
	}

	@Override
	public boolean isInstanceOf(TypeDefinition it) {
		return _delta.isInstanceOf(it);
	}

	@Override
	public CadseRuntime getCadse() {
		return _delta.getCadse();
	}
	
	@Override
	public UUID getCadseId() {
		return _delta.getCadseId();
	}

	@Override
	public void setCadse(CadseRuntime cr) {
		_delta.setCadse(cr);
	}

    @Override
    public int getIdInPackage() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setIdInPackage(int idInPackage) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setUUID(long itemMsb, long itemLsb) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

	@Override
	public Exporter[] getExporter(String exporterType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getObjectId() {
		return _delta.getObjectId();
	}

	@Override
	public void setUUID(UUID uuid) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IAttributeType<?> getLocalAttributeType(UUID attrName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void build(IBuildingContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clean(IBuildingContext context, boolean componentsContent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void compose(IBuildingContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setComposers(Composer... composers) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setExporters(Exporter... exporters) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<LinkType> getLocalOutgoingLinkTypes() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public ItemType[] getTypes() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented method");
	}

}
