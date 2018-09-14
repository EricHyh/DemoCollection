package com.hyh.tools.download.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Administrator
 * @description
 * @data 2018/3/1
 */

public class TagInfo implements Parcelable {

    private String tagStr;

    private String tagClassName;

    private Object tag;

    public TagInfo(String tagStr, String tagClassName, Object tag) {
        this.tagStr = tagStr;
        this.tagClassName = tagClassName;
        this.tag = tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public Object getTag() {
        return tag;
    }

    public String getTagStr() {
        return tagStr;
    }

    public String getTagClassName() {
        return tagClassName;
    }

    public <T> T getTag(Class<T> tagClass) {
        if (tag == null) {
            return null;
        }
        T result = null;
        if (tag.getClass().equals(tagClass)) {
            result = (T) this.tag;
        }
        return result;
    }

    @Override
    public String toString() {
        return "TagInfo{" +
                "tagStr='" + tagStr + '\'' +
                ", tagClassName='" + tagClassName + '\'' +
                ", tag=" + tag +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }


    protected TagInfo(Parcel in) {
        tagStr = in.readString();
        tagClassName = in.readString();
    }

    public static final Creator<TagInfo> CREATOR = new Creator<TagInfo>() {
        @Override
        public TagInfo createFromParcel(Parcel in) {
            return new TagInfo(in);
        }

        @Override
        public TagInfo[] newArray(int size) {
            return new TagInfo[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(tagStr);
        dest.writeString(tagClassName);
    }
}
