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
package fr.imag.adele.cadse.core.impl.attribute;

import java.net.URL;
import java.util.UUID;

import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.impl.internal.delta.ItemDeltaImpl;

public class URLAttributeType extends AttributeType implements fr.imag.adele.cadse.core.attribute.URLAttributeType {
	public URLAttributeType(UUID id, String name, int flag) {
		super(id, name, flag);
	}

	public URLAttributeType(ItemDeltaImpl item) {
		super(item);
	}

	public Class<URL> getAttributeType() {
		return URL.class;
	}

	public ItemType getType() {
		return CadseGCST.URL;
	}

	public int getIntID() {
		return 0;
	}

	@Override
	public URL getDefaultValue() {
		return null;
	}

	@Override
	public URL convertTo(Object v) {
		// TODO Auto-generated method stub
		return (URL) super.convertTo(v);
	}
}
