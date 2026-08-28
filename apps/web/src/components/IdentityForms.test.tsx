import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";

import { IdentityForm } from "./IdentityForms";

const push = vi.fn();
vi.mock("next/navigation", () => ({ usePathname: () => "/entrar", useRouter: () => ({ push }) }));

describe("IdentityForm", () => {
  afterEach(() => {
    vi.restoreAllMocks();
    push.mockReset();
    document.cookie = "XSRF-TOKEN=; Max-Age=0; path=/";
  });

  it("mantém o cadastro acessível e exibe a resposta neutra da API", async () => {
    document.cookie = "XSRF-TOKEN=csrf-sintetico; path=/";
    const fetchMock = vi.spyOn(globalThis, "fetch")
      .mockResolvedValueOnce(new Response(null, { status: 200 }))
      .mockResolvedValueOnce(new Response(JSON.stringify({
        message: "Se o endereço puder ser cadastrado, enviaremos as próximas instruções.",
      }), { status: 202, headers: { "Content-Type": "application/json" } }));

    render(<IdentityForm mode="register" />);
    fireEvent.change(screen.getByLabelText("Como quer ser chamado"), { target: { value: "Ana" } });
    fireEvent.change(screen.getByLabelText("E-mail"), { target: { value: "ana@example.test" } });
    fireEvent.change(screen.getByLabelText(/^Senha/), { target: { value: "uma senha longa e segura" } });
    fireEvent.click(screen.getByRole("button", { name: "Criar minha conta" }));

    expect(await screen.findByRole("status")).toHaveTextContent("Se o endereço puder ser cadastrado");
    await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(2));
    expect(fetchMock.mock.calls[1]?.[1]).toEqual(expect.objectContaining({
      method: "POST",
      credentials: "include",
      headers: expect.objectContaining({ "X-XSRF-TOKEN": "csrf-sintetico" }),
    }));
  });

  it("não navega e mostra uma falha de credenciais sem detalhe interno", async () => {
    document.cookie = "XSRF-TOKEN=csrf-sintetico; path=/";
    vi.spyOn(globalThis, "fetch")
      .mockResolvedValueOnce(new Response(null, { status: 200 }))
      .mockResolvedValueOnce(new Response(JSON.stringify({
        title: "INVALID_CREDENTIALS", status: 401, detail: "E-mail ou senha inválidos.",
      }), { status: 401, headers: { "Content-Type": "application/problem+json" } }));

    render(<IdentityForm mode="login" />);
    fireEvent.change(screen.getByLabelText("E-mail"), { target: { value: "ana@example.test" } });
    fireEvent.change(screen.getByLabelText(/^Senha/), { target: { value: "senha incorreta" } });
    fireEvent.click(screen.getByRole("button", { name: "Entrar" }));

    expect(await screen.findByRole("alert")).toHaveTextContent("E-mail ou senha inválidos.");
    expect(push).not.toHaveBeenCalled();
  });

  it("solicita novo link de verificação com resposta neutra", async () => {
    document.cookie = "XSRF-TOKEN=csrf-sintetico; path=/";
    const fetchMock = vi.spyOn(globalThis, "fetch")
      .mockResolvedValueOnce(new Response(null, { status: 200 }))
      .mockResolvedValueOnce(new Response(JSON.stringify({
        message: "Se existir uma conta pendente, enviaremos uma nova mensagem de verificação.",
      }), { status: 202, headers: { "Content-Type": "application/json" } }));

    render(<IdentityForm mode="request-verification" />);
    fireEvent.change(screen.getByLabelText("E-mail"), { target: { value: "ana@example.test" } });
    fireEvent.click(screen.getByRole("button", { name: "Reenviar verificação" }));

    expect(await screen.findByRole("status")).toHaveTextContent("Se existir uma conta pendente");
    expect(fetchMock.mock.calls[1]?.[0]).toContain("/api/v1/auth/verification-requests");
  });
});
