import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";

import { BetaCenter } from "./BetaCenter";
import { SpacesCenter } from "./SpacesCenter";

vi.mock("next/navigation", () => ({ usePathname: () => "/app/espacos", useRouter: () => ({ push: vi.fn() }) }));

const spaces = [{ id: "11111111-1111-1111-1111-111111111111", type: "PERSONAL", name: "Meu planejamento",
  baseCurrency: "BRL", role: "OWNER", memberCount: 1, profileConfigured: true }];
const invitation = { id: "22222222-2222-2222-2222-222222222222", spaceId: "33333333-3333-3333-3333-333333333333",
  spaceName: "Plano do casal", inviterName: "Pessoa sintética", role: "EDITOR", status: "PENDING",
  expiresAt: "2026-09-04T12:00:00Z", createdAt: "2026-08-28T12:00:00Z" };

describe("colaboração e beta", () => {
  afterEach(() => {
    vi.restoreAllMocks();
    window.localStorage.clear();
    document.cookie = "XSRF-TOKEN=; Max-Age=0; path=/";
  });

  it("explica o escopo do convite e aceita somente por ação explícita", async () => {
    document.cookie = "XSRF-TOKEN=csrf-space; path=/";
    const fetchMock = vi.spyOn(globalThis, "fetch").mockImplementation(async (input, init) => {
      const url = String(input);
      if (url.endsWith("/planning-spaces")) return jsonResponse(spaces);
      if (url.endsWith("/planning-space-invitations")) return jsonResponse([invitation]);
      if (url.includes("/responses") && init?.method === "POST") return new Response(null, { status: 204 });
      if (url.endsWith("/auth/csrf")) return new Response(null, { status: 200 });
      if (url.endsWith("/beta/events")) return new Response(null, { status: 202 });
      throw new Error(`URL inesperada: ${url}`);
    });

    render(<SpacesCenter />);
    expect(await screen.findByRole("heading", { name: "Antes de entrar, revise o escopo." })).toBeInTheDocument();
    expect(screen.getByText(/Valores pessoais não são copiados automaticamente/)).toBeInTheDocument();
    fireEvent.click(screen.getByRole("button", { name: "Aceitar convite para Plano do casal" }));
    await waitFor(() => expect(screen.queryByText("Plano do casal")).not.toBeInTheDocument());
    const response = fetchMock.mock.calls.find(([url]) => String(url).includes("/responses"));
    expect(JSON.parse(String(response?.[1]?.body))).toEqual({ accept: true });
  });

  it("mantém o feedback fechado quando o gate técnico está desligado", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(jsonResponse({ enabled: false, feedbackEnabled: false }));
    render(<BetaCenter />);
    expect(await screen.findByText("Entrada de participantes bloqueada")).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Enviar feedback" })).not.toBeInTheDocument();
    expect(screen.getByText(/revisão jurídica/i)).toBeInTheDocument();
  });
});

function jsonResponse(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), { status, headers: { "Content-Type": "application/json" } });
}
