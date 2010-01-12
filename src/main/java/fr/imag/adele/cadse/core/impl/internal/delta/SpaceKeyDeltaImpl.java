package fr.imag.adele.cadse.core.impl.internal.delta;

import fr.imag.adele.cadse.core.delta.ItemDelta;
import fr.imag.adele.cadse.core.delta.OperationTypeCst;
import fr.imag.adele.cadse.core.delta.SetAttributeOperation;
import fr.imag.adele.cadse.core.delta.SpaceKeyDelta;
import fr.imag.adele.cadse.core.delta.WLWCOperationImpl;
import fr.imag.adele.cadse.core.key.ISpaceKey;
import fr.imag.adele.cadse.core.key.SpaceKeyType;
import fr.imag.adele.cadse.core.util.Convert;

public class SpaceKeyDeltaImpl extends WLWCOperationImpl implements SpaceKeyDelta {

	public SpaceKeyDeltaImpl(ItemDelta parent) {
		super(OperationTypeCst.KEY_DELTA, parent);
	}

	SetAttributeOperation[]	_attOperation;
	ISpaceKey				_newKey;
	ISpaceKey				_oldKey;

	public SetAttributeOperation[] getAttributeOperations() {
		return _attOperation;
	}

	public SpaceKeyType getKeyType() {
		return _newKey != null ? _newKey.getType() : _oldKey != null ? _oldKey.getType() : null;
	}

	public ISpaceKey getNewKey() {
		return _newKey;
	}

	public ISpaceKey getOldKey() {
		return _oldKey;
	}

	public void toString(StringBuilder sb, String tab) {
		if (_oldKey != null) {
			sb.append(tab).append("oldKey=").append(_oldKey).append("\n");
		}
		if (_newKey != null) {
			sb.append(tab).append("newKey=").append(_newKey).append("\n");
		}
		if (isChangeParent()) {
			sb.append(tab).append("change parent\n");
		}
	}

	@Override
	public boolean isModified() {
		return _newKey != null && _oldKey != null && (!(_newKey.equals(_oldKey))) || isChangeParent()
				|| (_oldKey == null && _newKey != null) || (_oldKey != null && _newKey == null);
	}

	public void setNewKey(ISpaceKey key) {
		_newKey = key;
	}

	public void setOldKey(ISpaceKey key) {
		_oldKey = key;
	}

	public boolean isChangeParent() {
		return _newKey != null && _oldKey != null
				&& !Convert.equals(_newKey.getParentSpaceKey(), _oldKey.getParentSpaceKey());
	}

}
