package com.example.yoavg.rxdemo.examples;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.yoavg.rxdemo.R;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class SimpleObservablesActivity extends AppCompatActivity {


    private TextView mTextView;
    private EditText mEditText;

    private PublishSubject<String> mSubject = PublishSubject.create();
    private CompositeSubscription mSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_observable);

        mTextView = (TextView) findViewById(R.id.textView);
        mEditText = (EditText) findViewById(R.id.editText);
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event == null || event.getAction() != KeyEvent.ACTION_DOWN || event.getKeyCode() != KeyEvent.KEYCODE_ENTER) {
                    return false;
                }
                String text = v.getText().toString();
                v.setText("");
                mSubject.onNext(text);
                return true;
            }
        });

        findViewById(R.id.buttonSimple).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              startSimpleObserver();
            }
        });

        findViewById(R.id.buttonThreaded).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startThreadedObserver();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSubscription = new CompositeSubscription();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSubscription.unsubscribe();
    }

    private void startThreadedObserver() {
        clear();

        mSubscription.add(

                mSubject.observeOn(Schedulers.io())
                .map(new Func1<String, List<String>>() {
                    @Override
                    public List<String> call(String s) {
                        List<String> list = new ArrayList<>();
                        list.add(s+":");
                        list.add("map1: "+ Thread.currentThread().getName());
                        return list;
                    }
                })
                .observeOn(Schedulers.computation())
                .map(new Func1<List<String>, List<String>>() {
                    @Override
                    public List<String> call(List<String> list) {
                        list.add("map2: "+ Thread.currentThread().getName());
                        return list;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<String>>() {
                    @Override
                    public void call(List<String> list) {
                        list.add("call: "+ Thread.currentThread().getName());
                        list.add("---------------------------");
                        for (int i =0; i<list.size();i++) {
                            mTextView.append(list.get(i)+"\n");
                        }
                    }
                })
        );
    }

    private void startSimpleObserver() {
        clear();

        mSubscription.add(mSubject.subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                mTextView.append(s+"\n");
            }
        }));
    }

    private void clear() {
        mSubscription.clear();
        mTextView.setText("");
    }
}
