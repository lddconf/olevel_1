package com.example.weather;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputLayout;

public class CitySearchDialog extends DialogFragment {
    private EditText input;
    private static final String inputKey = "InputKey";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setMaxLines(1);
        input.setSingleLine(true);

        TextInputLayout layout = new TextInputLayout(getActivity());
        layout.setPadding(32, 0, 32, 0);
        layout.addView(input);

        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(getString(R.string.find_city_title));
        alert.setView(layout);
        alert.setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String result = input.getText().toString().trim();
                        if (!result.isEmpty()) {
                            ((MainActivity)getActivity()).onCitySearchRequested(result);
                        }
                    }
                }
        );
        alert.setNegativeButton(R.string.cancel_label, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Nothing to do
            }
        });

        if (savedInstanceState != null) {
            String saved = savedInstanceState.getString(inputKey);
            if ( saved != null ) {
                input.setText(saved);
            }
        }

        return alert.create();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(inputKey, input.getText().toString().trim());
        super.onSaveInstanceState(outState);
    }

}
