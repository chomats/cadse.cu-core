package fr.imag.adele.cadse.core.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream.GetField;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.bind.JAXBException;


import adele.util.io.FileUtil;
import adele.util.io.ZipUtil;
import fr.imag.adele.cadse.core.CadseException;
import fr.imag.adele.cadse.core.CadseRuntime;
import java.util.UUID;

import fr.imag.adele.cadse.core.CadseGCST;
import fr.imag.adele.cadse.core.Item;
import fr.imag.adele.cadse.core.ItemType;
import fr.imag.adele.cadse.core.Link;
import fr.imag.adele.cadse.core.LogicalWorkspace;
import fr.imag.adele.cadse.core.ProjectAssociation;
import fr.imag.adele.cadse.core.attribute.IAttributeType;
import fr.imag.adele.cadse.core.transaction.LogicalWorkspaceTransaction;
import fr.imag.adele.cadse.core.transaction.delta.ItemDelta;
import fr.imag.adele.cadse.core.transaction.delta.LinkDelta;
import fr.imag.adele.cadse.core.transaction.delta.SetAttributeOperation;

public class ExportImportCadseFunction {

	String MF_template = "Manifest-Version: 1.0\n"+
"Bundle-ManifestVersion: 2\n"+
"Bundle-Name: #BUNDLE-NAME#\n"+
"Bundle-SymbolicName: #BUNDLE-NAME#\n"+
"Bundle-Version: 2.3.0\n"+
"Bundle-DocURL: http://www-adele.imag.fr/\n"+
"Import-Package: fr.imag.adele.cadse.core;version=\"2.3\",\n"+
" fr.imag.adele.cadse.core.transaction;version=\"2.3\",\n"+
" fr.imag.adele.cadse.core.transaction.delta;version=\"2.3\",\n"+
" fr.imag.adele.cadse.core.impl;version=\"2.3\"\n"+
"Bundle-Activator: fr.imag.adele.cadse.core.impl.BundleInstallActivator\n";
	
	
	protected Set<Link>						outgoinglinks				= new HashSet<Link>();
	protected Set<ItemType>					requireItemType				= new HashSet<ItemType>();
	protected Set<CadseRuntime>				requireCadse				= new HashSet<CadseRuntime>();

	
	
	
	protected final HashSet<Item>				items						= new HashSet<Item>();

	/**
	 * Association project-name to item id
	 */
	protected HashMap<String, UUID>			projectsMap					= new HashMap<String, UUID>();

	/**
	 * Association file to zip entry path
	 */
	protected HashMap<File, String>			files;


	protected String exportNameFile;

	/** The Constant MELUSINE_DIR. */
	public static final String		MELUSINE_DIR				= ".melusine-dir/";

	/** The Constant MELUSINE_DIR_CADSENAME. */
	public static final String		MELUSINE_DIR_CADSENAME		= ".melusine-dir/cadsename";

	/** The Constant MELUSINE_DIR_CADSENAME_ID. */
	public static final String		MELUSINE_DIR_CADSENAME_ID	= ".melusine-dir/cadsename.id";
	
	public static final String		MELUSINE_DIR_CADSENAME_IDS	= ".melusine-dir/cadsename.ids";

	/** The Constant MELUSINE_DIR_CADSENAME_ID. */
	public static final String		REQUIRE_CADSEs				= ".melusine-dir/require-cadses";
	/** The Constant MELUSINE_DIR_CADSENAME_ID. */
	public static final String		REQUIRE_ITEM_TYPEs			= ".melusine-dir/require-its";
	/** The Constant MELUSINE_DIR_CADSENAME_ID. */

	public static final String		PROJECTS					= ".melusine-dir/projects";

