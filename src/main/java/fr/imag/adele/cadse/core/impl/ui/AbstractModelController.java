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
package fr.imag.adele.cadse.core.impl.ui;

import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.ui.AbstractUIRunningValidator;
import fr.imag.adele.cadse.core.ui.RunningModelController;
import fr.imag.adele.cadse.core.ui.UIField;
import fr.imag.adele.cadse.core.ui.UIPlatform;
import fr.imag.adele.cadse.core.ui.UIRunningValidator;

/**
 * The Class AbstractModelController.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class AbstractModelController extends AbstractUIRunningValidator implements RunningModelController {

	public UIField _uiField;
	public Object 	_ruiField;
	
	
	public AbstractModelController(Item desc) {
		super(desc);
	}
	
	public AbstractModelController() {
		super(null);
	}
	
	@Override
	public void init(UIPlatform uiPlatform) {	
		_uiPlatform = uiPlatform;
	}
	
	public Item getItem() {
		return _uiPlatform.getItem(getUIField());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IModelController#getUIField()
	 */
	public UIField getUIField() {
		if (_uiField != null)
			return _uiField;
		if (_desc == null)
			return null;
		return (UIField) _desc.getPartParent();
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IModelController#initAfterUI()
	 */
	public void initAfterUI() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IModelController#dispose()
	 */
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IModelController#defaultValue()
	 */
	public Object defaultValue() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IValidateContributor#validValue(fr.imag.adele.cadse.core.ui.UIField,
	 *      java.lang.Object)
	 */
	public boolean validValue(UIField field, Object value) {
		return validValueChanged(field, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IEventListener#notifieSubValueAdded(fr.imag.adele.cadse.core.ui.UIField,
	 *      java.lang.Object)
	 */
	public void notifieSubValueAdded(UIField field, Object added) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IEventListener#notifieSubValueRemoved(fr.imag.adele.cadse.core.ui.UIField,
	 *      java.lang.Object)
	 */
	public void notifieSubValueRemoved(UIField field, Object removed) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.ui.IEventListener#notifieValueDeleted(fr.imag.adele.cadse.core.ui.UIField,
	 *      java.lang.Object)
	 */
	public void notifieValueDeleted(UIField field, Object oldvalue) {
		throw new UnsupportedOperationException();
	}

	

	public boolean isAnonymous() {
		return false;
	}

	@Override
	public Object getValue() {
		return null;
	}
	
	@Override
	public Object getHeritableValue() {
		return null;
	}

	@Override
	public void initAfterUI(UIField field) {		
	}

	public Object visualToModel(Object ret) {
		final UIField uiField = getUIField();
		if (uiField == null) return ret;
		IAttributeType<?> attRef = uiField.getAttributeDefinition();
		
		if (attRef != null) {
			if (attRef.getType() == CadseGCST.LINK_TYPE && attRef.getMax() != 1) {
				return ret;
			}
			try {
				return attRef.convertTo(ret);
			} catch (IllegalArgumentException e) {
				//ignored;
			}
		}
		return ret;
	}


	}
