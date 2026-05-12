package com.example.storekeep;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.storekeep.data.DatabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        DatabaseHelper db = new DatabaseHelper(this);

        TextInputEditText editNick = findViewById(R.id.edit_nickname);
        TextInputEditText editEmail = findViewById(R.id.edit_email);
        TextInputEditText editPhone = findViewById(R.id.edit_phone);
        TextInputEditText editPass = findViewById(R.id.edit_password);
        MaterialButton btn = findViewById(R.id.button_register);

        btn.setOnClickListener(v -> {
            String nick = String.valueOf(editNick.getText());
            String email = String.valueOf(editEmail.getText());
            String phone = String.valueOf(editPhone.getText());
            String pass = String.valueOf(editPass.getText());
            if (nick.trim().isEmpty() || email.trim().isEmpty()
                    || phone.trim().isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, R.string.error_required, Toast.LENGTH_SHORT).show();
                return;
            }
            if (db.registerUser(nick, email, phone, pass)) {
                Toast.makeText(this, R.string.register_ok, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, R.string.error_register_exists, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
