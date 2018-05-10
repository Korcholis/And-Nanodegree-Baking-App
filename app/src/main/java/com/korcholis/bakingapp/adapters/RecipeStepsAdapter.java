package com.korcholis.bakingapp.adapters;

import android.annotation.SuppressLint;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.korcholis.bakingapp.R;
import com.korcholis.bakingapp.models.RecipeStep;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecipeStepsAdapter extends RecyclerView.Adapter<RecipeStepsAdapter.IngredientHolder> {

    private List<RecipeStep> recipeSteps;
    private OnItemClickListener listener = null;

    public RecipeStepsAdapter(List<RecipeStep> steps) {
        this.recipeSteps = steps;
    }

    public void swapContent(List<RecipeStep> recipeSteps) {
        this.recipeSteps = recipeSteps;
        notifyDataSetChanged();
    }

    @Override
    public IngredientHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_step_list_item, parent, false);
        return new IngredientHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final IngredientHolder holder, int position) {
        RecipeStep recipeStep = recipeSteps.get(position);
        int stepNumber = recipeStep.getStep();
        holder.step.setText(Integer.valueOf(stepNumber).toString());
        if (stepNumber == 0) {
            holder.step.setVisibility(View.GONE);
        } else {
            holder.step.setVisibility(View.VISIBLE);
        }
        if (recipeStep.getThumbnailURL().isEmpty() || !recipeStep.getThumbnailURL().matches("\\.(jpe?g|png|gif|bmp)$")) {
            holder.thumbWrapper.setVisibility(View.GONE);
        } else {
            Picasso.with(holder.thumb.getContext()).load(recipeStep.getThumbnailURL()).into(holder.thumb);
            holder.thumbWrapper.setVisibility(View.VISIBLE);
        }
        holder.name.setText(recipeStep.getShortDescription());


        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    int position = holder.getAdapterPosition();
                    listener.onClick(recipeSteps.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipeSteps.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onClick(RecipeStep step);
    }

    public class IngredientHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.container)
        ConstraintLayout container;
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.step)
        TextView step;
        @BindView(R.id.thumb_wrapper)
        CardView thumbWrapper;
        @BindView(R.id.thumb)
        ImageView thumb;

        IngredientHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
