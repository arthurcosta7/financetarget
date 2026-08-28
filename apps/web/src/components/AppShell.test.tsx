import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";

import { AppShell } from "./AppShell";

vi.mock("next/navigation", () => ({
  usePathname: () => "/app/metas/nova",
  useRouter: () => ({ push: vi.fn() }),
}));

describe("shell autenticado", () => {
  it("marca somente a rota específica como atual", () => {
    render(<AppShell section="Nova meta"><h1>Conteúdo</h1></AppShell>);

    expect(screen.getByRole("link", { name: "Nova meta" })).toHaveAttribute("aria-current", "page");
    expect(screen.getByRole("link", { name: /^Metas$/ })).not.toHaveAttribute("aria-current");
  });

  it("expõe controles nomeados para recolher e abrir a navegação", () => {
    render(<AppShell section="Nova meta"><h1>Conteúdo</h1></AppShell>);

    const mobileButton = screen.getByRole("button", { name: "Abrir navegação" });
    expect(mobileButton).toHaveAttribute("aria-expanded", "false");
    fireEvent.click(mobileButton);
    expect(mobileButton).toHaveAttribute("aria-expanded", "true");

    fireEvent.click(screen.getByRole("button", { name: "Recolher navegação" }));
    expect(screen.getByRole("button", { name: "Expandir navegação" })).toBeInTheDocument();
  });
});
