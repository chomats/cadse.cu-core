package fr.imag.adele.cadse.core.impl.internal.delta;

import java.util.Collection;
import java.util.UUID;

import fr.imag.adele.cadse.core.CadseError;
import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.impl.ReflectLink;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransaction;
import fr.imag.adele.cadse.core.transaction.delta.CreateOperation;
import fr.imag.adele.cadse.core.transaction.delta.DeleteOperation;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.transaction.delta.LinkDelta;
import fr.imag.adele.cadse.core.transaction.delta.OperationType;
import fr.imag.adele.cadse.core.transaction.delta.SetAttributeOperation;
import fr.imag.adele.cadse.core.transaction.delta.WLWCOperation;

public class DeriviedLinkDelta extends ReflectLink implements LinkDelta {

	public DeriviedLinkDelta(LogicalWorkspaceTransaction lw, LinkType lt, Item source, Item dest, int index,
			int flag) {
		super(lt, source, dest, index, flag);
		_lw = lw;
	}

	private LogicalWorkspaceTransaction _lw;

	@Override
	public void changeDestination(Item att) {
		throw new UnsupportedOperationException("Derived link");
	}

	@Override
	public void delete(DeleteOperation options) throws CadseException {
		throw new UnsupportedOperationException("Derived link");
	}

	@Override
	public IAttributeType<?> getAttributeType(
			SetAttributeOperation setAttributeOperation) {
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public Link getBaseLink() {
		return null;
	}

	@Override
	public ItemDelta getDestinationOperation() throws CadseException {
		return getDestination();
	}
	
	@Override
	public ItemDelta getDestination(boolean resolved) {
		return getDestination();
	}
	
	@Override
	public ItemDelta getResolvedDestination() {
		return getDestination();
	}
	
	@Override
	public ItemDelta getDestination() {
		return _lw.getItem(super.getDestination());
	}
	
	@Override
	public ItemDelta getSource() {
		return _lw.getItem(super.getSource());
	}

	@Override
	public String getHandleIdentifier() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public LinkDelta getInverseLink() {
		return null;
	}

	@Override
	public ItemDelta getItemOperationParent() {
		return getSourceOperation();
	}

	@Override
	public String getLinkTypeName() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public ItemDelta getSourceOperation() {
		return _lw.getItem(getDestination());
	}

	@Override
	public boolean isCreatedLink() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public boolean isMappingLink() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public boolean isOther() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public boolean isPart() {
		return getLinkType().isPart();
	}

	@Override
	public boolean mustDeleteDestination() {
		return false;
	}

	@Override
	public boolean mustDeleteSource() {
		return false;
	}

	@Override
	public void setAttribute(IAttributeType<?> key, Object v) {
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public void setAttribute(IAttributeType<?> key, Object v, Object oldValue,
			boolean loaded) {
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public void setIndex(int index, boolean loaded) {
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public void setInfo(String info) {
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public void setInfo(String info, boolean loaded) {
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public void setObjectID(int linkId) {
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public void setVersion(int version, boolean loaded) {
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public void toString(String begin, StringBuilder sb, String tab) {
		sb.append(begin).append("derived ");
		sb.append(super.toString());
	}

	@Override
	public <T> T getAttribute(IAttributeType<T> key) {
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public <T> T getAttribute(IAttributeType<T> key, boolean returnDefault) {
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public <T> T getAttributeWithDefaultValue(IAttributeType<T> att,
			T defaultValue) {
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public CreateOperation getCreateOperation() {
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public DeleteOperation getDeleteOperation() {
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public Collection<SetAttributeOperation> getSetAttributeOperation() {
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public SetAttributeOperation getSetAttributeOperation(IAttributeType<?> key) {
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public boolean isAdded() {
		return false;
	}

	@Override
	public boolean isDeleted() {
		return false;
	}

	@Override
	public void addError(String msg, Object... args) {
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public void addError(CadseError e) {
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public UUID getOperationId() {
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public OperationType getOperationType() {
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public WLWCOperation getParentOperDelta() {
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public OperationType getParentType() {
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public LogicalWorkspaceTransaction getWorkingCopy() {
		return _lw;
	}

	@Override
	public boolean isLoaded() {
		return false;
	}

	@Override
	public boolean isModified() {
		return false;
	}

	@Override
	public void addInParent() throws CadseException {
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public void removeInParent() {
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public void setLoaded(boolean loaded) {
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public void add(SetAttributeOperation setAttributeOperation) {
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public void add(SetAttributeOperation setAttributeOperation, boolean check) {
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public void setCreateOperation(CreateOperation createItemOperation) {
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public void setDeleteOperation(DeleteOperation deleteItemOperation) {
		throw new UnsupportedOperationException("Not implemented method");
	}

}
