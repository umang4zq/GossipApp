package com.example.gossipapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PdfPagerAdapter extends RecyclerView.Adapter<PdfPagerAdapter.PageHolder> {

    private PdfRenderer renderer;
    private Activity activity;

    public PdfPagerAdapter(Activity activity, PdfRenderer renderer) {
        this.activity = activity;
        this.renderer = renderer;
    }

    @NonNull
    @Override
    public PageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pdf_page, parent, false);
        return new PageHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PageHolder holder, int position) {
        PdfRenderer.Page page = renderer.openPage(position);

        int width = holder.itemView.getWidth();
        if (width == 0)
            width = activity.getResources().getDisplayMetrics().widthPixels;

        float ratio = (float) width / page.getWidth();
        int height = (int) (page.getHeight() * ratio);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        page.close();

        holder.img.setImageBitmap(bitmap);
    }

    @Override
    public int getItemCount() {
        return renderer.getPageCount();
    }

    static class PageHolder extends RecyclerView.ViewHolder {
        ImageView img;

        PageHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.pdfImage);
        }
    }
}
