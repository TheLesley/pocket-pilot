plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

// ---- Ktlint (Phase 20: Kotlin style guide enforcement) ----
// AGP 9's built-in Kotlin integration doesn't expose the KotlinSourceSet API
// that JLLeitschuh's plugin auto-discovers, so the auto-registered tasks only
// cover .kts build scripts. Below we register KtLintCheckTask / KtLintFormatTask
// explicitly for the Android main + test Kotlin sources and wire them into the
// aggregate ktlintCheck / ktlintFormat tasks so `./gradlew ktlintCheck` runs a
// full lint.
ktlint {
    version.set(libs.versions.ktlintTool.get())
    android.set(true)
    ignoreFailures.set(false)
    enableExperimentalRules.set(false)
    verbose.set(true)
    outputToConsole.set(true)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
    filter {
        exclude { entry -> entry.file.toString().contains("generated") }
        exclude("**/build/**")
    }
}

// Bring in the ktlint CLI as a dedicated configuration so we can run it via a
// JavaExec task against the Android source sets. This works uniformly across
// AGP versions instead of relying on the plugin's source-set autodetection.
val ktlintCli: Configuration by configurations.creating

dependencies {
    ktlintCli("com.pinterest.ktlint:ktlint-cli:${libs.versions.ktlintTool.get()}")
}

val ktlintKotlinPatterns = listOf(
    "src/main/java/**/*.kt",
    "src/main/kotlin/**/*.kt",
    "src/test/java/**/*.kt",
    "src/test/kotlin/**/*.kt",
    "src/androidTest/java/**/*.kt",
    "src/androidTest/kotlin/**/*.kt",
)

val ktlintMainCheck = tasks.register<JavaExec>("ktlintMainSourceSetCheck") {
    description = "Lint Android Kotlin source sets (Phase 20)."
    group = "verification"
    classpath = ktlintCli
    mainClass.set("com.pinterest.ktlint.Main")
    // Android code style is set via .editorconfig (ktlint_code_style). Args
    // here just configure reporters and the source-file globs to lint.
    args = listOf(
        "--reporter=plain",
        "--reporter=checkstyle,output=${layout.buildDirectory.get()}/reports/ktlint/main.xml",
    ) + ktlintKotlinPatterns
    // ktlint uses reflection into JDK internals; open the packages it needs on
    // Java 17+ to silence warnings that otherwise appear as build noise.
    jvmArgs = listOf(
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.util=ALL-UNNAMED",
    )
}

val ktlintMainFormat = tasks.register<JavaExec>("ktlintMainSourceSetFormat") {
    description = "Format Android Kotlin source sets (Phase 20)."
    group = "formatting"
    classpath = ktlintCli
    mainClass.set("com.pinterest.ktlint.Main")
    args = listOf("-F") + ktlintKotlinPatterns
    jvmArgs = listOf(
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.util=ALL-UNNAMED",
    )
    // Format is best-effort; still surface non-format failures.
    isIgnoreExitValue = false
}

tasks.named("ktlintCheck").configure { dependsOn(ktlintMainCheck) }
tasks.named("ktlintFormat").configure { dependsOn(ktlintMainFormat) }

// ---- Detekt (Phase 20: complexity, memory-leak, architectural rules) ----
detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    baseline = file("$rootDir/config/detekt/baseline.xml")
    buildUponDefaultConfig = true
    allRules = false
    parallel = true
    ignoreFailures = false
    source.setFrom(files("src/main/java", "src/main/kotlin"))
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = "11"
    reports {
        html.required.set(true)
        xml.required.set(true)
        sarif.required.set(true)
        md.required.set(false)
        txt.required.set(false)
    }
    exclude("**/build/**", "**/generated/**")
}

tasks.withType<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>().configureEach {
    jvmTarget = "11"
}

android {
    namespace = "com.example.pocketpilot"
    compileSdk {
        version =
            release(36) {
                minorApiLevel = 1
            }
    }

    defaultConfig {
        applicationId = "com.example.pocketpilot"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Release signing (Phase 22: CI/CD). The keystore is materialized by the CI
    // workflow from GitHub Secrets and its path passed via POCKETPILOT_KEYSTORE_PATH.
    // When the secrets are absent (local dev, PRs from forks), the block is
    // skipped and the release build type falls back to the debug signing config
    // so `assembleRelease` still succeeds for smoke checks.
    val keystorePath = System.getenv("POCKETPILOT_KEYSTORE_PATH")
    val keystorePassword = System.getenv("POCKETPILOT_KEYSTORE_PASSWORD")
    val keyAlias = System.getenv("POCKETPILOT_KEY_ALIAS")
    val keyPassword = System.getenv("POCKETPILOT_KEY_PASSWORD")
    val hasReleaseSigning =
        !keystorePath.isNullOrBlank() &&
            !keystorePassword.isNullOrBlank() &&
            !keyAlias.isNullOrBlank() &&
            !keyPassword.isNullOrBlank() &&
            file(keystorePath).exists()

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(keystorePath!!)
                storePassword = keystorePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig =
                if (hasReleaseSigning) {
                    signingConfigs.getByName("release")
                } else {
                    signingConfigs.getByName("debug")
                }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    // Android Lint configuration for Phase 20 (Code Quality & Static Analysis).
    // A repo-level lint.xml defines severities; the baseline captures the
    // pre-existing warnings so new regressions become build failures without
    // requiring the entire backlog to be resolved in one PR.
    lint {
        abortOnError = true
        warningsAsErrors = false
        checkReleaseBuilds = true
        checkDependencies = true
        checkAllWarnings = false
        ignoreTestSources = true
        explainIssues = true
        htmlReport = true
        xmlReport = true
        sarifReport = true
        lintConfig = file("$rootDir/config/lint/lint.xml")
        baseline = file("$rootDir/config/lint/lint-baseline.xml")
        // Fail the build on any of these regardless of severity in lint.xml.
        error +=
            listOf(
                "StopShip",
                "HardcodedText",
                "ContentDescription",
                "LabelFor",
                "ClickableViewAccessibility",
            )
        // Downgraded to warning; addressed incrementally.
        warning +=
            listOf(
                "UnusedResources",
                "MissingTranslation",
            )
        // Not applicable to a Compose-only, single-locale MVP.
        disable +=
            listOf(
                "GoogleAppIndexingWarning",
                "MissingDefaultResource",
            )
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.window.size)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization.converter)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.lifecycle.process)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    testImplementation(libs.junit)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.core.ktx)
    testImplementation(libs.androidx.junit)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.turbine)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