	/**
	 * Le format du fichier <exportNameFile><postFix><-yyyy-MM-dd-HHmm>.zip
	 * @param directory where the zip file is put
	 * @param exportNameFile the name of zip file
	 * @param postFix null if none 
	 * @param tstamp true if want add postfix tstamp. 
	 * @param rootItems
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public File exportItems(File directory, String exportNameFile, String postFix, boolean tstamp, Item... rootItems)
			throws FileNotFoundException, IOException {
		this.exportNameFile = exportNameFile;
		
		File pf = null;
		CadseCore.getCadseDomain().beginOperation("Export cadse");
		try {

			if (postFix == null)
				postFix = "";
			pf = new File(directory, exportNameFile + postFix+ ".zip");
			if (tstamp) {
				Date d = new Date(System.currentTimeMillis());
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HHmm");
				System.out.println(formatter.format(d));
				pf = new File(directory, exportNameFile + postFix+"-" + formatter.format(d) + ".zip");
			}
			ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(pf));

			files = new HashMap<File, String>();

			beginTask("export cadse items ", 3);

			File wsFile = CadseCore.getCadseDomain().getLocation();
			File melusineDir = new File(wsFile, ".cadse");
			UUID[] uuidRoot = new UUID[rootItems.length];
			int i = 0;
			for (Item rootItem : rootItems) {
				setTaskName(rootItem.getName());
				getPersistanceFileAll(melusineDir, rootItem);
				uuidRoot[i++] = rootItem.getId();
			}

			includesContents(files);
			worked(1);
			setTaskName("zip entries...");
			ZipUtil.addEntryZip(
					outputStream, new ByteArrayInputStream(getManifest().getBytes()), "META-INF/MANIFEST.MF",-1);
			ZipUtil.zip(files, outputStream);

			worked(2);

			ArrayList<Object> requireCadseIds = new ArrayList<Object>();
			for (CadseRuntime cr : requireCadse) {
				if (items.contains(cr)) {
					continue;
				}

				requireCadseIds.add(cr.getId());
				requireCadseIds.add(cr.getName());
				requireCadseIds.add(cr.getQualifiedName());
				requireCadseIds.add(cr.getVersion());
			}

			// format  UUID, name, qname, int version
			ZipUtil.addEntryZip(outputStream, new ByteArrayInputStream(toByteArray(requireCadseIds.toArray())), REQUIRE_CADSEs,
					-1);

			ArrayList<Object> requireItemIds = new ArrayList<Object>();
			for (ItemType cr : requireItemType) {
				if (items.contains(cr)) {
					continue;
				}
				requireItemIds.add(cr.getId());
				requireItemIds.add(cr.getName());
				requireItemIds.add(cr.getQualifiedName());
				requireItemIds.add(cr.getVersion());
			}

			// format  UUID, name, qname, int version
			ZipUtil.addEntryZip(outputStream, new ByteArrayInputStream(toByteArray(requireItemIds.toArray())),
					REQUIRE_ITEM_TYPEs, -1);

			ZipUtil.addEntryZip(outputStream, new ByteArrayInputStream(toByteArray(projectsMap)), PROJECTS, -1);
			
			ZipUtil.addEntryZip(outputStream, new ByteArrayInputStream(toByteArray(uuidRoot)), MELUSINE_DIR_CADSENAME_IDS, -1);
			
			addExtraEntry(outputStream);

			worked(3);
			outputStream.close();
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			CadseCore.getCadseDomain().endOperation();
		}
		return pf;
	}

	protected void addExtraEntry(ZipOutputStream outputStream) {
	}

	protected void setTaskName(String name) {
	}

	protected void worked(int work) {
	}

	protected void beginTask(String name, int totalWork) {
	}
	
	protected String getManifest() {
		return MF_template.replaceAll("#BUNDLE-NAME#", exportNameFile);
	}

	private byte[] toByteArray(Object v) throws IOException {
		ByteArrayOutputStream cd = new ByteArrayOutputStream();
		ObjectOutputStream outObj = new ObjectOutputStream(cd);

		outObj.writeObject(v);
		outObj.flush();
		outObj.close();

		return cd.toByteArray();
	}

	protected void includesContents(HashMap<File, String> files) {
	}

	/**
	 * Gets the persistance file all.
	 * 
	 * @param melusineDir
	 *            the melusine dir
	 * @param item
	 *            the item
	 * @param files
	 *            the files
	 * @param items
	 *            the items
	 * 
	 * @return the persistance file all
	 */
	protected void getPersistanceFileAll(File melusineDir, Item item) {

		if (items.contains(item)) {
			System.err.println("entry duplicate " + item.getId() + " " + item.getQualifiedName());
			return;
		}

		if (exclude(item)) return;
		
		items.add(item);
		ItemType it = item.getType();
		if (it != null) {
			if (!requireItemType.contains(it)) {
				requireItemType.add(it);
				CadseRuntime cr = it.getCadse();
				if (cr != null) {
					requireCadse.add(cr);
				}
			}
		}
		computeContentFromItem(item);

		File xmlfile = new File(melusineDir, item.getId().toString() + ".ser");
		files.put(xmlfile, MELUSINE_DIR);
		xmlfile = new File(melusineDir, item.getId().toString() + ".xml");
		if (xmlfile.exists()) {
			files.put(xmlfile, MELUSINE_DIR);
		}

		List<? extends Link> links = item.getOutgoingLinks();
		for (Link link : links) {
			if (!link.getLinkType().isPart()) {
				if (!items.contains(link.getDestination())) {
					outgoinglinks.add(link);
				}
				continue;
			}
			if (!link.isLinkResolved()) {
				outgoinglinks.add(link);
				continue;
			}
			getPersistanceFileAll(melusineDir, link.getDestination());
		}
	}

