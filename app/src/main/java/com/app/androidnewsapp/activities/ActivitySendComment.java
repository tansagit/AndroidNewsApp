package com.app.androidnewsapp.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.app.androidnewsapp.Config;
import com.app.androidnewsapp.R;
import com.app.androidnewsapp.models.Setting;
import com.app.androidnewsapp.models.Value;
import com.app.androidnewsapp.rests.ApiInterface;
import com.app.androidnewsapp.rests.RestAdapter;
import com.app.androidnewsapp.utils.Constant;

import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ActivitySendComment extends AppCompatActivity {

    String user_id;
    Long nid;
    View view;
    EditText edt_user_id, edt_nid, edt_comment_message;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_comment);
        view = findViewById(android.R.id.content);

        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle(getResources().getString(R.string.title_send_comment));
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            user_id = getIntent().getStringExtra("user_id");
            nid = getIntent().getLongExtra("nid", 0);
        }

        edt_user_id = findViewById(R.id.edt_user_id);
        edt_nid = findViewById(R.id.edt_nid);
        edt_comment_message = findViewById(R.id.edt_comment_message);

        edt_user_id.setText(user_id);
        //edt_nid.setText(String.valueOf(nid));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_comment, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.send:
                if (edt_comment_message.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), R.string.msg_write_comment, Toast.LENGTH_SHORT).show();
                } else if (edt_comment_message.getText().toString().length() <= 6) {
                    Toast.makeText(getApplicationContext(), R.string.msg_write_comment_character, Toast.LENGTH_SHORT).show();
                } else {
                    dialogSendComment();
                }
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    public void dialogSendComment() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivitySendComment.this);
        builder.setMessage(getString(R.string.confirm_send_comment));
        builder.setPositiveButton(getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                requestAction();
            }
        });
        builder.setNegativeButton(getString(R.string.dialog_no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void requestAction() {
        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<Setting> call = apiInterface.getPrivacyPolicy();
        call.enqueue(new Callback<Setting>() {
            @Override
            public void onResponse(Call<Setting> call, Response<Setting> response) {
                String comment_approval = response.body().getComment_approval();
                try {
                    if (comment_approval.equals("yes")) {
                        sendCommentApproval();
                    } else {
                        sendComment();
                    }
                } catch (Exception e) {
                    Log.d("onResponse", "There is an error");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Setting> call, Throwable t) {

            }

        });
    }

    public void sendComment() {

        progress = new ProgressDialog(this);
        progress.setCancelable(false);
        progress.setMessage(getResources().getString(R.string.sending_comment));
        progress.show();

        String user_id = edt_user_id.getText().toString();
        String content = edt_comment_message.getText().toString();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date_time = simpleDateFormat.format(new Date());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.ADMIN_PANEL_URL + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<Value> call = apiInterface.sendComment(nid, user_id, content, date_time);

        call.enqueue(new Callback<Value>() {
            @Override
            public void onResponse(Call<Value> call, Response<Value> response) {
                final String value = response.body().getValue();
                final String message = response.body().getMessage();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progress.dismiss();
                        if (value.equals("1")) {
                            Toast.makeText(getApplicationContext(), R.string.msg_comment_success, Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.msg_comment_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                }, Constant.DELAY_REFRESH);

            }

            @Override
            public void onFailure(Call<Value> call, Throwable t) {
                progress.dismiss();
                Toast.makeText(getApplicationContext(), "Network Error!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void sendCommentApproval() {

        progress = new ProgressDialog(this);
        progress.setCancelable(false);
        progress.setMessage(getResources().getString(R.string.sending_comment));
        progress.show();

        String user_id = edt_user_id.getText().toString();
        String content = edt_comment_message.getText().toString();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date_time = simpleDateFormat.format(new Date());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.ADMIN_PANEL_URL + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<Value> call = apiInterface.sendComment(nid, user_id, content, date_time);

        call.enqueue(new Callback<Value>() {
            @Override
            public void onResponse(Call<Value> call, Response<Value> response) {
                final String value = response.body().getValue();
                final String message = response.body().getMessage();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progress.dismiss();
                        if (value.equals("1")) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActivitySendComment.this);
                            builder.setMessage(R.string.msg_comment_approval);
                            builder.setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                            AlertDialog alert = builder.create();
                            alert.show();
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.msg_comment_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                }, Constant.DELAY_REFRESH);

            }

            @Override
            public void onFailure(Call<Value> call, Throwable t) {
                progress.dismiss();
                Toast.makeText(getApplicationContext(), "Network Error!", Toast.LENGTH_SHORT).show();
            }
        });

    }

}
