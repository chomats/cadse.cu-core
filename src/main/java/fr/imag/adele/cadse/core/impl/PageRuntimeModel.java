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

public class PageRuntimeModel {
	public static final IPage[] EMPTY_PAGE = new IPage[0];
	public static PageRuntimeModel INSTANCE = new PageRuntimeModel();

	public Pages lastCreationPages = null;
	
	public Pages getModificationPages(Item item, FilterContext context) {
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
		((TypeDefinition.Internal) item.getType()).computeGroup(groups);
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

		HierachicPageImpl genericPage = new HierachicPageImpl(item.getType(),
				false);
		iComputeGenericPage(item, context, genericPage, inSpecificPages, ro);
		list.add(0, genericPage);
	}

	protected void iComputeModificationPage(Item item, FilterContext context,
			List<IPage> list, Set<IAttributeType<?>> ro) {
		iRecurcifComputeModificationPage(item, context, list, ro);
		HashSet<IAttributeType<?>> inSpecificPages = new HashSet<IAttributeType<?>>();
		for (IPage iPage : list) {
			inSpecificPages.addAll(Arrays.asList(iPage.getAttributes()));
			inSpecificPages.addAll(Arrays.asList(iPage.getHiddenAttributes()));
		}

		HierachicPageImpl genericPage = new HierachicPageImpl(item.getType(),
				true);
		iComputeGenericPage(item, context, genericPage, inSpecificPages, ro);
		list.add(0, genericPage);
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
		((TypeDefinition.Internal) item.getType()).recurcifComputeCreationPage(
				context, list);
		for (IPage page : list) {
			ro.addAll(Arrays.asList(page.getReadOnlyAttributes()));
		}
	}

	protected void iRecurcifComputeModificationPage(Item item,
			FilterContext context, List<IPage> list, Set<IAttributeType<?>> ro) {
		((TypeDefinition.Internal) item.getType())
				.recurcifComputeModificationPage(context, list, ro);
	}

	protected void iComputeGenericPage(Item item, FilterContext context,
			HierachicPageImpl genericPage,
			HashSet<IAttributeType<?>> inSpecificPages,
			Set<IAttributeType<?>> ro) {
		((TypeDefinition.Internal) item.getType()).computeGenericPage(context,
				genericPage, inSpecificPages, ro, CadseGCST.ITEM_at_NAME_);
	}

	protected void iComputeValidators(Item item, FilterContext context,
			List<UIValidator> validators) {
		((TypeDefinition.Internal) item.getType()).computeValidators(context,
				validators);
	}

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
		
		return ret;
	}

	protected List<UIRunningValidator> createRunning(
			List<UIValidator> validators) {
		ArrayList<UIRunningValidator> ret = new ArrayList<UIRunningValidator>();
		for (UIValidator v : validators) {
			AbstractUIRunningValidator rv = (AbstractUIRunningValidator) v
					.create();
			rv._desc = v;
			ret.add(rv);
		}
		return ret;
	}
}
