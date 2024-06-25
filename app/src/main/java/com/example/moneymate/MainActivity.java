package com.example.moneymate;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import entities.AppDatabase;
import utils.SyncService;

public class MainActivity extends AppCompatActivity {

    private ImageView logoImageView;
    private LinearLayout buttonLinearLayout;
    private TextView textView;
    private TextView userEmailTextView;
    private AppDatabase db;
    private FirebaseAuth mAuth;
    private SyncService syncService;
    Button logoutButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        syncService = new SyncService(this);

        userEmailTextView = findViewById(R.id.userEmailTextView);
        logoutButton = findViewById(R.id.logoutButton);

        userEmailTextView.setText(currentUser.getEmail());
        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });



        // Botón para ver movimientos
        Button btnVerMovimientos = findViewById(R.id.btn_ver_movimientos);
        btnVerMovimientos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MovimientosActivity.class);
                startActivity(intent);
            }
        });

        // Botón para ver cuentas
        Button btnVerCuentas = findViewById(R.id.btn_ver_cuentas);
        btnVerCuentas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CuentasActivity.class);
                startActivity(intent);
            }
        });

        // Botón para ver categorias
        Button btnVerCategorias = findViewById(R.id.btn_ver_categorias);
        btnVerCategorias.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CategoriaActivity.class);
                startActivity(intent);
            }
        });

        // Botón para registrar nuevo movimiento
        Button btnNuevoMovimiento = findViewById(R.id.btn_nuevo_movimiento);
        btnNuevoMovimiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NuevoMovimientoActivity.class);
                startActivity(intent);
            }
        });

        Animaciones();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    protected void Animaciones() {
        //Probando la animación del título
        TextView textView = findViewById(R.id.tituloPrincipal);
        TextView textView2 = findViewById(R.id.tituloSecundario);

        final String text = textView.getText().toString();
        final String text2 = textView2.getText().toString();

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                int endIndex = (int) (text.length() * fraction);
                int endIndex2 = (int) (text2.length() * fraction);
                textView.setText(text.substring(0, endIndex));
                textView2.setText(text2.substring(0, endIndex2));
            }
        });
        animator.setDuration(3000); // Duración de la animación en milisegundos
        animator.setStartDelay(1000); // Retraso de 1 segundo antes de comenzar la animación
        animator.start();
    }
}
