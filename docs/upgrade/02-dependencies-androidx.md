# 02 — Dependencies & AndroidX migration

## Current dependencies (`build.gradle`)

| Artifact | Version | Status |
|---|---|---|
| `com.android.support:support-v4` | 28.0.0 | **dead** (last release 2018) → AndroidX |
| `com.android.support:appcompat-v7` | 28.0.0 | dead → `androidx.appcompat:appcompat` |
| `com.android.support:cardview-v7` | 28.0.0 | dead → `androidx.cardview:cardview` |
| `com.android.support:design` | 28.0.0 | dead → `com.google.android.material:material` |
| `com.google.dagger:dagger` | 2.15 (2018) | very old |
| `com.google.dagger:dagger-compiler` (annotationProcessor) | 2.15 | very old; JDK 17+/AGP 9 compatibility not guaranteed |
| `javax.inject:javax.inject` | 1 | redundant — transitive of Dagger |
| `javax.annotation:jsr250-api` | 1.0 | not needed (no `@PostConstruct` etc. in code) |
| `junit:junit` (test) | 4.12 | → 4.13.2 |
| spots-dialog (`com.github.d-max`) | removed | vendored in `ui/dialog` ✓ |

No Kotlin, no kapt/KSP, no Hilt, no networking, no image loading, no analytics, no Play Services.

## Target set

