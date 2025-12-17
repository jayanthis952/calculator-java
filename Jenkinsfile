pipeline {
    agent any

    tools {
        maven 'M3' // Make sure 'M3' is configured under Jenkins Global Tool Configuration
    }

    environment {
        PROJECT_KEY = "java-calculator-k8s"
        // Nexus credential ID
        NEXUS_CREDENTIAL_ID = 'nexus-creds'
    }

    stages {
        stage('SCM Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/jayanthis952/calculator-java.git'
            }
        }

        stage('SonarQube Analysis') {
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

        stage('Quality Gate Check') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Deploy to Nexus') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: "${NEXUS_CREDENTIAL_ID}", 
                    usernameVariable: 'NEXUS_USER', 
                    passwordVariable: 'NEXUS_PASS'
                )]) {
                    sh '''
                        # Get project version from pom.xml
                        VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

                        # Determine repository URL based on version
                        if [[ "$VERSION" == *-SNAPSHOT ]]; then
                            REPO_URL="http://54.85.68.109:30002/repository/maven-snapshots/"
                        else
                            REPO_URL="http://54.85.68.109:30002/repository/maven-releases1/"
                        fi

                        echo "Deploying version $VERSION to $REPO_URL"

                        # Deploy to Nexus
                        mvn deploy -DaltDeploymentRepository=nexus::default::$REPO_URL \
                                   -Dnexus.username=$NEXUS_USER \
                                   -Dnexus.password=$NEXUS_PASS
                    '''
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline succeeded ✅ Quality Gate passed and artifact deployed to Nexus"
        }
        failure {
            echo "Pipeline failed ❌ Please check the logs"
        }
    }
}
