package com.example.storekeep.util;

import android.content.Context;

import com.example.storekeep.R;

import java.text.NumberFormat;
import java.util.Locale;

/** Отображение сум в узбекских сўм (UZS). */
public final class MoneyFormat {

    private MoneyFormat() {}

    public static String som(Context context, double amount) {
        NumberFormat nf = NumberFormat.getIntegerInstance(new Locale("uz", "UZ"));
        String num = nf.format(Math.round(amount));
        return context.getString(R.string.price_som, num);
    }
}
