package fr.imag.adele.cadse.core.impl.ui;

import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.internal.AbstractGeneratedItem;
import fr.imag.adele.cadse.core.impl.script.ScriptDescriptor;
import fr.imag.adele.cadse.core.impl.ui.mc.UIValidator_Descriptor;
import fr.imag.adele.cadse.core.ui.UIRunningValidator;
import fr.imag.adele.cadse.core.ui.UIValidator;

public class JavaClassValidator extends UIValidator_Descriptor {
	
	
	public JavaClassValidator(ItemType it, Object... keyvalues) {
		super(it, keyvalues);
	}

	Class<? extends UIRunningValidator> _clazz;
	
	
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
		
		return null;
	}

	public Class<? extends UIRunningValidator> getClazz() {
		return _clazz;
	}

	public void setClazz(Class<? extends UIRunningValidator> clazz) {
		_clazz = clazz;
	}


}
