package com.gdtw.general.util;

import java.net.URI;
import java.net.URISyntaxException;

public class UrlNormalizerUtil {

    private UrlNormalizerUtil() {}

    public static String normalizeUrl(String url) throws URISyntaxException {
        // Parse the URL
        URI uri = new URI(url);

        // Convert the host (domain name) to lowercase
        String host = uri.getHost();
        if (host != null) {
            host = host.toLowerCase();
        }

        // Process the path by removing redundant slashes
        String path = uri.getPath();
        if (path != null && path.length() > 1) {
            path = path.replaceAll("/+", "/");  // Replace multiple "/" with a single "/"
        } else if (path == null || path.isEmpty()) {
            path = "/";
        }

        // Rebuild the URI, removing default ports
        int port = uri.getPort();
        if ((port == 80 && "http".equals(uri.getScheme())) || (port == 443 && "https".equals(uri.getScheme()))) {
            port = -1;  // Default port, no need to specify
        }

        // Remove the fragment (the part after '#')
        String query = uri.getQuery();  // Keep query parameters
        URI normalizedUri = new URI(uri.getScheme(), uri.getUserInfo(), host, port, path, query, null);

        // Convert the URI back to a string
        return normalizedUri.toString();
    }

}
