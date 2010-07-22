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
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.EventFilter;
import fr.imag.adele.cadse.core.ILinkTypeManager;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemFilter;
import fr.imag.adele.cadse.core.ItemState;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.WorkspaceListener;
import fr.imag.adele.cadse.core.attribute.CheckStatus;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.content.ContentItem;
import fr.imag.adele.cadse.core.internal.IWorkingLoadingItems;
import fr.imag.adele.cadse.core.internal.IWorkspaceNotifier;
import fr.imag.adele.cadse.core.key.Key;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransactionListener;
import fr.imag.adele.cadse.core.transaction.delta.ImmutableWorkspaceDelta;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.util.IErrorCollector;
import fr.imag.adele.cadse.objectadapter.ObjectAdapter;
import fr.imag.adele.cadse.util.OrderWay;

public class LinkTypeItemDeltaAdapter extends ItemItemDeltaAdapter implements LinkType {

	public LinkTypeItemDeltaAdapter(ItemDelta itemDelta) {
		super(itemDelta);
	}

	@Override
	public ItemType getDestination() {
		return _accessor(CadseGCST.LINK_TYPE_lt_DESTINATION, ItemType.class);
	}

	private <T> T _accessor(LinkType lt, Class<T> clazz) {
		ItemDelta destDelta = _delta.getOutgoingItem(lt, true);
		if (destDelta == null) {
			return null;
		}
		return destDelta.getAdapter(clazz);
	}

	@Override
	public LinkType getInverse() {
		return _accessor(CadseGCST.LINK_TYPE_lt_INVERSE_LINK, LinkType.class);
	}

	@Override
	public int getKind() {
		return _delta.getIntAttribut(CadseGCST.LINK_TYPE_at_KIND_, 0);
	}

	@Override
	public IAttributeType<?> getLinkTypeAttributeType(String attrName) {
		return null;
	}

	@Override
	public ILinkTypeManager getManager() {
		return null;
	}

	@Override
	public int getMax() {
		return _delta.getIntAttribut(CadseGCST.LINK_TYPE_at_MAX_, 0);
	}

	@Override
	public int getMin() {
		return _delta.getIntAttribut(CadseGCST.LINK_TYPE_at_MIN_, 0);
	}

	@Override
	public String getName() {
		return _delta.getName();
	}

	@Override
	public Collection<Item> getSelectingDestination(Item source) {
		return null;
	}

	@Override
	public ItemType getSource() {
		ItemType ret = _accessor(CadseGCST.LINK_TYPE_lt_SOURCE, ItemType.class);
		if (ret == null) {
			if (_delta.getPartParent() != null) {
				return _delta.getPartParent().getAdapter(ItemType.class);
			}
		}
		return ret;
	}

	@Override
	public boolean isAggregation() {
		return _delta.getAttribute(CadseGCST.LINK_TYPE_at_AGGREGATION_, true);
	}

	@Override
	public boolean isAnnotation() {
		return _delta.getAttribute(CadseGCST.LINK_TYPE_at_ANNOTATION_, true);
	}

	@Override
	public boolean isComposition() {
		return _delta.getAttribute(CadseGCST.LINK_TYPE_at_COMPOSITION_, true);
	}

	@Override
	public boolean isInversePart() {
		return false;
	}

	@Override
	public boolean isNonCircular() {
		return false;
	}

	@Override
	public boolean isOrdered() {
		return false;
	}

	@Override
	public boolean isPart() {
		return _delta.getAttribute(CadseGCST.LINK_TYPE_at_PART_, true);
	}

	@Override
	public boolean isRequire() {
		return _delta.getAttribute(CadseGCST.LINK_TYPE_at_REQUIRE_, true);
	}

