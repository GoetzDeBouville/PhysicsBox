plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.android.lint)
}

kotlin {
    jvm("desktop")

    androidLibrary {
        namespace = "dev.zinchenko.physicsbox"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.ui)
                implementation(libs.compose.foundation)
            }
        }

        val jvmCommonMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.jbox2d)
            }
        }

        val androidMain by getting { dependsOn(jvmCommonMain) }
        val desktopMain by getting { dependsOn(jvmCommonMain) }

        val androidDeviceTest by getting {
            dependencies {
                implementation(libs.androidx.runner)
                implementation(libs.androidx.core)
            }
        }
    }
}
