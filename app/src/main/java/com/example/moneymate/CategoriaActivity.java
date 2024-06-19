    package com.example.moneymate;

    import android.content.Context;
    import android.net.ConnectivityManager;
    import android.net.NetworkInfo;
    import android.os.Bundle;
    import android.view.View;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.Toast;

    import androidx.appcompat.app.AppCompatActivity;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;
    import androidx.room.Room;

    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;

    import java.util.List;

    import adapters.CategoriaAdapter;
    import entities.AppDatabase;
    import entities.Categoria;
    import utils.SyncCallback;
    import utils.SyncService;

    public class CategoriaActivity extends AppCompatActivity {

        private AppDatabase db;
        private CategoriaAdapter adapter;
        private List<Categoria> categoriaList;
        private RecyclerView recyclerView;
        private EditText nombreCategoriaEditText;
        private Button addCategoriaButton;
        private String userId;
        private SyncService syncService;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_categoria);

            db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database-name")
                    .allowMainThreadQueries()
                    .build();
            syncService = new SyncService(this);

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            userId = (currentUser != null) ? currentUser.getUid() : null;

            recyclerView = findViewById(R.id.recyclerViewCategorias);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            addCategoriaButton = findViewById(R.id.addCategoriaButton);
            nombreCategoriaEditText = findViewById(R.id.nombreCategoriaEditText);

            addCategoriaButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String nombre = nombreCategoriaEditText.getText().toString();
                    if (!nombre.isEmpty()) {
                        Categoria categoria = new Categoria();
                        categoria.nombre = nombre;
                        categoria.userId = userId;

                        if (isNetworkAvailable()) {
                            // Registrar la categoría en la API y luego en la base de datos local
                            syncService.agregarCategoriaEnApi(categoria, new SyncCallback() {
                                @Override
                                public void onSyncComplete() {

                                }

                                @Override
                                public void onSyncComplete(Categoria syncedCategoria) {
                                    syncedCategoria.setIsSynced(true);
                                    db.categoriaDao().insert(syncedCategoria);
                                    loadCategories();
                                }
                            });
                        } else {
                            // Registrar la categoría solo localmente con isSynced = false
                            categoria.setIsSynced(false);
                            db.categoriaDao().insert(categoria);
                            Toast.makeText(CategoriaActivity.this, "Categoria agregada offline", Toast.LENGTH_SHORT).show();
                            loadCategories();
                        }
                    }
                }
            });
        }

        @Override
        protected void onResume() {
            super.onResume();
            if (isNetworkAvailable()) {
                syncService.syncCategories();
                syncService.fetchCategoriesFromApi();
            }
            loadCategories();
        }


        private void loadCategories() {
            categoriaList = db.categoriaDao().getAllByUser(userId);
            if (adapter == null) {
                adapter = new CategoriaAdapter(categoriaList, this);
                recyclerView.setAdapter(adapter);
            } else {
                adapter.updateCategories(categoriaList);
            }
        }
        private boolean isNetworkAvailable() {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
    }
