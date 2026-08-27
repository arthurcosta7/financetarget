import { AuthShell } from "@/components/AuthShell";
import { IdentityForm } from "@/components/IdentityForms";

export default async function RedefinirSenhaPage({ searchParams }: { searchParams: Promise<{ token?: string }> }) {
  const { token } = await searchParams;
  return <AuthShell eyebrow="Nova credencial" title="Uma nova chave para o seu plano."
    intro="Defina uma senha longa. Após a mudança, todas as sessões anteriores serão encerradas."
    aside={<p>Frases longas são bem-vindas. Não exigimos trocas periódicas arbitrárias.</p>}>
    <IdentityForm initialToken={token} mode="reset-password" />
  </AuthShell>;
}
