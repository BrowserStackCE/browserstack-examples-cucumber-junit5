plugins {
    java
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:latest.release")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:latest.release")
    testImplementation("io.cucumber:cucumber-java:latest.release")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:latest.release")
    testImplementation("io.cucumber:cucumber-picocontainer:latest.release")
    testImplementation("org.seleniumhq.selenium:selenium-java:3.141.59")
    testImplementation("com.browserstack:browserstack-local-java:1.0.6")
    testImplementation("org.apache.commons:commons-lang3:3.11")
    testImplementation("com.googlecode.json-simple:json-simple:1.1.1")
}

tasks {
    test {
        ignoreFailures = true
        systemProperties(System.getProperties().toMap() as Map<String, Object>)
        systemProperty("cucumber.execution.parallel.enabled", "true")
        systemProperty("cucumber.publish.quiet", "true")
        systemProperty("cucumber.execution.parallel.config.fixed.parallelism", "4")
        systemProperty("cucumber.execution.parallel.config.strategy", "fixed")
        systemProperty("cucumber.filter.tags", "not @Local")
        systemProperty("cucumber.plugin", "json:build/reports/cucumber.json")
        systemProperty("config","single.config.json")
        useJUnitPlatform {
            excludeTags("disabled")
        }
    }
    task("parallel_test", JavaExec::class) {
        group = "local"
        main = "com.com.browserstack.ParallelTest"
        classpath = sourceSets["test"].runtimeClasspath
        systemProperty("config","parallel.config.json")
        systemProperty("cucumber.filter.tags", "not @Local")
        systemProperty("cucumber.publish.quiet","true")
    }
    task("local_test", JavaExec::class) {
        group = "local"
        main = "com.com.browserstack.ParallelTest"
        classpath = sourceSets["test"].runtimeClasspath
        systemProperty("config","local.config.json")
        systemProperty("cucumber.publish.quiet","true")
        systemProperty("cucumber.filter.tags","@Local")
    }

}
