package com.example.myapplication;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class SignUpFragment extends Fragment {

    private EditText userNameEditText;
    private EditText passwordEditText;
    private EditText emailEditText;
    private EditText confirmPasswordEditText;
    private Button signUpBtn;
    private AppDatabase app_db;
    private String temp = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        app_db = AppDatabase.getDatabase(getActivity());

        userNameEditText = view.findViewById(R.id.user_name);
        passwordEditText = view.findViewById(R.id.password);
        signUpBtn = view.findViewById(R.id.signUP);
        emailEditText = view.findViewById(R.id.email);
        confirmPasswordEditText = view.findViewById(R.id.password_confirm);
        setupErrorValidation();
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performSignUp();
            }
        });
        return view;
    }

    private void setupErrorValidation() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                validateFields();
            }
        };
        userNameEditText.addTextChangedListener(textWatcher);
        passwordEditText.addTextChangedListener(textWatcher);
        confirmPasswordEditText.addTextChangedListener(textWatcher);
    }

    private void validateFields() {
        String userName = userNameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();

        if (userName.isEmpty()) {
            userNameEditText.setError("שם משתמש חסר, אנא מלאו חלק זה.");
        } else if (userName.length() < 3) {
            userNameEditText.setError("שם משתמש צריך להכיל 3 תווים או יותר.");
        } else {
            userNameEditText.setError(null);
        }

        if (password.isEmpty()) {
            passwordEditText.setError("ססמא חסרה, אנא מלאו חלק זה.");
        } else if (password.length() < 3) {
            passwordEditText.setError("ססמא צריכה להכיל 3 תווים או יותר.");
        } else {
            passwordEditText.setError(null);
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordEditText.setError("אישור ססמא חסרה, אנא מלאו חלק זה.");
        } else if (!confirmPassword.equals(password)) {
            confirmPasswordEditText.setError("ססמא ואישור ססמא אינם מתאימים.");
        } else {
            confirmPasswordEditText.setError(null);
        }

        if (email.isEmpty()) {
            emailEditText.setError("דואר אלקטרוני חסר, אנא מלאו חלק זה.");
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("דואר אלקטרוני אינו תקין.");
        } else {
            emailEditText.setError(null);
        }
    }

    private void performSignUp() {
        validateFields();

        String userName = userNameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();

        if (userName.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || email.isEmpty()) {
            Toast.makeText(getActivity(), "אנא מלאו את כל השדות הנדרשים", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getActivity(), "ססמא ואישור ססמא אינם תואמים", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userName.length() < 3 || password.length() < 4) {
            Toast.makeText(getActivity(), "שם משתמש או ססמא קצרים מדי", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getActivity(), "דואר אלקטרוני אינו תקין", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isCustomerExists = (app_db.customerDAO().checkCustomerExists(userName, password) > 0);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isCustomerExists) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Customer customer = app_db.customerDAO().getCustomerByNameAndPassword(userName, password);
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (customer.email.equals(email)) {
                                                Toast.makeText(getActivity(), "דואר אלקטרוני זה כבר רשום", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                                intent.putExtra("customer", customer);
                                                startActivity(intent);
                                            }
                                        }
                                    });
                                }
                            }).start();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage("הפרטים שמילאתם\n האם אתם בטוחים ליצירת משתמש?\n " + " שם משתמש: " + userName + "\n" + " דואר אלקטרוני: " + email)
                                    .setCancelable(false)
                                    .setPositiveButton("כן", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            Customer customer = new Customer(userName, password, email); // Remove the ID parameter from the constructor
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    app_db.customerDAO().insert(customer);
                                                    getActivity().runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(getActivity(), "נוצר משתמש חדש", Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(getActivity(), MainActivity.class);
                                                            intent.putExtra("customer", customer);
                                                            startActivity(intent);
                                                        }
                                                    });
                                                }
                                            }).start();
                                        }
                                    })
                                    .setNegativeButton("לא", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    }
                });
            }
        }).start();
    }
}
