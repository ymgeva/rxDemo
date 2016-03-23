package com.example.yoavg.rxdemo.examples;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.yoavg.rxdemo.R;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

public class CombiningObservablesActivity extends AppCompatActivity {

    private static String[] LETTERS = {"a","b","c","d","e"};

    private TextView mCounterTextView;
    private TextView mStringsTextView;
    private TextView mJoinTextView;
    private TextView mMergeTextView;
    private TextView mZipTextView;

    private BehaviorSubject<Long> mCounterSubject = BehaviorSubject.create();
    private BehaviorSubject<String> mStringsSubject = BehaviorSubject.create();
    private CompositeSubscription mSubscription;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combining_observables);

        mCounterTextView = (TextView) findViewById(R.id.tvMainCounter);
        mStringsTextView = (TextView) findViewById(R.id.tvMainStrings);
        mJoinTextView = (TextView) findViewById(R.id.tvJoin);
        mMergeTextView= (TextView) findViewById(R.id.tvMerge);
        mZipTextView= (TextView) findViewById(R.id.tvZip);

    }

    private void observeMain() {
        mSubscription.add(mCounterSubject.subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                mCounterTextView.setText(""+aLong);
            }
        }));

        mSubscription.add(mStringsSubject.subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                mStringsTextView.setText(s);
            }
        }));
    }

    @Override
    protected void onResume() {
        super.onResume();
        observe();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSubscription.unsubscribe();
    }

    private void observe() {

        mSubscription = new CompositeSubscription();

        observeMain();
        observeMerge();
        observeJoin();
        observeZip();

        startCounter();
    }

    private void startCounter() {
        mSubscription.add(Observable
                .interval(1, TimeUnit.SECONDS)
                .onBackpressureLatest()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        mCounterSubject.onNext(aLong);
                    }
                })
        );

        mSubscription.add(Observable
                .interval(2500, TimeUnit.MILLISECONDS)
                .onBackpressureLatest()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        mStringsSubject.onNext(LETTERS[(int) (aLong % LETTERS.length)]);
                    }
                })
        );
    }

    private void observeZip() {
        mSubscription.add(Observable
                .zip(mCounterSubject.onBackpressureLatest(), mStringsSubject.onBackpressureLatest(), new Func2<Long, String, String>() {
                    @Override
                    public String call(Long aLong, String s) {
                        return ""+aLong+s;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        mZipTextView.setText(s);
                    }
                })
        );
    }

    private void observeMerge() {
        mSubscription.add(Observable
                .merge(mCounterSubject.map(new Func1<Long, String>() {
                            @Override
                            public String call(Long aLong) {
                                return ""+aLong;
                            }
                        })
                        ,mStringsSubject)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        mMergeTextView.setText(s);
                    }
                })
        );
    }

    private void observeJoin() {
        mSubscription.add(mStringsSubject.onBackpressureLatest()
                .join(mCounterSubject.onBackpressureLatest(),
                        new Func1<String, Observable<Object>>() {
                            @Override
                            public Observable<Object> call(String s) {
                                return Observable.never();
                            }
                        },
                        new Func1<Long, Observable<Object>>() {
                            @Override
                            public Observable<Object> call(Long aLong) {
                                return Observable.never();
                            }
                        },
                        new Func2<String, Long, String>() {
                            @Override
                            public String call(String s, Long aLong) {
                                return s+aLong;
                            }
                        }
                )
                .onBackpressureLatest()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        mJoinTextView.setText(s);
                    }
                })

        );
    }}
