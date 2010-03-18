/* 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package fr.imag.adele.cadse.core.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;

/**
 * Can collect incoming link or outgoing link, not both (pas les deux � la
 * fois). Peut collecter soit les liens entrants ou soit les liens sortants
 * 
 * @author Team cadse
 */
public class CollectedReflectLink extends ArrayList<Link> implements java.util.List<Link> {
	Item	source_or_destination;
	private boolean _derived;

	/**
	 * Give source item for collect outgoing links or give destination item for
	 * incoming links
	 * 
	 * @param source_or_destination
	 */

	public CollectedReflectLink(Item source_or_destination) {
		this.source_or_destination = source_or_destination;
	}

	public <T extends Item> void addOutgoing(LinkType lt, T singleton) {
		addOutgoing(lt, singleton,0);
	}
	
	public <T extends Item> void addOutgoing(LinkType lt, T singleton, int flag) {
		if (singleton == null) {
			return;
		}
		flag |= _derived?Item.DERIVED:0;
		super.add(new ReflectLink(lt, source_or_destination, singleton, 0, flag));
	}

	public void addIncoming(LinkType lt, Item src) {
		if (src == null) {
			return;
		}
		super.add(new ReflectLink(lt, src, source_or_destination, -1, _derived?Item.DERIVED:0));
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
				super.add(new ReflectLink(lt, source_or_destination, items[i], i, flag));
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
			super.add(new ReflectLink(lt, source_or_destination, items.get(i), i, flag));
		}
	}
	
	public <T extends Item> void addOutgoing(LinkType lt, Collection<T> items) {
		if (items == null) {
			return;
		}
		ensureCapacity(size() + items.size());
		int i = 0;
		for (T t : items) {
			super.add(new ReflectLink(lt, source_or_destination, t, i++, _derived?Item.DERIVED:0));
		}
	}

	public void setDerived(boolean b) {
		_derived = b;
	}
	
	@Override
	public boolean add(Link e) {
		if (_derived) {
			return super.add(new ReflectLink(e.getLinkType(), e.getSource(), e.getDestination(), -1, Item.DERIVED));
		}
		else
			return super.add(e);
	}
	
	@Override
	public boolean addAll(Collection<? extends Link> c) {
		if (_derived) {
			boolean ret = false;
			ensureCapacity(size() + c.size());
			for (Link e : c) {
				if (super.add(new ReflectLink(e.getLinkType(), e.getSource(), e.getDestination(), -1, Item.DERIVED)))
					ret = true;
			}
			return ret;
		}
		else
			return super.addAll(c);
	}
}
