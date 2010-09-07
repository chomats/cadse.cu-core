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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseGCST;
import java.util.UUID;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.attribute.CheckStatus;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.CadseIllegalArgumentException;
import fr.imag.adele.cadse.core.impl.ui.UIFieldImpl;
import fr.imag.adele.cadse.core.impl.ui.ic.IC_Descriptor;
import fr.imag.adele.cadse.core.impl.ui.mc.MC_Descriptor;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.ui.EPosLabel;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.ui.UIPlatform;
import fr.imag.adele.cadse.core.util.Convert;
import fr.imag.adele.cadse.util.NLS;

/**
 * The Class EnumAttributeType.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class EnumAttributeType<X extends Enum<X>> extends AttributeType implements
		fr.imag.adele.cadse.core.attribute.EnumAttributeType<X> {

	/** The value. */
	private String	defaultValue;

	/** The clazz. */
	Class<X>	clazz;

	String[]	values;

	private Map<String, X> enumConstantDirectory;

    public EnumAttributeType() {
    }

        
	/**
	 * Instantiates a new enum attribute type.
	 * 
	 * @param id
	 *            the id
	 * @param clazz
	 *            the clazz
	 * @param min
	 *            the min
	 * @param value
	 *            the value
	 */
	public EnumAttributeType(UUID id, int flag, String name, Class<X> clazz, String value) {
		super(id, name, flag);
		assert clazz != null;
		this.defaultValue = value;
		this.clazz = clazz;
	}

	public EnumAttributeType(ItemDelta item) {
		super(item);
	}

	/**
	 * Find.
	 * 
	 * @param enumConstants
	 *            the enum constants
	 * @param value2
	 *            the value2
	 * 
	 * @return the x
	 */
	private X find(X[] enumConstants, String value2) {
		if (value2 == null) {
			return null;
		}
		if (enumConstants == null) {
			return null;
		}
		for (X x : enumConstants) {
			if (x.name().equals(value2)) {
				return x;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IAttributeType#getDefaultValue()
	 */
	@Override
	public X getDefaultValue() {
		if (clazz == null)
			return null;
		return toEnum(defaultValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.IAttributeType#getAttributeType()
	 */
	public Class<X> getAttributeType() {
		return clazz;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.INamed#getIntID()
	 */
	public int getIntID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Link commitLoadCreateLink(LinkType lt, Item destination) throws CadseException {
		// if (lt == CadseGCST.enum && destination.isResolved()) {
		// addLast((UIField)destination);
		// return new ReflectLink(lt,this, destination, this._fields.length-1);
		// }
		return super.commitLoadCreateLink(lt, destination);
	}

	@Override
	public <T> T internalGetOwnerAttribute(IAttributeType<T> type) {
		if (CadseGCST.ATTRIBUTE_at_DEFAULT_VALUE_ == type) {
			return (T) defaultValue;
		}
		if (CadseGCST.ENUM_at_ENUM_CLAZZ_ == type) {
			return (T) clazz;
		}
		if (CadseGCST.ENUM_at_VALUES_ == type) {
			return (T) new ArrayList<String>(Arrays.asList(getValues()));
		}
		return super.internalGetOwnerAttribute(type);
	}

	public void setDefaultValue(X value) {
		this.defaultValue = value == null ? null : value.toString();
	}

	@Override
	public boolean commitSetAttribute(IAttributeType<?> type, Object value) {
		if (CadseGCST.ATTRIBUTE_at_DEFAULT_VALUE_ == type) {
			this.defaultValue = Convert.toString(value);
			return true;
		}
		if (CadseGCST.ENUM_at_VALUES_ == type) {
			if (value instanceof String[]) {
				this.values = (String[]) value;
			}
			if (value instanceof ArrayList) {
				this.values = ((ArrayList<String>) value).toArray(new String[0]);
			}
		}
		return super.commitSetAttribute(type, value);
	}

	public X toEnum(Object value2) {
		if (value2 == null) {
			return null;
		}
		
		if (clazz == null)
			throw new CadseIllegalArgumentException(Messages.cannot_connvert_to_enum_clazz);
		
		if (value2 instanceof String) {
			return find(clazz.getEnumConstants(), (String) value2);
		}
		if (clazz.isInstance(value2)) {
			return (X) value2;
		}
		throw new ClassCastException(NLS.bind(Messages.cannot_convert_to_from, clazz, value2.getClass()));
	}

	public ItemType getType() {
		return CadseGCST.ENUM;
	}

	@Override
	public CheckStatus check(Item item, Object value) {
		if (!getFlag(CAN_BE_UNDEFINED) && value == VALUE_NOT_DEFINED) {
			return new CheckStatus(UIPlatform.ERROR, Messages.cannot_be_undefined);
		}
		if (value == null) {
			return null;
		}

		if (value instanceof String) {
			value = find(clazz.getEnumConstants(), (String) value);
			if (value == null) {
				return new CheckStatus(UIPlatform.ERROR, Messages.unkown_value, value);
			}
		}
		if (!clazz.isInstance(value)) {
			return new CheckStatus(UIPlatform.ERROR, Messages.bad_type, value.getClass());
		}

		return null;
	}

	@Override
	public X convertTo(Object v) {
		return toEnum(v);
	}

	public String[] getValues() {
		if (values == null) {
			if (clazz != null) {
				X[] XValues = clazz.getEnumConstants();
				if (XValues != null) {
					values = new String[XValues.length];
					for (int i = 0; i < XValues.length; i++) {
						values[i] = XValues[i].name();
					}
				}
			}
			if (values == null) {
				values = new String[0];
			}
		}
		return values;
	}
	
	public Map<String, X> enumConstantDirectory() {
		if (enumConstantDirectory == null) {
            X[] universe = clazz.getEnumConstants();  // Does unnecessary clone
            if (universe == null)
                throw new IllegalArgumentException(
                		clazz.getName() + " is not an enum type");
            Map<String, X> m = new HashMap<String, X>(2 * universe.length);
            for (X constant : universe)
                m.put(((Enum)constant).name(), constant);
            enumConstantDirectory = m;
        }
        return enumConstantDirectory;
	}

	@Override
	public UIField generateDefaultField() {
		return new UIFieldImpl(CadseGCST.DBROWSER, UUID.randomUUID(), this, getDisplayName(), EPosLabel.defaultpos, 
				new MC_Descriptor(CadseGCST.MC_ENUM), 
				new IC_Descriptor(CadseGCST.IC_ENUM_FOR_BROWSER_COMBO));
	}
}
