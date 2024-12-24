// 필요한 변수를 선언할 수 있다. (내가 직접 선언하는 변수, 젠킨스 환경변수를 끌고 올 수 있음)
def ecrLoginHelper="docker-credential-ecr-login" // ECR credential helper 이름

// 젠킨스의 선언형 파이프라인 정의부 시작 (그루비 언어)
pipeline {
    agent any // 어느 젠킨스 서버에서도 실행이 가능
    environment {
        REGION = "ap-northeast-2"
        ECR_URL = "124355678220.dkr.ecr.ap-northeast-2.amazonaws.com"
        SERVICE_DIRS = "config-service,gateway-service,user-service,ordering-service,product-service"
        K8S_REPO_URL = "https://github.com/jh080724/orderservice-k8s.git"
        K8S_REPO_CRED = "github-k8s-repo-token"
    }
    stages {
        stage('Pull Codes from Github'){ // 스테이지 제목 (맘대로 써도 됨.)
            steps{
                checkout scm // 젠킨스와 연결된 소스 컨트롤 매니저(git 등)에서 코드를 가져오는 명령어
            }
        }
        //--------------------------------------------
        stage('Detect Changes') {
            steps {
                script {
                    // 변경된 파일 감지
                    def changedFiles = sh(script: "git diff --name-only HEAD~1 HEAD", returnStdout: true)
                        .trim()
                        .split('\n') // 변경된 파일을 줄 단위로 분리

                    // 변경된 파일 출력
                    // user-service/src/main/resources/application.yml
                    // user-service/src/main/java/com/plaudata/userservice/controller/userController.java
                    echo "Changed files: ${changedFiles}"

                    def changedServices = []    // 이 배열에 들어가는 애들만 빌드함.
                    def serviceDirs = env.SERVICE_DIRS.split(",")

                    serviceDirs.each { service ->
                        if (changedFiles.any { it.startsWith(service + "/") }) {
                            changedServices.add(service)
                        }
                    }

                    env.CHANGED_SERVICES = changedServices.join(",")
                    if (env.CHANGED_SERVICES == "") {
                        echo "No changes detected in service directories. Skipping build and deployment."
                        // 성공 상태로 파이프라인 종료
                        currentBuild.result = 'SUCCESS' // 성공으로 표시
                    }
                }
            }
        }
        //--------------------------------------------

//         stage('Build Codes by Gradle') {
//             steps {
//                 script {
//                     def serviceDirs = env.SERVICE_DIRS.split(",")
//                     serviceDirs.each { service ->
//                         sh """
//                         echo "Building ${service}"
//                         cd ${service}
//                         chmod +x ./gradlew
//                         ./gradlew clean build -x test
//                         ls -al ./build/libs
//                         cd ..
//                         """
//                     }
//                 }
//             }
//         }
        stage('Build Changed Services') {
        // 이 스테이지는 빌드되어야 할 서비스가 존재한다면 실행되는 스테이지.
        // 이전 스테이지에서 체팅한 CHANGED_SERVICES라는 환경변수가 비어있지 않아야만 실행.
            when {
                expression { env.CHANGED_SERVICES != "" } // 변경된 서비스가 있을 때만 실행
            }
            steps {
                script {
                    def changedServices = env.CHANGED_SERVICES.split(",")
                    changedServices.each { service ->
                        sh """
                        echo "Building ${service}..."
                        cd ${service}
                        chmod +x ./gradlew
                        ./gradlew clean build -x test
                        ls -al ./build/libs
                        cd ..
                        """
                    }
                }
            }
        }

        //-----------------------------------------------
//         stage('Build Docker Image & Push to AWS ECR') {
//             steps {
//                  script {
//                     // withAWS를 통해 리전과 계정의 access, secret 키를 가져옴.
//                     withAWS(region: "${REGION}", credentials: "aws-key") {
//                         def serviceDirs = env.SERVICE_DIRS.split(",")
//                         serviceDirs.each { service ->
//                             // AWS에 접속해서 ECR을 사용해야 하는데, 젠킨스에는 aws-cli를 설치하지 않았어요.
//                             // aws-cli 없이도 접속을 할 수 있게 도와주는 라이브러리 설치.
//                             // helper가 여러분들 대신 aws에 로그인을 진행. 그리고 그 인증 정보를 json으로 만들어서
//                             // docker에게 세팅해 줍니다. -> docker가 ECR에 push가 가능해짐.
//                             sh """
//                                 curl -O https://amazon-ecr-credential-helper-releases.s3.us-east-2.amazonaws.com/0.4.0/linux-amd64/${ecrLoginHelper}
//                                 chmod +x ${ecrLoginHelper}
//                                 mv ${ecrLoginHelper} /usr/local/bin/
//
//                                 echo '{"credHelpers": {"${ECR_URL}": "ecr-login"}}' > ~/.docker/config.json
//
//                                 # Docker 이미지 빌드(서비스 이름으로)
//                                 docker build -t ${service}:latest ${service}
//
//                                 # ECR 레포지토리로 태깅
//                                 docker tag ${service}:latest ${ECR_URL}/${service}:latest
//
//                                 # ECR 로 푸시
//                                 docker push ${ECR_URL}/${service}:latest
//                             """
//                         }
//                     }
//                 }
//             }
//         }
        stage('Build Docker Image & Push to AWS ECR') {
           when {
               expression { env.CHANGED_SERVICES != "" } // 변경된 서비스가 있을 때만 실행
           }
            steps {
                script {
                    withAWS(region: "${REGION}", credentials: "aws-key") {
                        def changedServices = env.CHANGED_SERVICES.split(",")
                        changedServices.each { service ->
                            // 여기서 원하는 버전을 정하거나, 커밋 태그를 붙여보자.
                            def newTag = "v1.0.1"
                            sh """
                            curl -O https://amazon-ecr-credential-helper-releases.s3.us-east-2.amazonaws.com/0.4.0/linux-amd64/${ecrLoginHelper}
                            chmod +x ${ecrLoginHelper}
                            mv ${ecrLoginHelper} /usr/local/bin/

                            echo '{"credHelpers": {"${ECR_URL}": "ecr-login"}}' > ~/.docker/config.json

                            docker build -t ${service}:${newTag} ${service}
                            docker tag ${service}:${newTag} ${ECR_URL}/${service}:${newTag}
                            docker push ${ECR_URL}/${service}:${newTag}
                            """
                        }
                    }
                }
            }
        }
        //-------------------------------------------------------------------------

        stage('Update k8s Repo') {
            when {
                expression { env.CHANGED_SERVICES != "" } // 변경된 서비스가 있을 때만 실행
            }

            steps {
                script {
                    // 1. k8s 레포지토리를 클론하자.
                    // git 스텝: 지정된 브랜치, 자격 증명, url을 사용하여 클론할 수 있게 해주는 문법.
                    git branch: 'main',
                    credentialId: "${K8S_REPO_CRED}",
                    url: "${K8S_REPO_URL}"

                    def changedServices = env.CHANGED_SERVICES.split(",")
                    changedServices.each { service ->
                        def newTag = "1.0.1" // 이미지 빌드할 때 사용한 태그를 동일하게 사용.

                        // umbrella-chart/charts/<service>/values.yaml 파일 내의 image 태그 교체.
                        // sed: 스트림 편집기(stream editor), 텍스트 파일을 수정하는 데 사용.
                        // s#^ -> 라인의 시작을 의미. image: -> 텍스트 image:을 찾아라, .* -> image: 다음에 오는 모든 문자
                        // 새로운 태그를 붙인 ecr 경로로 수정을 진행해라
                        sh """
                            echo "Updating ${service} image tag in k8s repo..."
                            sed -i 's#^image: .*#image: ${ECR_URL}/${service}:${newTag}#' umbrella-chart/charts/${service}/values.yaml

                        """
                    }

                    // 변경사항 commit & push
                    sh """
                        git config user.name "stephen Lee"
                        git config user.email "stephen4951@gmail.com"
                        git add .
                        git commit -m "Update images for changed services"
                        git push origin main
                    """

                }
            }
        }

        //----------------------------------------------------------------
    }
}