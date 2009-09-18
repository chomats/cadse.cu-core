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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.ipojo.util.Tracker;
import org.osgi.framework.BundleContext;

import fr.imag.adele.cadse.core.CadseDomain;
import fr.imag.adele.cadse.core.ChangeID;
import fr.imag.adele.cadse.core.CompactUUID;
import fr.imag.adele.cadse.core.IWorkspaceOperation;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.WSEvent;
import fr.imag.adele.cadse.core.delta.ImmutableWorkspaceDelta;
import fr.imag.adele.fede.workspace.as.eclipse.IEclipse;
import fr.imag.adele.fede.workspace.as.initmodel.IInitModel;
import fr.imag.adele.fede.workspace.as.persistence.IPersistence;
import fr.imag.adele.teamwork.db.ModelVersionDBService;

/**
 * The Class WorkspaceDomainImpl.
 * 
 * @author nguyent
 * @version 6
 * @date 26/09/05
 */

public class CadseDomainImpl implements CadseDomain {

	/** The INSTANCE. */
	private static CadseDomainImpl		INSTANCE;

	/** The workspace logique. */
	private LogicalWorkspaceImpl		workspaceLogique;

	/** The events manager. */
	private transient EventsManagerImpl	eventsManager	= null;

	private BundleContext				_cxt;

	private Tracker						_ModelDBTracker;

	private Tracker						_IInitModelTracker;

	private Tracker						_IEclipseTracker;

	private Tracker						_IPersistenceTracker;

	/**
	 * Instantiates a new workspace domain impl.
	 */
	public CadseDomainImpl(BundleContext cxt) {
		Logger mLogger = Logger.getLogger("CU.Workspace.Workspace");
		mLogger.info("create instance");
		_cxt = cxt;
	}

