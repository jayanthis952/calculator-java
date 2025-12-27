pipeline {
    agent any

    tools {
        maven 'M3'
        jdk 'jdk11'
    }

    environment {
        PROJECT_VERSION = ''
    }

    stages {

        stage('Checkout SCM') {
            steps {
                checkout scm
            }
        }

        stage('Set Project Version') {
            steps {
                script {
                    env.PROJECT_VERSION = sh(
                        script: "mvn help:evaluate -Dexpression=project.version -q -DforceStdout",
                        returnStdout: true
                    ).trim()
                    echo "Project Version: ${env.PROJECT_VERSION}"
                }
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
            steps {
                withSonarQubeEnv('sonar-k8s') {
                    sh '''
                      mvn sonar:sonar \
                      -Dsonar.projectKey=java-calculator-k8s \
                      -Dsonar.projectName=java-calculator-k8s
                    '''
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
            when {
                branch 'main'
            }
            steps {
                sh 'mvn deploy'
            }
        }
    }

    post {
        always {
            junit '**/target/surefire-reports/*.xml'
            archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
            cleanWs()
        }

        success {
            echo '✅ Pipeline succeeded!'
        }

        failure {
            echo '❌ Pipeline failed: Check logs for errors.'
        }
    }
}
