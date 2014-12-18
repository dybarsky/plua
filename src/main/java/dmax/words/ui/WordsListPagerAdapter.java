package dmax.words.ui;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;

import dmax.words.R;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 18.12.14 at 12:38
 */
public class WordsListPagerAdapter extends PagerAdapter {

    private Object[] data = new Object[] {
            new Object(),
            new Object(),
            new Object(),
            new Object(),
            new Object()
    };
    private Context context;

    public WordsListPagerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return data[view.getId()].equals(object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        CardView card = (CardView) View.inflate(context, R.layout.v_wordslist_item, null);

        card.setId(position);

        container.addView(card);

        return data[position];
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

    }
}
