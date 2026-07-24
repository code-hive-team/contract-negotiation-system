pipeline {
    agent any

    tools {
        maven 'maven'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package'
            }
        }

        stage('Archive Artifact') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonarqube') {
                    sh 'mvn sonar:sonar'
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
    }

    post {

        success {
            echo "Sending success email..."

            emailext(
                subject: "SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
The build completed successfully.

Job Name : ${env.JOB_NAME}
Build Number : ${env.BUILD_NUMBER}
Build URL : ${env.BUILD_URL}

SonarQube Analysis completed successfully.
Quality Gate Passed.

Regards,
Jenkins CI/CD
""",
                to: "chandrika05k@gmail.com,srig4783@gmail.com,medidhilalitha@gmail.com,bunnynaidunarukula@gmail.com,raviteja1932005@gmail.com,munikumar3456@gmail.com"
            )
        }

        failure {
            echo "Sending failure email..."

            emailext(
                subject: "FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
The build has failed.

Job Name : ${env.JOB_NAME}
Build Number : ${env.BUILD_NUMBER}
Build URL : ${env.BUILD_URL}

Please check the Jenkins Console Output for more details.

Regards,
Jenkins CI/CD
""",
                to: "chandrika05k@gmail.com,srig4783@gmail.com,medidhilalitha@gmail.com,bunnynaidunarukula@gmail.com,raviteja1932005@gmail.com,munikumar3456@gmail.com"
            )
        }
    }
}
