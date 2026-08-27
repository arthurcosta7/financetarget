"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";

import { apiFetch } from "@/lib/api/client";

export function AppShell({ section, children }: { section: string; children: React.ReactNode }) {
  const router = useRouter();
  async function logout() {
    try {
      await apiFetch("/auth/sessions/current", { method: "DELETE" });
    } finally {
      router.push("/entrar");
    }
  }

  return (
    <main className="app-page" id="conteudo-principal">
      <nav className="app-nav" aria-label="Área autenticada">
        <p className="eyebrow">{section}</p>
        <div>
          <Link href="/app/onboarding">Perfil financeiro</Link>
          <Link href="/app/conta">Conta e privacidade</Link>
          <button onClick={logout} type="button">Sair</button>
        </div>
      </nav>
      {children}
    </main>
  );
}
