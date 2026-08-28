"use client";

import {
  ArrowLeftFromLine,
  ArrowRightFromLine,
  CircleUserRound,
  CreditCard,
  Goal,
  House,
  LogOut,
  Menu,
  Plus,
  SlidersHorizontal,
  Target,
  X,
} from "lucide-react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useState } from "react";

import { ThemeSwitcher } from "@/components/ThemeSwitcher";
import { apiFetch } from "@/lib/api/client";

const navigation = [
  {
    label: "Planejamento",
    items: [
      { href: "/app/inicio", label: "Visão geral", icon: House },
      { href: "/app/metas", label: "Metas", icon: Target },
      { href: "/app/metas/nova", label: "Nova meta", icon: Plus },
    ],
  },
  {
    label: "Preferências",
    items: [
      { href: "/app/plano", label: "Plano", icon: CreditCard },
      { href: "/app/onboarding", label: "Perfil financeiro", icon: SlidersHorizontal },
      { href: "/app/conta", label: "Conta e privacidade", icon: CircleUserRound },
    ],
  },
] as const;

export function AppShell({ section, children }: { section: string; children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [collapsed, setCollapsed] = useState(false);

  useEffect(() => {
    function closeOnEscape(event: KeyboardEvent) {
      if (event.key === "Escape") setMobileOpen(false);
    }
    window.addEventListener("keydown", closeOnEscape);
    return () => window.removeEventListener("keydown", closeOnEscape);
  }, []);

  async function logout() {
    try {
      await apiFetch("/auth/sessions/current", { method: "DELETE" });
    } finally {
      router.push("/entrar");
    }
  }

  function isCurrent(href: string) {
    if (href === "/app/inicio") return pathname === href;
    if (href === "/app/metas") return pathname.startsWith(href) && pathname !== "/app/metas/nova";
    return pathname.startsWith(href);
  }

  return (
    <main className={`app-shell${collapsed ? " app-shell--collapsed" : ""}`} id="conteudo-principal">
      <button
        aria-controls="app-sidebar"
        aria-expanded={mobileOpen}
        aria-label="Abrir navegação"
        className="app-mobile-menu"
        onClick={() => setMobileOpen(true)}
        type="button"
      >
        <Menu aria-hidden="true" size={20} strokeWidth={1.7} />
      </button>

      {mobileOpen && <button aria-label="Fechar navegação" className="app-sidebar-backdrop" onClick={() => setMobileOpen(false)} type="button" />}

      <aside className={`app-sidebar${mobileOpen ? " app-sidebar--open" : ""}`} id="app-sidebar">
        <div className="app-sidebar__brand">
          <Link className="app-sidebar__wordmark" href="/app/inicio" aria-label="FinanceTarget, visão geral">
            <Goal aria-hidden="true" size={20} strokeWidth={1.7} />
            <span>FinanceTarget.</span>
          </Link>
          <button aria-label="Fechar navegação" className="app-sidebar__mobile-close" onClick={() => setMobileOpen(false)} type="button">
            <X aria-hidden="true" size={20} />
          </button>
        </div>

        <div className="app-sidebar__context" aria-label="Espaço atual">
          <span className="app-sidebar__context-mark" aria-hidden="true">FT</span>
          <span><small>Espaço atual</small><strong>Meu planejamento</strong></span>
        </div>

        <nav className="app-sidebar__nav" aria-label="Área autenticada">
          {navigation.map((group) => (
            <div className="app-sidebar__group" key={group.label}>
              <p>{group.label}</p>
              {group.items.map(({ href, label, icon: Icon }) => (
                <Link aria-current={isCurrent(href) ? "page" : undefined} href={href} key={href} onClick={() => setMobileOpen(false)} title={collapsed ? label : undefined}>
                  <Icon aria-hidden="true" size={19} strokeWidth={1.65} />
                  <span>{label}</span>
                </Link>
              ))}
            </div>
          ))}
        </nav>

        <div className="app-sidebar__footer">
          <ThemeSwitcher />
          <button className="app-sidebar__logout" onClick={logout} type="button">
            <LogOut aria-hidden="true" size={18} strokeWidth={1.65} />
            <span>Sair</span>
          </button>
          <button
            aria-label={collapsed ? "Expandir navegação" : "Recolher navegação"}
            className="app-sidebar__collapse"
            onClick={() => setCollapsed((current) => !current)}
            type="button"
          >
            {collapsed ? <ArrowRightFromLine aria-hidden="true" size={17} /> : <ArrowLeftFromLine aria-hidden="true" size={17} />}
            <span>Recolher</span>
          </button>
        </div>
      </aside>

      <section className="app-workspace">
        <header className="app-workspace__bar">
          <p><span>FinanceTarget</span><b aria-hidden="true">/</b>{section}</p>
        </header>
        <div className="app-page">{children}</div>
      </section>
    </main>
  );
}
