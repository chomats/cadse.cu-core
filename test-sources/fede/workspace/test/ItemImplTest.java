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
package fede.workspace.test;

import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.WorkspaceDomain;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.internal.WorkspaceDomainImpl;


import junit.framework.TestCase;

public class ItemImplTest extends TestCase {
	
	static WorkspaceDomain wd;
	static ItemType wt;
	ItemType config, tool, role, feature, cu, lib, sources, classes;
	LinkType w_config, w_tool, w_role, w_feature, w_cu, w_lib, t_source,
			t_classe, t_use, t_impl, t_uselib, t_usecu, r_source, r_classe,
			r_uselib, r_usecu, c_tool, c_role, c_cu, c_lib, c_feature,
			c_tool_agge, f_source, f_classe, f_uselib, f_usecu;

	Item config1, tool1, tool2, role1,lib1, source_tool1; 
	protected void setUp() throws Exception {
		super.setUp();
		wd = new WorkspaceDomainImpl();
		wt = wd.createWorkspaceType("ExempleWSType");
		config = wd.createItemType("Config",false);
		tool = wd.createItemType("Tool", false);
		role = wd.createItemType("Role", false);
		feature = wd.createItemType("Feature", false);
		cu = wd.createItemType("CU", false);
		lib = wd.createItemType("Lib", false);

		/* contents */
		sources = wd.createItemType("sources", true);
		classes = wd.createItemType("classes", true); 
		
		w_config = wt.createLinkType("configs", LinkType.AGGREGATION, 0, -1, config);
		w_tool = wt.createLinkType("tools", LinkType.AGGREGATION, 0, -1, tool);
		w_role = wt.createLinkType("roles", LinkType.AGGREGATION, 0, -1, role);
		w_feature = wt.createLinkType("features", LinkType.AGGREGATION, 0, -1, feature);
		w_cu = wt.createLinkType("cus", LinkType.AGGREGATION, 0, -1, cu);
		w_lib = wt.createLinkType("libs", LinkType.AGGREGATION, 0, -1, lib);

		t_source = tool.createLinkType("sources", LinkType.CONTAINMENT, 1, 1, sources);
		t_classe = tool.createLinkType("classes", LinkType.CONTAINMENT, 1, 1, classes);
		t_use = tool.createLinkType("uses", LinkType.OTHER, 0, -1, role);
		t_impl = tool.createLinkType("impls", LinkType.OTHER, 1, -1, role);
		t_uselib = tool.createLinkType("use-libs", LinkType.OTHER, 0, -1, lib);
		t_usecu = tool.createLinkType("use-cu", LinkType.OTHER, 0, 1, cu);

		r_source = role.createLinkType("sources", LinkType.CONTAINMENT, 1, 1, sources);
		r_classe = role.createLinkType("classes", LinkType.CONTAINMENT, 1, 1, classes);
		r_uselib = role.createLinkType("use-libs", LinkType.OTHER, 0, -1, lib);
		r_usecu = role.createLinkType("use-cu", LinkType.OTHER, 0, 1, cu);

		c_tool = config.createLinkType("tools", LinkType.OTHER, 0, -1, tool);
		c_role = config.createLinkType("roles", LinkType.OTHER, 0, -1, role);
		c_cu = config.createLinkType("cu", LinkType.OTHER, 1, 1, cu);
		c_lib = config.createLinkType("libs", LinkType.OTHER, 0, -1, lib);
		c_feature = config.createLinkType("features", LinkType.OTHER, 0, -1, lib);
		c_tool_agge = config.createLinkType("tools-agree", LinkType.OTHER, 1, 1, tool);

		f_source = feature.createLinkType("sources", LinkType.CONTAINMENT, 1, 1, sources);
		f_classe = feature.createLinkType("classes", LinkType.CONTAINMENT, 1, 1, classes);
		f_uselib = feature.createLinkType("use-libs", LinkType.OTHER, 0, -1, lib);
		f_usecu = feature.createLinkType("use-cu", LinkType.OTHER, 0, 1, cu);
		
		
		
		wt.addLinkType(w_config);
		wt.addLinkType(w_tool);
		wt.addLinkType(w_role);
		wt.addLinkType(w_feature);
		wt.addLinkType(w_cu);
		wt.addLinkType(w_lib);

		wt.addLinkType(t_source);
		wt.addLinkType(t_classe);
		wt.addLinkType(t_use);
		wt.addLinkType(t_impl);
		wt.addLinkType(t_usecu);
		wt.addLinkType(t_uselib);

		wt.addLinkType(r_source);
		wt.addLinkType(r_classe);
		wt.addLinkType(r_uselib);
		wt .addLinkType(r_usecu);

		wt.addLinkType(c_tool);
		wt.addLinkType(c_role);
		wt.addLinkType(c_cu);
		wt.addLinkType(c_lib);
		wt.addLinkType(c_feature);
		wt.addLinkType(c_tool_agge);

		wt.addLinkType(f_classe);
		wt.addLinkType(f_source);
		wt.addLinkType(f_usecu);
		wt.addLinkType(f_uselib);
		wd.createWorkspace("sample ws", wt);
		Item ws = wd.getCurrentWorkspace();
		config1 = ws.createItem("config1", config, ws, w_config);
		tool1 = ws.createItem("tool1", tool, ws, w_tool);
		tool2 = ws.createItem("tool2", tool, ws, w_tool);
		role1 = ws.createItem("role1", role, ws, w_role);
		lib1 =  ws.createItem("lib1", lib, ws, w_lib);
		source_tool1 = ws.createItem("src-tool1",sources, tool1, t_source);

		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test method for 'fr.imag.adele.cadse.core.internal.ItemImpl.createLink(LinkType, Item)'
	 */
	public void testCreateLinkLinkTypeItem() {
		Link l = tool1.createLink(t_impl,role1);
		assertEquals(t_impl, l.getType());
	}

//	/*
//	 * Test method for 'fr.imag.adele.cadse.core.internal.ItemImpl.createLink(LinkType, String)'
//	 */
//	public void testCreateLinkLinkTypeString() {
//
//	}
//
//	/*
//	 * Test method for 'fr.imag.adele.cadse.core.internal.ItemImpl.delete()'
//	 */
//	public void testDelete() {
//
//	}

}
