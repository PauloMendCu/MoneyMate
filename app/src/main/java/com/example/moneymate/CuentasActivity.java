package com.example.moneymate;

import android.content.Intent;
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

import adapters.CuentaAdapter;
import entities.Cuenta;
import entities.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import services.IFinanceService;

public class CuentasActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CuentaAdapter adapter;
    private List<Cuenta> cuentas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuentas);

        recyclerView = findViewById(R.id.recycler_view_cuentas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cuentas = new ArrayList<>();
        adapter = new CuentaAdapter(cuentas, cuenta -> {
            Intent intent = new Intent(this, MovimientosPorCuentaActivity.class);
            intent.putExtra("cuenta_id", cuenta.getId()); // se pasa el id de la cuenta seleccionada
            Log.e("MAIN_APP", String.valueOf(cuenta.getId()));
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        fetchCuentas();
    }

    private void fetchCuentas() {
        IFinanceService api = RetrofitClient.getInstance().create(IFinanceService.class);
        api.getCuentas().enqueue(new Callback<List<Cuenta>>() {
            @Override
            public void onResponse(Call<List<Cuenta>> call, Response<List<Cuenta>> response) {
                if (response.isSuccessful()) {
                    cuentas.clear();
                    cuentas.addAll(response.body());
                    adapter.notifyDataSetChanged(); // Actualizar el adaptador para mostrar las cuentas
                }
            }

            @Override
            public void onFailure(Call<List<Cuenta>> call, Throwable t) {
                Toast.makeText(CuentasActivity.this, "No se pudo conectar al servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }
}