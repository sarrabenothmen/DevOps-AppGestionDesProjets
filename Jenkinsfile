pipeline {
    agent any

    tools {
        // Noms definis dans Administrer Jenkins > Tools (Atelier 3)
        jdk   'JAVA_HOME'
        maven 'M2_HOME'
    }

    triggers {
        // Declenche le build a chaque push recu via le webhook GitHub
        githubPush()
    }

    environment {
        EMAIL_DEST = 'sarra.benothmen@esprit.tn'   // <-- a remplacer
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
            echo 'Build reussi : livrable genere dans backend/target/'
        }
    }
}
