package com.example.mad_project;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class PDFTextExtractor {
    
    public static String extractTextFromPDF(File pdfFile) throws IOException {
        StringBuilder extractedText = new StringBuilder();
        
        try {
            ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);
            
            int pageCount = pdfRenderer.getPageCount();
            
            // For now, we'll use text extraction from PDF bytes since PdfRenderer doesn't extract text directly
            // We'll read the PDF file bytes and extract text
            fileDescriptor.close();
            pdfRenderer.close();
            
            // Read PDF file and extract text using byte analysis
            FileInputStream fis = new FileInputStream(pdfFile);
            byte[] pdfBytes = new byte[(int) pdfFile.length()];
            fis.read(pdfBytes);
            fis.close();
            
            String pdfContent = new String(pdfBytes, StandardCharsets.ISO_8859_1);
            
            // Extract text between BT (Begin Text) and ET (End Text) markers
            int startIndex = 0;
            int btCount = 0;
            while (true) {
                int btIndex = pdfContent.indexOf("BT", startIndex);
                if (btIndex == -1) break;
                
                int etIndex = pdfContent.indexOf("ET", btIndex);
                if (etIndex == -1) break;
                
                String textSection = pdfContent.substring(btIndex + 2, etIndex);
                
                // Extract text in parentheses (literal strings) - most common format
                int parenStart = 0;
                while (true) {
                    int openParen = textSection.indexOf("(", parenStart);
                    if (openParen == -1) break;
                    
                    int closeParen = findMatchingParen(textSection, openParen);
                    if (closeParen == -1) {
                        // Try simple find if no nested parens
                        closeParen = textSection.indexOf(")", openParen);
                        if (closeParen == -1) break;
                    }
                    
                    String text = textSection.substring(openParen + 1, closeParen);
                    
                    // Handle escape sequences in PDF strings
                    text = text.replace("\\n", "\n")
                              .replace("\\r", "\r")
                              .replace("\\t", "\t")
                              .replace("\\(", "(")
                              .replace("\\)", ")");
                    
                    // Filter out pure numbers, coordinates, and very short strings
                    if (text.length() > 0 && 
                        !text.matches("^[\\d\\s\\.\\-\\+]+$") && 
                        text.matches(".*[a-zA-Z].*")) {
                        extractedText.append(text);
                        // Add space if text doesn't end with punctuation or newline
                        if (!text.matches(".*[.!?\\n]$")) {
                            extractedText.append(" ");
                        }
                    }
                    
                    parenStart = closeParen + 1;
                }
                
                // Also extract hex strings <hex> (alternative encoding)
                int hexStart = 0;
                while (true) {
                    int openHex = textSection.indexOf("<", hexStart);
                    if (openHex == -1 || openHex == textSection.length() - 1) break;
                    
                    // Check if it looks like a hex string (even length hex digits)
                    int closeHex = textSection.indexOf(">", openHex);
                    if (closeHex == -1) break;
                    
                    String hexString = textSection.substring(openHex + 1, closeHex);
                    if (hexString.length() > 0 && hexString.length() % 2 == 0 && 
                        hexString.length() < 200 && hexString.matches("[0-9A-Fa-f]+")) {
                        try {
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
                            String decodedText = decoded.toString();
                            if (decodedText.length() > 1 && decodedText.matches(".*[a-zA-Z].*")) {
                                extractedText.append(decodedText).append(" ");
                            }
                        } catch (Exception e) {
                            // Ignore hex conversion errors
                        }
                    }
                    
                    hexStart = closeHex + 1;
                }
                
                startIndex = etIndex + 2;
                btCount++;
            }
            
            // If no text found in BT/ET blocks, try extracting readable ASCII sequences
            if (extractedText.length() == 0) {
                StringBuilder word = new StringBuilder();
                for (int i = 0; i < pdfContent.length(); i++) {
                    char c = pdfContent.charAt(i);
                    if (c >= 32 && c <= 126 && 
                        (Character.isLetterOrDigit(c) || Character.isWhitespace(c) || 
                         c == '.' || c == ',' || c == '!' || c == '?' || c == ':' || 
                         c == ';' || c == '-' || c == '\'' || c == '"')) {
                        word.append(c);
                    } else if (word.length() > 3) {
                        extractedText.append(word.toString()).append(" ");
                        word.setLength(0);
                    } else {
                        word.setLength(0);
                    }
                }
                if (word.length() > 3) {
                    extractedText.append(word.toString());
                }
            }
            
        } catch (Exception e) {
            throw new IOException("Error extracting text from PDF: " + e.getMessage());
        }
        
        String result = extractedText.toString().trim();
        return result.length() > 0 ? result : "Unable to extract text from this PDF. It may be a scanned document or image-based PDF.";
    }
    
    private static int findMatchingParen(String text, int openIndex) {
        int depth = 1;
        for (int i = openIndex + 1; i < text.length(); i++) {
            if (text.charAt(i) == '(') {
                depth++;
            } else if (text.charAt(i) == ')') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }
}

