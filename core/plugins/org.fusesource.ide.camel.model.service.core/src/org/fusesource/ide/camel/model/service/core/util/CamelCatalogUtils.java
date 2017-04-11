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
package org.fusesource.ide.camel.model.service.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.fusesource.ide.camel.model.service.core.catalog.Dependency;
import org.fusesource.ide.camel.model.service.core.catalog.cache.CamelCatalogCoordinates;
import org.fusesource.ide.camel.model.service.core.internal.CamelModelServiceCoreActivator;
import org.fusesource.ide.foundation.core.util.Strings;

/**
 * collection of camel catalog related util methods
 * 
 * @author lhein
 */
public class CamelCatalogUtils {
	
	public static final String CAMEL_SPRING_BOOT_STARTER = "camel-spring-boot-starter";
	public static final String CAMEL_WILDFLY = "org.wildfly.camel";
	
	public static final String DEFAULT_CAMEL_VERSION = "2.18.1.redhat-000012";
	private static final String FUSE_63_R1_BOM_VERSION = "6.3.0.redhat-224";	
	
	public static final String RUNTIME_PROVIDER_KARAF = "karaf";
	public static final String RUNTIME_PROVIDER_SPRINGBOOT = "springboot";
	public static final String RUNTIME_PROVIDER_WILDFLY = "wildfly";

	public static final String CATALOG_CAMEL_ARTIFACTID = "camel-catalog";
	
	public static final String CATALOG_KARAF_GROUPID = "org.apache.camel";
	public static final String CATALOG_KARAF_ARTIFACTID = "camel-catalog-provider-karaf";
	
	public static final String CATALOG_SPRINGBOOT_GROUPID = "org.apache.camel";
	public static final String CATALOG_SPRINGBOOT_ARTIFACTID = "camel-catalog-provider-springboot";
	
	public static final String CATALOG_WILDFLY_GROUPID = CAMEL_WILDFLY;
	public static final String CATALOG_WILDFLY_ARTIFACTID = "wildfly-camel-catalog";
	
	public static final String GAV_KEY_GROUPID = "groupId";
	public static final String GAV_KEY_ARTIFACTID = "artifactId";
	public static final String GAV_KEY_VERSION = "version";
	
	private static final List<String> OFFICAL_SUPPORTED_CAMEL_CATALOG_VERSIONS;
	private static final Map<String, String> camelVersionToFuseBOMMapping;
	static {
		camelVersionToFuseBOMMapping = new HashMap<>();
		camelVersionToFuseBOMMapping.put("2.15.1.redhat-621084", "6.2.1.redhat-084");
		camelVersionToFuseBOMMapping.put("2.15.1.redhat-621117", "6.2.1.redhat-117");
		camelVersionToFuseBOMMapping.put("2.17.0.redhat-630187", "6.3.0.redhat-187");
		camelVersionToFuseBOMMapping.put("2.17.0.redhat-630224", FUSE_63_R1_BOM_VERSION);
		
		OFFICAL_SUPPORTED_CAMEL_CATALOG_VERSIONS = new ArrayList<>();
		OFFICAL_SUPPORTED_CAMEL_CATALOG_VERSIONS.add("2.17.0.redhat-630187");
		OFFICAL_SUPPORTED_CAMEL_CATALOG_VERSIONS.add("2.17.0.redhat-630224");
		OFFICAL_SUPPORTED_CAMEL_CATALOG_VERSIONS.add("2.18.1.redhat-000012");
	}
	
	private static final Set<String> pureFisVersions = Stream.of("2.18.1.redhat-000012").collect(Collectors.toSet());
	
	private static final String LATEST_BOM_VERSION = FUSE_63_R1_BOM_VERSION;

	private CamelCatalogUtils() {
		// utility class
	}
	
	public static List<String> getOfficialSupportedCamelCatalogVersions() {
		return OFFICAL_SUPPORTED_CAMEL_CATALOG_VERSIONS;
	}
	
