# Architecture

Snapshot of the working tree on 2026-11-17 (≈3.4k lines of Java, 170 commits of history).

## Overview

Single-module, single-Activity, fragment-based app. No MVP/MVVM framework: fragments talk directly to a `DataSource`
facade; persistence is a hand-rolled DAO layer over `SQLiteOpenHelper`. Dagger 2 wires two singletons.

```
MainActivity (AppCompatActivity, hosts Toolbar + FrameLayout-like container)
 ├─ CardsFragment        home screen: ViewPager of cards, FAB, toolbar spinner, undo Snackbar
 │    ├─ CardsPagerAdapter   one card View per Link; CardViewHolder holds both Words + views
 │    ├─ CardInteractionListener  touch → flip card;  click Forgot/Recalled → priority (spaced mode only)
 │    │    ├─ CardStateSwitcher   flip animation (circular reveal API21+, alpha fade below)
 │    │    └─ CardPriorityManager priority ±1 + go to next page
 │    ├─ LanguageSwitcher    custom ActionBar spinner (Polish / Ukrainian = "original" side)
 │    └─ Importer (AsyncTask) loads assets/data into DB, shows SpotsDialog
 ├─ LinkDetailFragment   add / edit a word pair (back stack, slide_up animation)
 ├─ AboutFragment        help / rate / feedback / about (WebView over assets/*.html)
 └─ SettingsFragment     settings list (one item: order mode random / spaced repetition), saved via `Settings`
```

## Packages (`src/main/java/dmax/plua`)

| Package | Role |
|---|---|
| `dmax.plua` | `DataSource` — the facade used by UI. Caches `List<Link>` (loaded once, shuffled), maps Link→Word, add/update/remove. |
| `di` | `DependenciesGraph` (`@Singleton @Component`) and `DependenciesProvider` (`@Module`, takes a `Context`). |
| `domain` | `Word`, `Link`, `Language` (enum POLISH, UKRAINIAN), `Persistable`. POJOs, `Serializable`, value-ish `equals`. |
| `persist` | `DataBaseHelper` (SQLiteOpenHelper), `DataBaseManager` (open/close + generic CRUD via a `Dao`), `Dao<T>` base, `Constants` (SQL + indexes). |
| `persist.dao` | `WordDao`, `LinkDao` (package-private), `DaoFactory`, `CloseableIterator` (cursor wrapper). |
| `importer` | `Importer` — reads `assets/data`, inserts pairs, remembers imported version in SharedPreferences `import`. |
| `ui` | `MainActivity`, `Util` (animations, toast). |
| `ui.cards` | Everything on the home screen (see above). |
| `ui.detail` | `LinkDetailFragment`. |
| `ui.about` | `AboutFragment`. |
| `ui.dialog` | **Vendored copy of the `d-max/spots-dialog 1.1` library** (`SpotsDialog`, `ProgressLayout`, `AnimatedView`, `AnimatorPlayer`, `HesitateInterpolator`) — added because JCenter is gone. Uncommitted. Resources: `attrs.xml` (`Dialog` styleable), `v_spots_layout.xml`, `dmax_spots_spot.xml` (+ `drawable-v21`), styles `Dialog`/`SpotsDialogDefault`. |

## Dependency injection

- `MainActivity.onCreate` builds `DaggerDependenciesGraph` with `new DependenciesProvider(getApplicationContext())`
  and exposes it with `getGraph()`.
- Fragments inject themselves: `((MainActivity) getActivity()).getGraph().inject(this)` in `onActivityCreated`.
  Injectable members: `MainActivity.database`, `CardsFragment.dataSource`, `LinkDetailFragment.dataSource`.
- Provided: `DataSource(DataBaseManager, Language.POLISH)` and `DataBaseManager(Context)`, both `@Singleton`
  (scope = lifetime of the component, which is the lifetime of the **Activity instance**, not the process).
- Dagger runs as a Java annotation processor (`annotationProcessor 'com.google.dagger:dagger-compiler:2.15'`);
  generated class `DaggerDependenciesGraph` is referenced from `MainActivity`.

## Key runtime flows

