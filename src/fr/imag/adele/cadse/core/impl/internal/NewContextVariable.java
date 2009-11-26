package fr.imag.adele.cadse.core.impl.internal;

import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransaction;
import fr.imag.adele.cadse.core.var.ContextVariable;

public class NewContextVariable extends ContextVariable {

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
		return item.getName();
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
		return _copy.getItem(item.getId()).getQualifiedName();
	}

	@Override
	public String getValue(Item item, String key) {
		return _copy.getItem(item.getId()).getAttribute(key);
	}

	@Override
	public String getAttribute(Item item, IAttributeType<String> att) {
		return _copy.getItem(item.getId()).getAttribute(att);
	}

	@Override
	public void putValue(Item item, String key, String value) {
		throw new UnsupportedOperationException("Context is readonly");
	}

	public NewContextVariable(LogicalWorkspaceTransaction copy) {
		this._copy = copy;
	}
}
