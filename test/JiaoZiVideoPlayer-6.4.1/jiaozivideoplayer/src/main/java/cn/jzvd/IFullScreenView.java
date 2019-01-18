package cn.jzvd;

/**
 * @author Administrator
 * @description
 * @data 2019/1/18
 */

public interface IFullScreenView {

    void setUp(Jzvd item);

    void show(OnFullScreenCloseListener listener);

    interface OnFullScreenCloseListener {

        void onClose(Jzvd full);
    }
}
