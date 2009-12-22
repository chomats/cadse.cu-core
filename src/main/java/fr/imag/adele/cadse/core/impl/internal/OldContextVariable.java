package fr.imag.adele.cadse.core.impl.internal;

import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransaction;
import fr.imag.adele.cadse.core.transaction.delta.SetAttributeOperation;
import fr.imag.adele.cadse.core.var.ContextVariable;

public class OldContextVariable extends ContextVariable {
	LogicalWorkspaceTransaction	_copy;

	@Override
	public String getName(Item item) {
		return getAttribute(item, CadseGCST.ITEM_at_NAME_);
	}

	@Override
	public String getQualifiedName(Item item) {
		return getAttribute(item, CadseGCST.ITEM_at_QUALIFIED_NAME_);
	}

	@Override
	public String getValue(Item item, IAttributeType<String> key) {
			SetAttributeOperation attOper = _copy.getItem(item.getId()).getSetAttributeOperation(key, false);
		if (attOper != null) {
			return (String) attOper.getOldValue();
		}
		return item.getAttribute(key);
	}

	@Override
	public String getAttribute(Item item, IAttributeType<String> att) {
		SetAttributeOperation attOper = _copy.getItem(item.getId()).getSetAttributeOperation(att, false);
		if (attOper != null) {
			return (String) attOper.getOldValue();
		}
		return item.getAttribute(att);
	}
	
	@Override
	public void putValue(Item item, IAttributeType<String> attr, String value) {
		throw new UnsupportedOperationException("Context is readonly");
	}

	public OldContextVariable(LogicalWorkspaceTransaction copy) {
		this._copy = copy;
	}
}
