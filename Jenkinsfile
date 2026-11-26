pipeline {
    agent any

    tools {
        jdk   'JAVA_HOME'
        maven 'M2_HOME'
    }

    triggers {
        githubPush()
    }

    environment {
        EMAIL_DEST   = 'sarra.benothmen@esprit.tn'
        DOCKER_IMAGE = 'sarrabenothmen/backend-gestion-projets'
        DOCKER_TAG   = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout GitHub') {
            steps {
                checkout scm
            }
        }

        stage('Tests unitaires') {
            steps {
                dir('backend') {
                    sh 'mvn clean test'
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'backend/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Livrable (target)') {
            steps {
                dir('backend') {
                    sh 'mvn package -DskipTests'
                    sh 'ls -l target/*.jar'
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: 'backend/target/*.jar', fingerprint: true
                }
            }
        }

        stage('Build image Docker') {
            steps {
                dir('backend') {
                    sh 'docker build -t $DOCKER_IMAGE:$DOCKER_TAG -t $DOCKER_IMAGE:latest .'
                    sh 'docker images | grep backend-gestion-projets'
                }
            }
        }

        stage('Push image Docker Hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                                                  usernameVariable: 'DOCKER_USER',
                                                  passwordVariable: 'DOCKER_PASS')]) {
                    sh 'echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin'
                    sh 'docker push $DOCKER_IMAGE:$DOCKER_TAG'
                    sh 'docker push $DOCKER_IMAGE:latest'
                }
            }
            post {
                always {
                    sh 'docker logout'
                }
            }
        }
    }

    post {
        failure {
            emailext(
                to: "${EMAIL_DEST}",
                subject: "ECHEC du build ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """<p>Le build <b>${env.JOB_NAME} #${env.BUILD_NUMBER}</b> a echoue.</p>
                         <p>Consulter les logs : <a href="${env.BUILD_URL}console">${env.BUILD_URL}console</a></p>""",
                mimeType: 'text/html'
            )
        }
        success {
            echo 'Build reussi : image Docker poussee sur Docker Hub'
        }
    }
}