**Startup** — `MainActivity.onCreate`: set layout → set localized `Language.codeName`s (static mutable state!) → build
graph → `database.open()` (`getWritableDatabase`, main thread) → add `CardsFragment` if `savedInstanceState == null`.
`onDestroy` closes the DB helper.

**Cards screen** — `CardsFragment.onActivityCreated`: sets the Toolbar as support action bar, injects, builds
`CardInteractionListener`/`LanguageSwitcher`/`CardsPagerAdapter`, calls `showCards()` (hides empty view only if
`adapter.getCount() > 0`), then `new Importer(...).execute(this)`. When import ends, `onDatabaseUpdated()` → `reload()`
(`dataSource.reset()` + `showCards()`) and `invalidateOptionsMenu()`.

**Card flip** — `CardsPagerAdapter.instantiateItem` inflates `v_word_card`, fills a `CardViewHolder`
(`originalWord` = word in the *selected* language, `translationWord` = other language) and sets the touch listener.
`ACTION_UP` → `CardStateSwitcher.switchCardState` swaps the visible side group with a circular reveal (API 21+) or alpha.

**Language switch** — toolbar spinner → `CardsFragment.updateLanguage` → `DataSource.setSelectedLanguage` →
`CardsPagerAdapter.onLanguageChanged` swaps original/translation words in all live card holders.

**Delete + undo** — menu `delete` → collapse animation → `PageRemover` (an `AnimatorListenerAdapter` +
`OnPageChangeListener`) pages to the neighbour first, then `DataSource.removeWords(...)`, shows a Snackbar `Undo`
that re-adds via `DataSource.addWords` (new row ids). State machine is in `RemoveTransaction` (`inProgress` guard).

**Add / edit** — FAB or menu `edit` → `LinkDetailFragment` (replace container, back stack). Edit mode is selected by
`Bundle` args holding serialized `Word`s. Save: `addWords` or `updateWords` (unchanged word → id `-1` → skipped).
Failure toast `saving_error` = "Word exists" (unique constraint).

**Import** — see [DATA_MODEL.md](DATA_MODEL.md#asset-file-data).

## UI notes

- Theme `AppTheme` → `BaseTheme` → `Theme.AppCompat.Light.NoActionBar`; indigo/pink palette in `colors.xml`.
- Layout is portrait-oriented with fixed dimens (`pager_margin_top 80dp`, `pager_margin_bottom 140dp`,
  `card_size 230dp`), overridden in `values-land`, `values-uk`, `values-v21`. `onConfigurationChanged` in
  `CardsFragment` patches margins manually because the Activity declares `configChanges="orientation|screenSize"`.
- Flags are mipmaps (`ic_polish`, `ic_ukrainian`); toolbar icons are PNGs in all densities (no vector drawables).
- Order mode (`settings.OrderMode`, stored by `settings.Settings` in shared prefs `settings`, provided by Dagger):
  `RANDOM` (default) shuffles links when loaded; `SPACED` shuffles then sorts with `DataSource.PrioritySorter`
  (priority desc, updated asc). `DataSource.getLinks()` reloads when the saved mode differs from the cached one, so the
  Settings screen only has to save the value. The Forgot/Recalled buttons in `v_word_card` are `gone` unless `SPACED`.

## Tests

`src/test/java/dmax/plua`: `SorterTest` (PrioritySorter), `settings/OrderModeTest`, `domain/LinkEqualsTest`, `domain/WordEqualsTest`.
JUnit 4.12, pure JVM, no Android dependencies. Nothing covers persistence, importer or UI.

## Threading & lifecycle (important for refactors)

- DB open, all DAO calls, `DataSource` cache reads: **main thread**.
- `Importer.doInBackground` calls `dataSource.addWords` **off the main thread** while the UI may read the same
  non-thread-safe `LinkedList` (`DataSource.links`) and the same shared `Dao<Word>` instance (stateful: holds the
  "current persistable"). Works in practice only because the import is short and mostly happens before cards exist.
- `Importer` holds an Activity `Context` + dialog; no cancellation on Activity destroy.
- A new Dagger graph (new `DataBaseManager`, new `DataSource`) is created each time the Activity is created.
