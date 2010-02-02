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
/*
 * Adele/LIG/ Grenoble University, France
 * 2006-2008
 */
package fr.imag.adele.cadse.core.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.GenContext;
import fr.imag.adele.cadse.core.GenStringBuilder;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.build.Composer;
import fr.imag.adele.cadse.core.build.Exporter;
import fr.imag.adele.cadse.core.build.IBuildingContext;
import fr.imag.adele.cadse.core.content.ContentItem;
import fr.imag.adele.cadse.core.impl.internal.AbstractGeneratedItem;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.impl.db.DBLogicalWorkspace;
import fr.imag.adele.cadse.core.impl.internal.AbstractGeneratedItem;
import fr.imag.adele.cadse.core.var.ContextVariable;
import fr.imag.adele.cadse.core.var.ContextVariableImpl;
import fr.imag.adele.cadse.util.ArraysUtil;

/**
 * This class manages the build of the derived content of an item, in order to
 * keep updated a version of this content based on modifications to the item,
 * its components or its proper content.
 * 
 * Item derived contents are used to compose items in other composite projects.
 * 
 * @author vega
 * 
 * Mapping eclipse...: Plusieur mapping par item. type create
 * FILE/FOLDER/PROJECT/ PART_OF_FILE /MULTI_PART_OF_FILE/MULTI_FILE/AUTRE Extra
 * type JAVA|AJ|PDE|RESOURCE
 * 
 * name pattern paramettres : qualified-id, short-id, qualified-parent-id,
 * short-parent-id format example message format. cas complexe extends method.
 * Dans le cas d'un FILE ou FOLDER Recuperer le container parent, donner un
 * chiffre 0, 1, 2 pour savoir � partir de quels item prendre le container. Il
 * faut s'appuyer sur la relation inverse de part pour descendre la chaine des
 * parents...
 */
public abstract class ContentItemImpl extends AbstractGeneratedItem implements ContentItem {

	/** The N o_ children. */
	static final ContentItem[]	NO_CHILDREN	= new ContentItem[0];

	/** The item. */
	private 	 Item			_ownerItem;

    private ContentItem[]		_children;
	private ContentItem[]		_childrenFromParent;
	
	
	
	protected ContentItemImpl(UUID id) {
		super(id, 0);
		_ownerItem = null;
		_parent = null;
	}
	
	protected ContentItemImpl(int objectId) {
		super(objectId, 0);
		_ownerItem = null;
		_parent = null;
	}

	@Override
	public Link commitLoadCreateLink(LinkType lt, Item destination)
			throws CadseException {
		if (lt == CadseGCST.CONTENT_ITEM_lt_OWNER_ITEM) {
			_ownerItem = destination;
			return new ReflectLink(lt, this, destination, 0);
		}
		return super.commitLoadCreateLink(lt, destination);
	}

//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see fr.imag.adele.cadse.core.ContentItem#getItem()
//	 */
//	@Deprecated
//	final public Item getItem() {
//		return _ownerItem;
//	}
	
	final public Item getOwnerItem() {
		return _ownerItem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ContentItem#init()
	 */
	public void init() throws CadseException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ContentItem#dispose()
	 */
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ContentItem#clean(fr.imag.adele.cadse.core.build.IBuildingContext,
	 *      boolean)
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
	 * @see fr.imag.adele.cadse.core.ContentItem#build(fr.imag.adele.cadse.core.build.IBuildingContext)
	 */
	public void build(IBuildingContext context) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ContentItem#compose(fr.imag.adele.cadse.core.build.IBuildingContext)
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

