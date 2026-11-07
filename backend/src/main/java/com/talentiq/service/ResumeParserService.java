package com.talentiq.service;

import com.talentiq.exception.BadRequestException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class ResumeParserService {

    public String extractText(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded resume file is empty");
        }

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        String contentType = file.getContentType() != null ? file.getContentType().toLowerCase() : "";

        boolean isPdf = originalFilename.endsWith(".pdf") || contentType.contains("pdf");
        boolean isText = originalFilename.endsWith(".txt") || originalFilename.endsWith(".rtf") || contentType.contains("text/plain");

        if (!isPdf && !isText) {
            throw new BadRequestException("Unsupported file type. Please upload a PDF (.pdf) or text (.txt) document.");
        }

        if (isPdf) {
            return extractTextFromPdf(file);
        } else {
            try {
                return new String(file.getBytes(), StandardCharsets.UTF_8).trim();
            } catch (IOException e) {
                throw new BadRequestException("Failed to read text file: " + e.getMessage());
            }
        }
    }

    private String extractTextFromPdf(MultipartFile file) {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            if (document.isEncrypted()) {
                throw new BadRequestException("Uploaded PDF resume is password protected.");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String rawText = stripper.getText(document);
            if (rawText == null || rawText.trim().isEmpty()) {
                throw new BadRequestException("Could not extract any readable text from the PDF. It may be scanned or empty.");
            }
            return cleanExtractedText(rawText);
        } catch (IOException e) {
            throw new BadRequestException("Failed to parse PDF document: " + e.getMessage());
        }
    }

    private String cleanExtractedText(String text) {
        return text.replaceAll("\r\n", "\n")
                   .replaceAll("\r", "\n")
                   .replaceAll("[ \t]+", " ")
                   .replaceAll("\n{3,}", "\n\n")
                   .trim();
    }
}
