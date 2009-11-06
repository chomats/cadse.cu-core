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
package fr.imag.adele.cadse.core.impl.internal.delta;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.delta.DeleteOperation;
import fr.imag.adele.cadse.core.delta.OperationTypeCst;
import fr.imag.adele.cadse.core.delta.WLWCOperationImpl;

public class DeleteOperationImpl extends WLWCOperationImpl implements DeleteOperation {

	int						_flag;

	public final static int	DELETE_CONTENT			= 0x000001;
	public final static int	DELETE_MAPPING			= 0x000002;
	public final static int	DELETE_INCOMING_LINK	= 0x000004;
	public final static int	DELETE_ANNOTATION_LINK	= 0x000008;
	public final static int	DELETE_PART_LINK		= 0x000010;

	public DeleteOperationImpl(ItemOrLinkDeltaImpl parent) throws CadseException {
		super(OperationTypeCst.DELETE_OPERATION, parent);
	}

	public DeleteOperationImpl(ItemOrLinkDeltaImpl parent, DeleteOperationImpl param) throws CadseException,
			CadseException {
		super(OperationTypeCst.DELETE_OPERATION, parent);
		if (param != null) {
			this._flag = param._flag;
		} else {
			this._flag = 0x1F;
		}
	}

	public DeleteOperationImpl(ItemOrLinkDeltaImpl parent, DeleteOperationImpl param, int flag) throws CadseException,
			CadseException {
		super(OperationTypeCst.DELETE_OPERATION, parent);
		if (param != null) {
			this._flag = param._flag;
		} else {
			this._flag = flag;
		}
	}

	private boolean getFlag(int f) {
		return (_flag & f) != 0;
	}

	private void setflag(boolean flag, int f) {
		if (flag) {
			this._flag |= f;
		} else {
			this._flag &= ~f;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.DeleteOperation#setDeleteContent(boolean)
	 */
	public void setDeleteContent(boolean deleteContent) {
		setflag(deleteContent, DELETE_CONTENT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.DeleteOperation#isDeleteContent()
	 */
	public boolean isDeleteContent() {
		return getFlag(DELETE_CONTENT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.DeleteOperation#setDeleteMapping(boolean)
	 */
	public void setDeleteMapping(boolean deleteMapping) {
		setflag(deleteMapping, DELETE_MAPPING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.DeleteOperation#isDeleteMapping()
	 */
	public boolean isDeleteMapping() {
		return getFlag(DELETE_MAPPING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.DeleteOperation#setDeleteIncomingLink(boolean)
	 */
	public void setDeleteIncomingLink(boolean deleteIncomingLink) {
		setflag(deleteIncomingLink, DELETE_INCOMING_LINK);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.DeleteOperation#isDeleteIncomingLink()
	 */
	public boolean isDeleteIncomingLink() {
		return getFlag(DELETE_INCOMING_LINK);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.DeleteOperation#setDeleteAnnotationLink(boolean)
	 */
	public void setDeleteAnnotationLink(boolean f) {
		setflag(f, DELETE_ANNOTATION_LINK);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.DeleteOperation#isDeleteAnnotationLink()
	 */
	public boolean isDeleteAnnotationLink() {
		return getFlag(DELETE_ANNOTATION_LINK);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.DeleteOperation#setisDeletePartLink(boolean)
	 */
	public void setisDeletePartLink(boolean f) {
		setflag(f, DELETE_PART_LINK);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.delta.DeleteOperation#isDeletePartLink()
	 */
	public boolean isDeletePartLink() {
		return getFlag(DELETE_PART_LINK);
	}

}
