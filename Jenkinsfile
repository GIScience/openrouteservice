pipeline {
  agent any
    
  environment {
    GIT_COMMITTER_NAME = "jenkins"
    GIT_COMMITTER_EMAIL = "support@openrouteservice.org"
    mvnHome = tool 'mvn-3.5'
  }
        
  stages {
    stage("Preparation") {
      when {
        branch 'development'
      }
      steps {
        deleteDir()
        git branch: 'development', url: 'https://github.com/GIScience/openrouteservice.git'
      }
    }
    stage("Build") {
      when {
        branch 'development'
      }
      steps {
        sh "cp ${WORKSPACE}/openrouteservice-api-tests/conf/app.config.test ${WORKSPACE}/openrouteservice/WebContent/WEB-INF/app.config"
        sh "'${mvnHome}/bin/mvn' -f ${WORKSPACE}/openrouteservice/pom.xml install -B"
        archiveArtifacts artifacts: '**/*.war', fingerprint: true
      }
    }
    stage("Test") {
      when {
        branch 'development'
      }
      steps {
        sh "nohup '${mvnHome}/bin/mvn' -f ${WORKSPACE}/openrouteservice/pom.xml tomcat7:run &"
        sh "sleep 5m"
        sh "'${mvnHome}/bin/mvn' -f ${WORKSPACE}/openrouteservice-api-tests/pom.xml test"
      }
    }     
  }
  post {
    always {
      deleteDir()
    }
    success {
      rocketSend channel: 'ors-jenkins', message: "SUCCESS: ${currentBuild.fullDisplayName}", rawMessage: true 
    }
    failure {
      rocketSend channel: 'ors-jenkins', message: "FAILURE: ${currentBuild.fullDisplayName}", rawMessage: true 
    }
  }
}