	protected boolean exclude(Item item) {
		return false;
	}

	protected void computeContentFromItem(Item item) {
	}

	public Set<Link> getOutgoinglinks() {
		return outgoinglinks;
	}

	public Set<ItemType> getRequireItemType() {
		return requireItemType;
	}

	public Set<CadseRuntime> getRequireCadse() {
		return requireCadse;
	}

	public HashSet<Item> getItemsHash() {
		return items;
	}

	/**
	 * Read cadse uuid.
	 * 
	 * @param f
	 *            the root directory
	 * 
	 * @return the compact uuid
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException
	 */
	public Object readObject(File pf, String key) throws IOException, ClassNotFoundException {
		if (!pf.exists())
			return null;
		File data = new File(pf, key);
		if (!data.exists())
			return null;
		return readObject(new FileInputStream(data));
	}
	
	public Object readObject(InputStream input) throws IOException, ClassNotFoundException {
		ObjectInputStream isr = new ObjectInputStream(input);
		try {
			Object o = isr.readObject();
			return o;
		} finally {
			isr.close();
		}
	}

	public void importCadseItems(File file) throws MalformedURLException, IOException, JAXBException, CadseException, ClassNotFoundException {
		importCadseItems(file.toURL());
	}
	
	/**
	 * Import a zip file and add new item...
	 * Delete project original project 
	 * @param file a zip file
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws JAXBException
	 * @throws CadseException
	 *  - Internal error : cannot find .melusine-dir
	 * 	- Missing cadse {0} version {1} ({2}, {3})
	 *  - Item {0} version {1} is not a cadse ! ({2}, {3})
	 *  - Missing item type {0} version {1} ({2}, {3})
	 *  - Item {0} version {1} is not an item type ! ({2}, {3})
	 * @throws ClassNotFoundException
	 */
	public void importCadseItems(URL file) throws IOException, MalformedURLException,
			JAXBException, CadseException, ClassNotFoundException {
		CadseCore.getCadseDomain().beginOperation("Import cadse");
		File pf = null;
		try {
			File dir = CadseCore.getCadseDomain().getLocation();
			pf = createTempDirectory(dir);
			pf.mkdirs();

			ZipUtil.unzip(file.openStream(), pf);
			String cadse = readCadseFolder(pf);
			if (cadse != null) {
				final File newDir = new File(dir, cadse);
				FileUtil.deleteDir(newDir);
				pf.renameTo(newDir);
				pf = newDir;
			}
			
			File melusineDir = new File(pf, ".melusine-dir");
			if (!melusineDir.exists()) throw new CadseException("Internal error : cannot find .melusine-dir");
			File[] filesserxml = melusineDir.listFiles();
			Collection<URL> itemdescription = new ArrayList<URL>();
			for (File fser : filesserxml) {
				if (fser.getName().endsWith(".ser")) {
					itemdescription.add(fser.toURI().toURL());
				}
			}
			
			LogicalWorkspace lw = CadseCore.getLogicalWorkspace();
			if (cadse == null) {
				Object[] requireCadseIds = (Object[]) readObject(pf,
						REQUIRE_CADSEs);
				for (int i = 0; i < requireCadseIds.length;) {
					UUID id = (UUID) requireCadseIds[i++];
					String name = (String) requireCadseIds[i++];
					String qname = (String) requireCadseIds[i++];
					Integer version = (Integer) requireCadseIds[i++];
					Item cr = lw.getItem(id);
					if (cr == null) {
						throw new CadseException(
								"Missing cadse {0} version {1} ({2}, {3})",
								name, version, qname, id);
					}
					if (!(cr instanceof CadseRuntime)) {
						throw new CadseException(
								"Item {0} version {1} is not a cadse ! ({2}, {3})",
								name, version, qname, id);
					}
				}
				Object[] requireItemTypeIds = (Object[]) readObject(pf,
						REQUIRE_ITEM_TYPEs);
				for (int i = 0; i < requireItemTypeIds.length;) {
					UUID id = (UUID) requireItemTypeIds[i++];
					String name = (String) requireItemTypeIds[i++];
					String qname = (String) requireItemTypeIds[i++];
					Integer version = (Integer) requireItemTypeIds[i++];
					Item cr = lw.getItem(id);
					if (cr == null) {
						throw new CadseException(
								"Missing item type {0} version {1} ({2}, {3})",
								name, version, qname, id);
					}
					if (!(cr instanceof ItemType)) {
						throw new CadseException(
								"Item {0} version {1} is not an item type ! ({2}, {3})",
								name, version, qname, id);
					}
				}
			}
			Collection<ProjectAssociation> projectAssociationSet = new ArrayList<ProjectAssociation>();
			projectsMap = (HashMap<String, UUID>) readObject(pf, PROJECTS);
			if (projectsMap != null) {
				for (Map.Entry<String, UUID> e : projectsMap.entrySet()) {
					ProjectAssociation pa = new ProjectAssociation(
							e.getValue(), e.getKey());
					projectAssociationSet.add(pa);
					File destProject = new File(dir, e.getKey());
					if (destProject.exists()) {
						destProject.delete();
					}
						
					new File(pf, e.getKey()).renameTo(destProject);
				}
			} else {
				UUID uuid = readCadseUUIDFolder(pf);
				ProjectAssociation pa = new ProjectAssociation(uuid, cadse);
				projectAssociationSet.add(pa);
			}
			LogicalWorkspaceTransaction transaction = lw.createTransaction();
			List<ItemDelta> Itemsdelta = transaction.loadItems(itemdescription);
			migrate(transaction);
			transaction.commit(false, true, false, projectAssociationSet);
			checkAction(Itemsdelta, transaction);
			
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (pf != null)
				pf.delete();
			CadseCore.getCadseDomain().endOperation();
		}
	}
	
	
	/**
	 * Import a zip file and add new item...
	 * Delete project original project 
	 * @param file a zip file
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws JAXBException
	 * @throws CadseException
	 *  - Internal error : cannot find .melusine-dir
	 * 	- Missing cadse {0} version {1} ({2}, {3})
	 *  - Item {0} version {1} is not a cadse ! ({2}, {3})
	 *  - Missing item type {0} version {1} ({2}, {3})
	 *  - Item {0} version {1} is not an item type ! ({2}, {3})
	 * @throws ClassNotFoundException
	 */
	public void importCadseItems(Enumeration<URL> enumURLs) throws IOException, MalformedURLException,
			JAXBException, CadseException, ClassNotFoundException {
		CadseCore.getCadseDomain().beginOperation("Import cadse");
		Collection<URL> itemdescription = new ArrayList<URL>();
		LogicalWorkspace lw = CadseCore.getLogicalWorkspace();
		Collection<ProjectAssociation> projectAssociationSet = new ArrayList<ProjectAssociation>();
		
		try {
			ArrayList<URL> contents = new ArrayList<URL>();
			HashMap<String, ProjectAssociation> projects = new HashMap<String, ProjectAssociation>();
			while(enumURLs.hasMoreElements()) {
				URL urlEntry = enumURLs.nextElement();
				String path = urlEntry.getPath();
				if (path.startsWith("/"))
					path = path.substring(1);
				
				if (path.startsWith("META-INF/")) {
					continue;
				}
				if (path.startsWith(".melusine-dir/")) {
					if (path.startsWith(REQUIRE_ITEM_TYPEs)) {
						Object[] requireItemTypeIds = (Object[]) readObject(urlEntry.openStream());
						for (int i = 0; i < requireItemTypeIds.length;) {
							UUID id = (UUID) requireItemTypeIds[i++];
							String name = (String) requireItemTypeIds[i++];
							String qname = (String) requireItemTypeIds[i++];
							Integer version = (Integer) requireItemTypeIds[i++];
							Item cr = lw.getItem(id);
							if (cr == null) {
								throw new CadseException(
										"Missing item type {0} version {1} ({2}, {3})",
										name, version, qname, id);
							}
							if (!(cr instanceof ItemType)) {
								throw new CadseException(
										"Item {0} version {1} is not an item type ! ({2}, {3})",
										name, version, qname, id);
							}
						}
						continue;
					}
					if (path.startsWith(REQUIRE_CADSEs)) {
						Object[] requireCadseIds = (Object[]) readObject(urlEntry.openStream());
						for (int i = 0; i < requireCadseIds.length;) {
							UUID id = (UUID) requireCadseIds[i++];
							String name = (String) requireCadseIds[i++];
							String qname = (String) requireCadseIds[i++];
							Integer version = (Integer) requireCadseIds[i++];
							Item cr = lw.getItem(id);
							if (cr == null) {
								throw new CadseException(
										"Missing cadse {0} version {1} ({2}, {3})",
										name, version, qname, id);
							}
							if (!(cr instanceof CadseRuntime)) {
								throw new CadseException(
										"Item {0} version {1} is not a cadse ! ({2}, {3})",
										name, version, qname, id);
							}
						}
						continue;
					}
					if (path.startsWith(PROJECTS)) {
						projectsMap = (HashMap<String, UUID>) readObject(urlEntry.openStream());
						if (projectsMap != null) {
							for (Map.Entry<String, UUID> e : projectsMap.entrySet()) {
								ProjectAssociation pa = new ProjectAssociation(
										e.getValue(), e.getKey());
								projectAssociationSet.add(pa);
								projects.put(pa.getProjectName(), pa);
							}
						}
						continue;
					}
					if (path.endsWith(".ser")) {
						itemdescription.add(urlEntry);
					}
					continue;
				}
				
				if (path.endsWith("/")) continue;
				
				contents.add(urlEntry);
			}
			
			
			if (itemdescription.size() == 0) throw new CadseException("Internal error : cannot find items");
			for (URL url : contents) {
				String path = url.getPath();
				String projectName = path.substring(1);
				int i = projectName.indexOf('/');
				projectName = projectName.substring(0, i);
				ProjectAssociation pa = projects.get(projectName);
				if (pa != null)
					pa.addContentEntry(path.substring(i+2), url);
			}
			
			LogicalWorkspaceTransaction transaction = lw.createTransaction();
			List<ItemDelta> Itemsdelta = transaction.loadItems(itemdescription);
			migrate(transaction);
			transaction.commit(false, true, false, projectAssociationSet);
			checkAction(Itemsdelta, transaction);
			
		} catch (RuntimeException e) {
			throw new CadseException(e.getMessage(), e);
		} finally {
			CadseCore.getCadseDomain().endOperation();
		}
	}
	

