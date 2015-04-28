package com.example.modernartui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;

/**
 * Created by jb-edu on 15-04-04.
 *
 * A simple activity that uses a custom view to draw colored rectangles on the screen and updates
 * the positions and colors of the rectangles based on the user input via the seek bar. It includes
 * a customized alert dialog that allows the user to launch a browser taking them to the Museum of
 * Modern Art website (www.MOMA.org).
 *
 * NOTE: various implementation options were explored, such as using ImageView and direct
 * manipulation of Bitmaps, instead of a custom View and OpenGL (as well as several other options).
 * A custom View approach was chosen for the trade-off between ease of implementation / maintenance
 * and performance on redrawing rectangles.
 */
public class ModernArtUIActivity extends ActionBarActivity {

    private final String MOMA_URL = "http://www.moma.org";
    private int mProgress = 0;
    private int mProgressChange = 0;
    private SeekBar mSeekBar = null;
    private boolean mSeekBarMaxIsSet = false;
    public SeekBarRectView mSeekBarRectView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgress = 0;
        mProgressChange = 0;

        mSeekBar = (SeekBar) findViewById(R.id.seekBarID);
        mSeekBar.setBackgroundColor(Color.BLACK);
        mSeekBar.setSaveEnabled(false);

        mSeekBarRectView = (SeekBarRectView) findViewById(R.id.seekBarRectView);
        mSeekBarRectView.setBackgroundColor(Color.BLACK);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mProgressChange = progress - mProgress;
                mProgress = progress;
                mSeekBarRectView.updateRectangles(mProgress, mProgressChange);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (!mSeekBarMaxIsSet) {
                    mSeekBar.setMax(mSeekBarRectView.getCalculatedSeekBarMax());
                    mSeekBarMaxIsSet = true;
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * jb-edu: If the user clicked on the "More Information" menu option,
     * then launch the custom alert dialog.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_more_info) {

            AlertDialog.Builder builder = new AlertDialog.Builder(ModernArtUIActivity.this);
            LayoutInflater inflater = ModernArtUIActivity.this.getLayoutInflater();

            builder.setView(inflater.inflate(R.layout.custom_dialog, null)).
                    // Add the buttons
                    setPositiveButton(
                            R.string.dialog_not_now, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            }).setNegativeButton(
                    R.string.dialog_visit_moma, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // jb-edu: Create an intent to launch a browser to
                            // to view the MOMA website
                            Intent momaIntent =
                                    new Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(MOMA_URL));

                            String title = getResources().getString(R.string.chooser_title);
                            Intent chooser = Intent.createChooser(momaIntent, title);

                            if (momaIntent.resolveActivity(getPackageManager()) != null) {
                                startActivity(chooser);
                                dialog.dismiss();
                            }
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }
}
