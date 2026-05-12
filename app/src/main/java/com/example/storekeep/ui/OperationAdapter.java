package com.example.storekeep.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storekeep.R;
import com.example.storekeep.model.StockOperation;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OperationAdapter extends RecyclerView.Adapter<OperationAdapter.VH> {

    private final List<StockOperation> items = new ArrayList<>();
    private final DateFormat dateFormat;
    private final Locale locale;

    public OperationAdapter(Locale locale) {
        this.locale = locale != null ? locale : Locale.getDefault();
        this.dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, this.locale);
    }

    public void setItems(List<StockOperation> operations) {
        items.clear();
        if (operations != null) items.addAll(operations);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_operation, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        StockOperation o = items.get(position);
        h.product.setText(o.productName);
        h.type.setText(o.type);
        h.amount.setText(h.itemView.getContext().getString(R.string.label_amount_value, o.amount));
        h.date.setText(dateFormat.format(new Date(o.dateMillis)));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView product;
        final TextView type;
        final TextView amount;
        final TextView date;

        VH(@NonNull View itemView) {
            super(itemView);
            product = itemView.findViewById(R.id.text_product);
            type = itemView.findViewById(R.id.text_type);
            amount = itemView.findViewById(R.id.text_amount);
            date = itemView.findViewById(R.id.text_date);
        }
    }
}
