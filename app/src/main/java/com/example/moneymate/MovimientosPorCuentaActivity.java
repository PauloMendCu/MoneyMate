package com.example.moneymate;

import android.content.Intent;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

public class MovimientosPorCuentaActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MovimientoAdapter adapter;
    private List<Movimiento> movimientosFiltrados;
    private List<Cuenta> cuentas;
    private List<Categoria> categorias;
    private Spinner spinnerCategoria;
    private int categoriaSeleccionada = -1; // -1 para mostrar todos los movimientos
    private List<Categoria> categoriasFiltradas;

    private int mesSeleccionado;
    private int anoSeleccionado;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movimientos_por_cuenta);

        recyclerView = findViewById(R.id.recycler_view_movimientos_cuenta);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = (currentUser != null) ? currentUser.getUid() : null;

        movimientosFiltrados = new ArrayList<>();
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
                Intent intent = new Intent(MovimientosPorCuentaActivity.this, MovimientosActivity.class);
                startActivity(intent);
            }
        });

        // Botón para ver cuentas
        ImageButton btnVerCuentas = findViewById(R.id.btn_ver_cuentas);
        btnVerCuentas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MovimientosPorCuentaActivity.this, CuentasActivity.class);
                startActivity(intent);
            }
        });



        // Botón para registrar nuevo movimiento
        ImageButton btnNuevoMovimiento = findViewById(R.id.btn_nuevo_movimiento);
        btnNuevoMovimiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MovimientosPorCuentaActivity.this, NuevoMovimientoActivity.class);
                startActivity(intent);
            }
        });


        cargarDatosLocales();

        adapter = new MovimientoAdapter(movimientosFiltrados, cuentas, categorias);
        recyclerView.setAdapter(adapter);

        int cuentaId = getIntent().getIntExtra("cuenta_id", -1);

        Log.e("MAIN_APP2", String.valueOf(cuentaId));
        fetchMovimientosPorCuenta(cuentaId);

        configurarSpinnerCategoria();
        filtrarMovimientosPorCategoria();

    }

    private String convertirFechaALegible(String fecha) {
        if (fecha.matches("\\d+")) {
            try {
                long tiempo = Long.parseLong(fecha);
                Date date = new Date(tiempo);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                return sdf.format(date);
            } catch (NumberFormatException e) {
                Log.e("MovimientosPorCuentaActivity", "Error al convertir timestamp: " + fecha, e);
                return null;
            }
        } else if (fecha.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
            // Si la fecha ya está en el formato legible, úsala tal cual
            return fecha;
        } else {
            Log.e("MovimientosPorCuentaActivity", "Formato de fecha incorrecto: " + fecha);
            return null;
        }
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

                filtrarMovimientosPorCategoria();
                actualizarMovimientos();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });
    }

    private void actualizarMovimientos() {
        adapter.updateMovimientos(movimientosFiltrados.stream()
                .filter(movimiento -> {
                    String fecha = convertirFechaALegible(movimiento.getFecha());
                    if (fecha == null) {
                        return false;
                    }

                    String[] partesFecha = fecha.split("-");
                    if (partesFecha.length < 2) {
                        Log.e("MovimientosPorCuentaActivity", "Formato de fecha incorrecto: " + fecha);
                        return false;
                    }

                    int mes, ano;
                    try {
                        mes = Integer.parseInt(partesFecha[1]);
                        ano = Integer.parseInt(partesFecha[0]);
                    } catch (NumberFormatException e) {
                        Log.e("MovimientosPorCuentaActivity", "Error al convertir fecha: " + fecha, e);
                        return false;
                    }

                    return (mes == mesSeleccionado && ano == anoSeleccionado) && (categoriaSeleccionada == -1 || movimiento.getCategoriaId() == categoriaSeleccionada);
                })
                .collect(Collectors.toList()));

        TextView tvMesAno = findViewById(R.id.tv_mes_ano);
        String mesAnoTexto = String.format("%02d/%04d", mesSeleccionado, anoSeleccionado);
        tvMesAno.setText(mesAnoTexto);
    }


    private void filtrarMovimientosPorCategoria() {
        Pair<Integer, Integer> mesYAno = obtenerMesYAnoActuales();
        int mesActual = mesYAno.first;
        int anoActual = mesYAno.second;

        adapter.updateMovimientos(movimientosFiltrados.stream()
                .filter(movimiento -> {
                    String fecha = convertirFechaALegible(movimiento.getFecha());
                    if (fecha == null) {
                        return false;
                    }

                    String[] partesFecha = fecha.split("-");
                    if (partesFecha.length < 2) {
                        Log.e("MovimientosPorCuentaActivity", "Formato de fecha incorrecto: " + fecha);
                        return false;
                    }

                    int mes, ano;
                    try {
                        mes = Integer.parseInt(partesFecha[1]);
                        ano = Integer.parseInt(partesFecha[0]);
                    } catch (NumberFormatException e) {
                        Log.e("MovimientosPorCuentaActivity", "Error al convertir fecha: " + fecha, e);
                        return false;
                    }

                    return (mes == mesActual && ano == anoActual) && (categoriaSeleccionada == -1 || movimiento.getCategoriaId() == categoriaSeleccionada);
                })
                .collect(Collectors.toList()));
    }




    private void fetchMovimientosPorCuenta(int cuentaId) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            MovimientoDao movimientoDao = db.movimientoDao();

            List<Movimiento> movimientos = movimientoDao.getMovimientosPorCuenta(userId, cuentaId);
            movimientosFiltrados.clear();
            movimientosFiltrados.addAll(movimientos);

            runOnUiThread(() -> {
                Collections.sort(movimientosFiltrados, Movimiento.ordenarPorFechaDescendente);
                filtrarMovimientosPorCategoria();
            });
        });
    }
    private void cargarDatosLocales() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            CuentaDao cuentaDao = db.cuentaDao();
            CategoriaDao categoriaDao = db.categoriaDao();

            cuentas.clear();
            cuentas.addAll(cuentaDao.getAllByUser(userId));

            categorias.clear();
            categorias.addAll(categoriaDao.getAllByUser(userId));

            runOnUiThread(() -> {
                ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
                adapterSpinner.add("Todas las categorías");
                for (Categoria categoria : categorias) {
                    adapterSpinner.add(categoria.getNombre());
                }
                spinnerCategoria.setAdapter(adapterSpinner);
                adapterSpinner.notifyDataSetChanged();

                actualizarMovimientos();
            });
        });
    }


    private Pair<Integer, Integer> obtenerMesYAnoActuales() {
        Calendar calendar = Calendar.getInstance();
        int mesActual = calendar.get(Calendar.MONTH) + 1; // Los meses van de 0 a 11
        int anoActual = calendar.get(Calendar.YEAR);
        return new Pair<>(mesActual, anoActual);
    }

}
