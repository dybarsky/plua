package dmax.words.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.TextView;

import dmax.words.R;
import dmax.words.domain.Language;
import dmax.words.domain.Link;
import dmax.words.domain.Word;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 18.12.14 at 12:38
 */
public class WordsListPagerAdapter extends PagerAdapter {

    private String[][] data = new String[][] {
            {"брехати", "вирішити", "пенсія", "різати"},
            {"kłamać", "zdecydować", "emerytura", "kroić"},
    };

    private Context context;
    private CardStateSwitcher switcher;

    public WordsListPagerAdapter(Context context) {
        this.context = context;
        this.switcher = new CardStateSwitcher();
    }

    @Override
    public int getCount() {
        return data[0].length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.getTag().equals(object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        CardView card = (CardView) View.inflate(context, R.layout.v_wordslist_item, null);

        CardViewHolder holder = new CardViewHolder();
        holder.originalViewGroup = card.findViewById(R.id.original);
        holder.translationViewGroup = card.findViewById(R.id.translation);
        holder.originalTextView = (TextView) card.findViewById(android.R.id.text1);
        holder.translationTextView = (TextView) card.findViewById(android.R.id.text2);

        holder.originalViewGroup.bringToFront();

        {
            Word word1 = new Word();
            word1.setLanguage(Language.UKRAINIAN);
            word1.setData(data[0][position]);

            Word word2 = new Word();
            word2.setLanguage(Language.POLISH);
            word2.setData(data[1][position]);

            holder.originalWord = word1;
            holder.translationWord = word2;
            holder.currentLanguage = word1.getLanguage();

            holder.originalTextView.setText(word1.getData());
        }

        card.setTag(holder);
        card.setOnTouchListener(switcher);
        container.addView(card);

        return holder;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = container.findViewWithTag(object);
        container.removeView(view);
    }

    //~

    private static class CardViewHolder {

        boolean isTranslationState;

        Link link;
        Word originalWord;
        Word translationWord;
        Language currentLanguage;

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
}