import { fireEvent, render, screen } from "@testing-library/react";
import { beforeEach, describe, expect, it } from "vitest";

import { ThemeSwitcher } from "./ThemeSwitcher";

describe("ThemeSwitcher", () => {
  beforeEach(() => {
    window.localStorage.clear();
    document.documentElement.removeAttribute("data-theme");
  });

  it("inverte e persiste o tema escolhido", () => {
    document.documentElement.dataset.theme = "light";
    render(<ThemeSwitcher />);

    const button = screen.getByRole("button", {
      name: "Inverter tema claro e escuro",
    });
    fireEvent.click(button);

    expect(document.documentElement).toHaveAttribute("data-theme", "dark");
    expect(window.localStorage.getItem("financetarget-theme")).toBe("dark");

    fireEvent.click(button);
    expect(document.documentElement).toHaveAttribute("data-theme", "light");
  });
});
