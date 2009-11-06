package fr.imag.adele.cadse.core.impl.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemFilter;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.delta.ItemDelta;
import fr.imag.adele.cadse.core.impl.ReflectLink;
import fr.imag.adele.cadse.core.impl.internal.ui.HierachicPage;
import fr.imag.adele.cadse.core.ui.IPage;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.ui.UIValidator;
import fr.imag.adele.cadse.core.util.ArraysUtil;

public class TypeDefinitionImpl extends ItemImpl {
	public static final IPage[]			EMPTY_PAGE		= new IPage[0];
	
	
	public TypeDefinitionImpl(LogicalWorkspace wl, CompactUUID id,
			ItemType type, String uniqueName, String shortName, Item parent,
			LinkType lt) throws CadseException {
		super(wl, id, type, uniqueName, shortName, parent, lt);
	}

	public TypeDefinitionImpl(LogicalWorkspace wl, CompactUUID id, ItemType it,
			String uniqueName, String shortName) {
		super(wl, id, it, uniqueName, shortName);
	}

	public TypeDefinitionImpl(LogicalWorkspace wl, ItemType itemtype,
			ItemDelta desc) {
		super(wl, itemtype, desc);
	}

	public TypeDefinitionImpl(LogicalWorkspaceImpl wl, ItemType type) {
		super(wl, type);
	}

	protected IAttributeType<?>[]							_attributesDefinitions		= null;


	protected IPage[]									_creationPages				= null;
	protected IPage[]									_modificationPages			= null;

	protected UIField[]									_fields						= null;
	