	// **********
	// mapping
	// **********

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ContentItem#getResources()
	 */
	@Deprecated
	public Object[] getResources() {
		return new Object[] { getMainResource() };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ContentItem#getMainMappingContent(java.lang.Class,
	 *      java.lang.Object)
	 */
	public <T> T getMainMappingContent(Class<T> clazz) {
		return getMainMappingContent(ContextVariableImpl.DEFAULT, clazz);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ContentItem#getResources(java.lang.String)
	 */
	@Deprecated
	public abstract Object[] getResources(String kind);

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ContentItem#setResources(java.lang.String,
	 *      java.lang.Object[])
	 */
	public abstract void setResources(String kind, Object[] resource) throws CadseException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ContentItem#getMainResource()
	 */
	public abstract Object getMainResource();

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ContentItem#getKindsResource()
	 */
	public abstract String[] getKindsResource();

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ContentItem#create()
	 */
	public abstract void create() throws CadseException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ContentItem#delete()
	 */
	public abstract void delete() throws CadseException;

	

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ContentItem#getParentPartContentManager(boolean)
	 */
	public ContentItem getParentPartContentManager(boolean includeContainers) {
		if (this._parent != null) {
			return (ContentItem) this._parent;
		}

		ContentItem cm;
		Item parentItem = getOwnerItem().getPartParent(false);
		if (parentItem == null) {
			return null;
		}
		if (parentItem.getContentItem() != null) {
			return parentItem.getContentItem();
		}
		for (Link l : getOwnerItem().getIncomingLinks()) {
			if (l.getLinkType().isPart() && l.getSource().getContentItem() != null) {
				return l.getSource().getContentItem();
			}
		}

		if (!includeContainers) {
			return parentItem.getContentItem();
		}

		cm = null;
		while (cm == null && parentItem != null) {
			cm = parentItem.getContentItem();
			parentItem = parentItem.getPartParent(false);
		}
		return cm;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ContentItem#migrateContentManager(fr.imag.adele.cadse.core.delta.ItemDelta,
	 *      fr.imag.adele.cadse.core.var.ContextVariable,
	 *      fr.imag.adele.cadse.core.var.ContextVariable)
	 */
	public void migrateContentItem(ItemDelta ownerItem, ContextVariable newCxt, ContextVariable oldCxt) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ContentItem#commitChangeParentContent(fr.imag.adele.cadse.core.ContentItem)
	 */
	public void setParentContent(ContentItem newParentContentManager) {
		setParent( newParentContentManager , null);
	}

