package com.example.mad_project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerViewHistory;
    private TextView noHistoryText;
    private HistoryAdapter historyAdapter;
    private SharedPrefManager prefManager;
    private ConversionHistoryDBHelper historyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Conversion History");
        }

        prefManager = new SharedPrefManager(this);
        historyDB = new ConversionHistoryDBHelper(this);

        recyclerViewHistory = findViewById(R.id.recyclerViewAllHistory);
        noHistoryText = findViewById(R.id.noHistoryText);

        // Setup RecyclerView
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new HistoryAdapter(null, history -> {
            showHistoryOptions(history);
        });
        recyclerViewHistory.setAdapter(historyAdapter);

        loadHistory();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadHistory() {
        try {
            String userEmail = prefManager.getUserEmail();
            if (historyDB != null) {
                List<ConversionHistory> historyList;
                if (userEmail != null && !userEmail.trim().isEmpty()) {
                    historyList = historyDB.getConversionsByUser(userEmail);
                } else {
                    historyList = historyDB.getAllConversions();
                }

                // Fallback to all conversions if user-specific list is empty
                if ((historyList == null || historyList.isEmpty()) && userEmail != null) {
                    historyList = historyDB.getAllConversions();
                }

                if (historyList != null && !historyList.isEmpty()) {
                    noHistoryText.setVisibility(View.GONE);
                    recyclerViewHistory.setVisibility(View.VISIBLE);
                    historyAdapter.updateHistory(historyList);
                } else {
                    noHistoryText.setVisibility(View.VISIBLE);
                    recyclerViewHistory.setVisibility(View.GONE);
                }
            } else {
                noHistoryText.setVisibility(View.VISIBLE);
                recyclerViewHistory.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            noHistoryText.setVisibility(View.VISIBLE);
            recyclerViewHistory.setVisibility(View.GONE);
        }
    }

    private void showHistoryOptions(ConversionHistory history) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(history.getConversionType());
        
        String message = "Original: " + history.getOriginalFile() + "\n\n" +
                        "Converted: " + history.getFileName() + "\n\n" +
                        "Date: " + new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", 
                                java.util.Locale.getDefault()).format(new java.util.Date(history.getTimestamp()));
        
        builder.setMessage(message);
        
        builder.setPositiveButton("Download", (dialog, which) -> {
            FileConversionUtil util = new FileConversionUtil(this);
            try {
                File sourceFile = new File(history.getConvertedFile());
                if (sourceFile.exists()) {
                    util.copyToDownloads(history.getConvertedFile());
                    Toast.makeText(this, "File saved to Downloads", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "File not found - it may have been deleted", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Close", null);
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistory();
    }
}
