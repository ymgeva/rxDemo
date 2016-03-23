package com.example.yoavg.rxdemo.examples;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.yoavg.rxdemo.R;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

public class BackpreasureObservablesActivity extends AppCompatActivity {


    private TextView mDebounceTextView;
    private TextView mBufferTextView;
    private TextView mMainCounterTextView;
    private TextView mSampleTextView;


    BehaviorSubject<Long> mSubject = BehaviorSubject.create();
    CompositeSubscription mSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backpressure_observables);

        mMainCounterTextView = (TextView) findViewById(R.id.tvMainCounter);
        mDebounceTextView = (TextView) findViewById(R.id.tvDebounce);
        mBufferTextView = (TextView) findViewById(R.id.tvBuffer);
        mSampleTextView = (TextView) findViewById(R.id.tvSample);
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
        observeDebounse();
        observeBuffer();
        observeSample();

        startCounter();
    }

    private void startCounter() {
        mSubscription.add(Observable
                .interval(1,TimeUnit.SECONDS)
                .filter(new Func1<Long, Boolean>() {
                    @Override
                    public Boolean call(Long aLong) {
                        return aLong % 3 != 0 && aLong % 5 != 0;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        mSubject.onNext(aLong);
                    }
                })
        );
    }

    private void observeBuffer() {
        mSubscription.add(mSubject
                .buffer(3)
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<List<Long>, String>() {
                    @Override
                    public String call(List<Long> aLongs) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < aLongs.size()-1; i++) {
                            sb.append(aLongs.get(i)).append(",");
                        }
                        return sb.append(aLongs.get(aLongs.size()-1)).toString();
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String string) {
                        mBufferTextView.setText(string);
                    }
                })
        );
    }

    private void observeDebounse() {
        mSubscription.add(mSubject
                .debounce(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        mDebounceTextView.setText(""+aLong);
                    }
                })
        );
    }

    private void observeSample() {
        mSubscription.add(mSubject
                .sample(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        mSampleTextView.setText(""+aLong);
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
