package com.hyh.web.multi;

/**
 * @author Administrator
 * @description
 * @data 2019/5/29
 */
class ItemTypeInfo {

    MultiModule multiModule;

    int itemType;

    ItemTypeInfo(MultiModule multiModule, int itemType) {
        this.multiModule = multiModule;
        this.itemType = itemType;
    }
}