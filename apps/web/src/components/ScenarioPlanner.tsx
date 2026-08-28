"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

import { AppShell } from "@/components/AppShell";
import { FormMessage } from "@/components/FormMessage";
import { ApiError, apiFetch } from "@/lib/api/client";
import { formatDate, formatMoney, type FinancialProfile, type Goal, type ScenarioComparison } from "@/lib/goals";

export function ScenarioPlanner({ goalId }: { goalId: string }) {
  const router = useRouter();
  const [goal, setGoal] = useState<Goal>();
  const [comparison, setComparison] = useState<ScenarioComparison>();
  const [pending, setPending] = useState(false);
  const [message, setMessage] = useState<{ kind: "error" | "success"; text: string }>();

  useEffect(() => {
    apiFetch<FinancialProfile>("/onboarding/financial-profile").then(async (profile) => {
      const [loadedGoal, loadedComparison] = await Promise.all([
        apiFetch<Goal>(`/planning-spaces/${profile.spaceId}/goals/${goalId}`),
        apiFetch<ScenarioComparison>(`/planning-spaces/${profile.spaceId}/goals/${goalId}/scenarios`),
      ]);
      setGoal(loadedGoal);
      setComparison(loadedComparison);
    }).catch((error) => {
      if (error instanceof ApiError && error.status === 401) router.push("/entrar");
      else setMessage({ kind: "error", text: error instanceof Error ? error.message : "Não foi possível carregar os cenários." });
    });
  }, [goalId, router]);

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!goal) return;
    setPending(true);
    setMessage(undefined);
    const form = event.currentTarget;
    const data = new FormData(form);
    const decimal = (name: string) => String(data.get(name) ?? "").replace(",", ".");
    try {
      const result = await apiFetch<ScenarioComparison>(`/planning-spaces/${goal.spaceId}/goals/${goal.id}/scenarios`, {
        method: "POST",
        body: JSON.stringify({ title: data.get("title"), targetDate: data.get("targetDate"),
          annualInflationRate: decimal("annualInflationRate"), annualReturnRate: decimal("annualReturnRate"),
          contributionTiming: data.get("contributionTiming") }),
      });
      setComparison(result);
      form.reset();
      setMessage({ kind: "success", text: "Cenário salvo como um novo snapshot. A base não foi alterada." });
    } catch (error) {
      setMessage({ kind: "error", text: error instanceof ApiError ? error.message : "Não foi possível criar o cenário." });
    } finally { setPending(false); }
  }

  if (!goal || !comparison) return <AppShell section="Cenários"><p className="goal-empty" aria-live="polite">{message?.text ?? "Carregando comparação…"}</p></AppShell>;
  const rows = [{ id: "base", title: "Plano atual", projection: comparison.base, delta: null },
    ...comparison.scenarios.map((scenario) => ({ id: scenario.id, title: scenario.title, projection: scenario.projection, delta: scenario.requiredContributionDelta }))];
  const maxMonths = Math.max(...rows.map((row) => row.projection.projectionMonths));

  return (
    <AppShell section="Metas · cenários">
      <header className="app-heading scenario-heading"><p className="eyebrow">{goal.title}</p><h1>Mude uma hipótese. Veja o impacto.</h1><p>Os cenários não escolhem um caminho por você. Eles mostram diferenças contra o snapshot atual.</p></header>
      <section className="scenario-chart" aria-labelledby="scenario-chart-title">
        <div className="scenario-chart__intro"><p className="eyebrow" id="scenario-chart-title">Prazo comparado</p><small>Quanto mais à direita, maior o prazo calculado.</small></div>
        {rows.map((row) => <div className="scenario-line" key={row.id}>
          <strong>{row.title}</strong><span><i style={{ left: `${(row.projection.projectionMonths / maxMonths) * 100}%` }} /></span><small>{row.projection.projectionMonths} meses</small>
        </div>)}
      </section>
      <div className="scenario-layout">
        <section className="scenario-comparison">
          <div className="table-scroll"><table><caption>Comparação do plano atual com cenários salvos</caption><thead><tr><th scope="col">Cenário</th><th scope="col">Aporte mensal</th><th scope="col">Diferença</th><th scope="col">Meta nominal</th><th scope="col">Prazo</th></tr></thead>
            <tbody>{rows.map((row) => <tr key={row.id}><th scope="row">{row.title}</th><td>{formatMoney(row.projection.requiredMonthlyContribution)}</td><td>{row.delta ? formatMoney(row.delta) : "Base"}</td><td>{formatMoney(row.projection.targetNominal)}</td><td>{row.projection.projectionMonths} meses</td></tr>)}</tbody></table></div>
          <p className="scenario-version">Motor {comparison.scenarioEngineVersion}. Valores negativos na diferença indicam aporte menor que o plano atual.</p>
          <div className="scenario-history"><p className="eyebrow">Histórico imutável</p>{comparison.scenarios.length === 0 && <p>Nenhum cenário salvo.</p>}{comparison.scenarios.map((scenario) => <article key={scenario.id}><h2>{scenario.title}</h2><p>Meta em {formatDate(scenario.targetDate)} · inflação {scenario.annualInflationRate} · retorno hipotético {scenario.annualReturnRate}</p><small>Criado em {new Date(scenario.createdAt).toLocaleDateString("pt-BR")}</small></article>)}</div>
        </section>
        <aside className="scenario-form-panel"><p className="eyebrow">Novo cenário · {comparison.scenarios.length}/3</p>
          <form className="form-stack" onSubmit={submit}>
            <label className="field"><span>Nome do cenário</span><input maxLength={80} minLength={2} name="title" required /></label>
            <label className="field"><span>Nova data da meta</span><input name="targetDate" required type="date" /></label>
            <label className="field"><span>Inflação anual em decimal</span><input inputMode="decimal" name="annualInflationRate" pattern="-?[0-9]+([.,][0-9]{1,8})?" required /></label>
            <label className="field"><span>Retorno anual hipotético em decimal</span><input inputMode="decimal" name="annualReturnRate" pattern="-?[0-9]+([.,][0-9]{1,8})?" required /></label>
            <label className="field"><span>Momento do aporte</span><select defaultValue="END_OF_MONTH" name="contributionTiming"><option value="END_OF_MONTH">No fim do mês</option><option value="BEGINNING_OF_MONTH">No início do mês</option></select></label>
            {message && <FormMessage kind={message.kind}>{message.text}</FormMessage>}
            <button className="button button--primary" disabled={pending || comparison.scenarios.length >= 3} type="submit">{pending ? "Comparando…" : comparison.scenarios.length >= 3 ? "Limite de cenários atingido" : "Salvar e comparar"}</button>
          </form>
        </aside>
      </div>
    </AppShell>
  );
}
