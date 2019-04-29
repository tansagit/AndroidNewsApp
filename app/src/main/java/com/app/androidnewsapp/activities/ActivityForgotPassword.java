package com.app.androidnewsapp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.app.androidnewsapp.Config;
import com.app.androidnewsapp.R;
import com.app.androidnewsapp.utils.Constant;
import com.app.androidnewsapp.utils.NetworkCheck;
import com.app.androidnewsapp.utils.validation.Rule;
import com.app.androidnewsapp.utils.validation.Validator;
import com.app.androidnewsapp.utils.validation.annotation.Email;
import com.app.androidnewsapp.utils.validation.annotation.Required;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ActivityForgotPassword extends AppCompatActivity implements Validator.ValidationListener {

    @Required(order = 1)
    @Email(order = 2, message = "Please Check and Enter a valid Email Address")
    EditText edtEmail;
    String strEmail, strMessage;
    private Validator validator;
    Button btn_forgot;
    ProgressBar progressBar;
    LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_forgot);

        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }

        edtEmail = findViewById(R.id.etUserName);
        btn_forgot = findViewById(R.id.btnForgot);
        progressBar = findViewById(R.id.progressBar);
        layout = findViewById(R.id.view);

        btn_forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validator.validateAsync();
            }
        });

        validator = new Validator(this);
        validator.setValidationListener(this);

        setupToolbar();

    }

    public void setupToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
            getSupportActionBar().setTitle("");
        }

        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        if (appBarLayout.getLayoutParams() != null) {
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
            AppBarLayout.Behavior appBarLayoutBehaviour = new AppBarLayout.Behavior();
            appBarLayoutBehaviour.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
                @Override
                public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                    return false;
                }
            });
            layoutParams.setBehavior(appBarLayoutBehaviour);
        }
    }

    @Override
    public void onValidationSucceeded() {
        strEmail = edtEmail.getText().toString();
        if (NetworkCheck.isNetworkAvailable(ActivityForgotPassword.this)) {
            new MyTaskForgot().execute(Constant.FORGET_PASSWORD_URL + strEmail);
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.msg_no_network), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onValidationFailed(View failedView, Rule<?> failedRule) {
        String message = failedRule.getFailureMessage();
        if (failedView instanceof EditText) {
            failedView.requestFocus();
            ((EditText) failedView).setError(message);
        } else {
            Toast.makeText(this, "Record Not Saved", Toast.LENGTH_SHORT).show();
        }
    }

    private class MyTaskForgot extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            layout.setVisibility(View.INVISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            return NetworkCheck.getJSONString(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (null == result || result.length() == 0) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.msg_no_network), Toast.LENGTH_SHORT).show();

            } else {

                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.CATEGORY_ARRAY_NAME);
                    JSONObject objJson = null;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);
                        strMessage = objJson.getString(Constant.MSG);
                        Constant.GET_SUCCESS_MSG = objJson.getInt(Constant.SUCCESS);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        setResult();
                    }
                }, Constant.DELAY_PROGRESS_DIALOG);
            }

        }
    }

    public void setResult() {

        if (Constant.GET_SUCCESS_MSG == 0) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(R.string.whops);
            dialog.setMessage(R.string.forgot_failed_message);
            dialog.setPositiveButton(R.string.dialog_ok, null);
            dialog.setCancelable(false);
            dialog.show();

            layout.setVisibility(View.VISIBLE);
            edtEmail.setText("");
            edtEmail.requestFocus();

        } else {

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(R.string.dialog_success);
            dialog.setMessage(R.string.forgot_success_message);
            dialog.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(ActivityForgotPassword.this, ActivityUserLogin.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            });
            dialog.setCancelable(false);
            dialog.show();

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }
}


