package com.korcholis.bakingapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("WeakerAccess")
public class Ingredient implements Parcelable {
    public static final Parcelable.Creator<Ingredient> CREATOR =
            new Parcelable.Creator<Ingredient>() {

                public Ingredient createFromParcel(Parcel in) {
                    return new Ingredient(in);
                }

                public Ingredient[] newArray(int size) {
                    return new Ingredient[size];
                }
            };
    @SerializedName("ingredient")
    String name;

    @SerializedName("measure")
    String measure;

    @SerializedName("quantity")
    float quantity;

    public Ingredient(String name, String measure, float quantity) {
        this.name = name;
        this.measure = measure;
        this.quantity = quantity;
    }

    public Ingredient(Parcel in) {
        name = in.readString();
        measure = in.readString();
        quantity = in.readFloat();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    public float getQuantity() {
        return quantity;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(measure);
        parcel.writeFloat(quantity);
    }
}