	/**
	 * 
	 * @param cadse
	 *            the name of the cadse : can be null, it read from
	 * @param input
	 * @return
	 * @throws IOException
	 * @throws CadseException
	 */
	public Item importCadse(String cadse, InputStream input) throws IOException, CadseException {
		CadseCore.getCadseDomain().beginOperation("Import cadse");
		try {
			File pf;
			File dir = CadseCore.getCadseDomain().getLocation();
			if (cadse != null)
				pf = new File(dir, cadse);
			else
				pf = createTempDirectory(dir);

			ZipUtil.unzip(input, pf);
			UUID uuid = readCadseUUIDFolder(pf);
			if (cadse == null) {
				cadse = readCadseFolder(pf);
				pf.renameTo(new File(dir, cadse));
				pf = new File(dir, cadse);
			}

			File melusineDir = new File(pf, ".melusine-dir");
			File[] filesserxml = melusineDir.listFiles();
			Collection<URL> itemdescription = new ArrayList<URL>();
			for (File fser : filesserxml) {
				if (fser.getName().endsWith(".ser")) {
					itemdescription.add(fser.toURI().toURL());
				}
			}
			Collection<ProjectAssociation> projectAssociationSet = new ArrayList<ProjectAssociation>();
			ProjectAssociation pa = new ProjectAssociation(uuid, cadse);
			projectAssociationSet.add(pa);
			LogicalWorkspaceTransaction transaction = CadseCore.getLogicalWorkspace().createTransaction();

			List<ItemDelta> Itemsdelta = transaction.loadItems(itemdescription);
		//	migrate(transaction);
			ItemDelta cadseDef = transaction.getItem(uuid);
			transaction.commit(false, true, false, projectAssociationSet);
			checkAction(Itemsdelta, transaction);
			return cadseDef.getBaseItem();
		} finally {
			CadseCore.getCadseDomain().endOperation();
		}
	}
	
