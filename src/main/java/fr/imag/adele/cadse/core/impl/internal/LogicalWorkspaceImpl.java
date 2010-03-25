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

import fr.imag.adele.cadse.core.AdaptableObjectImpl;
import fr.imag.adele.cadse.core.INamedUUID;
import fr.imag.adele.cadse.core.internal.InternalLogicalWorkspace;
import fr.imag.adele.cadse.core.CadseException;
import java.util.UUID;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.imag.adele.cadse.core.CadseDomain;
import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.CadseRuntime;
import fr.imag.adele.cadse.core.ChangeID;
import fr.imag.adele.cadse.core.DefaultItemManager;
import fr.imag.adele.cadse.core.EventFilter;
import fr.imag.adele.cadse.core.ExtendedType;
import fr.imag.adele.cadse.core.IItemManager;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemDescriptionRef;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkDescription;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.Messages;
import fr.imag.adele.cadse.core.TypeDefinition;
import fr.imag.adele.cadse.core.WSEvent;
import fr.imag.adele.cadse.core.WSModelState;
import fr.imag.adele.cadse.core.WorkspaceListener;
import fr.imag.adele.cadse.core.CadseRuntime.Binding;
import fr.imag.adele.cadse.core.attribute.BooleanAttributeType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.transaction.delta.ImmutableWorkspaceDelta;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.transaction.delta.OperationType;
import fr.imag.adele.cadse.core.transaction.delta.OperationTypeCst;
import fr.imag.adele.cadse.core.transaction.delta.WLWCOperationImpl;
import fr.imag.adele.cadse.core.impl.CadseCore;
import fr.imag.adele.cadse.core.CadseIllegalArgumentException;
import fr.imag.adele.cadse.core.impl.CadseRuntimeImpl;
import fr.imag.adele.cadse.core.impl.attribute.AttributeTypeUnresolved;
import fr.imag.adele.cadse.core.impl.internal.delta.ItemDeltaImpl;
import fr.imag.adele.cadse.core.impl.internal.delta.LinkDeltaImpl;
import fr.imag.adele.cadse.core.internal.ILoggableAction;
import fr.imag.adele.cadse.core.internal.IWorkspaceNotifier;
import fr.imag.adele.cadse.core.key.Key;
import fr.imag.adele.cadse.core.key.KeyDefinition;
import fr.imag.adele.cadse.core.key.DefaultKeyDefinitionImpl;
import fr.imag.adele.cadse.core.key.FacetteItemTypeKey;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransaction;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransactionListener;
import fr.imag.adele.cadse.core.transaction.delta.ImmutableWorkspaceDelta;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.transaction.delta.OperationType;
import fr.imag.adele.cadse.core.transaction.delta.OperationTypeCst;
import fr.imag.adele.cadse.core.transaction.delta.WLWCOperationImpl;
import fr.imag.adele.cadse.core.ui.view.DefineNewContext;
import fr.imag.adele.cadse.core.ui.view.FilterContext;
import fr.imag.adele.cadse.core.ui.view.NewContext;
import fr.imag.adele.cadse.core.var.ContextVariable;
import fr.imag.adele.cadse.core.var.ContextVariableImpl;
import fr.imag.adele.cadse.util.ArraysUtil;
import fr.imag.adele.fede.workspace.as.initmodel.ErrorWhenLoadedModel;
import fr.imag.adele.teamwork.db.ModelVersionDBService2;
import java.util.UUID;

