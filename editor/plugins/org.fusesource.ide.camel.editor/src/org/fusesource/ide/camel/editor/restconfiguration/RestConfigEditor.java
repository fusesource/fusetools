/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.fusesource.ide.camel.editor.restconfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.EditorPart;
import org.fusesource.ide.camel.editor.CamelEditor;
import org.fusesource.ide.camel.editor.internal.CamelEditorUIActivator;
import org.fusesource.ide.camel.model.service.core.model.CamelBasicModelElement;
import org.fusesource.ide.camel.model.service.core.model.CamelFile;
import org.fusesource.ide.camel.model.service.core.model.ICamelModelListener;
import org.fusesource.ide.foundation.core.util.Strings;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author bfitzpat
 */
public class RestConfigEditor extends EditorPart implements ICamelModelListener, ISelectionProvider {

	private HashMap<String, Color> colorMap;
	public static final String REST_COLOR_LIGHT_BLUE = "light-blue";  //$NON-NLS-1$
	public static final String REST_COLOR_LIGHT_GREEN = "light-green";  //$NON-NLS-1$
	public static final String REST_COLOR_LIGHT_ORANGE = "light-orange";  //$NON-NLS-1$
	public static final String REST_COLOR_LIGHT_RED = "light-red"; //$NON-NLS-1$
	public static final String REST_COLOR_LIGHT_GREY = "light-grey"; //$NON-NLS-1$
	public static final String REST_COLOR_DARK_BLUE = "dark-blue";  //$NON-NLS-1$
	public static final String REST_COLOR_DARK_GREEN = "dark-green";  //$NON-NLS-1$
	public static final String REST_COLOR_DARK_ORANGE = "dark-orange";  //$NON-NLS-1$
	public static final String REST_COLOR_DARK_RED = "dark-red"; //$NON-NLS-1$

	private static final String REST_TAG = "rest"; //$NON-NLS-1$
	private static final String REST_CONFIGURATION_TAG = "restConfiguration"; //$NON-NLS-1$
	private static final String REST_VERB_FLAG = "restVerb"; //$NON-NLS-1$

	private static final String GET_VERB = "get"; //$NON-NLS-1$
	private static final String POST_VERB = "post"; //$NON-NLS-1$
	private static final String PUT_VERB = "put"; //$NON-NLS-1$
//	private static final String PATCH_VERB = "patch"; //$NON-NLS-1$
	private static final String DELETE_VERB = "delete"; //$NON-NLS-1$
//	private static final String HEAD_VERB = "head"; //$NON-NLS-1$
//	private static final String TRACE_VERB = "trace"; //$NON-NLS-1$
//	private static final String CONNECT_VERB = "connect"; //$NON-NLS-1$
//	private static final String OPTIONS_VERB = "options"; //$NON-NLS-1$

	private static final String IMG_DESC_ADD = "icons/editor/add.png"; //$NON-NLS-1$
	private static final String IMG_DESC_DELETE = "icons/rem_co.png"; //$NON-NLS-1$

	private CamelEditor parentEditor;
	private Composite parent;
	private ScrolledForm form;
	private FormToolkit toolkit;
	private HashMap<String, ArrayList<Object>> model;
	private ImageRegistry mImageRegistry;	
	private ListenerList<ISelectionChangedListener> listeners = new ListenerList<>();
	private Object selection;
	private Control selectedControl;
	private CTabFolder tabFolder;
	private CTabItem configurationTab;
	private CTabItem operationsTab;
	private Combo componentCombo;
	private Text contextPathText;
	private Text portText;
	private Combo bindingModeCombo;
	private Text hostText;
	private Composite restOpsSection;
	private ListViewer restList;

	/**
	 *
	 * @param parentEditor
	 */
	public RestConfigEditor(CamelEditor parentEditor) {
		this.parentEditor = parentEditor;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		this.parentEditor.doSave(monitor);
	}

	@Override
	public void doSaveAs() {
		this.parentEditor.doSaveAs();
	}

