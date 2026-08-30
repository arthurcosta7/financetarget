"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

import { AppShell } from "@/components/AppShell";
import { ApiError, apiFetch } from "@/lib/api/client";
import { formatDate, formatMoney, goalTypeCopy, type Goal } from "@/lib/goals";
import { usePlanningSpaces } from "@/lib/spaces";

export function GoalList() {
  const router = useRouter();
  const [goals, setGoals] = useState<Goal[]>();
  const [message, setMessage] = useState<string>();
  const { spaces, activeSpace, error: spacesError, selectSpace } = usePlanningSpaces();

  useEffect(() => {
    if (!activeSpace) return;
    apiFetch<Goal[]>(`/planning-spaces/${activeSpace.id}/goals`).then(setGoals)
      .catch((error) => {
        if (error instanceof ApiError && error.status === 401) router.push("/entrar");
        else if (error instanceof ApiError && error.status === 404) router.push("/app/onboarding");
        else setMessage(error instanceof Error ? error.message : "Não foi possível carregar as metas.");
      });
  }, [activeSpace, router]);

  return (
    <AppShell activeSpace={activeSpace} onSpaceChange={selectSpace} section="Metas" spaces={spaces}>
      <header className="app-heading goal-list-heading">
        <div><p className="eyebrow">Planos ativos</p><h1>Uma direção de cada vez.</h1></div>
        <Link className="button button--primary" href="/app/metas/nova">Criar meta</Link>
      </header>
      {(message || spacesError) && <p className="goal-empty">{message ?? spacesError?.message}</p>}
      {goals?.length === 0 && <section className="goal-empty"><p>Comece pela meta que mais ocupa sua cabeça hoje.</p><Link className="text-link" href="/app/metas/nova">Criar a primeira meta</Link></section>}
      {goals?.map((goal) => (
        <article className="goal-row" key={goal.id}>
          <div><p className="eyebrow">{goalTypeCopy[goal.goalType]}</p><h2>{goal.title}</h2><p>{formatMoney(goal.currentBalance)} de {formatMoney(goal.projection.targetNominal)}</p></div>
          <div className="goal-row__projection"><span>Estimativa mensal</span><strong>{formatMoney(goal.projection.requiredMonthlyContribution)}</strong><small>até {formatDate(goal.targetDate)}</small></div>
          <Link className="text-link" href={`/app/metas/${goal.id}`}>Ver plano</Link>
        </article>
      ))}
      {!goals && !message && <p className="goal-empty" aria-live="polite">Carregando metas…</p>}
    </AppShell>
  );
}
