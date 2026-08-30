"use client";

import { useCallback, useEffect, useState } from "react";

import { ApiError, apiFetch } from "@/lib/api/client";

const STORAGE_KEY = "financetarget-active-space";

export type PlanningSpace = {
  id: string;
  type: "PERSONAL" | "SHARED";
  name: string;
  baseCurrency: string;
  role: "OWNER" | "EDITOR" | "VIEWER";
  memberCount: number;
  profileConfigured: boolean;
};

export type SpaceInvitation = {
  id: string;
  spaceId: string;
  spaceName: string;
  inviterName: string;
  role: PlanningSpace["role"];
  status: string;
  expiresAt: string;
  createdAt: string;
};

export type SpaceMember = {
  userId: string;
  displayName: string;
  role: PlanningSpace["role"];
  joinedAt: string;
};

export function usePlanningSpaces() {
  const [spaces, setSpaces] = useState<PlanningSpace[]>();
  const [activeSpace, setActiveSpace] = useState<PlanningSpace>();
  const [error, setError] = useState<ApiError | Error>();

  const refresh = useCallback(async () => {
    try {
      const loaded = await apiFetch<PlanningSpace[]>("/planning-spaces");
      setSpaces(loaded);
      const saved = window.localStorage.getItem(STORAGE_KEY);
      const selected = loaded.find((space) => space.id === saved) ?? loaded[0];
      setActiveSpace(selected);
      if (selected) window.localStorage.setItem(STORAGE_KEY, selected.id);
      setError(undefined);
      return loaded;
    } catch (cause) {
      setError(cause instanceof Error ? cause : new Error("Não foi possível carregar os espaços."));
      return [];
    }
  }, []);

  useEffect(() => {
    apiFetch<PlanningSpace[]>("/planning-spaces").then((loaded) => {
      setSpaces(loaded);
      const saved = window.localStorage.getItem(STORAGE_KEY);
      const selected = loaded.find((space) => space.id === saved) ?? loaded[0];
      setActiveSpace(selected);
      if (selected) window.localStorage.setItem(STORAGE_KEY, selected.id);
    }).catch((cause) => setError(cause instanceof Error ? cause : new Error("Não foi possível carregar os espaços.")));
  }, []);

  function selectSpace(id: string) {
    const selected = spaces?.find((space) => space.id === id);
    window.localStorage.setItem(STORAGE_KEY, id);
    if (selected) setActiveSpace(selected);
    else void refresh();
  }

  return { spaces, activeSpace, error, refresh, selectSpace };
}

export function deviceClass(): "MOBILE" | "TABLET" | "DESKTOP" | "UNKNOWN" {
  if (typeof window === "undefined") return "UNKNOWN";
  if (window.innerWidth < 640) return "MOBILE";
  if (window.innerWidth < 1024) return "TABLET";
  return "DESKTOP";
}

export async function recordBetaEvent(eventName: string, journeyStage: string, outcome = "COMPLETED") {
  try {
    await apiFetch("/beta/events", {
      method: "POST",
      body: JSON.stringify({ eventName, journeyStage, outcome, deviceClass: deviceClass() }),
    });
  } catch {
    // Aprendizado nunca bloqueia a jornada principal.
  }
}
