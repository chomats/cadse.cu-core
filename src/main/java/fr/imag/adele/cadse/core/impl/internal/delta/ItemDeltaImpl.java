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
package fr.imag.adele.cadse.core.impl.internal.delta;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import fr.imag.adele.cadse.core.CadseDomain;
import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.CadseRuntime;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.ContentChangeInfo;
import fr.imag.adele.cadse.core.DerivedLink;
import fr.imag.adele.cadse.core.DerivedLinkDescription;
import fr.imag.adele.cadse.core.EventFilter;
import fr.imag.adele.cadse.core.ExtendedType;
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
import fr.imag.adele.cadse.core.Messages;
import fr.imag.adele.cadse.core.TypeDefinition;
import fr.imag.adele.cadse.core.WorkspaceListener;
import fr.imag.adele.cadse.core.attribute.BooleanAttributeType;
import fr.imag.adele.cadse.core.attribute.DelegateValue;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.attribute.IntegerAttributeType;
import fr.imag.adele.cadse.core.attribute.StringAttributeType;
import fr.imag.adele.cadse.core.attribute.URLAttributeType;
import fr.imag.adele.cadse.core.build.Composer;
import fr.imag.adele.cadse.core.build.Exporter;
import fr.imag.adele.cadse.core.build.IBuildingContext;
import fr.imag.adele.cadse.core.content.ContentItem;
import fr.imag.adele.cadse.core.impl.CadseIllegalArgumentException;
import fr.imag.adele.cadse.core.impl.PageRuntimeModel;
import fr.imag.adele.cadse.core.impl.internal.AbstractGeneratedItem;
import fr.imag.adele.cadse.core.impl.internal.Accessor;
import fr.imag.adele.cadse.core.impl.internal.ItemTypeImpl;
import fr.imag.adele.cadse.core.impl.internal.LogicalWorkspaceImpl;
import fr.imag.adele.cadse.core.impl.internal.LogicalWorkspaceTransactionImpl;
import fr.imag.adele.cadse.core.internal.IWorkingLoadingItems;
import fr.imag.adele.cadse.core.internal.IWorkspaceNotifier;
import fr.imag.adele.cadse.core.key.DefaultKeyImpl;
import fr.imag.adele.cadse.core.key.Key;
import fr.imag.adele.cadse.core.key.KeyDefinition;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransaction;
import fr.imag.adele.cadse.core.transaction.delta.DeleteOperation;
import fr.imag.adele.cadse.core.transaction.delta.ImmutableWorkspaceDelta;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.transaction.delta.LinkDelta;
import fr.imag.adele.cadse.core.transaction.delta.LinkKey;
import fr.imag.adele.cadse.core.transaction.delta.MappingOperation;
import fr.imag.adele.cadse.core.transaction.delta.OperationTypeCst;
import fr.imag.adele.cadse.core.transaction.delta.OrderOperation;
import fr.imag.adele.cadse.core.transaction.delta.SetAttributeOperation;
import fr.imag.adele.cadse.core.transaction.delta.WLWCOperation;
import fr.imag.adele.cadse.core.ui.Pages;
import fr.imag.adele.cadse.core.ui.view.FilterContext;
import fr.imag.adele.cadse.core.ui.view.NewContext;
import fr.imag.adele.cadse.core.util.Convert;
import fr.imag.adele.cadse.core.util.IErrorCollector;
import fr.imag.adele.cadse.util.ArraysUtil;
import fr.imag.adele.cadse.util.Assert;
import fr.imag.adele.cadse.util.OrderWay;

public class ItemDeltaImpl extends ItemOrLinkDeltaImpl implements ItemDelta {
	static Map<Class<?>, ItemDeltaAdapterFactory<?>> _registerAdapter = new HashMap<Class<?>, ItemDeltaAdapterFactory<?>>();
	static {
		_registerAdapter.put(ItemType.class,
				new ItemTypeItemDeltaAdapterFactory());
		_registerAdapter.put(LinkType.class,
				new LinkTypeItemDeltaAdapterFactory());
		_registerAdapter.put(CadseRuntime.class,
				new CadseRuntimeItemDeltaAdapterFactory());
	}

	public static void aRefx(MappingOperation a, MappingOperation x) {
		if (a == null) {
			return;
		}
		if (x == null) {
			return;
		}

		MappingOperation[] beforeOperation = a.getBeforeOperation();
		if (beforeOperation == null || beforeOperation.length == 1
				|| ArraysUtil.indexOf(beforeOperation, x) == -1) {
			a.addBeforeMappingOperation(x);
		}
	}

	private int _localId = -1;
	private Map<LinkKey, LinkDelta> _links = null;
	private LinkDelta[] _incomingLinks = null;
	private List<LinkDelta> _orders = null;
	private List<OrderOperation> _ordersOperation = null;
	private MappingOperation[] _mappings = null;

	private ItemDelta _oldParentItem = null;
	private LinkType _oldParentLinkType;

	private ItemDelta _parentItem = null;
	private LinkType _parentLinkType;

	private final LogicalWorkspaceTransactionImpl _copy;

	/** The id. */
	private UUID _id;

	/** The type. */
	private ItemType _itemType;
	private ItemType[] _types;
	private boolean _update;
	private boolean _finishLoad;
	private boolean _doubleClick;
	private Key _key;
	private SpaceKeyDeltaImpl _keyDelta;
	private Item _baseItem;
	public Item _realItem;
	
	private Key _nextKey;
	private ItemType _group;
	private int _parentID;
	private int _cadseID;
	private CadseRuntime _cadse;

	public ItemDeltaImpl(LogicalWorkspaceTransactionImpl copy, UUID id,
			boolean add) {
		super(OperationTypeCst.ITEM_OPERATION, null);
		Assert.isNotNull(id);
		this._copy = copy;
		this._id = id;
		if (add) {
			addInParent();
		}

	}

	public ItemDeltaImpl(LogicalWorkspaceTransactionImpl copy, UUID id,
			ItemType itemType, boolean add) throws CadseException {
		super(OperationTypeCst.ITEM_OPERATION, null);
		Assert.isNotNull(id);
		Assert.isNotNull(itemType);
		this._copy = copy;
		this._id = id;
		this._itemType = itemType;

		addItemType(itemType);
		if (add) {
			addInParent();
		}
	}

	public ItemDeltaImpl(LogicalWorkspaceTransactionImpl copy, Item original,
			boolean add) {
		super(OperationTypeCst.ITEM_OPERATION, null);
		Assert.isNotNull(original);
		Assert.isNotNull(original.getId());
		Assert.isNotNull(original.getType());
		this._copy = copy;
		this._id = original.getId();
		this._itemType = original.getType();
		this._localId = original.getObjectId();
		addItemType(original.getType());
		this._baseItem = original;
		this._group = original.getGroup();
		Item partParent = original.getPartParent();
		if (partParent != null)
			this._parentItem = copy.getItem(partParent);
		if (add) {
			addInParent();
		}
	}
	
	@Override
	public Item getRealItem() {
		return _realItem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#addIncomingLink(fr.imag
	 * .adele.cadse.core.Link, boolean)
	 */
	public void addIncomingLink(Link linkImpl, boolean notifie) {
		_incomingLinks = ArraysUtil.add(LinkDelta.class, _incomingLinks,
				(LinkDelta) linkImpl);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#addInParent()
	 */
	@Override
	public void addInParent() {
		_copy.actionAddOperation(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#addListener(fr.imag.adele
	 * .cadse.core.WorkspaceListener, fr.imag.adele.cadse.core.EventFilter)
	 */
	public void addListener(WorkspaceListener l, EventFilter eventFilter) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#addListener(fr.imag.adele
	 * .cadse.core.WorkspaceListener, int)
	 */
	public void addListener(WorkspaceListener l, int eventFilter) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#addMappingOperaion(fr
	 * .imag.adele.cadse.core.delta.MappingOperation)
	 */
	public void addMappingOperaion(MappingOperation mappingOperation)
			throws CadseException {
		this._mappings = ArraysUtil.add(MappingOperation.class, this._mappings,
				mappingOperation);
		mappingOperation.addInParent();
		_copy.notifyAddMappingOperation(this, mappingOperation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#addOutgoingItem(fr.imag
	 * .adele.cadse.core.LinkType, fr.imag.adele.cadse.core.Item)
	 */
	public LinkDelta addOutgoingItem(LinkType lt, Item destination)
			throws CadseException {
		getWorkingCopy().check_write();
		return createLink(lt, destination);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#buildComposite()
	 */
	@Deprecated
	public void buildComposite() throws CadseException {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#canCreateLink(fr.imag
	 * .adele.cadse.core.LinkType, fr.imag.adele.cadse.core.UUID)
	 */
	public boolean canCreateLink(LinkType lt, UUID destination) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#canEditeContent(java.
	 * lang.String)
	 */
	public boolean canEditContent(String slashedPath) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#canSetAttribute(java.
	 * lang.String, java.lang.Object)
	 */
	public boolean canSetAttribute(String key, Object value) {
		return true;
	}

