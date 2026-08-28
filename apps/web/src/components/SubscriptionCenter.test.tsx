import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";

import { SubscriptionCenter } from "./SubscriptionCenter";

const push = vi.fn();
vi.mock("next/navigation", () => ({ useRouter: () => ({ push }) }));

const overview = { subscription: null, entitlements: {}, mockCheckoutEnabled: true, availablePlans: [{
  code: "TEST_COMPLETE", displayName: "Completo sintético",
  entitlements: { SCENARIO_LIMIT: "3", SHARED_PLANNING: "enabled" },
}] };
const preferences = { essential: true, planningReminders: false, productUpdates: false, marketing: false };
const features = { paymentsMock: true, notificationsMock: true, openFinance: false, loyalty: false, travel: false,
  realEstateFinancing: false, autoFinancing: false };

describe("plano e comunicações", () => {
  afterEach(() => { vi.restoreAllMocks(); push.mockReset(); document.cookie = "XSRF-TOKEN=; Max-Age=0; path=/"; });

  it("expõe o limite simulado sem apresentar preço ou integração como ativa", async () => {
    vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
      const url = String(input);
      if (url.endsWith("/subscriptions/current")) return response(overview);
      if (url.endsWith("/notification-preferences")) return response(preferences);
      if (url.endsWith("/features")) return response(features);
      throw new Error(`URL inesperada: ${url}`);
    });
    render(<SubscriptionCenter />);
    expect(await screen.findByRole("heading", { name: "Completo sintético" })).toBeInTheDocument();
    expect(screen.getByText("Cenários por meta")).toBeInTheDocument();
    expect(screen.getByText("Open Finance").nextSibling).toHaveTextContent("Desligado");
    expect(screen.queryByText(/R\$/)).not.toBeInTheDocument();
    expect(screen.getByLabelText("Mensagens essenciais")).toBeDisabled();
  });

  it("cria checkout mock e salva somente preferências opcionais", async () => {
    document.cookie = "XSRF-TOKEN=phase6-csrf; path=/";
    const fetchMock = vi.spyOn(globalThis, "fetch").mockImplementation(async (input, init) => {
      const url = String(input);
      if (url.endsWith("/auth/csrf")) return new Response(null, { status: 200 });
      if (url.endsWith("/subscriptions/current")) return response(overview);
      if (url.endsWith("/features")) return response(features);
      if (url.endsWith("/notification-preferences") && init?.method === "PUT") return response({ ...preferences, marketing: true });
      if (url.endsWith("/notification-preferences")) return response(preferences);
      if (url.endsWith("/subscriptions/mock-checkouts") && init?.method === "POST") return response({
        id: "11111111-1111-1111-1111-111111111111", planCode: "TEST_COMPLETE", provider: "MOCK",
        reference: "mock_test", status: "SIMULATED", createdAt: "2026-08-28T12:00:00Z",
      }, 201);
      throw new Error(`URL inesperada: ${url}`);
    });
    render(<SubscriptionCenter />);
    fireEvent.click(await screen.findByRole("button", { name: "Simular esta escolha" }));
    expect(await screen.findByText(/Nenhuma cobrança foi realizada/)).toBeInTheDocument();
    fireEvent.click(screen.getByLabelText("Novidades e pesquisas"));
    fireEvent.click(screen.getByRole("button", { name: "Salvar preferências" }));
    await waitFor(() => expect(fetchMock.mock.calls.filter(([, init]) => init?.method === "POST")).toHaveLength(1));
    const put = fetchMock.mock.calls.find(([, init]) => init?.method === "PUT")?.[1];
    expect(JSON.parse(String(put?.body))).toEqual({ planningReminders: false, productUpdates: false, marketing: true });
  });
});

function response(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), { status, headers: { "Content-Type": "application/json" } });
}
