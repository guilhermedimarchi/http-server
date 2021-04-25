package com.gui.http.handlers;

import com.gui.http.models.Request;
import com.gui.http.models.Response;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.gui.http.HttpStatus.*;


public class StaticHandler implements HttpHandler {

    private String rootPath;

    public StaticHandler(String rootPath) {
        this.rootPath = rootPath;
    }

    @Override
    public Response handle(Request request) throws IOException {
        if (methodNotImplemented(request))
            return new Response(NOT_IMPLEMENTED);

        File file = new File(rootPath + request.getPath());
        if (!file.exists())
            return new Response(NOT_FOUND);


        byte[] body;
        Map<String, String> headers = new HashMap<>();
        if (file.isDirectory()) {
            body = fileExplorerHtml(file);
            headers.put("Content-Type", "text/html");
        } else {
            body = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
            headers.put("Content-Type", Files.probeContentType(Paths.get(file.getAbsolutePath())));
        }
        headers.put("Content-Length", "" + body.length);

        if ("HEAD".equals(request.getMethod()))
            return new Response(OK, null, headers);
        else
            return new Response(OK, body, headers);
    }

    private byte[] fileExplorerHtml(File file) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append("<html>");
        html.append("<head><title>Index of ");
        html.append(file.getPath().replace(rootPath, ""));
        html.append("</title>");
        html.append("</head>");
        html.append("<body>");
        html.append("<h1>Index of ");
        html.append(file.getPath());
        html.append("</h1>");
        html.append("<pre>Name | Last modified | Size</pre><hr/>");

        Path path = Paths.get(file.getPath());
        if (path.getNameCount() > 1) {
            if (path.getNameCount() == 2) {
                html.append("<pre><a href=\"../\">../</a>\n");
            } else {
                html.append("<pre><a href=\"");
                html.append(path.getParent().toString().replace(rootPath, ""));
                html.append("\">../</a>\n");
            }
        }

        for (File nestedFile : file.listFiles()) {
            String filePath = nestedFile.getPath().replace(rootPath, "");
            BasicFileAttributes attr = Files.readAttributes(nestedFile.toPath(), BasicFileAttributes.class);
            html.append("<pre><a href=\"");
            html.append(filePath);
            html.append("\">");
            html.append(nestedFile.getName());
            html.append("</a> | ");
            html.append(attr.lastModifiedTime());
            html.append(" | ");
            if (nestedFile.isDirectory()) {
                html.append(folderSize(nestedFile));
            }else {
                html.append(attr.size());
            }
            html.append(" bytes");
            html.append("</pre>");
        }
        html.append("</body>");
        html.append("</html>");
        return html.toString().getBytes();
    }

    private long folderSize(File directory) throws IOException {
        FileVisitor total = new FileVisitor();
        Files.walkFileTree(directory.toPath(), total);
        return total.getSize();
    }

    private boolean methodNotImplemented(Request request) {
        return !"GET".equals(request.getMethod()) && !"HEAD".equals(request.getMethod());
    }

    private class FileVisitor extends SimpleFileVisitor<Path> {
        private long size;

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            this.size += attrs.size();
            return FileVisitResult.CONTINUE;
        }
        public long getSize() {
            return size;
        }
    }
}
