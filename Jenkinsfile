pipeline {
  agent  label:'openrouteservice'
  
  when {
    branch 'development'
  }
     
  environment {
    GIT_COMMITTER_NAME = "jenkins"
    GIT_COMMITTER_EMAIL = "support@openrouteservice.org"
  }
     
  def mvnHome
     
  stages {
    stage("Preparation") {
      steps {
        deleteDir()
        git branch: 'development', url: 'https://github.com/GIScience/openrouteservice.git'
        mvnHome = tool 'mvn-3.5'
        //sh 'mvn clean install -Dmaven.test.failure.ignore=true'
      }
    }
    stage("Build") {
      steps {
        sh "cp ${WORKSPACE}/openrouteservice-api-tests/conf/app.config.test ${WORKSPACE}/openrouteservice/WebContent/WEB-INF/app.config"
        sh "'${mvnHome}/bin/mvn' -f ${WORKSPACE}/openrouteservice/pom.xml install -B"
        archiveArtifacts artifacts: '**/*.war', fingerprint: true
        //sh 'mvn clean install -Dmaven.test.failure.ignore=true'
      }
    }
    stage("Test") {
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
