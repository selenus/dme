# Data Modeling Environment (DME)

The Data Modeling Environment (DME) is a component of the DARIAH-DE Data Federation Architecture (DFA) software stack. The main intention of the DME is to enable domain experts in the arts and humanities to specify and correlate data structures in order to make data more accessible. The DME is a web-application and the frontend to the grammatical transformation framework, which will be published to GitHub in the near future. A particular focus lies on the explication of contextual knowledge in the form of rules to (1) define data in terms of domain specific languages and (2) provide transformative functions that operate of parsed instances of the data.

Issues for the DME are tracked here: https://minfba.de.dariah.eu/mantisbt/set_project.php?project_id=11

Further information on the concepts behind the DME are accessible at https://de.dariah.eu/dme.

Please note that the following Instructions are primarily oriented towards Linux-based environments - specifically DARIAH-DE/Ubuntu/Tomcat8/Apache. Please evaluate and modify the steps according to your installation environment.

## 1) Prerequisites

The installation of an instance of the DME requires the setup of some required components:
* An installed Java, minimum version JavaSE-1.8 - **install the JDK as the DME creates and compiles Java code** 
* A Java web-application server such as Tomcat or Jetty
* MongoDB 3 as storage backend

## 2) Installation

Aside from building the application on your own, two primary installation methods are provided. For debian-based systems, the installation of the *deb package is recommended* as it takes care of some post-installation steps and thus simplifies installation and upgrades. 

### 2.1) Debian Repository

The DARIAH-DE developer portal generates and provides packages for a simplified installation of the DME on Debian-based machines. The repository can be found at: https://ci.de.dariah.eu/packages/

#### Register repository as apt source
To install productive versions:
```
echo "deb [arch=amd64] https://ci.de.dariah.eu/packages trusty releases" > /etc/apt/sources.list.d/dariah_apt_repository.list
```
For development releases
```
echo "deb [arch=amd64] https://ci.de.dariah.eu/packages trusty snapshots" > /etc/apt/sources.list.d/dariah_apt_repository.list
```
#### Register repository as apt source
```
wget -O - https://ci.de.dariah.eu/packages/repository.asc | sudo apt-key add -
```

#### Update apt sources and install DME
```
apt-get update && apt-get install dme
```
Continue with the steps in 2.3)

### 2.2) WAR Container

As an alternative installation method WAR container files of the DME can be found at:
* Production releases: https://minfba.de.dariah.eu/artifactory/webapp/#/artifacts/browse/tree/General/dariah-minfba-releases/de/unibamberg/minf/dme
* Snapshot releases: https://minfba.de.dariah.eu/artifactory/webapp/#/artifacts/browse/tree/General/dariah-minfba-snapshots/de/unibamberg/minf/dme

To install the dme, simply place the WAR container in the applications directory of your web server - e.g. */path/to/tomcat/webapps* for Tomcat.

The post-installation script within the debian package creates some directories and files and downloads NLP models (see the following section 2.3). In case of a manual installation, these steps need to be executed as well. Have a look at the [post-install bash script](https://github.com/tgradl/dme/blob/master/src/deb/control/postinst).


### 2.3) Post-installation setup

#### Directory setup
The deb package (post-installation script):
* installs the DME at */var/dfa/webapps/dme*. This directory needs to be linked from within the Tomcat webapps directory (e.g. in a standard Ubuntu install: `ln -s /var/dfa/webapps/dme/ /var/lib/tomcat8/webapps/`)
* creates */etc/dfa/dme* and installs the simple sample configuration if *dme.yml* does not yet exist. This configuration will probably need to be adapted, which is discussed in section 3) of this page.
* creates - if it does not exist - */var/lib/dme/models* and downloads some Stanford CoreNLP and OpenNLP models required for NLP processing
* creates - if it does not exist - */var/lib/dme/grammars*: **make sure the Tomcat running user has write access to this directory** (in a standard Ubuntu install: `chown -R tomcat8 /var/lib/dme/grammars`)

#### Configure Tomcat
Although the debian package comes with a complete configuration setup, manual registration of the dme configuration file is explicitly required. Modify/create the *setenv.sh* script within the *bin* directory of the installed Tomcat (default Ubuntu/Tomcat8 `/usr/share/tomcat8/bin/`) to point to the dme configuration:
```
#!/bin/sh
# Application specific environment variables

export CATALINA_OPTS="$CATALINA_OPTS -Ddme.yml=/etc/dfa/dme/dme.yml"
```

