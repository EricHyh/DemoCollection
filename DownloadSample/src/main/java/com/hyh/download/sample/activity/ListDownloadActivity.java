package com.hyh.download.sample.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hyh.download.Callback;
import com.hyh.download.DownloadInfo;
import com.hyh.download.FileDownloader;
import com.hyh.download.State;
import com.hyh.download.sample.bean.DownloadBean;
import com.hyh.download.sample.model.TestDownlodModel;
import com.hyh.download.sample.widget.ProgressButton;
import com.yly.mob.ssp.downloadsample.R;

import java.text.DecimalFormat;
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
        recyclerView.setAdapter(new DownloadListAdapter(this, new TestDownlodModel().getDownloadBeanList()));
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
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_download_list, parent, false);
            return new DownloadHolder(view);
        }

        @Override
        public void onBindViewHolder(DownloadHolder holder, int position) {
            holder.bindViewHolder(mDownloadBeanList.get(position));
        }

        @Override
        public int getItemCount() {
            return mDownloadBeanList == null ? 0 : mDownloadBeanList.size();
        }
    }

    private static class DownloadHolder extends RecyclerView.ViewHolder implements Callback, View.OnClickListener {

        private final TextView mTitle;
        private final TextView mSize;
        private final TextView mSpeed;
        private final ProgressButton mProgress;
        private DownloadBean mOldDownloadBean;
        private int mDownloadStatus;

        DownloadHolder(View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.item_download_tv_title);
            mSize = itemView.findViewById(R.id.item_download_tv_size);
            mSpeed = itemView.findViewById(R.id.item_download_tv_speed);
            mProgress = itemView.findViewById(R.id.item_download_pb_progress);

            mProgress.setOnClickListener(this);
        }

        void bindViewHolder(DownloadBean downloadBean) {
            if (mOldDownloadBean != null) {
                String url = mOldDownloadBean.url;
                FileDownloader.getInstance().removeDownloadListener(url, this);
            }
            mOldDownloadBean = downloadBean;
            mTitle.setText(downloadBean.title);

            String url = downloadBean.url;
            DownloadInfo downloadInfo = FileDownloader.getInstance().getDownloadInfo(url);

            if (downloadInfo != null) {
                mDownloadStatus = downloadInfo.getCurrentStatus();
                mProgress.setProgress(downloadInfo.getProgress());
                mSpeed.setText(getSpeedStr(downloadInfo));
                String text = "下载";
                switch (mDownloadStatus) {
                    case State.NONE: {
                        text = "下载";
                        break;
                    }
                    case State.PREPARE: {
                        text = "暂停";
                        break;
                    }
                    case State.WAITING_IN_QUEUE: {
                        text = "暂停";
                        break;
                    }
                    case State.DOWNLOADING: {
                        text = "暂停";
                        break;
                    }
                    case State.DELETE: {
                        text = "下载";
                        break;
                    }
                    case State.PAUSE: {
                        text = "继续";
                        break;
                    }
                    case State.SUCCESS: {
                        text = "成功";
                        break;
                    }
                    case State.WAITING_FOR_WIFI: {
                        text = "等待wifi";
                        break;
                    }
                    case State.LOW_DISK_SPACE: {
                        text = "空间不足";
                        break;
                    }
                    case State.FAILURE: {
                        text = "重试";
                        break;
                    }
                }
                mProgress.setText(text);
            } else {
                mProgress.setProgress(0);
                mSpeed.setText(0.0 + "K/s");
                mProgress.setText("下载");
            }

            FileDownloader.getInstance().addDownloadListener(url, this);
        }

        @Override
        public void onPrepare(DownloadInfo downloadInfo) {
            mDownloadStatus = downloadInfo.getCurrentStatus();
            mProgress.setText("暂停");
            mProgress.setProgress(downloadInfo.getProgress());
            mSpeed.setText(getSpeedStr(downloadInfo));
        }

        @Override
        public void onDownloading(DownloadInfo downloadInfo) {
            mDownloadStatus = downloadInfo.getCurrentStatus();
            mProgress.setText("暂停");
            mProgress.setProgress(downloadInfo.getProgress());
            long totalSize = downloadInfo.getTotalSize();
            float mb = totalSize / 1024.0f / 1024.0f;
            mSize.setText(mb + " MB");
            mSpeed.setText(getSpeedStr(downloadInfo));
        }

        @Override
        public void onWaitingInQueue(DownloadInfo downloadInfo) {
            mDownloadStatus = downloadInfo.getCurrentStatus();
            mProgress.setText("暂停");
            mProgress.setProgress(downloadInfo.getProgress());
            mSpeed.setText(getSpeedStr(downloadInfo));
        }

        @Override
        public void onPause(DownloadInfo downloadInfo) {
            mDownloadStatus = downloadInfo.getCurrentStatus();
            mProgress.setText("继续");
            mProgress.setProgress(downloadInfo.getProgress());
            mSpeed.setText(getSpeedStr(downloadInfo));
        }

        @Override
        public void onDelete(DownloadInfo downloadInfo) {
            mDownloadStatus = downloadInfo.getCurrentStatus();
            mProgress.setText("下载");
            mProgress.setProgress(downloadInfo.getProgress());
            mSpeed.setText(getSpeedStr(downloadInfo));
        }

        @Override
        public void onSuccess(DownloadInfo downloadInfo) {
            mDownloadStatus = downloadInfo.getCurrentStatus();
            mProgress.setText("成功");
            mProgress.setProgress(downloadInfo.getProgress());
            mSpeed.setText(getSpeedStr(downloadInfo));
        }

        @Override
        public void onWaitingForWifi(DownloadInfo downloadInfo) {
            mDownloadStatus = downloadInfo.getCurrentStatus();
            mProgress.setText("等待wifi");
            mProgress.setProgress(downloadInfo.getProgress());
            mSpeed.setText(getSpeedStr(downloadInfo));
        }

        @Override
        public void onLowDiskSpace(DownloadInfo downloadInfo) {
            mDownloadStatus = downloadInfo.getCurrentStatus();
            mProgress.setText("空间不足");
            mProgress.setProgress(downloadInfo.getProgress());
            mSpeed.setText(getSpeedStr(downloadInfo));
        }

        @Override
        public void onFailure(DownloadInfo downloadInfo) {
            mDownloadStatus = downloadInfo.getCurrentStatus();
            mProgress.setText("重试");
            mProgress.setProgress(downloadInfo.getProgress());
            mSpeed.setText(getSpeedStr(downloadInfo));
        }

        @Override
        public void onHaveNoTask() {

        }

        @Override
        public void onClick(View v) {
            switch (mDownloadStatus) {
                case State.NONE: {
                    startDownload(mOldDownloadBean);
                    break;
                }
                case State.PREPARE: {
                    FileDownloader.getInstance().pauseTask(mOldDownloadBean.url);
                    break;
                }
                case State.WAITING_IN_QUEUE: {
                    FileDownloader.getInstance().pauseTask(mOldDownloadBean.url);
                    break;
                }
                case State.DOWNLOADING: {
                    FileDownloader.getInstance().pauseTask(mOldDownloadBean.url);
                    break;
                }
                case State.DELETE: {
                    startDownload(mOldDownloadBean);
                    break;
                }
                case State.PAUSE: {
                    startDownload(mOldDownloadBean);
                    break;
                }
                case State.SUCCESS: {
                    Toast.makeText(mTitle.getContext(), "已下载完成", Toast.LENGTH_SHORT).show();
                    break;
                }
                case State.WAITING_FOR_WIFI: {
                    startDownload(mOldDownloadBean);
                    break;
                }
                case State.LOW_DISK_SPACE: {
                    Toast.makeText(mTitle.getContext(), "存储空间不足", Toast.LENGTH_SHORT).show();
                    break;
                }
                case State.FAILURE: {
                    startDownload(mOldDownloadBean);
                    break;
                }
            }
        }

        private void startDownload(DownloadBean downloadBean) {
            String url = downloadBean.url;
            DownloadInfo downloadInfo = FileDownloader.getInstance().getDownloadInfo(url);

            if (downloadInfo != null) {
                mDownloadStatus = downloadInfo.getCurrentStatus();
                String text = "下载";
                switch (mDownloadStatus) {
                    case State.NONE: {
                        text = "下载";
                        break;
                    }
                    case State.PREPARE: {
                        text = "暂停";
                        break;
                    }
                    case State.WAITING_IN_QUEUE: {
                        text = "暂停";
                        break;
                    }
                    case State.DOWNLOADING: {
                        text = "暂停";
                        break;
                    }
                    case State.DELETE: {
                        text = "下载";
                        break;
                    }
                    case State.PAUSE: {
                        text = "继续";
                        break;
                    }
                    case State.SUCCESS: {
                        text = "成功";
                        break;
                    }
                    case State.WAITING_FOR_WIFI: {
                        text = "等待wifi";
                        break;
                    }
                    case State.LOW_DISK_SPACE: {
                        text = "空间不足";
                        break;
                    }
                    case State.FAILURE: {
                        text = "重试";
                        break;
                    }
                }
                mProgress.setText(text);
                int progress = downloadInfo.getProgress();
                mProgress.setProgress(progress);
                mSpeed.setText(getSpeedStr(downloadInfo));
            } else {
                mProgress.setProgress(0);
                mSpeed.setText(0.0 + "K/s");
                mProgress.setText("下载");
            }

            FileDownloader.getInstance().addDownloadListener(url, this);
            FileDownloader.getInstance().startTask(url);
        }

        @NonNull
        private String getSpeedStr(DownloadInfo downloadInfo) {
            float speed = downloadInfo.getSpeed();
            if (speed >= 1024) {
                DecimalFormat decimalFormat = new DecimalFormat("0.0");
                return decimalFormat.format(speed / 1024.0f) + "M/s";
            } else {
                DecimalFormat decimalFormat = new DecimalFormat("0.0");
                return decimalFormat.format(speed) + "K/s";
            }
        }
    }
}
