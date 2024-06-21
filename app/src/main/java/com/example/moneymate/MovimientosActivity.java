package com.example.moneymate;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import adapters.MovimientoAdapter;
import dao.CategoriaDao;
import dao.CuentaDao;
import dao.MovimientoDao;
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
    private TextView tvMesAno;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movimientos);

        // Inicializaciones
        inicializarComponentes();

        // Cargar datos locales y sincronizar
        cargarDatosLocales();
        //actualizarMovimientos();

        // Sincronizar datos con el servidor
        sincronizarDatos();

    }

    private void inicializarComponentes() {
        recyclerView = findViewById(R.id.recycler_view_movimientos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        movimientosCompletos = new ArrayList<>();
        cuentas = new ArrayList<>();
        categorias = new ArrayList<>();
        spinnerCategoria = findViewById(R.id.spinner_categorias);
        tvMesAno = findViewById(R.id.tv_mes_ano);

        Pair<Integer, Integer> mesYAno = obtenerMesYAnoActuales();
        mesSeleccionado = mesYAno.first;
        anoSeleccionado = mesYAno.second;
        actualizarTextoMesAno();

        // Botones de navegación entre meses
        configurarBotonesNavegacion();

        // Botones de navegación principal
        configurarBotonesPrincipal();

        adapter = new MovimientoAdapter(movimientosCompletos, cuentas, categorias);
        recyclerView.setAdapter(adapter);

        configurarSpinnerCategoria();
    }

    private void configurarBotonesNavegacion() {
        ImageButton btnMesAnterior = findViewById(R.id.btn_mes_anterior);
        ImageButton btnMesSiguiente = findViewById(R.id.btn_mes_siguiente);

        btnMesAnterior.setOnClickListener(v -> {
            cambiarMes(-1);
        });

        btnMesSiguiente.setOnClickListener(v -> {
            cambiarMes(1);
        });
    }

    private void cambiarMes(int incremento) {
        mesSeleccionado += incremento;
        if (mesSeleccionado < 1) {
            mesSeleccionado = 12;
            anoSeleccionado--;
        } else if (mesSeleccionado > 12) {
            mesSeleccionado = 1;
            anoSeleccionado++;
        }
        actualizarTextoMesAno();
        actualizarMovimientos();
    }

    private void configurarBotonesPrincipal() {
        // Botón para ver movimientos
        ImageButton btnVerMovimientos = findViewById(R.id.btn_ver_movimientos);
        btnVerMovimientos.setOnClickListener(view -> {
            Intent intent = new Intent(MovimientosActivity.this, MovimientosActivity.class);
            startActivity(intent);
        });

        // Botón para ver cuentas
        ImageButton btnVerCuentas = findViewById(R.id.btn_ver_cuentas);
        btnVerCuentas.setOnClickListener(view -> {
            Intent intent = new Intent(MovimientosActivity.this, CuentasActivity.class);
            startActivity(intent);
        });

        // Botón para registrar nuevo movimiento
        ImageButton btnNuevoMovimiento = findViewById(R.id.btn_nuevo_movimiento);
        btnNuevoMovimiento.setOnClickListener(v -> {
            Intent intent = new Intent(MovimientosActivity.this, NuevoMovimientoActivity.class);
            startActivity(intent);
        });
    }

    private void cargarDatosLocales() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            MovimientoDao movimientoDao = db.movimientoDao();
            CategoriaDao categoriaDao = db.categoriaDao();
            CuentaDao cuentaDao = db.cuentaDao();

            // Cargar movimientos locales
            List<Movimiento> movimientosLocales = movimientoDao.getAllMovimientos();
            runOnUiThread(() -> {
                movimientosCompletos.addAll(movimientosLocales);
                adapter.notifyDataSetChanged(); // Actualizar el adaptador con los movimientos cargados
            });

            // Cargar categorías locales
            List<Categoria> categoriasLocales = categoriaDao.getAllCategorias();
            runOnUiThread(() -> {
                categorias.addAll(categoriasLocales);
                configurarSpinnerCategoria(); // Configurar el spinner con las categorías cargadas
            });

            // Cargar cuentas locales
            List<Cuenta> cuentasLocales = cuentaDao.getAllCuentas();
            runOnUiThread(() -> {
                cuentas.addAll(cuentasLocales);
                adapter.notifyDataSetChanged(); // Actualizar el adaptador con las cuentas cargadas
            });
        });
    }

    private void sincronizarDatos() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            sincronizarMovimientos();
            sincronizarCategorias();
            sincronizarCuentas();  // Agregar método para sincronizar cuentas
        });
    }

    private void sincronizarCategorias() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        CategoriaDao categoriaDao = AppDatabase.getInstance(this).categoriaDao();
        ICategoriaService categoriaService = RetrofitClient.getInstanceCategorias().create(ICategoriaService.class);

        // Obtener categorías no sincronizadas localmente
        List<Categoria> categoriasNoSincronizadas = categoriaDao.getCategoriasNoSincronizadas();

        // Sincronizar categorías locales con el servidor
        if (!categoriasNoSincronizadas.isEmpty()) {
            for (Categoria categoria : categoriasNoSincronizadas) {
                categoriaService.agregarCategoria(categoria).enqueue(new Callback<Categoria>() {
                    @Override
                    public void onResponse(Call<Categoria> call, Response<Categoria> response) {
                        if (response.isSuccessful()) {
                            Categoria categoriaSincronizada = response.body();
                            if (categoriaSincronizada != null) {
                                categoriaSincronizada.setIsSynced(true);
                                executorService.execute(() -> {
                                    categoriaDao.update(categoriaSincronizada);
                                });
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Categoria> call, Throwable t) {
                        // Manejar error
                    }
                });
            }
        }

        // Obtener categorías del servidor y sincronizar con la base de datos local
        categoriaService.getCategorias().enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                if (response.isSuccessful()) {
                    List<Categoria> categoriasServidor = response.body();
                    Log.d("Sync", "Categorías obtenidas del servidor: " + categoriasServidor.size());
                    if (categoriasServidor != null) {
                        executorService.execute(() -> {
                            for (Categoria categoria : categoriasServidor) {
                                Log.d("Sync", "Procesando categoría: " + categoria.getNombre());
                                categoria.setIsSynced(true);
                                // Verificar si la categoría ya existe antes de insertarla
                                Categoria categoriaExistente = categoriaDao.getCategoriaById(categoria.getId(), "asd");
                                if (categoriaExistente == null) {
                                    categoriaDao.insert(categoria);
                                    Log.d("Sync", "Categoría insertada: " + categoria.getNombre());
                                } else {
                                    categoriaDao.update(categoria);
                                    Log.d("Sync", "Categoría actualizada: " + categoria.getNombre());
                                }
                            }

                            runOnUiThread(() -> {
                                categorias.clear();
                                categorias.addAll(categoriasServidor);
                                configurarSpinnerCategoria(); // Actualizar el spinner con las categorías sincronizadas
                            });

                            // Actualizar categorías en el servidor como sincronizadas
                            for (Categoria categoria : categoriasServidor) {
                                categoriaService.actualizarCategoria(categoria.getId(), categoria).enqueue(new Callback<Categoria>() {
                                    @Override
                                    public void onResponse(Call<Categoria> call, Response<Categoria> response) {
                                        Log.d("Sync", "Categoría sincronizada en el servidor: " + categoria.getNombre());
                                    }

                                    @Override
                                    public void onFailure(Call<Categoria> call, Throwable t) {
                                        Log.e("Sync", "Error al sincronizar la categoría en el servidor", t);
                                    }
                                });
                            }
                        });
                    } else {
                        Log.e("Sync", "Error al obtener las categorías del servidor");
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Categoria>> call, Throwable t) {
                Log.e("Sync", "Error en la llamada de red para obtener categorías", t);
            }
        });
    }


    // Método para obtener movimientos por ID
    private Movimiento getMovimientoById(int id) {
        MovimientoDao movimientoDao = AppDatabase.getInstance(this).movimientoDao();
        return movimientoDao.getMovimientoById(id, "userId");  // Aquí debes pasar el ID del usuario o algún valor adecuado
    }

    // Modificación en la sincronización
    private void sincronizarMovimientos() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        MovimientoDao movimientoDao = AppDatabase.getInstance(this).movimientoDao();
        IFinanceService apiService = RetrofitClient.getInstance().create(IFinanceService.class);

        List<Movimiento> movimientosNoSincronizados = movimientoDao.getMovimientosNoSincronizados();

        if (!movimientosNoSincronizados.isEmpty()) {
            for (Movimiento movimiento : movimientosNoSincronizados) {
                apiService.agregarMovimiento(movimiento).enqueue(new Callback<Movimiento>() {
                    @Override
                    public void onResponse(Call<Movimiento> call, Response<Movimiento> response) {
                        if (response.isSuccessful()) {
                            Movimiento movimientoSincronizado = response.body();
                            if (movimientoSincronizado != null) {
                                movimientoSincronizado.setIsSynced(true);
                                executorService.execute(() -> {
                                    movimientoDao.update(movimientoSincronizado);
                                });
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Movimiento> call, Throwable t) {
                        // Manejar error
                    }
                });
            }
        }

        apiService.getMovimientos().enqueue(new Callback<List<Movimiento>>() {
            @Override
            public void onResponse(Call<List<Movimiento>> call, Response<List<Movimiento>> response) {
                if (response.isSuccessful()) {
                    List<Movimiento> movimientosServidor = response.body();
                    if (movimientosServidor != null) {
                        executorService.execute(() -> {
                            for (Movimiento movimiento : movimientosServidor) {
                                movimiento.setIsSynced(true);
                                Movimiento movimientoExistente = movimientoDao.getMovimientoById(movimiento.getId(), "userId"); // Ajustar la llamada
                                if (movimientoExistente == null) {
                                    movimientoDao.insert(movimiento);
                                } else {
                                    movimientoDao.update(movimiento);
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Movimiento>> call, Throwable t) {
                // Manejar error
            }
        });
    }




    private void sincronizarCuentas() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        CuentaDao cuentaDao = AppDatabase.getInstance(this).cuentaDao();
        IFinanceService apiService = RetrofitClient.getInstance().create(IFinanceService.class);

        // Obtener cuentas no sincronizadas localmente
        List<Cuenta> cuentasNoSincronizadas = cuentaDao.getCuentasNoSincronizadas();

        // Sincronizar cuentas locales con el servidor
        if (!cuentasNoSincronizadas.isEmpty()) {
            for (Cuenta cuenta : cuentasNoSincronizadas) {
                apiService.crearCuenta(cuenta).enqueue(new Callback<Cuenta>() {
                    @Override
                    public void onResponse(Call<Cuenta> call, Response<Cuenta> response) {
                        if (response.isSuccessful()) {
                            Cuenta cuentaSincronizada = response.body();
                            if (cuentaSincronizada != null) {
                                cuentaSincronizada.setIsSynced(true);
                                executorService.execute(() -> {
                                    cuentaDao.update(cuentaSincronizada);
                                });
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Cuenta> call, Throwable t) {
                        // Manejar error
                    }
                });
            }
        }

        // Obtener cuentas del servidor y sincronizar con la base de datos local
        apiService.getCuentas().enqueue(new Callback<List<Cuenta>>() {
            @Override
            public void onResponse(Call<List<Cuenta>> call, Response<List<Cuenta>> response) {
                if (response.isSuccessful()) {
                    List<Cuenta> cuentasServidor = response.body();
                    if (cuentasServidor != null) {
                        executorService.execute(() -> {
                            for (Cuenta cuenta : cuentasServidor) {
                                cuenta.setIsSynced(true);
                                // Verificar si la cuenta ya existe antes de insertarla
                                Cuenta cuentaExistente = cuentaDao.getCuentaById(cuenta.getId(), "asd");
                                if (cuentaExistente == null) {
                                    cuentaDao.insert(cuenta);
                                } else {
                                    cuentaDao.update(cuenta);
                                }
                            }

                            // Actualizar cuentas en el servidor como sincronizadas
                            for (Cuenta cuenta : cuentasServidor) {
                                apiService.actualizarCuenta(cuenta.getId(), cuenta).enqueue(new Callback<Cuenta>() {
                                    @Override
                                    public void onResponse(Call<Cuenta> call, Response<Cuenta> response) {
                                        // Cuenta actualizada correctamente en el servidor
                                    }

                                    @Override
                                    public void onFailure(Call<Cuenta> call, Throwable t) {
                                        // Manejar error
                                    }
                                });
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Cuenta>> call, Throwable t) {
                // Manejar error
            }
        });
    }



    private void actualizarMovimientos() {
        AppDatabase.getDatabaseWriteExecutor().execute(() -> {
            movimientosCompletos = AppDatabase.getInstance(MovimientosActivity.this).movimientoDao().getAllMovimientos();
            runOnUiThread(() -> {
                filtrarMovimientos();
            });
        });
    }

    private void filtrarMovimientos() {
        List<Movimiento> movimientosFiltrados = movimientosCompletos.stream()
                .filter(movimiento -> {
                    String[] partesFecha = movimiento.getFecha().split("-");
                    int mes = Integer.parseInt(partesFecha[1]);
                    int ano = Integer.parseInt(partesFecha[0]);
                    boolean matchesCategoria = categoriaSeleccionada == -1 || movimiento.getCategoriaId() == categoriaSeleccionada;
                    return (mes == mesSeleccionado && ano == anoSeleccionado) && matchesCategoria;
                })
                .collect(Collectors.toList());
        adapter.updateMovimientos(movimientosFiltrados);
    }

    private void configurarSpinnerCategoria() {
        List<String> nombresCategorias = new ArrayList<>();
        nombresCategorias.add("Todas las categorias");
        for (Categoria categoria : categorias) {
            nombresCategorias.add(categoria.getNombre());
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nombresCategorias);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(spinnerAdapter);

        spinnerCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    categoriaSeleccionada = -1;
                } else {
                    categoriaSeleccionada = categorias.get(position - 1).getId();
                }
                filtrarMovimientos();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed
            }
        });
    }



    private void actualizarTextoMesAno() {
        String[] nombresMeses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        String textoMesAno = nombresMeses[mesSeleccionado - 1] + " " + anoSeleccionado;
        tvMesAno.setText(textoMesAno);
    }

    private Pair<Integer, Integer> obtenerMesYAnoActuales() {
        Calendar calendar = Calendar.getInstance();
        return new Pair<>(calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}