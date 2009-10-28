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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.imag.adele.cadse.core.ChangeID;
import fr.imag.adele.cadse.core.EventsManager;
import fr.imag.adele.cadse.core.IWorkspaceOperation;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.WSEvent;
import fr.imag.adele.cadse.core.WorkspaceListener;
import fr.imag.adele.cadse.core.WorkspaceListener.ListenerKind;
import fr.imag.adele.cadse.core.delta.ImmutableItemDelta;
import fr.imag.adele.cadse.core.delta.ImmutableWorkspaceDelta;
import fr.imag.adele.cadse.core.impl.CadseIllegalArgumentException;

/**
 * The Class EventsManager.
 * 
 * @author <a href="mailto:stephane.chomat@imag.fr">Stephane Chomat</a>
 */
public class EventsManagerImpl extends Thread implements EventsManager {

	/** The Oper number. */
	private long	OperNumber	= 0;

	/**
	 * The Class Operation.
	 */
	private final class Operation implements IWorkspaceOperation {

		/** The number. */
		long							number;

		/** The events. */
		private ImmutableWorkspaceDelta	events;

		/** The current. */
		private Thread					current;

		/** The number lock. */
		int								numberLock	= 0;

		// ASyncOperationThread operationThread = null;

		/**
		 * Instantiates a new operation.
		 * 
		 * @param currentThread
		 *            the current thread
		 */
		public Operation(Thread currentThread, long number) {
			this.current = currentThread;
			this.events = createImmutableEvents();
			this.number = number;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see fr.imag.adele.cadse.core.IWorkspaceOperation#getCurrentThread()
		 */
		public Thread getCurrentThread() {
			return current;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see fr.imag.adele.cadse.core.IWorkspaceOperation#getEventDelta()
		 */
		public ImmutableWorkspaceDelta getEventDelta() {
			return events;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see fr.imag.adele.cadse.core.IWorkspaceOperation#getNumber()
		 */
		public long getNumber() {
			return number;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see fr.imag.adele.cadse.core.IWorkspaceOperation#getNumberLock()
		 */
		public int getNumberLock() {
			return numberLock;
		}

		/**
		 * Begin operation.
		 * 
		 * @param name
		 *            the name
		 * @param wait
		 *            the wait
		 * 
		 * @return the i workspace operation
		 */
		IWorkspaceOperation beginOperation(String name, boolean wait) {
			if (numberLock == 0 && !wait && lockThread != null && lockThread != current) {
				return null;
			}

			if (numberLock == 0) {
				try {
					domain.beginRule(domain.getLogicalWorkspace()); // prend le
					// verou
					// eclipse
				} catch (RuntimeException e) {
					remove();
					domain.endRule(domain.getLogicalWorkspace());
					throw e;
				}
				if (lockThread != null) {
					throw new RuntimeException("Cannot excute it !! ");
				}
				lockThread = current;
				numberLock = 1;
				mLogger.info("begin[" + number + "] " + name + "(" + numberLock + ") ");
				return this;
			}
			numberLock++;
			mLogger.info("begin[" + number + "]: " + name + "(" + numberLock + ")");
			return this;
		}

		/**
		 * End operation.
		 * 
		 * @param finalEnd
		 *            the final end
		 */
		void endOperation(boolean finalEnd) {

			if (lockThread == null) {
				mLogger.log(Level.SEVERE, "end[" + number + "] : Workspace is not locked !!!");
				throw new CadseIllegalArgumentException("Workspace is not locked !!!");
			}
			if (lockThread != current) {
				mLogger.log(Level.SEVERE, "end[" + number + "] : Workspace is not locked by the current thread !!!");
				throw new CadseIllegalArgumentException("Workspace is not locked by the current thread !!!");
			}
			if (numberLock == 0) {
				mLogger.log(Level.SEVERE, "end[" + number + "] : Workspace is not locked by the current thread !!!",
						new CadseIllegalArgumentException("Workspace is not locked by the current thread !!!"));
				finalEnd = true;
				numberLock = 1;
			}
			if (finalEnd && numberLock != 1) {
				mLogger.severe("end[" + number + "] : missing end operation");
				numberLock = 1;
			}

			if (numberLock == 1) {
				lockThread = null;
				remove();
				// avaibleOperation.signal();
				domain.endRule(domain.getLogicalWorkspace()); // libere le
				// verou eclipse
				mLogger.info("end[" + number + "] : after endRule");

				sendEvents(events);
			} else {
				mLogger.info("end[" + number + "] " + numberLock);
				numberLock--;
			}
		}

		/**
		 * Checks if is run.
		 * 
		 * @return true, if is run
		 */
		boolean isRun() {
			return this.current == lockThread;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * fr.imag.adele.cadse.core.internal.IWorkspaceOperation#notifieChangeEvent
		 * (fr.imag.adele.cadse.core.ChangeID, java.lang.Object)
		 */
		public void notifieChangeEvent(final ChangeID id, final Object... values) {
			WSEvent e = new WSEvent(newVersion(), id, values);
			model_events_log.info(id.toString(values));

			events.addEvent(e);
		}

	}

	/** The m logger. */
	private Logger									mLogger;
	private Logger									model_events_log;

	/** The domain. */
	CadseDomainImpl									domain;

	/** The is stopped. */
	boolean											isStopped			= false;

	/** The lock thread. */
	Thread											lockThread			= null;

	/** The lockoperation. */
	private transient final ReentrantLock			lockoperation		= new ReentrantLock();

	/** The avaible operation. */
	private transient final Condition				avaibleOperation	= lockoperation.newCondition();

	/** The operations. */
	Map<Thread, Operation>							operations			= new HashMap<Thread, Operation>();

	/** The events array. */
	LinkedBlockingQueue<ImmutableWorkspaceDelta>	eventsArray			= new LinkedBlockingQueue<ImmutableWorkspaceDelta>(
																				Integer.MAX_VALUE);

	/** The local register event. */
	boolean											localRegisterEvent	= false;

	/** The local notify event. */
	boolean											localNotifyEvent	= true;

	/** The lock run. */
	boolean											lockRun				= false;

	/**
	 * Instantiates a new events manager.
	 * 
	 * @param domain
	 *            the domain
	 */
	public EventsManagerImpl(CadseDomainImpl domain) {
		super("Workspace events");

		mLogger = Logger.getLogger("CU.Workspace.EventsManager");
		mLogger.info("start");
		model_events_log = Logger.getLogger("Model.Events");

		this.domain = domain;
	}

	long	version	= 0;

	public long newVersion() {
		return ++version;
	}

	/**
	 * Gets the.
	 * 
	 * @return the operation
	 */
	Operation get() {
		synchronized (operations) {
			Thread t = Thread.currentThread();
			Operation o = operations.get(t);
			if (o == null) {
				o = new Operation(t, OperNumber++);
				operations.put(t, o);
			}
			return o;
		}
	}

	/**
	 * Removes the.
	 * 
	 * @return the i workspace operation
	 */
	IWorkspaceOperation remove() {
		synchronized (operations) {
			Thread t = Thread.currentThread();
			IWorkspaceOperation o = operations.remove(t);
			return o;
		}
	}

	/**
	 * Begin operation.
	 * 
	 * @param name
	 *            the name
	 * @param wait
	 *            the wait
	 * 
	 * @return the i workspace operation
	 */
	// ne pas mettre synchronized
	public IWorkspaceOperation beginOperation(String name, boolean wait) {
		return get().beginOperation(name, wait);
	}

	/**
	 * End operation.
	 */
	// ne pas mettre synchronized
	public void endOperation() {
		get().endOperation(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		while (!isStopped) {
			try {
				for (;;) {
					if (isStopped && eventsArray.isEmpty()) {
						mLogger.info("stop");
						break;
					}
					// mLogger.debug(" wait...");
					synchronized (eventsArray) {
						ImmutableWorkspaceDelta events = eventsArray.poll(2, TimeUnit.SECONDS);
						if (events != null && events._events.size() > 0) {
							mLogger.finest("send events...");
							localNotifyChangeEvent(events);
						}
					}
					// mLogger.debug("[eventsManager] sleep...");
					// Thread.sleep(10);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Notifie change event.
	 * 
	 * @param id
	 *            the id
	 * @param values
	 *            the values
	 */
	public void notifieChangeEvent(final ChangeID id, final Object... values) {

		final ReentrantLock lock0 = this.lockoperation;
		lock0.lock();
		try {
			Operation oper = get();
			if (oper.isRun()) {
				oper.notifieChangeEvent(id, values);
			} else {
				mLogger.log(Level.WARNING, "Un �v�nement a �t� envoy� en dehors d'un begin/end operation",
						new Throwable());
				oper.beginOperation("events async", true);
				try {
					oper.notifieChangeEvent(id, values);
				} catch (Throwable e) {
					mLogger.log(Level.SEVERE, "raise an exception", e);
				} finally {
					oper.endOperation(true);
				}
			}
			mLogger.finest(id.toString(values));
		} finally {
			lock0.unlock();
		}
	}

	static interface CallNotifie {
		ListenerKind getKind();

		void notifie() throws Throwable;
	}

	static class WSCallNotifie implements CallNotifie {
		WorkspaceListener		l;
		ImmutableWorkspaceDelta	delta;

		public WSCallNotifie(WorkspaceListener l, ImmutableWorkspaceDelta delta) {
			super();
			this.l = l;
			this.delta = delta;
		}

		public void notifie() throws Throwable {
			if (CadseDomainImpl.isStarted())
				l.workspaceChanged(delta);
	
		}

		public ListenerKind getKind() {
			return l.getKind();
		}

		@Override
		public int hashCode() {
			return l.hashCode() + delta.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof WSCallNotifie) {
				WSCallNotifie c = (WSCallNotifie) obj;
				return l == c.l && c.delta == delta;
			}
			return super.equals(obj);
		}
	}

	/**
	 * Local notify change event.
	 * 
	 * @param values
	 *            the values
	 */
	void localNotifyChangeEvent(ImmutableWorkspaceDelta values) {
		events = new HashSet[2];

		try {
			for (ImmutableItemDelta itemDelta : values.getItems()) {
				Item item = itemDelta.getItem();
				List<WorkspaceListener> listeners2 = item.isResolved() ? item.filter(itemDelta.getFilters(), values)
						: null;
				if (listeners2 != null) {
					for (WorkspaceListener l : listeners2) {
						mLogger.finest("[eventsManager] add call...");
						add(new WSCallNotifie(l, values));
					}
				}
			}

			List<WorkspaceListener> listeners2 = this.domain.getLogicalWorkspace().filter(values.getFilters(), values);
			if (listeners2 != null) {
				for (WorkspaceListener l : listeners2) {
					mLogger.finest("[eventsManager] add call...");
					add(new WSCallNotifie(l, values));
				}
			}

			for (int i = 0; i < events.length; i++) {
				HashSet<CallNotifie> calls = events[i];
				if (calls == null) {
					continue;
				}
				for (CallNotifie callNotifie : calls) {
					try {
						mLogger.finest("[eventsManager] send call...");
						callNotifie.notifie();
					} catch (Throwable t) {
						mLogger.log(Level.SEVERE, "Listener " + callNotifie + " : " + t.getMessage(), t);
						domain.log("error", t.getMessage() == null ? "" : t.getMessage(), t);
					}
				}
				calls.clear();
				events[i] = null;
			}
		} catch (Exception e) {
			mLogger.log(Level.SEVERE, e.getMessage(), e);
		}
		events = null;
	}

	// private ListenerList listeners = new ListenerList();

	HashSet<CallNotifie>[]	events;

	private void add(CallNotifie l) {
		int kind = l.getKind().ordinal();
		if (events[kind] == null) {
			events[kind] = new HashSet<CallNotifie>();
		}
		events[kind].add(l);
	}

	// /**
	// * Run safe.
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
	// public void runSafe(final IWSRunnable action, boolean async, boolean
	// registerevent, boolean sortevent, boolean lock)
	// throws Throwable {
	// beginOperation("runsafe", true);
	// try {
	// Throwable error = null;
	//
	// try {
	// action.run2(new ErrorReporter() {
	// public void cancel() {
	// }
	//
	// public void reportError(Item item, int errorCode, Throwable exception,
	// String description,
	// Object... parameters) {
	// CadseDomainImpl.log(item, errorCode, "error",
	// MessageFormat.format(description, parameters), exception);
	// }
	//
	// public void reportInfo(Item item, int errorCode, Throwable exception,
	// String description,
	// Object... parameters) {
	// CadseDomainImpl.log(item, errorCode, "info",
	// MessageFormat.format(description, parameters),
	// exception);
	// }
	//
	// public void reportProblem(Item item, int errorCode, Throwable exception,
	// String description,
	// Object... parameters) {
	// CadseDomainImpl.log(item, errorCode, "warning",
	// MessageFormat.format(description,
	// parameters), exception);
	//
	// }
	//
	// });
	// } catch (Throwable e1) {
	// error = e1;
	// }
	//
	// if (error != null) {
	// rollback();
	// throw error;
	// }
	// } finally {
	// endOperation();
	// }
	// }

	/**
	 * Rollback.
	 */
	private void rollback() {
		// TODO Auto-generated method stub

	}

	/**
	 * Sets the stopped.
	 * 
	 * @param b
	 *            the new stopped
	 */
	public void setStopped(boolean b) {
		mLogger.info("set stopped  " + b);
		isStopped = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.imag.adele.cadse.core.impl.internal.EventManager#isLocked()
	 */
	public boolean isLocked() {
		synchronized (operations) {
			return operations.size() != 0;
		}
	}

	public void waitEndAsyncEvents(int timeout) throws InterruptedException, TimeoutException {
		if (timeout == 0) {
			timeout = Integer.MAX_VALUE;
		}
		long ends = System.currentTimeMillis() + timeout;

		while (true) {
			long millis = 100;
			long _millis_ends = System.currentTimeMillis() + millis;
			if (_millis_ends > ends) {
				millis = ends - System.currentTimeMillis();
				if (millis < 0) {
					throw new TimeoutException();
				}
			}
			Thread.sleep(millis);
			synchronized (eventsArray) {
				if (eventsArray.size() == 0) {
					return;
				}
			}
		}
	}

	public void sendEvents(ImmutableWorkspaceDelta events) {
		if (events.getEvents().size() > 0) {
			try {
				EventsManagerImpl.this.eventsArray.put(events);
			} catch (InterruptedException e) {
				mLogger.log(Level.SEVERE, "send events has been interrupted" + e.getMessage(), e);
			}
		}
	}

	public ImmutableWorkspaceDelta createImmutableEvents() {
		return new ImmutableWorkspaceDelta(domain, this);
	}

}
