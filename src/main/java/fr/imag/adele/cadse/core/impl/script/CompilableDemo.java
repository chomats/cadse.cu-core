package fr.imag.adele.cadse.core.impl.script;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class CompilableDemo {

	public static void main(String[] args) throws Exception {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("js");

		Compilable jsCompile = (Compilable) engine;
		CompiledScript script = jsCompile.compile("function hi () {print ('www.java2s.com !'); }; hi ();");

		for (int i = 0; i < 5; i++) {
			script.eval();
		}
	}
}