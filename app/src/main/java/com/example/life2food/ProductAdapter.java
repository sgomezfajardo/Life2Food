package com.example.life2food;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> productList;
    private String currentUserEmail;
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
        holder.productPrice.setText("Precio: $" + String.format("%.2f", product.getPrice()));

        // Cargar la imagen del producto si está disponible
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            holder.productImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(product.getImageUrl())
                    .into(holder.productImage);
        } else {
            holder.productImage.setVisibility(View.GONE);
        }

        // Mostrar o esconder el botón de eliminar dependiendo del usuario
        if (product.getEmail().equals(currentUserEmail)) {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(v -> listener.onDeleteProductClick(product));
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
        TextView productPrice;
        ImageView productImage; // ImageView para la imagen del producto
        ImageButton deleteButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.product_name);
            productQuantity = itemView.findViewById(R.id.product_quantity);
            productPrice = itemView.findViewById(R.id.product_price);
            productImage = itemView.findViewById(R.id.image_product); // Asignar ImageView
            deleteButton = itemView.findViewById(R.id.btn_delete_product);
        }
    }

    public interface OnProductClickListener {
        void onDeleteProductClick(Product product);
    }

    public void updateData(List<Product> newProductList) {
        this.productList = newProductList;
        notifyDataSetChanged();
    }
}
