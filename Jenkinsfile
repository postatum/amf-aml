#!groovy

pipeline {
  agent {
    dockerfile true
  }
  environment {
    NEXUS = credentials('exchange-nexus')
  }
  stages {
    stage('Test') {
      steps {
        wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'XTerm']) {
          withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'sonarqube-official', passwordVariable: 'SONAR_SERVER_TOKEN', usernameVariable: 'SONAR_SERVER_URL']]) {
            sh 'sbt -mem 4096 -Dfile.encoding=UTF-8 clean coverage test coverageReport sonarMe'
          }
        }
      }
    }
    stage('Publish') {
      when {
        anyOf {
          branch 'master'
          branch 'develop'
          branch 'release/*'
        }
      }
      steps {
        wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'XTerm']) {
          sh 'sbt publish'
        }
      }
    }
    /* enable triggers when snapshots branch be created*/
    /*stage('Trigger amf projects') {

      when {
        branch 'develop'
      }
      steps {
        echo "Starting TCKutor Applications/AMF/amf/extract-core"
        build job: 'application/AMF/amf/extract-core', wait: false
      }
    }*/
  }
}