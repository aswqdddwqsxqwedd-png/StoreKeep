package com.example.storekeep;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storekeep.data.DatabaseHelper;
import com.example.storekeep.model.Product;
import com.example.storekeep.ui.ProductAdapter;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class ProductsFragment extends Fragment implements ProductAdapter.Listener {

    private DatabaseHelper db;
    private ProductAdapter adapter;
    private TextInputEditText editSearch;
    private MaterialAutoCompleteTextView dropdownCategory;

    private String searchQuery = "";
    /** null = все категории */
    private String selectedCategory = null;
    private final List<String> categoryItems = new ArrayList<>();
    private ArrayAdapter<String> categoryAdapter;
    private boolean dropdownBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_products, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = new DatabaseHelper(requireContext());
        editSearch = view.findViewById(R.id.edit_search);
        dropdownCategory = view.findViewById(R.id.dropdown_category);
        RecyclerView recycler = view.findViewById(R.id.recycler_products);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ProductAdapter(this);
        recycler.setAdapter(adapter);

        categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                categoryItems);
        dropdownCategory.setAdapter(categoryAdapter);
        dropdownCategory.setKeyListener(null);
        dropdownCategory.setOnClickListener(v -> dropdownCategory.showDropDown());
        dropdownCategory.setOnItemClickListener((parent, v, position, id) -> {
            if (dropdownBinding) return;
            applyCategorySelection(position);
        });

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s != null ? s.toString() : "";
                load();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        bindCategoryDropdown();
        load();
    }

    @Override
    public void onResume() {
        super.onResume();
        bindCategoryDropdown();
        load();
    }

    private void bindCategoryDropdown() {
        List<String> fromDb = db.getDistinctCategories();
        categoryItems.clear();
        categoryItems.add(getString(R.string.all_categories));
        categoryItems.addAll(fromDb);
        categoryAdapter.notifyDataSetChanged();

        dropdownBinding = true;
        if (selectedCategory == null) {
            dropdownCategory.setText(categoryItems.get(0), false);
        } else {
            int idx = categoryItems.indexOf(selectedCategory);
            if (idx >= 0) {
                dropdownCategory.setText(categoryItems.get(idx), false);
            } else {
                selectedCategory = null;
                dropdownCategory.setText(categoryItems.get(0), false);
            }
        }
        dropdownBinding = false;
    }

    private void applyCategorySelection(int position) {
        if (position <= 0) {
            selectedCategory = null;
        } else if (position < categoryItems.size()) {
            selectedCategory = categoryItems.get(position);
        } else {
            selectedCategory = null;
        }
        load();
    }

    private void load() {
        String q = searchQuery.trim();
        adapter.setItems(db.getProducts(q.isEmpty() ? null : q, selectedCategory));
    }

    @Override
    public void onEdit(Product product) {
        Intent i = new Intent(requireContext(), AddEditProductActivity.class);
        i.putExtra(AddEditProductActivity.EXTRA_PRODUCT_ID, product.id);
        startActivity(i);
    }

    @Override
    public void onDelete(Product product) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (d, w) -> {
                    db.deleteProduct(product.id);
                    bindCategoryDropdown();
                    load();
                })
                .show();
    }
}
