package fr.imag.adele.cadse.core.impl.ui;


import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.Item_Descriptor;
import fr.imag.adele.cadse.core.ui.GroupOfAttributes;
import fr.imag.adele.cadse.core.util.ArraysUtil;

public class GroupOfAttributesDescriptor extends Item_Descriptor implements GroupOfAttributes {

	private GroupOfAttributes _ow = null;
	private IAttributeType<?>[]          _attr = null;
	
	public GroupOfAttributesDescriptor(CompactUUID id, Object[] keyvalues) {
		super(id, CadseGCST.GROUP_OF_ATTRIBUTES, keyvalues);
	}

	public GroupOfAttributesDescriptor(Object... keyvalues) {
		super(CadseGCST.GROUP_OF_ATTRIBUTES, keyvalues);
	}
	
	/* (non-Javadoc)
	 * @see fr.imag.adele.cadse.core.impl.ui.GroupAttributes#getAttributes()
	 */
	public IAttributeType<?>[] getAttributes() {
		return _attr;
	}
	
	/* (non-Javadoc)
	 * @see fr.imag.adele.cadse.core.impl.ui.GroupAttributes#add(fr.imag.adele.cadse.core.attribute.IAttributeType)
	 */
	public void add(IAttributeType<?> attributes) {
		_attr = ArraysUtil.add(IAttributeType.class, _attr, attributes);
	}
	
	/* (non-Javadoc)
	 * @see fr.imag.adele.cadse.core.impl.ui.GroupAttributes#getOverWriteGroup()
	 */
	public GroupOfAttributes getOverWriteGroup() {
		return _ow;
	}
	
	/* (non-Javadoc)
	 * @see fr.imag.adele.cadse.core.impl.ui.GroupAttributes#setOverWriteGroup(fr.imag.adele.cadse.core.impl.ui.GroupAttributes)
	 */
	public void setOverWriteGroup(GroupOfAttributes ow) {
		_ow = ow;
	}

}
