package com.example.admin.exercise_intermediate.exercise2;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.example.admin.exercise_intermediate.R;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class IdCardEditText extends FrameLayout {

    EditText idEditText;

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


    private String strJoin(List<String> aArr) {
        StringBuilder sbStr = new StringBuilder();
        for (int i = 0, il = aArr.size(); i < il; i++) {
            sbStr.append(aArr.get(i));
        }
        return sbStr.toString();
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
}
