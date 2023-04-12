import org.jetbrains.compose.compose

plugins {
    kotlin("multiplatform")

    id("org.jetbrains.compose")
    id("com.android.library")
}

group = "com.rjuszczyk.compose"
version = "1.0-SNAPSHOT"


kotlin {
    android()
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
       // withJava()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {

                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)

            }
        }
        val androidMain by getting {
            dependencies {
                api("androidx.activity:activity-compose:1.7.0")
                api("androidx.appcompat:appcompat:1.6.1")
                api("androidx.core:core-ktx:1.10.0")
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)

                implementation("org.jetbrains.compose.ui:ui-graphics:1.1.1")
                implementation("uk.co.caprica:vlcj:4.8.2")
            }
        }
        val desktopTest by getting
    }
}

android {
    compileSdk = 33
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    namespace = "com.rjuszczyk.compose"
    defaultConfig {
        minSdk =28
        targetSdk = 33
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
