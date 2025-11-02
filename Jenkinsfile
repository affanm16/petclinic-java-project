pipeline {
    agent any

    options {
        skipDefaultCheckout()
        timestamps()
    }

    environment {
        APP_NAME = 'pet-clinic'
        APP_PORT = '8081'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw clean verify'
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    env.DEPLOY_IMAGE = "${APP_NAME}:build-${BUILD_NUMBER}"
                    sh "docker build -t ${DEPLOY_IMAGE} ."
                }
            }
        }

        stage('Deploy Container') {
            steps {
                sh 'docker rm -f $APP_NAME || true'
                sh 'docker run -d --name $APP_NAME -p $APP_PORT:$APP_PORT $DEPLOY_IMAGE'
            }
        }
    }

    post {
        always {
            sh 'docker ps --format "table {{.Names}}\t{{.Image}}\t{{.Status}}"'
            cleanWs()
        }
    }
}
