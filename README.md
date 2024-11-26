# Kyobo-CDA1-9900
## 🎯 프로젝트 개요
교보 DTS CDA 1기 과정에서 진행한 **AWS를 활용한 식당 예약 플랫폼 인프라 구축** 프로젝트입니다.
* 이 프로젝트는 특정 시즌의 트래픽 폭증과 시스템 안정성 문제를 해결하고자 시작되었습니다.
* 사용자에게 쉽고 직관적인 예약 경험을 제공하며, 글로벌 확장을 고려한 안정적인 시스템 설계가 목표입니다.

## 🎯 프로젝트 목표
### AWS 기반 안정적인 예약 시스템 구축
* 안정성과 확장성 확보: AWS 클라우드 인프라를 활용하여 안정적이고 확장 가능한 식당 예약 플랫폼을 구축.
* 효율적인 트래픽 관리: 트래픽 급증 상황에서도 자동 스케일링과 로드 밸런싱을 통해 서비스 성능 유지.

### 사용자 중심 서비스 제공 
* 편리한 예약 경험: 사용자에게 직관적인 인터페이스와 실시간 예약 기능을 제공.
* 신뢰성 있는 시스템 운영: 중복 예약 및 오버부킹 방지로 고객 신뢰도 강화.


### 프로젝트 배경 및 문제 해결
### 문제 정의 및 해결 방안
### 문제 정의

**1. 트래픽 집중에 따른 서비스 불안정**
* 특정 시즌(연말연시, 명절, 크리스마스 등) 또는 소셜미디어 트렌드로 인해 인기 있는 식당 예약 요청이 급증하여 서버 부하 및 시스템 중단이 발생.
* 예약 실패로 인해 고객 불만이 증가.

**2. 중복 예약 및 오버부킹 문제**

* 실시간 데이터 동기화 부족으로 인해 이중 예약 또는 오버부킹 발생.
* 고객에게 예약이 완료되었다는 알림이 발송되었지만 실제로는 자리가 없는 상황이 초래되어 서비스 신뢰도 저하.

**3. 예약 서비스 이용률 증가**
- 최근 O2O 서비스와 예약 플랫폼의 사용률이 폭발적으로 증가.
- 안정적인 데이터 관리와 트래픽 응답성이 필수.

### 프로젝트 목표와 해결 방안

#### 주요 해결 방안
- AWS 기반의 **자동 스케일링 인프라 구축**: 예약 요청 폭증 시 자원을 유연하게 확장하여 안정적인 서비스 제공.
- **실시간 데이터 동기화**: 중복 예약과 오버부킹 문제를 원천 차단.
- **글로벌 확장성**: 국내 사용자뿐 아니라 국제 사용자 기반 확장을 고려한 설계.

### 기대 효과
#### 안정적인 예약 서비스:
* 트래픽 관리 및 고가용성 확보로 서비스 중단 없이 사용자에게 원활한 예약 경험 제공.
#### 서비스 신뢰성 강화:
* 중복 예약 및 오버부킹 문제 해결로 사용자 신뢰도 증대.
#### 글로벌 시장 진출 가능:
* AWS 기반 확장성을 통해 국제적 사용자 기반 확보.

## 🎯 기술 스택

### 프론트엔드
- <img src="https://img.shields.io/badge/React-61DAFB?style=flat-square&logo=React&logoColor=white"/> 
- <img src="https://img.shields.io/badge/JavaScript-ES6+-F7DF1E?style=flat-square&logo=JavaScript&logoColor=yellow"/>
- <img src="https://img.shields.io/badge/HTML5-E34F26?style=flat-square&logo=HTML5&logoColor=white"/>
- <img src="https://img.shields.io/badge/CSS3-1572B6?style=flat-square&logo=CSS3&logoColor=white"/> 

### 백엔드
- <img src="https://img.shields.io/badge/Java-007396?style=flat-square&logo=Java&logoColor=white"/> 
- <img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?style=flat-square&logo=Spring%20Boot&logoColor=white"/>
- <img src="https://img.shields.io/badge/Python-3776AB?style=flat-square&logo=Python&logoColor=white" alt="Python">

### 인프라
- <img src="https://img.shields.io/badge/AWS%20ECS-FF9900?style=flat-square&logo=Amazon%20ECS&logoColor=white" alt="ECS">
- <img src="https://img.shields.io/badge/AWS%20ELB-FF9900?style=flat-square&logo=Amazon%20AWS&logoColor=white" alt="ELB">
- <img src="https://img.shields.io/badge/AWS%20EC2-FF9900?style=flat-square&logo=Amazon%20EC2&logoColor=white" alt="EC2">
- <img src="https://img.shields.io/badge/AWS%20ECR-FF9900?style=flat-square&logo=Amazon%20ECR&logoColor=white" alt="ECR">
- <img src="https://img.shields.io/badge/AWS%20SSM%20Parameter%20Store-007C89?style=flat-square&logo=Amazon%20AWS&logoColor=white" alt="Parameter Store">
- <img src="https://img.shields.io/badge/AWS%20OpenSearch-527FFF?style=flat-square&logo=Amazon%20OpenSearch&logoColor=white" alt="OpenSearch">
- <img src="https://img.shields.io/badge/AWS%20API%20Gateway-EF2D5E?style=flat-square&logo=Amazon%20AWS&logoColor=white" alt="API Gateway">
- <img src="https://img.shields.io/badge/AWS%20ElastiCache-527FFF?style=flat-square&logo=Amazon%20ElastiCache&logoColor=white" alt="ElastiCache">
- <img src="https://img.shields.io/badge/AWS%20VPC-232F3E?style=flat-square&logo=Amazon%20AWS&logoColor=white" alt="VPC">


