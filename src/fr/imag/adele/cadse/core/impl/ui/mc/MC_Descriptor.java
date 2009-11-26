package fr.imag.adele.cadse.core.impl.ui.mc;

import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.impl.Item_Descriptor;
import fr.imag.adele.cadse.core.util.CreatedObjectManager;

public class MC_Descriptor extends Item_Descriptor {
	
	static {
		CreatedObjectManager.DEFAULTObjectMANAGER.register(CadseGCST.LINK_MODEL_CONTROLLER, LinkModelController.class);
		CreatedObjectManager.DEFAULTObjectMANAGER.register(CadseGCST.MC_DATE, MC_Date.class);
		CreatedObjectManager.DEFAULTObjectMANAGER.register(CadseGCST.STRING_TO_BOOLEAN_MODEL_CONTROLLER, MC_StringToBoolean.class);
		CreatedObjectManager.DEFAULTObjectMANAGER.register(CadseGCST.MC_NAME_ATTRIBUTE, MC_Name.class);
		CreatedObjectManager.DEFAULTObjectMANAGER.register(CadseGCST.INT_MODEL_CONTROLLER, MC_Integer.class);
	}
	
	public MC_Descriptor(ItemType it, Object ... keyvalues) {
		super(it, keyvalues);
	}
}
