pipeline {
    agent any

    tools {
        maven 'M3' // Ensure 'M3' is configured under Jenkins Global Tool Configuration
    }

    environment {
        PROJECT_KEY = "java-calculator-k8s"
        NEXUS_URL = 'http://98.87.13.40:30002'
        NEXUS_REPO_SNAPSHOT = 'maven-snapshots'
        NEXUS_REPO_RELEASE = 'maven-releases1'
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
                    // Read version from pom.xml
                    PROJECT_VERSION = sh(
                        script: "mvn help:evaluate -Dexpression=project.version -q -DforceStdout",
                        returnStdout: true
                    ).trim()
                    echo "Project Version: ${PROJECT_VERSION}"
                }
            }
        }

        stage('Sonar Analysis') {
            steps {
                withSonarQubeEnv('sonar-k8s') {
                    sh """
                        mvn clean verify sonar:sonar \
                        -Dsonar.projectKey=${PROJECT_KEY} \
                        -Dsonar.projectName=${PROJECT_KEY}
                    """
                }
            }
        }

        stage('Quality Gate Validate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Deploy to Nexus') {
            steps {
                script {
                    def isSnapshot = PROJECT_VERSION.endsWith("-SNAPSHOT")
                    def repoUrl = isSnapshot ? "${NEXUS_URL}/repository/${NEXUS_REPO_SNAPSHOT}/" : "${NEXUS_URL}/repository/${NEXUS_REPO_RELEASE}/"

                    echo "Deploying to: ${repoUrl}"

                    withCredentials([usernamePassword(credentialsId: 'nexus-creds', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASS')]) {
                        // Create temporary Maven settings with credentials
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

                        sh """
                            mvn deploy -s temp-settings.xml \
                            -DaltDeploymentRepository=nexus::default::${repoUrl}
                        """
                    }
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline succeeded ✅ Quality Gate passed and artifact deployed to Nexus."
        }
        failure {
            echo "Pipeline failed ❌ Check logs for errors."
        }
        cleanup {
            deleteDir() // clean workspace safely
        }
    }
}
