plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.android.lint)
    alias(libs.plugins.maven.plugin)
    alias(libs.plugins.dokka)
}

kotlin {
    jvm("desktop")

    androidLibrary {
        namespace = "io.github.zinchenko_dev"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.ui)
                implementation(libs.compose.foundation)
            }
        }

        val jvmMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.jbox2d)
            }
        }

        val desktopMain by getting { dependsOn(jvmMain) }
        val androidMain by getting { dependsOn(jvmMain) }
    }
}

mavenPublishing {
    coordinates(
        groupId = "io.github.zinchenko-dev",
        artifactId = "physicsbox",
        version = libs.versions.packageVersion.get()
    )
    pom {
        name.set("PhysicsBox")
        description.set("Compose Multiplatform Box layout physics simulation using JBox2D")
        inceptionYear.set("2026")
        url.set("https://github.com/GoetzDeBouville/PhysicsBox")

        licenses {
            license {
                name.set("Apache-2.0")
                url.set("https://opensource.org/license/apache-2-0")
            }
        }

        developers {
            developer {
                id.set("GoetzDeBouville")
                name.set("Alex Zinchenko")
                email.set("support@zinchenko-dev.com")
            }
        }

        scm {
            url.set("https://github.com/GoetzDeBouville/PhysicsBox")
        }
    }

    publishToMavenCentral(automaticRelease = true)

    signAllPublications()
}
