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

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.imag.adele.cadse.core.CadseDomain;
import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.CadseRuntime;
import fr.imag.adele.cadse.core.ChangeID;
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
import fr.imag.adele.cadse.core.Messages;
import fr.imag.adele.cadse.core.TypeDefinition;
import fr.imag.adele.cadse.core.WorkspaceListener;
import fr.imag.adele.cadse.core.TypeDefinition.Internal;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.build.Composer;
import fr.imag.adele.cadse.core.build.Exporter;
import fr.imag.adele.cadse.core.build.IBuildingContext;
import fr.imag.adele.cadse.core.content.ContentItem;
import fr.imag.adele.cadse.core.impl.CadseCore;
import fr.imag.adele.cadse.core.impl.CadseIllegalArgumentException;
import fr.imag.adele.cadse.core.impl.CollectedReflectLink;
import fr.imag.adele.cadse.core.impl.PageRuntimeModel;
import fr.imag.adele.cadse.core.impl.ReflectLink;
import fr.imag.adele.cadse.core.impl.db.DBObject;
import fr.imag.adele.cadse.core.impl.internal.delta.ItemTypeItemDeltaAdapter;
import fr.imag.adele.cadse.core.internal.IWorkingLoadingItems;
import fr.imag.adele.cadse.core.internal.IWorkspaceNotifier;
import fr.imag.adele.cadse.core.internal.InternalItem;
import fr.imag.adele.cadse.core.key.DefaultKeyImpl;
import fr.imag.adele.cadse.core.key.Key;
import fr.imag.adele.cadse.core.key.KeyDefinition;
import fr.imag.adele.cadse.core.transaction.delta.ImmutableWorkspaceDelta;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.transaction.delta.LinkDelta;
import fr.imag.adele.cadse.core.ui.Pages;
import fr.imag.adele.cadse.core.ui.view.FilterContext;
import fr.imag.adele.cadse.core.ui.view.NewContext;
import fr.imag.adele.cadse.core.util.Convert;
import fr.imag.adele.cadse.core.util.IErrorCollector;
import fr.imag.adele.cadse.util.*;
import fr.imag.adele.teamwork.db.ModelVersionDBException;

