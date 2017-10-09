node {
  def mvnHome
  
  stage('Preparation') {
    git 'https://github.com/tgradl/dme.git'
    mvnHome = tool 'Maven 3.0.4'
  }

  stage('Build') {
    sh "'${mvnHome}/bin/mvn' -U -Pdariah.deb -Dmaven.test.failure.ignore clean package"
  }

  stage('Publish') { 
    def pom = readMavenPom file: 'pom.xml'
    def uiVersion = pom.version
    def release = uiVersion.contains("RELEASE")
    def snapshot = uiVersion.contains("SNAPSHOT")

    if (snapshot || release) {
        echo "publishing deb package dme for " + (snapshot ? "SNAPSHOT" : "RELEASE") + " version ${uiVersion}"
		
        sh "PLOC=\$(ls dme/target/*.deb); curl -X POST -F file=@\${PLOC} http://localhost:8008/api/files/dme-${uiVersion}"
        sh "curl -X POST http://localhost:8008/api/repos/" + (snapshot ? "snapshots" : "releases") + "/file/dme-${uiVersion}"
        sh "curl -X PUT -H 'Content-Type: application/json' --data '{}' http://localhost:8008/api/publish/:./trusty"
        sh "rm dme/target/*.deb"
        sh "rm dme/target/*.changes"

    }
    else {
        echo "deb package dme for version ${uiVersion} will not be published"
    }
  }
}