	protected UIValidator[]								_validators					= null;
	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#getCreationPage()
	 */
	public IPage[] getCreationPage() {
		return this._creationPages == null ? EMPTY_PAGE : this._creationPages;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ItemType#getModificationPage()
	 */
	public IPage[] getModificationPage() {
		return this._modificationPages == null ? EMPTY_PAGE : this._modificationPages;
	}
	
	@Override
	public Link commitLoadCreateLink(LinkType lt, Item destination)
			throws CadseException {
		if (lt == CadseGCST.TYPE_DEFINITION_lt_CREATION_PAGES) {
			this._creationPages = ArraysUtil.add(IPage.class, this._creationPages, (IPage) destination);
			resetCreationPages();
			return new ReflectLink(lt, this, destination, this._creationPages.length - 1);
		}
		if (lt == CadseGCST.TYPE_DEFINITION_lt_MODIFICATION_PAGES) {
			this._modificationPages = ArraysUtil.add(IPage.class, this._modificationPages, (IPage) destination);
			resetModificationPages();
			return new ReflectLink(lt, this, destination, this._modificationPages.length - 1);
		}
		return super.commitLoadCreateLink(lt, destination);
	}
	
	@Override
	public synchronized void removeOutgoingLink(Link link, boolean notifie) {
		LinkType lt = link.getLinkType();
		Item destination = link.getDestination();
		if (lt == CadseGCST.TYPE_DEFINITION_lt_CREATION_PAGES && destination.isResolved()) {
			this._creationPages = ArraysUtil.remove(IPage.class, this._creationPages, (IPage) destination);
			resetCreationPages();
			return;
		}
		if (lt == CadseGCST.TYPE_DEFINITION_lt_MODIFICATION_PAGES && destination.isResolved()) {
			this._modificationPages = ArraysUtil.remove(IPage.class, this._modificationPages, (IPage) destination);
			resetModificationPages();
			return;
		}
		super.removeOutgoingLink(link, notifie);
	}
	
	/**
	 * Ajoute des pages de creation.
	 * 
	 * @param creationPages
	 *            the creation pages
	 */
	public synchronized void addCreationPages(List<IPage> creationPages) {
		if (creationPages == null || creationPages.size() == 0) {
			return; // todo nothing
		}
		this._creationPages = ArraysUtil.addList(IPage.class, this._creationPages,
				creationPages);
		resetCreationPages();
	}

	/**
	 * Ajoute des pages de modification.
	 * 
	 * @param modificationPages
	 *            the modification pages
	 */
	public void addModificationPages(List<IPage> modificationPages) {
		if (modificationPages == null || modificationPages.size() == 0) {
			return; // todo nothing
		}
		this._modificationPages = ArraysUtil.addList(IPage.class, this._modificationPages,
				modificationPages);
		resetModificationPages();
	}
	
	public <T> Link addAttributeType(IAttributeType<T> type) {
		Link l = _addAttributeType(type);
		this._wl.registerItem(type);
		if (l != null) {
			type.addIncomingLink(l, false);
		}
		return l;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.IAttributableType#addAttributeType(fr.imag.adele
	 * .cadse.core.IAttributeType)
	 */
	public <T> Link _addAttributeType(IAttributeType<T> type) {
		int index = ArraysUtil.indexOf(_attributesDefinitions, type);
		if (index != -1) {
			_wl._wd.log(getDisplayName(), "Cannot add " + type, null);
			return null;
		}

		_attributesDefinitions = ArraysUtil.add(IAttributeType.class, _attributesDefinitions, type);
		type.setParent(this, CadseGCST.TYPE_DEFINITION_lt_ATTRIBUTES);
		index = _attributesDefinitions.length - 1;

		if (CadseGCST.TYPE_DEFINITION_lt_ATTRIBUTES == null) {
			// le model root n'est pas encore charg�.
			return null;
		}

		return new ReflectLink(CadseGCST.TYPE_DEFINITION_lt_ATTRIBUTES, this, type, index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.IAttributableType#addAttributeType(fr.imag.adele
	 * .cadse.core.IAttributeType)
	 */
	public <T> int removeAttributeType(IAttributeType<T> type) {
		int index = ArraysUtil.indexOf(_attributesDefinitions, type);
		if (index == -1) {
			return index;
		}
		_attributesDefinitions = ArraysUtil.remove(IAttributeType.class, _attributesDefinitions, index);
		// Pose un probleme lors de la destruction d'un lien
		// type.setParent(null); (note 22)
		return index;
	}

	
	protected void resetModificationPages() {
		
	}

	protected void resetCreationPages() {
		// TODO Auto-generated method stub
		
	}
	
	public void computegetGoodCreationPage(List<IPage> list) {
		if (_creationPages != null) {
			for (IPage f : _creationPages) {
				IPage[] owPages = f.getOverwritePage();
				boolean setPage = false;
				if (owPages != null && owPages.length != 0) {
					List<IPage> owPagesList = Arrays.asList(owPages);
					for (int i = 0; i < list.size(); i++) {
						if (owPagesList.indexOf(list.get(i)) != -1) {
							list.set(i, f);
							setPage = true;
							break;
						}
					}
					list.removeAll(owPagesList);
				} 
				if (!setPage){
					list.add(f);
				}
			}
		}
	}
	
	public void getAllAttributeTypes(List<IAttributeType<?>> all, ItemFilter filter) {
		if (_attributesDefinitions != null) {
			if (filter == null) {
				all.addAll(Arrays.asList(_attributesDefinitions));
			} else {
				for (IAttributeType<?> at : _attributesDefinitions) {
					if (filter.accept(at)) {
						all.add(at);
					}
				}
			}
		}
	}
	
	protected void computeValidators(List<UIValidator> validators) {
		if (_validators != null) {
			for (UIValidator f : _validators) {
				UIValidator[] owValidators = f.getOverwriteValidator();
				boolean setPage = false;
				if (owValidators != null && owValidators.length != 0) {
					List<UIValidator> owValidatorsList = Arrays.asList(owValidators);
					for (int i = 0; i < validators.size(); i++) {
						if (owValidatorsList.indexOf(validators.get(i)) != -1) {
							validators.set(i, f);
							setPage = true;
							break;
						}
					}
					validators.removeAll(owValidatorsList);
				} 
				if (!setPage){
					validators.add(f);
				}
			}
		}
	}
	
	protected void recurcifComputeGoodModificationPage(List<IPage> list) {
		if (_modificationPages != null) {
			for (IPage f : _modificationPages) {
				IPage[] owPages = f.getOverwritePage();
				boolean setPage = false;
				if (owPages != null && owPages.length != 0) {
					List<IPage> owPagesList = Arrays.asList(owPages);
					for (int i = 0; i < list.size(); i++) {
						if (owPagesList.indexOf(list.get(i)) != -1) {
							list.set(i, f);
							setPage = true;
							break;
						}
					}
					list.removeAll(owPagesList);
				} 
				if (!setPage){
					list.add(f);
				}
			}
		}
	}
	
	protected void computeGenericPage(HierachicPage genericPage,
			HashSet<IAttributeType<?>> inSpecificPages) {
		if (_attributesDefinitions != null) {
			ArrayList<IAttributeType> notPutAttr = new ArrayList<IAttributeType>();
			for (IAttributeType<?> attr : _attributesDefinitions) {
				if (!inSpecificPages.contains(attr) && canBeAddedInGenericPage(genericPage, attr)) {
					notPutAttr.add(attr);
				}
			}
			if (notPutAttr.isEmpty())
				return;
			IPage bloc = genericPage.insertNewBloc(getDisplayName());
			bloc.addLast(notPutAttr);
		}
	}
	
	protected UIField findField(IAttributeType<?> att) {
		if (_fields != null) {
			for (UIField f : _fields) {
				if (f.getAttributeDefinition() == att)
					return f;
			}
		}
		return null;
	}

	protected boolean canBeAddedInGenericPage(HierachicPage genericPage,
			IAttributeType<?> attr) {
		return true;
	}
}
