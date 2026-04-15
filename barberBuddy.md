# BarberBuddy - Persistent Memory

## Project Overview
BarberBuddy is an Android app that analyzes users' face shapes using MediaPipe's FaceLandmarker and provides personalized hairstyle recommendations using fuzzy logic membership functions. Includes AR try-on functionality and saved styles collection.

**Tech Stack:**
- Java (Android)
- CameraX for camera preview
- MediaPipe Tasks Vision (face_landmarker.task model)
- ARCore + Sceneform for AR try-on feature
- Glide for image loading
- RecyclerView with GridLayoutManager for lists

**Build Config:**
- minSdk: 26, targetSdk: 34
- ViewBinding enabled
- `noCompress` for .task and .tflite files

---

## Activities (10 total)

| # | Activity | Purpose |
|---|---------|---------|
| 1 | SplashActivity | Launch screen, onboarded check, navigation routing |
| 2 | OnboardingActivity | Welcome screens with animations |
| 3 | MainActivity | Face scanning with CameraX, gallery upload |
| 4 | ResultsActivity | Face shape results display |
| 5 | RecommendationsActivity | Hairstyle grid with search/filter |
| 6 | StyleDetailActivity | Individual hairstyle details |
| 7 | ProfileActivity | User stats and settings |
| 8 | ARPreviewActivity | Camera with landmark overlay |
| 9 | SavedStylesActivity | Saved hairstyles collection |
| 10 | TryOnActivity | AR try-on with 3D hair models |

---

## Core Java Classes (19 files)

### Models
| File | Lines | Purpose |
|------|-------|---------|
| Hairstyle.java | 121 | Data class with id, name, imageRes, overlayRes, suitableFaceShapes, description, maintenanceLevel, maintenanceTips, trend, modelPath, category, isAsianStyle |
| HairstyleRepository.java | 250 | Static repository with 18 hairstyles, recommendation engine |

### Face Analysis
| File | Lines | Purpose |
|------|-------|---------|
| FaceAnalyzer.java | 143 | CameraX ImageAnalysis.Analyzer with MediaPipe FaceLandmarker |
| FaceShapeAnalyzer.java | 397 | Fuzzy logic analysis with 5 ratios and trapezoidal membership functions |

### UI/Adapters
| File | Lines | Purpose |
|------|-------|---------|
| HairstyleAdapter.java | 115 | RecyclerView adapter for hairstyle cards |
| LandmarkOverlayView.java | 129 | Draws face oval and 478 landmarks on camera preview |

### Activities
| File | Lines | Purpose |
|------|-------|---------|
| SplashActivity.java | 46 | Launch screen with onboarded check |
| OnboardingActivity.java | 73 | Welcome screens with animations |
| MainActivity.java | 271 | Camera preview with face scanning |
| ResultsActivity.java | 102 | Face shape results display |
| RecommendationsActivity.java | 241 | Hairstyle grid with search/filter chips |
| StyleDetailActivity.java | 122 | Hairstyle details, save, share, AR |
| ProfileActivity.java | 108 | User profile, stats, reset |
| ARPreviewActivity.java | 198 | Camera with landmark overlay, screenshot capture |
| SavedStylesActivity.java | 84 | Saved hairstyles collection |
| TryOnActivity.java | ~350 | AR try-on with 3D hair models + MediaPipe hair segmentation |

### AR Try-On
| File | Lines | Purpose |
|------|-------|---------|
| HairSegmentationHelper.java | ~280 | MediaPipe HairSegmenter integration for hair removal |

### Data/Services
| File | Lines | Purpose |
|------|-------|---------|
| SavedStylesManager.java | 49 | SharedPreferences wrapper for saved styles |
| Prefs.java | 20 | Helper for "onboarded" preference |

---

## Navigation Flow

```
SplashActivity
    ↓ (check onboarded)
OnboardingActivity (first time) OR MainActivity (returning)
    ↓
MainActivity (face scan)
    ↓
ResultsActivity (face shape result)
    ↓
RecommendationsActivity (browse hairstyles)
    ↓
StyleDetailActivity (hairstyle details)
    ↓
TryOnActivity (AR try-on) OR ARPreviewActivity (landmark overlay)

Bottom Navigation (from RecommendationsActivity):
    Home → RecommendationsActivity
    Scan → MainActivity  
    Saved → SavedStylesActivity
    Profile → ProfileActivity
```

