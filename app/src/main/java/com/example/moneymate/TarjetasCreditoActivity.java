package com.example.moneymate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import adapters.TarjetaCreditoAdapter;
import entities.RetrofitClient;
import entities.TarjetaCredito;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import services.ICategoriaService;

public class TarjetasCreditoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TarjetaCreditoAdapter adapter;
    private List<TarjetaCredito> tarjetasCredito;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tarjetas_credito);

        recyclerView = findViewById(R.id.recycler_view_tarjetas_credito);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tarjetasCredito = new ArrayList<>();
        adapter = new TarjetaCreditoAdapter(tarjetasCredito);
        recyclerView.setAdapter(adapter);

        Button btnAgregarTarjeta = findViewById(R.id.btn_registrar_tarjeta_credito);
        btnAgregarTarjeta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TarjetasCreditoActivity.this, RegistrarTarjetaCreditoActivity.class);
                startActivity(intent);
            }
        });

        // Botón para ver movimientos
        ImageButton btnVerMovimientos = findViewById(R.id.btn_ver_movimientos);
        btnVerMovimientos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TarjetasCreditoActivity.this, MovimientosActivity.class);
                startActivity(intent);
            }
        });

        // Botón para ver cuentas
        ImageButton btnVerCuentas = findViewById(R.id.btn_ver_cuentas);
        btnVerCuentas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TarjetasCreditoActivity.this, CuentasActivity.class);
                startActivity(intent);
            }
        });



        // Botón para registrar nuevo movimiento
        ImageButton btnNuevoMovimiento = findViewById(R.id.btn_nuevo_movimiento);
        btnNuevoMovimiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TarjetasCreditoActivity.this, NuevoMovimientoActivity.class);
                startActivity(intent);
            }
        });


        fetchTarjetasCredito();
    }

    private void fetchTarjetasCredito() {
        ICategoriaService api = RetrofitClient.getInstanceCategorias().create(ICategoriaService.class);
        api.getCreditos().enqueue(new Callback<List<TarjetaCredito>>() {
            @Override
            public void onResponse(Call<List<TarjetaCredito>> call, Response<List<TarjetaCredito>> response) {
                if (response.isSuccessful()) {
                    tarjetasCredito.clear();
                    tarjetasCredito.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<TarjetaCredito>> call, Throwable t) {
                Toast.makeText(TarjetasCreditoActivity.this, "No se pudo conectar al servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }
}