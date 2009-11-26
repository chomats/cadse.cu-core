package fr.imag.adele.cadse.core.impl.internal.delta;

import fr.imag.adele.cadse.core.CadseDomain;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.delta.ItemDelta;
import fr.imag.adele.cadse.core.impl.CadseCore;

public class ItemTypeItemDeltaAdapterFactory implements ItemDeltaAdapterFactory<ItemType>{

	@Override
	public ItemType getAdapter(ItemDelta itemDelta) {
		if (itemDelta.getItemTypeId().equals(CadseDomain.ITEMTYPE_ID) || itemDelta.isInstanceOf(CadseGCST.ITEM_TYPE))
		return new ItemTypeItemDeltaAdapter(itemDelta);
		return null;
	}

	@Override
	public Class<ItemType> getAdapterClass() {
		return ItemType.class;
	}

}
