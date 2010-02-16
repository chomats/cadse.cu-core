package fr.imag.adele.cadse.core.impl.db;

import bak.pcj.map.IntKeyOpenHashMap;
import bak.pcj.map.IntKeyOpenWeakHashMap;
import fr.imag.adele.cadse.core.CadseDomain;
import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseRuntime;
import java.util.UUID;
import fr.imag.adele.cadse.core.IItemManager;
import fr.imag.adele.cadse.core.INamedUUID;
import fr.imag.adele.cadse.core.ItemDescriptionRef;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.TypeDefinition;
import fr.imag.adele.cadse.core.WSModelState;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.attribute.ListAttributeType;
import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.impl.internal.AbstractGeneratedItem;
import fr.imag.adele.cadse.core.impl.internal.CadseDomainImpl;
import fr.imag.adele.cadse.core.impl.internal.LinkImpl;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransaction;
import fr.imag.adele.cadse.core.var.ContextVariable;
import fr.imag.adele.teamwork.db.ModelVersionDBException;
import java.util.Collection;
import java.util.List;

import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.WorkspaceListener;
import fr.imag.adele.cadse.core.impl.CadseRuntimeImpl;
import fr.imag.adele.cadse.core.impl.internal.LogicalWorkspaceImpl;
import fr.imag.adele.cadse.core.transaction.delta.ImmutableWorkspaceDelta;
import fr.imag.adele.cadse.util.ArraysUtil;
import fr.imag.adele.teamwork.db.DBIteratorID;
import fr.imag.adele.teamwork.db.ID3;
import fr.imag.adele.teamwork.db.ModelVersionDBService2;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.reflect.Array;
import java.util.UUID;

