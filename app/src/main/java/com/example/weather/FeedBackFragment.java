package com.example.weather;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.weather.diplayoption.WeatherDisplayOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class FeedBackFragment extends Fragment {
    private TextInputEditText editText;
    private MaterialButton sendButton;
    private final String feedBackKey = "FeedBackFragmentKey";

    public FeedBackFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed_back, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);

        setupSendButton();
        setupEditText();
        sendButton.setEnabled(false);
    }

    private void findViews(View view) {
        editText = view.findViewById(R.id.feedbackText);
        sendButton = view.findViewById(R.id.send_feedback_button);
    }

    private void setupSendButton() {
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String feedback = Objects.requireNonNull(editText.getText()).toString();
                if ( feedback.length() > 0 ) {
                    sendFeedBackViaEmail();
                }
            }
        });

    }

    private void setupEditText() {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if ( charSequence.length() != 0 ) {
                    sendButton.setEnabled(true);
                    editText.setError(null);
                } else {
                    sendButton.setEnabled(false);
                    editText.setError("Feedback is empty");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }


    private void sendFeedBackViaEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:feedback@somecompany.com"));

        //Check for assigned email account on device
        ComponentName emailApp = intent.resolveActivity(requireContext().getPackageManager());
        ComponentName unsupportedAction = ComponentName.unflattenFromString("com.android.fallback/.Fallback");
        boolean hasEmailApp = emailApp != null && !emailApp.equals(unsupportedAction);

        intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
        intent.putExtra(Intent.EXTRA_TEXT, editText.getText().toString());

        if ( hasEmailApp ) {
            startActivity(Intent.createChooser(intent, "Send feedback"));
        } else {
            Snackbar snackbar = Snackbar.make(sendButton, "Can't send Feedback. Please setup email account and try again", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }
}