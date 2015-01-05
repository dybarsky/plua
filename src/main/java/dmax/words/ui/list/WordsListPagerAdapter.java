package dmax.words.ui.list;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import dmax.words.DataSource;
import dmax.words.R;
import dmax.words.domain.Language;
import dmax.words.domain.Link;
import dmax.words.domain.Word;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 18.12.14 at 12:38
 */
public class WordsListPagerAdapter extends PagerAdapter {

    private Context context;
    private CardStateSwitcher switcher;
    private DataSource dataSource;
    private ViewGroup container;

    public WordsListPagerAdapter(Context context, DataSource dataSource) {
        this.context = context;
        this.dataSource = dataSource;
        this.switcher = new CardStateSwitcher();
    }

    @Override
    public int getCount() {
        return dataSource.getLinks().size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (this.container == null) this.container = container;

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

        card.setOnTouchListener(switcher);
        card.setTag(holder);
        container.addView(root);

        return root;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public void onLanguageChanged() {
        Language current = dataSource.getSelectedLanguage();

        for (int i = 0; i < container.getChildCount(); i++) {
            CardView cardView = (CardView) container.getChildAt(i).findViewById(R.id.card);
            if (cardView != null) {
                CardViewHolder holder = (CardViewHolder) cardView.getTag();

                Word word1 = holder.originalWord;
                Word word2 = holder.translationWord;

                boolean translation = word2.getLanguage().equals(current);
                holder.originalWord = translation ? word2 : word1;
                holder.translationWord = translation ? word1 : word2;

                holder.originalTextView.setText(holder.originalWord.getData());
            }
        }
    }

    //~

    static class CardViewHolder {

        boolean isTranslationState;

        Link link;
        Word originalWord;
        Word translationWord;

        View originalViewGroup;
        View translationViewGroup;
        TextView originalTextView;
        TextView translationTextView;
    }

}