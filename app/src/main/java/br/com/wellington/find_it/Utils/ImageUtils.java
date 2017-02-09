package br.com.wellington.find_it.Utils;

import android.graphics.Bitmap;
import android.util.Base64;


import java.io.ByteArrayOutputStream;

/**
 * Classe Nova
 *
 * @author Wellington
 * @version 1.0 - 13/01/2017.
 */
public class ImageUtils {

    public static String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

}

