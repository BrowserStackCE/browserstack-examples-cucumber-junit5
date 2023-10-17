plugins {
    java
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    testImplementation("org.slf4j:slf4j-api:1.7.30")
    testImplementation("ch.qos.logback:logback-classic:1.2.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
    testImplementation("io.cucumber:cucumber-java:6.9.1")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:6.9.1")
    testImplementation("io.cucumber:cucumber-picocontainer:6.9.1")
    testImplementation("org.seleniumhq.selenium:selenium-java:3.141.59")
    testImplementation("org.apache.commons:commons-lang3:3.11")
    testImplementation("com.googlecode.json-simple:json-simple:1.1.1")

}

tasks.test{
    dependsOn(":on-prem")
}

tasks.register<Test>("on-prem"){
    dependsOn(":clean")
    ignoreFailures = true
    systemProperties(System.getProperties().toMap() as Map<String, Object>)
    systemProperty("env", "on-prem")
    systemProperty("cucumber.publish.quiet", "true")
    if (System.getProperty("test-name") != null) {
        systemProperty("cucumber.filter.name", System.getProperty ("test-name"))
    } else {
        systemProperty("cucumber.filter.name", "End to End Scenario")
    }
    systemProperty("cucumber.plugin", "json:build/reports/cucumber.json")
    useJUnitPlatform {
        excludeTags("disabled")
    }
}

tasks.register<Test>("on-prem-suite"){
    dependsOn(":clean")
    ignoreFailures = true
    systemProperties(System.getProperties().toMap() as Map<String, Object>)
    systemProperty("env", "on-prem")
    systemProperty("cucumber.publish.quiet", "true")
    systemProperty("cucumber.plugin", "json:build/reports/cucumber.json")
    useJUnitPlatform {
        excludeTags("disabled")
    }
}


tasks.register<Test>("bstack-single"){
    dependsOn(":clean")
    ignoreFailures = true
    systemProperties(System.getProperties().toMap() as Map<String, Object>)
    systemProperty("env", "remote")
    systemProperty("caps-type", "single")
    systemProperty("cucumber.publish.quiet", "true")
    if (System.getProperty("test-name") != null) {
        systemProperty("cucumber.filter.name", System.getProperty ("test-name"))
    } else {
        systemProperty("cucumber.filter.name", "End to End Scenario")
    }
    systemProperty("cucumber.plugin", "json:build/reports/cucumber.json")
    useJUnitPlatform {
        excludeTags("disabled")
    }
}

tasks.register<Test>("bstack-parallel"){
    dependsOn(":clean")
    ignoreFailures = true
    systemProperties(System.getProperties().toMap() as Map<String, Object>)
    systemProperty("env", "remote")
    systemProperty("caps-type", "single")
    systemProperty("cucumber.publish.quiet", "true")
    systemProperty("cucumber.plugin", "json:build/reports/cucumber.json")
    useJUnitPlatform {
        excludeTags("disabled")
    }
}

tasks.register<Test>("bstack-local"){
    dependsOn(":clean")
    ignoreFailures = true
    systemProperties(System.getProperties().toMap() as Map<String, Object>)
    systemProperty("env", "remote_local")
    systemProperty("caps-type", "single_local")
    systemProperty("cucumber.publish.quiet", "true")
    if (System.getProperty("test-name") != null) {
        systemProperty("cucumber.filter.name", System.getProperty ("test-name"))
    } else {
        systemProperty("cucumber.filter.name", "End to End Scenario")
    }
    systemProperty("cucumber.plugin", "json:build/reports/cucumber.json")
    useJUnitPlatform {
        excludeTags("disabled")
    }
}

tasks.register<Test>("bstack-local-parallel"){
    dependsOn(":clean")
    ignoreFailures = true
    systemProperties(System.getProperties().toMap() as Map<String, Object>)
    systemProperty("env", "remote_local")
    systemProperty("caps-type", "single_local")
    systemProperty("cucumber.publish.quiet", "true")
    systemProperty("cucumber.plugin", "json:build/reports/cucumber.json")
    useJUnitPlatform {
        excludeTags("disabled")
    }
}