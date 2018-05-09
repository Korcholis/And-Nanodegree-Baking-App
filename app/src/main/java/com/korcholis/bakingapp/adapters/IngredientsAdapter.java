package com.korcholis.bakingapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.korcholis.bakingapp.R;
import com.korcholis.bakingapp.RecipeDetailActivity;
import com.korcholis.bakingapp.models.Ingredient;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.IngredientHolder> {

    private List<Ingredient> ingredients;
    private final Context context;
    private OnItemClickListener listener = null;
    private final RecipeDetailActivity parentActivity;

    public IngredientsAdapter(List<Ingredient> ingredients, RecipeDetailActivity activity) {
        this.ingredients = ingredients;
        this.context = activity;
        this.parentActivity = activity;
    }

    public void swapContent(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
        notifyDataSetChanged();
    }

    @Override
    public IngredientHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ingredients_list_item, parent, false);
        return new IngredientHolder(view);
    }

    @Override
    public void onBindViewHolder(IngredientHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);
        holder.quantity.setText(ingredient.getQuantity() + " " + ingredient.getMeasure());
        holder.name.setText(ingredient.getName());
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    public class IngredientHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.quantity)
        TextView quantity;
        @BindView(R.id.name_cb)
        TextView name;

        public IngredientHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onClick(int id);
    }
}
