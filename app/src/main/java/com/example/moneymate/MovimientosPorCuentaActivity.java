package com.example.moneymate;

import android.os.Bundle;
import android.util.Log;
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

import adapters.MovimientoAdapter;
import entities.Cuenta;
import entities.Movimiento;
import entities.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import services.IFinanceService;

public class MovimientosPorCuentaActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MovimientoAdapter adapter;
    private List<Movimiento> movimientosFiltrados;
    private List<Cuenta> cuentas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movimientos_por_cuenta);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        movimientosFiltrados = new ArrayList<>();
        cuentas = new ArrayList<>();
        adapter = new MovimientoAdapter(movimientosFiltrados, cuentas);
        recyclerView.setAdapter(adapter);

        int cuentaId = getIntent().getIntExtra("cuenta_id", -1);
        fetchMovimientosPorCuenta(cuentaId);  // Obtener los movimientos para la cuenta específica
    }

    private void fetchMovimientosPorCuenta(int cuentaId) {
        IFinanceService api = RetrofitClient.getInstance().create(IFinanceService.class);
        api.getMovimientos().enqueue(new Callback<List<Movimiento>>() {
            @Override
            public void onResponse(Call<List<Movimiento>> call, Response<List<Movimiento>> response) {
                if (response.isSuccessful()) {
                    List<Movimiento> movimientos = response.body();
                    List<Movimiento> movimientosFiltradosTemp = new ArrayList<>(); // Lista temporal
                    for (Movimiento movimiento : movimientos) {
                        if (movimiento.getCuentaId() == cuentaId) {
                            movimientosFiltradosTemp.add(movimiento);
                        }
                    }
                    movimientosFiltrados.clear();
                    movimientosFiltrados.addAll(movimientosFiltradosTemp); // Asignar la lista filtrada a movimientosFiltrados
                    adapter.notifyDataSetChanged();

                    fetchCuentas();
                }
            }

            @Override
            public void onFailure(Call<List<Movimiento>> call, Throwable t) {
                Toast.makeText(MovimientosPorCuentaActivity.this, "No se pudo conectar al servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void fetchCuentas() {
        IFinanceService api = RetrofitClient.getInstance().create(IFinanceService.class);
        api.getCuentas().enqueue(new Callback<List<Cuenta>>() {
            @Override
            public void onResponse(Call<List<Cuenta>> call, Response<List<Cuenta>> response) {
                if (response.isSuccessful()) {
                    List<Cuenta> cuentas = response.body();
                    adapter = new MovimientoAdapter(movimientosFiltrados, cuentas);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Cuenta>> call, Throwable t) {
                Toast.makeText(MovimientosPorCuentaActivity.this, "No se pudo conectar al servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
