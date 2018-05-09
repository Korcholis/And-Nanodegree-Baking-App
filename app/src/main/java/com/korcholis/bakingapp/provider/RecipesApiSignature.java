package com.korcholis.bakingapp.provider;

import com.korcholis.bakingapp.models.Recipe;

import java.util.List;

import io.reactivex.Single;
import retrofit2.http.GET;

public interface RecipesApiSignature {
    String PATH_RECIPES = "android-baking-app-json";

    @GET(PATH_RECIPES)
    Single<List<Recipe>> recipes();
}
