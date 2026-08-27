import Link from "next/link";

import { Trajectory } from "@/components/Trajectory";

export default function Home() {
  return (
    <main id="conteudo-principal">
      <section className="hero" aria-labelledby="hero-title">
        <div className="hero__copy">
          <p className="eyebrow">Planejamento por metas</p>
          <h1 id="hero-title">Um plano claro para o que vem depois.</h1>
          <p className="hero__lead">
            Organize metas pessoais ou compartilhadas, compare caminhos e entenda
            as premissas por trás de cada projeção.
          </p>
          <div className="hero__actions">
            <Link className="button button--primary" href="/cadastro">
              Começar meu plano
            </Link>
            <Link className="text-link" href="/entrar">Já tenho uma conta</Link>
          </div>
        </div>
        <aside className="hero__statement" aria-label="Princípio do produto">
          <span className="hero__number">01</span>
          <p>
            Projeções são possibilidades calculadas,
            <br /> não promessas.
          </p>
        </aside>
      </section>

      <section className="path-section" aria-labelledby="path-title">
        <div className="path-section__heading">
          <p className="eyebrow">Linha de trajetória</p>
          <h2 id="path-title">Do agora até a meta, sem esconder as variáveis.</h2>
        </div>
        <Trajectory />
      </section>

      <section className="principles" aria-label="Princípios da experiência">
        <article>
          <span>01</span>
          <h2>Compreensível</h2>
          <p>Cada valor deve explicar de onde veio e o que pode alterá-lo.</p>
        </article>
        <article>
          <span>02</span>
          <h2>Compartilhável</h2>
          <p>Casais podem construir metas em conjunto, com papéis explícitos.</p>
        </article>
        <article>
          <span>03</span>
          <h2>Reversível</h2>
          <p>Cenários permitem explorar escolhas sem modificar o plano vigente.</p>
        </article>
      </section>
    </main>
  );
}
