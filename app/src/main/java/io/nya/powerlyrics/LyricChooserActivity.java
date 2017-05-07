package io.nya.powerlyrics;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ListView;

import com.jakewharton.rxbinding2.widget.RxTextView;

import java.util.concurrent.TimeUnit;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DisposableObserver;

public class LyricChooserActivity extends Activity {

    private CompositeDisposable mDisposable = new CompositeDisposable();

    ListView mSongChooser;
    EditText mSearchBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyric_chooser);
        mSongChooser = (ListView) findViewById(R.id.song_chooser);
        mSearchBox = (EditText) findViewById(R.id.search_box);

    }

    @Override
    protected void onResume() {
        mDisposable.add(
                RxTextView.textChanges(mSearchBox)
                        .filter(new Predicate<CharSequence>() {
                            @Override
                            public boolean test(@NonNull CharSequence charSequence) throws Exception {
                                return !TextUtils.isEmpty(charSequence);
                            }
                        })
                        .debounce(500, TimeUnit.MILLISECONDS)
                        .distinctUntilChanged()
                        .subscribeWith(new DisposableObserver<CharSequence>() {
                            @Override
                            public void onNext(CharSequence s) {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {

                            }
                        })
        );
        super.onResume();
    }

    @Override
    protected void onPause() {
        mDisposable.clear();
        super.onPause();
    }
}
