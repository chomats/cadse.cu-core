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
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.WorkspaceDomain;
import fr.imag.adele.cadse.core.LinkType;
import fr.imag.adele.cadse.core.internal.WorkspaceDomainImpl;


/**
 * @author nguyent
 *
 */
public class TestMain {
	
	static WorkspaceDomain wd;
	static ItemType wt;
	ItemType config, tool, role, feature, cu, lib, sources, classes;
	LinkType w_config, w_tool, w_role, w_feature, w_cu, w_lib, t_source,
			t_classe, t_use, t_impl, t_uselib, t_usecu, r_source, r_classe,
			r_uselib, r_usecu, c_tool, c_role, c_cu, c_lib, c_feature,
			c_tool_agge, f_source, f_classe, f_uselib, f_usecu;

	Item config1, tool1, tool2, role1,lib1, source_tool1; 

	TestMain() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		TestMain tm =  new TestMain();
		try{
			   tm.senario_03(8);
			}
		catch(IllegalArgumentException e){
			e.printStackTrace();
			System.out.println("Test ok");
			return;
		}
		catch(Throwable e){
			e.printStackTrace();
		}
		System.out.println("Test fail");
	}

	
	/**
	 * cr�er un domain de workspace.
	 * @param flag 
	 */
	private void senario_00(int flag){
		wd = new WorkspaceDomainImpl();
	}
	
	/**
	 * cr�er un type de workspace. 
	 * @param flag
	 */
	private void senario_01(int flag){
		senario_00(0);
		switch(flag){
		case 1:
			wt = wd.createWorkspaceType(null);
			break;
		case 2:
			wt = wd.createWorkspaceType("");
			break;
		case 3:
			wt = wd.createWorkspaceType("ExempleWSType");
			wt = wd.createWorkspaceType("ExempleWSType");
			break;
		default:
			wt = wd.createWorkspaceType("ExempleWSType");
		}
	}
	
	/**
	 * cr�er un type d'item
	 * @param flag
	 */
	private void senario_02(int flag) {
		senario_01(0);
		switch(flag){
		case 1:
			config = wd.createItemType(null,false);
			break;
		case 2:
			config = wd.createItemType("",false);
			break;
		case 3:
			config = wd.createItemType("Config",false);
			config = wd.createItemType("Config",false);
			break;
		default:
			config = wd.createItemType("Config",false);
			tool = wd.createItemType("Tool", false);
			role = wd.createItemType("Role", false);
			feature = wd.createItemType("Feature", false);
			cu = wd.createItemType("CU", false);
			lib = wd.createItemType("Lib", false);

			/* contents */
			sources = wd.createItemType("sources", true);
			classes = wd.createItemType("classes", true); 
		}		
	}

	/**
	 * cr�er un type de lien
	 * @param flag
	 */
	private void senario_03(int flag){
		senario_02(0);
		switch(flag){
		case 1:
			w_config = wt.createLinkType(null, LinkType.AGGREGATION, 0, -1, config);
			break;
		case 2:
			w_config = wt.createLinkType("", LinkType.AGGREGATION, 0, -1, config);
			break;
		case 3:
			w_config = wt.createLinkType("configs",  LinkType.AGGREGATION , -1, -1, config);
			break;
		case 4:
			w_config = wt.createLinkType("configs",  LinkType.AGGREGATION , 1, 0, config);
			break;
		case 5:
			w_config = wt.createLinkType("configs",  LinkType.AGGREGATION , 0, -2, config);
			break;
		case 6:
			w_config = wt.createLinkType("configs",  LinkType.AGGREGATION , 0, -1,(ItemType) null);
			break;
		case 7:
			w_config = wt.createLinkType("configs", LinkType.AGGREGATION, 0, -1, config);
			w_config = wt.createLinkType("configs", LinkType.AGGREGATION, 0, -1, config);
			break;
		case 8:
			w_config = wt.createLinkType("configs", LinkType.AGGREGATION, 0, -1, config);
			w_config = wt.createLinkType("configs", LinkType.CONTAINMENT, 0, -1, config);
			break;
		default:
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
		}

	}
		
	/**
	 * ajouter un type d'item.
	 * @param flag
	 */
	private void senario_04(int flag){
		senario_03(0);
		switch(flag){
		case 1:
			break;
		case 2:
			break;
		case 3:
			break;
		default:
			;
		}
	}
	
	/**
	 * ajouter un type de lien.
	 * @param flag
	 */
	private void senario_05(int flag){
		senario_04(0);
		//senario_03(0);
		switch(flag){
		case 1:
			wt.addLinkType(null);
			break;
		case 2:
			wt.addLinkType(w_config);
			wt.addLinkType(w_config);
			break;
		default:
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
		}
	}
	
	/**
	 * creer un workspace
	 */
	private void senario_06(int flag){
		senario_05(0);
		switch(flag){
		case 1:
			wd.createWorkspace(null, wt);
		break;
		case 2:
			wd.createWorkspace("", wt);
		break;
		case 3:
			wd.createWorkspace("sample ws", null);
		break;
		case 4:
			wd.createWorkspace("sample ws1", wt);
			wd.createWorkspace("sample ws2", wt);
		break;
		default:
			wd.createWorkspace("sample ws", wt);
		}
	}
	
	/**
	 * cr�er un item.
	 * @param flag
	 */
	private void senario_07(int flag){
		senario_06(0);
		Item ws = wd.getCurrentWorkspace();
		switch(flag){
		case 1:
			config1 = ws.createItem(null, config, ws, w_config);
		break;
		case 2:
			config1 = ws.createItem("", config, ws, w_config);
		break;
		case 3:
			config1 = ws.createItem("config1", null, ws, w_config);
		break;
		case 4:
			config1 = ws.createItem("config1", config, null, w_config);
			break;
		case 5:
			config1 = ws.createItem("config1", config, ws, null);
			break;
		case 6:
			config1 = ws.createItem("config1", config, ws, w_tool);
			break;
		case 7:
			config1 = ws.createItem("config1", config, ws, w_config);
			config1 = ws.createItem("config1", config, ws, w_config);
		break;
		case 8:
			ItemType newIt = wd.createItemType("newIt", false);
			config1 = ws.createItem("config1", newIt, ws, w_config);		
		break;
		default:
			config1 = ws.createItem("config1", config, ws, w_config);
			tool1 = ws.createItem("tool1", tool, ws, w_tool);
			tool2 = ws.createItem("tool2", tool, ws, w_tool);
			role1 = ws.createItem("role1", role, ws, w_role);
			lib1 =  ws.createItem("lib1", lib, ws, w_lib);
			source_tool1 = ws.createItem("src-tool1",sources, tool1, t_source);
		}
	}
	
	/**
	 * cr�er un lien.
	 */
	private void senario_08(int flag){
		senario_07(0);
		Item ws = wd.getCurrentWorkspace();
		switch(flag){
		case 1:
			config1.createLink(null,tool1); // parameter link type null.
			break;
		case 2:
			Item item = null;
			config1.createLink(c_tool,item); // parameter destination est un item null.
			break;
		case 3:
			String i = null;
			config1.createLink(c_tool,i); // parameter destination est un String null.
			break;
		case 4:
			config1.createLink(c_tool, ""); // parameter destination est une chaine vide.
			break;
		case 5:
			config1.createLink(w_tool,tool1); // type de la source config1 est incompatible avec type de la source de w_tool.
			break;
		case 6:
			config1.createLink(c_tool,role1); // type de la destination role1 est incompatible avec type de la destination de c_tool.    
			break;
		case 7:
			LinkType newLt = config.createLinkType("newLt1", 0, 0 ,-1, config); 			
			wt.addLinkType(newLt);		
			config1.createLink(newLt,config1); //vers lui m�me
			break;
		case 8:
			config1.createLink(c_tool,tool1); // cr�er deux fois
			config1.createLink(c_tool,tool1);
			break;
		case 9:
			LinkType newLt1 = config.createLinkType("newLt1", 0, 0 ,-1, tool); // newLt n'est pas registr� dans le workspace type.
			config1.createLink(newLt1,tool1);
			break;
		case 10:
			LinkType newLt2 = config.createLinkType("newLt2", 0, 0 ,1, tool); //Le nombre d'instances de newLt1 maximum est 1.			
			wt.addLinkType(newLt2);
			config1.createLink(newLt2,tool1);  // cr�er la premiere instance de newLt -> ok.
			config1.createLink(newLt2,tool2);  // cr�er la deuxieme instance de newLt1 -> �a provoque d'erreur , pq le nombre d'instances de newLt1 ne peut pas d�pass� 1.
		break;
		default:
			config1.createLink(c_tool,tool1);
			tool1.createLink(t_impl,role1);
		
		}
	}
	
	/**
	 * supprimer le workspace
	 * @param flag
	 */
	private void senario_09(int flag){
		senario_07(0);
		Item ws = wd.getCurrentWorkspace();
		switch(flag){
		case 1:
			ws.delete(true);
			Item ws2 = wd.getCurrentWorkspace();
			if (ws2 == null)
				throw new IllegalArgumentException("ws2 is null"); 
			break;
		}
	}

	/**
	 * supprimer un item
	 * @param flag
	 */
	private void senario_10(int flag){
		senario_08(0);
		Item ws = wd.getCurrentWorkspace();
		switch(flag){
		case 1:
			String id1 = tool1.getId();
			String id2 = source_tool1.getId();
			tool1.delete(true);
			if (ws.getItem(id1) == null && ws.getItem(id2) == null)
				throw new IllegalArgumentException("tool1 was deleted"); 
		break;
		}
	}
	/**
	 * supprimer un liens
	 * @param flag
	 */
	private void senario_11(int flag){
		senario_08(0);
		Item ws = wd.getCurrentWorkspace();
		switch(flag){
		case 1:
			Link l = tool2.createLink(t_source,source_tool1);
			String id = source_tool1.getId();
			l.delete();
			if (ws.getItem(id) == null)
				throw new IllegalArgumentException("lien was deleted"); 
		break;
		}
	}
	
	
}
