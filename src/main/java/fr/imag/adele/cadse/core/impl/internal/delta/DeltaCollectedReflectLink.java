package fr.imag.adele.cadse.core.impl.internal.delta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.impl.ReflectLink;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransaction;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;

public class DeltaCollectedReflectLink extends ArrayList<Link> implements java.util.List<Link> {
	ItemDelta	source_or_destination;
	private boolean _derived;
	LogicalWorkspaceTransaction _lw; 
	/**
	 * Give source item for collect outgoing links or give destination item for
	 * incoming links
	 * 
	 * @param source_or_destination
	 */

	public DeltaCollectedReflectLink(ItemDelta source_or_destination) {
		this.source_or_destination = source_or_destination;
		_lw = source_or_destination.getWorkingCopy();
	}

	public <T extends Item> void addOutgoing(LinkType lt, T singleton) {
		addOutgoing(lt, singleton,0);
	}
	
	public <T extends Item> void addOutgoing(LinkType lt, T singleton, int flag) {
		if (singleton == null) {
			return;
		}
		flag |= _derived?Item.DERIVED:0;
		super.add(new DeriviedLinkDelta(_lw, lt, source_or_destination, singleton, 0, flag));
	}

	public void addIncoming(LinkType lt, Item src) {
		if (src == null) {
			return;
		}
		super.add(new DeriviedLinkDelta(_lw, lt, src, source_or_destination, -1, _derived?Item.DERIVED:0));
	}

	public <T extends Item> void addOutgoing(LinkType lt, T[] items) {
		addOutgoing(lt, 0, items);
	}

	public <T extends Item> void addOutgoing(LinkType lt, int flag, T[] items) {
		if (items == null) {
			return;
		}
		flag |= _derived?Item.DERIVED:0;
		ensureCapacity(size() + items.length);
		for (int i = 0; i < items.length; i++) {
			if (items[i] != null) {
				super.add(new DeriviedLinkDelta(_lw, lt, source_or_destination, items[i], i, flag));
			}
		}
	}

	public <T extends Item> void addOutgoing(LinkType lt, List<T> items) {
		addOutgoing(lt, 0, items);
	}
	
	public <T extends Item> void addOutgoing(LinkType lt, int flag, List<T> items) {
		if (items == null) {
			return;
		}
		ensureCapacity(size() + items.size());
		flag |= _derived?Item.DERIVED:0;
		for (int i = 0; i < items.size(); i++) {
			super.add(new DeriviedLinkDelta(_lw, lt, source_or_destination, items.get(i), i, flag));
		}
	}
	
	public <T extends Item> void addOutgoing(LinkType lt, Collection<T> items) {
		if (items == null) {
			return;
		}
		ensureCapacity(size() + items.size());
		int i = 0;
		for (T t : items) {
			super.add(new DeriviedLinkDelta(_lw, lt, source_or_destination, t, i++, _derived?Item.DERIVED:0));
		}
	}

	public void setDerived(boolean b) {
		_derived = b;
	}
	
	@Override
	public boolean add(Link e) {
		return super.add(new DeriviedLinkDelta(_lw, e.getLinkType(), e.getSource(), e.getDestination(), -1, Item.DERIVED));
	}
	
	@Override
	public boolean addAll(Collection<? extends Link> c) {
		boolean ret = false;
		ensureCapacity(size() + c.size());
		for (Link e : c) {
			if (super.add(new DeriviedLinkDelta(_lw, e.getLinkType(), e.getSource(), e.getDestination(), -1, Item.DERIVED)))
				ret = true;
		}
		return ret;
	}
}
