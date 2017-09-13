//properties([pipelineTriggers([githubPush()])])

node {
   def mvnHome
   stage('Preparation') { // for display purposes
        deleteDir()
        git branch: 'development', url: 'https://github.com/GIScience/openrouteservice.git'
        //sh(script: 'git clone https://lh131:PvAWPUy1740Q@gitlab.gistools.geog.uni-heidelberg.de/giscience/openrouteservice/core.git')
        // Get the Maven tool.
        // NOTE: This 'M3' Maven tool must be configured
        //        in the global configuration.           
        mvnHome = tool 'mvn-3.5'
    }
    
    stage('build-ors') {
        sh "cp ${WORKSPACE}/openrouteservice-api-tests/conf/app.config.test ${WORKSPACE}/openrouteservice/WebContent/WEB-INF/app.config"
        sh "'${mvnHome}/bin/mvn' -f ${WORKSPACE}/openrouteservice/pom.xml install -B"
        archive "${WORKSPACE}/openrouteservice/target/*.war" 
    }
    
    stage('test-ors') {
        sh "nohup '${mvnHome}/bin/mvn' -f ${WORKSPACE}/openrouteservice/pom.xml tomcat7:run &"
        sh "sleep 5m"
        sh "'${mvnHome}/bin/mvn' -f ${WORKSPACE}/openrouteservice-api-tests/pom.xml test"
    }
    
    stage('Clean') {
        deleteDir()
    }
    
}
