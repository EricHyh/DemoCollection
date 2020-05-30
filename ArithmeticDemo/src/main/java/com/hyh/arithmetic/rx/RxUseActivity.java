package com.hyh.arithmetic.rx;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * @author Administrator
 * @description
 * @data 2020/5/30
 */
public class RxUseActivity extends Activity {

    private static final String TAG = "RxUseActivity_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Button button = new Button(this);
        button.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        button.setText("测试");
        button.setOnClickListener(v -> {


            /*Observable.just(0, 1, 2, 3)
                    .subscribeOn(Schedulers.io())
                    .flatMap(new Function<Integer, ObservableSource<?>>() {
                        @Override
                        public ObservableSource<?> apply(Integer integer) throws Throwable {
                            return null;
                        }
                    })*/


            Observable.just("0123456789")
                    .subscribeOn(Schedulers.io())
                    .map(new Function<String, List<Observable<String>>>() {
                        @Override
                        public List<Observable<String>> apply(String s) throws Throwable {
                            List<Observable<String>> observables = new ArrayList<>();
                            for (int index = 0; index < s.length(); index++) {
                                final char c = s.charAt(index);
                                final int finalIndex = index;
                                observables.add(new Observable<String>() {

                                    @Override
                                    protected void subscribeActual(@NonNull Observer<? super String> observer) {
                                        Log.d(TAG, "subscribeActual: index" + finalIndex
                                                + ", c = " + c
                                                + ", thread = " + Thread.currentThread().getName());
                                        String result = finalIndex + "_" + c;
                                        if (finalIndex == 2) {
                                            observer.onError(new RuntimeException("xxx"));
                                        } else {
                                            observer.onNext(result);
                                            observer.onComplete();
                                        }
                                    }
                                }.subscribeOn(Schedulers.io()));
                            }
                            return observables;
                        }
                    })
                    .flatMap(new Function<List<Observable<String>>, ObservableSource<List<String>>>() {
                        @Override
                        public ObservableSource<List<String>> apply(List<Observable<String>> observables) throws Throwable {
                            return Observable.combineLatest(observables, new Function<Object[], List<String>>() {
                                @Override
                                public List<String> apply(Object[] objects) throws Throwable {
                                    List objects1 = Arrays.asList(objects);
                                    return objects1;
                                }
                            });
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<String>>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onNext(@NonNull List<String> strings) {
                            Log.d(TAG, "onNext: strings = " + strings);
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Log.d(TAG, "onError: e = " + e);
                        }

                        @Override
                        public void onComplete() {
                            Log.d(TAG, "onComplete");
                        }
                    });





                   /* .map(new Function<String, ObservableSource<String>>() {
                        @Override
                        public ObservableSource<String> apply(String s) throws Throwable {
                            List<Observable<String>> sources = new ArrayList<>();
                            for (int index = 0; index < s.length(); index++) {
                                final char c = s.charAt(index);
                                final int finalIndex = index;
                                sources.add(new Observable<String>() {

                                    @Override
                                    protected void subscribeActual(@NonNull Observer<? super String> observer) {
                                        Log.d(TAG, "subscribeActual: index" + finalIndex
                                                + ", c = " + c
                                                + ", thread = " + Thread.currentThread().getName());
                                        String result = finalIndex + "_" + c;
                                        observer.onNext(result);
                                        observer.onComplete();
                                    }
                                }.subscribeOn(Schedulers.io()));
                            }

                            @SuppressWarnings("all")
                            Observable<String>[] observables = (Observable<String>[]) sources.toArray();
                            @NonNull Observable<String> observable = Observable.mergeArray(observables);
                            return observable;
                        }
                    })
                    .map(new Function<ObservableSource<String>, String>() {
                        @Override
                        public String apply(ObservableSource<String> stringObservableSource) throws Throwable {
                            stringObservableSource.subscribe(new BlockingBaseObserver<String>() {
                                @Override
                                public void onNext(@NonNull String s) {

                                }

                                @Override
                                public void onError(@NonNull Throwable e) {

                                }
                            });
                            return null;
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onNext(@NonNull String s) {

                        }

                        @Override
                        public void onError(@NonNull Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });*/


        });
        setContentView(button);
    }
}
