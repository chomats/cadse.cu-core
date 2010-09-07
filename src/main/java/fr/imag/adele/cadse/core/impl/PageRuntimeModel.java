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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.TypeDefinition;
import fr.imag.adele.cadse.core.attribute.GroupOfAttributes;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.internal.delta.ItemTypeItemDeltaAdapter;
import fr.imag.adele.cadse.core.impl.internal.ui.HierachicalPageImpl;
import fr.imag.adele.cadse.core.impl.internal.ui.PagesImpl;
import fr.imag.adele.cadse.core.ui.AbstractUIRunningValidator;
import fr.imag.adele.cadse.core.ui.IPage;
import fr.imag.adele.cadse.core.ui.PageParticipator;
import fr.imag.adele.cadse.core.ui.Pages;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.ui.UIRunningValidator;
import fr.imag.adele.cadse.core.ui.UIValidator;
import fr.imag.adele.cadse.core.ui.view.FilterContext;
import fr.imag.adele.cadse.core.ui.view.NewContext;

/**
 * Compute the pages structure en function du data model et des object (Fiel, validator, page, attribute, pageparticipator)
 * 
 * @author chomats
 *
 */
public class PageRuntimeModel {
	public static final IPage[] EMPTY_PAGE = new IPage[0];
	public static PageRuntimeModel INSTANCE = new PageRuntimeModel();

	public Pages lastCreationPages = null;
	
	public Pages getModificationPages(Item item, FilterContext context) {
		context.setItem(item);
		context.setModificationPages(true);
		
		/*
		 * List des attributs read only
		 */
		Set<IAttributeType<?>> ro = new HashSet<IAttributeType<?>>();
		/*
		 * List des validators
		 */
		List<UIValidator> validators = new ArrayList<UIValidator>();
		iComputeValidators(item, context, validators);

		HashSet<GroupOfAttributes> groups = new HashSet<GroupOfAttributes>();
		iComputeGroup(item, groups);
		IPage[] pages = iGetAllModificationPage(item, context, ro);
		return new PagesImpl(context, true, ((TypeDefinition.Internal) item
				.getType()).createDefaultModificationAction(context),
				iComputeFields(item, pages),
				pages,
				createRunning(validators), ro, groups);
	}

	public Pages getCreationPages(Item item, NewContext context, IAttributeType<?> ...hiddenAttributesInGenericPage)
			throws CadseException {
		context.setItem(item);
		context.setModificationPages(false);
		
		Set<IAttributeType<?>> ro = new HashSet<IAttributeType<?>>();
		context.setDefaultName(item.getType().getDefaultInstanceName());
		List<UIValidator> validators = new ArrayList<UIValidator>();
		iComputeValidators(item, context, validators);
		HashSet<GroupOfAttributes> groups = new HashSet<GroupOfAttributes>();
		iComputeGroup(item, groups);
		IPage[] pages = iGetAllCreationPage(item, context, ro, hiddenAttributesInGenericPage);
		lastCreationPages = new PagesImpl(context, false, ((TypeDefinition.Internal) item
				.getType()).createDefaultCreationAction(context),
				iComputeFields(item, pages), pages,
				createRunning(validators), ro, groups);
		return lastCreationPages;
	}

	private void iComputeGroup(Item item, HashSet<GroupOfAttributes> groups) {
		Set<TypeDefinition> visited = new HashSet<TypeDefinition>();
		((TypeDefinition.Internal) item.getType()).computeGroup(groups, visited);
	}

	/**
	 * Gets the good creation page_.
	 * 
	 * @return the good creation page_
	 */
	protected IPage[] iGetAllModificationPage(Item item, FilterContext context,
			Set<IAttributeType<?>> ro) {
		List<IPage> list = new ArrayList<IPage>();
		iComputeModificationPage(item, context, list, ro);
		int count = list.size();
		for (IPage factory : list) {
			if (factory.isEmptyPage()) {
				count--;
			}
		}
		if (count == 0) {
			return EMPTY_PAGE;
		} else {
			IPage[] creationPages = new IPage[count];
			int i = 0;
			for (IPage factory : list) {
				if (factory.isEmptyPage()) {
					continue;
				}
				creationPages[i++] = factory;
			}
			assert i == count;
			return creationPages;
		}
	}

