package fr.imag.adele.cadse.core.impl.internal;

import fr.imag.adele.cadse.core.CadseRootCST;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.delta.SetAttributeOperation;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransaction;
import fr.imag.adele.cadse.core.var.ContextVariable;

public class OldContextVariable extends ContextVariable {
	LogicalWorkspaceTransaction	_copy;

	/**
	 * @deprecated Use {@link #getName(Item)} instead
	 */
	@Deprecated
	@Override
	public String getShortName(Item item) {
		return getName(item);
	}

	@Override
	public String getName(Item item) {
		return getAttribute(item, CadseRootCST.ITEM_TYPE_at_NAME_);
	}

	/**
	 * @deprecated Use {@link #getQualifiedName(Item)} instead
	 */
	@Deprecated
	@Override
	public String getUniqueName(Item item) {
		return getQualifiedName(item);
	}

	@Override
	public String getQualifiedName(Item item) {
		return getAttribute(item, CadseRootCST.ITEM_TYPE_at_QUALIFIED_NAME_);
	}

	@Override
	public String getValue(Item item, String key) {
		SetAttributeOperation attOper = _copy.getItem(item.getId()).getSetAttributeOperation(key, false);
		if (attOper != null) {
			return (String) attOper.getOldValue();
		}
		return item.getAttribute(key);
	}

	@Override
	public String getAttribute(Item item, IAttributeType<String> att) {
		SetAttributeOperation attOper = _copy.getItem(item.getId()).getSetAttributeOperation(att.getName(), false);
		if (attOper != null) {
			return (String) attOper.getOldValue();
		}
		return item.getAttribute(att);
	}

	@Override
	public void putValue(Item item, String key, String value) {
		throw new UnsupportedOperationException("Context is readonly");
	}

	public OldContextVariable(LogicalWorkspaceTransaction copy) {
		this._copy = copy;
	}
}
