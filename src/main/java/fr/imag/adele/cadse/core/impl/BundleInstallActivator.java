package fr.imag.adele.cadse.core.impl;

import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransaction;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;

public class BundleInstallActivator implements BundleActivator {

	//public static final String CADSE_ZIP = "cadse.zip";
	Set<UUID> itemsloaded = new HashSet<UUID>();
	
	@Override
	public void start(BundleContext context) throws Exception {
		Enumeration en = context.getBundle().findEntries("/", null, true);
		
		final ExportImportCadseFunction exportImportCadseFunction = new ExportImportCadseFunction();
		exportImportCadseFunction.importCadseItems(en);
		
		for (Item item : exportImportCadseFunction.getItemsHash()) {
			itemsloaded.add(item.getId());
		};
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		//delete items;
		LogicalWorkspaceTransaction t = CadseCore.getLogicalWorkspace().createTransaction();
		for (UUID key : itemsloaded) {
			ItemDelta item = t.getItem(key);
			if (item == null || item.isDeleted()) continue;
			item.unload();
		}
		t.commit();
	}

}
