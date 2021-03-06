package com.android.dtrescatering;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.android.dtrescatering.base.MethodeFunction.shortToast;

public class BookingActivity extends AppCompatActivity {

    private EditText mAlamatEditText;
    private Button mTambahButton;
    private Button mKurangButton;
    private Button mOrderButton;
    private TextView mNamaTextView;
    private TextView mHargaTextView;
    private TextView mJumlahTextView;
    private TextView mTotalTextView;
    private TextView mOngkirTextView;
    private TextView mSubTotalTextView;
    private int qty = 0;

    private String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private String username = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        mAlamatEditText = findViewById(R.id.editText_booking_address);
        mTambahButton = findViewById(R.id.button_booking_plus);
        mKurangButton = findViewById(R.id.button_booking_min);
        mOrderButton = findViewById(R.id.button_booking_order);
        mNamaTextView = findViewById(R.id.textView_booking_nama);
        mHargaTextView = findViewById(R.id.textView_booking_harga);
        mJumlahTextView = findViewById(R.id.textView_booking_qty);
        mTotalTextView = findViewById(R.id.textView_booking_total_price);
        mOngkirTextView = findViewById(R.id.textView_booking_ongkir);
        mSubTotalTextView = findViewById(R.id.textView_booking_sub_total);

        mShowData(savedInstanceState);
    }

    private void mShowData(final Bundle savedInstanceState) {
        String itemId = getItemId(savedInstanceState);
        String storeId = getStoreId(savedInstanceState);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Stores").child(storeId).child("items").child(itemId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String harga = dataSnapshot.child("harga").getValue(String.class);
                String nama = dataSnapshot.child("nama").getValue(String.class);

                mNamaTextView.setText(nama);
                mHargaTextView.setText(harga);

                mButtonQtyClicked(harga, savedInstanceState);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void mButtonQtyClicked(final String harga, final Bundle bundle) {
        mTambahButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qty = qty + 1;
                int hargaitem = Integer.parseInt(harga) * qty;
                int ongkir = Integer.parseInt(mOngkirTextView.getText().toString());
                int subTotal = hargaitem + ongkir;

                mJumlahTextView.setText(String.valueOf(qty));
                mTotalTextView.setText(String.valueOf(hargaitem));
                mSubTotalTextView.setText(String.valueOf(subTotal));
            }
        });

        mKurangButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (qty <= 0) {
                    shortToast(getApplicationContext(), "Tidak Bisa dibawah 0");
                } else {
                    qty = qty - 1;
                    int hargaitem = Integer.parseInt(mTotalTextView.getText().toString()) - Integer.parseInt(harga);
                    int ongkir = Integer.parseInt(mOngkirTextView.getText().toString());
                    int subTotal = hargaitem - ongkir;

                    mJumlahTextView.setText(String.valueOf(qty));
                    mTotalTextView.setText(String.valueOf(hargaitem));

                    if (subTotal < 0) {
                        mSubTotalTextView.setText("0");
                    } else {
                        mSubTotalTextView.setText(String.valueOf(subTotal));
                    }

                }
            }
        });

        mOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(bundle);
            }
        });
    }

    private String getStoreId(Bundle savedInstanceState) {
        String storeId;
        if (savedInstanceState == null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle == null) {
                return storeId = null;
            } else {
                return storeId = bundle.getString("storeId");
            }
        } else {
            return storeId = (String) savedInstanceState.getSerializable("storeId");
        }
    }

    private String getItemId(Bundle savedInstanceState) {
        String itemId;
        if (savedInstanceState == null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle == null) {
                return itemId = null;
            } else {
                return itemId = bundle.getString("itemId");
            }
        } else {
            return itemId = (String) savedInstanceState.getSerializable("itemId");
        }
    }

    private void sendMessage(Bundle bundle) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(getStoreId(bundle));
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String phone = dataSnapshot.child("phone").getValue(String.class);
                Uri uri = Uri.parse("smsto:" + phone);

                String alamat = mAlamatEditText.getText().toString();
                String namaBarang = mNamaTextView.getText().toString();
                String total = String.valueOf(qty);

                String message = "Saya pesan " + namaBarang + " sebanyak " + total + " porsi. dikirim ke : " + alamat;

                Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                intent.putExtra("sms_body",message);
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
