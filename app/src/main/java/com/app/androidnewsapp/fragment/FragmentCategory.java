package com.app.androidnewsapp.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.androidnewsapp.Config;
import com.app.androidnewsapp.R;
import com.app.androidnewsapp.activities.ActivityCategoryDetails;
import com.app.androidnewsapp.activities.MainActivity;
import com.app.androidnewsapp.adapter.AdapterCategory;
import com.app.androidnewsapp.callbacks.CallbackCategories;
import com.app.androidnewsapp.models.Category;
import com.app.androidnewsapp.rests.ApiInterface;
import com.app.androidnewsapp.rests.RestAdapter;
import com.app.androidnewsapp.utils.Constant;
import com.app.androidnewsapp.utils.NetworkCheck;
import com.app.androidnewsapp.utils.Tools;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentCategory extends Fragment {

    private View root_view, parent_view;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipe_refresh;
    private AdapterCategory mAdapter;
    private Call<CallbackCategories> callbackCall = null;
    private InterstitialAd interstitialAd;
    int counter = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_category, null);
        parent_view = getActivity().findViewById(R.id.main_content);

        loadInterstitialAd();

        swipe_refresh = root_view.findViewById(R.id.swipe_refresh_layout_category);
        swipe_refresh.setColorSchemeResources(R.color.colorOrange, R.color.colorGreen, R.color.colorBlue, R.color.colorRed);
        recyclerView = root_view.findViewById(R.id.recyclerViewCategory);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, LinearLayoutManager.VERTICAL));
        recyclerView.setHasFixedSize(true);

        //set data and list adapter
        mAdapter = new AdapterCategory(getActivity(), new ArrayList<Category>());
        recyclerView.setAdapter(mAdapter);

        if (Config.ENABLE_RTL_MODE) {
            recyclerView.setRotationY(180);
        }

        // on item list clicked
        mAdapter.setOnItemClickListener(new AdapterCategory.OnItemClickListener() {
            @Override
            public void onItemClick(View v, Category obj, int position) {
                ActivityCategoryDetails.navigate((MainActivity) getActivity(), v.findViewById(R.id.lyt_parent), obj);
                showInterstitialAd();
            }
        });

        // on swipe list
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mAdapter.resetListData();
                requestAction();
            }
        });

        requestAction();

        return root_view;
    }

    private void displayApiResult(final List<Category> categories) {
        mAdapter.setListData(categories);
        swipeProgress(false);
        if (categories.size() == 0) {
            showNoItemView(true);
        }
    }

    private void requestCategoriesApi() {
        ApiInterface apiInterface = RestAdapter.createAPI();
        callbackCall = apiInterface.getAllCategories();
        callbackCall.enqueue(new Callback<CallbackCategories>() {
            @Override
            public void onResponse(Call<CallbackCategories> call, Response<CallbackCategories> response) {
                CallbackCategories resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    displayApiResult(resp.categories);
                } else {
                    onFailRequest();
                }
            }

            @Override
            public void onFailure(Call<CallbackCategories> call, Throwable t) {
                if (!call.isCanceled()) onFailRequest();
            }

        });
    }

    private void onFailRequest() {
        swipeProgress(false);
        if (NetworkCheck.isConnect(getActivity())) {
            showFailedView(true, getString(R.string.msg_no_network));
        } else {
            showFailedView(true, getString(R.string.msg_offline));
        }
    }

    private void requestAction() {
        showFailedView(false, "");
        swipeProgress(true);
        showNoItemView(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                requestCategoriesApi();
            }
        }, Constant.DELAY_TIME);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        swipeProgress(false);
        if(callbackCall != null && callbackCall.isExecuted()){
            callbackCall.cancel();
        }
    }

    private void showFailedView(boolean flag, String message) {
        View lyt_failed = root_view.findViewById(R.id.lyt_failed_category);
        ((TextView) root_view.findViewById(R.id.failed_message)).setText(message);
        if (flag) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        root_view.findViewById(R.id.failed_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAction();
            }
        });
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = root_view.findViewById(R.id.lyt_no_item_category);
        ((TextView) root_view.findViewById(R.id.no_item_message)).setText(R.string.msg_no_category);
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
            swipe_refresh.setRefreshing(show);
            return;
        }
        swipe_refresh.post(new Runnable() {
            @Override
            public void run() {
                swipe_refresh.setRefreshing(show);
            }
        });
    }

    private void loadInterstitialAd() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS) {
            MobileAds.initialize(getActivity(), getResources().getString(R.string.admob_app_id));
            interstitialAd = new InterstitialAd(getActivity());
            interstitialAd.setAdUnitId(getResources().getString(R.string.admob_interstitial_unit_id));
            interstitialAd.loadAd(Tools.getAdRequest(getActivity()));
            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    interstitialAd.loadAd(new AdRequest.Builder().build());
                }
            });
        }
    }

    private void showInterstitialAd() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS) {
            if (interstitialAd != null && interstitialAd.isLoaded()) {
                if (counter == Config.ADMOB_INTERSTITIAL_ADS_INTERVAL) {
                    interstitialAd.show();
                    counter = 1;
                } else {
                    counter++;
                }
            }
        }
    }

}
