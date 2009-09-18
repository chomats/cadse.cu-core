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
		if (singleton == null) {
			return;
		}
		add(new ReflectLink(lt, source_or_destination, singleton, 0));
	}

	public void addIncoming(LinkType lt, Item src) {
		if (src == null) {
			return;
		}
		add(new ReflectLink(lt, src, source_or_destination, -1));
	}

	public <T extends Item> void addOutgoing(LinkType lt, T[] items) {
		if (items == null) {
			return;
		}
		ensureCapacity(size() + items.length);
		for (int i = 0; i < items.length; i++) {
			if (items[i] != null) {
				add(new ReflectLink(lt, source_or_destination, items[i], i));
			}
		}
	}

	public <T extends Item> void addOutgoing(LinkType lt, int flag, T[] items) {
		if (items == null) {
			return;
		}
		ensureCapacity(size() + items.length);
		for (int i = 0; i < items.length; i++) {
			if (items[i] != null) {
				add(new ReflectLink(lt, source_or_destination, items[i], i, flag));
			}
		}
	}

	public <T extends Item> void addOutgoing(LinkType lt, List<T> items) {
		if (items == null) {
			return;
		}
		ensureCapacity(size() + items.size());
		for (int i = 0; i < items.size(); i++) {
			add(new ReflectLink(lt, source_or_destination, items.get(i), i));
		}
	}
}
