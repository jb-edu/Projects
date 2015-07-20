package com.example.dailyselfie;

import java.util.Date;

/**
 * Created by jb-edu on 15-06-23.
 */
public class DailySelfie {

    private String mImagePath = null;
    private Date mDate = null;
    private boolean mChecked = false;

    public DailySelfie(String imagePath, Date date) {
        this.mImagePath = imagePath;
        this.mDate = date;
    }

    public String getImagePath() {
        return mImagePath;
    }

    public void setImagePath(String imagePath) {
        this.mImagePath = imagePath;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        this.mDate = date;
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean mChecked) {
        this.mChecked = mChecked;
    }
}
