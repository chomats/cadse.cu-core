package fr.imag.adele.cadse.core.impl.db;

import java.util.UUID;
import java.util.UUID;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseRuntime;
import java.util.UUID;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.TypeDefinition;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.CadseIllegalArgumentException;
import fr.imag.adele.teamwork.db.ModelVersionDBException;
import fr.imag.adele.teamwork.db.ModelVersionDBService2;

public class DBLinkImpl extends DBObject implements Link {

	public DBLinkImpl(int localId) {
		super(localId);
	}

	@Override
	public void addCompatibleVersions(int... versions) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearCompatibleVersions() {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete() throws CadseException {
		// TODO Auto-generated method stub

	}

	@Override
	public int[] getCompatibleVersions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Item getDestination() {
		try {
			return _dblw.item(_dblw.getDB().getLinkDest(_objectId));
		} catch (ModelVersionDBException e) {
			throw new CadseIllegalArgumentException(
					"Cannot get destination from {0}.", e, _objectId);
		}
	}

	@Override
	public Item getDestination(boolean mustBeResolved) {
		Item ret = getDestination();
		if (ret.isResolved())
			return ret;
		return null;
	}

	@Override
	public UUID getDestinationCadseId() {
		Item destination = getDestination();
		if (destination == null)
			return null;
		CadseRuntime cr = destination.getCadse();
		if (cr == null)
			return null;
		return cr.getId();
	}

	@Override
	public UUID getDestinationId() {
		Item destination = getDestination();
		if (destination == null)
			return null;
		return destination.getId();
	}

	@Override
	public String getDestinationName() {
		Item destination = getDestination();
		if (destination == null)
			return null;
		return destination.getName();
	}

	@Override
	public String getDestinationQualifiedName() {
		Item destination = getDestination();
		if (destination == null)
			return null;
		return destination.getQualifiedName();
	}

	@Override
	public TypeDefinition getDestinationType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> T getLinkAttributeOwner(IAttributeType<T> attDef) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkType getLinkType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Item getResolvedDestination() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Item getSource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UUID getSourceCadseId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UUID getSourceId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isAggregation() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAnnotation() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isComposition() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDerived() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isHidden() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isInterCadseLink() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLinkResolved() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequire() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStatic() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean resolve() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setHidden(boolean hidden) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setReadOnly(boolean readOnly) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVersion(int version) {
		// TODO Auto-generated method stub

	}

	@Override
	public void commitDelete() throws CadseException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean commitSetAttribute(IAttributeType<?> type, String key,
			Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void destroy() throws CadseException {
		// TODO Auto-generated method stub

	}

}
