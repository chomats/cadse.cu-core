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

package fr.imag.adele.cadse.core.impl.internal;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.imag.adele.cadse.as.platformide.IPlatformIDE;
import fr.imag.adele.cadse.core.CadseDomain;
import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.ChangeID;
import fr.imag.adele.cadse.core.IWorkspaceOperation;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemDescriptionRef;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.WSEvent;
import fr.imag.adele.cadse.core.impl.db.DBLogicalWorkspace;
import fr.imag.adele.cadse.core.impl.db.DBObject;
import fr.imag.adele.cadse.core.transaction.delta.ImmutableWorkspaceDelta;
import fr.imag.adele.fede.workspace.as.initmodel.IInitModel;
import fr.imag.adele.teamwork.db.ModelVersionDBService;
import fr.imag.adele.teamwork.db.ModelVersionDBService2;

/**
 * The Class WorkspaceDomainImpl.
 * 
 * @author nguyent
 * @version 6
 * @date 26/09/05
 */

public class CadseDomainImpl implements CadseDomain {

	/** The INSTANCE. */
	private static CadseDomainImpl INSTANCE;

	/** The workspace logique. */
	private DBLogicalWorkspace _logicalWorkspace;

	/** The events manager. */
	private transient EventsManagerImpl eventsManager = null;

	public static boolean STOPPED = false;
	public static boolean STARTED = false;

	public ModelVersionDBService2 _modelDB2Service;
	public IInitModel _initModelService;
	public IPlatformIDE _platformService;
	public ModelVersionDBService _modelDBService;

	/**
	 * Instantiates a new workspace domain impl.
	 */
	public CadseDomainImpl() {
		Logger mLogger = Logger.getLogger("CU.Workspace.Workspace");
		mLogger.info("create instance");
	}

	/**
	 * Inits the.
	 */
	public static void init() {
		System.out
				.println("***Appel de init dans workspaceDomain Implementation");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.WorkspaceDomain#getWorkspaceLogique()
	 */
	public DBLogicalWorkspace getLogicalWorkspace() {
		return _logicalWorkspace;
	}

	/**
	 * Get location.
	 * 
	 * @return the location of the workspace domain
	 */
	public File getLocation() {
		return getIdeService().getLocation();
	}

	// -------------------------------------------------//

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.WorkspaceDomain#notifieChangeEvent(fr.imag.adele
	 * .cadse.core.ChangeID, java.lang.Object[])
	 */
	public void notifieChangeEvent(ChangeID id, Object... values) {
		eventsManager.notifieChangeEvent(id, values);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.WorkspaceDomain#notifieChangeEvent(fr.imag.adele
	 * .cadse.core.ChangeID, java.lang.Object[])
	 */
	public void notifieChangeEventSingle(ChangeID id, Object... values) {
		ImmutableWorkspaceDelta im = eventsManager.createImmutableEvents();
		im.addEvent(new WSEvent(System.currentTimeMillis(), id, values));
		eventsManager.sendEvents(im);
	}

	public void start() {
		STARTED = true;
		STOPPED = false;
		INSTANCE = this;
		eventsManager = new EventsManagerImpl(this);
		_logicalWorkspace = new DBLogicalWorkspace(this);
		DBObject._dblw = _logicalWorkspace;
		eventsManager.start();
		Logger mLogger = Logger.getLogger("CU.Workspace.Workspace");
		mLogger.info("start");
	}

	public void stop() {
		STARTED = false;
		STOPPED = true;
		Logger mLogger = Logger.getLogger("CU.Workspace.Workspace");
		eventsManager.setStopped(true);
		try {
			eventsManager.waitEndAsyncEvents(2000);
		} catch (InterruptedException e1) {
			mLogger.log(Level.SEVERE, "Interupted !!!", e1);
			eventsManager.stop();
		} catch (TimeoutException e1) {
			mLogger.log(Level.SEVERE, "Cannot send all events !!!", e1);
			eventsManager.stop();
		}
		if (eventsManager.isAlive())
			mLogger.log(Level.WARNING, "Events manager is allready alive");

		// if (unresolvedObject != null) {
		// try {
		// unresolvedObject.store(new FileOutputStream(propFile), "");
		// } catch (FileNotFoundException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		DBObject._dblw = null;
		INSTANCE = null;
		mLogger.info("stop");

	}

	// Properties unresolvedObject = null;

