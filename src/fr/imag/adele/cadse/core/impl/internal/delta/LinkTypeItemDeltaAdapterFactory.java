package fr.imag.adele.cadse.core.impl.internal.delta;

import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.delta.ItemDelta;

public class LinkTypeItemDeltaAdapterFactory implements ItemDeltaAdapterFactory<LinkType>{

	@Override
	public LinkType getAdapter(ItemDelta itemDelta) {
		return new LinkTypeItemDeltaAdapter(itemDelta);
	}

	@Override
	public Class<LinkType> getAdapterClass() {
		return LinkType.class;
	}

}
