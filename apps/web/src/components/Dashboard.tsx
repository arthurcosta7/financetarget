"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

import { AppShell } from "@/components/AppShell";
import { ApiError, apiFetch } from "@/lib/api/client";
import { formatDate, formatMoney, goalTypeCopy, type FinancialProfile, type Goal } from "@/lib/goals";

export function Dashboard() {
  const router = useRouter();
  const [goals, setGoals] = useState<Goal[]>();
  const [message, setMessage] = useState<string>();

  useEffect(() => {
    apiFetch<FinancialProfile>("/onboarding/financial-profile")
      .then((profile) => apiFetch<Goal[]>(`/planning-spaces/${profile.spaceId}/goals`))
      .then(setGoals)
      .catch((error) => {
        if (error instanceof ApiError && error.status === 401) router.push("/entrar");
        else if (error instanceof ApiError && error.status === 404) router.push("/app/onboarding");
        else setMessage(error instanceof Error ? error.message : "Não foi possível carregar a visão geral.");
      });
  }, [router]);

  return (
    <AppShell section="Visão geral">
      <header className="app-heading dashboard-heading">
        <p className="eyebrow">Horizonte atual</p>
        <h1>Seus planos, sem esconder as premissas.</h1>
        <p>A visão reúne resultados calculados no servidor. Cada meta continua independente e revisável.</p>
      </header>
      {message && <p className="goal-empty">{message}</p>}
      {goals && <section className="dashboard-index" aria-label="Resumo das metas">
        <div><span>Metas ativas</span><strong>{goals.length.toString().padStart(2, "0")}</strong></div>
        <div><span>Com aportes</span><strong>{goals.filter((goal) => goal.contributions.length > 0).length.toString().padStart(2, "0")}</strong></div>
        <Link className="button button--primary" href="/app/metas/nova">Adicionar meta</Link>
      </section>}
      <section className="dashboard-goals" aria-label="Trajetórias das metas">
        {goals?.map((goal, index) => <article key={goal.id}>
          <div className="dashboard-goal-number">{String(index + 1).padStart(2, "0")}</div>
          <div><p className="eyebrow">{goalTypeCopy[goal.goalType]}</p><h2>{goal.title}</h2><p>Até {formatDate(goal.targetDate)} · {formatMoney(goal.projection.requiredMonthlyContribution)} por mês</p></div>
          <div className="dashboard-progress" aria-label={`${goal.progressPercentage}% concluído`}>
            <span style={{ "--goal-progress": `${goal.progressPercentage}%` } as React.CSSProperties} />
            <small>{goal.progressPercentage}%</small>
          </div>
          <Link className="text-link" href={`/app/metas/${goal.id}`}>Abrir plano</Link>
        </article>)}
        {goals?.length === 0 && <div className="goal-empty"><p>Nenhuma meta ativa ainda.</p><Link className="text-link" href="/app/metas/nova">Criar a primeira</Link></div>}
        {!goals && !message && <p className="goal-empty" aria-live="polite">Organizando o horizonte…</p>}
      </section>
    </AppShell>
  );
}
