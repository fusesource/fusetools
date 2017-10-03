/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.fusesource.ide.server.karaf.core.server.subsystems;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.wst.server.core.IServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Karaf4xStartupLaunchConfiguratorTest {
	@Mock
	private IServer server;
	@InjectMocks
	private Karaf4xStartupLaunchConfigurator karaf4xStartupLaunchConfigurator;

	@Test
	public void testGetVMArguments() throws Exception {
		String vmArguments = karaf4xStartupLaunchConfigurator.getVMArguments("karafInstallDir", "endorsedDirs", "extDirs");
		
		assertThat(vmArguments)
		.doesNotContain("PermSize")
		.contains("karaf.etc")
		.doesNotContain("derby");
	}

}
