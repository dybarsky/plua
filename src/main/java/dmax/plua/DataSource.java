package dmax.plua;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import dmax.plua.domain.Language;
import dmax.plua.domain.Link;
import dmax.plua.domain.Persistable;
import dmax.plua.domain.Word;
import dmax.plua.persist.Dao;
import dmax.plua.persist.DataBaseManager;
import dmax.plua.persist.dao.CloseableIterator;
import dmax.plua.persist.dao.DaoFactory;

/**
 * Main access and operations point with links and words. Uses links cache inside.
 * All operations will be done with database and cache.
 *
 * <br/><br/>
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 05.01.15 at 11:21
 */
public class DataSource {

    private Language language;
    private DataBaseManager dataBaseManager;
    private Dao<Word> dao;

    private List<Link> links;

    /**
     * Create data source.
     * @param language default language. Used in methods {@link #loadOriginalWord(dmax.plua.domain.Link)}
     * and {@link #loadTranslationWord(dmax.plua.domain.Link)}
     */
    @Inject
    public DataSource(DataBaseManager dataBaseManager, Language language) {
        this.dataBaseManager = dataBaseManager;
        this.language = language;
        this.dao = DaoFactory.createDao(Word.class);
    }

    /**
     * Set current default language. Used in methods {@link #loadOriginalWord(dmax.plua.domain.Link)}
     * and {@link #loadTranslationWord(dmax.plua.domain.Link)}
     */
    public void setSelectedLanguage(Language language) {
        this.language = language;
    }

    /**
     * Get current default language. Used in methods {@link #loadOriginalWord(dmax.plua.domain.Link)}
     * and {@link #loadTranslationWord(dmax.plua.domain.Link)}
     */
    public Language getSelectedLanguage() {
        return this.language;
    }

    /**
     * Save new words into database. Words should contain different language.
     * Link will be created and saved into database and cache.
     * @return false if error during saving occurred
     */
    public boolean addWords(Word word1, Word word2) {
        Link link = new Link();
        Dao<Link> linkDao = DaoFactory.createDao(Link.class);

        for (Word word : new Word[]{ word1, word2 }) {
            Word saved = dataBaseManager.insert(dao.setPersistable(word));
            if (!verifySaved(saved)) return false;
            link.setWord(saved);
        }
        link = dataBaseManager.insert(linkDao.setPersistable(link));
        if (!verifySaved(link)) return false;

        return links.add(link);
    }

    /**
     * Save words into database. This words should be loaded from database before (should contain id).
     * @return false if error during saving occurred
     */
    public boolean updateWords(Word word1, Word word2) {
        Word saved;

        for (Word word : new Word[]{ word1, word2 }) {
            if (word.getId() == -1) continue;
            saved = dataBaseManager.update(dao.setPersistable(word));
            if (saved == null) return false;
        }
        return true;
    }

    /**
     * Save link into database. This link should be loaded from database before (should contain id).
     */
    public void updateLink(Link link) {
        Dao<Link> linkDao = DaoFactory.createDao(Link.class);
        dataBaseManager.update(linkDao.setPersistable(link));
    }

    /**
     * Remove link and words from cache and database.
     * @param link link to be removed from cache and database
     * @param word1 word to be removed from database
     * @param word2 word to be removed from database
     */
    public void removeWords(Link link, Word word1, Word word2) {
        links.remove(link);

        Dao<Link> linkDao = DaoFactory.createDao(Link.class);
        Dao<Word> wordDao = DaoFactory.createDao(Word.class);

        dataBaseManager.delete(linkDao.setPersistable(link));
        dataBaseManager.delete(wordDao.setPersistable(word1));
        dataBaseManager.delete(wordDao.setPersistable(word2));
    }

    /**
     * Load word from database which corresponds to id from link and default language passed to constructor.
     * @param link link which contains id of requested word.
     */
    public Word loadOriginalWord(Link link) {
        return loadWord(link, language);
    }

    /**
     * Load word from database which corresponds to id from link and other language then default.
     * @param link link which contains id of requested word.
     */
    public Word loadTranslationWord(Link link) {
        return loadWord(link, Language.UKRAINIAN.equals(language)
                                ? Language.GERMAN
                                : Language.UKRAINIAN);
    }

    private Word loadWord(Link link, Language language) {
        Word target = new Word();
        target.setId(link.getWordId(language));
        target.setLanguage(language);
        return dataBaseManager.retrieve(dao.setPersistable(target));
    }

    /**
     * Return cached collection of links or load if cache not exists.
     * Collection is sorted by priority and updated time.
     */
    public List<Link> getLinks() {
        if (links == null) loadLinks();
        return links;
    }

    /**
     * Load links from database and cache them. Sort by priority.
     */
    private void loadLinks() {
        links = new LinkedList<Link>();
        Dao<Link> linkDao = DaoFactory.createDao(Link.class);
        CloseableIterator<Link> it = dataBaseManager.retrieveIterator(linkDao);

        while (it.hasNext()) links.add(it.next());
        it.close();

        Collections.shuffle(links);
    }

    /**
     * Clear cached links collection. Will be loaded again when call {@link #getLinks()}.
     */
    public void reset() {
        links = null;
    }


    private boolean verifySaved(Persistable persistable) {
        return persistable.getId() != -1;
    }

    //~

    /**
     * Used for collection sorting by priority and updated time.
     * After sorting at beginning of collection should be links with big priority value.
     * If priorities are same, link with less updated time should be before link with bigger time.
     * That means, link which was updated earlier (but has same priority) should be shown first.
     */
    static class PrioritySorter implements Comparator<Link> {

        @Override
        public int compare(Link lhs, Link rhs) {
            /*
            if priorities different: return <0 if right link priority less, >0 if lest priority less.
            if priorities same: return <0 if left updated time less, >0 if right updated time less.
            */
            int priorityDiff = rhs.getPriority() - lhs.getPriority();
            return priorityDiff != 0
                    ? priorityDiff
                    : (int) (lhs.getUpdated() - rhs.getUpdated());
        }
    }
}
