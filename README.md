# Data Modeling Environment (DME)

The Data Modeling Environment (DME) is a component of the DARIAH-DE Data Federation Architecture (DFA) software stack. The main intention of the DME is to enable domain experts in the arts and humanities to specify and correlate data structures in order to make data more accessible. The DME is a web-application and the frontend to the grammatical transformation framework, which will be published to GitHub in the near future. A particular focus lies on the explication of contextual knowledge in the form of rules to (1) define data in terms of domain specific languages and (2) provide transformative functions that operate of parsed instances of the data.

Issues for the DME are tracked here: https://minfba.de.dariah.eu/mantisbt/set_project.php?project_id=11

Further information on the concepts behind the DME are accessible at https://de.dariah.eu/dme.

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
