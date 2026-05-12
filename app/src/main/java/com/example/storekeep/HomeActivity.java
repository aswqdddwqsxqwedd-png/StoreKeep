package com.example.storekeep;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.storekeep.data.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SessionManager.isLoggedIn(this)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_home);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bottomNav = findViewById(R.id.bottom_nav);

        bottomNav.setOnItemSelectedListener(item -> {
            showFragmentFor(item.getItemId());
            return true;
        });

        if (savedInstanceState == null) {
            showFragmentFor(R.id.nav_home);
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    /** Переключение вкладки нижнего меню (например с главного экрана). */
    public void navigateTo(int itemId) {
        bottomNav.setSelectedItemId(itemId);
    }

    private void showFragmentFor(int itemId) {
        Fragment f;
        int title = R.string.title_home;
        if (itemId == R.id.nav_home) {
            f = new DashboardFragment();
            title = R.string.title_home;
        } else if (itemId == R.id.nav_products) {
            f = new ProductsFragment();
            title = R.string.title_product_list;
        } else if (itemId == R.id.nav_history) {
            f = new HistoryFragment();
            title = R.string.title_history;
        } else if (itemId == R.id.nav_reports) {
            f = new ReportsFragment();
            title = R.string.title_reports;
        } else {
            f = new DashboardFragment();
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, f)
                .commit();
        toolbar.setTitle(title);
    }
}
