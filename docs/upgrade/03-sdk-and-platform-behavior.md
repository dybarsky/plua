# 03 — SDK levels & platform behavior changes

## Levels

| | Now | Proposed | Reason |
|---|---|---|---|
| compileSdk | 34 (not installed) | **36** (optionally 37) | installed platforms 36 / 37.0; AGP 9.5 max API 37; compile against newest to see deprecations |
| targetSdk | 34 | **36** | Google Play: new apps and app updates must target API 36 (deadline was 2026-08-31); existing apps ≥ 35 to remain available to new users on newer devices. Source: Google Play support page via search — **re-check [target-sdk requirements](https://developer.android.com/google/play/requirements/target-sdk)** before release |
| minSdk | 15 | **23** (floor 21) | AndroidX `appcompat 1.7`/`material 1.12+` need ≥ 21; some current AndroidX libs need 23. API 15–20 devices are < 0.1 % of the market. Product decision for the owner. |

Deleting API branches after the raise (minSdk 23):
- `CardStateSwitcher.java:49` ternary → always `Util.prepareCircularRevealTransition`; drop `Util.prepareAlphaTransition`
  if nothing else uses it.
- Remove `@TargetApi(LOLLIPOP)` (`Util.java:42`, `ProgressLayout.java:34`) and the 4-arg `ProgressLayout` guard.
- Fold `res/values-v21/{style,dimens}.xml` and `res/drawable-v21/dmax_spots_spot.xml` into `values/` / `drawable/`
  (the v21 variants are the "real" ones: `windowDrawsSystemBarBackgrounds`, `DialogSpotColor`).
- With minSdk ≥ 26 you could also drop the legacy PNG launcher icons (`ic_launcher*.png`) and keep only
  `mipmap-anydpi-v26`; with 23–25 you still need the PNGs.
- Vector drawables: `ic_*` PNGs in 5 densities can be replaced by VectorDrawables (optional, shrinks the app).

## Behavior changes that affect this app

Check each against the official behavior-change pages for the API level when implementing (not exhaustively verified here).

### Edge-to-edge (enforced for apps targeting 35+, no opt-out at 36)
`a_main.xml` = `LinearLayout` → `AppBarLayout`+`Toolbar` → content container; FAB bottom/end in `f_cards.xml`.
After targeting 35+, the window draws behind status/navigation bars: the Toolbar will sit under the status bar and the
FAB / bottom of the card pager under the navigation bar.
Fix: call `EdgeToEdge.enable(this)` (`androidx.activity`) in `MainActivity.onCreate` and apply insets —
either `android:fitsSystemWindows="true"` on the `AppBarLayout` + bottom padding on the container, or
`ViewCompat.setOnApplyWindowInsetsListener`. The detail and about fragments also have `EditText`/`WebView`
content: handle the IME inset (`windowSoftInputMode="adjustResize"` + `WindowInsetsCompat.Type.ime()`). The
`@style/AppTheme` v21 `windowDrawsSystemBarBackgrounds=true` plus `colorPrimaryDark` become irrelevant on 35+.
There is a dedicated `edge-to-edge` agent skill, but it targets Compose; for Views follow the Android guide.

### Predictive back (default on for apps targeting 36)
`MainActivity.onBackPressed()` is **not called** when predictive back is enabled; it also uses the wrong
`FragmentManager`. Remove the override: AppCompat + AndroidX Fragment handle back-stack pops natively. If custom
interception is needed (e.g. dismiss undo Snackbar) use `getOnBackPressedDispatcher().addCallback(...)`.
Add `android:enableOnBackInvokedCallback="true"` to `<application>` to opt in explicitly.

### Large screens / resizability (targetSdk 36)
On displays ≥ 600 dp the system ignores `screenOrientation`, `resizeableActivity`, aspect-ratio restrictions. The app
uses fixed dimens (`card_size 230dp`, `pager_margin_top/bottom 80/140dp`) and relies on
`configChanges="orientation|screenSize"` + a manual `onConfigurationChanged` in `CardsFragment`. On foldables /
tablets / desktop windowing, resizes also change `smallestScreenSize|screenLayout|density`, which currently
**recreate** the Activity (new Dagger graph, `Importer` may re-run, selected language lost). Either
declare `orientation|screenSize|smallestScreenSize|screenLayout|keyboardHidden|uiMode` or (better) remove
`configChanges` and make the screens recreation-safe (ViewModel/state saving). Constrain card width
(`maxWidth`) for tablets.

### Manifest hygiene
- Remove `package=`; keep `android:exported="true"` on the launcher activity ✓.
- Add `android:allowBackup` explicitly plus `android:dataExtractionRules` (API 31+) and `android:fullBackupContent`;
  decide whether the user's word DB should be backed up (probably yes). Lint flags this otherwise.
- `android:supportsRtl="true"`, `android:theme` on `<application>`, `android:enableOnBackInvokedCallback="true"`.
- No permissions are requested; keep it that way (Play data-safety form depends on it).
- `AboutFragment` starts `mailto:` / `market://` intents: on API 30+ `startActivity` is fine without `<queries>`, but
  wrap in `try/catch(ActivityNotFoundException)` (currently only the market intent is).

### Other platform changes worth a glance
- **Splash screen (API 31+):** the system now shows a splash automatically using the launcher icon; set
  `windowSplashScreenBackground`/use `androidx.core:core-splashscreen` for consistent look (optional).
- **Predictive back animations & `ViewPager`** are fine.
- **`Bundle.getSerializable(String)` deprecated (API 33)** → `BundleCompat.getSerializable(bundle, key, Word.class)` (04).
- **Resources.getDrawable(int)** deprecated → `ResourcesCompat.getDrawable` / `AppCompatResources` (`CardsFragment.java:155`).
- **Photo picker / notification permission / foreground services / exact alarms:** not applicable (none used).
- **16 KB page size** (required for native code on new Play uploads): no native libs → N/A.
- **WebView:** `file:///android_asset/` still works; for hardening move to `androidx.webkit.WebViewAssetLoader` (04).
- **Android 17 / API 37:** platform `android-37.0` is installed; read its behavior-changes page before moving
  `compileSdk`/`targetSdk` to 37. Not analyzed here. (Google had not published a dated Play requirement for 37 at the time of search.)

## Verification matrix

Run on emulators: API 23 (minSdk), API 34, API 36; phone portrait/landscape, a tablet/foldable (resizable) profile,
gesture navigation and 3-button navigation, dark mode (theme is light-only; make sure text stays readable if the system
forces dark: add `android:forceDarkAllowed="false"` or implement DayNight), 200 % font scale, Ukrainian locale.
