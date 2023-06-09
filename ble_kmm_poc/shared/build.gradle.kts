plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("com.squareup.sqldelight")
}

kotlin {
    android {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../iosApp/Podfile")
//        homepage = "https://github.com/touchlab"
//        pod("SQLCipher", "~> 4.0")
        framework {
            baseName = "shared"
        }
    }
    val sqlDelightVersion = "1.5.3"
    val coroutinesVersion = "1.6.2"
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                implementation("com.squareup.sqldelight:runtime:$sqlDelightVersion")

            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("net.zetetic:android-database-sqlcipher:4.5.0")
                implementation("com.squareup.sqldelight:android-driver:$sqlDelightVersion")
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)

            dependencies {
                implementation("com.squareup.sqldelight:native-driver:$sqlDelightVersion")
                implementation("co.touchlab:sqliter-driver:1.0.10")
//                implementation("co.touchlab:sqliter:0.7.1") {
//                    version {
//                        strictly("0.7.1")
//                    }
//                }
            }
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
    }
}

android {
    namespace = "kaz.bpmandroid"
    compileSdk = 33
    defaultConfig {
        minSdk = 24
        targetSdk = 33
    }
}


sqldelight {
    database("AppDatabase") {
        packageName = "kaz.bpmandroid.db"
        deriveSchemaFromMigrations = false
        linkSqlite = false
//        migrationOutputDirectory = file("$buildDir/generated/migrations")
    }
}