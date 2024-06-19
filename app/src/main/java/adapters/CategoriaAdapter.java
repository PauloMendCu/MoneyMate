// CategoriaAdapter.java
package adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.moneymate.R;

import java.util.List;

import entities.AppDatabase;
import entities.Categoria;
import utils.SyncService;

public class CategoriaAdapter extends RecyclerView.Adapter<CategoriaAdapter.ViewHolder> {

    private List<Categoria> categoriaList;
    private AppDatabase db;
    private SyncService syncService;
    private Context context;

    public CategoriaAdapter(List<Categoria> categoriaList, Context context) {
        this.categoriaList = categoriaList;
        this.context = context;
        this.db = Room.databaseBuilder(context, AppDatabase.class, "database-name")
                .allowMainThreadQueries().build();
        this.syncService = new SyncService(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_categoria, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Categoria categoria = categoriaList.get(position);
        holder.nombreTextView.setText(categoria.nombre);

        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    showEditDialog(categoriaList.get(adapterPosition));
                }
            }
        });

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    Categoria categoriaToDelete = categoriaList.get(adapterPosition);
                    db.categoriaDao().delete(categoriaToDelete);
                    categoriaList.remove(adapterPosition);
                    notifyItemRemoved(adapterPosition);
                    notifyItemRangeChanged(adapterPosition, categoriaList.size());

                    if (isNetworkAvailable(context)) {
                        syncService.deleteCategoria(categoriaToDelete);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoriaList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombreTextView;
        Button editButton;
        Button deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreTextView = itemView.findViewById(R.id.categoriaNombreTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    private void showEditDialog(Categoria categoria) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Editar Categoria");

        View viewInflated = LayoutInflater.from(context).inflate(R.layout.edit_categoria_dialog, null);
        final EditText input = viewInflated.findViewById(R.id.input);
        input.setText(categoria.nombre);

        builder.setView(viewInflated);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                categoria.nombre = input.getText().toString();
                db.categoriaDao().update(categoria);
                notifyDataSetChanged();

                if (isNetworkAvailable(context)) {
                    syncService.updateCategoria(categoria);
                }
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void updateCategories(List<Categoria> newCategoriaList) {
        this.categoriaList.clear();  // Limpiar la lista actual
        this.categoriaList.addAll(newCategoriaList);  // Agregar las nuevas categorías
        notifyDataSetChanged();  // Notificar al adaptador que los datos han cambiado
    }

}
