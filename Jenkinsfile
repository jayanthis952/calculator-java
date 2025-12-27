pipeline {
    agent any

    tools {
        maven 'M3' // Make sure 'M3' is configured in Jenkins Global Tool Configuration
    }

    environment {
        PROJECT_KEY = "java-calculator-k8s"
        NEXUS_URL = 'http://34.239.186.88:30002'
        NEXUS_REPO_SNAPSHOT = 'maven-snapshots'
        NEXUS_REPO_RELEASE = 'maven-releases'
        PROJECT_VERSION = ''
    }

    stages {
        stage('Checkout SCM') {
            steps {
                git branch: 'main', url: 'https://github.com/jayanthis952/calculator-java.git'
            }
        }

        stage('Set Project Version') {
            steps {
                script {
                    def version = sh(
                        script: "mvn help:evaluate -Dexpression=project.version -q -DforceStdout",
                        returnStdout: true
                    ).trim()
                    env.PROJECT_VERSION = version
                    echo "Project Version: ${env.PROJECT_VERSION}"
                }
            }
        }

        stage('Build & Test') {
            steps {
                sh "mvn clean verify"
            }
        }

        stage('JaCoCo Coverage') {
            steps {
                sh "mvn jacoco:report"
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonar-k8s') {
                    sh "mvn sonar:sonar -Dsonar.projectKey=${PROJECT_KEY} -Dsonar.projectName=${PROJECT_KEY} -Dsonar.login=${SONAR_TOKEN}"
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Deploy to Nexus') {
            steps {
                script {
                    def isSnapshot = env.PROJECT_VERSION.endsWith("-SNAPSHOT")
                    def repoUrl = isSnapshot ? "${NEXUS_URL}/repository/${NEXUS_REPO_SNAPSHOT}/" : "${NEXUS_URL}/repository/${NEXUS_REPO_RELEASE}/"

                    echo "Deploying ${env.PROJECT_VERSION} to: ${repoUrl}"

                    withCredentials([usernamePassword(credentialsId: 'nexus-creds', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASS')]) {
                        writeFile file: 'temp-settings.xml', text: """
<settings>
  <servers>
    <server>
      <id>nexus</id>
      <username>\${env.NEXUS_USER}</username>
      <password>\${env.NEXUS_PASS}</password>
    </server>
  </servers>
</settings>
                        """
                        sh "mvn deploy -s temp-settings.xml -DaltDeploymentRepository=nexus::default::${repoUrl}"
                    }
                }
            }
        }
    }

    post {
        success {
            echo "✅ Pipeline succeeded: Quality Gate passed and artifact deployed to Nexus."
        }
        failure {
            echo "❌ Pipeline failed: Check logs for errors."
        }
        always {
            archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
            junit '**/target/surefire-reports/*.xml'
        }
        cleanup {
            deleteDir()
        }
    }
}