public class DBLogicalWorkspace extends LogicalWorkspaceImpl implements
		LogicalWorkspace {

	IntKeyOpenWeakHashMap<DBObject> _cache;
	CadseDomainImpl _cadseDomain;

	private ModelVersionDBService2 _db;

	public DBLogicalWorkspace(CadseDomainImpl cadseDomain) {
		super(cadseDomain);
		_cadseDomain = cadseDomain;
		_db = cadseDomain.getDB();
	}

	public Item item(int objectId) {
		if (_cache.containsKey(objectId))
			return (Item) _cache.get(objectId);
		return new DBItemImpl(objectId);
	}

	public void addListener(int localId, WorkspaceListener l, int eventFilter) {
		// TODO Auto-generated method stub

	}

	public List<WorkspaceListener> filter(int localId, int filters,
			ImmutableWorkspaceDelta delta) {
		// TODO Auto-generated method stub
		return null;
	}

	public void buildComposite(int localId) {
		// TODO Auto-generated method stub

	}

	public boolean canCreateLink(int linkTypeId, int destId) {
		return true;
	}

	List<Link> getOutgoingLinks(INamedUUID obj) {
		ArrayList<Link> ret = new ArrayList<Link>();
		try {
			int[] links = _db.getOutgoingLinks(obj.getObjectId());
			for (int i = 0; i < links.length; i++) {
				int linkId = links[i];
				ret.add(new DBLinkImpl(linkId));
			}
		} catch (ModelVersionDBException ex) {
			Logger.getLogger(DBLogicalWorkspace.class.getName()).log(
					Level.SEVERE, null, ex);
		}

		return ret;
	}

	public ModelVersionDBService2 getDB() {
		return _db;
	}

	// public ItemType[] getRootItemType() {
	// int[] ids = _db.getObjects(true,
	// equal(CadseGCST.ITEM_TYPE_at_IS_ROOT_ELEMENT_.getObjectID(),true));
	// return null;
	// }

	private int obj(INamedUUID object) throws CadseException {
		int ret = object.getObjectId();
		if (ret != ModelVersionDBService2.NULL_ID)
			return ret;
		try {
			ret = _db.checkLocalIdentifier(object.getId());
		} catch (ModelVersionDBException e) {
			throw new CadseException("Cannot get object from UUID.", e);
		}
		if (ret != ModelVersionDBService2.NULL_ID)
			return ret;
		throw new CadseException("Not found object from UUID.", object.getId());
	}

	public void delete(INamedUUID object) throws CadseException {
		int objectId = obj(object);
		try {
			_db.deleteObject(objectId);
		} catch (ModelVersionDBException e) {
			throw new CadseException("Cannot bind type/ext", e);
		}

	}

//	public boolean exists(INamedUUID object) throws CadseException {
//		int objectId = obj(object);
//		try {
//			boolean ret = _db.objExists(objectId);
//			return ret;
//		} catch (ModelVersionDBException e) {
//			throw new CadseException("Cannot bind type/ext", e);
//		}
//	}

	public List<Link> getAggregations(INamedUUID object) throws CadseException {
		int objId = obj(object);
		try {
			DBIteratorID<ID3> ids = _db.getOutgoingLinksAggregation(ID3.class,
					objId);
			return toList(Link.class, ids);
		} catch (ModelVersionDBException e) {
			throw new CadseException(e);
		}

	}

	private <T> List<T> toList(Class<T> clazz, DBIteratorID<ID3> ids) {
		ArrayList<T> ret = new ArrayList<T>();
		try {
			while (ids.hasNext()) {
				ID3 id = ids.next();
				ret.add(toGoodObj(clazz, id));
			}
		} finally {
			ids.close();
		}

		return ret;
	}

	private <T> T[] toArray(Class<T> clazz, DBIteratorID<ID3> ids) {
		final List<T> l = toList(clazz, ids);
		return l.toArray((T[]) Array.newInstance(clazz, l.size()));
	}

	private <T> T toGoodObj(Class<T> clazz, ID3 id) {
		T ret = (T) _cache.get(id.objectId);
		if (ret != null)
			return ret;
		ret = newElt(clazz, id);
		_cache.put(id.objectId, ret);
		return ret;
	}

	private <T> T newElt(Class<T> clazz, ID3 id) {
		if (clazz == Link.class)
			return (T) new DBLinkImpl(id.objectId);
		return null;
	}

//	public TypeDefinition[] getAllTypes(INamedUUID object)
//			throws CadseException {
//		int objId = obj(object);
//		try {
//			DBIteratorID<ID3> typesIds = _db.getAllObjectTypes(objId);
//			return toArray(TypeDefinition.class, typesIds);
//		} catch (ModelVersionDBException e) {
//			throw new CadseException(e);
//		}
//	}

	public void addBindExendsItemType(INamedUUID typeId, ItemType extType)
			throws CadseException {
		int _extId = obj(extType);
		int _typeId = obj(typeId);
		try {
			_db.bindExtension(_typeId, _extId);
		} catch (ModelVersionDBException e) {
			throw new CadseException("Cannot bind type/ext", e);
		}
	}

	public <T> T getValue(IAttributeType<T> attr, int objectId)
			throws CadseException {
		try {
			return (T) _db.getObjectValue(objectId, attr.getObjectId());
		} catch (ModelVersionDBException e) {
			throw new CadseException(
					"Cannot get attribute value for object id {0} and attribute {1}",
					e, objectId, attr.getName());
		}
	}

	public <T> void setValue(IAttributeType<T> attr, int objectId, T v)
			throws CadseException {
		try {
			_db.setObjectValue(objectId, attr.getObjectId(), v);
		} catch (ModelVersionDBException e) {
			throw new CadseException(
					"Cannot set attribute value for object id {0} and attribute {1}",
					e, objectId, attr.getName());

		}
	}

	public INamedUUID getObj(int objectId) throws ModelVersionDBException {
		INamedUUID ret = (INamedUUID) _cache.get(objectId);
		if (ret == null) {
			ret = load(objectId);
		}
		return ret;
	}

	private INamedUUID load(int objectId) throws ModelVersionDBException {
		int typeID = _db.getObjectType(objectId);
		if (typeID == _db.NULL_ID)
			throw new ModelVersionDBException("Cannot found main type of "
					+ objectId);
		int cadseID = _db.getObjectCadse(typeID);
		INamedUUID ret = toGoodObj(INamedUUID.class, new ID3(cadseID, typeID,
				objectId));
		_cache.put(objectId, ret);
		return ret;
	}

	public int newId() throws CadseException {
		try {
			return _db.createLocalIdentifier();
		} catch (ModelVersionDBException ex) {
			Logger.getLogger(DBLogicalWorkspace.class.getName()).log(
					Level.SEVERE, null, ex);
			throw new CadseException("Cannot get a new id.", ex);
		}
	}

	public <T> void setLinkAttribute(Link link,
			IAttributeType<T> att,
			T value) {
		try {
			LogicalWorkspaceTransaction t = createTransaction();
			t.getLink(link).setAttribute(att, value);
			t.commit();
		} catch (CadseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
}
