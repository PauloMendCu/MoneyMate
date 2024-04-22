package com.example.moneymate;

import android.os.Bundle;
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

import adapters.CuentaAdapter;
import adapters.MovimientoAdapter;
import entities.Cuenta;
import entities.Movimiento;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import services.IFinanceService;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvMovimientos;
    private MovimientoAdapter movimientoAdapter;
    private List<Movimiento> movimientos = new ArrayList<>();

    private IFinanceService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvMovimientos = findViewById(R.id.rvMovimientos);

        setupRecyclerView();
        setupRetrofit();
        loadData();
    }

    private void setupRecyclerView() {
        movimientoAdapter = new MovimientoAdapter(movimientos);

        rvMovimientos.setLayoutManager(new LinearLayoutManager(this));
        rvMovimientos.setAdapter(movimientoAdapter);
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://your-api-base-url.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(IFinanceService.class);
    }

    private void loadData() {
        service.getTodosLosMovimientos().enqueue(new Callback<List<Movimiento>>() {
            @Override
            public void onResponse(Call<List<Movimiento>> call, Response<List<Movimiento>> response) {
                if (response.isSuccessful()) {
                    movimientos.clear();
                    movimientos.addAll(response.body());
                    movimientoAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "Error al cargar movimientos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Movimiento>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}