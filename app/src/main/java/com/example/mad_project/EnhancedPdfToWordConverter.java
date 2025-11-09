package com.example.mad_project;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.cos.COSName;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDResources;
import com.tom_roush.pdfbox.pdmodel.graphics.PDXObject;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;
import com.tom_roush.pdfbox.rendering.PDFRenderer;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EnhancedPdfToWordConverter {
    private static final String TAG = "EnhancedPdfConverter";
    private Context context;

    public EnhancedPdfToWordConverter(Context context) {
        this.context = context;
        PDFBoxResourceLoader.init(context);
    }

    public boolean convertPdfToWord(File pdfFile, File wordFile) {
        return convertPdfToWord(pdfFile, wordFile, null);
    }

    public boolean convertPdfToWord(File pdfFile, File wordFile, String password) {
        try {
            // Load PDF document (with password if provided)
            PDDocument document;
            if (password != null && !password.isEmpty()) {
                document = PDDocument.load(pdfFile, password);
            } else {
                document = PDDocument.load(pdfFile);
            }
            
            // Create Word document
            XWPFDocument wordDoc = new XWPFDocument();
            
            // Extract text with layout information
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);
            
            // Check if document has minimal text
            if (text == null || text.trim().isEmpty()) {
                Log.d(TAG, "Document has no text content");
                text = "[This PDF appears to contain scanned images or no text content]\n\n";
            }
            
            // Extract images
            List<Bitmap> images = extractImages(document);
            
            // Process the text and detect tables
            processContent(wordDoc, text, document);
            
            // Add extracted images at the end
            if (!images.isEmpty()) {
                addImagesToDocument(wordDoc, images);
            }
            
            // Save Word document
            FileOutputStream out = new FileOutputStream(wordFile);
            wordDoc.write(out);
            out.close();
            wordDoc.close();
            document.close();
            
            Log.d(TAG, "Conversion successful: " + wordFile.getAbsolutePath());
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Conversion failed", e);
            return false;
        }
    }

    // OCR functionality removed to avoid dependency issues
    // Text-based PDFs will extract text normally
    // Scanned PDFs will show a message that they contain images

    private List<Bitmap> extractImages(PDDocument document) {
        List<Bitmap> images = new ArrayList<>();
        
        try {
            for (PDPage page : document.getPages()) {
                PDResources resources = page.getResources();
                
                if (resources != null) {
                    Iterable<COSName> xObjectNames = resources.getXObjectNames();
                    if (xObjectNames != null) {
                        for (COSName name : xObjectNames) {
                            PDXObject xObject = resources.getXObject(name);
                            
                            if (xObject instanceof PDImageXObject) {
                                PDImageXObject image = (PDImageXObject) xObject;
                                Bitmap bitmap = image.getImage();
                                
                                if (bitmap != null) {
                                    images.add(bitmap);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Image extraction failed", e);
        }
        
        return images;
    }

    private void addImagesToDocument(XWPFDocument wordDoc, List<Bitmap> images) {
        try {
            // Add separator
            XWPFParagraph separator = wordDoc.createParagraph();
            XWPFRun sepRun = separator.createRun();
            sepRun.addBreak();
            sepRun.setText("--- Extracted Images ---");
            sepRun.setBold(true);
            sepRun.addBreak();
            
            // Add each image
            for (int i = 0; i < images.size(); i++) {
                Bitmap bitmap = images.get(i);
                
                XWPFParagraph imgPara = wordDoc.createParagraph();
                imgPara.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun imgRun = imgPara.createRun();
                
                // Convert bitmap to byte array
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] imageBytes = stream.toByteArray();
                stream.close();
                
                // Add image to document
                imgRun.addPicture(
                    new java.io.ByteArrayInputStream(imageBytes),
                    XWPFDocument.PICTURE_TYPE_PNG,
                    "image_" + i + ".png",
                    Units.toEMU(400), // width
                    Units.toEMU(300)  // height
                );
                
                imgRun.addBreak();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to add images to document", e);
        }
    }

    private void processContent(XWPFDocument wordDoc, String text, PDDocument pdfDoc) throws IOException {
        String[] lines = text.split("\\n");
        List<String> tableLines = new ArrayList<>();
        boolean inTable = false;
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            
            if (line.isEmpty()) {
                if (inTable && !tableLines.isEmpty()) {
                    createTable(wordDoc, tableLines);
                    tableLines.clear();
                    inTable = false;
                }
                continue;
            }
            
            // Detect if line is part of a table (has multiple columns)
            if (isTableRow(line)) {
                inTable = true;
                tableLines.add(line);
            } else {
                if (inTable && !tableLines.isEmpty()) {
                    createTable(wordDoc, tableLines);
                    tableLines.clear();
                    inTable = false;
                }
                
                // Add as regular paragraph
                createParagraph(wordDoc, line);
            }
        }
        
        // Handle remaining table lines
        if (!tableLines.isEmpty()) {
            createTable(wordDoc, tableLines);
        }
    }

    private boolean isTableRow(String line) {
        String trimmed = line.trim();
        String[] parts = trimmed.split("\\s{2,}");
        return parts.length >= 2;
    }

    private void createTable(XWPFDocument wordDoc, List<String> tableLines) {
        if (tableLines.isEmpty()) return;
        
        String firstLine = tableLines.get(0);
        String[] firstCols = firstLine.trim().split("\\s{2,}");
        int numCols = firstCols.length;
        
        XWPFTable table = wordDoc.createTable(tableLines.size(), numCols);
        
        for (int i = 0; i < tableLines.size(); i++) {
            String line = tableLines.get(i);
            String[] cols = line.trim().split("\\s{2,}");
            XWPFTableRow row = table.getRow(i);
            
            for (int j = 0; j < Math.min(cols.length, numCols); j++) {
                XWPFTableCell cell = row.getCell(j);
                cell.setText(cols[j].trim());
                
                if (i == 0) {
                    cell.getParagraphs().get(0).getRuns().get(0).setBold(true);
                }
            }
        }
        
        wordDoc.createParagraph();
    }

    private void createParagraph(XWPFDocument wordDoc, String text) {
        XWPFParagraph paragraph = wordDoc.createParagraph();
        XWPFRun run = paragraph.createRun();
        
        if (isHeading(text)) {
            run.setBold(true);
            run.setFontSize(14);
        } else if (text.startsWith("Note:") || text.startsWith("Holiday is when")) {
            run.setItalic(true);
        }
        
        run.setText(text);
    }

    private boolean isHeading(String text) {
        return (text.length() < 50 && text.equals(text.toUpperCase()) && text.matches(".*[A-Z].*")) ||
               text.matches("^[A-Z][a-z]+ [A-Z][a-z]+$");
    }

    public String getOutputFileName(File inputFile) {
        String fileName = inputFile.getName();
        String nameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));
        return nameWithoutExt + "_converted.docx";
    }

    public void cleanup() {
        // No resources to cleanup (OCR disabled)
    }
}
