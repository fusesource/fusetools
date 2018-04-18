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
package org.fusesource.ide.wsdl2rest.ui.wizard;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.fusesource.ide.camel.model.service.core.tests.integration.core.io.FuseProject;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test to ensure we can run the wsdl2rest wizard.
 * 
 * @author brianf
 */
public class Wsdl2RestWizardTest {
	
    static final String WSDL_LOCATION = "src/test/resources/wsdl/Address.wsdl"; //$NON-NLS-1$
    static final String OUTPUT_PATH = "target/generated-wsdl2rest"; //$NON-NLS-1$
    static final String SPRING_CAMEL_PATH = "/src/main/resources/META-INF/spring/doclit-camel-context.xml"; //$NON-NLS-1$

    @Rule
	public FuseProject fuseProject = new FuseProject(Wsdl2RestWizardTest.class.getName());

	@Test
	public void testWsdl2RestWizardFinish() throws Exception {
    	Wsdl2RestWizard wizard = new Wsdl2RestWizard();
        File wsdlFile = new File(WSDL_LOCATION);
        Path outpath = new File(OUTPUT_PATH).toPath();
        wizard.setProject(fuseProject.getProject());
		wizard.getOptions().setWsdlURL(wsdlFile.toURI().toURL().toExternalForm());
		wizard.getOptions().setProjectName(fuseProject.getProject().getName());
		wizard.getOptions().setDestinationJava(outpath.toString());
		wizard.getOptions().setDestinationCamel(SPRING_CAMEL_PATH);
		wizard.getOptions().setTargetRestServiceAddress(new URL("http://localhost:8083/myjaxrs").toExternalForm()); //$NON-NLS-1$
		wizard.getOptions().setTargetServiceAddress(new URL("http://localhost:8080/doclit").toExternalForm()); //$NON-NLS-1$
		try {
			wizard.generate();
		} catch (Exception ex) {
			throw ex;
		}

		IProject pr = fuseProject.getProject();
		pr.refreshLocal(IResource.DEPTH_INFINITE, null);
		
		List<IFile> xmlFiles = findAllFilesWithExtension(pr, "xml");
		IResource camelFile = findFileWithNameInList("rest-camel-context.xml", xmlFiles); //$NON-NLS-1$
		Assert.assertTrue("Generated Camel file not found", camelFile != null && camelFile.exists());
		
		List<IFile> javaFiles = findAllFilesWithExtension(pr, "java");
		IResource addAddressJavaFile = findFileWithNameInList("AddAddress.java", javaFiles); //$NON-NLS-1$
		Assert.assertTrue("Generated AddAddress class not found",  //$NON-NLS-1$
				addAddressJavaFile != null && addAddressJavaFile.exists());
	}

	private IFile findFileWithNameInList(String name, List<IFile> files) {
		Iterator<IFile> fileIter = files.iterator();
		while (fileIter.hasNext()) {
			IFile tempFile = fileIter.next();
			if (tempFile.getName().equals(name)) {
				return tempFile;
			}
		}
		return null;
	}
	
	/**
	 * Returns a list of *.xml files in {@code container}.
	 */
	private List<IFile> findAllFilesWithExtension(IContainer container, String extension) {
	  final List<IFile> xmlFiles = new ArrayList<>();

	  try {
	    IResourceVisitor webInfCollector = new IResourceVisitor() {
	      @Override
	      public boolean visit(IResource resource) throws CoreException {
	        if (resource.getType() == IResource.FILE && extension.equalsIgnoreCase(resource.getFileExtension())) {
	          xmlFiles.add((IFile) resource);
	          return false;  // No need to visit sub-directories.
	        }
	        return true;
	      }
	    };
	    container.accept(webInfCollector);
	  } catch (CoreException ex) {
	    // Our attempt to find files failed, but don't error out.
	  }
	  return xmlFiles;
	}	

}