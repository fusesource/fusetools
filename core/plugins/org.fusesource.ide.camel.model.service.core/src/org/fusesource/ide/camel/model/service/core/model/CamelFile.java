/******************************************************************************* 
 * Copyright (c) 2015 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 

package org.fusesource.ide.camel.model.service.core.model;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IResource;
import org.fusesource.ide.camel.model.service.core.internal.CamelModelServiceCoreActivator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * this object represents the camel xml file. It can be of a schema type
 * like Spring or Blueprint. It can also contain Bean Definitions for things
 * like connection factories, property placeholder beans or loadbalancers etc.
 * 
 * The only children for the camel file is the camel context.
 * 
 * @author lhein
 */
public class CamelFile extends CamelModelElement {
	
	public static final int XML_INDENT_VALUE = 3;
	
	/**
	 * these maps contains endpoints and bean definitions stored using their ID value
	 */
	private Map<String, Node> globalDefinitions = new HashMap<String, Node>();

	/**
	 * the resource the camel file is stored in
	 */
	private IResource resource;
	
	/**
	 * Spring / Blueprint / Routes
	 */
	private CamelSchemaType schemaType;
	
	/**
	 * the xml document
	 */
	private Document document;

	/**
	 * list of listeners looking for changes in the internal model
	 */
	private List<ICamelModelListener> modelListeners = new ArrayList<ICamelModelListener>();
	
	
	/**
	 * creates a camel file object for the given resource
	 * 
	 * @param resource
	 */
	public CamelFile(IResource resource) {
		super(null, null);
		this.resource = resource;
	}

	/**
	 * 
	 * @param xmlString
	 */
	public void reloadModelFromXML(String xmlString) {
		// TODO: code me
		System.err.println("Reloading model from xml string:\n " + xmlString );
	}
	
	/**
	 * @return the globalDefinitions
	 */
	public Map<String, Node> getGlobalDefinitions() {
		return this.globalDefinitions;
	}
	
	/**
	 * @param globalDefinitions the globalDefinitions to set
	 */
	public void setGlobalDefinitions(Map<String, Node> globalDefinitions) {
		this.globalDefinitions = globalDefinitions;
	}
	
	/**
	 * adds the given global definition to the context 
	 * 
	 * @param id
	 * @param def
	 * @return the id used for adding the definition or null if not added
	 */
	public String addGlobalDefinition(String id, Node def) {
		String usedId = id != null ? id : "_def" + UUID.randomUUID().toString();
		if (id != null && this.globalDefinitions.containsKey(id)) return null;
		if (id == null && this.globalDefinitions.containsValue(def)) return null;
		this.globalDefinitions.put(usedId, def);
		fireModelChanged();
		return usedId;
	}
	
	/**
	 * removes the global definition from context 
	 * 
	 * @param id
	 */
	public void removeBeanDefinition(String id) {
		if (this.globalDefinitions.remove(id) != null) fireModelChanged();
	}
	
	/**
	 * deletes all global definitions
	 */
	public void clearGlobalDefinitions() {
		this.globalDefinitions.clear();
	}
	
	/**
	 * @return the schemaType
	 */
	public CamelSchemaType getSchemaType() {
		return this.schemaType;
	}
	
	/**
	 * @param schemaType the schemaType to set
	 */
	public void setSchemaType(CamelSchemaType schemaType) {
		this.schemaType = schemaType;
	}
	
	/**
	 * @return the resource
	 */
	public IResource getResource() {
		return this.resource;
	}
	
	/**
	 * @param resource the resource to set
	 */
	public void setResource(IResource resource) {
		this.resource = resource;
	}
	
	/**
	 * @return the document
	 */
	public Document getDocument() {
		return this.document;
	}
	
	/**
	 * @param document the document to set
	 */
	public void setDocument(Document document) {
		this.document = document;
	}
	
	/**
	 * checks whether this is a blueprint file or not
	 * 
	 * @return true if its a blueprint, false if no schema type detected or not blueprint
	 */
	public boolean isBlueprint() {
		return this.schemaType != null && schemaType.equals(CamelSchemaType.BLUEPRINT);
	}
	
	/**
	 * checks whether this is a blueprint file or not
	 * 
	 * @return true if its a blueprint, false if no schema type detected or not blueprint
	 */
	public boolean isSpring() {
		return this.schemaType != null && schemaType.equals(CamelSchemaType.SPRING);
	}
	
	/* (non-Javadoc)
	 * @see org.fusesource.ide.camel.model.service.core.model.CamelModelElement#getCamelFile()
	 */
	@Override
	public CamelFile getCamelFile() {
		return this;
	}
	
	/* (non-Javadoc)
	 * @see org.fusesource.ide.camel.model.service.core.model.CamelModelElement#createNode()
	 */
	@Override
	protected Node createNode() {
		return null;
	}
	
//	/* (non-Javadoc)
//	 * @see org.fusesource.ide.camel.model.service.core.model.CamelModelElement#updateUnderlyingNode()
//	 */
//	@Override
//	protected void updateUnderlyingNode() {
//		super.updateUnderlyingNode();
//		System.err.println(getDocumentAsXML());
//	}
	
	/**
	 * returns the string representing the dom model
	 * 
	 * @return	the dom model as string or null on error
	 */
	public String getDocumentAsXML() {
    	try {
    		DOMSource domSource = new DOMSource(getDocument());
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
	        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	        transformer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
	        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "" + XML_INDENT_VALUE);
	        tf.setAttribute("indent-number", XML_INDENT_VALUE);
            transformer.transform(domSource, result);
            writer.flush();
            return writer.toString().trim();
    	} catch (Exception ex) {
    		CamelModelServiceCoreActivator.pluginLog().logError("Unable to save the camel file to " + getResource().getFullPath().toOSString(), ex);
    	}
    	return null;
	}
	
	/**
	 * adds a model listener
	 * 
	 * @param listener
	 */
	public void addModelListener(ICamelModelListener listener) {
		if (this.modelListeners.contains(listener) == false) {
			this.modelListeners.add(listener);
		}
	}
	
	/**
	 * removes a listener
	 * 
	 * @param listener
	 */
	public void removeModelListener(ICamelModelListener listener) {
		if (this.modelListeners.contains(listener)) {
			this.modelListeners.remove(listener);
		}
	}
	
	/**
	 * notifies all listeners that the model has been changed
	 */
	public void fireModelChanged() {
		for (ICamelModelListener listener : this.modelListeners) {
			if (listener != null) listener.modelChanged();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.fusesource.ide.camel.model.service.core.model.CamelModelElement#getCamelContext()
	 */
	@Override
	public CamelContextElement getCamelContext() {
		for (CamelModelElement e : getChildElements()) {
			if (e.getXmlNode().getNodeName().equalsIgnoreCase("camelContext")) return (CamelContextElement)e;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.fusesource.ide.camel.model.service.core.model.CamelModelElement#supportsBreakpoint()
	 */
	@Override
	public boolean supportsBreakpoint() {
		return false;
	}
}
