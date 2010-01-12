package fr.imag.adele.cadse.core.impl.internal;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.key.ISpaceKey;

public interface InternalLogicalWorkspace extends LogicalWorkspace {

	boolean containsUniqueName(String un);

	boolean containsSpaceKey(ISpaceKey key);

	void commit(LogicalWorkspaceTransactionImpl logicalWorkspaceTransactionImpl, boolean b) throws CadseException;

}
