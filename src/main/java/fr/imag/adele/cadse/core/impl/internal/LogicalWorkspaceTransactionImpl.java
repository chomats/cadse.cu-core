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
package fr.imag.adele.cadse.core.impl.internal;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.imag.adele.cadse.core.CadseDomain;
import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.CadseRuntime;
import fr.imag.adele.cadse.core.ContentChangeInfo;
import fr.imag.adele.cadse.core.EventFilter;
import fr.imag.adele.cadse.core.ExtendedType;
import fr.imag.adele.cadse.core.IItemManager;
import fr.imag.adele.cadse.core.INamedUUID;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemDescription;
import fr.imag.adele.cadse.core.ItemDescriptionRef;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkDescription;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.Messages;
import fr.imag.adele.cadse.core.ProjectAssociation;
import fr.imag.adele.cadse.core.TypeDefinition;
import fr.imag.adele.cadse.core.WSModelState;
import fr.imag.adele.cadse.core.WorkspaceListener;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.attribute.SetAttrVal;
import fr.imag.adele.cadse.core.impl.CadseCore;
import fr.imag.adele.cadse.core.impl.internal.delta.CreateOperationImpl;
import fr.imag.adele.cadse.core.impl.internal.delta.DeleteOperationImpl;
import fr.imag.adele.cadse.core.impl.internal.delta.ItemDeltaImpl;
import fr.imag.adele.cadse.core.impl.internal.delta.ItemOrLinkDeltaImpl;
import fr.imag.adele.cadse.core.impl.internal.delta.SetAttributeOperationImpl;
import fr.imag.adele.cadse.core.internal.ILoggableAction;
import fr.imag.adele.cadse.core.internal.IWorkspaceNotifier;
import fr.imag.adele.cadse.core.internal.InternalLogicalWorkspace;
import fr.imag.adele.cadse.core.key.Key;
import fr.imag.adele.cadse.core.key.KeyDefinition;
import fr.imag.adele.cadse.core.transaction.AbstractLogicalWorkspaceTransactionListener;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransaction;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransactionListener;
import fr.imag.adele.cadse.core.transaction.delta.DeleteOperation;
import fr.imag.adele.cadse.core.transaction.delta.ImmutableWorkspaceDelta;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.transaction.delta.LinkDelta;
import fr.imag.adele.cadse.core.transaction.delta.MappingOperation;
import fr.imag.adele.cadse.core.transaction.delta.OperationTypeCst;
import fr.imag.adele.cadse.core.transaction.delta.OrderOperation;
import fr.imag.adele.cadse.core.transaction.delta.SetAttributeOperation;
import fr.imag.adele.cadse.core.transaction.delta.WLWCOperation;
import fr.imag.adele.cadse.core.transaction.delta.WLWCOperationImpl;
import fr.imag.adele.cadse.core.ui.view.FilterContext;
import fr.imag.adele.cadse.core.ui.view.NewContext;
import fr.imag.adele.cadse.core.util.ComputeElementOrder;
import fr.imag.adele.cadse.core.util.Convert;
import fr.imag.adele.cadse.core.util.ElementsOrder;
import fr.imag.adele.cadse.core.util.HashList;
import fr.imag.adele.cadse.core.var.ContextVariable;
import fr.imag.adele.cadse.util.ArraysUtil;
import fr.imag.adele.cadse.util.NLS;

