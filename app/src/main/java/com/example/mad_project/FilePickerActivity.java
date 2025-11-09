package com.example.mad_project;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FilePickerActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;
    private static final int PICK_MULTIPLE_FILES_REQUEST = 2;

    private TextView selectedToolText, fileDetailsText;
    private Button browseButton, convertButton;
    private ImageView filePickerImage;
    private ProgressBar progressBar;

    private String selectedTool;
    private Uri selectedFileUri = null;
    private Uri[] selectedFileUris = null;
    private String originalFileName;
    private SharedPrefManager prefManager;
    private FileConversionUtil conversionUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check authentication first
        prefManager = new SharedPrefManager(this);
        if (!prefManager.isLoggedIn()) {
            Intent intent = new Intent(FilePickerActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_file_picker);

        selectedToolText = findViewById(R.id.selectedToolText);
        fileDetailsText = findViewById(R.id.fileDetailsText);
        browseButton = findViewById(R.id.browseButton);
        filePickerImage = findViewById(R.id.filePickerImage);

        convertButton = new Button(this);
        convertButton.setText("Convert");
        convertButton.setBackgroundTintList(getColorStateList(android.R.color.holo_green_dark));
        convertButton.setTextColor(getResources().getColor(android.R.color.white));
        convertButton.setVisibility(View.GONE);

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
        progressBar.setVisibility(View.GONE);

        LinearLayout root = findViewById(R.id.rootLayout);
        root.addView(convertButton);
        root.addView(progressBar);

        conversionUtil = new FileConversionUtil(this);

        selectedTool = getIntent().getStringExtra("SELECTED_TOOL");
        if (selectedTool != null) {
            selectedToolText.setText("Selected Tool: " + selectedTool);
        }

        filePickerImage.setOnClickListener(v -> openFilePicker());
        browseButton.setOnClickListener(v -> openFilePicker());
        convertButton.setOnClickListener(v -> {
            if (selectedTool != null && (selectedTool.equals("Merge PDF"))) {
                if (selectedFileUris != null && selectedFileUris.length > 1) {
                    performConversion(selectedTool, selectedFileUris);
                } else {
                    Toast.makeText(this, "Please select multiple PDF files first", Toast.LENGTH_SHORT).show();
                }
            } else if (selectedTool != null && selectedTool.equals("Image to PDF")) {
                if (selectedFileUris != null && selectedFileUris.length > 0) {
                    performConversion(selectedTool, selectedFileUris);
                } else if (selectedFileUri != null) {
                    performConversion(selectedTool, new Uri[]{selectedFileUri});
                } else {
                    Toast.makeText(this, "Please select image file(s) first", Toast.LENGTH_SHORT).show();
                }
            } else if (selectedFileUri != null) {
                performConversion(selectedTool, selectedFileUri);
            } else {
                Toast.makeText(this, "Please select a file first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        if (selectedTool != null) {
            if (selectedTool.equals("Merge PDF")) {
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("application/pdf");
            } else if (selectedTool.equals("PPT to PDF")) {
                // PPT to PDF - need PPT/PPTX file as input
                intent.setType("*/*");
            } else if (selectedTool.equals("Word to PDF") || selectedTool.equals("DOC to PDF")) {
                // Word to PDF - need DOC/DOCX file as input
                intent.setType("*/*");
            } else if (selectedTool.contains("Image")) {
                intent.setType("image/*");
                // Allow multiple image selection for Image to PDF
                if (selectedTool.equals("Image to PDF")) {
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                }
            } else if (selectedTool.contains("Compress PDF") || selectedTool.contains("Split PDF")) {
                // PDF tools - need PDF file as input
                intent.setType("application/pdf");
            } else {
                intent.setType("*/*");
            }
        } else {
            intent.setType("*/*");
        }

        intent.addCategory(Intent.CATEGORY_OPENABLE);

            if (selectedTool != null && (selectedTool.equals("Merge PDF") || selectedTool.equals("Image to PDF"))) {
                String chooserTitle = selectedTool.equals("Image to PDF") ? "Select image files" : "Select PDF files";
                startActivityForResult(Intent.createChooser(intent, chooserTitle), PICK_MULTIPLE_FILES_REQUEST);
            } else {
                String chooserTitle = "Select a file";
                if (selectedTool != null) {
                if (selectedTool.equals("PPT to PDF")) {
                    chooserTitle = "Select PowerPoint file (.ppt/.pptx)";
                } else if (selectedTool.equals("Word to PDF") || selectedTool.equals("DOC to PDF")) {
                    chooserTitle = "Select Word file (.doc or .docx)";
                } else if (selectedTool.contains("Image")) {
                    chooserTitle = "Select image file";
                } else if (selectedTool.contains("PDF")) {
                    chooserTitle = "Select PDF file";
                }
                }
                startActivityForResult(Intent.createChooser(intent, chooserTitle), PICK_FILE_REQUEST);
            }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            if (selectedFileUri != null) {
                originalFileName = getFileName(selectedFileUri);

                // Validate file type based on selected tool
                if (selectedTool != null) {
                    String lowerFileName = originalFileName != null ? originalFileName.toLowerCase() : "";
                    if (selectedTool.equals("PPT to PDF")) {
                        // Must be PPT or PPTX file
                        if (!lowerFileName.endsWith(".ppt") && !lowerFileName.endsWith(".pptx")) {
                            Toast.makeText(this, "Please select a .ppt or .pptx file", Toast.LENGTH_LONG).show();
                            selectedFileUri = null;
                            return;
                        }
                    } else if (selectedTool.equals("Word to PDF") || selectedTool.equals("DOC to PDF")) {
                        // Must be DOC/DOCX file
                        if (!lowerFileName.endsWith(".doc") && !lowerFileName.endsWith(".docx")) {
                            Toast.makeText(this, "Please select a .doc or .docx file", Toast.LENGTH_LONG).show();
                            selectedFileUri = null;
                            return;
                        }
                    } else if (selectedTool.equals("JPG to PNG")) {
                        // Must be JPG/JPEG file
                        if (!lowerFileName.endsWith(".jpg") && !lowerFileName.endsWith(".jpeg")) {
                            Toast.makeText(this, "Please select a .jpg or .jpeg file", Toast.LENGTH_LONG).show();
                            selectedFileUri = null;
                            return;
                        }
                    } else if (selectedTool.equals("PNG to JPG")) {
                        // Must be PNG file
                        if (!lowerFileName.endsWith(".png")) {
                            Toast.makeText(this, "Please select a .png file", Toast.LENGTH_LONG).show();
                            selectedFileUri = null;
                            return;
                        }
                    } else if (selectedTool.contains("Image")) {
                        // Must be image file
                        if (!lowerFileName.endsWith(".jpg") && !lowerFileName.endsWith(".jpeg") &&
                            !lowerFileName.endsWith(".png") && !lowerFileName.endsWith(".gif") &&
                            !lowerFileName.endsWith(".bmp") && !lowerFileName.endsWith(".webp")) {
                            Toast.makeText(this, "Please select an image file", Toast.LENGTH_LONG).show();
                            selectedFileUri = null;
                            return;
                        }
                    } else if (selectedTool.contains("PDF") && !selectedTool.contains("Word") && !selectedTool.contains("Image")) {
                        // PDF tools (Compress, Split) - must be PDF
                        if (!lowerFileName.endsWith(".pdf")) {
                            Toast.makeText(this, "Please select a PDF file", Toast.LENGTH_LONG).show();
                            selectedFileUri = null;
                            return;
                        }
                    }
                }

                fileDetailsText.setText("File Selected:\n" + originalFileName);
                browseButton.setVisibility(View.GONE);
                convertButton.setVisibility(View.VISIBLE);
                Toast.makeText(this, "File selected for: " + selectedTool, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PICK_MULTIPLE_FILES_REQUEST && resultCode == RESULT_OK) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                if (selectedTool != null && selectedTool.equals("Image to PDF")) {
                    selectedFileUris = new Uri[count];
                    for (int i = 0; i < count; i++) {
                        selectedFileUris[i] = data.getClipData().getItemAt(i).getUri();
                    }
                    fileDetailsText.setText("Selected " + count + " image file(s)");
                } else {
                    selectedFileUris = new Uri[count];
                    for (int i = 0; i < count; i++) {
                        selectedFileUris[i] = data.getClipData().getItemAt(i).getUri();
                    }
                    fileDetailsText.setText("Selected " + count + " PDF file(s)");
                }
                browseButton.setVisibility(View.GONE);
                convertButton.setVisibility(View.VISIBLE);
            } else if (data.getData() != null) {
                selectedFileUri = data.getData();
                originalFileName = getFileName(selectedFileUri);

                // Check if Image to PDF and multiple selection was attempted
                if (selectedTool != null && selectedTool.equals("Image to PDF")) {
                    // Single image selected
                    selectedFileUris = new Uri[]{selectedFileUri};
                }

                fileDetailsText.setText("File Selected:\n" + originalFileName);
                browseButton.setVisibility(View.GONE);
                convertButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private String getFileName(Uri uri) {
        String fileName = "Unknown File";
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return fileName;
    }

    private void performConversion(String tool, Uri fileUri) {
        performConversion(tool, new Uri[]{fileUri});
    }

    private void performConversion(String tool, Uri[] fileUris) {
        progressBar.setVisibility(View.VISIBLE);
        convertButton.setEnabled(false);

        new Thread(() -> {
            try {
                String convertedFilePath = null;
                String conversionType = tool;

                switch (tool) {
                    case "PPT to PDF":
                        convertedFilePath = conversionUtil.convertPPTToPDF(fileUris[0],
                                conversionUtil.generateOutputFileName(originalFileName, "pdf"));
                        break;
                    case "Word to PDF":
                    case "DOC to PDF":
                        convertedFilePath = conversionUtil.convertDOCToPDF(fileUris[0],
                                conversionUtil.generateOutputFileName(originalFileName, "pdf"));
                        break;
                    case "Image to PDF":
                        // Handle multiple images
                        if (fileUris.length > 1) {
                            convertedFilePath = conversionUtil.convertImagesToPDF(fileUris,
                                    conversionUtil.generateOutputFileName("images", "pdf"));
                        } else {
                            convertedFilePath = conversionUtil.convertImageToPDF(fileUris[0],
                                    conversionUtil.generateOutputFileName(originalFileName, "pdf"));
                        }
                        break;
                    case "JPG to PNG":
                        convertedFilePath = conversionUtil.convertImageToPNG(fileUris[0],
                                conversionUtil.generateOutputFileName(originalFileName, "png"));
                        break;
                    case "PNG to JPG":
                        convertedFilePath = conversionUtil.convertImageToJPG(fileUris[0],
                                conversionUtil.generateOutputFileName(originalFileName, "jpg"));
                        break;
                    case "Compress PDF":
                        convertedFilePath = conversionUtil.compressPDF(fileUris[0],
                                conversionUtil.generateOutputFileName(originalFileName, "pdf"));
                        break;
                    case "Split PDF":
                        // Show dialog to get page range from user
                        runOnUiThread(() -> showSplitPDFDialog(fileUris[0], originalFileName));
                        return; // Exit thread, dialog will handle conversion
                    case "Merge PDF":
                        convertedFilePath = conversionUtil.mergePDFs(fileUris,
                                conversionUtil.generateOutputFileName("merged", "pdf"));
                        break;
                    default:
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Conversion not implemented yet", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            convertButton.setEnabled(true);
                        });
                        return;
                }

                if (convertedFilePath != null) {
                    // ✅ FIXED: Save to conversion history
                    if (prefManager != null) {
                        String userEmail = prefManager.getUserEmail();
                        android.util.Log.d("FilePickerActivity", "Saving conversion for user: " + userEmail);

                        if (userEmail != null && !userEmail.isEmpty()) {
                            ConversionHistoryDBHelper historyDB = new ConversionHistoryDBHelper(FilePickerActivity.this);
                            String originalFile = (originalFileName != null) ? originalFileName : "Unknown";

                            long result = historyDB.addConversion(
                                    userEmail,
                                    conversionType,
                                    originalFile,
                                    convertedFilePath
                            );

                            android.util.Log.d("FilePickerActivity",
                                    "History saved with ID: " + result + ", Type: " + conversionType);
                        } else {
                            android.util.Log.e("FilePickerActivity", "User email is null — history not saved.");
                        }
                    }

                    final String finalPath = convertedFilePath;
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        convertButton.setEnabled(true);
                        showConversionSuccess(finalPath);
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Conversion failed - No output file created", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                        convertButton.setEnabled(true);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                final String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
                runOnUiThread(() -> {
                    android.app.AlertDialog.Builder errorDialog = new android.app.AlertDialog.Builder(this);
                    errorDialog.setTitle("Conversion Error");
                    errorDialog.setMessage("Error: " + errorMessage + "\n\nPlease try again or check if the file is valid.");
                    errorDialog.setPositiveButton("OK", null);
                    errorDialog.show();
                    progressBar.setVisibility(View.GONE);
                    convertButton.setEnabled(true);
                });
            }
        }).start();
    }

    private void showConversionSuccess(String filePath) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Conversion Successful");
        builder.setMessage("File saved at: " + new File(filePath).getName());
        builder.setPositiveButton("Download", (dialog, which) -> {
            // Copy to Downloads folder
            copyToDownloads(filePath);
            Toast.makeText(this, "File saved to Downloads", Toast.LENGTH_SHORT).show();
            finish();
        });
        builder.setNegativeButton("OK", (dialog, which) -> {
            finish();
        });
        builder.show();
    }

    private void copyToDownloads(String sourcePath) {
        try {
            FileConversionUtil util = new FileConversionUtil(this);
            util.copyToDownloads(sourcePath);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save to Downloads", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSplitPDFDialog(Uri pdfUri, String originalFileName) {
        progressBar.setVisibility(View.GONE);
        convertButton.setEnabled(true);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Split PDF - Enter Page Range");

        // Create custom layout for input
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        TextView instructionText = new TextView(this);
        instructionText.setText("Enter the page range to extract:\n(e.g., start page 1, end page 5)");
        layout.addView(instructionText);

        final android.widget.EditText startPageInput = new android.widget.EditText(this);
        startPageInput.setHint("Start Page (e.g., 1)");
        startPageInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(startPageInput);

        final android.widget.EditText endPageInput = new android.widget.EditText(this);
        endPageInput.setHint("End Page (e.g., 5)");
        endPageInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(endPageInput);

        builder.setView(layout);

        builder.setPositiveButton("Split", (dialog, which) -> {
            String startStr = startPageInput.getText().toString().trim();
            String endStr = endPageInput.getText().toString().trim();

            if (startStr.isEmpty() || endStr.isEmpty()) {
                Toast.makeText(this, "Please enter both start and end page numbers", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int startPage = Integer.parseInt(startStr);
                int endPage = Integer.parseInt(endStr);

                if (startPage < 1 || endPage < 1) {
                    Toast.makeText(this, "Page numbers must be greater than 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (startPage > endPage) {
                    Toast.makeText(this, "Start page must be less than or equal to end page", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Perform split in background
                progressBar.setVisibility(View.VISIBLE);
                convertButton.setEnabled(false);

                new Thread(() -> {
                    try {
                        String convertedFilePath = conversionUtil.splitPDF(pdfUri, startPage, endPage,
                                conversionUtil.generateOutputFileName(originalFileName, "pdf"));

                        if (convertedFilePath != null) {
                            // Save to conversion history
                            if (prefManager != null) {
                                String userEmail = prefManager.getUserEmail();
                                android.util.Log.d("FilePickerActivity", "Saving Split PDF conversion for user: " + userEmail);
                                if (userEmail != null) {
                                    ConversionHistoryDBHelper historyDB = new ConversionHistoryDBHelper(this);
                                    String convertedFile = new File(convertedFilePath).getName();
                                    long result = historyDB.addConversion(userEmail, "Split PDF", originalFileName, convertedFilePath);
                                    android.util.Log.d("FilePickerActivity", "Split PDF history saved with ID: " + result);
                                }
                            }

                            final String finalPath = convertedFilePath;
                            runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                convertButton.setEnabled(true);
                                showConversionSuccess(finalPath);
                            });
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Split failed - No output file created", Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                                convertButton.setEnabled(true);
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        final String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
                        runOnUiThread(() -> {
                            android.app.AlertDialog.Builder errorDialog = new android.app.AlertDialog.Builder(this);
                            errorDialog.setTitle("Split Error");
                            errorDialog.setMessage("Error: " + errorMessage);
                            errorDialog.setPositiveButton("OK", null);
                            errorDialog.show();
                            progressBar.setVisibility(View.GONE);
                            convertButton.setEnabled(true);
                        });
                    }
                }).start();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter valid page numbers", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            progressBar.setVisibility(View.GONE);
            convertButton.setEnabled(true);
        });

        builder.show();
    }
}
