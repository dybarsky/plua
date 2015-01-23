package dmax.words.persist;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 11.12.14 at 12:48
 */
interface Constants {

    String DATABASE_NAME = "dictionary";
    int DATABASE_VERSION = 1;

    String TABLE_POLISH = "pl";
    String TABlE_UKRAINIAN = "ua";
    String TABlE_LINK = "link";

    String COLUMN_ID = "id";
    String COLUMN_WORD_DATA = "data";
    String COLUMN_LINK_ORIGINAL = "original";
    String COLUMN_LINK_TRANSLATION = "translation";
    String COLUMN_LINK_PRIORITY = "priority";
    String COLUMN_LINK_UPDATED = "updated";

    int COLUMN_ID_INDEX = 0;
    int COLUMN_WORD_DATA_INDEX = 1;
    int COLUMN_LINK_ORIGINAL_INDEX = 1;
    int COLUMN_LINK_TRANSLATION_INDEX = 2;
    int COLUMN_LINK_PRIORITY_INDEX = 3;
    int COLUMN_LINK_UPDATED_INDEX = 4;

    String SQL_WORD_TABLE_TEMPLATE = "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_WORD_DATA + " TEXT UNIQUE NOT NULL"
            + ")";

    String SQL_CREATE_POLISH_TABLE = "CREATE TABLE " + TABLE_POLISH + SQL_WORD_TABLE_TEMPLATE;

    String SQL_CREATE_UKRAINIAN_TABLE = "CREATE TABLE " + TABlE_UKRAINIAN + SQL_WORD_TABLE_TEMPLATE;

    String SQL_CREATE_LINK_TABLE = "CREATE TABLE " + TABlE_LINK + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_LINK_ORIGINAL + " INTEGER, "
            + COLUMN_LINK_TRANSLATION + " INTEGER, "
            + COLUMN_LINK_PRIORITY + " INTEGER, "
            + COLUMN_LINK_UPDATED + " INTEGER "
            + "FOREIGN KEY(original) REFERENCES pl(id), "
            + "FOREIGN KEY(translation) REFERENCES ua(id)"
            + ")";

    String SQL_SELECT_ALL_LINKS = "SELECT * FROM " + TABlE_LINK;

    String SQL_SELECT_ALL_POLISH = "SELECT * FROM " + TABLE_POLISH;

    String SQL_SELECT_ALL_UKRAINIAN = "SELECT * FROM " + TABlE_UKRAINIAN;

    String SQL_SELECT_BY_ID_LINK = "SELECT * FROM " + TABlE_LINK + " WHERE " + COLUMN_ID + " = ?";

    String SQL_SELECT_BY_ID_POLISH = "SELECT * FROM " + TABLE_POLISH + " WHERE " + COLUMN_ID + " = ?";

    String SQL_SELECT_BY_ID_UKRAINIAN = "SELECT * FROM " + TABlE_UKRAINIAN + " WHERE " + COLUMN_ID + " = ?";
}