public abstract class AbstractGeneratedItem extends DBObject implements Item,
		InternalItem {

	private static final Composer[] NO_COMPOSER = new Composer[0];
	private static final Exporter[] NO_EXPORTER = new Exporter[0];
	// listener attributes
	protected WorkspaceListener[] _listeners = null;
	protected int[] _filter = null;
	// flag
	private int _flag = 0;
	private int _definedflag = 0;
	// links
	private Object[] _incomings = null;
	protected Object[] _outgoings = null;

	// evol attributes
	protected String _committedBy;
	protected long _committedDate;
	protected int _version;
	protected IAttributeType<?>[] _modifiedAttributeTypes = null;

	// generic attributes
	protected Object[] _attributes = null;
	protected ItemDelta _workingCopyOperation = null;

	/** The state. */
	protected ItemState _state = ItemState.NOT_IN_WORKSPACE;

	/** The key. */
	private Key _key = DefaultKeyImpl.NO_INIT_KEY;

	/** The contentmanager. */
	ContentItem _contentitem = null;
	/** The parent. */
	protected Item _parent = null;
	protected ItemType _group;
	protected CadseRuntime _cadse;

	// private LinkType partLinkType;

	/** The composers. */
	private Composer[] _composers;

	/** The exporters. */
	private Exporter[] _exporters;

	public boolean isTWAttributeModified(IAttributeType<?> att) {
		return ArraysUtil.indexOf(_modifiedAttributeTypes, att) != -1;
	}

	public AbstractGeneratedItem() {
	}

	public AbstractGeneratedItem(boolean createUUID)
			throws ModelVersionDBException {
		this(-1, 0);
		if (createUUID)
			setUUID(UUID.randomUUID());
	}

	public AbstractGeneratedItem(int objectId) {
		this(objectId, 0);
	}

	public AbstractGeneratedItem(UUID id, int flag) {
		this(-1, flag);
		setUUID(id);
	}

	public AbstractGeneratedItem(int objectId, int flag) {
		super(objectId);
		_flag = flag;
		_definedflag = flag;
	}

	public AbstractGeneratedItem(ItemDelta item) {
		_objectId = item.getObjectId();
		setUUID( item.getId());
		if (_objectId == -1) {
			// this._id = UUID.randomUUID();
		}
	}

	@Override
	public boolean exists() {
		return _dblw.exists(this);
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
			ImmutableWorkspaceDelta delta) {
		if (_listeners == null) {
			return null;
		}
		ArrayList<WorkspaceListener> ret = new ArrayList<WorkspaceListener>();
		for (int i = 0; i < _listeners.length; i++) {
			int f = _filter[i];
			WorkspaceListener l = _listeners[i];
			if ((f == -1) && l.getFilter().accept(filters, delta)) {
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

	public Link addOutgoingItem(LinkType linkType, Item destination)
			throws CadseException {
		return createLink(linkType, destination);
	}

	public Link removeOutgoingItem(LinkType linkType, Item destination)
			throws CadseException {
		return Accessor.removeOutgoingItem(this, linkType, destination);
	}

	public void buildComposite() throws CadseException {
		// TODO Auto-generated method stub

	}

	public boolean canCreateLink(LinkType lt, UUID destination) {
		if (isReadOnly()) {
			return false;
		}
		return true;
	}

	public boolean canEditContent(String slashedPath) {
		if (isReadOnly()) {
			return false;
		}
		return true;
	}

	public boolean canSetAttribute(String key, Object value) {
		if (isReadOnly()) {
			return false;
		}
		return true;
	}

	public void computeAttribute(String attributeName, Object theirsValue,
			Object baseValue, Object mineValue) {
		// TODO Auto-generated method stub

	}

	public boolean containsComponent(UUID id) {
		return false;
	}

	public boolean containsPartChild(Item item) {
		return false;
	}

	public boolean contentIsLoaded() {
		return _contentitem != null
				&& _contentitem != ContentItem.INVALID_CONTENT;
	}

	/**
	 * Create a link. <br/>
	 * <br/>
	 * 
	 * @param lt
	 *            : type of link to create.
	 * @param destination
	 *            : destination of link<br/>
	 * <br/>
	 * 
	 * @return a new link <br/>
	 * <br/>
	 * 
	 * @throws CadseException
	 *             the melusine exception
	 * @throws IllegalArgumentException
	 *             : Link type <tt>lt</tt> is null.<br/>
	 *             IllegalArgumentException: Link type <tt>lt</tt> was not
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
	 * 
	 * @NOTE: Use this method to create a link instead of using directly
	 *        contructor of class Link. This hepls us control the cohenrence all
	 *        over model. Link created by this method is normal link, it means
	 *        it has a physic object destination.
	 * @contraints: - 1. Link type <tt>lt</tt> cannot be null. <br/>
	 *              - 2. Link type <tt>lt</tt> must be selected in workspace
	 *              type. <br/>
	 *              - 3. Type of <tt>source</tt> must be the same type as item
	 *              type source defined in link type<tt>lt</tt> <br/>
	 *              - 4. Type of <tt>destination</tt> must be the same type as
	 *              item type destination defined in <tt>lt</tt> <br/>
	 *              - 5. The number of outgoing links having type <tt>lt</tt> of
	 *              this item can not excerce the cardinality min and max
	 *              defined in link type <tt>lt</tt>. <br/>
	 *              - 6. <tt>destination</tt> cannot be null. <br/>
	 *              - 7. Source and destination must be two different objects
	 *              (it means an item cannot point to itself).<br/>
	 *              - 8. Beetwen source and destination there have no link the
	 *              same type have ready created.<br/>
	 * <br/>
	 * @OCL: <b>context:</b> Item::createLink(String id, LinkType lt, Item
	 *       destination) : Link </br> <b>pre:</b> <tt>lt</tt> <> null <i> //
	 *       Link type <tt>lt</tt> cannot be null. <br/>
	 *       <b>pre:</b> <tt>self.workspace.type.selectedLinkTypes</tt>
	 *       ->include(<tt>lt</tt>) <i> // Link type <tt>lt</tt> must be
	 *       selected in workspace type. <br/>
	 *       <b>pre:</b> <tt>self.type</tt> = <tt>lt.type.source</tt> <i> //
	 *       Type of <tt>source</tt> must be the same type as item type source
	 *       defined in link type<tt>lt</tt> <br/>
	 *       <b>pre:</b> <tt>destination.type</tt> = <tt>lt.type.dest </tt> <i>
	 *       // Type of <tt>destination</tt> must be the same type as item type
	 *       destination defined in <tt>lt</tt> <br/>
	 *       <b>pre:</b> let <tt>s</tt> = <tt>self.to</tt>->collect(<tt>l</tt> |
	 *       <tt>l.type</tt> = <tt>lt</tt>) <tt>s</tt>->size() >=
	 *       <tt>lt.min</tt> and if (<tt>lt.max</tt> !=-1) then <tt>s</tt>
	 *       ->size() <= <tt>lt.max</tt> <i> // The number of outgoing links
	 *       having type <tt>lt</tt> of this item can not excerce the
	 *       cardinality min and max defined in link type <tt>lt</tt>. <br/>
	 *       <b>pre:</b> <tt>destination</tt> <> null <i>// <tt>destination</tt>
	 *       cannot be null. <br/>
	 *       <b>pre:</b> <tt>self</tt> != <tt>destination </tt> <i> // Source
	 *       and destination must be two different objects (it means an item
	 *       cannot point to itself).<br/>
	 *       <b>pre:</b> not <tt>self.to</tt>->exist(<tt>l</tt> |
	 *       <tt>l.dest</tt> = <tt>destination</tt> or <tt>l.destId</tt> =
	 *       <tt>destination.id</tt>) <i> // Beetwen source and destination
	 *       there have no link the same type have ready created.<br/>
	 * <br/>
	 */
	public Link createLink(LinkType lt, Item destination) throws CadseException {
		return Accessor.createLink(this, lt, destination);
	}

	/**
	 * Delete an item.<br/>
	 * 
	 * @throws CadseException
	 * 
	 * @NOTE: This method does: - Delete all <tt>incomings</tt> and
	 *        <tt>outgoings</tt> links of this item. <br/>
	 *        - Delete all contents of item.<br/>
	 *        - Detach item itself from workspace.<br/>
	 * 
	 * @throws IllegalArgumentException
	 *             : It is not possible to delete item <tt>$this.getId()</tt>
	 *             because of it has an incomming link with a read only item
	 *             <tt>$source.id</tt> <br/>
	 */
	public void delete(boolean deleteContent) throws CadseException {
		Accessor.delete(this, deleteContent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.Item#forceState(fr.imag.adele.cadse.core.ItemState
	 * )
	 */
	public void forceState(ItemState state) {
		if (ItemState.NOT_IN_WORKSPACE == _state) {
			throw new CadseIllegalArgumentException(
					Messages.error_cannot_change_state, getId());
		}
		ItemState oldstate = _state;
		_state = state;
		getCadseDomain().notifieChangeEvent(ChangeID.STATE, this, oldstate,
				_state);
	}

	/**
	 * Get all outgoing links of kind Aggregation.
	 * 
	 * @return list links of kind Aggregation.
	 */
	public List<Link> getAggregations() {
		return Accessor
				.getLinksByKind(getOutgoingLinks(), LinkType.AGGREGATION);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#getAndCreateContentManager()
	 */
	@Deprecated
	public ContentItem getAndCreateContentManager() {
		return getContentItem();
	}

	public Item getComponentInfo(UUID id) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<Item> getComponents() {
		return Collections.emptySet();
	}

	public Set<UUID> getComponentIds() {
		return Collections.emptySet();
	}

	public List<Item> getCompositeParent() {
		return Collections.emptyList();
	}

	public ContentItem getContentItem() {
		// if (_contentitem == null && this._state !=
		// ItemState.NOT_IN_WORKSPACE) {
		// try {
		// _dblw.loadContentManager(this);
		// } catch (CadseException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// _contentitem = ContentItem.INVALID_CONTENT;
		// }
		// }
		if (_contentitem == ContentItem.NO_CONTENT
				|| _contentitem == ContentItem.INVALID_CONTENT) {
			return null;
		}
		return _contentitem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#getContentItem()
	 */
	final public ContentItem _getContentItem() {
		return _contentitem;
	}

	@Deprecated
	public Set<DerivedLinkDescription> getDerivedLinkDescriptions(
			ItemDescription source) {
		return Collections.emptySet();
	}

	@Deprecated
	public Set<DerivedLink> getDerivedLinks() {
		return Collections.emptySet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#getDisplayName()
	 */
	public String getDisplayName() {
		return getType().getItemManager().getDisplayName(this);
	}

	public String getHandleIdentifier() throws CadseException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.Item#getIncomingItem(fr.imag.adele.cadse.core
	 * .LinkType, boolean)
	 */
	public Collection<Item> getIncomingItems(LinkType lt) {
		return Accessor.getIncomingItem(getIncomingLinks(lt));
	}

	public Collection<Item> getIncomingItems() {
		return Accessor.getIncomingItem(getIncomingLinks());
	}

	public Item getIncomingItem(LinkType lt) {
		Collection<Item> incomingItem = getIncomingItems(lt);
		Item ret = incomingItem.size() == 1 ? incomingItem.iterator().next()
				: null;
		return ret;
	}

	public List<Link> getIncomingLinks() {
		CollectedReflectLink ret = new CollectedReflectLink(this);
		if (_incomings != null) {
			for (int i = 0; i < _incomings.length; i += 2) {
				try {
					ret.addIncoming((LinkType) _incomings[i],
							(Item) _incomings[i + 1]);
				} catch (RuntimeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return ret;
	}

	public void addIncomingLink(Link link, boolean notifie) {
		Item source = link.getSource();
		LinkType lt = link.getLinkType();
		Assert.isNotNull(lt);
		Assert.isNotNull(source);
		int index = findIndex(_incomings, lt, source);
		if (index != -1) {
			Logger.getLogger("CU.Workspace.incomingslinks").log(Level.FINE,
					"Allready register " + link,
					new CadseException("Allready register " + link));
			return;
		}
		_incomings = ArraysUtil.addList2(Object.class, _incomings, lt, source);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.Item#removeIncomingLink(fr.imag.adele
	 * .cadse.core.Link, boolean)
	 */
	public void removeIncomingLink(Link link, boolean notifie) {
		Item source = link.getSource();
		LinkType lt = link.getLinkType();
		int index = findIndex(_incomings, lt, source);
		if (index != -1) {
			_incomings = ArraysUtil.remove(Object.class, _incomings, index, 2);
			if (notifie) {
				getCadseDomain().notifieChangeEvent(
						ChangeID.UNRESOLVE_INCOMING_LINK, this, link);
			}
		}
	}

	static int findIndex(Object[] links, LinkType lt, Item item) {
		if (links != null) {
			for (int i = 0; i < links.length; i += 2) {
				if (lt == links[i] && item == links[i + 1]) {
					return i;
				}
			}
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.Item#removeOutgoingLink(fr.imag.adele
	 * .cadse.core.Link, boolean)
	 */
	public void removeOutgoingLink(Link link, boolean notifie) {
		if (link.getLinkType() == CadseGCST.ITEM_lt_MODIFIED_ATTRIBUTES) {
			int index = ArraysUtil.indexOf(this._modifiedAttributeTypes, link
					.getDestination());
			if (index == -1) {
				return;
			}
			this._modifiedAttributeTypes = ArraysUtil.remove(
					IAttributeType.class, this._modifiedAttributeTypes, index);

			if (notifie) {
				getCadseDomain().notifieChangeEvent(
						ChangeID.DELETE_OUTGOING_LINK, this, link);
			}
		}
		Item dest = link.getDestination();
		LinkType lt = link.getLinkType();
		int index = findIndex(_outgoings, lt, dest);
		if (index != -1) {
			_outgoings = ArraysUtil.remove(Object.class, _outgoings, index, 2);
			if (notifie) {
				getCadseDomain().notifieChangeEvent(
						ChangeID.DELETE_OUTGOING_LINK, this, link);
			}
		}
	}

	public Link getIncomingLinks(LinkType lt, Item source) {
		int index = findIndex(_incomings, lt, source);
		if (index != -1) {
			return new ReflectLink(lt, source, this, -1);
		}
		return null;
	}

	public List<Link> getIncomingLinks(LinkType lt) {
		CollectedReflectLink ret = new CollectedReflectLink(this);
		if (_incomings != null) {
			for (int i = 0; i < _incomings.length; i += 2) {
				LinkType incoming_lt = (LinkType) _incomings[i];
				if (incoming_lt == lt) {
					ret.addIncoming(incoming_lt, (Item) _incomings[i + 1]);
				}
			}
		}
		return ret;
	}

	public Link getIncomingLink(LinkType lt, UUID srcId) {
		if (_incomings != null) {
			for (int i = 0; i < _incomings.length; i += 2) {
				LinkType incoming_lt = (LinkType) _incomings[i];
				Item src = (Item) _incomings[i + 1];
				if (src == null) {
					continue;
				}
				if (incoming_lt == lt && src.getId().equals(srcId)) {
					return new ReflectLink(incoming_lt,
							(Item) _incomings[i + 1], this, -1);
				}
			}
		}
		return null;
	}

	public String getInfo() {
		return null;
	}

	public Key getKey() {
		_key = Accessor.computekey(_key, getType(), this);
		return _key;
	}

	public File getLocation() {
		return getMainMappingContent(File.class);
	}

	@Override
	public <T> T getMainMappingContent(Class<T> clazz) {
		ContentItem cm = getContentItem();
		if (cm == null) {
			return null;
		}
		return cm.getMainMappingContent(clazz);
	}

	public List<?> getMappingContents() {
		ContentItem cm = getContentItem();
		if (cm == null) {
			return null;
		}
		return cm.getMappingContents();
	}

	@Override
	public <T> List<T> getMappingContents(Class<T> clazz) {
		ContentItem cm = getContentItem();
		if (cm == null) {
			return null;
		}
		return cm.getMappingContents(clazz);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#getOutgoingItems(java.lang.String,
	 * fr.imag.adele.cadse.core.UUID, boolean)
	 */
	public Item getOutgoingItem(String attributte_link, UUID itemId,
			boolean resovledOnly) {
		return Accessor.getOutgoingItems(getOutgoingLinks(), attributte_link,
				itemId, resovledOnly);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.Item#getOutgoingItems(fr.imag.adele.cadse.core
	 * .LinkType, boolean)
	 */
	public Collection<Item> getOutgoingItems(LinkType lt, boolean resovledOnly) {
		return Accessor
				.getOutgoingItems(getOutgoingLinks(lt), lt, resovledOnly);
	}

	public Collection<Item> getOutgoingItems(boolean resovledOnly) {
		return Accessor.getOutgoingItems(getOutgoingLinks(), resovledOnly);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#getOutgoingItems(java.lang.String,
	 * boolean)
	 */
	public Collection<Item> getOutgoingItems(String linkTypeName,
			boolean resovledOnly) {
		return Accessor.getOutgoingItems(getOutgoingLinks(), linkTypeName,
				resovledOnly);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.Item#getOutgoingItem(fr.imag.adele.cadse.core
	 * .LinkType, boolean)
	 */
	public Item getOutgoingItem(LinkType lt, boolean resovledOnly) {
		return Accessor.getOutgoingItem(getOutgoingLinks(lt), lt, resovledOnly);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#getOutgoingItem(java.lang.String,
	 * boolean)
	 */
	public Item getOutgoingItem(String linkTypeName, boolean resovledOnly) {
		return Accessor.getOutgoingItem(getOutgoingLinks(), linkTypeName,
				resovledOnly);
	}

	public Link getOutgoingLink(LinkType linkType) {
		CollectedReflectLink ret = new CollectedReflectLink(this);
		collectOutgoingLinks(linkType, ret);
		return ret.size() == 1 ? ret.get(0) : null;
	}

	public Link getOutgoingLink(Item item) {
		List<Link> links = getOutgoingLinks();
		return Accessor.getOutgoingLink(links, item);
	}

	public Link getOutgoingLink(LinkType lt, UUID destId) {
		List<Link> links = getOutgoingLinks(lt);
		return Accessor.getOutgoingLink(destId, links);
	}

	public List<Link> getOutgoingLinks(LinkType linkType) {
		CollectedReflectLink ret = new CollectedReflectLink(this);
		collectOutgoingLinks(linkType, ret);
		return ret;
	}

	public Item getPartParent() {
		return getPartParent(false);
	}

	/**
	 * find the parent item (fist) from the incoming links
	 * 
	 * @param set
	 * @return the parent if found or null;
	 */
	protected Item findPartParentFromIncoming(boolean set) {
		return findPartParentFromIncoming(null, set);
	}

	/**
	 * find the parent item from the incoming links
	 * 
	 * @param lt
	 *            the type of the link or null if (first part). Null is the
	 *            first and default comportement
	 * @param set
	 *            true if call setParent
	 * @return the parent if found or null
	 */
	protected Item findPartParentFromIncoming(LinkType lt, boolean set) {
		Item parent = null;
		for (Link l : getIncomingLinks()) {
			if (l.getLinkType() == null) {
				getCadseDomain().error(
						this,
						"Type of link is null : dest = " + getId()
								+ " source = " + l.getSource(), null);
				continue;
			}
			if ((lt == null && l.getLinkType().isPart())
					|| (l.getLinkType() == lt)) {
				parent = l.getSource();
				if (set) {
					setParent(parent, l.getLinkType());
				}
				return parent;
			}
		}
		return null;
	}

	/**
	 * find the parent item from the outgoings links
	 * 
	 * @param lt
	 *            the type of the link or null if (first part). Null is the
	 *            first and default comportement
	 * @param set
	 *            true if call setParent
	 * @return the parent if found or null
	 */
	protected Item findPartParentFromOutgoing(LinkType lt, boolean set) {
		Item parent = null;
		for (Link l : getOutgoingLinks()) {
			final LinkType theTypeOfThisLink = l.getLinkType();
			if (theTypeOfThisLink == null) {
				getCadseDomain().error(
						this,
						"Type of link is null : dest = " + getId()
								+ " source = " + l.getSource(), null);
				continue;
			}
			if ((theTypeOfThisLink.isInversePart())
					&& (lt == null || theTypeOfThisLink.getInverse() == lt)) {
				parent = l.getDestination();
				if (set) {
					setParent(parent, theTypeOfThisLink.getInverse());
				}
				return parent;
			}
		}
		return null;
	}

	public Item getPartParent(boolean attemptToRecreate) {
		if (_parent == null) {
			if (_outgoings != null)
				for (int i = 0; i < _outgoings.length; i++) {
					LinkType lt = (LinkType) _outgoings[i];
					Item d = (Item) _outgoings[++i];
					if (lt == CadseGCST.ITEM_lt_PARENT) {
						setParent(d, null);
						break;
					}
				}
		}
		return _parent;
	}

	public Item getPartParent(LinkType lt) {
		return getIncomingItem(lt);
	}

	public Item getPartParent(LinkType lt, boolean attemptToRecreate)
			throws CadseException {
		return getPartParent(lt);
	}

	public Item getPartParent(ItemType typeID) {
		return Accessor.getPartParent(this, typeID);
	}

	public Item getPartParentByName(String typeName) {
		return Accessor.getPartParentByName(this, typeName);
	}

	/**
	 * @deprecated Use {@link #getPartParentLinkType()} instead
	 */
	@Deprecated
	public LinkType getPartParentLink() {
		return Accessor.getPartParentLinkType(this);
	}

	public LinkType getPartParentLinkType() {
		return Accessor.getPartParentLinkType(this);
	}

	/**
	 * Gets the part link parent.
	 * 
	 * @return the part link parent
	 */
	public Link getPartLinkParent() {
		return Accessor.getPartParentLink(this);
	}

	/**
	 * Get all Contents.
	 * 
	 * @return an unmodifiable list all contents of item.
	 * 
	 * @NOTE This method does: - Get all outgoing links of kind Containement. -
	 *       For each link, if its destination is not null, take this
	 *       destination to return list.
	 */
	public Collection<Item> getPartChildren() {
		return Accessor.getParts(getOutgoingLinks());
	}

	/**
	 * Get all part item.
	 * 
	 * @param lt
	 *            the link type definition object
	 * 
	 * @return an unmodifiable list all contents of item.
	 * 
	 * @NOTE This method does: - Get all outgoing links of kind Part. - For each
	 *       link, if its destination is not null, take this destination to
	 *       return list.
	 */
	public Collection<Item> getPartChildren(LinkType lt) {
		return Accessor.getParts(getOutgoingLinks(lt), lt);
	}

	/**
	 * Get a part by id.
	 * 
	 * @param id
	 *            : content's id we want seek.
	 * 
	 * @return an object content if found, return null if not found.
	 * 
	 * @NOTE this method does: Seek content of a item by id given. - Search all
	 *       outgoing links of kind Containement. - If found a link has
	 *       destination's id equal to id parameter, then return this object
	 *       destination. - If not found, return null.
	 */
	public Item getPartChild(UUID id) {
		return Accessor.getParts(getOutgoingLinks(), id);
	}

	public String getQualifiedDisplayName() {
		Key key = getKey();
		if (key != null) {
			return key.getQualifiedString();
		}
		return Item.NO_VALUE_STRING;
	}

	public String getName() {
		return Item.NO_VALUE_STRING;
	}

	@Deprecated
	final public String getShortName() {
		return getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#getState()
	 */
	public ItemState getState() {
		if (_state == null) {
			_state = ItemState.NOT_IN_WORKSPACE;
		}
		return _state;
	}

	@Deprecated
	final public String getUniqueName() {
		return getQualifiedName();
	}

	public String getQualifiedName() {
		Key key = getKey();
		if (key != null) {
			return key.getQualifiedString();
		}
		return Item.NO_VALUE_STRING;
	}

	public CadseDomain getCadseDomain() {
		return CadseCore.getCadseDomain();
	}

	public LogicalWorkspace getLogicalWorkspace() {
		return _dblw;
	}

	public boolean isAccessible() {
		return true;
	}

	public boolean isAncestorOf(Item item2) {
		return Accessor.isAncestorOf(this, item2);
	}

	public boolean isClosed() {
		return false;
	}

	public boolean isComposite() {
		if (getType() == null) {
			return false;
		}
		return getType().isComposite();
	}

	public boolean isHidden() {
		if (getFlag(IS_HIDDEN)) {
			return true;
		}
		ItemType _type = getType();
		if (_type == this) {
			return false;// cas du mt type
		}
		if (_type == null) {
			return false;
		}
		return _type.isHidden();
	}

	public boolean isInIncomingLinks(Link l) {
		assert l != null;
		return l.getDestination() == this;
	}

	public boolean isInOutgoingLinks(Link l) {
		assert l != null;
		return l.getSource() == this;
	}

	public boolean isInstanceOf(TypeDefinition it) {
		return Accessor.isInstanceOf(this, it);
	}

	/**
	 * Ask if this item was modified.
	 * 
	 * @return true if this item is modified</tt>.
	 */
	public boolean isModified() {
		return getFlag(IS_MODIFIED);
	}

	public boolean isOrphan() {
		return getFlag(WORKING_COPY);
	}

	public boolean isPartItem() {
		if (getType() == null)
			return false;
		return getType().isPartType();
	}

	public boolean isReadOnly() {
		if (getState() == ItemState.CREATED
				|| getState() == ItemState.NOT_IN_WORKSPACE) {
			return false;
		}
		// a static item is readonly
		return getFlag(UNRESOLVED) || getFlag(READONLY);
	}

	public boolean isResolved() {
		return !getFlag(UNRESOLVED);
	}

	public boolean isValid() {
		return true;
	}

	public boolean itemHasContent() {
		ItemType type = getType();
		if (type != null)
			return type.hasContent();

		return getFlag(HAS_CONTENT);
	}

	public void refresh() {
		_dblw.getCadseDomain().refresh(this);
	}

	public void removeContentItem() {
		this._contentitem = ContentItem.NO_CONTENT;
	}

	public void setComponents(Set<ItemDescriptionRef> comp)
			throws CadseException {
		// TODO Auto-generated method stub

	}

	@Deprecated
	public void setDerivedLinks(Set<DerivedLinkDescription> derivedLinks) {
	}

	public void setModified(boolean flag) {
		setFlag(IS_MODIFIED, flag);
	}

	@Override
	public Collection<Link> setOutgoingItems(LinkType lt, Collection<Item> value)
			throws CadseException {
		return Accessor.setOutgoingItem(this, lt, value);
	}

	public Link setOutgoingItem(LinkType lt, Item destination)
			throws CadseException {
		return Accessor.setOutgoingItemOne(this, lt, destination);
	}

	public void setReadOnly(boolean readOnly) {
		if (setFlag(READONLY, readOnly)) {
			setModified(true);
			_dblw.getCadseDomain().setReadOnly(this, readOnly);
		}
	}

	public void setShortName(String shortname) throws CadseException {
		setName(shortname);
	}

	public void setName(String name) {
		try {
			setAttribute(CadseGCST.ITEM_at_NAME_, name);
		} catch (CadseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setType(ItemType selectedItemType) {
		// TODO Auto-generated method stub

	}

	public void setQualifiedName(String qualifiedName) {
		try {
			setAttribute(CadseGCST.ITEM_at_QUALIFIED_NAME_, qualifiedName);
		} catch (CadseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setUniqueName(String uniqueName) throws CadseException {
		setAttribute(CadseGCST.ITEM_at_QUALIFIED_NAME_, uniqueName);
	}

	public void setValid(boolean valid) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIdInPackage(int idInPackage) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * Shadow an item.<br/>
	 * 
	 * @param deleteContent
	 *            the delete content
	 * 
	 * @throws CadseException
	 *             the melusine exception
	 * 
	 * @NOTE: This method does: - Unresolve all <tt>incomings</tt> of this item. <br/>
	 *        - Delete all <tt>outgoings</tt> links. <br/>
	 *        - Delete all contents.<br/>
	 *        - Detach item itself from workspace.<br/>
	 */
	public void shadow(boolean deleteContent) throws CadseException {
		Accessor.shadow(this, deleteContent);

	}

	public void unload() throws CadseException {
		// TODO Auto-generated method stub

	}

	public List<Link> getOutgoingLinks() {
		CollectedReflectLink ret = new CollectedReflectLink(this);
		ItemType type = getType();
		if (type == null) {
			return ret;
		}

		List<LinkType> lts = getLocalOutgoingLinkTypes();
		for (LinkType linkType : lts) {
			collectOutgoingLinks(linkType, ret);
		}
		return ret;
	}

	public List<LinkType> getLocalOutgoingLinkTypes() {
		Set<TypeDefinition> visited = new HashSet<TypeDefinition>();
		List<LinkType> ret = new ArrayList<LinkType>();
		computeLocalOutgoingLinkTypes(ret , visited);
		return ret;
	}

	protected void computeLocalIncomingLinkTypes(List<LinkType> ret, Set<TypeDefinition> visited) {
		if (visited.contains(this)) return;
		if (getType() != null) {
			((Internal) getType()).computeIncomingLinkTypes(ret, visited);
		}
		if (_group != null)
			((Internal) _group).computeIncomingLinkTypes(ret, visited);
	}

	protected void computeLocalOutgoingLinkTypes(List<LinkType> ret, Set<TypeDefinition> visited) {
		if (getType() != null) {
			((Internal) getType()).computeOutgoingLinkTypes(ret, visited);
		}
		if (_group != null)
			((Internal) _group).computeOutgoingLinkTypes(ret, visited);
	}


	protected void collectOutgoingLinks(LinkType linkType,
			CollectedReflectLink ret) {
		if (linkType == CadseGCST.GROUP_EXT_ITEM_lt_MEMBER_OF) {
			ret.addOutgoing(CadseGCST.GROUP_EXT_ITEM_lt_MEMBERS, _group);
			return;
		}
		if (linkType == CadseGCST.ITEM_lt_INSTANCE_OF) {
			ret.addOutgoing(CadseGCST.ITEM_lt_INSTANCE_OF, getType());
			return;
		}
		if (linkType == CadseGCST.ITEM_lt_MODIFIED_ATTRIBUTES) {
			ret.addOutgoing(CadseGCST.ITEM_lt_MODIFIED_ATTRIBUTES,
					Item.IS_HIDDEN, this._modifiedAttributeTypes);
			return;
		}
		if (linkType == CadseGCST.ITEM_lt_PARENT) {
			ret.addOutgoing(CadseGCST.ITEM_lt_PARENT, _parent, Item.IS_HIDDEN);
			return;
		}

		if (linkType == CadseGCST.ITEM_lt_CONTENTS) {
			if (_contentitem != ContentItem.NO_CONTENT
					&& _contentitem != ContentItem.INVALID_CONTENT)
				ret.addOutgoing(linkType, _contentitem, Item.IS_HIDDEN);
		}
		if (_outgoings != null) {
			for (int i = 0; i < _outgoings.length; i += 2) {
				if (linkType == _outgoings[i]) {
					Item dest = (Item) _outgoings[i + 1];
					try {
						ret.add(new ReflectLink(linkType, this, dest, -1));
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	final public <T> T getAttribute(IAttributeType<T> att) {
		return getLogicalWorkspace().getAttribute(this, att, false);
	}

	final public <T> T getAttributeOwner(IAttributeType<T> att) {
		return getLogicalWorkspace().getAttribute(this, att, true);
	}

	final public <T> T getAttributeWithDefaultValue(IAttributeType<T> att,
			T defaultValue) {
		try {
			T v = getAttribute(att);
			if (v != null) {
				return v;
			}
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return defaultValue;
	}

	public void setAttribute(IAttributeType<?> att, Object value)
			throws CadseException {
		CadseCore.getLogicalWorkspace().setAttribute(this, att, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.Item#internalGetOwnerAttribute(fr.imag
	 * .adele.cadse.core.attribute.IAttributeType)
	 */
	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {
		if (type == CadseGCST.ITEM_at_ID_) {
			return (T) getId();
		}
		if (type == CadseGCST.ITEM_at_DISPLAY_NAME_) {
			String d = getDisplayName();
			if (d != null && d != Item.NO_VALUE_STRING) {
				return (T) d;
			}
			return (T) getAttributeWithDefaultValue(CadseGCST.ITEM_at_NAME_,
					Item.NO_VALUE_STRING);
		}
		/*
		 * if (type == CadseGCST.ITEM_at_QUALIFIED_DISPLAY_NAME_) { return (T)
		 * getQualifiedName(); }
		 */
		if (type == CadseGCST.ITEM_at_QUALIFIED_NAME_) {
			return (T) getAttributeWithDefaultValue(CadseGCST.ITEM_at_NAME_,
					Item.NO_VALUE_STRING);
		}
		if (type == CadseGCST.ITEM_at_NAME_) {
			return (T) Item.NO_VALUE_STRING;
		}

		if (type == CadseGCST.ITEM_at_TW_VERSION_) {
			return (T) Integer.valueOf(_version);
		}
		if (type == CadseGCST.ITEM_at_COMMITTED_BY_) {
			return (T) _committedBy;
		}
		if (type == CadseGCST.ITEM_at_COMMITTED_DATE_) {
			return (T) new Long(_committedDate);
		}
		if (type == CadseGCST.ITEM_at_REQUIRE_NEW_REV_) {
			return (T) Boolean.valueOf(getFlag(Item.EVOL_REQUIRER_NEW_REV));
		}
		if (type == CadseGCST.ITEM_at_REV_MODIFIED_) {
			return (T) Boolean.valueOf(getFlag(Item.EVOL_REV_MODIFIED));
		}

		if (type == CadseGCST.ITEM_at_ITEM_HIDDEN_) {
			return (T) Boolean.valueOf(getFlag(Item.IS_HIDDEN));
		}
		if (type == CadseGCST.ITEM_at_ITEM_READONLY_) {
			return (T) Boolean.valueOf(getFlag(Item.READONLY));
		}
		return internalGetGenericOwnerAttribute(type);
	}

	public Iterator<Item> propagateValue(IAttributeType<?> type) {
		return null;
	}

	public Iterator<Item> propagateValue(String key) {
		return null;
	}

	// /*
	// * (non-Javadoc)
	// *
	// * @see
	// * fr.imag.adele.cadse.core.internal.Item#internalGetGenericOwnerAttribute
	// * (java.lang.String)
	// */
	// public <T> T internalGetGenericOwnerAttribute(String key) {
	// IAttributeType<? extends Object> attDefFound =
	// getType().getAttributeType(key, false);
	// if (attDefFound == null) return null;
	// if (attDefFound instanceof LinkType) {
	// return (T) getOutgoingLinks((LinkType) attDefFound);
	// }
	// if (_attributes == null) {
	// return null;
	// }
	// for (int i = 0; i < _attributes.length; i++) {
	// Object attDef = _attributes[i++];
	// Object av = _attributes[i];
	// if (attDefFound == attDef) {
	// return (T) av;
	// }
	// }
	// return null;
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.Item#internalGetGenericOwnerAttribute
	 * (fr.imag.adele.cadse.core.attribute.IAttributeType)
	 */
	public <T> T internalGetGenericOwnerAttribute(IAttributeType<T> attDefFound) {
		if (attDefFound instanceof LinkType) {
			return (T) getOutgoingLinks((LinkType) attDefFound);
		}
		if (_attributes == null) {
			return null;
		}
		for (int i = 0; i < _attributes.length; i++) {
			Object attDef = _attributes[i++];
			Object av = _attributes[i];
			if (attDef == attDefFound) {
				return (T) av;
			}
		}
		return null;
	}

	protected void loadCache() {
		setflag(true, PERSISTENCE_CACHE_LOADED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.Item#commitSetAttribute(fr.imag.adele
	 * .cadse.core.attribute.IAttributeType, java.lang.String, java.lang.Object)
	 */
	public boolean commitSetAttribute(IAttributeType<?> type, Object value) {
		if (CadseGCST.ITEM_at_TW_VERSION_ == type) {
			int _local_version = Convert.toInt(value,
					CadseGCST.ITEM_at_TW_VERSION_, 0);
			if (_version == _local_version) {
				return false;
			}
			_version = _local_version;
			return true;
		}
		if (CadseGCST.ITEM_at_COMMITTED_BY_ == type) {
			String _local_committedby = Convert.toString(value);
			if (Convert.equals(_committedBy, _local_committedby)) {
				return false;
			}
			_committedBy = _local_committedby;
			return true;
		}

		if (CadseGCST.ITEM_at_COMMITTED_DATE_ == type) {
			long _local_committedDate = Convert.toLong(value,
					CadseGCST.ITEM_at_COMMITTED_DATE_, new Date().getTime());
			if (Convert.equals(_committedBy, _local_committedDate)) {
				return false;
			}
			_committedDate = _local_committedDate;
			return true;
		}
		if (CadseGCST.ITEM_at_REQUIRE_NEW_REV_ == type) {
			boolean _local_b = Convert.toBoolean(value,
					CadseGCST.ITEM_at_REQUIRE_NEW_REV_, false);
			if (getFlag(Item.EVOL_REQUIRER_NEW_REV) == _local_b) {
				return false;
			}
			setFlag(Item.EVOL_REQUIRER_NEW_REV, _local_b);
			return true;
		}
		if (CadseGCST.ITEM_at_REV_MODIFIED_ == type) {
			boolean _local_b = Convert.toBoolean(value,
					CadseGCST.ITEM_at_REV_MODIFIED_, false);
			if (getFlag(Item.EVOL_REV_MODIFIED) == _local_b) {
				return false;
			}
			setFlag(Item.EVOL_REV_MODIFIED, _local_b);
			return true;
		}

		if (CadseGCST.ITEM_at_ITEM_HIDDEN_ == type) {
			boolean _local_b = Convert.toBoolean(value,
					CadseGCST.ITEM_at_ITEM_HIDDEN_, false);
			if (getFlag(Item.IS_HIDDEN) == _local_b) {
				return false;
			}
			setFlag(Item.IS_HIDDEN, _local_b);
			return true;
		}

		if (CadseGCST.ITEM_at_ITEM_READONLY_ == type) {
			boolean _local_b = Convert.toBoolean(value,
					CadseGCST.ITEM_at_ITEM_READONLY_, false);
			if (getFlag(Item.READONLY) == _local_b) {
				return false;
			}
			setReadOnly(_local_b);
			return true;
		}
		if (CadseGCST.ITEM_at_DISPLAY_NAME_ == type) {
			return false;
		}
		if (type == null)
			return false;

		return commitGenericSetAttribute(type, value);
	}

	protected boolean commitGenericSetAttribute(IAttributeType<?> type,
			Object value) {
		if (_attributes == null) {
			_attributes = ArraysUtil.addList2(Object.class, _attributes, type,
					value);
			return !type.getName().startsWith("#");
		}
		for (int i = 0; i < _attributes.length;) {
			Object ak = _attributes[i++];
			Object av = _attributes[i];
			if (ak == type) {
				if (!type.isValueModified(av, value)) {
					return false;
				}
				if (value != null) {
					_attributes[i] = value;
				} else {// remove
					_attributes = ArraysUtil.remove(Object.class, _attributes,
							i - 1, 2);
				}
				return !type.getName().startsWith("#");
			}
			i++;
		}
		_attributes = ArraysUtil.addList2(Object.class, _attributes, type,
				value);
		return !type.getName().startsWith("#");
	}

	public boolean isStatic() {
		return getFlag(IS_STATIC);
	}

	public void setIsStatic(boolean flag) {
		setflag(flag, IS_STATIC);
	}

	public boolean getFlag(int f) {
		if ((_definedflag & f) == 0) {
			return (Item.DEFAULT_FLAG_VALUE & f) != 0;
		}
		return (_flag & f) != 0;
	}

	public boolean isDefinedFlag(int f) {
		return (_definedflag & f) != 0;
	}

	@Deprecated
	public boolean setflag(boolean flag, int f) {
		return setFlag(f, flag);
	}

	public boolean setFlag(int f, boolean flag) {
		boolean oldv = getFlag(f);
		if (flag) {
			this._flag |= f;
		} else {
			this._flag &= ~f;
		}
		this._definedflag |= f;
		return oldv != flag;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.Item#commitLoadCreateLink(fr.imag.adele
	 * .cadse.core.LinkType, fr.imag.adele.cadse.core.Item)
	 */
	public Link commitLoadCreateLink(LinkType lt, Item destination)
			throws CadseException {
		if (lt == CadseGCST.ITEM_lt_CONTENTS) {
			_contentitem = (ContentItem) destination;
			return new ReflectLink(lt, this, destination, 0);
		}
		if (lt == CadseGCST.ITEM_lt_INSTANCE_OF) {
			ItemType loacltype2 = this.getType();
			if (loacltype2 != destination) {
				throw new CadseException(
						"Try to create a link of type 'instance-of' : Cannot change type "
								+ loacltype2 + " to " + destination);
			}
			return new ReflectLink(lt, this, destination, 0);
		}
		if (lt == CadseGCST.ITEM_lt_MODIFIED_ATTRIBUTES) {
			IAttributeType<?> att = (IAttributeType<?>) destination;
			this._modifiedAttributeTypes = ArraysUtil.add(IAttributeType.class,
					this._modifiedAttributeTypes, att);
			return new ReflectLink(lt, this, destination,
					this._modifiedAttributeTypes.length - 1);
		}
		if (lt == CadseGCST.ITEM_lt_PARENT) {
			if (!destination.isResolved())
				throw new CadseException("Cannot set unresolved parent", this,
						destination);
			if (destination != null
					&& destination instanceof ItemTypeItemDeltaAdapter)
				throw new CadseIllegalArgumentException(
						"Bad type of parent : {0} ", destination.getClass());

			_parent = destination;
			return new ReflectLink(lt, this, destination, 0);
		}
		return createDefaultLink(lt, destination);
	}

	protected Link createDefaultLink(LinkType lt, Item destination)
			throws CadseException {
		Assert.isNotNull(lt);
		Assert.isNotNull(destination);
		_outgoings = ArraysUtil.addList2(Object.class, _outgoings, lt,
				destination);
		ReflectLink reflectLink = new ReflectLink(lt, this, destination, -1);
		if (reflectLink != null) {
			destination.addIncomingLink(reflectLink, false);
		}
		return reflectLink;
	}

	public int indexOf(Link link) {
		LinkType lt = link.getLinkType();
		Item dest = link.getDestination();
		if (!lt.isNatif())
			return indexGeneric(lt, dest);
		CollectedReflectLink ret = new CollectedReflectLink(this);
		collectOutgoingLinks(lt, ret);
		int i = 0;
		for (Link l : ret) {
			if (l.getLinkType() == lt && l.getDestination() == dest)
				return i;
			i++;
		}
		return -1;
	}

	private int indexGeneric(LinkType lt, Item dest) {
		if (_outgoings != null) {
			for (int i = 0; i < _outgoings.length; i += 2) {
				if (lt == _outgoings[i] && dest == _outgoings[i + 1]) {
					return i >> 1;
				}
			}
		}
		return -1;
	}

	final public void setState(ItemState modifing) {
		this._state = modifing;

	}

	public void computeAttributes() {
		ItemType type = getType();
		if (type == null) {
			_key = null;
		} else {
			KeyDefinition keyType = type.getKeyDefinition();
			if (keyType != null) {
				try {
					_key = keyType.computeKey(this);
				} catch (Throwable e) {
					e.printStackTrace();
					_key = null; // sinon erreur !!!
				}
			} else {
				_key = null;
			}
		}
	}

	public void finishLoad() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.Item#loadItem(fr.imag.adele.cadse.core
	 * .IWorkingLoadingItems, fr.imag.adele.cadse.core.delta.ItemOperation,
	 * fr.imag.adele.cadse.core.util.IErrorCollector)
	 */
	public void loadItem(IWorkingLoadingItems wl, ItemDelta itemOperation,
			IErrorCollector errorCollector) throws CadseException {
		// find the parent

		Accessor.loadAttributes(this, itemOperation, errorCollector);

		for (LinkDelta ldesc : itemOperation.getOutgoingLinkOperations()) {
			try {
				if (ldesc.isDerived() || ldesc.isDeleted()) {
					continue;
				}
				if (!ldesc.isLoaded() && !ldesc.isAdded()) {
					continue;
				}
				LinkType lt = ldesc.getLinkType();
				if (lt == null) {
					errorCollector.addError(this, "Cannot load link " + ldesc);
				}

				Item dest = wl.loadItem(ldesc.getDestination());

				if (lt == CadseGCST.ITEM_lt_CONTENTS)
					continue;
				if (lt == CadseGCST.ITEM_lt_PARENT)
					continue;

				Link goodLink = null;
				LinkType invLt = lt.getInverse();
				if (invLt != null && invLt.isPart()) {
					// this -- lt --> dest
					// dest -- inv-lt --> this
					setParent(dest, invLt);
				}
				if (dest.isResolved()) {
					goodLink = commitLoadCreateLink(lt, dest);
				} else {
					goodLink = createDefaultLink(lt, dest);
				}
				if (goodLink != null) {
					dest.addIncomingLink(goodLink, false);
				}
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void update(IWorkingLoadingItems items, ItemDelta desc,
			IWorkspaceNotifier notifie) {

	}

	public String getQualifiedName(boolean recompute) {
		return getQualifiedName();
	}

	@Deprecated
	final public String getUniqueName(boolean recompute) throws CadseException {
		return getQualifiedName(recompute);
	}

	@Override
	public String toString() {
		String qdn = getQualifiedDisplayName();
		if (qdn != null && qdn.length() != 0 && !qdn.equals(NO_VALUE_STRING)) {
			return qdn + "(" + _objectId + ")";
		}

		ItemType it = getType();
		if (it != null)
			return it.getDisplayName() + ":" + _objectId;

		return getClass().getName() + ":" + _objectId;

	}

	public void setKey(Key newkey) {
		this._dblw.removeItemInKeyMap(this);
		this._key = newkey;
		this._dblw.addItemInKeyMap(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.internal.Item#commitMove(fr.imag.adele.cadse
	 * .core.util.OrderWay, fr.imag.adele.cadse.core.Link,
	 * fr.imag.adele.cadse.core.Link)
	 */
	public boolean commitMove(OrderWay kind, Link l1, Link l2) {
		return false;
	}

	public int getVersion() {
		return _version;
	}

	public void setVersion(int version) {
		this._version = version;
	}

	public int getLastVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isRequireNewRev() {
		return getFlag(Item.EVOL_REQUIRER_NEW_REV);
	}

	public boolean isRevModified() {
		return getFlag(Item.EVOL_REV_MODIFIED);
	}

	public void getLocalAllAttributeTypes(List<IAttributeType<?>> all) {
		if (getType() == null) {
			throw new UnsupportedOperationException("type is undefined");
		}
		getType().getAllAttributeTypes(all);
		if (_group != null)
			_group.getLocalAllAttributeTypes(all);
	}

	public void getLocalAllAttributeTypes(Map<String, IAttributeType<?>> all,
			boolean keepLastAttribute) {
		if (getType() == null) {
			throw new UnsupportedOperationException("type is undefined");
		}
		getType().getAllAttributeTypes(all, keepLastAttribute);

		if (_group != null)
			_group.getLocalAllAttributeTypes(all, keepLastAttribute);
	}

	public void getLocalAllAttributeTypes(List<IAttributeType<?>> all,
			ItemFilter filter) {
		if (getType() == null) {
			throw new UnsupportedOperationException("type is undefined");
		}
		getType().getAllAttributeTypes(all, filter);
		if (_group != null)
			_group.getLocalAllAttributeTypes(all, filter);
	}

	public void getLocalAllAttributeTypes(Map<String, IAttributeType<?>> all,
			boolean keepLastAttribute, ItemFilter filter) {
		if (getType() == null) {
			throw new UnsupportedOperationException("type is undefined");
		}
		getType().getAllAttributeTypes(all, keepLastAttribute, filter);
		if (_group != null)
			_group.getLocalAllAttributeTypes(all, keepLastAttribute, filter);
	}

	public void getLocalAllAttributeTypesKeys(Set<String> all, ItemFilter filter) {
		if (getType() == null) {
			throw new UnsupportedOperationException("type is undefined");
		}
		getType().getAllAttributeTypesKeys(all, filter);
		if (_group != null)
			_group.getLocalAllAttributeTypesKeys(all, filter);
	}

	public IAttributeType<?> getLocalAttributeType(String attName) {
		if (getType() == null) {
			throw new UnsupportedOperationException("type is undefined");
		}
		IAttributeType<?> ret = getType().getAttributeType(attName);
		if (ret != null)
			return ret;
		if (_group != null)
			ret = _group.getLocalAttributeType(attName);
		return ret;
	}
	
	@Override
	public IAttributeType<?> getLocalAttributeType(UUID attrName) {
		Item ret = _dblw.getItem(attrName);
		return (IAttributeType<?>) ret;
	}
	

	public IAttributeType<?>[] getLocalAllAttributeTypes() {
		if (_group != null) {
			HashSet<IAttributeType<?>> ret = new HashSet<IAttributeType<?>>();
			ret.addAll(Arrays.asList(getType().getAllAttributeTypes()));
			ret.addAll(Arrays.asList(_group.getLocalAllAttributeTypes()));
			return (IAttributeType<?>[]) ret.toArray(new IAttributeType<?>[ret
					.size()]);
		}
		return getType().getAllAttributeTypes();
	}

	@Override
	public Item getBaseItem() {
		return this;
	}

	static public <T> T castItem(Item item, Class<T> classItem)
			throws CadseException {
		if (classItem.isInstance(item))
			return (T) item;
		throw new CadseException("Cannot cast item {0} of type {1} to {2}.",
				item.getName(), item.getType() == null ? "??" : item.getType()
						.getDisplayName(), classItem.getSimpleName());
	}

	@Override
	public void setParent(Item parent, LinkType lt) {
		if (parent != null && !parent.isResolved())
			throw new CadseIllegalArgumentException(
					"Cannot set unresolved parent", this, parent);
		if (parent != null && parent instanceof ItemTypeItemDeltaAdapter)
			throw new CadseIllegalArgumentException(
					"Bad type of parent : {0} ", parent.getClass());
		_parent = parent;
	}

	@Override
	public boolean isMember() {
		return _group != null;
	}

	@Override
	public boolean isMemberOf(Item item) {
		return _group == item || (_group != null && _group.isMemberOf(item));
	}

	@Override
	public ItemType getGroup() {
		return _group;
	}

	@Override
	public List<LinkType> getInstanceOutgoingLinkTypes() {
		return getType().getOutgoingLinkTypes();
	}

	@Override
	public Pages getModificationPages(FilterContext context) {
		return PageRuntimeModel.INSTANCE.getModificationPages(this, context);
	}

	@Override
	public Pages getCreationPages(NewContext context) throws CadseException {
		return PageRuntimeModel.INSTANCE.getCreationPages(this, context);
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
	public int getIdInPackage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ItemType getType() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ItemType[] getTypes() {
		ItemType type = getType();
		if (type == null && _group == null)
			return ItemTypeImpl.NO_SUB_TYPES;
		if (this instanceof ItemType && ((ItemType)this).isGroupType() ) {
			if (_group == null && type != null)
				return new ItemType[] { type, CadseGCST.ITEM_TYPE };
			if (_group != null && type != null)
				return new ItemType[] { type, _group, CadseGCST.ITEM_TYPE };
		} else {
			if (_group == null && type != null)
				return new ItemType[] { type, CadseGCST.ITEM_TYPE };
			if (_group != null && type != null)
				return new ItemType[] { type, _group, CadseGCST.ITEM_TYPE };
		}
		return ItemTypeImpl.NO_SUB_TYPES;
		
	}

	@Override
	public void setCadse(CadseRuntime cr) {
		_cadse = cr;
	}

	public boolean isProxy() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ContentItem#getExporters()
	 */
	public Exporter[] getExporters() {
		if (_exporters == null) {
			return NO_EXPORTER;
		}
		return _exporters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ContentItem#getExporter(java.lang.String)
	 */
	public Exporter[] getExporter(String exporterType) {
		Exporter[] ex = getExporters();
		List<Exporter> ret = new ArrayList<Exporter>();
		for (int i = 0; i < ex.length; i++) {
			if (ex[i].containsExporterType(exporterType)) {
				ret.add(ex[i]);
			}
		}
		return ret.toArray(new Exporter[ret.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.ContentItem#setExporters(fr.imag.adele.cadse
	 * .core.build.Exporter)
	 */
	public void setExporters(Exporter... exporters) {
		if (exporters.length == 0) {
			_exporters = NO_EXPORTER;
			return;
		}
		this._exporters = exporters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ContentItem#getComposers()
	 */
	public Composer[] getComposers() {
		if (_composers == null) {
			return NO_COMPOSER;
		}
		return _composers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.ContentItem#setComposers(fr.imag.adele.cadse
	 * .core.build.Composer)
	 */
	public void setComposers(Composer... composers) {
		if (composers.length == 0) {
			_composers = NO_COMPOSER;
			return;
		}
		this._composers = composers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.ContentItem#clean(fr.imag.adele.cadse.core.build
	 * .IBuildingContext, boolean)
	 */
	public void clean(IBuildingContext context, boolean componentsContent) {
		if (componentsContent) {
			for (Composer composer : getComposers()) {
				composer.clean(context);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.ContentItem#build(fr.imag.adele.cadse.core.build
	 * .IBuildingContext)
	 */
	public void build(IBuildingContext context) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.ContentItem#compose(fr.imag.adele.cadse.core
	 * .build.IBuildingContext)
	 */
	public void compose(IBuildingContext context) {
		for (Composer composer : getComposers()) {
			try {
				composer.compose(context);
			} catch (Throwable e) {
				e.printStackTrace();
				context.report("Error in composition {0}", e.getMessage());
			}
		}
	}

}
