package com.example.storekeep;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storekeep.data.DatabaseHelper;
import com.example.storekeep.model.Product;
import com.example.storekeep.ui.ProductAdapter;
import com.google.android.material.appbar.MaterialToolbar;


public class CatalogActivity extends AppCompatActivity implements ProductAdapter.Listener {

    private DatabaseHelper db;
    private ProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(R.string.title_warehouse);
        }
        db = new DatabaseHelper(this);
        RecyclerView recycler = findViewById(R.id.recycler_catalog);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(this);
        recycler.setAdapter(adapter);
        load();
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        adapter.setItems(db.getProducts(null));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEdit(Product product) {
        Intent i = new Intent(this, AddEditProductActivity.class);
        i.putExtra(AddEditProductActivity.EXTRA_PRODUCT_ID, product.id);
        startActivity(i);
    }

    @Override
    public void onDelete(Product product) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (d, w) -> {
                    db.deleteProduct(product.id);
                    load();
                })
                .show();
    }
}
