plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.barberbuddy"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.barberbuddy"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Required for MediaPipe — prevents .task file from being compressed
    androidResources {
        noCompress += listOf("task", "tflite")
    }
}

dependencies {
    // CameraX
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")

    // MediaPipe
    implementation("com.google.mediapipe:tasks-vision:0.10.14")

    // UI
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // CircleImageView for rounded profile images
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // ... inside dependencies ...
// SceneView for ARCore (3D Rendering)
    implementation("com.gorisse.thomas.sceneform:sceneform:1.23.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}