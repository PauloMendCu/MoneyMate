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
import java.util.stream.Collectors;

import adapters.MovimientoAdapter;
import entities.Categoria;
import entities.Cuenta;
import entities.Movimiento;
import entities.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import services.ICategoriaService;
import services.IFinanceService;

public class MovimientosPorCuentaActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MovimientoAdapter adapter;
    private List<Movimiento> movimientosFiltrados;
    private List<Cuenta> cuentas;
    private List<Categoria> categorias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movimientos_por_cuenta);

        recyclerView = findViewById(R.id.recycler_view_movimientos_cuenta);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        movimientosFiltrados = new ArrayList<>();
        cuentas = new ArrayList<>();
        categorias = new ArrayList<>();

        fetchCuentas();
        fetchCategorias();

        inicializarCuentas();
        inicializarCategorias();

        adapter = new MovimientoAdapter(movimientosFiltrados, cuentas, categorias);
        recyclerView.setAdapter(adapter);

        int cuentaId = getIntent().getIntExtra("cuenta_id", -1);

        Log.e("MAIN_APP2", String.valueOf(cuentaId));
        fetchMovimientosPorCuenta(cuentaId);


    }

    private void fetchMovimientosPorCuenta(int cuentaId) {
        IFinanceService api = RetrofitClient.getInstance().create(IFinanceService.class);
        api.getMovimientosPorCuenta(cuentaId).enqueue(new Callback<List<Movimiento>>() {
            @Override
            public void onResponse(Call<List<Movimiento>> call, Response<List<Movimiento>> response) {
                if (response.isSuccessful()) {
                    movimientosFiltrados.clear();
                    List<Movimiento> movimientosRecibidos = response.body();
                    for (Movimiento movimiento : movimientosRecibidos) {
                        if (movimiento.getCuentaId() == cuentaId) {
                            movimientosFiltrados.add(movimiento);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Movimiento>> call, Throwable t) {
                Toast.makeText(MovimientosPorCuentaActivity.this, "No se pudo conectar al servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void fetchCategorias() {
        ICategoriaService categoriaApi = RetrofitClient.getInstanceCategorias().create(ICategoriaService.class);
        categoriaApi.getCategorias().enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                if (response.isSuccessful()) {
                    categorias = response.body();
                    fetchCuentas();
                }
            }

            @Override
            public void onFailure(Call<List<Categoria>> call, Throwable t) {
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
                    cuentas = response.body();
                    adapter = new MovimientoAdapter(movimientosFiltrados, cuentas, categorias);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Cuenta>> call, Throwable t) {
                Toast.makeText(MovimientosPorCuentaActivity.this, "No se pudo conectar al servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void inicializarCuentas() {
        IFinanceService api = RetrofitClient.getInstance().create(IFinanceService.class);
        api.getCuentas().enqueue(new Callback<List<Cuenta>>() {
            @Override
            public void onResponse(Call<List<Cuenta>> call, Response<List<Cuenta>> response) {
                if (response.isSuccessful()) {
                    cuentas = response.body();
                }
            }

            @Override
            public void onFailure(Call<List<Cuenta>> call, Throwable t) {
                Toast.makeText(MovimientosPorCuentaActivity.this, "No se pudo conectar al servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void inicializarCategorias() {
        ICategoriaService categoriaApi = RetrofitClient.getInstanceCategorias().create(ICategoriaService.class);
        categoriaApi.getCategorias().enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                if (response.isSuccessful()) {
                    categorias = response.body();
                }
            }

            @Override
            public void onFailure(Call<List<Categoria>> call, Throwable t) {
                Toast.makeText(MovimientosPorCuentaActivity.this, "No se pudo conectar al servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
