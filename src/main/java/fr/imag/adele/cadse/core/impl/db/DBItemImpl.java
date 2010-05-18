package fr.imag.adele.cadse.core.impl.db;

import fr.imag.adele.cadse.core.CadseException;
import java.util.UUID;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.imag.adele.cadse.core.CadseDomain;
import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseRuntime;
import java.util.UUID;

import fr.imag.adele.cadse.core.build.Composer;
import fr.imag.adele.cadse.core.build.Exporter;
import fr.imag.adele.cadse.core.build.IBuildingContext;
import fr.imag.adele.cadse.core.content.ContentItem;
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
import fr.imag.adele.cadse.core.attribute.DelegateValue;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.transaction.delta.ImmutableWorkspaceDelta;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.CadseIllegalArgumentException;
import fr.imag.adele.cadse.core.internal.IWorkingLoadingItems;
import fr.imag.adele.cadse.core.internal.IWorkspaceNotifier;
import fr.imag.adele.cadse.core.key.Key;
import fr.imag.adele.cadse.core.ui.Pages;
import fr.imag.adele.cadse.core.ui.view.FilterContext;
import fr.imag.adele.cadse.core.ui.view.NewContext;
import fr.imag.adele.cadse.core.util.IErrorCollector;
import fr.imag.adele.cadse.util.OrderWay;
import fr.imag.adele.teamwork.db.ModelVersionDBException;
import fr.imag.adele.teamwork.db.ModelVersionDBService2;

public class DBItemImpl extends DBObject implements Item {

	public DBItemImpl(int localId) {
		super(localId);
	}

	public void addListener(WorkspaceListener l, int eventFilter) {
		_dblw.addListener(_objectId, l, eventFilter);
	}

	public void addListener(WorkspaceListener l, EventFilter eventFilter) {
		l.setFilter(eventFilter);
		addListener(l, -1);
	}

	public List<WorkspaceListener> filter(int filters,
			ImmutableWorkspaceDelta delta) {
		return _dblw.filter(_objectId, filters, delta);
	}

	@Override
	public Link addOutgoingItem(LinkType linkType, Item destination)
			throws CadseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void buildComposite() throws CadseException {
		_dblw.buildComposite(_objectId);

	}

	@Override
	public boolean canCreateLink(LinkType linkType, UUID destItemId) {
		int destId;
		try {
			destId = _dblw.getDB().checkLocalIdentifier(destItemId);
		} catch (ModelVersionDBException e) {
			throw new CadseIllegalArgumentException(
					"Cannot get local identifier ", e);
		}
		if (destId == -1)
			return false;
		return _dblw.canCreateLink(linkType.getObjectId(), destId);
	}

