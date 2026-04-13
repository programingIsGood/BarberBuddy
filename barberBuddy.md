# BarberBuddy - Persistent Memory

## Project Overview
BarberBuddy is an Android app that analyzes users' face shapes using MediaPipe's FaceLandmarker and provides personalized hairstyle recommendations using fuzzy logic membership functions.

**Tech Stack:**
- Kotlin/Java (Android)
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

## Activities (Navigation Flow)
1. **SplashActivity** → Checks if onboarded, shows logo, navigates to OnboardingActivity or MainActivity
2. **OnboardingActivity** → Welcome screen with "Find Your Perfect Cut" headline, Get Started/Skip buttons
3. **MainActivity** → Face scanning with CameraX, face detection, capture button
4. **ResultsActivity** → Shows detected face shape with confidence %, shape description, stats, and browse button
5. **RecommendationsActivity** → Grid of hairstyle cards, bottom nav (Scan/Saved/Profile)
6. **StyleDetailActivity** → Full hairstyle details, save/bookmark, try-on button, share
7. **ProfileActivity** → Shows last scan, scan count, saved count, reset profile
8. **TryOnActivity** → ARCore face tracking with 3D GLB hair model overlay

---

## Core Java Classes

### Models
- **Hairstyle.java** - Data class with id, name, imageRes, overlayRes, suitableFaceShapes, description, maintenanceLevel, maintenanceTips, trend, modelPath (GLB path)
- **HairstyleRepository.java** - Static repository with 8 predefined hairstyles (Textured Crop, Pompadour, Buzz Cut, Side Part, Quiff, Undercut, Caesar, Slick Back). Methods: getRecommendations(), getForFaceShape(), getById(), getAll()

### Face Analysis
- **FaceAnalyzer.java** - CameraX ImageAnalysis.Analyzer that uses MediaPipe FaceLandmarker in LIVE_STREAM mode. Converts camera frames to Bitmap → MPImage, calls faceLandmarker.detectAsync()
- **FaceShapeAnalyzer.java** - Analyzes 478-point face landmarks using fuzzy logic:
  - Computes 5 ratios: R1(cheek/height), R2(jaw/cheek), R3(forehead/cheek), R4(jaw/forehead), R5(elongation)
  - Trapezoidal membership functions for: Oval, Round, Square, Heart, Diamond, Oblong, Triangle
  - Returns FaceShapeResult with primaryShape, confidence, allMemberships (sorted), ratios, explanation
  - Landmark indices: RIGHT_CHEEK=50, LEFT_CHEEK=280, CHIN=152, JAW_EDGE=288, etc.

### UI/Adapters
- **HairstyleAdapter.java** - RecyclerView adapter for hairstyle cards. Uses Glide for image loading, click listener for navigation
- **LandmarkOverlayView.java** - Custom View that draws face oval (34 oval indices) and landmark dots on camera preview. Handles scale/offset/mirror for front camera

### Data/Services
- **SavedStylesManager.java** - SharedPreferences wrapper. save(), remove(), isSaved(), getSavedStyles() using PREFS="saved_styles", KEY="saved_ids"
- **Prefs.java** - Helper for "onboarded" shared preference check

### AR Try-On
- **TryOnActivity.java** - ARCore + Sceneform face tracking. Loads GLB models from assets/models/. Shows error if model file not found: "Error: No AR function available for this hairstyle. Wait for future updates."

---

## Layouts (Key)
- **activity_main.xml** - Camera preview with PreviewView, LandmarkOverlayView, scan oval frame, instruction text, capture button, upload action
- **activity_results.xml** - Face shape header (green card), description card, stats row (confidence, styles found, trending), shape match analysis card, browse button, retry link
- **activity_recommendations.xml** - RecyclerView grid with BottomNavigationView
- **item_hairstyle.xml** - MaterialCard with image, match % badge, bookmark button, name, trend chip, maintenance text
- **activity_style_detail.xml** - Full style info, save/share/try-on buttons
- **activity_try_on.xml** - AR fragment container, loading progress, back button
- **activity_profile.xml** - Stats display, scan button, reset button, bottom nav
- **activity_splash.xml** - Logo/brand splash screen

---

## Navigation Flow
```
Splash → [onboarded?] 
    → NO: OnboardingActivity → MainActivity
    → YES: OnboardingActivity → MainActivity
          ↓
    MainActivity (scan face) → ResultsActivity
          ↓                           ↓
    RecommendationsActivity ←──────────┘ (Browse button)
          ↓
    StyleDetailActivity → TryOnActivity (AR with GLB models)
          ↓
    ProfileActivity (via bottom nav)
```

---

## Data Models

### Face Shapes (detected by fuzzy logic)
- Oval, Round, Square, Heart, Oblong, Diamond, Triangle
- Each has description in ResultsActivity.DESCRIPTIONS map

### Hairstyles (8 total with AR model paths)
1. Textured Crop - models/textured_crop.glb - Round, Square, Triangle, Diamond - Low - Trending
2. Classic Pompadour - models/pompadour.glb - Round, Oblong, Heart, Diamond - High - Classic
3. Buzz Cut - models/buzz_cut.glb - Oval, Square, Oblong, Diamond - Low - Timeless
4. Side Part - models/side_part.glb - Oval, Square, Heart, Triangle - Medium - Classic
5. Modern Quiff - models/quiff.glb - Oval, Round, Heart, Triangle - Medium - Trending
6. Undercut - models/undercut.glb - Oval, Square, Oblong, Diamond - Medium - Trending
7. Caesar Cut - models/caesar.glb - Oval, Round, Square, Triangle - Low - Classic
8. Slick Back - models/slick_back.glb - Oval, Oblong, Heart, Diamond - Medium - Classic

---

## Resources Created
- **Overlay Drawables**: hair_crop_overlay.xml, hair_pomp_overlay.xml, hair_buzz_overlay.xml, hair_sidepart_overlay.xml, hair_quiff_overlay.xml, hair_undercut_overlay.xml, hair_caesar_overlay.xml, hair_slick_overlay.xml

---

## Search History
- Initial codebase scan completed: All Java, KTS, XML files analyzed
- Merge conflict fixes completed (2026-04-14)

## Conversation History
- 2026-04-13: Initial codebase documentation
- 2026-04-14: Fixed merge conflicts, updated AR try-on with graceful error handling

## Notes
- AR try-on requires: ARCore installed, device compatible, GLB hair model files in assets/models/
- If model file missing, shows user-friendly error instead of crash
- TryOnOverlayView.java deleted (not needed for Sceneform AR approach)
