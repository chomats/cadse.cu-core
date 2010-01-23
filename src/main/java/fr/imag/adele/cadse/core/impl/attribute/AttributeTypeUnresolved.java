package fr.imag.adele.cadse.core.impl.attribute;

import java.util.UUID;

import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.enumdef.TWEvol;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.ui.UIField;

public class AttributeTypeUnresolved extends AttributeType implements IAttributeType<Object> {
	ItemType _it;
	
	public AttributeTypeUnresolved(UUID id, String name, ItemType it, int flag) {
		super(id, name, flag);
		setEvol(TWEvol.twTransient);
		_it = it;
		if (_it == null)
			it = CadseGCST.UNRESOLVED_ATTRIBUTE_TYPE;
	}

	public AttributeTypeUnresolved(ItemDelta item) {
		super(item);
	}

	public ItemType getType() {
		return _it;
	}

	public Class<Object> getAttributeType() {
		return Object.class;
	}

	public int getIntID() {
		return 0;
	}
	
	@Override
	public UIField generateDefaultField() {
		return null;
	}

}