---

## Hairstyle Data (18 total)

### Western Styles (1-8)
| # | Name | Model Path | Face Shapes | Maintenance | Trend |
|---|------|------------|-------------|------------|-------|
| 1 | Textured Crop | models/textured_crop.glb | Round, Square, Triangle, Diamond | Low | Trending |
| 2 | Classic Pompadour | models/pompadour.glb | Round, Oblong, Heart, Diamond | High | Classic |
| 3 | Buzz Cut | models/buzz_cut.glb | Oval, Square, Oblong, Diamond | Low | Timeless |
| 4 | Side Part | models/side_part.glb | Oval, Square, Heart, Triangle | Medium | Classic |
| 5 | Modern Quiff | models/quiff.glb | Oval, Round, Heart, Triangle | Medium | Trending |
| 6 | Undercut | models/undercut.glb | Oval, Square, Oblong, Diamond | Medium | Trending |
| 7 | Caesar Cut | models/caesar.glb | Oval, Round, Square, Triangle | Low | Classic |
| 8 | Slick Back | models/slick_back.glb | Oval, Oblong, Heart, Diamond | Medium | Classic |

### Asian Styles (9-18)
| # | Name | Model Path | Face Shapes | Maintenance | Trend |
|---|------|------------|-------------|------------|-------|
| 9 | Korean Two-Block | models/twoblock.glb | Oval, Heart, Oblong, Diamond | Medium | Trending |
| 10 | Curtain Fringe | models/curtain.glb | Oval, Heart, Round, Triangle | Low | Trending |
| 11 | Korean Perm | models/korean_perm.glb | Oval, Square, Oblong, Diamond | Medium | Trending |
| 12 | Japanese Wolf Cut | models/wolfcut.glb | Oval, Round, Heart, Square | Medium | Trending |
| 13 | Asian High Fade | models/highfade.glb | Oval, Square, Round, Triangle | Low | Classic |
| 14 | Asian Textured Fringe | models/fringe.glb | Heart, Oval, Oblong, Diamond | Low | Trending |
| 15 | K-Drama Pushed Back | models/kdrama.glb | Oval, Square, Diamond, Oblong | Medium | Classic |
| 16 | Asian Taper Fade | models/taper.glb | Round, Square, Oval, Triangle | Low | Classic |
| 17 | Modern Bowl Cut | models/bowl.glb | Oval, Round, Heart, Diamond | Low | Trending |
| 18 | Mullet Fade | models/mullet.glb | Oval, Square, Heart, Round | Medium | Trending |

---

## Face Shape Analysis

### Detected Shapes (7)
- Oval, Round, Square, Heart, Oblong, Diamond, Triangle

### Fuzzy Logic Ratios
- R1: Cheekbone Width / Face Height (width-to-height)
- R2: Jaw Width / Cheekbone Width
- R3: Forehead Width / Cheekbone Width
- R4: Jaw Width / Forehead Width
- R5: Face Height / Cheekbone Width (elongation)

### Key Landmark Indices
| Landmark | Index |
|----------|-------|
| RIGHT_CHEEK | 50 |
| LEFT_CHEEK | 280 |
| CHIN | 152 |
| JAW_EDGE | 288 |
| RIGHT_FOREHEAD | 103 |
| LEFT_FOREHEAD | 332 |
| FACE_TOP | 10 |
| FACE_BOTTOM | 152 |
| LEFT_JAW_LOWER | 58 |

### Face Oval Overlay Indices (34 points)
```
10, 338, 297, 332, 284, 251, 389, 356, 454, 323, 361,
288, 397, 365, 379, 378, 400, 377, 152, 148, 176, 149,
150, 136, 172, 58, 132, 93, 234, 127, 162, 21, 54, 103, 67, 109
```

---

## Layouts (11 files)