	/**
	 * returns the latest and greatest supported Camel version we have a catalog 
	 * for. If there are 2 catalogs with the same version (for instance 2.15.1 and 
	 * 2.15.1.redhat-114) then we will always prefer the Red Hat variant.
	 * 
	 * @return
	 */
	public static String getLatestCamelVersion() {
		return DEFAULT_CAMEL_VERSION;
	}

	/**
	 * tries to map a FUSE BOM version to the Camel version
	 * 
	 * @param camelVersion
	 * @return
	 */
	public static String getFuseVersionForCamelVersion(String camelVersion) {
		String bomVersion = camelVersionToFuseBOMMapping.get(camelVersion);
		if (bomVersion == null) {
			bomVersion = LATEST_BOM_VERSION;
		}
		return bomVersion;
	}
	
	/**
	 * checks if the given camel version is a pure fis version
	 * 
	 * @param camelVersion
	 * @return
	 */
	public static boolean isPureFISVersion(String camelVersion) {
		return pureFisVersions.contains(camelVersion);
	}
	
	/**
	 * takes a label from the catalog which is a possibly comma separated string and splits it into pieces storing 
	 * each of the substrings in a string array list
	 * 
	 * @param label
	 * @return
	 */
	public static List<String> initializeTags(String label) {
		List<String> tags = new ArrayList<>();
		if (label != null && label.trim().length()>0) {
			String[] tagArray = label.split(",");
			for (String s_tag : tagArray) {
				tags.add(s_tag);
			}
		}
		return tags;
	}
	
	private static boolean isValidGAV(String groupId, String artifactId, String version) {
		return 	!Strings.isBlank(groupId) &&
				!Strings.isBlank(artifactId) && 
				!Strings.isBlank(version);
	}
	
	/**
	 * returns the catalog coordinates for the given maven coords
	 * 
	 * @param groupId
	 * @param artifactId
	 * @param version
	 * @return
	 */
	public static CamelCatalogCoordinates getCatalogCoordinatesFor(String groupId, String artifactId, String version) {
		if (!CATALOG_CAMEL_ARTIFACTID.equals(artifactId) && isCamelVersionWithoutProviderSupport(version)) {
			CamelCatalogCoordinates coord = getDefaultCatalogCoordinates();
			coord.setArtifactId(CATALOG_CAMEL_ARTIFACTID);
			coord.setVersion(version);
			return coord;
		}
		return new CamelCatalogCoordinates(groupId, artifactId, version);
	}
	
	/**
	 * returns the catalog coordinates for the given maven coords
	 * 
	 * @param groupId
	 * @param artifactId
	 * @param version
	 * @return
	 */
	public static CamelCatalogCoordinates getCatalogCoordinatesFor(String runtimeProvider, String version) {
		if (isCamelVersionWithoutProviderSupport(version)) {
			CamelCatalogCoordinates coord = getDefaultCatalogCoordinates();
			coord.setArtifactId(CATALOG_CAMEL_ARTIFACTID);
			coord.setVersion(version);
			return coord;
		}
		if (RUNTIME_PROVIDER_SPRINGBOOT.equalsIgnoreCase(runtimeProvider)) {
			return new CamelCatalogCoordinates(CATALOG_SPRINGBOOT_GROUPID, CATALOG_SPRINGBOOT_ARTIFACTID, version);
//		} else if (RUNTIME_PROVIDER_WILDFLY.equalsIgnoreCase(runtimeProvider)) {
//			return new CamelCatalogCoordinates(CATALOG_WILDFLY_GROUPID, CATALOG_WILDFLY_ARTIFACTID, version);
		} else {
			return new CamelCatalogCoordinates(CATALOG_KARAF_GROUPID, CATALOG_KARAF_ARTIFACTID, version);
		}
	}
	
	public static boolean isCamelVersionWithoutProviderSupport(String version) {
		if (version == null) return true; // happens if no camel dep is defined in the pom.xml
		ComparableVersion v1 = new ComparableVersion(version);
		ComparableVersion v2 = new ComparableVersion("2.18.1");
		return v1.compareTo(v2) < 0;
	}
	
	public static CamelCatalogCoordinates getDefaultCatalogCoordinates() {
		return CamelCatalogUtils.getCatalogCoordinatesFor(CATALOG_KARAF_GROUPID, CATALOG_KARAF_ARTIFACTID, DEFAULT_CAMEL_VERSION);
	}
	
