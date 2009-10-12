package fr.imag.adele.cadse.core.impl.internal.delta;

import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.CadseRuntime;
import fr.imag.adele.cadse.core.delta.ItemDelta;

public class CadseRuntimeItemDeltaAdapterFactory implements
		ItemDeltaAdapterFactory<CadseRuntime> {

	@Override
	public CadseRuntime getAdapter(ItemDelta itemDelta) {
		if (itemDelta.isInstanceOf(CadseGCST.CADSE_RUNTIME))
			return new CadseRuntimeItemDeltaAdapter(itemDelta);
		return null;
	}

	@Override
	public Class<CadseRuntime> getAdapterClass() {
		return CadseRuntime.class;
	}

}
