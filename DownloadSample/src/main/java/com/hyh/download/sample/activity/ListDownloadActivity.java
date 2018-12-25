package com.hyh.download.sample.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.hyh.download.sample.bean.DownloadBean;

import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2018/12/25
 */

public class ListDownloadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

    }


    private static class DownloadListAdapter extends RecyclerView.Adapter<DownloadHolder> {

        private Context mContext;

        private List<DownloadBean> mDownloadBeanList;

        public DownloadListAdapter(Context context, List<DownloadBean> downloadBeanList) {
            mContext = context;
            mDownloadBeanList = downloadBeanList;
        }

        @Override
        public DownloadHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(DownloadHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }

    private static class DownloadHolder extends RecyclerView.ViewHolder {

        public DownloadHolder(View itemView) {
            super(itemView);
        }
    }

}
