package com.example.yoavg.rxdemo.examples;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.example.yoavg.rxdemo.R;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

public class FilterObservablesActivity extends AppCompatActivity {

    private TextView mMainCounterTextView;
    private TextView mEvenTextView;
    private TextView mOddThousandsTextView;
    private TextView mDistinctTextView;
    private TextView mDistinctUntilChangedTextView;
    
    private BehaviorSubject<Long> mSubject = BehaviorSubject.create();
    private CompositeSubscription mSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_observables_example);

        mMainCounterTextView = (TextView) findViewById(R.id.tvMainCounter);
        mEvenTextView = (TextView) findViewById(R.id.tvEvenCounter);
        mOddThousandsTextView = (TextView) findViewById(R.id.tvOddThousand);
        mDistinctTextView = (TextView) findViewById(R.id.tvDistinct);
        mDistinctUntilChangedTextView = (TextView) findViewById(R.id.tvDistinctUntilChanged);

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
        observeEven();
        observerOddThousands();
        observeDistinct();
        observeDistinctUntilChange();

        startCounter();
    }

    private void observeDistinctUntilChange() {
        mSubscription.add(mSubject
                .map(new Func1<Long, Long>() {
                    @Override
                    public Long call(Long aLong) {
                        Long res = aLong % 3 == 0 || aLong % 5 == 0 ? -1 : aLong;
                        Log.d("distinctUntilChanged","got "+aLong+" emit: "+res);
                        return res;
                    }
                })
                .distinctUntilChanged()
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        mDistinctUntilChangedTextView.setText(""+aLong);
                    }
                })
        );
    }

    private void observeDistinct() {
        mSubscription.add(mSubject
                .map(new Func1<Long, Long>() {
                    @Override
                    public Long call(Long aLong) {
                        Long res = aLong == 0 ? 0 : aLong % (long)Math.sqrt(aLong);
                        Log.d("distinct","got "+aLong+" emit: "+res);
                        return res;
                    }
                })
                .distinct()
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        mDistinctTextView.setText(""+aLong);
                    }
                })

        );
    }

    private void startCounter() {
        mSubscription.add(Observable
                .interval(1,TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        mSubject.onNext(aLong);
                    }
                })
        );
    }

    private void observerOddThousands() {
        mSubscription.add(mSubject.
                filter(new Func1<Long, Boolean>() {
                    @Override
                    public Boolean call(Long aLong) {
                        return aLong % 2 > 0;
                    }
                })
                .map(new Func1<Long, String>() {
                    @Override
                    public String call(Long aLong) {
                        return ""+(aLong*1000);
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String string) {
                        mOddThousandsTextView.setText(string);
                    }
                })
        );
    }

    private void observeEven() {
        mSubscription.add(mSubject
                .filter(new Func1<Long, Boolean>() {
                    @Override
                    public Boolean call(Long aLong) {
                        return aLong % 2 == 0;
                    }
            })
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        mEvenTextView.setText(""+aLong);
                    }
                })
        );
    }

    private void observeMainCounter() {
        mSubscription.add(mSubject.subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                mMainCounterTextView.setText(""+aLong);
            }
        }));
    }
}
