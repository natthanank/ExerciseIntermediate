package com.example.exercise2;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Arrays;

public class IdCardEditText extends FrameLayout {

    private EditText idEditText;
    private String stringID;

    public IdCardEditText(@NonNull Context context) {
        super(context);
        setup(null);
    }

    public IdCardEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup(attrs);
    }

    public IdCardEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(attrs);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public IdCardEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup(attrs);

    }

    private void setup(AttributeSet attrs) {
        inflate(getContext(), R.layout.id_card_view, this);
        bindView();
    }

    private void bindView() {
        idEditText = findViewById(R.id.id_card_edit);
        idEditText.addTextChangedListener(new TextWatcher() {
            int prevL = 0;
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                prevL = idEditText.getText().toString().length();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void afterTextChanged(Editable editable) {
                int length = editable.length();
                    if ((prevL < length) && (length == 1 || length == 6 || length == 12 || length == 15)) {
                        editable.append("-");
                    } else {
                        if ((prevL > length && (length == 16 || length == 13 || length == 7 || length == 2))) {
                            editable.delete(length - 1, length);
                        }
                    }
                    check();

            }
        });
    }


    private void check() {
        Editable rawID = idEditText.getText();
        String id = rawID.toString();
        ArrayList<String> idList = new ArrayList<>();
        idList.addAll(Arrays.asList(id.split("")));
        if (id.length() < 17) {
            idEditText.setTextColor(Color.RED);
        } else {
            idEditText.setTextColor(Color.BLACK);
        }
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.stringID = this.stringID;
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
        this.stringID = ss.stringID;
        idEditText.setText(stringID);
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
