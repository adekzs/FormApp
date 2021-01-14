package com.adeks.formapp;

import okhttp3.ResponseBody;

public interface ProcessResult {
    void onProcessCompleted();
    void onProcessFailed(String t);
}