	/**
	 * Inits the.
	 */
	public static void init() {
		System.out.println("***Appel de init dans workspaceDomain Implementation");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.WorkspaceDomain#getWorkspaceLogique()
	 */
	public LogicalWorkspace getLogicalWorkspace() {
		return workspaceLogique;
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
	 * @see fr.imag.adele.cadse.core.WorkspaceDomain#disablePersistance()
	 */
	@Deprecated
	public void disablePersistance() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.WorkspaceDomain#enablePersistance()
	 */
	@Deprecated
	public void enablePersistance() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.WorkspaceDomain#reloadWSContent()
	 */
	@Deprecated
	public void reloadWSContent() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.WorkspaceDomain#isEnablePersistance()
	 */
	@Deprecated
	public boolean isEnablePersistance() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.WorkspaceDomain#notifieChangeEvent(fr.imag.adele.cadse.core.ChangeID,
	 *      java.lang.Object[])
	 */
	public void notifieChangeEvent(ChangeID id, Object... values) {
		eventsManager.notifieChangeEvent(id, values);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.WorkspaceDomain#notifieChangeEvent(fr.imag.adele.cadse.core.ChangeID,
	 *      java.lang.Object[])
	 */
	public void notifieChangeEventSingle(ChangeID id, Object... values) {
		ImmutableWorkspaceDelta im = eventsManager.createImmutableEvents();
		im.addEvent(new WSEvent(System.currentTimeMillis(), id, values));
		eventsManager.sendEvents(im);
	}

	// /**
	// * events sont enregister durant toutes l'action, il ne sont notifier qu'�
	// * la fin si registerevent il sont rejouer (trier si sortenvent /
	// * createnvent, create link, resolve link .../) en cas d'erreur le
	// workspace
	// * est d'en l'�tat du debut
	// *
	// * @param action
	// * the action
	// * @param async
	// * the async
	// * @param registerevent
	// * the registerevent
	// * @param sortevent
	// * the sortevent
	// * @param lock
	// * the lock
	// *
	// * @throws Throwable
	// * the throwable
	// */
	// public void runSafe(IWSRunnable action, boolean async, boolean
	// registerevent, boolean sortevent, boolean lock)
	// throws Throwable {
	// eventsManager.runSafe(action, async, registerevent, sortevent, lock);
	// }

	public void start() {
		INSTANCE = this;
		eventsManager = new EventsManagerImpl(this);
		workspaceLogique = new LogicalWorkspaceImpl(this);
		eventsManager.start();
		Logger mLogger = Logger.getLogger("CU.Workspace.Workspace");
		mLogger.info("start");

		_ModelDBTracker = new Tracker(_cxt, ModelVersionDBService.class.getName(), null);
		_ModelDBTracker.open();

		_IInitModelTracker = new Tracker(_cxt, IInitModel.class.getName(), null);
		_IInitModelTracker.open();

		_IEclipseTracker = new Tracker(_cxt, IEclipse.class.getName(), null);
		_IEclipseTracker.open();

		_IPersistenceTracker = new Tracker(_cxt, IPersistence.class.getName(), null);
		_IPersistenceTracker.open();

	}

	public void stop() {
		INSTANCE = null;
		eventsManager.setStopped(true);
		Logger mLogger = Logger.getLogger("CU.Workspace.Workspace");
		mLogger.info("stop");
		if (unresolvedObject != null) {
			try {
				unresolvedObject.store(new FileOutputStream(propFile), "");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		_ModelDBTracker.close();
		_ModelDBTracker = null;

		_IInitModelTracker.close();
		_IInitModelTracker = null;

		_IEclipseTracker.close();
		_IEclipseTracker = null;

		_IPersistenceTracker.close();
		_IPersistenceTracker = null;
	}

	Properties		unresolvedObject	= null;

	private File	propFile;

	public CompactUUID getUnresolvedId(String key) {
		if (unresolvedObject == null) {
			File l = getLocation();
			unresolvedObject = new Properties();
			propFile = new File(l, ".cadse.unresolved.id.properties");
			if (propFile.exists()) {
				try {
					unresolvedObject.load(new FileInputStream(propFile));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		String id = unresolvedObject.getProperty(key);
		CompactUUID randomUUID = null;
		if (id != null) {
			try {
				return new CompactUUID(id);
			} catch (IllegalArgumentException e) {

			}
		}

		randomUUID = CompactUUID.randomUUID();
		unresolvedObject.put(key, randomUUID.toString());
		System.out.println("*** create unresolved object " + key + ":" + randomUUID);
		return randomUUID;
	}

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
	public void log(Item item, int errorcode, String type, String message, Throwable e) {
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

		final IEclipse ideService = getIdeService();
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
	 * @see fr.imag.adele.cadse.core.WorkspaceDomain#beginOperation(java.lang.String)
	 */
	public IWorkspaceOperation beginOperation(String name) {
		return eventsManager.beginOperation(name, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.WorkspaceDomain#beginOperation(java.lang.String,
	 *      boolean)
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
	public static CompactUUID generateUUID() {
		CompactUUID uuid = CompactUUID.randomUUID();
		return uuid;
	}

	/**
	 * Gets the single instance of WorkspaceDomainImpl.
	 * 
	 * @return single instance of WorkspaceDomainImpl
	 */
	public static CadseDomain getInstance() {
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

	public void waitEndAsyncEvents(int timeout) throws InterruptedException, TimeoutException {
		eventsManager.waitEndAsyncEvents(timeout);
	}

	public ModelVersionDBService getModelVersionDBService() {
		if (_ModelDBTracker == null) {
			return null;
		}

		return (ModelVersionDBService) _ModelDBTracker.getService();
	}

	public IEclipse getIdeService() {
		if (_IEclipseTracker == null) {
			return null;
		}

		return (IEclipse) _IEclipseTracker.getService();
	}

	public IInitModel getInitModelService() {
		if (_IInitModelTracker == null) {
			return null;
		}

		return (IInitModel) _IInitModelTracker.getService();
	}

	public IPersistence getPersistence() {
		if (_IPersistenceTracker == null) {
			return null;
		}

		return (IPersistence) _IPersistenceTracker.getService();
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
}
