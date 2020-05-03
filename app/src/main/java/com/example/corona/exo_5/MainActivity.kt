package com.example.corona.exo_5

import android.content.ContentResolver
import android.content.pm.PackageManager
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import android.Manifest.permission
import android.Manifest.permission.SEND_SMS
import androidx.core.content.ContextCompat
import android.R.id.message
import android.app.PendingIntent
import android.content.Intent
import android.telephony.SmsManager
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    val REQUEST_READ_CONTACTS = 79
    var MY_PERMISSIONS_REQUEST_SEND_SMS = 0
    var list : ListView? = null
    var mobileArray : ArrayList<String> = ArrayList<String>()
    var phoneNumbers = ArrayList<String>()
    var message: String? = "Hello There!"
    val sms : SmsManager = SmsManager.getDefault()

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
            == PackageManager.PERMISSION_GRANTED) {
            // load the list of contacts
            mobileArray = getAllContacts()
        } else {
            // ask the permission to get contacts
            requestPermission()
        }
        // load the list view with all the contacts
        var adapter : ArrayAdapter<String> = ArrayAdapter<String>(this,
            android.R.layout.simple_list_item_1, android.R.id.text1, mobileArray);
        list_view.setAdapter(adapter)

        // click listener for sending a message to all contacts
        send_txt.setOnClickListener {
            sendSMSMessage()
        }
    }

    // send a message to all the contacts
    fun sendSMSMessage() {

        // check if we have the permission to send SMS
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        )
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.SEND_SMS
                )
            ) {
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.SEND_SMS),
                    MY_PERMISSIONS_REQUEST_SEND_SMS
                )
            }
        } else{
            // if we have the permission , go and send the SMS
            for (i : Int in 0..phoneNumbers!!.size-1)
                sms.sendTextMessage(phoneNumbers.get(i), null, message, null, null)
            Toast.makeText(getApplicationContext(), "SMS sent.",
                Toast.LENGTH_LONG).show()
        }

    }

    // check if we have the permission to get the contacts
    fun requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_CONTACTS)) {
            // show UI part if you want here to show some rationale !!!
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_CONTACTS),
                REQUEST_READ_CONTACTS);
        }
    }


    // get the permission to both : load contacts , and send SMS
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            REQUEST_READ_CONTACTS ->{
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mobileArray = getAllContacts();
                }
            }
            MY_PERMISSIONS_REQUEST_SEND_SMS->{
                if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    for (i : Int in 0..phoneNumbers!!.size-1) sms.sendTextMessage(
                        phoneNumbers.get(i), null, message, null, null)
                    Toast.makeText(getApplicationContext(), "SMS sent.",
                        Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                        "SMS faild, please try again.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // return the list of contacts
    fun getAllContacts() : ArrayList<String> {
        var nameList : ArrayList<String> = ArrayList<String>()
        var cr : ContentResolver = contentResolver
        var cur : Cursor? = cr.query(ContactsContract.Contacts.CONTENT_URI,
        null, null, null, null);
        if (cur != null && cur.getCount() > 0)  {
            while (cur != null && cur.moveToNext()) {
            var id : String = cur.getString(
                    cur.getColumnIndex(ContactsContract.Contacts._ID));
            var name  : String = cur.getString(cur.getColumnIndex(
                ContactsContract.Contacts.DISPLAY_NAME));
            nameList.add(name);
            if (cur.getInt(cur.getColumnIndex( ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                var pCur : Cursor? = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                arrayOf(id), null);
                while (pCur!=null && pCur.moveToNext()) {
                    var phoneNo : String = pCur.getString(pCur.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER))
                    phoneNumbers.add(phoneNo)
                }
                pCur?.close()
            }
        }
        }

        cur?.close()
        return nameList
    }

}
