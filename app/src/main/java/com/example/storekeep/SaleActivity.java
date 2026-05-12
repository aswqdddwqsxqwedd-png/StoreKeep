package com.example.storekeep;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.storekeep.data.DatabaseHelper;
import com.example.storekeep.model.Product;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class SaleActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private Spinner spinner;
    private List<Product> products = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale);
        db = new DatabaseHelper(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        spinner = findViewById(R.id.spinner_product);
        TextInputEditText editQty = findViewById(R.id.edit_qty);
        MaterialButton sell = findViewById(R.id.button_sell);

        sell.setOnClickListener(v -> {
            if (products.isEmpty()) {
                Toast.makeText(this, R.string.error_required, Toast.LENGTH_SHORT).show();
                return;
            }
            int pos = spinner.getSelectedItemPosition();
            if (pos < 0 || pos >= products.size()) return;
            Product p = products.get(pos);
            int amount;
            try {
                amount = Integer.parseInt(String.valueOf(editQty.getText()).trim());
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.error_number, Toast.LENGTH_SHORT).show();
                return;
            }
            if (amount <= 0) {
                Toast.makeText(this, R.string.sale_fail_qty, Toast.LENGTH_SHORT).show();
                return;
            }
            if (db.sellProduct(p.id, amount)) {
                Toast.makeText(this, R.string.sale_ok, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, R.string.sale_fail_stock, Toast.LENGTH_SHORT).show();
                bindSpinner();
            }
        });

        bindSpinner();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindSpinner();
    }

    private void bindSpinner() {
        products = db.getProducts(null);
        List<String> labels = new ArrayList<>();
        for (Product p : products) {
            labels.add(p.name + " (" + p.quantity + ")");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