| Layout | Purpose |
|--------|---------|
| activity_splash.xml | Logo splash screen |
| activity_onboarding.xml | Welcome screens |
| activity_main.xml | Camera preview with scan oval |
| activity_results.xml | Face shape results |
| activity_recommendations.xml | Hairstyle grid with search/filter |
| activity_style_detail.xml | Hairstyle details |
| activity_profile.xml | User profile/stats |
| activity_ar_preview.xml | Camera with landmark overlay |
| activity_saved_styles.xml | Saved styles collection |
| activity_try_on.xml | AR try-on screen |
| item_hairstyle.xml | RecyclerView item card |

---

## Drawable Resources

### Icons
- ic_nav_home.xml, ic_nav_scan.xml, ic_nav_saved.xml, ic_nav_profile.xml
- ic_arrow_back.xml, ic_arrow_back_white.xml
- ic_bookmark_outline.xml, ic_bookmark_filled.xml
- ic_share.xml, ic_share_white.xml
- ic_search.xml, ic_close.xml
- ic_profile_placeholder.xml, ic_scan_oval.xml
- ic_logo_scissors.xml, ic_face_oval.xml, ic_face_oval_gold.xml

### Hair Overlay Drawables
- hair_crop_overlay.xml, hair_pomp_overlay.xml, hair_buzz_overlay.xml
- hair_sidepart_overlay.xml, hair_quiff_overlay.xml, hair_undercut_overlay.xml
- hair_caesar_overlay.xml, hair_slick_overlay.xml

### Backgrounds
- bg_chip_green.xml, bg_chip_gray.xml, bg_chip_gold.xml
- bg_icon_btn.xml, bg_icon_btn_dark.xml, bg_icon_btn_white.xml
- bg_circle_outline.xml, bg_circle_filled_dark.xml
- bottom_nav_selector.xml
- gradient_image_bottom.xml, gradient_detail_overlay.xml
- placeholder_hairstyle.xml

---

## Assets Status

### 3D Models (`app/src/main/assets/models/`)
| File | Status |
|------|--------|
| pompadour.glb | ✅ Exists |
| face_landmarker.task | ✅ Exists |
| textured_crop.glb | ❌ Missing |
| buzz_cut.glb | ❌ Missing |
| side_part.glb | ❌ Missing |
| quiff.glb | ❌ Missing |
| undercut.glb | ❌ Missing |
| caesar.glb | ❌ Missing |
| slick_back.glb | ❌ Missing |
| twoblock.glb | ❌ Missing |
| curtain.glb | ❌ Missing |
| korean_perm.glb | ❌ Missing |
| wolfcut.glb | ❌ Missing |
| highfade.glb | ❌ Missing |
| fringe.glb | ❌ Missing |
| kdrama.glb | ❌ Missing |
| taper.glb | ❌ Missing |
| bowl.glb | ❌ Missing |
| mullet.glb | ❌ Missing |

### Image Resources (Style Photos)
All style_*.png drawables needed (style_textured_crop.png, style_pompadour.png, etc.)

---

## SharedPreferences Data

### barberbuddy_prefs
| Key | Type | Purpose |
|-----|------|---------|
| onboarded | boolean | Has user completed onboarding |
| last_face_shape | String | Most recent detected face shape |
| last_confidence | int | Confidence of last scan |
| scan_count | int | Total number of scans |
| last_secondary_shape | String | Secondary face shape |
| asian_context | boolean | Asian hairstyle preference |

### saved_styles
| Key | Type | Purpose |
|-----|------|---------|
| saved_ids | StringSet | Set of saved hairstyle IDs |

---

## Security Vulnerabilities (for Presentation)

| # | Vulnerability | Severity | OWASP | Testable |
|---|---------------|----------|-------|----------|
| 1 | Insecure SharedPreferences | Medium | M1 | Yes |
| 2 | allowBackup=true | Medium | M1 | Yes |
| 3 | Exported SplashActivity | Low | M4 | Yes |
| 4 | Intent parameter injection | High | M1 | Yes |
| 5 | Invalid ID handling | Medium | M7 | Yes |
| 6 | Debug logging | Low | M3 | Yes |
| 7 | No certificate pinning | Medium | M3 | Future |
| 8 | No code obfuscation | Low | M8 | Yes |

---

## Conversation History

