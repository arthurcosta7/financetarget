import Link from "next/link";

export function AuthShell({
  eyebrow,
  title,
  intro,
  children,
  aside,
}: {
  eyebrow: string;
  title: string;
  intro: string;
  children: React.ReactNode;
  aside: React.ReactNode;
}) {
  return (
    <main className="auth-page" id="conteudo-principal">
      <section className="auth-copy" aria-labelledby="auth-title">
        <Link className="back-link" href="/">← Voltar</Link>
        <div>
          <p className="eyebrow">{eyebrow}</p>
          <h1 id="auth-title">{title}</h1>
          <p className="auth-copy__intro">{intro}</p>
        </div>
        <div className="auth-aside">{aside}</div>
      </section>
      <section className="auth-form-panel" aria-label={title}>
        {children}
      </section>
    </main>
  );
}
