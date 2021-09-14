-include= ~${workspace}/cnf/resources/bnd/feature.props
symbolicName=com.ibm.websphere.appserver.mpOpenTracing-3.0
visibility=public
singleton=true
IBM-App-ForceRestart: install, \
 uninstall
IBM-ShortName: mpOpenTracing-3.0
Subsystem-Name: MicroProfile OpenTracing 3.0
IBM-API-Package: \
    org.eclipse.microprofile.opentracing; type="stable"
-features=com.ibm.websphere.appserver.opentracing-3.0, \
  com.ibm.websphere.appserver.mpConfig-3.0, \
  com.ibm.websphere.appserver.org.eclipse.microprofile.opentracing-3.0, \
  io.openliberty.mpCompatible-5.0
-bundles=\
    io.openliberty.microprofile.opentracing.3.0.internal
kind=ga
edition=core
WLP-Activation-Type: parallel
