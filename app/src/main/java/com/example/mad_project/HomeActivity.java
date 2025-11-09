package com.example.mad_project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private GridLayout toolsGrid;
    private RecyclerView recyclerViewHistory;
    private TextView noRecentText;
    private HistoryAdapter historyAdapter;
    private SharedPrefManager prefManager;
    private ConversionHistoryDBHelper historyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_home);

            prefManager = new SharedPrefManager(this);
            historyDB = new ConversionHistoryDBHelper(this);

            // Check if user is logged in
            if (!prefManager.isLoggedIn()) {
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }

            toolsGrid = findViewById(R.id.toolsGrid);
            recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
            noRecentText = findViewById(R.id.noRecentText);
            TextView viewAllHistory = findViewById(R.id.viewAllHistory);
            
            // Setup View All History button
            if (viewAllHistory != null) {
                viewAllHistory.setOnClickListener(v -> {
                    Intent intent = new Intent(HomeActivity.this, HistoryActivity.class);
                    startActivity(intent);
                });
            }

            // Setup RecyclerView
            if (recyclerViewHistory != null) {
                recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
                historyAdapter = new HistoryAdapter(null, history -> {
                    openHistoryFile(history);
                });
                recyclerViewHistory.setAdapter(historyAdapter);
            }

            // Setup grid click listeners
            if (toolsGrid != null) {
                for (int i = 0; i < toolsGrid.getChildCount(); i++) {
                    View card = toolsGrid.getChildAt(i);

                    if (card instanceof CardView) {
                        card.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    TextView label = (TextView) ((CardView) v)
                                            .getChildAt(0)
                                            .findViewById(android.R.id.text1);

                                    if (label == null) {
                                        LinearLayout layout = (LinearLayout) ((CardView) v).getChildAt(0);
                                        if (layout != null && layout.getChildCount() > 0) {
                                            label = (TextView) layout.getChildAt(layout.getChildCount() - 1);
                                        }
                                    }

                                    if (label != null) {
                                        String selectedTool = label.getText().toString();
                                        Intent intent = new Intent(HomeActivity.this, FilePickerActivity.class);
                                        intent.putExtra("SELECTED_TOOL", selectedTool);
                                        startActivity(intent);
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(HomeActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }

            // Setup FloatingActionButton (+ Photo)
            FloatingActionButton fabPhoto = findViewById(R.id.fabPhoto);
            if (fabPhoto != null) {
                fabPhoto.setOnClickListener(v -> showFileTypeDialog());
            }

            loadHistory();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading screen: " + e.getMessage(), Toast.LENGTH_LONG).show();
            // Fallback to login if something goes wrong
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistory();
    }

    private void showFileTypeDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Select Conversion Type");
        String[] options = {
            "Image Conversion",
            "PDF Tools",
            "DOC Conversion"
        };

        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    showImageConversionDialog();
                    break;
                case 1:
                    showPDFToolsDialog();
                    break;
                case 2:
                    showDOCConversionDialog();
                    break;
            }
        });
        builder.show();
    }

    private void showImageConversionDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Image Conversion");
        String[] options = {
            "Image to PDF",
            "JPG to PNG",
            "PNG to JPG"
        };

        builder.setItems(options, (dialog, which) -> {
            String[] tools = {"Image to PDF", "JPG to PNG", "PNG to JPG"};
            openFilePicker(tools[which]);
        });
        builder.show();
    }

    private void showPDFToolsDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("PDF Tools");
        String[] options = {
            "PPT to PDF",
            "Compress PDF",
            "Split PDF",
            "Merge PDF"
        };

        builder.setItems(options, (dialog, which) -> {
            String[] tools = {"PPT to PDF", "Compress PDF", "Split PDF", "Merge PDF"};
            openFilePicker(tools[which]);
        });
        builder.show();
    }

    private void showDOCConversionDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("DOC Conversion");
        String[] options = {
            "DOC to PDF"
        };

        builder.setItems(options, (dialog, which) -> {
            openFilePicker("Word to PDF");
        });
        builder.show();
    }

    private void openFilePicker(String tool) {
        Intent intent = new Intent(HomeActivity.this, FilePickerActivity.class);
        intent.putExtra("SELECTED_TOOL", tool);
        startActivity(intent);
    }

    private void loadHistory() {
        try {
            String userEmail = prefManager.getUserEmail();
            android.util.Log.d("HomeActivity", "Loading history for user: " + userEmail);
            if (userEmail != null && historyDB != null) {
                List<ConversionHistory> historyList = historyDB.getConversionsByUser(userEmail);
                android.util.Log.d("HomeActivity", "History list size: " + (historyList != null ? historyList.size() : "null"));
                if (historyList != null && !historyList.isEmpty()) {
                    if (noRecentText != null) {
                        noRecentText.setVisibility(View.GONE);
                    }
                    if (recyclerViewHistory != null) {
                        recyclerViewHistory.setVisibility(View.VISIBLE);
                    }
                    if (historyAdapter != null) {
                        historyAdapter.updateHistory(historyList);
                    }
                    Toast.makeText(this, "Loaded " + historyList.size() + " conversions", Toast.LENGTH_SHORT).show();
                } else {
                    android.util.Log.d("HomeActivity", "No history found or list is empty");
                    if (noRecentText != null) {
                        noRecentText.setVisibility(View.VISIBLE);
                    }
                    if (recyclerViewHistory != null) {
                        recyclerViewHistory.setVisibility(View.GONE);
                    }
                }
            } else {
                android.util.Log.d("HomeActivity", "userEmail is null or historyDB is null");
                if (noRecentText != null) {
                    noRecentText.setVisibility(View.VISIBLE);
                }
                if (recyclerViewHistory != null) {
                    recyclerViewHistory.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            android.util.Log.e("HomeActivity", "Error loading history: " + e.getMessage());
            // Silently handle history loading errors
            if (noRecentText != null) {
                noRecentText.setVisibility(View.VISIBLE);
            }
            if (recyclerViewHistory != null) {
                recyclerViewHistory.setVisibility(View.GONE);
            }
        }
    }

    private void openHistoryFile(ConversionHistory history) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(history.getConversionType());
        builder.setMessage("Original: " + history.getOriginalFile() + "\n\n" +
                "Converted: " + history.getFileName());
        builder.setPositiveButton("Download", (dialog, which) -> {
            FileConversionUtil util = new FileConversionUtil(this);
            try {
                java.io.File sourceFile = new java.io.File(history.getConvertedFile());
                if (sourceFile.exists()) {
                    util.copyToDownloads(history.getConvertedFile());
                    Toast.makeText(this, "File saved to Downloads", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

}