	public static CamelCatalogCoordinates getCatalogCoordinatesForProject(IProject project) {
		String camelVersion = CamelMavenUtils.getCamelVersionFromMaven(project);
//		String wildFlyCamelVersion = CamelMavenUtils.getWildFlyCamelVersionFromMaven(project);
		String runtimeProvider = CamelCatalogUtils.getRuntimeprovider(project, new NullProgressMonitor());
		if (CamelCatalogUtils.RUNTIME_PROVIDER_SPRINGBOOT.equalsIgnoreCase(runtimeProvider)) {
			return CamelCatalogUtils.getCatalogCoordinatesFor(CATALOG_SPRINGBOOT_GROUPID, CATALOG_SPRINGBOOT_ARTIFACTID, camelVersion);
//		} else if (CamelCatalogUtils.RUNTIME_PROVIDER_WILDFLY.equalsIgnoreCase(runtimeProvider)) {
//			return CamelCatalogUtils.getCatalogCoordinatesFor(CATALOG_WILDFLY_GROUPID, CATALOG_WILDFLY_ARTIFACTID, wildFlyCamelVersion);
		} else {
			// unsupported - fall back to Karaf
			return CamelCatalogUtils.getCatalogCoordinatesFor(CATALOG_KARAF_GROUPID, CATALOG_KARAF_ARTIFACTID, camelVersion);
		}
	}
	
	public static String getRuntimeprovider(IProject camelProject, IProgressMonitor monitor) {
		if(camelProject != null){
			IMavenProjectFacade m2prj = MavenPlugin.getMavenProjectRegistry().create(camelProject, monitor);
			try {
				if(m2prj != null){
					MavenProject mavenProject = m2prj.getMavenProject(monitor);
					if(mavenProject != null){
						List<org.apache.maven.model.Dependency> dependencies = mavenProject.getDependencies();
						return getRuntimeProviderFromDependencyList(dependencies);
					}
				}
			} catch (CoreException e) {
				CamelModelServiceCoreActivator.pluginLog().logWarning(e);
			}
		}
		return RUNTIME_PROVIDER_KARAF;
	}
	
	public static String getRuntimeProviderFromDependency(org.apache.maven.model.Dependency dependency) {
		List<org.apache.maven.model.Dependency> deps = Arrays.asList(dependency);
		return getRuntimeProviderFromDependencyList(deps);
	}
	
	public static String getRuntimeProviderFromDependencyList(List<org.apache.maven.model.Dependency> dependencies) {
		if(hasSpringBootDependency(dependencies)){
			return RUNTIME_PROVIDER_SPRINGBOOT;
//		} else if (hasWildflyDependency(dependencies)) {
//			return RUNTIME_PROVIDER_WILDFLY;
		} else {
			return RUNTIME_PROVIDER_KARAF;
		}
	}
		
	public static boolean hasSpringBootDependency(List<org.apache.maven.model.Dependency> dependencies){
		return dependencies != null
				&& dependencies.stream()
					.filter(dependency -> CAMEL_SPRING_BOOT_STARTER.equals(dependency.getArtifactId()))
					.findFirst().isPresent();
	}
	
	public static boolean hasWildflyDependency(List<org.apache.maven.model.Dependency> dependencies){
		return dependencies != null
				&& dependencies.stream()
					.filter(dependency -> CAMEL_WILDFLY.equals(dependency.getGroupId()))
					.findFirst().isPresent();
	}
	
	public static void parseDependencies(List<Dependency> dependencies, Map<String, String> properties) {
		String grpId = properties.get(GAV_KEY_GROUPID);
		String artId = properties.get(GAV_KEY_ARTIFACTID);
		String version = properties.get(GAV_KEY_VERSION);
		
		if (isValidGAV(grpId, artId, version)) {
			// we process only fully specified dependencies
			Dependency dep = new Dependency();
			
			dep.setGroupId(grpId);
			dep.setArtifactId(artId);
			dep.setVersion(version);
			
			dependencies.add(dep);
		}
	}
}
