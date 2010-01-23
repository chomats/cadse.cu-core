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

import java.text.MessageFormat;

/**
 * The Class CadseIllegalArgumentException.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class CadseIllegalArgumentException extends IllegalArgumentException {

	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= 1L;
	private String				msg;
	private Object[]			args;

	/**
	 * Instantiates a new melusine error.
	 * 
	 * @param msg
	 *            the msg
	 * @param args
	 *            the args
	 */
	public CadseIllegalArgumentException(String msg, Object... args) {
		super(MessageFormat.format(msg, args));
		this.msg = msg;
		this.args = args;
	}

	/**
	 * Instantiates a new melusine error.
	 * 
	 * @param msg
	 *            the msg
	 * @param e
	 *            the e
	 * @param args
	 *            the args
	 */
	public CadseIllegalArgumentException(String msg, Throwable e, Object... args) {
		super(MessageFormat.format(msg, args), e);
		this.msg = msg;
		this.args = args;
	}

	public String getMsg() {
		return msg;
	}

	public Object[] getArgs() {
		return args;
	}
}
