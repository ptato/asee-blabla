package com.ptato.aseeblabla;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap>
{
    private ImageView imageView;

    public DownloadImageTask(ImageView _imageView) { imageView = _imageView; }

    @Override
    protected Bitmap doInBackground(String... urls)
    {
        Bitmap icon11 = null;
        try
        {
            String urlDisplay = urls[0];
            InputStream is = new URL(urlDisplay).openStream();
            icon11 = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e)
        {
            Log.e(this.getClass().getName(), "Can't download image");
            Log.e(this.getClass().getName(), e.getMessage());
        }
        return icon11;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap)
    {
        if (bitmap != null)
            imageView.setImageBitmap(bitmap);
        else
            imageView.setVisibility(View.GONE);
    }
}