package fr.imag.adele.cadse.core.impl.ui.mc;

import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.Item_Descriptor;
import fr.imag.adele.cadse.core.ui.UIValidator;

public abstract class UIValidator_Descriptor extends Item_Descriptor implements UIValidator {
	
	
	private int _error;
	private UIValidator[] _ow;
	private IAttributeType<?>[] _listen;
	
	public UIValidator_Descriptor(ItemType it, Object ... keyvalues) {
		super(it, keyvalues);
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


	public int getError() {
		return _error;
	}

	public void setError(int error) {
		_error = error;
	}

	public UIValidator[] getOw() {
		return _ow;
	}

	public void setOw(UIValidator... ow) {
		_ow = ow;
	}

	public IAttributeType<?>[] getListenAttributes() {
		return _listen;
	}

	public void setListenAttributes(IAttributeType<?>... listenAttributes) {
		_listen = listenAttributes;
	}
}
