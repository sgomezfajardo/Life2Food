package com.example.life2food;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private String currentUserEmail;
    private OnProductClickListener productClickListener;
    private OnAddToCartClickListener addToCartClickListener;

    public ProductAdapter(List<Product> productList, String currentUserEmail, OnProductClickListener listener) {
        this.productList = productList;
        this.currentUserEmail = currentUserEmail;
        this.productClickListener = listener;
    }

    public interface OnProductClickListener {
        void onDeleteProductClick(Product product);
    }

    public interface OnAddToCartClickListener {
        void onAddToCartClick(Product product);
    }

    public void setOnAddToCartClickListener(OnAddToCartClickListener listener) {
        this.addToCartClickListener = listener;
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

        // Load image using Glide
        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .into(holder.productImage);

        holder.productName.setText(product.getName());
        holder.productQuantity.setText("Cantidad: " + product.getQuantity());
        holder.productPrice.setText("Precio: $" + product.getPrice());

        // Show/hide delete button based on user's email
        if (product.getEmail().equals(currentUserEmail)) {
            holder.deleteButton.setVisibility(View.VISIBLE);
        } else {
            holder.deleteButton.setVisibility(View.GONE);
        }

        // Set click listeners
        holder.deleteButton.setOnClickListener(v -> {
            if (productClickListener != null) {
                productClickListener.onDeleteProductClick(product);
                Toast.makeText(holder.itemView.getContext(), "Producto eliminado", Toast.LENGTH_SHORT).show();
            }
        });

        holder.addToCartButton.setOnClickListener(v -> {
            if (addToCartClickListener != null) {
                addToCartClickListener.onAddToCartClick(product);
                Toast.makeText(holder.itemView.getContext(), "Producto agregado al carrito", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateData(List<Product> newProductList) {
        productList = newProductList;
        notifyDataSetChanged();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        public ImageView productImage;
        public TextView productName;
        public TextView productQuantity;
        public TextView productPrice;
        public ImageButton deleteButton;
        public ImageButton addToCartButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.image_product);
            productName = itemView.findViewById(R.id.product_name);
            productQuantity = itemView.findViewById(R.id.product_quantity);
            productPrice = itemView.findViewById(R.id.product_price);
            deleteButton = itemView.findViewById(R.id.btn_delete_product);
            addToCartButton = itemView.findViewById(R.id.btn_add_to_cart);
        }
    }
}