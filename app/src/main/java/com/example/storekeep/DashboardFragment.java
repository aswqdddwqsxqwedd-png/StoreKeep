package com.example.storekeep;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.storekeep.data.SessionManager;
import com.google.android.material.button.MaterialButton;

public class DashboardFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_products).setOnClickListener(v -> openHome(R.id.nav_products));
        view.findViewById(R.id.btn_add_product).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddEditProductActivity.class)));
        view.findViewById(R.id.btn_sales).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SaleActivity.class)));
        view.findViewById(R.id.btn_stock).setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), CatalogActivity.class);
            startActivity(i);
        });
        view.findViewById(R.id.btn_reports).setOnClickListener(v -> openHome(R.id.nav_reports));

        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            SessionManager.setLoggedIn(requireContext(), false);
            startActivity(new Intent(requireContext(), LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            requireActivity().finish();
        });
    }

    private void openHome(int tabId) {
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).navigateTo(tabId);
        }
    }
}
