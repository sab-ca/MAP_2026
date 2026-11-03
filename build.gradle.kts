plugins {
    id("java")
    id("application")
}

group = "socialnetwork"
version = "1.0"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

application {
    mainClass.set("Main")
}

// consola citeste de la tastatura si cand aplicatia e pornita cu "gradlew run"
tasks.named<JavaExec>("run") {
    standardInput = System.`in`
    workingDir = projectDir
}