#### 보안 및 권한 관리
- <img src="https://img.shields.io/badge/AWS%20Certificate%20Manager-FF4C4C?style=flat-square&logo=Amazon%20AWS&logoColor=white" alt="AWS Certificate Manager">
- <img src="https://img.shields.io/badge/IAM-232F3E?style=flat-square&logo=Amazon%20AWS&logoColor=white" alt="IAM">

#### 이벤트 처리 및 알림 서비스
- <img src="https://img.shields.io/badge/AWS%20Lambda-FF9900?style=flat-square&logo=AWS%20Lambda&logoColor=white" alt="Lambda">
- <img src="https://img.shields.io/badge/Amazon%20SES-FF4C4C?style=flat-square&logo=Amazon%20AWS&logoColor=white" alt="Amazon Simple Email Service">

#### 도메인 구매/등록
- <img src="https://img.shields.io/badge/Route%2053-527FFF?style=flat-square&logo=Amazon%20AWS&logoColor=white" alt="Route 53">

#### 모니터링
- <img src="https://img.shields.io/badge/Fluent%20Bit-00C0A3?style=flat-square&logo=Fluent%20Bit&logoColor=white" alt="Fluent Bit">
- <img src="https://img.shields.io/badge/OpenSearch-3366CC?style=flat-square&logo=Elasticsearch&logoColor=white" alt="OpenSearch">
- <img src="https://img.shields.io/badge/Kibana-005571?style=flat-square&logo=Kibana&logoColor=white" alt="Kibana">

### DataBase
- <img src="https://img.shields.io/badge/Amazon%20RDS-527FFF?style=flat-square&logo=Amazon%20RDS&logoColor=white" alt="Amazon RDS">
- <img src="https://img.shields.io/badge/DynamoDB-4053D6?style=flat-square&logo=Amazon%20DynamoDB&logoColor=white" alt="DynamoDB">
- <img src="https://img.shields.io/badge/PostgreSQL-336791?style=flat-square&logo=PostgreSQL&logoColor=white" alt="PostgreSQL">



### 배포 및 CDN
- <img src="https://img.shields.io/badge/Amazon%20S3-569A31?style=flat-square&logo=Amazon%20S3&logoColor=white" alt="Amazon S3">
- <img src="https://img.shields.io/badge/CloudFront-527FFF?style=flat-square&logo=Amazon%20CloudFront&logoColor=white" alt="CloudFront">




### 목업 및 UI/UX 기획
- <img src="https://img.shields.io/badge/Figma-F24E1E?style=flat-square&logo=Figma&logoColor=white" alt="Figma">



### 협업 도구
- <img src="https://img.shields.io/badge/Jira-0052CC?style=flat-square&logo=Jira&logoColor=white" alt="Jira">
- <img src="https://img.shields.io/badge/Slack-4A154B?style=flat-square&logo=Slack&logoColor=white" alt="Slack">
- <img src="https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=GitHub&logoColor=white" alt="GitHub">


## 🎯 시연 영상

[![시연 영상](https://img.youtube.com/vi/FBUFNmD7Cwc/0.jpg)](https://youtu.be/FBUFNmD7Cwc)



## 🎯 주요 기능
* 회원 관리
  * 회원가입 및 로그인 기능(JWT 인증)
  * 마이페이지에서 예약 내역 조회 및 관리(예약 내용 확인 및 삭제)
 
* 식당 검색 및 정보 제공
  * 위치 기반 식당 검색 기능(Google Maps API를 사용하여 검색한 식당 위치 제공)  
  * 식당 상세 정보 및 메뉴 제공(메뉴, 가격, 주소, 등)

* 예약 관리
  * 실시간 예약 가능 시간대 조회
  * 예약 확정 알림 전송
  * 예약 취소 알림 전송
  * 예약 대기 상태 알림 전송
  * 예약 생성
  * 예약 취소
  * 예약 정보 조회
  * 예약 대기 등록
  * 예약 가능 시간대 조회

## 🎯 팀원
*고OO: 백엔드 개발
*김OO: 프론트 개발
*박OO: 벡엔드 개발
*이OO: 프론트 개발


 




 



   

