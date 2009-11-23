package fr.imag.adele.cadse.core.impl.internal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.ItemFilter;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.internal.ui.HierachicPageImpl;
import fr.imag.adele.cadse.core.impl.ui.CreationAction;
import fr.imag.adele.cadse.core.ui.IActionPage;
import fr.imag.adele.cadse.core.ui.IPage;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.ui.UIValidator;
import fr.imag.adele.cadse.core.ui.view.FilterContext;
import fr.imag.adele.cadse.core.ui.view.NewContext;

public interface TypeDefinition {

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#getCreationPage()
	 */
	public abstract IPage[] getCreationPage();

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#getModificationPage()
	 */
	public abstract IPage[] getModificationPage();

	public abstract void recurcifComputeCreationPage(FilterContext context, List<IPage> list, Set<IAttributeType<?>> ro);

	public abstract void getAllAttributeTypes(List<IAttributeType<?>> all, ItemFilter filter);

	public abstract void computeValidators(FilterContext context, List<UIValidator> validators);

	public abstract void recurcifComputeModificationPage(FilterContext context, List<IPage> list,
			Set<IAttributeType<?>> ro);

	public abstract void computeGenericPage(FilterContext context, HierachicPageImpl genericPage,
			HashSet<IAttributeType<?>> inSpecificPages, Set<IAttributeType<?>> ro);

	public abstract UIField findField(IAttributeType<?> att);

	
	public IActionPage createDefaultCreationAction(NewContext context) throws CadseException;

	/**
	 * Creates the default modification action.
	 * 
	 * @param node
	 *            the node
	 * 
	 * @return the i action page
	 */
	public IActionPage createDefaultModificationAction(FilterContext context) ;
	
	public void addValidators(UIValidator v);
}