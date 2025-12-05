# Shifty's Music

Spring Boot 기반의 음악 추천 및 관리 시스템입니다.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.x-brightgreen)



##프로젝트 소개

사용자가 음악을 검색하고, 재생목록을 관리하며, 
Spotify API를 통해 새로운 곡을 추가할 수 있는 웹입니다.

### 주요 기능
- 음악 검색 - 아티스트, 곡명, 앨범으로 검색
- 음악 목록 - 아티스트별 그룹화
- Spotify 연동 - Spotify에서 곡 검색 및 DB 추가
- 회원 관리 - 로그인, 회원가입
- 보안 - Spring Security 기반


=========기술 스택=========

### Backend
| 기술 | 버전 | 설명 |
| Java | 17+ | 프로그래밍 언어 |
| Spring Boot | 3.x | 웹 프레임워크 |
| Spring Data JPA | 3.x | ORM / 데이터 액세스 |
| Spring Security | 6.x | 인증 및 권한 관리 |
| Lombok | 1.18.x | 코드 감소 |

### Frontend
| 기술 | 설명 |
| Thymeleaf | 서버사이드 템플릿 엔진 |
| HTML5/CSS3 | 웹 및 스타일링 |
| JavaScript | 클라이언트 스크립트 |

### Database
| 기술 | 설명 |
| MySQL | 관계형 데이터베이스 |
| H2   | 개발용 인메모리 DB |

### External API
| 서비스 | 용도 |
| Spotify Web API | 곡 검색 및 정보 조회 |

---

## 프로젝트 구조

src/main/java/kr/ac/kopo/
├── config/
│ ├─ SecurityConfig.java # Spring Security 설정
│ └── DataInitializer.java # 초기 데이터 설정
├── controller/
│ ├── MainController.java # 메인 페이지 컨트롤러
│ ├── LoginController.java # 로그인/회원가입
│ └── SpotifyController.java # Spotify API 연동
├── entity/
│ ├── Song.java # 곡 엔티티
│ └── User.java # 사용자 엔티티
├── repository/
│ ├── SongRepository.java # 곡 데이터 액세스
│ └── UserRepository.java # 사용자 데이터 액세스
├── service/
│ ├── SongService.java # 곡 비즈니스 로직
│ ├── UserService.java # 사용자 비즈니스 로직
│ └── SpotifyService.java # Spotify API 서비스
└── dto/
└── SpotifyTrackDto.java # Spotify 응답 DTO

src/main/resources/
├── templates/
│ ├── assignment.html # 메인 페이지
│ ├── assignmentInner1.html # 곡 목록 1
│ ├── assignmentInner2.html # 곡 목록 2
│ ├── login.html # 로그인 페이지
│ └── signup.html # 회원가입 페이지
├── static/
│ ├── css/ # 스타일시트
│ └── imgs/ # 이미지 파일
└── application.properties # 애플리케이션 설정



## 데이터베이스 설계

### Entity-Relationship 다이어그램


│ users │ songs │
│ id (PK) │ id (PK) │
│ username │ artist │
│ password │ title │
│ email │ album │
│ full_name │ genre │
│ role │ category │
│ enabled │ year_released│
|   -   │ duration │
|    -  │ spotify_url │


### 테이블 상세

#### songs 테이블
| 컬럼명 | 타입 | 제약조건 | 설명 |
| id | BIGINT | PK, AUTO_INCREMENT | 기본키 |
| artist | VARCHAR(100) | NOT NULL | 아티스트명 |
| title | VARCHAR(200) | NOT NULL | 곡 제목 |
| album | VARCHAR(200) | - | 앨범명 |
| genre | VARCHAR(50) | - | 장르 |
| category | VARCHAR(50) | - | 카테고리 |
| year_released | INT | - | 발매년도 |
| duration | INT | - | 재생시간(초) |
| spotify_url | VARCHAR(500) | - | Spotify 링크 |

#### users 테이블
| 컬럼명 | 타입 | 제약조건 | 설명 |
| id | BIGINT | PK, AUTO_INCREMENT | 기본키 |
| username | VARCHAR(50) | UNIQUE, NOT NULL | 사용자명 |
| password | VARCHAR(255) | NOT NULL | 비밀번호 |
| email | VARCHAR(100) | - | 이메일 |
| full_name | VARCHAR(100) | - | 실명 |
| role | VARCHAR(20) | - | 권한 (USER/ADMIN) |
| enabled | BOOLEAN | DEFAULT TRUE | 계정 활성화 |


## 설치 및 실행

### 1. 사전 요구사항
- Java 17 이상
- MySQL 8.0 이상
- Gradle 8.x

### 2. 프로젝트 클론
git clone https://github.com/ShiftyIsMine/FrameworkTeamProject.git


### 3. 데이터베이스 설정
CREATE DATABASE music_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

text

### 4. application.properties 설정
데이터베이스 연결
spring.datasource.url=jdbc:mysql://localhost:3306/yours_db
spring.datasource.username=
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

JPA 설정
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

Spotify API (선택)
spotify.client.id=your_spotify_client_id
spotify.client.secret=your_spotify_client_secret


### 5. 빌드 및 실행
빌드
./gradlew build

실행
./gradlew bootRun


### 6. 접속
http://localhost:8080

##  테스트 계정

| 관리자 | shifty | 1234 |
| 일반 사용자 | user | 1234 |

---

## API 엔드포인트

### 페이지 라우팅
| Method | URL | 설명 |
| GET | `/` | 메인 페이지 |
| GET | `/inner1` | 곡 목록 (아티스트별) |
| GET | `/inner2` | 곡 목록 (테이블형) |
| GET | `/login` | 로그인 페이지 |
| GET | `/signup` | 회원가입 페이지 |
| GET | `/add-song` | 곡 추가 페이지 |

### REST API
| Method | URL | 설명 |
| GET | `/api/spotify/search-unified?query={query}` | Spotify 검색 |
| POST | `/api/spotify/add-to-db` | DB에 곡 추가 |
| POST | `/api/playlist/add/{songId}` | 재생목록 추가 |
| DELETE | `/api/playlist/remove/{id}` | 재생목록 삭제 |


### 메인 페이지
- 음악 카드 형태로 표시
- 카테고리/아티스트 필터

### 곡 목록 페이지
- 아티스트별 그룹화

### 로그인/회원가입
- Spring Security 기반
- 비밀번호 암호화



## 개발자

- 이름: 정재선(JEONG JAESEON)
- 이메일: jjs5827852@naver.com
- GitHub: https://github.com/ShiftyIsMine