	/**
	 * Gets the good creation page_.
	 * 
	 * @return the good creation page_
	 */
	protected IPage[] iGetAllCreationPage(Item item, FilterContext context,
			Set<IAttributeType<?>> ro, IAttributeType<?>[] hiddenAttributesInGenericPage) {
		List<IPage> list = new ArrayList<IPage>();
		iComputeCreationPage(item, context, list, ro, hiddenAttributesInGenericPage);
		int count = list.size();
		for (IPage p : list) {
			if (p.isEmptyPage()) {
				count--;
			}
		}
		if (count == 0) {
			return EMPTY_PAGE;
		} else {
			IPage[] creationPages = new IPage[count];
			int i = 0;
			for (IPage p : list) {
				if (p.isEmptyPage()) {
					continue;
				}
				creationPages[i++] = p;
			}
			assert i == count;
			return creationPages;
		}
	}

	/** 
	 * Calcul les pages spécifique à partir d'item et la page calculé.
	 * Return l'information dans list.
	 * 
	 * @param item
	 * @param context
	 * @param list
	 * @param ro
	 */
	protected void iComputeCreationPage(Item item, FilterContext context,
			List<IPage> list, Set<IAttributeType<?>> ro, IAttributeType<?>[] hiddenAttributes) {
		iRecurcifComputeCreationPage(item, context, list, ro);
		HashSet<IAttributeType<?>> inSpecificPages = new HashSet<IAttributeType<?>>();
		inSpecificPages.addAll(Arrays.asList(hiddenAttributes));
		for (IPage iPage : list) {
			inSpecificPages.addAll(Arrays.asList(iPage.getAttributes()));
			inSpecificPages.addAll(Arrays.asList(iPage.getHiddenAttributes()));
		}

		PageParticipator p = item.getType().adapt(PageParticipator.class);
		if (p != null)
			p.filterPage(item, context, list, ro, inSpecificPages);
		
		iComputeGenericPage(item, context, inSpecificPages, ro, list, false);
	}

	protected void iComputeModificationPage(Item item, FilterContext context,
			List<IPage> list, Set<IAttributeType<?>> ro) {
		iRecurcifComputeModificationPage(item, context, list, ro);
		HashSet<IAttributeType<?>> inSpecificPages = new HashSet<IAttributeType<?>>();
		for (IPage iPage : list) {
			inSpecificPages.addAll(Arrays.asList(iPage.getAttributes()));
			inSpecificPages.addAll(Arrays.asList(iPage.getHiddenAttributes()));
		}
		
		PageParticipator p = item.getType().adapt(PageParticipator.class);
		if (p != null)
			p.filterPage(item, context, list, ro, inSpecificPages);
		
		iComputeGenericPage(item, context, inSpecificPages, ro, list, true);
		
	}
	
	/** 
	 * Calcul les pages spécifique à partir d'un instance item et retourn l'information dans list et ro pour les attribut readonly
	 * @param item
	 * @param context
	 * @param list
	 * @param ro
	 */

	protected void iRecurcifComputeCreationPage(Item item,
			FilterContext context, List<IPage> list, Set<IAttributeType<?>> ro) {
		Set<TypeDefinition> visited = new HashSet<TypeDefinition>();
		((TypeDefinition.Internal) item.getType()).recurcifComputeCreationPage(
				context, list, visited);
		
		ItemType group = item.getGroup();
		while (group != null) {
			group.recurcifComputeCreationPage(context, list, visited);
			group = group.getGroup();
		}
		
		for (IPage page : list) {
			ro.addAll(Arrays.asList(page.getReadOnlyAttributes()));
		}
	}

