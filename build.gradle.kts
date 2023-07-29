plugins {
    idea
    kotlin("jvm") version "1.8.20"

}

val kotlinVersion: String by extra("1.8.20")
val kotlinCoroutinesVersion: String by extra("1.6.4")


group = "com.andrewhebert"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
}


task("run", JavaExec::class) {
    standardInput = System.`in`
    group = "application"
    mainClass.set("HereToThereKt")
    setArgsString("/home/ahebert/shared-3 /mnt/hdd-2/Media/Pictures /mnt/hdd-2/Media/Videos /tmp")
    classpath = sourceSets["main"].runtimeClasspath
}

//task("run", JavaExec::class) {
//    standardInput = System.`in`
//    group = "application"
//    mainClass.set("HereToThereKt")
//    setArgsString("/tmp/source /tmp/pics /tmp/videos /tmp/backup")
//    classpath = sourceSets["main"].runtimeClasspath
//}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlin:kotlin-script-runtime:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-scripting-common:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-scripting-dependencies:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-scripting-dependencies-maven:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

    implementation("com.drewnoakes:metadata-extractor:2.18.0")
    implementation("commons-codec:commons-codec:1.16.0")


    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testImplementation("org.junit-pioneer:junit-pioneer:1.6.2")
}