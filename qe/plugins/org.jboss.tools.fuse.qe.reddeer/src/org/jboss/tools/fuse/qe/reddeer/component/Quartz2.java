/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.fuse.qe.reddeer.component;

public class Quartz2 implements CamelComponent {

	@Override
	public String getPaletteEntry() {
		return "Quartz2";
	}

	@Override
	public String getLabel() {
		return "quartz2:groupName/triggerName";
	}

	@Override
	public String getTooltip() {
		return null;
	}

}