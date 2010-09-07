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
 *
 * Copyright (C) 2006-2010 Adele Team/LIG/Grenoble University, France
 */
package fr.imag.adele.cadse.core.impl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import fr.imag.adele.cadse.core.CadseDomain;
import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.GenContext;
import fr.imag.adele.cadse.core.IItemManager;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemFilter;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.WSModelState;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.internal.CadseDomainImpl;
import fr.imag.adele.cadse.core.iter.ItemIterable;
import fr.imag.adele.cadse.core.iter.ItemPartIterable;
import fr.imag.adele.cadse.core.oper.WSOperation;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransaction;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.var.ContextVariable;
import fr.imag.adele.cadse.objectadapter.ObjectAdapter;
import fr.imag.adele.cadse.util.ArraysUtil;

/**
 * The Class CadseCore.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class CadseCore {

	/** The type of any item. */
	static public ItemType theItem = null;

	/** The item type : the type of any item type. */
	static public ItemType theItemType = null;

	/** The link type of any link type. */
	static public LinkType theLinkType = null;

	public static ItemType theExtendedType = null;
	
	private static Map<String, IAttributeType<?>> _oldname = new HashMap<String, IAttributeType<?>>();
	

	/**
	 * Gets the name.
	 * 
	 * @param wrapper
	 *            the wrapper
	 * @param name
	 *            the name
	 * @param parent
	 *            the parent
	 * @param lt
	 *            the lt
	 * 
	 * @return the name
	 */
	public static String getName(Item wrapper, String name, Item parent,
			LinkType lt) {
		IItemManager im = wrapper.getType().getItemManager();
		return im.computeQualifiedName(wrapper, name, parent, lt);
	}

	/**
	 * Sets the name.
	 * 
	 * @param wrapper
	 *            the wrapper
	 * @param shortName
	 *            the short name
	 * @param parent
	 *            the parent
	 * @param lt
	 *            the lt
	 * 
	 * @throws CadseException
	 *             the melusine exception
	 * @throws MelusineUnModifiedAttribute
	 *             *
	 * @throws CadseIllegalArgumentException
	 *             the melusine error
	 */

	public static void setName(Item wrapper, String shortName, Item parent,
			LinkType lt) throws CadseIllegalArgumentException, CadseException {
		wrapper.setName(shortName);
		if (wrapper.getType().hasQualifiedNameAttribute()) {
			String uniqueName = getName(wrapper, shortName, parent, lt);
			wrapper.setQualifiedName(uniqueName);
		}
	}

	/**
	 * Sets the name.
	 * 
	 * @param createdItem
	 *            the created item
	 * @param name
	 *            the name
	 * 
	 * @throws CadseIllegalArgumentException
	 *             the melusine error
	 * @throws CadseException
	 *             the melusine exception
	 */
	public static void setName(Item createdItem, String name)
			throws CadseException {
		Item parent = createdItem.getPartParent(false);
		LinkType lt = createdItem.getPartParentLinkType();
		setName(createdItem, name, parent, lt);
	}

	/**
	 * Gets the workspace domain.
	 * 
	 * @return the workspace domain
	 */
	static public CadseDomain getCadseDomain() {
		return getInstance();
	}

	/**
	 * Gets the item.
	 * 
	 * @param uniqueName
	 *            the unique name
	 * @param shortname
	 *            the shortname
	 * @param it
	 *            the it
	 * @param parent
	 *            the parent
	 * @param lt
	 *            the lt
	 * 
	 * @return the item
	 * 
	 * @throws CadseException
	 *             the melusine exception
	 */
	public static Item getItem(String uniqueName, String shortname,
			ItemType it, Item parent, LinkType lt) throws CadseException {

		LogicalWorkspace wl = getLogicalWorkspace();
		if (it.hasQualifiedNameAttribute() == false) {
			throw new CadseException(
					"Cannot get an item from unique name when itemtype {0} has no unique name attribute",
					it.getName());
		}

		if (uniqueName == null) {
			LogicalWorkspaceTransaction copy = wl.createTransaction();
			Item newItem = copy.createItem(it, parent, lt);
			uniqueName = getName(newItem, shortname, parent, lt);
		}
		Item findItem = wl.getItem(uniqueName);
		return findItem;
	}

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
	public static Item createItemIfNeed(String uniqueName, String shortname,
			ItemType it, Item parent, LinkType lt, Object... attributes)
			throws CadseException {
		LogicalWorkspace wl = getLogicalWorkspace();

		LogicalWorkspaceTransaction copy = wl.createTransaction();
		ItemDelta newItem = copy.createItemIfNeed(uniqueName, shortname, it,
				parent, lt, attributes);
		UUID id = newItem.getId();
		if (newItem.isAdded()) {
			copy.commit();
		} else {
			Item ret = newItem.getBaseItem();
			if (ret != null) {
				return ret;
			}
		}
		return wl.getItem(id);
	}

	/**
	 * Retourne le premier linktype venant de source vers dest. La recherche
	 * s'effectue � partir de dest et non � partir de source.
	 * 
	 * @param dest
	 *            La destination de la relation recherchee
	 * @param source
	 *            La source la relation recherchee
	 * 
	 * @return Le premier linttype or null.
	 */
	public static LinkType getIncomingLingType(ItemType dest, ItemType source) {
		for (LinkType lt : dest.getIncomingLinkTypes()) {
			if (lt.getSource().equals(source)) {
				return lt;
			}
		}
		return null;
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
	public static Link createLinkIfNeed(Item item, Item dest, LinkType lt)
			throws CadseException {
		Link ret;

		ret = item.getOutgoingLink(lt, dest.getId());
		if (ret != null) {
			return ret;
		}

		ret = item.createLink(lt, dest);
		return ret;
	}

	/**
	 * Gets the single instance of CadseCore.
	 * 
	 * @return single instance of CadseCore
	 */
	public static CadseDomainImpl getInstance() {
		return CadseDomainImpl.getInstance();
	}

	/**
	 * Gets the workspace logique.
	 * 
	 * @return the workspace logique
	 */
	public static LogicalWorkspace getLogicalWorkspace() {
		if (getInstance() == null) {
			return null;
		}
		return getInstance().getLogicalWorkspace();
	}

	 /**
	 * Load from persistence.
	 *
	 * @param url
	 * the url
	 *
	 * @return the item description
	 *
	 * @throws IOException
	 * Signals that an I/O exception has occurred.
	 */
	 public static ItemDelta[] loadFromPersistence(LogicalWorkspaceTransaction
	 transaction, Map<UUID, URL> urls) throws CadseException {
		 if (getInstance() == null) {
			 return null;
		 }
		 return ((CadseDomainImpl)  getInstance()).getPersistence().loadFromPersistence(transaction, urls);
	 }

	/**
	 * Sets the item persistence id.
	 * 
	 * @param projectName
	 *            the project name
	 * @param item
	 *            the item
	 * 
	 * @throws CadseException
	 *             the melusine exception
	 */
	public static void setItemPersistenceID(String projectName, Item item)
			throws CadseException {
		if (getInstance() == null) {
			return;
		}
		((CadseDomainImpl) getInstance()).getIdeService().setItemPersistenceID(
				projectName, item);
	}

	/**
	 * Copy resource.
	 * 
	 * @param item
	 *            the item
	 * @param path
	 *            the path
	 * @param data
	 *            the data
	 * 
	 * @throws CadseException
	 *             the melusine exception
	 */
	public static void copyResource(Item item, String path, URL data)
			throws CadseException {
		if (getInstance() == null) {
			return;
		}
		((CadseDomainImpl) getInstance()).getIdeService().copyResource(item,
				path, data);
	}

	/**
	 * register l'operation avec le service de test. cette methode est
	 * captur�.
	 * 
	 * @param oper
	 */
	public static void registerInTestIfNeed(WSOperation oper) {
		// TODO Auto-generated method stub

	}

	/**
	 * parcours tout les types et regard ceux qui ont pour supertype itemType
	 * 
	 * @param itemType
	 * @return les sous type suivant la relation supertype On pourrai uliiser le
	 *         niveau meta
	 */
	public static ItemType[] getSubTypes(ItemType itemType) {
		// Collection<ItemType> itemTypes =
		// getWorkspaceLogique().getItemTypes();
		ArrayList<ItemType> subTypes = new ArrayList<ItemType>();
		for (ItemType anIt : subTypes) {
			if (anIt.getSuperType() == itemType) {
				subTypes.add(anIt);
			}
		}
		return subTypes.toArray(new ItemType[subTypes.size()]);
	}

	public static boolean isStopped() {
		return CadseDomainImpl.STOPPED;
	}

	public static boolean isStarted() {
		return CadseDomainImpl.STARTED;
	}
	
	/**
	 * Return true if the cadse runtime is started, loaded and running
	 * @return
	 */
	public static boolean isCadseRuntimeRunning() {
		return CadseDomainImpl.STARTED && CadseGCST.ITEM != null && getLogicalWorkspace() != null
		  && getLogicalWorkspace().getState() == WSModelState.RUN;
	}

	public static Map<String, IAttributeType<?>> getOldNameMap() {
		return _oldname;
	}

	
	public static IAttributeType<?> findAttributeFrom(ItemType it,
			String attName) {
		while (it != null) {
			IAttributeType<?> ret = null;
			
			ret = _oldname.get(it.getId()+"::"+attName);
			if (ret != null) {
				return ret;
			}
			ret = _oldname.get(it.getName()+"::"+attName);
			if (ret != null) {
				return ret;
			}
			ret = _oldname.get(attName);
			if (ret != null) {
				return ret;
			}
			if (_oldNameREs != null) {
				for (OldNameRE onr : _oldNameREs) {
					if ((onr._typeRe == null || Pattern.matches(onr._typeRe, it.getName())) &&
							(onr._attRe == null || Pattern.matches(onr._attRe, attName))) {
						return onr._att;
					}
							
				}
			}
			it = it.getSuperType();
		}
		return _oldname.get(attName);
	}

	public static boolean ignoreAttribute(ItemType it, String attName) {
		if (_ignoreCompiled == null && _ignore != null)
			_ignoreCompiled = Pattern.compile(_ignore);
		if (_ignoreCompiled == null)
			return false;
		
		while (it != null) {
			if (_ignoreCompiled.matcher((it.getId()+"::"+attName)).matches())
				return true;
			if (_ignoreCompiled.matcher((it.getName()+"::"+attName)).matches())
				return true;
			it = it.getSuperType();
		}
		return _ignoreCompiled.matcher(attName).matches();		
	}
	
	static String _ignore = null;
	static Pattern _ignoreCompiled = null;

	public static void addIgnore(String s) {
		if (_ignore == null)
			_ignore = Pattern.quote(s);
		else
			_ignore = _ignore+"|"+Pattern.quote(s);
		_ignoreCompiled = null;
	}
	
	public static void addIgnorePattern(String p) {
		if (_ignore == null)
			_ignore = p;
		else
			_ignore = _ignore+"|"+p;
		_ignoreCompiled = null;
	
	}

	public static boolean ignoreType(UUID type) {
		if (_ignoreCompiled == null)
			_ignoreCompiled = Pattern.compile(_ignore);
		return _ignoreCompiled.matcher(type.toString()).matches();		
	}
	
	static OldNameRE[] _oldNameREs = null;

	static class OldNameRE {
		String _typeRe;
		String _attRe;
		IAttributeType<?> _att;
	}
	public static void addOldNameRE(String typeRe, String attRe,
			IAttributeType<?> att) {
		
		OldNameRE  oldNameRE = new OldNameRE();
		oldNameRE._typeRe =typeRe;
		oldNameRE._attRe = attRe;
		oldNameRE._att = att;
		_oldNameREs = ArraysUtil.add(OldNameRE.class, _oldNameREs, oldNameRE);
	}

	public static boolean inDevelopmentMode() {
		final CadseDomain cadseDomain = getCadseDomain();
		if (cadseDomain == null) return false;
		return cadseDomain.inDevelopmentMode();
	}
	
	static public <T extends ObjectAdapter<T>> void adapts(Item currentItem,
			ContextVariable context,
			Class<T> clazz, Set<T> adapters, ItemFilter<T> filter) {
		ItemIterable iter = null;
		iter = currentItem.getType().adapt(ItemIterable.class);
		if (iter == null)
			iter = new ItemPartIterable();
		
		adapts(currentItem, context, iter, clazz, adapters, filter);
	}

	static public <T extends ObjectAdapter<T>> void adapts(Item currentItem, 
			ContextVariable context, ItemIterable giter,
			Class<T> clazz, Set<T> adapters, ItemFilter<T> filter) {
		giter.beginAll(currentItem, context);
		for (Item anItem : giter) {
			T[] producteurs = anItem.getType().adapts(clazz);

			for (int i = 0; i < producteurs.length; i++) {
				T p = producteurs[i];
				
				if (filter != null && !filter.accept(p))
					continue;
				adapters.add(p);
			}
		}
		giter.endAll(currentItem, context);

	}
	
	public interface AdapterCalleble<T extends ObjectAdapter<T>> {
		
		void call(Item currentItem, T adapter);
	}
	
	static public <T extends ObjectAdapter<T>> void callAdapts(Item currentItem,
			ContextVariable context,
			Class<T> clazz, AdapterCalleble<T> call, ItemFilter<T> filter) throws CadseException {
		ItemIterable iter = null;
		iter = currentItem.getType().adapt(ItemIterable.class);
		if (iter == null)
			iter = new ItemPartIterable();
		
		callAdapts(currentItem, context, iter, clazz, call, filter);
	}
	
	static public <T extends ObjectAdapter<T>> void callAdapts(Item currentItem, 
			ContextVariable context, ItemIterable giter,
			Class<T> clazz, AdapterCalleble<T> call, ItemFilter<T> filter) throws CadseException {
		List<Throwable> errors = new ArrayList<Throwable>();
		giter.beginAll(currentItem, context);
		for (Item anItem : giter) {
			T[] producteurs = anItem.getType().adapts(clazz);

			for (int i = 0; i < producteurs.length; i++) {
				T p = producteurs[i];
				
				if (filter != null && !filter.accept(p))
					continue;
				try {
					call.call(anItem, p);
				} catch (Throwable e) {
					errors.add(e);
				}
			}
		}
		giter.endAll(currentItem, context);
		if (!errors.isEmpty()) {
			throw new CadseException("Errors in call adapter", (Throwable[]) errors.toArray(new Throwable[errors.size()]));
		}

	}
	
	static public class AdapterEntry<T extends ObjectAdapter<T>> {
		
		public AdapterEntry(Item currentItem, T adapter) {
			super();
			this.currentItem = currentItem;
			this.adapter = adapter;
		}
		
		public final T adapter;
		public final Item currentItem;
		
		
		public T getAdapter() {
			return adapter;
		}
		
		public Item getCurrentItem() {
			return currentItem;
		}
	}
	
	static public <T extends ObjectAdapter<T>> void adapts(Item currentItem,
			ContextVariable context,
			Class<T> clazz, List<AdapterEntry<T>> adapters, ItemFilter<T> filter) {
		ItemIterable iter = null;
		iter = currentItem.getType().adapt(ItemIterable.class);
		if (iter == null)
			iter = new ItemPartIterable();
		
		adapts(currentItem, context, iter, clazz, adapters, filter);
	}
	
	static public <T extends ObjectAdapter<T>> void adapts(Item currentItem, 
			ContextVariable context, ItemIterable giter,
			Class<T> clazz, List<AdapterEntry<T>> adatpters, ItemFilter<T> filter)  {
		giter.beginAll(currentItem, context);
		for (Item anItem : giter) {
			T[] producteurs = anItem.getType().adapts(clazz);

			for (int i = 0; i < producteurs.length; i++) {
				T p = producteurs[i];
				
				if (filter != null && !filter.accept(p))
					continue;
					adatpters.add(new AdapterEntry<T>(anItem, p));
			}
		}
		giter.endAll(currentItem, context);
	}

	public static <T extends ObjectAdapter<T>> T adapt(Item currentItem,
			Class<T> clazz, ItemFilter<T> filter) {
		
		T[] producteurs = currentItem.getType().adapts(clazz);

		for (int i = 0; i < producteurs.length; i++) {
			T p = producteurs[i];
			
			if (filter != null && !filter.accept(p))
				continue;
			
			return p;
		}
		return null;
	}

}
