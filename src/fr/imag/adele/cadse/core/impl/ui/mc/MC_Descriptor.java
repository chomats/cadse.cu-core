package fr.imag.adele.cadse.core.impl.ui.mc;

import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.internal.AbstractGeneratedItem;
import fr.imag.adele.cadse.core.impl.script.ScriptDescriptor;
import fr.imag.adele.cadse.core.ui.UIRunningValidator;
import fr.imag.adele.cadse.core.ui.UIValidator;

public class MC_Descriptor extends AbstractGeneratedItem implements UIValidator {
	ItemType _it;
	private int _error;
	private UIValidator[] _ow;
	private IAttributeType<?>[] _listen;
	private ScriptDescriptor<UIRunningValidator> _script;
	
	public MC_Descriptor(ItemType it) {
		_it = it;
	}
	
	@Override
	public UIRunningValidator create() {
		return _script.create(this);
	}

	@Override
	public IAttributeType<?>[] getListenAttributeType() {
		return _listen;
	}

	@Override
	public UIValidator[] getOverwriteValidator() {
		return _ow;
	}

	@Override
	public int incrementError() {
		return ++_error;
	}

	@Override
	public ItemType getType() {
		return _it;
	}

}