	@Override
	public void init(IEditorSite editorSite, IEditorInput input)
			throws PartInitException {
		setSite(editorSite);
		setInput(input);
		getSite().setSelectionProvider(this);
	}

	@Override
	public boolean isDirty() {
		return parentEditor.isDirty();
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	private void createTabs(Composite parent) {
		tabFolder = new CTabFolder(parent, SWT.BORDER | SWT.FLAT | SWT.RIGHT);
		tabFolder.setSimple(false);
		tabFolder.setLayout(new GridLayout(2, false));
		tabFolder.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1, 10));
		
		configurationTab = new CTabItem(tabFolder, SWT.NULL);
		configurationTab.setText("Configuration");

		operationsTab = new CTabItem(tabFolder, SWT.NULL);
		operationsTab.setText("Operations");
		
		setSelectionBackground();
	}

	private void createColors() {
		colorMap = new HashMap<>();
		colorMap.put(REST_COLOR_LIGHT_BLUE, new Color(Display.getDefault(), 235, 242, 250));
		colorMap.put(REST_COLOR_LIGHT_ORANGE, new Color(Display.getDefault(), 250, 241, 230));
		colorMap.put(REST_COLOR_LIGHT_GREEN, new Color(Display.getDefault(), 232, 245, 239));
		colorMap.put(REST_COLOR_LIGHT_GREY, new Color(Display.getDefault(), 240, 248, 255));
		colorMap.put(REST_COLOR_LIGHT_RED, new Color(Display.getDefault(), 250, 231, 231));
		colorMap.put(REST_COLOR_DARK_BLUE, new Color(Display.getDefault(), 93, 173, 255));
		colorMap.put(REST_COLOR_DARK_ORANGE, new Color(Display.getDefault(), 254, 162, 24));
		colorMap.put(REST_COLOR_DARK_GREEN, new Color(Display.getDefault(), 65, 205, 142));
		colorMap.put(REST_COLOR_DARK_RED, new Color(Display.getDefault(), 252, 60, 55));
	}

	private void destroyColors() {
		if (!colorMap.isEmpty()) {
			for (Color color : colorMap.values()) {
				color.dispose();
			}
		}
	}

	@Override
	public void createPartControl(Composite p) {
		getImages();

		this.parent = new Composite(p, SWT.FLAT);

		GridLayout gl = new GridLayout(1, false);
		gl.horizontalSpacing = 10;

		this.parent.setLayout(gl);
		createContents();

		reload();
		CamelFile designEditorModel = parentEditor.getDesignEditor().getModel();
		if (designEditorModel != null) {
			designEditorModel.addModelListener(this);
		}
	}

	@Override
	public void dispose() {
		if (parentEditor != null && parentEditor.getDesignEditor() != null && parentEditor.getDesignEditor().getModel() != null) {
			parentEditor.getDesignEditor().getModel().removeModelListener(this);
		}
		destroyColors();
		mImageRegistry.dispose();
		super.dispose();
	}

	@Override
	public void modelChanged() {
		Display.getDefault().asyncExec( () -> {
			if (RestConfigEditor.this.parentEditor != null
					&& RestConfigEditor.this.equals(RestConfigEditor.this.parentEditor.getActiveEditor())) {
				reload();
			}
		});
	}

	@Override
	public void setFocus() {
		Display.getDefault().asyncExec(this::reload);
	}

	private void createContents() {
		toolkit = new FormToolkit(Display.getDefault());

		createColors();
		createTabs(parent);
		
		createRestConfigurationTabSection();
		
		form = toolkit.createScrolledForm(tabFolder);
		form.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1, 10));
		form.getBody().setLayout(new GridLayout(2, false));
		
		createRestTabSection();
		restOpsSection = createRestOperationTabSection();

		operationsTab.setControl(form);
		form.layout();
		toolkit.decorateFormHeading(form.getForm());
	}
	
	private Color getBackgroundColorForType(String tag) {
		if (GET_VERB.equals(tag)) {
			return colorMap.get(REST_COLOR_LIGHT_BLUE);
		}
		if (PUT_VERB.equals(tag)) {
			return colorMap.get(REST_COLOR_LIGHT_GREEN);
		}
		if (POST_VERB.equals(tag)) {
			return colorMap.get(REST_COLOR_LIGHT_ORANGE);
		}
		if (DELETE_VERB.equals(tag)) {
			return colorMap.get(REST_COLOR_LIGHT_RED);
		}
		return colorMap.get(REST_COLOR_LIGHT_GREY);
	}		

	private Color getForegroundColorForType(String tag) {
		Color foregroundColor = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
		if (PUT_VERB.equals(tag)) {
			foregroundColor = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
		}
		return foregroundColor;
	}		

	private Color getImageColorForType(String tag) {
		if (GET_VERB.equals(tag)) {
			return colorMap.get(REST_COLOR_DARK_BLUE);
		}
		if (PUT_VERB.equals(tag)) {
			return colorMap.get(REST_COLOR_DARK_GREEN);
		}
		if (POST_VERB.equals(tag)) {
			return colorMap.get(REST_COLOR_DARK_ORANGE);
		}
		if (DELETE_VERB.equals(tag)) {
			return colorMap.get(REST_COLOR_DARK_RED);
		}
		return Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
	}

	private String compareTextAndTag(String text, String tag) {
		if (text == null || tag == null) {
			return null;
		}
		if (tag.equals(text) || text.startsWith(tag)) {
			return tag;
		}
		return null;
	}
	
	private String getTextForImage(String text) {
		if (compareTextAndTag(text, GET_VERB) != null) {
			return GET_VERB;
		}
		if (compareTextAndTag(text, PUT_VERB) != null) {
			return PUT_VERB;
		}
		if (compareTextAndTag(text, POST_VERB) != null) {
			return POST_VERB;
		}
		if (compareTextAndTag(text, DELETE_VERB) != null) {
			return DELETE_VERB;
		}
		return null;
	}
	
	private Composite createVerbComposite(Composite parent, String labelText, String content) {
		Composite client=toolkit.createComposite(parent,SWT.BORDER);
		client.setBackground(colorMap.get(REST_COLOR_LIGHT_BLUE));
		client.setLayout(new GridLayout(2, false));
		GridData gd = GridDataFactory.fillDefaults().grab(true, false).span(2, 1).create();
		client.setLayoutData(gd);

		String graphicLabel = getTextForImage(labelText);
		Color imageColor = getImageColorForType(graphicLabel);
		Color backgroundColor = getBackgroundColorForType(graphicLabel);
		Color foregroundColor = getForegroundColorForType(graphicLabel);
		client.setBackground(backgroundColor);

		Label image=new Label(client,SWT.WRAP | SWT.BOLD | SWT.CENTER);
		image.setText(graphicLabel);
		image.setBackground(imageColor);
		image.setForeground(foregroundColor);
		image.setLayoutData(GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).create());
		image.addListener(SWT.MouseDown, new SelectionListener());

		Label label=new Label(client,SWT.WRAP);
		label.setText(content);
		label.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
		label.setBackground(client.getBackground());
		label.addListener(SWT.MouseDown, new SelectionListener());

		return client;
	}

	private Object getDataFromSelectedUIElement(Control control) {
		Node data = null;
		if (!control.isDisposed()) {
			if (control.getData(REST_VERB_FLAG) != null) {
				data = (Node) control.getData(REST_VERB_FLAG);
			} else if (control.getData(REST_TAG) != null) {
				data = (Node) control.getData(REST_TAG);
			} else if (control.getData(REST_CONFIGURATION_TAG) != null) {
				data = (Node) control.getData(REST_CONFIGURATION_TAG);
			}
			if (data != null) {
				return new CamelBasicModelElement(null, data);
			}
			if (control.getParent() != null) {
				return getDataFromSelectedUIElement(control.getParent());
			}
		}
		return null;
	}

	class ToggleAction extends Action {
		public ToggleAction ( ) {
			super(null, IAction.AS_PUSH_BUTTON);
		}
	}

	class AddAction extends ToggleAction {
		@Override
		public void run() {
			MessageBox box = new MessageBox(Display.getCurrent().getActiveShell(), SWT.CANCEL | SWT.OK);
			box.setText("Add something");
			box.setMessage("In place of this message, we will actually add something.");
			box.open();
		}
		@Override
		public String getToolTipText() {
			return "Add something";
		}
		@Override
		public ImageDescriptor getImageDescriptor() {
			return mImageRegistry.getDescriptor(IMG_DESC_ADD);
		}
	}

	class DeleteAction extends ToggleAction {
		@Override
		public void run() {
			MessageBox box = new MessageBox(Display.getCurrent().getActiveShell(), SWT.CANCEL | SWT.OK);
			box.setText("Delete something");
			box.setMessage("In place of this message, we will actually delete something.");
			box.open();
		}
		@Override
		public String getToolTipText() {
			return "Delete something";
		}
		@Override
		public ImageDescriptor getImageDescriptor() {
			return mImageRegistry.getDescriptor(IMG_DESC_DELETE);
		}
	}

	private void getImages() {
		mImageRegistry = new ImageRegistry();
		mImageRegistry.put(IMG_DESC_ADD, ImageDescriptor
				.createFromURL(CamelEditorUIActivator.getDefault().getBundle()
						.getEntry(IMG_DESC_ADD)));	
		mImageRegistry.put(IMG_DESC_DELETE, ImageDescriptor
				.createFromURL(CamelEditorUIActivator.getDefault().getBundle()
						.getEntry(IMG_DESC_DELETE)));	
	}

	private Composite createRestTabSection() {
		Section section = toolkit.createSection(form.getBody(), Section.EXPANDED | Section.TITLE_BAR | Section.DESCRIPTION);
		section.setText(REST_TAG);
		section.setDescription("Specify the details for each REST entry.");
		section.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(1, 5).create());
		section.setLayout(new GridLayout(2, false));

		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		ToolBar toolbar = toolBarManager.createControl(section);
		toolbar.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

		// Add action to the tool bar
		AddAction action = new AddAction();
		toolBarManager.add(action);
		DeleteAction daction = new DeleteAction();
		toolBarManager.add(daction);

		toolBarManager.update(true);

		section.setTextClient(toolbar);
		
		Composite client=toolkit.createComposite(section,SWT.NONE);
		client.setLayout(new GridLayout(1, false));
		client.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(1, 5).create());
		client.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		
		restList = new ListViewer(client, SWT.V_SCROLL | SWT.SINGLE | SWT.BORDER);
		restList.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(1, 5).create());
		restList.setContentProvider(new ArrayContentProvider());
		restList.setLabelProvider(new RestLabelProvider());
		restList.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getStructuredSelection().getFirstElement() instanceof Element) {
					Control[] children = restOpsSection.getChildren();
					for (int i = 0; i < children.length; i++) {
						Control child = children[i];
						child.dispose();
					}
					
					Element restElement = (Element) event.getStructuredSelection().getFirstElement();
					if (restElement.getChildNodes().getLength() > 0) {
						for (int i = 0; i < restElement.getChildNodes().getLength(); i++) {
							Node child = restElement.getChildNodes().item(i);
							if (child instanceof Element) {
								Element elChild = (Element) child;
								String verbUri = elChild.getAttribute("uri"); //$NON-NLS-1$
								Composite operation = createVerbComposite(restOpsSection, elChild.getTagName(), verbUri);
								operation.setData(REST_VERB_FLAG, elChild);
							}
						}
					}
					restOpsSection.layout();
				}
			}
		});

		section.setClient(client);
		
		return client;
	}
	
	private Composite createRestOperationTabSection() {
		Section section = toolkit.createSection(form.getBody(), Section.EXPANDED | Section.TITLE_BAR | Section.DESCRIPTION);
		section.setText("REST Operations");
		section.setDescription("Specify the details for each REST operation.");
		GridData gd = new GridData(SWT.FILL,SWT.FILL,true,true);
		section.setLayoutData(gd);
		section.setLayout(new GridLayout(2, false));

		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		ToolBar toolbar = toolBarManager.createControl(section);
		toolbar.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

		// Add action to the tool bar
		AddAction action = new AddAction();
		toolBarManager.add(action);
		DeleteAction daction = new DeleteAction();
		toolBarManager.add(daction);

		toolBarManager.update(true);

		section.setTextClient(toolbar);

		Composite client=toolkit.createComposite(section,SWT.BORDER);
		client.setLayout(new GridLayout(1, false));
		client.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		client.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		
		section.setClient(client);
		
		return client;
	}

	class RestLabelProvider extends LabelProvider {

		@Override
		public String getText(Object element) {
			if (element instanceof Element) {
				Element restElement = (Element) element;
				if (getAttrValue(restElement, "path") != null) {
					return getAttrValue(restElement, "path");
				}
			}
			return super.getText(element);
		}
		
	}

	private Composite createRestConfigurationTabSection() {
		Section section = toolkit.createSection(tabFolder, Section.EXPANDED | Section.TITLE_BAR | Section.DESCRIPTION);
		section.setText(REST_CONFIGURATION_TAG);
		section.setDescription("Specify the details of the REST Configuration.");
		GridData gd = new GridData(SWT.FILL,SWT.FILL,true,false);
		section.setLayoutData(gd);
		section.setLayout(new GridLayout(2, false));

		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);

		ToolBar toolbar = toolBarManager.createControl(section);
		toolbar.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

		// Add action to the tool bar
		AddAction action = new AddAction();
		toolBarManager.add(action);
		DeleteAction daction = new DeleteAction();
		toolBarManager.add(daction);

		toolBarManager.update(true);

		section.setTextClient(toolbar);		

		Composite client=toolkit.createComposite(section,SWT.NONE);
		client.setLayout(new GridLayout(2, false));
		client.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		
		toolkit.createLabel(client, "Component:");
		componentCombo = new Combo(client, SWT.DROP_DOWN);
		componentCombo.add("restlet");
		componentCombo.add("spark-rest");
		componentCombo.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		
		toolkit.createLabel(client, "Context Path:");
		contextPathText = toolkit.createText(client, "", SWT.BORDER);
		contextPathText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		
		toolkit.createLabel(client, "Port:");
		portText = toolkit.createText(client, "", SWT.BORDER);
		portText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());

		toolkit.createLabel(client, "Binding Mode:");
		bindingModeCombo = new Combo(client, SWT.DROP_DOWN);
		bindingModeCombo.add("auto");
		bindingModeCombo.add("json");
		bindingModeCombo.add("json_xml");
		bindingModeCombo.add("off");
		bindingModeCombo.setText("off");
		bindingModeCombo.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());

		toolkit.createLabel(client, "Host:");
		hostText = toolkit.createText(client, "", SWT.BORDER);
		hostText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		
		section.setClient(client);
		configurationTab.setControl(section);
		
		return client;
	}

	public void setSelectionBackground() {
		Display display = Display.getCurrent();
		tabFolder.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		tabFolder.setSelectionBackground(display.getSystemColor(SWT.COLOR_GRAY));
	}
	
	private void buildModel() {
		model = new HashMap<>();
		getModel().put(REST_CONFIGURATION_TAG, new ArrayList<Object>());
		getModel().put(REST_TAG, new ArrayList<Object>());
		CamelFile cf = parentEditor.getDesignEditor().getModel();
		if (cf != null && cf.getRouteContainer() != null) {
			Node node = cf.getRouteContainer().getXmlNode();
			if (node instanceof Element) {
				NodeList configlist = ((Element) node).getElementsByTagName(REST_CONFIGURATION_TAG);
				if (configlist != null && configlist.getLength() > 0) {
					for (int i=0; i < configlist.getLength(); i++) {
						Element config = (Element) configlist.item(i);
						getModel().get(REST_CONFIGURATION_TAG).add(config);
					}
				}
				NodeList restlist = ((Element) node).getElementsByTagName(REST_TAG);
				if (restlist != null && restlist.getLength() > 0) {
					for (int i=0; i < restlist.getLength(); i++) {
						Element config = (Element) restlist.item(i);
						getModel().get(REST_TAG).add(config);
					}
				}
			}
		}
	}

	private void clearUI() {
//		Control[] children = restConfigParent.getChildren();
//		for (int i = 0; i < children.length; i++) {
//			Control child = children[i];
//			child.dispose();
//		}
//		if (restComposite != null) {
//			restComposite.dispose();
//		}
//		form.layout();
	}

	private String getAttrValue(Element element, String attrName) {
		if (element.getAttribute(attrName) != null) {
			String tempValue = element.getAttribute(attrName);
			if (!Strings.isEmpty(tempValue)) {
				return tempValue;
			}
		}
		return null;
	}
	
	private void refreshRestConfigurationSection() {
		if (!getModel().get(REST_CONFIGURATION_TAG).isEmpty()) {
			for (Iterator<?> iterator = getModel().get(REST_CONFIGURATION_TAG).iterator(); iterator.hasNext();) {
				Element restConfig = (Element) iterator.next();
				
				String component = "";
				if (getAttrValue(restConfig, "component") != null) {
					component = getAttrValue(restConfig, "component");
				}
				
				String bindingMode = "off";
				if (getAttrValue(restConfig, "bindingMode") != null) {
					bindingMode = getAttrValue(restConfig, "bindingMode");
				}
				String port = "";
				if (getAttrValue(restConfig, "port") != null) {
					port = getAttrValue(restConfig, "port");
				}
				String contextPath = "";
				if (getAttrValue(restConfig, "contextPath") != null) {
					contextPath = getAttrValue(restConfig, "contextPath");
				}
				String host = "";
				if (getAttrValue(restConfig, "host") != null) {
					host = getAttrValue(restConfig, "host");
				}
				
				componentCombo.setText(component);
				bindingModeCombo.setText(bindingMode);
				portText.setText(port);
				contextPathText.setText(contextPath);
				hostText.setText(host);
				
				break;
			}
		}
	}
	
	private void refreshRestSection() {
		if (!getModel().get(REST_TAG).isEmpty()) {
			restList.setInput(getModel().get(REST_TAG));
		}
	}	
	
	public void reload() {
		buildModel();
		clearUI();
		refreshRestConfigurationSection();
		refreshRestSection();
		form.layout();
		toolkit.decorateFormHeading(form.getForm());
	}

	/**
	 * @return the model
	 */
	public Map<String, ArrayList<Object>> getModel() {
		return model;
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	@Override
	public ISelection getSelection() {
		if (selection != null) {
			return new StructuredSelection(selection);
		}
		return null;
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);		
	}

	@Override
	public void setSelection(ISelection selection) {
		Object[] list = listeners.getListeners();
		for (int i = 0; i < list.length; i++) {
			((ISelectionChangedListener) list[i]).selectionChanged(new SelectionChangedEvent(this, selection));
		}
	}

	class SelectionListener implements Listener {
		@Override
		public void handleEvent(Event event) {
			Control newControl = (Control) event.widget;
			updateSelectionDisplay(selectedControl, newControl);
			selectedControl = newControl;
			selection = getDataFromSelectedUIElement(newControl);
			if (selection != null) {
				setSelection(new StructuredSelection(selection));
			}
		}

		private void updateSelectionDisplay(Control oldControl, Control newControl) {
			if (oldControl != null && getDataFromSelectedUIElement(oldControl) != null) {
				CamelBasicModelElement node = (CamelBasicModelElement) getDataFromSelectedUIElement(oldControl);
				Color background = getBackgroundColorForType("");
				if (node != null && node.getXmlNode() != null) {
					background = getBackgroundColorForType(node.getXmlNode().getNodeName());
				}
				updateBorder(oldControl, background);
			}
			updateBorder(newControl, newControl.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
		}
		
		private void updateBorder(Control control, Color color) {
			if (control instanceof Composite) {
				Composite composite = (Composite) control;
				composite.setBackground(color);
			} else if (control.getParent() != null) {
				updateBorder(control.getParent(), color);
			}
		}
	}
	
}