	@Override
	public boolean canEditContent(String slashedPath) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canSetAttribute(String attrName, Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsPartChild(Item item) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contentIsLoaded() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Link createLink(LinkType linkType, Item destItem)
			throws CadseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(boolean deleteContent) throws CadseException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean exists() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void finishLoad() {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Link> getAggregations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ContentItem getAndCreateContentManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Item getBaseItem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CadseRuntime getCadse() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CadseDomain getCadseDomain() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ContentItem getContentItem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Pages getCreationPages(NewContext context) throws CadseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ItemType getGroup() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Item getIncomingItem(LinkType linkType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Item> getIncomingItems(LinkType linkType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Item> getIncomingItems() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Link getIncomingLink(LinkType linkType, UUID srcId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Link> getIncomingLinks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Link> getIncomingLinks(LinkType linkType) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Key getKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLastVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public File getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LogicalWorkspace getLogicalWorkspace() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getMainMappingContent(Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> getMappingContents(Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<?> getMappingContents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Pages getModificationPages(FilterContext context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Item getOutgoingItem(String linkTypeName, UUID itemId,
			boolean resolvedOnly) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Item getOutgoingItem(LinkType lt, boolean resolvedOnly) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Item getOutgoingItem(String linkTypeName, boolean resolvedOnly) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Item> getOutgoingItems(LinkType lt, boolean resolvedOnly) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Item> getOutgoingItems(boolean resolvedOnly) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Item> getOutgoingItems(String typesLink,
			boolean resolvedOnly) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Link getOutgoingLink(LinkType linkType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Link getOutgoingLink(Item destItem) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Link getOutgoingLink(LinkType lt, UUID destId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Link> getOutgoingLinks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Link> getOutgoingLinks(LinkType linkType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Item getPartChild(UUID destItemId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Item> getPartChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Item> getPartChildren(LinkType linkType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Item getPartParent(boolean attemptToRecreate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Item getPartParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Item getPartParent(LinkType linkType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Item getPartParent(LinkType linkType, boolean attemptToRecreate)
			throws CadseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Item getPartParent(ItemType itemType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Item getPartParentByName(String typeName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkType getPartParentLink() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkType getPartParentLinkType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getQualifiedDisplayName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getQualifiedName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getQualifiedName(boolean recompute) throws CadseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ItemState getState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ItemType getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int indexOf(Link link) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isAccessible() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAncestorOf(Item item) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isComposite() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isHidden() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isInIncomingLinks(Link l) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isInOutgoingLinks(Link l) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isInstanceOf(TypeDefinition it) {
		// TODO Auto-generated method stub
		return false;
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
	public boolean isOrphan() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPartItem() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequireNewRev() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isResolved() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRevModified() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRuntime() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean itemHasContent() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeContentItem() {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeListener(WorkspaceListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public Link removeOutgoingItem(LinkType linkType, Item destination)
			throws CadseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCadse(CadseRuntime cr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setKey(Key newkey) throws CadseException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public Link setOutgoingItem(LinkType linkType, Item dest)
			throws CadseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Link> setOutgoingItems(LinkType linkType,
			Collection<Item> value) throws CadseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setQualifiedName(String qualifiedName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setReadOnly(boolean readOnly) throws CadseException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setState(ItemState newState) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setValid(boolean isValid) {
		// TODO Auto-generated method stub

	}

	@Override
	public void shadow(boolean deleteContent) throws CadseException {
		// TODO Auto-generated method stub

	}

	@Override
	public void unload() throws CadseException {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> T getAttribute(IAttributeType<T> att) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getAttributeOwner(IAttributeType<T> att) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getAttributeWithDefaultValue(IAttributeType<T> att,
			T defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isTWAttributeModified(IAttributeType<?> att) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setAttribute(IAttributeType<?> att, Object value)
			throws CadseException {
		// TODO Auto-generated method stub

	}

	@Override
	public void getLocalAllAttributeTypes(
			List<IAttributeType<?>> allLocalAttrDefs) {
		// TODO Auto-generated method stub

	}

	@Override
	public void getLocalAllAttributeTypes(
			Map<String, IAttributeType<?>> allLocalAttrDefs,
			boolean keepLastAttribute) {
		// TODO Auto-generated method stub

	}

	@Override
	public void getLocalAllAttributeTypes(
			List<IAttributeType<?>> allLocalAttrDefs, ItemFilter filter) {
		// TODO Auto-generated method stub

	}

	@Override
	public IAttributeType<?>[] getLocalAllAttributeTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void getLocalAllAttributeTypes(
			Map<String, IAttributeType<?>> allLocalAttrDefs,
			boolean keepLastAttribute, ItemFilter filter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void getLocalAllAttributeTypesKeys(Set<String> allLocalAttrDefs,
			ItemFilter filter) {
		// TODO Auto-generated method stub

	}

	@Override
	public IAttributeType<?> getLocalAttributeType(String attrName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ContentItem _getContentItem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addIncomingLink(Link link, boolean notify) {
		// TODO Auto-generated method stub

	}

	@Override
	public Link commitLoadCreateLink(LinkType lt, Item destination)
			throws CadseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean commitMove(OrderWay kind, Link l1, Link l2)
			throws CadseException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean commitSetAttribute(IAttributeType<?> type, Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void computeAttribute(String attributeName, Object theirsValue,
			Object baseValue, Object mineValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void computeAttributes() {
		// TODO Auto-generated method stub

	}

	@Override
	public void forceState(ItemState state) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> T internalGetGenericOwnerAttribute(IAttributeType<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void loadItem(IWorkingLoadingItems wl, ItemDelta itemOperation,
			IErrorCollector errorCollector) throws CadseException {
		// TODO Auto-generated method stub

	}

	@Override
	public Iterator<Item> propagateValue(IAttributeType<?> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeIncomingLink(Link link, boolean notify) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeOutgoingLink(Link link, boolean notify) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean setFlag(int f, boolean flag) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setIsRuntime(boolean isStatic) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setParent(Item parent, LinkType lt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setType(ItemType itemType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVersion(int version) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(IWorkingLoadingItems items, ItemDelta desc,
			IWorkspaceNotifier notifie) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getIdInPackage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getShortName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setIdInPackage(int idInPackage) {
		// TODO Auto-generated method stub

	}

	@Override
	public Iterator<Item> propagateValue(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IAttributeType<?> getLocalAttributeType(UUID attrName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UUID getCadseId() {
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
	public boolean containsComponent(UUID itemIdentification) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<UUID> getComponentIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Item getComponentInfo(UUID id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Item> getComponents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Exporter[] getExporter(String exporterType) {
		// TODO Auto-generated method stub
		return null;
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

	@Override
	public boolean canInstantiateValue(IAttributeType<?> attr) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DelegateValue getDelegateValue(IAttributeType<?> attr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDelegatedValue(IAttributeType<?> attr) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public Composer[] getComposers() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented method");
	}
}
