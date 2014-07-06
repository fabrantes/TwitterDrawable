package org.abrantix.twitterdrawable;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;


public class TwitterDrawableActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ImageView v = (ImageView) findViewById(R.id.twitter_icon);
        final TwitterDrawable td = new TwitterDrawable();
        v.setImageDrawable(td);

        v.postDelayed(new Runnable() {
            @Override
            public void run() {
                td.setFlying(true);
            }
        }, 2999);

        v.postDelayed(new Runnable() {
            @Override
            public void run() {
                td.setFlying(false);
            }
        }, 6999);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.twitter_drawable_example, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
