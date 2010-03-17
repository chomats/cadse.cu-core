package fr.imag.adele.cadse.core.impl.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import fr.imag.adele.cadse.core.CPackage;
import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.CadseIllegalArgumentException;
import fr.imag.adele.cadse.core.CadseRuntime;
import fr.imag.adele.cadse.core.IItemManager;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemFilter;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.Messages;
import fr.imag.adele.cadse.core.TypeDefinition;
import fr.imag.adele.cadse.core.attribute.GroupOfAttributes;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.CadseCore;
import fr.imag.adele.cadse.core.impl.CollectedReflectLink;
import fr.imag.adele.cadse.core.impl.PageRuntimeModel;
import fr.imag.adele.cadse.core.impl.ReflectLink;
import fr.imag.adele.cadse.core.impl.ui.CreationAction;
import fr.imag.adele.cadse.core.impl.ui.ModificationAction;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransactionListener;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.ui.AbstractActionContributor;
import fr.imag.adele.cadse.core.ui.HierarchicPage;
import fr.imag.adele.cadse.core.ui.IActionContributor;
import fr.imag.adele.cadse.core.ui.IActionPage;
import fr.imag.adele.cadse.core.ui.IPage;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.ui.UIValidator;
import fr.imag.adele.cadse.core.ui.view.FilterContext;
import fr.imag.adele.cadse.core.ui.view.NewContext;
import fr.imag.adele.cadse.util.ArraysUtil;
import fr.imag.adele.cadse.util.OrderWay;

public class TypeDefinitionImpl extends ItemImpl implements TypeDefinition, TypeDefinition.Internal {

	protected String _packageName = NO_VALUE_STRING;

	/**
	 * old string
	 * 
	 * @deprecated use {@link CadseGCST#ITEM_TYPE_at_QUALIFIED_NAME_}
	 */
	@Deprecated
	static final String UNIQUE_NAME_KEY = "#unique-name";

	/**
	 * old string
	 * 
	 * @deprecated use {@link CadseGCST#ITEM_TYPE_at_DISPLAY_NAME_}
	 */
	@Deprecated
	static final String DISPLAY_NAME_KEY = "#display-name";

	/**
	 * old string
	 * 
	 * @deprecated use {@link CadseGCST#ITEM_TYPE_at_NAME_}
	 */
	@Deprecated
	public static final String SHORT_NAME_KEY = "#short-name";

	/*
	 * old string
	 */
	@Deprecated
	public static final String ATTR_SHORT_NAME = "ws::private::short-name";

	public TypeDefinitionImpl() {
	}

	public TypeDefinitionImpl(UUID id, ItemType type, String qualifiedName, String name, Item parent, LinkType lt)
			throws CadseException {
		super(id, type, qualifiedName, name, parent, lt);
	}

	public TypeDefinitionImpl(UUID id, ItemType it, String uniqueName, String shortName) {
		super(id, it, uniqueName, shortName);
	}

	public TypeDefinitionImpl(ItemType itemtype, ItemDelta desc) {
		super(itemtype, desc);
	}

	public TypeDefinitionImpl(ItemType type) {
		super(type);
	}

	protected IAttributeType<?>[] _attributesDefinitions = null;
	protected IPage[] _creationPages = null;
	protected IPage[] _modificationPages = null;
	protected UIField[] _fields = null;
	protected UIValidator[] _validators = null;
	protected GroupOfAttributes[] _groupOfAttributes = null;

	/** The action contributors. */
	protected IActionContributor[] _actionContributors = null;

	protected String _cadseName = null;

	public static final int IT_DEFAULT_FLAG_VALUE = 0;
	protected String _cstName;
	int _it_definedflag = IT_ABSTRACT; // IT_ABSTRACT All ready defined not heritable.
	int _it_flag;

	public boolean getITFlag(int f) {
		if ((_it_definedflag & f) == 0) {
			return (IT_DEFAULT_FLAG_VALUE & f) != 0;
		}
		return (_it_flag & f) != 0;
	}

	public boolean isITDefinedFlag(int f) {
		return (_it_definedflag & f) != 0;
	}

	public boolean setITFlag(int f, boolean flag) {
		boolean oldv = getFlag(f);
		if (flag) {
			this._it_flag |= f;
		}
		else {
			this._it_flag &= ~f;
		}
		this._it_definedflag |= f;
		return oldv != flag;
	}

