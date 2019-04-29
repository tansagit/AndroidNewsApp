package com.app.androidnewsapp.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.app.androidnewsapp.Config;
import com.app.androidnewsapp.R;
import com.app.androidnewsapp.models.Value;
import com.app.androidnewsapp.rests.ApiInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ActivityUpdateComment extends AppCompatActivity {

    EditText edt_id, edt_comment_message, edt_date_time;
    private ProgressDialog progress;
    String str_id, str_date_time, str_content;
    View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_comment);
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
            getSupportActionBar().setTitle(getResources().getString(R.string.title_update_comment));
        }

        Intent intent = getIntent();
        str_id = intent.getStringExtra("id");
        str_content = intent.getStringExtra("content");
        str_date_time = intent.getStringExtra("date_time");

        edt_id = findViewById(R.id.edt_id);
        edt_date_time = findViewById(R.id.edt_timestamp);
        edt_comment_message = findViewById(R.id.edt_comment_message);

        edt_id.setText(str_id);
        edt_date_time.setText(str_date_time);
        edt_comment_message.setText(str_content);
        edt_comment_message.setSelection(edt_comment_message.getText().length());

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
                    dialogUpdateComment();
                }
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    public void dialogUpdateComment() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityUpdateComment.this);
        builder.setMessage(getString(R.string.confirm_update_comment));
        builder.setPositiveButton(getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                updateComment();
            }
        });

        builder.setNegativeButton(getString(R.string.dialog_no), null);
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void updateComment() {

        progress = new ProgressDialog(this);
        progress.setCancelable(false);
        progress.setMessage(getResources().getString(R.string.updating_comment));
        progress.show();

        String comment_id = edt_id.getText().toString();
        String date_time = edt_date_time.getText().toString();
        String content = edt_comment_message.getText().toString();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.ADMIN_PANEL_URL + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<Value> call = apiInterface.updateComment(comment_id, date_time, content);
        call.enqueue(new Callback<Value>() {
            @Override
            public void onResponse(Call<Value> call, Response<Value> response) {
                String value = response.body().getValue();
                String message = response.body().getMessage();
                progress.dismiss();
                if (value.equals("1")) {
                    Toast.makeText(getApplicationContext(), R.string.msg_comment_update, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.msg_update_comment_failed, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Value> call, Throwable t) {
                t.printStackTrace();
                progress.dismiss();
                Toast.makeText(getApplicationContext(), "Jaringan Error!", Toast.LENGTH_SHORT).show();
            }
        });

    }

}
