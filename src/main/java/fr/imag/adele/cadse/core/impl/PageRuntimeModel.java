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
import fr.imag.adele.cadse.core.impl.internal.ui.HierachicPageImpl;
import fr.imag.adele.cadse.core.impl.internal.ui.PagesImpl;
import fr.imag.adele.cadse.core.ui.AbstractUIRunningValidator;
import fr.imag.adele.cadse.core.ui.IPage;
import fr.imag.adele.cadse.core.ui.Pages;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.ui.UIRunningValidator;
import fr.imag.adele.cadse.core.ui.UIValidator;
import fr.imag.adele.cadse.core.ui.view.FilterContext;
import fr.imag.adele.cadse.core.ui.view.NewContext;
import fr.imag.adele.cadse.util.ArraysUtil;

public class PageRuntimeModel {
	public static final IPage[] EMPTY_PAGE = new IPage[0];
	public static PageRuntimeModel INSTANCE = new PageRuntimeModel();

	public Pages lastCreationPages = null;
	
	public Pages getModificationPages(Item item, FilterContext context) {
		context.setItem(item);
		
		Set<IAttributeType<?>> ro = new HashSet<IAttributeType<?>>();
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

	public Pages getCreationPages(Item item, NewContext context)
			throws CadseException {
		context.setItem(item);
		
		Set<IAttributeType<?>> ro = new HashSet<IAttributeType<?>>();
		context.setDefaultName(item.getType().getDefaultInstanceName());
		List<UIValidator> validators = new ArrayList<UIValidator>();
		iComputeValidators(item, context, validators);
		HashSet<GroupOfAttributes> groups = new HashSet<GroupOfAttributes>();
		iComputeGroup(item, groups);
		IPage[] pages = iGetAllCreationPage(item, context, ro);
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
			Set<IAttributeType<?>> ro) {
		List<IPage> list = new ArrayList<IPage>();
		iComputeCreationPage(item, context, list, ro);
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
			List<IPage> list, Set<IAttributeType<?>> ro) {
		iRecurcifComputeCreationPage(item, context, list, ro);
		HashSet<IAttributeType<?>> inSpecificPages = new HashSet<IAttributeType<?>>();
		for (IPage iPage : list) {
			inSpecificPages.addAll(Arrays.asList(iPage.getAttributes()));
			inSpecificPages.addAll(Arrays.asList(iPage.getHiddenAttributes()));
		}

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
		HierachicPageImpl genericPage = new HierachicPageImpl(item.getType(),
				modificationPage);
		Set<TypeDefinition> visited = new HashSet<TypeDefinition>();
		item.getType().computeGenericPage(context,
				genericPage, inSpecificPages, ro, visited, CadseGCST.ITEM_at_NAME_);
		list.add(0, genericPage);
		ItemType group = item.getGroup();
		if (group instanceof ItemTypeItemDeltaAdapter) {
			group = (ItemType) ((ItemTypeItemDeltaAdapter) group).delta().getBaseItem();
		}
		genericPage = new HierachicPageImpl(group,
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
	
//	static class TypeOrder {
//		ItemType it;
//		int count;
//		int index;
//		TypeOrder[] parents;
//	}
	
//	protected TypeDefinition[] computeTypeOrder(Item item) {
//		HashMap<ItemType, TypeOrder> maps = new HashMap<ItemType, TypeOrder>();
//		TypeOrder[] instanceTypeOrder = compute(maps, item.getType());
//		if (item.getGroup() != null)
//			instanceTypeOrder = ArraysUtil.addList(TypeOrder.class, instanceTypeOrder, compute(maps, item.getGroup()));
//		
//		ArrayList<TypeDefinition> ret = new ArrayList<TypeDefinition>();
//		for (int i = 0; i < instanceTypeOrder.length; i++) {
//			compute(ret, instanceTypeOrder[i]);
//		}
//	}
//
//	private void compute(TypeOrder ... typeOrder ) {
//		ArrayList<TypeOrder> v;
//		ArrayList<TypeOrder> next;
//		int index = 0;
//		
//	}
//
//	private TypeOrder[] compute(HashMap<ItemType, TypeOrder> maps, ItemType... types) {
//		if (types.length == 0)
//			return null;
//		if (types.length == 1 && types[0] == null)
//			return null;
//		int i = 0;
//		TypeOrder[] retArray = new TypeOrder[types.length];
//		for (ItemType type : types) {
//			TypeOrder ret = maps.get(type);
//			if (ret == null) {
//				ret = new TypeOrder();
//				ret.it = type;
//				ret.count = 1;
//				ret.parents = compute(maps, type.getSuperType());
//				maps.put(type, ret);
//			} else {
//				ret.count++;
//				compute(maps, type.getSuperType());
//			}
//			retArray[i++] = ret;
//		}
//		
//		return retArray;
//	}

	protected Map<IAttributeType<?>, UIField> iComputeFields(Item item, IPage[] pages) {
		Map<IAttributeType<?>, UIField> fiedls = new HashMap<IAttributeType<?>, UIField>();
		HashSet<IAttributeType<?>> localAllAttributeTypes = new HashSet<IAttributeType<?>>(Arrays.asList(item.getLocalAllAttributeTypes()));
		for (IPage p : pages) {
			localAllAttributeTypes.addAll(Arrays.asList(p.getAttributes()));
		}
		
		for (IAttributeType<?> att : localAllAttributeTypes) {
			UIField f = iFindField(item, att);
			if (f == null)
				f = att.generateDefaultField();
			if (f != null)
				fiedls.put(att, f);
		}
		return fiedls;
	}

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
