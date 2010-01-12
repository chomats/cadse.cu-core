package fr.imag.adele.cadse.core.impl.script;

import fr.imag.adele.cadse.core.Item;

public interface ScriptDescriptor<T> {

	T create(Item descriptor);
}
