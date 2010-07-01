/**
 *
 */
package fr.imag.adele.cadse.core.impl.ui.mc;

import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.WorkspaceListener;
import fr.imag.adele.cadse.core.transaction.delta.ImmutableItemDelta;
import fr.imag.adele.cadse.core.transaction.delta.ImmutableWorkspaceDelta;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.ui.UIPlatform;

public class ItemLinkTypeWorkspaceListener extends WorkspaceListener {
	Item		item;
	UIField		uifield;
	LinkType	lt;
	UIPlatform uiPlatform;
	
	public ItemLinkTypeWorkspaceListener(UIPlatform uiPlatform, Item item, UIField uifield, LinkType lt) {
		super();
		this.item = item;
		this.uifield = uifield;
		this.lt = lt;
		this.uiPlatform = uiPlatform;
	}

	@Override
	public void workspaceChanged(ImmutableWorkspaceDelta delta) {
		ImmutableItemDelta _id = delta.getItem(item);
		if (_id != null) {
			if (_id.hasAddedOutgoingLink()) {
				for (Link l : _id.getLinksAdded()) {
					if (l.getLinkType().equals(lt)) {
						uiPlatform.broadcastThisFieldHasChanged(uifield);
						return;
					}
				}
			}
			if (_id.hasRemovedOutgoingLink()) {
				for (Link l : _id.getLinksRemoved()) {
					if (l.getLinkType().equals(lt)) {
						uiPlatform.broadcastThisFieldHasChanged(uifield);
						return;
					}
				}
			}
		}
	}

}