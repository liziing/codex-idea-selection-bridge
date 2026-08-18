#!/usr/bin/env node

const endpoint = process.env.IDEA_SELECTION_BRIDGE_URL
  ?? "http://127.0.0.1:47321/v1/active-selection";

function output(payload) {
  process.stdout.write(`${JSON.stringify(payload)}\n`);
}

try {
  const response = await fetch(endpoint, {
    method: "GET",
    headers: { Accept: "application/json" },
    signal: AbortSignal.timeout(2000),
  });

  if (!response.ok) {
    output({
      ok: false,
      error: "IDEA_BRIDGE_HTTP_ERROR",
      message: `IDEA bridge returned HTTP ${response.status}.`,
    });
    process.exit(0);
  }

  const payload = await response.json();
  output(payload);
} catch (error) {
  output({
    ok: false,
    error: "IDEA_BRIDGE_UNAVAILABLE",
    message: "Could not connect to the IDEA selection bridge on 127.0.0.1:47321.",
  });
}
