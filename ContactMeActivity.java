package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ContactMeActivity extends AppCompatActivity {
    private Button contactViaPhone;
    private Customer customer;
    private Button contactViaEmail;
    private BroadcastReceiver broadcastReceiver;
    private EditText editTextMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_me);
        init();
    }

    public void init() {
        Intent intent = getIntent();
        customer = (Customer) intent.getSerializableExtra("customer");
        contactViaEmail = findViewById(R.id.contactViaEmailBtn);
        contactViaPhone = findViewById(R.id.contactViaPhoneBtn);
        editTextMessage = findViewById(R.id.editTextMessage);
// contact using email
        contactViaEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editTextMessage.getText().toString();
                if (isValidEmail(customer.getEmail())) {
                    showEmailConfirmationDialog(customer.getEmail(), message);
                } else {
                    showEmailConfirmationDialog("drukeramit90@gmail.com", message);
                }
            }
        });
// use phone to contact function
        contactViaPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = "0587364632";
                String message = "הודעה מפרויקט" +editTextMessage.getText().toString();
                if (isValidPhoneNumber(phoneNumber)) {
                    showSMSConfirmationDialog(phoneNumber, message);
                } else {
                    Toast.makeText(ContactMeActivity.this, "מספר טלפון לא תקין", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // use broadcast receiver
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

                if (!isConnected) {
                    showNoDataConnectionDialog();
                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    Toast.makeText(context, "מחובר לרשת Wi-Fi", Toast.LENGTH_SHORT).show();
                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    Toast.makeText(context, "מחובר לרשת נתונים סלולרית", Toast.LENGTH_SHORT).show();
                }
            }
        };

        registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }
//no data alert dialog
    public void showNoDataConnectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("אין חיבור לרשת")
                .setMessage("האם ברצונך להמשיך?")
                .setPositiveButton("כן", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("לא", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }
// confirm email dialog
    public void showEmailConfirmationDialog(String recipientEmail, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("שליחת אימייל")
                .setMessage("האם אתה בטוח שברצונך לשלוח אימייל זה?\n\n" + message)
                .setPositiveButton("שלח", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendEmail(recipientEmail, message);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("ביטול", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }
// send email (not wrokgin)
    public void sendEmail(String recipientEmail, String message) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + recipientEmail));
        intent.putExtra(Intent.EXTRA_SUBJECT, "צור קשר איתי - אפליקציית ספורט");
        intent.putExtra(Intent.EXTRA_TEXT, message);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "לא נמצא מזין אימייל", Toast.LENGTH_SHORT).show();
        }
    }
// show arlet dialog
    public void showSMSConfirmationDialog(String phoneNumber, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("שליחת הודעת SMS")
                .setMessage("האם אתה בטוח שברצונך לשלוח הודעת SMS זו?\n\n" + message)
                .setPositiveButton("שלח", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendSMS(phoneNumber, message);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("ביטול", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }
//send sms
    public void sendSMS(String phoneNumber, String message) {
        if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "הודעת SMS נשלחה", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "לא אושרה הרשאת SMS", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isValidEmail(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public boolean isValidPhoneNumber(String phoneNumber) {
     return phoneNumber != null && phoneNumber.matches("\\d{10}");
    }
}