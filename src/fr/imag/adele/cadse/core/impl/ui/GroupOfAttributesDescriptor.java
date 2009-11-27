package fr.imag.adele.cadse.core.impl.ui;


import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.attribute.GroupOfAttributes;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.attribute.AttributeType;
import fr.imag.adele.cadse.core.ui.EPosLabel;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.util.ArraysUtil;

public class GroupOfAttributesDescriptor extends AttributeType implements GroupOfAttributes {

	private GroupOfAttributes _ow = null;
	private IAttributeType<?>[]          _attr = null;
	private int	_column;
	private String	_label;
	
	public GroupOfAttributesDescriptor(CompactUUID id, String label, int column, Object[] keyvalues) {
		super(id, label, 0);
		_column = column;
		_label = label;
	}

	public GroupOfAttributesDescriptor(String label, int column, Object... keyvalues) {
		this(CompactUUID.randomUUID(), label, column, keyvalues);
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

	@Override
	public int getColumn() {
		return _column;
	}

	@Override
	public String getLabel() {
		return _label;
	}

	@Override
	public String getName() {
		return _label;
	}
	
	@Override
	public String getDisplayName() {
		return _label;
	}

	@Override
	public Class<Object> getAttributeType() {
		return Object.class;
	}

	@Override
	public ItemType getType() {
		return CadseGCST.GROUP_OF_ATTRIBUTES;
	}
	
	@Override
	public IAttributeType<?>[] getChildren() {
		return _attr;
	}
	
	@Override
	public UIField generateDefaultField() {
		return new UIFieldImpl(CadseGCST.DGROUP, 
				CompactUUID.randomUUID(), this, getName(), EPosLabel.none, null, null,
				CadseGCST.DGROUP_at_COLUMN_, _column,
				CadseGCST.DGROUP_at_MAKE_COLUMNS_EQUAL_WIDTH_, false);
	}
}
