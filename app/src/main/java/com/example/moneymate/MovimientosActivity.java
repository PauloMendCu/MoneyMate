package com.example.moneymate;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import adapters.MovimientoAdapter;
import entities.AppDatabase;
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
    private int categoriaSeleccionada = -1;
    private int mesSeleccionado;
    private int anoSeleccionado;

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
        TextView tvMesAno = findViewById(R.id.tv_mes_ano);
        Pair<Integer, Integer> mesYAno = obtenerMesYAnoActuales();
        String mesAnoTexto = String.format("%02d/%04d", mesYAno.first, mesYAno.second);
        tvMesAno.setText(mesAnoTexto);

        mesSeleccionado = mesYAno.first;
        anoSeleccionado = mesYAno.second;

        ImageButton btnMesAnterior = findViewById(R.id.btn_mes_anterior);
        ImageButton btnMesSiguiente = findViewById(R.id.btn_mes_siguiente);

        btnMesAnterior.setOnClickListener(v -> {
            // Retroceder al mes anterior
            mesSeleccionado--;
            if (mesSeleccionado < 1) {
                mesSeleccionado = 12;
                anoSeleccionado--;
            }
            actualizarMovimientos();
        });

        btnMesSiguiente.setOnClickListener(v -> {
            // Avanzar al mes siguiente
            mesSeleccionado++;
            if (mesSeleccionado > 12) {
                mesSeleccionado = 1;
                anoSeleccionado++;
            }
            actualizarMovimientos();
        });

        // Botón para ver movimientos
        ImageButton btnVerMovimientos = findViewById(R.id.btn_ver_movimientos);
        btnVerMovimientos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MovimientosActivity.this, MovimientosActivity.class);
                startActivity(intent);
            }
        });

        // Botón para ver cuentas
        ImageButton btnVerCuentas = findViewById(R.id.btn_ver_cuentas);
        btnVerCuentas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MovimientosActivity.this, CuentasActivity.class);
                startActivity(intent);
            }
        });




        // Botón para registrar nuevo movimiento
        ImageButton btnNuevoMovimiento = findViewById(R.id.btn_nuevo_movimiento);
        btnNuevoMovimiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MovimientosActivity.this, NuevoMovimientoActivity.class);
                startActivity(intent);
            }
        });

        adapter = new MovimientoAdapter(movimientosCompletos, cuentas, categorias);
        recyclerView.setAdapter(adapter);

        cargarMovimientosLocales();
        sincronizarMovimientos();
        configurarSpinnerCategoria();

    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void cargarMovimientosLocales() {
        AppDatabase.getDatabaseWriteExecutor().execute(() -> {
            movimientosCompletos = AppDatabase.getInstance(MovimientosActivity.this).movimientoDao().getAllMovimientos();
            runOnUiThread(() -> {
                //adapter.setMovimientos(movimientosCompletos);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void sincronizarMovimientos() {
        if (isNetworkAvailable()) {
            IFinanceService api = RetrofitClient.getInstance().create(IFinanceService.class);
            api.getMovimientos().enqueue(new Callback<List<Movimiento>>() {
                @Override
                public void onResponse(Call<List<Movimiento>> call, Response<List<Movimiento>> response) {
                    if (response.isSuccessful()) {
                        List<Movimiento> movimientosAPI = response.body();
                        AppDatabase.getDatabaseWriteExecutor().execute(() -> {
                            List<Movimiento> movimientosLocalesNoSincronizados = AppDatabase.getInstance(MovimientosActivity.this).movimientoDao().getMovimientosNoSincronizados();

                            for (Movimiento movimientoLocal : movimientosLocalesNoSincronizados) {
                                guardarMovimientoEnServidor(movimientoLocal);
                            }

                            for (Movimiento movimientoAPI : movimientosAPI) {
                                boolean existeEnLocal = false;
                                for (Movimiento movimientoLocal : movimientosCompletos) {
                                    if (movimientoLocal.getId() == movimientoAPI.getId()) {
                                        existeEnLocal = true;
                                        break;
                                    }
                                }
                                if (!existeEnLocal) {
                                    movimientoAPI.setIsSynced(true);
                                    AppDatabase.getInstance(MovimientosActivity.this).movimientoDao().insert(movimientoAPI);
                                }
                            }

                            movimientosCompletos = AppDatabase.getInstance(MovimientosActivity.this).movimientoDao().getAllMovimientos();
                            runOnUiThread(() -> {
                                //adapter.setMovimientos(movimientosCompletos);
                                adapter.notifyDataSetChanged();
                            });
                        });
                    }
                }

                @Override
                public void onFailure(Call<List<Movimiento>> call, Throwable t) {
                    Toast.makeText(MovimientosActivity.this, "No se pudo conectar al servidor", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void guardarMovimientoEnServidor(Movimiento movimientoLocal) {
        IFinanceService api = RetrofitClient.getInstance().create(IFinanceService.class);
        api.agregarMovimiento(movimientoLocal).enqueue(new Callback<Movimiento>() {
            @Override
            public void onResponse(Call<Movimiento> call, Response<Movimiento> response) {
                if (response.isSuccessful()) {
                    Movimiento movimientoCreado = response.body();
                    movimientoCreado.setIsSynced(true);
                    AppDatabase.getDatabaseWriteExecutor().execute(() -> {
                        AppDatabase.getInstance(MovimientosActivity.this).movimientoDao().update(movimientoCreado);
                        movimientosCompletos = AppDatabase.getInstance(MovimientosActivity.this).movimientoDao().getAllMovimientos();
                        runOnUiThread(() -> {
                            //adapter.setMovimientos(movimientosCompletos);
                            adapter.notifyDataSetChanged();
                        });
                    });
                }
            }

            @Override
            public void onFailure(Call<Movimiento> call, Throwable t) {
                // Log failure
            }
        });
    }

    private void sincronizarCategorias() {
        if (isNetworkAvailable()) {
            ICategoriaService api = RetrofitClient.getInstance().create(ICategoriaService.class);
            api.getCategorias().enqueue(new Callback<List<Categoria>>() {
                @Override
                public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                    if (response.isSuccessful()) {
                        List<Categoria> categoriasAPI = response.body();
                        AppDatabase.getDatabaseWriteExecutor().execute(() -> {
                            for (Categoria categoria : categoriasAPI) {
                                if (AppDatabase.getInstance(MovimientosActivity.this).categoriaDao().getCategoriaById(categoria.getId()) == null) {
                                    AppDatabase.getInstance(MovimientosActivity.this).categoriaDao().insert(categoria);
                                }
                            }
                            categorias = AppDatabase.getInstance(MovimientosActivity.this).categoriaDao().getAllCategorias();
                            runOnUiThread(() -> {
                                // Actualizar spinnerCategoria con las nuevas categorías
                            });
                        });
                    }
                }

                @Override
                public void onFailure(Call<List<Categoria>> call, Throwable t) {
                    Toast.makeText(MovimientosActivity.this, "No se pudo sincronizar las categorías", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private Pair<Integer, Integer> obtenerMesYAnoActuales() {
        Calendar calendar = Calendar.getInstance();
        int mesActual = calendar.get(Calendar.MONTH) + 1; // Los meses van de 0 a 11
        int anoActual = calendar.get(Calendar.YEAR);
        return new Pair<>(mesActual, anoActual);
    }
    private void actualizarMovimientos() {
        adapter.updateMovimientos(movimientosCompletos.stream()
                .filter(movimiento -> {
                    String[] partesFecha = movimiento.getFecha().split("-");
                    int mes = Integer.parseInt(partesFecha[1]);
                    int ano = Integer.parseInt(partesFecha[0]);
                    return (mes == mesSeleccionado && ano == anoSeleccionado) && (categoriaSeleccionada == -1 || movimiento.getCategoriaId() == categoriaSeleccionada);
                })
                .collect(Collectors.toList()));
        TextView tvMesAno = findViewById(R.id.tv_mes_ano);
        String mesAnoTexto = String.format("%02d/%04d", mesSeleccionado, anoSeleccionado);
        tvMesAno.setText(mesAnoTexto);
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
                actualizarMovimientos();
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
                    actualizarMovimientos();
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
        Pair<Integer, Integer> mesYAno = obtenerMesYAnoActuales();
        int mesActual = mesYAno.first;
        int anoActual = mesYAno.second;

        adapter.updateMovimientos(movimientosCompletos.stream()
                .filter(movimiento -> {
                    String[] partesFecha = movimiento.getFecha().split("-");
                    int mes = Integer.parseInt(partesFecha[1]);
                    int ano = Integer.parseInt(partesFecha[0]);
                    return (mes == mesActual && ano == anoActual) && (categoriaSeleccionada == -1 || movimiento.getCategoriaId() == categoriaSeleccionada);
                })
                .collect(Collectors.toList()));

    }

}