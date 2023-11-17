pipeline {
  options {
    buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '20'))
    skipDefaultCheckout true
  }
  environment {
    REGISTRY_HOSTNAME = "harbor.ioannisgk.com"
    REGISTRY_REPOSITORY = "harbor.ioannisgk.com/myproject/spring-boot-webapp"
    NEW_IMAGE_TAG = "v0.0${BUILD_NUMBER}"
    HARBOR_CREDS = credentials('harbor-credentials')
    COSIGN_KEY = credentials('cosign-key')
    COSIGN_PUBLIC = credentials('cosign-public')
    GIT_EMAIL = "automationbot@example.com"
    GIT_USERNAME = "automationbot"
    GIT_REPOSITORY = "gitlab.ioannisgk.com/automationbot/kubernetes-infrastructure.git"
    GIT_BRANCH = "main"
    GIT_CREDS = credentials('gitlab-credentials')
    DEPLOYMENT_FILE_PATH = "development/spring-boot-webapp/spring-app-deployment.yaml"
  }
  agent {
    kubernetes {
      yaml '''
        apiVersion: v1
        kind: Pod
        spec:
          containers:
          - name: maven
            image: maven:3.9.5-eclipse-temurin-17-alpine
            command:
            - cat
            tty: true
            resources:
              requests:
                memory: "128Mi"
                cpu: "250m"
              limits:
                memory: "512Mi"
                cpu: "500m"
          - name: kaniko
            image: gcr.io/kaniko-project/executor:debug
            imagePullPolicy: Always
            command:
            - cat
            tty: true
            resources:
              requests:
                memory: "128Mi"
                cpu: "250m"
              limits:
                memory: "512Mi"
                cpu: "500m"
            volumeMounts:
              - name: jenkins-docker-cfg
                mountPath: /kaniko/.docker
          - name: cosign
            image: alpine:3.18.4
            command: ["/bin/sh"]
            args: ["-c", "apk update; wget -O /usr/local/bin/cosign https://github.com/sigstore/cosign/releases/download/v2.2.1/cosign-linux-amd64; chmod +x /usr/local/bin/cosign; cat;"]
            tty: true
            resources:
              requests:
                memory: "64Mi"
                cpu: "100m"
              limits:
                memory: "128Mi"
                cpu: "150m"
          - name: git
            image: alpine/git:2.40.1
            command:
            - cat
            tty: true
            resources:
              requests:
                memory: "64Mi"
                cpu: "100m"
              limits:
                memory: "128Mi"
                cpu: "150m"
          volumes:
          - name: jenkins-docker-cfg
            projected:
              sources:
              - secret:
                  name: harbor-credentials
                  items:
                    - key: .dockerconfigjson
                      path: config.json
        '''
    }
  }
  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }
    stage('Build Application') {
      steps {
        container('maven') {
          sh '''
            mvn -v
            mvn clean install
          '''
        }
      }
    }
    stage('Kaniko Build & Push Image') {
      steps {
        container('kaniko') {
          sh '''
            ls -last
            /kaniko/executor --context . --destination ${REGISTRY_REPOSITORY}:${NEW_IMAGE_TAG} 2>&1 | tee outfile.txt
          '''
        }
      }
    }

    stage('Sign Image & Push Signature') {
      steps {
        container('cosign') {
          sh '''
            IMAGE_URI_LATEST_DIGEST=$(tail -n 1 outfile.txt | awk \'{ print $NF }\')
            cosign -version
            cosign login ${REGISTRY_HOSTNAME} -u ${HARBOR_CREDS_USR} -p ${HARBOR_CREDS_PSW}

            # Sign the image and push the signature to the registry
            export COSIGN_PASSWORD="" && cosign sign --key \"${COSIGN_KEY}\" $IMAGE_URI_LATEST_DIGEST --yes

            # Validate and verify the signature of the image
            cosign verify --key \"${COSIGN_PUBLIC}\" $IMAGE_URI_LATEST_DIGEST
          ''' 
        }
      }
    } 
  }
}
