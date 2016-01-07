package abysmel.com.collapsibletoolbarhelper;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import abysmel.com.collapsibletoolbarhelper.widgets.MetaballMenu;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        MetaballMenu menu = (MetaballMenu) findViewById(R.id.metaball_menu);

        menu.setElevationRequired(true);

    }

}
