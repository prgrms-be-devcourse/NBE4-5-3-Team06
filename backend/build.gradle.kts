plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.4.4"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
// SlackAppender 의존성 추가
	implementation("com.github.maricn:logback-slack-appender:1.3.0")

	// JJWT 최신 버전 (0.12.6) 추가
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

	// Spring Boot 스타터 의존성
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")

	// Gson
	implementation("com.google.code.gson:gson:2.11.0")

	// Lombok
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	// 데이터베이스 드라이버
	runtimeOnly("com.h2database:h2")
	runtimeOnly("com.mysql:mysql-connector-j")

	// 테스트 의존성
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	// Redis 세션
	implementation("org.springframework.session:spring-session-data-redis")

	// WebSocket
	implementation("org.springframework.boot:spring-boot-starter-websocket")

	// Mail
	implementation("org.springframework.boot:spring-boot-starter-mail")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

