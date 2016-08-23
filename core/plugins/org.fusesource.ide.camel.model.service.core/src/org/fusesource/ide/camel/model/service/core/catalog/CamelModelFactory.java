/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.fusesource.ide.camel.model.service.core.catalog;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.m2e.core.MavenPlugin;
import org.fusesource.ide.camel.model.service.core.CamelServiceManagerUtil;
import org.fusesource.ide.camel.model.service.core.ICamelManagerService;
import org.fusesource.ide.camel.model.service.core.internal.CamelModelServiceCoreActivator;
import org.osgi.framework.Version;

/**
 * @author lhein
 *
 */
public class CamelModelFactory {
	
	private static final String CAMEL_VERSION_2_15 = "2.15";
	private static final String FUSE_VERSION_6_3_0_REDHAT_077 = "6.3.0.redhat-077";
	private static final String FUSE_VERSION_6_2_1_REDHAT_084 = "6.2.1.redhat-084";
	private static HashMap<String, CamelModel> supportedCamelModels;
	
	/**
	 * initializes all available models for the connectors group of the camel editor palette
	 */
	public static void initializeModels() {
		supportedCamelModels = new HashMap<String, CamelModel>();
		
		String[] versions = CamelServiceManagerUtil.getAvailableVersions();
		for (String version : versions) {
			supportedCamelModels.put(version, null); // we initialize on access
		}
	}
	
	/**
	 * /!\ Used for Unit testing only
	 * 
	 * @param mockedSupportedCamelModels
	 */
	@Deprecated
	public static void initializeModels(HashMap<String, CamelModel> mockedSupportedCamelModels) {
		supportedCamelModels = mockedSupportedCamelModels;
	}

	/**
	 * returns the list of supported camel versions
	 * 
	 * @return
	 */
	public static List<String> getSupportedCamelVersions() {
		if (supportedCamelModels == null || supportedCamelModels.isEmpty()) {
			initializeModels();
		}
		return Arrays.asList(supportedCamelModels.keySet().toArray(new String[supportedCamelModels.size()]));
	}
	
	/**
	 * returns the supported major.minor.micro version for the given major.minor version
	 * (for instance handing over 2.17 will return 2.17.1.redhat-xxxxx)
	 * 
	 * @param majorMinorVersion	a version string in format major.minor (2.17)
	 * @return	the full supported camel version (2.17.1.redhat-630xxx) or null if not supported
	 */
	private static String getCamelVersionFor(String majorMinorVersion) {
		String resVal = null;
		for (String supportedVersion : getSupportedCamelVersions()) {
			if (supportedVersion.startsWith(majorMinorVersion + ".")) {
				// we want to return the latest greatest for the version
				if (resVal == null || supportedVersion.compareTo(resVal)>0) {
					resVal = supportedVersion;
				}
			}
		}
		return resVal;
	}
	
	/**
	 * returns the model for a given camel version or null if not supported
	 * 
	 * @param camelVersion
	 * @return
	 */
	public static CamelModel getModelForVersion(String camelVersion) {
		CamelModel cm = supportedCamelModels.get(camelVersion);
		
		if (!supportedCamelModels.containsKey(camelVersion)) {
			// seems user wants a version we don't have - look for compatible
			// alternative supported version of camel
			String alternateVersion = getCompatibleCamelVersion(camelVersion);
			CamelModelServiceCoreActivator.pluginLog().logWarning("Selected Camel version " + camelVersion + " is not directly supported. Using alternative version: " + alternateVersion);
			cm = supportedCamelModels.get(alternateVersion);
		}
		
		if (cm == null) {
			// not initialized yet
			ICamelManagerService svc = CamelServiceManagerUtil.getManagerService(camelVersion);
			if (svc != null) {
				cm = svc.getCamelModel();
				cm.setCamelVersion(camelVersion);
				supportedCamelModels.put(camelVersion, cm);
			}
		}
		
		return cm;
	}
	
