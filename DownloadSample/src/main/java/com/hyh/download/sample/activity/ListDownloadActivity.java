package com.hyh.download.sample.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hyh.download.sample.bean.DownloadBean;
import com.hyh.download.sample.model.TestDownlodModel;
import com.hyh.download.sample.presenter.DownloadItemPresenter;
import com.hyh.download.sample.presenter.IDownloadItemPresenter;
import com.hyh.download.sample.view.DownloadItemView;
import com.hyh.download.sample.view.IDownloadItemView;
import com.yly.mob.ssp.downloadsample.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2018/12/25
 */

public class ListDownloadActivity extends AppCompatActivity {

    private static final String TAG = "ListDownloadActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(new DownloadListAdapter(this, new TestDownlodModel().getDownloadBeanList()));
    }


    private static class DownloadListAdapter extends RecyclerView.Adapter<DownloadHolder> {

        private Context mContext;

        private List<IDownloadItemPresenter> mDownloadItemPresenters;

        DownloadListAdapter(Context context, List<DownloadBean> downloadBeanList) {
            mContext = context;
            if (downloadBeanList != null && !downloadBeanList.isEmpty()) {
                mDownloadItemPresenters = new ArrayList<>(downloadBeanList.size());
                for (DownloadBean downloadBean : downloadBeanList) {
                    mDownloadItemPresenters.add(new DownloadItemPresenter(mContext, downloadBean));
                }
            }
        }

        @Override
        public DownloadHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_download_list, parent, false);
            return new DownloadHolder(view);
        }

        @Override
        public void onBindViewHolder(DownloadHolder holder, int position) {
            Log.d(TAG, "onBindViewHolder: position = " + position);
            holder.bindViewHolder(mDownloadItemPresenters.get(position));
        }

        @Override
        public void onViewAttachedToWindow(DownloadHolder holder) {
            super.onViewAttachedToWindow(holder);
        }

        @Override
        public void onViewDetachedFromWindow(DownloadHolder holder) {
            super.onViewDetachedFromWindow(holder);
        }

        @Override
        public int getItemCount() {
            return mDownloadItemPresenters == null ? 0 : mDownloadItemPresenters.size();
        }
    }

    private static class DownloadHolder extends RecyclerView.ViewHolder {

        private final IDownloadItemView mDownloadItemView;

        DownloadHolder(View itemView) {
            super(itemView);
            mDownloadItemView = new DownloadItemView(itemView);
        }

        void bindViewHolder(IDownloadItemPresenter downloadItemPresenter) {
            downloadItemPresenter.bindDownloadItemView(mDownloadItemView);
        }
    }
}
