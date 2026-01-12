pipeline {
    agent any

    options {
        disableConcurrentBuilds()
    }

    environment {
        // ===============================
        // AWS / ECR
        // ===============================
        AWS_REGION        = "eu-west-3"
        ECR_REPO_URI      = "118320467932.dkr.ecr.eu-west-3.amazonaws.com/terraform-ecr"
        IMAGE_TAG         = "${BUILD_NUMBER}"
        EKS_CLUSTER_NAME  = "terraform-eks-cluster"

        // ===============================
        // App URL (Ingress 생성 후 수정)
        // ===============================
        PUBLIC_BASE_URL = ""
    }

    stages {

        // ===============================
        // 1️⃣ Gradle Build
        // ===============================
        stage('Build') {
            steps {
                sh '''
                  chmod +x gradlew
                  ./gradlew clean build -x test
                '''
            }
        }

        // ===============================
        // 2️⃣ Docker Build & Push (ECR)
        // ===============================
        stage('Docker Build & Push') {
            steps {
                withCredentials([
                    [$class: 'AmazonWebServicesCredentialsBinding',
                     credentialsId: 'aws-credentials']
                ]) {
                    sh '''
                      aws ecr get-login-password --region ${AWS_REGION} \
                        | docker login --username AWS --password-stdin ${ECR_REPO_URI}

                      docker build -t ${ECR_REPO_URI}:${IMAGE_TAG} .
                      docker push ${ECR_REPO_URI}:${IMAGE_TAG}
                    '''
                }
            }
        }

        // ===============================
        // 3️⃣ Update kubeconfig (공통)
        // ===============================
        stage('Configure kubectl') {
            steps {
                withCredentials([
                    [$class: 'AmazonWebServicesCredentialsBinding',
                     credentialsId: 'aws-credentials']
                ]) {
                    sh '''
                      aws eks update-kubeconfig \
                        --region ${AWS_REGION} \
                        --name ${EKS_CLUSTER_NAME}
                    '''
                }
            }
        }

        // ===============================
        // 4️⃣ Infra Deploy (Redis / ChromaDB)
        // ===============================
        stage('Deploy Infra') {
            steps {
                sh '''
                  kubectl apply -f k8s/infra/
                '''
            }
        }

        // ===============================
        // 5️⃣ App Deploy
        // ===============================
        stage('Deploy App') {
            steps {
                sh '''
                  envsubst < k8s/app/app-deploy.yaml | kubectl apply -f -
                '''
            }
        }

        // ===============================
        // 6️⃣ Ingress Deploy
        // ===============================
        stage('Deploy Ingress') {
            steps {
                sh '''
                  kubectl apply -f k8s/app/ingress.yaml
                '''
            }
        }

        // ===============================
        // 7️⃣ Monitoring Deploy (ServiceMonitor)
        // ===============================
        stage('Deploy Monitoring') {
            steps {
                sh '''
                  kubectl apply -f k8s/monitoring/
                '''
            }
        }
    }

    post {
        success {
            echo "🚀 OrbitFlow deploy success!"
        }
        failure {
            echo "❌ OrbitFlow deploy failed"
        }
    }
}