	/**
	 * Gets the parent content manager resource associated with an item.
	 * 
	 * If there is no associated resource and the item is part of another item,
	 * it is possible to request the resource associated with the first
	 * containing ancestor having one.
	 * 
	 * @param includeContainers
	 *            recursif
	 * @param item
	 *            the item
	 * 
	 * @return the value can be null;
	 * 
	 */
	public static ContentItem getParentPartContentManager(LinkType lt, boolean includeContainers, Item item) {

		ContentItem cm;
		Item parentItem = item.getPartParent(lt);
		if (parentItem == null) {
			return null;
		}
		if (!includeContainers) {
			return parentItem.getContentItem();
		}

		cm = null;
		while (cm == null && parentItem != null) {
			cm = parentItem.getContentItem();
			parentItem = parentItem.getPartParent(lt);
		}
		return cm;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ContentItem#getParentContentManager()
	 */
	@Deprecated
	public ContentItem getParentContentManager() {
		if (_parent == null) {
			setParent( getParentPartContentManager(true), null);
		}

		return (ContentItem) _parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ContentItem#getParentPartContentManager()
	 */
	@Deprecated
	public ContentItem getParentPartContentManager() {
		return getParentPartContentManager(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ContentItem#getChildrenContentManager()
	 */
	public ContentItem[] getChildrenContentManager() {
		return NO_CHILDREN;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ContentItem#getChildrenPropreContentManager()
	 */
	public ContentItem[] getChildrenPropreContentManager() {
		return NO_CHILDREN;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ContentItem#generate(fr.imag.adele.cadse.core.GenStringBuilder,
	 *      java.lang.String, java.lang.String, java.util.Set,
	 *      fr.imag.adele.cadse.core.GenContext)
	 */
	public void generate(GenStringBuilder sb, String type, String kind, Set<String> imports, GenContext context) {
	}

	/**
	 * Generate.
	 * 
	 * @param item
	 *            the item
	 * @param sb
	 *            the sb
	 * @param type
	 *            the type
	 * @param kind
	 *            the kind
	 * @param imports
	 *            the imports
	 * @param context
	 *            the context
	 */
	public static void generate(Item item, GenStringBuilder sb, String type, String kind, Set<String> imports,
			GenContext context) {
		ContentItem cm = item.getContentItem();
		if (cm != null) {
			cm.generate(sb, type, kind, imports, context);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ContentItem#generateParts(fr.imag.adele.cadse.core.GenStringBuilder,
	 *      java.lang.String, java.lang.String, java.util.Set,
	 *      fr.imag.adele.cadse.core.GenContext)
	 */
	public void generateParts(GenStringBuilder sb, String type, String kind, Set<String> imports, GenContext context) {
		generateParts(getOwnerItem(), sb, type, kind, imports, context);
	}

	/**
	 * Generate parts.
	 * 
	 * @param item
	 *            the item
	 * @param sb
	 *            the sb
	 * @param type
	 *            the type
	 * @param kind
	 *            the kind
	 * @param imports
	 *            the imports
	 * @param context
	 *            the context
	 */
	public static void generateParts(Item item, GenStringBuilder sb, String type, String kind, Set<String> imports,
			GenContext context) {
		for (Link l : new ArrayList<Link>(item.getOutgoingLinks())) {
			if (l.getLinkType().isPart() && l.isLinkResolved()) {
				ContentItem cm = l.getResolvedDestination().getContentItem();
				if (cm != null) {
					cm.generate(sb, type, kind, imports, context);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ContentItem#generatePart(java.lang.String,
	 *      fr.imag.adele.cadse.core.GenStringBuilder, java.lang.String,
	 *      java.lang.String, java.util.Set,
	 *      fr.imag.adele.cadse.core.GenContext)
	 */
	public void generatePart(String linkID, GenStringBuilder sb, String type, String kind, Set<String> imports,
			GenContext context) {

		Item dest = _ownerItem.getOutgoingItem(linkID, false);
		if (dest != null) {
			ContentItem cm = dest.getContentItem();
			if (cm != null) {
				cm.generate(sb, type, kind, imports, context);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ContentItem#generateParts(java.lang.String,
	 *      fr.imag.adele.cadse.core.GenStringBuilder, java.lang.String,
	 *      java.lang.String, java.util.Set,
	 *      fr.imag.adele.cadse.core.GenContext)
	 */
	public void generateParts(String linkID, GenStringBuilder sb, String type, String kind, Set<String> imports,
			GenContext context) {
		Collection<Item> dests = _ownerItem.getOutgoingItems(linkID, false);
		for (Item aItem : dests) {
			ContentItem cm = aItem.getContentItem();
			if (cm != null) {
				cm.generate(sb, type, kind, imports, context);
			}
		}

	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ContentItem#getResourceName()
	 */
	@Deprecated
	public String getResourceName() {
		return getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ContentItem#setResourceName(java.lang.String)
	 */
	@Deprecated
	public void setResourceName(String resourceName) {
		//this.resourceName = resourceName;
	}

	public ItemType getType() {
		return CadseGCST.CONTENT_ITEM;
	}

	public void setParent(Item parent, LinkType lt) {
		if (parent instanceof ContentItem || parent == null) {
			if (_parent != null) {
				((ContentItem) _parent).removeChild(this);
			}
			_parent =  parent;
			if (_parent != null) {
				((ContentItem) _parent).addChild(this);
			}
		} else {
			throw new IllegalArgumentException("parent not type ContentItem");
		}
	}

	@Override
	public ContentItem getPartParent() {
		return (ContentItem) _parent;
	}
	
	@Override
	public ContentItem getPartParent(boolean attemptToRecreate) {
		super.getPartParent(attemptToRecreate);
		return getPartParent();
	}

	public Collection<ContentItem> getPartChildrenContents() {
		Collection<ContentItem> ret = new ArrayList<ContentItem>();
		for (Link l : getOutgoingLinks()) {
			if (l.isLinkResolved() && l.getLinkType().isPart() && l.getDestination() instanceof ContentItem) {
				ret.add((ContentItem) l.getDestination());
			}
		}
		return ret;
	}
	
	@Override
	public void addChild(ContentItem contentItem) {
		_childrenFromParent = ArraysUtil.add(ContentItem.class, _childrenFromParent, contentItem);
	}
	
	@Override
	public void removeChild(ContentItem contentItem) {
		_childrenFromParent = ArraysUtil.remove(ContentItem.class, _childrenFromParent, contentItem);
	}

	
	
	
	@Override
	public String getName() {
		ItemType type = getType();
		if (type == null ) {
			if (_ownerItem == null)
				return "Content of item ?? (??)";
			else
				return "Content of item "+_ownerItem.getDisplayName()+ " (??)";
		}
		else if (_ownerItem == null) {
			return "Content of item ?? ("+type.getDisplayName()+")";
		}
		return "Content of item "+_ownerItem.getDisplayName()+ " ("+type.getDisplayName()+")";
	}
	
	@Override
	public String getDisplayName() {
		return getName();
	}
	
	@Override
	public boolean isReadOnly() {
		return true;
	}
	
	public void setOwnerItem(Item ownerItem) {
		_ownerItem = ownerItem;
	}
	
	@Override
	public void collectOutgoingLinks(LinkType linkType,
			CollectedReflectLink ret) {
		if (linkType == CadseGCST.CONTENT_ITEM_lt_OWNER_ITEM) {
			ret.addOutgoing(linkType, _ownerItem);
		}
		
		if (linkType == CadseGCST.CONTENT_ITEM_lt_CHILDREN) {
			ret.addOutgoing(linkType, _childrenFromParent);
			ret.addOutgoing(linkType, _children);
		}
		super.collectOutgoingLinks(linkType, ret);
	}
	
	public <T> T getMainMappingContent(ContextVariable cxt, Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

}
