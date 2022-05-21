plugins {
    kotlin("jvm") version "1.6.21"
}

group = "com.matthalstead"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.1-native-mt")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testImplementation("org.assertj:assertj-core:3.22.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}