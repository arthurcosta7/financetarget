"use client";

export function ThemeSwitcher() {
  function toggleTheme() {
    const currentTheme = document.documentElement.dataset.theme;
    const nextTheme = currentTheme === "dark" ? "light" : "dark";
    document.documentElement.dataset.theme = nextTheme;
    window.localStorage.setItem("financetarget-theme", nextTheme);
  }

  return (
    <button
      aria-label="Inverter tema claro e escuro"
      className="theme-switcher"
      onClick={toggleTheme}
      type="button"
    >
      <span aria-hidden="true" className="theme-switcher__track">
        <span className="theme-switcher__dot" />
      </span>
      <span>Tema</span>
    </button>
  );
}
