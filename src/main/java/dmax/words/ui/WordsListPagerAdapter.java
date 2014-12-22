package dmax.words.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActionBar;
import android.content.Context;
import android.content.res.Resources;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import dmax.words.R;
import dmax.words.domain.Language;
import dmax.words.domain.Link;
import dmax.words.domain.Word;
import dmax.words.persist.Dao;
import dmax.words.persist.DataBaseManager;
import dmax.words.persist.dao.DaoFactory;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 18.12.14 at 12:38
 */
public class WordsListPagerAdapter extends PagerAdapter {

    private Context context;
    private CardStateSwitcher switcher;
    private DataSource dataSource;

    public WordsListPagerAdapter(Context context, DataBaseManager db, Language defaultLanguage) {
        this.context = context;
        this.dataSource = new DataSource(db, defaultLanguage);
        this.switcher = new CardStateSwitcher();
    }

    @Override
    public int getCount() {
        return dataSource.getLinks().size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.findViewById(R.id.card).getTag().equals(object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View root = View.inflate(context, R.layout.v_wordslist_item, null);

        CardView card = (CardView) root.findViewById(R.id.card);
        CardViewHolder holder = new CardViewHolder();
        holder.originalViewGroup = card.findViewById(R.id.original);
        holder.translationViewGroup = card.findViewById(R.id.translation);
        holder.originalTextView = (TextView) card.findViewById(android.R.id.text1);
        holder.translationTextView = (TextView) card.findViewById(android.R.id.text2);

        holder.originalViewGroup.bringToFront();

        // TODO reimplement
        holder.link = dataSource.getLinks().get(position);
        holder.originalWord = dataSource.loadOriginalWord(holder.link);
        holder.translationWord = dataSource.loadTranslationWord(holder.link);
        holder.originalTextView.setText(holder.originalWord.getData());

        card.setTag(holder);
        card.setOnTouchListener(switcher);
        container.addView(root);

        return holder;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = container.findViewWithTag(object);
        container.removeView(view);
    }

    @Override
    public void notifyDataSetChanged() {
        dataSource.reset();
        super.notifyDataSetChanged();
    }

    //~

    private static class CardViewHolder {

        boolean isTranslationState;

        Link link;
        Word originalWord;
        Word translationWord;

        View originalViewGroup;
        View translationViewGroup;
        TextView originalTextView;
        TextView translationTextView;
    }

    private static class CardStateSwitcher implements View.OnTouchListener {

        @Override
        public boolean onTouch(final View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                CardView card = (CardView) v;
                final CardViewHolder holder = (CardViewHolder) v.getTag();

                View viewGroup;
                TextView textView;
                String text;
                if (holder.isTranslationState) {
                    viewGroup = holder.originalViewGroup;
                    textView = holder.originalTextView;
                    text = holder.originalWord.getData();
                } else {
                    viewGroup = holder.translationViewGroup;
                    textView = holder.translationTextView;
                    text = holder.translationWord.getData();
                }

                textView.setText(text);
                viewGroup.bringToFront();
                prepareTransition(viewGroup, event).start();

                holder.isTranslationState = !holder.isTranslationState;

                return true;
            }
            return false;
        }

        private Animator prepareTransition(View v, MotionEvent event) {
            int cx = (int) event.getX();
            int cy = (int) event.getY();

            float maxX = Math.max(cx, v.getWidth() - cx);
            float maxY = Math.max(cy, v.getHeight() - cy);
            float radius = (float) Math.sqrt(Math.pow(maxX, 2) + Math.pow(maxY, 2));

            return ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, radius);
        }
    }

    private static class DataSource {

        private Language defaultLanguage;
        private DataBaseManager dataBaseManager;
        private Dao<Word> dao;

        private List<Link> links;

        private DataSource(DataBaseManager dataBaseManager, Language defaultLanguage) {
            this.dataBaseManager = dataBaseManager;
            this.defaultLanguage = defaultLanguage;
            this.dao = DaoFactory.createDao(Word.class);
        }

        public Word loadOriginalWord(Link link) {
            return loadWord(link, defaultLanguage);
        }

        public Word loadTranslationWord(Link link) {
            return loadWord(link, Language.UKRAINIAN.equals(defaultLanguage)
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
        }

        private void reset() {
            links = null;
        }
    }
}