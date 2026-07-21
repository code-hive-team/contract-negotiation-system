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
                    sh '''
                        python3 -m pip install --user -r requirements.txt
                        python3 -m pip install --user -r requirements-dev.txt
                    '''
                }
            }
        }

        stage('Run Tests') {
            steps {
                dir('GenAI_Part') {
                    sh 'python3 -m pytest --cov=. --cov-report=xml || true'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                dir('GenAI_Part') {
                    script {
                        def scannerHome = tool 'sonarqube'

                        withSonarQubeEnv('sonarqube') {
                            sh """
                                ${scannerHome}/bin/sonar-scanner \
                                -Dsonar.projectKey=genai-project \
                                -Dsonar.projectName=genai-project \
                                -Dsonar.sources=. \
                                -Dsonar.python.version=3.9
                            """
                        }
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
