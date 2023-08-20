@file:Suppress("UnstableApiUsage")

import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName
import java.util.Properties
import java.io.FileInputStream

plugins {
   id("com.android.application")
   id("org.jetbrains.kotlin.android")
}

android {
   namespace = "de.salomax.muzei.thevergewallpapers"
   compileSdk = 33

   defaultConfig {
      applicationId = namespace
      minSdk = 21
      targetSdk = 33
      // SemVer
      versionName = "1.0.3"
      versionCode = 10003
      archivesName.set("$applicationId-v$versionCode")
      //
      vectorDrawables.useSupportLibrary = true
   }

   signingConfigs {
      create("release") {
         if (getSecret("KEYSTORE_FILE") != null) {
            storeFile = File(getSecret("KEYSTORE_FILE")!!)
            storePassword = getSecret("KEYSTORE_PASSWORD")
            keyAlias = getSecret("KEYSTORE_KEY_ALIAS")
            keyPassword = getSecret("KEYSTORE_KEY_PASSWORD")
         }
      }
   }

   buildTypes {
      release {
         signingConfig = signingConfigs.getByName("release")
         isDebuggable = false
         isJniDebuggable = false
         isMinifyEnabled = true
         isShrinkResources = true
         proguardFiles(
            getDefaultProguardFile("proguard-android.txt"),
            "proguard-rules.pro"
         )
      }
      debug {
         applicationIdSuffix = ".debug"
         versionNameSuffix = " [DEBUG]"
      }
   }

   compileOptions {
      sourceCompatibility(JavaVersion.VERSION_17)
      targetCompatibility(JavaVersion.VERSION_17)
   }

   kotlinOptions {
      jvmTarget = JavaVersion.VERSION_17.toString()
   }

   lint {
      disable.add("InvalidPackage")
      disable.add("AllowBackup")
   }

   buildFeatures {
      buildConfig = true
   }
}

dependencies {
   // kotlin
   implementation("androidx.core:core-ktx:1.10.1")
   // muzei
   implementation("com.google.android.apps.muzei:muzei-api:3.4.1")
   // retrofit
   implementation("com.squareup.retrofit2:retrofit:2.9.0")
   implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
   implementation("org.jsoup:jsoup:1.16.1")
   // misc
   implementation("androidx.work:work-runtime-ktx:2.8.1")
   implementation("androidx.preference:preference-ktx:1.2.1")
   // test
   testImplementation("junit:junit:4.13.2")
}

tasks.withType(Test::class.java) {
   testLogging {
      events.add(TestLogEvent.STARTED)
      events.add(TestLogEvent.PASSED)
      events.add(TestLogEvent.SKIPPED)
      events.add(TestLogEvent.FAILED)
   }
}

fun getSecret(key: String): String? {
   val secretsFile: File = rootProject.file("secrets.properties")
   return if (secretsFile.exists()) {
      val props = Properties()
      props.load(FileInputStream(secretsFile))
      props.getProperty(key)
   } else {
      null
   }
}

// versionCode <-> versionName /////////////////////////////////////////////////////////////////////

/**
 * Checks if versionCode and versionName match.
 * Needed because of F-Droid: both have to be hard-coded and can't be assigned dynamically.
 * So at least check during build for them to match.
 */
tasks.register("checkVersion") {
   doLast {
      val versionCode: Int? = android.defaultConfig.versionCode
      val correctVersionCode: Int = generateVersionCode(android.defaultConfig.versionName!!)
      if (versionCode != correctVersionCode) throw GradleException(
         "versionCode and versionName don't match: versionCode should be $correctVersionCode. Is $versionCode."
      )
   }
}
tasks.findByName("assemble")!!.dependsOn(tasks.findByName("checkVersion")!!)

/**
 * Checks if a fastlane changelog for the current version is present.
 */
tasks.register("checkFastlaneChangelog") {
   doLast {
      val versionCode: Int? = android.defaultConfig.versionCode
      val changelogFile: File =
         file("$rootDir/fastlane/metadata/android/en-US/changelogs/${versionCode}.txt")
      if (!changelogFile.exists())
         throw GradleException(
            "Fastlane changelog missing: expecting file '$changelogFile'"
         )
   }
}
tasks.findByName("build")!!.dependsOn(tasks.findByName("checkFastlaneChangelog")!!)

/**
 * Generates a versionCode based on the given semVer String.
 *
 * @param semVer e.g. 1.3.1
 * @return e.g. 10301 (-> 1 03 01)
 */
fun generateVersionCode(semVer: String): Int {
   return semVer.split('.')
      .map { Integer.parseInt(it) }
      .reduce { sum, value -> sum * 100 + value }
}
