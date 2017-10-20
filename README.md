# Data Modeling Environment (DME)

The Data Modeling Environment (DME) is a component of the DARIAH-DE Data Federation Architecture (DFA) software stack. The main intention of the DME is to enable domain experts in the arts and humanities to specify and correlate data structures in order to make data more accessible. The DME is a web-application and the frontend to the grammatical transformation framework, which will be published to GitHub in the near future. A particular focus lies on the explication of contextual knowledge in the form of rules to (1) define data in terms of domain specific languages and (2) provide transformative functions that operate of parsed instances of the data.

Issues for the DME are tracked here: https://minfba.de.dariah.eu/mantisbt/set_project.php?project_id=11

Further information on the concepts behind the DME are accessible at https://de.dariah.eu/dme.

## 1) Prerequisites

The installation of an instance of the DME requires the setup of some required components:
* An installed Java, minimum version JavaSE-1.8
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

#### Install in Tomcat

The package installs the DME at */var/dfa/webapps/dme*. Make sure to register this path with your web application server. When using Tomcat, a link to the installation directory from */path/to/tomcat/webapps* can be placed. For a default Ubuntu installation:
```
apt-get update && apt-get install dme
```
 




Once installed and successfully started as (within a Java web application server) an empty CR dashboard is presented - with default ports and installed on a local machine as http://localhost:8080

### 2.2) WAR Container

WAR container files of the DME can be found at:
* Production releases: https://minfba.de.dariah.eu/artifactory/webapp/#/artifacts/browse/tree/General/dariah-minfba-releases/de/unibamberg/minf/dme
* Snapshot releases: https://minfba.de.dariah.eu/artifactory/webapp/#/artifacts/browse/tree/General/dariah-minfba-snapshots/de/unibamberg/minf/dme

Place the WAR container in the applications directory of your web server - e.g. */path/to/tomcat/webapps* for Tomcat.  