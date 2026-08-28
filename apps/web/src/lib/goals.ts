export type Money = { amount: string; currency: string };

export type GoalProjection = {
  targetNominal: Money;
  requiredMonthlyContribution: Money;
  projectedValueAtTarget: Money;
  estimatedCompletionDate: string;
  totalContributed: Money;
  projectedGrowth: Money;
  shortfallOrSurplus: Money;
  projectionMonths: number;
  warnings: string[];
  engineVersion: string;
  formulaVersion: string;
};

export type Contribution = {
  id: string;
  amount: Money;
  contributionDate: string;
  note?: string | null;
  createdAt: string;
};

export type Goal = {
  id: string;
  spaceId: string;
  goalType: "HOME_DOWN_PAYMENT";
  title: string;
  targetAmount: Money;
  targetValueBasis: "CURRENT_VALUE" | "FIXED_NOMINAL";
  targetDate: string;
  initialBalance: Money;
  annualInflationRate: string;
  annualReturnRate: string;
  contributionTiming: "END_OF_MONTH" | "BEGINNING_OF_MONTH";
  status: "ACTIVE" | "ARCHIVED";
  version: number;
  projection: GoalProjection;
  currentBalance: Money;
  remainingAmount: Money;
  progressPercentage: string;
  contributions: Contribution[];
  createdAt: string;
};

export type FinancialProfile = {
  spaceId: string;
  initialGoalBalance: string;
  confirmedMonthlyCapacity: string;
  currency: string;
};

export function formatMoney(money: Money): string {
  const negative = money.amount.startsWith("-");
  const unsigned = negative ? money.amount.slice(1) : money.amount;
  const [integer, fraction = "00"] = unsigned.split(".");
  const grouped = integer.replace(/\B(?=(\d{3})+(?!\d))/g, ".");
  return `${negative ? "−" : ""}${money.currency} ${grouped},${fraction.padEnd(2, "0").slice(0, 2)}`;
}

export function formatDate(value: string): string {
  return new Intl.DateTimeFormat("pt-BR", { month: "long", year: "numeric", timeZone: "UTC" })
    .format(new Date(`${value}T12:00:00Z`));
}

export const warningCopy: Record<string, string> = {
  TARGET_ALREADY_FUNDED: "O saldo informado já cobre o valor projetado da meta.",
  NEGATIVE_RETURN_ASSUMPTION: "A projeção usa uma hipótese de retorno anual negativa.",
  INFLATION_NOT_INCLUDED: "A inflação não foi incluída nesta projeção.",
  FEES_NOT_INCLUDED: "Custos e tarifas não estão incluídos.",
  TAXES_NOT_INCLUDED: "Impostos não estão incluídos.",
  CONTRIBUTION_EXCEEDS_DECLARED_CAPACITY: "O aporte estimado supera a capacidade mensal declarada no perfil.",
  PROJECTION_NOT_GUARANTEE: "Esta projeção organiza premissas; não garante rendimento nem a realização da meta.",
};
