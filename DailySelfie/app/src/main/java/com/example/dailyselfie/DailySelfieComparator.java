package com.example.dailyselfie;

import java.util.Comparator;

/**
 * Created by jb-edu on 15-07-03.
 */
public class DailySelfieComparator implements Comparator<DailySelfie> {

    public int compare (DailySelfie lhs, DailySelfie rhs) {

        long lhsDate = lhs.getDate().getTime();
        long rhsDate = rhs.getDate().getTime();

        if (lhsDate < rhsDate) {
            return -1;
        }
        else if (lhsDate > rhsDate) {
            return 1;
        }

        return 0;
    }
}
