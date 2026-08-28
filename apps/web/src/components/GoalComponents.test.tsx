import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";

import { GoalCreateForm } from "./GoalCreateForm";
import { GoalDetail } from "./GoalDetail";
import type { Goal } from "@/lib/goals";

const push = vi.fn();
vi.mock("next/navigation", () => ({ usePathname: () => "/app/metas", useRouter: () => ({ push }) }));

const profile = { spaceId: "11111111-1111-1111-1111-111111111111", initialGoalBalance: "24000.00", confirmedMonthlyCapacity: "2500.00", currency: "BRL" };
const goal: Goal = {
  id: "22222222-2222-2222-2222-222222222222", spaceId: profile.spaceId, goalType: "HOME_DOWN_PAYMENT",
  title: "Entrada do imóvel", targetAmount: { amount: "120000.00", currency: "BRL" },
  targetValueBasis: "FIXED_NOMINAL", targetDate: "2030-08-27",
  initialBalance: { amount: "24000.00", currency: "BRL" }, annualInflationRate: "0.00",
  annualReturnRate: "0.00", contributionTiming: "END_OF_MONTH", status: "ACTIVE", version: 0,
  projection: {
    targetNominal: { amount: "120000.00", currency: "BRL" },
    requiredMonthlyContribution: { amount: "2000.00", currency: "BRL" },
    projectedValueAtTarget: { amount: "120000.00", currency: "BRL" }, estimatedCompletionDate: "2030-08-27",
    totalContributed: { amount: "120000.00", currency: "BRL" }, projectedGrowth: { amount: "0.00", currency: "BRL" },
    shortfallOrSurplus: { amount: "0.00", currency: "BRL" }, projectionMonths: 48,
    warnings: ["INFLATION_NOT_INCLUDED", "PROJECTION_NOT_GUARANTEE"], engineVersion: "goal-engine-1", formulaVersion: "monthly-annuity-1",
  },
  currentBalance: { amount: "24000.00", currency: "BRL" }, remainingAmount: { amount: "96000.00", currency: "BRL" },
  progressPercentage: "20.00", contributions: [], createdAt: "2026-08-27T12:00:00Z",
};

describe("jornada de metas", () => {
  afterEach(() => {
    vi.restoreAllMocks();
    push.mockReset();
    document.cookie = "XSRF-TOKEN=; Max-Age=0; path=/";
  });

  it("mostra projeção, premissas e limitações recebidas do backend", async () => {
    vi.spyOn(globalThis, "fetch")
      .mockResolvedValueOnce(jsonResponse(profile))
      .mockResolvedValueOnce(jsonResponse(goal));

    render(<GoalDetail goalId={goal.id} />);

    expect(await screen.findByRole("heading", { name: "Entrada do imóvel" })).toBeInTheDocument();
    expect(screen.getByText("BRL 2.000,00")).toBeInTheDocument();
    expect(screen.getByText("A inflação não foi incluída nesta projeção.")).toBeInTheDocument();
    expect(screen.getByText(/não garante rendimento/)).toBeInTheDocument();
    expect(screen.getByLabelText("Progresso de 20.00%")).toBeInTheDocument();
  });

  it("envia apenas entradas declaradas e navega para a meta criada", async () => {
    document.cookie = "XSRF-TOKEN=csrf-goal; path=/";
    const fetchMock = vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
      const url = String(input);
      if (url.endsWith("/onboarding/financial-profile")) return jsonResponse(profile);
      if (url.endsWith("/auth/csrf")) return new Response(null, { status: 200 });
      if (url.includes("/planning-spaces/")) return jsonResponse(goal, 201);
      throw new Error(`URL inesperada no teste: ${url}`);
    });

    render(<GoalCreateForm />);
    await screen.findByDisplayValue("24000.00");
    fireEvent.change(screen.getByLabelText(/Valor que pretende alcançar/), { target: { value: "120000,00" } });
    fireEvent.change(screen.getByLabelText("Data da meta"), { target: { value: "2030-08-27" } });
    fireEvent.change(screen.getByLabelText("Inflação em decimal"), { target: { value: "0,04" } });
    fireEvent.change(screen.getByLabelText("Retorno hipotético em decimal"), { target: { value: "0,06" } });
    fireEvent.click(screen.getByRole("button", { name: "Calcular e criar meta" }));

    await waitFor(() => expect(push).toHaveBeenCalledWith(`/app/metas/${goal.id}`));
    const request = fetchMock.mock.calls.find(([url]) => String(url).includes("/planning-spaces/"))?.[1];
    expect(JSON.parse(String(request?.body))).toEqual(expect.objectContaining({
      targetAmount: "120000.00", annualInflationRate: "0.04", annualReturnRate: "0.06",
    }));
  });
});

function jsonResponse(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), { status, headers: { "Content-Type": "application/json" } });
}
