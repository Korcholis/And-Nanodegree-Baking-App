package com.korcholis.bakingapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Recipe implements Parcelable {
    public static final Parcelable.Creator<Recipe> CREATOR =
            new Parcelable.Creator<Recipe>() {

                public Recipe createFromParcel(Parcel in) {
                    return new Recipe(in);
                }

                public Recipe[] newArray(int size) {
                    return new Recipe[size];
                }
            };
    @SerializedName("id")
    int id;

    @SerializedName("image")
    String image;

    @SerializedName("name")
    String name;

    @SerializedName("servings")
    int servings;

    @SerializedName("ingredients")
    List<Ingredient> ingredients;

    @SerializedName("steps")
    List<RecipeStep> steps;

    public Recipe(int id, String name, String image, int servings, List<Ingredient> ingredients, List<RecipeStep> steps) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.servings = servings;
        this.ingredients = ingredients;
        this.steps = steps;
    }

    protected Recipe(Parcel in) {
        id = in.readInt();
        image = in.readString();
        name = in.readString();
        servings = in.readInt();
        ingredients = in.createTypedArrayList(Ingredient.CREATOR);
        steps = in.createTypedArrayList(RecipeStep.CREATOR);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getServings() {
        return servings;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public List<RecipeStep> getSteps() {
        return steps;
    }

    public void setSteps(List<RecipeStep> steps) {
        this.steps = steps;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeString(image);
        parcel.writeInt(servings);
        parcel.writeTypedList(ingredients);
        parcel.writeTypedList(steps);
    }
}
