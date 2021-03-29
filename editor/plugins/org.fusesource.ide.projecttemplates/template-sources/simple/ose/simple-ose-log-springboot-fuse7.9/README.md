# Spring Boot Camel XML QuickStart

This example demonstrates how to configure Camel routes in Spring Boot via
a Spring XML configuration file.

The application utilizes the Spring [`@ImportResource`](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/context/annotation/ImportResource.html) annotation to load a Camel Context definition via a _src/main/resources/spring/camel-context.xml_ file on the classpath.

IMPORTANT: This quickstart can run in 2 modes: standalone on your machine and on Kubernetes / OpenShift Cluster

## Deployment options

You can run this quickstart in the following modes:

* Kubernetese / Single-node OpenShift cluster
* Standalone on your machine

The most effective way to run this quickstart is to deploy and run the project on OpenShift.

For more details about running this quickstart on a single-node OpenShift cluster, CI/CD deployments, as well as the rest of the runtime, see the [Spring Boot Runtime Guide](https://appdev.openshift.io/docs/spring-boot-runtime.html).

NOTE: Eclipse Fuse Tooling is providing specific menus to run it in a more preconfigured and integrated way.

## Running the Quickstart on a single-node Kubernetes/OpenShift cluster

IMPORTANT: You need to run this example on Container Development Kit 3.3+ or OpenShift 3.7+.

Both of these products have suitable Fuse images pre-installed.
If you run it in an environment where those images are not preinstalled follow the steps described in [this section](#single-node-without-preinstalled-images).

A single-node Kubernetes/OpenShift cluster provides you with access to a cloud environment that is similar to a production environment.

If you have a single-node Kubernetes/OpenShift cluster, such as Minishift or the Red Hat Container Development Kit, [installed and running](https://appdev.openshift.io/docs/minishift-installation.html), you can deploy your quickstart there.


* Log in to your OpenShift cluster:

    $ oc login -u developer -p developer

* Create a new OpenShift project for the quickstart:

    $ oc new-project MY_PROJECT_NAME

* Build and deploy the project to the OpenShift cluster:

    $ mvn clean -DskipTests oc:deploy -Popenshift

* In your browser, navigate to the `MY_PROJECT_NAME` project in the OpenShift console.
Wait until you can see that the pod for the `spring-boot-camel-xml` has started up.

* On the project's `Overview` page, navigate to the details page deployment of the `spring-boot-camel-xml` application: `https://OPENSHIFT_IP_ADDR:8443/console/project/MY_PROJECT_NAME/browse/rcs/spring-boot-camel-xml-NUMBER_OF_DEPLOYMENT?tab=details`.

* Switch to tab `Logs` and then see the messages sent by Camel.

<a name="single-node-without-preinstalled-images"></a>

### Running the Quickstart on a single-node Kubernetes/OpenShift cluster without preinstalled images through CLI

A single-node Kubernetes/OpenShift cluster provides you with access to a cloud environment that is similar to a production environment.

If you have a single-node Kubernetes/OpenShift cluster, such as Minishift or the Red Hat Container Development Kit, [installed and running](http://appdev.openshift.io/docs/minishift-installation.html), you can deploy your quickstart there.


* Log in to your OpenShift cluster:

    $ oc login -u developer -p developer

* Create a new OpenShift project for the quickstart:

    $ oc new-project MY_PROJECT_NAME

* Import base images in your newly created project (MY_PROJECT_NAME):

    $ oc import-image fuse-java-openshift:2.0 --from=registry.access.redhat.com/jboss-fuse-7/fuse-java-openshift:2.0 --confirm

* Build and deploy the project to the OpenShift cluster:

    $ mvn clean -DskipTests oc:deploy -Popenshift -Djkube.generator.fromMode=istag -Djkube.generator.from=MY_PROJECT_NAME/fis-java-openshift:2.0

* In your browser, navigate to the `MY_PROJECT_NAME` project in the OpenShift console.
Wait until you can see that the pod for the `spring-boot-camel-xml` has started up.

* On the project's `Overview` page, navigate to the details page deployment of the `spring-boot-camel-xml` application: `https://OPENSHIFT_IP_ADDR:8443/console/project/MY_PROJECT_NAME/browse/pods/spring-boot-camel-xml-NUMBER_OF_DEPLOYMENT?tab=details`.

* Switch to tab `Logs` and then see the messages sent by Camel.

## Running the quickstart standalone on your machine through CLI

To run this quickstart as a standalone project on your local machine:

* Build the project:

    $ mvn clean package
    
* Run the service:

    $ mvn spring-boot:run

* See the messages sent by Camel.
