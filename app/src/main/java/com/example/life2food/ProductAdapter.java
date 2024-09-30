package com.example.life2food;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> productList;
    private String currentUserEmail; // Correo electrónico del usuario actual
    private OnProductClickListener listener;

    public ProductAdapter(List<Product> productList, String currentUserEmail, OnProductClickListener listener) {
        this.productList = productList;
        this.currentUserEmail = currentUserEmail;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.productName.setText(product.getName());
        holder.productQuantity.setText("Cantidad: " + product.getQuantity());
        holder.productPrice.setText("Precio: $" + String.format("%.2f", product.getPrice())); // Mostrar el precio

        // Habilitar el botón de eliminar solo si el producto fue subido por el usuario actual
        if (product.getEmail().equals(currentUserEmail)) {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(v -> {
                listener.onDeleteProductClick(product); // Llamar al listener para eliminar
            });
        } else {
            holder.deleteButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName;
        TextView productQuantity;
        TextView productPrice; // Añadir TextView para el precio
        ImageButton deleteButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.product_name);
            productQuantity = itemView.findViewById(R.id.product_quantity);
            productPrice = itemView.findViewById(R.id.product_price); // Inicializar el TextView para el precio
            deleteButton = itemView.findViewById(R.id.btn_delete_product);
        }
    }

    public interface OnProductClickListener {
        void onDeleteProductClick(Product product);
    }
}
