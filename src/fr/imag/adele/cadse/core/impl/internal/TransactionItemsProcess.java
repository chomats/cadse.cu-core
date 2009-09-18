/**
 *
 */
package fr.imag.adele.cadse.core.impl.internal;

import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseRootCST;
import fr.imag.adele.cadse.core.ChangeID;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.ContentItem;
import fr.imag.adele.cadse.core.IItemFactory;
import fr.imag.adele.cadse.core.IItemManager;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemDescriptionRef;
import fr.imag.adele.cadse.core.ItemState;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.Messages;
import fr.imag.adele.cadse.core.ProjectAssociation;
import fr.imag.adele.cadse.core.WSEvent;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.delta.ImmutableWorkspaceDelta;
import fr.imag.adele.cadse.core.delta.ItemDelta;
import fr.imag.adele.cadse.core.delta.LinkDelta;
import fr.imag.adele.cadse.core.delta.MappingOperation;
import fr.imag.adele.cadse.core.delta.OrderOperation;
import fr.imag.adele.cadse.core.delta.SetAttributeOperation;
import fr.imag.adele.cadse.core.delta.WLWCOperationImpl;
import fr.imag.adele.cadse.core.impl.CadseCore;
import fr.imag.adele.cadse.core.impl.ItemFactory;
import fr.imag.adele.cadse.core.impl.ReflectLink;
import fr.imag.adele.cadse.core.internal.IWorkingLoadingItems;
import fr.imag.adele.cadse.core.internal.IWorkspaceNotifier;
import fr.imag.adele.cadse.core.key.AbstractSpaceKey;
import fr.imag.adele.cadse.core.key.ISpaceKey;
import fr.imag.adele.cadse.core.util.ArraysUtil;
import fr.imag.adele.cadse.core.util.Assert;
import fr.imag.adele.cadse.core.util.ElementsOrder;
import fr.imag.adele.cadse.core.util.IErrorCollector;

public final class TransactionItemsProcess implements IWorkingLoadingItems, IErrorCollector {

	static class RegisterWorkspanceNotififier implements IWorkspaceNotifier {
		private EventsManagerImpl		eventsMgr;
		private ImmutableWorkspaceDelta	events;

		public RegisterWorkspanceNotififier(EventsManagerImpl eventMgr) {
			this.eventsMgr = eventMgr;
			this.events = eventMgr.createImmutableEvents();
		}

		public void notifieChangeEvent(ChangeID id, Object... values) {
			events.addEvent(new WSEvent(System.currentTimeMillis(), id, values));
		}

		public List<WSEvent> getEvents() {
			return events.getEvents();
		}

		public void sendAllTo() {
			eventsMgr.sendEvents(events);
		}
	}

	final LogicalWorkspaceImpl		wl;
	LogicalWorkspaceTransactionImpl	copy;

	Map<CompactUUID, Item>			visited			= new HashMap<CompactUUID, Item>();

	final boolean					update;
	final boolean					forceToSave;
	Map<ItemDelta, String[]>		errors			= null;

	HashMap<ItemDelta, Item>		itemsAdded		= new HashMap<ItemDelta, Item>();
	HashMap<ItemDelta, Item>		itemsDeleted	= new HashMap<ItemDelta, Item>();
	HashMap<ItemDelta, Item>		itemsLoaded		= new HashMap<ItemDelta, Item>();

	Collection<ProjectAssociation>	projectAssociationSet;
	RegisterWorkspanceNotififier	notifie;

	public TransactionItemsProcess(LogicalWorkspaceImpl wl, LogicalWorkspaceTransactionImpl copy) {
		super();
		this.wl = wl;

		this.update = copy.isUpdate();
		this.forceToSave = copy.isForceToSave();
		this.copy = copy;
		this.projectAssociationSet = copy.getProjectAssociationSet();
	}

	public void setProjectAssociationSet(Collection<ProjectAssociation> projectAssociationSet) {
		this.projectAssociationSet = projectAssociationSet;
	}

	public Collection<ProjectAssociation> getProjectAssociationSet() {
		return projectAssociationSet;
	}

	public Item loadItem(ItemDescriptionRef ref) throws CadseException {
		Item dest = getItem(ref.getId());
		if (dest == null) {
			dest = wl.loadItem(ref);
		}
		return dest;
	}