	/**
	 * Read cadse.
	 * 
	 * @param f
	 *            the f
	 * 
	 * @return the string
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws JAXBException
	 *             the JAXB exception
	 */
	static public String readCadse(File f) throws IOException, JAXBException {
		JarFile jis = new JarFile(f);
		ZipEntry entry = jis.getEntry(ExportImportCadseFunction.MELUSINE_DIR_CADSENAME);
		if (entry == null) {
			entry = jis.getEntry("/" + ExportImportCadseFunction.MELUSINE_DIR_CADSENAME);
			if (entry == null) {
				return null;
			}
		}
		InputStream imput = jis.getInputStream(entry);
		BufferedReader isr = new BufferedReader(new InputStreamReader(imput));
		return isr.readLine();
	}

	static public String readCadseFolder(File f) throws IOException {
		File cadseNameFile = new File(f, ExportImportCadseFunction.MELUSINE_DIR_CADSENAME);
		if (!cadseNameFile.exists()) {
			return null;
		}
		InputStream imput = null;
		try {
			imput = new FileInputStream(cadseNameFile);
			BufferedReader isr = new BufferedReader(new InputStreamReader(imput));
			return isr.readLine();
		} finally {
			if (imput != null)
				imput.close();
		}
	}
	
	/**
	 * Read cadse uuid.
	 * 
	 * @param f
	 *            the f
	 * 
	 * @return the compact uuid
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws JAXBException
	 *             the JAXB exception
	 */
	public UUID readCadseUUID(File f) throws IOException, JAXBException {
		JarFile jis = new JarFile(f);
		ZipEntry entry = jis.getEntry(ExportImportCadseFunction.MELUSINE_DIR_CADSENAME_ID);
		if (entry == null) {
			entry = jis.getEntry("/" + ExportImportCadseFunction.MELUSINE_DIR_CADSENAME_ID);
			if (entry == null) {
				return null;
			}
		}
		InputStream imput = jis.getInputStream(entry);
		BufferedReader isr = new BufferedReader(new InputStreamReader(imput));
		return UUID.fromString(isr.readLine());
	}

