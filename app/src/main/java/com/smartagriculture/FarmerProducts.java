package com.smartagriculture;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FarmerProducts extends Fragment {

    private static final String TAG = "AppTest>>>>";
    private ArrayList<Product> products;
    private UiEnhancement uiEnhancement;
    private TextView btnExpColAddProd, txtPoductsHeading;
    private Spinner spinnerCategory, spinnerUnits;
    private TextInputEditText txtName, txtQuantity, txtPrice, txtUnit;
    private CheckBox checkAutoUnit;
    private Button btnAddProduct;
    private ListView listProducts;
    private ScrollView addViewGroup/*, productListGroup*/;
    private ArrayList<Category> categories;
    private String[] units = {"kg", "g", "ltr"};
    private SharedPreferences userDetails;
    private Farmer farmer;
    private ViewGroup viewContainer;
    private FirebaseDatabase database;
    private ArrayList<String> categoryNames;
    private ArrayAdapter<String> categoryNameAdapter;
    private FarmerProductsProductListAdapter farmerProductsProductListAdapter;
    private boolean modify = false;
    private String oldProductName;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_farmer_products, container, false);
        viewContainer = container;
        userDetails = getActivity().getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        farmer = new Farmer(userDetails.getString("name", ""), userDetails.getString("phno", ""), userDetails.getString("area", ""));
        database = FirebaseDatabase.getInstance();
        categories = new ArrayList<>();
        products = new ArrayList<>();
        categoryNames = new ArrayList<>();
        uiEnhancement = new UiEnhancement();
        btnAddProduct = v.findViewById(R.id.btnAddProduct);
        btnExpColAddProd = v.findViewById(R.id.btnExpColAddProd);
        spinnerCategory = v.findViewById(R.id.spinnerCategory);
        spinnerUnits = v.findViewById(R.id.spinnerUnits);
        txtName = v.findViewById(R.id.txtName);
        txtQuantity = v.findViewById(R.id.txtQuantity);
        txtPrice = v.findViewById(R.id.txtPrice);
        txtUnit = v.findViewById(R.id.txtUnit);
        checkAutoUnit = v.findViewById(R.id.checkAutoUnit);
        listProducts = v.findViewById(R.id.listProducts);
        addViewGroup = v.findViewById(R.id.addViewGroup);
        /*productListGroup = v.findViewById(R.id.productListGroup);*/
        txtPoductsHeading = v.findViewById(R.id.txtPoductsHeading);
        categoryNameAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, categoryNames);
        farmerProductsProductListAdapter = new FarmerProductsProductListAdapter(getActivity(), products);

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //farmer = new Farmer(userDetails.getString("name", ""), userDetails.getString("phno", ""), userDetails.getString("area", ""));

        getFarmer();
        getCategories();
        getProducts();
        txtPoductsHeading.setText(R.string.farmer_product_products_heading_loading_txt);

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, units);
        spinnerUnits.setAdapter(unitAdapter);

        btnExpColAddProd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnAddProduct.setText(R.string.farmer_products_btn_add_txt);
                if (addViewGroup.getVisibility() == View.VISIBLE) {
                    uiEnhancement.hide(true, 0, 200, addViewGroup);
                    uiEnhancement.gone(addViewGroup);
                    btnExpColAddProd.setText(R.string.farmer_products_seg1_heading_open_txt);
                    txtName.setText("");
                    txtQuantity.setText("");
                    txtPrice.setText("");
                    txtUnit.setText("");
                    hideKeyboard();
                } else {
                    uiEnhancement.hide(false, 0, 0, addViewGroup);
                    uiEnhancement.visible(addViewGroup);
                    uiEnhancement.show(true, 0, 200, addViewGroup);
                    btnExpColAddProd.setText(R.string.farmer_products_seg1_heading_close_txt);
                }
            }
        });

        checkAutoUnit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    txtUnit.setText(spinnerUnits.getSelectedItem().toString());
                    txtUnit.setEnabled(false);
                } else {
                    txtUnit.setEnabled(true);
                    txtUnit.requestFocus();
                }
            }
        });

        btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (txtName.getText().toString().isEmpty() || txtQuantity.getText().toString().isEmpty() || txtPrice.getText().toString().isEmpty() || txtUnit.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), R.string.farmer_products_add_product_empty_fields_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                String price = getString(R.string.farmer_products_add_product_currency_txt) + " " + txtPrice.getText().toString().trim() + " / " + txtUnit.getText().toString().replace(" ", "");
                String quantity = txtQuantity.getText().toString().trim() + " " + spinnerUnits.getSelectedItem().toString();
                Product p = new Product(spinnerCategory.getSelectedItem().toString().split(" ")[0], txtName.getText().toString().trim(), quantity, farmer.getPhno(), price);
                p.setOwnerName(userDetails.getString("name", ""));
                Log.i(TAG, "onClick: " + p.getCategoryName() + " " + p.getName() + " " + p.getQuantity());
                // TODO: 18-03-2018 check for category change and adequately change the product count in the required categories
                addNewProduct(p);
                btnAddProduct.setText(R.string.farmer_products_btn_add_txt);
            }
        });

        listProducts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (addViewGroup.getVisibility() == View.GONE) {
                    uiEnhancement.hide(false, 0, 0, addViewGroup);
                    uiEnhancement.visible(addViewGroup);
                    uiEnhancement.show(true, 0, 200, addViewGroup);
                    btnExpColAddProd.setText(R.string.farmer_products_seg1_heading_close_txt);
                }
                btnAddProduct.setText(R.string.farmer_products_btn_modify_txt);
                modify = true;
                oldProductName = farmerProductsProductListAdapter.getProductAt(i).getName();
                txtName.setText(farmerProductsProductListAdapter.getProductAt(i).getName());
                txtQuantity.setText(farmerProductsProductListAdapter.getProductAt(i).getQuantity().split(" ")[0]);
                txtPrice.setText(farmerProductsProductListAdapter.getProductAt(i).getPrice().split(" ")[1]);
                txtUnit.setText(farmerProductsProductListAdapter.getProductAt(i).getPrice().split(" ")[3]);
                int k;
                Log.i(TAG, "onItemClick: product: " + farmerProductsProductListAdapter.getProductAt(i).getName());
                for (k = 0; k < categoryNames.size(); k++)
                    if (farmerProductsProductListAdapter.getProductAt(i).getCategoryName().equalsIgnoreCase(categoryNames.get(k).split(" ")[0]))
                        break;
                spinnerCategory.setSelection(k);
                for (k = 0; k < units.length; k++)
                    if (farmerProductsProductListAdapter.getProductAt(i).getQuantity().split(" ")[1].equalsIgnoreCase(units[k]))
                        break;
                spinnerUnits.setSelection(k);
                txtQuantity.requestFocus();

            }
        });


    }

    private void getProducts() {
        DatabaseReference pGroup = database.getReference("products");
        DatabaseReference pOwner = pGroup.child(farmer.getPhno());
        pOwner.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                products.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    products.add(snapshot.getValue(Product.class));
                }
                if (products.size() == 0 && isAdded()) {
                    txtPoductsHeading.setText(R.string.farmer_product_product_list_empty_heading_txt);
                } else {
                    if (isAdded()) {
                        txtPoductsHeading.setText(R.string.farmer_product_product_list_normal_heading_txt);
                        farmerProductsProductListAdapter.notifyDataSetChanged();
                        listProducts.setAdapter(farmerProductsProductListAdapter);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addNewProduct(final Product p) {
        DatabaseReference pGroup = database.getReference("products");
        DatabaseReference pOwner = pGroup.child(p.getOwnerId());
        if(modify){
            DatabaseReference pRef = pOwner.child(oldProductName);
            pRef.removeValue();
        }
        final DatabaseReference pRef = pOwner.child(p.getName());
        pRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren() || modify) {
                    pRef.setValue(p);
                    txtName.setText("");
                    txtQuantity.setText("");
                    txtPrice.setText("");
                    checkAutoUnit.setChecked(false);
                    txtUnit.setText("");
                    modify = false;
                    Toast.makeText(getActivity(), "Modified " + p.getName() + " to " + p.getQuantity(), Toast.LENGTH_SHORT).show();
                    refreshProductList();
                } else {
                    pRef.setValue(p);
                    DatabaseReference categoriesRef = database.getReference("categories");
                    final DatabaseReference category = categoriesRef.child(p.getCategoryName());
                    category.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Category catMod = dataSnapshot.getValue(Category.class);
                            assert catMod != null;
                            catMod.increaseCount();
                            category.setValue(catMod);
                            txtName.setText("");
                            txtQuantity.setText("");
                            txtPrice.setText("");
                            checkAutoUnit.setChecked(false);
                            txtUnit.setText("");
                            Toast.makeText(getActivity(), "Added " + p.getName() + " of " + p.getQuantity(), Toast.LENGTH_SHORT).show();
                            refreshProductList();

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //pRef.setValue(p);

    }

    private void refreshProductList() {
        getProducts();
    }

    private void getCategories() {
        DatabaseReference categoriesRef = database.getReference("categories");
        categoriesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                categories.clear();
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        categories.add(snapshot.getValue(Category.class));
                        prepareSpinnerCategory();
                        spinnerCategory.setSelection(spinnerCategory.getSelectedItemPosition());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void prepareSpinnerCategory() {
        categoryNames.clear();
        for (Category c : categories)
            categoryNames.add(c.getName() + " (" + c.getCount() + ")");
        categoryNameAdapter.notifyDataSetChanged();
        spinnerCategory.setAdapter(categoryNameAdapter);

    }

    private void getFarmer() {

        DatabaseReference fGroup = database.getReference("farmers");
        DatabaseReference fMember = fGroup.child(userDetails.getString("phno", ""));
        fMember.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    farmer = dataSnapshot.getValue(Farmer.class);
                    refreshProductList();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void hideKeyboard() {
        if (viewContainer != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(viewContainer.getWindowToken(), 0);
            }
        }
    }




}
