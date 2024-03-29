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
 *
 * Copyright (C) 2006-2010 Adele Team/LIG/Grenoble University, France
 */
package fr.imag.adele.cadse.core.impl.attribute;

import fr.imag.adele.cadse.core.CadseException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import fr.imag.adele.cadse.core.CadseException;
import java.util.UUID;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;

public class ObjectAttributType<X> extends AttributeType implements IAttributeType<X> {
	private Class<X>	_class;
	private X			_defaultValue;
	Constructor<X>		_constructor;
	IAttributeType<?>[]	_args;

	public ObjectAttributType(UUID id, String name, int flag, Class<X> class_, X defaultValue,
			Constructor<X> constructor, IAttributeType<?>... args) {
		super(id, name, flag);
		_class = class_;
		_defaultValue = defaultValue;
		_constructor = constructor;
		if (_constructor != null) {
			_args = args;
		}
	}

	public ObjectAttributType(ItemDelta item) {
		super(item);
	}

	public ItemType getType() {
		return null;
	}

	public Class<X> getAttributeType() {
		return getClassX();
	}

	@Override
	public X getDefaultValue() {
		return _defaultValue;
	}

	public void setCassX(Class<X> _class) {
		this._class = _class;
	}

	public Class<X> getClassX() {
		return _class;
	}

	@Override
	public Object createNewValueFor(Item anItem) throws CadseException {
		if (_constructor == null) {
			return null;
		}
		try {
			Object[] args = getArgsFromItem(anItem);
			if (args == null) {
				return _constructor.newInstance();
			} else {
				return _constructor.newInstance(args);
			}
		} catch (IllegalArgumentException e) {
			throw new CadseException("Cannot create a value for $0 : $1", e, getName(), e.getMessage());
		} catch (InstantiationException e) {
			throw new CadseException("Cannot create a value for $0 : $1", e, getName(), e.getMessage());
		} catch (IllegalAccessException e) {
			throw new CadseException("Cannot create a value for $0 : $1", e, getName(), e.getMessage());
		} catch (InvocationTargetException e) {
			throw new CadseException("Cannot create a value for $0 : $1", e.getTargetException(), getName(), e
					.getTargetException().getMessage());
		}
	}

	@Override
	public boolean mustBeCreateNewValueAtCreationTimeOfItem() {
		return _constructor != null;
	}
	
	@Override
	public X convertTo(Object v) {
		// TODO Auto-generated method stub
		return (X) super.convertTo(v);
	}
	
	protected Object[] getArgsFromItem(Item anItem) {
		if (_args == null) {
			return null;
		}
		Object[] ret = new Object[_args.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = anItem.getAttribute(_args[i]);
		}
		return ret;
	}

}
