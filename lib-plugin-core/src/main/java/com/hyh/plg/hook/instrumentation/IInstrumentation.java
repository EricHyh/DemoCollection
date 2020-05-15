package com.hyh.plg.hook.instrumentation;

import android.app.Instrumentation;

/**
 * @author Administrator
 * @description
 * @data 2019/6/26
 */

public interface IInstrumentation {

    int getInstrumentationVersion();

    void requestReplaceInstrumentation();

    void requestRecoverInstrumentation();

    void onInstrumentationReplaced(Instrumentation newInstrumentation);

    void onInstrumentationRecovered();

}