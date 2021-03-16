package fr.swapparel.data;

import android.graphics.Bitmap;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import fr.swapparel.R;
import fr.swapparel.extensions.ImageLoader;

public class Apparel extends AbstractItem<Apparel, Apparel.ViewHolder> implements Serializable{

    public String type;
    public int heaviness;
    public String drawablePath;
    public String color;
    public List<String> complementaryColors;
    public double lowestScoreMatching; //Used only for Perfect Matches

    public Apparel(String type, String drawablePath, String color, int heaviness, List<String> complementaryColors, double lowestScoreMatching) {
        this.type = type;
        this.drawablePath = drawablePath;
        this.heaviness = heaviness;
        this.color = color;
        this.complementaryColors = complementaryColors;
        this.lowestScoreMatching = lowestScoreMatching;
    }

    // / Fast Adapter methods
    @Override
    public int getType() { return R.id.apparel; }

    @Override
    public int getLayoutRes() { return R.layout.list_item; }

    @NonNull
    @Override
    public ViewHolder getViewHolder(@NonNull View v) {
        return new ViewHolder(v);
    }

    //The logic to bind your data to the view
    @Override
    public void bindView(@NonNull ViewHolder viewHolder, @NonNull List payloads) {
        super.bindView(viewHolder, payloads);

        viewHolder.type.setText(type);
        Glide.with(viewHolder.itemView.getContext())
                .load(new ImageLoader(viewHolder.itemView.getContext()).
                        setFileName(drawablePath).
                        setDirectoryName("Snapparel").getFilePath())
                .into(viewHolder.image);
        viewHolder.image.setBorderColor(Color.parseColor(color));
        Random r = new Random();
        List<String> pickableColors = new ArrayList<>();
        pickableColors.addAll(complementaryColors);
        int choix = r.nextInt(pickableColors.size() - 1);
        viewHolder.complementaryColor1.setColorFilter(Color.parseColor(pickableColors.get(choix)));
        pickableColors.remove(choix);
        try {
            choix = r.nextInt(pickableColors.size() - 1);
            viewHolder.complementaryColor2.setColorFilter(Color.parseColor(pickableColors.get(choix)));
            pickableColors.remove(choix);
        } catch (Exception e) {
            viewHolder.complementaryColor2.setVisibility(View.INVISIBLE);
        }

        try {
            choix = r.nextInt(pickableColors.size() - 1);
            viewHolder.complementaryColor3.setColorFilter(Color.parseColor(pickableColors.get(choix)));
        } catch (Exception e) {
            viewHolder.complementaryColor3.setVisibility(View.INVISIBLE);
        }
    }

    // Manually create the ViewHolder class
    static class ViewHolder extends RecyclerView.ViewHolder implements Serializable {
        final TextView type;
        final CircularImageView image;
        final ImageView complementaryColor1;
        final ImageView complementaryColor2;
        final ImageView complementaryColor3;

        ViewHolder(View view) {
            super(view);
            this.type = view.findViewById(R.id.type);
            this.image = view.findViewById(R.id.image);
            this.complementaryColor1 = view.findViewById(R.id.color1);
            this.complementaryColor2 = view.findViewById(R.id.color2);
            this.complementaryColor3 = view.findViewById(R.id.color3);
        }
    }
}