	/**
	 * return les actions contributions du propre du type. cf {@link #getAllActionContribution()};
	 * 
	 * @return the action contribution
	 */
	public IActionContributor[] getActionContribution() {
		return _actionContributors == null ? AbstractActionContributor.EMPTY : _actionContributors;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.imag.adele.cadse.core.ItemType#getCreationPage()
	 */
	/*
	 * (non-Javadoc)
	 * @see fr.imag.adele.cadse.core.impl.internal.TypeDefinition#getCreationPage()
	 */
	public IPage[] getCreationPage() {
		return this._creationPages == null ? PageRuntimeModel.EMPTY_PAGE : this._creationPages;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.imag.adele.cadse.core.ItemType#getModificationPage()
	 */
	/*
	 * (non-Javadoc)
	 * @see fr.imag.adele.cadse.core.impl.internal.TypeDefinition#getModificationPage ()
	 */
	public IPage[] getModificationPage() {
		return this._modificationPages == null ? PageRuntimeModel.EMPTY_PAGE : this._modificationPages;
	}

	@Override
	public void collectOutgoingLinks(LinkType linkType, CollectedReflectLink ret) {
		if (linkType == CadseGCST.TYPE_DEFINITION_lt_CADSE) {
			ret.addOutgoing(CadseGCST.TYPE_DEFINITION_lt_CADSE, getCadse(), Item.IS_HIDDEN);
			return;
		}
		if (linkType == CadseGCST.TYPE_DEFINITION_lt_ATTRIBUTES) {
			ret.addOutgoing(CadseGCST.TYPE_DEFINITION_lt_ATTRIBUTES, _attributesDefinitions);
			return;
		}
		if (linkType == CadseGCST.TYPE_DEFINITION_lt_CREATION_PAGES) {
			ret.addOutgoing(CadseGCST.TYPE_DEFINITION_lt_CREATION_PAGES, Item.IS_HIDDEN, _creationPages);
			return;
		}
		if (linkType == CadseGCST.TYPE_DEFINITION_lt_MODIFICATION_PAGES) {
			ret.addOutgoing(CadseGCST.TYPE_DEFINITION_lt_MODIFICATION_PAGES, Item.IS_HIDDEN, _modificationPages);
			return;
		}
		super.collectOutgoingLinks(linkType, ret);
	}

	@Override
	public Link commitLoadCreateLink(LinkType lt, Item destination) throws CadseException {
		if (lt == CadseGCST.TYPE_DEFINITION_lt_CADSE) {
			_cadse = (CadseRuntime) destination;
			_cadseName = destination.getQualifiedName();
			// _cadseRuntime = destination
			return new ReflectLink(lt, this, destination, 0, Item.IS_HIDDEN);
		}
		if (lt == CadseGCST.TYPE_DEFINITION_lt_ATTRIBUTES) {
			if (destination.getType() == CadseGCST.UNRESOLVED_ATTRIBUTE_TYPE) {
				return null;
			}
			if (destination.getType() == CadseGCST.LINK_TYPE) {
				if (!(destination instanceof LinkType)) {
					throw new CadseException("Destination is not a LinkType : {0}", destination
							.getQualifiedDisplayName());
				}
				LinkType atlt = (LinkType) destination;
				if (!this.m_outgoings.contains(atlt)) {
					this.m_outgoings.add(atlt);
				}
				if (lt.isComposition()) {
					setITFlag(IT_COMPOSITE, true);
				}
			}

			if (!(destination instanceof IAttributeType)) {
				throw new CadseException("Destination is not an IAttributeType : {0}", destination
						.getQualifiedDisplayName());
			}
			return _addAttributeType((IAttributeType<?>) destination);
		}

		if (lt == CadseGCST.TYPE_DEFINITION_lt_CREATION_PAGES) {
			this._creationPages = ArraysUtil.add(IPage.class, this._creationPages, (IPage) destination);
			return new ReflectLink(lt, this, destination, this._creationPages.length - 1);
		}
		if (lt == CadseGCST.TYPE_DEFINITION_lt_MODIFICATION_PAGES) {
			this._modificationPages = ArraysUtil.add(IPage.class, this._modificationPages, (IPage) destination);
			return new ReflectLink(lt, this, destination, this._modificationPages.length - 1);
		}
		return super.commitLoadCreateLink(lt, destination);
	}

	@Override
	public synchronized void removeOutgoingLink(Link link, boolean notifie) {
		LinkType lt = link.getLinkType();
		Item destination = link.getDestination();
		if (lt == CadseGCST.TYPE_DEFINITION_lt_ATTRIBUTES && destination.isResolved()) {
			removeAttributeType((IAttributeType<?>) destination);
			if (destination.getType() == CadseGCST.LINK_TYPE) {
				LinkType atlt = (LinkType) destination;
				this.m_outgoings.remove(atlt);
				if (atlt.isComposition()) {
					for (LinkType outgoing_lt : getOutgoingLinkTypes()) {
						if (outgoing_lt.isComposite()) {
							return;
						}
					}
					setITFlag(IT_COMPOSITE, false);
				}
			}
			return;
		}
		if (lt == CadseGCST.TYPE_DEFINITION_lt_CREATION_PAGES && destination.isResolved()) {
			this._creationPages = ArraysUtil.remove(IPage.class, this._creationPages, (IPage) destination);
			return;
		}
		if (lt == CadseGCST.TYPE_DEFINITION_lt_MODIFICATION_PAGES && destination.isResolved()) {
			this._modificationPages = ArraysUtil.remove(IPage.class, this._modificationPages, (IPage) destination);
			return;
		}
		super.removeOutgoingLink(link, notifie);
	}

	public LinkType createLinkType(UUID uuid, int intID, String name, int _kind, int min, int max, String selection,
			TypeDefinition destination) {
		LinkType ret = null;

		preconditions_createLinkType(name, _kind, min, max, destination);

		ret = new LinkTypeImpl(uuid, _kind, this, name, intID, min, max, selection, destination);
		Link l = addOutgoingLinkType(ret);
		return ret;
	}

	/**
	 * Adds the outgoing link type.
	 * 
	 * @param ret
	 *            the ret
	 */
	public Link addOutgoingLinkType(LinkType ret) {
		this.m_outgoings.add(ret);
		Link l = addAttributeType(ret);
		if (ret.isComposition()) {
			setITFlag(IT_COMPOSITE, true);
		}
		return l;
	}

	void removeOutgoingLinkType(LinkType ret) {
		this.m_outgoings.remove(ret);
		removeAttributeType(ret);
		if (ret.isComposition()) {
			for (LinkType lt : getOutgoingLinkTypes()) {
				if (lt.isComposite()) {
					return;
				}
			}
			setITFlag(IT_COMPOSITE, false);
		}
	}

	/**
	 * Get an ougoing link type by id.
	 * 
	 * @param name
	 *            the name
	 * @return a link type if found; null if not found.
	 */
	public LinkType getOutgoingLinkType(String name) {
		return _getOutgoingLinkType(this, name);
	}

	static public LinkType _getOutgoingLinkType(TypeDefinition _this, String name) {
		IAttributeType<? extends Object> a = _this.getAttributeType(name);
		if (a instanceof LinkType) {
			return (LinkType) a;
		}

		return Accessor.filterName(_this.getOutgoingLinkTypes(), name);
	}

	/*
	 * (non-Javadoc)
	 * @see fr.imag.adele.cadse.core.ItemType#getOutgoingLinkType(fr.imag.adele.cadse .core.ItemType, java.lang.String)
	 */
	public LinkType getOutgoingLinkType(TypeDefinition destination, String name) {
		return _getOutgoingLinkType(this, destination, name);
	}

	static public LinkType _getOutgoingLinkType(TypeDefinition _this, TypeDefinition destination, String name) {
		Iterator<LinkType> iter = _this.getOutgoingLinkTypes().iterator();
		while (iter.hasNext()) {
			LinkType lt = iter.next();
			if (lt.getDestination().equals(destination) && (lt.getName().equals(name))) {
				return lt;
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.imag.adele.cadse.core.ItemType#getOutgoingLinkType(fr.imag.adele.cadse .core.ItemType, int)
	 */
	public LinkType getOutgoingLinkType(TypeDefinition dest, int kind) {
		return _getOutgoingLinkType(this, dest, kind);
	}

	static public LinkType _getOutgoingLinkType(TypeDefinition _this, TypeDefinition dest, int kind) {
		Iterator<LinkType> iter = _this.getOutgoingLinkTypes().iterator();
		while (iter.hasNext()) {
			LinkType lt = iter.next();
			if (lt.getDestination().equals(dest) && (lt.getKind() == kind)) {
				return lt;
			}
		}
		return null;
	}

	public List<LinkType> getOwnerOugoingLinkTypes() {
		return getOwnerOutgoingLinkTypes();
	}

	/**
	 * Get an incoming link type by id.
	 * 
	 * @param name
	 *            the name
	 * @return a link type if found; null if not found.
	 */
	public LinkType getIncomingLinkType(String name) {
		return Accessor.filterName(getIncomingLinkTypes(), name);
	}

	/**
	 * Get all outgoing link types.
	 * 
	 * @return an unmodifiable list all outgoing link types.
	 */
	public List<LinkType> getOutgoingLinkTypes() {
		ArrayList<LinkType> ret = new ArrayList<LinkType>();
		computeOutgoingLinkTypes(ret, new HashSet<TypeDefinition>());
		return ret;
	}

	/**
	 * Get all owned outgoing link types, not hierarchical.
	 * 
	 * @return an unmodifiable list all owned outgoing link types.
	 */
	public List<LinkType> getOwnerOutgoingLinkTypes() {
		List<LinkType> ret = new ArrayList<LinkType>();

		for (Link l : this.m_outgoings) {
			if (l.getLinkType() == CadseCore.theLinkType) {
				ret.add((LinkType) l);
			}
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.imag.adele.cadse.core.ItemType#createLinkType(int, java.lang.String, int, int, int, java.lang.String,
	 * fr.imag.adele.cadse.core.LinkType)
	 */
	public LinkType createLinkType(UUID uuid, int intID, String id, int kind, int min, int max, String selection,
			LinkType inverse) throws CadseException {
		if (!inverse.getDestination().equals(this)) {
			throw new CadseException(Messages.error_destination_bad_inverse_link, getName(), inverse.getDestination()
					.getName());
		}
		LinkTypeImpl lt = (LinkTypeImpl) createLinkType(uuid, intID, id, kind, min, max, selection, inverse.getSource());
		lt.setInverseLinkType(inverse);
		((LinkTypeImpl) inverse).setInverseLinkType(lt);
		return lt;
	}

	/**
	 * Ajoute des pages de creation.
	 * 
	 * @param creationPages
	 *            the creation pages
	 */
	@Override
	public synchronized void addCreationPages(IPage... creationPages) {
		if (creationPages == null || creationPages.length == 0) {
			return; // todo nothing
		}
		this._creationPages = ArraysUtil.addList(IPage.class, this._creationPages, creationPages);
	}

	public synchronized void addValidators(UIValidator v) {
		_validators = ArraysUtil.add(UIValidator.class, _validators, v);
	}

	public synchronized void addField(UIField v) {
		_fields = ArraysUtil.add(UIField.class, _fields, v);
	}

	/**
	 * Ajoute des pages de modification.
	 * 
	 * @param modificationPages
	 *            the modification pages
	 */
	@Override
	public void addModificationPages(IPage... modificationPages) {
		if (modificationPages == null || modificationPages.length == 0) {
			return; // todo nothing
		}
		this._modificationPages = ArraysUtil.addList(IPage.class, this._modificationPages, modificationPages);
	}

	public <T> Link addAttributeType(IAttributeType<T> type) {
		Link l = _addAttributeType(type);
		_dblw.registerItem(type);
		if (l != null) {
			type.addIncomingLink(l, false);
		}
		return l;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.imag.adele.cadse.core.IAttributableType#addAttributeType(fr.imag.adele .cadse.core.IAttributeType)
	 */
	public <T> Link _addAttributeType(IAttributeType<T> type) {
		int index = ArraysUtil.indexOf(_attributesDefinitions, type);
		if (index != -1) {
			_dblw.getCadseDomain().log(getDisplayName(), "Cannot add " + type, null);
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
	 * @see fr.imag.adele.cadse.core.IAttributableType#addAttributeType(fr.imag.adele .cadse.core.IAttributeType)
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

	/*
	 * (non-Javadoc)
	 * @seefr.imag.adele.cadse.core.impl.internal.TypeDefinition# recurcifComputeCreationPage
	 * (fr.imag.adele.cadse.core.ui.view.FilterContext, java.util.List, java.util.Set)
	 */
	public void recurcifComputeCreationPage(FilterContext context, List<IPage> list, Set<TypeDefinition> visited) {
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
				if (!setPage) {
					list.add(f);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fr.imag.adele.cadse.core.impl.internal.TypeDefinition#getAllAttributeTypes (java.util.List,
	 * fr.imag.adele.cadse.core.ItemFilter)
	 */
	public void getAllAttributeTypes(List<IAttributeType<?>> all, ItemFilter filter) {
		if (_attributesDefinitions != null) {
			if (filter == null) {
				all.addAll(Arrays.asList(_attributesDefinitions));
			}
			else {
				for (IAttributeType<?> at : _attributesDefinitions) {
					if (filter.accept(at)) {
						all.add(at);
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fr.imag.adele.cadse.core.impl.internal.TypeDefinition#computeValidators
	 * (fr.imag.adele.cadse.core.ui.view.FilterContext, java.util.List)
	 */
	@Override
	public void computeValidators(FilterContext context, List<UIValidator> validators, Set<TypeDefinition> visited) {
		if (!visited.add(this)) {
			return;
		}
		if (_validators != null) {
			for (UIValidator f : _validators) {
				UIValidator[] owValidators = f.getOverwriteValidator();
				boolean addValidator = false;
				if (owValidators != null && owValidators.length != 0) {
					List<UIValidator> owValidatorsList = Arrays.asList(owValidators);
					for (int i = 0; i < validators.size(); i++) {
						if (owValidatorsList.indexOf(validators.get(i)) != -1) {
							validators.set(i, f);
							addValidator = true;
							break;
						}
					}
					validators.removeAll(owValidatorsList);
				}
				if (!addValidator) {
					validators.add(f);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @seefr.imag.adele.cadse.core.impl.internal.TypeDefinition# recurcifComputeModificationPage
	 * (fr.imag.adele.cadse.core.ui.view.FilterContext, java.util.List, java.util.Set)
	 */
	public void recurcifComputeModificationPage(FilterContext context, List<IPage> list, Set<IAttributeType<?>> ro,
			Set<TypeDefinition> visited) {
		if (!visited.add(this)) {
			return;
		}
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
				if (!setPage) {
					list.add(f);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fr.imag.adele.cadse.core.impl.internal.TypeDefinition#computeGenericPage
	 * (fr.imag.adele.cadse.core.ui.view.FilterContext, fr.imag.adele.cadse.core.impl.internal.ui.HierachicPageImpl,
	 * java.util.HashSet, java.util.Set)
	 */
	public void computeGenericPage(FilterContext context, HierarchicPage genericPage,
			HashSet<IAttributeType<?>> inSpecificPages, Set<IAttributeType<?>> ro, Set<TypeDefinition> visited,
			IAttributeType<?>... firstAttributes) {
		if (!visited.add(this)) {
			return;
		}
		ArrayList<IAttributeType> notPutAttr = new ArrayList<IAttributeType>();
		for (IAttributeType firstAtt : firstAttributes) {
			if (!inSpecificPages.contains(firstAtt)) {
				notPutAttr.add(firstAtt);
			}
			inSpecificPages.add(firstAtt);
		}
		if (_attributesDefinitions != null) {
			for (IAttributeType<?> attr : _attributesDefinitions) {
				if (!inSpecificPages.contains(attr) && canBeAddedInGenericPage(genericPage, attr)) {
					if (context.getItem().isDelegatedValue(attr)) {
						ro.add(attr);
					}
					notPutAttr.add(attr);
				}
			}
		}
		if (notPutAttr.isEmpty()) {
			return;
		}
		IPage bloc = genericPage.insertNewBloc(getDisplayName());
		bloc.addLast(notPutAttr);
	}

	@Override
	public void computeGroup(Set<GroupOfAttributes> groups, Set<TypeDefinition> visited) {
		if (!visited.add(this)) {
			return;
		}
		if (_groupOfAttributes != null) {
			for (GroupOfAttributes g : _groupOfAttributes) {
				if (g.getOverWriteGroup() != null) {
					groups.remove(g.getOverWriteGroup());
				}
				groups.add(g);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fr.imag.adele.cadse.core.impl.internal.TypeDefinition#findField(fr.imag
	 * .adele.cadse.core.attribute.IAttributeType)
	 */
	@Override
	public UIField findField(IAttributeType<?> att) {
		if (_fields != null) {
			for (UIField f : _fields) {
				if (f.getAttributeDefinition() == att) {
					return f;
				}
			}
		}
		return null;
	}

	protected boolean canBeAddedInGenericPage(HierarchicPage genericPage, IAttributeType<?> attr) {
		if (!attr.isShowInDefaultMP() && genericPage.isModificationPage()) {
			return false;
		}
		if (genericPage.isGroupPage() && attr.isAttributeHead()) {
			return false;
		}
		if (genericPage.isModificationPage()) {
			return true;
		}
		if (attr.isShowInDefaultCP()) {
			return true;
		}
		return false;
	}

	/**
	 * Creates the default creation action.
	 * 
	 * @param parent
	 *            the parent
	 * @param type
	 *            the type
	 * @param lt
	 *            the lt
	 * @return the i action page
	 * @throws CadseException
	 *             the melusine exception
	 */
	@Override
	public IActionPage createDefaultCreationAction(NewContext context) throws CadseException {
		return new CreationAction(context);
	}

	/**
	 * Creates the default modification action.
	 * 
	 * @param node
	 *            the node
	 * @return the i action page
	 */
	@Override
	public IActionPage createDefaultModificationAction(FilterContext context) {
		return new ModificationAction(context);
	}

	@Override
	public void addGroupOfAttributes(GroupOfAttributes g) {
		_groupOfAttributes = ArraysUtil.add(GroupOfAttributes.class, _groupOfAttributes, g);
	}

	@Override
	public GroupOfAttributes[] getGroupOfAttributes() {
		return _groupOfAttributes;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.imag.adele.cadse.core.ItemType#getIncomingPart(fr.imag.adele.cadse .core.ItemType)
	 */
	public LinkType getIncomingPart(ItemType typeParent) {

		for (LinkType lt : getIncomingLinkTypes()) {
			if (lt.isPart() && lt.getSource().equals(typeParent)) {
				return lt;
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.imag.adele.cadse.core.ItemType#getIncomingOne(fr.imag.adele.cadse. core.ItemType)
	 */
	public LinkType getIncomingOne(ItemType typeParent) throws CadseException {
		LinkType found = null;
		for (LinkType lt : getIncomingLinkTypes()) {
			if (lt.getSource().equals(typeParent)) {
				if (found == null) {
					found = lt;
				}
				else {
					return null;

					// throw new
					// CadseException(Messages.error_linktype_more_one_link_found,
					// typeParent.getId(), getId());
				}
			}
		}
		return found;
	}

	/**
	 * Compute incoming link types.
	 */
	@Override
	public void computeIncomingLinkTypes(List<LinkType> ret, Set<TypeDefinition> visited) {
		if (!visited.add(this)) {
			return;
		}
		for (Link l : this._incomings) {
			if (l.getLinkType() == CadseCore.theLinkType && l instanceof LinkType) {
				ret.add((LinkType) l);
			}
		}
	}

	/**
	 * List<LinkType> ret = new ArrayList<LinkType>(); Compute ougoing link types.
	 */
	@Override
	public void computeOutgoingLinkTypes(List<LinkType> ret, Set<TypeDefinition> visited) {
		if (!visited.add(this)) {
			return;
		}
		for (Link l : this.m_outgoings) {
			if (l.getLinkType() == CadseCore.theLinkType) {
				ret.add((LinkType) l);
			}
		}
	}

	/**
	 * Get all incoming link types.
	 * 
	 * @return an unmodifiable list all incoming link types.
	 */
	@Override
	public List<LinkType> getIncomingLinkTypes() {
		List<LinkType> ret = new ArrayList<LinkType>();
		computeIncomingLinkTypes(ret, new HashSet<TypeDefinition>());
		return ret;
	}

	@Override
	public IAttributeType<?>[] getAllAttributeTypes() {
		ArrayList<IAttributeType<?>> all = new ArrayList<IAttributeType<?>>();
		getAllAttributeTypes(all);
		return all.toArray(new IAttributeType<?>[all.size()]);
	}

	@Override
	public void getAllAttributeTypes(List<IAttributeType<?>> all) {
		getAllAttributeTypes(all, null);
	}

	@Override
	public void getAllAttributeTypes(Map<String, IAttributeType<?>> all, boolean keepLastAttribute) {
		getAllAttributeTypes(all, keepLastAttribute, null);
	}

	@Override
	public IAttributeType<?> getAttributeType(String name) {
		if (SHORT_NAME_KEY.equals(name) || ATTR_SHORT_NAME.equals(name)) {
			return CadseGCST.ITEM_at_NAME_;
		}
		if (UNIQUE_NAME_KEY.equals(name) || Item.ATTR_LONG_NAME.equals(name)) {
			return CadseGCST.ITEM_at_QUALIFIED_NAME_;
		}
		if (Item.IS_READ_ONLY_KEY.equals(name)) {
			return CadseGCST.ITEM_at_ITEM_READONLY_;
		}
		if (DISPLAY_NAME_KEY.equals(name)) {
			return CadseGCST.ITEM_at_DISPLAY_NAME_;
		}
		// if ("UUID_ATTRIBUTE".equals(name)) {
		// if (this == CadseGCST.CADSE_DEFINITION)
		// return CadseGCST.CADSE_DEFINITION_at_ID_RUNTIME_;
		// if (this == CadseGCST.ATTRIBUTE)
		// return CadseGCST.ATTRIBUTE_at_ID_RUNTIME_;
		// if (this == CadseGCST.ABSTRACT_ITEM_TYPE)
		// return CadseGCST.ABSTRACT_ITEM_TYPE_at_ID_RUNTIME_;
		// }

		if (_attributesDefinitions != null) {
			for (IAttributeType<?> att : _attributesDefinitions) {
				if (att.getName().equals(name)) {
					return att;
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.imag.adele.cadse.core.IAttributableType#getAttributeTypeIds()
	 */
	@Override
	public String[] getAttributeTypeIds() {
		HashSet<String> returnKeys = new HashSet<String>();
		getAllAttributeTypesKeys(returnKeys, new FilterOutLinkType());
		return returnKeys.toArray(new String[returnKeys.size()]);
	}

	/**
	 * Ajoute un contributeur d'actions.
	 * 
	 * @param contributor
	 *            the contributor
	 */
	@Override
	public synchronized void addActionContributeur(IActionContributor contributor) {
		_actionContributors = ArraysUtil.add(IActionContributor.class, _actionContributors, contributor);
	}

	@Override
	final public Set<IActionContributor> getAllActionContribution() {
		Set<IActionContributor> overwrittenAction = new HashSet<IActionContributor>();
		Set<IActionContributor> action = new HashSet<IActionContributor>();
		computeAllActionContribution(action, overwrittenAction);
		action.removeAll(overwrittenAction);
		return action;
	}

	protected void computeAllActionContribution(Set<IActionContributor> action,
			Set<IActionContributor> overwrittenAction) {
		IActionContributor[] localAction = getActionContribution();
		if (localAction.length == 0) {
			return;
		}
		action.addAll(Arrays.asList(localAction));
		for (IActionContributor iActionContributor : localAction) {
			IActionContributor[] overwriteActionContributor = iActionContributor.getOverwriteActionContributor();
			if (overwriteActionContributor.length == 0) {
				continue;
			}
			overwrittenAction.addAll(Arrays.asList(overwriteActionContributor));
		}
	}

	@Override
	public CadseRuntime getCadse() {
		if (_cadse != null) {
			return _cadse;
		}
		Item parent = _parent;
		while (parent != null) {
			if (parent.isInstanceOf(CadseGCST.CADSE) && parent instanceof CadseRuntime) {
				return _cadse = (CadseRuntime) parent;
			}
			parent = parent.getPartParent();
		}
		if (_cadseName != null) {
			return _cadse = _dblw.getCadseRuntime(_cadseName);
		}
		return null;
	}

	public String getCadseName() {
		CadseRuntime cr = getCadse();
		if (cr != null) {
			_cadseName = cr.getQualifiedName();
			return cr.getQualifiedName();
		}
		return _cadseName;
	}

	public void setCadseName(String cadseName) {
		CadseRuntime cr = getCadse();
		if (cr != null) {
			_cadseName = cr.getQualifiedName();
		}
		this._cadseName = cadseName;
	}

	/**
	 * Test preconditions before creating a link type.<br/>
	 * <br/>
	 * Preconditions: <br/>
	 * - 1. <tt>name</tt> cannot be null.<br/>
	 * - 2. <tt>name</tt> cannot be empty. - 3. <tt>destination</tt> cannot be null.<br/>
	 * - 4. <tt>name</tt> muqt be unique.<br/>
	 * - 5. <tt>destination</tt> cannot be type workspace.<br/>
	 * - 6. <tt>min</tt> must greater or equal 0; <tt>max</tt> either equal -1 (means the instance's number of this link
	 * type is undefined), or either greater than <tt>min</tt>.
	 * 
	 * @param name
	 *            : name of link type to create.
	 * @param kind
	 *            : kind of link type, can be a Aggregation, or a Contaiment, or Other.
	 * @param min
	 *            : the minimum instances of this link type that we want create.
	 * @param max
	 *            : the maximum instances of this link type that we want create.
	 * @param destination
	 *            : link type's destination.<br/>
	 * <br/>
	 * @OCL: pre: name <> null pre: id <> '' pre: destination <> null pre: self.to->forAll(rt | rt.name <> id) -- id must
	 *       be unique. pre: not destination.oclIsTypeOf(WorkspaceType) -- destination cannot be a Workspace Type. pre:
	 *       ((max>=min)||(max==-1))&&(min>=0))
	 * @exception IllegalArgumentException
	 *                : Invalid assignment, <tt>name</tt> can not be null.<br/>
	 *                IllegalArgumentException: Invalid assignment, <tt>name</tt> can not be empty.<br/>
	 *                IllegalArgumentException: Invalid assignment, item type <tt>$name</tt> can not be null.<br/>
	 *                IllegalArgumentException: Invalid assignment, this link type <tt>destination</tt> already exist.<br/>
	 *                IllegalArgumentException: Invalid assignment, you can not create a link type whose destination is
	 *                an object of WorkspaceType.<br/>
	 *                IllegalArgumentException: Invalid assignment, verify the values min and max.<br/>
	 */
	protected void preconditions_createLinkType(String name, int kind, int min, int max, TypeDefinition destination) {

		// 1. pre: name <> null
		if (name == null) {
			throw new CadseIllegalArgumentException(Messages.error_linktype_id_is_null);
		}

		// 2. pre: id <> ''
		if (name.length() == 0) {
			throw new CadseIllegalArgumentException(Messages.error_linktype_id_is_empty);
		}

		// 3. pre: destination <> null
		if (destination == null) {
			throw new CadseIllegalArgumentException(Messages.error_item_type_can_not_be_null);
		}

		// 4. pre: self.to->forAll(rt | rt.name <> id)
		for (Iterator outgoers = getOwnerOutgoingLinkTypes().iterator(); outgoers.hasNext();) {
			LinkType lt = (LinkType) outgoers.next();
			if (lt.getName().equals(name)) {
				throw new CadseIllegalArgumentException(Messages.error_linktype_id_already_exits, name, getId());
			}
		}

		// 6. pre: ((max>=min)||(max==-1))&&(min>=0))
		if (!(((max >= min) || (max == -1)) && (min >= 0))) {
			throw new CadseIllegalArgumentException(Messages.error_linktype_min_max);
		}

		// in Item not in ItemType
		// // 7. one relation of containment by destination.
		// if ((kind & LinkType.CONTAINMENT) != 0) {
		// if ( destination.isContainmentType())
		// exception("Cannot create a containment link form {0} to {1}, the
		// desti

	}

	protected LogicalWorkspaceTransactionListener[] workspaceLogiqueCopyListeners;

	public void addLogicalWorkspaceTransactionListener(LogicalWorkspaceTransactionListener l) {
		workspaceLogiqueCopyListeners = ArraysUtil.add(LogicalWorkspaceTransactionListener.class,
				workspaceLogiqueCopyListeners, l);
	}

	public void removeLogicalWorkspaceTransactionListener(LogicalWorkspaceTransactionListener l) {
		workspaceLogiqueCopyListeners = ArraysUtil.remove(LogicalWorkspaceTransactionListener.class,
				workspaceLogiqueCopyListeners, l);
	}

	public LogicalWorkspaceTransactionListener[] getLogicalWorkspaceTransactionListener() {
		return workspaceLogiqueCopyListeners;

	}

	@Override
	public List<Item> getItems() {
		return _dblw.getItems(this);
	}

	@Override
	public void getAllAttributeTypes(Map<String, IAttributeType<?>> all, boolean keepLastAttribute, ItemFilter filter) {
		if (_attributesDefinitions != null) {
			for (IAttributeType<?> att : _attributesDefinitions) {
				if (keepLastAttribute && all.containsKey(att.getName())) {
					continue;
				}
				if (filter == null || filter.accept(att)) {
					all.put(att.getName(), att);
				}
			}
		}
	}

	@Override
	public void getAllAttributeTypesKeys(Set<String> all, ItemFilter filter) {
		if (_attributesDefinitions != null) {
			for (IAttributeType<?> att : _attributesDefinitions) {
				if (filter == null || filter.accept(att)) {
					all.add(att.getName());
				}
			}
		}
	}

	@Override
	public boolean isExtendedType() {
		return false;
	}

	@Override
	public boolean isMainType() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.imag.adele.cadse.core.ItemType#hasIncomingParts()
	 */
	public boolean hasIncomingParts() {
		for (LinkType lt : getIncomingLinkTypes()) {
			if (lt.isPart()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public IAttributeType<?> getCStructuralFeatures(int index) {
		return _attributesDefinitions[index];
	}

	@Override
	public IItemManager getItemManager() {
		throw new UnsupportedOperationException("By default no manager");
	}

	CPackage _package = null;

	protected String _image;

	@Override
	public CPackage getPackage() {
		return _package;
	}

	@Override
	public void setPackage(CPackage p) {
		_package = p;
	}

	@Override
	public LinkType getOutgoingLinkType(UUID idLinkType) {
		LinkType ret = (LinkType) _dblw.getItem(idLinkType);

		return ret;
	}

	@Override
	public String getImage() {
		return _image;
	}

	@Override
	public void setIcon(String uri) {
		_image = uri;
	}

	public String getCSTName() {
		return _cstName;
	}

	public void setCSTName(String cst) {
		_cstName = cst;
	}

	public void setPackageName(String packageName) {
		if (packageName == null) {
			this._packageName = NO_VALUE_STRING;
		}
		else {
			this._packageName = packageName;
		}
	}

	public String getPackageName() {
		return _packageName;
	}
	
	@Override
	public boolean commitMove(OrderWay kind, Link l1, Link l2) {
		if (l1.getLinkType() == CadseGCST.TYPE_DEFINITION_lt_ATTRIBUTES && l2.getLinkType() == CadseGCST.TYPE_DEFINITION_lt_ATTRIBUTES) {
			return ArraysUtil.move(kind,  _attributesDefinitions, l1.getDestination(), l2.getDestination());
		}
		return super.commitMove(kind, l1, l2);
	}

}