/**
 * The Class WorkspaceLogique.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class LogicalWorkspaceImpl implements LogicalWorkspace,
		InternalLogicalWorkspace {

	static public class CaptureNewOperation implements ILoggableAction {
		ILoggableAction log;
		private ArrayList<WLWCOperationImpl> removedOperations = new ArrayList<WLWCOperationImpl>();;
		private ArrayList<WLWCOperationImpl> addedOperations = new ArrayList<WLWCOperationImpl>();;

		public CaptureNewOperation(ILoggableAction log) {
			this.log = log;
		}

		public <T> void actionAddAttribute(UUID itemId, IAttributeType<T> key,
				T value) throws CadseException {
			if (log != null)
				log.actionAddAttribute(itemId, key, value);
		}

		public <T> void actionAddAttribute(LinkDescription linkDescription,
				IAttributeType<T> key, T value) throws CadseException {
			if (log != null)
				log.actionAddAttribute(linkDescription, key, value);
		}

		public <T> void actionChangeAttribute(UUID itemId,
				IAttributeType<T> key, T value) throws CadseException {
			if (log != null)
				log.actionChangeAttribute(itemId, key, value);
		}

		public <T> void actionChangeAttribute(LinkDescription linkDescription,
				IAttributeType<T> key, T value) throws CadseException {
			if (log != null)
				log.actionChangeAttribute(linkDescription, key, value);
		}

		public <T> void actionRemoveAttribute(UUID itemId, IAttributeType<T> key)
				throws CadseException {
			if (log != null)
				log.actionRemoveAttribute(itemId, key);
		}

		public <T> void actionRemoveAttribute(LinkDescription linkDescription,
				IAttributeType<T> key) throws CadseException {
			if (log != null)
				log.actionRemoveAttribute(linkDescription, key);
		}

		public void actionAddItem(ItemDescriptionRef itemDescriptionRef)
				throws CadseException {
			if (log != null) {
				log.actionAddItem(itemDescriptionRef);
			}
		}

		public void actionAddLink(LinkDescription linkDescription)
				throws CadseException {
			if (log != null) {
				log.actionAddLink(linkDescription);
			}
		}

		public void actionAddOperation(WLWCOperationImpl operation) {
			if (log != null) {
				log.actionAddOperation(operation);
			}
			addedOperations.add(operation);
		}

		public void actionRemoveItem(ItemDescriptionRef itemDescriptionRef)
				throws CadseException {
			if (log != null) {
				log.actionRemoveItem(itemDescriptionRef);
			}
		}

		public void actionRemoveLink(LinkDescription linkDescription)
				throws CadseException {
			if (log != null) {
				log.actionRemoveLink(linkDescription);
			}

		}

		public void actionRemoveOperation(WLWCOperationImpl operation) {
			if (log != null) {
				log.actionRemoveOperation(operation);
			}
			removedOperations.add(operation);
			addedOperations.remove(operation);
		}
	}

	/** The item types. */
	private Collection<ItemType> _itemTypes;
	/** The item types. */
	
	private Collection<ExtendedType> _extTypes;
	
	protected CadseRuntime[] _cadses = null;

	/** The state. */
	WSModelState _state = WSModelState.INIT;

	/** The items. */
	Map<UUID, Item> _items;

	/** The items_by_key. */
	private Map<Key, Item> _items_by_key;

	/** The items_by_unique_name. */
	private Map<String, Item> _items_by_qualified_name;

	/** The wd. */
	CadseDomainImpl _wd;
	WorkspaceListener[] _listeners = null;
	int[] _filter = null;

	private IItemManager _unresolveManager = new DefaultItemManager();
	LogicalWorkspaceTransactionListener[] _workspaceLogiqueCopyListeners = null;

	private Logger _logger = Logger
			.getLogger("fr.imag.adele.cadse.logicalworkspace");

	public class WorkspaceLogigueWorkspaceListener extends WorkspaceListener {
		@Override
		public void workspaceChanged(ImmutableWorkspaceDelta wd) {
			try {
				if (wd.getEvents() != null) {
					for (WSEvent wse : wd.getEvents()) {
						try {
							notifieChangeEvent(wse);
						} catch (Throwable e) {
							_wd.log(Messages.error, Messages.error_in_event
									+ wse, e);
						}
					}
				}
			} catch (RuntimeException e) {
				_wd.log(Messages.error, Messages.error_in_event, e);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * fr.imag.adele.cadse.core.IWSNotifieChange#notifieChangeEvent(fr.imag
		 * .adele.cadse.core.ChangeID, java.lang.Object[])
		 */
		@SuppressWarnings("unchecked")
		public void notifieChangeEvent(WSEvent wse) {
			try {
				Link link;
				Item item;
				switch (wse.getEventTypeId()) {
				case RESOLVE_INCOMING_LINK:
					item = (Item) wse.getOperationArgs()[0];
					link = (Link) wse.getOperationArgs()[1];

					// if (link.isComposition()) {
					// addComponent(link, item);
					// }

					// creation des liens d�rived.
					// createDerivedLinkFromLink(link);
					break;

				case CREATE_ITEM:
					item = (Item) wse.getOperationArgs()[0];

					if (item instanceof ItemImpl) {
						// recalcul les information de type composants et
						// derived
						// link.
						// seulement si l'item est ouvert.
						// l'information obtenu n'est par forc�ment
						// complete.
						// Elle se completera au fure et � mesure.
						((ItemImpl) item)._composants = ((ItemImpl) item)
								.computeComponents(true);
						// ((ItemImpl) item).reComputeDerivedLink(false);

					}
					break;
				case DELETE_OUTGOING_LINK:
					link = (Link) wse.getOperationArgs()[0];
					if (link.getLinkType().isComposition()) {
						// il faut surment enlever un composant.
						removeComponent((ItemImpl) link.getSource());
					}

					break;

				case ADD_COMPONENT:
					// Je mets � jours la liste de composition des composite
					// parent...
					item = (Item) wse.getOperationArgs()[0];
					for (Link l : item.getIncomingLinks()) {
						if (l.getLinkType().isComposition()) {
							addComponent((ItemImpl) l.getSource(),
									(List<Item>) wse.getOperationArgs()[1]);
						}
					}
					break;
				case REMOVE_COMPONENT:
					item = (Item) wse.getOperationArgs()[0];
					for (Link l : item.getIncomingLinks()) {
						if (l.getLinkType().isComposition()) {
							removeComponent((ItemImpl) l.getSource());
						}
					}
					break;

				default:
					break;
				}
			} catch (CadseIllegalArgumentException e) {
				_wd.log("wl",
						"Error in Processing event "
								+ wse.getEventTypeId().toString(
										wse.getOperationArgs()), e);
			}
		}
	}

	/**
	 * Instantiates a new workspace logique.
	 * 
	 * @param wd
	 *            the wd
	 */
	public LogicalWorkspaceImpl(CadseDomainImpl wd) {
		this._items = new HashMap<UUID, Item>();
		this._items_by_key = new HashMap<Key, Item>();
		this._items_by_qualified_name = new HashMap<String, Item>();
		this._wd = wd;
		_itemTypes = new ArrayList<ItemType>();
		_extTypes = new ArrayList<ExtendedType>();
		addListener(new WorkspaceLogigueWorkspaceListener(), ChangeID.toFilter(
				ChangeID.REMOVE_COMPONENT, ChangeID.ADD_COMPONENT,
				ChangeID.DELETE_OUTGOING_LINK, ChangeID.CREATE_ITEM,
				ChangeID.RESOLVE_INCOMING_LINK));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IWorkspaceLogique#getCadseName()
	 */
	public String[] getCadseName() {
		if (_cadses == null) {
			return new String[0];
		}
		String[] ret = new String[_cadses.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = _cadses[i].getQualifiedName();
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IWorkspaceLogique#getCadseVersion()
	 */
	public int[] getCadseVersion() {
		if (_cadses == null) {
			return new int[0];
		}
		int[] ret = new int[_cadses.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = _cadses[i].getVersion();
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IWorkspaceLogique#loadMetaModel()
	 */
	public LinkType createMLTIfNeed() throws CadseException {
		return CadseCore.theItemType.createLinkType(CadseDomain.META_LINK_ID,
				-1, "#mLT", 0, 0, -1, null, CadseCore.theItemType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IWorkspaceLogique#getState()
	 */
	public WSModelState getState() {
		return _state;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.IWorkspaceLogique#setState(fr.imag.adele.cadse
	 * .core.WSModelState)
	 */
	public void setState(WSModelState state) {
		this._state = state;
		((CadseDomainImpl) _wd).notifieChangeEventSingle(ChangeID.MODEL_STATE,
				this, this._state, state);

	}

	/**
	 * Create an item.
	 * 
	 * NOTE: After initializing a new item, a link between parent and this item
	 * will be created.
	 * 
	 * @param it
	 *            : item type.
	 * @param parent
	 *            : item's parent. can be null if is not an containment
	 * @param lt
	 *            : link type. can be null
	 * 
	 * @return new item.
	 * 
	 * @throws CadseException
	 *             the melusine exception
	 * 
	 * @OCL<br> context: Workspace::createItem(String id, ItemType it) : Item<br>
	 *          1. pre: id <> null <br/>
	 *          2. pre: id <> '' <br/>
	 *          3. pre: it <> null <br/>
	 *          6. pre: items->forAll(item | item.id <> id )<br/>
	 *          7. pre: self.type.selectedItemtype->include(it) <br/>
	 *          8. pre: parent.type = lt.source and it = lt.dest<br/>
	 * @exception CadseIllegalArgumentException
	 *                : Id can not be null. CadseIllegalArgumentException : Id
	 *                can not be empty. CadseIllegalArgumentException : Parent
	 *                can not be null. CadseIllegalArgumentException : Link type
	 *                can not be null. CadseIllegalArgumentException : Invalid
	 *                assignment, this item <tt>$id</tt> already exist.<br/>
	 *                CadseIllegalArgumentException : Invalid assignment, type
	 *                of this item is not supported for this kind of workspace.<br/>
	 *                CadseIllegalArgumentException : Link type is not correct.
	 *                CadseIllegalArgumentException : You must specifie the
	 *                parent for the containement item
	 *                CadseIllegalArgumentException : You must specifie a
	 *                parent(<tt>$parent</tt>) of type <tt>$lt.source</tt> not
	 *                of type <tt>$parent.type</tt>
	 */
	public Item createItem(ItemType it, Item parent, LinkType lt)
			throws CadseException {
		LogicalWorkspaceTransaction copy = createTransaction();
		ItemDelta copyRet = copy.createItem(it, parent, lt);
		copy.commit();
		return copyRet.getBaseItem();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.IWorkspaceLogique#createItem(fr.imag.adele.cadse
	 * .core.ItemType, fr.imag.adele.cadse.core.Item,
	 * fr.imag.adele.cadse.core.LinkType, fr.imag.adele.cadse.core.UUID,
	 * java.lang.String, java.lang.String)
	 */
	public Item createItem(ItemType it, Item parent, LinkType lt, UUID id,
			String longName, String shortName) throws CadseException {
		LogicalWorkspaceTransaction copy = createTransaction();
		ItemDelta copyRet = copy.createItem(it, parent, lt, id, longName,
				shortName);
		copy.commit();
		return copyRet.getBaseItem();
	}

	public void setAttribute(Item item, IAttributeType<?> key, Object value)
			throws CadseException {
		if (!item.exists()) {
			item.commitSetAttribute(key, value);
			return;
		}
		LogicalWorkspaceTransaction copy = createTransaction();
		ItemDelta itemOperation = copy.getItem(item.getId());
		if (itemOperation == null) {
			throw new CadseException("Cannot found item "
					+ item.getDisplayName());
		}
		itemOperation.setAttribute(key, value);
		copy.commit();
	}

	/**
	 * Creates the item.
	 * 
	 * @param it
	 *            the it
	 * 
	 * @return the item
	 * 
	 * @throws CadseException
	 *             the melusine exception
	 */
	public Item createItem(ItemType it) throws CadseException {
		return createItem(it, null, null);
	}

	/**
	 * Get resolved item by id.
	 * 
	 * @param id
	 *            the id
	 * 
	 * @return an item if found, null if not found.
	 */
	public Item getItem(UUID id) {
		Item i = _items.get(id);
		/*
		 * if (i != null && i.isResolved()) { return i; }
		 */
		return i;
	}

	/**
	 * Get resolved item by unique name.
	 * 
	 * @param uniqueName
	 *            l'identifier unique calculer � partir du short-name
	 * 
	 * @return the item
	 */
	public Item getItem(String uniqueName) {
		Item i = _items_by_qualified_name.get(uniqueName);
		if (i != null && i.isResolved()) {
			return i;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.IWorkspaceLogique#getItemByShortName(fr.imag
	 * .adele.cadse.core.ItemType, java.lang.String)
	 */
	public Item getItemByName(TypeDefinition type, String name) {

		if (type instanceof ExtendedType) {
			ItemType[] extendedType = ((ExtendedType) type).getExendsItemType();
			for (int i = 0; i < extendedType.length; i++) {
				Item item = getItemByName(extendedType[i], name);
				if (item != null)
					return item;
			}
			return null;
		}
		KeyDefinition spacekeytype = ((ItemType) type).getKeyDefinition();
		if (spacekeytype != null && spacekeytype.getParentKey() == null
				&& spacekeytype.getKeyElements().length == 1
				&& spacekeytype.getKeyElements()[0] == CadseGCST.ITEM_at_NAME_) {
			try {
				Key key;
				key = spacekeytype.computeKey(null, name);
				return getItem(key);
			} catch (CadseException e) {
				_wd.log("wl", "error when search compute a key of type " + type
						+ " for name " + name, e);
			}
		}
		return null;
	}

	/**
	 * Get the items by item type not items of sub item type.
	 * 
	 * @param it
	 *            : item type to seek.
	 * 
	 * @return a list of items.
	 */
	public List<Item> getItems(String it) {
		List<Item> items_ret = new ArrayList<Item>();
		for (Iterator item = _items.values().iterator(); item.hasNext();) {
			Item i = ((Item) item.next());
			if (i.isResolved() && i.getType().getId().equals(it)) {
				items_ret.add(i);
			}
		}
		return items_ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IWorkspaceLogique#getWorkspaceDomain()
	 */
	public CadseDomain getCadseDomain() {
		return _wd;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.IWorkspaceLogique#canDeleteLink(fr.imag.adele
	 * .cadse.core.Link)
	 */
	public boolean canDeleteLink(Link link) {
		return !link.isReadOnly();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.IWorkspaceLogique#canDeleteInverseLink(fr.imag
	 * .adele.cadse.core.Link)
	 */
	public boolean canDeleteInverseLink(Link link) {
		return true;
	}

	/**
	 * Remove an item.
	 * 
	 * @param item
	 *            the item
	 */
	void removeItem(Item item) {
		// if (item == root)
		// throw new
		// CadseIllegalArgumentException(Messages."WSModelImpl.11",item.getId(),
		// getModelType().getModelName()); //$NON-NLS-1$
		this._items.remove(item.getId());
		if (item.getKey() != null) {
			this._items_by_key.remove(item.getKey());
		}
		if (item.getQualifiedName() != null) {
			this._items_by_qualified_name.remove(item.getQualifiedName());
		}
		if (item instanceof ItemType) {
			removeItemType((ItemType) item);
		}
		if (item instanceof ExtendedType) {
			removeExtendedType((ExtendedType) item);
		}
	}

	protected void removeItemType(ItemType item) {
		this._itemTypes.remove(item);
		ExtendedType[] exts = item.getExtendedType();
		if (exts != null) {
			for (ExtendedType extendedType : exts) {
				extendedType.removeExendsItemType(item);
			}
		}
	}
	
	protected void removeExtendedType(ExtendedType item) {
		this._extTypes.remove(item);
		ItemType[] exts = item.getExendsItemType();
		for (ItemType it : exts) {
			it.removeExtendedType(item);
		}
	}

	/**
	 * Adds the keys.
	 * 
	 * @param aNewItem
	 *            the new item
	 */
	void addKeys(Item aNewItem) {
		Key key = aNewItem.getKey();
		if (key != null) {
			this._items_by_key.put(key, aNewItem);
		}
		if (aNewItem.getQualifiedName() != null) {
			this._items_by_qualified_name.put(aNewItem.getQualifiedName(),
					aNewItem);
		}
	}

	void removeKeys(Item anItem) {
		Key key = anItem.getKey();
		if (key != null) {
			this._items_by_key.remove(key);
		}
		if (anItem.getQualifiedName() != null) {
			this._items_by_qualified_name.remove(anItem.getQualifiedName());
		}
	}

	/**
	 * Adds the id.
	 * 
	 * @param newItem
	 *            the new item
	 * @param loadedItemsProcess
	 * @param notifie
	 *            the notifie
	 * 
	 * @return the item
	 */
	Item addId(Item newItem, IWorkspaceNotifier notifier,
			TransactionItemsProcess loadedItemsProcess) {
		Item oldItem = this._items.get(newItem.getId());
		if (oldItem != null && oldItem.isResolved()) {
			if (loadedItemsProcess != null) {
				loadedItemsProcess
						.addError(newItem.getId(), MessageFormat.format(
								Messages.error_duplicate_item, newItem.getId()));
				return oldItem;
			}
			throw new CadseIllegalArgumentException(
					Messages.error_duplicate_item + newItem.getId());
		}
		// add item in registry
		this._items.put(newItem.getId(), newItem);
		if (newItem instanceof ItemType) {
			// if item is a type
			// add this in item type register
			_itemTypes.add((ItemType) newItem);
		}
		if (newItem instanceof ExtendedType) {
			// if item is a type
			// add this in item type register
			_extTypes.add((ExtendedType) newItem);
		}
		resolveItem(newItem, notifier, oldItem);
		return oldItem;
	}

	private void resolveItem(Item newItem, IWorkspaceNotifier notifier,
			Item oldItem) {
		if (oldItem != null) {
			// Resolve all unseresolved links in workspace.
			if (!oldItem.isResolved()) {
				for (Link l : new ArrayList<Link>(oldItem.getIncomingLinks())) {
					if (l.resolve()) {
						if (notifier != null) {
							notifier.notifieChangeEvent(
									ChangeID.RESOLVE_INCOMING_LINK, l
											.getResolvedDestination(), l);
						}
					}
				}
			}

			for (Item item : this._items.values()) {
				if (item.isResolved() && item instanceof ItemImpl) {
					ItemImpl iimpl = (ItemImpl) item;
					iimpl.resolveComponent(newItem);
				}
			}
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
	 * @exception CadseIllegalArgumentException
	 *                : Id can not be null. CadseIllegalArgumentException : Id
	 *                can not be empty. CadseIllegalArgumentException : Invalid
	 *                assignment, this item <tt>$id</tt> already exist.<br/>
	 *                CadseIllegalArgumentException : Invalid assignment, type
	 *                of this item is not supported for this kind of workspace.<br/>
	 *                CadseIllegalArgumentException : Invalid assignment, link
	 *                type is not correct. CadseIllegalArgumentException :
	 *                Invalid assignment, item parent <tt>$parent.id</tt> + " is
	 *                in state read only.
	 */

	private void preconditions_createItem(Item THIS, ItemType it, Item parent,
			LinkType lt) {

		// pre: it <> null
		if (it == null) {
			throw new CadseIllegalArgumentException(
					Messages.error_item_type_is_null2);
		}

		// TODO on l'enleve temporairement checkUniqueName(THIS); il faut bien
		// sur la remetre en novembre 08

		if (it.isPartType()) {
			if (parent == null) {
				throw new CadseIllegalArgumentException(
						Messages.error_you_must_specifi_parent_item);
			}
			if (lt == null) {
				throw new CadseIllegalArgumentException(
						Messages.error_parent_link_type_is_null);
			}

			if (!lt.isPart()) {
				throw new CadseIllegalArgumentException(
						Messages.error_parent_link_type_not_part, lt);
			}
		}

		if (parent == null || lt == null) {
			return;
		}

		// parent.type = lt.source.type
		if (!parent.isInstanceOf(lt.getSource())) {
			throw new CadseIllegalArgumentException(
					Messages.error_cannot_create_an_item_bad_source, parent
							.getName(), lt.getName(), lt.getSource().getId(),
					parent.getType().getId());
		}

		if (!(isLinkCompatible(lt, it))) {
			throw new CadseIllegalArgumentException(
					Messages.error_cannot_create_an_item_bad_destination,
					parent.getName(), lt.getName(), lt.getDestination()
							.getName(), lt.getDestination().getId(), it
							.getName(), it.getId());
		}

	}

	public static boolean isLinkCompatible(LinkType lt, ItemType it) {
		TypeDefinition destType = lt.getDestination();
		if (destType.isMainType()) {
			ItemType destItemType = (ItemType) destType;
			return destItemType.isSuperTypeOf(it) || it == destItemType;
		}
		ExtendedTypeImpl extType = (ExtendedTypeImpl) destType;
		for (ItemType destItemType : extType.getExendsItemType()) {
			boolean ret = destItemType.isSuperTypeOf(it) || it == destItemType;
			if (ret)
				return true;
		}
		return false;
	}

	/**
	 * Check unique name.
	 * 
	 * @param THIS
	 *            the tHIS
	 * 
	 * @throws CadseIllegalArgumentException
	 *             the melusine error
	 */
	public void checkUniqueName(Item THIS) throws CadseIllegalArgumentException {
		Key key = THIS.getKey();
		if (key != null) {
			// pre: items->forAll(item | item.id <> id )
			Item i = _items_by_key.get(key);

			if (i == null) {
				return;
			}

			if (i == THIS) {
				return;
			}
			if (i.isResolved()) {
				throw new CadseIllegalArgumentException(
						Messages.error_invalid_assignement_allready_exists,
						THIS.getName());
			}
			if (!i.getType().equals(THIS.getType())) {
				throw new CadseIllegalArgumentException(
						Messages.error_invalid_assignement_bad_type, THIS
								.getName(), THIS.getType().getName(), i
								.getType().getId());
			}
		}
	}

	/**
	 * Check unique name for rename.
	 * 
	 * @param THIS
	 *            the tHIS
	 * @param shortName
	 *            the short name
	 * @param uniqueName
	 *            the unique name
	 * 
	 * @throws CadseIllegalArgumentException
	 *             the melusine error
	 * @throws CadseException
	 */
	public void checkUniqueNameForRename(Item THIS, String shortName,
			String uniqueName) throws CadseIllegalArgumentException,
			CadseException {
		KeyDefinition spacetype = THIS.getType().getKeyDefinition();
		if (spacetype != null) {
			Key newkey = spacetype.computeKey(THIS);
			newkey.setName(shortName);

			// pre: items->forAll(item | item.id <> id )
			Item i = _items_by_key.get(newkey);
			check(i, THIS);
		}

		if (THIS.getType().hasQualifiedNameAttribute() && uniqueName != null
				&& uniqueName != Item.NO_VALUE_STRING) {
			Item i = _items_by_qualified_name.get(uniqueName);
			check(i, THIS);
		}
	}

	/**
	 * Check.
	 * 
	 * @param i
	 *            the i
	 * @param THIS
	 *            the tHIS
	 */
	private void check(Item i, Item THIS) {
		if (i == null || i == THIS) {
			return;
		}

		if (i.isResolved()) {
			throw new CadseIllegalArgumentException(
					Messages.error_invalid_assignement_allready_exists, THIS
							.getName());
		}
		if (!i.getType().equals(THIS.getType())) {
			throw new CadseIllegalArgumentException(
					Messages.error_invalid_assignement_bad_type,
					THIS.getName(), THIS.getType().getName(), i.getType()
							.getId());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IWorkspaceLogique#getUnresolvedLinks()
	 */
	public List<Link> getUnresolvedLinks() {
		List<Item> itemsunresolved = getUnresolvedItem();
		List<Link> ret = new ArrayList<Link>();
		for (Item item : itemsunresolved) {
			ret.addAll(item.getIncomingLinks());
		}
		return ret;
	}

	/**
	 * Gets the unresolved link.
	 * 
	 * @param id
	 *            the id
	 * 
	 * @return the unresolved link
	 */
	public List<Link> getUnresolvedLink(UUID id) {
		List<Link> ret = new ArrayList<Link>();
		Item item = getItem(id);
		if (item == null) {
			return ret;
		}
		ret.addAll(item.getIncomingLinks());
		return ret;
	}

	/**
	 * Get all destination ids of all unresolved links in workspace.
	 * 
	 * @return the unresolved item
	 */
	public List<Item> getUnresolvedItem() {
		List<Item> ret = new ArrayList<Item>();
		for (Item item : this.getItems()) {
			if (((AbstractItem) item).isResolved()) {
				continue;
			}
			ret.add(item);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.IWorkspaceLogique#getItem(fr.imag.adele.cadse
	 * .core.key.ISpaceKey)
	 */
	public Item getItem(Key key) {
		return _items_by_key.get(key);
	}

	@Override
	public List<Item> getItems(TypeDefinition it) {
		List<Item> items_ret = new ArrayList<Item>();
		for (Iterator item = _items.values().iterator(); item.hasNext();) {
			Item i = ((Item) item.next());
			if (i.isResolved() && i.isInstanceOf(it)) {
				items_ret.add(i);
			}
		}
		return items_ret;
	}

	/**
	 * Get all items.
	 * 
	 * @return the items
	 */
	public Collection<Item> getItems() {
		List<Item> ret = new ArrayList<Item>();
		for (Item item : _items.values()) {
			if (item.isResolved()) {
				ret.add(item);
			}
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.IWorkspaceLogique#existsItem(fr.imag.adele.cadse
	 * .core.Item)
	 */
	public boolean existsItem(Item item) {
		Key key = getKeyItem(item, null, _logger);
		if (key != null) {
			Item foundItem = this._items_by_key.get(key);
			if (foundItem == null || foundItem == item)
				return false;

			return true;
		}
		if (!item.getType().hasQualifiedNameAttribute()) {
			return false;
		}

		String qname = item.getQualifiedName();
		if (qname == null || qname == Item.NO_VALUE_STRING) {
			return true;
		}

		Item foundItem = _items_by_qualified_name.get(qname);
		if (foundItem == null || foundItem == item)
			return false;

		return true;
	}
	
	public boolean exists(INamedUUID object) {
		return _items.containsKey(object.getId());
	}

	/**
	 * 
	 * @param item
	 *            an item (not null)
	 * @param name
	 *            a name or null (to change the name)
	 * @return a key
	 */
	public static Key getKeyItem(Item item, String name, Logger logger) {
		KeyDefinition spacetype = item.getType().getKeyDefinition();
		if (spacetype != null) {
			Key key = null;
			try {
				key = spacetype.computeKey(item);
			} catch (CadseException e) {
				logger.log(Level.SEVERE, "cannot compute compute a key of "
						+ item + " for name " + name, e);
				return null;
			}
			if (name != null) {
				key.setName(name);
			}
			return key;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.IWorkspaceLogique#existsItem(fr.imag.adele.cadse
	 * .core.Item, java.lang.String)
	 */
	public boolean existsItem(Item item, String shortName)
			throws CadseException {
		Key key = getKeyItem(item, shortName, _logger);
		if (key != null) {
			return containsSpaceKey(key);
		}
		if (!item.getType().hasQualifiedNameAttribute()) {
			return false;
		}
		String un = item.getType().getItemManager().computeQualifiedName(item,
				shortName, item.getPartParent(false),
				item.getPartParentLinkType());
		return un == null || un == Item.NO_VALUE_STRING
				|| containsUniqueName(un);

	}

	public boolean containsUniqueName(String key) {
		if (key == null || key == Item.NO_VALUE_STRING) {
			return true;
		}
		return this._items_by_qualified_name.containsKey(key);
	}

	public boolean containsSpaceKey(Key key) {
		return this._items_by_key.containsKey(key);
	}

	/**
	 * Ajoute une liste de composant ayant �t� ajout� � l'un de ces fils
	 * � l'item source.
	 * 
	 * @param source
	 *            L'item auquel on ajout des composant.
	 * @param addedsource
	 *            Une liste de composants qu'un de ces fils a ajout�.
	 */
	private void addComponent(ItemImpl source, List<Item> addedsource) {
		if (source._composants == null) {
			source._composants = new HashMap<UUID, Item>();
		}
		List<Item> added = new ArrayList<Item>();

		for (Item il : addedsource) {
			if (source._composants.put(il.getId(), il) == null) {
				added.add(il);
			}
		}
		if (added.size() > 0) {
			_wd.notifieChangeEvent(ChangeID.ADD_COMPONENT, source, added);
		}
	}

	// /**
	// * Adds the composant.
	// *
	// * @param cl
	// * the cl
	// * @param item
	// * the item
	// */
	// private void addComponent(Link cl, Item item) {
	// ItemImpl source = (ItemImpl) cl.getSource();
	//
	// if (source._composants == null) {
	// source._composants = new HashMap<UUID, Item>();
	// }
	// List<Item> added = new ArrayList<Item>();
	//
	// Item icl = cl.getDestination();
	// if (source._composants.put(icl.getId(), icl) == null) {
	// added.add(icl);
	// }
	// for (Item il : item.getComponents()) {
	// if (source._composants.put(il.getId(), il) == null) {
	// added.add(il);
	// }
	// }
	// if (added.size() > 0) {
	// _wd.notifieChangeEvent(ChangeID.ADD_COMPONENT, source, added);
	// }
	// }

	/**
	 * Removes the composant.
	 * 
	 * @param source
	 *            the source
	 */
	private void removeComponent(ItemImpl source) {
		if (source._composants == null) {
			source._composants = new HashMap<UUID, Item>();
		}
		Map<UUID, Item> result = source.computeComponents(true);

		for (UUID id : result.keySet()) {
			source._composants.remove(id);
		}
		ArrayList<Item> removed = new ArrayList<Item>(source._composants
				.values());
		source._composants = result;
		if (removed.size() > 0) {
			_wd.notifieChangeEvent(ChangeID.REMOVE_COMPONENT, source, removed);
		}
	}

	// /**
	// * Creates the derived link from link.
	// *
	// * @param link
	// * the link
	// */
	// private void createDerivedLinkFromLink(Link link) {
	// if (link.getLinkType().isComposition()) {
	// // un lien de composition est cr��.
	// // la source du lien est le composite auquel on cherche � rajouter
	// // des liens d�riv�s ou � en enlever.
	// // Il est pr�f�rable de recalculer les lien d�riv�es.
	// ((ItemImpl) link.getSource()).reComputeDerivedLink(false);
	// } else {
	// // deuxi�me cas un lien non composite a �t� cre�,
	// // il faut int�grer ce lien � tout les composite �ventuelle auquel
	// // appartiendrai la source du lien.
	// for (Link cl : link.getSource().getIncomingLinks()) {
	// if (cl.getLinkType().isComposition()) {
	// ItemImpl source = (ItemImpl) cl.getSource();
	// if (!source.containsComponent(link.getDestinationId())) {
	// if (source._derivedLinks == null) {
	// source._derivedLinks = new HashSet<DerivedLink>();
	// }
	// DerivedLink dl = source.createOneDerivedLink(link);
	// if (source._derivedLinks.add(dl)) {
	//
	// }
	// }
	// }
	// }
	// }
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Messages.model_to_string + Arrays.asList(getCadseName());
	}

	/**
	 * Gets the item.
	 * 
	 * @param id
	 *            the id
	 * @param type
	 *            the type
	 * @param uniqueName
	 *            the unique name
	 * @param shortname
	 *            the shortname
	 * 
	 * @return the item
	 * 
	 * @throws CadseException
	 *             the melusine exception
	 */
	synchronized Item getItem(UUID id, ItemType type, String uniqueName,
			String shortname) throws CadseException {
		Item i = _items.get(id);

		if (i == null) {

			if (type == CadseGCST.CADSE) {
				i = new CadseRuntimeImpl(shortname, id, id);
			} else {
				i = new ItemUnresolved(id, type, uniqueName, shortname);
			}
			i.setFlag(Item.UNRESOLVED, true);
			this._items.put(id, i);
		} else {
			if (!i.getType().equals(type)) {
				throw new CadseException(Messages.error_bad_type, i.getType()
						.getId(), type.getId(), shortname);
			}
		}
		return i;
	}

	/**
	 * Rename unique name.
	 * 
	 * @param impl
	 *            the impl
	 * @param oldValue
	 *            the old value
	 * @param newValue
	 *            the new value
	 */
	public void renameUniqueName(Item impl, String oldValue, String newValue) {
		if (oldValue != null) {
			this._items_by_qualified_name.remove(oldValue);
		}
		if (newValue != null) {
			this._items_by_qualified_name.put(newValue, impl);
		}
	}

	/**
	 * Removes the item in key map.
	 * 
	 * @param impl
	 *            the impl
	 */
	public void removeItemInKeyMap(Item impl) {
		Key key = impl.getKey();
		if (key != null) {
			this._items_by_key.remove(key);
		}
	}

	/**
	 * Adds the item in key map.
	 * 
	 * @param impl
	 *            the impl
	 */
	public void addItemInKeyMap(Item impl) {
		Key key = impl.getKey();
		if (key != null) {
			this._items_by_key.put(key, impl);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.IWorkspaceLogique#loadItem(fr.imag.adele.cadse
	 * .core.ItemDescriptionRef)
	 */
	synchronized public Item loadItem(ItemDescriptionRef ref)
			throws CadseException {
		if (ref == null) {
			return null;
		}

		ItemType itemtype = getItemType(ref.getTypeObject().getId());
		if (itemtype == null) {
			System.err.println(Messages.bind(Messages.error_cannot_find_type,
					ref.getType()));
			throw new CadseException(Messages.bind(
					Messages.error_cannot_find_type, ref.getType()));
		}
		return getItem(ref.getId(), itemtype, ref.getQualifiedName(), ref
				.getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.IWorkspaceLogique#loadItem(fr.imag.adele.cadse
	 * .core.ItemDescriptionRef)
	 */
	synchronized public Item loadItem(ItemDelta ref) throws CadseException {
		if (ref == null) {
			return null;
		}

		ItemType itemtype = getItemType(ref.getType().getId());
		if (itemtype == null) {
			System.err.println(Messages.bind(Messages.error_cannot_find_type,
					ref.getType()));
			throw new CadseException(Messages.bind(
					Messages.error_cannot_find_type, ref.getType()));
		}
		return getItem(ref.getId(), itemtype, ref.getQualifiedName(), ref
				.getName());
	}

	/**
	 * Gets the item type.
	 * 
	 * @param desc
	 *            the desc
	 * @param unresolvedType
	 *            the unresolved type
	 * 
	 * @return the item type
	 * 
	 * @throws CadseException
	 *             the melusine exception
	 */
	public ItemType getItemType(ItemDescriptionRef desc,
			Map<UUID, String> unresolvedType) throws CadseException {
		ItemType itemtype = getItemType(desc.getTypeObject().getId());
		if (itemtype != null) {
			return itemtype;
		}
		if (unresolvedType != null) {
			String shortName = unresolvedType.get(desc.getType());
			if (shortName != null) {
				throw new CadseException(
						Messages.error_cannot_find_type_with_shortName,
						shortName, desc.getType());
			}
		}
		throw new CadseException(Messages.error_cannot_find_type, desc
				.getType());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.IWorkspaceLogique#getItemType(fr.imag.adele.
	 * cadse.core.UUID)
	 */
	public ItemType getItemType(UUID id) {

		Item i = _items.get(id);
		if (i != null) {
			if (i instanceof ItemType) {
				return (ItemType) i;
			}
			if (i instanceof ItemUnresolved) {
				return null;
			}

			throw new CadseIllegalArgumentException(
					"Le type est attendu est metaItemType pour l'id " + id
							+ " alors qu'il est " + i.getType().getName()
							+ " : bad type for " + id);
		}
		return null;

	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.IWorkspaceLogique#getItemType(fr.imag.adele.
	 * cadse.core.UUID)
	 */
	public TypeDefinition getTypeDefinition(UUID id) {

		Item i = _items.get(id);
		if (i != null) {
			if (i instanceof TypeDefinition) {
				return (TypeDefinition) i;
			}
			if (i instanceof ItemUnresolved) {
				return null;
			}

			throw new CadseIllegalArgumentException(
					"Le type est attendu est metaItemType pour l'id " + id
							+ " alors qu'il est " + i.getType().getName()
							+ " : bad type for " + id);
		}
		return null;

	}
	
	
	
	@Override
	public ExtendedType getExtendedType(UUID id) {
		Item i = _items.get(id);
		if (i != null) {
			if (i instanceof ExtendedType) {
				return (ExtendedType) i;
			}
			if (i instanceof ItemUnresolved) {
				return null;
			}

			throw new CadseIllegalArgumentException(
					"Le type est attendu est metaItemType pour l'id " + id
							+ " alors qu'il est " + i.getType().getName()
							+ " : bad type for " + id);
		}
		return null;
	}

	/**
	 * Get all item types.
	 * 
	 * @return list of item types.
	 */
	public synchronized Collection<ItemType> getItemTypes() {
		return Collections.unmodifiableCollection(_itemTypes);
	}
	
	@Override
	public Collection<ExtendedType> getExtendedTypes() {
		return Collections.unmodifiableCollection(_extTypes);
	}

	/**
	 * Create an item type.
	 * 
	 * @param id
	 *            : id item type.
	 * @param superType
	 *            the super type
	 * @param intID
	 *            the int id
	 * @param shortName
	 *            the short name
	 * @param displayName
	 *            the display name
	 * @param hasContent
	 *            the has content
	 * @param isAbstract
	 *            the is abstract
	 * 
	 * @return a new item type.
	 * 
	 * @OCL context: WorkspaceDomain::createItemType(String id, boolean
	 *      isContent) : ItemType 1. pre: id <> null <br/>
	 *      2. pre: id <> '' <br/>
	 *      3. pre: self.itemTypes->forAll(it | not it.id = id)
	 *      CadseIllegalArgumentException : Id can not be null.
	 *      CadseIllegalArgumentException : Id can not be empty.
	 *      CadseIllegalArgumentException : Item type $id$ already exist.
	 */
	public ItemType createItemType(ItemType metaType, CadseRuntime cadseName,
			ItemType superType, int intID, UUID id, String shortName,
			String displayName, boolean hasContent, boolean isAbstract,
			IItemManager manager) {
		/*
		 * La convention est de charger d'abord le meta item type en premier
		 * puis tout de suite apres item-type on stoke mIT dans CadseCore.mIT et
		 * on set sont type
		 */
		if (!id.equals(CadseDomain.ITEMTYPE_ID)
				&& CadseCore.theItemType == null) {
			throw new CadseIllegalArgumentException(
					"Before, you must load Model.Workspace.Root");
		}
		if (id.equals(CadseDomain.ITEM_ID) && CadseCore.theItemType == null) {
			throw new CadseIllegalArgumentException(
					"Before, you must load Model.Workspace.Root");
		}
		if (!id.equals(CadseDomain.ITEMTYPE_ID)
				&& !id.equals(CadseDomain.ITEM_ID) && CadseCore.theItem == null) {
			throw new CadseIllegalArgumentException(
					"Before, you must load Model.Workspace.Root");
		}
		if (id.equals(CadseDomain.EXT_ITEM_ID)
				|| id.equals(CadseDomain.ITEM_ID)) {
			superType = null;
		} else {
			if (superType == null) {
				// si je n'ai pas de super type et que je ne suis pas le type
				// Item ou ExtItem.
				// on met d'office le type Item comme super type.
				superType = CadseCore.theItem;
			}
		}

		if (metaType == null) {
			metaType = CadseCore.theItemType;
		}

		// cretion du super type et enregistrement.
		ItemTypeImpl it = new ItemTypeImpl(metaType,
				(ItemTypeImpl) (superType), id, intID, hasContent, isAbstract,
				shortName, displayName);
		it.setParent(cadseName, CadseGCST.CADSE_lt_ITEM_TYPES);
		cadseName.addItemType(it);
		registerItemType(it);
		if (id.equals(CadseDomain.ITEM_ID)) {
			// initialisation de la constante theItemType
			CadseCore.theItem = it;
		} else if (id.equals(CadseDomain.ITEMTYPE_ID)) {
			CadseCore.theItemType = it;
			it.setType(it);
		}
		if (manager != null) {
			it.setItemManager(manager);
		}
		return it;
	}
	
	@Override
	public ExtendedType createExtendedType(ItemType metaType, CadseRuntime cadseName, 
			UUID uuid, String qualifiedName, String name) throws CadseException {
		if (metaType == null)
			throw new CadseException("Type is null !!!");
		ExtendedTypeImpl et = new ExtendedTypeImpl(uuid, metaType, qualifiedName, name);
		et.setParent(cadseName, CadseGCST.CADSE_lt_ITEM_TYPES);
		cadseName.addExtendedType(et);
		et.setCadse(cadseName);
		registerExtendedType(et);
		return et;
	}
	
	public void addInconmmingLink(Item src, Item dst, LinkType lt) {
		if (lt == null || src == null || dst == null) {
			return;
		}

		Link l = src.getOutgoingLink(lt, dst.getId());
		if (l != null) {
			dst.addIncomingLink(l, false);
		}
	}
	
	@Override
	public void addBinding(CadseRuntime cadse, ItemType it, ExtendedType et) {
		it.addExtendedType(et);
		et.addExendsItemType(it);
		cadse.addBinding(new Binding(it, et));
	}

	/**
	 * Register a type.
	 * 
	 * @param it
	 *            the itemtype which be register.
	 * 
	 * @OCL context: WSModelType 1. pre: self.itemTypes->forAll(it2 | not it2.id
	 *      = it.id) CadseIllegalArgumentException : Item type $id$ already
	 *      exist.
	 * @since 2.0
	 */
	protected void registerItemType(ItemType it) {
		final Item item = _items.get(it.getId());
		if (item != null && item.isResolved()) {
			throw new CadseIllegalArgumentException(
					"Invalid assignment, this item type {0}"
							+ " already exist.", it.getId());
		}
		_items.put(it.getId(), it);
		_itemTypes.add(it);

		resolveItem(it, null, item);
	}
	
	protected void registerExtendedType(ExtendedType it) {
		final Item item = _items.get(it.getId());
		if (item != null && item.isResolved()) {
			throw new CadseIllegalArgumentException(
					"Invalid assignment, this item type {0}"
							+ " already exist.", it.getId());
		}
		_items.put(it.getId(), it);
		_extTypes.add(it);

		resolveItem(it, null, item);
	}

	public void registerItem(Item item) {
		if (item.getId() != null) {
			_items.put(item.getId(), item);
		}
	}

	/**
	 * Delete.
	 */
	public void delete() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.IWorkspaceLogique#getItemTypeByName(java.lang
	 * .String)
	 */
	public synchronized ItemType getItemTypeByName(String shortName) {
		for (ItemType it : getItemTypes()) {
			if (it.getName().equals(shortName)) {
				return it;
			}
		}
		return null;
	}

	int stateLoadContentManager = 0; // 0 not loaded, 1 loading, 2 loaded

	public LogicalWorkspaceTransaction createTransaction() {
		final LogicalWorkspaceTransactionImpl logicalWorkspaceTransactionImpl = new LogicalWorkspaceTransactionImpl(this, ArraysUtil
				.clone(_workspaceLogiqueCopyListeners));
		logicalWorkspaceTransactionImpl.beginTransaction();
		return logicalWorkspaceTransactionImpl;
	}

	/**
	 * Test preconditions before creating a link.
	 * 
	 * @param lt
	 *            : type of link to create.
	 * @param destination
	 *            : destination of link<br/>
	 * <br/>
	 * @throws CadseException
	 * 
	 * @throws CadseIllegalArgumentException
	 *             : Link type <tt>lt</tt> is null.<br/>
	 *             CadseIllegalArgumentException: Link type <tt>lt</tt> is not
	 *             selected yet in workspace type. <br/>
	 *             IllegalArgumentException: Type of <tt>source</tt> has not the
	 *             type like as item type source defined in <tt>lt</tt>. <br/>
	 *             IllegalArgumentException: Type of <tt>destination</tt> has
	 *             not the type like as item type destination defined in
	 *             <tt>lt</tt>. <br/>
	 *             IllegalArgumentException: The maximum cardinatily is exceed.<br/>
	 *             IllegalArgumentException: Destination is null.<br/>
	 *             IllegalArgumentException: An object reference to itself.<br/>
	 *             IllegalArgumentException: Link to this destination has been
	 *             created.<br/>
	 * <br/>
	 */
	static public void preconditions_createLink(Link l, LinkType lt,
			Item source, Item destination) throws CadseException {
		// 7. one relation of containment by destination.
		if (lt.isPart()) {
			// if (destination.getPartParent(false) != null &&
			// destination.getPartParent(false) != source)
			// throw new IllegalArgumentException(
			// MessageFormat
			// .format(
			// Messages.error_cannot_create_part_link, //$NON-NLS-1$
			// source.getId(), destination.getId()));
			Item partParent = destination.getPartParent(lt);
			if (partParent != null && partParent != source) {
				throw new CadseException(MessageFormat.format(
						Messages.error_cannot_create_part_link, source.getId(),
						destination.getId()));
			}
		}
		if (lt.isInversePart()) {
			final Item partParent = source.getPartParent(lt.getInverse());
			if (partParent != null && partParent != destination) {
				throw new CadseException(MessageFormat.format(
						Messages.error_cannot_create_part_link, destination
								.getId(), source.getId()));
			}
		}
		constraints_LinkType(lt);
		constraints_SourceItem(l, lt, source);
		if (destination.getType() == null)
			throw new CadseException(
					"the type of the destinion of this link is null : " + l);
		constraints_DestItem(lt, source, destination);
		constraints_BetweenDestAndSource(l, lt, source, destination);
	}

	/**
	 * Called before creating a link. Verify contraints between source and
	 * destination.
	 * 
	 * @param lt
	 *            : type of link to create.
	 * @param destination
	 *            : destination of link<br/>
	 * <br/>
	 * @throws CadseException
	 * 
	 * @throws CadseIllegalArgumentException
	 *             : An object reference to itself.<br/>
	 *             CadseIllegalArgumentException: Link to this destination has
	 *             been created.<br/>
	 * <br/>
	 * 
	 * @NOTE: This method is used when create normal link. Source in this case
	 *        is object <tt>this</tt> <br/>
	 * @contraints: - 1. Source and destination must be two different objects
	 *              (it means an item cannot point to itself).<br/>
	 *              - 2. Beetwen source and destination there have no link the
	 *              same type have ready created.<br/>
	 * <br/>
	 * @OCL: <b>pre:</b> <tt>self</tt> != <tt>destination </tt> <i> // Source
	 *       and destination must be two different objects (it means an item
	 *       cannot point to itself).<br/>
	 *       <b>pre:</b> not <tt>self.to</tt>->exist(<tt>l</tt> |
	 *       <tt>l.dest</tt> = <tt>destination</tt> or <tt>l.destId</tt> =
	 *       <tt>destination.id</tt>) <i> // Beetwen source and destination
	 *       there have no link the same type have ready created.<br/>
	 * <br/>
	 */
	static public void constraints_BetweenDestAndSource(Link l_orig,
			LinkType lt, Item source, Item destination) throws CadseException {

		// 2. Beetwen source and destination there have no link the same type
		// have ready created.
		List<? extends Link> links = source.getOutgoingLinks();
		for (Link l : links) {
			if (l_orig == l) {
				continue;
			}

			if ((l.getLinkType().equals(lt) || l.getLinkType().getName()
					.equals(lt.getName()))
					&& (l.getDestinationId().equals(destination.getId()))) {
				throw new CadseException(Messages.error_link_already_exist,
						source.getId(), l.getDestinationId());
			}
		}
	}

	/**
	 * Called before creating a link. Verify contrainsts on link type
	 * <tt>lt</tt>. <br/>
	 * 
	 * NOTE: This method is used in both methods: Create normal link and Create
	 * non resolved link. In two case, it must always verify contrainsts on link
	 * type.
	 * 
	 * Constraints: - 1. Link type <tt>lt</tt> cannot be null. <br/>
	 * - 2. Link type <tt>lt</tt> must be selected in workspace type. <br/>
	 * 
	 * @param lt
	 *            : type of link to create. <br/>
	 * <br/>
	 * @throws CadseException
	 * 
	 * @throws CadseIllegalArgumentException
	 *             : Link type <tt>lt</tt> is null.<br/>
	 * 
	 * @OCL: <b>context:</b> Item::createLink(String id, LinkType lt, Item
	 *       destination) : Link </br> <b>pre:</b> <tt>lt</tt> <> null <i> //
	 *       Link type <tt>lt</tt> cannot be null. <br/>
	 *       <b>pre:</b> <tt>self.workspace.type.selectedLinkTypes</tt>
	 *       ->include(<tt>lt</tt>) <i> // Link type <tt>lt</tt> must be
	 *       selected in workspace type. <br/>
	 */
	static public void constraints_LinkType(LinkType lt) throws CadseException {
		// 1. Link type lt cannot be null.
		if (lt == null) {
			throw new CadseException(Messages.error_linktype_is_null);
		}
	}

	/**
	 * Called before creating a link. Verify contraints on item source.
	 * 
	 * @param lt
	 *            : type of link to create.
	 * @throws CadseException
	 * 
	 * @NOTE: This method is used in both methods: Create normal link and Create
	 *        non resolved link. In two case, it must always verify contrainsts
	 *        on item source. Note that in this case source is object
	 *        <tt>this</tt> <br/>
	 * @constraints: - 1. Type of <tt>source</tt> must be the same type as item
	 *               type source defined in link type<tt>lt</tt> <br/>
	 *               - 2. The number of outgoing links having type <tt>lt</tt>
	 *               of this item can not excerce the cardinality min and max
	 *               defined in link type <tt>lt</tt>. <br/>
	 *               - 3. The source isn't in read only state.
	 * @OCL: <b>pre:</b> <tt>self.type</tt> = <tt>lt.type.source</tt> <i> //
	 *       Type of <tt>source</tt> must be the same type as item type source
	 *       defined in link type<tt>lt</tt> <br/>
	 *       <b>pre:</b> let <tt>s</tt> = <tt>self.to</tt>->collect(<tt>l</tt> |
	 *       <tt>l.type</tt> = <tt>lt</tt>) <tt>s</tt>->size() >=
	 *       <tt>lt.min</tt> and if (<tt>lt.max</tt> !=-1) then <tt>s</tt>
	 *       ->size() <= <tt>lt.max</tt> <i> // The number of outgoing links
	 *       having type <tt>lt</tt> of this item can not excerce the
	 *       cardinality min and max defined in link type <tt>lt</tt>. <br/>
	 *       <b>pre:</b> self.isReadOnly = false
	 * @exception CadseIllegalArgumentException
	 *                : Type of <tt>source</tt> has not the type like as item
	 *                type source defined in <tt>lt</tt>. <br/>
	 *                CadseIllegalArgumentException: The maximum cardinatily is
	 *                exceed.<br/>
	 *                CadseIllegalArgumentException: The source is in read only
	 *                state.<br/>
	 */
	static public void constraints_SourceItem(Link l_orig, LinkType lt,
			Item source) throws CadseException {
		// 1. Type of source must be the same type as item type source defined
		// in link type lt
		if (lt.getSource() != source.getType()
				&& !source.isInstanceOf(lt.getSource())) {
			// !lt.getSource().getId().equals(WorkspaceDomain.ANY_ID) && !
			// (this.type.equals(lt.getSource())))
			throw new CadseException(Messages.error_itemtype_source_is_bad
					+ lt.getName());
		}

		// 2. source.isReadOnly = true
		if (!lt.isDerived() && source.isReadOnly()) {
			throw new CadseException("It is not possible to create link" //$NON-NLS-1$
					+ " because of its source {0} is in state read only.", //$NON-NLS-1$
					source.getId());
		}

		// 3. The number of outgoing links having type lt of this item can not
		// excerce the cardinality min and max defined in link type lt.
		int min = lt.getMin();
		int max = lt.getMax();
		int numLinks = 0;
		// count link to type lt;
		List<? extends Link> outgoingLinks = source.getOutgoingLinks();
		for (Link l : outgoingLinks) {
			if (l != l_orig && l.getLinkType() == lt) {
				numLinks++;
			}
		}

		if (numLinks + 1 > max && max != -1) {
			throw new CadseException(
					Messages.error_maximum_cardinality_is_exceed, numLinks + 1,
					max);
		}
		if (numLinks + 1 < min) {
			source.setValid(false); // TODO...
		}

	}

	/**
	 * Called before creating a link. Verify contraints on item destination.
	 * 
	 * @param lt
	 *            : type of link to create.
	 * @param destination
	 *            : destination of link<br/>
	 * <br/>
	 * 
	 * @throws CadseIllegalArgumentException
	 *             : Destination is null.<br/>
	 *             CadseIllegalArgumentException: Type of <tt>destination</tt>
	 *             has not the type like as item type destination defined in
	 *             <tt>lt</tt>. <br/>
	 * 
	 * @NOTE: This method is used just when Create normal link.
	 * @constraints: - 1. <tt>destination</tt> cannot be null. <br/>
	 *               - 2. Type of <tt>destination</tt> must be the same type as
	 *               item type destination defined in <tt>lt</tt> <br/>
	 * @OCL: <b>pre:</b> <tt>destination</tt> <> null <i>// <tt>destination</tt>
	 *       cannot be null. <br/>
	 *       <b>pre:</b> <tt>destination.type</tt> = <tt>lt.type.dest </tt> <i>
	 *       // Type of <tt>destination</tt> must be the same type as item type
	 *       destination defined in <tt>lt</tt> <br/>
	 */
	static public void constraints_DestItem(LinkType lt, Item source,
			Item destination) {
		// 1. destination cannot be null.
		if (destination == null) {
			throw new CadseIllegalArgumentException(
					Messages.error_cannot_create_link_destination_is_null,
					source.getId(), lt.getName());
		}

		// 2. Type of destination must be the same type as item type destination
		// defined in lt
		if (!destination.isInstanceOf(lt.getDestination())) {
			throw new CadseIllegalArgumentException(
					"Cannot create a link {0}({1}) to {2}({3}) of type {4} : " //$NON-NLS-1$
							+ "type of destination has not the type like as item type destination defined in lt {5} != {6}. ", //$NON-NLS-1$
					source.getId(), source.getQualifiedName(), destination
							.getId(), destination.getQualifiedName(), lt
							.getName(), lt.getDestination().getName(),
					destination.getType().getName());
		}

		// 3
		// if (lt.isComposition()) {
		// if (destination.containsComponent(source.getId())) {
		//				throw new CadseIllegalArgumentException("Cannot create a link {0} to {1} of type {2} : " //$NON-NLS-1$
		//						+ "The source ({0}) is contained in the composition list of the destination ({1}). " //$NON-NLS-1$
		//						+ "That will create a composition cycle : {0} -c-> {1} -c->* {0}.", //$NON-NLS-1$
		// source.getId(), destination.getId(), lt.getName());
		//
		// }
		// }
	}

	public void commit(LogicalWorkspaceTransaction workingLogiqueCopy,
			boolean check) throws CadseException {
		if (!CadseDomainImpl.isStarted()) {
			if (!CadseDomainImpl.isStopped())
				throw new CadseException("Cadse is stopped");
			throw new CadseException("Cadse not started");
		}
		if (((CadseDomainImpl) _wd).getIdeService() == null)
			throw new CadseException("IDE service not started");

		try {
			// _wd.beginOperation("WSModel.commit");
			_wd.beginRule(this);
			synchronized (this) {
				if (check) {
					// propagation des operations
					Collection<WLWCOperationImpl> operations;
					operations = new ArrayList<WLWCOperationImpl>(
							workingLogiqueCopy.getOperations());
					
					workingLogiqueCopy.checkAll();
					if (operations.size() != 0) {
						for (WLWCOperationImpl oper : operations) {
							OperationType opertype = oper.getOperationType();
							if (opertype == OperationTypeCst.ITEM_OPERATION) {
								ItemDelta d = (ItemDelta) oper;
								Key nkey = d.getNextKey();
								if (nkey != null) {
									final Item item = getItem(nkey);
									if (item != null && d.getBaseItem() != item) {
										throw new CadseException(
												Messages.error_invalid_assignement_key_allready_exists,
												nkey);
									}
								}
							}
							if (opertype == OperationTypeCst.SET_ATTRIBUTE_OPERATION) {
								opertype = oper.getParentType();
								if (opertype == OperationTypeCst.ITEM_OPERATION) {
									ItemDelta item = (ItemDelta) oper
											.getParentOperDelta();
									if (item.getBaseItem() != null
											&& item.getBaseItem().isRuntime()) {
										throw new CadseException(
												"Cannot set attribute on a static item");
									}
									continue;
								}
							}
							if (opertype == OperationTypeCst.DELETE_OPERATION) {
								opertype = oper.getParentType();
								if (opertype == OperationTypeCst.ITEM_OPERATION) {
									ItemDelta item = (ItemDelta) oper
											.getParentOperDelta();
									if (item.getBaseItem().isRuntime()) {
										throw new CadseException(
												"Cannot delete a static item");
									}
									continue;
								}
								if (opertype == OperationTypeCst.LINK_OPERATION) {
									LinkDeltaImpl linkOperation = (LinkDeltaImpl) oper
											.getParentOperDelta();
									Link linkbase = linkOperation.getBaseLink();

									if (linkbase != null && linkbase.isRuntime()) {
										throw new CadseException(
												"Cannot delete a link of a static item");
									}
									continue;
								}
								continue;
							}
							if (opertype == OperationTypeCst.CREATE_OPERATION) {
								opertype = oper.getParentType();
								if (opertype == OperationTypeCst.ITEM_OPERATION) {

									ItemDelta item = (ItemDelta) oper
											.getParentOperDelta();
									preconditions_createItem(item, item
											.getType(), item.getPartParent(),
											item.getPartParentLinkType());
									continue;
								}
								if (opertype == OperationTypeCst.LINK_OPERATION) {

									LinkDeltaImpl linkOperation = (LinkDeltaImpl) oper
											.getParentOperDelta();
									ItemDeltaImpl source = linkOperation
											.getSource();
									if (source.isDeleted()) {
										// TODO throw an exception
										linkOperation.delete();
										continue;
									}
									if (!source.isAdded() && !source.isLoaded()) {
										if (source.getBaseItem() != null
												&& source.getBaseItem()
														.isReadOnly()) {
											throw new CadseException(
													"Source is readonly");
										}
									}
									LinkType link_lt = linkOperation
											.getLinkType();

									Item destination = linkOperation
											.getDestination();
									if (destination == null) {
										throw new CadseException(
												"Destination is null!!!");
									}
									preconditions_createLink(linkOperation,
											link_lt, source, destination);
									continue;
								}
								continue;
							}
						}
					}
				}
				workingLogiqueCopy.setState(WSModelState.COPY_PRE_LOAD);
				new TransactionItemsProcess(this,
						(LogicalWorkspaceTransactionImpl) workingLogiqueCopy)
						.processCommit(new ArrayList<ItemDelta>(
								workingLogiqueCopy.getItemOperations()),
								((CadseDomainImpl) _wd).getEventsManager());
				workingLogiqueCopy.setState(WSModelState.COPY_READ_ONLY);

				workingLogiqueCopy.notifyCommitTransaction();
				return;
			}
		} catch (CadseException e) {
			workingLogiqueCopy.notifyAbortTransaction();
			e.printStackTrace();
			throw new CadseException(e.getMessage(), e);
		} catch (RuntimeException e) {
			workingLogiqueCopy.notifyAbortTransaction();
			e.printStackTrace();
			throw e;
		} finally {
			_wd.endRule(this);
		}
	}

	// public void load(LogicalWorkspaceTransactionImpl workingLogiqueCopy) {
	// try {
	// _wd.beginOperation("WSModel.commit");
	// Collection<ItemDelta> operations =
	// workingLogiqueCopy.getItemOperations();
	// new TransactionItemsProcess(this,
	// workingLogiqueCopy).processCommit(operations, _wd);
	// } catch (CadseException e) {
	// e.printStackTrace();
	// } finally {
	// _wd.endOperation();
	// }
	// }

	public void dispose() {
	}

	public void addListener(WorkspaceListener l, int eventFilter) {
		_listeners = ArraysUtil.add(WorkspaceListener.class, _listeners, l);
		_filter = ArraysUtil.add(_filter, eventFilter);
	}

	public void addListener(WorkspaceListener l, EventFilter eventFilter) {
		l.setFilter(eventFilter);
		addListener(l, -1);
	}

	public List<WorkspaceListener> filter(int filters,
			ImmutableWorkspaceDelta workspaceDelta) {
		if (_listeners == null) {
			return null;
		}
		ArrayList<WorkspaceListener> ret = new ArrayList<WorkspaceListener>();
		for (int i = 0; i < _listeners.length; i++) {
			int f = _filter[i];
			WorkspaceListener l = _listeners[i];
			if (f == -1 && l.getFilter().accept(filters, workspaceDelta)) {
				ret.add(l);
			} else if (f != -1 && (f & filters) != 0) {
				ret.add(l);
			}
		}
		if (ret.size() == 0) {
			return null;
		}
		return ret;
	}

	public void removeListener(WorkspaceListener l) {
		int index = ArraysUtil.indexOf(_listeners, l);
		if (index == -1) {
			return;
		}
		_listeners = ArraysUtil.remove(WorkspaceListener.class, _listeners,
				index);
		_filter = ArraysUtil.remove(_filter, index);
	}

	public void clear() throws CadseException {
		ArrayList<Item> itemsToDelete = new ArrayList<Item>(_items.values());
		ArrayList<CadseException> ex = new ArrayList<CadseException>();
		for (Item dItem : itemsToDelete) {
			UUID id = dItem.getId();
			Item realItem = getItem(id);
			if (realItem != null) {
				try {
					realItem.delete(true);
				} catch (CadseException e) {
					ex.add(e);
					realItem = getItem(id);
					if (realItem != null) {
						removeItem(realItem);
					}
				}
			}
		}
		CadseCore.theItemType = null;
		// loadMetaModel();
		if (ex.size() != 0) {
			throw new CadseException("Error on clear", ex
					.toArray(new CadseException[ex.size()]));
		}
	}

	public void loadCadseModel(String qualifiedCadseModelName) {
		try {
			CadseRuntime cr = getCadseRuntime(qualifiedCadseModelName);
			if (cr != null) {
				((CadseDomainImpl) _wd).getInitModelService().executeCadses(cr);
			}
		} catch (ErrorWhenLoadedModel e) {
			throw new Error("Error when loaded model "
					+ qualifiedCadseModelName, e);
		}

	}

	public void addLogicalWorkspaceTransactionListener(
			LogicalWorkspaceTransactionListener l) {
		_workspaceLogiqueCopyListeners = ArraysUtil.add(
				LogicalWorkspaceTransactionListener.class,
				_workspaceLogiqueCopyListeners, l);
	}

	public void removeLogicalWorkspaceTransactionListener(
			LogicalWorkspaceTransactionListener l) {
		_workspaceLogiqueCopyListeners = ArraysUtil.remove(
				LogicalWorkspaceTransactionListener.class,
				_workspaceLogiqueCopyListeners, l);
	}

	public LogicalWorkspaceTransactionListener[] getLogicalWorkspaceTransactionListener() {
		return _workspaceLogiqueCopyListeners;
	}

	public CadseRuntime[] getCadseRuntime() {
		return this._cadses;
	}

	public CadseRuntime getCadseRuntime(String qualifiedCadseModelName) {
		if (this._cadses != null) {
			for (CadseRuntime cr : _cadses) {
				if (cr.getName().equals(qualifiedCadseModelName)) {
					return cr;
				}
			}
		}
		return null;
	}

	public <T> T getAttribute(Item source, IAttributeType<T> type,
			boolean ownerOnly) {
		LinkedList<Iterator<Item>> stack = null;
		if (source.isDelegatedValue(type)) {
			if (ownerOnly)
				return (T) (type instanceof BooleanAttributeType ? Boolean.FALSE: null);
			return getAttribute(source.getGroup(), type, ownerOnly);		
		}
		Item s = source;
		while (true) {
			Iterator<Item> iterpro = null;
			if (s != null) {
				T ret = null;
				// TODO use if (type.isNatif())
				ret = s.internalGetOwnerAttribute(type);
				if (ret != null && ret != IAttributeType.VALUE_NOT_DEFINED && ret != IAttributeType.VALUE_NOT_INIT) {
					return ret;
				}
				if (ownerOnly) {
					return (T) (type instanceof BooleanAttributeType ? Boolean.FALSE: null);
				}
				iterpro = s.propagateValue(type);
			}

			if (stack == null) {
				stack = new LinkedList<Iterator<Item>>();
			}

			if (iterpro == null) {
				if (stack.isEmpty()) {
					return (T) (type instanceof BooleanAttributeType ? Boolean.FALSE: null);
				}
				iterpro = stack.pollLast();
			}
			s = iterpro.hasNext() ? iterpro.next() : null;
			if (iterpro.hasNext()) {
				stack.add(iterpro);
			}
		}
	}

	public IAttributeType<?> createUnresolvedAttributeType(
			TypeDefinition sourceType, ItemType attrType, UUID attrID,
			String attName) {
		IAttributeType<?> att = (IAttributeType<?>) getItem(attrID);
		if (att != null) {
			return att;
		}
		att = new AttributeTypeUnresolved(attrID, attName, attrType,
				Item.UNRESOLVED);
		sourceType.addAttributeType(att);
		return att;
	}

	/**
	 * Creates the unresolved link type.
	 * 
	 * @param type
	 *            the type
	 * @param type2
	 *            the type2
	 * @param type3
	 *            the type3
	 * 
	 * @return the link type
	 */
	synchronized public LinkType createUnresolvedLinkType(UUID id,
			String linktypeName, TypeDefinition sourcetype,
			TypeDefinition desttype) {
		try {
			// _wd.beginOperation("Create unresolved link type");
			Item foundItem = getItem(id);
			if (foundItem != null) {
				return (LinkType) foundItem;
			}
			LinkTypeImpl linkTypeImpl = new LinkTypeImpl(id, 0, sourcetype,
					linktypeName, 0, -1, null, desttype);
			linkTypeImpl.setFlag(Item.UNRESOLVED, true);
			((TypeDefinition.Internal) sourcetype)
					.addOutgoingLinkType(linkTypeImpl);
			if (getItem(id) == null) {
				throw new IllegalStateException(
						"Cannot found the inresolved link");
			}
			return linkTypeImpl;
		} finally {
			// /_wd.endOperation();
		}
	}

	// // Properties unresolvedObject = null;
	//
	// protected UUID getUnresolvedId(String key) {
	// return ((CadseDomainImpl) _wd).getUnresolvedId(key);
	// // UUID randomUUID = UUID.randomUUID();
	// // System.out.println("*** create unresolved object " + key + ":" +
	// // randomUUID);
	// // return randomUUID;
	// }

	public CadseRuntime findCadse(UUID id) {
		for (CadseRuntime cr : _cadses) {
			if (cr.getId().equals(id)) {
				return cr;
			}
		}
		return null;
	}

	public LinkType findLinkType(ItemType sourceType, ItemType destType,
			UUID ltId, String ltName, boolean createUnresolvedObject) {
		if (sourceType == null) {
			return null;
		}

		LinkType _lt_object = null;
		if (ltId == null) {
			return sourceType.getOutgoingLinkType(ltName);
		}
		_lt_object = sourceType.getOutgoingLinkType(ltId);
		if (createUnresolvedObject && _lt_object == null) {
			return createUnresolvedLinkType(ltId, ltName, sourceType, destType);
		}
		return _lt_object;
	}

	/**
	 * Create a unresolved item Type
	 * 
	 * @param id
	 * @return
	 * @throws CadseException
	 */
	public ItemType createUnresolvedItemType(UUID cadseid, UUID id, String sn,
			String un) throws CadseException {

		CadseRuntime cr = findCadse(cadseid);
		if (cr != null && cr.isExecuted())
			return null;
		if (cr == null)
			cr = getCadseRuntimeForUnresolvedItemType(cadseid);
		ItemTypeImpl itemTypeImpl = (ItemTypeImpl) createItemType(null, cr,
				null, 0, id, sn, "Unresolved Item type" + id, false, true,
				_unresolveManager);
		itemTypeImpl.setFlag(Item.UNRESOLVED, true);
		itemTypeImpl._qualifiedName = un;
		itemTypeImpl.setFlag(Item.IS_HIDDEN, true);
		itemTypeImpl.setFlag(Item.IS_RUNTIME, true);
		itemTypeImpl.setFlag(Item.READONLY, true);
		return itemTypeImpl;
	}

	private CadseRuntime getCadseRuntimeForUnresolvedItemType(UUID cadseid) {
		CadseRuntime cr = (CadseRuntime) getItem(cadseid);
		if (cr != null)
			return cr;
		cr = createCadseRuntime("#crUnresolvedItemType", cadseid, cadseid);
		cr.setIsRuntime(true);
		cr.setDisplayName("Cadse for id " + cadseid);
		cr.setFlag(Item.UNRESOLVED, true);
		return cr;
	}

	@Override
	public ContextVariable getContext() {
		return new ContextVariableImpl();
	}

	@Override
	public NewContext[] getNewContextFrom(FilterContext context) {
		ArrayList<NewContext> ret = new ArrayList<NewContext>();
		if (_cadses != null)
			for (CadseRuntime cr : _cadses) {
				DefineNewContext[] dfs = cr.getDefineNewContexts();
				if (dfs == null)
					continue;
				for (DefineNewContext df : dfs) {
					try {
						df.computeNew(context, ret);
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		return (NewContext[]) ret.toArray(new NewContext[ret.size()]);
	}

	public void addKey(INamedUUID item, Key key) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void changeKey(INamedUUID item, Key oldKey, Key newKey) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Key[] getChildrenKey(Key aThis) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public CadseRuntime createCadseRuntime(String name, UUID runtimeId,
			UUID definitionId) {
		CadseRuntime cadseRuntime = new CadseRuntimeImpl(name, runtimeId,
				definitionId);
		this._cadses = ArraysUtil.add(CadseRuntime.class, this._cadses,
				cadseRuntime);
		registerItem(cadseRuntime);
		return cadseRuntime;
	}

	@Override
	public <T> T adapt(Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void waitStart() {
	}

	
}
