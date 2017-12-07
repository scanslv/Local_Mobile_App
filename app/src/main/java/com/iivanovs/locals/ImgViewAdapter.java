package com.iivanovs.locals;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.iivanovs.locals.entity.LocalImg;

import java.util.ArrayList;

public class ImgViewAdapter extends ArrayAdapter {
    private Context context;
    private ArrayList<LocalImg> data = new ArrayList();
    private DBManager db;

    public ImgViewAdapter(Context context, ArrayList<LocalImg> data) {
        super(context, 0, data);
        this.context = context;
        this.data = data;
        db = new DBManager(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(R.layout.img_layout, parent, false);
            holder = new ViewHolder();
            holder.deleteImg = (ImageView) row.findViewById(R.id.remove_location_image);
            holder.image = (ImageView) row.findViewById(R.id.location_image);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        final LocalImg item = (LocalImg) data.get(position);
        holder.deleteImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.deleteLocalImg(item);
            }
        });
        setPic(holder.image, item.getImg_path());
//        holder.image.setImageBitmap(item.getImg_path());
        return row;
    }

    static class ViewHolder {
        ImageView deleteImg;
        ImageView image;
    }

    private void setPic(ImageView imgview, String path) {
//        int targetW = imgview.getWidth();
//        int targetH = imgview.getHeight();
        int targetW = 110;
        int targetH = 110;

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        imgview.setImageBitmap(bitmap);
    }
}
