package fr.imag.adele.cadse.core.impl.internal.ui;

import java.util.List;

import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.internal.AbstractGeneratedItem;
import fr.imag.adele.cadse.core.impl.ui.PageImpl;
import fr.imag.adele.cadse.core.impl.ui.UIFieldImpl;
import fr.imag.adele.cadse.core.ui.HierarchicPage;
import fr.imag.adele.cadse.core.ui.IActionPage;
import fr.imag.adele.cadse.core.ui.IPage;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.util.ArraysUtil;

public class HierachicPageImpl extends AbstractGeneratedItem implements IPage, HierarchicPage {
	protected IPage[] _bloc = null;
	private ItemType _it;
	boolean _modificationPage;
	
	public HierachicPageImpl(ItemType it, boolean modificationPage) {
		_it = it;
		_modificationPage = modificationPage;
	}
	
	
	public IPage insertNewBloc(String label) {
		PageImpl p = new PageImpl(label, label, label, label, true);
		_bloc = ArraysUtil.add(IPage.class, _bloc, p);
		return p;
	}
	
	@Override
	public void addAfter(IAttributeType<?> afterAttr,
			IAttributeType<?> attributeToInsert) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void addBefore(IAttributeType<?> beforeAttr,
			IAttributeType<?> attributeToInsert) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void addLast(List<IAttributeType> attrs) {
		throw new UnsupportedOperationException();
	}
	
	public void addLast(IAttributeType<?>[] attr) {
		throw new UnsupportedOperationException();
	}


	@Override
	public IActionPage getActionPage() {
		return null;
	}


	@Override
	public IAttributeType<?>[] getAttributes() {
		return EMPTY_UIFIELD;
	}


	@Override
	public String getDescription() {
		return _it.getDisplayName();
	}


	@Override
	public String getLabel() {
		return _it.getDisplayName();
	}


	@Override
	public IPage[] getOverwritePage() {
		return null;
	}


	@Override
	public ItemType getParentItemType() {
		return _it;
	}


	@Override
	public String getTitle() {
		return _it.getDisplayName();
	}


	@Override
	public boolean isEmptyPage() {
		if (_bloc != null) {
			for (IPage p : _bloc) {
				if (!p.isEmptyPage())
					return false;
			}
		}
		return true;
	}


	@Override
	public boolean isModificationPage() {
		return _modificationPage;
	}


	@Override
	public boolean isPageComplete() {
		return true;
	}


	@Override
	public ItemType getType() {
		return _it;
	}


	@Override
	public boolean isLast(IAttributeType<?> attributeDefinition) {
		return false;
	}


	@Override
	public IPage[] getBlocks() {
		return _bloc;
	}
	
	
	
}
