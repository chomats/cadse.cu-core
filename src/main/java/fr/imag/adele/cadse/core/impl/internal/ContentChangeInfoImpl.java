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
package fr.imag.adele.cadse.core.impl.internal;

import fr.imag.adele.cadse.core.ContentChangeInfo;

/**
 * The Class ContentChangeInfoImpl.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class ContentChangeInfoImpl implements ContentChangeInfo {

	/** The kind. */
	int		kind;

	/** The old path. */
	String	oldPath;

	/** The path. */
	String	path;

	/**
	 * Instantiates a new content change info impl.
	 * 
	 * @param kind
	 *            the kind
	 * @param path
	 *            the path
	 * @param path2
	 *            the path2
	 */
	public ContentChangeInfoImpl(int kind, String path, String path2) {
		super();
		this.kind = kind;
		this.path = path;
		oldPath = path2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ContentChangeInfo#getKindOfElement()
	 */
	public int getKind() {
		return kind;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ContentChangeInfo#getOldPathOfElement()
	 */
	public String getOldPathOfElement() {
		return oldPath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ContentChangeInfo#getPathOfElement()
	 */
	public String getPathOfElement() {
		return path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Integer.toHexString(kind) + " " + path + " --> " + oldPath;
	}
}
