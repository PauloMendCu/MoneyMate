package com.example.moneymate;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import adapters.CategoriaAdapter;
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

public class MovimientosActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MovimientoAdapter adapter;
    private List<Movimiento> movimientosCompletos;
    private List<Cuenta> cuentas;
    private List<Categoria> categorias;
    private Spinner spinnerCategoria;
    private int categoriaSeleccionada = -1; // -1 para mostrar todos los movimientos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movimientos);

        recyclerView = findViewById(R.id.recycler_view_movimientos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        movimientosCompletos = new ArrayList<>();
        movimientosCompletos.clear();
        cuentas = new ArrayList<>();
        categorias = new ArrayList<>();
        spinnerCategoria = findViewById(R.id.spinner_categorias);

        fetchCuentas();
        fetchCategorias();

        adapter = new MovimientoAdapter(movimientosCompletos, cuentas, categorias);
        recyclerView.setAdapter(adapter);

        fetchMovimientos();
        configurarSpinnerCategoria();

        filtrarMovimientos();
    }

    private void configurarSpinnerCategoria() {
        // Configurar el adaptador y el listener del spinner
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        adapterSpinner.add("Todas las categorías");
        spinnerCategoria.setAdapter(adapterSpinner);
        spinnerCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    categoriaSeleccionada = -1; // Mostrar todos los movimientos
                } else {
                    categoriaSeleccionada = categorias.get(position - 1).getId();
                }

                filtrarMovimientos();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });
    }

    private void fetchCategorias() {
        ICategoriaService categoriaApi = RetrofitClient.getInstanceCategorias().create(ICategoriaService.class);
        categoriaApi.getCategorias().enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                if (response.isSuccessful()) {
                    categorias.clear();

                    ArrayAdapter<String> adapterSpinner = (ArrayAdapter<String>) spinnerCategoria.getAdapter();
                    adapterSpinner.clear();
                    adapterSpinner.add("Todas las categorías");

                    categorias.addAll(response.body());

                    for (Categoria categoria : categorias) {
                        adapterSpinner.add(categoria.getNombre());
                    }
                    adapterSpinner.notifyDataSetChanged();

                    filtrarMovimientos();
                }
            }

            @Override
            public void onFailure(Call<List<Categoria>> call, Throwable t) {
                Toast.makeText(MovimientosActivity.this, "No se pudo conectar al servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCuentas() {
        IFinanceService api = RetrofitClient.getInstance().create(IFinanceService.class);
        api.getCuentas().enqueue(new Callback<List<Cuenta>>() {
            @Override
            public void onResponse(Call<List<Cuenta>> call, Response<List<Cuenta>> response) {
                if (response.isSuccessful()) {
                    cuentas.clear();
                    cuentas.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Cuenta>> call, Throwable t) {
                Toast.makeText(MovimientosActivity.this, "No se pudo conectar al servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchMovimientos() {
        IFinanceService api = RetrofitClient.getInstance().create(IFinanceService.class);
        api.getMovimientos().enqueue(new Callback<List<Movimiento>>() {
            @Override
            public void onResponse(Call<List<Movimiento>> call, Response<List<Movimiento>> response) {
                if (response.isSuccessful()) {
                    movimientosCompletos.clear();
                    movimientosCompletos.addAll(response.body());
                    Collections.sort(movimientosCompletos, Movimiento.ordenarPorFechaDescendente);
                    filtrarMovimientos();
                }
            }

            @Override
            public void onFailure(Call<List<Movimiento>> call, Throwable t) {
                Toast.makeText(MovimientosActivity.this, "No se pudo conectar al servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filtrarMovimientos() {
        adapter.updateMovimientos(movimientosCompletos.stream()
                .filter(movimiento -> categoriaSeleccionada == -1 || movimiento.getCategoriaId() == categoriaSeleccionada)
                .collect(Collectors.toList()));
    }

}