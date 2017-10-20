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

### 3.2) SAML Integration
