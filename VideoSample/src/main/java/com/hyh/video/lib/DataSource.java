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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataSource that = (DataSource) o;

        if (pathType != that.pathType) return false;
        return path != null ? path.equals(that.path) : that.path == null;
    }
}