	protected void iRecurcifComputeModificationPage(Item item,
			FilterContext context, List<IPage> list, Set<IAttributeType<?>> ro) {
		Set<TypeDefinition> visited = new HashSet<TypeDefinition>();
		item.getType()
				.recurcifComputeModificationPage(context, list, ro, visited);
		ItemType group = item.getGroup();
		while (group != null) {
			group.recurcifComputeModificationPage(context, list, ro, visited);
			group = group.getGroup();
		}	
	}

	protected void iComputeGenericPage(Item item, FilterContext context,
			HashSet<IAttributeType<?>> inSpecificPages,
			Set<IAttributeType<?>> ro, List<IPage> list, boolean modificationPage) {
		HierachicalPageImpl genericPage = new HierachicalPageImpl(item.getType(),
				modificationPage);
		Set<TypeDefinition> visited = new HashSet<TypeDefinition>();
		item.getType().computeGenericPage(context,
				genericPage, inSpecificPages, ro, visited, CadseGCST.ITEM_at_NAME_);
		list.add(0, genericPage);
		IPage[] blocs = genericPage.getBlocks();
		for (IPage b : blocs) {
			if (b.isEmptyPage()) continue;
			b.setTitle(item.getType().getDisplayName());
			b.setLabel(item.getType().getDisplayName());
			break;
		}
		ItemType group = item.getGroup();
		if (group instanceof ItemTypeItemDeltaAdapter) {
			group = (ItemType) ((ItemTypeItemDeltaAdapter) group).delta().getBaseItem();
		}
		genericPage = new HierachicalPageImpl(group,
				modificationPage);
		genericPage.setGroupPage(true);
		while (group != null) {
			group.computeGenericPage(context,
					genericPage, inSpecificPages, ro, visited);
			group = group.getGroup();
		}
		if (!genericPage.isEmptyPage())
			list.add(1, genericPage);
		
	}

	/**
	 * Compute the validator list in the main type and all the group.
	 * 
	 * @param item
	 * @param context
	 * @param validators
	 */
	protected void iComputeValidators(Item item, FilterContext context,
			List<UIValidator> validators) {
		Set<TypeDefinition> visited = new HashSet<TypeDefinition>();
		item.getType().computeValidators(context,
				validators, visited );
		ItemType group = item.getGroup();
		while (group != null) {
			group.computeValidators(context, validators, visited);
			group = group.getGroup();
		}
	}
	

	protected Map<IAttributeType<?>, UIField> iComputeFields(Item item, IPage[] pages) {
		Map<IAttributeType<?>, UIField> fiedls = new HashMap<IAttributeType<?>, UIField>();
		HashSet<IAttributeType<?>> localAllAttributeTypes = new HashSet<IAttributeType<?>>(Arrays.asList(item.getLocalAllAttributeTypes()));
		for (IPage p : pages) {
			localAllAttributeTypes.addAll(Arrays.asList(p.getAttributes()));
		}
		
		for (IAttributeType<?> att : localAllAttributeTypes) {
			UIField f = iFindField(item, att);
			// if no field is found then create a default field.
			if (f == null)
				f = att.generateDefaultField(); 
			if (f != null)
				fiedls.put(att, f);
		}
		return fiedls;
	}

	/**
	 * Search a specific field form the main type of item or in group.
	 * @param item the point from the field is searched
	 * @param att the att associated for field
	 * @return a specific field or null if not found.
	 */
	protected UIField iFindField(Item item, IAttributeType<?> att) {
		UIField ret = null;
		ItemType type = item.getType();
		ret = type.findField(att);
		ItemType group = item.getGroup();
		while (ret == null && group != null) {
			ret = group.findField(att);
			group = group.getGroup();
		}
		return ret;
	}
	
	/**
	 * Create running validator from model validator.
	 * @param validators
	 * @return
	 */
	protected List<UIRunningValidator> createRunning(
			List<UIValidator> validators) {
		ArrayList<UIRunningValidator> ret = new ArrayList<UIRunningValidator>();
		for (UIValidator v : validators) {
			AbstractUIRunningValidator rv = (AbstractUIRunningValidator) v
					.create();
			if (rv == null) continue;
			rv._desc = v;
			ret.add(rv);
		}
		return ret;
	}
}
