pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Install Dependencies') {
            steps {
                dir('GenAI_Part') {
                    sh 'python3 -m pip install -r requirements.txt'
                }
            }
        }

        stage('Run Tests') {
            steps {
                dir('GenAI_Part') {
                    sh 'pytest || true'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                dir('GenAI_Part') {
                    withSonarQubeEnv('sonarqube') {
                        sh 'sonar-scanner'
                    }
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
        always {
            emailext(
                subject: "${currentBuild.currentResult}: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
Job : ${env.JOB_NAME}
Branch : ${env.BRANCH_NAME}
Status : ${currentBuild.currentResult}

Build URL:
${env.BUILD_URL}
""",
                to: "chandrika05k@gmail.com,srig4783@gmail.com,medidhilalitha@gmail.com,bunnynaidunarukula@gmail.com,raviteja1932005@gmail.com,munikumar3456@gmail.com"
            )
        }
    }
}
