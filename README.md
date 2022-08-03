# 카테고리 API : 권희운

### 빌드 방법
WORK_DIR :  category

jar 파일 빌드 \
./gradlew build


### 서버 구동 방법
빌드된 jar 파일 실행 \
java -jar ./build/libs/category-0.0.1-SNAPSHOT.jar 


### API 사용 가이드
### SWAGGER UI : localhost:8080/swagger-ui/#/category-rest-controller


### 전체 카테고리 조회
GET : localhost:8080 \
Body : {} 

### 상위 카테고리 조회
GET : localhost:8080/categorys/{id} \
Body : {} 

### 카테고리 저장 (부모객체 미 포함) 
POST : localhost:8080/categorys \
Body : 
{
      "categoryNm" : "카테고리 메뉴"
} \

저장시 depth : 1, orderNo : 현 depth 기준 orderNo 자동 저장

### 카테고리 저장 (부모객체 포함)
POST : localhost:8080/categorys \
Body :
{\
"categoryNm" : "카테고리 메뉴", \
"parentCategory" : {
"id" : 부모 카테고리 아이디
}\
}

저장시 depth : 1, orderNo : 현 depth 기준 orderNo 자동 저장


### 카테고리 수정 (카테고리 명)