	@Override
	public void setInverseLinkType(LinkType lt) {
		try {
			_delta.setOutgoingItem(CadseGCST.LINK_TYPE_lt_INVERSE_LINK, lt);
		}
		catch (CadseException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void setManager(ILinkTypeManager manager) {
	}

	@Override
	public boolean canBeUndefined() {
		return false;
	}

	@Override
	public boolean cannotBeUndefined() {
		return false;
	}

	@Override
	public CheckStatus check(Item item, Object value) {
		return null;
	}

	@Override
	public Link convertTo(Object value) {
		return null;
	}

	@Override
	public Object createNewValueFor(Item createdItem) throws CadseException {
		return null;
	}

	@Override
	public Class<Link> getAttributeType() {
		return null;
	}

	@Override
	public Link getDefaultValue() {
		return null;
	}

	@Override
	public Item getParent() {
		return null;
	}

	@Override
	public boolean isNatif() {
		return false;
	}

	@Override
	public boolean isTWRevSpecific() {
		return false;
	}

	@Override
	public boolean isTWValueModified(Object oldValue, Object newValue) {
		return false;
	}

	@Override
	public boolean isTransient() {
		return false;
	}

	@Override
	public boolean isValueModified(Object oldValue, Object newValue) {
		return false;
	}

	@Override
	public boolean mustBeCreateNewValueAtCreationTimeOfItem() {
		return false;
	}

	@Override
	public boolean setIsNatif(boolean isNatif) {
		return false;
	}

	@Override
	public void addListener(WorkspaceListener listener, int eventFilter) {
	}

	@Override
	public void addListener(WorkspaceListener listener, EventFilter eventFilter) {
	}

	@Override
	public Link addOutgoingItem(LinkType linkType, Item destination) throws CadseException {
		return null;
	}

	@Override
	public void buildComposite() throws CadseException {
	}

	@Override
	public boolean canCreateLink(LinkType linkType, UUID destItemId) {
		return false;
	}

	@Override
	public boolean canEditContent(String slashedPath) {
		return false;
	}

	@Override
	public boolean canSetAttribute(String attrName, Object value) {
		return false;
	}

	@Override
	public boolean containsPartChild(Item item) {
		return false;
	}

	@Override
	public boolean contentIsLoaded() {
		return false;
	}

	@Override
	public Link createLink(LinkType linkType, Item destItem) throws CadseException {
		return null;
	}

	@Override
	public void delete(boolean deleteContent) throws CadseException {
	}

	@Override
	public List<WorkspaceListener> filter(int eventFilter, ImmutableWorkspaceDelta delta) {
		return null;
	}

	@Override
	public void finishLoad() {
	}

	@Override
	public List<Link> getAggregations() {
		return null;
	}

	@Override
	public ContentItem getAndCreateContentManager() {
		return null;
	}

	@Override
	public Item getBaseItem() {
		return null;
	}

	@Override
	public CadseDomain getCadseDomain() {
		return null;
	}

	@Override
	public ContentItem getContentItem() {
		return null;
	}

	@Override
	public String getDisplayName() {
		return null;
	}

	@Override
	public UUID getId() {
		return _delta.getId();
	}

	@Override
	public Item getIncomingItem(LinkType linkType) {
		return null;
	}

	@Override
	public Collection<Item> getIncomingItems(LinkType linkType) {
		return null;
	}

	@Override
	public Collection<Item> getIncomingItems() {
		return null;
	}

	@Override
	public Link getIncomingLink(LinkType linkType, UUID srcId) {
		return null;
	}

	@Override
	public List<Link> getIncomingLinks() {
		return null;
	}

	@Override
	public List<Link> getIncomingLinks(LinkType linkType) {
		return null;
	}

	@Override
	public Key getKey() {
		return null;
	}

	@Override
	public int getLastVersion() {
		return 0;
	}

	@Override
	public File getLocation() {
		return null;
	}

	@Override
	public LogicalWorkspace getLogicalWorkspace() {
		return null;
	}

	@Override
	public <T> T getMainMappingContent(Class<T> clazz) {
		return null;
	}

	@Override
	public <T> List<T> getMappingContents(Class<T> clazz) {
		return null;
	}

	@Override
	public List<?> getMappingContents() {
		return null;
	}

	@Override
	public Item getOutgoingItem(String linkTypeName, UUID itemId, boolean resolvedOnly) {
		return null;
	}

	@Override
	public Item getOutgoingItem(LinkType lt, boolean resolvedOnly) {
		return null;
	}

	@Override
	public Item getOutgoingItem(String linkTypeName, boolean resolvedOnly) {
		return null;
	}

	@Override
	public Collection<Item> getOutgoingItems(LinkType lt, boolean resolvedOnly) {
		return null;
	}

	@Override
	public Collection<Item> getOutgoingItems(boolean resolvedOnly) {
		return null;
	}

	@Override
	public Collection<Item> getOutgoingItems(String typesLink, boolean resolvedOnly) {
		return null;
	}

	@Override
	public Link getOutgoingLink(LinkType linkType) {
		return null;
	}

	@Override
	public Link getOutgoingLink(Item destItem) {
		return null;
	}

	@Override
	public Link getOutgoingLink(LinkType lt, UUID destId) {
		return null;
	}

	@Override
	public List<Link> getOutgoingLinks() {
		return null;
	}

	@Override
	public List<Link> getOutgoingLinks(LinkType linkType) {
		return null;
	}

	@Override
	public Item getPartChild(UUID destItemId) {
		return null;
	}

	@Override
	public Collection<Item> getPartChildren() {
		return null;
	}

	@Override
	public Collection<Item> getPartChildren(LinkType linkType) {
		return null;
	}

	@Override
	public Item getPartParent(boolean attemptToRecreate) {
		return null;
	}

	@Override
	public Item getPartParent() {
		return null;
	}

	@Override
	public Item getPartParent(LinkType linkType) {
		return null;
	}

	@Override
	public Item getPartParent(LinkType linkType, boolean attemptToRecreate) throws CadseException {
		return null;
	}

	@Override
	public Item getPartParent(ItemType itemType) {
		return null;
	}

	@Override
	public Item getPartParentByName(String typeName) {
		return null;
	}

	@Override
	public LinkType getPartParentLink() {
		return null;
	}

	@Override
	public LinkType getPartParentLinkType() {
		return null;
	}

	@Override
	public String getQualifiedDisplayName() {
		return null;
	}

	@Override
	public String getQualifiedName() {
		return null;
	}

	@Override
	public String getQualifiedName(boolean recompute) throws CadseException {
		return null;
	}

	@Override
	public ItemState getState() {
		return null;
	}

	@Override
	public ItemType getType() {
		return null;
	}

	@Override
	public int getVersion() {
		return 0;
	}

	@Override
	public int indexOf(Link link) {
		return 0;
	}

	@Override
	public boolean isAncestorOf(Item item) {
		return false;
	}

	@Override
	public boolean isComposite() {
		return false;
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public boolean isInIncomingLinks(Link l) {
		return false;
	}

	@Override
	public boolean isInOutgoingLinks(Link l) {
		return false;
	}

	@Override
	public boolean isInstanceOf(ItemType it) {
		return false;
	}

	@Override
	public boolean isOrphan() {
		return false;
	}

	@Override
	public boolean isPartItem() {
		return false;
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public boolean isRequireNewRev() {
		return false;
	}

	@Override
	public boolean isResolved() {
		return false;
	}

	@Override
	public boolean isRevModified() {
		return false;
	}

	@Override
	public boolean isRuntime() {
		return false;
	}

	@Override
	public boolean isValid() {
		return false;
	}

	@Override
	public boolean itemHasContent() {
		return false;
	}

	@Override
	public void removeContentItem() {
	}

	@Override
	public void removeListener(WorkspaceListener listener) {
	}

	@Override
	public Link removeOutgoingItem(LinkType linkType, Item destination) throws CadseException {
		return null;
	}

	@Override
	public void setKey(Key newkey) {
	}

	@Override
	public Link setOutgoingItem(LinkType linkType, Item dest) throws CadseException {
		return null;
	}

	@Override
	public Collection<Link> setOutgoingItems(LinkType linkType, Collection<Item> value) throws CadseException {
		return null;
	}

	@Override
	public void setReadOnly(boolean readOnly) {
	}

	@Override
	public void setState(ItemState newState) {
	}

	@Override
	public void setValid(boolean isValid) {
	}

	@Override
	public void shadow(boolean deleteContent) throws CadseException {
	}

	@Override
	public void unload() throws CadseException {
	}

	@Override
	public <T> T getAttribute(IAttributeType<T> att) {
		return null;
	}

	@Override
	public <T> T getAttributeOwner(IAttributeType<T> att) {
		return null;
	}

	@Override
	public <T> T getAttributeWithDefaultValue(IAttributeType<T> att, T defaultValue) {
		return null;
	}

	@Override
	public boolean isTWAttributeModified(IAttributeType<?> att) {
		return false;
	}

	@Override
	public void setAttribute(IAttributeType<?> att, Object value) throws CadseException {
	}

	@Override
	public void getLocalAllAttributeTypes(List<IAttributeType<?>> allLocalAttrDefs) {
	}

	@Override
	public void getLocalAllAttributeTypes(Map<String, IAttributeType<?>> allLocalAttrDefs, boolean keepLastAttribute) {
	}

	@Override
	public void getLocalAllAttributeTypes(List<IAttributeType<?>> allLocalAttrDefs, ItemFilter filter) {
	}

	@Override
	public IAttributeType<?>[] getLocalAllAttributeTypes() {
		return null;
	}

	@Override
	public void getLocalAllAttributeTypes(Map<String, IAttributeType<?>> allLocalAttrDefs, boolean keepLastAttribute,
			ItemFilter filter) {
	}

	@Override
	public void getLocalAllAttributeTypesKeys(Set<String> allLocalAttrDefs, ItemFilter filter) {
	}

	@Override
	public IAttributeType<?> getLocalAttributeType(String attrName) {
		return null;
	}

	@Override
	public ContentItem _getContentItem() {
		return null;
	}

	@Override
	public void addIncomingLink(Link link, boolean notify) {
	}

	@Override
	public Link commitLoadCreateLink(LinkType lt, Item destination) throws CadseException {
		return null;
	}

	@Override
	public boolean commitMove(OrderWay kind, Link l1, Link l2) throws CadseException {
		return false;
	}

	@Override
	public boolean commitSetAttribute(IAttributeType<?> type, Object value) {
		return false;
	}

	@Override
	public void computeAttribute(String attributeName, Object theirsValue, Object baseValue, Object mineValue) {
	}

	@Override
	public void computeAttributes() {
	}

	@Override
	public void forceState(ItemState state) {
	}

	@Override
	public <T> T internalGetGenericOwnerAttribute(IAttributeType<T> type) {
		return null;
	}

	@Override
	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {
		return null;
	}

	@Override
	public void loadItem(IWorkingLoadingItems wl, ItemDelta itemOperation, IErrorCollector errorCollector)
			throws CadseException {
	}

	@Override
	public Iterator<Item> propagateValue(IAttributeType<?> type) {
		return null;
	}

	@Override
	public Iterator<Item> propagateValue(String key) {
		return null;
	}

	@Override
	public void refresh() {
	}

	@Override
	public void removeIncomingLink(Link link, boolean notify) {
	}

	@Override
	public void removeOutgoingLink(Link link, boolean notify) {
	}

	@Override
	public boolean setFlag(int f, boolean flag) {
		return false;
	}

	@Override
	public void setIsRuntime(boolean isStatic) {
	}

	@Override
	public void setParent(Item parent, LinkType lt) {
	}

	@Override
	public void setType(ItemType itemType) {
	}

	@Override
	public void setVersion(int version) {
	}

	@Override
	public void update(IWorkingLoadingItems items, ItemDelta desc, IWorkspaceNotifier notifie) {
	}

	@Override
	public void addCompatibleVersions(int... versions) {
	}

	@Override
	public void clearCompatibleVersions() {
	}

	@Override
	public void delete() throws CadseException {
	}

	@Override
	public int[] getCompatibleVersions() {
		return null;
	}

	@Override
	public Item getDestination(boolean mustBeResolved) {
		return null;
	}

	@Override
	public UUID getDestinationId() {
		return null;
	}

	@Override
	public String getDestinationName() {
		return null;
	}

	@Override
	public String getDestinationQualifiedName() {
		return null;
	}

	@Override
	public ItemType getDestinationType() {
		return null;
	}

	@Override
	public int getIndex() {
		return 0;
	}

	@Override
	public LinkType getLinkType() {
		return null;
	}

	@Override
	public Item getResolvedDestination() {
		return null;
	}

	@Override
	public UUID getSourceId() {
		return null;
	}

	@Override
	public boolean isDerived() {
		return false;
	}

	@Override
	public boolean isLinkResolved() {
		return false;
	}

	@Override
	public boolean resolve() {
		return false;
	}

	@Override
	public void setHidden(boolean hidden) {
	}

	@Override
	public void commitDelete() throws CadseException {
	}

	@Override
	public void destroy() throws CadseException {
	}

	@Override
	public String getCSTName() {
		return null;
	}

	@Override
	public void setCSTName(String cst) {
	}

	@Override
	public void setIsGroup(boolean b) {
	}

	@Override
	public boolean isGroup() {
		return false;
	}

	@Override
	public boolean commitSetAttribute(IAttributeType<?> type, String key, Object value) {
		return false;
	}

	@Override
	public UIField generateDefaultField() {
		return null;
	}

	@Override
	public IAttributeType<?>[] getChildren() {
		return null;
	}

	@Override
	public boolean isFinal() {
		return false;
	}

	@Override
	public void setFinal(boolean flag) {
	}

	@Override
	public UUID getDestinationCadseId() {
		return null;
	}

	@Override
	public UUID getSourceCadseId() {
		return null;
	}

	@Override
	public boolean isInterCadseLink() {
		return false;
	}

	@Override
	public IAttributeType<?>[] getLinkTypeAttributeTypes() {
		return null;
	}

	@Override
	public <T> T getLinkAttributeOwner(IAttributeType<T> attDef) {
		return null;
	}

	@Override
	public <T extends ObjectAdapter<T>> T adapt(Class<T> clazz) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	@Override
	public <T extends ObjectAdapter<T>> T[] adapts(Class<T> clazz) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void addLogicalWorkspaceTransactionListener(LogicalWorkspaceTransactionListener listener) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void removeLogicalWorkspaceTransactionListener(LogicalWorkspaceTransactionListener listener) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public LogicalWorkspaceTransactionListener[] getLogicalWorkspaceTransactionListener() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isAttributeHead() {
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public boolean isAttributeMember() {
		return false;
	}

	@Override
	public boolean isShowInDefaultCP() {
		return false;
	}

	@Override
	public boolean isShowInDefaultMP() {
		return false;
	}

	@Override
	public void setShowInDefaultCP(boolean flag) {
	}

	@Override
	public void setShowInDefaultMP(boolean flag) {
	}

	@Override
	public boolean isNotEditableInCP() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public boolean isNotEditableInMP() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented method");
	}
}
