package fr.imag.adele.cadse.core.impl.attribute;

import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.delta.ItemDelta;
import fr.imag.adele.cadse.core.enumdef.TWEvol;

public class AttributeTypeUnresolved extends AttributeType implements IAttributeType<Object> {

	public AttributeTypeUnresolved(CompactUUID id, String name, int flag) {
		super(id, name, flag);
		setEvol(TWEvol.twTransient);
	}

	public AttributeTypeUnresolved(ItemDelta item) {
		super(item);
	}

	public ItemType getType() {
		return CadseGCST.UNRESOLVED_ATTRIBUTE_TYPE;
	}

	public Class<Object> getAttributeType() {
		return Object.class;
	}

	public int getIntID() {
		return 0;
	}

}
