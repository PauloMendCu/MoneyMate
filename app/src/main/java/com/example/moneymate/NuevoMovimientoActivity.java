package com.example.moneymate;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import entities.Categoria;
import entities.Cuenta;
import entities.Movimiento;
import entities.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import services.ICategoriaService;
import services.IFinanceService;

public class NuevoMovimientoActivity extends AppCompatActivity {

    private Spinner spinnerCategoria, spinnerCuenta;
    private EditText etDescripcion, etMonto;
    private RadioGroup rgTipo;
    private RadioButton rbIngreso, rbGasto, rbTransferencia;
    private Button btnGuardar;

    private List<Categoria> categorias;
    private List<Cuenta> cuentas;
    private String fecha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_movimiento);

        spinnerCategoria = findViewById(R.id.spinner_categoria);
        spinnerCuenta = findViewById(R.id.spinner_cuenta);
        etDescripcion = findViewById(R.id.et_descripcion);
        etMonto = findViewById(R.id.et_monto);
        rgTipo = findViewById(R.id.rg_tipo);
        rbIngreso = findViewById(R.id.rb_ingreso);
        rbGasto = findViewById(R.id.rb_gasto);
        rbTransferencia = findViewById(R.id.rb_transferencia);
        btnGuardar = findViewById(R.id.btn_guardar);

        categorias = new ArrayList<>();
        cuentas = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        fecha = sdf.format(new Date());

        fetchCategorias();
        fetchCuentas();

        btnGuardar.setOnClickListener(v -> guardarMovimiento());
    }

    private void fetchCategorias() {
        ICategoriaService categoriaApi = RetrofitClient.getInstanceCategorias().create(ICategoriaService.class);
        categoriaApi.getCategorias().enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                if (response.isSuccessful()) {
                    categorias = response.body();
                    configurarSpinnerCategoria();
                }
            }

            @Override
            public void onFailure(Call<List<Categoria>> call, Throwable t) {
                Toast.makeText(NuevoMovimientoActivity.this, "No se pudo conectar al servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configurarSpinnerCategoria() {
        List<String> categoriasNombres = new ArrayList<>();
        categoriasNombres.add("Seleccionar categoría"); // Agregar opción vacía
        for (Categoria categoria : categorias) {
            categoriasNombres.add(categoria.getNombre());
        }
        ArrayAdapter<String> adapterCategoria = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoriasNombres);
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapterCategoria);
    }

    private void fetchCuentas() {
        IFinanceService api = RetrofitClient.getInstance().create(IFinanceService.class);
        api.getCuentas().enqueue(new Callback<List<Cuenta>>() {
            @Override
            public void onResponse(Call<List<Cuenta>> call, Response<List<Cuenta>> response) {
                if (response.isSuccessful()) {
                    cuentas = response.body();
                    configurarSpinnerCuenta();
                }
            }

            @Override
            public void onFailure(Call<List<Cuenta>> call, Throwable t) {
                Toast.makeText(NuevoMovimientoActivity.this, "No se pudo conectar al servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configurarSpinnerCuenta() {
        List<String> cuentasNombres = new ArrayList<>();
        cuentasNombres.add("Seleccionar cuenta"); // Agregar opción vacía
        for (Cuenta cuenta : cuentas) {
            cuentasNombres.add(cuenta.getNombre());
        }
        ArrayAdapter<String> adapterCuenta = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cuentasNombres);
        adapterCuenta.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCuenta.setAdapter(adapterCuenta);
    }

    private void guardarMovimiento() {
        String descripcion = etDescripcion.getText().toString().trim();
        String montoString = etMonto.getText().toString().trim();
        int categoriaSeleccionada = spinnerCategoria.getSelectedItemPosition();
        int cuentaSeleccionada = spinnerCuenta.getSelectedItemPosition();
        String tipo = obtenerTipoMovimiento();

        // Validaciones
        if (descripcion.isEmpty()) {
            Toast.makeText(this, "Debes ingresar una descripción", Toast.LENGTH_SHORT).show();
            return;
        }

        if (montoString.isEmpty()) {
            Toast.makeText(this, "Debes ingresar un monto", Toast.LENGTH_SHORT).show();
            return;
        }

        double monto;
        try {
            monto = Double.parseDouble(montoString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "El monto debe ser un número válido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (categoriaSeleccionada == 0) {
            Toast.makeText(this, "Debes seleccionar una categoría", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cuentaSeleccionada == 0) {
            Toast.makeText(this, "Debes seleccionar una cuenta", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tipo == null) {
            Toast.makeText(this, "Debes seleccionar un tipo de movimiento", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener los IDs de la categoría y cuenta seleccionadas
        int categoriaId = categorias.get(categoriaSeleccionada - 1).getId(); // Restar 1 para compensar la opción vacía
        int cuentaId = cuentas.get(cuentaSeleccionada - 1).getId(); // Restar 1 para compensar la opción vacía

        // Si todas las validaciones pasan, crear el objeto Movimiento y guardarlo
        Movimiento nuevoMovimiento = new Movimiento();
        nuevoMovimiento.setDescripcion(descripcion);
        nuevoMovimiento.setMonto(monto);
        nuevoMovimiento.setTipo(tipo);
        nuevoMovimiento.setCategoriaId(categoriaId);
        nuevoMovimiento.setCuentaId(cuentaId);
        nuevoMovimiento.setFecha(fecha);

        IFinanceService api = RetrofitClient.getInstance().create(IFinanceService.class);
        api.agregarMovimiento(nuevoMovimiento).enqueue(new Callback<Movimiento>() {
            @Override
            public void onResponse(Call<Movimiento> call, Response<Movimiento> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(NuevoMovimientoActivity.this, "Movimiento guardado correctamente", Toast.LENGTH_SHORT).show();
                    finish(); // Cierra la actividad actual
                } else {
                    Toast.makeText(NuevoMovimientoActivity.this, "Error al guardar el movimiento", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Movimiento> call, Throwable t) {
                Toast.makeText(NuevoMovimientoActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String obtenerTipoMovimiento() {
        if (rbIngreso.isChecked()) {
            return "Ingreso";
        } else if (rbGasto.isChecked()) {
            return "Gasto";
        } else if (rbTransferencia.isChecked()){
            return "Transferencia";
        } else {
            return null;
        }
    }
}