package com.example.storekeep;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.storekeep.data.DatabaseHelper;
import com.example.storekeep.data.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SessionManager.isLoggedIn(this)) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_login);

        TextInputEditText editLogin = findViewById(R.id.edit_login);
        TextInputEditText editPassword = findViewById(R.id.edit_password);
        MaterialButton signIn = findViewById(R.id.button_sign_in);
        MaterialButton openRegister = findViewById(R.id.button_register);
        DatabaseHelper db = new DatabaseHelper(this);

        signIn.setOnClickListener(v -> {
            String login = String.valueOf(editLogin.getText()).trim();
            String password = String.valueOf(editPassword.getText());
            if (login.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.error_required, Toast.LENGTH_SHORT).show();
                return;
            }
            if (db.authenticate(login, password)) {
                SessionManager.setLoggedIn(this, true);
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            } else {
                Toast.makeText(this, R.string.error_auth, Toast.LENGTH_SHORT).show();
            }
        });

        openRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }
}
