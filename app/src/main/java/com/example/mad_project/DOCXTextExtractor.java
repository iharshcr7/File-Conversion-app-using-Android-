package com.example.mad_project;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DOCXTextExtractor {
    
    public static String extractTextFromDOCX(File docxFile) throws IOException {
        StringBuilder text = new StringBuilder();
        
        try {
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(docxFile));
            ZipEntry entry;
            
            // Look for word/document.xml in the ZIP file
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.getName().equals("word/document.xml")) {
                    // Read the XML content
                    ByteArrayOutputStream xmlContent = new ByteArrayOutputStream();
                    byte[] buffer = new byte[8192];
                    int length;
                    while ((length = zipInputStream.read(buffer)) != -1) {
                        xmlContent.write(buffer, 0, length);
                    }
                    
                    String xmlString = new String(xmlContent.toByteArray(), StandardCharsets.UTF_8);
                    
                    // Extract text from <w:t> tags
                    int startIndex = 0;
                    while (true) {
                        int openTag = xmlString.indexOf("<w:t", startIndex);
                        if (openTag == -1) {
                            // Try without namespace
                            openTag = xmlString.indexOf("<t", startIndex);
                            if (openTag == -1 || openTag > xmlString.length() - 10) break;
                            
                            // Check if it's actually a text tag (not other tags)
                            int tagEnd = xmlString.indexOf(">", openTag);
                            if (tagEnd == -1) break;
                            String tagName = xmlString.substring(openTag, tagEnd);
                            if (!tagName.contains("w:t") && !tagName.equals("<t>")) {
                                startIndex = tagEnd + 1;
                                continue;
                            }
                            openTag = tagEnd + 1;
                        } else {
                            // Find end of opening tag
                            int tagEnd = xmlString.indexOf(">", openTag);
                            if (tagEnd == -1) break;
                            openTag = tagEnd + 1;
                        }
                        
                        // Find closing tag
                        int closeTag = xmlString.indexOf("</w:t>", openTag);
                        if (closeTag == -1) {
                            closeTag = xmlString.indexOf("</t>", openTag);
                            if (closeTag == -1) {
                                closeTag = xmlString.indexOf("<", openTag);
                                if (closeTag == -1) closeTag = xmlString.length();
                            }
                        }
                        
                        if (closeTag > openTag) {
                            String extracted = xmlString.substring(openTag, closeTag);
                            
                            // Decode XML entities
                            extracted = extracted.replace("&amp;", "&")
                                               .replace("&lt;", "<")
                                               .replace("&gt;", ">")
                                               .replace("&quot;", "\"")
                                               .replace("&apos;", "'")
                                               .replace("&#10;", "\n")
                                               .replace("&#13;", "\r");
                            
                            if (!extracted.trim().isEmpty()) {
                                text.append(extracted.trim());
                                
                                // Check for paragraph breaks
                                int nextPara = xmlString.indexOf("<w:p", closeTag);
                                int nextBreak = xmlString.indexOf("<w:br", closeTag);
                                if ((nextPara != -1 && nextPara < closeTag + 100) || 
                                    (nextBreak != -1 && nextBreak < closeTag + 50)) {
                                    text.append("\n");
                                } else {
                                    text.append(" ");
                                }
                            }
                        }
                        startIndex = closeTag + 6;
                    }
                    
                    zipInputStream.closeEntry();
                    break;
                }
                zipInputStream.closeEntry();
            }
            
            zipInputStream.close();
            
        } catch (Exception e) {
            throw new IOException("Error extracting text from DOCX: " + e.getMessage());
        }
        
        String result = text.toString().trim();
        // Clean up multiple spaces and newlines
        result = result.replaceAll(" +", " ").replaceAll("\n +", "\n").replaceAll("\n{3,}", "\n\n");
        
        return result.length() > 0 ? result : "Unable to extract text from this document.";
    }
}

