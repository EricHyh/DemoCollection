package com.hyh.web;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hyh.web.widget.CustomWebView;
import com.hyh.web.widget.WebClient;


/**
 * Created by Eric_He on 2017/11/12.
 */

public class WebFragment extends Fragment {

    private CustomWebView mWebView;
    private WebClient mWebClient;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mWebView = new CustomWebView (getContext());
        mWebClient = new WebClient(getContext(), mWebView);
        return mWebView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mWebClient.loadUrl("http://www.baidu.com");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mWebClient.destroy();
    }
}