public class LogicalWorkspaceTransactionImpl implements
		LogicalWorkspaceTransaction, InternalLogicalWorkspace {

	private final InternalLogicalWorkspace base;

	private Map<UUID, WLWCOperationImpl> all_operations;

	private Map<UUID, ItemDelta> _operations;

	/** The items. */
	private Map<UUID, ItemDelta> items;

	/** The items. */
	private Map<UUID, ItemDelta> items_deleted;

	/** The items_by_key. */
	private Map<Key, ItemDelta> items_by_key_deleted;

	/** The items_by_key. */
	private Map<Key, ItemDelta> items_by_key;

	/** The items_by_unique_name. */
	private Map<String, ItemDelta> items_by_unique_name;

	/** The items_by_unique_name. */
	private Map<String, ItemDelta> items_by_unique_name_deleted;
	ILoggableAction log = null;
	private WSModelState state = WSModelState.COPY_WRITE;
	LogicalWorkspaceTransactionListener[] _logicalWorkspaceTransactionListeners;

	private boolean update = false;
	private boolean forceToSave = false;
	private boolean forcetoLoad = false;
	private Collection<Item> loadedItems = null;
	private IWorkspaceNotifier notifier = null;
	private Collection<ProjectAssociation> projectAssociationSet = null;
	private boolean echec;

	private boolean _commitState;

	private Logger _logger = Logger
			.getLogger("fr.imag.adele.cadse.logicalworkspacetransaction");

	private ContextVariable _newContextVariable;

	private ContextVariable _oldContextVariable;

	public LogicalWorkspaceTransactionImpl(InternalLogicalWorkspace base,
			LogicalWorkspaceTransactionListener[] workspaceLogiqueCopyListeners) {
		this.base = base;
		_operations = new HashMap<UUID, ItemDelta>();
		this.items_deleted = new HashMap<UUID, ItemDelta>();
		this.items_by_key_deleted = new HashMap<Key, ItemDelta>();
		this.items_by_unique_name_deleted = new HashMap<String, ItemDelta>();
		this.items = new HashMap<UUID, ItemDelta>();
		this.items_by_key = new HashMap<Key, ItemDelta>();
		this.items_by_unique_name = new HashMap<String, ItemDelta>();
		this._logicalWorkspaceTransactionListeners = workspaceLogiqueCopyListeners;
		addLogicalWorkspaceTransactionListener(new WoLProWCListener());
		addLogicalWorkspaceTransactionListener(new TeamWorkStatePropagationWLWCListener());
	}

	public boolean canDeleteInverseLink(Link link) {
		return base.canDeleteInverseLink(link);
	}

	public boolean canDeleteLink(Link link) {
		return base.canDeleteLink(link);
	}

	/**
	 * ajout un item, et eventuellement un lien entre le parent et l'item creer.
	 */
	public ItemDelta createItem(ItemType it, Item parent, LinkType lt, UUID id,
			String uniqueName, String shortName) throws CadseException {
		check_write();
		ItemDelta ret = actionAddItem(new ItemDescriptionRef(id, it,
				uniqueName, shortName), parent == null ? null : parent.getId(),
				lt);
		return ret;
	}

	@Override
	public ItemDelta createItem(NewContext c) throws CadseException {
		return actionAddItem(c);
	}

	public ItemDelta createItem(ItemType it, Item parent, LinkType lt)
			throws CadseException {
		check_write();
		ItemDelta ret = actionAddItem(new ItemDescriptionRef(UUID.randomUUID(),
				it, null, null), parent == null ? null : parent.getId(), lt);
		return ret;
	}

	public void check_write() {
		if (state != WSModelState.COPY_WRITE) {
			throw new IllegalStateException("Working copy is not writable.");
		}
	}

	public LogicalWorkspaceTransaction createTransaction() {
		if (base.getCadseDomain().inDevelopmentMode()) {
			return new LogicalWorkspaceTransactionImpl(this,
					getLogicalWorkspaceTransactionListener());
		}
		throw new UnsupportedOperationException();

	}

	public boolean existsItem(Item item, String shortName) {
		Key key = LogicalWorkspaceImpl.getKeyItem(item, shortName, _logger);
		if (key != null) {
			Item foundItem = getItem(key);
			if (foundItem == null || foundItem == item)
				return false;

			return true;
		}
		if (!item.getType().hasQualifiedNameAttribute()) {
			return false;
		}
		String un = item.getType().getItemManager().computeQualifiedName(item,
				shortName, item.getPartParent(), item.getPartParentLinkType());
		return containsUniqueName(un);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #containsUniqueName(java.lang.String)
	 */
	public boolean containsUniqueName(String un) {
		if (un == null || un == Item.NO_VALUE_STRING) {
			return true;
		}
		if (this.items_by_unique_name.containsKey(un)) {
			return true;
		}
		if (this.items_by_unique_name_deleted.containsKey(un)) {
			return false;
		}

		return base.containsUniqueName(un);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #containsSpaceKey(fr.imag.adele.cadse.core.key.Key)
	 */
	public boolean containsSpaceKey(Key key) {
		if (this.items_by_key.containsKey(key)) {
			return true;
		}
		if (this.items_by_key_deleted.containsKey(key)) {
			return false;
		}
		return base.containsSpaceKey(key);
	}

	public boolean existsItem(Item item) {
		Key key = LogicalWorkspaceImpl.getKeyItem(item, null, _logger);
		if (key != null) {
			Item foundItem = getItem(key);
			if (foundItem == null || foundItem == item)
				return false;

			return true;
		}
		if (!item.getType().hasQualifiedNameAttribute()) {
			return false;
		}

		String un = item.getQualifiedName();
		if (un == null || un == Item.NO_VALUE_STRING)
			return true;
		Item foundItem = getItem(un);
		return foundItem != null && foundItem != item;
	}

	public String[] getCadseName() {
		return base.getCadseName();
	}

	public int[] getCadseVersion() {
		return base.getCadseVersion();
	}

	public ItemDelta getItem(UUID id) {
		return getItem(id, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #getItemOperation(fr.imag.adele.cadse.core.UUID)
	 */
	public ItemDelta getItemOperation(UUID id) {
		return _operations.get(id);
	}

	public ItemDelta getItem(Item item) {
		return getItem(item.getId(), false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #getItem(fr.imag.adele.cadse.core.UUID, boolean)
	 */
	public ItemDelta getItem(UUID id, boolean showDeleteItem) {
		ItemDelta oper = this._operations.get(id);
		if (oper != null) {
			if (oper.isDeleted() && !showDeleteItem) {
				return null;
			}
			return oper;
		}
		Item baseitem = base.getItem(id);
		if (baseitem == null) {
			return null;
		}
		try {
			return getOrCreateItemOperation(id);
		} catch (CadseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public ItemDelta getItem(Key key) {

		ItemDelta ret = null;
		if ((ret = this.items_by_key.get(key)) != null) {
			return ret;
		}
		if (this.items_by_key_deleted.containsKey(key)) {
			return null;
		}
		return oper(base.getItem(key));
	}

	public ItemDelta getItem(String qualifiedName) {
		if (qualifiedName == null || qualifiedName == Item.NO_VALUE_STRING) {
			return null;
		}
		ItemDelta ret = this.items_by_unique_name.get(qualifiedName);

		if (ret != null) {
			return ret;
		}
		if (this.items_by_unique_name_deleted.containsKey(qualifiedName)) {
			return null;
		}

		return oper(base.getItem(qualifiedName));
	}

	private ItemDelta oper(Item item) {
		if (item == null) {
			return null;
		}
		return getOrCreateItemOperation(item);
	}

	public ItemDelta getItemByName(TypeDefinition type, String name) {
		return oper(base.getItemByName(type, name));
	}

	public Collection<Item> getItems() {
		return (base.getItems());
	}

	public List<Item> getItems(TypeDefinition it) {
		return base.getItems(it);
	}

	public List<Item> getItems(String it) {
		return base.getItems(it);
	}

	public ItemType getItemType(UUID id) {
		return getItemType(null, id, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #getItemType(fr.imag.adele.cadse.core.UUID, boolean)
	 */
	public ItemType getItemType(UUID cadseId, UUID id,
			boolean createUnresolvedType) {
		ItemDelta itemTypeDelta = getItem(id, false);

		// ItemType itemType = base.getItemType(id);
		if (createUnresolvedType && itemTypeDelta == null) {
			try {
				return createUnresolvedItemType(cadseId, id, null, null);
			} catch (CadseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		if (itemTypeDelta == null)
			return null;

		if (itemTypeDelta.isModified())
			return itemTypeDelta.getAdapter(ItemType.class);
		return (ItemType) itemTypeDelta.getBaseItem();
	}

	public TypeDefinition getTypeDefinition(UUID cadseId, UUID id,
			boolean createUnresolvedType) {
		ItemDelta itemTypeDelta = getItem(id, false);

		// ItemType itemType = base.getItemType(id);
		if (createUnresolvedType && itemTypeDelta == null) {
			try {
				return createUnresolvedItemType(cadseId, id, null, null);
			} catch (CadseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		if (itemTypeDelta == null)
			return null;

		return itemTypeDelta.getAdapter(TypeDefinition.class);
	}

	public ItemType getItemTypeByName(String shortName) {
		return base.getItemTypeByName(shortName);
	}

	public Collection<ItemType> getItemTypes() {
		return base.getItemTypes();
	}

	public WSModelState getState() {
		return state;
	}

	public List<Item> getUnresolvedItem() {
		return base.getUnresolvedItem();
	}

	public List<Link> getUnresolvedLink(UUID id) {
		return base.getUnresolvedLink(id);
	}

	public List<Link> getUnresolvedLinks() {
		return base.getUnresolvedLinks();
	}

	public CadseDomain getCadseDomain() {
		return base.getCadseDomain();
	}

	public ItemDelta loadItem(UUID id, UUID type) throws CadseException {
		TypeDefinition it = findTypeDefinition(type, null, true);
		return loadItem(id, (ItemType) it);
	}

	@Override
	public ItemDelta loadItem(UUID id, ItemType type) throws CadseException {
		ItemDelta loadingItem = _operations.get(id);
		if (loadingItem != null) {
			return loadingItem;
		}

		return new ItemDeltaImpl(this, new UUID(id.getMostSignificantBits(), id
				.getLeastSignificantBits()), type, true);
	}

	public Item loadItem(ItemDescription desc, boolean update)
			throws CadseException {
		ItemDelta ret = loadItem(desc);
		ret.setUpdate(update);
		return ret;
	}

	public ItemDelta loadItem(Item desc) {
		return getOrCreateItemOperation(desc);
		/*
		 * ItemDelta ret = loadItem(desc.getId(), desc.getType().getId());; try
		 * { if (desc.getQualifiedName() != null) {
		 * ret.setQualifiedName(desc.getQualifiedName(), true); } if
		 * (desc.getName() != null) { ret.setName(desc.getName(), true); } }
		 * catch (CadseException e) { // ignored }
		 * 
		 * return ret;
		 */
	}

	public ItemDelta loadItem(ItemDescription desc) throws CadseException {
		ItemDelta ret;
		try {
			ret = loadItem(desc.getId(), desc.getType());
			if (desc.getQualifiedName() != null) {
				ret.setQualifiedName(desc.getQualifiedName(), true);
			}
			if (desc.getName() != null) {
				ret.setName(desc.getName(), true);
			}
			for (Map.Entry<IAttributeType<?>, Object> entry : desc
					.getAttributes().entrySet()) {
				IAttributeType<?> attributeName = entry.getKey();
				if (attributeName.equals(ItemTypeImpl.ATTR_SHORT_NAME)
						|| attributeName.equals(ItemTypeImpl.SHORT_NAME_KEY)
						|| attributeName.equals(CadseGCST.ITEM_at_NAME)) {
					attributeName = CadseGCST.ITEM_at_NAME_;
					if (entry.getValue() == null
							|| Item.NO_VALUE_STRING.equals(entry.getValue())) {
						System.err
								.println("short name is overwritten in attribute");
						continue;
					}
				}
				ret.loadAttribute(attributeName, entry.getValue());
			}
			for (LinkDescription l : desc.getLinks()) {
				ItemDelta destItem = loadItem(l.getDestination());
				ret.loadLink(l.getLinkType(), destItem);
			}
			ret.finishLoad();
		} catch (CadseException e) {
			throw new CadseException(e.getMessage(), e);
		}

		return ret;
	}

	public ItemDelta loadItem(ItemDescriptionRef desc) {
		ItemDelta loadingItem = _operations.get(desc.getId());
		if (loadingItem != null && loadingItem.isLoaded()) {
			return loadingItem;
		}
		try {
			loadingItem = loadItem(desc.getId(), desc.getTypeObject());
			if (desc.getQualifiedName() != null) {
				loadingItem.setQualifiedName(desc.getQualifiedName(), true);
			}
			if (desc.getName() != null) {
				loadingItem.setName(desc.getName(), true);
			}

		} catch (CadseException e) {
			// ignored
		}

		return loadingItem;
	}

	public Collection<Item> loadItems(
			Collection<ItemDescription> itemdescription, boolean update,
			boolean forceToSave, boolean forceLoad,
			IWorkspaceNotifier notifier, Map<UUID, String> unresolvedType,
			Collection<ProjectAssociation> projectAssociationSet) {
		throw new UnsupportedOperationException();
	}

	public List<Item> loadItems(Collection<URL> itemdescription,
			Collection<ProjectAssociation> projectAssociationSet,
			boolean update, boolean forceToSave) throws CadseException,
			IOException {
		throw new UnsupportedOperationException();
	}

	public void loadItems(Collection<URL> itemdescription)
			throws CadseException, IOException {
		// for (URL itemURL : itemdescription) {
		// CadseCore.loadFromPersistence(this, itemURL);
		// }
	}

	public void loadMetaModel() {

	}

	@Override
	public void setState(WSModelState state) {
		this.state = state;
	}

	@Override
	public <T> void actionAddAttribute(UUID itemId, IAttributeType<T> key,
			T value) throws CadseException, CadseException {
		check_write();
		if (log != null) {
			log.actionAddAttribute(itemId, key, value);
		}
		ItemDelta itemOper = getOrCreateItemOperation(itemId);
		Object oldValue = null;
		SetAttributeOperation oldSetOperation = itemOper
				.getSetAttributeOperation(key);
		if (oldSetOperation == null) {
			Item t = itemOper.getBaseItem();
			if (t != null && t.isResolved()) {
				oldValue = t.getAttribute(key);
			}
		} else {
			oldValue = oldSetOperation.getOldValue();
		}

		SetAttributeOperation attOper = new SetAttributeOperationImpl(itemOper,
				key, value, oldValue);
		((ItemOrLinkDeltaImpl) itemOper).add(attOper);
		notifyChangeAttribute(itemOper, attOper);
	}

	public <T> void actionAddAttribute(LinkDescription linkDescription,
			IAttributeType<T> key, T value) throws CadseException,
			CadseException {
		check_write();
		if (log != null) {
			log.actionAddAttribute(linkDescription, key, value);
		}
		LinkDelta linkOper = getLinkOperation(linkDescription, false);
		SetAttributeOperation attOper = new SetAttributeOperationImpl(linkOper,
				key, value, null);
		linkOper.add(attOper);
		notifyChangeAttribute(linkOper, attOper);
	}

	public ItemDelta actionAddItem(NewContext context) throws CadseException {
		check_write();
		UUID itemId = UUID.randomUUID();
		ItemDeltaImpl ret = new ItemDeltaImpl(this, itemId, context
				.getDestinationType(), false);

		if (ret.isAdded()) {
			return ret;
		}
		if (ret.isDeleted()) {
			return ret;
		}

		CreateOperationImpl createOperation = new CreateOperationImpl(ret);
		ret.setCreateOperation(createOperation);
		createOperation.addInParent();

		ret.addInParent();

		List<LinkDelta> linkToNotifie = new ArrayList<LinkDelta>();
		{
			LinkType[] outLT = context.getOutgoingLinkType();
			Item[] outDest = context.getOutgoingDestinations();
			for (int i = 0; i < outDest.length; i++) {
				ItemDelta deltaDest = getOrCreateItemOperation(outDest[i]);
				linkToNotifie.add(ret.createLink(outLT[i], deltaDest, false, false));
			}
		}
		Item parent = context.getPartParent();
		if (parent != null) {
			// set field and store in this parentItem and link part
			ret.setParent(getOrCreateItemOperation(parent), context
					.getPartLinkType(), false, false);
		}

		{
			LinkType[] inLT = context.getIncomingLinkType();
			Item[] inSrc = context.getIncomingSources();
			if (inLT != null) {
				for (int i = 0; i < inSrc.length; i++) {
					ItemDeltaImpl deltaSrc = (ItemDeltaImpl) getOrCreateItemOperation(inSrc[i]);
					linkToNotifie.add(deltaSrc.createLink(inLT[i], ret, false, false));
				}
			}
		}
		List<SetAttributeOperation> setAttToNotifie = new ArrayList<SetAttributeOperation>();
		SetAttrVal<?>[] setAttrs = context.getSetAttrs();
		if (setAttrs != null) {
			for (int i = 0; i < setAttrs.length; i++) {
				IAttributeType<?> def = setAttrs[i].getAttrDef();
				Object v = setAttrs[i].getValue();
				SetAttributeOperation setAtt = ret.setAttribute(def, v, true,
						false);
				if (setAtt == null)
					continue;
				setAttToNotifie.add(setAtt);
			}
		}
		validateCreatedItem(ret);
		for (LinkDelta createdLink : linkToNotifie) {
			validateCreatedLink(createdLink);
		}
		for (SetAttributeOperation setAtt : setAttToNotifie) {
			validateChangeAttribute(ret, setAtt);
		}
		notifyCreatedItem(ret);

		for (LinkDelta createdLink : linkToNotifie) {
			notifyCreatedLink(createdLink);
		}
		for (SetAttributeOperation setAtt : setAttToNotifie) {
			notifyChangeAttribute(ret, setAtt);
		}

		IAttributeType<?>[] attributes = ret.getType().getAllAttributeTypes();
		for (IAttributeType<?> attributeType : attributes) {
			Object v = attributeType.getDefaultValue();
			if (v == null)
				continue;
			ret.setAttribute(attributeType, v);
		}

		return ret;
	}

	public ItemDelta actionAddItem(ItemDescriptionRef itemDescriptionRef,
			UUID parent, LinkType lt) throws CadseException {
		check_write();
		ItemDelta ret = getOrCreateItemOperation(itemDescriptionRef.getId(),
				itemDescriptionRef.getType(), false);

		if (ret.isAdded()) {
			return ret;
		}
		if (ret.isDeleted()) {
			return ret;
		}

		CreateOperationImpl createOperation = new CreateOperationImpl(ret);
		ret.setCreateOperation(createOperation);
		createOperation.addInParent();

		ret.addInParent();
		if (parent != null) {
			// set field and store in this parentItem and link part
			ret.setParent(getOrCreateItemOperation(parent), lt, true, true);
		}
		validateCreatedItem(ret);

		// notify create after the set parent ..
		notifyCreatedItem(ret);
		// if (ret.getParentItem() != null) { deja fait dans setParent
		// // set attribute value and create link
		// ret.createPartParentLink();
		// }
		ret.createLink(CadseGCST.ITEM_lt_INSTANCE_OF, ret.getType());

		if (itemDescriptionRef.getName() != null) {
			ret.setName(itemDescriptionRef.getName());
		}
		if (itemDescriptionRef.getQualifiedName() != null) {
			ret.setQualifiedName(itemDescriptionRef.getQualifiedName());
		}

		IAttributeType<?>[] attributes = ret.getType().getAllAttributeTypes();
		for (IAttributeType<?> attributeType : attributes) {
			Object v = attributeType.getDefaultValue();
			if (v == null)
				continue;
			ret.setAttribute(attributeType, v);
		}

		if (log != null) {
			log.actionAddItem(itemDescriptionRef);
		}

		return ret;
	}

	private UUID findTypeDefinition(ItemDescriptionRef itemDescriptionRef) {
		// TODO Auto-generated method stub
		return null;
	}

	private void addItemOperation(ItemDelta ret) {
		_operations.put(ret.getId(), ret);
	}

	private void echoueWC() {
		this.echec = true;
	}

	@Override
	public void actionAddLink(LinkDescription linkDescription)
			throws CadseException {
		check_write();
		LinkDelta linkOper = getLinkOperation(linkDescription, false);
		if (linkOper != null && !linkOper.isDeleted()) {
			return;
		}

		if (log != null) {
			log.actionAddLink(linkDescription);
		}

		ItemDelta itemOper = getOrCreateItemOperation(linkDescription
				.getSource().getId());
		ItemDelta destination = getOrCreateItemOperation(linkDescription
				.getDestination().getId());
		LinkType lt = itemOper.getType().getOutgoingLinkType(
				linkDescription.getType());
		itemOper.createLink(lt, destination);
	}

	public <T> void actionChangeAttribute(UUID itemId, IAttributeType<T> key,
			T value) throws CadseException, CadseException {
		check_write();
		if (log != null) {
			log.actionChangeAttribute(itemId, key, value);
		}

		ItemDelta ret = getOrCreateItemOperation(itemId);
		Item t = ret.getBaseItem();
		Object oldValue = null;
		if (t != null && t.isResolved()) {
			oldValue = t.getAttribute(key);
		}

		SetAttributeOperation attOper = new SetAttributeOperationImpl(ret, key,
				value, oldValue);
		((ItemOrLinkDeltaImpl) ret).add(attOper);
	}

	public <T> void actionChangeAttribute(LinkDescription linkDescription,
			IAttributeType<T> key, T value) throws CadseException,
			CadseException {
		check_write();
		if (log != null) {
			log.actionChangeAttribute(linkDescription, key, value);
		}
		LinkDelta linkOper = getLinkOperation(linkDescription, false);
		SetAttributeOperation attOper = new SetAttributeOperationImpl(linkOper,
				key, value, null);
		linkOper.add(attOper);
	}

	public <T> void actionRemoveAttribute(UUID itemId, IAttributeType<T> key)
			throws CadseException {
		check_write();
		if (log != null) {
			log.actionRemoveAttribute(itemId, key);
		}

		ItemDelta ret = getOrCreateItemOperation(itemId);
		Item t = ret.getBaseItem();
		Object oldValue = null;
		if (t != null && t.isResolved()) {
			oldValue = t.getAttribute(key);
		}

		SetAttributeOperation attOper = new SetAttributeOperationImpl(ret, key,
				null, oldValue);
		((ItemOrLinkDeltaImpl) ret).add(attOper);
	}

	@Override
	public <T> void actionRemoveAttribute(LinkDescription linkDescription,
			IAttributeType<T> key) throws CadseException, CadseException {
		check_write();
		if (log != null) {
			log.actionRemoveAttribute(linkDescription, key);
		}
		LinkDelta linkOper = getLinkOperation(linkDescription, false);
		Object oldValue = linkOper.getAttribute(key);
		SetAttributeOperation attOper = new SetAttributeOperationImpl(linkOper,
				key, null, oldValue);
		linkOper.add(attOper);
	}

	public void actionRemoveItem(ItemDescriptionRef itemDescriptionRef)
			throws CadseException {

		check_write();
		ItemDelta ret = getOrCreateItemOperation(itemDescriptionRef.getId(),
				itemDescriptionRef.getType());

		if (ret.getDeleteOperation() != null) {
			return;
		}

		if (log != null) {
			log.actionRemoveItem(itemDescriptionRef);
		}

		DeleteOperationImpl deleteItemOperation = new DeleteOperationImpl(
				(ItemOrLinkDeltaImpl) ret);
		((ItemOrLinkDeltaImpl) ret).setDeleteOperation(deleteItemOperation);
		deleteItemOperation.addInParent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #getOrCreateItemOperation(fr.imag.adele.cadse.core.UUID,
	 * fr.imag.adele.cadse.core.UUID)
	 */
	public ItemDelta getOrCreateItemOperation(UUID id, UUID type)
			throws CadseException, CadseException {
		return getOrCreateItemOperation(id, type, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #getOrCreateItemOperation(fr.imag.adele.cadse.core.UUID,
	 * fr.imag.adele.cadse.core.UUID, boolean)
	 */
	public ItemDelta getOrCreateItemOperation(UUID id, UUID type, boolean add)
			throws CadseException, CadseException {
		ItemDelta ret = _operations.get(id);
		if (ret != null) {
			return ret;
		}
		ItemType it = base.getItemType(type);
		if (it == null) {
			throw new CadseException(Messages.error_item_type_is_null2);
		}
		ret = new ItemDeltaImpl(this, new UUID(id.getMostSignificantBits(), id
				.getLeastSignificantBits()), it, add);
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #getOrCreateItemOperation(fr.imag.adele.cadse.core.UUID)
	 */
	public ItemDelta getOrCreateItemOperation(UUID id) throws CadseException {
		ItemDelta ret = _operations.get(id);
		if (ret != null) {
			return ret;
		}

		Item item = base.getItem(id);
		if (item == null) {
			throw new CadseException("Not found item");
		}
		ret = new ItemDeltaImpl(this, item, true);
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #getOrCreateItemOperation(fr.imag.adele.cadse.core.Item)
	 */
	public ItemDelta getOrCreateItemOperation(Item itembase) {
		ItemDelta ret = _operations.get(itembase.getId());
		if (ret != null) {
			return ret;
		}

		ret = new ItemDeltaImpl(this, itembase, true);
		return ret;
	}

	public void actionRemoveLink(LinkDescription linkDescription)
			throws CadseException {

		check_write();
		if (log != null) {
			log.actionRemoveLink(linkDescription);
		}

		LinkDelta linkOper = getLinkOperation(linkDescription, true);
		if (linkOper.isDeleted()) {
			return;
		}

		linkOper.delete();
	}

	private LinkDelta getLinkOperation(LinkDescription linkDescription,
			boolean added) throws CadseException, CadseException {
		ItemDelta itemOper = getOrCreateItemOperation(linkDescription
				.getSource().getId());
		LinkDelta linkOper = itemOper.getOutgoingLinkOperation(linkDescription
				.getType(), linkDescription.getDestination());
		return linkOper;
	}

	public void commit() throws CadseException {
		if (!isModified()) {
			return;
		}
		base.commit(this, true);
		_commitState = true;
	}

	public void load() {
		try {
			base.commit(this, false);
		} catch (CadseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isCommitted() {
		return _commitState;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #remove(fr.imag.adele.cadse.core.delta.ItemOperation)
	 */
	public void remove(ItemDelta itemOperation) {
		this.items.remove(itemOperation.getId());
		this._operations.remove(itemOperation.getId());
		itemOperation.removeInParent();
	}

	public void checkAll() throws CadseException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #getItemOperations()
	 */
	public Collection<ItemDelta> getItemOperations() {
		return new ArrayList<ItemDelta>(this._operations.values());
	}

	public Item getWithHandleIdentifier(String handleIdentifier) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setLog(ILoggableAction log) {
		this.log = log;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #getLog()
	 */
	public ILoggableAction getLog() {
		return log;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #actionDelete(fr.imag.adele.cadse.core.delta.ItemOperation)
	 */
	public void actionDelete(ItemDelta item) {
		try {
			getOrCreateItemOperation(item.getId()).delete(true);
		} catch (CadseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #actionAdd(fr.imag.adele.cadse.core.delta.ItemOperation)
	 */
	public void actionAdd(ItemDelta item) throws CadseException {
		check_write();
		Item base = item.getBaseItem();
		if (base == null) {
			return;
		}
		try {
			Link parentLink = item.getPartLinkParent(true, false);
			ItemDelta addedItem = actionAddItem(new ItemDescriptionRef(base),
					parentLink == null ? null : parentLink.getSource().getId(),
					parentLink == null ? null : parentLink.getLinkType());
			for (Link l : base.getOutgoingLinks()) {
				actionAddLink(new LinkDescription(l));
			}

			for (IAttributeType<?> key : base.getLocalAllAttributeTypes()) {
				if (key.equals(ItemTypeImpl.SHORT_NAME_KEY)) {
					continue;
				}
				if (key.equals(ItemTypeImpl.UNIQUE_NAME_KEY)) {
					continue;
				}
				actionAddAttribute(base.getId(), (IAttributeType<Object>) key,
						base.getAttribute(key));
			}
		} catch (CadseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #getBaseItem(fr.imag.adele.cadse.core.UUID)
	 */
	public Item getBaseItem(UUID id) {
		return base.getItem(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #isModified()
	 */
	public boolean isModified() {
		for (ItemDelta oper : this._operations.values()) {
			if (oper.isModified()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (ItemDelta oper : this._operations.values()) {
			if (oper.isModified()) {
				oper.toString(sb, "");
			}
		}
		return sb.toString();
	}

	public void addListener(WorkspaceListener l, int eventFilter) {
		throw new UnsupportedOperationException();
	}

	public void addListener(WorkspaceListener l, EventFilter eventFilter) {
		throw new UnsupportedOperationException();
	}

	public List<WorkspaceListener> filter(int filters,
			ImmutableWorkspaceDelta workspaceDelta) {
		throw new UnsupportedOperationException();
	}

	public void removeListener(WorkspaceListener l) {
		throw new UnsupportedOperationException();
	}

	public void actionAddOperation(WLWCOperationImpl operation) {
		if (log != null) {
			log.actionAddOperation(operation);
		}
		if (all_operations == null) {
			all_operations = new HashMap<UUID, WLWCOperationImpl>();
		}
		all_operations.put(operation.getOperationId(), operation);
		if (operation.getOperationType() == OperationTypeCst.ITEM_OPERATION) {
			addItemOperation((ItemDelta) operation);
		}
	}

	public void actionRemoveOperation(WLWCOperationImpl operation) {
		if (all_operations == null) {
			return;
		}
		if (log != null) {
			log.actionRemoveOperation(operation);
		}
		all_operations.remove(operation.getOperationId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #getOperations()
	 */
	public Collection<WLWCOperationImpl> getOperations() {
		if (all_operations == null) {
			return Collections.emptyList();
		}
		return all_operations.values();
	}

	public void commitAbeforeB(MappingOperation a, MappingOperation b) {
		b.addBeforeMappingOperation(a);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #getMappingOrder()
	 */
	public ElementsOrder<MappingOperation> getMappingOrder() {
		ArrayList<MappingOperation[]> references = new ArrayList<MappingOperation[]>();
		HashSet<MappingOperation> operations = new HashSet<MappingOperation>();
		if (all_operations != null) {
			for (WLWCOperationImpl oper : this.all_operations.values()) {
				if (oper.getOperationType() == OperationTypeCst.MAPPING_OPERATION) {
					MappingOperation mo = (MappingOperation) oper;
					operations.add(mo);
					if (mo.getBeforeOperation() != null) {
						for (MappingOperation bmo : mo.getBeforeOperation()) {
							references.add(new MappingOperation[] { mo, bmo });
						}
					}
				}
			}
		}
		return ComputeElementOrder.computeElementOrder(MappingOperation.class,
				operations, references);
	}

	private static class WoLProWCListener extends
			AbstractLogicalWorkspaceTransactionListener {

		@Override
		public void validateDeletedLink(LogicalWorkspaceTransaction wc,
				LinkDelta link) throws CadseException {
			if (link.getLinkType() == CadseGCST.ITEM_lt_PARENT) {
				if (link.getSource().getType().isPartType()) {
					if (link.getSource().getPartParent() == null) {
						throw new CadseException(Messages.parent_must_be_set,
								link);
					}
					if (link.getDestination().isDeleted()
							|| link.getSource().isDeleted())
						return;
					if (link.getSource().getPartParent().equals(
							link.getDestination())) {
						throw new CadseException(Messages.parent_must_be_set,
								link);
					}
				}

			}
		}

		@Override
		public void validateCreatedItem(LogicalWorkspaceTransaction wc,
				ItemDelta item) throws CadseException, CadseException {
			ItemType it = item.getType();
			if (it == null) {
				throw new CadseException(Messages.error_item_type_is_null2);
			}

			if (it.getItemManager() == null) {
				throw new CadseException(Messages.error_no_item_manager_found,
						it.getId(), item.getId());
			}

			LinkType lt = null;
			Item parent = null;

			if (it.isPartType()) {
				parent = item.getParentInStorage();
				if (parent == null) {
					throw new CadseException(
							Messages.error_you_must_specifi_parent_item);
				}
				lt = item.getParentLinkTypeInStorage(parent);
				if (lt == null) {
					throw new CadseException(
							Messages.error_parent_link_type_is_null);
				}
				if (!lt.isPart()) {
					throw new CadseException(
							Messages.error_parent_link_type_not_part, lt);
				}
				// parent.type = lt.source.type
				if (!parent.isInstanceOf(lt.getSource())) {
					throw new CadseException(
							Messages.error_cannot_create_an_item_bad_source,
							parent.getName(), lt.getName(), lt.getSource()
									.getId(), parent.getType().getId());
				}

				if (!item.isInstanceOf(lt.getDestination())) {
					throw new CadseException(
							Messages.error_cannot_create_an_item_bad_destination,
							parent.getName(), lt.getName(), lt.getDestination()
									.getName(), lt.getDestination().getId(), it
									.getName(), it.getId());
				}
			}
		}

		@Override
		public void validateCreatedLink(LogicalWorkspaceTransaction wc,
				LinkDelta link) throws CadseException, CadseException {
			// cardinalite
			LinkType lt = link.getLinkType();
			if (lt == null) {
				throw new CadseException("Link type is null");
			}
			int max = lt.getMax();
			if (max != -1) {
				int countLink = link.getSource().getOutgoingItems(lt, false)
						.size();
				if (countLink > max) {
					throw new CadseException(
							"Depassement du nombre de link autorizé pour le type "
									+ lt.getName());
				}
			}
			if (lt.getMin() != 0) {
				// TODO add a warning
			}
			ItemDelta item = link.getSource();
			if (item.getType() != null
					&& item.getType().getKeyDefinition() != null) {
				KeyDefinition keyType = item.getType().getKeyDefinition();
				KEY: {
					if (item.isAdded()) {
						Key key = item.getKey();
						if (key == null) {
							break KEY;
						}
						IAttributeType<?>[] attKeys = keyType.getKeyElements();
						if (attKeys == null) {
							break KEY;
						}
						for (int i = 0; i < attKeys.length; i++) {
							if (attKeys[i] == link.getLinkType()) {
								// Attribute of the key has changed
								Key newK = keyType.computeKey(item);
								if (!key.equals(newK))
									item.setNextKey(newK);
								break KEY;
							}
						}
						break KEY;
					}
					if (item.isDeleted()) {
						break KEY;
					}
					// item is modified
					Item baseItem = item.getBaseItem();
					if (baseItem != null) {
						Key key = baseItem.getKey();
						if (key == null) {
							break KEY;
						}
						IAttributeType<?>[] attKeys = keyType.getKeyElements();
						if (attKeys == null) {
							break KEY;
						}
						for (int i = 0; i < attKeys.length; i++) {
							if (attKeys[i] == link.getLinkType()) {
								// Attribute of the key has changed
								Key newK = keyType.computeKey(item);
								if (!key.equals(newK))
									item.setNextKey(newK);
								break;
							}
						}
					}
				}
			}

		}

		@Override
		public void notifyCreatedLink(LogicalWorkspaceTransaction wc,
				LinkDelta link) throws CadseException, CadseException {
			ItemDelta item = link.getSource();
			if (item.getType() != null
					&& item.getType().getKeyDefinition() != null) {
				Key key = item.getNextKey();
				if (key != null) {
					item.setKey(key);
					item.setNextKey(null);
				}
			}

			LinkType link_lt = link.getLinkType();

			LinkType inverseLinkType = link_lt.getInverse();
			Item destination = link.getDestination();
			if (destination == null) {
				throw new CadseException("Destination is null!!!");
			}

			Item source = link.getSource();
			if (inverseLinkType != null && destination.isResolved()) {
				Link inverseLink = destination.getOutgoingLink(inverseLinkType,
						source.getId());
				if (inverseLink == null) {
					try {
						wc.getItem(destination.getId()).createLink(
								inverseLinkType, source);
					} catch (CadseException e) {
						link.delete();
						throw e;
					}
				}
			}

			/* if a link is part, the inverse link must be created. */

			// set parent attribute
			// if (link_lt.isPart()) {
			// destination.setAttribute(CadseGCST.ITEM_at_PARENT_ITEM_ID_,
			// source.getId());
			// destination.setAttribute(CadseGCST.ITEM_at_PARENT_ITEM_TYPE_ID_,
			// link_lt.getShortName());
			// }
		}

		@Override
		public void notifyCreatedItem(LogicalWorkspaceTransaction wc,
				ItemDelta item) throws CadseException, CadseException {
			ItemType it = item.getType();
			LinkType lt = null;
			ItemDelta parent = null;

			if (it.isPartType()) {
				parent = item.getParentInStorage();
				if (parent != null) {
					lt = item.getParentLinkTypeInStorage(parent);
					LinkDelta l = parent.getOutgoingLink(lt, item.getId());
					if (l == null || !l.isCreatedLink()) {
						parent.createLink(lt, item);
					}
				}
			}

			if (item.getType() != null
					&& item.getType().getKeyDefinition() != null) {
				KeyDefinition keyType = item.getType().getKeyDefinition();
				Key newK = keyType.computeKey(item);
				item.setKey(newK);
			}
		}

		@Override
		public void notifyDeletedItem(LogicalWorkspaceTransaction wc,
				ItemDelta item) throws CadseException, CadseException {
			if (item.getType() != null
					&& item.getType().getKeyDefinition() != null) {
				item.setKey(null);
			}

			DeleteOperation deleteOperation = item.getDeleteOperation();

			/*
			 * Suppresion de tous les liens outgoings sauf ceux qui sont déjà
			 * détruit Il sont toujours détruit car il ne peuvent exister sans
			 * la source
			 */
			for (LinkDelta l : item.getOutgoingLinkOperations()) {
				if (!l.isDeleted()) {
					l.delete(deleteOperation);
				}
			}

			/*
			 * Suppresion des liens incomings si l'option "delete incoming link"
			 * est a true ou si la source doit est detruite (ex: une annotation
			 * ne peut pas exister sans l'item qu'elle annote)
			 */
			for (LinkDelta l : item.getIncomingLinkOperations()) {
				if (!l.isDeleted()
						&& (deleteOperation.isDeleteIncomingLink() || (l
								.mustDeleteSource() && deleteOperation
								.isDeleteAnnotationLink()))) {
					l.delete(deleteOperation);
				}
			}

		}

		@Override
		public void notifyDeletedLink(LogicalWorkspaceTransaction wc,
				LinkDelta link) throws CadseException, CadseException {
			DeleteOperation deleteOperation = link.getDeleteOperation();
			/* delete annotation if option is set */
			if (link.mustDeleteDestination()
					&& deleteOperation.isDeletePartLink()) {
				ItemDelta dest = link.getDestinationOperation();
				dest.delete(deleteOperation, 0);
			}
			/* delete part if option is set */
			if (link.mustDeleteSource()
					&& deleteOperation.isDeleteAnnotationLink()) {
				ItemDelta src = link.getSourceOperation();
				src.delete(deleteOperation, 0);
			}

			/* delete mapping if option is set */
			if (link.isMappingLink() && deleteOperation.isDeleteMapping()
					&& !link.getDestination().isDeleted()) {
				link.getDestination().delete(deleteOperation, 0);
			}

			/* delete inverse link */
			LinkDelta inverseLink = link.getInverseLink();
			if (inverseLink != null && !inverseLink.isDeleted()) {
				inverseLink.delete(deleteOperation);
			}
		}

		@Override
		public void validateChangeAttribute(LogicalWorkspaceTransaction wc,
				ItemDelta item, SetAttributeOperation attOperation)
				throws CadseException {

			if (item.getType() != null
					&& item.getType().getKeyDefinition() != null) {
				KeyDefinition keyType = item.getType().getKeyDefinition();
				KEY: {
					if (item.isAdded()) {
						Key key = item.getKey();
						if (key == null) {
							break KEY;
						}
						IAttributeType<?>[] attKeys = keyType.getKeyElements();
						if (attKeys == null) {
							break KEY;
						}
						for (int i = 0; i < attKeys.length; i++) {
							if (attKeys[i] == attOperation
									.getAttributeDefinition()) {
								// Attribute of the key has changed
								Key newK = keyType.computeKey(item);
								if (!key.equals(newK))
									item.setNextKey(newK);
								break KEY;
							}
						}
						break KEY;
					}
					if (item.isDeleted()) {
						break KEY;
					}
					// item is modified
					Item baseItem = item.getBaseItem();
					if (baseItem != null) {
						Key key = baseItem.getKey();
						if (key == null) {
							break KEY;
						}
						IAttributeType<?>[] attKeys = keyType.getKeyElements();
						if (attKeys == null) {
							break KEY;
						}
						for (int i = 0; i < attKeys.length; i++) {
							if (attKeys[i] == attOperation
									.getAttributeDefinition()) {
								// Attribute of the key has changed
								Key newK = keyType.computeKey(item);
								if (!key.equals(newK))
									item.setNextKey(newK);
								break;
							}
						}
					}
				}
			}

		}

		@Override
		public void notifyChangeAttribute(LogicalWorkspaceTransaction wc,
				ItemDelta item, SetAttributeOperation attOperation)
				throws CadseException {

			if (item.getType() != null
					&& item.getType().getKeyDefinition() != null) {
				Key key = item.getNextKey();
				if (key != null) {
					item.setKey(key);
					item.setNextKey(null);
				}
			}

			if (attOperation.getAttributeDefinition() == CadseGCST.ITEM_at_QUALIFIED_NAME_) {
				recomputeUniqueName(item);
			}
			if (attOperation.getAttributeDefinition() == CadseGCST.ITEM_at_NAME_) {
				if (item.getType().hasQualifiedNameAttribute()) {
					item.setQualifiedName(CadseCore.getName(item, item
							.getName(), item.getPartParent(), item
							.getPartParentLinkType()));
				}
			}

			try {
				if (attOperation.getAttributeDefinition() == CadseGCST.ITEM_at_DISPLAY_NAME_) {
					String dn = item.getDisplayName();

					ItemType it = item.getType();
					if (it != null) {
						String newdn = it.getItemManager().getDisplayName(item);

						if (newdn != null && !Convert.equals(dn, newdn)) {
							item.setAttribute(CadseGCST.ITEM_at_DISPLAY_NAME_,
									newdn);
						}
					}

				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		private void recomputeUniqueName(ItemDelta item) throws CadseException {
			List<Link> outLinks = item.getOutgoingLinks();
			for (Link l : outLinks) {
				if (l.getLinkType().isPart() && l.isLinkResolved()) {
					Item dest = l.getDestination();
					if (dest.getType().hasQualifiedNameAttribute()) {
						dest.setQualifiedName(CadseCore.getName(dest, dest
								.getName(), item, l.getLinkType()));
					}
				}
			}

			List<Link> inLinks = item.getIncomingLinks();
			for (Link l : inLinks) {
				if (l.getLinkType().isAnnotation()) {
					Item src = l.getSource();
					if (src.getType().hasQualifiedNameAttribute()) {
						src.setQualifiedName(CadseCore.getName(src, src
								.getName(), src.getPartParent(), src
								.getPartParentLinkType()));
					}
				}
			}
		}

		@Override
		public void notifyLoadedItem(
				LogicalWorkspaceTransaction workspaceLogiqueWorkingCopy,
				List<ItemDelta> loadedItems) {
			for (ItemDelta itemDelta : loadedItems) {
				try {
					if (itemDelta.getType() != null
							&& itemDelta.getType().isPartType()) {
						// get the parent
						ItemDelta partParent = itemDelta.getParentItem();
						// get the link parent.
						Link l = getInversePartLink(itemDelta);
						if (l != null) {
							if (partParent == null)
								itemDelta.setParent(l.getDestination(), null);
							continue;
						}

						l = getIncommingLinkParent(itemDelta);
						if (l != null) {
							createLinkPartToChild((ItemDelta) l.getSource(),
									itemDelta, l.getLinkType());
							createLinkChildToPart((ItemDelta) l.getSource(),
									itemDelta);
							continue;
						}
						if (partParent != null) {
							LinkType lt = getParentLinkTypeInStorage(itemDelta,
									partParent);
							if (lt == null) {
								lt = itemDelta.getType().getIncomingPart(
										partParent.getType());
							}
							if (lt != null && lt.isPart()) {
								createLinkPartToChild(partParent, itemDelta, lt);
								createLinkChildToPart(partParent, itemDelta);
							}
						}
					}
				} catch (RuntimeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		Link createLinkPartToChild(ItemDelta partParent, ItemDelta itemDelta,
				LinkType lt) {
			try {
				Link l = partParent.getOutgoingLink(lt, itemDelta.getId());
				if (l == null) {
					if (lt.getMax() == 1) {
						Link findOtherLink = partParent.getOutgoingLink(lt);
						if (findOtherLink == null) {
							// error(this, "Cannot recreate Item children
							// alldready exist", null);
							return null;
						}
					}
					try {
						return partParent.createLink(lt, itemDelta);
					} catch (CadseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		void createLinkChildToPart(ItemDelta partParent, ItemDelta itemDelta) {
			try {
				Link l = itemDelta.getOutgoingLink(CadseGCST.ITEM_lt_PARENT,
						partParent.getId());
				if (l != null) {
					return;
				} else {
					Link findOtherLink = itemDelta
							.getOutgoingLink(CadseGCST.ITEM_lt_PARENT);
					if (findOtherLink != null) {
						// error(this, "Cannot recreate Item children
						// alldready exist", null);
						return;
					}

					try {
						itemDelta.createLink(CadseGCST.ITEM_lt_PARENT,
								partParent);
					} catch (CadseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/**
		 * Gets the parent link type in storage.
		 * 
		 * @param parent
		 *            the parent
		 * 
		 * @return the parent link type in storage
		 */
		final LinkType getParentLinkTypeInStorage(ItemDelta child, Item parent) {
			SetAttributeOperation attLinkTypeId = child
					.getSetAttributeOperation(Accessor.ATTR_PARENT_ITEM_TYPE_ID);
			if (attLinkTypeId != null
					&& attLinkTypeId.getCurrentValue() != null
					&& parent.getType() != null) {
				return parent.getType().getOutgoingLinkType(
						(String) attLinkTypeId.getCurrentValue());
			}

			if (parent.getType() == null) {
				return null;
			}
			return parent.getType().getPartParentLinkType();
		}

		private Link getIncommingLinkParent(ItemDelta itemDelta) {
			List<Link> links;
			links = itemDelta.getIncomingLinks();
			for (Link l : links) {
				if (l.getLinkType() != null && l.getLinkType().isPart()) {
					return l;
				}
			}
			return null;
		}

		private Link getInversePartLink(ItemDelta itemDelta) {
			return itemDelta.getOutgoingLink(CadseGCST.ITEM_lt_PARENT);
		}

	}

	/**
	 * This method used to verify the preconditions before create an item. An
	 * item have not yet been created and item type must be ready in workspace . <br>
	 * 
	 * @param it
	 *            not null
	 * @param parent
	 *            can be null, the source of the link which will attache the new
	 *            item.
	 * @param lt
	 *            can can be null. lt can be an aggregation type, it's not
	 *            obligatoire.
	 * @param THIS
	 *            the tHIS
	 * @throws CadseException
	 * 
	 * @OCL<br> context: Workspace::createItem(String id, ItemType it) : Item<br>
	 *          1. pre: id <> null <br/>
	 *          2. pre: id <> '' <br/>
	 *          3. pre: it <> null <br/>
	 *          4. pre: parent <> null <br/>
	 *          5. pre: lt <> null <br/>
	 *          6. pre: items->forAll(item | item.id <> id )<br/>
	 *          7. pre: self.type.selectedItemtype->include(it) <br/>
	 *          8. pre: parent.type = lt.source and it = lt.dest<br/>
	 *          9. pre: parent.isReadOnly = false<br/>
	 * @exception CadseException
	 *                : Id can not be null. CadseException : Id can not be
	 *                empty. CadseException : Invalid assignment, this item
	 *                <tt>$id</tt> already exist.<br/>
	 *                CadseException : Invalid assignment, type of this item is
	 *                not supported for this kind of workspace.<br/>
	 *                CadseException : Invalid assignment, link type is not
	 *                correct. CadseException : Invalid assignment, item parent
	 *                <tt>$parent.id</tt> + " is in state read only.
	 */

	private void preconditions_createItem(Item THIS, ItemType it, Item parent,
			LinkType lt) throws CadseException {

		checkUniqueName(THIS);

	}

	/**
	 * Check unique name.
	 * 
	 * @param THIS
	 *            the tHIS
	 * 
	 * @throws CadseException
	 *             the melusine error
	 */
	public void checkUniqueName(Item THIS) throws CadseException {
		Key key = THIS.getKey();
		if (key != null) {
			// pre: items->forAll(item | item.id <> id )
			Item i = items_by_key.get(key);

			if (i == null) {
				return;
			}

			if (i == THIS) {
				return;
			}
			if (i.isResolved()) {
				throw new CadseException(
						Messages.error_invalid_assignement_allready_exists,
						THIS.getName());
			}
			if (!i.getType().equals(THIS.getType())) {
				throw new CadseException(
						Messages.error_invalid_assignement_bad_type, THIS
								.getName(), THIS.getType().getName(), i
								.getType().getId());
			}
		}
	}

	@Override
	public void clear() throws CadseException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void loadCadseModel(String qualifiedCadseModelName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAttribute(Item item, IAttributeType<?> key, Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public LinkType createUnresolvedLinkType(UUID id, String name,
			TypeDefinition source, TypeDefinition dest) {
		return base.createUnresolvedLinkType(id, name, source, dest);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #notifyChangeAttribute(fr.imag.adele.cadse.core.delta.ItemOperation,
	 * fr.imag.adele.cadse.core.delta.SetAttributeOperation)
	 */
	@Override
	public void notifyChangeAttribute(ItemDelta item,
			SetAttributeOperation attOperation) {
		if (_logicalWorkspaceTransactionListeners != null) {
			for (int i = 0; i < _logicalWorkspaceTransactionListeners.length; i++) {
				try {
					_logicalWorkspaceTransactionListeners[i]
							.notifyChangeAttribute(this, item, attOperation);
				} catch (Throwable e) {
					addError(_logicalWorkspaceTransactionListeners[i],
							attOperation, e);
				}
			}
		}
		ItemType it = item.getType();
		if (it != null) {
			LogicalWorkspaceTransactionListener[] l = it
					.getLogicalWorkspaceTransactionListener();
			if (l != null) {
				for (int i = 0; i < l.length; i++) {
					try {
						l[i].notifyChangeAttribute(this, item, attOperation);
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						addError(_logicalWorkspaceTransactionListeners[i],
								attOperation, e);
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #notifyChangeAttribute(fr.imag.adele.cadse.core.delta.LinkOperation,
	 * fr.imag.adele.cadse.core.delta.SetAttributeOperation)
	 */
	public void notifyChangeAttribute(LinkDelta link,
			SetAttributeOperation attOperation) throws CadseException,
			CadseException {
		if (_logicalWorkspaceTransactionListeners != null) {
			for (int i = 0; i < _logicalWorkspaceTransactionListeners.length; i++) {
				try {
					_logicalWorkspaceTransactionListeners[i]
							.notifyChangeAttribute(this, link, attOperation);
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		LinkType lt = link.getLinkType();
		if (lt != null) {
			HashSet<LogicalWorkspaceTransactionListener> listener = computeLtListener(lt);
			if (listener.size() > 0) {
				for (LogicalWorkspaceTransactionListener list : listener) {
					try {
						list.notifyChangeAttribute(this, link, attOperation);
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	private HashSet<LogicalWorkspaceTransactionListener> computeLtListener(
			LinkType lt) {
		HashSet<LogicalWorkspaceTransactionListener> listener = new HashSet<LogicalWorkspaceTransactionListener>();
		LogicalWorkspaceTransactionListener[] l = lt
				.getLogicalWorkspaceTransactionListener();
		if (l != null) {
			listener.addAll(Arrays.asList(l));
		}
		TypeDefinition it = lt.getSource();
		if (it != null) {
			l = it.getLogicalWorkspaceTransactionListener();
			if (l != null) {
				listener.addAll(Arrays.asList(l));
			}
		}
		it = lt.getDestination();
		if (it != null) {
			l = it.getLogicalWorkspaceTransactionListener();
			if (l != null) {
				listener.addAll(Arrays.asList(l));
			}
		}
		return listener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #notifyChangeLinkOrder(fr.imag.adele.cadse.core.delta.LinkOperation,
	 * fr.imag.adele.cadse.core.internal.delta.OrderOperation)
	 */
	@Override
	public void notifyChangeLinkOrder(LinkDelta link,
			OrderOperation orderOperation) {
		if (_logicalWorkspaceTransactionListeners != null) {
			for (int i = 0; i < _logicalWorkspaceTransactionListeners.length; i++) {
				try {
					_logicalWorkspaceTransactionListeners[i]
							.notifyChangeLinkOrder(this, link, orderOperation);
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	@Override
	public void notifyCommitTransaction() {
		HashSet<Object> ts = new HashSet<Object>();
		if (_logicalWorkspaceTransactionListeners != null) {
			for (int i = 0; i < _logicalWorkspaceTransactionListeners.length; i++) {
				try {
					ts.add(_logicalWorkspaceTransactionListeners[i]);
					_logicalWorkspaceTransactionListeners[i]
							.notifyCommitTransaction(this);
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		// call only one the workingcopylistener associated to a type of a
		// modified item or a type of a modified link
		for (ItemDelta id : getItemOperations()) {
			Collection<LinkDelta> links = id.getOutgoingLinkOperations();
			for (LinkDelta linkDelta : links) {
				if (!linkDelta.isModified()) {
					continue;
				}
				LinkType lt = linkDelta.getLinkType();
				if (lt == null) {
					continue;
				}
				if (ts.contains(lt)) {
					continue;
				}
				LogicalWorkspaceTransactionListener[] l = lt
						.getLogicalWorkspaceTransactionListener();
				if (l != null) {
					for (int i = 0; i < l.length; i++) {
						if (ts.contains(l[i])) {
							continue;
						}
						try {
							ts.add(l[i]);
							l[i].notifyCommitTransaction(this);
						} catch (Throwable e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				ts.add(lt);

			}
			if (!id.isModified()) {
				continue;
			}
			ItemType it = id.getType();
			if (it == null) {
				continue;
			}
			if (ts.contains(it)) {
				continue;
			}
			LogicalWorkspaceTransactionListener[] l = it
					.getLogicalWorkspaceTransactionListener();
			if (l != null) {
				for (int i = 0; i < l.length; i++) {
					if (ts.contains(l[i])) {
						continue;
					}
					try {
						ts.add(l[i]);
						l[i].notifyCommitTransaction(this);
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			ts.add(it);

		}
	}

	public void notifyAbortTransaction() {
		HashSet<Object> ts = new HashSet<Object>();
		if (_logicalWorkspaceTransactionListeners != null) {
			for (int i = 0; i < _logicalWorkspaceTransactionListeners.length; i++) {
				try {
					ts.add(_logicalWorkspaceTransactionListeners[i]);
					_logicalWorkspaceTransactionListeners[i]
							.notifyAbortTransaction(this);
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		// call only one the workingcopylistener associated to a type of a
		// modified item or a type of a modified link
		for (ItemDelta id : getItemOperations()) {
			Collection<LinkDelta> links = id.getOutgoingLinkOperations();
			for (LinkDelta linkDelta : links) {
				if (!linkDelta.isModified()) {
					continue;
				}
				LinkType lt = linkDelta.getLinkType();
				if (lt == null) {
					continue;
				}
				if (ts.contains(lt)) {
					continue;
				}
				LogicalWorkspaceTransactionListener[] l = lt
						.getLogicalWorkspaceTransactionListener();
				if (l != null) {
					for (int i = 0; i < l.length; i++) {
						if (ts.contains(l[i])) {
							continue;
						}
						try {
							ts.add(l[i]);
							l[i].notifyAbortTransaction(this);
						} catch (Throwable e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				ts.add(lt);

			}
			if (!id.isModified()) {
				continue;
			}
			ItemType it = id.getType();
			if (it == null) {
				continue;
			}
			if (ts.contains(it)) {
				continue;
			}
			LogicalWorkspaceTransactionListener[] l = it
					.getLogicalWorkspaceTransactionListener();
			if (l != null) {
				for (int i = 0; i < l.length; i++) {
					if (ts.contains(l[i])) {
						continue;
					}
					try {
						ts.add(l[i]);
						l[i].notifyAbortTransaction(this);
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			ts.add(it);

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #notifyCreatedItem(fr.imag.adele.cadse.core.delta.ItemOperation)
	 */
	@Override
	public void notifyCreatedItem(ItemDelta item) throws CadseException {
		if (_logicalWorkspaceTransactionListeners != null) {
			for (int i = 0; i < _logicalWorkspaceTransactionListeners.length; i++) {
				try {
					_logicalWorkspaceTransactionListeners[i].notifyCreatedItem(
							this, item);
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		ItemType it = item.getType();
		if (it != null) {
			LogicalWorkspaceTransactionListener[] l = it
					.getLogicalWorkspaceTransactionListener();
			if (l != null) {
				for (int i = 0; i < l.length; i++) {
					try {
						l[i].notifyCreatedItem(this, item);
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void notifyMigratePartLink(ItemDelta childItem,
			ItemDelta newPartParent, LinkType lt, LinkDelta newPartLink,
			LinkDelta oldPartLink) {
		if (_logicalWorkspaceTransactionListeners != null) {
			for (int i = 0; i < _logicalWorkspaceTransactionListeners.length; i++) {
				try {
					_logicalWorkspaceTransactionListeners[i]
							.notifyMigratePartLink(this, childItem,
									newPartParent, lt, newPartLink, oldPartLink);
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		ItemType it = childItem.getType();
		if (it != null) {
			LogicalWorkspaceTransactionListener[] l = it
					.getLogicalWorkspaceTransactionListener();
			if (l != null) {
				for (int i = 0; i < l.length; i++) {
					try {
						l[i].notifyMigratePartLink(this, childItem,
								newPartParent, lt, newPartLink, oldPartLink);
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void notifieChangedContent(ItemDelta item, ContentChangeInfo[] change) {
		if (_logicalWorkspaceTransactionListeners != null) {
			for (int i = 0; i < _logicalWorkspaceTransactionListeners.length; i++) {
				try {
					_logicalWorkspaceTransactionListeners[i]
							.notifyChangedContent(this, item, change);
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		ItemType it = item.getType();
		if (it != null) {
			LogicalWorkspaceTransactionListener[] l = it
					.getLogicalWorkspaceTransactionListener();
			if (l != null) {
				for (int i = 0; i < l.length; i++) {
					try {
						l[i].notifyChangedContent(this, item, change);
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #notifyAddMappingOperation(fr.imag.adele.cadse.core.delta.ItemOperation,
	 * fr.imag.adele.cadse.core.delta.MappingOperation)
	 */
	public void notifyAddMappingOperation(ItemDelta item,
			MappingOperation mappingOperation) {
		if (_logicalWorkspaceTransactionListeners != null) {
			for (int i = 0; i < _logicalWorkspaceTransactionListeners.length; i++) {
				try {
					_logicalWorkspaceTransactionListeners[i]
							.notifyAddMappingOperation(this, item,
									mappingOperation);
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		ItemType it = item.getType();
		if (it != null) {
			LogicalWorkspaceTransactionListener[] l = it
					.getLogicalWorkspaceTransactionListener();
			if (l != null) {
				for (int i = 0; i < l.length; i++) {
					try {
						l[i].notifyAddMappingOperation(this, item,
								mappingOperation);
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void validateCreatedItem(ItemDelta item) throws CadseException {
		if (_logicalWorkspaceTransactionListeners != null) {
			for (int i = 0; i < _logicalWorkspaceTransactionListeners.length; i++) {
				_logicalWorkspaceTransactionListeners[i].validateCreatedItem(
						this, item);
			}
		}
		ItemType it = item.getType();
		if (it != null) {
			LogicalWorkspaceTransactionListener[] l = it
					.getLogicalWorkspaceTransactionListener();
			if (l != null) {
				for (int i = 0; i < l.length; i++) {
					l[i].validateCreatedItem(this, item);
				}
			}
		}
	}

	public void validateChangeAttribute(ItemDelta item,
			SetAttributeOperation attOperation) {
		if (_logicalWorkspaceTransactionListeners != null) {
			for (int i = 0; i < _logicalWorkspaceTransactionListeners.length; i++) {
				try {
					_logicalWorkspaceTransactionListeners[i]
							.validateChangeAttribute(this, item, attOperation);
				} catch (CadseException ex) {
					addError(_logicalWorkspaceTransactionListeners[i],
							attOperation, ex);
					Logger.getLogger(
							LogicalWorkspaceTransactionImpl.class.getName())
							.log(Level.SEVERE, null, ex);
				}
			}
		}
		ItemType it = item.getType();
		if (it != null) {
			LogicalWorkspaceTransactionListener[] l = it
					.getLogicalWorkspaceTransactionListener();
			if (l != null) {
				for (int i = 0; i < l.length; i++) {
					try {
						l[i].validateChangeAttribute(this, item, attOperation);
					} catch (CadseException ex) {
						addError(_logicalWorkspaceTransactionListeners[i],
								attOperation, ex);
						Logger
								.getLogger(
										LogicalWorkspaceTransactionImpl.class
												.getName()).log(Level.SEVERE,
										null, ex);
					}
				}
			}
		}
	}

	private void addError(Object source, WLWCOperation operation, Throwable ex) {
		Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
				ex.getMessage(), ex);

	}

	public void validateCreatedLink(LinkDelta link) throws CadseException {
		if (_logicalWorkspaceTransactionListeners != null) {
			for (int i = 0; i < _logicalWorkspaceTransactionListeners.length; i++) {
				_logicalWorkspaceTransactionListeners[i].validateCreatedLink(
						this, link);
			}
		}
		LinkType lt = link.getLinkType();
		if (lt != null) {
			HashSet<LogicalWorkspaceTransactionListener> listener = computeLtListener(lt);
			if (listener.size() > 0) {
				for (LogicalWorkspaceTransactionListener list : listener) {
					list.validateCreatedLink(this, link);
				}
			}
		}
	}

	public void validateDeleteLink(LinkDelta link) throws CadseException {
		if (_logicalWorkspaceTransactionListeners != null) {
			for (int i = 0; i < _logicalWorkspaceTransactionListeners.length; i++) {
				_logicalWorkspaceTransactionListeners[i].validateDeletedLink(
						this, link);
			}
		}
		LinkType lt = link.getLinkType();
		if (lt != null) {
			HashSet<LogicalWorkspaceTransactionListener> listener = computeLtListener(lt);
			if (listener.size() > 0) {
				for (LogicalWorkspaceTransactionListener list : listener) {
					list.validateDeletedLink(this, link);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #notifyCreatedLink(fr.imag.adele.cadse.core.delta.LinkOperation)
	 */
	public void notifyCreatedLink(LinkDelta link) throws CadseException {
		if (_logicalWorkspaceTransactionListeners != null) {
			for (int i = 0; i < _logicalWorkspaceTransactionListeners.length; i++) {
				try {
					_logicalWorkspaceTransactionListeners[i].notifyCreatedLink(
							this, link);
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		LinkType lt = link.getLinkType();
		if (lt != null) {
			HashSet<LogicalWorkspaceTransactionListener> listener = computeLtListener(lt);
			if (listener.size() > 0) {
				for (LogicalWorkspaceTransactionListener list : listener) {
					try {
						list.notifyCreatedLink(this, link);
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #notifyDeletedItem(fr.imag.adele.cadse.core.delta.ItemOperation)
	 */
	@Override
	public void notifyDeletedItem(ItemDelta item) throws CadseException {
		if (_logicalWorkspaceTransactionListeners != null) {
			for (int i = 0; i < _logicalWorkspaceTransactionListeners.length; i++) {
				_logicalWorkspaceTransactionListeners[i].notifyDeletedItem(
						this, item);
			}
		}
		ItemType it = item.getType();
		if (it != null) {
			LogicalWorkspaceTransactionListener[] l = it
					.getLogicalWorkspaceTransactionListener();
			if (l != null) {
				for (int i = 0; i < l.length; i++) {
					l[i].notifyDeletedItem(this, item);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #notifyDoubleClick(fr.imag.adele.cadse.core.delta.ItemOperation)
	 */
	@Override
	public void notifyDoubleClick(ItemDelta item) throws CadseException {
		if (_logicalWorkspaceTransactionListeners != null) {
			for (int i = 0; i < _logicalWorkspaceTransactionListeners.length; i++) {
				_logicalWorkspaceTransactionListeners[i].notifyDoubleClick(
						this, item);
			}
		}
		ItemType it = item.getType();
		if (it != null) {
			LogicalWorkspaceTransactionListener[] l = it
					.getLogicalWorkspaceTransactionListener();
			if (l != null) {
				for (int i = 0; i < l.length; i++) {
					l[i].notifyDoubleClick(this, item);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #notifyDeletedLink(fr.imag.adele.cadse.core.delta.LinkOperation)
	 */
	public void notifyDeletedLink(LinkDelta link) throws CadseException {
		if (_logicalWorkspaceTransactionListeners != null) {
			for (int i = 0; i < _logicalWorkspaceTransactionListeners.length; i++) {
				_logicalWorkspaceTransactionListeners[i].notifyDeletedLink(
						this, link);
			}
		}
		LinkType lt = link.getLinkType();
		if (lt != null) {
			HashSet<LogicalWorkspaceTransactionListener> listener = computeLtListener(lt);
			if (listener.size() > 0) {
				for (LogicalWorkspaceTransactionListener list : listener) {
					try {
						list.notifyDeletedLink(this, link);
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #notifyCancelCreatedItem(fr.imag.adele.cadse.core.delta.ItemOperation)
	 */
	@Override
	public void notifyCancelCreatedItem(ItemDelta item) throws CadseException {
		if (_logicalWorkspaceTransactionListeners != null) {
			for (int i = 0; i < _logicalWorkspaceTransactionListeners.length; i++) {
				try {
					_logicalWorkspaceTransactionListeners[i]
							.notifyCancelCreatedItem(this, item);
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		ItemType it = item.getType();
		if (it != null) {
			LogicalWorkspaceTransactionListener[] l = it
					.getLogicalWorkspaceTransactionListener();
			if (l != null) {
				for (int i = 0; i < l.length; i++) {
					try {
						l[i].notifyCancelCreatedItem(this, item);
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #notifyCancelCreatedLink(fr.imag.adele.cadse.core.delta.LinkOperation)
	 */
	@Override
	public void notifyCancelCreatedLink(LinkDelta link) throws CadseException {
		if (_logicalWorkspaceTransactionListeners != null) {
			for (int i = 0; i < _logicalWorkspaceTransactionListeners.length; i++) {
				_logicalWorkspaceTransactionListeners[i]
						.notifyCancelCreatedLink(this, link);
			}
		}
		LinkType lt = link.getLinkType();
		if (lt != null) {
			HashSet<LogicalWorkspaceTransactionListener> listener = computeLtListener(lt);
			if (listener.size() > 0) {
				for (LogicalWorkspaceTransactionListener list : listener) {
					try {
						list.notifyCancelCreatedLink(this, link);
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void addLogicalWorkspaceTransactionListener(
			LogicalWorkspaceTransactionListener l) {
		_logicalWorkspaceTransactionListeners = ArraysUtil.add(
				LogicalWorkspaceTransactionListener.class,
				_logicalWorkspaceTransactionListeners, l);
	}

	@Override
	public void removeLogicalWorkspaceTransactionListener(
			LogicalWorkspaceTransactionListener l) {
		_logicalWorkspaceTransactionListeners = ArraysUtil.remove(
				LogicalWorkspaceTransactionListener.class,
				_logicalWorkspaceTransactionListeners, l);
	}

	@Override
	public LogicalWorkspaceTransactionListener[] getLogicalWorkspaceTransactionListener() {
		return _logicalWorkspaceTransactionListeners;
	}

	@Override
	public CadseRuntime createCadseRuntime(String name, UUID runtimeId,
			UUID definitionId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CadseRuntime[] getCadseRuntime() {
		return base.getCadseRuntime();
	}

	@Override
	public ItemType createItemType(ItemType metaType, CadseRuntime cadseName,
			ItemType superType, int intID, UUID id, String shortName,
			String displayName, boolean hasContent, boolean isAbstract,
			IItemManager manager) {
		ItemType acc = null;

		// ItemDelta TypeA = createItem(CadseGCST.ITEM_TYPE, dm,
		// CadseGCST.DATA_MODEL_lt_TYPES);
		// TypeA.setName(static_generator.newName());
		// copy.commit();
		return acc;
	}

	@Override
	public Collection<Item> commit(boolean update, boolean forceToSave,
			boolean forceLoad,
			Collection<ProjectAssociation> projectAssociationSet)
			throws CadseException {
		this.update = update;
		this.forceToSave = forceToSave;
		this.forcetoLoad = forceLoad;
		this.notifier = getCadseDomain();
		this.projectAssociationSet = projectAssociationSet;
		this.notifyLoadedItem();
		base.commit(this, true);
		return this.loadedItems;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #addLoadedItem(fr.imag.adele.cadse.core.Item)
	 */
	public void addLoadedItem(Item item) {
		if (loadedItems == null) {
			this.loadedItems = new ArrayList<Item>();
		}
		this.loadedItems.add(item);
	}

	private void notifyLoadedItem() {
		if (this.all_operations == null) {
			return;
		}
		HashList<ItemType, ItemDelta> loadedItemMap = new HashList<ItemType, ItemDelta>();
		List<ItemDelta> allLoadedItems = new ArrayList<ItemDelta>();
		for (WLWCOperationImpl o : this.all_operations.values()) {
			if (o.getOperationType() == OperationTypeCst.ITEM_OPERATION) {
				ItemDelta itemOperation = (ItemDelta) o;
				if (!itemOperation.isLoaded())
					continue;
				allLoadedItems.add(itemOperation);
				ItemType it = itemOperation.getType();
				if (it == null) {
					continue;
				}
				loadedItemMap.add(it, itemOperation);
			}
		}
		if (_logicalWorkspaceTransactionListeners != null) {
			for (int i = 0; i < _logicalWorkspaceTransactionListeners.length; i++) {
				_logicalWorkspaceTransactionListeners[i].notifyLoadedItem(this,
						allLoadedItems);
			}
		}
		for (ItemType it : loadedItemMap.keySet()) {
			List<ItemDelta> loadedItems = loadedItemMap.get(it);

			LogicalWorkspaceTransactionListener[] l = it
					.getLogicalWorkspaceTransactionListener();
			if (l != null) {
				for (int i = 0; i < l.length; i++) {
					l[i].notifyLoadedItem(this, loadedItems);
				}
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #isUpdate()
	 */
	@Override
	public boolean isUpdate() {
		return update;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #isForceToSave()
	 */
	@Override
	public boolean isForceToSave() {
		return forceToSave;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #isForcetoLoad()
	 */
	@Override
	public boolean isForcetoLoad() {
		return forcetoLoad;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #getNotifier()
	 */
	@Override
	public IWorkspaceNotifier getNotifier() {
		return notifier;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.InternaleWorkspaceLogiqueWorkingCopy
	 * #getProjectAssociationSet()
	 */
	@Override
	public Collection<ProjectAssociation> getProjectAssociationSet() {
		return projectAssociationSet;
	}

	@Override
	public ItemDelta createItemIfNeed(ItemType itemType, Item parent,
			LinkType partLinkType, String uniqueName, String shortName,
			SetAttrVal<?>... attributes) throws CadseException {

		// create item already and compute the unique name.
		if (!itemType.hasShortNameAttribute()) {
			shortName = Item.NO_VALUE_STRING;
		}
		ItemDelta newItem = null;
		ItemDelta parentDelta = parent == null ? null : getItem(parent.getId());
		KeyDefinition keyType = itemType.getKeyDefinition();
		if (keyType != null) {
			IAttributeType<?>[] attribuesDefintions = keyType.getKeyElements();
			Object[] keyvaluse = new Object[attribuesDefintions.length];
			for (int i = 0; i < keyvaluse.length; i++) {
				if (attribuesDefintions[i] == CadseGCST.ITEM_at_NAME_) {
					keyvaluse[i] = shortName;
					continue;
				}
				for (int j = 0; j < attributes.length;) {
					SetAttrVal<?> v = attributes[j++];
					if (attribuesDefintions[i] == v.getAttrDef()) {
						keyvaluse[i] = v.getValue();
						break;
					}
				}
				if (keyvaluse[i] == null)
					throw new CadseException(
							NLS
									.bind(
											"Cannot find the value for the attribute definition {0} ({1}).",
											attribuesDefintions[i].getName(),
											attribuesDefintions[i].getCSTName()));
			}
			Key key = keyType.computeKey(parentDelta.getKey(), keyvaluse);
			newItem = getItem(key);
			if (newItem != null)
				return newItem;
		}
		newItem = createItem(itemType, parentDelta, partLinkType);
		newItem.setName(shortName);

		for (int i = 0; i < attributes.length;) {
			SetAttrVal<?> setAttr = attributes[i];

			if (setAttr.getAttrDef() instanceof LinkType) {
				newItem.createLink((LinkType) setAttr.getAttrDef(),
						(Item) setAttr.getValue());
			} else {
				newItem.setAttribute(setAttr.getAttrDef(), setAttr.getValue());
			}
		}

		if (!itemType.hasQualifiedNameAttribute()) {
			uniqueName = Item.NO_VALUE_STRING;
		} else
		// compute unique name at the end because can depend of some attribute
		if (uniqueName == null) {
			uniqueName = CadseCore.getName(newItem, shortName, parentDelta,
					partLinkType);
		}
		ItemDelta findItem = null;

		Key key = newItem.getKey();
		if (key != null) {
			findItem = getItem(key);
		}
		if (findItem == null && itemType.hasQualifiedNameAttribute()) {
			findItem = getItem(uniqueName);
		}

		if (findItem == null && parentDelta != null && partLinkType != null) {
			Collection<Item> findItems = parentDelta.getOutgoingItems(
					partLinkType, false);
			for (Item f : findItems) {
				if (f == newItem) {
					continue; // find other
				}

				if (f.getName().equals(shortName) && f.isInstanceOf(itemType)) {
					findItem = (ItemDelta) f;
					break;
				}
			}
		}

		if (findItem == null) {
			newItem.setQualifiedName(uniqueName);
		} else {
			newItem.delete(false);
			// cancel tout les liens qui pointe vers lui
			List<LinkDelta> oper = newItem.getIncomingLinkOperations();
			for (LinkDelta linkOperation : oper) {
				if (linkOperation.isAdded()) {
					linkOperation.delete();
				}
			}
			newItem = findItem;
			if (parentDelta != null) {
				createLinkIfNeed(parentDelta, findItem, partLinkType);
			}
		}

		return newItem;
	};

	/**
	 * Creates the item if need.
	 * 
	 * @param uniqueName
	 *            can be null
	 * @param shortname
	 *            the short name
	 * @param it
	 *            the type
	 * @param parent
	 *            the parent can be null if the item type has no part incoming
	 *            link type
	 * @param lt
	 *            the link type between parent.getType() and it, can be null if
	 *            the item type has no par incoming link type or if there is one
	 *            link type between parent.getType() and it
	 * @param attributes
	 *            the attributes : it's a pair of objects which types can be :
	 *            (linktype, item) or (String, item) or (String, Object).
	 * 
	 * @return a new item or find item
	 * 
	 * @throws CadseException
	 *             the melusine exception
	 */
	@Override
	public ItemDelta createItemIfNeed(String uniqueName, String shortname,
			ItemType it, Item parent, LinkType lt, Object... attributes)
			throws CadseException {

		// create item already and compute the unique name.
		if (!it.hasShortNameAttribute()) {
			shortname = Item.NO_VALUE_STRING;
		}
		ItemDelta parentDelta = parent == null ? null : getItem(parent.getId());
		KeyDefinition keyType = it.getKeyDefinition();
		if (keyType != null) {
			IAttributeType<?>[] attribuesDefintions = keyType.getKeyElements();
			Object[] keyvaluse = new Object[attribuesDefintions.length];
			for (int i = 0; i < keyvaluse.length; i++) {
				if (attribuesDefintions[i] == CadseGCST.ITEM_at_NAME_) {
					keyvaluse[i] = shortname;
					continue;
				}
				for (int j = 0; j < attributes.length;) {
					Object key = attributes[j++];
					Object value = attributes[j++];
					if (attribuesDefintions[i] == key
							|| attribuesDefintions[i].getName().equals(key)) {
						keyvaluse[i] = value;
						break;
					}
				}
				if (keyvaluse[i] == null)
					throw new CadseException(
							NLS
									.bind(
											"Cannot find the value for the attribute definition {0} ({1}).",
											attribuesDefintions[i].getName(),
											attribuesDefintions[i].getCSTName()));
			}
			Key key = keyType.computeKey(parentDelta.getKey(), keyvaluse);
			ItemDelta newItem = getItem(key);
			if (newItem != null)
				return newItem;
		}
		ItemDelta newItem = createItem(it, parentDelta, lt);
		newItem.setName(shortname);

		for (int i = 0; i < attributes.length;) {
			Object key = attributes[i++];
			Object value = attributes[i++];

			if (key instanceof LinkType) {
				newItem.createLink((LinkType) key, (Item) value);
			} else if (key instanceof IAttributeType<?>) {
				newItem.setAttribute((IAttributeType) key, value);
			}
		}

		if (!it.hasQualifiedNameAttribute()) {
			uniqueName = Item.NO_VALUE_STRING;
		} else
		// compute unique name at the end because can depend of some attribute
		if (uniqueName == null) {
			uniqueName = CadseCore.getName(newItem, shortname, parentDelta, lt);
		}
		ItemDelta findItem = null;

		Key key = newItem.getKey();
		if (key != null) {
			findItem = getItem(key);
			if (findItem == newItem)
				findItem = null;
		}
		if (findItem == null && it.hasQualifiedNameAttribute()) {
			findItem = getItem(uniqueName);
			if (findItem == newItem)
				findItem = null;
		}

		if (findItem == null && parentDelta != null && lt != null) {
			Collection<Item> findItems = parentDelta
					.getOutgoingItems(lt, false);
			for (Item f : findItems) {
				if (f == newItem) {
					continue; // find other
				}

				if (f.getName().equals(shortname) && f.isInstanceOf(it)) {
					findItem = (ItemDelta) f;
					break;
				}
			}
		}

		if (findItem == null) {
			newItem.setQualifiedName(uniqueName);
		} else {
			newItem.delete(false);
			// cancel tout les liens qui pointe vers lui
			List<LinkDelta> oper = newItem.getIncomingLinkOperations();
			for (LinkDelta linkOperation : oper) {
				if (linkOperation.isAdded()) {
					linkOperation.delete();
				}
			}
			newItem = findItem;
			if (parentDelta != null) {
				createLinkIfNeed(parentDelta, findItem, lt);
			}
		}

		return newItem;
	}

	/**
	 * Creates the link if need.
	 * 
	 * @param item
	 *            the item
	 * @param dest
	 *            the dest
	 * @param lt
	 *            the lt
	 * 
	 * @return the link
	 * 
	 * @throws CadseException
	 *             the melusine exception
	 */
	@Override
	public LinkDelta createLinkIfNeed(ItemDelta item, Item dest, LinkType lt)
			throws CadseException {
		LinkDelta ret;

		ret = item.getOutgoingLink(lt, dest.getId());
		if (ret != null) {
			return ret;
		}

		ret = item.createLink(lt, dest);
		return ret;
	}

	@Override
	public void rollback() {
		_commitState = false;
	}

	@Override
	public <T> T getAttribute(Item source, IAttributeType<T> type,
			boolean ownerOnly) {
		throw new UnsupportedOperationException();
	}

	public <T> T getAttribute(Item source, String key, boolean ownerOnly) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ContextVariable getNewContext() {
		if (_newContextVariable == null) {
			_newContextVariable = new NewContextVariable(this);
		}
		return _newContextVariable;
	}

	@Override
	public ContextVariable getOldContext() {
		if (_oldContextVariable == null) {
			_oldContextVariable = new OldContextVariable(this);
		}
		return _oldContextVariable;
	}

	public LinkDelta getLink(Link link) throws CadseException {
		ItemDelta delta = getItem(link.getSourceId());
		if (delta == null) {
			throw new CadseException(
					"the source of this link  not found in outgoing " + link);
		}
		return delta.getOutgoingLinkOperation(link);
	}

	public void commit(
			LogicalWorkspaceTransactionImpl logicalWorkspaceTransactionImpl,
			boolean b) throws CadseException {
		// todo merge hierachical transatcion

	}

	public void changeKey(ItemDeltaImpl itemDeltaImpl, Key oldkey, Key newkey)
			throws CadseException {
		if (itemDeltaImpl.isAdded()) {
			if (oldkey != null) {
				items_by_key.remove(oldkey);
			}
			Item findItem = getItem(newkey);
			if (findItem != null && findItem != itemDeltaImpl) {
				throw new CadseException(
						Messages.error_invalid_assignement_key_allready_exists,
						newkey);
			}
			items_by_key.put(newkey, itemDeltaImpl);
		} else if (itemDeltaImpl.isDeleted()) {
			items_by_key.remove(oldkey);
			items_by_key_deleted.put(oldkey, itemDeltaImpl);
		} else {
			if (itemDeltaImpl.getBaseItem() == null) {
				if (oldkey != null) {
					items_by_key.remove(oldkey);
					items_by_key_deleted.remove(oldkey);
				}
				if (newkey != null) {
					items_by_key.remove(newkey);
					items_by_key_deleted.remove(newkey);
				}
			} else {
				if (oldkey != null) {
					items_by_key.remove(oldkey);
				}
				Key baseKey = itemDeltaImpl.getBaseItem().getKey();
				if (baseKey != null) {
					items_by_key_deleted.put(baseKey, itemDeltaImpl);
				}
				if (containsSpaceKey(newkey)) {
					throw new CadseException(
							Messages.error_invalid_assignement_key_allready_exists,
							newkey);
				}
				items_by_key.put(newkey, itemDeltaImpl);
			}
		}
	}

	public void checkKey(ItemDeltaImpl itemDeltaImpl, Key oldkey, Key newkey)
			throws CadseException {
		if (itemDeltaImpl.isAdded()) {
			Item findItem = getItem(newkey);
			if (findItem != null && findItem != itemDeltaImpl) {
				throw new CadseException(
						Messages.error_invalid_assignement_key_allready_exists,
						newkey);
			}
		} else if (itemDeltaImpl.isDeleted()) {
		} else {
			if (itemDeltaImpl.getBaseItem() == null) {
			} else {
				if (containsSpaceKey(newkey)) {
					throw new CadseException(
							Messages.error_invalid_assignement_key_allready_exists,
							newkey);
				}
			}
		}
	}

	public void changeQualifiedName(ItemDeltaImpl itemDeltaImpl, String qName) {
		if (itemDeltaImpl.isAdded()) {
			items_by_unique_name.put(qName, itemDeltaImpl);
		} else if (itemDeltaImpl.isDeleted()) {
			items_by_unique_name_deleted.put(qName, itemDeltaImpl);
		} else {
			items_by_unique_name.put(qName, itemDeltaImpl);
		}

	}

	@Override
	public ContextVariable getContext() {
		return getNewContext();
	}

	@Override
	public NewContext[] getNewContextFrom(FilterContext context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IAttributeType<?> findAttribute(UUID key, UUID attSourceType,
			UUID attSourceCadsetype, UUID attTypeType, UUID attTypeCadsetype)
			throws CadseException {
		ItemDelta attrDef = getItem(key);
		if (attrDef != null)
			return attrDef.getAdapter(IAttributeType.class);

		TypeDefinition sourceType = findTypeDefinition(attSourceType,
				attSourceCadsetype, true);
		ItemType typeAttr = (ItemType) findTypeDefinition(attTypeType,
				attTypeCadsetype, true);

		return base.createUnresolvedAttributeType(sourceType, typeAttr, key,
				null);
	}

	@Override
	public IAttributeType<?> createUnresolvedAttributeType(
			TypeDefinition sourceType, ItemType attrType, UUID attrID,
			String attName) {
		return base.createUnresolvedAttributeType(sourceType, attrType, attrID,
				attName);
	}

	@Override
	public TypeDefinition findTypeDefinition(UUID id, UUID cadse,
			boolean createUnresolved) throws CadseException {
		if (cadse == null) {
			ItemDelta itemTypeDelta = getItem(id, false);
			if (itemTypeDelta != null) {
				if (itemTypeDelta.isModified())
					return itemTypeDelta.getAdapter(TypeDefinition.class);
				else
					return (TypeDefinition) itemTypeDelta.getBaseItem();
			}
		}
		ItemDelta cadseItem = getItem(cadse);
		if (cadseItem == null) {
			if (createUnresolved)
				return createUnresolvedItemType(id, cadse);
		}
		CadseRuntime cr = cadseItem.getAdapter(CadseRuntime.class);
		if (cr == null || !cr.isExecuted()) {
			if (createUnresolved)
				return createUnresolvedItemType(cadse, id);
		}

		return getTypeDefinition(cadse, id, createUnresolved);
	}

	@Override
	public ItemType createUnresolvedItemType(UUID cadseid, UUID id, String sn,
			String un) throws CadseException {
		return base.createUnresolvedItemType(cadseid, id, sn, un);
	}

	private TypeDefinition createUnresolvedItemType(UUID cadse, UUID id)
			throws CadseException {
		return base.createUnresolvedItemType(cadse, id, null, null);
	}

	@Override
	public LinkType findLinkType(UUID linkTypeID, UUID lTSourceType,
			UUID lTSourceCadsetype, UUID lTDestType, UUID lTDestCadsetype)
			throws CadseException {

		TypeDefinition it = findTypeDefinition(lTSourceType, lTSourceCadsetype,
				true);
		if (it.isResolved()) {
			ItemDelta lkDelta = getItem(linkTypeID);
			if (lkDelta != null)
				return lkDelta.getAdapter(LinkType.class);
		}
		TypeDefinition dest = findTypeDefinition(lTDestType, lTDestCadsetype,
				true);

		return createUnresolvedLinkType(linkTypeID, null, it, dest);
	}

	@Override
	public ItemDelta createEmptyItem(UUID id) {
		ItemDelta ret = getItemOperation(id);
		if (ret != null)
			return ret;
		ret = new ItemDeltaImpl(this, id, true);
		ret.setLoaded(true);
		return null;
	}

	@Override
	public IAttributeType<?> findAttribute(ItemDelta attHeader,
			ItemDelta attSourceHeader) throws CadseException {
		ItemDelta attrDef = getItem(attHeader.getId());
		if (attrDef != null)
			return attrDef.getAdapter(IAttributeType.class);

		TypeDefinition sourceType = findTypeDefinition(attSourceHeader, true);
		ItemType typeAttr = attHeader.getType();

		return base.createUnresolvedAttributeType(sourceType, typeAttr,
				attHeader.getId(), null);
	}

	@Override
	public LinkType findLinkType(ItemDelta ltHeader, ItemDelta ltSourceHeader,
			ItemDelta ltDestTypeHeader) throws CadseException {
		TypeDefinition it = findTypeDefinition(ltSourceHeader, true);
		if (it.isResolved()) {
			ItemDelta lkDelta = getItem(ltHeader.getId());
			if (lkDelta != null)
				return lkDelta.getAdapter(LinkType.class);
		}
		TypeDefinition dest = findTypeDefinition(ltDestTypeHeader, true);

		return createUnresolvedLinkType(ltHeader.getId(), null, it, dest);
	}

	@Override
	public TypeDefinition findTypeDefinition(ItemDelta destHeader,
			boolean createUnresolved) throws CadseException {
		CadseRuntime cadse = destHeader.getCadse();
		if (cadse == null) {
			if (createUnresolved)
				return createUnresolvedItemType(null, destHeader.getId(),
						destHeader.getName(), destHeader.getQualifiedName());
		}

		return getTypeDefinition(cadse, destHeader, createUnresolved);
	}

	public TypeDefinition getTypeDefinition(CadseRuntime cadseId,
			ItemDelta destHeader, boolean createUnresolvedType) {
		ItemDelta itemTypeDelta = getItem(destHeader.getId(), false);

		// ItemType itemType = base.getItemType(id);
		if (createUnresolvedType && itemTypeDelta == null) {
			try {
				return createUnresolvedItemType(cadseId.getId(), destHeader
						.getId(), destHeader.getName(), destHeader
						.getQualifiedName());
			} catch (CadseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		if (itemTypeDelta == null)
			return null;

		return itemTypeDelta.getAdapter(ItemType.class);
	}

	@Override
	public <T> T adapt(Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addKey(INamedUUID item, Key key) {
		// TODO Auto-generated method stub

	}

	@Override
	public void changeKey(INamedUUID item, Key oldKey, Key newKey) {
		// TODO Auto-generated method stub

	}

	@Override
	public Key[] getChildrenKey(Key aThis) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void commit(
			LogicalWorkspaceTransaction logicalWorkspaceTransactionImpl,
			boolean b) throws CadseException {
		// TODO Auto-generated method stub

	}

	@Override
	public LinkType findLinkType(ItemType sourceType, ItemType destType,
			UUID ltId, String ltName, boolean createUnresolvedObject) {
		return base.findLinkType(sourceType, destType, ltId, ltName,
				createUnresolvedObject);
	}

	@Override
	public ExtendedType createExtendedType(ItemType metaType,
			CadseRuntime cadseName, UUID uuid,
			String qualifiedName, String name) {
		// TODO Auto-generated method stub
		return base.createExtendedType(metaType, cadseName , uuid, qualifiedName, name);
	}

	@Override
	public Collection<ExtendedType> getExtendedTypes() {
		return base.getExtendedTypes();
	}

}
