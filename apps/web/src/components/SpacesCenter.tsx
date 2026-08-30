"use client";

import { Check, Plus, UserPlus, X } from "lucide-react";
import { useEffect, useState } from "react";

import { AppShell } from "@/components/AppShell";
import { FormMessage } from "@/components/FormMessage";
import { ApiError, apiFetch } from "@/lib/api/client";
import type { FinancialProfile } from "@/lib/goals";
import { recordBetaEvent, type SpaceInvitation, type SpaceMember, usePlanningSpaces } from "@/lib/spaces";

export function SpacesCenter() {
  const { spaces, activeSpace, error, refresh, selectSpace } = usePlanningSpaces();
  const [invitations, setInvitations] = useState<SpaceInvitation[]>([]);
  const [members, setMembers] = useState<SpaceMember[]>([]);
  const [profile, setProfile] = useState<FinancialProfile>();
  const [message, setMessage] = useState<{ kind: "error" | "success"; text: string }>();
  const [pending, setPending] = useState(false);
  const [memberLimit, setMemberLimit] = useState<number>();

  useEffect(() => {
    apiFetch<SpaceInvitation[]>("/planning-space-invitations").then(setInvitations).catch(() => undefined);
    apiFetch<{ maximumSharedMembers: number }>("/beta/config")
      .then((config) => setMemberLimit(config.maximumSharedMembers)).catch(() => undefined);
  }, []);

  useEffect(() => {
    if (!activeSpace || activeSpace.type !== "SHARED") return;
    Promise.all([
      apiFetch<SpaceMember[]>(`/planning-spaces/${activeSpace.id}/members`),
      apiFetch<FinancialProfile>(`/planning-spaces/${activeSpace.id}/financial-profile`),
    ]).then(([loadedMembers, loadedProfile]) => {
      setMembers(loadedMembers);
      setProfile(loadedProfile);
    }).catch((cause) => setMessage({ kind: "error", text: cause instanceof Error ? cause.message : "Não foi possível carregar o espaço." }));
  }, [activeSpace]);

  async function createSpace(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPending(true);
    const form = event.currentTarget;
    try {
      const created = await apiFetch<{ id: string }>("/planning-spaces", {
        method: "POST", body: JSON.stringify({ name: new FormData(form).get("name") }),
      });
      await refresh();
      selectSpace(created.id);
      form.reset();
      setMessage({ kind: "success", text: "Espaço compartilhado criado. Configure os valores agregados antes de criar metas." });
      void recordBetaEvent("SPACE_CREATED", "COLLABORATION");
    } catch (cause) {
      setMessage({ kind: "error", text: cause instanceof ApiError ? cause.message : "Não foi possível criar o espaço." });
    } finally { setPending(false); }
  }

  async function invite(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!activeSpace) return;
    setPending(true);
    const form = event.currentTarget;
    const data = new FormData(form);
    try {
      await apiFetch(`/planning-spaces/${activeSpace.id}/invitations`, {
        method: "POST", body: JSON.stringify({ email: data.get("email"), role: data.get("role") }),
      });
      form.reset();
      setMessage({ kind: "success", text: "Convite criado. A pessoa verá o convite ao entrar com o e-mail informado." });
    } catch (cause) {
      setMessage({ kind: "error", text: cause instanceof ApiError ? cause.message : "Não foi possível criar o convite." });
    } finally { setPending(false); }
  }

  async function respond(invitationId: string, accept: boolean) {
    setPending(true);
    try {
      await apiFetch(`/planning-space-invitations/${invitationId}/responses`, {
        method: "POST", body: JSON.stringify({ accept }),
      });
      setInvitations((current) => current.filter((item) => item.id !== invitationId));
      await refresh();
      setMessage({ kind: "success", text: accept ? "Convite aceito. O espaço já está disponível." : "Convite recusado." });
      if (accept) void recordBetaEvent("INVITATION_ACCEPTED", "COLLABORATION");
    } catch (cause) {
      setMessage({ kind: "error", text: cause instanceof ApiError ? cause.message : "Não foi possível responder ao convite." });
    } finally { setPending(false); }
  }

  async function saveProfile(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!activeSpace) return;
    setPending(true);
    const data = new FormData(event.currentTarget);
    const decimal = (name: string) => String(data.get(name) ?? "").replace(",", ".");
    try {
      const saved = await apiFetch<FinancialProfile>(`/planning-spaces/${activeSpace.id}/financial-profile`, {
        method: "PUT", body: JSON.stringify({ recurringIncome: decimal("recurringIncome"),
          essentialExpenses: decimal("essentialExpenses"), initialGoalBalance: decimal("initialGoalBalance"),
          confirmedMonthlyCapacity: decimal("confirmedMonthlyCapacity") }),
      });
      setProfile(saved);
      await refresh();
      setMessage({ kind: "success", text: "Ponto de partida compartilhado atualizado." });
    } catch (cause) {
      setMessage({ kind: "error", text: cause instanceof ApiError ? cause.message : "Não foi possível salvar o perfil compartilhado." });
    } finally { setPending(false); }
  }

  return <AppShell activeSpace={activeSpace} onSpaceChange={selectSpace} section="Espaços" spaces={spaces}>
    <header className="app-heading shared-heading">
      <p className="eyebrow">Planejamento a dois</p>
      <h1>Compartilhe a meta, não a conta.</h1>
      <p>Cada pessoa mantém sua identidade. Somente valores informados dentro do espaço passam a fazer parte do plano compartilhado.</p>
    </header>

    {(message || error) && <FormMessage kind={message?.kind ?? "error"}>{message?.text ?? error?.message}</FormMessage>}

    {invitations.length > 0 && <section className="shared-invitations" aria-labelledby="pending-invitations">
      <div><p className="eyebrow">Convites recebidos</p><h2 id="pending-invitations">Antes de entrar, revise o escopo.</h2></div>
      {invitations.map((invitation) => <article key={invitation.id}>
        <div><strong>{invitation.spaceName}</strong><p>{invitation.inviterName} convidou você como {roleCopy(invitation.role)}. Valores pessoais não são copiados automaticamente.</p></div>
        <div><button aria-label={`Recusar convite para ${invitation.spaceName}`} className="icon-action" disabled={pending} onClick={() => respond(invitation.id, false)} type="button"><X aria-hidden="true" /></button>
          <button aria-label={`Aceitar convite para ${invitation.spaceName}`} className="icon-action icon-action--primary" disabled={pending} onClick={() => respond(invitation.id, true)} type="button"><Check aria-hidden="true" /></button></div>
      </article>)}
    </section>}

    <div className="shared-layout">
      <section className="shared-main">
        <div className="shared-section-heading"><div><p className="eyebrow">Espaços disponíveis</p><h2>Onde o plano acontece</h2></div><span>{spaces?.length ?? 0}</span></div>
        <div className="space-list">{spaces?.map((space) => <button aria-pressed={activeSpace?.id === space.id} key={space.id} onClick={() => selectSpace(space.id)} type="button">
          <span className="space-list__mark">{space.type === "PERSONAL" ? "01" : "02"}</span><span><strong>{space.name}</strong><small>{space.type === "PERSONAL" ? "Pessoal" : `${space.memberCount} ${space.memberCount === 1 ? "participante" : "participantes"}`} · {roleCopy(space.role)}</small></span>
        </button>)}</div>

        {activeSpace?.type === "SHARED" && <>
          <div className="shared-section-heading shared-section-heading--spaced"><div><p className="eyebrow">Participantes</p><h2>Papéis visíveis</h2></div><span>{members.length}</span></div>
          <div className="member-list">{members.map((member) => <div key={member.userId}><span aria-hidden="true">{member.displayName.slice(0, 1).toUpperCase()}</span><div><strong>{member.displayName}</strong><small>{roleCopy(member.role)}</small></div></div>)}</div>

          <form className="shared-profile form-stack" onSubmit={saveProfile}>
            <div><p className="eyebrow">Valores do espaço</p><h2>Ponto de partida agregado</h2><p>Informe somente o que vocês decidiram colocar no plano conjunto.</p></div>
            <div className="field-row"><MoneyField defaultValue={profile?.recurringIncome} label="Renda recorrente conjunta" name="recurringIncome" /><MoneyField defaultValue={profile?.essentialExpenses} label="Despesas essenciais conjuntas" name="essentialExpenses" /></div>
            <div className="field-row"><MoneyField defaultValue={profile?.initialGoalBalance} label="Saldo destinado às metas" name="initialGoalBalance" /><MoneyField defaultValue={profile?.confirmedMonthlyCapacity} label="Capacidade mensal confirmada" name="confirmedMonthlyCapacity" /></div>
            <button className="button button--primary" disabled={pending || activeSpace.role === "VIEWER"} type="submit">Salvar valores do espaço</button>
          </form>
        </>}
      </section>

      <aside className="shared-actions">
        <form className="form-stack" onSubmit={createSpace}><p className="eyebrow">Novo espaço</p><label className="field"><span>Nome do planejamento</span><input maxLength={80} minLength={2} name="name" placeholder="Ex.: Plano da casa" required /></label><button className="button button--quiet" disabled={pending} type="submit"><Plus aria-hidden="true" size={17} /> Criar espaço</button></form>
        {activeSpace?.type === "SHARED" && activeSpace.role === "OWNER" && <form className="form-stack shared-invite-form" onSubmit={invite}><p className="eyebrow">Convidar uma pessoa</p><label className="field"><span>E-mail</span><input autoComplete="email" name="email" required type="email" /></label><label className="field"><span>Papel inicial</span><select defaultValue="EDITOR" name="role"><option value="OWNER">Proprietário</option><option value="EDITOR">Editor</option><option value="VIEWER">Leitor</option></select></label><button className="button button--primary" disabled={pending || (memberLimit !== undefined && activeSpace.memberCount >= memberLimit)} type="submit"><UserPlus aria-hidden="true" size={17} /> Criar convite</button>{memberLimit !== undefined && <small>Durante o beta, o limite configurado é de {memberLimit} pessoas.</small>}</form>}
      </aside>
    </div>
  </AppShell>;
}

function MoneyField({ defaultValue, label, name }: { defaultValue?: string; label: string; name: string }) {
  return <label className="field"><span>{label}</span><span className="money-input"><b>BRL</b><input defaultValue={defaultValue ?? "0.00"} inputMode="decimal" name={name} pattern="[0-9]+([.,][0-9]{1,2})?" required /></span></label>;
}

function roleCopy(role: "OWNER" | "EDITOR" | "VIEWER") {
  return { OWNER: "Proprietário", EDITOR: "Editor", VIEWER: "Leitor" }[role];
}
