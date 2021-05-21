/******************************************************************************* 
 * Copyright (c) 2016 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at https://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.fusesource.ide.projecttemplates.tests.integration.wizards.pages.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.fusesource.ide.camel.model.service.core.util.CamelCatalogUtils;
import org.fusesource.ide.projecttemplates.wizards.pages.model.CategoryItem;
import org.fusesource.ide.projecttemplates.wizards.pages.model.EnvironmentData;
import org.fusesource.ide.projecttemplates.wizards.pages.model.FuseDeploymentPlatform;
import org.fusesource.ide.projecttemplates.wizards.pages.model.FuseRuntimeKind;
import org.fusesource.ide.projecttemplates.wizards.pages.model.TemplateItem;
import org.fusesource.ide.projecttemplates.wizards.pages.model.TemplateModel;
import org.fusesource.ide.projecttemplates.wizards.pages.model.TemplateOrCategoryItem;
import org.junit.Test;

public class TemplateModelIT {
	
	@Test
	public void testInitializationWorks() throws Exception {
		new TemplateModel();
	}
	
	@Test
	public void testCorrectNumberOfTopLevelCategories() throws Exception {
		//currently only 4: Empty, Beginner, Advanced and Expert
		List<TemplateOrCategoryItem> rootTemplates = new TemplateModel().getRootTemplates();
		assertThat(rootTemplates.stream().filter(item -> item instanceof CategoryItem).collect(Collectors.toList())).hasSize(4);
	}
	
	@Test
	public void testCorrectNumberOfTopLevelTemplates() throws Exception {
		//currently only 11: 6 SpringBoot, 5 EAP (no EAP for Fuse 7.9 yet)
		List<TemplateOrCategoryItem> rootTemplates = new TemplateModel().getRootTemplates();
		assertThat(rootTemplates.stream().filter(item -> item instanceof TemplateItem).collect(Collectors.toList())).hasSize(11);
	}
	
	@Test
	public void testCorrectMediumFuseContent() throws Exception {
		List<TemplateOrCategoryItem> templateOrCategoryItems = new TemplateModel().getRootTemplates();
		checkSingleTemplateForVersion(templateOrCategoryItems, CamelCatalogUtils.CAMEL_VERSION_LATEST_PRODUCTIZED_63);
		checkSingleTemplateForVersion(templateOrCategoryItems, CamelCatalogUtils.CAMEL_VERSION_LATEST_COMMUNITY);
	}

	private void checkSingleTemplateForVersion(List<TemplateOrCategoryItem> templateOrCategoryItems, String camelVersionLatestProductized63) {
		assertThat(templateOrCategoryItems
				.stream()
				.filter(templateOrCategoryItem -> templateOrCategoryItem instanceof TemplateItem)
				.filter(template -> ((TemplateItem)template).isCompatible(new EnvironmentData(CamelCatalogUtils.CAMEL_VERSION_LATEST_PRODUCTIZED_63, FuseDeploymentPlatform.STANDALONE, FuseRuntimeKind.WILDFLY)))
				.collect(Collectors.toList()))
		.hasSize(1);
	}

}
