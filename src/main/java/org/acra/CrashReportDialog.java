package org.acra;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * This is the dialog Activity used by ACRA to get authorization from the user
 * to send reports. Requires android:launchMode="singleInstance" in your
 * AndroidManifest to work properly.
 **/
public class CrashReportDialog extends BaseCrashReportDialog implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener {

    private static final String STATE_EMAIL = "email";
    private static final String STATE_COMMENT = "comment";
    private EditText userCommentView;
    private EditText userEmailView;

    AlertDialog mDialog;
    private static DialogBuilderFactory factory = new DialogBuilderFactory(){
        @Override
        public AlertDialog.Builder getBuilder(Context context) {
            return new AlertDialog.Builder(context);
        }
    };

    public static void setDialogBuilderFactory(DialogBuilderFactory factory) {
        CrashReportDialog.factory = factory;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final AlertDialog.Builder dialogBuilder = factory.getBuilder(this);
        final int titleResourceId = ACRA.getConfig().resDialogTitle();
        if (titleResourceId != 0) {
            dialogBuilder.setTitle(titleResourceId);
        }
        final int iconResourceId = ACRA.getConfig().resDialogIcon();
        if (iconResourceId != 0) {
            dialogBuilder.setIcon(iconResourceId);
        }
        if(ACRA.getConfig().resDialogLayout() != ACRAConstants.DEFAULT_RES_VALUE) {
            View root = LayoutInflater.from(this).inflate(ACRA.getConfig().resDialogLayout(), null, false);
            dialogBuilder.setView(root);
            if(root.findViewById(android.R.id.button1) != null) {
                root.findViewById(android.R.id.button1).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CrashReportDialog.this.onClick(null, DialogInterface.BUTTON_POSITIVE);
                    }
                });
            }
            if(root.findViewById(android.R.id.button2) != null) {
                root.findViewById(android.R.id.button2).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CrashReportDialog.this.onClick(null, DialogInterface.BUTTON_NEGATIVE);
                    }
                });
            }
        } else {
            dialogBuilder.setView(buildCustomView(savedInstanceState));
            dialogBuilder.setPositiveButton(getText(ACRA.getConfig().resDialogPositiveButtonText()), CrashReportDialog.this);
            dialogBuilder.setNegativeButton(getText(ACRA.getConfig().resDialogNegativeButtonText()), CrashReportDialog.this);
        }

        mDialog = dialogBuilder.create();
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setOnDismissListener(this);
        mDialog.show();
    }

    protected View buildCustomView(Bundle savedInstanceState) {
        final LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(10, 10, 10, 10);
        root.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        root.setFocusable(true);
        root.setFocusableInTouchMode(true);

        final ScrollView scroll = new ScrollView(this);
        root.addView(scroll, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f));
        final LinearLayout scrollable = new LinearLayout(this);
        scrollable.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(scrollable);

        final TextView text = new TextView(this);
        final int dialogTextId = ACRA.getConfig().resDialogText();
        if (dialogTextId != 0) {
            text.setText(getText(dialogTextId));
        }
        scrollable.addView(text);

        // Add an optional prompt for user comments
        final int commentPromptId = ACRA.getConfig().resDialogCommentPrompt();
        if (commentPromptId != 0) {
            final TextView label = new TextView(this);
            label.setText(getText(commentPromptId));

            label.setPadding(label.getPaddingLeft(), 10, label.getPaddingRight(), label.getPaddingBottom());
            scrollable.addView(label, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));

            userCommentView = new EditText(this);
            userCommentView.setLines(2);
            if (savedInstanceState != null) {
                String savedValue = savedInstanceState.getString(STATE_COMMENT);
                if (savedValue != null) {
                    userCommentView.setText(savedValue);
                }
            }
            scrollable.addView(userCommentView);
        }

        // Add an optional user email field
        final int emailPromptId = ACRA.getConfig().resDialogEmailPrompt();
        if (emailPromptId != 0) {
            final TextView label = new TextView(this);
            label.setText(getText(emailPromptId));

            label.setPadding(label.getPaddingLeft(), 10, label.getPaddingRight(), label.getPaddingBottom());
            scrollable.addView(label);

            userEmailView = new EditText(this);
            userEmailView.setSingleLine();
            userEmailView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

            String savedValue = null;
            if (savedInstanceState != null) {
                savedValue = savedInstanceState.getString(STATE_EMAIL);
            }
            if (savedValue != null) {
                userEmailView.setText(savedValue);
            } else {
                final SharedPreferences prefs = ACRA.getACRASharedPreferences();
                userEmailView.setText(prefs.getString(ACRA.PREF_USER_EMAIL_ADDRESS, ""));
            }
            scrollable.addView(userEmailView);
        }

        return root;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            // Retrieve user comment
            final String comment = userCommentView != null ? userCommentView.getText().toString() : "";

            // Store the user email
            final String userEmail;
            final SharedPreferences prefs = ACRA.getACRASharedPreferences();
            if (userEmailView != null) {
                userEmail = userEmailView.getText().toString();
                final SharedPreferences.Editor prefEditor = prefs.edit();
                prefEditor.putString(ACRA.PREF_USER_EMAIL_ADDRESS, userEmail);
                prefEditor.commit();
            } else {
                userEmail = prefs.getString(ACRA.PREF_USER_EMAIL_ADDRESS, "");
            }
            sendCrash(comment, userEmail);
        } else {
            cancelReports();
        }

        finish();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (userCommentView != null && userCommentView.getText() != null) {
            outState.putString(STATE_COMMENT, userCommentView.getText().toString());
        }
        if (userEmailView != null && userEmailView.getText() != null) {
            outState.putString(STATE_EMAIL, userEmailView.getText().toString());
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        finish();
    }

    public interface DialogBuilderFactory {
        AlertDialog.Builder getBuilder(Context context);
    }
}