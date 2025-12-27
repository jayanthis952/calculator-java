pipeline{
    agent any
    environment{
        PROJECT_KEY="java-calculator-k8s"
    }
    stages{
        stage('SCM'){
            steps{
                git branch: 'main', url: 'https://github.com/jayanthis952/calculator-java.git'
            }
        }
        stage('sonar analysis'){
            steps{
                withSonarQubeEnv('sonar-k8s'){
                    sh """ mvn clean verify sonar:sonar \
                    -Dsonar.projectKey=${PROJECT_KEY} \
                    -Dsonar.projectName=${PROJECT_KEY}
                    """
                }
            }
        }
        stage('Quality gate validate'){
            steps{
                timeout(time: 5, unit: 'MINUTES'){
                    waitForQualityGate abortPipeline: true
                }
            }
                  post{
                    success{
                           echo "Quality Gate Passed"
                        }
                   failure{
                           echo "Quality Gate Failed"
                     }
                }
            }
        stage('Build'){
            steps{
                sh 'mvn clean install'
            }
        }
        stage('Nexus-artifactory'){
            steps{
                nexusArtifactUploader artifacts: [[artifactId: 'calculator-java', classifier: '', file: 'target/calculator-java-1.0-SNAPSHOT.jar', type: 'jar']],
                    credentialsId: 'nexus-cred', groupId: 'com.example', nexusUrl: '34.239.186.88:30002', nexusVersion: 'nexus3', protocol: 
                    'http', repository: 'maven-snapshots', version: '1.0-SNAPSHOT'
            }
        }
        }
    }
  