	public void addError(ItemDelta id, String msg) {
		if (id == null) {
			return;
		}
		Assert.isNotNull(msg);
		if (errors == null) {
			errors = new HashMap<ItemDelta, String[]>();
		}
		String[] error = errors.get(id);
		error = ArraysUtil.add(String.class, error, msg);
		Assert.isNotNull(error);
		errors.put(id, error);
	}

	public void addError(Item item, String msg) {
		if (item instanceof ItemDelta) {
			addError((ItemDelta) item, msg);
		} else {
			addError(item.getId(), msg);
		}
	}

	public void addError(CompactUUID id, String msg) {
		addError(copy.getItem(id), msg);
	}

	public void showErros() {
		if (errors == null) {
			return;
		}
		StringBuilder sb = new StringBuilder(" \n");
		for (ItemDelta id : errors.keySet()) {
			Item item = visited.get(id.getId());
			sb.append("\t");
			if (item != null) {
				sb.append(item.getName()).append(" - ");
			}
			sb.append(id.getId()).append(":\n");
			String[] error = errors.get(id);
			Assert.isNotNull(error);
			for (String msg : error) {
				sb.append("\t\t- ").append(msg).append("\n");
			}
		}
		wl.getCadseDomain().log(null, sb.toString(), null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.IWorkingLoadingItems#getItem(fr.imag.adele.cadse.core.CompactUUID)
	 */
	public Item getItem(CompactUUID id) {
		Item ret = visited.get(id);
		if (ret != null) {
			return ret;
		}
		return wl.getItem(id);
	}

	public LogicalWorkspace getLogicalWorkspace() {
		return wl;
	}

	/**
	 * Load one Item ( patron visitor)
	 * 
	 * @param operations
	 * @param realnotifie
	 * @throws CadseException
	 * @throws CadseException
	 */
	void processCommit(Collection<ItemDelta> operations, EventsManagerImpl realnotifie) throws CadseException,
			CadseException {

		notifie = new RegisterWorkspanceNotififier(realnotifie);
		for (ItemDelta item : operations) {
			if (visited.containsKey(item.getId())) {
				continue;
			}
			loadOneItem(item);
		}
		// traite les item d��truits...
		for (ItemDelta item : itemsDeleted.keySet()) {
			loadDeletedItem(item);
		}
		// traite les item charg��s...
		for (ItemDelta loadedOperation : itemsLoaded.keySet()) {
			callLoadItem(notifie, loadedOperation);
		}

		// traite les item ajout��s
		for (ItemDelta addOperation : itemsAdded.keySet()) {
			callLoadCommitItem(notifie, addOperation);
		}

		for (ItemDelta item : operations) {
			if (!item.isModified()) {
				continue;
			}

			Item goodItem = null;
			if (item.isDeleted()) {
				goodItem = itemsDeleted.get(item);
			} else {
				goodItem = wl._items.get(item.getId());
				if (goodItem == null || !goodItem.isResolved()) {
					// error le traitement pr��c��dent n'a pas march��
					continue;
				}
				/*
				 * load parent
				 */
				if (!item.isAdded() && !item.isLoaded()) {
					wl.removeKeys(goodItem);

				}
				goodItem.setState(ItemState.MODIFING);
				for (SetAttributeOperation att : item.getSetAttributeOperation()) {
					if (att.getAttributeName().equals(Accessor.ATTR_PARENT_ITEM)) {
						continue;
					}
					if (att.isLoaded() || !att.isModified()) {
						continue;
					}
					try {
						Object currentValue = att.getCurrentValue();
						IAttributeType<?> attributeDefinition = att.getAttributeDefinition();
						if (attributeDefinition == CadseRootCST.ITEM_TYPE_at_NAME_ && !item.isAdded()) {
							ISpaceKey key = item.getKey();
							if (key != null) {
								key.setName((String) currentValue);
							}
						}
						if (attributeDefinition != null) {
							currentValue = attributeDefinition.convertTo(currentValue);
						}
						if (goodItem.commitSetAttribute(attributeDefinition, att.getAttributeName(), currentValue)) {
							notifie.notifieChangeEvent(ChangeID.SET_ATTRIBUTE, goodItem, attributeDefinition, att
									.getOldValue(), currentValue);
						}

					} catch (Throwable e) {
						wl.getCadseDomain().log("wl", "error in commit set attribute " + att, e);
						addError(item, "Add setAttribute " + att + ":" + e.getMessage());
					}

				}
			}
			for (LinkDelta link : item.getOutgoingLinkOperations()) {
				if (!link.isModified()) {
					continue;
				}
				if (link.isDeleted()) {
					Link l = goodItem.getOutgoingLink(link.getLinkType(), link.getDestinationId());
					if (l != null) {
						if (!l.getLinkType().isNatif() && item.isDeleted()) {
							if (link.getDestination().isDeleted()) {
								// nothing
							} else {
								l.getDestination().removeIncomingLink(l, false);
							}
						} else {
							l.commitDelete();
						}
						notifie.notifieChangeEvent(ChangeID.DELETE_OUTGOING_LINK, l);
						if (!goodItem.isOrphan() && goodItem.isResolved() && !link.getDestination().isDeleted()) {
							notifie.notifieChangeEvent(ChangeID.UNRESOLVE_INCOMING_LINK, l.getDestination(), l);
						}
					} else {
						addError(item, "no find " + link);
					}

					continue;
				}
				if (item.isDeleted()) {
					// added link is ignored and the set attribute on a
					// link...
					continue;
				}

				if (link.isLoaded()) {
					continue;
				}

				// added link or modified link
				Item destination = getItem(link.getDestinationId());
				if (destination == null) {
					destination = loadItem(link.getDestinationDescription());
				}
				if (!destination.isResolved()) {
					System.out.println("" + link + " has unresoled destination");
				}
				Link l = null;
				if (link.isAdded()) {
					l = goodItem.commitLoadCreateLink(link.getLinkType(), destination);
					if (l == null) {
						continue;
					}
					if (l instanceof ReflectLink && destination.isResolved()) {
						destination.addIncomingLink(l, false);
					}
					notifie.notifieChangeEvent(ChangeID.CREATE_OUTGOING_LINK, l);
					if (l.isLinkResolved()) {
						notifie.notifieChangeEvent(ChangeID.RESOLVE_INCOMING_LINK, l.getResolvedDestination(), l);
					}
					// l.getLinkType().getManager().createdLink(l);
				} else {
					l = goodItem.getOutgoingLink(link.getLinkType(), destination.getId());
					if (l == null) {
						continue;
					}
				}
				for (SetAttributeOperation att : link.getSetAttributeOperation()) {

					// if (compse != null)
					// compse.actionAddAttribute(linkDescription, key,
					// value)(goodItem.getId(), att.getKey(),
					// goodItem.getAttribute(att.getKey()));

					if (l.commitSetAttribute(att.getAttributeDefinition(), att.getAttributeName(), att
							.getCurrentValue())) {
						notifie.notifieChangeEvent(ChangeID.SET_LINK_ATTRIBUTE, l, att.getAttributeName(), att
								.getOldValue(), att.getCurrentValue());
					}
				}
			}

			if (item.isDeleted()) {
				continue;
			}

			// for only added or modified item
			for (OrderOperation o : item.getOrdersOperation()) {
				Link l1 = goodItem.getOutgoingLink(o.getFrom().getLinkType(), o.getFrom().getDestinationId());
				Link l2 = goodItem.getOutgoingLink(o.getTo().getLinkType(), o.getTo().getDestinationId());
				if (goodItem.commitMove(o.getKind(), l1, l2)) {
					;
				}
				notifie.notifieChangeEvent(ChangeID.ORDER_OUTGOING_LINK, goodItem, l1, l2, o.getKind());
			}
			if (item.isAdded()) {
				goodItem.computeAttributes();
			}
			if (forceToSave) {
				notifie.notifieChangeEvent(ChangeID.FORCE_SAVE, goodItem);
			}
			goodItem.setState(ItemState.CREATED);
			goodItem.setKey(AbstractSpaceKey.NO_INIT_KEY);
			wl.addKeys(goodItem);
		}
		callCreatedItem();
		loadProjectAssociation();
		commitMappingOperation();

		if (this.itemsLoaded.size() != 0) {
			notifie.notifieChangeEvent(ChangeID.LOAD_ITEMS, this, new ArrayList<Item>(this.itemsLoaded.values()));
		}
		showErros();
		notifie.sendAllTo();
	}

	private void commitMappingOperation() {
		ElementsOrder<MappingOperation> mappingoder = copy.getMappingOrder();
		for (MappingOperation mo : mappingoder.elements) {
			try {
				ItemDelta parent = mo.getParent();
				Item goodItem = wl.getItem(parent.getId());
				mo.commit(wl, goodItem);
			} catch (Throwable e) {
				wl.getCadseDomain().log("wl", "error in mapping " + mo, e);
				e.printStackTrace();
			}
		}
	}

	protected void loadItem(IWorkingLoadingItems wl, Item goodItem, ItemDelta desc, IWorkspaceNotifier notifie,
			IErrorCollector errorCollector) throws CadseException {
		goodItem.loadItem(wl, desc, errorCollector);
		goodItem.finishLoad();
	}

	protected void loadCommitItem(Item goodItem, ItemDelta desc, IWorkspaceNotifier notifie) throws CadseException {
		if (desc.getPartParent() != null) {
			Item parentOperation = desc.getPartParent();
			Item goodParent = goodItem.getLogicalWorkspace().getItem(parentOperation.getId());
			if (goodParent != null) {
				goodItem.setParent(goodParent, desc.getPartParentLinkType());
			}
		}
	}

	protected void update(IWorkingLoadingItems wl, ItemDelta desc, Item i, IWorkspaceNotifier notifie) {
		i.update(wl, desc, notifie);
	}

	private void callLoadCommitItem(RegisterWorkspanceNotififier notifie, ItemDelta addOperation) throws CadseException {
		try {
			Item goodItem = itemsAdded.get(addOperation);
			ItemType it = goodItem.getType();
			IItemFactory factory = it.getItemFactory();
			if (factory == null) {
				factory = ItemFactory.SINGLETON;
			}
			loadCommitItem(goodItem, addOperation, notifie);
		} catch (Throwable e) {
			e.printStackTrace();
			addError(addOperation, "Cannot load commit item : exception : " + e.getMessage());
		}
	}

	private void callLoadItem(RegisterWorkspanceNotififier notifie, ItemDelta loadedOperation) throws CadseException {
		try {
			Item goodItem = itemsLoaded.get(loadedOperation);
			ItemType it = goodItem.getType();
			IItemFactory factory = it.getItemFactory();
			if (factory == null) {
				factory = ItemFactory.SINGLETON;
			}
			loadItem(this, goodItem, loadedOperation, notifie, this);
		} catch (Throwable e) {
			e.printStackTrace();
			addError(loadedOperation, "Cannot finish load item : exception : " + e.getMessage());
		}
	}

	/**
	 * - remove in workspace table - send event DELETE_ITEM - delete content if
	 * option is set - call deleteItem of the ItemManager
	 * 
	 * Les liens incoming sont supposer etre supprimer dans la copy il suffit
	 * d'executer la copy
	 * 
	 * @param item
	 * @throws CadseException
	 */
	private void loadDeletedItem(ItemDelta item) throws CadseException {
		Item deletedItem = itemsDeleted.get(item);
		IItemManager im = deletedItem.getType().getItemManager();
		//
		// remove in workspace table
		wl.removeItem(deletedItem);
		// Les liens incoming sont supposer etre supprimer dans la copy
		// il suffit d'executer la copy
		notifie.notifieChangeEvent(ChangeID.DELETE_ITEM, deletedItem);
		if (item.getDeleteOperation().isDeleteContent() && deletedItem.getContentItem() != null) {
			try {
				deletedItem.getContentItem().delete();
			} catch (CadseException e) {
				wl.getCadseDomain().log(item, 0, "error", //$NON-NLS-1$
						Messages.error_cannot_delete_content, e);
			}
		}
		im.deletedItem(deletedItem);
		deletedItem.setState(ItemState.DELETED);
	}

	private Item loadOneItem(ItemDelta item) throws CadseException {
		try {
			if (visited.containsKey(item.getId())) {
				return visited.get(item.getId()); // allready visited ...
			}

			ItemType it = findOrCreateItemTypeFromItemOperation(item);
			if (it == null) {
				return null; // Cannot find type ...
			}

			// find the item factory
			IItemFactory factory = it.getItemFactory();
			if (factory == null) {
				factory = ItemFactory.SINGLETON;
			}

			if (item.isAdded()) {
				Item goodItem = null;
				goodItem = factory.newForCommitItem(wl, it, item);
				goodItem.setState(ItemState.NOT_IN_WORKSPACE);
				itemsAdded.put(item, goodItem);
				notifie.notifieChangeEvent(ChangeID.CREATE_ITEM, goodItem);
				wl.addId(goodItem, notifie, this);
				// add created item in visited hashset
				visited.put(goodItem.getId(), goodItem);
				return goodItem;
			} else if (item.isDeleted()) {
				Item deletedItem = getItem(item.getId());
				if (deletedItem != null && deletedItem.isResolved()) {
					for (Link l : deletedItem.getIncomingLinks()) {
						final ItemDelta sourceItem = this.copy.getItem(l.getSourceId(), true);
						if (sourceItem.isDeleted()) {
							continue;
						}
						LinkDelta link = sourceItem.getOutgoingLinkOperation(l);
						if (link != null && link.isDeleted()) {
							continue;
						}

						notifie.notifieChangeEvent(ChangeID.UNRESOLVE_INCOMING_LINK, deletedItem, l);
					}

					itemsDeleted.put(item, deletedItem);
					// add deleted item in visited hashset
					visited.put(deletedItem.getId(), deletedItem);
				}
				return null;
			} else if (item.isLoaded() && item.isFinishLoad()) {
				// est-ce que l'item existe déjà ...
				Item goodItem = wl.getItem(item.getId());

				if (goodItem != null) {
					if (!goodItem.getType().equals(it)) {
						addError(item, MessageFormat.format(Messages.error_bad_type, goodItem.getType().getName(), it
								.getName(), item.getName()));
						return null;
					}
					// if (!update && goodItem.isResolved()) {
					// addError(item,
					// MessageFormat.format(Messages.error_allready_exist,
					// //$NON-NLS-1$
					// goodItem.getType().getShortName(),
					// item.getShortName()));
					// return null;
					// }
				}

				if (goodItem != null && goodItem.isStatic()) {
					visited.put(goodItem.getId(), goodItem);
					return goodItem;
				}
				if (goodItem != null && goodItem.isResolved()) {
					update(this, item, goodItem, notifie);
					visited.put(goodItem.getId(), goodItem);
					return goodItem;
				}
				if (CadseRootCST.META_ITEM_TYPE.isSuperTypeOf(it)) {
					// load du super type ...
					findOrCreateSuperItemTypeFromItemOperation(item);

				}
				goodItem = factory.newForCommitItem(wl, it, item);
				if (CadseRootCST.META_ITEM_TYPE.isSuperTypeOf(it)) {
					assert goodItem instanceof ItemTypeImpl;
					// assert si le type a s un super type
					// il charg�� et connecter ici
				}
				itemsLoaded.put(item, goodItem);
				// add loaded item in visited hashset
				visited.put(goodItem.getId(), goodItem);
				wl.addId(goodItem, notifie, this);
				return goodItem;
			}
		} catch (Throwable e) {
			e.printStackTrace();
			addError(item, e.getMessage());
		}
		return null;
	}

	private ItemType findOrCreateItemTypeFromItemOperation(ItemDelta item) throws CadseException {
		// do not create a unresolved item type
		ItemType it = item.getType(false);
		if (it == null) {
			it = wl.getItemType(item.getItemTypeId());
		}

		if (it == null) {
			// recupere l'id du type de l'item
			CompactUUID itemTypeID = item.getItemTypeId();
			// on l'a deja creer pr��c��dement ?
			Item anItem = visited.get(itemTypeID);
			if (anItem == null) {
				// non
				// est-ce qu' nous l'avons dans les items �� charg��/creer
				ItemDelta itemTypeOperation = copy.getItem(itemTypeID);
				if (itemTypeOperation != null && !itemTypeOperation.isDeleted()) {
					if (itemTypeID.equals(item.getId())) {
						// error le type pointe sur lui meme ...
						addError(item, "error when loaded type item type of self !!!");
						return null;
					}
					// on essaye de creer le type ...
					anItem = loadOneItem(itemTypeOperation);
					if (anItem == null) {
						// une erreur c'est produit ...
						addError(item, "error when loaded this type.");
						return null;
					}
				} else {

					Link linkInstanceOf = item.getOutgoingLink(CadseRootCST.ITEM_TYPE_lt_INSTANCE_OF);
					if (linkInstanceOf == null) {
						addError(item, "type not found, cannot create unresolved type, instance-of link not found");
						return null;
					}
					String sn = linkInstanceOf.getDestinationName();
					String un = linkInstanceOf.getDestinationQualifiedName();
					ItemType ret = wl.createUnresolvedItemType(itemTypeID, sn, un);
					// nous n'avons pas trouv�� le type
					addError(item, "type not found : create an unresolved type !!!");
					return ret;
				}
			}
			if (!(anItem instanceof ItemType)) {
				// le type trouver n'est pas un type !!!
				addError(item, "type not instance of ItemType !!!");
				return null;
			}
			it = (ItemType) anItem;
		}
		return it;
	}

	private ItemType findOrCreateSuperItemTypeFromItemOperation(ItemDelta item) throws CadseException {

		Item superItem = item.getOutgoingItem(CadseRootCST.META_ITEM_TYPE_lt_SUPER_TYPE, false);
		if (superItem == null) {
			return CadseRootCST.ITEM_TYPE;
		}
		// recupere l'id du super type de l'item
		CompactUUID itemTypeID = superItem.getId();

		// est-ce qu'il existe d��j�� ?
		ItemType it = wl.getItemType(superItem.getId());
		if (it == null) {
			// on l'a deja creer pr��c��dement ?
			Item anItem = visited.get(itemTypeID);
			if (anItem == null) {
				// non
				// est-ce qu' nous l'avons dans les items �� charg��/creer
				ItemDelta itemTypeOperation = copy.getItem(itemTypeID);
				if (itemTypeOperation != null && !itemTypeOperation.isDeleted()) {
					if (itemTypeID.equals(item.getId())) {
						// error le type pointe sur lui meme ...
						addError(item, "error when loaded super type of before loaded this item type !!!");
						return null;
					}
					// on essaye de creer le type ...
					anItem = loadOneItem(itemTypeOperation);
					if (anItem == null) {
						// une erreur c'est produit ...
						addError(item, "error when loaded super type of before loaded this item type !!!");
						return null;
					}
				} else {
					// nous n'avons pas trouv�� le type
					addError(item, "super type not found !!!");
					return null;
				}
			}
			if (!(anItem instanceof ItemType)) {
				// le type trouver n'est pas un type !!!
				addError(item, "super type not instance of ItemType !!!");
				return null;
			}
			it = (ItemType) anItem;
		}
		return it;
	}

	private void callCreatedItem() throws CadseException {
		for (ItemDelta item : itemsAdded.keySet()) {
			Item goodItem = wl._items.get(item.getId());
			try {
				IItemManager im = item.getType().getItemManager();
				// /for (Link l : goodItem.getIncomingLinks()) {
				// l.getLinkType().getManager().createdDestinationItem(goodItem);
				// }
				im.createdItem(goodItem);
			} catch (Throwable e) {
				addError(item, e.getMessage());
				e.printStackTrace();
			}
			IAttributeType<?>[] attributesDefinitions = goodItem.getLocalAllAttributeTypes();
			if (attributesDefinitions != null) {
				for (IAttributeType<?> attributeType : attributesDefinitions) {
					try {
						if (attributeType.mustBeCreateNewValueAtCreationTimeOfItem()) {
							Object v = attributeType.createNewValueFor(goodItem);
							if (v != null) {
								goodItem.commitSetAttribute(attributeType, attributeType.getName(), v);
							}
						}
					} catch (Throwable e) {
						wl.getCadseDomain().log("wl",
								"error in create automatic value at the createion time " + attributeType, e);
					}
				}
			}
			if (goodItem.itemHasContent()) {
				try {
					goodItem.loadContent();
					ContentItem contentManager = item.getContentItem();
					if (contentManager != null) {
						contentManager.create();
					}
				} catch (CadseException e) {
					addError(item, e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}

	void loadProjectAssociation() throws CadseException {
		if (projectAssociationSet != null) {
			for (ProjectAssociation pa : projectAssociationSet) {
				Item item = wl._items.get(pa.getItemref());
				if (item == null) {
					throw new CadseException("Cannot find item form " + pa.getItemref());
				}
				CadseCore.setItemPersistenceID(pa.getProjectName(), item);
				HashMap<String, URL> entries = pa.getContentEntries();
				if (entries != null) {
					for (Map.Entry<String, URL> e : entries.entrySet()) {
						URL data = e.getValue();
						String path = e.getKey();
						CadseCore.copyResource(item, path, data);
					}
				}
			}
		}
	}

	public void addError(WLWCOperationImpl id, String msg) {
		// TODO Auto-generated method stub

	}

}