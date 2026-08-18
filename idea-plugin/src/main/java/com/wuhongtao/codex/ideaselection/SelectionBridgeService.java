package com.wuhongtao.codex.ideaselection;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.editor.event.SelectionListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service(Service.Level.APP)
public final class SelectionBridgeService implements Disposable {
    static final int PORT = 47321;

    private static final Logger LOG = Logger.getInstance(SelectionBridgeService.class);

    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicReference<ActiveSelection> currentSelection = new AtomicReference<>();

    private volatile HttpServer server;
    private volatile ExecutorService serverExecutor;

    public void start() {
        if (!started.compareAndSet(false, true)) {
            return;
        }

        EditorFactory.getInstance().getEventMulticaster().addSelectionListener(
                new SelectionListener() {
                    @Override
                    public void selectionChanged(@NotNull SelectionEvent event) {
                        capture(event.getEditor());
                    }
                },
                this
        );

        try {
            HttpServer localServer = HttpServer.create(
                    new InetSocketAddress(InetAddress.getByName("127.0.0.1"), PORT),
                    0
            );
            ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
                Thread thread = new Thread(runnable, "codex-idea-selection-bridge");
                thread.setDaemon(true);
                return thread;
            });

            localServer.createContext("/health", this::handleHealth);
            localServer.createContext("/v1/active-selection", this::handleActiveSelection);
            localServer.setExecutor(executor);
            localServer.start();

            server = localServer;
            serverExecutor = executor;
            LOG.info("Codex IDEA Selection Bridge listening on 127.0.0.1:" + PORT);
        } catch (IOException exception) {
            LOG.warn("Could not start Codex IDEA Selection Bridge on port " + PORT, exception);
        }
    }

    private void capture(Editor editor) {
        if (editor.isDisposed()) {
            currentSelection.set(null);
            return;
        }

        Project project = editor.getProject();
        SelectionModel selectionModel = editor.getSelectionModel();
        if (project == null || !selectionModel.hasSelection()) {
            currentSelection.set(null);
            return;
        }

        Document document = editor.getDocument();
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        if (file == null) {
            currentSelection.set(null);
            return;
        }

        int startOffset = selectionModel.getSelectionStart();
        int endOffset = selectionModel.getSelectionEnd();
        int startLine = document.getLineNumber(startOffset) + 1;
        int endLine = document.getLineNumber(Math.max(startOffset, endOffset - 1)) + 1;
        boolean dirty = FileDocumentManager.getInstance().isDocumentUnsaved(document);

        currentSelection.set(new ActiveSelection(
                project.getBasePath() == null ? "" : project.getBasePath(),
                file.getPath(),
                startLine,
                endLine,
                System.currentTimeMillis(),
                dirty
        ));
    }

    private void handleHealth(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            send(exchange, 405, JsonResponses.error("METHOD_NOT_ALLOWED", "Only GET is supported."));
            return;
        }
        send(exchange, 200, JsonResponses.health());
    }

    private void handleActiveSelection(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            send(exchange, 405, JsonResponses.error("METHOD_NOT_ALLOWED", "Only GET is supported."));
            return;
        }

        ActiveSelection selection = currentSelection.get();
        if (selection == null) {
            send(exchange, 200, JsonResponses.error(
                    "NO_SELECTION",
                    "Select code in IntelliJ IDEA, then try again."
            ));
            return;
        }

        if (selection.dirty()) {
            send(exchange, 200, JsonResponses.fileError(
                    "FILE_NOT_SAVED",
                    "Save the selected file in IntelliJ IDEA, reselect the code, then try again.",
                    selection.filePath()
            ));
            return;
        }

        send(exchange, 200, JsonResponses.success(selection));
    }

    private static void send(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(statusCode, body.length);
        try (var responseBody = exchange.getResponseBody()) {
            responseBody.write(body);
        }
    }

    @Override
    public void dispose() {
        HttpServer localServer = server;
        if (localServer != null) {
            localServer.stop(0);
        }

        ExecutorService executor = serverExecutor;
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}
