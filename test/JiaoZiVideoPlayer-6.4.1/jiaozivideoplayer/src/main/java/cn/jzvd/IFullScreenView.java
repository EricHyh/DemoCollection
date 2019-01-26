package cn.jzvd;

/**
 * @author Administrator
 * @description
 * @data 2019/1/18
 */

public interface IFullScreenView {

    void setUp(Jzvd small);

    boolean isShow();

    void show();

    boolean close();

    void destroy();
}
