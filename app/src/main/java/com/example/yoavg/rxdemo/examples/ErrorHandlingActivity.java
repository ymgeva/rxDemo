package com.example.yoavg.rxdemo.examples;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.yoavg.rxdemo.R;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

public class ErrorHandlingActivity extends AppCompatActivity {


    private TextView mMainCounterTextView;
    private TextView mSimpleTextView;
    private TextView mRetryTextView;
    private TextView mCatchTextView;

    private AtomicBoolean isRetry = new AtomicBoolean();

    private BehaviorSubject<Long> mSubject = BehaviorSubject.create();
    private CompositeSubscription mSubscription;
    private Observable<Long> mThrowOnNegativeObservable = mSubject.filter(new Func1<Long, Boolean>() {
        @Override
        public Boolean call(Long aLong) {
            if (aLong < 0) {
                if (isRetry.get()) {
                    return true;
                }
                isRetry.set(true);
                long someStupidAction = aLong /0;
                return someStupidAction > 0;
            }
            return true;
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_handling);

        mMainCounterTextView = (TextView) findViewById(R.id.tvMainCounter);
        mSimpleTextView = (TextView) findViewById(R.id.tvSimple);
        mRetryTextView = (TextView) findViewById(R.id.tvRetry);
        mCatchTextView = (TextView) findViewById(R.id.tvCatch);

        findViewById(R.id.buttonThrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRetry.set(false);
                mSubject.onNext((long) -1);
            }
        });

    }

    private void observeMainCounter() {
        mSubscription.add(mSubject.subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                mMainCounterTextView.setText(""+aLong);
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

        observeMainCounter();
        observeSimple();
        observeRetry();
        observeCatch();

        startCounter();
    }

    private void startCounter() {
        mSubscription.add(Observable
                .interval(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        mSubject.onNext(aLong);
                    }
                })
        );
    }

    private void observeSimple() {
        mSubscription.add(mThrowOnNegativeObservable
            .subscribe(new Action1<Long>() {
                @Override
                public void call(Long aLong) {
                    mSimpleTextView.setText("" + aLong);
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    Log.e("onError: ","got error: ",throwable);
                }
            }));
    }

    private void observeRetry() {
        mSubscription.add(mThrowOnNegativeObservable
                .retry(3)
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        mRetryTextView.setText("" + aLong);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("retry: ","got error: ",throwable);
                    }
                }));
    }

    private void observeCatch() {
        mSubscription.add(mThrowOnNegativeObservable
                .onExceptionResumeNext(mSubject)
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        mCatchTextView.setText("" + aLong);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("catch: ","got error: ",throwable);
                    }
                }));
    }
}
