# Data model

## Domain (`dmax.plua.domain`)

| Class | Fields | Notes |
|---|---|---|
| `Language` | enum `POLISH`, `UKRAINIAN`; mutable `codeName` | `codeName` is the localized display name, set in `MainActivity.initLanguageCodeNames()`. `toString()` returns it (null before that). Adding a language means touching `Constants`, `WordDao`, `LinkDao`, `DataSource.loadTranslationWord`, `LanguageSwitcher`, `LinkDetailFragment`. |
| `Word` | `id` (-1 = unsaved), `data`, `language` | `equals` ignores id when either is -1. |
| `Link` | `id`, `priority`, `updated`, `EnumMap<Language, Long> map` | One Link = one translation pair: maps each language to the id of its Word row. |
| `Persistable` | `getId/setId` | Implemented by `Word`, `Link`; used by the generic DAO layer. |

The data model is hard-wired to **exactly two languages**: `DataSource.loadTranslationWord` returns "the other one"
using `UKRAINIAN ? POLISH : UKRAINIAN`.

## SQLite (`persist.Constants`, `DataBaseHelper`)

Database name `dictionary`, **version 1**, `onUpgrade` is empty.

```sql
CREATE TABLE ua   (id INTEGER PRIMARY KEY AUTOINCREMENT, data TEXT UNIQUE NOT NULL);
CREATE TABLE pl   (id INTEGER PRIMARY KEY AUTOINCREMENT, data TEXT UNIQUE NOT NULL);
CREATE TABLE link (id INTEGER PRIMARY KEY AUTOINCREMENT,
                   original    INTEGER,   -- in practice: id in `ua` (Language.UKRAINIAN)
                   translation INTEGER,   -- in practice: id in `pl` (Language.POLISH)
                   priority    INTEGER,
                   updated     INTEGER,   -- epoch millis
                   FOREIGN KEY(original)    REFERENCES pl(id),
                   FOREIGN KEY(translation) REFERENCES ua(id));
```

This is byte-for-byte the schema of the published 0.6 app (tables `ua`, `pl`, `link`), so installed databases need no
migration.

Gotchas:
- Column names are misleading: `original` is always the **Ukrainian** id and `translation` always the **Polish** id
  (`LinkDao.prepareContentValues`/`createLink`), regardless of which language the user is learning from.
- The foreign keys are crossed relative to actual usage: `original` is declared `REFERENCES pl(id)` but the DAO stores
  the Ukrainian id there, and `translation` is declared `REFERENCES ua(id)` but stores the Polish id. This is the schema
  the published app has always shipped. It is harmless only because SQLite foreign-key enforcement is off
  (`PRAGMA foreign_keys` is never turned on); turning it on would break inserts. Don't "fix" the DDL without a real
  migration.
- Commit `44bf59d` "Move to german" briefly renamed the Polish table to `de`; that was reverted (Polish is the app's
  second language again), so no installed database ever needs a `de` table.
- Word uniqueness is per table. `DataSource.addWords` inserts word1, then word2, then the link, with no transaction:
  if word2 is a duplicate, word1 stays as an orphan row.
- `retrieveIterator` loops use `!cursor.isLast() && getCount() != 0` — correct only because the cursor starts at -1.

## DAO layer

`Dao<T extends Persistable>` is an abstract, **stateful** builder: `setPersistable(entity)` / `setRetrieveId(id)` then
`DataBaseManager.insert|update|delete|retrieve|retrieveIterator(dao)` runs the operation against the open DB and calls
`dao.reset()`. `DaoFactory.createDao(Class)` returns a fresh `WordDao`/`LinkDao`; `DataSource` additionally keeps one
long-lived `Dao<Word>` (not thread-safe). `DataBaseManager` throws `IllegalStateException("DBManager not
initialized")` if `open()` wasn't called.

## Asset file `data`

Location `src/main/assets/data`, UTF-8, consumed by `importer.Importer`:

```
version|<int>                  line 1: asset version
<LANG1>|<LANG2>                line 2: Language enum names, e.g. UKRAINIAN|POLISH
<word in LANG1>|<word in LANG2>   lines 3..N: one pair per line, split on '|' (must be exactly 2 fields)
```

Behavior:
- If the version equals SharedPreferences `import`/`version`, nothing is imported. **To ship new words, bump the
  version in the file.** Re-import re-adds *all* lines; duplicates fail the unique constraint and are skipped
  (the `Importer` ignores the boolean result of `addWords`).
- Words are lower-cased before insertion (`toLowerCase()` with the default locale).
- Lines with ≠ 2 fields are silently ignored.
- Header language names go through `Language.valueOf` → an unknown name (e.g. `FRENCH`) throws
  `IllegalArgumentException` inside `doInBackground` and **crashes the app** (see KNOWN_ISSUES P0-1).
- The version is written only after the whole file is read without cancel/IOException.
- Parsing/inserting happens on the AsyncTask thread, one `addWords` (3 inserts, auto-commit) per line — slow for large
  files; wrap in a transaction when touching this.

State of the file: 481 lines, header `version|1` / `UKRAINIAN|POLISH`, one `Ukrainian|Polish` pair per line.

## Preferences

`SharedPreferences "import"` → `version` (int). Nothing else is persisted: the selected language is **not** saved
(resets to `Language.POLISH` in `DependenciesProvider.DEFAULT`).

## Static HTML assets

`about.html`, `help.html` (≈40 lines each), loaded by `AboutFragment` via `file:///android_asset/...` in a `WebView`.
