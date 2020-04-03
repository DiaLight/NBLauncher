import java.time.LocalDateTime

buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath("edu.sc.seis.gradle:launch4j:${findProperty("launch4j.version")}")
    }
}

plugins {
    base
    java
    application
    id("edu.sc.seis.launch4j") version "${findProperty("launch4j.version")}"
}

group = findProperty("project.group")!!
version = findProperty("project.version")!!

application {
    applicationName = findProperty("project.name")!!.toString()
    mainClassName = findProperty("project.entry")!!.toString()
}

repositories {
    mavenCentral()
    jcenter()
}

val compile by configurations.getting

dependencies {
    compile("com.google.code.gson:gson:${findProperty("gson.version")}")
    implementation("org.jetbrains:annotations:${findProperty("jetbrains.annotations.version")}")
    implementation(files(findProperty("scenicView.path")))
    implementation("org.fxmisc.cssfx:cssfx:1.1.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<Jar> {
    manifest {
        attributes["Implementation-Title"] = application.applicationName
        attributes["Implementation-Version"] = project.version
        attributes["Application-Name"] = application.applicationName
        attributes["Built-Date"] = LocalDateTime.now()
        attributes["Main-Class"] = application.mainClassName
        attributes["Built-Java"] = System.getProperty("java.version")
    }
    compile.forEach { if(it.isDirectory) from(it) else from(zipTree(it)) }
}

val dontCopy by tasks.creating(Copy::class)

launch4j {
    val version = findProperty("project.version")!!
    mainClassName = application.mainClassName
    icon = "${projectDir}/src/main/resources/dialight/nblauncher/icon.ico"
    copyConfigurable = dontCopy
    libraryDir = "."
    jar = tasks["jar"].outputs.files.singleFile.absolutePath
    outfile = "${project.name}-$version.exe"
//    xmlFileName = "${projectDir}/createExe.xml"
}
