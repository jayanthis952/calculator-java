pipeline {
    agent any

    tools {
        jdk 'JDK17'       // Use Java 17 installation configured in Jenkins
        maven 'M3'        // Maven installation name in Jenkins Global Tool Config
    }

    environment {
        SONARQUBE = 'sonar-k8s'          // SonarQube server configured in Jenkins
        NEXUS_CREDENTIAL_ID = 'nexus-creds' // Nexus credentials ID in Jenkins
        PATH = "${tool 'JDK17'}/bin:${tool 'M3'}/bin:${env.PATH}" // Ensure correct PATH
    }

    stages {

        stage('Checkout SCM') {
            steps {
                git branch: 'main', url: 'https://github.com/jayanthis952/calculator-java.git'
            }
        }

        stage('Build & Unit Tests') {
            steps {
                sh 'mvn clean verify'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv(SONARQUBE) {
                    sh 'mvn sonar:sonar -Dsonar.projectKey=java-calculator-k8s -Dsonar.projectName=java-calculator-k8s'
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
                        VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
                        
                        # Determine repository based on version
                        if echo "$VERSION" | grep -q "SNAPSHOT"; then
                            REPO_URL="http://54.85.68.109:30002/repository/maven-snapshots/"
                        else
                            REPO_URL="http://54.85.68.109:30002/repository/maven-releases1/"
                        fi

                        echo "Deploying version $VERSION to $REPO_URL"

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
            echo "✅ Pipeline completed successfully!"
        }
        failure {
            echo "❌ Pipeline failed. Check the logs."
        }
        always {
            cleanWs()
        }
    }
}
