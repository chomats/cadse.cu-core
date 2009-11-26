package fr.imag.adele.cadse.core.impl;

import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.internal.AbstractGeneratedItem;

public class Item_Descriptor extends AbstractGeneratedItem {
	protected ItemType _it;
	
	
	public Item_Descriptor(CompactUUID id, ItemType it, Object ... keyvalues) {
		super(id);
		_it = it;
		for (int i = 0; i < keyvalues.length; i++) {
			IAttributeType<?> att = (IAttributeType<?>) keyvalues[i++];
			Object v = keyvalues[i];
			commitSetAttribute(att, v);
		}
	}
	
	public Item_Descriptor(ItemType it, Object ... keyvalues) {
		this(CompactUUID.randomUUID(), it, keyvalues);
	}
	
	
	@Override
	public ItemType getType() {
		return _it;
	}

}
