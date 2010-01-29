package fr.imag.adele.cadse.core.impl.internal.delta;

import java.util.ArrayList;
import java.util.Properties;
import java.util.UUID;

import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.CadseRuntime;
import fr.imag.adele.cadse.core.ExtendedType;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.transaction.delta.LinkDelta;
import fr.imag.adele.cadse.core.ui.view.DefineNewContext;

public class CadseRuntimeItemDeltaAdapter extends ItemItemDeltaAdapter implements CadseRuntime {

	public CadseRuntimeItemDeltaAdapter(ItemDelta itemDelta) {
		super(itemDelta);
	}

	@Override
	public boolean canBeExecuted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getCstQualifiedClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getErrors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UUID getIdCadseDefinition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CadseRuntime[] getRequiredCadses() {
		java.util.List<Link> extendsLinks = _delta.getOutgoingLinks(CadseGCST.CADSE_lt_EXTENDS);
		ArrayList<CadseRuntime> ret = new ArrayList<CadseRuntime>();
		for (Link l : extendsLinks) {
			if (l.isLinkResolved()) {
				CadseRuntime cr = ((LinkDelta) l).getDestination().getAdapter(CadseRuntime.class);
				if (cr != null)
					ret.add(cr);
			}
		}
		if (ret.size() == 0)
			return null;

		return (CadseRuntime[]) ret.toArray(new CadseRuntime[ret.size()]);
	}

	@Override
	public boolean isCadseRoot() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isExecuted() {
		return _delta.getAttribute(CadseGCST.CADSE_at_EXECUTED_);
	}

	@Override
	public boolean isRequired(CadseRuntime cr) {
		CadseRuntime[] requiredCadses = getRequiredCadses();
		if (requiredCadses == null) {
			return false; // cr n'est pas etendu par this
		}
		for (CadseRuntime eCr : requiredCadses) {
			if (eCr == cr) {
				return true; // this extends eCr
			}
		}
		for (CadseRuntime eCr : requiredCadses) {
			if (eCr.isRequired(cr)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void addError(String msg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addItemType(ItemType it) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCadseroot(boolean cadseroot) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCstQualifiedClassName(String cstClass) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDescription(String description) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDisplayName(String displayName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setExecuted(boolean executed) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIdCadseDefinition(UUID idCadseDefintiion) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRequiredCadses(CadseRuntime[] extendsCadse) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getCSTName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addDefineNewContext(DefineNewContext d) {
		// TODO Auto-generated method stub

	}

	@Override
	public DefineNewContext[] getDefineNewContexts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeDefineNewContext(DefineNewContext d) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addBinding(Binding b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addExtendedType(ExtendedType et) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Binding[] getBinding() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExtendedType[] getExtendedType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ItemType[] getItemTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeBinding(Binding b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeExtendedType(ExtendedType et) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getLocalizedLabel(IAttributeType<?> attr) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented method");
	}

	@Override
	public void setLocalizedLabels(Properties localizedLabels) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented method");
	}

}
