package dmax.plua.ui.cards;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.viewpager.widget.PagerAdapter;

import dmax.plua.DataSource;
import dmax.plua.R;
import dmax.plua.domain.Language;
import dmax.plua.domain.Link;
import dmax.plua.domain.Word;
import dmax.plua.settings.OrderMode;

/**
 * Cards view pager adapter.
 *
 * <br/><br/>
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 18.12.14 at 12:38
 */
public class CardsPagerAdapter extends PagerAdapter {

    private Context context;
    private DataSource dataSource;
    private ViewGroup container;
    private CardInteractionListener listener;

    public CardsPagerAdapter(Context context, DataSource dataSource, CardInteractionListener listener) {
        this.context = context;
        this.dataSource = dataSource;
        this.listener = listener;
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
        this.container = container;

        View root = View.inflate(context, R.layout.v_word_card, null);
        root.setId(position);

        CardView card = (CardView) root.findViewById(R.id.card);

        // init card data holder
        CardViewHolder holder = new CardViewHolder();
        holder.originalViewGroup = card.findViewById(R.id.original);
        holder.translationViewGroup = card.findViewById(R.id.translation);
        holder.originalTextView = (TextView) card.findViewById(android.R.id.text1);
        holder.translationTextView = (TextView) card.findViewById(android.R.id.text2);

        holder.originalViewGroup.bringToFront();

        // forgot / recalled buttons make sense only when cards are ordered by priority
        View actions = root.findViewById(R.id.actions);
        actions.setVisibility(dataSource.getOrderMode() == OrderMode.SPACED ? View.VISIBLE : View.GONE);
        for (int id : new int[] { R.id.forgot, R.id.remember }) {
            View button = root.findViewById(id);
            button.setTag(holder);
            button.setOnClickListener(listener);
        }

        // fill out data holder with actual data from datasource
        holder.link = dataSource.getLinks().get(position);
        holder.originalWord = dataSource.loadOriginalWord(holder.link);
        holder.translationWord = dataSource.loadTranslationWord(holder.link);
        holder.originalTextView.setText(holder.originalWord.getData());

        card.setOnTouchListener(listener);
        card.setTag(holder);
        container.addView(root);

        return root;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    /**
     * Update all cards using new language
     */
    public void onLanguageChanged() {
        if (container == null) return;

        Language currentLanguage = dataSource.getSelectedLanguage();

        for (int i = 0; i < container.getChildCount(); i++) {
            CardView cardView = (CardView) container.getChildAt(i).findViewById(R.id.card);
            if (cardView != null) {
                CardViewHolder holder = (CardViewHolder) cardView.getTag();

                // if language not changed - stop
                if (holder.originalWord.getLanguage().equals(currentLanguage)) {
                    return;
                }

                // switch from translation to original mode
                if (holder.isTranslationState) {
                    holder.isTranslationState = false;
                    holder.originalViewGroup.setVisibility(View.VISIBLE);
                    holder.translationViewGroup.setVisibility(View.INVISIBLE);
                    holder.originalViewGroup.bringToFront();
                }

                // update data of card
                Word tmp = holder.originalWord;
                holder.originalWord = holder.translationWord;
                holder.translationWord = tmp;

                holder.originalTextView.setText(holder.originalWord.getData());
            }
        }
    }

    //~

    /**
     * Class for hold card data
     */
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
