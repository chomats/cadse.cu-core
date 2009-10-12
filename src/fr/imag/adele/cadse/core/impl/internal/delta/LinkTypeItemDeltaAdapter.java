package fr.imag.adele.cadse.core.impl.internal.delta;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.imag.adele.cadse.core.CadseDomain;
import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.ContentItem;
import fr.imag.adele.cadse.core.DerivedLink;
import fr.imag.adele.cadse.core.DerivedLinkDescription;
import fr.imag.adele.cadse.core.EventFilter;
import fr.imag.adele.cadse.core.ILinkTypeManager;
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
import fr.imag.adele.cadse.core.attribute.CheckStatus;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.delta.ImmutableWorkspaceDelta;
import fr.imag.adele.cadse.core.delta.ItemDelta;
import fr.imag.adele.cadse.core.internal.IWorkingLoadingItems;
import fr.imag.adele.cadse.core.internal.IWorkspaceNotifier;
import fr.imag.adele.cadse.core.key.ISpaceKey;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransactionListener;
import fr.imag.adele.cadse.core.util.IErrorCollector;
import fr.imag.adele.cadse.core.util.OrderWay;

public class LinkTypeItemDeltaAdapter extends ItemItemDeltaAdapter implements LinkType {

	public LinkTypeItemDeltaAdapter(ItemDelta itemDelta) {
		super(itemDelta);
	}

	@Override
	public ItemType getDestination() {
		return _accessor(CadseGCST.LINK_lt_DESTINATION, ItemType.class);
	}

	private <T> T _accessor(LinkType lt, Class<T> clazz) {
		ItemDelta destDelta = _delta.getOutgoingItem(lt, true);
		if (destDelta == null)
			return null;
		return destDelta.getAdapter(clazz);
	}

	@Override
	public LinkType getInverse() {
		return _accessor(CadseGCST.LINK_lt_INVERSE_LINK, LinkType.class);
	}

	@Override
	public int getKind() {
		return _delta.getIntAttribut(CadseGCST.LINK_at_KIND_, 0);
	}

