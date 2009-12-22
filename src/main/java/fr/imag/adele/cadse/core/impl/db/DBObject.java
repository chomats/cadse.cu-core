package fr.imag.adele.cadse.core.impl.db;

import fr.imag.adele.cadse.core.AdaptableObjectImpl;
import java.util.UUID;

import java.util.UUID;
import fr.imag.adele.cadse.core.INamed;
import fr.imag.adele.cadse.core.INamedUUID;
import fr.imag.adele.cadse.core.CadseIllegalArgumentException;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.teamwork.db.ModelVersionDBException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBObject extends AdaptableObjectImpl implements INamedUUID, INamed {

	public int _objectId;
	static public DBLogicalWorkspace _dblw;

        public DBObject() {
	}


        public DBObject(DBLogicalWorkspace dblw, int objectId) {
		super();
		_dblw = dblw;
		_objectId = objectId;
	}

	@Override
	public UUID getId() {
                if (_objectId == -1)
                return null;
		try {
			return _dblw.getDB().getUniqueIdentifier(_objectId);
		} catch (ModelVersionDBException e) {
			throw new CadseIllegalArgumentException("Cannot get identifier from {0}.", e, _objectId);
		}
	}

	@Override
	public int getObjectID() {
		return _objectId;
	}

	@Override
	public String getName() {
		try {
			if (_dblw.getDB().objExists(_objectId))
                            return _dblw.getDB().getName(_objectId);
                        return Item.NO_VALUE_STRING;

		} catch (ModelVersionDBException e) {
			throw new CadseIllegalArgumentException("Cannot get identifier from {0}.", e, _objectId);
		}
	}

        /*
	 * (non-Javadoc)
	 *
	 * @see fr.imag.adele.cadse.core.internal.LinkImpl#hashCode()
	 */
	@Override
	public int hashCode() {
		return _objectId;
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
			return _objectId == ((Item) obj).getObjectID();
		}

		return false;
	}

        @Override
	public String toString() {
		return getClass().getName() + ":" + _objectId;
	}

        @Override
        public void setUUID(long itemMsb, long itemLsb) {
            try {
                _objectId = _dblw.getDB().getOrCreateLocalIdentifier(new UUID(itemMsb, itemLsb));
            } catch (ModelVersionDBException ex) {
                Logger.getLogger(DBObject.class.getName()).log(Level.SEVERE, "Cannot create UUID id db", ex);
            }
        }
}
