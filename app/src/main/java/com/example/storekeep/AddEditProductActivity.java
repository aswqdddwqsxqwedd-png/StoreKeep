package com.example.storekeep;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.storekeep.data.DatabaseHelper;
import com.example.storekeep.model.Product;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AddEditProductActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "product_id";

    private long productId = -1;
    private int previousQuantity = 0;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_product);
        db = new DatabaseHelper(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        TextInputEditText editName = findViewById(R.id.edit_name);
        TextInputEditText editPrice = findViewById(R.id.edit_price);
        TextInputEditText editQty = findViewById(R.id.edit_quantity);
        TextInputEditText editCat = findViewById(R.id.edit_category);
        MaterialButton save = findViewById(R.id.button_save);

        productId = getIntent().getLongExtra(EXTRA_PRODUCT_ID, -1);
        if (productId != -1) {
            if (ab != null) ab.setTitle(R.string.title_edit_product);
            Product p = db.getProduct(productId);
            if (p != null) {
                editName.setText(p.name);
                editPrice.setText(String.valueOf(p.price));
                editQty.setText(String.valueOf(p.quantity));
                editCat.setText(p.category);
                previousQuantity = p.quantity;
            }
        } else {
            if (ab != null) ab.setTitle(R.string.title_add_product);
        }

        save.setOnClickListener(v -> {
            String name = String.valueOf(editName.getText()).trim();
            String priceRaw = String.valueOf(editPrice.getText()).trim().replace(',', '.');
            String qtyRaw = String.valueOf(editQty.getText()).trim();
            String cat = String.valueOf(editCat.getText()).trim();

            if (name.isEmpty() || priceRaw.isEmpty() || qtyRaw.isEmpty()) {
                Toast.makeText(this, R.string.error_required, Toast.LENGTH_SHORT).show();
                return;
            }
            double price;
            int qty;
            try {
                price = Double.parseDouble(priceRaw);
                qty = Integer.parseInt(qtyRaw);
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.error_number, Toast.LENGTH_SHORT).show();
                return;
            }
            if (qty < 0) {
                Toast.makeText(this, R.string.error_number, Toast.LENGTH_SHORT).show();
                return;
            }

            if (productId == -1) {
                db.insertProduct(name, price, qty, cat);
            } else {
                db.updateProduct(productId, name, price, qty, cat, previousQuantity);
            }
            finish();
        });
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
