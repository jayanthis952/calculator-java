pipeline {
    agent any

    tools {
        maven 'M3'        // Maven installation in Jenkins (Global Tool Configuration)
           // Ensure Java 11 is installed in Jenkins
    }

    environment {
        PROJECT_KEY = "java-calculator-k8s"
        NEXUS_URL   = "http://98.87.13.40:30002/repository/maven-releases1/"
        SONAR_SERVER = "sonar-k8s"
    }

    stages {
        // ----------------- Checkout Code -----------------
        stage('Checkout SCM') {
            steps {
                git branch: 'main', url: 'https://github.com/jayanthis952/calculator-java.git'
            }
        }

        // ----------------- SonarQube Analysis -----------------
        stage('Sonar Analysis') {
            steps {
                withSonarQubeEnv("${SONAR_SERVER}") {
                    sh """
                        mvn clean verify sonar:sonar \
                        -Dsonar.projectKey=${PROJECT_KEY} \
                        -Dsonar.projectName=${PROJECT_KEY}
                    """
                }
            }
        }

        // ----------------- SonarQube Quality Gate -----------------
        stage('Quality Gate Validate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        // ----------------- Deploy to Nexus -----------------
        stage('Deploy to Nexus') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'nexus-creds', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASS')]) {
                    sh """
                        mvn deploy \
                        -DaltDeploymentRepository=nexus::default::${NEXUS_URL} \
                        -Dusername=$NEXUS_USER \
                        -Dpassword=$NEXUS_PASS
                    """
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
