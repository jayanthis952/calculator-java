pipeline {
    agent any

    environment {
        SONAR_TOKEN = credentials('sonar-token') // Jenkins credential ID
        NEXUS_USER = credentials('nexus-user')    // Jenkins credential ID
        NEXUS_PASS = credentials('nexus-pass')    // Jenkins credential ID
    }

    tools {
        maven 'Maven-3.9.3'  // Your Maven installation name in Jenkins
        jdk 'JDK11'           // Your JDK installation name in Jenkins
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/jayanthis952/calculator-java.git'
            }
        }

        stage('Build & Test') {
            steps {
                sh 'mvn clean verify'
            }
        }

        stage('JaCoCo Coverage') {
            steps {
                sh 'mvn jacoco:report'
            }
        }

        stage('SonarQube Analysis') {
            environment {
                SONAR_HOST_URL = 'http://your-sonarqube-server:9000'
            }
            steps {
                withSonarQubeEnv('sonar-k8s') {
                    sh "mvn sonar:sonar -Dsonar.projectKey=java-calculator-k8s -Dsonar.login=${SONAR_TOKEN}"
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Deploy to Nexus') {
            steps {
                sh """
                mvn deploy -Dnexus.username=${NEXUS_USER} -Dnexus.password=${NEXUS_PASS}
                """
            }
        }
    }

    post {
        always {
            junit '**/target/surefire-reports/*.xml'
            archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
        }
        success {
            echo "✅ Pipeline succeeded!"
        }
        failure {
            echo "❌ Pipeline failed. Check logs."
        }
    }
}
