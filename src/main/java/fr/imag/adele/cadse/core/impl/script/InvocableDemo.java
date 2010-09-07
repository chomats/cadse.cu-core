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
package fr.imag.adele.cadse.core.impl.script;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class InvocableDemo {

	public static void main(String[] args) throws Exception {
		ScriptEngineManager manager = new ScriptEngineManager();

		ScriptEngine engine = manager.getEngineByName("js");

		engine.eval("function add (a, b) {c = a + b; return c; }");
		Invocable jsInvoke = (Invocable) engine;

		Object result1 = jsInvoke.invokeFunction("add", new Object[] { 10, 5 });
		System.out.println(result1);

		Adder adder = jsInvoke.getInterface(Adder.class);
		int result2 = adder.add(10, 5);
		System.out.println(result2);
	}
}

interface Adder {
	int add(int a, int b);
}
