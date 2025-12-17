pipeline {
    agent any

    environment {
        MVN_HOME = tool name: 'Maven 3', type: 'maven' // Change to your Maven tool name in Jenkins
        NEXUS_URL = 'http://54.85.68.109:30002'
        NEXUS_REPO_SNAPSHOT = 'maven-snapshots'
        NEXUS_REPO_RELEASE = 'maven-releases'
        PROJECT_VERSION = ''
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Set Project Version') {
            steps {
                script {
                    // Read version from pom.xml
                    PROJECT_VERSION = sh(returnStdout: true, script: "mvn help:evaluate -Dexpression=project.version -q -DforceStdout").trim()
                    echo "Project Version: ${PROJECT_VERSION}"
                }
            }
        }

        stage('Build & Test') {
            steps {
                sh "${MVN_HOME}/bin/mvn clean verify"
            }
        }

        stage('Generate Coverage Report') {
            steps {
                sh "${MVN_HOME}/bin/mvn jacoco:report"
            }
        }

        stage('Deploy to Nexus') {
            steps {
                script {
                    // Determine if snapshot or release
                    def isSnapshot = PROJECT_VERSION.endsWith("-SNAPSHOT")
                    def repoId = isSnapshot ? 'nexus-snapshots' : 'nexus-releases'
                    def repoUrl = isSnapshot ? "${NEXUS_URL}/repository/${NEXUS_REPO_SNAPSHOT}/" : "${NEXUS_URL}/repository/${NEXUS_REPO_RELEASE}/"

                    echo "Deploying to ${repoUrl}"

                    // Use Jenkins credentials for Nexus (username/password)
                    withCredentials([usernamePassword(credentialsId: 'nexus-credentials', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASS')]) {
                        // Create temporary Maven settings.xml with credentials
                        writeFile file: 'temp-settings.xml', text: """
<settings>
  <servers>
    <server>
      <id>${repoId}</id>
      <username>\${env.NEXUS_USER}</username>
      <password>\${env.NEXUS_PASS}</password>
    </server>
  </servers>
</settings>
                        """

                        sh "${MVN_HOME}/bin/mvn deploy -s temp-settings.xml"
                    }
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
            junit '**/target/surefire-reports/*.xml'
        }
        cleanup {
            deleteDir() // clean workspace
        }
    }
}
