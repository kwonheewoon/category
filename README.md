# 카테고리 API : 권희운

## 빌드 방법
WORK_DIR :  category

jar 파일 빌드 \
./gradlew build


## 서버 구동 방법
빌드된 jar 파일 실행 \
java -jar ./build/libs/category-0.0.1-SNAPSHOT.jar 


## API 사용 가이드
### Swagger Ui : localhost:8080/swagger-ui/#/category-rest-controller


### 전체 카테고리 조회
GET : http://localhost:8080 
```c
body : {}
```

### 상위 카테고리 조회
GET : http://localhost:8080/categorys/{id} 
```c
body : {} 
```

### 카테고리 저장 (부모객체 미 포함) 
POST : http://localhost:8080/categorys 

```c
body : { 
    "categoryNm" : "카테고리1"
} 
```

저장시 depth : 1, orderNo : 현 depth 기준 orderNo 자동 저장

### 카테고리 저장 (부모객체 포함)
POST : http://localhost:8080/categorys 

```c
body : {
    "categoryNm" : "카테고리1", 
    "parentCategory" : {
        "id" : "부모 카테고리 아이디"
    }
}
```
저장시 depth : 1, orderNo : 현 depth 기준 orderNo 자동 저장


### 카테고리 수정 (카테고리 명, 정렬번호)
PATCH : http://localhost:8080/categorys/{id} 

```c
body : { 
      "categoryNm" : "카테고리명 수정",
      "orderNo" : 1
}
```


### 카테고리 수정 (카테고리 명, depth)

PATCH : http://localhost:8080/categorys/{id} 

```c
body : { 
    "categoryNm" : "카테고리명 수정",
    "depth" : 1
}
```

depth는 1이상 수정 불가 \
ex) depth를 1이상으로 수정시 어느 카테고리의 부모인지 불명확하기 때문
depth 1이상 수정시 반환 되는 메시지

```c
response : { 
      "code": 400, 
      "message": "DEPTH는 1-DEPTH로 수정만 가능합니다.", 
      "result": null 
} 
```


### 카테고리 수정 (부모 카테고리 포함)
PATCH : http://localhost:8080/categorys/{id} 

```c
body : { 
    "categoryNm" : "부모 카테고리 변경", 
    "parentCategory" : { 
        "id" : "부모 카테고리 아이디" 
    } 
}
```


### 카테고리 삭제(하위 자식 카테고리 전부 삭제)

DELETE : http://localhost:8080/categorys/{id}