	/**
	 * Read cadse uuid.
	 * 
	 * @param f
	 *            the f
	 * 
	 * @return the compact uuid
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws JAXBException
	 *             the JAXB exception
	 */
	static public UUID readCadseUUIDFolder(File f) throws IOException {
		File uuid = new File(f, ExportImportCadseFunction.MELUSINE_DIR_CADSENAME_ID);

		if (!uuid.exists()) {
			return null;
		}
		InputStream imput = new FileInputStream(uuid);
		BufferedReader isr = new BufferedReader(new InputStreamReader(imput));
		return UUID.fromString(isr.readLine());
	}

	static private void migrate(LogicalWorkspaceTransaction transaction) throws CadseException {
		Collection<ItemDelta> operations = transaction.getItemOperations();
		for (ItemDelta itemDelta : operations) {

			if (itemDelta.getType() == null) {
				if (itemDelta.getBaseItem() != null) {
					itemDelta.setType(itemDelta.getBaseItem().getType());
				} else
					System.out.println("type has no type " + itemDelta);
			}

			if (!itemDelta.isLoaded())
				continue;
			if (itemDelta.isInstanceOf(CadseGCST.PAGE) || itemDelta.isInstanceOf(CadseGCST.FIELD) || itemDelta.isInstanceOf(CadseGCST.INTERACTION_CONTROLLER) || itemDelta.isInstanceOf(CadseGCST.MODEL_CONTROLLER) || itemDelta.isInstanceOf(CadseGCST.DISPLAY)) {
				itemDelta.delete(false);
			}
	
			if (itemDelta.getType() == CadseGCST.LINK_TYPE) {
				if (itemDelta.getName().startsWith("#invert_part")) {
					itemDelta.delete(false);
					for (Link l : itemDelta.getIncomingLinks()) {
						l.delete();
					}
				}
				LinkDelta l = itemDelta.getOutgoingLink(CadseGCST.LINK_TYPE_lt_INVERSE_LINK);
				if (l != null && l.getDestination().getName().startsWith("#invert_part")) {
					l.delete();
				}
			}
			SetAttributeOperation committed_date_value = itemDelta
					.getSetAttributeOperation(CadseGCST.ITEM_at_COMMITTED_DATE_);
			if (committed_date_value != null) {
				if (committed_date_value.getCurrentValue() instanceof Date) {
					Date d = (Date) committed_date_value.getCurrentValue();
					itemDelta.setAttribute(CadseGCST.ITEM_at_COMMITTED_DATE_, d.getTime());
				}
			}
		}

		for (ItemDelta itemDelta : operations) {
			if (!itemDelta.isLoaded())
				continue;
			for (LinkDelta l : itemDelta.getOutgoingLinkOperations()) {
				if (l.getLinkTypeName().startsWith("#parent:") || l.getLinkTypeName().startsWith("#invert_part")) {
					if (itemDelta.getPartParent() == null) {
						itemDelta.setParent(l.getDestination(), null);
					}
					if (itemDelta.getOutgoingLink(CadseGCST.ITEM_lt_PARENT) == null) {
						itemDelta.createLink(CadseGCST.ITEM_lt_PARENT, l.getDestination());
					}
					l.delete();
				} else if (l.getDestination().getName().contains("#invert_part_")) {
					l.delete();
				}
				if (l.getLinkType() != null && l.getLinkType().isPart() && l.getDestination().getPartParent() == null) {
					l.getDestination().setParent(l.getSource(), l.getLinkType());
				}
			}
			for (LinkDelta l : itemDelta.getOutgoingLinkOperations(CadseGCST.ITEM_lt_MODIFIED_ATTRIBUTES)) {
				if (!l.isLoaded())
					continue;
				if (l.getDestination().getType() == null) {
					IAttributeType<?> att = l.getSource().getLocalAttributeType(l.getDestinationName());
					
					if (att != null) {
						LinkDelta latt = itemDelta.getOutgoingLink(CadseGCST.ITEM_lt_MODIFIED_ATTRIBUTES, att.getId());
						if (latt != null) {
							l.delete();
						} else
							l.changeDestination(att);
					} else
						l.delete();
				}
			}
		}
		for (ItemDelta itemDelta : operations) {
			if (!itemDelta.isLoaded())
				continue;
			if (itemDelta.getPartParent() == null && itemDelta.getType() != null && itemDelta.getType().isPartType()) {
				System.out.println("Error cannot found parent for " + itemDelta.getQualifiedName());
			}
		}
	}

