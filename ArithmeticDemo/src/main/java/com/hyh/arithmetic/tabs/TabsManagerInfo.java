package com.hyh.arithmetic.tabs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2019/12/24
 */
public class TabsManagerInfo {

    @NonNull
    public List<String> usedChannelList;

    @Nullable
    public List<String> moreChannelList;

    public TabsManagerInfo(@NonNull List<String> usedChannelList,
                           @Nullable List<String> moreChannelList) {
        this.usedChannelList = usedChannelList;
        this.moreChannelList = moreChannelList;
    }
}