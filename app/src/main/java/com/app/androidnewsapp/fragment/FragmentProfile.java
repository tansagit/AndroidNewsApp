package com.app.androidnewsapp.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.androidnewsapp.Config;
import com.app.androidnewsapp.R;
import com.app.androidnewsapp.activities.ActivityPrivacyPolicy;
import com.app.androidnewsapp.activities.ActivityProfile;
import com.app.androidnewsapp.activities.ActivityUserLogin;
import com.app.androidnewsapp.activities.ActivityUserRegister;
import com.app.androidnewsapp.activities.MyApplication;
import com.app.androidnewsapp.adapter.AdapterAbout;
import com.app.androidnewsapp.utils.ApiConnector;
import com.app.androidnewsapp.utils.Constant;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FragmentProfile extends Fragment {

    private View root_view, parent_view;
    MyApplication myApplication;
    RelativeLayout lyt_is_login, lyt_login_register;
    TextView txt_edit;
    TextView txt_login;
    TextView txt_logout;
    ProgressDialog progressDialog;
    TextView txt_register, txt_username, txt_email;
    ImageView img_profile;
    RecyclerView recyclerView;
    AdapterAbout adapterAbout;
    LinearLayout lyt_root;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_profile, null);
        parent_view = getActivity().findViewById(R.id.main_content);
        lyt_root = root_view.findViewById(R.id.root_layout);

        myApplication = MyApplication.getInstance();

        lyt_is_login = root_view.findViewById(R.id.lyt_is_login);
        lyt_login_register = root_view.findViewById(R.id.lyt_login_register);
        txt_login = root_view.findViewById(R.id.btn_login);
        txt_logout = root_view.findViewById(R.id.txt_logout);
        txt_edit = root_view.findViewById(R.id.btn_logout);
        txt_register = root_view.findViewById(R.id.txt_register);
        txt_username = root_view.findViewById(R.id.txt_username);
        txt_email = root_view.findViewById(R.id.txt_email);
        img_profile = root_view.findViewById(R.id.img_profile);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(getResources().getString(R.string.title_please_wait));
        progressDialog.setMessage(getResources().getString(R.string.logout_process));
        progressDialog.setCancelable(false);

        recyclerView = root_view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapterAbout = new AdapterAbout(getDataInformation(), getActivity());
        recyclerView.setAdapter(adapterAbout);

        if (Config.ENABLE_RTL_MODE) {
            lyt_root.setRotationY(180);
        }

        adapterAbout.setOnItemClickListener(new AdapterAbout.OnItemClickListener() {
            @Override
            public void onItemClick(View v, Data obj, int position) {
                if (position == 0) {
                    startActivity(new Intent(getActivity(), ActivityPrivacyPolicy.class));
                }
                if (position == 1) {
                    final String appName = getActivity().getPackageName();
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appName)));
                    }
                } else if (position == 2) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.play_more_apps))));
                } else if (position == 3) {
                    aboutDialog();
                }
            }
        });

        return root_view;
    }

    @Override
    public void onResume() {

        if (myApplication.getIsLogin()) {
            lyt_is_login.setVisibility(View.VISIBLE);
            lyt_login_register.setVisibility(View.GONE);

            new getUserImage().execute(new ApiConnector());

            txt_logout.setVisibility(View.VISIBLE);
            txt_logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logoutDialog();
                }
            });

        } else {
            lyt_is_login.setVisibility(View.GONE);
            lyt_login_register.setVisibility(View.VISIBLE);
            txt_login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity(), ActivityUserLogin.class));
                }
            });

            txt_register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity(), ActivityUserRegister.class));
                }
            });
            txt_logout.setVisibility(View.GONE);
        }

        super.onResume();
    }

    public void logoutDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.logout_title);
        builder.setMessage(R.string.logout_message);
        builder.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface di, int i) {

                MyApplication.getInstance().saveIsLogin(false);
                progressDialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage(R.string.logout_success);
                        builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                refreshFragment();
                            }
                        });
                        builder.setCancelable(false);
                        builder.show();
                    }
                }, Constant.DELAY_PROGRESS_DIALOG);

            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, null);
        builder.show();

    }

    public void refreshFragment() {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.detach(this).attach(this).commit();
    }

    private class getUserImage extends AsyncTask<ApiConnector, Long, JSONArray> {
        @Override
        protected JSONArray doInBackground(ApiConnector... params) {
            return params[0].GetCustomerDetails(myApplication.getUserId());
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {

            try {
                JSONObject objJson = null;
                objJson = jsonArray.getJSONObject(0);
                final String user_id = objJson.getString("id");
                final String name = objJson.getString("name");
                final String email = objJson.getString("email");
                final String user_image = objJson.getString("imageName");
                final String password = objJson.getString("password");

                txt_username.setText(name);
                txt_email.setText(email);

                if (user_image.equals("")) {
                    img_profile.setImageResource(R.drawable.ic_user_account);
                } else {
                    Picasso.with(getActivity())
                            .load(Config.ADMIN_PANEL_URL + "/upload/avatar/" + user_image.replace(" ", "%20"))
                            .resize(300, 300)
                            .centerCrop()

                            .into(img_profile);
                }

                txt_edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), ActivityProfile.class);
                        intent.putExtra("name", name);
                        intent.putExtra("email", email);
                        intent.putExtra("user_image", user_image);
                        intent.putExtra("password", password);
                        startActivity(intent);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private List<Data> getDataInformation() {

        List<Data> data = new ArrayList<>();

        data.add(new Data(
                R.drawable.ic_drawer_privacy,
                getResources().getString(R.string.title_about_privacy),
                getResources().getString(R.string.sub_title_about_privacy)
        ));

        data.add(new Data(
                R.drawable.ic_drawer_rate,
                getResources().getString(R.string.title_about_rate),
                getResources().getString(R.string.sub_title_about_rate)
        ));

        data.add(new Data(
                R.drawable.ic_drawer_more,
                getResources().getString(R.string.title_about_more),
                getResources().getString(R.string.sub_title_about_more)
        ));

        data.add(new Data(
                R.drawable.ic_drawer_info,
                getResources().getString(R.string.title_about_info),
                ""
        ));

        return data;
    }

    public class Data {
        private int image;
        private String title;
        private String sub_title;

        public int getImage() {
            return image;
        }

        public String getTitle() {
            return title;
        }

        public String getSub_title() {
            return sub_title;
        }

        public Data(int image, String title, String sub_title) {
            this.image = image;
            this.title = title;
            this.sub_title = sub_title;
        }
    }

    public void aboutDialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getActivity());
        View mView = layoutInflaterAndroid.inflate(R.layout.custom_dialog_about, null);

        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setView(mView);
        alert.setCancelable(false);
        alert.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

}
