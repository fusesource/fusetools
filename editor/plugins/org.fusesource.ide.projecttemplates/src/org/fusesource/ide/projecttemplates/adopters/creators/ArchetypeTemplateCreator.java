/******************************************************************************* 
 * Copyright (c) 2016 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.fusesource.ide.projecttemplates.adopters.creators;

import org.apache.maven.archetype.catalog.Archetype;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IProjectConfigurationManager;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.fusesource.ide.projecttemplates.internal.Messages;
import org.fusesource.ide.projecttemplates.internal.ProjectTemplatesActivator;
import org.fusesource.ide.projecttemplates.util.NewProjectMetaData;

/**
 * this class can be used to create a template project from an existing 
 * maven archetype. due to lack of config in the wizard we have to fill in 
 * the missing config items manually.
 * 
 * @author lhein
 */
public abstract class ArchetypeTemplateCreator implements TemplateCreatorSupport {

	/* (non-Javadoc)
	 * @see org.fusesource.ide.projecttemplates.adopters.creators.TemplateCreatorSupport#create(org.eclipse.core.resources.IProject, org.fusesource.ide.projecttemplates.util.NewProjectMetaData)
	 */
	@Override
	public boolean create(IProject project, NewProjectMetaData metadata, IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, Messages.ArchetypeTemplateCreator_CreatingTemplateFromArchetypeMonitorMessage, 2);
		Archetype archetype = getArchetype(metadata, subMonitor.newChild(1));
		
		ProjectImportConfiguration config = null;
		
		IProjectConfigurationManager manager = MavenPlugin.getProjectConfigurationManager();
		try {
			manager.createArchetypeProjects(project.getLocation(), archetype, archetype.getGroupId(), archetype.getArtifactId(), archetype.getVersion(), getJavaPackage(), archetype.getProperties(), config, subMonitor.newChild(1)); 
		} catch (CoreException ex) {
			ProjectTemplatesActivator.pluginLog().logError(ex);
			return false;
		}
		return true;
	}
	
	/**
	 * returns the archetype to execute
	 * 
	 * @param metadata	the project meta data
	 * @param monitor 
	 * @return	the archetype
	 */
	protected abstract Archetype getArchetype(NewProjectMetaData metadata, IProgressMonitor monitor);

	/**
	 * returns the java package
	 * 
	 * @return
	 */
	protected abstract String getJavaPackage();
}
