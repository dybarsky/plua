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

    int COLUMN_ID_INDEX = 0;
    int COLUMN_WORD_DATA_INDEX = 1;

    String SQL_WORD_TABLE_TEMPLATE = "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_WORD_DATA + " TEXT UNIQUE NOT NULL"
            + ")";

    String SQL_CREATE_POLISH_TABLE = "CREATE TABLE " + TABLE_POLISH + SQL_WORD_TABLE_TEMPLATE;

    String SQL_CREATE_UKRAINIAN_TABLE = "CREATE TABLE " + TABlE_UKRAINIAN + SQL_WORD_TABLE_TEMPLATE;

    String SQL_CREATE_LINK_TABLE = "CREATE TABLE " + TABlE_LINK + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_LINK_ORIGINAL + " INTEGER, "
            + COLUMN_LINK_TRANSLATION + " INTEGER,"
            + "FOREIGN KEY(original) REFERENCES pl(id), "
            + "FOREIGN KEY(translation) REFERENCES ua(id)"
            + ")";

    String SQL_SELECT_ALL = "SELECT * FROM ?";

    String SQL_SELECT_BY_ID = "SELECT * FROM ? WHERE " + COLUMN_ID + "=?";
}
