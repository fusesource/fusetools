/*******************************************************************************
 * Copyright (c) 2016 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.fuse.transformation.editor.internal.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Properties;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.junit.Test;

public class ManifestConfigurationUpdaterTest {

	@Test
	public void testDependencyLowerTo63() throws Exception {
		Model pomModel = createModelWithVersion("6.2.1");
		
		boolean shouldImport = new ManifestConfigurationUpdater().shouldAddImportPackage(pomModel);
		
		assertThat(shouldImport).isFalse();
	}
	
	@Test
	public void testDependencyEqualTo63() throws Exception {
		Model pomModel = createModelWithVersion("6.3.0");
		
		boolean shouldImport = new ManifestConfigurationUpdater().shouldAddImportPackage(pomModel);
		
		assertThat(shouldImport).isTrue();
	}
	
	@Test
	public void testDependencyHigherTo63() throws Exception {
		Model pomModel = createModelWithVersion("6.4.0");
		
		boolean shouldImport = new ManifestConfigurationUpdater().shouldAddImportPackage(pomModel);
		
		assertThat(shouldImport).isTrue();
	}
	
	@Test
	public void testDependencyWithInvalidVersion_returnsTrueToEnsureAddingTheImportPackage() throws Exception {
		Model pomModel = createModelWithVersion("Invalid");
		
		boolean shouldImport = new ManifestConfigurationUpdater().shouldAddImportPackage(pomModel);
		
		assertThat(shouldImport).isTrue();
	}
	
	@Test
	public void testDependencyWithNoCamelCoreVersionSpecified_returnsTrueToEnsureAddingTheImportPackage() throws Exception {
		Model pomModel = new Model();
		Build build = new Build();
		pomModel.setBuild(build);
		
		boolean shouldImport = new ManifestConfigurationUpdater().shouldAddImportPackage(pomModel);
		
		assertThat(shouldImport).isTrue();
	}
	
	@Test
	public void testWarpackaging_returnsFalse() throws Exception {
		Model pomModel = createModelWithVersion("6.3.0");
		pomModel.setPackaging("war");
		
		boolean shouldImport = new ManifestConfigurationUpdater().shouldAddImportPackage(pomModel);
		
		assertThat(shouldImport).isFalse();
	}
	
	@Test
	public void testDependencySpecifiedWithProperty_LowerThan63() throws Exception {
		Model pomModel = createModelWithVersion("${camel.version}");
		Properties properties = new Properties();
		properties.setProperty("camel.version", "6.2.1");
		pomModel.setProperties(properties);
		
		
		boolean shouldImport = new ManifestConfigurationUpdater().shouldAddImportPackage(pomModel);
		
		assertThat(shouldImport).isFalse();
	}
	
	@Test
	public void testDependencySpecifiedWithProperty_HigherThan63() throws Exception {
		Model pomModel = createModelWithVersion("${camel.version}");
		Properties properties = new Properties();
		properties.setProperty("camel.version", "6.3.0");
		pomModel.setProperties(properties);
		
		boolean shouldImport = new ManifestConfigurationUpdater().shouldAddImportPackage(pomModel);
		
		assertThat(shouldImport).isTrue();
	}
	
	@Test
	public void testDependencySpecifiedWithPropertyInvalid() throws Exception {
		Model pomModel = createModelWithVersion("${camel.version}");
		Properties properties = new Properties();
		properties.setProperty("camel.version", "Invalid");
		pomModel.setProperties(properties);
		
		boolean shouldImport = new ManifestConfigurationUpdater().shouldAddImportPackage(pomModel);
		
		assertThat(shouldImport).isTrue();
	}
	
	@Test
	public void testDependencySpecifiedWithPropertyNotDefined() throws Exception {
		Model pomModel = createModelWithVersion("${camel.version}");
		
		boolean shouldImport = new ManifestConfigurationUpdater().shouldAddImportPackage(pomModel);
		
		assertThat(shouldImport).isTrue();
	}
	
	private Model createModelWithVersion(String version) {
		Model pomModel = new Model();
		Build build = new Build();
		Plugin plugin = new Plugin();
		plugin.setGroupId("org.apache.camel");
		plugin.setArtifactId("camel-core");
		plugin.setVersion(version);
		build.setPlugins(Arrays.asList(plugin));
		pomModel.setBuild(build );
		return pomModel;
	}
}