	// private File propFile;
	//
	// public UUID getUnresolvedId(String key) {
	// if (unresolvedObject == null) {
	// File l = getLocation();
	// unresolvedObject = new Properties();
	// propFile = new File(l, ".cadse.unresolved.id.properties");
	// if (propFile.exists()) {
	// try {
	// unresolvedObject.load(new FileInputStream(propFile));
	// } catch (FileNotFoundException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// }
	//
	// String id = unresolvedObject.getProperty(key);
	// UUID randomUUID = null;
	// if (id != null) {
	// try {
	// return new UUID(id);
	// } catch (IllegalArgumentException e) {
	//
	// }
	// }
	//
	// randomUUID = UUID.randomUUID();
	// unresolvedObject.put(key, randomUUID.toString());
	// System.out.println("*** create unresolved object " + key + ":" +
	// randomUUID);
	// return randomUUID;
	// }

	/**
	 * Log.
	 * 
	 * @param item
	 *            the item
	 * @param errorcode
	 *            the errorcode
	 * @param type
	 *            the type
	 * @param message
	 *            the message
	 * @param e
	 *            the e
	 */
	public void log(Item item, int errorcode, String type, String message,
			Throwable e) {
		Logger mLogger = Logger.getLogger("CU.Workspace.Workspace");
		mLogger.log(Level.SEVERE, message, e);

		getIdeService().log(type, message, e, item);
	}

	/**
	 * Log.
	 * 
	 * @param type
	 *            the type
	 * @param message
	 *            the message
	 * @param e
	 *            the e
	 */
	public void log(String type, String message, Throwable e) {
		Logger mLogger = Logger.getLogger("CU.Workspace.Workspace");
		mLogger.log(Level.SEVERE, message, e);

		final IPlatformIDE ideService = getIdeService();
		if (ideService != null) {
			ideService.log(type, message, e);
		}
	}

	/**
	 * Log.
	 * 
	 * @param m_type
	 *            the type
	 * @param message
	 *            the message
	 * @param e
	 *            the e
	 */
	public void error(Item item, String message, Throwable e) {
		Logger mLogger = Logger.getLogger("CU.Workspace.Workspace");
		mLogger.log(Level.SEVERE, message, e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.WorkspaceDomain#isLocked()
	 */
	public boolean isLocked() {
		return eventsManager.isLocked();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.WorkspaceDomain#endOperation()
	 */
	public void endOperation() {
		eventsManager.endOperation();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.WorkspaceDomain#beginOperation(java.lang.String)
	 */
	public IWorkspaceOperation beginOperation(String name) {
		return eventsManager.beginOperation(name, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.imag.adele.cadse.core.WorkspaceDomain#beginOperation(java.lang.String,
	 * boolean)
	 */
	public IWorkspaceOperation beginOperation(String name, boolean wait) {
		return eventsManager.beginOperation(name, wait);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.WorkspaceDomain#beginRule(java.lang.Object)
	 */
	public void beginRule(Object rule) {
		getIdeService().beginRule(rule);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.WorkspaceDomain#endRule(java.lang.Object)
	 */
	public void endRule(Object rule) {
		getIdeService().endRule(rule);
	}

	/**
	 * Generate uuid.
	 * 
	 * @return the compact uuid
	 */
	public static UUID generateUUID() {
		UUID uuid = UUID.randomUUID();
		return uuid;
	}

	/**
	 * Gets the single instance of WorkspaceDomainImpl.
	 * 
	 * @return single instance of WorkspaceDomainImpl
	 */
	public static CadseDomainImpl getInstance() {
		return INSTANCE;
	}

	/**
	 * Load class.
	 * 
	 * @param cstClass
	 *            the cst class
	 * 
	 * @return the class
	 */
	public Class loadClass(String cstClass) {
		// TODO Auto-generated method stub
		return null;
	}

	public void waitEndAsyncEvents(int timeout) throws InterruptedException,
			TimeoutException {
		eventsManager.waitEndAsyncEvents(timeout);
	}

	public ModelVersionDBService getModelVersionDBService() {
		return _modelDBService;
	}

	public IPlatformIDE getIdeService() {
		return _platformService;
	}

	public IInitModel getInitModelService() {
		return _initModelService;
	}

	public void refresh(Item item) {
		getIdeService().refresh(item);
	}

	public void setReadOnly(Item item, boolean flag) {
		getIdeService().setReadOnly(item, flag);
	}

	public EventsManagerImpl getEventsManager() {
		return eventsManager;
	}

	@Override
	public Item createUnresolvedItem(ItemType itemType, String name, UUID id)
			throws CadseException {
		return this._logicalWorkspace.loadItem(new ItemDescriptionRef(id,
				itemType, name, name));
	}

	public static boolean isStopped() {
		return STOPPED;
	}

	public static boolean isStarted() {
		return STARTED;
	}

	public ModelVersionDBService2 getDB() {
		return _modelDB2Service;
	}

	public boolean inDevelopmentMode() {
		return _platformService != null && _platformService.inDevelopmentMode();
	}

}
