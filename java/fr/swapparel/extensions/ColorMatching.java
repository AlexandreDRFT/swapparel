package fr.swapparel.extensions;

import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ColorMatching {
    //Pinterest algorithm v2.0
    public static List<String> matchPinterest(String color)
    {
        String[] pretendants = {"#F0F0F0", "#E4D4C5", "#C0C0C0", "#87CEEB", "#FFC0CB", "#FFFF00", "#FFA500", "#000000", "#A52A2A", "#000080", "#008000", "#FF0000", "#800080"};
        //White, Beige, Grey, Sky Blue, Pink, Yellow, Orange, Black, Brown, Navy, Green, Red, Purple

        float t = 100000000000f;
        float score;

        int id = -1;
        int idFinal = -1;

        for (String pretendant : pretendants)
        {
            id++;
            score = calculateSimilarity(color, pretendant);
            if(score < t) {
                idFinal = id;
                t = score;
            }
        }

        String[] whiteColors = { "#131313", "#F00000", "#00008B", "#87CEFA", "#00ff00", "#3399ff", "#ffcc66", "#000066", "#ff6600" };
        String[] beigeColors = { "#101010", "#511F16", "#A32D29", "#417F40", "#6F7685", "#D0C7A8", "#BABBB5"};
        String[] greyColors = { "#A52A2A", "#273C2B", "#C0C0C0", "#FF0000", "#3366ff" };
        String[] skyBlueColors = { "#512019", "#273C2B", "#C82F67", "#422456", "#E6D5C5", "#A7B3BF" };
        String[] pinkColors = { "#3F1E55", "#DECDBD", "#A6AFBE", "#201349", "#0E103D"};
        String[] yellowColors = { "#0B0B0B", "#56251E", "#A3ADB9", "#211249", "#8B898E", "#CAA41B" };
        String[] orangeColors = { "#F2F2F2", "#DDCB67", "#0E0E0E", "#213625", "#6699ff", "#222E6A", "#5965B1", "#FF8856"};
        String[] blackColors = { "#F2F2F2", "#DECEBF", "#121212", "#000066", "#3366ff" };
        String[] brownColors = { "#F2F2F2", "#DECEBF", "#0E0E0E", "#A02226", "#223728"};
        String[] navyColors = { "#F2F2F2", "#DECEBF", "#E4D771", "#D03770", "#488645" };
        String[] greenColors = { "#F2F2F2", "#DECEBF", "#BBDAD5", "#E0D472", "#231552" };
        String[] redColors = { "#F2F2F2", "#DECEBF", "#BBDAD5", "#201349", "#131313", "#522216" };
        String[] purpleColors = { "#F2F2F2", "#DECEBF", "#BBDAD5", "#F0BCC9", "#366581", "#502016" };

        List<String> returnList;
        String[] returnArray;


        switch(idFinal)
        {
            case 0 : returnArray = whiteColors; break;
            case 1 : returnArray = beigeColors; break;
            case 2 : returnArray = greyColors; break;
            case 3 : returnArray = skyBlueColors; break;
            case 4 : returnArray = pinkColors; break;
            case 5 : returnArray = yellowColors; break;
            case 6 : returnArray = orangeColors; break;
            case 7 : returnArray = blackColors; break;
            case 8 : returnArray = brownColors; break;
            case 9 : returnArray = navyColors; break;
            case 10 : returnArray = greenColors; break;
            case 11 : returnArray = redColors; break;
            case 12 : returnArray = purpleColors; break;
            default : returnArray = whiteColors; break;
        }
        returnList = Arrays.asList(returnArray);
        return returnList;
    }


    //S and Vs are between 0 and 1. They are converted in the method.
    //Old, shit method of color matching
    public static List<String> matchHSV(float H, float S, float V)
    {
        List<Integer> outp = new ArrayList<>();
        List<String> realOutput = new ArrayList<>();

        //To comply w/ java, S and V are between 0 and 1, which is stupid.
        S = S * 255;
        V = V * 255;

        double y_H = H;
        double y_S = S;
        double y_V;
        double yx_H = 0;
        double yx_S = 0;
        double yx_V = 0;

        float[] f = {H, S/255, V/255};
        outp.add(Color.HSVToColor(f));

        if (V > 70) { y_V = V - 30; } else { y_V = V + 30; }
        float[] f2 = {(float)y_H, (float)y_S/255, (float)y_V/255};
        outp.add(Color.HSVToColor(f2));

        if ((H >= 0) && (H < 30))
        {
            yx_H = y_H = H + 30; yx_S = y_S = S; y_V = V;
            if (V > 70) { yx_V = V - 30; } else { yx_V = V + 30; }
        }

        if ((H >= 30) && (H < 60))
        {
            yx_H = y_H = H + 150;
            y_S = RC(S - 30);
            y_V = RC(V - 20);
            yx_S = RC(S - 50);
            yx_V = RC(V + 20);
        }

        if ((H >= 60) && (H < 180))
        {
            yx_H = y_H = H - 40;
            y_S = yx_S = S;
            y_V = V; if (V > 70) { yx_V = V - 30; } else { yx_V = V + 30; }
        }

        if ((H >= 180) && (H < 220))
        {
            yx_H = H - 170;
            y_H = H - 160;
            yx_S = y_S = S;
            y_V = V;
            if (V > 70) { yx_V = V - 30; } else { yx_V = V + 30; }

        }
        if ((H >= 220) && (H < 300))
        {
            yx_H = y_H = H;
            yx_S = y_S = RC(S - 40);
            y_V = V;
            if (V > 70) { yx_V = V - 30; } else { yx_V = V + 30; }
        }
        if (H >= 300)
        {
            if (S > 50) { y_S = yx_S = S - 40; } else { y_S = yx_S = S + 40; }
            yx_H = y_H = (H + 20) % 360;
            y_V = V;
            if (V > 70) { yx_V = V - 30; } else { yx_V = V + 30; }
        }

        float[] f3 = {(float)y_H, (float)y_S/255, (float)y_V/255};
        outp.add(Color.HSVToColor(f3));
        float[] f4 = {(float)yx_H, (float)yx_S/255, (float)yx_V/255};
        outp.add(Color.HSVToColor(f4));

        y_H = 0;
        y_S = 0;
        y_V = 100 - V;
        float[] f5 = {(float)y_H, (float)y_S/255, (float)y_V/255};
        outp.add(Color.HSVToColor(f5));

        y_H = 0;
        y_S = 0;
        y_V = V;
        float[] f6 = {(float)y_H, (float)y_S/255, (float)y_V/255};
        outp.add(Color.HSVToColor(f6));

        for (int i = 0; i < outp.size(); i++) {
            String hexColor = String.format("#%06X", (0xFFFFFF & outp.get(i)));
            realOutput.add(hexColor);
        }

        return realOutput;
    }

    private static float[] getRGB(String color)
    {
        int r = Color.red(Color.parseColor(color));
        int g = Color.green(Color.parseColor(color));
        int b = Color.blue(Color.parseColor(color));

        return new float[]{r, g, b};
    }

    public static int calculateSimilarity(String a, String b){
        float[] ch = getRGB(a);
        float a_r = ch[0];
        float a_g = ch[1];
        float a_b = ch[2];

        float[] bh = getRGB(b);
        float b_r = bh[0];
        float b_g = bh[1];
        float b_b = bh[2];

        double distance = (a_r - b_r)*(a_r - b_r) + (a_g - b_g)*(a_g - b_g) + (a_b - b_b)*(a_b - b_b);
        return (int) distance;
    }

    private static double RC(double x)
    {
        if (x > (double) 100) { return (double) 100; }
        if (x < 0) { return 0; } else { return x; }
    }
}

