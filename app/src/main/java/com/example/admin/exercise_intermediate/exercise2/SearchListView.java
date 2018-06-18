package com.example.admin.exercise_intermediate.exercise2;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.example.admin.exercise_intermediate.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class SearchListView extends FrameLayout {

    private String searchText;
    private EditText searchEditText;
    private RecyclerView recyclerView;
    private ArrayList<String> sports;
    private Database database;

    public SearchListView(@NonNull Context context) {
        super(context);
        setup(null);
    }

    public SearchListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup(attrs);
    }

    public SearchListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SearchListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup(attrs);
    }

    public void setSports(ArrayList<String> sports) {
        for (String sport: sports) {
            database.create(sport);
        }
    }

    private void setup(AttributeSet attrs) {
        inflate(getContext(), R.layout.search_list_view, this);
        database = new Database(this.getContext());
        sports = new ArrayList<>();
        bindView();
    }

    private void bindView() {
        // RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        Ex2Adapter adapter = new Ex2Adapter(sports);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.setAdapter(adapter);

        // Search
        searchEditText = findViewById(R.id.search_edit);
        searchEditText.addTextChangedListener(new TextWatcher() {
            int prevL = 0;
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                prevL = searchEditText.getText().toString().length();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().equals("")) {
                    ArrayList<String> sports = database.read(editable.toString());
                    Log.i("Sports", sports.toString());
                    recyclerView.setAdapter(new Ex2Adapter(sports));
                } else {
                    sports.clear();
                    recyclerView.setAdapter(new Ex2Adapter(sports));
                }
            }
        });


    }


    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.stringID = this.searchText;
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        this.searchText = ss.stringID;
        searchEditText.setText(searchText);
    }

    private static class SavedState extends BaseSavedState {

        String stringID;

        public SavedState(Parcelable source) {
            super(source);
        }

        private SavedState(Parcel in) {
            super(in);
            this.stringID = in.readString();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(this.stringID);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            @Override
            public SavedState[] newArray(int i) {
                return new SavedState[i];
            }
        };
    }
}
