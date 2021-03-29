/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.fusesource.ide.projecttemplates.impl.simple;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;

import org.fusesource.ide.projecttemplates.wizards.pages.model.EnvironmentData;
import org.fusesource.ide.projecttemplates.wizards.pages.model.FuseRuntimeKind;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class OSESpringBootXMLTemplateForFuse79Test {

	@Parameter
	public String version;
	
	@Parameter(1)
	public boolean isCompatible;
	
	@Parameters(name = "{0} should be compatible? {1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{ "2.23.2.fuse-790XXX-redhat-X", true },
			{ "2.23.2.fuse-780XXX-redhat-X", false },
			{ "2.23.2.fuse-770010-redhat-00001", false },
			{ "2.21.0.fuse-710XXX-redhat-X", false },
			{ "2.21.1", false },
			{ "2.21.0.fuse-000XXX-redhat-X", false },
			{ "2.22.0", false },
			{ "2.21.0", false },
			{ "2.20.0", false },
			{ "2.19.9", false },
		});
	}
	
	@Test
	public void testIsCompatible() throws Exception {
		assertThat(new OSESpringBootXMLTemplateForFuse79().isCompatible(new EnvironmentData(version, null, FuseRuntimeKind.SPRINGBOOT))).isEqualTo(isCompatible);
	}

}
