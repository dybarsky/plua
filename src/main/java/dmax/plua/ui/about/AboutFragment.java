package dmax.plua.ui.about;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import dmax.plua.R;

/**
 * Fragment for about page.
 *
 * <br/><br/>
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 26.01.15 at 14:06
 */
public class AboutFragment extends Fragment implements View.OnClickListener {

    private static final String ABOUT_URL = "file:///android_asset/about.html";
    private static final String HELP_URL = "file:///android_asset/help.html";

    View items;
    WebView webView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.f_about, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        items = getView().findViewById(R.id.about_layout);
        webView = (WebView) getView().findViewById(R.id.web);

        getView().findViewById(R.id.help).setOnClickListener(this);
        getView().findViewById(R.id.rate).setOnClickListener(this);
        getView().findViewById(R.id.feedback).setOnClickListener(this);
        getView().findViewById(R.id.about).setOnClickListener(this);

        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setTitle(R.string.help);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                getActivity().getFragmentManager().popBackStack();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.help: {
                showHelp();
                break;
            }
            case R.id.rate: {
                openPlayStore();
                break;
            }
            case R.id.feedback: {
                sendFeedback();
                break;
            }
            case R.id.about: {
                openAbout();
                break;
            }
        }
    }

    private void sendFeedback() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse(getString(R.string.send_feedback)));
        startActivity(intent);
    }

    private void openPlayStore() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(getString(R.string.rate) + getActivity().getPackageName()));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(getString(R.string.rate_web) + getActivity().getPackageName()));
            startActivity(intent);
        }
    }

    private void openAbout() {
        openWebView(ABOUT_URL);
    }

    private void showHelp() {
        openWebView(HELP_URL);
    }

    private void openWebView(String url) {
        webView.loadUrl(url);
        items.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
    }
}
