import { render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";

import { ApiStatus } from "./ApiStatus";

describe("ApiStatus", () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("informa quando a URL da API não foi configurada", () => {
    render(<ApiStatus />);

    expect(
      screen.getByText("URL da API não configurada neste ambiente."),
    ).toBeVisible();
  });

  it("confirma a conexão usando o contrato da API", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response(
        JSON.stringify({
          status: "UP",
          database: { status: "UP", schemaVersion: "1" },
        }),
        { status: 200, headers: { "Content-Type": "application/json" } },
      ),
    );

    render(<ApiStatus apiBaseUrl="http://api.example.test" />);

    expect(
      await screen.findByText("Web, API e PostgreSQL conectados"),
    ).toBeVisible();
    expect(screen.getByText("schema 1")).toBeVisible();
    expect(globalThis.fetch).toHaveBeenCalledWith(
      "http://api.example.test/api/v1/system/status",
      expect.objectContaining({ signal: expect.any(AbortSignal) }),
    );
  });

  it("não expõe detalhes técnicos quando a API falha", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response("internal detail", { status: 500 }),
    );

    render(<ApiStatus apiBaseUrl="http://api.example.test" />);

    expect(
      await screen.findByText(
        "API ou banco indisponível. Verifique o ambiente local.",
      ),
    ).toBeVisible();
    expect(screen.queryByText("internal detail")).not.toBeInTheDocument();
  });
});
