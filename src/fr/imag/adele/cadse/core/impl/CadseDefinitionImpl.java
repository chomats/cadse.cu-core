package fr.imag.adele.cadse.core.impl;

import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.util.Convert;

public class CadseDefinitionImpl extends CadseRuntimeImpl {

	public CadseDefinitionImpl(String name, CompactUUID runtimeId,
			CompactUUID definitionId) {
		super(name, runtimeId, definitionId);
	}

	
	@Override
	public ItemType getType() {
		return CadseGCST.CADSE_DEFINITION;
	}
	
	@Override
	public boolean commitSetAttribute(IAttributeType<?> type, Object value) {
		if (type == CadseGCST.CADSE_DEFINITION_at_CADSE_NAME_) {
			_cadseName = Convert.toString(value);
			return true;
		}
		
		if (type == CadseGCST.CADSE_at_DESCRIPTION_) {
			_description = Convert.toString(value);
			return true;
		}
		
		return super.commitSetAttribute(type, value);
	}
	
	@Override
	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {
		if (CadseGCST.CADSE_DEFINITION_at_CADSE_NAME_ == type) {
			return (T) _cadseName;
		}
		return super.internalGetOwnerAttribute(type);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.Item#getDisplayName()
	 */
	public String getDisplayName() {
		return getType().getItemManager().getDisplayName(this);
	}

}
