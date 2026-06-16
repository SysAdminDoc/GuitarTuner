import com.android.build.api.artifact.SingleArtifact
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.util.Locale

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.sysadmindoc.guitartuner"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.sysadmindoc.guitartuner"
        minSdk = 26
        targetSdk = 36
        versionCode = 5
        versionName = "0.0.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    val releaseKeystorePath = providers.environmentVariable("GUITARTUNER_KEYSTORE_PATH")
    val releaseKeystorePassword = providers.environmentVariable("GUITARTUNER_KEYSTORE_PASSWORD")
    val releaseKeyAlias = providers.environmentVariable("GUITARTUNER_KEY_ALIAS")
    val releaseKeyPassword = providers.environmentVariable("GUITARTUNER_KEY_PASSWORD")
    val hasReleaseSigning = listOf(
        releaseKeystorePath,
        releaseKeystorePassword,
        releaseKeyAlias,
        releaseKeyPassword,
    ).all { it.orNull?.isNotBlank() == true }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseKeystorePath.get())
                storePassword = releaseKeystorePassword.get()
                keyAlias = releaseKeyAlias.get()
                keyPassword = releaseKeyPassword.get()
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }

        release {
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildTypes.configureEach {
        vcsInfo {
            include = false
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.05.01"))
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")

    debugImplementation("androidx.compose.ui:ui-tooling")

    testImplementation("junit:junit:4.13.2")
}

androidComponents {
    onVariants(selector().all()) { variant ->
        val variantName = variant.name.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString()
        }
        val verifyManifestTask = tasks.register<VerifyMergedManifestPermissionsTask>(
            "verify${variantName}MergedManifestPermissions",
        ) {
            mergedManifest.set(variant.artifacts.get(SingleArtifact.MERGED_MANIFEST))
        }
        tasks.named("check").configure {
            dependsOn(verifyManifestTask)
        }
    }
}

abstract class VerifyMergedManifestPermissionsTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val mergedManifest: RegularFileProperty

    @TaskAction
    fun verify() {
        val manifestText = mergedManifest.get().asFile.readText()
        val permissions = Regex("""<uses-permission(?:-sdk-\d+)?\s+[^>]*android:name="([^"]+)"""")
            .findAll(manifestText)
            .map { it.groupValues[1] }
            .filter { it.startsWith("android.permission.") }
            .toSet()

        val requiredPermission = "android.permission.RECORD_AUDIO"
        val unexpectedPermissions = permissions - requiredPermission
        if (requiredPermission !in permissions) {
            throw GradleException("Merged manifest must declare $requiredPermission.")
        }
        if (unexpectedPermissions.isNotEmpty()) {
            throw GradleException(
                "Merged manifest declares disallowed Android permissions: " +
                    unexpectedPermissions.sorted().joinToString(),
            )
        }
        if (Regex("""android:foregroundServiceType="[^"]*\bmicrophone\b[^"]*"""")
                .containsMatchIn(manifestText)
        ) {
            throw GradleException("Microphone foreground services are not allowed for the MVP tuner.")
        }
    }
}