	/**
	 * returns an alternative for the requested camel version
	 * 
	 * @param requestedCamelVersion
	 * @param supportedVersions
	 * @param latestCamelVersion
	 * @return
	 */
	static String getCompatibleCamelVersion(String requestedCamelVersion, List<String> supportedVersions, String latestCamelVersion) {
		String alternative = null;
		String lastFound = null;
		String smallestVersion = null;
		Version reqVersion = new Version(requestedCamelVersion);
		for (String supV : supportedVersions) {
			Version testVersion = new Version(supV);
			if (testVersion.compareTo(reqVersion) == 0) {
				// in case we support the requested version directly we
				// should use it
				return supV;
			}
			if (testVersion.compareTo(reqVersion) < 0) {
				if (lastFound == null) {
					lastFound = supV;
				} else {
					Version lastCompatible = new Version(lastFound);
					if (testVersion.compareTo(lastCompatible)>0) {
						lastFound = supV;
					}
				}
			} else {
				// this case is determining the smallest available camel version
				if (smallestVersion == null) {
					smallestVersion = supV;
				} else {
					Version smallest = new Version(smallestVersion);
					if (testVersion.compareTo(smallest)<0) {
						smallestVersion = supV;
					}
				}
			}
		}
		alternative = lastFound;
		
		// in case the requested version is earlier than all we have available
		// then we simply return our earliest available version
		if (lastFound == null) {
			alternative = smallestVersion;
		}
		
		return alternative != null ? alternative : latestCamelVersion;
	}
	
	/**
	 * retrieves a compatible version of camel which is shipped with the tools
	 * 
	 * @param requestedCamelVersion
	 * @return
	 */
	public static String getCompatibleCamelVersion(String requestedCamelVersion) {
		return getCompatibleCamelVersion(requestedCamelVersion, getSupportedCamelVersions(), getLatestCamelVersion());
	}
	
	/**
	 * returns the latest and greatest supported Camel version we have a catalog 
	 * for. If there are 2 catalogs with the same version (for instance 2.15.1 and 
	 * 2.15.1.redhat-114) then we will always prefer the Red Hat variant.
	 * 
	 * @return
	 */
	public static String getLatestCamelVersion() {
		String latest = null;
		for (String v : supportedCamelModels.keySet()) {
			if (latest == null) {
				latest = v;
			} else if (v.compareTo(latest)>0) {
				latest = v;
			}
		}
		if (latest != null) return latest;
		
		return supportedCamelModels.keySet().iterator().next();
	}
	
	/**
	 * TODO   This method should be used as much as possible to make sure
	 * the editor is pulling the proper model for the given project. 
	 * 
	 * 
	 * @return
	 */
	public static String getCamelVersion(IProject p) {
		// TODO stubbed out for now. We should check the facets if possible and fallback to the version used in the pom.xml
		String version = getCamelVersionFromMaven(p);
		if (version != null && 
			CamelModelFactory.getSupportedCamelVersions().contains(version)) {
			return version;
		}
		return getLatestCamelVersion();
	}
	
	/**
	 * checks for the camel version in the dependencies of the pom.xml
	 * 
	 * @param project
	 * @return
	 */
	public static String getCamelVersionFromMaven(IProject project) {
		if (project == null) return null;
		IPath pomPathValue = project.getProject().getRawLocation() != null ? project.getProject().getRawLocation().append("pom.xml") : ResourcesPlugin.getWorkspace().getRoot().getLocation().append(project.getFullPath().append("pom.xml"));
        String pomPath = pomPathValue.toOSString();
        final File pomFile = new File(pomPath);
        if (pomFile.exists() == false || pomFile.isDirectory()) return null;
        try {
        	final Model model = MavenPlugin.getMaven().readModel(pomFile);

        	// get camel-core or another camel dep
	        List<Dependency> deps = model.getDependencies();
	        for (Dependency pomDep : deps) {
	            if (pomDep.getGroupId().equalsIgnoreCase("org.apache.camel") &&
	                pomDep.getArtifactId().startsWith("camel-")) {
	                return pomDep.getVersion();
	            }
	        }
        } catch (Exception ex) {
        	CamelModelServiceCoreActivator.pluginLog().logError("Unable to load camel version from " + pomPath, ex);
        }
        return null;
	}

	public static String getFuseVersionForCamelVersion(String camelVersion) {
		if(camelVersion.startsWith(CAMEL_VERSION_2_15)){
			return FUSE_VERSION_6_2_1_REDHAT_084;
		} else {
			return FUSE_VERSION_6_3_0_REDHAT_077;
		}
	}
}
