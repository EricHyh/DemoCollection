package com.hyh.video.lib;

/**
 * @author Administrator
 * @description
 * @data 2019/3/1
 */

public class DataSource {

    public static final int TYPE_NET = 1;
    public static final int TYPE_FILE = 2;
    public static final int TYPE_URI = 3;

    private String path;

    private int pathType;

    public DataSource(String path, int pathType) {
        this.path = path;
        this.pathType = pathType;
    }

    public String getPath() {
        return path;
    }

    public int getPathType() {
        return pathType;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        DataSource that = (DataSource) object;

        return path != null ? path.equals(that.path) : that.path == null;
    }
}
