package com.example.life2food;


import android.widget.Toast;

import java.util.ArrayList;


public class Cart {

    ArrayList products;

    public Cart(){
        products = new ArrayList();
    }

    public void addProduct(Product product){
        products.add(product);
        System.out.println("added to arraylist");
    }
}
