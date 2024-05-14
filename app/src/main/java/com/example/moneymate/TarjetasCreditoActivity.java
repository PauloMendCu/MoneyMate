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