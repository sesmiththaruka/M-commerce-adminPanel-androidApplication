package lk.jiat.xpectadmin.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;

import lk.jiat.xpectadmin.MainActivity;
import lk.jiat.xpectadmin.R;
import lk.jiat.xpectadmin.fragment.DashboardFragment;

public class AdminHome extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, NavigationBarView.OnItemSelectedListener {
    public static final String TAG = MainActivity.class.getName();
    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        drawerLayout = findViewById(R.id.drawerLayout);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        bottomNavigationView.setOnItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.bottomNavHome) {
            Log.i(TAG, "sh");
            loadFragment(new DashboardFragment());
        }
        return true;
    }

    public void loadFragment(Fragment fragment) {
        if (fragment instanceof DashboardFragment) {
            FragmentManager supportFragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container, fragment);
            fragmentTransaction.commit();
        }
    }
}


