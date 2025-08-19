# KRX API Explorer

KRX 정보데이터시스템의 모든 API를 탐색하고 응답 구조를 분석하는 Spring Boot 기반 도구입니다.

## 🎯 주요 기능

- **전체 API 탐색**: 19개의 모든 KRX API를 한번에 호출하여 응답 확인
- **카테고리별 탐색**: 지수, 주식, 증권상품, 채권 카테고리별 API 탐색
- **개별 API 호출**: 특정 API를 개별적으로 호출하여 상세 분석
- **응답 구조 분석**: JSON 응답의 스키마와 필드 타입 자동 분석
- **파일 저장**: 모든 API 응답을 타임스탬프와 함께 파일로 저장

## 📊 지원하는 KRX API (19개)

### 지수 관련 (5개)
- `krx_dd_trd` - KRX 시리즈 일별시세정보
- `kospi_dd_trd` - KOSPI 시리즈 일별시세정보
- `kosdaq_dd_trd` - KOSDAQ 시리즈 일별시세정보
- `bon_dd_trd` - 채권지수 시세정보
- `drvprod_dd_trd` - 파생상품지수 시세정보

### 주식 관련 (8개)
- `stk_bydd_trd` - 유가증권 일별매매정보
- `ksq_bydd_trd` - 코스닥 일별매매정보
- `knx_bydd_trd` - 코넥스 일별매매정보
- `sw_bydd_trd` - 신주인수권증권 일별매매정보
- `sr_bydd_trd` - 신주인수권증서 일별매매정보
- `stk_isu_base_info` - 유가증권 종목기본정보
- `ksq_isu_base_info` - 코스닥 종목기본정보
- `knx_isu_base_info` - 코넥스 종목기본정보

### 증권상품 관련 (3개)
- `etf_bydd_trd` - ETF 일별매매정보
- `etn_bydd_trd` - ETN 일별매매정보
- `elw_bydd_trd` - ELW 일별매매정보

### 채권 관련 (3개)
- `kts_bydd_trd` - 국채전문유통시장 일별매매정보
- `bnd_bydd_trd` - 일반채권시장 일별매매정보
- `smb_bydd_trd` - 소액채권시장 일별매매정보

## 🚀 실행 방법

### 1. Docker Compose 사용 (권장)

```bash
# 환경변수 파일 설정
cp .env.example .env
# .env 파일에서 KRX_API_KEY 설정

# 애플리케이션 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f krx-api-explorer
```

### 2. 로컬 실행

```bash
# Gradle 빌드 및 실행
./gradlew bootRun

# 또는 JAR 파일로 실행
./gradlew build
java -jar build/libs/krx-api-explorer-1.0.0.jar
```

## 📡 API 엔드포인트

### 1. 모든 API 탐색
```bash
curl http://localhost:8080/api/explore/all
```

### 2. 카테고리별 탐색
```bash
# 주식 관련 API들
curl http://localhost:8080/api/explore/category/stock

# 지수 관련 API들  
curl http://localhost:8080/api/explore/category/index

# 증권상품 관련 API들
curl http://localhost:8080/api/explore/category/securities

# 채권 관련 API들
curl http://localhost:8080/api/explore/category/bond
```

### 3. 개별 API 탐색
```bash
# 기본 (어제 날짜 사용)
curl http://localhost:8080/api/explore/single/stk_bydd_trd

# 특정 날짜 지정
curl http://localhost:8080/api/explore/single/ksq_bydd_trd?bizDate=20240115

# Raw 응답 확인
curl http://localhost:8080/api/explore/single/stk_bydd_trd/raw
```

### 4. 유틸리티 엔드포인트
```bash
# 사용 가능한 카테고리 확인
curl http://localhost:8080/api/explore/categories

# 헬스 체크
curl http://localhost:8080/api/explore/health
```

## 📁 응답 파일 저장

모든 API 응답은 다음 위치에 저장됩니다:
```
logs/responses/
├── stk_bydd_trd_20240118_143022.json
├── ksq_bydd_trd_20240118_143025.json
└── ...
```

파일명 형식: `{apiId}_{yyyyMMdd_HHmmss}.json`

## 📋 응답 예시

### 개별 API 응답
```json
{
  "apiId": "stk_bydd_trd",
  "success": true,
  "httpStatus": 200,
  "responseTimeMs": 1250,
  "timestamp": "2024-01-18T14:30:22",
  "response": "{\n  \"result\": {\n    \"data\": [...]\n  }\n}",
  "structure": {
    "type": "object",
    "fields": {
      "result": {
        "type": "object",
        "nestedFields": {
          "data": {
            "type": "array",
            "arraySize": 100,
            "elementType": "object"
          }
        }
      }
    },
    "depth": 3
  }
}
```

### 전체 API 탐색 응답
```json
{
  "totalApis": 19,
  "results": {
    "stk_bydd_trd": { 
      "success": true,
      "responseTimeMs": 1250,
      // ... 기타 정보
    },
    // ... 다른 API 결과들
  },
  "summary": {
    "total": 19,
    "success": 18,
    "failure": 1,
    "successRate": 0.947
  }
}
```

## ⚙️ 환경 설정

### 환경 변수
- `KRX_API_KEY`: KRX API 키 (필수)
- `KRX_API_BASE_URL`: KRX API 베이스 URL (기본값: https://data-api.krx.co.kr)

### application.yml 커스터마이징
```yaml
krx:
  api:
    timeout: 30s        # API 타임아웃
    default-format: json # 기본 응답 형식
```

## 🔧 기술 스택
- **Spring Boot 3.2.1** - 메인 프레임워크
- **Spring WebFlux** - 비동기 HTTP 클라이언트
- **Jackson** - JSON 처리
- **Docker** - 컨테이너화
- **Java 17** - 런타임

## 📝 다음 단계

이 도구로 모든 API 응답을 수집한 후:

1. **응답 구조 분석**: `logs/responses/` 디렉터리의 JSON 파일들을 분석
2. **스키마 설계**: 분석된 구조를 바탕으로 데이터베이스 스키마 설계
3. **ETL 파이프라인 구축**: 실제 데이터 수집 및 처리 시스템 구축

## 🐛 문제 해결

### API 키 관련 오류
```bash
# 환경변수 확인
echo $KRX_API_KEY

# Docker 환경에서 확인
docker-compose exec krx-api-explorer env | grep KRX
```

### 로그 확인
```bash
# 애플리케이션 로그
tail -f logs/krx-api-explorer.log

# Docker 로그
docker-compose logs -f krx-api-explorer
```