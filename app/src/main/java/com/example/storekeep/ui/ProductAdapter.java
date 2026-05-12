package com.example.storekeep.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storekeep.R;
import com.example.storekeep.model.Product;
import com.example.storekeep.util.MoneyFormat;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.VH> {

    public interface Listener {
        void onEdit(Product product);

        void onDelete(Product product);
    }

    private final List<Product> items = new ArrayList<>();
    private final Listener listener;

    public ProductAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<Product> products) {
        items.clear();
        if (products != null) items.addAll(products);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Product p = items.get(position);
        h.name.setText(p.name);
        h.price.setText(MoneyFormat.som(h.itemView.getContext(), p.price));
        h.qty.setText(h.itemView.getContext().getString(R.string.label_qty_value, p.quantity));
        h.category.setText(p.category.isEmpty() ? "—" : p.category);
        h.btnEdit.setOnClickListener(v -> listener.onEdit(p));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(p));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView price;
        final TextView qty;
        final TextView category;
        final MaterialButton btnEdit;
        final MaterialButton btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_name);
            price = itemView.findViewById(R.id.text_price);
            qty = itemView.findViewById(R.id.text_quantity);
            category = itemView.findViewById(R.id.text_category);
            btnEdit = itemView.findViewById(R.id.button_edit);
            btnDelete = itemView.findViewById(R.id.button_delete);
        }
    }
}
