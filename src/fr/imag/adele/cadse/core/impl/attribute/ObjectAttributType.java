package fr.imag.adele.cadse.core.impl.attribute;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.delta.ItemDelta;

public class ObjectAttributType<X> extends AttributeType implements IAttributeType<X> {
	private Class<X>	_class;
	private X			_defaultValue;
	Constructor<X>		_constructor;
	IAttributeType<?>[]	_args;

	public ObjectAttributType(CompactUUID id, String name, int flag, Class<X> class_, X defaultValue,
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
