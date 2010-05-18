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
package fr.imag.adele.cadse.core.impl.ui.mc;

import java.text.MessageFormat;
import java.util.List;

import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.ui.UIField;

public abstract class MC_LinkToBoolean extends MC_Link   {
	Item currentItemDest = null;
	
    public MC_LinkToBoolean(boolean mandatory, String msg) {
    	super(mandatory, msg);
    }

    @Override
    public Object getValue() {
    	Object value =  super.getValue();
    	return Boolean.valueOf(value != null);
    }
    
    @Override
    public void initAfterUI(UIField uiPlatform) {
    	/** mise � jour du label */
		Item dest = getDestinationLink();
		if (dest == currentItemDest) return;
		currentItemDest = dest;
		
        if (dest == null) {
        	_uiPlatform.setVisible(getUIField(), false);
        	_uiPlatform.setTextLabel(getUIField(), "");
        } else {
        	_uiPlatform.setVisible(getUIField(), true);
        	String message = (String) getUIField().getLabel();
        	_uiPlatform.setTextLabel(getUIField(), MessageFormat.format(message,dest.getDisplayName()));
        }
    }
    
    @Override
    public void notifieValueChanged(UIField field, Object value) {
    	if (field == getUIField()) {
    		Boolean valueBool = (Boolean) value;
    		try {
				value =  setValue(valueBool.booleanValue()?currentItemDest:null);
			} catch (CadseException e) {
				e.printStackTrace();
			}
    		super.notifieValueChanged(field, value);
    	}
    }
    
    private Link setValue(Item cu) throws CadseException { 
    	LinkType lt = (LinkType) getAttributeDefinition();
    	
        if (lt == null) return null;
        
        Item theCurrentItem = _uiPlatform.getItem(getUIField());
    	
        List<Link> result = theCurrentItem.getOutgoingLinks(lt);
        for (Link l : result) {
			l.delete();
		}
        if (cu == null)
            return null;
        
        Link l ;
        l = theCurrentItem.createLink(lt, cu);
        return l;
    }
    
    

    protected abstract Item getDestinationLink();

  
}
