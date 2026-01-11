pipeline {
    agent any

    environment {
        // ===============================
        // AWS / ECR
        // ===============================
        AWS_REGION   = "eu-west-3"
        ECR_REPO_URI = "118320467932.dkr.ecr.eu-west-3.amazonaws.com/terraform-ecr"
        IMAGE_TAG    = "${BUILD_NUMBER}"
        EKS_CLUSTER_NAME = "terraform-eks-cluster"

        // ===============================
        // App URL (Ingress 생성 후 수정)
        // ===============================
        PUBLIC_BASE_URL = ""
    }

    stages {

        // ===============================
        // 1️⃣ Git Checkout
        // ===============================
        stage('Checkout') {
            steps {
                git branch: 'main',
                    credentialsId: 'git-credentials',
                    url: 'https://github.com/100jnghn/OrbitFlow.git'
            }
        }

        // ===============================
        // 2️⃣ Gradle Build
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
        // 3️⃣ Docker Build & Push (ECR)
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
        // 4️⃣ Infra Deploy (Redis / ChromaDB)
        // ===============================
        stage('Deploy Infra (Redis / Chroma)') {
            steps {
                withCredentials([
                    [$class: 'AmazonWebServicesCredentialsBinding',
                     credentialsId: 'aws-credentials']
                ]) {
                    sh '''
                      aws eks update-kubeconfig \
                        --region ${AWS_REGION} \
                        --name ${EKS_CLUSTER_NAME}

                      kubectl apply -f k8s/redis.yaml
                      kubectl apply -f k8s/chromadb.yaml
                    '''
                }
            }
        }

        // ===============================
        // 5️⃣ App Deploy
        // ===============================
        stage('Deploy App') {
            steps {
                withCredentials([
                    [$class: 'AmazonWebServicesCredentialsBinding',
                     credentialsId: 'aws-credentials']
                ]) {
                    sh '''
                      aws eks update-kubeconfig \
                        --region ${AWS_REGION} \
                        --name ${EKS_CLUSTER_NAME}

                      envsubst < k8s/app-deploy.yaml | kubectl apply -f -
                    '''
                }
            }
        }

        // ===============================
        // 6️⃣ Ingress Deploy (ALB 생성)
        // ===============================
        stage('Deploy Ingress') {
            steps {
                withCredentials([
                    [$class: 'AmazonWebServicesCredentialsBinding',
                     credentialsId: 'aws-credentials']
                ]) {
                    sh '''
                      aws eks update-kubeconfig \
                        --region ${AWS_REGION} \
                        --name ${EKS_CLUSTER_NAME}

                      kubectl apply -f k8s/ingress.yaml
                    '''
                }
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
