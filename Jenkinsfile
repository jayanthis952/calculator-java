pipeline {
    agent any

    tools {
        maven 'M3' // Make sure 'M3' is configured under Jenkins Global Tool Configuration
    }

    environment {
        PROJECT_KEY = "java-calculator-k8s"
    }

    stages {
        stage('SCM') {
            steps {
                git branch: 'main', url: 'https://github.com/jayanthis952/calculator-java.git'
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
    }

    post {
        success {
            echo "Pipeline succeeded and Quality Gate passed ✅"
        }
        failure {
            echo "Pipeline failed or Quality Gate failed ❌"
        }
    }
}
