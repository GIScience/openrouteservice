node {
     
   when {
         branch 'development'
   }
     
   def mvnHome
   stage('Preparation') { // for display purposes
      
      rocketSend channel: 'ors-jenkins', message: 'Preparing build...', rawMessage: true
        
      deleteDir()
      git branch: 'development', url: 'https://github.com/GIScience/openrouteservice.git'
      // Get the Maven tool.
      // NOTE: This 'M3' Maven tool must be configured
      //        in the global configuration.           
      mvnHome = tool 'mvn-3.5'
    }
    
   stage('build-ors') {
      
      rocketSend channel: 'ors-jenkins', message: 'Starting to build ORS development...', rawMessage: true 

      sh "cp ${WORKSPACE}/openrouteservice-api-tests/conf/app.config.test ${WORKSPACE}/openrouteservice/WebContent/WEB-INF/app.config"
      sh "'${mvnHome}/bin/mvn' -f ${WORKSPACE}/openrouteservice/pom.xml install -B"
      archiveArtifacts artifacts: '**/*.war', fingerprint: true

      rocketSend channel: 'ors-jenkins', message: 'Built ORS development...', rawMessage: true
   
   }
    
   stage('test-ors') {
        
      rocketSend channel: 'ors-jenkins', message: 'Starting to test ORS...', rawMessage: true 

      sh "nohup '${mvnHome}/bin/mvn' -f ${WORKSPACE}/openrouteservice/pom.xml tomcat7:run &"
      sh "sleep 5m"
      sh "'${mvnHome}/bin/mvn' -f ${WORKSPACE}/openrouteservice-api-tests/pom.xml test"

      rocketSend channel: 'ors-jenkins', message: 'Tests passed!', rawMessage: true 
       
    }
    
    stage('Clean') {
       
      rocketSend channel: 'ors-jenkins', message: 'Cleaning directory...', rawMessage: true
      deleteDir()
       
    }
    
}