	private void checkAction(List<ItemDelta> itemsdelta, LogicalWorkspaceTransaction transaction) {
		LogicalWorkspace lw = CadseCore.getLogicalWorkspace();
		for (ItemDelta itemDelta : itemsdelta) {
			Item gI = lw.getItem(itemDelta.getId());
			if (gI == null) {
				System.err.println("Cannot found commited item " + itemDelta);
				continue;
			}
			items.add(gI);
			Item parent = gI.getPartParent();
			if (parent == null && itemDelta.getPartParent() != null) {
				System.err.println("Parent not setted " + itemDelta + " -> " + itemDelta.getPartParent());
			} else {
				if (parent != null && itemDelta.getPartParent() != null) {
					if (!parent.getId().equals(itemDelta.getPartParent().getId())) {
						System.err.println("Parent not same " + itemDelta + " -> " + itemDelta.getPartParent() + "<>"
								+ parent);
					}
				}
			}

		}

	}

	

	/**
	 * 
	 * @param dir
	 *            can be null : see
	 *            {@link File#createTempFile(String, String, File)}.
	 * @return a tempory folder
	 * @throws IOException
	 */

	public static File createTempDirectory(File dir) throws IOException {
		final File temp;

		temp = File.createTempFile("temp", Long.toString(System.nanoTime()), dir);

		if (!(temp.delete())) {
			throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
		}

		if (!(temp.mkdir())) {
			throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
		}

		return (temp);
	}


}
