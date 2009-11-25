package fr.imag.adele.cadse.core.impl.ui;

import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.internal.AbstractGeneratedItem;
import fr.imag.adele.cadse.core.impl.script.ScriptDescriptor;
import fr.imag.adele.cadse.core.ui.UIRunningValidator;
import fr.imag.adele.cadse.core.ui.UIValidator;

public class JavaClassValidator extends AbstractGeneratedItem implements UIValidator {
	Class<? extends UIRunningValidator> _clazz;
	private ScriptDescriptor<? extends UIRunningValidator> _script;
	
	ItemType _it;
	private int _error;
	private UIValidator[] _ow;
	private IAttributeType<?>[] _listenAttributes;
	
	@Override
	public UIRunningValidator create() {
		try {
			if (_clazz != null)
				return _clazz.newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		return _script.create(this);
	}

	@Override
	public IAttributeType<?>[] getListenAttributeType() {
		return _listenAttributes;
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

	public Class<? extends UIRunningValidator> getClazz() {
		return _clazz;
	}

	public void setClazz(Class<? extends UIRunningValidator> clazz) {
		_clazz = clazz;
	}

	public ScriptDescriptor<? extends UIRunningValidator> getScript() {
		return _script;
	}

	public void setScript(ScriptDescriptor<? extends UIRunningValidator> script) {
		_script = script;
	}

	public ItemType getIt() {
		return _it;
	}

	public void setIt(ItemType it) {
		_it = it;
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
		return _listenAttributes;
	}

	public void setListenAttributes(IAttributeType<?>... listenAttributes) {
		_listenAttributes = listenAttributes;
	}


}
