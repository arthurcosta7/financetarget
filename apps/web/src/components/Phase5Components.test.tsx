import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";

import { Dashboard } from "./Dashboard";
import { ScenarioPlanner } from "./ScenarioPlanner";
import type { Goal, ScenarioComparison } from "@/lib/goals";

const push = vi.fn();
vi.mock("next/navigation", () => ({ usePathname: () => "/app/inicio", useRouter: () => ({ push }) }));

const profile = { spaceId: "11111111-1111-1111-1111-111111111111", initialGoalBalance: "10000.00", confirmedMonthlyCapacity: "2000.00", currency: "BRL" };
const goal: Goal = {
  id: "22222222-2222-2222-2222-222222222222", spaceId: profile.spaceId, goalType: "TRAVEL", title: "Viagem longa",
  targetAmount: { amount: "60000.00", currency: "BRL" }, targetValueBasis: "FIXED_NOMINAL", targetDate: "2029-08-28",
  initialBalance: { amount: "10000.00", currency: "BRL" }, annualInflationRate: "0.00", annualReturnRate: "0.00",
  contributionTiming: "END_OF_MONTH", status: "ACTIVE", version: 0, currentBalance: { amount: "10000.00", currency: "BRL" },
  remainingAmount: { amount: "50000.00", currency: "BRL" }, progressPercentage: "16.67", contributions: [], createdAt: "2026-08-28T12:00:00Z",
  projection: { targetNominal: { amount: "60000.00", currency: "BRL" }, requiredMonthlyContribution: { amount: "1388.89", currency: "BRL" },
    projectedValueAtTarget: { amount: "60000.00", currency: "BRL" }, estimatedCompletionDate: "2029-08-28", totalContributed: { amount: "60000.00", currency: "BRL" },
    projectedGrowth: { amount: "0.00", currency: "BRL" }, shortfallOrSurplus: { amount: "0.00", currency: "BRL" }, projectionMonths: 36,
    warnings: ["PROJECTION_NOT_GUARANTEE"], engineVersion: "goal-engine-1", formulaVersion: "monthly-annuity-1" },
};
const comparison: ScenarioComparison = { base: goal.projection, scenarioEngineVersion: "scenario-engine-1", scenarios: [{
  id: "33333333-3333-3333-3333-333333333333", title: "Mais tempo", targetDate: "2030-08-28", annualInflationRate: "0.00",
  annualReturnRate: "0.00", contributionTiming: "END_OF_MONTH", projection: { ...goal.projection, projectionMonths: 48,
    requiredMonthlyContribution: { amount: "1041.67", currency: "BRL" } }, requiredContributionDelta: { amount: "-347.22", currency: "BRL" },
  targetNominalDelta: { amount: "0.00", currency: "BRL" }, projectionMonthsDelta: 12, createdAt: "2026-08-28T13:00:00Z",
}] };

describe("dashboard e cenários", () => {
  afterEach(() => { vi.restoreAllMocks(); push.mockReset(); document.cookie = "XSRF-TOKEN=; Max-Age=0; path=/"; });

  it("mostra múltiplas metas usando o progresso calculado pelo backend", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValueOnce(jsonResponse(profile)).mockResolvedValueOnce(jsonResponse([goal]));
    render(<Dashboard />);
    expect(await screen.findByRole("heading", { name: "Progresso por meta" })).toBeInTheDocument();
    expect(screen.getByLabelText("Comparação do percentual concluído de cada meta")).toBeInTheDocument();
    expect(screen.getAllByText("Viagem longa")).toHaveLength(3);
    expect(screen.getByText("16.67%")).toBeInTheDocument();
    expect(screen.getByText(/Viagem · agosto de 2029/)).toBeInTheDocument();
  });

  it("compara a base, exibe delta e envia somente premissas declaradas", async () => {
    document.cookie = "XSRF-TOKEN=csrf-scenario; path=/";
    const fetchMock = vi.spyOn(globalThis, "fetch").mockImplementation(async (input, init) => {
      const url = String(input);
      if (url.endsWith("/onboarding/financial-profile")) return jsonResponse(profile);
      if (url.endsWith(`/goals/${goal.id}`)) return jsonResponse(goal);
      if (url.endsWith(`/goals/${goal.id}/scenarios`) && init?.method === "POST") return jsonResponse(comparison, 201);
      if (url.endsWith(`/goals/${goal.id}/scenarios`)) return jsonResponse(comparison);
      if (url.endsWith("/auth/csrf")) return new Response(null, { status: 200 });
      throw new Error(`URL inesperada: ${url}`);
    });
    render(<ScenarioPlanner goalId={goal.id} />);
    expect(await screen.findByRole("table", { name: /Comparação do plano atual/ })).toBeInTheDocument();
    expect(screen.getByText("−BRL 347,22")).toBeInTheDocument();
    fireEvent.change(screen.getByLabelText("Nome do cenário"), { target: { value: "Prazo alternativo" } });
    fireEvent.change(screen.getByLabelText("Nova data da meta"), { target: { value: "2031-08-28" } });
    fireEvent.change(screen.getByLabelText("Inflação anual em decimal"), { target: { value: "0,04" } });
    fireEvent.change(screen.getByLabelText("Retorno anual hipotético em decimal"), { target: { value: "0,05" } });
    fireEvent.click(screen.getByRole("button", { name: "Salvar e comparar" }));
    await waitFor(() => expect(fetchMock.mock.calls.some(([, init]) => init?.method === "POST")).toBe(true));
    const request = fetchMock.mock.calls.find(([, init]) => init?.method === "POST")?.[1];
    expect(JSON.parse(String(request?.body))).toEqual({ title: "Prazo alternativo", targetDate: "2031-08-28",
      annualInflationRate: "0.04", annualReturnRate: "0.05", contributionTiming: "END_OF_MONTH" });
  });
});

function jsonResponse(body: unknown, status = 200) { return new Response(JSON.stringify(body), { status, headers: { "Content-Type": "application/json" } }); }
