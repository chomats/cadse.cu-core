package fr.imag.adele.cadse.core.impl.internal.delta;

import fr.imag.adele.cadse.core.delta.ItemDelta;

/**
 * Get a facette, an adapter, or an accessor to an itemDelta to manipulate an ItemType or a LinkType form an ItemDelta in a transaction.
 * 
 * @author chomats
 *
 * @param <T>
 */
public interface  ItemDeltaAdapterFactory<T> {

	Class<T> getAdapterClass();
	
	T getAdapter(ItemDelta itemDelta);
	
}