> Versions below are the latest ones known to the author of this doc. **Look up the current release on
> [Google Maven](https://maven.google.com) / Maven Central when you apply this**, and prefer a version catalog.

```groovy
dependencies {
    implementation 'androidx.appcompat:appcompat:1.7.1'
    implementation 'androidx.fragment:fragment:1.8.9'         // transitive of appcompat; declare for fragment-ktx-free explicit control (optional)
    implementation 'androidx.cardview:cardview:1.0.0'
    implementation 'androidx.viewpager:viewpager:1.1.0'       // or androidx.viewpager2 (see 04)
    implementation 'androidx.coordinatorlayout:coordinatorlayout:1.3.0'
    implementation 'com.google.android.material:material:1.13.0'

    implementation 'com.google.dagger:dagger:2.60'
    annotationProcessor 'com.google.dagger:dagger-compiler:2.60'

    testImplementation 'junit:junit:4.13.2'
}
```
Use a `gradle/libs.versions.toml` version catalog if more dependencies are expected (Room, test libs).

## Support → AndroidX mapping for this code

Prefer **Android Studio ▸ Refactor ▸ Migrate to AndroidX** (updates Java imports, XML class names, and Gradle).
Verify afterwards with `grep -rn "android\.support" src build.gradle` → must be empty.

| Old | New |
|---|---|
| `android.support.v7.app.AppCompatActivity`, `ActionBar` | `androidx.appcompat.app.AppCompatActivity`, `androidx.appcompat.app.ActionBar` |
| `android.support.v4.app.Fragment` | `androidx.fragment.app.Fragment` |
| `android.support.v4.view.ViewPager`, `PagerAdapter` | `androidx.viewpager.widget.ViewPager`, `PagerAdapter` |
| `android.support.v7.widget.CardView` | `androidx.cardview.widget.CardView` |
| `android.support.v7.widget.Toolbar` | `androidx.appcompat.widget.Toolbar` |
| `android.support.design.widget.CoordinatorLayout` | `androidx.coordinatorlayout.widget.CoordinatorLayout` |
| `android.support.design.widget.AppBarLayout` | `com.google.android.material.appbar.AppBarLayout` |
| `android.support.design.widget.FloatingActionButton` | `com.google.android.material.floatingactionbutton.FloatingActionButton` |
| `android.support.design.widget.Snackbar` | `com.google.android.material.snackbar.Snackbar` |
| `android.support.annotation.StringRes/StyleRes` (in `SpotsDialog`) | `androidx.annotation.StringRes/StyleRes` |
| `@style/ThemeOverlay.AppCompat.*`, `Theme.AppCompat.Light.NoActionBar` | unchanged names (AppCompat is still AppCompat) |

Files touched: `MainActivity`, `AboutFragment`, `CardsFragment`, `LinkDetailFragment`, `CardsPagerAdapter`,
`CardPriorityManager`, `LanguageSwitcher`, `SpotsDialog`, layouts `a_main`, `f_cards`, `v_word_card` (CardView),
`v_spinner`/`v_language_item_*` (check), styles.

### Behavioral traps

1. **Material theme enforcement.** `FloatingActionButton`, `AppBarLayout`, `Snackbar` work under an AppCompat theme,
   but any `MaterialButton`/`TextInputLayout` you add (or Material's own dialogs) require a
   `Theme.Material3.*`/`Theme.MaterialComponents.*` parent. Minimal path: keep `Theme.AppCompat.Light.NoActionBar` and
   verify; better: switch `BaseTheme` to `Theme.Material3.Light.NoActionBar` and map `colorPrimary`/`colorSecondary`
   (the app uses `colorAccent` = pink → Material 3 uses `colorSecondary`/`colorTertiary`; FAB color will change).
   Expect visual diffs (FAB shape/elevation, Snackbar, Toolbar) — screenshot before/after.
2. **`app:elevation`, `app:borderWidth`, `app:rippleColor`** on the FAB still exist in Material but defaults differ
   (Material 3 FAB is rounded-square, different size).
3. **`AppBarLayout` + `Toolbar` inside a plain `LinearLayout`** (`a_main.xml`) — under targetSdk 35+ edge-to-edge this
   needs inset handling (see 03).
4. **`Fragment.getFragmentManager()`**, `onActivityCreated`, `setHasOptionsMenu` are deprecated in AndroidX Fragment
   (still compile, warnings). Fix in 04.
5. **ViewPager** (`androidx.viewpager`) is in maintenance mode; works. `PagerAdapter` with `POSITION_NONE` rebuild is used
   intentionally — if moving to ViewPager2 you must rewrite the adapter as `RecyclerView.Adapter` (04).
6. **Do not enable Jetifier.** All dependencies are source-owned; Jetifier exists only for third-party binaries that
   still reference `android.support`. There are none.

## Dagger 2.15 → 2.60

- API used is only: `@Component`, `@Module`, `@Provides`, `@Singleton`, `@Inject`, field injection into fragments/Activity,
  component builder with module instance (`DaggerDependenciesGraph.builder().dependenciesProvider(...)`).
  All of this is still supported in 2.60. The builder method name is derived from the module class name and
  stays `dependenciesProvider(...)`.
- Annotation processing is Java `annotationProcessor` — AGP 9 doesn't change that. Build must run on JDK 17+.
- Don't add `javax.annotation:jsr250-api`; Dagger doesn't need it. If a `@Generated` compile error appears with
  newer JDKs, add `compileOnly 'javax.annotation:javax.annotation-api:1.3.2'` as a last resort, not preemptively.
- Dagger release line: AGP 9 support landed with the release after 2.59.2 (per Dagger release notes mirrors). Use ≥ 2.59.2,
  latest 2.60 per dagger.dev.
- **Option:** the graph is two singletons. Dropping Dagger for a hand-rolled `Application`-scoped holder
  (`PluaApp.get().dataSource()`) removes the annotation processor and the per-Activity re-creation issue
  (ARCHITECTURE "Threading & lifecycle"). Decide separately; not required for the upgrade.

## Other cleanups

- Remove `javax.inject:javax.inject:1` explicit dependency (Dagger brings it) and `jsr250-api`.
- `junit` 4.13.2 fixes `ExpectedException`/TemporaryFolder CVEs; tests are trivial so no code change.
- For future tests: `androidx.test:core`, `androidx.test.ext:junit`, `androidx.test.espresso:espresso-core`,
  `org.robolectric:robolectric` — only add when writing those tests (05).
- Dependency verification: after migration run `./gradlew :dependencies --configuration releaseRuntimeClasspath` and
  confirm there is no `com.android.support` left.
