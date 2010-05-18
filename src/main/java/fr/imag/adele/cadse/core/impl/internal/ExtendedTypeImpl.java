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
	 * @see fr.imag.adele.cadse.core.impl.internal.ExtendedType#addExendsItemType(fr.imag.adele.cadse.core.ItemType)
	 */
	public void addExendsItemType(ItemType exendsItemType) {
		int index = ArraysUtil.indexOf(_exendsItemType, exendsItemType);
		if (index == -1) {
			_exendsItemType = ArraysUtil.add(ItemType.class, _exendsItemType, exendsItemType);
		}
	}

	@Override
	public void removeExendsItemType(ItemType et) {
		_exendsItemType = ArraysUtil.remove(ItemType.class, _exendsItemType, et);
	}
	
	@Override
	public boolean isExtendedType() {
		return true;
	}
}
