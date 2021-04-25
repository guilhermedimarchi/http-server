package com.gui.http.models;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class FileExplorerHtml {

    private final String html;

    public FileExplorerHtml(File file, String rootPath) throws IOException {
        StringBuilder html = new StringBuilder();
        String pathWithoutRoot = file.getPath().replace(rootPath, "");
        html.append("<html>");
        html.append("<head><title>Index of .");
        html.append(pathWithoutRoot);
        html.append("</title>");
        html.append("</head>");
        html.append("<body>");
        html.append("<h1>Index of .");
        html.append(pathWithoutRoot);
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
            } else {
                html.append(attr.size());
            }
            html.append(" bytes");
            html.append("</pre>");
        }
        html.append("</body>");
        html.append("</html>");
        this.html = html.toString();
    }

    public byte[] getHtmlBytes() {
        return html.getBytes();
    }

    private long folderSize(File directory) throws IOException {
        FileVisitor total = new FileVisitor();
        Files.walkFileTree(directory.toPath(), total);
        return total.getSize();
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
