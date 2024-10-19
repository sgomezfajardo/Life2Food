package com.example.life2food;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private String currentUserEmail;
    private OnProductClickListener productClickListener;
    private OnAddToCartClickListener addToCartClickListener;
    private boolean isEcommerce;

    public ProductAdapter(List<Product> productList, String currentUserEmail, OnProductClickListener listener) {
        this.productList = productList;
        this.currentUserEmail = currentUserEmail;
        this.productClickListener = listener;
        isEcommerce = false;
    }

    public ProductAdapter(List<Product> productList, OnProductClickListener listener) {
        this.productList = productList;
        this.productClickListener = listener;
        isEcommerce = true;
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

        View view = null;

        if(isEcommerce){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_ecommerce, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        }
        return new ProductViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        String imageUrl = product.getImageUrl();

        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.e("ProductAdapter", "Error al cargar la imagen: " + e.getMessage());
                        Toast.makeText(holder.itemView.getContext(), "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                        return false; // Dejar que Glide maneje el error
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false; // Dejar que Glide maneje el recurso
                    }
                })
                .into(holder.productImage);

        holder.productName.setText(String.valueOf(product.getName()));
        holder.productQuantity.setText("Cantidad: " + product.getQuantity());
        holder.productPrice.setText("$" + String.valueOf(product.getPrice()).substring(0,String.valueOf(product.getPrice()).indexOf(".")));
        holder.productDescription.setText(product.getDescription());
        holder.addToCartButton.setOnClickListener(v -> {
            if (addToCartClickListener != null) {
                addToCartClickListener.onAddToCartClick(product);
            }

        });
        holder.buyButton.setOnClickListener(v -> {
            if (productClickListener != null) {
                productClickListener.onDeleteProductClick(product);
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
        public TextView productDescription;
        public Button addToCartButton;
        public Button buyButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.image_product);
            productName = itemView.findViewById(R.id.product_name);
            productQuantity = itemView.findViewById(R.id.product_quantity);
            productPrice = itemView.findViewById(R.id.product_price);
            productDescription = itemView.findViewById(R.id.product_description);
            addToCartButton = itemView.findViewById(R.id.btn_add_to_cart);
            buyButton = itemView.findViewById(R.id.btn_buy);
        }
    }
}