package com.hyh.web.multi.load;

import android.view.View;

/**
 * @author Administrator
 * @description
 * @data 2017/11/24
 */

public interface IFootView {

    View onCreateView(LoadingModule loadingModule);

    void onLoadStateChanged(int oldState, int newState);

}
