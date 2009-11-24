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
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.internal.TypeDefinition;
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
	public static final IPage[]			EMPTY_PAGE		= new IPage[0];
	public static PageRuntimeModel	INSTANCE = new PageRuntimeModel();
	
	
	
	public Pages getModificationPages(Item item, FilterContext context) {
		Set<IAttributeType<?>> ro = new HashSet<IAttributeType<?>>();
		List<UIValidator> validators = new ArrayList<UIValidator>();
		iComputeValidators(item, context, validators);
		
		return new PagesImpl(context, true, 
				((TypeDefinition) item.getType()).createDefaultModificationAction(context), 
				iComputeFields(item), 
				iGetAllModificationPage(item, context, ro), 
				createRunning(validators), ro);
	}
	
	
	public Pages getCreationPages(Item item, NewContext context) throws CadseException {
		Set<IAttributeType<?>> ro = new HashSet<IAttributeType<?>>();
		context.setDefaultName(item.getType().getDefaultInstanceName());
		List<UIValidator> validators = new ArrayList<UIValidator>();
		iComputeValidators(item, context, validators);
		return new PagesImpl(context, false, 
				((TypeDefinition) item.getType()).createDefaultCreationAction(context), 
				iComputeFields(item), 
				iGetAllCreationPage(item, context, ro), 
				createRunning(validators), ro);
	}
	
	
	/**
	 * Gets the good creation page_.
	 * 
	 * @return the good creation page_
	 */
	protected IPage[] iGetAllModificationPage(Item item, FilterContext context, Set<IAttributeType<?>> ro) {
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
	protected IPage[] iGetAllCreationPage(Item item, FilterContext context, Set<IAttributeType<?>> ro) {
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
	
	protected void iComputeCreationPage(Item item, FilterContext context, List<IPage> list, Set<IAttributeType<?>> ro) {
		iRecurcifComputeCreationPage(item, context, list, ro);
		HashSet<IAttributeType<?>> inSpecificPages = new HashSet<IAttributeType<?>>();
		for (IPage iPage : list) {
			inSpecificPages.addAll(Arrays.asList(iPage.getAttributes()));
		}
		
		HierachicPageImpl genericPage = new HierachicPageImpl(item.getType(), false);
		iComputeGenericPage(item, context, genericPage, inSpecificPages, ro);
		list.add(0, genericPage);
	}
	
	protected void iComputeModificationPage(Item item, FilterContext context, List<IPage> list, Set<IAttributeType<?>> ro) {
		iRecurcifComputeModificationPage(item, context, list, ro);
		HashSet<IAttributeType<?>> inSpecificPages = new HashSet<IAttributeType<?>>();
		for (IPage iPage : list) {
			inSpecificPages.addAll(Arrays.asList(iPage.getAttributes()));
		}
		
		HierachicPageImpl genericPage = new HierachicPageImpl(item.getType(), true);
		iComputeGenericPage(item, context, genericPage, inSpecificPages, ro);
		list.add(0, genericPage);
	}
	
	protected void iRecurcifComputeCreationPage(Item item, FilterContext context, List<IPage> list, Set<IAttributeType<?>> ro) {
		((TypeDefinition) item.getType()).recurcifComputeCreationPage(context, list, ro);
	}
	
	protected void iRecurcifComputeModificationPage(Item item, FilterContext context, List<IPage> list, Set<IAttributeType<?>> ro) {
		((TypeDefinition) item.getType()).recurcifComputeModificationPage(context, list, ro);
	}
	
	protected void iComputeGenericPage(Item item, FilterContext context, HierachicPageImpl genericPage,
			HashSet<IAttributeType<?>> inSpecificPages, Set<IAttributeType<?>> ro) {
		((TypeDefinition) item.getType()).computeGenericPage(context, genericPage, inSpecificPages, ro, 
				CadseGCST.ITEM_at_NAME_);
	}
	
	
	protected void iComputeValidators(Item item, FilterContext context, List<UIValidator> validators) {
		((TypeDefinition) item.getType()).computeValidators(context, validators);
	}
	
	protected Map<IAttributeType<?>, UIField> iComputeFields(Item item) {
		Map<IAttributeType<?>, UIField> fiedls = new HashMap<IAttributeType<?>, UIField>();
		for (IAttributeType<?> att : item.getLocalAllAttributeTypes()) {
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
		ret = ((TypeDefinition) item.getType()).findField(att);
		return ret;
	}

	protected List<UIRunningValidator> createRunning(List<UIValidator> validators) {
		ArrayList<UIRunningValidator> ret = new ArrayList<UIRunningValidator>();
		for (UIValidator v : validators) {
			AbstractUIRunningValidator rv = (AbstractUIRunningValidator) v.create();
			rv._desc = v;
			ret.add(rv);
		}
		return ret;
	}
}
