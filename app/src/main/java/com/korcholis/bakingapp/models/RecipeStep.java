package com.korcholis.bakingapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class RecipeStep implements Parcelable {
    public static final Parcelable.Creator<RecipeStep> CREATOR =
            new Parcelable.Creator<RecipeStep>() {

                public RecipeStep createFromParcel(Parcel in) {
                    return new RecipeStep(in);
                }

                public RecipeStep[] newArray(int size) {
                    return new RecipeStep[size];
                }
            };
    @SerializedName("id")
    int step;
    @SerializedName("description")
    String description;
    @SerializedName("shortDescription")
    String shortDescription;
    @SerializedName("thumbnailURL")
    String thumbnailURL;
    @SerializedName("videoURL")
    String videoURL;
    @SerializedName("recipeId")
    int recipeId;

    public RecipeStep(int step, String description, String shortDescription, String thumbnailURL, String videoURL, int recipeId) {
        this.step = step;
        this.description = description;
        this.shortDescription = shortDescription;
        this.thumbnailURL = thumbnailURL;
        this.videoURL = videoURL;
        this.recipeId = recipeId;
    }

    public RecipeStep(Parcel parcel) {
        this.step = parcel.readInt();
        this.description = parcel.readString();
        this.shortDescription = parcel.readString();
        this.thumbnailURL = parcel.readString();
        this.videoURL = parcel.readString();
        this.recipeId = parcel.readInt();
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public String getVideoURL() {
        return videoURL;
    }

    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
    }

    public int getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(step);
        parcel.writeString(description);
        parcel.writeString(shortDescription);
        parcel.writeString(thumbnailURL);
        parcel.writeString(videoURL);
        parcel.writeInt(recipeId);
    }
}