### 2.3) Test startup
Start/restart Tomcat: the DME will be accessible at http://localhost:8080/dme. To debug startup issues analyze the Tomcat log file (`/var/log/tomcat8/catalina.out`)

![Empty DME Startup page](https://github.com/tgradl/dme/raw/master/docs/img/screenshot-empty-startup.png "Empty DME Startup page")

## 3) Additional configuration

### 3.1) Tomcat behind proxy
If the tomcat server is running behind a web proxy, additional configuration steps are required in order to let the application know about the outside perspective (e.g. to show correct links).

#### Tomcat configuration
If the proxy server is providing SSL access over port 443 (recommended), modify the Tomcat *conf/server.xml* file (Ubuntu/Tomcat8: */var/lib/tomcat8/conf/server.xml*) and update the Connector for port 8080 to
```
<Connector port="8080" protocol="HTTP/1.1" connectionTimeout="20000" proxyPort="443" scheme="https" />
```

Based on an Apache as proxy server, the host configuration is required to pass some header information to the tomcat. An appropriate proxy configuration could be as follows. Modify accordingly or adapt as nginx configuration.
```
# Pass host information to tomcat 
ProxyPreserveHost on

# Redirect incomplete paths to the actual tomcat equivalent
Redirect /dme https://dme.de.dariah.eu/dme/
Redirect / https://dme.de.dariah.eu/dme/

# Actual proxy
ProxyPass /dme/ http://127.0.0.1:8080/dme/
ProxyPassReverse /dme/ http://127.0.0.1:8080/dme/

```

If the Apache proxy has been setup correctly, the dme should be available at https://localhost:443 or - in the case of the above snippet - https://dme.de.dariah.eu.

Hint: make sure that the Apache server has the modules *ssl*, *proxy* and *proxy-http* enabled.

### 3.2) SAML Integration
The DME includes components of the [dariahsp](https://github.com/tgradl/dariahsp) and is prepared for integration with SAML-based AAI infrastructures such as the [DFN-AAI](https://www.aai.dfn.de/). Please review the documentation of the [dariahsp](https://github.com/tgradl/dariahsp) component in order to create custom configurations.

Here, only the default DARIAH-DE/DFN-AAI case is illustrated - skipping some steps that might be necessary in other scenarios.

#### Java keystore (JKS)
The DME requires a Java keystore (JKS) to be present and configured in order to generate SAML metadata. A JKS can easily be created by means of openssl as illustrated [here](https://github.com/tgradl/dariahsp#java-keystore).

Modify the dme.yml configuration according to the available JKS, appending the following snippet (modify accordingly)
```
  saml:
    keystore:
      path: /etc/dfa/key/dfa-de-dariah-eu.jks
      # Uncomment if keystore is protected by password
      #pass: 'somepass'
      alias: dfa.de.dariah.eu
      aliaspass: ''
```

#### DFN-AAI Metadata
Point the configuration towards the correct DFN-AAI metadata. For new installations this will typically be metadata of the testfederation, which might later be changed to productive metadata. Append some configuration to the existing *saml* block:
```
    metadata:
      url: https://www.aai.dfn.de/fileadmin/metadata/dfn-aai-test-metadata.xml
      #url: https://www.aai.dfn.de/fileadmin/metadata/dfn-aai-basic-metadata.xml
```

#### SP Metadata
To be able to register your installation of the DME with the DFN-AAI, you will need to create appropriate service provider (SP) metadata. While the DME includes a webbased SAML metadata management component (see https://github.com/tgradl/dariahsp#2-saml-sp-metadata), the fastest way to produce metadata is to provide some sp configuration parameters - leading to the automaic generation of deployable SP metadata.

Append the following configuration to the existing *saml* block - taking particular care of:
* *baseUrl*: this is the base URL of the installation as configured e.g. in the apache proxy above
* *entityId*: use a good identifier of your SP. The baseUrl is a good and commonly used entityId.
* *signingKey*, *encryptionKey* and *tlsKey* point to the correct alias within your JKS
* *discovery.return* is build from the *baseUrl* and the *alias* of the installation

```
    sp:
      maxAuthAge: -1
      alias: dme
      baseUrl: https://dme.de.dariah.eu/dme
      entityId: https://dme.de.dariah.eu/dme
      securityProfile: metaiop
      sslSecurityProfile: pkix
      sslHostnameVerification: default
      signMetadata: true
      signingAlgorithm: "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"
      signingKey: dfa.de.dariah.eu
      encryptionKey: dfa.de.dariah.eu
      tlsKey: dfa.de.dariah.eu
      requireArtifactResolveSigned: true
      requireAttributeQuerySigned: true
      requireLogoutRequestSigned: true
      requireLogoutResponseSigned: false
      discovery:
        enabled: true
        url: https://wayf.aai.dfn.de/DFN-AAI-Test/wayf
        #url: https://auth.dariah.eu/CDS/WAYF
        return: https://dme.de.dariah.eu/dme/saml/login/alias/dme?disco:true
      allowedNameIds : EMAIL, TRANSIENT, PERSISTENT, UNSPECIFIED, X509_SUBJECT
    
```

#### Required attributes
The DME relies on the availability of some specific attributes. The DARIAH-DE AAI is based on top of Terms of Use, which are also presented as attributes. In order to be able to generate local metadata that reflects these required attributes, append the following parameters to the existing *saml* block. You will not have to change any parameter in case of a DARIAH-DE-based installation.
```
      # Attribute querying
      attributeQuery:
        enabled: true
        excludedEndpoints: 
          urls: ["https://ldap-dariah-clone.esc.rzg.mpg.de/idp/shibboleth", "https://idp.de.dariah.eu/idp/shibboleth"]
          assumeAttributesComplete: true
        queryIdp: https://ldap-dariah-clone.esc.rzg.mpg.de/idp/shibboleth
        #queryIdp: https://idp.de.dariah.eu/idp/shibboleth
        queryByNameID: false
        queryAttribute:
          friendlyName: eduPersonPrincipalName
          name: urn:oid:1.3.6.1.4.1.5923.1.1.1.6
          nameFormat: urn:oasis:names:tc:SAML:2.0:attrname-format:uri
        # For now without parameters bc DARIAH Self Service is broken 
        incompleteAttributesRedirect: "https://dariah.daasi.de/Shibboleth.sso/Login?target=/cgi-bin/selfservice/ldapportal.pl"
        #incompleteAttributesRedirect: "https://dariah.daasi.de/Shibboleth.sso/Login?target=/cgi-bin/selfservice/ldapportal.pl%3Fmode%3Dauthenticate%3Bshibboleth%3D1%3Bnextpage%3Dregistration%3Breturnurl%3D{returnUrl}&entityID={entityId}"
        #incompleteAttributesRedirect: "https://auth.dariah.eu/Shibboleth.sso/Login?target=/cgi-bin/selfservice/ldapportal.pl%3Fmode%3Dauthenticate%3Bshibboleth%3D1%3Bnextpage%3Dregistration%3Breturnurl%3D{returnUrl}&entityID={entityId}"
      requiredAttributes:
        - stage: ATTRIBUTES
          required: true
          attributeGroup:
            - check: AND
              attributes:
                - friendlyName: mail
                  name: urn:oid:0.9.2342.19200300.100.1.3
                  nameFormat: urn:oasis:names:tc:SAML:2.0:attrname-format:uri
        - stage: ATTRIBUTES
          required: true
          attributeGroup:
            - check: OR
              attributes:
                - friendlyName: dariahTermsOfUse
                  name: urn:oid:1.3.6.1.4.1.10126.1.52.4.15
                  nameFormat: urn:oasis:names:tc:SAML:2.0:attrname-format:uri
                  value: Terms_of_Use_v5.pdf
                - friendlyName: dariahTermsOfUse
                  name: urn:oid:1.3.6.1.4.1.10126.1.52.4.15
                  nameFormat: urn:oasis:names:tc:SAML:2.0:attrname-format:uri
                  value: foobar-service-agreement_version1.pdf     
        - stage: AUTHENTICATION
          required: true
          attributeGroup:
            - check: AND
              attributes:
                - friendlyName: eduPersonPrincipalName
                  name: urn:oid:1.3.6.1.4.1.5923.1.1.1.6
                  nameFormat: urn:oasis:names:tc:SAML:2.0:attrname-format:uri
        - stage: AUTHENTICATION
          required: false
          attributeGroup:
            - check: OPTIONAL
              attributes:
                - friendlyName: mail
                  name: urn:oid:0.9.2342.19200300.100.1.3
                  nameFormat: urn:oasis:names:tc:SAML:2.0:attrname-format:uri
                - friendlyName: displayName
                  name: urn:oid:2.16.840.1.113730.3.1.241
                  nameFormat: urn:oasis:names:tc:SAML:2.0:attrname-format:uri
```

#### Modify tomcat configuration and restart
In order to be able to easily switch between authentication mechanisms, a flag is provided as environment parameter. Please modify the *setenv.sh* script within the *bin* directory of your Tomcat installation and append the *saml* flag:
```
export CATALINA_OPTS="$CATALINA_OPTS -Dsaml=true"
```  

When executing a now necessary restart of the Tomcat, closely monitor the Tomcat log in order to find out if parameters around the JKS and metadata has been configured correctly.

You can access the generated metadata at https://your-servername/dme/saml/metadata if everything has been setup correctly. This URL can be used to fill some parameters in the DFN-AAI self-management tools.

Also, please download the metadata yourself and place them within the configuration directory of the DME - i.e. /etc/dfa/dme/sp_metadata.xml and configure the *externalMetadata* parameter in the *dme.yml* under *saml.sp* (see the complete dme.yml sample configuration below). In doing so, no automatic generation of metadata is attempted on every restart of the Tomcat/DME and the configured static metadata is utilized instead. This has proven to be more robust especially in production scenarios.

#### Complete DARIAH-DE sample confguration
This sample configuration comes within the installed DME directory and is also accessible [here](https://github.com/tgradl/dme/blob/master/src/main/resources/dme.sample_dariah.yml)
```
paths:
  main: /var/run/dme
  tmp: /tmp
  logging: ${paths.main}/log
  grammars: ${paths.main}/grammars
  models: ${paths.main}/models
  tmpUploadDir: ${paths.tmp}/dme_uploads/
  sampleFilesDir: ${paths.tmp}/dme_samples/

editors:
  samples:
    maxTravelSize: 50000

db:
  host: 127.0.0.1
  port: 27017
  database: dme

auth:
  local: 
    users:  
      - username: 'admin'
        passhash: '$2a$10$nbXRnAx5wKurTrbaUkT/MOLXKAJgpT8R71/jujzPwgXXrG.OqlBKW'
        roles: ["ROLE_ADMINISTRATOR"]
  saml:
    keystore:
      path: /etc/dfa/key/dfa-de-dariah-eu.jks
      # Uncomment if keystore is protected by password
      #pass: 'somepass'
      alias: dfa.de.dariah.eu
      aliaspass: ''
    metadata:
      url: https://www.aai.dfn.de/fileadmin/metadata/dfn-aai-test-metadata.xml
      #url: https://www.aai.dfn.de/fileadmin/metadata/dfn-aai-basic-metadata.xml
    sp:
      #externalMetadata: /etc/dfa/dme/sp_metadata.xml
      maxAuthAge: -1
      alias: dme
      baseUrl: https://dme.de.dariah.eu/dme
      entityId: https://dme.de.dariah.eu/dme
      securityProfile: metaiop
      sslSecurityProfile: pkix
      sslHostnameVerification: default
      signMetadata: true
      signingAlgorithm: "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"
      signingKey: dfa.de.dariah.eu
      encryptionKey: dfa.de.dariah.eu
      tlsKey: dfa.de.dariah.eu
      requireArtifactResolveSigned: true
      requireAttributeQuerySigned: true
      requireLogoutRequestSigned: true
      requireLogoutResponseSigned: false
      discovery:
        enabled: true
        url: https://wayf.aai.dfn.de/DFN-AAI-Test/wayf
        #url: https://auth.dariah.eu/CDS/WAYF
        return: https://dme.de.dariah.eu/dme/saml/login/alias/dme?disco:true
      allowedNameIds : EMAIL, TRANSIENT, PERSISTENT, UNSPECIFIED, X509_SUBJECT
    
      # Attribute querying
      attributeQuery:
        enabled: true
        excludedEndpoints: 
          urls: ["https://ldap-dariah-clone.esc.rzg.mpg.de/idp/shibboleth", "https://idp.de.dariah.eu/idp/shibboleth"]
          assumeAttributesComplete: true
        queryIdp: https://ldap-dariah-clone.esc.rzg.mpg.de/idp/shibboleth
        #queryIdp: https://idp.de.dariah.eu/idp/shibboleth
        queryByNameID: false
        queryAttribute:
          friendlyName: eduPersonPrincipalName
          name: urn:oid:1.3.6.1.4.1.5923.1.1.1.6
          nameFormat: urn:oasis:names:tc:SAML:2.0:attrname-format:uri
        # For now without parameters bc DARIAH Self Service is broken 
        incompleteAttributesRedirect: "https://dariah.daasi.de/Shibboleth.sso/Login?target=/cgi-bin/selfservice/ldapportal.pl"
        #incompleteAttributesRedirect: "https://dariah.daasi.de/Shibboleth.sso/Login?target=/cgi-bin/selfservice/ldapportal.pl%3Fmode%3Dauthenticate%3Bshibboleth%3D1%3Bnextpage%3Dregistration%3Breturnurl%3D{returnUrl}&entityID={entityId}"
        #incompleteAttributesRedirect: "https://auth.dariah.eu/Shibboleth.sso/Login?target=/cgi-bin/selfservice/ldapportal.pl%3Fmode%3Dauthenticate%3Bshibboleth%3D1%3Bnextpage%3Dregistration%3Breturnurl%3D{returnUrl}&entityID={entityId}"
      requiredAttributes:
        - stage: ATTRIBUTES
          required: true
          attributeGroup:
            - check: AND
              attributes:
                - friendlyName: mail
                  name: urn:oid:0.9.2342.19200300.100.1.3
                  nameFormat: urn:oasis:names:tc:SAML:2.0:attrname-format:uri
        - stage: ATTRIBUTES
          required: true
          attributeGroup:
            - check: OR
              attributes:
                - friendlyName: dariahTermsOfUse
                  name: urn:oid:1.3.6.1.4.1.10126.1.52.4.15
                  nameFormat: urn:oasis:names:tc:SAML:2.0:attrname-format:uri
                  value: Terms_of_Use_v5.pdf
                - friendlyName: dariahTermsOfUse
                  name: urn:oid:1.3.6.1.4.1.10126.1.52.4.15
                  nameFormat: urn:oasis:names:tc:SAML:2.0:attrname-format:uri
                  value: foobar-service-agreement_version1.pdf     
        - stage: AUTHENTICATION
          required: true
          attributeGroup:
            - check: AND
              attributes:
                - friendlyName: eduPersonPrincipalName
                  name: urn:oid:1.3.6.1.4.1.5923.1.1.1.6
                  nameFormat: urn:oasis:names:tc:SAML:2.0:attrname-format:uri
        - stage: AUTHENTICATION
          required: false
          attributeGroup:
            - check: OPTIONAL
              attributes:
                - friendlyName: mail
                  name: urn:oid:0.9.2342.19200300.100.1.3
                  nameFormat: urn:oasis:names:tc:SAML:2.0:attrname-format:uri
                - friendlyName: displayName
                  name: urn:oid:2.16.840.1.113730.3.1.241
                  nameFormat: urn:oasis:names:tc:SAML:2.0:attrname-format:uri

```

#### Finalize the installation
Having saved the metadata in the configuration directory and pointed to it within the configuration perform another restart of the Tomcat/DME to make sure that the SAML configuration is complete and correct.

After a successful restart, click on Login and you should see the following screen - indicating that the login is supposed to be handled by the DFN-AAI, which does not know our new SP installation, however. Now, the generated SP metadata needs to be registered with the DFN as soon as the DFN refreshes its metadata including the newly provided SP, logins are possible (without having to restart the DME again).

![DFN-AAI EntityID unknown](https://github.com/tgradl/dme/raw/master/docs/img/screenshot-dfn-entityid-unknown.png "DFN-AAI EntityID unknown")

It is up to you/your organization to then push the instance to the Basic federation of the DFN-AAI.