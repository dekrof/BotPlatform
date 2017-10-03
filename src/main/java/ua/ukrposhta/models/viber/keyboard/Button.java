package ua.ukrposhta.models.viber.keyboard;

import com.google.gson.annotations.SerializedName;

public abstract class Button {
    @SerializedName("Columns")
    protected int columns;
    @SerializedName("Rows")
    protected int rows;
    @SerializedName("Text")
    protected String text;
    @SerializedName("BgColor")
    protected String bgColor = "#fec62b";
    @SerializedName("TextSize")
    protected String textSize = "regular";
    @SerializedName("TextHAlign")
    protected String textHAlign = "center";

    public void setColumn(int column) {
        this.columns = column;
    }

    public void setRow(int row) {
        this.rows = row;
    }

    public void setText(String text) {
        this.text = text;
    }
}
