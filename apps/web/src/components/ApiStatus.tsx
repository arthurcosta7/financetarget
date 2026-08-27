"use client";

import { useEffect, useState } from "react";

import type { components } from "@/lib/api/generated/schema";

type StatusState =
  | { kind: "loading" }
  | { kind: "ready"; schemaVersion: string }
  | { kind: "unavailable"; message: string };

type SystemStatusResponse = components["schemas"]["SystemStatus"];

type ApiStatusProps = {
  apiBaseUrl?: string;
};

export function ApiStatus({
  apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL,
}: ApiStatusProps) {
  const [status, setStatus] = useState<StatusState>(
    apiBaseUrl
      ? { kind: "loading" }
      : {
          kind: "unavailable",
          message: "URL da API não configurada neste ambiente.",
        },
  );

  useEffect(() => {
    if (!apiBaseUrl) {
      return;
    }

    const controller = new AbortController();

    async function loadStatus() {
      try {
        const response = await fetch(`${apiBaseUrl}/api/v1/system/status`, {
          signal: controller.signal,
        });

        if (!response.ok) {
          throw new Error(`HTTP ${response.status}`);
        }

        const payload = (await response.json()) as SystemStatusResponse;
        setStatus({ kind: "ready", schemaVersion: payload.database.schemaVersion });
      } catch (error) {
        if (error instanceof DOMException && error.name === "AbortError") {
          return;
        }

        setStatus({
          kind: "unavailable",
          message: "API ou banco indisponível. Verifique o ambiente local.",
        });
      }
    }

    void loadStatus();
    return () => controller.abort();
  }, [apiBaseUrl]);

  if (status.kind === "loading") {
    return <p role="status">Verificando a fundação técnica…</p>;
  }

  if (status.kind === "unavailable") {
    return <p className="status-message status-message--warning">{status.message}</p>;
  }

  return (
    <div className="status-message status-message--ready" role="status">
      <span aria-hidden="true" className="status-message__dot" />
      <span>Web, API e PostgreSQL conectados</span>
      <code>schema {status.schemaVersion}</code>
    </div>
  );
}
