package com.gdtw.cssjsminify.service;

import com.gdtw.general.service.ratelimiter.RateLimiterService;
import com.google.javascript.jscomp.*;
import com.helger.css.ECSSVersion;
import com.helger.css.decl.CascadingStyleSheet;
import com.helger.css.reader.CSSReader;
import com.yahoo.platform.yui.compressor.CssCompressor;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CssJsMinifyService {

    private static final DecimalFormat FORMAT = new DecimalFormat("#,##0.#");

    public Map<String, String> autoDetectAndMinify(String source) {
        String normalized = normalize(source);
        Map<String, String> response = new HashMap<>();

        String jsMinified = tryMinifyJs(normalized);
        if (jsMinified != null) {
            response.put("type", "JS");
            response.put("result", jsMinified);
            response.put("status", buildStatus("JavaScript", normalized, jsMinified));
            return response;
        }

        if (isValidCssStrict(normalized)) {
            String cssMinified = minifyCss(normalized);
            response.put("type", "CSS");
            response.put("result", cssMinified);
            response.put("status", buildStatus("CSS", normalized, cssMinified));
            return response;
        }

        response.put("type", "ERROR");
        response.put("error", "Content is not valid CSS or JavaScript.");
        return response;
    }

    private String tryMinifyJs(String jsCode) {
        try {
            Compiler compiler = new Compiler();
            CompilerOptions options = new CompilerOptions();
            CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);

            SourceFile input = SourceFile.fromCode("input.js", jsCode);
            Result result = compiler.compile(List.of(), List.of(input), options);

            return result.success ? compiler.toSource() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isValidCssStrict(String cssCode) {
        CascadingStyleSheet css = CSSReader.readFromString(cssCode, ECSSVersion.CSS30);
        return css != null &&
                (!css.getAllStyleRules().isEmpty()
                        || !css.getAllMediaRules().isEmpty()
                        || !css.getAllFontFaceRules().isEmpty()
                        || !css.getAllPageRules().isEmpty()
                        || !css.getAllViewportRules().isEmpty()
                        || !css.getAllUnknownRules().isEmpty());
    }

    private String minifyCss(String cssCode) {
        try {
            StringReader reader = new StringReader(cssCode);
            StringWriter writer = new StringWriter();
            CssCompressor compressor = new CssCompressor(reader);
            compressor.compress(writer, -1);
            return writer.toString();
        } catch (Exception e) {
            return cssCode;
        }
    }

    private String buildStatus(String type, String original, String compressed) {
        long originalBytes = original.getBytes().length;
        long compressedBytes = compressed.getBytes().length;
        return String.format("Type: %s  |  Input: %s  |  Output: %s  |  Saved: %.1f%%",
                type,
                formatSize(originalBytes),
                formatSize(compressedBytes),
                calculateSavingPercent(originalBytes, compressedBytes));
    }

    private String normalize(String input) {
        return input.replaceAll("\r\n?", "\n").trim();
    }

    private String formatSize(long bytes) {
        return bytes >= 1024
                ? FORMAT.format(bytes / 1024.0) + " KB"
                : FORMAT.format(bytes) + " bytes";
    }

    private double calculateSavingPercent(long original, long compressed) {
        return (original == 0) ? 0 : ((original - compressed) * 100.0) / original;
    }

}
