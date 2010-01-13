package fr.imag.adele.cadse.core.impl.db;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.imag.adele.cadse.core.AdaptableObjectImpl;
import fr.imag.adele.cadse.core.CadseIllegalArgumentException;
import fr.imag.adele.cadse.core.INamed;
import fr.imag.adele.cadse.core.INamedUUID;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.teamwork.db.ModelVersionDBException;

public class DBObject extends AdaptableObjectImpl implements INamedUUID, INamed {

	public int _objectId;
	static public DBLogicalWorkspace _dblw;
	UUID _uuid;
	public DBObject() {
	}

	public DBObject(int objectId) {
		super();
		_objectId = objectId;
	}

	@Override
	public UUID getId() {
		//
		//return _uuid;
		if (_objectId == -1)
			return null;
		try {
			return _dblw.getDB().getUniqueIdentifier(_objectId);
		} catch (ModelVersionDBException e) {
			throw new CadseIllegalArgumentException(
					"Cannot get identifier from {0}.", e, _objectId);
		}
	}

	@Override
	public int getObjectId() {
		return _objectId;
	}

	@Override
	public String getName() {
//		try {
//			if (_dblw.getDB().objExists(_objectId))
//				return _dblw.getDB().getName(_objectId);
//			return Item.NO_VALUE_STRING;
//
//		} catch (ModelVersionDBException e) {
//			throw new CadseIllegalArgumentException(
//					"Cannot get identifier from {0}.", e, _objectId);
//		}
		return Item.NO_VALUE_STRING;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.LinkImpl#hashCode()
	 */
	@Override
	public int hashCode() {
		return _objectId!=-1 ?
					_objectId : 
						_uuid != null ? _uuid.hashCode() : super.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.internal.LinkImpl#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof INamedUUID) {
			return _objectId == ((Item) obj).getObjectId();
		}

		return false;
	}

	@Override
	public String toString() {
		return getClass().getName() + ":" + _objectId;
	}

	@Override
	public void setUUID(long itemMsb, long itemLsb) {
		setUUID(new UUID(itemMsb, itemLsb));
	}

	@Override
	public void setUUID(UUID uuid) {
		if (uuid == null) return;
		_uuid = uuid;
		try {
			_objectId = _dblw.getDB().getOrCreateLocalIdentifier(uuid);
		} catch (ModelVersionDBException ex) {
			Logger.getLogger(DBObject.class.getName()).log(Level.SEVERE,
					"Cannot create UUID id db", ex);
		}
	}
}
