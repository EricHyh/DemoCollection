package cn.jzvd;

/**
 * @author Administrator
 * @description
 * @data 2019/1/18
 */

class JzvdInfo {

    JZTextureView textureView;

    JZDataSource jzDataSource;

    int currentState;

    int progress;

    JzvdInfo(JZTextureView textureView, JZDataSource jzDataSource, int currentState, int progress) {
        this.textureView = textureView;
        this.jzDataSource = jzDataSource;
        this.currentState = currentState;
        this.progress = progress;
    }
}
