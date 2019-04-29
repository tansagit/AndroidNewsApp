package com.app.androidnewsapp.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.androidnewsapp.Config;
import com.app.androidnewsapp.R;
import com.app.androidnewsapp.adapter.AdapterComments;
import com.app.androidnewsapp.callbacks.CallbackComments;
import com.app.androidnewsapp.models.Comments;
import com.app.androidnewsapp.models.Value;
import com.app.androidnewsapp.rests.ApiInterface;
import com.app.androidnewsapp.rests.RestAdapter;
import com.app.androidnewsapp.utils.NetworkCheck;
import com.balysv.materialripple.MaterialRippleLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ActivityComments extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AdapterComments adapterCategory;
    public static final String EXTRA_OBJC = "key.EXTRA_OBJC";
    private Call<CallbackComments> callbackCall = null;
    StaggeredGridLayoutManager staggeredGridLayoutManager;
    Long nid, comments_count;
    RelativeLayout lyt_parent;
    FloatingActionButton btn_add_comment;
    MyApplication myApplication;
    View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        view = findViewById(android.R.id.content);

        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }

        myApplication = MyApplication.getInstance();

        nid = getIntent().getLongExtra("nid", 0);
        comments_count = getIntent().getLongExtra("count", 0);

        setupToolbar();

        lyt_parent = findViewById(R.id.lyt_parent);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout_category);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorOrange, R.color.colorGreen, R.color.colorBlue, R.color.colorRed);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        staggeredGridLayoutManager = new StaggeredGridLayoutManager(1, 1);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);

        //set data and list adapter
        adapterCategory = new AdapterComments(ActivityComments.this, new ArrayList<Comments>());
        recyclerView.setAdapter(adapterCategory);

        // on item list clicked
        adapterCategory.setOnItemClickListener(new AdapterComments.OnItemClickListener() {
            @Override
            public void onItemClick(View v, final Comments obj, int position, final Context context) {

                if (myApplication.getIsLogin() && myApplication.getUserId().equals(obj.user_id)) {

                    LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
                    View mView = layoutInflaterAndroid.inflate(R.layout.custom_dialog_edit, null);

                    final AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    alert.setView(mView);

                    final MaterialRippleLayout btn_edit = mView.findViewById(R.id.menu_edit);
                    final MaterialRippleLayout btn_delete = mView.findViewById(R.id.menu_delete);

                    final AlertDialog alertDialog = alert.create();

                    btn_edit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                            Intent i = new Intent(getApplicationContext(), ActivityUpdateComment.class);
                            i.putExtra("id", obj.comment_id);
                            i.putExtra("date_time", obj.date_time);
                            i.putExtra("content", obj.content);
                            startActivity(i);
                        }
                    });
                    btn_delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setMessage(getString(R.string.confirm_delete_comment));
                            builder.setPositiveButton(getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int which) {
                                    Retrofit retrofit = new Retrofit.Builder()
                                            .baseUrl(Config.ADMIN_PANEL_URL + "/")
                                            .addConverterFactory(GsonConverterFactory.create())
                                            .build();
                                    ApiInterface apiInterface = retrofit.create(ApiInterface.class);
                                    Call<Value> call = apiInterface.deleteComment(obj.comment_id);
                                    call.enqueue(new Callback<Value>() {
                                        @Override
                                        public void onResponse(Call<Value> call, Response<Value> response) {
                                            String value = response.body().getValue();
                                            String message = response.body().getMessage();
                                            if (value.equals("1")) {
                                                Toast.makeText(ActivityComments.this, message, Toast.LENGTH_SHORT).show();
                                                requestActionOnResume();
                                            } else {
                                                Toast.makeText(ActivityComments.this, message, Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Value> call, Throwable t) {
                                            t.printStackTrace();
                                            Toast.makeText(ActivityComments.this, "Network error!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });

                            builder.setNegativeButton(getString(R.string.dialog_no), null);
                            AlertDialog alert = builder.create();
                            alert.show();

                            alertDialog.dismiss();
                        }
                    });

                    alertDialog.show();

                } else if (myApplication.getIsLogin()) {

                    LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
                    View mView = layoutInflaterAndroid.inflate(R.layout.custom_dialog_reply, null);

                    final AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    alert.setView(mView);

                    final MaterialRippleLayout btn_reply = mView.findViewById(R.id.menu_reply);

                    final AlertDialog alertDialog = alert.create();

                    btn_reply.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();

                            Intent intent = new Intent(getApplicationContext(), ActivityReplyComment.class);
                            intent.putExtra("user_id", myApplication.getUserId());
                            intent.putExtra("user_name", obj.name);
                            intent.putExtra("nid", nid);
                            startActivity(intent);
                        }
                    });
                    alertDialog.show();

                }

            }
        });

        // on swipe list
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                lyt_parent.setVisibility(View.VISIBLE);
                adapterCategory.resetListData();
                requestAction();
            }
        });

        requestAction();
        lyt_parent.setVisibility(View.VISIBLE);

        btn_add_comment = findViewById(R.id.btn_add_comment);
        btn_add_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myApplication.getIsLogin()) {
                    //Toast.makeText(getApplicationContext(), "id : " + myApplication.getUserId() + nid, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), ActivitySendComment.class);
                    intent.putExtra("user_id", myApplication.getUserId());
                    intent.putExtra("nid", nid);
                    startActivity(intent);
                } else {
                    Intent login = new Intent(getApplicationContext(), ActivityUserLogin.class);
                    startActivity(login);

                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_required), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void setupToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle(getResources().getString(R.string.title_comments));
        }
    }

    private void displayApiResult(final List<Comments> categories) {
        swipeProgress(false);
        adapterCategory.setListData(categories);
        if (categories.size() == 0) {
            showNoItemView(true);
        }
    }

    private void requestCategoriesApi() {
        ApiInterface apiInterface = RestAdapter.createAPI();
        callbackCall = apiInterface.getComments(nid);
        callbackCall.enqueue(new Callback<CallbackComments>() {
            @Override
            public void onResponse(Call<CallbackComments> call, Response<CallbackComments> response) {
                CallbackComments resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    displayApiResult(resp.comments);
                } else {
                    onFailRequest();
                }
            }

            @Override
            public void onFailure(Call<CallbackComments> call, Throwable t) {
                if (!call.isCanceled()) onFailRequest();
            }

        });
    }

    private void onFailRequest() {
        swipeProgress(false);
        if (NetworkCheck.isConnect(ActivityComments.this)) {
            showFailedView(true, getString(R.string.msg_no_network));
        } else {
            showFailedView(true, getString(R.string.msg_offline));
        }
    }

    private void requestAction() {
        showFailedView(false, "");
        swipeProgress(true);
        showNoItemView(false);
        requestCategoriesApi();
    }

    private void requestActionOnResume() {
        showFailedView(false, "");
        swipeProgress(false);
        showNoItemView(false);
        requestCategoriesApi();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        swipeProgress(false);
        if (callbackCall != null && callbackCall.isExecuted()) {
            callbackCall.cancel();
        }
    }

    private void showFailedView(boolean flag, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed_category);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (flag) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAction();
            }
        });
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = findViewById(R.id.lyt_no_item_category);
        ((TextView) findViewById(R.id.txt_no_comment)).setText(R.string.msg_no_comment);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipeRefreshLayout.setRefreshing(show);
            return;
        }
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(show);
            }
        });
    }

    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        requestActionOnResume();
        lyt_parent.setVisibility(View.VISIBLE);
    }

}
