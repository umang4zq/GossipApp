package com.example.gossipapp;

import android.app.Activity;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.widget.TextView;
import android.widget.Toast;
import androidx.viewpager2.widget.ViewPager2;

import java.io.File;

public class PdfViewerActivity extends Activity {

    private PdfRenderer pdfRenderer;
    private ParcelFileDescriptor descriptor;
    private TextView tvPageIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        ViewPager2 pager = findViewById(R.id.pdfPager);
        tvPageIndicator = findViewById(R.id.tvPageIndicator);

        String pdfPath = getIntent().getStringExtra("pdf_path");

        try {
            File file = new File(pdfPath);
            descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            pdfRenderer = new PdfRenderer(descriptor);

            int totalPages = pdfRenderer.getPageCount();
            updatePageIndicator(0, totalPages);

            PdfPagerAdapter adapter = new PdfPagerAdapter(this, pdfRenderer);
            pager.setAdapter(adapter);

            pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    updatePageIndicator(position, totalPages);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading PDF", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void updatePageIndicator(int currentPage, int totalPages) {
        tvPageIndicator.setText((currentPage + 1) + " / " + totalPages);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (pdfRenderer != null) pdfRenderer.close();
            if (descriptor != null) descriptor.close();
        } catch (Exception ignored) {}
    }
}
