package com.korcholis.bakingapp.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.korcholis.bakingapp.R;
import com.korcholis.bakingapp.models.Recipe;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecipesListAdapter extends RecyclerView.Adapter<RecipesListAdapter.RecipesHolder> {

    private List<Recipe> recipes;
    private final Context context;
    private OnItemClickListener listener = null;

    public RecipesListAdapter(List<Recipe> recipes, Context context) {
        this.recipes = recipes;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    public void swapContent(List<Recipe> recipes) {
        this.recipes = recipes;
        notifyDataSetChanged();
    }

    @Override
    public RecipesHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipes_list_item, parent, false);
        return new RecipesHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecipesHolder holder, int position) {
        Recipe recipe = recipes.get(position);

        holder.name.setText(recipe.getName());
        if (!recipe.getImage().isEmpty()) {
            Picasso.with(context)
                    .load(recipe.getImage())
                    .into(holder.backgroundImage);
        }

        holder.viewWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onClick(recipes.get(holder.getAdapterPosition()));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (recipes == null) {
            return 0;
        }
        return recipes.size();
    }


    public interface OnItemClickListener {
        void onClick(Recipe recipe);
    }


    public class RecipesHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.wrapper_cv)
        CardView viewWrapper;
        @BindView(R.id.background_iv)
        ImageView backgroundImage;
        @BindView(R.id.name_wrapper)
        FrameLayout nameWrapper;
        @BindView(R.id.name_tv)
        TextView name;

        public RecipesHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
