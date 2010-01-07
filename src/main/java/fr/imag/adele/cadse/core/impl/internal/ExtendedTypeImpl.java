package fr.imag.adele.cadse.core.impl.internal;

import java.util.UUID;

import fr.imag.adele.cadse.core.ExtendedType;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.util.ArraysUtil;

public class ExtendedTypeImpl extends TypeDefinitionImpl implements ExtendedType {

	private ItemType[] _exendsItemType;
	
	public ExtendedTypeImpl(UUID id, ItemType it, String qualifiedName, String name) {
		super(id, it, qualifiedName, name);
	}

	
	/* (non-Javadoc)
	 * @see fr.imag.adele.cadse.core.impl.internal.ExtendedType#getExendsItemType()
	 */
	public ItemType[] getExendsItemType() {
		if (_exendsItemType == null)
			return new ItemType[0];
		return _exendsItemType;
	}
	
	
	/* (non-Javadoc)
	 * @see fr.imag.adele.cadse.core.impl.internal.ExtendedType#setExendsItemType(fr.imag.adele.cadse.core.ItemType[])
	 */
	public void setExendsItemType(ItemType[] exendsItemType) {
		_exendsItemType = exendsItemType;
	}
	
	/* (non-Javadoc)
	 * @see fr.imag.adele.cadse.core.impl.internal.ExtendedType#addExendsItemType(fr.imag.adele.cadse.core.ItemType)
	 */
	public void addExendsItemType(ItemType... exendsItemType) {
		_exendsItemType = ArraysUtil.addList(ItemType.class, _exendsItemType, exendsItemType);
	}


}
