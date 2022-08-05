import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.7.0"

    kotlin("jvm") version "1.6.20"
    kotlin("plugin.serialization") version "1.6.20"

    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.jetbrains.dokka") version "1.6.20"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

intellij {
    println("Using ide version: ${properties("platformVersion")}")
    version.set(properties("platformVersion"))
    type.set("PY")
    pluginName.set(properties("pluginName"))
}

repositories {
    maven(url = "https://www.jetbrains.com/intellij-repository/releases")
    maven(url = "https://jetbrains.bintray.com/intellij-third-party-dependencies")
    maven(url = "https://jitpack.io")
    maven(url = "https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")

    mavenCentral()
    jcenter()
    google()
}


dependencies {
    implementation(kotlin("stdlib-jdk8"))
    //https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/migration.md
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("com.opencsv", "opencsv", "5.6")
    implementation("joda-time", "joda-time", "2.10.14")
    implementation("org.apache.commons", "commons-csv", "1.9.0")
    // https://mvnrepository.com/artifact/com.gluonhq/charm-glisten
    implementation("com.google.code.gson", "gson", "2.9.0")
    implementation("com.squareup.okhttp3", "okhttp", "5.0.0-alpha.6")
//    implementation("org.controlsfx:controlsfx:11.0.3")
    implementation("com.google.auto.service:auto-service:1.0.1")
    implementation("org.eclipse.mylyn.github", "org.eclipse.egit.github.core", "2.1.5")
    // https://mvnrepository.com/artifact/net.lingala.zip4j/zip4j (used for unzipping required plugins)
    implementation("net.lingala.zip4j", "zip4j", "2.9.1")
    implementation("com.github.holgerbrandl:krangl:0.17.3")
    implementation("org.jetbrains.kotlinx:kotlinx-html:0.7.5")
    //Fix java.lang.ClassCastException: class org.apache.xerces.jaxp.DocumentBuilderFactoryImpl for kotlinx-html
    implementation("xerces:xercesImpl:2.12.2")

    testImplementation("junit", "junit", "4.12")
}

tasks{
    patchPluginXml{
        version.set(project.version.toString())
        sinceBuild.set(properties("pluginSinceBuild"))
        untilBuild.set(properties("pluginUntilBuild"))
    }
}


configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
}
tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes.set(
        """
      Add change notes here.<br>
      <em>most HTML tags may be used</em>"""
    )
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = properties("javaVersion")
}

tasks.withType<ShadowJar> {
    project.logger.warn(
        "Don't forget to:\n" +
                "- set your remote server as baseUrl in QueryExecutor class\n" +
                "- turn OFF org.jetbrains.research.ml.tasktracker.Plugin.testMode"
    )
}

tasks.withType<Wrapper> {
    gradleVersion = properties("gradleVersion")
}
