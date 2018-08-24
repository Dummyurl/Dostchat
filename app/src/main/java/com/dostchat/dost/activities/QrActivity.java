package com.dostchat.dost.activities;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dostchat.dost.R;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.helpers.PreferenceManager;
import com.dostchat.dost.models.users.contacts.ContactsModel;


public class QrActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        ImageView qr_image = (ImageView) findViewById(R.id.qr_image);
        TextView qr_text = (TextView) findViewById(R.id.qr_text);
        TextView qr_username = (TextView) findViewById(R.id.qr_username);
        ContactsModel contactsModel = DostChatApp.getRealmDatabaseInstance().where(ContactsModel.class).equalTo("id", PreferenceManager.getID(this)).findFirst();

        if (contactsModel != null) {
            if (contactsModel.getUsername() == null) {
                qr_username.setText("Welcome");
            } else {
                qr_username.setText(contactsModel.getUsername());
            }


        }
        qr_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        qr_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }


}
