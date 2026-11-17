# 05 — R8, lint, tests, release

## R8 / ProGuard (`proguard-android.txt`)

Current rules (copy of the old SDK `proguard-android.txt` + 2 custom lines) and release block
`minifyEnabled true`, `shrinkResources false`. Findings:

| Rule | Verdict |
|---|---|
| `-dontoptimize`, `-dontpreverify` | remove: disables R8 optimizations (allowed in an *app* module, but pointless). AGP 9 also stopped supporting `getDefaultProguardFile('proguard-android.txt')` precisely because of `-dontoptimize`. |
| `-dontobfuscate` | decide: the app has no reflection; obfuscation is safe and shrinks dex. If enabled, upload `mapping.txt` to Play Console (AAB uploads include it automatically via the Gradle plugin). Keep it off only if you want readable crash stacks without mapping. |
| `-dontwarn android.support.**` | delete after AndroidX migration |
| `-keep class dmax.dialog.** { *; }` | stale (library vendored into `dmax.plua.ui.dialog`); not needed — no reflection on it |
| `-keepclassmembers public class * extends android.view.View { void set*(***); *** get*(); }` | needed for `ObjectAnimator` property animation on `AnimatedView.xFactor` (`setXFactor/getXFactor`) and `scaleX/Y`, `alpha`, `rotation`, `y` — **but** `ObjectAnimator.ofFloat(animatedView, "xFactor", …)` uses reflection on the custom view → keep a targeted rule: `-keepclassmembers class dmax.plua.ui.dialog.AnimatedView { *** get*(); void set*(***); }` |
| `-keepclassmembers class **.R$* { public static <fields>; }` | not needed with modern AGP |
| enum / Parcelable / native / Activity `onClick` rules | covered by AGP's default rules; keep none locally |
| `-keepattributes *Annotation*` | Dagger generates code at compile time; not needed at runtime |

Recommended `proguard-rules.pro` (replace the file; `getDefaultProguardFile('proguard-android-optimize.txt')` in
Gradle supplies the rest):
```
# Property animation by name via reflection
-keepclassmembers class dmax.plua.ui.dialog.AnimatedView {
    public float getXFactor();
    public void setXFactor(float);
}
# Serializable payloads passed in Bundles (same-process only, but keep names stable for crash reports)
-keepnames class dmax.plua.domain.** { *; }
```
`Language.valueOf(String)` on asset header names: the enum's `name()` strings are baked into the constants and R8's
default rules keep `values()`/`valueOf()`, so `POLISH`/`UKRAINIAN` keep resolving. Still, verify with a release build on
a clean install — the importer is the one reflective-ish path.

Enable `shrinkResources true` (works with minify). Check `build/outputs/mapping/release/` for `configuration.txt`
and `usage.txt`. AGP 9 sets `android.proguard.failOnMissingFiles=true`, so file names must exist.

## Lint

- Config: `lint { abortOnError false; checkReleaseBuilds true; htmlReport true; xmlReport false }` (rename to the new
  DSL form, 01). Lint **never fails the build today** — keep it that way until a baseline exists.
- Run `./gradlew lintDebug` once after AndroidX migration and create a baseline:
  `lint { baseline = file('lint-baseline.xml') }` then fix new issues only.
- Expect findings: `MissingPermission` none; `GoogleAppIndexingWarning`; `HardcodedText`; `RtlHardcoded`
  (layouts use `left/right` — the pager uses `layout_alignParentLeft` etc.); `UnusedResources`
  (`forgot`, `remember`, `yellow`, `separator`, `pink_light` partly, mipmap flags); `ObsoleteSdkInt` (after minSdk raise);
  `IconMissingDensityFolder` (`drawable-xxxhdpi` only has 2 icons); `AllowBackup`; `Overdraw`;
  `SetJavaScriptEnabled` (not used); `UnusedAttribute`; `InflateParams` (`LayoutInflater.inflate(…, null)`).
- Translations: `values-uk/strings.xml` should contain every translatable string in `values/strings.xml` (check with
  lint `MissingTranslation`).

## Tests

| Layer | Today | Add |
|---|---|---|
| JVM unit | `SorterTest`, `LinkEqualsTest`, `WordEqualsTest` (JUnit 4.12) | importer parser, `DataSource` (Robolectric or extracted logic) |
| Instrumented | none | DAO/DB test on device/emulator: create → add words → remove → re-add (undo), duplicate word, migration test if Room is introduced |
| UI | none | Espresso smoke: first-launch import → card visible → tap flips → add word → delete+undo |

Commands: `./gradlew testDebugUnitTest`, `./gradlew connectedDebugAndroidTest`.
The agent skills `testing-setup` and `ui-test` (adb-driven) can bootstrap this. The repo's three tests need no changes
for the toolchain upgrade — JUnit 4.12 → 4.13.2 is binary compatible.

## Release checklist

1. `versionCode` must be > the last Play upload (HEAD says 7; working tree 8 / name 0.7 — confirm against Play Console).
2. `./gradlew bundleRelease` → AAB (Play requires AAB for new uploads) signed with `~/.android/dmax.keystore`
   (alias `dmax`). If the keystore is lost, enroll in Play App Signing / key reset.
3. Target API requirement (03) satisfied; Data-safety form says "no data collected" **and PRIVACY.md is rewritten**
   to match (current text claims IP/location/analytics collection and third-party sharing — false for this app).
4. Test the upgrade path from the *currently published* version: install the Play build, add/delete some words,
   then install the new build over it. Check: DB opens, `import` pref prevents or re-runs the import as intended,
   no crash on `Language.valueOf`.
5. Update README (remove or implement spaced repetition claim; badges/links), `about.html` version text if any.
6. Screenshots for Play listing after edge-to-edge/Material changes.
7. Confirm `assets/data` header and content (`UKRAINIAN|POLISH` header, Polish words) and bump its `version|N` if the word list changed.
8. Store listing: confirm supported devices after minSdk change (API 23+).

## CI (optional)

GitHub Actions: `actions/setup-java@v4` (temurin 21), `gradle/actions/setup-gradle`, then
`./gradlew testDebugUnitTest lintDebug assembleDebug`. Release signing on CI via secrets + `providers.environmentVariable`.
