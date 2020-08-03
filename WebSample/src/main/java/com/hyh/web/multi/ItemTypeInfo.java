package com.hyh.web.multi;

/**
 * @author Administrator
 * @description
 * @data 2019/5/29
 */
class ItemTypeInfo {

    MultiItemFactory multiItemFactory;

    int itemType;

    ItemTypeInfo(MultiItemFactory multiItemFactory, int itemType) {
        this.multiItemFactory = multiItemFactory;
        this.itemType = itemType;
    }
}