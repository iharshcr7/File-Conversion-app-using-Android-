package com.example.mad_project;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font;
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.apache.poi.xwpf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EnhancedWordToPdfConverter {
    private static final String TAG = "EnhancedWordConverter";
    private Context context;
    private static final float MARGIN = 50;
    private static final float LINE_HEIGHT = 15;
    private static final float FONT_SIZE = 12;

    public EnhancedWordToPdfConverter(Context context) {
        this.context = context;
        PDFBoxResourceLoader.init(context);
    }

    public boolean convertWordToPdf(File wordFile, File pdfFile) {
        String fileName = wordFile.getName().toLowerCase();
        
        // Check file extension
        if (fileName.endsWith(".docx")) {
            return convertDocxToPdf(wordFile, pdfFile);
        } else if (fileName.endsWith(".doc")) {
            return convertDocToPdf(wordFile, pdfFile);
        } else {
            Log.e(TAG, "Unsupported file format: " + fileName);
            return false;
        }
    }

    private boolean convertDocxToPdf(File wordFile, File pdfFile) {
        try (FileInputStream fis = new FileInputStream(wordFile);
             PDDocument document = new PDDocument()) {

            XWPFDocument docx = new XWPFDocument(fis);
            PDFont font = PDType1Font.HELVETICA;
            float fontSize = 12;
            float leading = 1.5f * fontSize;

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            float margin = 50;
            float yPosition = page.getMediaBox().getHeight() - margin;

            for (XWPFParagraph para : docx.getParagraphs()) {
                String text = para.getText();
                if (text == null || text.trim().isEmpty()) continue;

                // Split text into lines (simple word wrap)
                String[] words = text.split("\\s+");
                StringBuilder line = new StringBuilder();

                for (String word : words) {
                    String testLine = line + word + " ";
                    float size = font.getStringWidth(testLine) / 1000 * fontSize;
                    if (size > page.getMediaBox().getWidth() - 2 * margin) {
                        // Write line to PDF
                        yPosition -= leading;
                        if (yPosition <= margin) {
                            contentStream.endText();
                            contentStream.close();

                            // Add new page
                            page = new PDPage(PDRectangle.A4);
                            document.addPage(page);
                            contentStream = new PDPageContentStream(document, page);
                            contentStream.beginText();
                            contentStream.setFont(font, fontSize);
                            contentStream.newLineAtOffset(margin, page.getMediaBox().getHeight() - margin);
                            yPosition = page.getMediaBox().getHeight() - margin;
                        }
                        contentStream.showText(line.toString().trim());
                        contentStream.newLineAtOffset(0, -leading);
                        line = new StringBuilder(word + " ");
                    } else {
                        line = new StringBuilder(testLine);
                    }
                }

                // Write remaining text in line
                if (!line.isEmpty()) {
                    yPosition -= leading;
                    contentStream.showText(line.toString().trim());
                    contentStream.newLineAtOffset(0, -leading);
                }
            }

            contentStream.endText();
            contentStream.close();
            document.save(pdfFile);
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error converting DOCX to PDF: " + e.getMessage(), e);
            return false;
        }
    }


    private boolean convertDocToPdf(File wordFile, File pdfFile) {
        try {
            FileInputStream fis = new FileInputStream(wordFile);
            HWPFDocument docFile = new HWPFDocument(fis);
            
            PDDocument pdfDoc = new PDDocument();
            PDPage page = new PDPage(PDRectangle.A4);
            pdfDoc.addPage(page);
            
            float yPosition = page.getMediaBox().getHeight() - MARGIN;
            PDPageContentStream contentStream = new PDPageContentStream(pdfDoc, page);
            
            Range range = docFile.getRange();
            
            for (int i = 0; i < range.numParagraphs(); i++) {
                Paragraph para = range.getParagraph(i);
                String text = para.text();
                
                if (text != null && !text.trim().isEmpty()) {
                    contentStream.beginText();
                    
                    // Check if first character run is bold
                    PDFont font = PDType1Font.HELVETICA;
                    if (para.numCharacterRuns() > 0) {
                        CharacterRun charRun = para.getCharacterRun(0);
                        if (charRun.isBold()) {
                            font = PDType1Font.HELVETICA_BOLD;
                        }
                    }
                    
                    contentStream.setFont(font, FONT_SIZE);
                    contentStream.newLineAtOffset(MARGIN, yPosition);
                    
                    // Handle text wrapping
                    float maxWidth = page.getMediaBox().getWidth() - (2 * MARGIN);
                    List<String> lines = wrapText(text.trim(), font, FONT_SIZE, maxWidth);
                    
                    for (String line : lines) {
                        contentStream.showText(line);
                        yPosition -= FONT_SIZE + 2;
                        contentStream.newLineAtOffset(0, -(FONT_SIZE + 2));
                        
                        if (yPosition < MARGIN + 50) {
                            contentStream.endText();
                            contentStream.close();
                            page = new PDPage(PDRectangle.A4);
                            pdfDoc.addPage(page);
                            yPosition = page.getMediaBox().getHeight() - MARGIN;
                            contentStream = new PDPageContentStream(pdfDoc, page);
                            contentStream.beginText();
                            contentStream.setFont(font, FONT_SIZE);
                            contentStream.newLineAtOffset(MARGIN, yPosition);
                        }
                    }
                    
                    contentStream.endText();
                    yPosition -= 5;
                }
            }
            
            contentStream.close();
            pdfDoc.save(pdfFile);
            pdfDoc.close();
            docFile.close();
            fis.close();
            
            Log.d(TAG, "DOC conversion successful");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "DOC conversion failed", e);
            return false;
        }
    }

    private float addImageToPdf(PDDocument pdfDoc, PDPage page, PDPageContentStream contentStream, 
                                XWPFPicture picture, float yPosition) throws IOException {
        try {
            byte[] imageData = picture.getPictureData().getData();
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            
            if (bitmap != null) {
                PDImageXObject pdImage = LosslessFactory.createFromImage(pdfDoc, bitmap);
                
                float imageWidth = 400;
                float imageHeight = (bitmap.getHeight() * imageWidth) / bitmap.getWidth();
                
                if (yPosition - imageHeight < MARGIN) {
                    contentStream.close();
                    page = new PDPage(PDRectangle.A4);
                    pdfDoc.addPage(page);
                    yPosition = page.getMediaBox().getHeight() - MARGIN;
                    contentStream = new PDPageContentStream(pdfDoc, page);
                }
                
                contentStream.drawImage(pdImage, MARGIN, yPosition - imageHeight, imageWidth, imageHeight);
                yPosition -= imageHeight + 10;
                
                bitmap.recycle();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to add image", e);
        }
        
        return yPosition;
    }

    private float processParagraph(PDDocument pdfDoc, PDPage page, PDPageContentStream contentStream, 
                                   XWPFParagraph paragraph, float yPosition) throws IOException {
        String text = paragraph.getText();
        if (text == null || text.trim().isEmpty()) {
            return yPosition - LINE_HEIGHT;
        }
        
        PDFont font = PDType1Font.HELVETICA;
        float fontSize = FONT_SIZE;
        
        for (XWPFRun run : paragraph.getRuns()) {
            if (run.isBold()) {
                font = PDType1Font.HELVETICA_BOLD;
            }
            if (run.isItalic()) {
                font = PDType1Font.HELVETICA_OBLIQUE;
            }
            if (run.getFontSize() > 0) {
                fontSize = run.getFontSize();
            }
        }
        
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        
        float maxWidth = page.getMediaBox().getWidth() - (2 * MARGIN);
        List<String> lines = wrapText(text, font, fontSize, maxWidth);
        
        for (String line : lines) {
            contentStream.showText(line);
            yPosition -= fontSize + 2;
            contentStream.newLineAtOffset(0, -(fontSize + 2));
        }
        
        contentStream.endText();
        return yPosition - 5;
    }

    private float processTable(PDDocument pdfDoc, PDPage page, PDPageContentStream contentStream, 
                               XWPFTable table, float yPosition) throws IOException {
        
        float tableWidth = page.getMediaBox().getWidth() - (2 * MARGIN);
        List<XWPFTableRow> rows = table.getRows();
        
        if (rows.isEmpty()) return yPosition;
        
        int numCols = rows.get(0).getTableCells().size();
        float colWidth = tableWidth / numCols;
        float rowHeight = 20;
        
        for (int i = 0; i < rows.size(); i++) {
            XWPFTableRow row = rows.get(i);
            List<XWPFTableCell> cells = row.getTableCells();
            
            contentStream.setLineWidth(1f);
            contentStream.addRect(MARGIN, yPosition - rowHeight, tableWidth, rowHeight);
            contentStream.stroke();
            
            for (int j = 0; j < cells.size(); j++) {
                XWPFTableCell cell = cells.get(j);
                String cellText = cell.getText();
                
                if (cellText != null && !cellText.isEmpty()) {
                    contentStream.beginText();
                    
                    if (i == 0) {
                        contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE);
                    } else {
                        contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE);
                    }
                    
                    float xPos = MARGIN + (j * colWidth) + 5;
                    float yPos = yPosition - 15;
                    
                    contentStream.newLineAtOffset(xPos, yPos);
                    String truncated = truncateText(cellText, PDType1Font.HELVETICA, FONT_SIZE, colWidth - 10);
                    contentStream.showText(truncated);
                    contentStream.endText();
                }
                
                if (j < cells.size() - 1) {
                    contentStream.moveTo(MARGIN + ((j + 1) * colWidth), yPosition);
                    contentStream.lineTo(MARGIN + ((j + 1) * colWidth), yPosition - rowHeight);
                    contentStream.stroke();
                }
            }
            
            yPosition -= rowHeight;
        }
        
        return yPosition - 10;
    }

    private List<String> wrapText(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            float width = font.getStringWidth(testLine) / 1000 * fontSize;
            
            if (width > maxWidth && currentLine.length() > 0) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }
        
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        
        return lines;
    }

    private String truncateText(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        if (text == null || text.isEmpty()) return "";
        
        float width = font.getStringWidth(text) / 1000 * fontSize;
        if (width <= maxWidth) return text;
        
        String ellipsis = "...";
        for (int i = text.length() - 1; i > 0; i--) {
            String truncated = text.substring(0, i) + ellipsis;
            width = font.getStringWidth(truncated) / 1000 * fontSize;
            if (width <= maxWidth) {
                return truncated;
            }
        }
        
        return ellipsis;
    }

    public String getOutputFileName(File inputFile) {
        String fileName = inputFile.getName();
        String nameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));
        return nameWithoutExt + "_converted.pdf";
    }
}
