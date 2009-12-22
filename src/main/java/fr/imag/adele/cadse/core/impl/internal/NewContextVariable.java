package fr.imag.adele.cadse.core.impl.internal;

import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransaction;
import fr.imag.adele.cadse.core.var.ContextVariable;
import fr.imag.adele.cadse.core.var.ContextVariableImpl;

public class NewContextVariable extends ContextVariableImpl {

	LogicalWorkspaceTransaction	_copy;

	@Override
	public String getName(Item item) {
		return item.getName();
	}

	@Override
	public String getQualifiedName(Item item) {
		return _copy.getItem(item.getId()).getQualifiedName();
	}

	@Override
	public String getAttribute(Item item, IAttributeType<String> att) {
		return _copy.getItem(item.getId()).getAttribute(att);
	}


	public NewContextVariable(LogicalWorkspaceTransaction copy) {
		this._copy = copy;
	}
}