	private void checkParamMove(LinkDelta linkOne, Link linkTwo_,
			LinkDelta linkTwo) {
		if (linkOne == null) {
			throw new CadseIllegalArgumentException(
					Messages.parameter_link_one_is_null);
		}
		if (linkTwo == null) {
			throw new CadseIllegalArgumentException(
					Messages.parameter_link_two_is_null, linkTwo_);
		}
		if (linkOne.isDeleted()) {
			throw new CadseIllegalArgumentException(
					Messages.parameter_link_one_is_deleted, linkOne);
		}
		if (linkTwo.isDeleted()) {
			throw new CadseIllegalArgumentException(
					Messages.parameter_link_two_id_deleted, linkTwo);
		}
		if (linkOne.getSource() != linkTwo.getSource()) {
			throw new CadseIllegalArgumentException(
					Messages.link_has_not_same_source);
		}
		if (linkOne.getLinkType() != null
				&& linkTwo.getLinkType() != linkOne.getLinkType()) {
			throw new CadseIllegalArgumentException(
					Messages.link_has_not_same_link_type_or_null, linkOne
							.getLinkType(), linkTwo.getLinkType());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#commitLoadCreateLink(
	 * fr.imag.adele.cadse.core.LinkType, fr.imag.adele.cadse.core.Item)
	 */
	public Link commitLoadCreateLink(LinkType lt, Item destination) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#commitMove(fr.imag.adele
	 * .cadse.core.util.OrderWay, fr.imag.adele.cadse.core.Link,
	 * fr.imag.adele.cadse.core.Link)
	 */
	public boolean commitMove(OrderWay kind, Link l1, Link l2)
			throws CadseException {
		if (kind == OrderWay.move_after) {
			return moveAfter(getOutgoingLinkOperation(l1), l2);
		}
		if (kind == OrderWay.move_before) {
			return moveBefore(getOutgoingLinkOperation(l1), l2);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#commitSetAttribute(fr
	 * .imag.adele.cadse.core.attribute.IAttributeType, java.lang.String,
	 * java.lang.Object)
	 */
	public boolean commitSetAttribute(IAttributeType<?> type, Object value) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#computeAttribute(java
	 * .lang.String, java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	public void computeAttribute(String attributeName, Object theirsValue,
			Object baseValue, Object mineValue) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#computeAttributes()
	 */
	public void computeAttributes() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#containsComponent(fr.
	 * imag.adele.cadse.core.UUID)
	 */
	@Deprecated
	public boolean containsComponent(UUID id) {
		return false;
		// throw new UnsupportedOperationException();
		/*
		 * TODO implements java.lang.UnsupportedOperationException at
		 * fr.imag.adele
		 * .cadse.core.delta.ItemOperation.containsComponent(ItemOperation
		 * .java:236) at
		 * fr.imag.adele.cadse.core.internal.WorkspaceLogique.constraints_DestItem
		 * (WorkspaceLogique.java:2270) at
		 * fr.imag.adele.cadse.core.internal.WorkspaceLogique
		 * .preconditions_createLink(WorkspaceLogique.java:2080) at
		 * fr.imag.adele
		 * .cadse.core.internal.WorkspaceLogique.commit(WorkspaceLogique
		 * .java:2371)
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#containsPart(fr.imag.
	 * adele.cadse.core.Item)
	 */
	public boolean containsPartChild(Item item) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#contentIsChanged()
	 */
	@Deprecated
	public boolean contentIsChanged() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#contentIsLoaded()
	 */
	public boolean contentIsLoaded() {
		return false;
	}

	public LinkDelta createLink(LinkType lt, Item destination)
			throws CadseException {
		return createLink(lt, destination, true, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#createLink(fr.imag.adele
	 * .cadse.core.LinkType, fr.imag.adele.cadse.core.Item)
	 */
	public LinkDelta createLink(LinkType lt, Item destination, boolean notify, boolean validate)
			throws CadseException {
		// 2. Type of destination must be the same type as item type destination
		// defined in lt
		if (!destination.isInstanceOf(lt.getDestination())) {
			throw new CadseException(Messages.cannot_create_link_bad_link_type,
					getId(), getQualifiedName(), destination.getId(),
					destination.getQualifiedName(), lt.getName(), lt
							.getDestination().getName(), destination.getType()
							.getName());
		}

		getWorkingCopy().check_write();
		syncOutgoingLinks();
		LinkDelta linkOperation;
		ItemDelta destiDelta = getWorkingCopy().loadItem(destination);
		if (destiDelta.isDeleted()) {
			throw new CadseException(
					Messages.cannot_create_link_to_deleted_destination, this,
					destiDelta);
		}
		if (isDeleted()) {
			throw new CadseException(
					Messages.cannot_create_link_from_deleted_source, this,
					destiDelta);
		}
		if (!destiDelta.exists()) {
			throw new CadseException(
					Messages.cannot_create_link_to_unexisting_destination,
					this, destiDelta);
		}
		if (!exists()) {
			throw new CadseException(
					Messages.cannot_create_link_from_unexisting_source, this,
					destiDelta);
		}

		linkOperation = getOrCreateLinkOperation(lt, destiDelta, null, -1,
				false);
		if (linkOperation.isAdded()) {
			return linkOperation;
		}
		LogicalWorkspaceImpl.constraints_SourceItem(linkOperation, lt, this);

		if (validate)
			_copy.validateCreatedLink(linkOperation);

		// synchronize _parentItem attribute
		if (lt == CadseGCST.ITEM_lt_PARENT
				|| linkOperation.getLinkTypeName().startsWith("#inverse-part") //$NON-NLS-1$
				|| linkOperation.getLinkTypeName().startsWith("#invert_part")) { //$NON-NLS-1$
			setParent(destiDelta, null, false, false);
		}
		if (lt == CadseGCST.GROUP_EXT_ITEM_lt_MEMBER_OF) {
			_group = new ItemTypeItemDeltaAdapter(destiDelta);
		}
		if (linkOperation.isDeleted()) {
			linkOperation.getDeleteOperation().removeInParent();
			linkOperation.setDeleteOperation(null);
			getWorkingCopy().notifyCancelCreatedLink(linkOperation);
			linkOperation.removeInParent();
			return linkOperation;
		}
		CreateOperationImpl createOperation = new CreateOperationImpl(
				linkOperation);
		linkOperation.setCreateOperation(createOperation);
		createOperation.addInParent();
		if (notify)
			getWorkingCopy().notifyCreatedLink(linkOperation);

		return linkOperation;
	}

	public boolean exists() {
		return isAdded() || isLoaded()
				|| (getBaseItem() != null && getBaseItem().exists());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#createPartParentLink()
	 */
	public void createPartParentLink() throws CadseException {
		if (_parentItem != null && _parentLinkType != null) {
			_parentItem.createLink(_parentLinkType, this);
			// the inverse link is automatic
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#delete(boolean)
	 */
	public void delete(boolean deleteContent) throws CadseException {
		delete(
				null,
				DeleteOperationImpl.DELETE_ANNOTATION_LINK
						| DeleteOperationImpl.DELETE_PART_LINK
						| DeleteOperationImpl.DELETE_INCOMING_LINK
						| (deleteContent ? (DeleteOperationImpl.DELETE_CONTENT | DeleteOperationImpl.DELETE_MAPPING)
								: 0));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#delete(fr.imag.adele.
	 * cadse.core.delta.DeleteOperation, int)
	 */
	public void delete(DeleteOperation operation, int flag)
			throws CadseException {
		getWorkingCopy().check_write();
		if (isRuntime()) {
			// cannot delete a static item
			throw new CadseException(
					Messages.cannot_delete_item_not_modifiable, getName(), this); //$NON-NLS-2$
		}
		try {
			if (this.getCreateOperation() != null) {

				// cancel create operation
				_copy.remove(this);
				_copy.notifyCancelCreatedItem(this);
				if (_orders != null) {
					// cancel all create link pointed to this
					for (LinkDelta l : _orders) {
						if (l.isAdded()) {
							getWorkingCopy().notifyCancelCreatedLink(l);
						}
						//
						if (l.isPart()) {
							ItemDelta dest = l.getDestination();
							if (dest != null && dest.isAdded()) {
								dest.delete(operation, flag);
							}
						}
					}
				}
				// TODO incomings links
				List<LinkDelta> incomingLinks = getIncomingLinkOperations();
				for (LinkDelta incomingLink : incomingLinks) {
					if (incomingLink.isAdded()) {
						incomingLink.delete(operation);
					}
				}
				_copy.changeKey(this, getKey(), null);
				return;
			}
			if (isLoaded())
				setLoaded(false);

			if (getBaseItem() == null || !getBaseItem().isResolved()) {
				// /the item doesn't exist in workspace logique
				return;
			}
			if (this.getDeleteOperation() != null) {
				// the item is already deleted
				return;
			}
			if (getBaseItem() instanceof ItemType) {
				// delete instance
				List<Item> items = ((ItemType) getBaseItem()).getItems();
				for (Item i : items) {
					final ItemDelta itemOfType = getWorkingCopy().getItem(
							i.getId(), true);
					if (itemOfType == null || itemOfType.isDeleted()) {
						continue;
					}
					itemOfType.delete(operation, flag);
				}
			}

			// add deleted operation

			DeleteOperationImpl deleteOperation = new DeleteOperationImpl(this,
					(DeleteOperationImpl) operation);
			setDeleteOperation(deleteOperation);
			deleteOperation.addInParent();
			getWorkingCopy().notifyDeletedItem(this);
		} catch (CadseException e) {
			throw new CadseException(e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#doubleClick()
	 */
	public void doubleClick() {
		if (_doubleClick) {
			return;
		}
		this._doubleClick = true;
		try {
			getWorkingCopy().notifyDoubleClick(this);
		} catch (CadseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<WorkspaceListener> filter(int filters,
			ImmutableWorkspaceDelta delta) {
		return Collections.emptyList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#finishLoad()
	 */
	public void finishLoad() {
		this._finishLoad = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#forceState(fr.imag.adele
	 * .cadse.core.ItemState)
	 */
	public void forceState(ItemState state) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getAddedLinks()
	 */
	public Collection<LinkDelta> getAddedLinks() {
		if (_links == null) {
			return Collections.emptyList();
		}
		ArrayList<LinkDelta> result = new ArrayList<LinkDelta>();
		for (LinkDelta link : _links.values()) {
			if (link.isAdded()) {
				result.add(link);
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getAggregations()
	 */
	public List<Link> getAggregations() {
		return Accessor
				.getLinksByKind(getOutgoingLinks(), LinkType.AGGREGATION);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getAndCreateContentManager
	 * ()
	 */
	public ContentItem getAndCreateContentManager() {
		return getContentItem();
	}

	public <T> T getAttribute(IAttributeType<T> att, boolean returnDefault) {
		if (att == null) {
			throw new CadseIllegalArgumentException(
					Messages.attribute_definition_is_null);
		}
		
		//delegation
		if (isDelegatedValue(att)) {
			return _group.getAttributeWithDefaultValue(att, att.getDefaultValue());
		}
		if (this._attributes != null) {
			SetAttributeOperation oa = this._attributes.get(att);
			if (oa != null) {
				Object v = oa.getCurrentValue();
				if (v != null) {
					return (T) att.convertTo(v);
				} else {
					if (returnDefault) {
						return att.getDefaultValue();
					}
					return null;
				}
			}
		}
		Item base = getBaseItem();
		if (base != null) {
			return base.getAttribute(att);
		} else {
			if (!returnDefault) {
				return null;
			}

			ItemType it = getType();
			if (it != null) {
				T t = att.getDefaultValue();
				if (t != null) {
					// try {
					// //probleme notifiy reentrant
					// //setAttribute(attributeName, t);
					// } catch (CadseException e) {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
					// }
					return t;
				}

			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getAttribute(fr.imag.
	 * adele.cadse.core.attribute.IAttributeType)
	 */
	public <T> T getAttribute(IAttributeType<T> att) {
		if (att == null)
			return null;
		return getAttribute(att, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getAttributeOwner(fr.
	 * imag.adele.cadse.core.attribute.IAttributeType)
	 */
	public <T> T getAttributeOwner(IAttributeType<T> att) {
		return getAttribute(att);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getAttributeWithDefaultValue
	 * (fr.imag.adele.cadse.core.attribute.IAttributeType, T)
	 */
	public <T> T getAttributeWithDefaultValue(IAttributeType<T> att,
			T defaultValue) {
		T v = getAttribute(att);
		if (v == null) {
			return defaultValue;
		}
		return v;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getBaseItem()
	 */
	public Item getBaseItem() {
		if (_baseItem == null)
			_baseItem = _copy.getBaseItem(getId());
		return _baseItem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getBooleanAttribut(fr
	 * .imag.adele.cadse.core.attribute.BooleanAttributeType, boolean)
	 */
	public boolean getBooleanAttribut(BooleanAttributeType key,
			boolean defaultValue) {
		if (_attributes != null) {
			SetAttributeOperation setAtt = _attributes.get(key);
			if (setAtt != null && setAtt.getCurrentValue() != null) {
				return Convert.toBoolean(setAtt.getCurrentValue());
			}
		}
		return defaultValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getComponentInfo(fr.imag
	 * .adele.cadse.core.UUID)
	 */
	public Item getComponentInfo(UUID id) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getComponents()
	 */
	public Set<Item> getComponents() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getComponentsId()
	 */
	public Set<UUID> getComponentIds() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getCompositeParent()
	 */
	public List<Item> getCompositeParent() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getContentChangeInfo()
	 */
	public ContentChangeInfo[] getContentChangeInfo() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getContentItem()
	 */
	public ContentItem getContentItem() {
		Item base = getBaseItem();
		if (base != null) {
			return base.getContentItem();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getDeletedLinks()
	 */
	public Collection<LinkDelta> getDeletedLinks() {
		if (_links == null) {
			return Collections.emptyList();
		}
		ArrayList<LinkDelta> result = new ArrayList<LinkDelta>();
		for (LinkDelta link : _links.values()) {
			if (link.isDeleted()) {
				result.add(link);
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getDerivedLinkDescriptions
	 * (fr.imag.adele.cadse.core.ItemDescription)
	 */
	public Set<DerivedLinkDescription> getDerivedLinkDescriptions(
			ItemDescription source) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getDerivedLinks()
	 */
	public Set<DerivedLink> getDerivedLinks() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		Item base = getBaseItem();
		String displayName = getAttribute(CadseGCST.ITEM_at_DISPLAY_NAME_);
		if (displayName == null && base != null) {
			return base.getDisplayName();
		}
		return displayName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getId()
	 */
	@Override
	public UUID getId() {
		return _id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getIncomingItem(boolean,
	 * boolean)
	 */
	@Override
	public Collection<ItemDelta> getIncomingItems(boolean acceptDelete,
			boolean acceptAdd) {
		return Accessor.getIncomingItemDelta(getIncomingLinks(acceptDelete,
				acceptAdd));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #getIncomingLinks(fr.imag.adele.cadse.core.delta.ItemOperation,
	 * fr.imag.adele.cadse.core.LinkType, boolean, boolean)
	 */
	public List<Link> getIncomingLinks(LinkType lt, boolean acceptDelete,
			boolean acceptAdd) {
		List<? extends Link> links = getIncomingLinks(acceptDelete, acceptAdd);
		ArrayList<Link> ret = new ArrayList<Link>();
		for (Link l : links) {
			if (l.getLinkType() == lt) {
				ret.add(l);
			}
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #getIncomingLinks(fr.imag.adele.cadse.core.delta.ItemOperation,
	 * fr.imag.adele.cadse.core.LinkType, boolean, boolean)
	 */
	public List<LinkDelta> getIncomingLinkDeltas(LinkType lt,
			boolean acceptDelete, boolean acceptAdd) {
		List<? extends Link> links = getIncomingLinks(acceptDelete, acceptAdd);
		ArrayList<LinkDelta> ret = new ArrayList<LinkDelta>();
		for (Link l : links) {
			if (l.getLinkType() == lt) {
				ret.add((LinkDelta) l);
			}
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getIncomingItem(fr.imag
	 * .adele.cadse.core.LinkType, boolean, boolean)
	 */
	public Collection<ItemDelta> getIncomingItems(LinkType lt,
			boolean acceptDelete, boolean acceptAdd) {
		return Accessor.getIncomingItemDelta(getIncomingLinkDeltas(lt,
				acceptDelete, acceptAdd));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #getIncomingItem(fr.imag.adele.cadse.core.delta.ItemOperation, boolean,
	 * boolean)
	 */
	public Collection<Item> getIncomingItem(boolean acceptDelete,
			boolean acceptAdd) {
		return Accessor.getIncomingItem(getIncomingLinks(acceptDelete,
				acceptAdd));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getIncomingItem()
	 */
	public Collection<Item> getIncomingItems() {
		return getIncomingItem(false, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getIncomingItem(fr.imag
	 * .adele.cadse.core.LinkType)
	 */
	public Collection<Item> getIncomingItems(LinkType lt) {
		return Accessor.getIncomingItem(getIncomingLinks(lt, false, true));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getIncomingItemOne(fr
	 * .imag.adele.cadse.core.LinkType)
	 */
	public Item getIncomingItem(LinkType lt) {
		Collection<Item> incomingItem = Accessor.getIncomingItem(getIncomingLinks(lt, true, true));
		Item ret = incomingItem.size() == 1 ? incomingItem.iterator().next()
				: null;
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getIncomingLinkOperations
	 * ()
	 */
	public List<LinkDelta> getIncomingLinkOperations() {
		return getIncomingLinks(true, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getIncomingLinks()
	 */
	public List<Link> getIncomingLinks() {
		return (List<Link>) (List<? extends Link>) getIncomingLinks(false, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getIncomingLinks(boolean,
	 * boolean)
	 */
	public List<LinkDelta> getIncomingLinks(boolean acceptDelete,
			boolean acceptAdd) {
		Item itembase = getBaseItem();
		HashSet<LinkDelta> links = new HashSet<LinkDelta>();
		if (_incomingLinks != null)
			for (LinkDelta lo : _incomingLinks) {
				if (!acceptDelete && lo.isDeleted()) {
					continue;
				}
				if (!acceptAdd && lo.isAdded()) {
					continue;
				}
				links.add(lo);
			}

		if (itembase != null) {
			List<? extends Link> incommingsLinks = itembase.getIncomingLinks();
			for (Link l : incommingsLinks) {
				if (l.isDerived()) {
					continue;
				}
				try {
					LinkDelta lo = _copy
							.getOrCreateItemOperation(l.getSource())
							.getOutgoingLinkOperation(l);
					if (lo == null)
						continue;
					if (!acceptDelete && lo.isDeleted()) {
						continue;
					}
					if (!acceptAdd && lo.isAdded()) {
						continue;
					}
					links.add(lo);
				} catch (CadseException e) {
					e.printStackTrace();
				}
			}
		}
		return new ArrayList<LinkDelta>(links);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getIncomingLinks(fr.imag
	 * .adele.cadse.core.LinkType)
	 */
	public List<Link> getIncomingLinks(LinkType lt) {
		return getIncomingLinks(lt, false, true);
	}

	public Link getIncomingLink(LinkType lt, UUID srcId) {
		List<LinkDelta> links = getIncomingLinks(false, true);
		for (Link l : links) {
			if (l.getLinkType() == lt && l.getSourceId().equals(srcId)) {
				return l;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getIntAttribut(fr.imag
	 * .adele.cadse.core.attribute.IntegerAttributeType, int)
	 */
	public int getIntAttribut(IntegerAttributeType key, int defaultValue) {
		if (_attributes != null) {
			SetAttributeOperation setAtt = _attributes.get(key);
			if (setAtt != null && setAtt.getCurrentValue() != null) {
				return Convert.toInteger(setAtt.getCurrentValue());
			}
		}
		if (key.getDefaultValue() != null)
			return key.getDefaultValue();

		return defaultValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getItemTypeId()
	 */
	public UUID getItemTypeId() {
		return _itemType.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#getKey()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getKey()
	 */
	public Key getKey() {
		if (_key != null) {
			return _key;
		}
		Key key = null;
		if (getType() == null) {
			key = null;
		} else {
			KeyDefinition keyType = this.getType().getKeyDefinition();
			if (keyType != null) {
				try {
					key = keyType.computeKey(this);
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					key = null; // sinon erreur !!!
				}
			} else {
				key = null;
			}
		}

		return key;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getLastVersion()
	 */
	public int getLastVersion() {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getLocalAllAttributeTypes
	 * ()
	 */
	public IAttributeType<?>[] getLocalAllAttributeTypes() {
		if (_group != null) {
			ArrayList<IAttributeType<?>> ret = new ArrayList<IAttributeType<?>>();
			ret.addAll(Arrays.asList(getType().getAllAttributeTypes()));
			ret.addAll(Arrays.asList(((ItemType) _group)
					.getLocalAllAttributeTypes()));
			return (IAttributeType<?>[]) ret.toArray(new IAttributeType<?>[ret
					.size()]);
		}
		return getType().getAllAttributeTypes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getLocalAllAttributeTypes
	 * (java.util.List)
	 */
	public void getLocalAllAttributeTypes(List<IAttributeType<?>> all) {
		if (_types != null)
			for (ItemType it : _types) {
				it.getAllAttributeTypes(all);
			}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getLocalAllAttributeTypes
	 * (java.util.List, fr.imag.adele.cadse.core.ItemFilter)
	 */
	public void getLocalAllAttributeTypes(List<IAttributeType<?>> all,
			ItemFilter filter) {
		if (_types != null)
			for (ItemType it : _types) {
				it.getAllAttributeTypes(all, filter);
			}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getLocalAllAttributeTypes
	 * (java.util.Map, boolean)
	 */
	public void getLocalAllAttributeTypes(Map<String, IAttributeType<?>> all,
			boolean keepLastAttribute) {
		if (_types != null)
			for (ItemType it : _types) {
				it.getAllAttributeTypes(all, keepLastAttribute);
			}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getLocalAllAttributeTypes
	 * (java.util.Map, boolean, fr.imag.adele.cadse.core.ItemFilter)
	 */
	public void getLocalAllAttributeTypes(Map<String, IAttributeType<?>> all,
			boolean keepLastAttribute, ItemFilter filter) {
		if (_types != null)
			for (ItemType it : _types) {
				it.getAllAttributeTypes(all, keepLastAttribute, filter);
			}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getLocalAllAttributeTypesKeys
	 * (java.util.Set, fr.imag.adele.cadse.core.ItemFilter)
	 */
	public void getLocalAllAttributeTypesKeys(Set<String> all, ItemFilter filter) {
		if (_types != null)
			for (ItemType it : _types) {
				it.getAllAttributeTypesKeys(all, filter);
			}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getLocalAttributeType
	 * (java.lang.String)
	 */
	public IAttributeType<?> getLocalAttributeType(String shortname) {
		if (_types != null)
			for (ItemType it : _types) {
				IAttributeType<?> ret = it.getAttributeType(shortname);
				if (ret != null)
					return ret;
			}
		return null;
	}
	


	@Override
	public IAttributeType<?> getLocalAttributeType(UUID attrName) {
		ItemDelta delta = _copy.getItem(attrName);
		return delta.getAdapter(IAttributeType.class);
	}
	
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getLocation()
	 */
	public File getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getMainMappingContent
	 * (java.lang.Class)
	 */
	public <T> T getMainMappingContent(Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<?> getMappingContents() {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> List<T> getMappingContents(Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getMappingOperation(java
	 * .lang.Class)
	 */
	public <T extends MappingOperation> T getMappingOperation(Class<T> clazz) {
		T ret = null;
		if (this._mappings != null) {
			for (MappingOperation mo : _mappings) {
				if (clazz.isAssignableFrom(mo.getClass())) {
					ret = (T) mo;
					break;
				}
			}
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getMappings()
	 */
	public MappingOperation[] getMappings() {
		return _mappings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getModel()
	 */
	public LogicalWorkspace getModel() {
		return _copy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getOpenCompositeParent()
	 */
	public List<Item> getOpenCompositeParent() {
		// TODO Auto-generated method stub
		return null;
	}

	protected LinkDelta getOrCreateLinkOperation(Link l, int index) {
		if (l == null || l.getDestination() == null) {
			getCadseDomain().error(this,
					Messages.cannot_add_link_of_dest_is_null, null);
			return null;
		}
		if (l.getDestination().getId() == null) {
			getCadseDomain().error(
					this,
					Messages.cannot_add_link_of_dest_is_null + l.getClass()
							+ " : " + l.getDestination().getClass(), //$NON-NLS-2$
					null);
			return null;
		}
		ItemDelta destiDelta = getWorkingCopy().loadItem(l.getDestination());

		return getOrCreateLinkOperation(l.getLinkType(), destiDelta, l, index,
				true);
	}

	/**
	 * Cherche ou creer un link operation.
	 * 
	 * @param type
	 *            le nom du type de lien
	 * @param destination
	 *            la description (id, nom) de la destination
	 * @return un link sur le quel on peut appliquer des operations futur
	 */
	protected LinkDelta getOrCreateLinkOperation(LinkType type,
			ItemDelta destination, Link lOriginal, int index, boolean loaded) {
		if (this._links == null) {
			this._links = new HashMap<LinkKey, LinkDelta>();
		}
		if (type == null) {
			getCadseDomain().error(this,
					Messages.cannot_add_link_of_type_is_null, null);
			return null;
		}
		if (destination.getId() == null) {
			getCadseDomain().error(this, "ca", null); //$NON-NLS-1$
			return null;
		}

		LinkKey key = new LinkKey(type, destination.getId());
		if (_links.containsKey(key)) {
			return _links.get(key);
		}
		LinkDelta l;
		if (_orders == null) {
			_orders = new ArrayList<LinkDelta>();
		}
		if (index == -1 || index != _orders.size()) {
			index = _orders.size();
		}
		l = new LinkDeltaImpl(this, type, destination, lOriginal, index, loaded);
		return l;
	}

	public void addLink(LinkDeltaImpl l) {
		LinkKey key = new LinkKey(l.getLinkType(), l.getDestinationId());
		_links.put(key, l);
		_orders.add(l);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getorCreateMappingOperation
	 * (java.lang.Class)
	 */
	public <T extends MappingOperation> T getorCreateMappingOperation(
			Class<T> clazz) {
		T ret = getMappingOperation(clazz);
		if (ret == null) {
			try {
				ret = clazz.getConstructor(ItemDeltaImpl.class).newInstance(
						this);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getOrdersOperation()
	 */
	public List<OrderOperation> getOrdersOperation() {
		if (_ordersOperation == null) {
			return Collections.emptyList();
		}
		return _ordersOperation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getOrdersOperation(fr
	 * .imag.adele.cadse.core.LinkType)
	 */
	public List<OrderOperation> getOrdersOperation(LinkType lt) {
		if (_ordersOperation == null) {
			return Collections.emptyList();
		}
		ArrayList<OrderOperation> ret = new ArrayList<OrderOperation>();
		for (OrderOperation o : _ordersOperation) {
			if (o.getLinkType() == lt) {
				ret.add(o);
			}
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getOutgoingItems(boolean,
	 * boolean)
	 */
	public Collection<ItemDelta> getOutgoingItems(boolean acceptDeletedLink,
			boolean resovledOnly) {
		return Accessor.getOutgoingItemDeltas(
				getOutgoingLinks(acceptDeletedLink), resovledOnly);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getOutgoingItems(boolean,
	 * fr.imag.adele.cadse.core.LinkType, boolean)
	 */
	public Collection<ItemDelta> getOutgoingItems(boolean acceptDeletedLink,
			LinkType lt, boolean resovledOnly) {
		return Accessor.getOutgoingItemDeltas(
				getOutgoingLinks(acceptDeletedLink), lt, resovledOnly);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getOutgoingItems(boolean,
	 * java.lang.String, boolean)
	 */
	public Collection<ItemDelta> getOutgoingItems(boolean acceptDeletedLink,
			String linkType, boolean resovledOnly) {
		return Accessor.getOutgoingItemDeltas(
				getOutgoingLinks(acceptDeletedLink), linkType, resovledOnly);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getOutgoingItems(boolean)
	 */
	public Collection<Item> getOutgoingItems(boolean resovledOnly) {
		return Accessor.getOutgoingItems(getOutgoingLinks(), resovledOnly);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getOutgoingItems(fr.imag
	 * .adele.cadse.core.LinkType, boolean)
	 */
	public Collection<Item> getOutgoingItems(LinkType lt, boolean resovledOnly) {
		return Accessor.getOutgoingItems(getOutgoingLinks(), lt, resovledOnly);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getOutgoingItems(java
	 * .lang.String, boolean)
	 */
	public Collection<Item> getOutgoingItems(String linkType,
			boolean resovledOnly) {
		return Accessor.getOutgoingItems(getOutgoingLinks(), linkType,
				resovledOnly);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getOutgoingItems(java
	 * .lang.String, fr.imag.adele.cadse.core.UUID, boolean)
	 */
	public Item getOutgoingItem(String linkname, UUID itemId,
			boolean resovledOnly) {
		Link l;
		try {
			l = getOutgoingLink(null, linkname, itemId);
		} catch (CadseException e) {
			e.printStackTrace();
			return null;
		}
		if (l == null) {
			return null;
		}
		return l.getDestination(resovledOnly);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.Item#getOutgoingItem(fr.imag.adele.cadse.core
	 * .LinkType, boolean)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getOutgoingItem(fr.imag
	 * .adele.cadse.core.LinkType, boolean)
	 */
	public ItemDelta getOutgoingItem(LinkType lt, boolean resovledOnly) {
		syncOutgoingLinks();
		if (_orders == null) {
			return null;
		}
		for (LinkDelta lo : _orders) {
			if (lo.getLinkType() == lt) {
				return lo.getDestination(resovledOnly);
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#getOutgoingItem(java.lang.String,
	 * boolean)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getOutgoingItem(java.
	 * lang.String, boolean)
	 */
	public ItemDelta getOutgoingItem(String linkNameID, boolean resovledOnly) {
		syncOutgoingLinks();
		if (_orders == null) {
			return null;
		}
		for (LinkDelta lo : _orders) {
			if (lo.getLinkType().getName().equals(linkNameID)) {
				return lo.getDestination(resovledOnly);
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.Item#getOutgoingLink(fr.imag.adele.cadse.core
	 * .Item)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getOutgoingLink(fr.imag
	 * .adele.cadse.core.Item)
	 */
	public LinkDelta getOutgoingLink(Item item) {
		List<Link> links = getOutgoingLinks();
		for (Link l : links) {
			if (l.getDestinationId().equals(item.getId())) {
				return (LinkDelta) l;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getOutgoingLink(fr.imag
	 * .adele.cadse.core.LinkType)
	 */
	public LinkDelta getOutgoingLink(LinkType linkType) {
		return (LinkDelta) Accessor.getOutgoingLink(getOutgoingLinks(),
				linkType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getOutgoingLink(fr.imag
	 * .adele.cadse.core.LinkType, fr.imag.adele.cadse.core.UUID)
	 */
	public LinkDelta getOutgoingLink(LinkType lt, UUID destId) {
		syncOutgoingLinks();
		if (_links == null) {
			return null;
		}
		LinkKey key = new LinkKey(lt, destId);
		LinkDelta linkOperation = _links.get(key);
		if (linkOperation == null || linkOperation.isDeleted()) {
			return null;
		}
		return linkOperation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getOutgoingLink(java.
	 * lang.String, fr.imag.adele.cadse.core.UUID)
	 */
	public LinkDelta getOutgoingLink(UUID ltId, String ltName, UUID destId)
			throws CadseException {
		LinkType lt = findLinkTypeFromNameAndDest(ltId, ltName, destId);
		if (lt == null)
			throw new CadseException("Cannot found linktype " + ltName);
		syncOutgoingLinks();
		if (_links == null) {
			return null;
		}
		LinkKey key = new LinkKey(lt, destId);
		LinkDelta linkOperation = _links.get(key);
		if (linkOperation == null || linkOperation.isDeleted()) {
			return null;
		}
		return linkOperation;
	}

	private LinkType findLinkTypeFromNameAndDest(UUID ltId, String ltName,
			UUID destId) {
		Item destItem = getCopy().getItem(destId);
		if (destItem == null)
			throw new CadseIllegalArgumentException(
					"Cannot found item whith id {0}", destId);

		ItemType destType = destItem.getType();
		if (destType == null)
			throw new CadseIllegalArgumentException(
					"Cannot found dest ItemType from item {0}", destItem);

		ItemType sourceType = getType();
		if (sourceType == null)
			throw new CadseIllegalArgumentException(
					"Cannot found source ItemType from item {0}", this);

		LinkType lt = getLogicalWorkspace().findLinkType(sourceType, destType,
				ltId, ltName, false);
		if (lt == null)
			throw new CadseIllegalArgumentException(
					"Cannot found linkType from name {0} ({1} to {2})", ltName,
					sourceType, destType);
		return lt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getOutgoingLinkOperation
	 * (fr.imag.adele.cadse.core.Link)
	 */
	public LinkDelta getOutgoingLinkOperation(Link l) throws CadseException {
		if (!isAdded() && this._links == null) {
			syncOutgoingLinks();
		}
		if (this._links == null) {
			throw new CadseException(Messages.link_not_found_in_outgoing + l);
		}
		LinkKey key = new LinkKey(l.getLinkType(), l.getDestinationId());
		if (_links.containsKey(key)) {
			return _links.get(key);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getOutgoingLinkOperation
	 * (java.lang.String, fr.imag.adele.cadse.core.ItemDescriptionRef)
	 */
	public LinkDelta getOutgoingLinkOperation(String type,
			ItemDescriptionRef destination) {
		if (!isAdded() && this._links == null) {
			syncOutgoingLinks();
		}
		if (this._links == null) {
			return null;
		}
		LinkType lt = findLinkTypeFromNameAndDest(null, type, destination
				.getId());
		LinkKey key = new LinkKey(lt, destination.getId());
		if (_links.containsKey(key)) {
			return _links.get(key);
		}
		ItemDelta destiDelta;
		destiDelta = getWorkingCopy().loadItem(destination);

		return getOrCreateLinkOperation(lt, destiDelta, null, -1, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getOutgoingLinkOperations
	 * ()
	 */
	public Collection<LinkDelta> getOutgoingLinkOperations() {
		// return all link opertion
		// add deleted link at the end

		syncOutgoingLinks();
		if (_links == null) {
			return Collections.emptyList();
		}
		final ArrayList<LinkDelta> ret = new ArrayList<LinkDelta>(_orders);
		for (LinkDelta linkOperation : _links.values()) {
			if (linkOperation.isDeleted()) {
				ret.add(linkOperation);
			}
		}
		return Collections.unmodifiableCollection(ret);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getOutgoingLinkOperations
	 * (fr.imag.adele.cadse.core.UUID)
	 */
	public List<LinkDelta> getOutgoingLinkOperations(UUID destId) {
		syncOutgoingLinks();
		if (_links == null) {
			return Collections.emptyList();
		}
		ArrayList<LinkDelta> ret = new ArrayList<LinkDelta>();
		for (LinkDelta lo : _links.values()) {
			if (!(lo.getDestinationId().equals(destId))) {
				continue;
			}
			ret.add(lo);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getOutgoingLinkOperations
	 * (fr.imag.adele.cadse.core.LinkType)
	 */
	public List<LinkDelta> getOutgoingLinkOperations(LinkType linkType) {
		syncOutgoingLinks();
		if (_links == null) {
			return Collections.emptyList();
		}
		ArrayList<LinkDelta> ret = new ArrayList<LinkDelta>();
		for (LinkDelta lo : _links.values()) {
			if (!linkType.equals(lo.getLinkType())) {
				continue;
			}
			ret.add(lo);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getOutgoingLinks()
	 */
	public List<Link> getOutgoingLinks() {
		syncOutgoingLinks();
		if (_orders == null) {
			return Collections.emptyList();
		}
		ArrayList<Link> ret = new ArrayList<Link>();
		for (LinkDelta lo : _orders) {
			if (!false && lo.isDeleted()) {
				continue;
			}
			ret.add(lo);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getOutgoingLinks(boolean)
	 */
	public List<LinkDelta> getOutgoingLinks(boolean acceptDeleted) {
		syncOutgoingLinks();
		if (_orders == null) {
			return Collections.emptyList();
		}
		ArrayList<LinkDelta> ret = new ArrayList<LinkDelta>();
		for (LinkDelta lo : _orders) {
			if (!acceptDeleted && lo.isDeleted()) {
				continue;
			}
			ret.add(lo);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getOutgoingLinks(fr.imag
	 * .adele.cadse.core.UUID)
	 */
	public List<LinkDelta> getOutgoingLinks(UUID destId) {
		syncOutgoingLinks();
		if (_orders == null) {
			return Collections.emptyList();
		}
		ArrayList<LinkDelta> ret = new ArrayList<LinkDelta>();
		for (LinkDelta lo : _orders) {
			if (lo.isDeleted()) {
				continue;
			}
			if (!(lo.getDestinationId().equals(destId))) {
				continue;
			}
			ret.add(lo);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getOutgoingLinks(fr.imag
	 * .adele.cadse.core.LinkType)
	 */
	public List<Link> getOutgoingLinks(LinkType linkType) {
		syncOutgoingLinks();
		if (_orders == null) {
			return Collections.emptyList();
		}
		ArrayList<Link> ret = new ArrayList<Link>();
		for (LinkDelta lo : _orders) {
			if (lo.isDeleted()) {
				continue;
			}
			if (!linkType.equals(lo.getLinkType())) {
				continue;
			}
			ret.add(lo);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getParentInStorage()
	 */
	public ItemDelta getParentInStorage() throws CadseException {
		if (this._parentItem != null) {
			return _parentItem;
		}

		Object value = getAttribute(Accessor.ATTR_PARENT_ITEM_ID);
		if (value instanceof CompactUUID) {
			// migration
			value = ((CompactUUID) value).toUUID();
			// setAttribute(ATTR_PARENT_ITEM_ID, value);
		}
		UUID parentId = (UUID) value;
		if (parentId == null) {
			return null;
		}
		ItemDelta retItem = _copy.getItem(parentId);
		if (retItem == null) {
			Item baseitem = getBaseItem();
			if (baseitem != null) {
				Item baseParentItem = baseitem.getPartParent();
				if (baseParentItem != null) {
					retItem = _copy.getOrCreateItemOperation(baseParentItem);
				}
			}
		}
		return retItem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getParentInStorage(fr
	 * .imag.adele.cadse.core.LinkType)
	 */
	public ItemDelta getParentInStorage(LinkType lt) {
		// lt A -> B
		LinkType invLt = lt.getInverse();
		return getOutgoingItem(invLt, false);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getParentLinkTypeInStorage
	 * (fr.imag.adele.cadse.core.Item)
	 */
	public LinkType getParentLinkTypeInStorage(Item parent) {
		if (_parentLinkType != null) {
			return _parentLinkType;
		}
		String parentId = (String) getAttribute(Accessor.ATTR_PARENT_ITEM_TYPE_ID);
		if (parentId == null) {
			return null;
		}
		if (parent.getType() == null) {
			return null;
		}
		return parent.getType().getOutgoingLinkType(parentId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getPart(java.lang.String)
	 */
	public Item getPartChild(UUID id) {
		return Accessor.getParts(getOutgoingLinks(), id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getPartLinkParent(boolean
	 * , boolean)
	 */
	public Link getPartLinkParent(boolean acceptDelete, boolean acceptAdd) {
		for (Link l : getIncomingLinks(acceptDelete, acceptAdd)) {
			if (l.getLinkType().isPart()) {
				return l;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getPartParent()
	 */
	public ItemDelta getPartParent() {
		return getPartParent(true, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getPartParent(boolean)
	 */
	public ItemDelta getPartParent(boolean attemptToRecreate) {
		return getPartParent(true, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getPartParent(boolean,
	 * boolean)
	 */
	public ItemDelta getPartParent(boolean attemptToRecreate,
			boolean acceptDeleted) {
		if (_parentItem != null) {
			return _parentItem;
		}

		Item baseitem = getBaseItem();
		if (baseitem != null) {
			Item baseParentItem = baseitem.getPartParent();
			if (baseParentItem != null) {
				_parentItem = _copy.getOrCreateItemOperation(baseParentItem);
				return _parentItem;
			}
		}

		ItemDelta findDeletedParentItem = null;

		if (_links != null) {
			for (LinkDelta linkOperation : _links.values()) {
				if (!acceptDeleted && linkOperation.isDeleted()) {
					continue;
				}
				if (linkOperation.getLinkTypeName().startsWith("#inverse-part")) { //$NON-NLS-1$
					_parentItem = linkOperation.getDestination();
					if (_parentItem != null) {
						if (!acceptDeleted && _parentItem.isDeleted()) {
							_parentItem = null;
							continue;
						}
						return _parentItem;
					}
				}
			}
		}

		for (Link l : getIncomingLinks(acceptDeleted, true)) {
			if (l.getLinkType().isPart()) {
				final ItemDelta source = (ItemDelta) l.getSource();
				if (source.isDeleted()) {
					findDeletedParentItem = source;
					continue;
				}
				return source;
			}
		}
		if (findDeletedParentItem != null) {
			return findDeletedParentItem;
		}

		try {
			_parentItem = getParentInStorage();
		} catch (CadseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (_parentItem == null) {
			return null;
		}

		// cherche a reparer une anomalie : un lien est manquant...
		// on fait ceci que si ce n'est pas un nouveau item ou si l'item
		// n'est pas en modification(todo)
		LinkType lt = getParentLinkTypeInStorage(_parentItem);
		if (lt == null) {
			lt = getType().getIncomingPart(_parentItem.getType());
		}
		if (lt == null || !lt.isPart()) {
			return null;
		}

		Link l = _parentItem.getOutgoingLink(lt, getId());
		if (l == null) {
			try {
				_parentItem.createLink(lt, this);
			} catch (CadseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return _parentItem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getPartParent(fr.imag
	 * .adele.cadse.core.ItemType)
	 */
	public Item getPartParent(ItemType typeID) {
		return Accessor.getPartParent(getPartParent(), typeID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getPartParent(fr.imag
	 * .adele.cadse.core.LinkType)
	 */
	public ItemDelta getPartParent(LinkType lt) {
		return getPartParent(lt, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getPartParent(fr.imag
	 * .adele.cadse.core.LinkType, boolean)
	 */
	public ItemDelta getPartParent(LinkType lt, boolean attemptToRecreate) {
		// if (parentItem != null)
		// return parentItem;
		ItemDelta ret = getParentInStorage(lt);
		if (ret != null) {
			return ret;
		}

		for (Link l : getIncomingLinks()) {
			if (l.getLinkType() == lt) {
				ItemDelta source = (ItemDelta) l.getSource();

				return source;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getPartParentByName(java
	 * .lang.String)
	 */
	public Item getPartParentByName(String typeName) {
		return Accessor.getPartParentByName(getPartParent(), typeName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getPartParentLink()
	 */
	@Deprecated
	public LinkType getPartParentLink() {
		return getPartParentLinkType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getPartParentLinkType()
	 */
	@Override
	public LinkType getPartParentLinkType() {
		return _parentLinkType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getParts()
	 */
	@Override
	public Collection<Item> getPartChildren() {
		return Accessor.getParts(getOutgoingLinks());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getParts(fr.imag.adele
	 * .cadse.core.LinkType)
	 */
	@Override
	public Collection<Item> getPartChildren(LinkType linkTypeName) {
		return Accessor.getParts(getOutgoingLinks(), linkTypeName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getQualifiedDisplayName()
	 */
	@Override
	public String getQualifiedDisplayName() {
		return getAttributeWithDefaultValue(CadseGCST.ITEM_at_DISPLAY_NAME_,
				getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getSetAttributeOperation
	 * (java.lang.String)
	 */
	@Override
	public SetAttributeOperation getSetAttributeOperation(IAttributeType<?> key) {
		return getSetAttributeOperation(key, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getSetAttributeOperation
	 * (java.lang.String, boolean)
	 */
	@Override
	public SetAttributeOperation getSetAttributeOperation(
			IAttributeType<?> key, boolean create) {
		SetAttributeOperation ret = super.getSetAttributeOperation(key);
		if (create && ret == null) {
			Item t = getBaseItem();

			Object value;
			if (t != null && t.isResolved()) {
				value = t.getAttribute(key);
				if (value != null) {
					ret = new SetAttributeOperationImpl(this, key, value, value);
					add(ret, false);
				}
			}
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getShortName()
	 */
	public String getName() {
		return getAttributeWithDefaultValue(CadseGCST.ITEM_at_NAME_,
				Item.NO_VALUE_STRING);
	}

	@Deprecated
	public String getShortName() {
		return getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getState()
	 */
	public ItemState getState() {
		return ItemState.NOT_IN_WORKSPACE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getStringAttribut(fr.
	 * imag.adele.cadse.core.attribute.StringAttributeType, java.lang.String)
	 */
	public String getStringAttribut(StringAttributeType key, String defaultValue) {
		if (_attributes != null) {
			SetAttributeOperation setAtt = _attributes.get(key);
			if (setAtt != null && setAtt.getCurrentValue() != null) {
				return Convert.toString(setAtt.getCurrentValue());
			}
		}
		return defaultValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getType()
	 */
	public ItemType getType() {
		return _types == null ? null : _types[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getUniqueName()
	 */
	public String getQualifiedName() {
		if (CadseGCST.ITEM_at_QUALIFIED_NAME_ == null) {
			throw new CadseIllegalArgumentException(
					"CadseGCST.ITEM_at_QUALIFIED_NAME_ is null"); //$NON-NLS-1$
		}
		return getAttribute(CadseGCST.ITEM_at_QUALIFIED_NAME_);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getUniqueName(boolean)
	 */
	public String getQualifiedName(boolean recompute) throws CadseException {
		String value = getQualifiedName();
		if (recompute || value == null) {
			value = getType().getItemManager().computeQualifiedName(this,
					getName(), getPartParent(), getPartParentLinkType());
			setQualifiedName(value);
		}
		return value;
	}

	@Deprecated
	public String getUniqueName() {
		return getQualifiedName();
	}

	@Deprecated
	public String getUniqueName(boolean recompute) throws CadseException {
		return getQualifiedName(recompute);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getURLAttribut(fr.imag
	 * .adele.cadse.core.attribute.URLAttributeType)
	 */
	public URL getURLAttribut(URLAttributeType key)
			throws MalformedURLException {
		if (_attributes != null) {
			SetAttributeOperation setAtt = _attributes.get(key);
			if (setAtt != null && setAtt.getCurrentValue() != null) {
				return Convert.toURL(setAtt.getCurrentValue());
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getVersion()
	 */
	public int getVersion() {
		return getAttributeWithDefaultValue(CadseGCST.ITEM_at_TW_VERSION_, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getWhyReadOnly()
	 */
	public List<Item> getWhyReadOnly() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getWorkingCopy()
	 */
	@Override
	public LogicalWorkspaceTransactionImpl getWorkingCopy() {
		return _copy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#getWorkspaceDomain()
	 */
	public CadseDomain getCadseDomain() {
		return this._copy.getCadseDomain();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#getWorkspaceLogique()
	 */
	public LogicalWorkspace getLogicalWorkspace() {
		return this._copy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#indexOf(fr.imag.adele
	 * .cadse.core.Link)
	 */
	public int indexOf(Link link) {
		try {
			return _orders.indexOf(getOutgoingLinkOperation(link));
		} catch (CadseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seefr.imag.adele.cadse.core.delta.ItemOperationItf#
	 * internalGetGenericOwnerAttribute
	 * (fr.imag.adele.cadse.core.attribute.IAttributeType)
	 */
	public <T> T internalGetGenericOwnerAttribute(IAttributeType<T> type) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seefr.imag.adele.cadse.core.delta.ItemOperationItf#
	 * internalGetGenericOwnerAttribute(java.lang.String)
	 */
	public <T> T internalGetGenericOwnerAttribute(String key) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#internalGetOwnerAttribute
	 * (fr.imag.adele.cadse.core.attribute.IAttributeType)
	 */
	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#internalGetOwnerAttribute
	 * (java.lang.String)
	 */
	public <T> T internalGetOwnerAttribute(String key) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#isAccessible()
	 */
	public boolean isAccessible() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#isAncestorOf(fr.imag.
	 * adele.cadse.core.Item)
	 */
	public boolean isAncestorOf(Item item2) {
		return Accessor.isAncestorOf(this, item2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#isComposite()
	 */
	public boolean isComposite() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#isDoubleClick()
	 */
	public boolean isDoubleClick() {
		return _doubleClick;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#isFinishLoad()
	 */
	public boolean isFinishLoad() {
		return _finishLoad;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#isHidden()
	 */
	public boolean isHidden() {
		return getAttribute(CadseGCST.ITEM_at_ITEM_HIDDEN_, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#isInIncomingLinks(fr.
	 * imag.adele.cadse.core.Link)
	 */
	public boolean isInIncomingLinks(Link l) {
		assert l != null;
		return l.getDestinationId().equals(getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#isInOutgoingLinks(fr.
	 * imag.adele.cadse.core.Link)
	 */
	public boolean isInOutgoingLinks(Link l) {
		assert l != null;
		return l.getSourceId().equals(getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#isInstanceOf(fr.imag.
	 * adele.cadse.core.ItemType)
	 */
	public boolean isInstanceOf(TypeDefinition it) {
		if (getType() == null) {
			return false;
		}
		if (it == CadseGCST.ITEM_TYPE && getType().isGroupType())
			return true;
		return Accessor.isInstanceOf(this, it);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#isModified()
	 */
	@Override
	public boolean isModified() {
		if (_links != null) {
			for (LinkDelta linkOper : _links.values()) {
				if (linkOper.isModified()) {
					return true;
				}
			}
		}
		if (_ordersOperation != null) {
			return true;
		}
		if (_mappings != null) {
			return true;
		}
		return super.isModified();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#isOrphan()
	 */
	public boolean isOrphan() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#isPartItem()
	 */
	public boolean isPartItem() {
		return _parent != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#isReadOnly()
	 */
	public boolean isReadOnly() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#isRequierNewRev()
	 */
	public boolean isRequireNewRev() {
		return getAttributeWithDefaultValue(CadseGCST.ITEM_at_REQUIRE_NEW_REV_,
				false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#isResolved()
	 */
	public boolean isResolved() {
		Item base = getBaseItem();
		if (base != null) {
			return base.isResolved();
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#isRevModified()
	 */
	public boolean isRevModified() {
		return getAttributeWithDefaultValue(CadseGCST.ITEM_at_REV_MODIFIED_,
				false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#isStatic()
	 */
	public boolean isRuntime() {
		Item base = getBaseItem();
		if (base != null) {
			return base.isRuntime();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#isTWAttributeModified
	 * (fr.imag.adele.cadse.core.attribute.IAttributeType)
	 */
	public boolean isTWAttributeModified(IAttributeType<?> att) {
		Link l = getOutgoingLink(CadseGCST.ITEM_lt_MODIFIED_ATTRIBUTES, att
				.getId());
		return l != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#isUpdate()
	 */
	public boolean isUpdate() {
		return _update;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#isValid()
	 */
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#itemHasContent()
	 */
	public boolean itemHasContent() {
		Item base = getBaseItem();
		if (base != null) {
			return base.itemHasContent();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#loadAttribute(fr.imag
	 * .adele.cadse.core.attribute.IAttributeType, java.lang.Object)
	 */
	public void loadAttribute(IAttributeType<?> key, Object value)
			throws CadseException {
		setAttribute(key, value, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#loadContent()
	 */
	public void loadContent() {
	}

	public ContentItem _getContentItem() {
		ItemDelta c = getOutgoingItem(CadseGCST.ITEM_lt_CONTENTS, true);
		if (c == null)
			return null;
		return c.getAdapter(ContentItem.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#loadDerivedLink(java.
	 * lang.String, fr.imag.adele.cadse.core.delta.ItemOperationItf, boolean,
	 * boolean, java.lang.String, java.lang.String,
	 * fr.imag.adele.cadse.core.UUID, fr.imag.adele.cadse.core.UUID, int)
	 */
	public void loadDerivedLink(String linkType, ItemDelta dest,
			boolean isAggregation, boolean isRequire, String link_info,
			String originLinkTypeID, UUID uuidOriginLinkSourceTypeID,
			UUID uuidOriginLinkDestinationTypeID, int version) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#loadItem(fr.imag.adele
	 * .cadse.core.IWorkingLoadingItems,
	 * fr.imag.adele.cadse.core.delta.ItemOperation,
	 * fr.imag.adele.cadse.core.util.IErrorCollector)
	 */
	public void loadItem(IWorkingLoadingItems wl, ItemDelta itemOperation,
			IErrorCollector errorCollector) throws CadseException {
	}

	@Override
	public LinkDelta loadLink(LinkType linkType, ItemDelta destItem)
			throws CadseException {
		getWorkingCopy().check_write();
		syncOutgoingLinks();
		LinkDelta linkOperation;
		linkOperation = getOrCreateLinkOperation(linkType, destItem, null, -1,
				true);
		if (linkOperation.isAdded()) {
			return linkOperation;
		}
		if (linkOperation.isDeleted()) {
			return linkOperation;
		}
		// synchronize _parentItem attribute
		if (linkType.equals(CadseGCST.ITEM_lt_PARENT.getName())
				|| linkType.getName().startsWith("#inverse-part") //$NON-NLS-1$
				|| linkType.getName().startsWith("#invert_part")) { //$NON-NLS-1$
			setParent(destItem, null, false, false);
		} else {
			ItemType it = getType();
			if (it != null && linkType.isPart()) {
				destItem.setParent(this, linkType, false, false);
			}
		}

		linkOperation.setLoaded(true);
		return linkOperation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#loadLink(java.lang.String
	 * , fr.imag.adele.cadse.core.delta.ItemOperation)
	 */
	public LinkDelta loadLink(String linkTypeName, ItemDelta destItem)
			throws CadseException {
		getWorkingCopy().check_write();
		syncOutgoingLinks();
		LinkDelta linkOperation;
		LinkType lt = findLinkTypeFromNameAndDest(null, linkTypeName, destItem
				.getId());
		linkOperation = getOrCreateLinkOperation(lt, destItem, null, -1, true);
		if (linkOperation.isAdded()) {
			return linkOperation;
		}
		if (linkOperation.isDeleted()) {
			return linkOperation;
		}
		// synchronize _parentItem attribute
		if (lt == CadseGCST.ITEM_lt_PARENT
				|| linkTypeName.startsWith("#inverse-part") //$NON-NLS-1$
				|| linkTypeName.startsWith("#invert_part")) { //$NON-NLS-1$
			setParent(destItem, null, false, false);
		} else {
			ItemType it = getType();
			if (it != null && lt.isPart()) {
				destItem.setParent(this, lt, false, false);
			}
		}

		linkOperation.setLoaded(true);
		return linkOperation;
	}

	public <T> T getAdapter(Class<T> clazz) {
		ItemDeltaAdapterFactory<T> itemDeltaAdapterFactory = (ItemDeltaAdapterFactory<T>) _registerAdapter
				.get(clazz);
		if (itemDeltaAdapterFactory == null)
			return null;
		return (T) itemDeltaAdapterFactory.getAdapter(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.Item#migratePartLink(fr.imag.adele.cadse.core
	 * .Item, fr.imag.adele.cadse.core.LinkType)
	 */
	public void migratePartLink(Item newPartParent, LinkType lt)
			throws CadseException {

		LinkDelta lpart = null;
		for (LinkDelta l : getIncomingLinks(false, true)) {
			if (l.getLinkType().isPart()) {
				lpart = l;
				break;
			}
		}
		if (lt == null) {
			lt = getType().getIncomingPart(newPartParent.getType());
		}
		if (!lt.isPart()) {
			throw new CadseException("{0} is not a part", lt); //$NON-NLS-1$
		}
		ItemDelta newPartParentDelta = getWorkingCopy().getItem(
				newPartParent.getId());
		if (newPartParentDelta == null) {
			throw new CadseException(Messages.cannot_find_item + newPartParent);
		}
		if (lpart != null) {
			final DeleteOperationImpl optionDelete = new DeleteOperationImpl(
					null, null, 0);
			lpart.delete(optionDelete);
		}

		LinkDelta newLink = newPartParentDelta.createLink(lt, this);
		setParent(newPartParentDelta, lt);
		getWorkingCopy().notifyMigratePartLink(this, newPartParentDelta, lt,
				newLink, lpart);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#moveAfter(fr.imag.adele
	 * .cadse.core.delta.LinkOperation, fr.imag.adele.cadse.core.Link)
	 */
	public boolean moveAfter(LinkDelta linkOne, Link linkTwo_)
			throws CadseException {
		LinkDelta linkTwo = getOutgoingLinkOperation(linkTwo_);

		checkParamMove(linkOne, linkTwo_, linkTwo);

		int indexOne = linkOne.getIndex();
		int indexTwo = linkTwo.getIndex();

		if (indexOne > indexTwo) {
			return false; // nothing
		}

		orderMove(indexOne + 1, indexTwo, -1);
		_orders.add(indexTwo + 1, linkOne);
		_orders.remove(indexOne);
		linkOne.setIndex(indexTwo, false);

		if (this._ordersOperation == null) {
			this._ordersOperation = new ArrayList<OrderOperation>();
		}
		OrderOperationImpl o = new OrderOperationImpl(this,
				OrderWay.move_after, linkOne, linkTwo);
		this._ordersOperation.add(o);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#moveBefore(fr.imag.adele
	 * .cadse.core.delta.LinkOperation, fr.imag.adele.cadse.core.Link)
	 */
	public boolean moveBefore(LinkDelta linkOne, Link linkTwo_)
			throws CadseException {
		LinkDelta linkTwo = getOutgoingLinkOperation(linkTwo_);
		checkParamMove(linkOne, linkTwo_, linkTwo);

		int indexOne = linkOne.getIndex();
		int indexTwo = linkTwo.getIndex();

		if (indexOne < indexTwo) {
			return false; // nothing
		}

		orderMove(indexTwo, indexOne - 1, -1);
		_orders.remove(indexOne);
		_orders.add(indexTwo, linkOne);
		linkOne.setIndex(indexTwo, false);

		if (this._ordersOperation == null) {
			this._ordersOperation = new ArrayList<OrderOperation>();
		}
		OrderOperationImpl o = new OrderOperationImpl(this,
				OrderWay.move_before, linkOne, linkTwo);
		this._ordersOperation.add(o);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#notifieChangedContent
	 * (fr.imag.adele.cadse.core.ContentChangeInfo[])
	 */
	public void notifieChangedContent(ContentChangeInfo[] change) {
		getWorkingCopy().notifieChangedContent(this, change);
	}

	private void orderMove(int from, int to, int delta) {
		if (_orders != null) {
			for (LinkDelta l : _orders) {
				int linkIndex = l.getIndex();
				if (linkIndex >= from && linkIndex <= to) {
					l.setIndex(linkIndex + delta, false);
				}
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#propagateValue(fr.imag
	 * .adele.cadse.core.attribute.IAttributeType)
	 */
	public Iterator<Item> propagateValue(IAttributeType<?> type) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#propagateValue(java.lang
	 * .String)
	 */
	public Iterator<Item> propagateValue(String key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNextKey(Key newK) throws CadseException {
		if (newK == null) {
			this._nextKey = null;
			return;
		}
		if (newK == DefaultKeyImpl.INVALID)
			return;

		if (_key == null && getBaseItem() != null)
			_key = getBaseItem().getKey();
		this._copy.checkKey(this, _key, newK);
		this._nextKey = newK;
	}

	@Override
	public Key getNextKey() {
		return _nextKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#recomputeKey(fr.imag.
	 * adele.cadse.core.key.ISpaceKey)
	 */
	public void setKey(Key newkey) throws CadseException {
		if (newkey == DefaultKeyImpl.INVALID)
			return;
		if (_key == null && getBaseItem() != null)
			_key = getBaseItem().getKey();
		this._copy.changeKey(this, _key, newkey);
		if (_keyDelta == null) {
			_keyDelta = new SpaceKeyDeltaImpl(this);
			if (getBaseItem() != null) {
				_keyDelta.setOldKey(getBaseItem().getKey());
			}
		}
		_keyDelta.setNewKey(newkey);

		_key = newkey;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#refresh()
	 */
	public void refresh() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#remove(fr.imag.adele.
	 * cadse.core.delta.LinkOperation)
	 */
	public void removeLink(LinkDelta linkOperation) {
		if (this._links == null) {
			return;
		}

		LinkKey key = new LinkKey(linkOperation.getLinkType(), linkOperation
				.getDestination().getId());
		linkOperation = _links.remove(key);
		removeInOrderArray(linkOperation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#removeContentManager()
	 */
	public void removeContentItem() {
		LinkDelta l = getOutgoingLink(CadseGCST.ITEM_lt_CONTENTS);
		if (l != null && !l.isDeleted()) {
			try {
				l.delete();
			} catch (CadseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#removeIncomingLink(fr
	 * .imag.adele.cadse.core.Link, boolean)
	 */
	public void removeIncomingLink(Link linkImpl, boolean notifie) {
		if (!(linkImpl instanceof LinkDelta))
			return;
		_incomingLinks = ArraysUtil.remove(LinkDelta.class, _incomingLinks,
				(LinkDelta) linkImpl);
	}

	void removeInOrderArray(LinkDelta linkOperation) {
		int i = _orders.indexOf(linkOperation);
		if (i != -1) {
			_orders.remove(i);
			orderMove(i, _orders.size(), -1);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#removeInParent()
	 */
	@Override
	public void removeInParent() {
		if (_links != null) {
			ArrayList<LinkDelta> arrayLinks = new ArrayList<LinkDelta>(_links
					.values());
			_links = null;
			for (LinkDelta l : arrayLinks) {
				l.removeInParent();
			}
		}
		if (_mappings != null) {
			for (MappingOperation mo : _mappings) {
				mo.removeInParent();
			}
		}
		super.removeInParent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#removeListener(fr.imag
	 * .adele.cadse.core.WorkspaceListener)
	 */
	public void removeListener(WorkspaceListener l) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#removeOutgoingItem(fr
	 * .imag.adele.cadse.core.LinkType, fr.imag.adele.cadse.core.Item)
	 */
	public Link removeOutgoingItem(LinkType linkType, Item destination)
			throws CadseException {
		return Accessor.removeOutgoingItem(this, linkType, destination);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#removeOutgoingLink(fr
	 * .imag.adele.cadse.core.Link, boolean)
	 */
	public void removeOutgoingLink(Link linkImpl, boolean notifie) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#resetContentIsChanged()
	 */
	@Deprecated
	public void resetContentIsChanged() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#resetTWAttributeModified
	 * ()
	 */
	public void resetTWAttributeModified() {
		List<Link> modiflinks = getOutgoingLinks(CadseGCST.ITEM_lt_MODIFIED_ATTRIBUTES);
		for (Link link : modiflinks) {
			try {
				link.delete();
			} catch (CadseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#setAttribute(fr.imag.
	 * adele.cadse.core.attribute.IAttributeType, java.lang.Object)
	 */
	public void setAttribute(IAttributeType<?> key, Object value) throws CadseException {
		setAttribute(key, value, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#setAttribute(fr.imag.
	 * adele.cadse.core.attribute.IAttributeType, java.lang.String,
	 * java.lang.Object, boolean)
	 */
	public void setAttribute(IAttributeType<?> key, Object newCurrentValue,
			boolean loaded) throws CadseException {
		setAttribute(key, newCurrentValue, loaded, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#setAttribute(fr.imag.
	 * adele.cadse.core.attribute.IAttributeType, java.lang.String,
	 * java.lang.Object, boolean)
	 */
	public SetAttributeOperation setAttribute(IAttributeType<?> key,
			Object newCurrentValue, boolean loaded, boolean notifie) throws CadseException {
		if (getBaseItem() != null && getBaseItem().isRuntime()) {
			return null;
		}
		if (_realItem != null)
			_realItem.commitSetAttribute(key, newCurrentValue);
		String attributeName = key.getName();

		if (attributeName.equals(ItemTypeImpl.ATTR_SHORT_NAME)
				|| attributeName.equals(ItemTypeImpl.SHORT_NAME_KEY)) {
			attributeName = CadseGCST.ITEM_at_NAME;
			key = CadseGCST.ITEM_at_NAME_;
		}
		if (!loaded && isRuntime()) {
			addError(Messages.cannot_set_attribute_not_modifiable,
					attributeName, newCurrentValue); //$NON-NLS-2$ //$NON-NLS-3$
		}
		if (attributeName.equals(Accessor.ATTR_PARENT_ITEM_ID)) {
			setParentFromAtt(newCurrentValue);
			return null;
		}

		SetAttributeOperation setAtt = null;
		setAtt = getSetAttributeOperation(key, true);

		if (setAtt != null) {
			// compare the currentValue with the precedent value (newValue
			// here)
			// if equals, no modification, no send event ...
			// it's important, avoid stack over flow happening
			Object currentValue = setAtt.getCurrentValue();
			if (key != null) {
				if (!key.isValueModified(currentValue, newCurrentValue)) {
					return null;
				}
			} else {
				if (Convert.equals(currentValue, newCurrentValue)) {
					return null;
				}
			}
			setAtt.setPrecCurrentValue(setAtt.getCurrentValue());
			setAtt.setCurrentValue(newCurrentValue);
		} else {
			setAtt = new SetAttributeOperationImpl(this, key, newCurrentValue,
					null);
			add(setAtt);
		}
		if (notifie && !loaded)
			_copy.validateChangeAttribute(this, setAtt);

		setAtt.setLoaded(loaded);
		if (notifie && !loaded) {
			getWorkingCopy().notifyChangeAttribute(this, setAtt);
		}
		return setAtt;
	}

	/**
	 * Gets the parent in storage.
	 * 
	 * @return the parent in storage
	 */
	void setParentFromAtt(Object value) {
		if (value instanceof CompactUUID) {
			// migration
			value = ((CompactUUID) value).toUUID();
		}
		UUID parentId = (UUID) value;
		if (parentId != null) {
			final ItemDelta parent = getWorkingCopy().getItem(parentId);
			if (parent != null) {
				setParent(parent, null);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#setComponents(java.util
	 * .Set)
	 */
	public void setComponents(Set<ItemDescriptionRef> comp)
			throws CadseException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#setDerivedLinks(java.
	 * util.Set)
	 */
	public void setDerivedLinks(Set<DerivedLinkDescription> derivedLinks) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#setIsStatic(boolean)
	 */
	public void setIsRuntime(boolean flag) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#setModified(boolean)
	 */
	public void setModified(boolean value) throws CadseException {
		setModified(value, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#setModified(boolean,
	 * boolean)
	 */
	public void setModified(boolean value, boolean loaded)
			throws CadseException {
		setAttribute(null, Item.IS_MODIFIED_KEY, value, loaded);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Item) {
			return ((Item) obj).getId().equals(getId());
		}
		return super.equals(obj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#setOutgoingItem(fr.imag
	 * .adele.cadse.core.LinkType, java.util.Collection)
	 */
	public Collection<Link> setOutgoingItems(LinkType lt, Collection<Item> value)
			throws CadseException {
		List<Link> removed = new ArrayList<Link>();
		List<Link> modified = new ArrayList<Link>();

		for (Link ol : getOutgoingLinks()) {
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
			deleteLink.delete();
		}
		for (Item item : value) {
			modified.add(createLink(lt, item));
		}
		return modified;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#setOutgoingItemOne(fr
	 * .imag.adele.cadse.core.LinkType, fr.imag.adele.cadse.core.Item)
	 */
	public Link setOutgoingItem(LinkType lt, Item destination)
			throws CadseException {
		if (lt.getMax() != 1) {
			throw new CadseIllegalArgumentException(
					Messages.error_bad_link_type_max_not_equal_to_one, lt
							.getName());
		}
		LinkDelta l = getOutgoingLink(lt);
		if (l != null) {
			if (l.getDestinationId().equals(destination.getId())) {
				return l;
			} else {
				l.delete();
			}
		}

		return createLink(lt, destination);
	}

	@Override
	public void setParent(Item parent, LinkType lt) {
		if (parent == null)
			throw new CadseIllegalArgumentException(
					Messages.cannot_set_parent_to_null, this, lt);

		try {
			setParent(_copy.getItem(parent), lt, true, true);
		} catch (CadseException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#setParent(fr.imag.adele
	 * .cadse.core.Item, fr.imag.adele.cadse.core.LinkType)
	 */
	public void setParent(ItemDelta parent, LinkType lt,
			boolean createLinkIfNeed, boolean notify) throws CadseException {
		if (parent != null) {
			if (parent == _parentItem) {
				if (lt != null)
					this._parentLinkType = lt;
				return;
			}

			if (_parentItem != null && _oldParentItem == null) {
				_oldParentItem = _parentItem;
			}
			this._parentItem = (ItemDelta) parent;
			if (lt == null) {
				if (_types != null)
					for (ItemType it : _types) {
						lt = it.getIncomingPart(parent.getType());
						if (lt != null)
							break;
					}

			}
			if (lt == null) {
				if (_types != null)
					for (ItemType it : _types) {
						lt = it.getIncomingOne(parent.getType());
						if (lt != null)
							break;
					}
			}

			if (lt != null) {
				this._parentLinkType = lt;
			}

			if (createLinkIfNeed) {

				LinkDelta l = getOutgoingLink(CadseGCST.ITEM_lt_PARENT);
				if (l == null)
					createLink(CadseGCST.ITEM_lt_PARENT, _parentItem, notify, true);
				else if (l.getDestination() != parent) {
					l.delete();
					createLink(CadseGCST.ITEM_lt_PARENT, _parentItem, notify, true);
				}

				if (lt != null) {
					if (_oldParentItem != null) {
						LinkDelta ltoparent = _oldParentItem.getOutgoingLink(
								lt, getId());
						if (ltoparent != null)
							ltoparent.delete(new DeleteOperationImpl(null,
									null, 0));
					}
					l = _parentItem.getOutgoingLink(CadseGCST.ITEM_lt_PARENT,
							getId());
					if (l == null)
						_parentItem.createLink(lt, this);
				}
			}

		}
	}

	public ItemDelta getParentItem() {
		return _parentItem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#setReadOnly(boolean)
	 */
	public void setReadOnly(boolean readOnly) throws CadseException {
		setReadOnly(readOnly, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#setReadOnly(boolean,
	 * boolean)
	 */
	public void setReadOnly(boolean readOnly, boolean loaded)
			throws CadseException {
		setAttribute(CadseGCST.ITEM_at_ITEM_READONLY_, readOnly, loaded);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seefr.imag.adele.cadse.core.delta.ItemOperationItf#
	 * setRecomputeComponantsAndDerivedLink(boolean)
	 */
	public void setRecomputeComponantsAndDerivedLink(boolean b) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#setRequierNewRev(boolean)
	 */
	public void setRequierNewRev(boolean flag) throws CadseException {
		if (flag) {
			setRevModified(true);
		}
		setAttribute(CadseGCST.ITEM_at_REQUIRE_NEW_REV_, flag);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#setRevModified(boolean)
	 */
	public void setRevModified(boolean flag) throws CadseException {
		setAttribute(CadseGCST.ITEM_at_REV_MODIFIED_, flag);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#setShortName(java.lang
	 * .String)
	 */
	public void setName(String shortname) throws CadseException {
		setName(shortname, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#setShortName(java.lang
	 * .String, boolean)
	 */
	public void setName(String shortname, boolean loaded) throws CadseException {
		setAttribute(CadseGCST.ITEM_at_NAME_, shortname, loaded);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#setState(fr.imag.adele
	 * .cadse.core.ItemState)
	 */
	public void setState(ItemState modifing) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#setTWAttributeModified
	 * (fr.imag.adele.cadse.core.attribute.IAttributeType, boolean)
	 */
	public void setTWAttributeModified(IAttributeType<?> att, boolean v)
			throws CadseException {
		LinkDelta l = getOutgoingLink(CadseGCST.ITEM_lt_MODIFIED_ATTRIBUTES,
				att.getId());
		if (l == null && !v) {
			return;
		}
		if (l != null && v) {
			return;
		}

		if (isDeleted() || isRuntime()) {
			return;
		}

		if (!v) {
			l.delete();
		} else {
			if (att.getType() == null) {
				throw new CadseException(
						Messages.cannot_create_link_to_an_untyped_item);
			}
			createLink(CadseGCST.ITEM_lt_MODIFIED_ATTRIBUTES, att);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#setType(fr.imag.adele
	 * .cadse.core.ItemType)
	 */
	public void setType(ItemType selectedItemType) {
		_itemType = selectedItemType;
		if (_itemType != null)
			addItemType(selectedItemType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#setUniqueName(java.lang
	 * .String)
	 */
	@Deprecated
	public void setUniqueName(String uniqueName) throws CadseException {
		setAttribute(CadseGCST.ITEM_at_QUALIFIED_NAME_, uniqueName, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#setUniqueName(java.lang
	 * .String)
	 */
	@Override
	public void setQualifiedName(String qName) throws CadseException {
		setAttribute(CadseGCST.ITEM_at_QUALIFIED_NAME_, qName, false);
		_copy.changeQualifiedName(this, qName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#setUniqueName(java.lang
	 * .String, boolean)
	 */
	@Override
	public void setQualifiedName(String uniqueName, boolean loaded) throws CadseException {
		setAttribute(CadseGCST.ITEM_at_QUALIFIED_NAME_, uniqueName, loaded);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#setUpdate(boolean)
	 */
	@Override
	public void setUpdate(boolean update) {
		this._update = update;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#setValid(boolean)
	 */
	@Override
	public void setValid(boolean valid) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#setValid(boolean,
	 * boolean)
	 */
	@Override
	public void setValid(boolean valid, boolean loaded) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#shadow(boolean)
	 */
	@Override
	public void shadow(boolean deleteContent) throws CadseException {
		delete(
				null,
				DeleteOperationImpl.DELETE_ANNOTATION_LINK
						| DeleteOperationImpl.DELETE_PART_LINK
						| (deleteContent ? (DeleteOperationImpl.DELETE_CONTENT | DeleteOperationImpl.DELETE_MAPPING)
								: 0));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#syncOutgoingLinks()
	 */
	@Override
	public void syncOutgoingLinks() {
		if (_orders != null) {
			return;
		}

		Item base = getBaseItem();
		if (base != null) {
			List<? extends Link> baselinks = base.getOutgoingLinks();
			if (baselinks != null) {
				int index = 0;
				for (Link bl : baselinks) {
					if (bl.isDerived()) {
						continue;
					}
					getOrCreateLinkOperation(bl, index++);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb, ""); //$NON-NLS-1$
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seefr.imag.adele.cadse.core.delta.ItemOperationItf#toString(java.lang.
	 * StringBuilder, java.lang.String)
	 */
	public void toString(StringBuilder sb, String tab) {
		sb.append(tab);
		toStringShort(sb);
		sb.append("\n"); //$NON-NLS-1$
		toStringAttributes(sb, tab);
		if (_orders != null) {
			for (LinkDelta linkOper : _orders) {
				if (linkOper.isModified()) {
					linkOper.toString(" - ", sb, tab); //$NON-NLS-1$
				}
			}
		}
		if (_links != null) {
			for (LinkDelta linkOper : _links.values()) {
				if (linkOper.isDeleted()) {
					linkOper.toString(" - ", sb, tab); //$NON-NLS-1$
				}
			}
		}
		if (_ordersOperation != null) {
			for (OrderOperation o : _ordersOperation) {
				if (o.getKind() == OrderWay.move_after) {
					sb
							.append(" - move  ").append(o.getFrom().getIndex()).append(" after ") //$NON-NLS-1$ //$NON-NLS-2$
							.append(o.getTo().getIndex()).append("\n"); //$NON-NLS-1$
				} else if (o.getKind() == OrderWay.move_before) {
					sb
							.append(" - move ").append(o.getFrom().getIndex()).append(" before ") //$NON-NLS-1$ //$NON-NLS-2$
							.append(o.getTo().getIndex()).append("\n"); //$NON-NLS-1$
				}
			}
		}
		if (_mappings != null) {
			for (MappingOperation mo : _mappings) {
				sb.append(" - "); //$NON-NLS-1$
				mo.toStringShort(sb);
				sb.append("\n"); //$NON-NLS-1$
			}
		}
		if (_doubleClick) {
			sb.append(" - doubleClick\n"); //$NON-NLS-1$
		}
		if (_keyDelta != null) {
			sb.append(" - keydelta "); //$NON-NLS-1$
			_keyDelta.toString(sb, tab + "  "); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#toStringShort(java.lang
	 * .StringBuilder)
	 */
	public void toStringShort(StringBuilder sb) {
		if (isDeleted()) {
			sb.append("Delete "); //$NON-NLS-1$
		}
		if (isAdded()) {
			sb.append("Added "); //$NON-NLS-1$
		}
		if (isLoaded()) {
			sb.append("Loaded "); //$NON-NLS-1$
		}

		sb.append("Item "); //$NON-NLS-1$
		if (_types == null)
			sb.append("<?type?> ");
		else {
			sb.append("<");
			for (ItemType ty : _types) {
				sb.append(ty.getName());
				sb.append(", ");
			}
			sb.setLength(sb.length() - 2);
			sb.append(">");
		}
		String sn = getName();
		if (sn != null) {
			sb.append(" ").append(sn); //$NON-NLS-1$
		}
		sb.append(" ").append(_id); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.ItemOperationItf#unload()
	 */
	public void unload() throws CadseException {
		delete(null, DeleteOperationImpl.DELETE_ANNOTATION_LINK
				| DeleteOperationImpl.DELETE_PART_LINK
				| DeleteOperationImpl.DELETE_CONTENT
				| DeleteOperationImpl.DELETE_MAPPING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf#update(fr.imag.adele.
	 * cadse.core.IWorkingLoadingItems,
	 * fr.imag.adele.cadse.core.delta.ItemOperationItf,
	 * fr.imag.adele.cadse.core.IWorkspaceNotifier)
	 */
	public void update(IWorkingLoadingItems items, ItemDelta desc,
			IWorkspaceNotifier notifie) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean setFlag(int f, boolean flag) {
		if (_realItem != null)
			return _realItem.setFlag(f, flag);
		return false;
	}

	@Override
	public void setVersion(int version) throws CadseException {
		setAttribute(CadseGCST.ITEM_at_TW_VERSION_, Integer.valueOf(version));
	}

	@Override
	public void setLoaded(boolean loaded) {
		if (isRuntime()) {
			throw new CadseIllegalArgumentException(
					Messages.cannot_load_a_static_item + getName(), this);
		}
		super.setLoaded(loaded);
	}

	@Override
	public LogicalWorkspaceTransaction getCopy() {
		return _copy;
	}

	@Override
	public boolean isMember() {
		return _group != null;
	}

	@Override
	public boolean isMemberOf(Item item) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ItemType getGroup() {
		return _group;
	}

	@Override
	public Pages getCreationPages(NewContext context) throws CadseException {
		return PageRuntimeModel.INSTANCE.getCreationPages(this, context);
	}

	@Override
	public Pages getModificationPages(FilterContext context) {
		return PageRuntimeModel.INSTANCE.getModificationPages(this, context);
	}

	@Override
	public CadseRuntime getCadse() {
		return _cadse;
	}

	@Override
	public UUID getCadseId() {
		if (_cadse == null)
			return null;
		return _cadse.getId();
	}
	
	@Override
	public void setCadse(CadseRuntime cr) {
		_cadse = cr;
		if (_realItem != null)
			_realItem.setCadse(cr);
	}

	@Override
	public void addItemType(ItemType it) {
		_types = ArraysUtil.add(ItemType.class, _types, it);
	}

	@Override
	public LinkDelta loadLink(int linkId, LinkType linkType, ItemDelta destItem)
			throws CadseException {
		LinkDelta ld = loadLink(linkType, destItem);
		ld.setObjectID(linkId);
		return ld;
	}

	@Override
	public void setCadseId(int cadseID) {
		_cadseID = cadseID;
	}

	@Override
	public void setParentId(int parentID) {
		_parentID = parentID;
	}

	public boolean isProxy() {
		return false;
	}

	public <T> T adapt(Class<T> clazz) {
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
	public int getObjectId() {
		return _localId;
	}

	@Override
	public void setUUID(UUID uuid) {
	}

	@Override
	public Exporter[] getExporter(String exporterType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getIdInPackage() {
		// TODO Auto-generated method stub
		return 0;
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

	/**
	 * La valeur est délégué au group. Elle ne peut pas etre instancié dans le membre.
	 */
	@Override
	public boolean isDelegatedValue(IAttributeType<?> attr) {
		return AbstractGeneratedItem._isDelegatedValue(this, attr, _group);
	}

}
