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

import fr.imag.adele.cadse.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "fr.imag.adele.cadse.core.impl.attribute.messages"; //$NON-NLS-1$
	public static String bad_type;
	public static String cannot_be_empty;
	public static String cannot_be_undefined;
	public static String cannot_connvert_to_enum_clazz;
	public static String cannot_convert_to_from;
	public static String cannot_convert_to_int;
	public static String cannot_convert_to_long;
	public static String must_be_a_boolean;
	public static String must_be_a_double;
	public static String must_be_a_long;
	public static String must_be_a_string;
	public static String must_be_an_integer;
	public static String unkown_value;
	public static String value_must_be_upper;
	public static String value_must_be_lower;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
