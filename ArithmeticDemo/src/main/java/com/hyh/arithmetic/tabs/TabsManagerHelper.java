package com.hyh.arithmetic.tabs;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2019/12/24
 */
public class TabsManagerHelper {

    @NonNull
    public static TabsManagerInfo computeOriginalTabsManagerInfo(@NonNull List<String> originalChannelList,
                                                                 @NonNull TabsManagerInfo userTabsManagerInfo) {
        if (originalChannelList.size() <= 2) {
            return new TabsManagerInfo(originalChannelList, null);
        }
        if (userTabsManagerInfo.usedChannelList.isEmpty()) {
            return new TabsManagerInfo(originalChannelList, null);
        }


        List<String> resultUsedChannelList;
        List<String> resultMoreChannelList = null;

        List<String> tempChannelList = new ArrayList<>(originalChannelList);

        //第一步，删除原始列表中，在更多频道列表中存在的频道
        List<String> userMoreChannelList = userTabsManagerInfo.moreChannelList;
        if (userMoreChannelList != null && !userMoreChannelList.isEmpty()) {
            resultMoreChannelList = new ArrayList<>();
            Iterator<String> iterator = tempChannelList.iterator();
            int index = 0;
            while (iterator.hasNext()) {
                String channelName = iterator.next();
                if (index > 0 && userMoreChannelList.contains(channelName)) {
                    resultMoreChannelList.add(channelName);
                    iterator.remove();
                }
                index++;
            }
        }

        int tempChannelSize = tempChannelList.size();
        int userUsedChannelSize = userTabsManagerInfo.usedChannelList.size();

        String[] resultUserChannelArray = new String[Math.max(tempChannelSize, userUsedChannelSize)];

        String fixedFirstChannelName = tempChannelList.remove(0);
        resultUserChannelArray[0] = fixedFirstChannelName;
        String userFirstChannelName = null;


        //第二步，确定原始列表中，在用户频道列表中的位置，并插入到频道数组中
        for (int index = 0; index < userUsedChannelSize; index++) {
            String channelName = userTabsManagerInfo.usedChannelList.get(index);
            int resultChannelIndex = tempChannelList.indexOf(channelName);
            if (resultChannelIndex < 0) {
                continue;
            }
            tempChannelList.remove(channelName);


            if (index == 0) {
                if (!TextUtils.equals(channelName, fixedFirstChannelName)) {
                    userFirstChannelName = channelName;
                }
            } else {
                resultUserChannelArray[index] = channelName;
            }
        }

        //第三步，将原始列表中还没有确定位置的频道，按照顺序插入到频道数组中频道为空的位置上
        int firstEmptyIndex = 0;
        for (int index = 0; index < resultUserChannelArray.length; index++) {
            String channelName = resultUserChannelArray[index];
            if (channelName == null) {
                if (userFirstChannelName != null && firstEmptyIndex == 0) {
                    firstEmptyIndex = index;
                } else {
                    if (tempChannelList.isEmpty()) {
                        break;
                    } else {
                        resultUserChannelArray[index] = tempChannelList.remove(0);
                    }
                }
            }
        }

        //第四步，将数组中频道为空的位置删除，得到最终结果
        resultUsedChannelList = new ArrayList<>();
        resultUsedChannelList.add(resultUserChannelArray[0]);
        if (userFirstChannelName != null) {
            resultUsedChannelList.add(userFirstChannelName);
        }
        for (int index = 1; index < resultUserChannelArray.length; index++) {
            String channelName = resultUserChannelArray[index];
            if (channelName == null) {
                continue;
            }
            resultUsedChannelList.add(channelName);
        }
        return new TabsManagerInfo(resultUsedChannelList, resultMoreChannelList);
    }
}