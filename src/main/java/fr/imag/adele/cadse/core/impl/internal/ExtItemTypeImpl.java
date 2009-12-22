package fr.imag.adele.cadse.core.impl.internal;

import java.util.UUID;

import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;

public class ExtItemTypeImpl extends ItemTypeImpl {

	public ExtItemTypeImpl(ItemType metaType, LogicalWorkspace wl,
			ItemTypeImpl superType, UUID id, int intId,
			boolean hasContent, boolean isAbstract, String shortname,
			String displayName) {
		super(metaType, wl, superType, id, intId, hasContent, isAbstract,
				shortname, displayName);
	}

	public ExtItemTypeImpl(LogicalWorkspace wl, ItemType it, ItemDelta desc) {
		super(wl, it, desc);
	}
	
	@Override
	public boolean isAbstract() {
		return true;
	}

}