| Date | Topic |
|------|-------|
| 2026-04-13 | Initial codebase documentation |
| 2026-04-14 | Fixed merge conflicts in Hairstyle.java, HairstyleRepository.java, StyleDetailActivity.java |
| 2026-04-14 | Updated AR try-on with graceful error handling |
| 2026-04-14 | Deleted unused drawable files (13 files removed) |
| 2026-04-14 | Added CircleImageView library, fixed ic_logo reference |
| 2026-04-15 | Security vulnerability analysis for presentation |
| 2026-04-15 | Project rescan - found SavedStylesActivity, updated asset status |
| 2026-04-15 | Complete project documentation updated |

---

## Dependencies (build.gradle.kts)

```kotlin
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
    }
    buildFeatures { viewBinding = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
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

    // CircleImageView
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // ARCore Sceneform
    implementation("com.gorisse.thomas.sceneform:sceneform:1.23.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
```

---

## Color Palette

| Name | Hex | Usage |
|------|-----|-------|
| bb_dark_green | #1A2E1A | Primary dark |
| bb_deep_green | #2D4A2D | Deep green |
| bb_medium_green | #3A5C3A | Medium green |
| bb_light_green | #4A7C4A | Light green |
| bb_gold | #C9A84C | Primary accent |
| bb_light_gold | #D4B870 | Light gold |
| bb_pale_gold | #E8D4A0 | Pale gold |
| bb_cream | #F5F0E8 | Cream |
| bb_text_primary | #FFFFFF | White text |
| bb_text_secondary | #B8C8B8 | Secondary text |
| bb_text_dark | #1A2E1A | Dark text |
| bb_text_muted | #7A9A7A | Muted text |
| bb_surface | #F5F0E8 | Surface color |
| bb_accent_cyan | #00E5FF | Accent cyan |

---

## Notes
- AR try-on requires ARCore installed, compatible device, and GLB hair model files
- 18 hairstyles total (8 Western + 10 Asian styles)
- 2 of 20 GLB model files exist (pompadour.glb, face_landmarker.task)
- All style image drawables needed
- Hair segmentation model (hair_segmenter.tflite) required for hair removal feature

---

## Hair Segmentation Feature (Implemented)

### Overview
MediaPipe HairSegmenter is used to detect and remove the user's original hair before placing the 3D hairstyle model in AR try-on.

### Architecture
```
TryOnActivity
    │
    ├── HairSegmentationHelper
    │   └── Uses: HairSegmenter (MediaPipe Tasks Vision)
    │   └── Model: models/hair_segmenter.tflite (512x512)
    │   └── Categories: 0=Background, 1=Hair
    │
    ├── OcclusionMesh (Fallback)
    │   └── Cube mesh with skin color
    │   └── Size: 0.32 x 0.08 x 0.20
    │
    └── ARCore Face Tracking
        └── 3D hair model placement
```

### Processing Flow
1. Camera frame captured
2. HairSegmenter processes frame (every 3rd frame)
3. Hair mask created (category 1 = hair)
4. Soft edge blur applied (alpha fade)
5. Skin color overlay applied to mask
6. Mask displayed on hairMaskOverlay ImageView
7. Cube mesh covers hair (fallback if segmentation fails)
8. 3D hair model rendered on top

### Key Components
| Component | Purpose |
|-----------|---------|
| HairSegmentationHelper | Initializes segmenter, processes frames, creates mask |
| hairMaskOverlay | ImageView displaying skin-colored hair mask |
| occlusionMesh | Cube mesh fallback when segmentation unavailable |
| tvSegmentationStatus | Shows segmentation status to user |
| tvHint | Guidance text for user |

### Segmentation Status Messages
- "Initializing hair segmentation..." - Startup
- "Hair segmentation ready" - Success
- "Segmentation unavailable, using fallback" - Fallback mode
- "Face detected! Processing..." - Face found
- "Hair covered! Try the hairstyle" - Ready

### Fallback Behavior
If HairSegmenter fails to initialize or model file is missing:
1. Segmentation disabled
2. Cube mesh occlusion applied (skin-colored)
3. Toast notification shown
4. AR try-on continues with visual fallback

### Model File Required
**Location:** `app/src/main/assets/models/hair_segmenter.tflite`
**Input:** 512x512 RGB
**Output:** Category mask (uint8)
**Categories:** 0=Background, 1=Hair
