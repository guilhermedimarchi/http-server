package com.gui.http.handlers;

import com.gui.http.models.Request;
import com.gui.http.models.Response;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import static com.gui.http.HttpStatus.*;


public class StaticHandler implements HttpHandler {

    private  String rootPath;

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
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<head><title>Index of ");
        sb.append(file.getPath().replace(rootPath, ""));
        sb.append("</title>");
        sb.append("</head>");
        sb.append("<body>");
        sb.append("<h1>Index of ");
        sb.append(file.getPath());
        sb.append("</h1>");
        sb.append("<pre>Name | Last modified | Size</pre><hr/>");

        Path path = Paths.get(file.getPath());
        if(path.getNameCount() > 1) {
            if (path.getNameCount() == 2) {
                sb.append("<pre><a href=\"../\">../</a>\n");
            } else {
                sb.append("<pre><a href=\"");
                sb.append(path.getParent().toString().replace(rootPath, ""));
                sb.append("\">../</a>\n");
            }
        }

        for (File sub : file.listFiles()) {
            String filePath = sub.getPath().replace(rootPath, "");
            BasicFileAttributes attr = Files.readAttributes(Paths.get(sub.getPath()), BasicFileAttributes.class);
            sb.append("<pre><a href=\"");
            sb.append(filePath);
            sb.append("\">");
            sb.append(sub.getName());
            sb.append("</a> | ");
            sb.append(attr.lastModifiedTime());
            sb.append(" | ");
            if(sub.isDirectory())
                sb.append(folderSize(sub));
            else
                sb.append(attr.size());
            sb.append(" bytes");
            sb.append("</pre>");
        }
        sb.append("</body>");
        sb.append("</html>");
        return sb.toString().getBytes();
    }

    private long folderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }

    private boolean methodNotImplemented(Request request) {
        return !"GET".equals(request.getMethod()) && !"HEAD".equals(request.getMethod());
    }
}
