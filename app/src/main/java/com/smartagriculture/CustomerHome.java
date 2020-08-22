package com.smartagriculture;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

public class CustomerHome extends Fragment {

    private static final String TAG = "AppTest>>>>";
    private SharedPreferences prefArea;
    private GridView categoryGrid;
    private ListView productsList;
    private ImageButton btnSearchCancel, btnClearFilter;
    private AutoCompleteTextView txtSearchField;
    private View v;
    private ArrayList<Product> products;
    private ArrayList<Category> categories;
    private ArrayList<Farmer> farmers;
    private ArrayList<String> areas;
    private FirebaseDatabase database;
    private CustomerHomeProductListAdapter customerHomeProductListAdapter;
    private CustomerHomeGategoryGridAdapter customerHomeGategoryGridAdapter;
    private TextView btnExpandCategory, txtProductsHeading, txtArea;
    private UiEnhancement uiEnhancement;
    private ArrayList<Product> filteredProducts;
    private ArrayList<String> searchProductHints;
    private ArrayAdapter<String> searchProductHintAdapter;
    private ArrayAdapter<String> areasAdapter;
    private ProgressBar prodProress, catProgress;
    private Button btnAreaChange;
    private String selectedArea = "";
    private int totProducts = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_customer_home, container, false);

        categoryGrid = v.findViewById(R.id.categoryGrid);
        productsList = v.findViewById(R.id.productsList);
        btnExpandCategory = v.findViewById(R.id.btnExpandCategory);
        txtProductsHeading = v.findViewById(R.id.txtProductsHeading);
        btnSearchCancel = v.findViewById(R.id.btnSearchCancel);
        btnClearFilter = v.findViewById(R.id.btnClearFilter);
        txtSearchField = v.findViewById(R.id.txtSearchField);
        btnAreaChange = v.findViewById(R.id.btnAreaChange);
        txtArea = v.findViewById(R.id.txtArea);
        database = FirebaseDatabase.getInstance();
        uiEnhancement = new UiEnhancement();
        products = new ArrayList<>();
        categories = new ArrayList<>();
        filteredProducts = new ArrayList<>();
        searchProductHints = new ArrayList<>();
        farmers = new ArrayList<>();
        areas = new ArrayList<>();
        searchProductHintAdapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), android.R.layout.simple_list_item_1, searchProductHints);
        areasAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, areas);
        customerHomeProductListAdapter = new CustomerHomeProductListAdapter(getActivity(), filteredProducts);
        customerHomeGategoryGridAdapter = new CustomerHomeGategoryGridAdapter(getActivity(), categories);
        catProgress = v.findViewById(R.id.catProgress);
        prodProress = v.findViewById(R.id.prodProgress);
        prefArea = getActivity().getSharedPreferences("prefArea",0);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        selectedArea = Objects.requireNonNull(getActivity()).getSharedPreferences("prefArea", 0).getString("area", "");
        if(selectedArea.isEmpty())
            selectedArea = Objects.requireNonNull(getActivity()).getSharedPreferences("userDetails", 0).getString("area", "");
        txtArea.setText(String.format("Sellers in %s", selectedArea));

        getAreas();
        getCategories();

        btnAreaChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                dialogBuilder.setTitle("Select Area")
                        .setSingleChoiceItems(areasAdapter, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                selectedArea = areas.get(i);
                                filterByArea(selectedArea);
                                if (filteredProducts.size() > 0) {
                                    customerHomeProductListAdapter.notifyDataSetChanged();
                                    Log.i(TAG, "onClick: productListAdapterCount: " + customerHomeProductListAdapter.getCount());
                                    uiEnhancement.visible(productsList);
                                    productsList.setAdapter(customerHomeProductListAdapter);
                                    txtProductsHeading.setText(R.string.customer_home_all_product_heading_txt);
                                } else {
                                    Log.i(TAG, "btnAreaChangeOnClick: filtered products empty");
                                    uiEnhancement.gone(productsList);
                                    txtProductsHeading.setText(R.string.customer_home_empty_product_heading_txt);
                                }
                                updateSearchAutoFillHints();
                                txtArea.setText(String.format("Sellers in %s", selectedArea));
                                prefArea.edit().putString("area",selectedArea).apply();
                                dialogInterface.dismiss();
                            }
                        })
                        .create().show();
            }
        });

        btnExpandCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (categoryGrid.getVisibility() == View.GONE) {
                    uiEnhancement.hide(false, 0, 0, categoryGrid);
                    uiEnhancement.visible(categoryGrid);
                    uiEnhancement.show(true, 0, 200, categoryGrid);
                    btnExpandCategory.setText(R.string.customer_home_hide_category_heading_txt);
                } else if (categoryGrid.getVisibility() == View.VISIBLE) {
                    uiEnhancement.hide(true, 0, 200, categoryGrid);
                    uiEnhancement.gone(categoryGrid);
                    btnExpandCategory.setText(R.string.customer_home_product_by_category_heading_txt);
                }
            }
        });

        categoryGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                uiEnhancement.visible(btnClearFilter);
                filterByArea(selectedArea);
                filterByCategory(customerHomeGategoryGridAdapter.categories.get(i).getName());
                if (filteredProducts.size() > 0) {
                    uiEnhancement.visible(productsList);
                    customerHomeProductListAdapter.notifyDataSetChanged();
                    productsList.setAdapter(customerHomeProductListAdapter);
                } else {
                    uiEnhancement.gone(productsList);
                    txtProductsHeading.setText(R.string.customer_home_empty_product_heading_txt);
                }
                txtProductsHeading.setText(String.format("Category: %s", customerHomeGategoryGridAdapter.categories.get(i).getName()));
                btnExpandCategory.performClick();
                if (txtSearchField.getVisibility() == View.VISIBLE) {
                    uiEnhancement.gone(txtSearchField);
                    uiEnhancement.hide(false, 0, 0, txtProductsHeading);
                    uiEnhancement.visible(txtProductsHeading);
                    uiEnhancement.show(true, 0, 200, txtProductsHeading);
                }
            }
        });

        btnClearFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filteredProducts.clear();
                txtSearchField.setHint(R.string.customer_home_search_all_hint);
                uiEnhancement.gone(btnClearFilter);
                //filteredProducts.addAll(products);
                filterByArea(selectedArea);
                customerHomeProductListAdapter.notifyDataSetChanged();
                if (customerHomeProductListAdapter.getCount() >= 1) {
                    uiEnhancement.visible(productsList);
                    productsList.setAdapter(customerHomeProductListAdapter);
                    txtProductsHeading.setText(R.string.customer_home_all_product_heading_txt);
                } else {
                    txtProductsHeading.setText(R.string.customer_home_empty_product_heading_txt);
                }
                uiEnhancement.visible(txtSearchField);
                btnSearchCancel.performClick();
                updateSearchAutoFillHints();

                hideKeyboard();
            }
        });

        btnSearchCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtSearchField.getVisibility() == View.GONE) {
                    uiEnhancement.gone(txtProductsHeading);
                    uiEnhancement.hide(false, 0, 0, txtSearchField);
                    uiEnhancement.visible(txtSearchField);
                    uiEnhancement.show(true, 0, 200, txtSearchField);
                    btnSearchCancel.setImageResource(R.drawable.cancel);
                    txtSearchField.requestFocus();
                    showKeyboard();
                } else if (txtSearchField.getVisibility() == View.VISIBLE) {
                    txtSearchField.setText("");
                    uiEnhancement.gone(txtSearchField);
                    uiEnhancement.hide(false, 0, 0, txtProductsHeading);
                    uiEnhancement.visible(txtProductsHeading);
                    uiEnhancement.show(true, 0, 200, txtProductsHeading);
                    btnSearchCancel.setImageResource(R.drawable.magnifier);
                    hideKeyboard();
                }
            }
        });

        txtSearchField.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                filterByName(searchProductHintAdapter.getItem(i));
                uiEnhancement.visible(btnClearFilter);
                customerHomeProductListAdapter.notifyDataSetChanged();
                productsList.setAdapter(customerHomeProductListAdapter);
                txtProductsHeading.setText(String.format("Search: %s", searchProductHintAdapter.getItem(i)));
                uiEnhancement.gone(txtSearchField);
                uiEnhancement.hide(false, 0, 0, txtProductsHeading);
                uiEnhancement.visible(txtProductsHeading);
                uiEnhancement.show(true, 0, 200, txtProductsHeading);
                btnSearchCancel.setImageResource(R.drawable.magnifier);
                hideKeyboard();

            }
        });

    }

    private void getAreas() {
        DatabaseReference farmersRef = database.getReference("farmers");
        farmersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        farmers.add(d.getValue(Farmer.class));
                        if (!areas.contains(d.getValue(Farmer.class).getArea()))
                            areas.add(d.getValue(Farmer.class).getArea());
                    }

                    //Log.i(TAG, "onDataChange: farmers size: "+farmers.size());
                    if (areas.size() > 0) {
                        areasAdapter.notifyDataSetChanged();
                        btnAreaChange.setEnabled(true);
                        getProducts();
                        Log.i(TAG, "onDataChange: AreaSize: "+areas.size());
                    } else {
                        btnAreaChange.setEnabled(false);
                        Log.i(TAG, "onDataChange: No areas found");
                    }
                }else{
                    Log.i(TAG, "onDataChange: No farmers found");
                    btnAreaChange.setEnabled(false);
                    getProducts();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void filterByName(String itemName) {
        ArrayList<Product> tempFilter = new ArrayList<>(filteredProducts);
        filteredProducts.clear();
        for (Product p : tempFilter) {

            if (p.getName().equalsIgnoreCase(itemName))
                filteredProducts.add(p);
        }
        Collections.sort(filteredProducts, new Comparator<Product>() {
            @Override
            public int compare(Product p1, Product p2) {
                int pr1 = Integer.parseInt(p1.getPrice().split(" / ")[0].split(" ")[1]);
                int pr2 = Integer.parseInt(p2.getPrice().split(" / ")[0].split(" ")[1]);
                return pr1 - pr2;
            }
        });

        Log.i(TAG, "filterByName: list: " + filteredProducts.toString());
    }

    private void filterByArea(String area) {
        filteredProducts.clear();

        ArrayList<String> farmerIds = new ArrayList<>();
        for (Farmer f : farmers) {
            if (f.getArea().equalsIgnoreCase(area)) {
                farmerIds.add(f.getPhno());
            }
        }

        for (String id : farmerIds) {
            for (Product p : products) {
                if (p.getOwnerId().equals(id)) {
                    filteredProducts.add(p);
                }
            }
        }

        if (filteredProducts.size() > 0) {
            Collections.sort(filteredProducts, new Comparator<Product>() {
                @Override
                public int compare(Product p1, Product p2) {
                    return p1.getName().compareToIgnoreCase(p2.getName());
                }
            });

            Log.i(TAG, "filterByArea: list: " + filteredProducts.toString());

        }


    }

    private void filterByCategory(String name) {
        ArrayList<Product> tempFilter = new ArrayList<>(filteredProducts);
        filteredProducts.clear();
        for (Product p : tempFilter) {
            if (p.getCategoryName().equalsIgnoreCase(name))
                filteredProducts.add(p);
        }

        Log.i(TAG, "filterByCategory: list: " + filteredProducts.toString());
    }

    private void getCategories() {
        // TODO: 11-04-2018 get the categories
        btnExpandCategory.setText(R.string.customer_home_loading_category_heading_txt);
        DatabaseReference categoryGroup = database.getReference("categories");
        categoryGroup.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    categories.add(snapshot.getValue(Category.class));
                    totProducts += snapshot.getValue(Category.class).getCount();

                }
                uiEnhancement.gone(catProgress);
                if (categories.size() > 0) {
                    btnExpandCategory.setText(R.string.customer_home_product_by_category_heading_txt);
                    customerHomeGategoryGridAdapter.notifyDataSetChanged();
                    categoryGrid.setAdapter(customerHomeGategoryGridAdapter);
                } else {
                    btnExpandCategory.setText(R.string.customer_home_empty_category_heading_txt);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateSearchAutoFillHints() {
        searchProductHints.clear();
        for (Product p : filteredProducts) {
            if (!searchProductHints.contains(p.getName()))
                searchProductHints.add(p.getName());
        }
        searchProductHintAdapter.notifyDataSetChanged();
        txtSearchField.setAdapter(searchProductHintAdapter);
        /*Log.i(TAG, "updateSearchAutoFillHints: searchHintsCount: " + searchProductHintAdapter.getCount());
        for (int i = 0; i < searchProductHintAdapter.getCount(); i++)
            Log.i(TAG, "updateSearchAutoFillHints: searchHints: " + searchProductHintAdapter.getItem(i));*/
    }

    private void getProducts() {
        txtProductsHeading.setText(R.string.customer_home_loading_product_heading_txt);
        DatabaseReference productsRef = database.getReference("products");
        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot productItem : snapshot.getChildren()) {
                        if (Integer.parseInt(productItem.getValue(Product.class).getQuantity().split(" ")[0]) > 0)
                            products.add(productItem.getValue(Product.class));
                        Log.i(TAG, "onDataChange: fetchedProduct: " + productItem.getValue(Product.class).getName());
                    }
                }
                uiEnhancement.gone(prodProress);
                filteredProducts.addAll(products);
                //Log.i(TAG, "onDataChange: area: "+getActivity().getSharedPreferences("userDetails",0).getString("area",""));
                filterByArea(selectedArea);
                customerHomeProductListAdapter.notifyDataSetChanged();
                if (customerHomeProductListAdapter.getCount() > 0) {
                    uiEnhancement.visible(productsList, btnSearchCancel);
                    productsList.setAdapter(customerHomeProductListAdapter);
                    txtProductsHeading.setText(R.string.customer_home_all_product_heading_txt);

                    updateSearchAutoFillHints();
                } else {
                    txtProductsHeading.setText(R.string.customer_home_empty_product_heading_txt);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //finish get orders
    }

    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void showKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, 0);
            }
        }
    }
}
