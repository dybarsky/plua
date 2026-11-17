# 04 — Code modernization

Two tiers. **Tier A** is behavior-preserving cleanup that should accompany the upgrade (deprecations, correctness).
**Tier B** is optional re-architecture; do it only as separate, deliberate projects.

## Tier A — deprecated / fragile APIs found in the code

| Location | Problem | Replacement |
|---|---|---|
| `importer/Importer.java:40` `extends AsyncTask` | `AsyncTask` deprecated since API 30; holds Activity context; ignores errors | `ExecutorService`/`Executor` + main-thread `Handler`, or (Tier B) coroutines + ViewModel. Keep behavior: show dialog, import in background, then call `Callback.onDatabaseUpdated()` on the main thread. Add `try/catch` around `Language.valueOf`, wrap inserts in one `db.beginTransaction()`, cancel on `onDestroyView`. |
| `CardsFragment.java:243,252` `getFragmentManager()` | deprecated | `getParentFragmentManager()` (or `requireActivity().getSupportFragmentManager()`) |
| `MainActivity.java:66` `onBackPressed` | wrong FragmentManager; dead under predictive back | delete override (see 03) |
| `CardsFragment.java:73`, `LinkDetailFragment.java:67` `onActivityCreated` | deprecated since Fragment 1.3 | do view setup in `onViewCreated`; get the Activity/graph in `onAttach`/`onCreate`. Note `CardsFragment` relies on the Activity layout (`R.id.toolbar`) being ready → `onViewCreated` is fine. |
| `setHasOptionsMenu(true)` + `onCreateOptionsMenu`/`onOptionsItemSelected` in 3 fragments | deprecated (Fragment 1.5+) | `requireActivity().addMenuProvider(MenuProvider, getViewLifecycleOwner(), State.RESUMED)` |
| `LinkDetailFragment.java:101` `getSerializable(key)` | deprecated API 33 | `BundleCompat.getSerializable(args, KEY, Word.class)` or make `Word` `Parcelable` |
| `CardsFragment.java:155` `Resources.getDrawable(int)` | deprecated | `ResourcesCompat.getDrawable(res, id, theme)` |
| `CardsFragment.java:293,343` `Snackbar.setCallback` | deprecated | `addCallback(BaseTransientBottomBar.BaseCallback)` |
| `SpotsDialog extends android.app.AlertDialog` | framework dialog; theme `android:Theme.DeviceDefault.Light.Dialog` | OK to keep; or `androidx.appcompat.app.AlertDialog`/Material progress dialog. Also called from `AsyncTask` thread-start path. |
| `AboutFragment` WebView `file:///android_asset` | works, but unmanaged | `WebViewAssetLoader` + `shouldInterceptRequest`; keep JS disabled (it is) |
| `AboutFragment.sendFeedback()` `:92` | unguarded `startActivity` | catch `ActivityNotFoundException` → toast |
| `Language.codeName` static setter | mutable static state | `Language.getLabel(Context)` using a `@StringRes` field |
| `DataSource` shared `Dao<Word>` + non-thread-safe `LinkedList` | race with importer thread | create DAOs per call (like `Link` does) and confine `links` to the main thread, or synchronize |
| `Link`/`Word` `Serializable` in Bundles | works | fine for now; use `Parcelable` if moving to Navigation |
| `res/values-v21`, `drawable-v21`, `Build.VERSION` checks | dead after minSdk 23 | fold in (03) |
| Switch-on-R rewrite | done | keep `if` chains (non-final R) — and fix `!= R.id.save` (P0-2) |
| Java level | implicit | `sourceCompatibility 17` (01) allows `var`-free modern syntax, lambdas, method refs — optional to refactor listeners |

Suggested commits: (1) Fragment APIs, (2) MenuProvider, (3) Importer executor + transaction, (4) misc deprecations,
(5) thread-safety of `DataSource`.

## Tier B — optional modernization (pick consciously)

| Idea | Benefit | Cost / notes |
|---|---|---|
| **ViewModel + LiveData/Flow** for cards state | survives configuration changes, removes `configChanges`, per-Activity graph and `CardViewHolder` state hacks | moderate; do before ViewPager2/Compose |
| **ViewPager2** + `RecyclerView.Adapter` | maintained pager, vertical/RTL, DiffUtil instead of `POSITION_NONE` | rewrite `CardsPagerAdapter`, `PageRemover`, `CardPriorityManager.showNextCard`, flip logic |
| **Room** instead of hand-rolled DAO/`Dao<T>` stack | schema-checked, migrations, transactions, background execution | **must migrate existing installs**: DB v1 with misleading FKs (`pl(id)`) and `original`=UA/`translation`=PL columns. Room validates the schema exactly → write `Migration(1,2)` that recreates `link` and copies data (and test with a DB captured from a real old install). Don't rename tables blindly. |
| **Kotlin** | null-safety, coroutines | AGP 9 has built-in Kotlin: *don't* add `org.jetbrains.kotlin.android`; use the built-in support (see `agp-9-upgrade` skill, `migrate-to-built-in-kotlin.md`). Convert incrementally (domain/persist first). |
| **Jetpack Compose** (Material 3) | replaces XML, fragments, ViewPager, custom dialog | an agent skill `migrate-xml-views-to-jetpack-compose` exists. UI is small (3 screens) → feasible; do after ViewModel. Navigation 3 skill also available. |
| **Re-enable spaced repetition** | README feature currently dead | product decision: restore forgot/recalled buttons in `v_word_card`, sort with `PrioritySorter` instead of `shuffle`, persist selected language |
| **Persist selected language / progress** | UX | `SharedPreferences`/DataStore |
| **Localization** | Polish/Ukrainian app; only `en` + `uk` UI strings | add `values-pl` if wanted; per-app locale (API 33+) |
| **Dependency injection removal** | less build machinery | `Application`-scoped holder, see 02 |

## Test-first guidance for Tier A

Add JVM tests around anything you rewrite and can't eyeball:
- `Importer` parsing: extract the line-parsing logic (header, pair splitting, case-folding) into a pure class and
  unit-test it with the asset format in DATA_MODEL.md.
- `DataSource` against an in-memory/Robolectric SQLite for add/remove/undo semantics.
Existing tests only cover `PrioritySorter`, `Link.equals`, `Word.equals` and don't need changes.
