package com.daemo.myfirsttrip.fragments;


import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.daemo.myfirsttrip.R;
import com.daemo.myfirsttrip.common.Constants;
import com.daemo.myfirsttrip.database.DataCost;
import com.daemo.myfirsttrip.models.Cost;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Locale;


public class CostDetailFragment extends DetailFragment {

    private Cost cost;


    public CostDetailFragment() {
        // Required empty public constructor
    }

    @Override
    DocumentReference getItemRef(String itemId) {
        return DataCost.getCostRef(itemId);
    }

    @Override
    void createDraftItemFromRef(DocumentReference itemDocReference, OnCompleteListener<DocumentReference> listener) {
        DataCost.createDraftCostFromRef(itemDocReference, listener);
    }

    @Override
    protected String getExtraItemId() {
        return Constants.EXTRA_COST_ID;
    }

    @Override
    protected boolean isItemDraft() {
        return cost.isDraft();
    }

    @Override
    protected String getItemId() {
        return cost.getId();
    }

    @Override
    protected boolean isItemSet() {
        return cost != null;
    }

    @Override
    protected void setEditViewDetails(View view) {
        EditText cost_quantity = view.findViewById(R.id.cost_quantity);
        EditText cost_motivation = view.findViewById(R.id.cost_motivation);
        cost_quantity.setText(String.format(Locale.getDefault(), "%f", cost.getQuantity()));
        cost_motivation.setText(cost.getMotivation());
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_cost_detail;
    }

    @Override
    protected int getEditLayout() {
        return R.layout.fragment_cost_edit;
    }

    @Override
    protected int getMenuEdit() {
        return R.menu.cost_detail_edit;
    }

    @Override
    protected int getMenu() {
        return R.menu.cost_detail;
    }

    @Override
    protected int getChooseMenuItem() {
        return R.id.choose_person;
    }

    @Override
    protected int getEditMenuItem() {
        return R.id.edit_cost;
    }

    @Override
    protected int getConfirmMenuItem() {
        return R.id.confirm_cost;
    }

    @Override
    protected int getClearMenuItem() {
        return R.id.clear_cost;
    }

    @Override
    protected String getListFragmentName() {
        return PeopleListFragment.class.getName();
    }

    @Override
    protected void setItemDetails(View view) {
        EditText costQuantity = view.findViewById(R.id.cost_quantity);
        Float quantity = Float.valueOf(costQuantity.getText().toString());
        EditText costMotivation = view.findViewById(R.id.cost_motivation);
        String motivation = costMotivation.getText().toString();
        cost.setQuantity(quantity);
        cost.setMotivation(motivation);
    }

    @Override
    protected void commitItem(OnCompleteListener<Void> listener) {
        DataCost.commitCostBatch(cost, listener);
    }

    @Override
    protected String getDetailFragmentName() {
        return CostDetailFragment.class.getName();
    }

    @Override
    protected void setViewDetails(@NonNull View view) {
        TextView cost_quantity = view.findViewById(R.id.cost_quantity);
        TextView cost_motivation = view.findViewById(R.id.cost_motivation);
        cost_quantity.setText(String.format(Locale.getDefault(), "%f", cost.getQuantity()));
        cost_motivation.setText(cost.getMotivation());
    }

    @Override
    protected int getListFragmentId() {
        return R.id.fragment_people_list;
    }

    @Override
    protected void deleteItem(OnCompleteListener<Void> listener) {
        DataCost.deleteCostBatch(getItemId(), listener);
    }

    @Override
    protected void setItem(DocumentSnapshot documentSnapshot) {
        cost = documentSnapshot.toObject(Cost.class);
    }
}