	@Override
	public IAttributeType<?> getLinkTypeAttributeType(String attrName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILinkTypeManager getManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMax() {
		return _delta.getIntAttribut(CadseGCST.LINK_at_MAX_, 0);
	}

	@Override
	public int getMin() {
		return _delta.getIntAttribut(CadseGCST.LINK_at_MIN_, 0);
	}

	@Override
	public String getName() {
		return _delta.getName();
	}

	@Override
	public Collection<Item> getSelectingDestination(Item source) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ItemType getSource() {
		ItemType ret = _accessor(CadseGCST.LINK_lt_SOURCE, ItemType.class);
		if (ret == null) {
			if (_delta.getPartParent() != null)
				return _delta.getPartParent().getAdapter(ItemType.class);
		}
		return ret;
	}

	@Override
	public boolean isAggregation() {
		return _delta.getAttribute(CadseGCST.LINK_at_AGGREGATION_, true);
	}

	@Override
	public boolean isAnnotation() {
		return _delta.getAttribute(CadseGCST.LINK_at_ANNOTATION_, true);
	}

	@Override
	public boolean isComposition() {
		return _delta.getAttribute(CadseGCST.LINK_at_COMPOSITION_, true);
	}

	@Override
	public boolean isInversePart() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isNonCircular() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOrdered() {
		return false;
	}

	@Override
	public boolean isPart() {
		return _delta.getAttribute(CadseGCST.LINK_at_PART_, true);
	}

	@Override
	public boolean isRequire() {
		return _delta.getAttribute(CadseGCST.LINK_at_REQUIRE_, true);
	}

	@Override
	public void setInverseLinkType(LinkType lt) {
		try {
			_delta.setOutgoingItem(CadseGCST.LINK_lt_INVERSE_LINK, lt);
		} catch (CadseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void setManager(ILinkTypeManager manager) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canBeUndefined() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean cannotBeUndefined() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public CheckStatus check(Item item, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object convertTo(Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object createNewValueFor(Item createdItem) throws CadseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<Link> getAttributeType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Link getDefaultValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Item getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isNatif() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isTWRevSpecific() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isTWValueModified(Object oldValue, Object newValue) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isTransient() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isValueModified(Object oldValue, Object newValue) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mustBeCreateNewValueAtCreationTimeOfItem() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mustBeInitializedAtCreationTime() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setIsNatif(boolean isNatif) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addListener(WorkspaceListener listener, int eventFilter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addListener(WorkspaceListener listener, EventFilter eventFilter) {
		// TODO Auto-generated method stub

	}

	@Override
	public Link addOutgoingItem(LinkType linkType, Item destination)
			throws CadseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void buildComposite() throws CadseException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canCreateLink(LinkType linkType, CompactUUID destItemId) {
		// TODO Auto-generated method stub
		return false;
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
	public boolean containsComponent(CompactUUID itemId) {
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
	public List<WorkspaceListener> filter(int eventFilter,
			ImmutableWorkspaceDelta delta) {
		// TODO Auto-generated method stub
		return null;
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
	public CadseDomain getCadseDomain() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<CompactUUID> getComponentIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Item getComponentInfo(CompactUUID itemId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Item> getComponents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Item> getCompositeParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ContentItem getContentItem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<DerivedLinkDescription> getDerivedLinkDescriptions(
			ItemDescription source) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<DerivedLink> getDerivedLinks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompactUUID getId() {
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
	public Link getIncomingLink(LinkType linkType, CompactUUID srcId) {
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
	public ISpaceKey getKey() {
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
	public Item getOutgoingItem(String linkTypeName, CompactUUID itemId,
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
	public Link getOutgoingLink(LinkType lt, CompactUUID destId) {
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
	public Item getPartChild(CompactUUID destItemId) {
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
	public String getShortName() {
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
	public String getUniqueName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUniqueName(boolean recompute) throws CadseException {
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
	public boolean isInstanceOf(ItemType it) {
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
	public boolean isRequierNewRev() {
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
	public boolean isStatic() {
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
	public void setComponents(Set<ItemDescriptionRef> comp)
			throws CadseException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDerivedLinks(Set<DerivedLinkDescription> derivedLinks) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setKey(ISpaceKey newkey) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setName(String name) throws CadseException {
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
	public void setQualifiedName(String qualifiedName) throws CadseException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setReadOnly(boolean readOnly)  {
		// TODO Auto-generated method stub

	}

	@Override
	public void setShortName(String name) throws CadseException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setState(ItemState newState) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setUniqueName(String qualifiedName) throws CadseException {
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
	public <T> T getAttribute(String att) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getAttributeH(String att, boolean fromSuperIfNull) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getAttributeKeys() {
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
	public <T> T getAttributeWithDefaultValue(String att, T defaultValue) {
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
	public void setAttribute(String att, Object value) throws CadseException {
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
	public boolean commitSetAttribute(IAttributeType<?> type, String key,
			Object value) {
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
	public <T> T internalGetGenericOwnerAttribute(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T internalGetGenericOwnerAttribute(IAttributeType<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T internalGetOwnerAttribute(String key) {
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
	public Iterator<Item> propagateValue(String key) {
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
	public void setIsStatic(boolean isStatic) {
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
	public void addCompatibleVersions(int... versions) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearCompatibleVersions() {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete() throws CadseException {
		// TODO Auto-generated method stub

	}

	@Override
	public int[] getCompatibleVersions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Item getDestination(boolean mustBeResolved) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompactUUID getDestinationId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDestinationName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDestinationQualifiedName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDestinationShortName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ItemType getDestinationType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public LinkType getLinkType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Item getResolvedDestination() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompactUUID getSourceId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDerived() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLinkResolved() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean resolve() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void restore() throws CadseException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setHidden(boolean hidden) {
		// TODO Auto-generated method stub

	}

	@Override
	public void commitDelete() throws CadseException {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() throws CadseException {
		// TODO Auto-generated method stub

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
	public String getCSTName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCSTName(String cst) {
		// TODO Auto-generated method stub
		
	}

}
