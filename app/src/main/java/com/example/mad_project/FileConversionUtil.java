package com.example.mad_project;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileConversionUtil {
    private Context context;

    public FileConversionUtil(Context context) {
        this.context = context;
    }

    public String convertImageToJPG(Uri imageUri, String outputFileName) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
        if (inputStream == null) {
            throw new IOException("Cannot read image file");
        }
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        if (inputStream != null) inputStream.close();

        if (bitmap == null) {
            throw new IOException("Cannot decode image. File may be corrupted or invalid format.");
        }

        File outputDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Converted");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        String fileName = outputFileName != null ? outputFileName : "converted_" + System.currentTimeMillis() + ".jpg";
        File outputFile = new File(outputDir, fileName);

        FileOutputStream fos = new FileOutputStream(outputFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
        fos.flush();
        fos.close();

        return outputFile.getAbsolutePath();
    }

    public String convertImageToPNG(Uri imageUri, String outputFileName) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
        if (inputStream == null) {
            throw new IOException("Cannot read image file");
        }
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        if (inputStream != null) inputStream.close();
        
        if (bitmap == null) {
            throw new IOException("Cannot decode image. File may be corrupted or invalid format.");
        }

        File outputDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Converted");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        String fileName = outputFileName != null ? outputFileName : "converted_" + System.currentTimeMillis() + ".png";
        File outputFile = new File(outputDir, fileName);

        FileOutputStream fos = new FileOutputStream(outputFile);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.flush();
        fos.close();

        return outputFile.getAbsolutePath();
    }

    public String convertImageToPDF(Uri imageUri, String outputFileName) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
        if (inputStream == null) {
            throw new IOException("Cannot read image file");
        }
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        if (inputStream != null) inputStream.close();
        
        if (bitmap == null) {
            throw new IOException("Cannot decode image. File may be corrupted or invalid format.");
        }

        int pageWidth = 612;
        int pageHeight = 792;
        int margin = 36; // Half-inch margin on all sides
        
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        canvas.drawColor(Color.WHITE);

        // Calculate available space (page minus margins)
        int availableWidth = pageWidth - (2 * margin);
        int availableHeight = pageHeight - (2 * margin);
        
        // Calculate scale to fit image on page while maintaining aspect ratio
        float scale = Math.min((float) availableWidth / bitmap.getWidth(), (float) availableHeight / bitmap.getHeight());
        float scaledWidth = bitmap.getWidth() * scale;
        float scaledHeight = bitmap.getHeight() * scale;
        // Center the image within the available space
        float left = margin + ((availableWidth - scaledWidth) / 2);
        float top = margin + ((availableHeight - scaledHeight) / 2);

        // Create destination rectangle for scaled image
        android.graphics.Rect destRect = new android.graphics.Rect(
            (int) left,
            (int) top,
            (int) (left + scaledWidth),
            (int) (top + scaledHeight)
        );
        
        // Draw bitmap scaled to fit the destination rectangle
        canvas.drawBitmap(bitmap, null, destRect, null);
        pdfDocument.finishPage(page);
        
        bitmap.recycle();

        File outputDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Converted");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        String fileName = outputFileName != null ? outputFileName : "converted_" + System.currentTimeMillis() + ".pdf";
        File outputFile = new File(outputDir, fileName);

        FileOutputStream fos = new FileOutputStream(outputFile);
        pdfDocument.writeTo(fos);
        pdfDocument.close();
        fos.flush();
        fos.close();

        return outputFile.getAbsolutePath();
    }

    public String convertImagesToPDF(Uri[] imageUris, String outputFileName) throws IOException {
        PdfDocument pdfDocument = new PdfDocument();
        int pageWidth = 612;
        int pageHeight = 792;
        int margin = 36; // Half-inch margin on all sides

        for (int i = 0; i < imageUris.length; i++) {
            Uri imageUri = imageUris[i];
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) continue;

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();

            if (bitmap == null) continue;

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, i + 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);

            Canvas canvas = page.getCanvas();
            canvas.drawColor(Color.WHITE);

            // Calculate available space (page minus margins)
            int availableWidth = pageWidth - (2 * margin);
            int availableHeight = pageHeight - (2 * margin);
            
            // Scale image to fit page while maintaining aspect ratio
            float scale = Math.min((float) availableWidth / bitmap.getWidth(), (float) availableHeight / bitmap.getHeight());
            float scaledWidth = bitmap.getWidth() * scale;
            float scaledHeight = bitmap.getHeight() * scale;
            // Center the image within the available space
            float left = margin + ((availableWidth - scaledWidth) / 2);
            float top = margin + ((availableHeight - scaledHeight) / 2);

            // Create destination rectangle for scaled image
            android.graphics.Rect destRect = new android.graphics.Rect(
                (int) left,
                (int) top,
                (int) (left + scaledWidth),
                (int) (top + scaledHeight)
            );
            
            // Draw bitmap scaled to fit the destination rectangle
            canvas.drawBitmap(bitmap, null, destRect, null);
            pdfDocument.finishPage(page);

            bitmap.recycle();
        }

        File outputDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Converted");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        String fileName = outputFileName != null ? outputFileName : "images_" + System.currentTimeMillis() + ".pdf";
        File outputFile = new File(outputDir, fileName);

        FileOutputStream fos = new FileOutputStream(outputFile);
        pdfDocument.writeTo(fos);
        pdfDocument.close();
        fos.flush();
        fos.close();

        return outputFile.getAbsolutePath();
    }

    public String convertDOCToPDF(Uri docUri, String outputFileName) throws IOException {
        // Read DOC file content
        InputStream inputStream = context.getContentResolver().openInputStream(docUri);
        if (inputStream == null) {
            throw new IOException("Cannot read DOC file");
        }

        // Read document content
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int nRead;
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        byte[] docBytes = buffer.toByteArray();
        inputStream.close();

        // Extract text from DOC/DOCX file
        String documentText = extractTextFromDOC(docBytes);

        // Create PDF with extracted text
        PdfDocument pdfDocument = new PdfDocument();
        int pageWidth = 612;
        int pageHeight = 792;
        int lineHeight = 30;
        int margin = 72;
        int currentY = margin;
        int pageNumber = 1;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        canvas.drawColor(Color.WHITE);

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(14);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setAntiAlias(true);

        // Split text into lines and add to PDF
        String[] lines = documentText.split("\n");
        for (String line : lines) {
            // Handle text wrapping
            if (line.length() > 80) {
                String[] words = line.split(" ");
                StringBuilder currentLine = new StringBuilder();
                for (String word : words) {
                    if ((currentLine + word).length() > 80) {
                        if (currentLine.length() > 0) {
                            canvas.drawText(currentLine.toString(), margin, currentY, paint);
                            currentY += lineHeight;
                            if (currentY > pageHeight - margin - lineHeight) {
                                pdfDocument.finishPage(page);
                                pageNumber++;
                                pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
                                page = pdfDocument.startPage(pageInfo);
                                canvas = page.getCanvas();
                                canvas.drawColor(Color.WHITE);
                                currentY = margin;
                            }
                        }
                        currentLine = new StringBuilder(word);
                    } else {
                        if (currentLine.length() > 0) {
                            currentLine.append(" ");
                        }
                        currentLine.append(word);
                    }
                }
                if (currentLine.length() > 0) {
                    canvas.drawText(currentLine.toString(), margin, currentY, paint);
                    currentY += lineHeight;
                }
            } else {
                if (currentY > pageHeight - margin - lineHeight) {
                    pdfDocument.finishPage(page);
                    pageNumber++;
                    pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
                    page = pdfDocument.startPage(pageInfo);
                    canvas = page.getCanvas();
                    canvas.drawColor(Color.WHITE);
                    currentY = margin;
                }
                canvas.drawText(line, margin, currentY, paint);
                currentY += lineHeight;
            }
        }

        pdfDocument.finishPage(page);

        File outputDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Converted");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        String fileName = outputFileName != null ? outputFileName : "converted_" + System.currentTimeMillis() + ".pdf";
        File outputFile = new File(outputDir, fileName);

        FileOutputStream fos = new FileOutputStream(outputFile);
        pdfDocument.writeTo(fos);
        pdfDocument.close();
        fos.flush();
        fos.close();

        return outputFile.getAbsolutePath();
    }

    private String extractTextFromDOC(byte[] docBytes) {
        StringBuilder text = new StringBuilder();
        
        // Check if it's a DOCX file (starts with PK - ZIP signature)
        boolean isDocx = docBytes.length > 4 && 
                         docBytes[0] == 0x50 && docBytes[1] == 0x4B && 
                         (docBytes[2] == 0x03 || docBytes[2] == 0x05 || docBytes[2] == 0x07);

        if (isDocx) {
            // DOCX is a ZIP file - need to extract and parse properly
            try {
                // Save to temp file first
                File tempDir = new File(context.getCacheDir(), "temp_doc");
                if (!tempDir.exists()) {
                    tempDir.mkdirs();
                }
                File tempDocxFile = new File(tempDir, "temp_" + System.currentTimeMillis() + ".docx");
                FileOutputStream tempFos = new FileOutputStream(tempDocxFile);
                tempFos.write(docBytes);
                tempFos.flush();
                tempFos.close();
                
                // Extract text using proper DOCX extractor
                String extractedText = DOCXTextExtractor.extractTextFromDOCX(tempDocxFile);
                text.append(extractedText);
                
                // Clean up
                tempDocxFile.delete();
                
            } catch (Exception e) {
                // Fall back to string-based extraction if ZIP parsing fails
                String docContent = new String(docBytes, StandardCharsets.UTF_8);
                
                // Look for XML text nodes <w:t>content</w:t>
                int startIndex = 0;
                while (true) {
                    int openTag = docContent.indexOf("<w:t", startIndex);
                    if (openTag == -1) {
                        openTag = docContent.indexOf("<w:t>", startIndex);
                        if (openTag == -1) break;
                        openTag += 5;
                    } else {
                        int tagEnd = docContent.indexOf(">", openTag);
                        if (tagEnd == -1) break;
                        openTag = tagEnd + 1;
                    }
                    
                    int closeTag = docContent.indexOf("</w:t>", openTag);
                    if (closeTag == -1) {
                        closeTag = docContent.indexOf("<", openTag);
                        if (closeTag == -1) closeTag = docContent.length();
                    }
                    
                    if (closeTag > openTag) {
                        String extracted = docContent.substring(openTag, closeTag).trim();
                        extracted = extracted.replace("&amp;", "&")
                                           .replace("&lt;", "<")
                                           .replace("&gt;", ">")
                                           .replace("&quot;", "\"")
                                           .replace("&apos;", "'");
                        if (!extracted.isEmpty()) {
                            text.append(extracted).append(" ");
                        }
                    }
                    startIndex = closeTag + 6;
                }
            }
        } else {
            // Old DOC format - extract readable text patterns
            String docContent = new String(docBytes);
            
            // Look for readable text sequences (at least 4 consecutive letters)
            StringBuilder word = new StringBuilder();
            for (int i = 0; i < docContent.length(); i++) {
                char c = docContent.charAt(i);
                if (Character.isLetterOrDigit(c) || Character.isWhitespace(c) || 
                    c == '.' || c == ',' || c == '!' || c == '?' || c == ':' || 
                    c == ';' || c == '-' || c == '(' || c == ')' || c == '\'' || 
                    c == '"' || c == '\n' || c == '\r') {
                    word.append(c);
                } else {
                    if (word.length() >= 4 && word.toString().matches(".*[a-zA-Z].*")) {
                        text.append(word.toString());
                        if (!word.toString().endsWith(".") && !word.toString().endsWith("!") && 
                            !word.toString().endsWith("?") && !word.toString().endsWith("\n")) {
                            text.append(" ");
                        }
                    }
                    word.setLength(0);
                }
            }
            if (word.length() >= 4) {
                text.append(word.toString());
            }
        }

        String result = text.toString().trim();
        // Clean up multiple spaces
        result = result.replaceAll(" +", " ").replaceAll("\n +", "\n");
        
        if (result.isEmpty()) {
            result = "Document content extracted.\n\n" +
                    "Note: This document may contain complex formatting or images\n" +
                    "that require specialized processing tools.";
        }

        return result;
    }

    public String convertPDFToWord(Uri pdfUri, String outputFileName) throws IOException {
        // Extract text from PDF and create RTF format (compatible with Word)
        InputStream inputStream = context.getContentResolver().openInputStream(pdfUri);
        if (inputStream == null) {
            throw new IOException("Cannot read PDF file");
        }

        // Save PDF to temp file for text extraction
        File tempDir = new File(context.getCacheDir(), "temp");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        File tempPdfFile = new File(tempDir, "temp_" + System.currentTimeMillis() + ".pdf");
        
        FileOutputStream tempFos = new FileOutputStream(tempPdfFile);
        byte[] buffer = new byte[8192];
        int nRead;
        while ((nRead = inputStream.read(buffer)) != -1) {
            tempFos.write(buffer, 0, nRead);
        }
        tempFos.flush();
        tempFos.close();
        inputStream.close();

        // Extract text from PDF using improved extraction
        String extractedText = PDFTextExtractor.extractTextFromPDF(tempPdfFile);
        
        // Clean up temp file
        tempPdfFile.delete();

        File outputDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Converted");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        String fileName = outputFileName != null ? outputFileName : "converted_" + System.currentTimeMillis() + ".rtf";
        File outputFile = new File(outputDir, fileName);

        // Create RTF file (Rich Text Format - compatible with Word)
        String rtfContent = createRTFDocument(extractedText);

        FileOutputStream fos = new FileOutputStream(outputFile);
        fos.write(rtfContent.getBytes("UTF-8"));
        fos.flush();
        fos.close();

        return outputFile.getAbsolutePath();
    }

    // Removed - now using PDFTextExtractor class for better extraction

    private void extractTextFromSection(String section, StringBuilder text) {
        // Extract text between parentheses
        int start = 0;
        while (true) {
            int openParen = section.indexOf("(", start);
            if (openParen == -1) break;
            
            int closeParen = section.indexOf(")", openParen);
            if (closeParen == -1) break;
            
            String textBetween = section.substring(openParen + 1, closeParen);
            // Skip very short strings (likely positioning commands)
            if (textBetween.length() > 1 && !textBetween.matches("^[\\d\\s-]+$")) {
                text.append(textBetween).append(" ");
            }
            
            start = closeParen + 1;
        }
        
        // Extract text between brackets (alternative PDF text encoding)
        start = 0;
        while (true) {
            int openBracket = section.indexOf("<", start);
            if (openBracket == -1 || openBracket == section.length() - 1) break;
            
            // Check if it's a hex string (starts with hex digits)
            char nextChar = section.charAt(openBracket + 1);
            if (Character.isLetterOrDigit(nextChar) && section.indexOf(">", openBracket) != -1) {
                int closeBracket = section.indexOf(">", openBracket);
                if (closeBracket != -1) {
                    String hexString = section.substring(openBracket + 1, closeBracket);
                    // Skip if too long (likely binary data)
                    if (hexString.length() < 100 && hexString.length() % 2 == 0) {
                        try {
                            // Convert hex to text
                            StringBuilder decoded = new StringBuilder();
                            for (int i = 0; i < hexString.length(); i += 2) {
                                if (i + 1 < hexString.length()) {
                                    String hex = hexString.substring(i, i + 2);
                                    int charCode = Integer.parseInt(hex, 16);
                                    if (charCode >= 32 && charCode <= 126) {
                                        decoded.append((char) charCode);
                                    }
                                }
                            }
                            if (decoded.length() > 0) {
                                text.append(decoded.toString()).append(" ");
                            }
                        } catch (Exception e) {
                            // Ignore hex conversion errors
                        }
                    }
                }
            }
            start = openBracket + 1;
        }
    }

    private String createRTFDocument(String text) {
        // Create RTF document format (Word-compatible)
        StringBuilder rtf = new StringBuilder();
        rtf.append("{\\rtf1\\ansi\\deff0 ");
        rtf.append("{\\fonttbl{\\f0 Times New Roman;}} ");
        rtf.append("{\\colortbl ;\\red0\\green0\\blue0;} ");
        rtf.append("\\f0\\fs24 ");
        
        // Escape RTF special characters
        String escapedText = text
                .replace("\\", "\\\\")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("\n", "\\par ");
        
        rtf.append(escapedText);
        rtf.append("}");
        
        return rtf.toString();
    }

    public String compressPDF(Uri pdfUri, String outputFileName) throws IOException {
        // Create temp directory
        File tempDir = new File(context.getCacheDir(), "temp_pdf");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        
        // Copy PDF to temp file
        InputStream inputStream = context.getContentResolver().openInputStream(pdfUri);
        if (inputStream == null) {
            throw new IOException("Cannot read PDF file");
        }
        
        File tempPdfFile = new File(tempDir, "temp_" + System.currentTimeMillis() + ".pdf");
        FileOutputStream tempFos = new FileOutputStream(tempPdfFile);
        
        byte[] buffer = new byte[8192];
        int nRead;
        while ((nRead = inputStream.read(buffer)) != -1) {
            tempFos.write(buffer, 0, nRead);
        }
        tempFos.flush();
        tempFos.close();
        inputStream.close();
        
        // Render PDF at lower quality to compress
        PdfDocument compressedPdf = new PdfDocument();
        
        try {
            ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(tempPdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);
            
            int pageCount = pdfRenderer.getPageCount();
            
            for (int i = 0; i < pageCount; i++) {
                PdfRenderer.Page page = pdfRenderer.openPage(i);
                
                // Compress by reducing resolution (50% of original)
                int width = page.getWidth() / 2;
                int height = page.getHeight() / 2;
                
                // Render page to bitmap at reduced size
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                bitmap.eraseColor(Color.WHITE);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
                
                // Add to compressed PDF
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, height, i + 1).create();
                PdfDocument.Page compressedPage = compressedPdf.startPage(pageInfo);
                Canvas canvas = compressedPage.getCanvas();
                canvas.drawBitmap(bitmap, 0, 0, null);
                compressedPdf.finishPage(compressedPage);
                
                bitmap.recycle();
                page.close();
            }
            
            pdfRenderer.close();
            fileDescriptor.close();
            
        } catch (Exception e) {
            compressedPdf.close();
            tempPdfFile.delete();
            throw new IOException("Failed to compress PDF: " + e.getMessage());
        }
        
        // Clean up temp file
        tempPdfFile.delete();
        
        File outputDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Converted");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        
        String fileName = outputFileName != null ? outputFileName : "compressed_" + System.currentTimeMillis() + ".pdf";
        File outputFile = new File(outputDir, fileName);
        
        FileOutputStream fos = new FileOutputStream(outputFile);
        compressedPdf.writeTo(fos);
        compressedPdf.close();
        fos.flush();
        fos.close();
        
        return outputFile.getAbsolutePath();
    }

    public String splitPDF(Uri pdfUri, int startPage, int endPage, String outputFileName) throws IOException {
        // Create temp directory
        File tempDir = new File(context.getCacheDir(), "temp_pdf");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        
        // Copy PDF to temp file
        InputStream inputStream = context.getContentResolver().openInputStream(pdfUri);
        if (inputStream == null) {
            throw new IOException("Cannot read PDF file");
        }
        
        File tempPdfFile = new File(tempDir, "temp_" + System.currentTimeMillis() + ".pdf");
        FileOutputStream tempFos = new FileOutputStream(tempPdfFile);
        
        byte[] buffer = new byte[8192];
        int nRead;
        while ((nRead = inputStream.read(buffer)) != -1) {
            tempFos.write(buffer, 0, nRead);
        }
        tempFos.flush();
        tempFos.close();
        inputStream.close();
        
        PdfDocument splitPdf = new PdfDocument();
        
        try {
            ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(tempPdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);
            
            int pageCount = pdfRenderer.getPageCount();
            
            // Validate page range
            if (startPage < 1) startPage = 1;
            if (endPage > pageCount) endPage = pageCount;
            if (startPage > endPage) {
                throw new IOException("Invalid page range");
            }
            
            // Extract specified pages (convert to 0-based index)
            int newPageNum = 1;
            for (int i = startPage - 1; i < endPage; i++) {
                PdfRenderer.Page page = pdfRenderer.openPage(i);
                
                int width = page.getWidth();
                int height = page.getHeight();
                
                // Render page to bitmap
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                bitmap.eraseColor(Color.WHITE);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
                
                // Add to split PDF
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, height, newPageNum++).create();
                PdfDocument.Page splitPage = splitPdf.startPage(pageInfo);
                Canvas canvas = splitPage.getCanvas();
                canvas.drawBitmap(bitmap, 0, 0, null);
                splitPdf.finishPage(splitPage);
                
                bitmap.recycle();
                page.close();
            }
            
            pdfRenderer.close();
            fileDescriptor.close();
            
        } catch (Exception e) {
            splitPdf.close();
            tempPdfFile.delete();
            throw new IOException("Failed to split PDF: " + e.getMessage());
        }
        
        // Clean up temp file
        tempPdfFile.delete();
        
        File outputDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Converted");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        
        String fileName = outputFileName != null ? outputFileName : "split_" + System.currentTimeMillis() + ".pdf";
        File outputFile = new File(outputDir, fileName);
        
        FileOutputStream fos = new FileOutputStream(outputFile);
        splitPdf.writeTo(fos);
        splitPdf.close();
        fos.flush();
        fos.close();
        
        return outputFile.getAbsolutePath();
    }

    public String mergePDFs(Uri[] pdfUris, String outputFileName) throws IOException {
        PdfDocument mergedPdf = new PdfDocument();
        
        // Create temp directory
        File tempDir = new File(context.getCacheDir(), "temp_pdf");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        // Process each PDF and render its pages
        for (int pdfIndex = 0; pdfIndex < pdfUris.length; pdfIndex++) {
            Uri pdfUri = pdfUris[pdfIndex];
            
            // Copy PDF to temp file
            InputStream inputStream = context.getContentResolver().openInputStream(pdfUri);
            if (inputStream == null) continue;
            
            File tempPdfFile = new File(tempDir, "temp_" + pdfIndex + "_" + System.currentTimeMillis() + ".pdf");
            FileOutputStream tempFos = new FileOutputStream(tempPdfFile);
            
            byte[] buffer = new byte[8192];
            int nRead;
            while ((nRead = inputStream.read(buffer)) != -1) {
                tempFos.write(buffer, 0, nRead);
            }
            tempFos.flush();
            tempFos.close();
            inputStream.close();

            // Render each page from this PDF
            try {
                ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(tempPdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
                PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);
                
                int pageCount = pdfRenderer.getPageCount();
                
                for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                    PdfRenderer.Page page = pdfRenderer.openPage(pageIndex);
                    
                    // Get page dimensions
                    int width = page.getWidth();
                    int height = page.getHeight();
                    
                    // Render page to bitmap
                    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    bitmap.eraseColor(Color.WHITE);
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
                    
                    // Add to merged PDF
                    PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, height, pageIndex + 1).create();
                    PdfDocument.Page mergedPage = mergedPdf.startPage(pageInfo);
                    Canvas mergedCanvas = mergedPage.getCanvas();
                    mergedCanvas.drawBitmap(bitmap, 0, 0, null);
                    mergedPdf.finishPage(mergedPage);
                    
                    bitmap.recycle();
                    page.close();
                }
                
                pdfRenderer.close();
                fileDescriptor.close();
                
            } catch (Exception e) {
                // If rendering fails, continue with next PDF
                e.printStackTrace();
            }
            
            // Clean up temp file
            tempPdfFile.delete();
        }

        File outputDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Converted");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        String fileName = outputFileName != null ? outputFileName : "merged_" + System.currentTimeMillis() + ".pdf";
        File outputFile = new File(outputDir, fileName);

        FileOutputStream fos = new FileOutputStream(outputFile);
        mergedPdf.writeTo(fos);
        mergedPdf.close();
        fos.flush();
        fos.close();

        return outputFile.getAbsolutePath();
    }

    public void copyToDownloads(String sourcePath) throws IOException {
        File sourceFile = new File(sourcePath);
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File destFile = new File(downloadsDir, sourceFile.getName());
        
        FileInputStream fis = new FileInputStream(sourceFile);
        FileOutputStream fos = new FileOutputStream(destFile);
        
        byte[] buffer = new byte[1024];
        int length;
        while ((length = fis.read(buffer)) > 0) {
            fos.write(buffer, 0, length);
        }
        
        fis.close();
        fos.close();
    }

    public String generateOutputFileName(String originalFileName, String targetExtension) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        
        if (originalFileName != null && originalFileName.contains(".")) {
            String baseName = originalFileName.substring(0, originalFileName.lastIndexOf("."));
            return baseName + "_" + timestamp + "." + targetExtension;
        }
        
        return "converted_" + timestamp + "." + targetExtension;
    }
}

