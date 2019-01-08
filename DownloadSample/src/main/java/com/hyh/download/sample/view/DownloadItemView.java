package com.hyh.download.sample.view;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.hyh.download.State;
import com.hyh.download.sample.presenter.IDownloadItemPresenter;
import com.hyh.download.sample.widget.ProgressButton;
import com.yly.mob.ssp.downloadsample.R;

import java.text.DecimalFormat;

/**
 * Created by Eric_He on 2019/1/8.
 */

public class DownloadItemView implements IDownloadItemView {

    private final TextView mTitle;
    private final TextView mName;
    private final TextView mSize;
    private final TextView mSpeed;
    private final ProgressButton mProgressButton;

    private IDownloadItemPresenter mDownloadItemPresenter;

    public DownloadItemView(View itemView) {
        mTitle = itemView.findViewById(R.id.item_download_tv_title);
        mName = itemView.findViewById(R.id.item_download_tv_name);
        mSize = itemView.findViewById(R.id.item_download_tv_size);
        mSpeed = itemView.findViewById(R.id.item_download_tv_speed);
        mProgressButton = itemView.findViewById(R.id.item_download_pb_progress);
    }

    @Override
    public void unBindDownloadItemPresenter(IDownloadItemPresenter downloadItemPresenter) {
        if (mDownloadItemPresenter == downloadItemPresenter) {
            mDownloadItemPresenter = null;
        }
    }

    @Override
    public void bindDownloadItemPresenter(IDownloadItemPresenter downloadItemPresenter) {
        if (mDownloadItemPresenter == downloadItemPresenter) {
            return;
        }
        if (mDownloadItemPresenter != null) {
            mDownloadItemPresenter.unBindDownloadItemView(this);
        }
        mDownloadItemPresenter = downloadItemPresenter;
    }

    @Override
    public void setOnClickListener(View.OnClickListener listener) {
        mProgressButton.setOnClickListener(listener);
    }

    @Override
    public void setTitle(String title) {
        mTitle.setText(title);
    }

    @Override
    public void setDownloadStatus(int downloadStatus) {
        mProgressButton.setText(getProgressButtonText(downloadStatus));
    }

    @Override
    public void setProgress(int progress) {
        mProgressButton.setProgress(progress);
    }

    @Override
    public void setSpeed(float speed) {
        mSpeed.setText(getSpeedStr(speed));
    }

    @Override
    public void setFileName(String fileName) {
        mName.setText(fileName);
    }

    @Override
    public void setTotalSize(long totalSize) {
        mSize.setText(getSizeStr(totalSize));
    }

    @NonNull
    private String getProgressButtonText(int status) {
        String text = "下载";
        switch (status) {
            case State.NONE: {
                text = "下载";
                break;
            }
            case State.PREPARE: {
                text = "准备中";
                break;
            }
            case State.WAITING_START: {
                text = "等待中";
                break;
            }
            case State.WAITING_END: {
                text = "下载中";
                break;
            }
            case State.CONNECTED: {
                text = "下载中";
                break;
            }
            case State.DOWNLOADING: {
                text = "下载中";
                break;
            }
            case State.RETRYING: {
                text = "重试中";
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
            case State.FAILURE: {
                text = "重试";
                break;
            }
        }
        return text;
    }

    @NonNull
    private String getSpeedStr(float speed) {
        if (speed >= 1024) {
            DecimalFormat decimalFormat = new DecimalFormat("0.0");
            return decimalFormat.format(speed / 1024.0f) + "M/s";
        } else {
            DecimalFormat decimalFormat = new DecimalFormat("0.0");
            return decimalFormat.format(speed) + "K/s";
        }
    }


    private String getSizeStr(long totalSize) {
        float mb = totalSize / 1024.0f / 1024.0f;
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return decimalFormat.format(mb) + " MB";
    }
}
