package dmax.words;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import dmax.words.domain.Language;
import dmax.words.domain.Link;
import dmax.words.domain.Word;
import dmax.words.persist.Dao;
import dmax.words.persist.DataBaseManager;
import dmax.words.persist.dao.DaoFactory;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 05.01.15 at 11:21
 */
public class DataSource {

    private Language language;
    private DataBaseManager dataBaseManager;
    private Dao<Word> dao;

    private List<Link> links;

    public DataSource(DataBaseManager dataBaseManager, Language language) {
        this.dataBaseManager = dataBaseManager;
        this.language = language;
        this.dao = DaoFactory.createDao(Word.class);
    }

    public void setSelectedLanguage(Language language) {
        this.language = language;
    }

    public Language getSelectedLanguage() {
        return this.language;
    }

    public void addWords(Word word1, Word word2) {
        Link link = new Link();
        Dao<Link> linkDao = DaoFactory.createDao(Link.class);

        link.setWord(dataBaseManager.insert(dao.setPersistable(word1)));
        link.setWord(dataBaseManager.insert(dao.setPersistable(word2)));
        link = dataBaseManager.insert(linkDao.setPersistable(link));

        links.add(link);
    }

    public void updateWords(Word word1, Word word2) {
        if (word1.getId() != -1) {
            dataBaseManager.update(dao.setPersistable(word1));
        }
        if (word2.getId() != -1) {
            dataBaseManager.update(dao.setPersistable(word2));
        }
    }

    public void removeWords(Link link, Word word1, Word word2) {
        links.remove(link);

        Dao<Link> linkDao = DaoFactory.createDao(Link.class);
        Dao<Word> wordDao = DaoFactory.createDao(Word.class);

        dataBaseManager.delete(linkDao.setPersistable(link));
        dataBaseManager.delete(wordDao.setPersistable(word1));
        dataBaseManager.delete(wordDao.setPersistable(word2));
    }

    public Word loadOriginalWord(Link link) {
        return loadWord(link, language);
    }

    public Word loadTranslationWord(Link link) {
        return loadWord(link, Language.UKRAINIAN.equals(language)
                                ? Language.POLISH
                                : Language.UKRAINIAN);
    }

    private Word loadWord(Link link, Language language) {
        Word target = new Word();
        target.setId(link.getWordId(language));
        target.setLanguage(language);
        return dataBaseManager.retrieve(dao.setPersistable(target));
    }

    public List<Link> getLinks() {
        if (links == null) loadLinks();
        return links;
    }

    private void loadLinks() {
        links = new LinkedList<Link>();
        Dao<Link> linkDao = DaoFactory.createDao(Link.class);
        Iterator<Link> it = dataBaseManager.retrieveIterator(linkDao);

        while (it.hasNext()) links.add(it.next());

        Collections.sort(links, new Randomizer());
    }

    public void reset() {
        links = null;
    }

    //~

    private static class Randomizer implements Comparator<Link> {

        @Override
        public int compare(Link lhs, Link rhs) {
            return lhs.hashCode() - rhs.hashCode();
        }
    }
}
