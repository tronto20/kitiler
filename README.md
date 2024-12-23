# kitiler

[titiler](https://github.com/developmentseed/titiler) 의 기능을 kotlin (JVM) 으로 구현하는 것을 목표로 합니다.

GDAL/Spring 기반으로 동작하는 동적 타일 렌더링 서버입니다.

## Features

---

### core

- COG (Cloud Optimized GeoTIFF) 지원
- 여러 TileMatrixSet 지원
- PNG 출력 지원, JPEG 제한적 지원
- OGC WMTS 지원

### spring-boot

- Spring Boot Starter 지원
- Spring WebFlux 기반으로 어플리케이션 자동 설정 

### spring-application

- Spring Boot Webflux 기반으로 구현된 어플리케이션
- OpenAPI 문서 지원
- Graalvm native-image 지원

## Modules

---

| Module                                                                                                               | Description                           |
|----------------------------------------------------------------------------------------------------------------------|---------------------------------------|
| [kitiler-core](https://github.com/tronto20/kitiler/tree/main/core)                                                   | COG 영상을 대상으로 하는 동적 타일링 서비스            |
| [kitiler-dependencies](https://github.com/tronto20/kitiler/tree/main/dependencies)                                   | kitiler 의 의존성 관리                      |
| [spring-boot-kitiler-autoconfigure](https://github.com/tronto20/kitiler/tree/main/spring-boot-kitiler-autoconfigure) | spring-boot 기반의 kitiler autoconfigure |
| [spring-boot-kitiler-starter-core](https://github.com/tronto20/kitiler/tree/main/spring-boot-kitiler-starter-core)   | kitiler-core 의 spring-boot-starter    |
| [kitiler-spring-application](https://github.com/tronto20/kitiler/tree/main/spring-application)                       | spring 기반의 demo application           |

## Installation

---

### spring-boot

현재는 webflux 기반으로만 지원합니다.

build.gradle :
```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "dev.tronto:spring-boot-kitiler-starter-core:0.2.1"
    implementation "org.springframework.boot:spring-boot-starter-webflux:3.4.0"
}
```

build.gradle.kts :
```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("dev.tronto:spring-boot-kitiler-starter-core:0.2.1")
    implementation("org.springframework.boot:spring-boot-starter-webflux:3.4.0")
}
```

## Container Image

Github registry 에 OCI Container Image 가 준비되어 있습니다.

Spring Boot Webflux + native-image (Default):
```bash
docker run --rm --name kitiler \
  -p 8080:8080 \
  -e PORT=8080 \
  ghcr.io/tronto20/kitiler:latest
```

Spring Boot Webflux:
```bash
docker run --rm --name kitiler \
  -p 8080:8080 \
  -e PORT=8080 \
  ghcr.io/tronto20/kitiler:spring-latest
```

### local 빌드

Spring Boot Webflux + native-image (Default):
```bash
git clone https://github.com/tronto20/kitiler
cd kitiler

./gradlew buildImage
```

Spring Boot Webflux:
```bash
git clone https://github.com/tronto20/kitiler
cd kitiler

./gradlew buildImage -Pimage.native.enabled=false
```


## License

[License](https://github.com/tronto20/kitiler/blob/main/LICENSE)


## Development

---

이 프로젝트는 IntelliJ 로 개발되고 있으며, 일관된 개발 경험을 위해 .idea/* 의 일부 파일을 vcs 로 관리합니다.

kotlinter 와 .editorconfig 를 사용하여 코드 스타일을 관리합니다.

dependencies 모듈을 통해 라이브러리 의존성을 관리합니다.
다른 모듈들은 dependencies 모듈을 사용하며 버전을 명시하지 않습니다.

```kotlin
...
dependencies {
    implementation(platform(projects.dependencies))
    ...
}
...
```

